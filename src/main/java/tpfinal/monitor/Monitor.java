package tpfinal.monitor;

import tpfinal.policies.*;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import tpfinal.rdp.PetriNet;
import tpfinal.rdp.Transitions;
import tpfinal.utils.Logger;
import tpfinal.utils.MathUtils;

/**
 * Monitor de Concurrencia encargado de arbitrar y sincronizar el disparo de
 * transiciones
 * en la Red de Petri. Utiliza exclusión mutua equitativa (fair lock) y colas de
 * condición
 * específicas por transición para evitar condiciones de carrera, esperas
 * activas e inanición.
 * Expone públicamente sólo el método fireTransition y no posee referencias
 * hardcodeadas a transiciones concretas (es totalmente agnóstico a la red).
 */
public class Monitor implements MonitorInterface {

    // Lock con equidad (fairness = true) para asegurar el orden de llegada FIFO y
    // evitar inanición
    private final ReentrantLock lock = new ReentrantLock(true);
    // Gestor de colas de condición específicas por transición
    private final Queue queue;
    // Política de resolución de conflictos inyectada dinámicamente
    private final Policy policy;
    // Logger para el registro secuencial y ordenado de los disparos
    private final Logger logger;

    /**
     * Constructor por defecto del Monitor. Inicializa con una política aleatoria
     * y sin logger activo.
     */
    public Monitor() {
        this(new RandomPolicy(), null);
    }

    /**
     * Constructor del Monitor con política personalizada.
     * 
     * @param policy Política inyectada para resolver conflictos entre transiciones
     *               habilitadas
     */
    public Monitor(Policy policy) {
        this(policy, null);
    }

    /**
     * Constructor completo del Monitor.
     * 
     * @param policy Política inyectada para la resolución de conflictos
     * @param logger Logger utilizado para registrar los disparos en orden estricto
     *               de exclusión mutua
     */
    public Monitor(Policy policy, Logger logger) {
        this.queue = new Queue(PetriNet.NUM_TRANSITIONS, lock);
        this.policy = policy;
        this.logger = logger;
    }

    /**
     * Intenta disparar la transición indicada de forma sincronizada y segura.
     * Si la transición no está habilitada por marcado, duerme al hilo pasivamente.
     * Si es temporal y está antes del EFT (alpha), duerme al hilo liberando el lock
     * y re-evalúa al despertar.
     * 
     * @param transition Índice de la transición que se intenta disparar
     * @return true si la transición se disparó y registró exitosamente
     */
    @Override
    public boolean fireTransition(int transition) {
        lock.lock();
        Transitions transitionEnum = Transitions.fromIndex(transition);
        try {
            while (true) {
                Set<Transitions> enabled = PetriNet.getEnabledTransitions();
                if (!enabled.contains(transitionEnum)) {
                    // No está habilitada por marcado, esperar en la cola normal (libera el lock
                    // adentro)
                    queue.acquire(transition);
                    continue; // Al despertar, volver a evaluar todo el bucle
                }

                // Habilitada por marcado. Ventana de tiempo [EFT, LFT]
                long now = System.currentTimeMillis();
                long w_i = PetriNet.getSensitizationTimestamp(transition);

                // EFT (Earliest Firing Time) = w_i + alpha
                long alpha = transitionEnum.getAlpha();
                long eft = w_i + alpha;

                // Nota: El LFT (Latest Firing Time) es w_i + beta. Como beta = Long.MAX_VALUE
                // (infinito),
                // el límite superior es ilimitado y no requiere control explícito (now <= LFT
                // siempre es true).
                if (now < eft) {
                    // Aún no transcurrió el tiempo mínimo. Liberar el lock y esperar fuera
                    long sleepTime = eft - now;
                    lock.unlock();
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                    lock.lock();
                    // Al re-adquirir el lock, volver a evaluar en el siguiente ciclo (el marcado
                    // puede haber cambiado)
                    continue;
                }

                // Habilitada por marcado y tiempo mínimo cumplido (now >= eft)
                boolean validFiring = PetriNet.fire(transitionEnum);
                if (validFiring) {
                    if (logger != null) {
                        logger.log(transitionEnum.getName());
                    }

                    // Despertar hilos concurrentes que estén esperando y que ahora estén
                    // habilitados
                    Set<Transitions> nextEnabled = PetriNet.getEnabledTransitions();
                    Set<Transitions> waitingThreads = queue.getWaitingThreads();
                    Set<Transitions> m = MathUtils.intersectSets(nextEnabled, waitingThreads);
                    if (!m.isEmpty()) {
                        int selectedTransition = policy.selectTransition(m);
                        queue.release(selectedTransition);
                    }
                    break; // Disparo exitoso, salir del bucle
                } else {
                    // Si por algún motivo fallara el disparo, ir a la cola
                    queue.acquire(transition);
                }
            }
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
        return true;
    }
}
