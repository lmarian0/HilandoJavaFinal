package tpfinal.rdp;

public enum Transitions {
    T0(0, "T0", 0),
    T1(1, "T1", 0),
    T2(2, "T2", 0),
    T3(3, "T3", 0),
    T4(4, "T4", 0),
    T5(5, "T5", 5),
    T6(6, "T6", 7),
    T7(7, "T7", 7),
    T8(8, "T8", 7),
    T9(9, "T9", 9),
    T10(10, "T10", 5),
    T11(11, "T11", 4);

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