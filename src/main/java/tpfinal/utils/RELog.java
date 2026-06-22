package tpfinal.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Verificación de los T-invariantes de la red mediante EXPRESIONES REGULARES
 * (requerimiento 11) a partir del archivo de log de transiciones disparadas.
 *
 * T-Invariantes de la red:
 *   IT1 (media):  T0, T1, T2, T3, T4, T11
 *   IT2 (simple): T0, T1, T5, T6, T11
 *   IT3 (alta):   T0, T1, T7, T8, T9, T10, T11
 *
 * Como el log se genera DENTRO del monitor, refleja el orden real de disparo.
 * Por el P-invariante de la unidad de procesamiento (CPU = 1) solo puede haber
 * un dato procesándose a la vez, por lo que las "etapas de procesamiento" de
 * cada dato aparecen de forma CONTIGUA en el log:
 *   - media  -> T2,T3,T4
 *   - simple -> T5,T6
 *   - alta   -> T7,T8,T9,T10
 * Las transiciones de entrada/salida (T0,T1,T11) usan otros recursos (bus y
 * buffer de salida) y se intercalan libremente, por eso se filtran antes de
 * validar la estructura de procesamiento.
 */
public class RELog {

    private List<String> logLines;

    // Patrón que reconoce ÚNICAMENTE secuencias formadas por bloques de
    // procesamiento completos y correctamente ordenados, uno tras otro.
    private static final Pattern PROCESSING_STRUCTURE =
            Pattern.compile("^((T2,T3,T4|T5,T6|T7,T8,T9,T10),?)*$");

    private static final Pattern MEDIA  = Pattern.compile("T2,T3,T4");
    private static final Pattern SIMPLE = Pattern.compile("T5,T6");
    private static final Pattern ALTA   = Pattern.compile("T7,T8,T9,T10");

    public RELog() {
    }

    /**
     * Carga el contenido del archivo de log en memoria.
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
     * Cuenta las ocurrencias de una transición exacta en el log usando regex.
     * Se usa \b (límite de palabra) para que, por ejemplo, "T1" no matchee "T11".
     */
    private int countTransition(String trace, String name) {
        Matcher m = Pattern.compile("\\b" + name + "\\b").matcher(trace);
        int count = 0;
        while (m.find()) {
            count++;
        }
        return count;
    }

    /** Cuenta matches no solapados de un patrón sobre un texto. */
    private int countMatches(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        int count = 0;
        while (m.find()) {
            count++;
        }
        return count;
    }

    /**
     * Verifica los T-invariantes a partir del log usando expresiones regulares.
     * @return true si el log cumple con los tres T-invariantes
     */
    public boolean checkInvariant() {
        if (logLines == null || logLines.isEmpty()) {
            System.err.println("Error: Log no cargado o vacío.");
            return false;
        }

        // Traza completa separada por comas: "T0,T1,T5,T6,T11,..."
        StringBuilder sb = new StringBuilder();
        for (String line : logLines) {
            String t = line.trim();
            if (!t.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append(',');
                }
                sb.append(t);
            }
        }
        String trace = sb.toString();

        // --- Conteo de cada transición (regex con límite de palabra) ---
        System.out.println("--- Conteo de transiciones ---");
        int[] counts = new int[12];
        for (int i = 0; i <= 11; i++) {
            counts[i] = countTransition(trace, "T" + i);
            System.out.println("T" + i + ": " + counts[i]);
        }

        // --- Verificación estructural de la parte de procesamiento ---
        // Se quitan las transiciones de entrada/salida que se intercalan.
        String core = trace.replaceAll("\\bT0\\b", "")
                           .replaceAll("\\bT1\\b", "")
                           .replaceAll("\\bT11\\b", "")
                           .replaceAll(",{2,}", ",")
                           .replaceAll("^,|,$", "");

        boolean valid = true;

        if (!PROCESSING_STRUCTURE.matcher(core).matches()) {
            System.out.println("FALLO: la secuencia de procesamiento contiene "
                    + "etapas incompletas o desordenadas (no respeta los T-invariantes).");
            valid = false;
        }

        int media  = countMatches(MEDIA, core);
        int simple = countMatches(SIMPLE, core);
        int alta   = countMatches(ALTA, core);
        int totalInvariants = media + simple + alta;

        System.out.println("--- Invariantes de transición detectados (regex) ---");
        System.out.println("IT1 - Media  (T2,T3,T4)    : " + media);
        System.out.println("IT2 - Simple (T5,T6)       : " + simple);
        System.out.println("IT3 - Alta   (T7,T8,T9,T10): " + alta);
        System.out.println("Total de invariantes       : " + totalInvariants);

        // Cierre entrada/salida: cada invariante empieza con T0,T1 y termina con T11
        if (counts[0] != counts[1] || counts[0] != counts[11]) {
            System.out.println("FALLO: entrada/salida desbalanceada "
                    + "T0(" + counts[0] + ") T1(" + counts[1] + ") T11(" + counts[11] + ")");
            valid = false;
        }
        if (counts[0] != totalInvariants) {
            System.out.println("FALLO: T0(" + counts[0] + ") != suma de invariantes ("
                    + totalInvariants + ")");
            valid = false;
        }

        System.out.println("--- Resultado ---");
        if (valid) {
            System.out.println("El log CUMPLE con todos los T-invariantes.");
        } else {
            System.out.println("El log NO cumple con los T-invariantes.");
        }
        return valid;
    }
}
