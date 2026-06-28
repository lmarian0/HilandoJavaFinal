package tpfinal.threads;

import java.util.concurrent.atomic.AtomicInteger;

import tpfinal.monitor.Monitor;
import tpfinal.rdp.Transitions;

/**
 * Agente de ejecución (Hilo) que intenta disparar cíclicamente una secuencia
 * predefinida de transiciones en el Monitor de Concurrencia.
 * Puede finalizar por cantidad fija de ciclos propios o compartiendo un
 * contador
 * atómico con otros hilos.
 */
public class TransitionThread implements Runnable {

    // Monitor a través del cual se solicitan los disparos
    private Monitor monitor;
    // Secuencia ordenada de transiciones que este hilo debe disparar en cada ciclo
    private Transitions[] secuencia;
    // Cantidad objetivo de invariantes a disparar
    private int fireAmount;
    // Contador atómico compartido para el reparto dinámico de cargas en la unidad de procesamiento
    private AtomicInteger counter;
    // Trazador detallado opcional
    private tpfinal.utils.TraceLogger traceLogger;

    /**
     * Constructor para hilos con carga de disparos estática (Entrada y Salida).
     */
    public TransitionThread(Monitor monitor, Transitions[] secuencia, int fireAmount, tpfinal.utils.TraceLogger traceLogger) {
        this.monitor = monitor;
        this.secuencia = secuencia;
        this.fireAmount = fireAmount;
        this.counter = null;
        this.traceLogger = traceLogger;
    }

    /**
     * Constructor para hilos de procesamiento en la unidad de procesamiento con balance dinámico de cargas.
     */
    public TransitionThread(Monitor monitor, Transitions[] secuencia, AtomicInteger counter, int target, tpfinal.utils.TraceLogger traceLogger) {
        this.monitor = monitor;
        this.secuencia = secuencia;
        this.fireAmount = target;
        this.counter = counter;
        this.traceLogger = traceLogger;
    }

    @Override
    public void run() {
        // El logueo se realiza dentro del monitor (orden real de disparo),
        // por eso aquí solo se solicita el disparo de cada transición.
        try {
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
        } finally {
            if (traceLogger != null) {
                traceLogger.logFinished(traceLogger.getLabel());
            }
        }
    }
}
