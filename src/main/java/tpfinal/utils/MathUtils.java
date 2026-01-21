package tpfinal.utils;

public class MathUtils {
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
}
