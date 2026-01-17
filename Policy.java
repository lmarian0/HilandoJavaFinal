import java.util.List;

/**
 * Patrón Strategy para la selección de transiciones a disparar.
 * Permite diferentes políticas de decisión cuando hay múltiples transiciones sensibilizadas.
 */
public interface Policy {
    /**
     * Decide cuál transición disparar entre las sensibilizadas.
     * @param sensitized Lista de transiciones sensibilizadas
     * @return Índice de la transición seleccionada
     */
    int decide(List<Integer> sensitized);
}
