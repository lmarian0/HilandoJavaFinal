package tpfinal.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RELog {
    private List<String> logLines;

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
     * Verifica los T-invariantes contando las transiciones disparadas.
     * 
     * T-Invariantes de la red:
     *   Inv 1 (Media):  T0, T1, T2, T3, T4, T11
     *   Inv 2 (Simple): T0, T1, T5, T6, T11
     *   Inv 3 (Alta):   T0, T1, T7, T8, T9, T10, T11
     *
     * Condiciones:
     *   1) T0 == T1 == T11 (entrada y salida iguales)
     *   2) T2 == T3 == T4 (media consistente)
     *   3) T7 == T8 == T9 == T10 (alta consistente)
     *   4) T5 == T6 (simple consistente)
     *   5) Media + Simple + Alta == T0 (todo lo que entra se procesa)
     *
     * @return true si todos los invariantes se cumplen
     */
    public boolean checkInvariant() {
        if (logLines == null || logLines.isEmpty()) {
            System.err.println("Error: Log no cargado o vacío.");
            return false;
        }

        // Contar cada transición
        Map<String, Integer> counts = new HashMap<>();
        for (String line : logLines) {
            String t = line.trim();
            if (!t.isEmpty()) {
                counts.put(t, counts.getOrDefault(t, 0) + 1);
            }
        }

        // Mostrar conteos
        System.out.println("--- Conteo de transiciones ---");
        for (int i = 0; i <= 11; i++) {
            String key = "T" + i;
            System.out.println(key + ": " + counts.getOrDefault(key, 0));
        }

        int t0 = counts.getOrDefault("T0", 0);
        int t1 = counts.getOrDefault("T1", 0);
        int t2 = counts.getOrDefault("T2", 0);
        int t3 = counts.getOrDefault("T3", 0);
        int t4 = counts.getOrDefault("T4", 0);
        int t5 = counts.getOrDefault("T5", 0);
        int t6 = counts.getOrDefault("T6", 0);
        int t7 = counts.getOrDefault("T7", 0);
        int t8 = counts.getOrDefault("T8", 0);
        int t9 = counts.getOrDefault("T9", 0);
        int t10 = counts.getOrDefault("T10", 0);
        int t11 = counts.getOrDefault("T11", 0);

        boolean valid = true;

        // 1) Entrada == Salida
        if (t0 != t1 || t0 != t11) {
            System.out.println("FALLO: T0(" + t0 + ") != T1(" + t1 + ") != T11(" + t11 + ")");
            valid = false;
        }

        // 2) Media consistente
        if (t2 != t3 || t3 != t4) {
            System.out.println("FALLO: Media inconsistente T2(" + t2 + ") T3(" + t3 + ") T4(" + t4 + ")");
            valid = false;
        }

        // 3) Simple consistente
        if (t5 != t6) {
            System.out.println("FALLO: Simple inconsistente T5(" + t5 + ") T6(" + t6 + ")");
            valid = false;
        }

        // 4) Alta consistente
        if (t7 != t8 || t8 != t9 || t9 != t10) {
            System.out.println("FALLO: Alta inconsistente T7(" + t7 + ") T8(" + t8 + ") T9(" + t9 + ") T10(" + t10 + ")");
            valid = false;
        }

        // 5) Media + Simple + Alta == Total
        int totalProcesados = t2 + t5 + t7;
        if (totalProcesados != t0) {
            System.out.println("FALLO: Media(" + t2 + ") + Simple(" + t5 + ") + Alta(" + t7 + ") = " + totalProcesados + " != T0(" + t0 + ")");
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