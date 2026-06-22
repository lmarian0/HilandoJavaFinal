package tpfinal.threads;

import java.util.concurrent.atomic.AtomicInteger;

import tpfinal.monitor.Monitor;
import tpfinal.rdp.Transitions;

public class TransitionThread implements Runnable {

    private Monitor monitor;
    private Transitions[] secuencia;
    private int fireAmount;
    private AtomicInteger counter;

    public TransitionThread(Monitor monitor, Transitions[] secuencia, int fireAmount) {
        this.monitor = monitor;
        this.secuencia = secuencia;
        this.fireAmount = fireAmount;
        this.counter = null;
    }

    public TransitionThread(Monitor monitor, Transitions[] secuencia, AtomicInteger counter, int target) {
        this.monitor = monitor;
        this.secuencia = secuencia;
        this.fireAmount = target;
        this.counter = counter;
    }

    @Override
    public void run() {
        // El logueo se realiza dentro del monitor (orden real de disparo),
        // por eso aquí solo se solicita el disparo de cada transición.
        if (counter == null) {
            for (int i = 0; i < fireAmount; i++) {
                for (Transitions t : secuencia) {
                    monitor.fireTransition(t.getIndex());
                }
            }
        } else {
            while (counter.getAndIncrement() < fireAmount) {
                for (Transitions t : secuencia) {
                    monitor.fireTransition(t.getIndex());
                }
            }
        }
    }
}
