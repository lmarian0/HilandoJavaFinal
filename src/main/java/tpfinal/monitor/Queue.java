package tpfinal.monitor;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;

import tpfinal.rdp.Transitions;

/**
 * Gestor de colas de espera asociadas al monitor.
 * Administra un array de semáforos binarios (uno por transición, inicializados en 0) que actúan como colas de condición.
 * Cada semáforo permite suspender y despertar hilos de forma dirigida, y realiza el seguimiento del número de hilos suspendidos en cada cola.
 * 
 * Semántica Signal-and-Exit: al despertar a un hilo con release(), el señalizador
 * NO libera el mutex del monitor. El hilo despertado "hereda" el acceso exclusivo al monitor
 * porque mutex.permits permanece en 0 (nadie más puede entrar).
 */
public class Queue {

    // Semáforos binarios que actúan como colas de condición (una por transición, init=0)
    private final Semaphore[] condSemaphores;
    // Contador de hilos suspendidos esperando por cada transición
    private final int[] waitingCounts;

    /**
     * Construye un gestor de colas inicializando los semáforos de condición.
     * Cada semáforo se inicializa con 0 permisos (cualquier acquire() se bloquea
     * inmediatamente) y fairness=true (FIFO en caso de múltiples waiters).
     *
     * @param transitionsNumber Número total de transiciones en la red
     */
    public Queue(int transitionsNumber) {
        this.condSemaphores = new Semaphore[transitionsNumber];
        this.waitingCounts = new int[transitionsNumber];

        for (int i = 0; i < transitionsNumber; i++) {
            condSemaphores[i] = new Semaphore(0, true);
            waitingCounts[i] = 0;
        }
    }

    /**
     * Suspende pasivamente al hilo actual en la cola asociada a la transición indicada.
     * El método libera el mutex del monitor antes de dormir (equivalente al await() de Condition),
     * y al despertar el hilo posee el mutex por herencia (Signal-and-Exit: el señalizador no liberó el mutex al hacer release()).
     *
     * Contrato: al retornar de este método, el hilo SIEMPRE posee el mutex del monitor.
     *
     * @param transition El índice de la transición en cuya cola se suspenderá el hilo
     * @param mutex El semáforo que actúa como mutex del monitor
     */
    public void acquire(int transition, Semaphore mutex) {
        waitingCounts[transition]++;
        mutex.release();  // Libera el mutex del monitor antes de dormir
        
        // acquireUninterruptibly previene abandonar la cola prematuramente por interrupciones.
        // Garantiza que el hilo solo despierte mediante un release() legítimo (heredando el mutex)
        // y evita falsos positivos en waitingCounts que romperían el Signal-and-Exit.
        condSemaphores[transition].acquireUninterruptibly();

        waitingCounts[transition]--;
    }
    
    /**
     * Despierta a un hilo que esté esperando en la cola asociada a la transición indicada.
     * Solo envía la señal si hay al menos un hilo durmiendo en esa condición.
     *
     * Semántica Signal-and-Exit: este método NO libera el mutex del monitor.
     * El hilo despertado hereda el acceso exclusivo porque mutex.permits permanece en 0.
     *
     * @param transition El índice de la transición cuyo hilo en espera se desea despertar
     */
    public void release(int transition) {
        if (waitingCounts[transition] > 0) {
            condSemaphores[transition].release();  // Despierta al dormido sin liberar mutex
        }
    }

    /**
     * Devuelve el conjunto de transiciones que tienen hilos durmiendo en sus colas.
     *
     * @return Un conjunto (Set) con los enums de las transiciones que poseen hilos en espera
     */
    public Set<Transitions> getWaitingThreads() {
        Set<Transitions> waitingThreads = new HashSet<>();
        for (int i = 0; i < waitingCounts.length; i++) {
            boolean isWaiting = waitingCounts[i] > 0;
            if (isWaiting) {
                waitingThreads.add(Transitions.fromIndex(i));
            }
        }
        return waitingThreads;
    }
}
