package tpfinal.rdp;

/**
 * Representación en Enum de las transiciones de la Red de Petri.
 * Cada transición tiene asociada una ventana de tiempo [alpha, beta] expresada
 * en milisegundos,
 * donde alpha (EFT) es el tiempo mínimo de sensibilización y beta (LFT) es el
 * tiempo máximo (infinito).
 */
public enum Transitions {
    // Ventana de Tiempo [alpha, beta]: alpha = EFT (tiempo mínimo de
    // sensibilización en ms), beta = LFT (tiempo máximo = Long.MAX_VALUE)
    // Transiciones inmediatas (alpha = 0 ms): T0, T2, T5, T7, T11
    // Transiciones temporales (alpha = 75 ms): T1, T3, T4, T6, T8, T9, T10
    T0(0, "T0", 0, Long.MAX_VALUE),
    T1(1, "T1", 75, Long.MAX_VALUE),
    T2(2, "T2", 0, Long.MAX_VALUE),
    T3(3, "T3", 75, Long.MAX_VALUE),
    T4(4, "T4", 75, Long.MAX_VALUE),
    T5(5, "T5", 0, Long.MAX_VALUE),
    T6(6, "T6", 75, Long.MAX_VALUE),
    T7(7, "T7", 0, Long.MAX_VALUE),
    T8(8, "T8", 75, Long.MAX_VALUE),
    T9(9, "T9", 75, Long.MAX_VALUE),
    T10(10, "T10", 75, Long.MAX_VALUE),
    T11(11, "T11", 0, Long.MAX_VALUE);

    private final int index;
    private final String name;
    private final long alpha;
    private final long beta;

    /**
     * Constructor privado del enum de transiciones.
     * 
     * @param index Índice numérico de la transición
     * @param name  Nombre de la transición
     * @param alpha Tiempo mínimo de sensibilización (EFT)
     * @param beta  Tiempo máximo de sensibilización (LFT)
     */
    private Transitions(int index, String name, long alpha, long beta) {
        this.index = index;
        this.name = name;
        this.alpha = alpha;
        this.beta = beta;
    }

    /**
     * Obtiene el índice numérico de la transición.
     * 
     * @return índice de la transición
     */
    public int getIndex() {
        return index;
    }

    /**
     * Obtiene el nombre textual de la transición.
     * 
     * @return nombre de la transición
     */
    public String getName() {
        return name;
    }

    /**
     * Obtiene el tiempo mínimo de sensibilización (EFT).
     * 
     * @return tiempo alpha en milisegundos
     */
    public long getAlpha() {
        return alpha;
    }

    /**
     * Obtiene el tiempo máximo de sensibilización (LFT).
     * 
     * @return tiempo beta en milisegundos
     */
    public long getBeta() {
        return beta;
    }

    /**
     * Alias de compatibilidad para obtener el tiempo mínimo alpha.
     * 
     * @return tiempo alpha en milisegundos
     */
    public long getTime() {
        return alpha;
    }

    /**
     * Retorna la transición correspondiente al índice numérico provisto.
     * 
     * @param index Índice de la transición (0 a 11)
     * @return El enum correspondiente de la transición
     * @throws IllegalArgumentException si el índice está fuera del rango permitido
     */
    public static Transitions fromIndex(int index) {
        Transitions[] transitions = Transitions.values();

        if (index < 0 || index >= transitions.length) {
            throw new IllegalArgumentException("Índice de transición no válido: " + index);
        }
        return transitions[index];
    }
}