package tpfinal.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
 * Algoritmo: reducción recursiva por expresiones regulares, implementada como
 * una aplicación iterativa de la misma reducción hasta alcanzar un punto fijo.
 * Se buscan en la traza normalizada y concatenada ciclos de vida completos
 * (T0 -> T1 -> bloque de procesamiento -> T11), se extraen sus transiciones obligatorias
 * conservando el interleaving, y se repite hasta que no haya más coincidencias.
 * La validación es exitosa cuando se detecta al menos un T-invariante completo
 * y la cadena resultante se reduce a vacío, sin transiciones residuales.
 * Este análisis comprueba la descomposición ordenada de la traza en los tres
 * T-invariantes; no reproduce el marcado de la red ni verifica habilitación o
 * restricciones temporales de los disparos.
 */
public class RELog {

    /** Código de salida para una traza reducida completamente. */
    private static final int EXIT_VALID = 0;
    /** Código de salida para una traza bien formada con residuo. */
    private static final int EXIT_INVALID = 1;
    /** Código de salida para errores de argumentos, lectura o formato. */
    private static final int EXIT_INPUT_ERROR = 2;
    /** Código de salida para fallos inesperados durante la validación. */
    private static final int EXIT_INTERNAL_ERROR = 3;

    /** Reconoce exactamente las transiciones T0 a T11. */
    private static final Pattern TRANSITION_PATTERN =
            Pattern.compile("T(?:1[01]|[0-9])");

    /**
     * Describe los tres T-invariantes y captura en los grupos (.*?) las
     * transiciones intercaladas que deben conservarse durante cada reducción.
     * El lookahead posterior a T1 evita confundirla con T10 o T11.
     */
    private static final Pattern INVARIANT_PATTERN = Pattern.compile(
            "(T0)(.*?)(T1)(?!\\d)(.*?)" +
            "((T2)(.*?)(T3)(.*?)(T4)|" +
            "(T5)(.*?)(T6)|" +
            "(T7)(.*?)(T8)(.*?)(T9)(.*?)(T10))" +
            "(.*?)(T11)(.*?)");

    /**
     * Crea un validador sin estado interno entre ejecuciones.
     */
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
     * Finaliza con código 0 para una traza válida, 1 para una traza inválida,
     * 2 para un error de argumentos, lectura o formato, y 3 para un error interno inesperado.
     *
     * @param args primer argumento: "random", "priority" o ruta al archivo de log
     */
    public static void main(String[] args) {
        int exitCode = new RELog().run(args);
        System.exit(exitCode);
    }

    /**
     * Procesa los argumentos, valida el archivo seleccionado, imprime el resultado y devuelve
     * el código correspondiente. El main de ReLog utiliza el valor devuelto para finalizar el proceso.
     *
     * @param args argumentos de la herramienta; el primero indica un alias o una ruta
     * @return 0 si la traza es válida, 1 si es inválida, 2 ante un error de
     *         entrada y 3 ante un error interno inesperado
     */
    int run(String[] args) {
        if (args.length == 0) {
            printUsage();
            return EXIT_INPUT_ERROR;
        }

        String logFile = resolveLogFile(args[0]);
        printHeader(logFile);

        try {
            ValidationResult result = validateFile(logFile);
            printResult(result);
            return result.isValid() ? EXIT_VALID : EXIT_INVALID;
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error de entrada: " + e.getMessage());
            return EXIT_INPUT_ERROR;
        } catch (RuntimeException e) {
            System.err.println("Error interno validando el log: " + e.getMessage());
            return EXIT_INTERNAL_ERROR;
        }
    }

    /**
     * Método utilizado por la simulación para leer un archivo, verificar sus
     * T-invariantes e imprimir el informe de resultados.
     *
     * Busca ciclos de vida completos (T0...T11) definidos por los invariantes,
     * los clasifica mediante los grupos de captura de la expresión regular y los remueve
     * conservando las transiciones intercaladas en el medio (interleaving) hasta reducir
     * la cadena a vacío. Los errores de lectura o formato se informan por la
     * salida de error y se representan mediante un retorno false.
     *
     * @param filePath ruta al archivo de log
     * @return true si el archivo contiene al menos un T-invariante completo y la reducción
     *         no deja transiciones residuales; false si el archivo no puede leerse, está vacío,
     *         contiene tokens inválidos o no cumple los invariantes
     */
    public boolean checkInvariant(String filePath) {
        try {
            ValidationResult result = validateFile(filePath);
            printResult(result);
            return result.isValid();
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Error validando el archivo de log: " + e.getMessage());
            return false;
        }
    }

