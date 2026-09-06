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
            { -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1 }, // P0  (Cola de arribo de datos)
            { 1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, // P1  (Dato accediendo al buffer a través del bus)
            { -1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }, // P2  (Bus de acceso al buffer - Recurso)
            { 0, 1, -1, 0, 0, -1, 0, -1, 0, 0, 0, 0 }, // P3  (Buffer de los datos a procesar)
            { 0, 0, 1, -1, 0, 0, 0, 0, 0, 0, 0, 0 }, // P4  (Complejidad media - Etapa 1)
            { 0, 0, 0, 1, -1, 0, 0, 0, 0, 0, 0, 0 }, // P5  (Complejidad media - Etapa 2)
            { 0, 0, -1, 0, 1, -1, 1, -1, 0, 0, 1, 0 }, // P6  (Unidad de procesamiento - Recurso)
            { 0, 0, 0, 0, 0, 1, -1, 0, 0, 0, 0, 0 }, // P7  (Complejidad simple - Etapa única)
            { 0, 0, 0, 0, 0, 0, 0, 1, -1, 0, 0, 0 }, // P8  (Complejidad alta - Etapa 1)
            { 0, 0, 0, 0, 0, 0, 0, 0, 1, -1, 0, 0 }, // P9  (Complejidad alta - Etapa 2)
            { 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, -1, 0 }, // P10 (Complejidad alta - Etapa 3)
            { 0, 0, 0, 0, 1, 0, 1, 0, 0, 0, 1, -1 } // P11 (Buffer de salida de los datos)
    };

    /**
     * Marcado Inicial (M0) basado en la Figura de la Red de Petri.
     * P0: 3 tokens (cola de arribo de datos con 3 datos pendientes)
     * P2: 1 token (bus de acceso al buffer libre)
     * P6: 1 token (unidad de procesamiento libre)
     */
    public static final int[][] INITIAL_MARKING = { { 3 }, { 0 }, { 1 }, { 0 }, { 0 }, { 0 }, { 1 }, { 0 }, { 0 },
            { 0 }, { 0 }, { 0 } };

    private static int[][] currentMarking = deepCopy(INITIAL_MARKING);
    public static final int NUM_TRANSITIONS = INCIDENCE_MATRIX[0].length;
    public static final int NUM_PLACES = INCIDENCE_MATRIX.length;

    private static final long[] sensitizationTimestamps = new long[NUM_TRANSITIONS];

    static {
        long now = System.currentTimeMillis();
        Set<Transitions> enabled = getEnabledTransitions();
        for (int t = 0; t < NUM_TRANSITIONS; t++) {
            sensitizationTimestamps[t] = enabled.contains(Transitions.fromIndex(t)) ? now : -1;
        }
    }

    /**
     * Invariantes de plaza (P-Invariantes) obtenidos con PIPE.
     * Cada fila es el vector de coeficientes sobre las plazas P0..P11.
     *   PI1: P0+P1+P3+P4+P5+P7+P8+P9+P10+P11 = 3  (conservación de datos en el sistema)
     *   PI2: P1+P2 = 1                              (bus de acceso al buffer, recurso compartido)
     *   PI3: P4+P5+P6+P7+P8+P9+P10 = 1              (unidad de procesamiento, recurso compartido)
     */
    private static final int[][] P_INVARIANTS = {
            // P0 P1 P2 P3 P4 P5 P6 P7 P8 P9 P10 P11
            { 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 1, 1 },
            { 0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
            { 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 0 }
    };
    private static final int[] P_INVARIANTS_CONST = { 3, 1, 1 };

    // Copia el marcado inicial para no modificar su referencia
    private static int[][] deepCopy(int[][] matrix) {
        int[][] copy = new int[matrix.length][];
        for (int i = 0; i < matrix.length; i++) {
            copy[i] = matrix[i].clone();
        }
        return copy;
    }

    /**
     * Imprime en consola el marcado o vector columna provisto.
     * 
     * @param matrix Vector o matriz a imprimir
     */
    public static void printMatrix(int[][] matrix) {
        System.out.print("[ ");

        for (int i = 0; i < matrix.length; i++) {
            System.out.print(matrix[i][0] + " ");
        }
        System.out.print("]");
    }

    /**
     * Calcula el próximo marcado potencial tras disparar una transición
     * determinada,
     * aplicando la Ecuación Fundamental de Estado: M(i+1) = M(i) + I * S
     * 
     * @param transition Índice de la transición que se desea disparar
     * @return El marcado resultante en formato de vector columna
     * @throws InvalidFireException si el marcado resultante viola restricciones
     *                              físicas (valores negativos)
     */
    public static int[][] getNextMarking(int transition) throws InvalidFireException {
        int[][] W = INCIDENCE_MATRIX;
        int[][] m_i = currentMarking;
        int[][] s = new int[NUM_TRANSITIONS][1];
        for (int j = 0; j < NUM_TRANSITIONS; j++) {
            if (j == transition) {
                s[j][0] = 1;
            } else {
                s[j][0] = 0;
            }
        }

        int[][] mult = MathUtils.multiplyMatrix(W, s);
        int[][] result = MathUtils.addMatrix(m_i, mult);
        if (!isValidMarking(result)) {
            throw new InvalidFireException("Disparar la transición " + transition + " genera un marcado no válido.");
        }
        return result;
    }

    /**
     * Determina si el marcado resultante es físicamente posible (sin marcas
     * negativas).
     */
    private static boolean isValidMarking(int[][] matrix) {
        for (int i = 0; i < NUM_PLACES; i++) {
            if (matrix[i][0] < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Establece el marcado actual de la Red de Petri.
     * 
     * @param newMarking Nuevo vector de marcado
     */
    public static void setCurrentMarking(int[][] newMarking) {
        currentMarking = newMarking;
    }

    /**
     * Ejecuta el disparo de una transición si esta es válida por marcado.
     * Modifica el marcado actual de la red, verifica invariantes de plaza y
     * recalcula los timestamps de sensibilización de todas las transiciones.
     * 
     * @param transition El enum de la transición a disparar
     * @return true si el disparo fue exitoso y el estado se actualizó, false en
     *         caso contrario
     */
    public static boolean fire(Transitions transition) {
        int[][] nextMarking = currentMarking;
        Set<Transitions> wasEnabled = getEnabledTransitions();
        try {
            nextMarking = getNextMarking(transition.getIndex());
            setCurrentMarking(nextMarking);
            // Verificar P-invariantes luego de cada disparo
            verifyPInvariants(transition);

            // Actualizar timestamps de sensibilización para la ventana de tiempo
            long now = System.currentTimeMillis();
            Set<Transitions> nowEnabled = getEnabledTransitions();
            for (int t = 0; t < NUM_TRANSITIONS; t++) {
                Transitions trans = Transitions.fromIndex(t);
                if (nowEnabled.contains(trans)) {
                    // Si se acaba de sensibilizar, o si es la misma que se disparó
                    // (resensibilización)
                    if (!wasEnabled.contains(trans) || trans == transition) {
                        sensitizationTimestamps[t] = now;
                    }
                } else {
                    // Si dejó de estar habilitada, limpiar el timestamp
                    sensitizationTimestamps[t] = -1;
                }
            }
            return true;
        } catch (InvalidFireException e) {
            return false;
        }
    }

    /**
     * Obtiene el milisegundo en el que se sensibilizó una transición por marcado.
     * 
     * @param index Índice de la transición
     * @return Marca de tiempo (System.currentTimeMillis) o -1 si no está habilitada
     */
    public static long getSensitizationTimestamp(int index) {
        return sensitizationTimestamps[index];
    }

    /**
     * Verifica que el marcado actual cumpla con TODOS los invariantes de plaza.
     * Se ejecuta luego de cada disparo exitoso. Si alguno
     * no se cumple, la red habría perdido una propiedad estructural, por lo que
     * se aborta la ejecución para evidenciar el error.
     * 
     * @param transition transición recién disparada (solo para el mensaje de error)
     * @return true si todos los P-invariantes se cumplen
     */
    public static boolean verifyPInvariants(Transitions transition) {
        for (int inv = 0; inv < P_INVARIANTS.length; inv++) {
            int sum = 0;
            for (int p = 0; p < NUM_PLACES; p++) {
                sum += P_INVARIANTS[inv][p] * currentMarking[p][0];
            }
            if (sum != P_INVARIANTS_CONST[inv]) {
                String msg = "VIOLACION P-Invariante " + (inv + 1) + " tras disparar "
                        + transition.getName() + ": suma=" + sum
                        + " (esperado " + P_INVARIANTS_CONST[inv] + ")";
                System.err.println(msg);
                throw new IllegalStateException(msg);
            }
        }
        return true;
    }

    /**
     * Comprueba si una transición está habilitada por marcado.
     * 
     * @param transition Índice de la transición a comprobar
     * @return true si está habilitada, false en caso contrario
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
     * Obtiene el conjunto completo de transiciones actualmente habilitadas por
     * marcado.
     * 
     * @return Un conjunto (Set) con las transiciones habilitadas
     */
    public static Set<Transitions> getEnabledTransitions() {
        Set<Transitions> enabledTransitions = new HashSet<>();
        for (int t = 0; t < NUM_TRANSITIONS; t++) {
            if (isTransitionEnabled(t)) {
                enabledTransitions.add(Transitions.fromIndex(t));
            }
        }
        return enabledTransitions;
    }
}
