package tpfinal.policies;
import java.util.Set;

import tpfinal.rdp.Transitions;

/**
 * Interfaz para las políticas de resolución de conflictos del Monitor.
 * Define la estrategia para elegir cuál transición disparar o despertar
 * cuando hay múltiples opciones habilitadas simultáneamente.
 */
public interface Policy {

    /**
     * Selecciona una transición de entre un conjunto de transiciones habilitadas.
     * 
     * @param enabledTransitions Conjunto de transiciones actualmente habilitadas y con hilos esperando
     * @return El índice de la transición seleccionada para disparar/despertar
     */
    int selectTransition(Set<Transitions> enabledTransitions);

    /**
     * Devuelve el mensaje descriptivo de la última decisión tomada por la política.
     * 
     * @return El mensaje formateado para el trazador detallado
     */
    String getDecisionMessage();
}
