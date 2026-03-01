package tpfinal.threads;

import tpfinal.rdp.Transitions;

public enum ThreadSecuence {
    Th0 (0, new Transitions[]{Transitions.T0, Transitions.T1}),
    Th1 (1, new Transitions[]{Transitions.T2, Transitions.T3, Transitions.T4}),
    Th2 (2, new Transitions[]{Transitions.T5, Transitions.T6}),
    Th3 (3, new Transitions[]{Transitions.T7, Transitions.T8, Transitions.T9, Transitions.T10}),
    Th4 (4, new Transitions[]{Transitions.T11});

    private int id;
    private Transitions[] secuence;

    private ThreadSecuence(int id, Transitions[] secuence) {
        this.id = id;
        this.secuence = secuence;
    }

    public static Transitions[] getSecuenceFromThreadId(int id) {
        for (ThreadSecuence threadSecuence : ThreadSecuence.values()) {
            if (threadSecuence.id == id) {
                return threadSecuence.secuence;
            }
        }
        return null; // o lanzar una excepción si el ID no es válido
    }
}