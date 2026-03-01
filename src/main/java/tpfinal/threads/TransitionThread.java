package tpfinal.threads;

import java.util.concurrent.atomic.AtomicInteger;

import tpfinal.monitor.Monitor;
import tpfinal.rdp.Transitions;
import tpfinal.utils.Logger;

public class TransitionThread implements Runnable {

    private Monitor monitor;
    private Transitions[] secuencia;
    private Logger logger;
    private int fireAmount;
    private AtomicInteger counter;

    public TransitionThread(Monitor monitor, Transitions[] secuencia, Logger logger, int fireAmount) {
        this.monitor = monitor;
        this.secuencia = secuencia;
        this.logger = logger;
        this.fireAmount = fireAmount;
        this.counter = null;
    }

    public TransitionThread(Monitor monitor, Transitions[] secuencia, Logger logger, AtomicInteger counter, int target) {
        this.monitor = monitor;
        this.secuencia = secuencia;
        this.logger = logger;
        this.fireAmount = target;
        this.counter = counter;
    }

    @Override
    public void run() {
        if (counter == null) {
            for (int i = 0; i < fireAmount; i++) {
                for (Transitions t : secuencia) {
                    monitor.fireTransition(t.getIndex());
                    logger.log(t.getName());
                }
            }
        } else {
            while (counter.getAndIncrement() < fireAmount) {
                for (Transitions t : secuencia) {
                    monitor.fireTransition(t.getIndex());
                    logger.log(t.getName());
                }
            }
        }
    }
}
