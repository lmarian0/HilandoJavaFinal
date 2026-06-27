package tpfinal.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Verificación de los T-invariantes de la red mediante EXPRESIONES REGULARES
 * a partir del archivo de log de transiciones disparadas.
 *
 * T-Invariantes de la red:
 *   IT1 (modo de complejidad media): T0, T1, T2, T3, T4, T11
 *   IT2 (modo de complejidad simple): T0, T1, T5, T6, T11
 *   IT3 (modo de complejidad alta): T0, T1, T7, T8, T9, T10, T11
 *
 * Algoritmo: Reducción recursiva por expresiones regulares.
 * Se busca iterativamente en la traza cruda del log ciclos de vida completos
 * (T0 -> T1 -> bloque de procesamiento -> T11), se los extrae conservando el
 * interleaving intermedio, y se repite hasta que no haya más coincidencias.
 * Si la cadena resultante queda vacía, el log cumple con todos los T-invariantes.
 * Este enfoque valida el invariante completo de punta a punta (entrada, procesamiento
 * y salida), a diferencia de validar cada capa por separado.
 */
public class RELog {

    /** Contenido del archivo de log cargado en memoria, línea por línea. */
    private List<String> logLines;

    public RELog() {
    }

    /**
     * Punto de entrada principal para ejecutar el validador de forma independiente.
     * Permite probar cualquier archivo de log sin necesidad de ejecutar la simulación completa,
     * facilitando el testing manual (editar el log, inyectar errores y re-validar).
     *
     * Modos de uso:
     *   java -cp target/classes tpfinal.utils.RELog random       -> valida log_random.txt
     *   java -cp target/classes tpfinal.utils.RELog priority     -> valida log_priority.txt
     *   java -cp target/classes tpfinal.utils.RELog mi_log.txt   -> valida una ruta arbitraria
     *   java -cp target/classes tpfinal.utils.RELog              -> (sin args) muestra ayuda de uso
     *
     * @param args Primer argumento: "random", "priority" o ruta al archivo de log
     */
    public static void main(String[] args) {
        // Sin argumentos: mostrar ayuda de uso y salir
        if (args.length == 0) {
            System.out.println("Uso: java -cp target/classes tpfinal.utils.RELog <random|priority|ruta_archivo>");
            System.out.println("  random   -> valida log_random.txt");
            System.out.println("  priority -> valida log_priority.txt");
            System.out.println("  <ruta>   -> valida el archivo indicado");
            return;
        }

        // Resolver el nombre del archivo según el argumento recibido
        String arg = args[0].toLowerCase();
        String logFile;
        switch (arg) {
            case "random":
                logFile = "log_random.txt";
                break;
            case "priority":
                logFile = "log_priority.txt";
                break;
            default:
                // Cualquier otro valor se interpreta como ruta directa al archivo
                logFile = args[0];
                break;
        }

        System.out.println("========================================================");
        System.out.println("  Verificador RELog - Validación de T-Invariantes");
        System.out.println("  Archivo: " + logFile);
        System.out.println("========================================================");

        RELog reLog = new RELog();
        reLog.loadLog(logFile);
        reLog.checkInvariant();
    }

    /**
     * Carga el contenido del archivo de log en memoria.
     * 
     * @param filePath Ruta al archivo (ej: "log.txt")
     */
    public void loadLog(String filePath) {
        try {
            this.logLines = Files.readAllLines(Paths.get(filePath));
            System.out.println("Log cargado correctamente. Transiciones: " + logLines.size());
        } catch (IOException e) {
            System.err.println("Error leyendo el archivo de log: " + e.getMessage());
        }
    }

    /**
     * Verifica los T-invariantes a partir del log usando el algoritmo de reducción recursiva.
     * Busca ciclos de vida completos (T0...T11) definidos por los invariantes,
     * los clasifica mediante los grupos de captura de la expresión regular y los remueve
     * conservando las transiciones intercaladas en el medio (interleaving) hasta reducir
     * la cadena a vacío.
     *
     * @return true si la cadena se redujo a vacío (secuencia de invariantes correcta)
     */
    public boolean checkInvariant() {
        if (logLines == null || logLines.isEmpty()) {
            System.err.println("Error: Log no cargado o vacío.");
            return false;
        }

        // Unir todo en una sola cadena sin comas ni saltos de línea
        StringBuilder sb = new StringBuilder();
        for (String line : logLines) {
            String t = line.trim();
            if (!t.isEmpty()) {
                sb.append(t);
            }
        }
        String trace = sb.toString();

        int media = 0;
        int simple = 0;
        int alta = 0;
        int totalMatches = 0;

        // Expresión regular que define la estructura jerárquica de los tres T-invariantes de la red
        Pattern pattern = Pattern.compile("(T0)(.*?)(T1)(?!\\d)(.*?)((T2)(.*?)(T3)(.*?)(T4)|(T5)(.*?)(T6)|(T7)(.*?)(T8)(.*?)(T9)(.*?)(T10))(.*?)(T11)(.*?)");
        String current = trace;
        int matchesInStep;

        do {
            Matcher m = pattern.matcher(current);
            StringBuffer stepSb = new StringBuffer();
            matchesInStep = 0;
            while (m.find()) {
                matchesInStep++;
                // Clasificación exacta usando los grupos de captura correspondientes a cada camino
                if (m.group(14) != null) {
                    alta++;
                } else if (m.group(11) != null) {
                    simple++;
                } else if (m.group(6) != null) {
                    media++;
                }

                // Reconstrucción del interleaving (conserva las transiciones intermedias de los grupos de comodín)
                StringBuilder replacement = new StringBuilder();
                int[] groups = {2, 4, 7, 9, 12, 15, 17, 19, 21, 23};
                for (int g : groups) {
                    String groupVal = m.group(g);
                    if (groupVal != null) {
                        replacement.append(groupVal);
                    }
                }
                m.appendReplacement(stepSb, Matcher.quoteReplacement(replacement.toString()));
            }
            m.appendTail(stepSb);
            current = stepSb.toString();
            totalMatches += matchesInStep;
        } while (matchesInStep > 0);

        System.out.println("--- Invariantes de transición detectados (reducción recursiva) ---");
        System.out.println("IT1 - Complejidad media (T0-T1-T2-T3-T4-T11)  : " + media);
        System.out.println("IT2 - Complejidad simple (T0-T1-T5-T6-T11)    : " + simple);
        System.out.println("IT3 - Complejidad alta (T0-T1-T7-T8-T9-T10-T11): " + alta);
        System.out.println("Coincidencias (ciclos válidos)                : " + totalMatches);

        boolean valid = (current.length() == 0);

        System.out.println("--- Resultado ---");
        if (valid) {
            System.out.println("VALIDACION DE INVARIANTES EXITOSA");
        } else {
            System.out.println("VALIDACION DE INVARIANTES FALLIDA");
            System.out.println("Transiciones restantes: " + current);
        }

        return valid;
    }
}
