package tpfinal.policies;
import tpfinal.rdp.Transitions;

import java.util.Random;
import java.util.Set;

public class PriorityFiring implements Policy {

    private final int PRIORITY_TRANSITION = 5;

    @Override
    public int selectTransition(Set<Transitions> enabledTransitions) {
        for(Transitions t : enabledTransitions){
            int index = t.getIndex();
            if(index == PRIORITY_TRANSITION){
                return PRIORITY_TRANSITION;
            }
        }

        int randomIndex = new Random().nextInt(enabledTransitions.size());
        int i = 0;
        for (Transitions t : enabledTransitions) {
            if (i == randomIndex) {
                return t.getIndex();
            }
            i++;
        }
        throw new IllegalStateException("No enabled transitions available");
    }
    
}
