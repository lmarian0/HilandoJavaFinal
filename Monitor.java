import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Monitor de Concurrencia para la Red de Petri.
 * Implementa el control de acceso concurrente a la red, garantizando exclusión mutua
 * y sincronización mediante variables de condición.
 */
public class Monitor implements MonitorInterface {
    private ReentrantLock mutex;
    private Condition[] conditions;
    private PetriNet petriNet;
    private Policy policy;
    private Log log;

    /**
     * Constructor del Monitor.
     * @param petriNet Red de Petri a controlar
     * @param policy Política de selección de transiciones
     * @param log Sistema de logging
     */
    public Monitor(PetriNet petriNet, Policy policy, Log log) {
        this.mutex = new ReentrantLock(true); // Fair lock para evitar starvation
        this.petriNet = petriNet;
        this.policy = policy;
        this.log = log;
        
        // Crear una condición por cada transición
        int numTransitions = PetriNet.INCIDENCE_MATRIX[0].length;
        this.conditions = new Condition[numTransitions];
        for (int i = 0; i < numTransitions; i++) {
            this.conditions[i] = mutex.newCondition();
        }
    }

    /**
     * Intenta disparar una transición.
     * Si no está sensibilizada, el hilo se bloquea hasta que lo esté.
     * @param transition Índice de la transición a disparar
     * @return true si la transición se disparó exitosamente
     */
    @Override
    public boolean fireTransition(int transition) {
        mutex.lock();
        try {
            // Esperar mientras la transición no esté sensibilizada
            while (!petriNet.isEnabled(transition)) {
                try {
                    conditions[transition].await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }

            // Dispara la transición usando la política
            shoot(transition);
            return true;

        } finally {
            mutex.unlock();
        }
    }

    /**
     * Dispara efectivamente una transición y despierta hilos en espera.
     * @param transition Transición a disparar
     */
    private void shoot(int transition) {
        // Actualizar el marcado
        petriNet.updateMarking(transition);
        
        // Registrar en el log
        log.log(transition, petriNet.getCurrentMarking());
        
        // Despertar todos los hilos que puedan estar esperando transiciones sensibilizadas
        List<Integer> sensitized = petriNet.getSensitized();
        for (Integer t : sensitized) {
            conditions[t].signalAll();
        }
    }

    /**
     * Cambia la política de selección de transiciones.
     * @param policy Nueva política a utilizar
     */
    public void setPolicy(Policy policy) {
        mutex.lock();
        try {
            this.policy = policy;
        } finally {
            mutex.unlock();
        }
    }

    /**
     * Obtiene la red de Petri controlada.
     * @return Red de Petri
     */
    public PetriNet getPetriNet() {
        return petriNet;
    }

    /**
     * Obtiene el log del monitor.
     * @return Log
     */
    public Log getLog() {
        return log;
    }
}

