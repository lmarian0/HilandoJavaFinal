import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Clase para registro y verificación de disparos de transiciones.
 * Guarda cada transición disparada junto con el marcado resultante.
 */
public class Log {
    private StringBuffer buffer;
    private String filePath;

    public Log(String filePath) {
        this.buffer = new StringBuffer();
        this.filePath = filePath;
    }

    /**
     * Registra una transición disparada con el marcado resultante.
     * @param transition Índice de la transición disparada
     * @param marking Marcado actual después del disparo
     */
    public synchronized void log(int transition, int[] marking) {
        // Formato: T0, T1, T2... para crear la secuencia de transiciones
        buffer.append("T").append(transition);
        
        // Opcional: guardar también el marcado para verificación detallada
        // buffer.append(" [").append(Arrays.toString(marking)).append("]\n");
    }

    /**
     * Escribe el buffer completo al disco.
     * Se debe llamar al finalizar todas las operaciones.
     */
    public synchronized void writeToDisk() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            writer.write(buffer.toString());
            System.out.println("Log escrito correctamente en: " + filePath);
        } catch (IOException e) {
            System.err.println("Error escribiendo el log: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Obtiene el contenido del buffer.
     * @return Contenido del log
     */
    public synchronized String getContent() {
        return buffer.toString();
    }

    /**
     * Limpia el buffer del log.
     */
    public synchronized void clear() {
        buffer = new StringBuffer();
    }
}
