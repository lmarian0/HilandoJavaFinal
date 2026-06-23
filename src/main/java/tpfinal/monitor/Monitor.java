package tpfinal.monitor;
import tpfinal.policies.*;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import tpfinal.rdp.PetriNet;
import tpfinal.rdp.Transitions;
import tpfinal.utils.Logger;
import tpfinal.utils.MathUtils;


public class Monitor implements MonitorInterface{
    // Implementación del Monitor
    private final ReentrantLock lock = new ReentrantLock(true);
    private final Queue queue;
    private final Policy policy;
    private final Logger logger;

    public Monitor() {
        this(new RandomPolicy(), null);
    }

    public Monitor(Policy policy) {
        this(policy, null);
    }

    public Monitor(Policy policy, Logger logger) {
        this.queue = new Queue(PetriNet.NUM_TRANSITIONS, lock);
        this.policy = policy;
        this.logger = logger;
    }

    @Override
    public boolean fireTransition(int transition) {
        lock.lock();
        Transitions transitionEnum = Transitions.fromIndex(transition);
        try {
            while (true) {
                Set<Transitions> enabled = PetriNet.getEnabledTransitions();
                if (!enabled.contains(transitionEnum)) {
                    // No está habilitada por marcado, esperar en la cola normal (libera el lock adentro)
                    queue.acquire(transition);
                    continue; // Al despertar, volver a evaluar todo el bucle
                }
                
                // Habilitada por marcado. Controlar el tiempo mínimo (EFT = w_i + alpha)
                long now = System.currentTimeMillis();
                long w_i = PetriNet.getSensitizationTimestamp(transition);
                long alpha = transitionEnum.getAlpha();
                long eft = w_i + alpha;
                
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
                    // Al re-adquirir el lock, volver a evaluar en el siguiente ciclo (el marcado puede haber cambiado)
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
