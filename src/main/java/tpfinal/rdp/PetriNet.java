package tpfinal.rdp;
import tpfinal.utils.MathUtils;

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
    public static final int[][] INITIAL_MARKING = {{3}, {0}, {1}, {0}, {0}, {0}, {1}, {0}, {0}, {0}, {0}, {0}};

    private int[][] incidenceMatrix = INCIDENCE_MATRIX;
    private static int[][] currentMarking = INITIAL_MARKING;
    public static final int NUM_TRANSITIONS = INCIDENCE_MATRIX[0].length;
    public static final int NUM_PLACES = INCIDENCE_MATRIX.length;


    public static void printMatrix(int[][] matrix) {
        System.out.print("[ "); 
        
        for (int i = 0; i < matrix.length; i++) {
            System.out.print(matrix[i][0] + " ");
        }
        System.out.print("]"); 
    }

    public static int[][] getNextMarking(int transition) {
        int[][] fireMatrix = new int[NUM_TRANSITIONS][1];

        // before j < 14
        for (int j = 0; j < NUM_TRANSITIONS; j++) {
                if (j == transition) {
                    fireMatrix[j][0] = 1;
                } else {
                    fireMatrix[j][0] = 0;
                }
            }

        int[][] multiply = MathUtils.multiplyMatrix(PetriNet.INCIDENCE_MATRIX, fireMatrix);
        return multiply;
    }

    public static int[][] nextIncidentMatrix(int transition) {
        int[][] W = INCIDENCE_MATRIX;
        int[][] m_i = currentMarking;
        int[][] s = new int[NUM_TRANSITIONS][1]; //before [12][1]
            for(int j=0; j<NUM_TRANSITIONS; j++){ //before j<12
                if(j == transition){
                    s[j][0] = 1; 
                } else {
                    s[j][0] = 0;
                }
            }

        int[][] mult = MathUtils.multiplyMatrix(W, s);
        int[][] result = MathUtils.addMatrix(m_i, mult);
        currentMarking = result;

        return result;
    }

    public static boolean willContinue(int[][] matrix) {
    
        int[][] nextMarking = matrix;
        for (int i = 0; i < NUM_TRANSITIONS; i++) {  //before i<12
            if (nextMarking[i][0] < 0) {
                return true;
            }
        }
        return false;
    }
}
