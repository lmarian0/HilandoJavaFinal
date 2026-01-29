package tpfinal.rdp;
import tpfinal.utils.MathUtils;

import java.util.HashSet;
import java.util.Set;

import tpfinal.Exceptions.InvalidFireException;

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

    private static int[][] currentMarking = INITIAL_MARKING;
    public static final int NUM_TRANSITIONS = INCIDENCE_MATRIX[0].length;
    public static final int NUM_PLACES = INCIDENCE_MATRIX.length;


    /**
     * This method prints the given matrix to the console.
     * @param matrix The matrix to be printed
     */
    public static void printMatrix(int[][] matrix) {
        System.out.print("[ "); 
        
        for (int i = 0; i < matrix.length; i++) {
            System.out.print(matrix[i][0] + " ");
        }
        System.out.print("]"); 
    }

    /**
     * Calculates the next marking of the Petri net after firing a given transition
     * applying the foundamental equation:  M(i+1) = M(i) + I * S
     * @param transition The transition to be fired
     * @return The resulting marking matrix after firing the transition
     */
    public static int[][] getNextMarking(int transition) throws InvalidFireException {
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
        if(!isValidMarking(result)) {
            throw new InvalidFireException("Firing transition " + transition + " results in an invalid marking.");
        }
        return result;
    }

    private static boolean isValidMarking(int[][] matrix) {
    
        for (int i = 0; i < NUM_PLACES; i++) {  //before i<12
            if (matrix[i][0] < 0) {
                return false;
            }
        }
        return true;
    }

    public static void setCurrentMarking(int[][] newMarking) {
        currentMarking = newMarking;
    }

    /**
     * Fires the given transition if it's valid, updates the current marking,
     * and simulates the transition time.
     * @param transition The transition to be fired
     * @return true if the transition was successfully fired, false otherwise
     */
    public static boolean fire(Transitions transition) {
        int[][] nextMarking = currentMarking;
        try{
            nextMarking = getNextMarking(transition.getIndex());
            setCurrentMarking(nextMarking);
            return true;
        } catch (InvalidFireException e){
            return false;
        }
    }

    /**
     * Checks if a transition is enabled based on the current marking.
     * @param transition The transition to be checked
     * @return true if the transition is enabled, false otherwise
     */
    private static boolean isTransitionEnabled(int transition) {
        try {
            getNextMarking(transition);
            return true;
        } catch (InvalidFireException e) {
            return false;
        }
    }

    /**
     * Obtains the set of enabled transitions based on the current marking.
     * @return A set of enabled transitions
     */
    public static Set<Transitions> getEnabledTransitions() {
        Set<Transitions> enabledTransitions = new HashSet<>();
        for (int t = 0; t < NUM_TRANSITIONS; t++) {
            if(isTransitionEnabled(t)) {
                enabledTransitions.add(Transitions.fromIndex(t));
            }
        }
        return enabledTransitions;
    }
}
