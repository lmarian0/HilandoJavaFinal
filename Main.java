import java.util.ArrayList;
import java.util.List;

/**
 * Clase principal que configura y ejecuta la simulación de la Red de Petri.
 * Gestiona la creación de hilos, el monitor, y la verificación de invariantes.
 */
public class Main {
    public static void main(String[] args) {
        System.out.println("========== INICIO DE SIMULACIÓN ==========");
        
        // 1. Tomar tiempo de inicio (en milisegundos)
        long startTime = System.currentTimeMillis();

        try {
            // 2. Crear la Red de Petri
            PetriNet petriNet = new PetriNet();
            System.out.println("Red de Petri creada");
            System.out.print("Marcado inicial: ");
            petriNet.printMarking();

            // 3. Crear el Log
            Log log = new Log("log.txt");

            // 4. Crear la Política (RandomPolicy o PriorityPolicy)
            Policy policy = new RandomPolicy(); // Cambiar a PriorityPolicy si se desea
            System.out.println("Política seleccionada: " + policy.getClass().getSimpleName());

            // 5. Crear el Monitor
            Monitor monitor = new Monitor(petriNet, policy, log);
            System.out.println("Monitor creado");

            // 6. Definir las secuencias de transiciones para cada hilo
            // Basado en la red de Petri del enunciado:
            
            // Segmento de Procesos de Complejidad Media
            int[] mediaSequence = {0, 1, 2, 3, 4, 11};
            
            // Segmento de Procesos Simples
            int[] simpleSequence = {0, 1, 5, 6, 11};
            
            // Segmento de Procesos de Alta Complejidad
            int[] altaSequence = {0, 1, 7, 8, 9, 10, 11};

            // 7. Definir el número de repeticiones
            // Para que la ejecución dure 20-40 segundos, ajustar este valor
            int repetitions = 20; // Ajustar según necesidad

            // 8. Crear los hilos
            List<Thread> threads = new ArrayList<>();
            
            // Crear múltiples hilos para cada tipo de proceso
            threads.add(new SegmentThread(monitor, mediaSequence, repetitions, petriNet, "Media-1"));
            threads.add(new SegmentThread(monitor, mediaSequence, repetitions, petriNet, "Media-2"));
            threads.add(new SegmentThread(monitor, mediaSequence, repetitions, petriNet, "Media-3"));
            
            threads.add(new SegmentThread(monitor, simpleSequence, repetitions, petriNet, "Simple-1"));
            threads.add(new SegmentThread(monitor, simpleSequence, repetitions, petriNet, "Simple-2"));
            threads.add(new SegmentThread(monitor, simpleSequence, repetitions, petriNet, "Simple-3"));
            
            threads.add(new SegmentThread(monitor, altaSequence, repetitions, petriNet, "Alta-1"));
            threads.add(new SegmentThread(monitor, altaSequence, repetitions, petriNet, "Alta-2"));
            threads.add(new SegmentThread(monitor, altaSequence, repetitions, petriNet, "Alta-3"));

            System.out.println("\n========== INICIANDO HILOS ==========");
            
            // 9. Iniciar todos los hilos
            for (Thread thread : threads) {
                thread.start();
                System.out.println("Hilo iniciado: " + thread.getName());
            }

            // 10. Esperar a que todos los hilos terminen
            for (Thread thread : threads) {
                thread.join();
                System.out.println("Hilo finalizado: " + thread.getName());
            }

            System.out.println("\n========== TODOS LOS HILOS FINALIZADOS ==========");
            
            // 11. Escribir el log a disco
            log.writeToDisk();
            
            // 12. Mostrar el marcado final
            System.out.print("Marcado final: ");
            petriNet.printMarking();
            
        } catch (Exception e) {
            System.err.println("Error durante la ejecución: " + e.getMessage());
            e.printStackTrace();
        }

        // 13. Tomar tiempo de fin
        long endTime = System.currentTimeMillis();

        // 14. Calcular duración
        long duration = endTime - startTime;
        
        System.out.println("\n========== RESULTADOS ==========");
        System.out.println("Tiempo de ejecución: " + duration + " ms (" + 
                          String.format("%.2f", duration / 1000.0) + " segundos)");
        
        // Verificación para el Requisito 8.a (20-40 seg)
        if (duration >= 20000 && duration <= 40000) {
            System.out.println("✅ Tiempo válido (entre 20s y 40s)");
        } else {
            System.out.println("⚠️  Tiempo fuera del rango esperado (20-40s)");
        }
        
        // 15. Verificar el invariante con RELog
        System.out.println("\n========== VERIFICACIÓN DE INVARIANTE ==========");
        RELog reLog = new RELog();
        
        // Cargar el archivo de log
        reLog.loadLog("log.txt");
        
        // Verificar si el log cumple con el invariante
        boolean invariantValid = reLog.checkInvariant();
        
        if (invariantValid) {
            System.out.println("✅ El invariante se cumple correctamente");
        } else {
            System.out.println("❌ El invariante NO se cumple");
        }
        
        System.out.println("\n========== FIN DE SIMULACIÓN ==========");
    }
}
