package tpfinal.utils;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class Logger {

    private final ConcurrentLinkedQueue<String> buffer = new ConcurrentLinkedQueue<>();

    public void log(String transitionName) {
        buffer.add(transitionName);
    }

    public void writeToFile(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename, false))) {
            String entry;
            while ((entry = buffer.poll()) != null) {
                writer.println(entry);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error escribiendo log", e);
        }
    }
}