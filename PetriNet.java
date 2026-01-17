
public class PetriNet {

    /**
     * Matriz de Incidencia (I = Pos - Pre). Filas: Plazas (P0 a P11) Columnas:
     * Transiciones (T0 a T11) Valores: -1: La transición consume un token de la
     * plaza (Pre) +1: La transición pone un token en la plaza (Pos) 0: No hay
     * conexión
     */
    public static final int[][] INCIDENCE_MATRIX = {
        // T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11
        {-1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}, // P0 (Cola Entrada)
        {1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // P1 (Acceso Bus)
        {-1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, // P2 (Bus - Recurso)
        {0, 1, -1, 0, 0, -1, 0, -1, 0, 0, 0, 0}, // P3 (Buffer Proc)
        {0, 0, 1, -1, 0, 0, 0, 0, 0, 0, 0, 0}, // P4 (Media 1)
        {0, 0, 0, 1, -1, 0, 0, 0, 0, 0, 0, 0}, // P5 (Media 2)
        {0, 0, -1, 0, 1, -1, 1, -1, 0, 0, 1, 0}, // P6 (CPU - Recurso)
        {0, 0, 0, 0, 0, 1, -1, 0, 0, 0, 0, 0}, // P7 (Simple)
        {0, 0, 0, 0, 0, 0, 0, 1, -1, 0, 0, 0}, // P8 (Alta 1)
        {0, 0, 0, 0, 0, 0, 0, 0, 1, -1, 0, 0}, // P9 (Alta 2)
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, -1, 0}, // P10 (Alta 3)
        {0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, -1} // P11 (Salida)
    };

    /**
     * Marcado Inicial (M0) Basado en los puntos negros visibles en la Figura 1.
     * P0: 3 tokens (Arribos pendientes) [cite: 1] P2: 1 token (El Bus está
     * libre) [cite: 50] P6: 1 token (La CPU está libre) [cite: 51]
     */
    public static final int[] INITIAL_MARKING = {3, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0};

    private int[][] incidenceMatrix = INCIDENCE_MATRIX;
    private int[] currentMarking = INITIAL_MARKING;
    private int transitions;

    public PetriNet(int[][] incidenceMatrix, int[] initialMarking) {
        this.incidenceMatrix = incidenceMatrix;
        this.currentMarking = initialMarking;
        this.transitions = incidenceMatrix.length;
    }

    public static int[][] multiplyMatrix(int[][] m1, int[][] m2) {
        if (m1[0].length != m2.length) {
            throw new IllegalArgumentException("The number of columns of the first matrix must be equal to the number of rows of the second matrix");
        }

        int filasA = m1.length;
        int columnasA = m1[0].length;
        int columnasB = m2[0].length;

        int[][] result = new int[filasA][columnasB];

        for (int i = 0; i < filasA; i++) {
            for (int j = 0; j < columnasB; j++) {
                result[i][j] = 0;
                for (int k = 0; k < columnasA; k++) {
                    result[i][j] += m1[i][k] * m2[k][j];
                }
            }
        }

        return result;
    }

    public static int[][] addMatrix(int[][] m1, int[][] m2) {
        if (m1.length != m2.length || m1[0].length != m2[0].length) {
            throw new IllegalArgumentException("Matrixs must have the same dimensions for addition.");
        }

        int filas = m1.length;
        int columnas = m1[0].length;
        int[][] result = new int[filas][columnas];

        // Sumar elemento por elemento
        for (int i = 0; i < filas; i++) {
            for (int j = 0; j < columnas; j++) {
                result[i][j] = m1[i][j] + m2[i][j];
            }
        }

        return result;
    }

    public static void imprimirMatriz(int[][] matriz) {
        for (int i = 0; i < matriz.length; i++) {
            for (int j = 0; j < matriz[i].length; j++) {
                System.out.print(matriz[i][j] + " ");
            }
            System.out.println();
        }
    }

    public static int[][] getNextMarking(int transition) {
        int[][] fireMatrix = new int[INCIDENCE_MATRIX[0].length][1];

        for (int i = 0; i < 1; i++) {
            for (int j = 0; j < 14; j++) {
                if (j == transition) {
                    fireMatrix[j][i] = 1;
                } else {
                    fireMatrix[j][i] = 0;
                }
            }
        }

        int[] multiply = PetriNet.multiplyMatrix(PetriNet.INCIDENCE_MATRIX, fireMatrix);
    }
}
    

}
