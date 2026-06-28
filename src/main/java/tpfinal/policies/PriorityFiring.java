package tpfinal.policies;
import tpfinal.rdp.Transitions;

import java.util.Random;
import java.util.Set;

/**
 * Implementación de la política de resolución de conflictos prioritaria.
 * Si la transición prioritaria (T5, inicio del modo de complejidad simple) está habilitada,
 * se selecciona automáticamente. En caso contrario, se selecciona cualquiera
 * de las otras transiciones habilitadas de manera aleatoria.
 */
public class PriorityFiring implements Policy {

    // Transición T5 (inicio del modo de complejidad simple) que posee prioridad absoluta sobre la unidad de procesamiento
    private final int PRIORITY_TRANSITION = 5;
    private String lastDecisionMessage = "";

    /**
     * Selecciona la transición a disparar dando prioridad absoluta a T5 si está presente.
     * En caso contrario, selecciona una transición aleatoria de entre las habilitadas.
     * 
     * @param enabledTransitions Conjunto de transiciones actualmente habilitadas y en espera
     * @return El índice de la transición elegida
     * @throws IllegalStateException si el conjunto de transiciones habilitadas está vacío
     */
    @Override
    public int selectTransition(Set<Transitions> enabledTransitions) {
        // Buscar si T5 está habilitada en el conjunto
        for(Transitions t : enabledTransitions){
            int index = t.getIndex();
            if(index == PRIORITY_TRANSITION){
                lastDecisionMessage = "Priorizando procesamiento simple.";
                return PRIORITY_TRANSITION; // Priorizar T5 de inmediato
            }
        }

        // Si T5 no está disponible, seleccionar aleatoriamente para evitar bloqueos
        int randomIndex = new Random().nextInt(enabledTransitions.size());
        int i = 0;
        for (Transitions t : enabledTransitions) {
            if (i == randomIndex) {
                lastDecisionMessage = "Selección aleatoria (T5 no disponible).";
                return t.getIndex();
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
