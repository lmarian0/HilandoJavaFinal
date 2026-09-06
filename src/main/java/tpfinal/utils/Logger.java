package tpfinal.utils;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Registrador (Logger) de eventos thread-safe.
 * Utiliza ConcurrentLinkedQueue para encolar los nombres de las transiciones
 * disparadas en tiempo real sin bloquear la ejecución de los hilos de simulación,
 * permitiendo una posterior escritura secuencial a disco.
 */
public class Logger {

    // Buffer concurrente que retiene los nombres de las transiciones en el orden exacto de su disparo
    private final ConcurrentLinkedQueue<String> buffer = new ConcurrentLinkedQueue<>();

    /**
     * Encola el disparo de una transición en el buffer concurrente de forma segura.
     * @param transitionName Nombre de la transición disparada (ej. "T0")
     */
    public void log(String transitionName) {
        buffer.add(transitionName);
    }

    /**
     * Vuelca (escribe) todo el contenido acumulado en el buffer concurrente a un archivo físico.
     * El buffer se vacía progresivamente durante la escritura.
     * 
     * @param filename Ruta o nombre del archivo de salida
     * @throws RuntimeException si ocurre algún fallo de Entrada/Salida (I/O) al escribir el archivo
     */
    public void writeToFile(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename, false))) {
            String entry;
            while ((entry = buffer.poll()) != null) {
                writer.println(entry);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error escribiendo el log en disco", e);
        }
    }
}