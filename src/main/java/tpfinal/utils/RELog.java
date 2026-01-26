package tpfinal.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RELog {
    private String regEx = "(T0)(T1)((T2)(T3)(T4)|(T5)(T6)|(T7)(T8)(T9)(T10))(T11)"; // El patrón del invariante
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
     * @return true si el log encaja con el patrón.
     */
    public boolean checkInvariant() {
        if (logContent == null || regEx == null) {
            System.err.println("Error: Log o Regex no inicializados.");
            return false;
        }

        // Compilar el patrón
        Pattern pattern = Pattern.compile(regEx);
        
        // Crear el matcher sobre el contenido del log
        Matcher matcher = pattern.matcher(logContent);

        // matches() intenta ajustar TODA la cadena al patrón.
        // find() busca subcadenas. Para validar invariantes, usualmente queremos matches()
        // o un find() que cubra la mayoría del texto.
        boolean match = matcher.matches();
        
        if (match) {
            System.out.println("El log CUMPLE con el invariante.");
        } else {
            System.out.println("El log NO cumple con el invariante.");
            // Opcional: Mostrar dónde falló (más complejo, pero útil)
        }
        
        return match;
    }
}