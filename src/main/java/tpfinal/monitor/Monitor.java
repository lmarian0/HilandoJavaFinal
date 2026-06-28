package tpfinal.monitor;

import tpfinal.policies.*;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import tpfinal.rdp.PetriNet;
import tpfinal.rdp.Transitions;
import tpfinal.utils.Logger;
import tpfinal.utils.MathUtils;
import tpfinal.utils.TraceLogger;

/**
 * Monitor de Concurrencia encargado de arbitrar y sincronizar el disparo de transiciones en la Red de Petri.
 * Utiliza exclusión mutua equitativa (fair lock) y colas de condición específicas por transición para evitar condiciones de carrera, esperas activas e inanición.
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
    // Trazador detallado opcional
    private TraceLogger traceLogger;
    
    // Contadores para control de finalización de invariantes
    private int t11Count = 0;
    private boolean completed = false;

    /**
     * Constructor completo del Monitor.
     * 
     * @param policy      Política inyectada para la resolución de conflictos
     * @param logger      Logger utilizado para registrar los disparos en orden estricto
     * @param traceLogger Trazador detallado para observabilidad en tiempo real (opcional)
     */
    public Monitor(Policy policy, Logger logger, TraceLogger traceLogger) {
        this.queue = new Queue(PetriNet.NUM_TRANSITIONS, lock);
        this.policy = policy;
        this.logger = logger;
        this.traceLogger = traceLogger;
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
        String label = "";
        if (traceLogger != null) {
            label = traceLogger.getLabel();
            traceLogger.logEnterMonitor(label);
        }
        Transitions transitionEnum = Transitions.fromIndex(transition);
        try {
            while (true) {
                if (completed) {
                    if (traceLogger != null) {
                        traceLogger.logCompletedAbandoned(label);
                    }
                    return false;
                }

                Set<Transitions> enabled = PetriNet.getEnabledTransitions();
                if (traceLogger != null) {
                    traceLogger.logAttempt(label, transitionEnum.getName());
                }

                if (!enabled.contains(transitionEnum)) {
                    if (traceLogger != null) {
                        traceLogger.logNoTokens(label, transitionEnum.getName());
                    }
                    // No está habilitada por marcado, esperar en la cola normal (libera el lock adentro)
                    queue.acquire(transition);
                    if (traceLogger != null) {
                        traceLogger.logWokeFromQueue(label);
                    }
                    continue; // Al despertar, volver a evaluar todo el bucle
                }

                // Habilitada por marcado. Ventana de tiempo [EFT, LFT]
                long now = System.currentTimeMillis();
                long w_i = PetriNet.getSensitizationTimestamp(transition);

                // EFT (Earliest Firing Time) = w_i + alpha
                long alpha = transitionEnum.getAlpha();
                long eft = w_i + alpha;

                if (now < eft) {
                    if (traceLogger != null) {
                        traceLogger.logTimeWait(label);
                    }
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
                    if (traceLogger != null) {
                        traceLogger.logWokeFromSleep(label);
                    }
                    // Al re-adquirir el lock, volver a evaluar en el siguiente ciclo (el marcado puede haber cambiado)
                    continue;
                }

                // Habilitada por marcado y tiempo mínimo cumplido (now >= eft)
                boolean validFiring = PetriNet.fire(transitionEnum);
                if (validFiring) {
                    if (logger != null) {
                        logger.log(transitionEnum.getName());
                    }

                    if (transitionEnum == Transitions.T11) {
                        t11Count++;
                        if (t11Count == 200) {
                            completed = true;
                        }
                    }

                    if (traceLogger != null) {
                        traceLogger.logFired(label, transitionEnum.getName());
                        if (completed && transitionEnum == Transitions.T11) {
                            traceLogger.logCompleted(label);
                        }
                        traceLogger.logState(
                            PetriNet.getCurrentMarkingArray(),
                            PetriNet.getEnabledArray(),
                            queue.getWaitingCounts()
                        );
                    }

                    // Si se completaron los invariantes, despertar a todos los hilos esperando en cualquier cola
                    if (completed) {
                        for (int i = 0; i < PetriNet.NUM_TRANSITIONS; i++) {
                            queue.release(i);
                        }
                    }

                    // Despertar hilos concurrentes que estén esperando y que ahora estén habilitados
                    Set<Transitions> nextEnabled = PetriNet.getEnabledTransitions();
                    Set<Transitions> waitingThreads = queue.getWaitingThreads();
                    Set<Transitions> m = MathUtils.intersectSets(nextEnabled, waitingThreads);
                    if (!m.isEmpty() && !completed) {
                        int selectedTransition = policy.selectTransition(m);
                        if (traceLogger != null) {
                            traceLogger.logPolicyDecision(label, policy.getDecisionMessage());
                            traceLogger.logWakeUp(label, selectedTransition);
                        }
                        queue.release(selectedTransition);
                    } else {
                        if (traceLogger != null) {
                            traceLogger.logNoWakeUp(label);
                        }
                    }

                    if (traceLogger != null) {
                        traceLogger.logLeaveMonitor(label);
                    }
                    break; // Disparo exitoso, salir del bucle
                } else {
                    // Si por algún motivo fallara el disparo, ir a la cola
                    if (traceLogger != null) {
                        traceLogger.logNoTokens(label, transitionEnum.getName());
                    }
                    queue.acquire(transition);
                    if (traceLogger != null) {
                        traceLogger.logWokeFromQueue(label);
                    }
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
