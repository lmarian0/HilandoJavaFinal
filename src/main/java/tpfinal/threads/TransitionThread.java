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

    /**
     * Constructor para hilos con carga de disparos estática (Entrada y Salida).
     * 
     * @param monitor    El monitor de concurrencia
     * @param secuencia  La secuencia de transiciones a disparar
     * @param fireAmount La cantidad fija de disparos/ciclos a realizar
     */
    public TransitionThread(Monitor monitor, Transitions[] secuencia, int fireAmount) {
        this.monitor = monitor;
        this.secuencia = secuencia;
        this.fireAmount = fireAmount;
        this.counter = null;
    }

    /**
     * Constructor para hilos de procesamiento en la unidad de procesamiento con balance dinámico de
     * cargas.
     * 
     * @param monitor   El monitor de concurrencia
     * @param secuencia La secuencia del modo de complejidad (simple, media o alta)
     * @param counter   El contador atómico compartido entre los hilos de procesamiento
     * @param target    La cantidad total de invariantes de procesamiento a procesar
     *                  en conjunto
     */
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
