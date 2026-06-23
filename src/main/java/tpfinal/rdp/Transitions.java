package tpfinal.rdp;

public enum Transitions {
    // Ventana de Tiempo [alpha, beta]: alpha = EFT (tiempo mínimo de sensibilización en ms), beta = LFT (tiempo máximo = Long.MAX_VALUE)
    // Transiciones inmediatas (alpha = 0 ms): T0, T2, T5, T7, T11
    // Transiciones temporales (alpha = 70 ms): T1, T3, T4, T6, T8, T9, T10
    T0(0, "T0", 0, Long.MAX_VALUE),
    T1(1, "T1", 70, Long.MAX_VALUE),
    T2(2, "T2", 0, Long.MAX_VALUE),
    T3(3, "T3", 70, Long.MAX_VALUE),
    T4(4, "T4", 70, Long.MAX_VALUE),
    T5(5, "T5", 0, Long.MAX_VALUE),
    T6(6, "T6", 70, Long.MAX_VALUE),
    T7(7, "T7", 0, Long.MAX_VALUE),
    T8(8, "T8", 70, Long.MAX_VALUE),
    T9(9, "T9", 70, Long.MAX_VALUE),
    T10(10, "T10", 70, Long.MAX_VALUE),
    T11(11, "T11", 0, Long.MAX_VALUE);

    private final int index;
    private final String name;
    private final long alpha;
    private final long beta;

    private Transitions(int index, String name, long alpha, long beta) {
        this.index = index;
        this.name = name;
        this.alpha = alpha;
        this.beta = beta;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public long getAlpha() {
        return alpha;
    }

    public long getBeta() {
        return beta;
    }

    public long getTime() {
        return alpha;
    }

    public static Transitions fromIndex(int index) {
        Transitions[] transitions = Transitions.values();

        if(index < 0 || index >= transitions.length) {
            throw new IllegalArgumentException("Invalid transition index: " + index);
        }
        return transitions[index];
    }
}