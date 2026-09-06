package tpfinal.threads;

import tpfinal.rdp.Transitions;

/**
 * Enum que define las secuencias de transiciones asociadas a cada hilo de la simulación.
 * Permite mantener la lógica de comportamiento de cada hilo desacoplada de la clase principal.
 */
public enum ThreadSecuence {
    // Hilo Entrada: Toma dato de la cola de arribo y accede al buffer a través del bus
    Th0 (0, new Transitions[]{Transitions.T0, Transitions.T1}),
    // Hilo Procesamiento - Modo de complejidad media: dos etapas en las plazas P4 y P5
    Th1 (1, new Transitions[]{Transitions.T2, Transitions.T3, Transitions.T4}),
    // Hilo Procesamiento - Modo de complejidad simple: una sola etapa en la plaza P7
    Th2 (2, new Transitions[]{Transitions.T5, Transitions.T6}),
    // Hilo Procesamiento - Modo de complejidad alta: tres etapas en las plazas P8, P9 y P10
    Th3 (3, new Transitions[]{Transitions.T7, Transitions.T8, Transitions.T9, Transitions.T10}),
    // Hilo Salida: Retira el dato procesado del buffer de salida
    Th4 (4, new Transitions[]{Transitions.T11});

    private int id;
    private Transitions[] secuence;

    /**
     * Constructor del enum para mapear secuencias.
     * @param id Identificador numérico del hilo
     * @param secuence Array de transiciones que debe disparar secuencialmente
     */
    private ThreadSecuence(int id, Transitions[] secuence) {
        this.id = id;
        this.secuence = secuence;
    }

    /**
     * Obtiene la secuencia de transiciones correspondiente al identificador de hilo provisto.
     * 
     * @param id Identificador numérico del hilo
     * @return El array de transiciones correspondiente, o null si no se encuentra el ID
     */
    public static Transitions[] getSecuenceFromThreadId(int id) {
        for (ThreadSecuence threadSecuence : ThreadSecuence.values()) {
            if (threadSecuence.id == id) {
                return threadSecuence.secuence;
            }
        }
        return null; 
    }
}