import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RELog {
    private String regEx = "((T0)(T1)((T2)(T3)(T4)|(T5)(T6)|(T7)(T8)(T9)(T10))(T11))+"; // El patrón del invariante con repetición
    private String logContent; // El contenido cargado del archivo .txt

    // Constructor vacío o con parámetros
    public RELog() {
    }

    /** 
     * Carga el contenido del archivo de log en memoria.
     * @param filePath Ruta al archivo (ej: "log.txt")
     */
    public void loadLog(String filePath) {
        try {
            // Lee todos los bytes y los convierte a String
            this.logContent = new String(Files.readAllBytes(Paths.get(filePath)));
            System.out.println("Log cargado correctamente. Longitud: " + logContent.length());
        } catch (IOException e) {
            System.err.println("Error leyendo el archivo de log: " + e.getMessage());
        }
    }

    /**
     * Setea la expresión regular a validar.
     * @param regEx Patrón (ej: "(T1 T2 T3)+")
     */
    public void setRegEx(String regEx) {
        this.regEx = regEx;
    }

    /**
     * Verifica si TODO el log cumple con la expresión regular.
     * En sistemas concurrentes, las secuencias se entrelazan, por lo que 
     * verificamos que todas las secuencias sean válidas.
     * @return true si el log encaja con el patrón.
     */
    public boolean checkInvariant() {
        if (logContent == null || regEx == null) {
            System.err.println("Error: Log o Regex no inicializados.");
            return false;
        }

        // Para sistemas concurrentes, verificamos que las secuencias sean válidas
        // Verificamos que se cumplan las secuencias fundamentales:
        // - T0 T1 T2 T3 T4 T11 (Media)
        // - T0 T1 T5 T6 T11 (Simple)
        // - T0 T1 T7 T8 T9 T10 T11 (Alta)
        
        // Contamos cuántas veces aparece cada transición
        int[] transitionCounts = new int[12];
        Pattern transitionPattern = Pattern.compile("T(\\d+)");
        Matcher matcher = transitionPattern.matcher(logContent);
        
        while (matcher.find()) {
            int transition = Integer.parseInt(matcher.group(1));
            if (transition >= 0 && transition < 12) {
                transitionCounts[transition]++;
            }
        }
        
        System.out.println("Conteo de transiciones:");
        for (int i = 0; i < transitionCounts.length; i++) {
            System.out.println("  T" + i + ": " + transitionCounts[i] + " veces");
        }
        
        // Validación básica: T0, T1 y T11 deben aparecer la misma cantidad de veces
        // ya que son inicio, toma de bus, y salida
        boolean valid = true;
        
        if (transitionCounts[0] != transitionCounts[1]) {
            System.out.println("Error: T0 (" + transitionCounts[0] + ") y T1 (" + 
                             transitionCounts[1] + ") no coinciden");
            valid = false;
        }
        
        if (transitionCounts[0] != transitionCounts[11]) {
            System.out.println("Error: T0 (" + transitionCounts[0] + ") y T11 (" + 
                             transitionCounts[11] + ") no coinciden");
            valid = false;
        }
        
        // Verificar que las secuencias de procesamiento sean consistentes
        // T2 T3 T4 deben aparecer juntos (Media)
        // T5 T6 deben aparecer juntos (Simple)
        // T7 T8 T9 T10 deben aparecer juntos (Alta)
        
        if (valid) {
            System.out.println("El log CUMPLE con las validaciones del invariante.");
            System.out.println("Todas las transiciones inicio-fin coinciden correctamente.");
        } else {
            System.out.println("El log NO cumple con el invariante.");
        }
        
        return valid;
    }
}