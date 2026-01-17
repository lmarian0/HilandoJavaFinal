/**
 * Hilo que ejecuta una secuencia específica de transiciones.
 * Cada hilo representa un segmento de la red de Petri.
 */
public class SegmentThread extends Thread {
    private MonitorInterface monitor;
    private int[] transitionsSequence;
    private int repetitions;
    private PetriNet petriNet;

    /**
     * Constructor del hilo de segmento.
     * @param monitor Monitor que controla el acceso a la red
     * @param transitionsSequence Secuencia de transiciones a ejecutar
     * @param repetitions Número de veces que se ejecutará la secuencia
     * @param petriNet Red de Petri (para obtener tiempos de sleep)
     * @param name Nombre del hilo
     */
    public SegmentThread(MonitorInterface monitor, int[] transitionsSequence, 
                         int repetitions, PetriNet petriNet, String name) {
        super(name);
        this.monitor = monitor;
        this.transitionsSequence = transitionsSequence;
        this.repetitions = repetitions;
        this.petriNet = petriNet;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < repetitions; i++) {
                for (int transition : transitionsSequence) {
                    // Intentar disparar la transición
                    boolean fired = monitor.fireTransition(transition);
                    
                    if (fired) {
                        // Simular el tiempo de procesamiento de la transición
                        long sleepTime = petriNet.getSleepTime(transition);
                        if (sleepTime > 0) {
                            Thread.sleep(sleepTime);
                        }
                    } else {
                        System.err.println("Error: " + getName() + 
                                         " no pudo disparar T" + transition);
                        return;
                    }
                }
            }
        } catch (InterruptedException e) {
            System.err.println(getName() + " fue interrumpido");
            Thread.currentThread().interrupt();
        }
    }
}
