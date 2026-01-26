package tpfinal.policies;
import java.util.Set;

import tpfinal.rdp.Transitions;

public interface Policy {
    int selectTransition(Set<Transitions> enabledTransitions);
}
