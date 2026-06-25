package tpfinal.monitor;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import tpfinal.rdp.Transitions;

/**
 * Gestor de colas de espera asociadas al monitor.
 * Administra un array de variables de condición (una Condition por transición)
 * y realiza el seguimiento del número de hilos suspendidos en cada una.
 */
public class Queue {

    // Variables de condición (una cola de espera por cada transición)
    private final Condition[] conditions;
    // Contador de hilos suspendidos esperando por cada transición
    private final int[] waitingCounts;

    /**
     * Construye un gestor de colas inicializando las variables de condición.
     * @param transitionsNumber Número total de transiciones en la red
     * @param monitorLock Lock del monitor necesario para crear las condiciones
     */
    public Queue(int transitionsNumber, ReentrantLock monitorLock) {
        this.conditions = new Condition[transitionsNumber];
        this.waitingCounts = new int[transitionsNumber];
        for (int i = 0; i < transitionsNumber; i++) {
            conditions[i] = monitorLock.newCondition();
            waitingCounts[i] = 0;
        }
    }

    /**
     * Suspende pasivamente al hilo actual en la cola asociada a la transición indicada.
     * Al ejecutarse await(), el hilo libera automáticamente el lock del monitor.
     * 
     * @param transition El índice de la transición en cuya cola se suspenderá el hilo
     */
    public void acquire(int transition) {
        try {
            waitingCounts[transition]++;
            conditions[transition].await(); // Espera pasiva liberando el lock
            waitingCounts[transition]--;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Despierta a un hilo que esté esperando en la cola asociada a la transición indicada.
     * Solo envía la señal si hay al menos un hilo durmiendo en esa condición.
     * 
     * @param transition El índice de la transición cuyo hilo en espera se desea despertar
     */
    public void release(int transition) {
        if (waitingCounts[transition] > 0) {
            conditions[transition].signal(); // Envía señal para despertar un hilo
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
