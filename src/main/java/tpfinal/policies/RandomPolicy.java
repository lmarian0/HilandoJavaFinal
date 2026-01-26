package tpfinal.policies;
import java.util.Set;
import tpfinal.rdp.Transitions;

public class RandomPolicy implements Policy {

    /**
     * Selects a random transition from the set of enabled transitions
     * @param enabledTransitions Set of currently enabled transitions
     * @return The index of the selected transition
     * @throws IllegalStateException if there are no enabled transitions
     */
    @Override
    public int selectTransition(Set<Transitions> enabledTransitions) {
        int size = enabledTransitions.size();
        int item = (int) (Math.random() * size);
        int i = 0;
        for (Transitions transition : enabledTransitions) {
            if (i == item) {
                return transition.getIndex();
            }
            i++;
        }
        throw new IllegalStateException("No enabled transitions available");
    }
    
}
