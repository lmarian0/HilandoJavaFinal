import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PetriNet {

    /**
     * Matriz de Incidencia (I = Pos - Pre).
     * Filas: Plazas (P0 a P11) -> Total 12
     * Columnas: Transiciones (T0 a T11) -> Total 12
    * [cite: 12, 39]
     */
    public static final int[][] INCIDENCE_MATRIX = {
        // T0, T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11
        {-1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  1}, // P0 (Cola Entrada) [cite: 57]
        { 1, -1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0}, // P1 (Acceso Bus) [cite: 58]
        {-1,  1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0}, // P2 (Bus - Recurso) [cite: 50]
        { 0,  1, -1,  0,  0, -1,  0, -1,  0,  0,  0,  0}, // P3 (Buffer Proc) [cite: 55]
        { 0,  0,  1, -1,  0,  0,  0,  0,  0,  0,  0,  0}, // P4 (Media 1)
        { 0,  0,  0,  1, -1,  0,  0,  0,  0,  0,  0,  0}, // P5 (Media 2)
        { 0,  0, -1,  0,  1, -1,  1, -1,  0,  0,  1,  0}, // P6 (CPU - Recurso) [cite: 51]
        { 0,  0,  0,  0,  0,  1, -1,  0,  0,  0,  0,  0}, // P7 (Simple) [cite: 59]
        { 0,  0,  0,  0,  0,  0,  0,  1, -1,  0,  0,  0}, // P8 (Alta 1)
        { 0,  0,  0,  0,  0,  0,  0,  0,  1, -1,  0,  0}, // P9 (Alta 2)
        { 0,  0,  0,  0,  0,  0,  0,  0,  0,  1, -1,  0}, // P10 (Alta 3)
        { 0,  0,  0,  0,  1,  0,  1,  0,  0,  0,  1, -1}  // P11 (Salida) [cite: 56]
    };

    /**
     * Marcado Inicial (M0).
     * P0: 3 tokens (Arribos)
     * P2: 1 token (Bus libre)
     * P6: 1 token (CPU libre)
     * [cite: 1, 39]
     */
    public static final int[] INITIAL_MARKING = {3, 0, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0};

    // Estado actual de la red
    private int[] currentMarking;
    
    // Mapa de tiempos asociados a cada transición (en milisegundos)
    private Map<Integer, Long> times;

    /**
     * Constructor: Inicializa el marcado copiando el vector inicial.
     */
    public PetriNet() {
        this.currentMarking = Arrays.copyOf(INITIAL_MARKING, INITIAL_MARKING.length);
        this.times = new HashMap<>();
        initializeTimes();
    }
    
    /**
     * Inicializa los tiempos de sleep para cada transición.
     * Estos tiempos simulan la duración de cada operación.
     */
    private void initializeTimes() {
        // Transiciones sin tiempo de espera (0ms)
        times.put(0, 0L);   // T0: Arribo
        times.put(1, 0L);   // T1: Tomar Bus
        times.put(11, 0L);  // T11: Salida
        
        // Transiciones con tiempos de procesamiento
        times.put(2, 100L);  // T2: Inicio proceso media
        times.put(3, 50L);   // T3: Media 1
        times.put(4, 50L);   // T4: Media 2 y fin
        times.put(5, 100L);  // T5: Inicio proceso simple
        times.put(6, 100L);  // T6: Fin simple
        times.put(7, 150L);  // T7: Inicio proceso alta
        times.put(8, 50L);   // T8: Alta 1
        times.put(9, 50L);   // T9: Alta 2
        times.put(10, 50L);  // T10: Alta 3 y fin
    }

    /**
     * Verifica si una transición está sensibilizada (Enabled).
     * Comprueba si hay suficientes tokens en las plazas de entrada.
     * Condición: Para todo P, (Marcado[P] + Incidencia[P][T]) >= 0
     * (Solo nos importa cuando Incidencia es negativa, es decir, consume tokens).
     */
    public boolean isEnabled(int transition) {
        for (int i = 0; i < INCIDENCE_MATRIX.length; i++) {
            // Si la transición consume tokens (valor < 0 en la matriz)
            if (INCIDENCE_MATRIX[i][transition] < 0) {
                // Si el marcado actual es menor que lo que necesito consumir
                if (currentMarking[i] < Math.abs(INCIDENCE_MATRIX[i][transition])) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Dispara una transición y actualiza el marcado actual.
     * Ecuación de Estado: M_{k+1} = M_k + I(t)
     * @param transition Índice de la transición a disparar.
     */
    public void fire(int transition) {
        if (!isEnabled(transition)) {
            throw new IllegalArgumentException("Intentando disparar una transición no sensibilizada: T" + transition);
        }

        // Actualizamos el marcado sumando la columna correspondiente de la matriz
        for (int i = 0; i < INCIDENCE_MATRIX.length; i++) {
            currentMarking[i] += INCIDENCE_MATRIX[i][transition];
        }
    }

    /**
     * Obtiene una lista con los índices de todas las transiciones que están
     * sensibilizadas en el estado actual.
     * Vital para que la clase Policy decida cuál disparar.
     */
    public List<Integer> getSensitized() {
        List<Integer> sensitized = new ArrayList<>();
        for (int i = 0; i < INCIDENCE_MATRIX[0].length; i++) {
            if (isEnabled(i)) {
                sensitized.add(i);
            }
        }
        return sensitized;
    }
    
    /**
     * Obtiene el tiempo de sleep asociado a una transición.
     * @param transition Índice de la transición
     * @return Tiempo en milisegundos
     */
    public long getSleepTime(int transition) {
        return times.getOrDefault(transition, 0L);
    }
    
    /**
     * Actualiza el marcado disparando una transición.
     * Alias para fire() para mantener compatibilidad con el diagrama UML.
     * @param transition Índice de la transición a disparar
     */
    public void updateMarking(int transition) {
        fire(transition);
    }

    /**
     * Devuelve una COPIA del marcado actual (para el Log y validaciones).
     * Se devuelve copia para evitar modificaciones externas accidentales.
     */
    public int[] getCurrentMarking() {
        return Arrays.copyOf(currentMarking, currentMarking.length);
    }
    
    /**
     * Método auxiliar para depuración.
     */
    public void printMarking() {
        System.out.print("Marcado: [");
        for (int i = 0; i < currentMarking.length; i++) {
            System.out.print(currentMarking[i] + (i < currentMarking.length - 1 ? ", " : ""));
        }
        System.out.println("]");
    }
}