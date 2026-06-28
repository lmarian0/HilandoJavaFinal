package tpfinal.policies;
import java.util.Set;
import tpfinal.rdp.Transitions;

/**
 * Implementación de la política de resolución de conflictos aleatoria.
 * Selecciona una transición al azar con distribución uniforme de entre el conjunto
 * de opciones habilitadas.
 */
public class RandomPolicy implements Policy {

    private String lastDecisionMessage = "";

    /**
     * Selecciona una transición aleatoria de entre las transiciones habilitadas.
     * 
     * @param enabledTransitions Conjunto de transiciones habilitadas y listas
     * @return El índice de la transición seleccionada aleatoriamente
     * @throws IllegalStateException si el conjunto de transiciones habilitadas está vacío
     */
    @Override
    public int selectTransition(Set<Transitions> enabledTransitions) {
        int size = enabledTransitions.size();
        // Genera un índice aleatorio entre 0 y size - 1
        int item = (int) (Math.random() * size);
        int i = 0;
        for (Transitions transition : enabledTransitions) {
            if (i == item) {
                lastDecisionMessage = "Selección aleatoria entre transiciones habilitadas.";
                return transition.getIndex();
            }
            i++;
        }
        throw new IllegalStateException("No hay transiciones habilitadas disponibles");
    }

    @Override
    public String getDecisionMessage() {
        return lastDecisionMessage;
    }
}
