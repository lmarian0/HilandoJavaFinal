package tpfinal.monitor;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import tpfinal.rdp.Transitions;

public class Queue {
    
    private final Condition[] conditions;
    private final int[] waitingCounts;
    private final ReentrantLock monitorLock;

    public Queue(int transitionsNumber, ReentrantLock monitorLock) {
        this.conditions = new Condition[transitionsNumber];
        this.waitingCounts = new int[transitionsNumber];
        this.monitorLock = monitorLock;
        for (int i = 0; i < transitionsNumber; i++) {
            conditions[i] = monitorLock.newCondition();
            waitingCounts[i] = 0;
        }
    }

    /**
     * Sleep the thread associated with the given transition
     * @param transition The transition whose thread will be put to sleep
     */
    public void acquire(int transition) {
        try {
            waitingCounts[transition]++;
            conditions[transition].await();
            waitingCounts[transition]--;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Wake up a thread associated with the given transition
     * @param transition The transition whose thread will be woken up
     */
    public void release(int transition) {
        if (waitingCounts[transition] > 0) {
            conditions[transition].signal();
        }
    }

    /**
     * Get a Set<Boolean> indicating which transitions have waiting threads
     * @return A set where each element indicates if there are waiting threads for that transition
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