    /**
     * Valida una secuencia sin realizar entrada/salida por consola. Cada
     * elemento se normaliza con trim(), se ignoran los elementos vacíos y se exige
     * que los restantes sean transiciones entre T0 y T11.
     * Luego aplica iterativamente la reducción hasta que una pasada completa no
     * encuentra nuevos T-invariantes. El resultado solo es válido si se detecta
     * al menos un ciclo completo y no quedan transiciones residuales.
     *
     * @param transitions transiciones del log, una por elemento
     * @return resultado estructurado de la reducción
     * @throws IllegalArgumentException si una transición tiene un formato inválido
     *         o no queda ninguna transición después de ignorar los elementos vacíos
     * @throws NullPointerException si transitions es null
     */
    public ValidationResult validate(List<String> transitions) {
        Objects.requireNonNull(transitions, "La lista de transiciones no puede ser null");

        List<String> normalizedTransitions = normalizeTransitions(transitions);
        if (normalizedTransitions.isEmpty()) {
            throw new IllegalArgumentException("El log no contiene transiciones");
        }

        String trace = String.join("", normalizedTransitions);

        int media = 0;
        int simple = 0;
        int alta = 0;

        String current = trace;
        int matchesInStep;

        do {
            Matcher m = INVARIANT_PATTERN.matcher(current);
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
        } while (matchesInStep > 0);

        List<String> remainingTransitions = splitTrace(current);
        int totalMatches = media + simple + alta;
        boolean valid = current.isEmpty() && totalMatches > 0;

        return new ValidationResult(
                valid,
                media,
                simple,
                alta,
                remainingTransitions);
    }

    /**
     * Elimina espacios laterales, descarta elementos vacíos y valida que cada
     * elemento restante represente exactamente una transición entre T0 y T11.
     *
     * @param transitions líneas recibidas por el núcleo de validación
     * @return transiciones normalizadas en el mismo orden de entrada
     * @throws IllegalArgumentException si un elemento no representa una transición válida
     */
    private List<String> normalizeTransitions(List<String> transitions) {
        List<String> normalized = new ArrayList<>();

        for (int i = 0; i < transitions.size(); i++) {
            String transition = transitions.get(i).trim();
            if (transition.isEmpty()) {
                continue;
            }
            if (!TRANSITION_PATTERN.matcher(transition).matches()) {
                throw new IllegalArgumentException(
                        "Transición inválida en la línea " + (i + 1) + ": " + transition);
            }

            normalized.add(transition);
        }

        return normalized;
    }

    /**
     * Convierte la traza residual concatenada en una lista ordenada de
     * transiciones reconocidas por TRANSITION_PATTERN.
     *
     * @param trace traza residual concatenada
     * @return transiciones residuales en el orden en que aparecen
     */
    private List<String> splitTrace(String trace) {
        List<String> transitions = new ArrayList<>();
        Matcher matcher = TRANSITION_PATTERN.matcher(trace);

        while (matcher.find()) {
            transitions.add(matcher.group());
        }

        return transitions;
    }

    /**
     * Lee las líneas de un archivo y las entrega al núcleo de validación.
     *
     * @param filePath ruta del archivo que se debe validar
     * @return resultado estructurado de la reducción
     * @throws IOException si el archivo no puede leerse
     * @throws IllegalArgumentException si el contenido no cumple el formato de entrada
     */
    private ValidationResult validateFile(String filePath) throws IOException {
        List<String> logLines = Files.readAllLines(Paths.get(filePath));
        return validate(logLines);
    }

    /**
     * Resuelve los alias conocidos y conserva sin cambios cualquier ruta directa.
     *
     * @param argument alias o ruta recibida por línea de comandos
     * @return archivo asociado al alias o la ruta original
     */
    private String resolveLogFile(String argument) {
        switch (argument.toLowerCase()) {
            case "random":
                return "log_random.txt";
            case "priority":
                return "log_priority.txt";
            default:
                return argument;
        }
    }

    /**
     * Muestra el uso correcto de la herramienta ante argumentos inválidos.
     */
    private void printUsage() {
        System.err.println("Uso: java -cp target/classes tpfinal.utils.RELog <random|priority|ruta_archivo>");
        System.err.println("  random   -> valida log_random.txt");
        System.err.println("  priority -> valida log_priority.txt");
        System.err.println("  <ruta>   -> valida el archivo indicado");
    }

    /**
     * Muestra la cabecera del informe correspondiente al archivo seleccionado.
     *
     * @param logFile archivo que se mostrará en la cabecera
     */
    private void printHeader(String logFile) {
        System.out.println("========================================================");
        System.out.println("  Verificador RELog - Validación de T-Invariantes");
        System.out.println("  Archivo: " + logFile);
        System.out.println("========================================================");
    }

    /**
     * Imprime el informe de resultados construido a partir de la validación.
     *
     * @param result resultado estructurado que se debe mostrar
     */
    private void printResult(ValidationResult result) {
        System.out.println("--- Invariantes de transición detectados (reducción recursiva) ---");
        System.out.println("IT1 - Complejidad media (T0-T1-T2-T3-T4-T11)    : " + result.getMediumCount());
        System.out.println("IT2 - Complejidad simple (T0-T1-T5-T6-T11)      : " + result.getSimpleCount());
        System.out.println("IT3 - Complejidad alta (T0-T1-T7-T8-T9-T10-T11) : " + result.getHighCount());
        System.out.println("Coincidencias (ciclos válidos)                  : " + result.getTotalMatches());

        System.out.println("--- Resultado ---");
        if (result.isValid()) {
            System.out.println("VALIDACION DE INVARIANTES EXITOSA");
        } else {
            System.out.println("VALIDACION DE INVARIANTES FALLIDA");
            System.out.println("Transiciones restantes: " + result.getRemainingTrace());
        }
    }
}
