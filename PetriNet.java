public class PetriNet {
    
    /**
     * Matriz de Incidencia (I = Pos - Pre).
     * Filas: Plazas (P0 a P11)
     * Columnas: Transiciones (T0 a T11)
     * Valores:
     * -1: La transición consume un token de la plaza (Pre)
     * +1: La transición pone un token en la plaza (Pos)
     * 0: No hay conexión
     */
        public static final int[][] INCIDENCE_MATRIX = {
            // T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11
            {-1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1}, // P0 (Cola Entrada)
            { 1, -1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0}, // P1 (Acceso Bus)
            {-1,  1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0}, // P2 (Bus - Recurso)
            { 0,  1, -1,  0,  0, -1,  0, -1,  0,  0,  0,  0}, // P3 (Buffer Proc)
            { 0,  0,  1, -1,  0,  0,  0,  0,  0,  0,  0,  0}, // P4 (Media 1)
            { 0,  0,  0,  1, -1,  0,  0,  0,  0,  0,  0,  0}, // P5 (Media 2)
            { 0,  0, -1,  0,  1, -1,  1, -1,  0,  0,  1,  0}, // P6 (CPU - Recurso)
            { 0,  0,  0,  0,  0,  1, -1,  0,  0,  0,  0,  0}, // P7 (Simple)
            { 0,  0,  0,  0,  0,  0,  0,  1, -1,  0,  0,  0}, // P8 (Alta 1)
            { 0,  0,  0,  0,  0,  0,  0,  0,  1, -1,  0,  0}, // P9 (Alta 2)
            { 0,  0,  0,  0,  0,  0,  0,  0,  0,  1, -1,  0}, // P10 (Alta 3)
            { 0,  0,  0,  0,  1,  0,  1,  0,  0,  0,  1, -1}  // P11 (Salida)
        };

        /**
         * Marcado Inicial (M0)
         * Basado en los puntos negros visibles en la Figura 1.
         * P0: 3 tokens (Arribos pendientes) [cite: 1]
         * P2: 1 token (El Bus está libre) [cite: 50]
         * P6: 1 token (La CPU está libre) [cite: 51]
         */
        public static final int[] INITIAL_MARKING = {3, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0};

        private int [][] incidenceMatrix = INCIDENCE_MATRIX;
        private int [] currentMarking = INITIAL_MARKING;
        private int transitions;

        public PetriNet(int [][] incidenceMatrix, int [] initialMarking) {
            this.incidenceMatrix = incidenceMatrix;
            this.currentMarking = initialMarking;
            this.transitions = incidenceMatrix.length;
        }

}