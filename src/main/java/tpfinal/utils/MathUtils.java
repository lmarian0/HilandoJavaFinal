package tpfinal.utils;
import java.util.HashSet;
import java.util.Set;
import tpfinal.rdp.Transitions;

/**
 * Biblioteca de funciones matemáticas auxiliares para álgebra de matrices
 * y operaciones de conjuntos asociadas a la Red de Petri.
 */
public class MathUtils {

    /**
     * Multiplica dos matrices de enteros (m1 * m2).
     * 
     * @param m1 Primera matriz
     * @param m2 Segunda matriz
     * @return Matriz producto resultante
     * @throws IllegalArgumentException si el número de columnas de m1 es distinto al número de filas de m2
     */
    public static int[][] multiplyMatrix(int[][] m1, int[][] m2) {
        if (m1[0].length != m2.length) {
            throw new IllegalArgumentException("El número de columnas de la primera matriz debe ser igual al número de filas de la segunda matriz.");
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

    /**
     * Suma dos matrices de enteros de igual dimensión elemento por elemento (m1 + m2).
     * 
     * @param m1 Primera matriz
     * @param m2 Segunda matriz
     * @return Matriz suma resultante
     * @throws IllegalArgumentException si las matrices no tienen las mismas dimensiones
     */
    public static int[][] addMatrix(int[][] m1, int[][] m2) {
        if (m1.length != m2.length || m1[0].length != m2[0].length) {
            throw new IllegalArgumentException("Las matrices deben tener las mismas dimensiones para poder sumarse.");
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

    /**
     * Calcula la intersección de dos conjuntos de transiciones.
     * Útil para contrastar transiciones habilitadas contra transiciones con hilos esperando.
     * 
     * @param set1 Primer conjunto
     * @param set2 Segundo conjunto
     * @return El conjunto intersección resultante
     */
    public static Set<Transitions> intersectSets(Set<Transitions> set1, Set<Transitions> set2) {
        Set<Transitions> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        return intersection;
    }
}
