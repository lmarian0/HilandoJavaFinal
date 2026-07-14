package tpfinal.monitor;

import tpfinal.policies.*;
import java.util.Set;
import java.util.concurrent.Semaphore;

import tpfinal.rdp.PetriNet;
import tpfinal.rdp.Transitions;
import tpfinal.utils.Logger;
import tpfinal.utils.MathUtils;

/**
 * Monitor de Concurrencia con semántica Signal-and-Exit.
 * Utiliza un semáforo binario (fair) como mutex para la exclusión mutua, y semáforos
 * por transición como colas de condición. Al señalizar a un hilo dormido, el señalizador
 * NO libera el mutex: el despertado hereda el acceso exclusivo al monitor porque
 * mutex.permits permanece en 0, impidiendo que cualquier otro hilo entre.
 *
 * Esto otorga preferencia absoluta al hilo señalizado sobre los hilos en la cola de entrada.
 */
public class Monitor implements MonitorInterface {

    // Semáforo binario con equidad (fairness = true) como mutex del monitor.
    // permits=1: monitor libre. permits=0: monitor ocupado.
    private final Semaphore mutex = new Semaphore(1, true);
    // Gestor de colas de condición específicas por transición
    private final Queue queue;
    // Política de resolución de conflictos inyectada dinámicamente
    private final Policy policy;
    // Logger para el registro secuencial y ordenado de los disparos
    private final Logger logger;

    /**
     * Constructor del Monitor.
     * @param policy Política inyectada para la resolución de conflictos
     * @param logger Logger utilizado para registrar los disparos en orden estricto de exclusión mutua
     */
    public Monitor(Policy policy, Logger logger) {
        this.queue = new Queue(PetriNet.NUM_TRANSITIONS);
        this.policy = policy;
        this.logger = logger;
    }

    /**
     * Intenta disparar la transición indicada de forma sincronizada y segura.
     * Si la transición no está habilitada por marcado, duerme al hilo pasivamente en su cola de condición.
     * Si es temporal y está antes del EFT (alpha), duerme al hilo liberando el mutex y re-evalúa al despertar.
     * @param transition Índice de la transición que se intenta disparar
     * @return true si la transición se disparó y registró exitosamente
     */
    @Override
    public boolean fireTransition(int transition) {
        // Intentar entrar al monitor (adquirir el mutex)
        try {
            mutex.acquire();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        // A diferencia de ReentrantLock, los semáforos no poseen el concepto nativo de "ownership" (hilo propietario).
        // Cualquier hilo puede llamar a release(), lo que incrementaría los permisos y destruiría la exclusión mutua.
        // Usamos holdingMutex para rastrear lógicamente la posesión y evitar que el bloque finally 
        // libere el mutex si ya lo cedimos por herencia (Signal-and-Exit) o si fallamos en adquirirlo.
        boolean holdingMutex = true;
        Transitions transitionEnum = Transitions.fromIndex(transition);

        try {
            while (true) {
                Set<Transitions> enabled = PetriNet.getEnabledTransitions();
                if (!enabled.contains(transitionEnum)) {
                    // No está habilitada por marcado: dormir en la cola de condición.
                    // queue.acquire() libera el mutex internamente y al despertar el hilo posee el mutex por herencia (Signal-and-Exit).
                    queue.acquire(transition, mutex);
                    continue; // Al despertar, volver a evaluar todo el bucle
                }

                // Habilitada por marcado. Ventana de tiempo [EFT, LFT]
                long now = System.currentTimeMillis();
                long w_i = PetriNet.getSensitizationTimestamp(transition);

                // EFT (Earliest Firing Time) = w_i + alpha
                long alpha = transitionEnum.getAlpha();
                long eft = w_i + alpha;

                // Nota: El LFT (Latest Firing Time) es w_i + beta. Como beta = Long.MAX_VALUE (infinito),
                // el límite superior es ilimitado y no requiere control explícito (now <= LFT siempre es true).
                if (now < eft) {
                    // Aún no transcurrió el tiempo mínimo. Liberar el mutex y esperar fuera del monitor.
                    long sleepTime = eft - now;
                    mutex.release();
                    holdingMutex = false;
                    try {
                        Thread.sleep(sleepTime);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return false; // holdingMutex=false evita hacer un double-release que subiría los permisos del semáforo a 2 (rompiendo exclusión mutua)
                    }
                    // Re-entrar al monitor: se encola en la cola FIFO del semáforo mutex.
                    // Sin preferencia (a diferencia de los hilos despertados por Signal-and-Exit).
                    try {
                        mutex.acquire();
                        holdingMutex = true;
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return false; // holdingMutex=false previene liberar el mutex al fallar la re-adquisición por interrupción
                    }
                    // Al re-adquirir el mutex, volver a evaluar en el siguiente ciclo (el marcado puede haber cambiado mientras dormia)
                    continue;
                }

                // Habilitada por marcado y tiempo mínimo cumplido (now >= eft)
                boolean validFiring = PetriNet.fire(transitionEnum);
                if (validFiring) {
                    if (logger != null) {
                        logger.log(transitionEnum.getName());
                    }

                    // Despertar hilos concurrentes que estén esperando y que ahora estén habilitados
                    Set<Transitions> nextEnabled = PetriNet.getEnabledTransitions();
                    Set<Transitions> waitingThreads = queue.getWaitingThreads();
                    Set<Transitions> m = MathUtils.intersectSets(nextEnabled, waitingThreads);
                    if (!m.isEmpty()) {
                        // Signal-and-Exit: despertar al hilo seleccionado SIN liberar el mutex.
                        // El despertado hereda el acceso exclusivo (mutex.permits permanece en 0).
                        // Los hilos en la cola de entrada del mutex siguen bloqueados.
                        int selectedTransition = policy.selectTransition(m);
                        queue.release(selectedTransition);
                        holdingMutex = false; // El mutex fue cedido al despertado por herencia
                    } else {
                        // Nadie que señalizar: liberar el mutex normalmente.
                        // El primero en la cola FIFO del semáforo mutex podrá entrar.
                        mutex.release();
                        holdingMutex = false;
                    }
                    return true; // holdingMutex=false previene un double-release en el bloque finally, dado que el mutex ya fue gestionado.
                } else {
                    // Matemáticamente imposible: 'enabled' se validó al inicio del ciclo y nadie pudo modificar el marcado porque poseemos el mutex. 
                    // Si ocurriera por algún bug, NO retornamos false porque el hilo debe lograr disparar la transición sí o sí para no romper su secuencia.
                    // Lo mandamos a dormir pasivamente a su cola de condición y al despertar el while(true) re-evaluará automáticamente.
                    queue.acquire(transition, mutex);
                }
            }
        } finally {
            // Red de seguridad: liberar el mutex si aún lo poseemos. En el flujo normal, holdingMutex es false al llegar acá.
            // Solo se ejecuta si una excepción no prevista interrumpió el flujo antes de liberar el mutex explícitamente.
            if (holdingMutex) {
                mutex.release();
            }
        }
    }
}
