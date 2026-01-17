import java.util.List;

/**
 * Política que selecciona la transición con menor índice (mayor prioridad).
 */
public class PriorityPolicy implements Policy {
    
    @Override
    public int decide(List<Integer> sensitized) {
        if (sensitized == null || sensitized.isEmpty()) {
            throw new IllegalArgumentException("No hay transiciones sensibilizadas para elegir");
        }
        // Selecciona la transición con menor índice (mayor prioridad)
        return sensitized.stream().min(Integer::compare).get();
    }
}
