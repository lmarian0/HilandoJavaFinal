package tpfinal.utils;

import java.util.List;

/**
 * Resultado inmutable de la validación de una traza.
 * Guarda el estado, los contadores de T-invariantes y las transiciones residuales.
 */
public final class ValidationResult {
    /** Indica si se detectó al menos un ciclo y la reducción consumió toda la traza. */
    private final boolean valid;
    /** Cantidad de T-invariantes del camino medio. */
    private final int mediumCount;
    /** Cantidad de T-invariantes del camino simple. */
    private final int simpleCount;
    /** Cantidad de T-invariantes del camino alto. */
    private final int highCount;
    /** Copia inmutable de las transiciones no consumidas. */
    private final List<String> remainingTransitions;

    /**
     * Inicializa el resultado de una reducción.
     *
     * @param valid indica si se encontró al menos un ciclo y no quedó residuo
     * @param mediumCount cantidad de invariantes de complejidad media
     * @param simpleCount cantidad de invariantes de complejidad simple
     * @param highCount cantidad de invariantes de complejidad alta
     * @param remainingTransitions transiciones no consumidas por la reducción
     * @throws IllegalArgumentException si algún contador es negativo
     * @throws NullPointerException si la lista residual o alguno de sus elementos es null
     */
    public ValidationResult(
            boolean valid,
            int mediumCount,
            int simpleCount,
            int highCount,
            List<String> remainingTransitions) {
        if (mediumCount < 0 || simpleCount < 0 || highCount < 0) {
            throw new IllegalArgumentException("Los contadores no pueden ser negativos");
        }

        this.valid = valid;
        this.mediumCount = mediumCount;
        this.simpleCount = simpleCount;
        this.highCount = highCount;
        this.remainingTransitions = List.copyOf(remainingTransitions);
    }

    /**
     * Indica si la traza completa cumplió los T-invariantes.
     *
     * @return true si se detectó al menos un ciclo y la reducción quedó sin residuo
     */
    public boolean isValid() {
        return valid;
    }

    /**
     * Obtiene el total del camino de complejidad media.
     *
     * @return cantidad de invariantes de complejidad media detectados
     */
    public int getMediumCount() {
        return mediumCount;
    }

    /**
     * Obtiene el total del camino de complejidad simple.
     *
     * @return cantidad de invariantes de complejidad simple detectados
     */
    public int getSimpleCount() {
        return simpleCount;
    }

    /**
     * Obtiene el total del camino de complejidad alta.
     *
     * @return cantidad de invariantes de complejidad alta detectados
     */
    public int getHighCount() {
        return highCount;
    }

    /**
     * Calcula el total de ciclos completos detectados.
     *
     * @return suma de los invariantes medios, simples y altos detectados
     */
    public int getTotalMatches() {
        return mediumCount + simpleCount + highCount;
    }

    /**
     * Expone el residuo estructurado de la reducción.
     *
     * @return lista inmutable de transiciones no consumidas por la reducción
     */
    public List<String> getRemainingTransitions() {
        return remainingTransitions;
    }

    /**
     * Calcula el tamaño del residuo estructurado.
     *
     * @return cantidad de transiciones no consumidas por la reducción
     */
    public int getRemainingCount() {
        return remainingTransitions.size();
    }

    /**
     * Mantiene el formato compacto utilizado por la salida humana existente.
     *
     * @return transiciones residuales concatenadas sin separador
     */
    public String getRemainingTrace() {
        return String.join("", remainingTransitions);
    }

    /**
     * Construye una representación completa para diagnóstico y mensajes de
     * error en los tests.
     *
     * @return estado, contadores y transiciones residuales del resultado
     */
    @Override
    public String toString() {
        return "ValidationResult{" +
                "valid=" + valid +
                ", mediumCount=" + mediumCount +
                ", simpleCount=" + simpleCount +
                ", highCount=" + highCount +
                ", totalMatches=" + getTotalMatches() +
                ", remainingCount=" + getRemainingCount() +
                ", remainingTransitions=" + remainingTransitions +
                '}';
    }
}
