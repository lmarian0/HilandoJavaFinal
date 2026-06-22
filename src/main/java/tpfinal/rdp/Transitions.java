package tpfinal.rdp;

public enum Transitions {
    // Transiciones temporales (enunciado): T1, T3, T4, T6, T8, T9, T10 -> tiempo > 0 (ms)
    // Transiciones inmediatas: T0, T2, T5, T7, T11 -> tiempo 0
    T0(0, "T0", 0),
    T1(1, "T1", 25),
    T2(2, "T2", 0),
    T3(3, "T3", 180),
    T4(4, "T4", 180),
    T5(5, "T5", 0),
    T6(6, "T6", 360),
    T7(7, "T7", 0),
    T8(8, "T8", 120),
    T9(9, "T9", 120),
    T10(10, "T10", 120),
    T11(11, "T11", 0);

    private final int index;
    private final String name;
    private final int time;

    private Transitions(int index, String name, int time) {
        this.index = index;
        this.name = name;
        this.time = time;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public int getTime() {
        return time;
    }

    public static Transitions fromIndex(int index) {
        Transitions[] transitions = Transitions.values();

        if(index < 0 || index >= transitions.length) {
            throw new IllegalArgumentException("Invalid transition index: " + index);
        }
        return transitions[index];
        
    }
}