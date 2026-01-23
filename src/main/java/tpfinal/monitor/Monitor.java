package tpfinal.monitor;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import tpfinal.Exceptions.InvalidFireException;
import tpfinal.rdp.PetriNet;
import tpfinal.rdp.Transitions;
import tpfinal.utils.MathUtils;


public class Monitor implements MonitorInterface{
    // Implementación del Monitor
    private final ReentrantLock lock = new ReentrantLock(true);
    private final Queue queue;

    public Monitor() {
        this.queue = new Queue(PetriNet.NUM_TRANSITIONS, lock);
    }

    @Override
    public boolean fireTransition(int transition){
        lock.lock();
        boolean key = true;
        boolean validFiring;
        Transitions transitionEnum = Transitions.fromIndex(transition);
        
        while(key){
            validFiring = PetriNet.fire(transitionEnum);
            if(validFiring){
                // Check sensibilized transitions (Vs)
                Set<Transitions> enabledTransitions = PetriNet.getEnabledTransitions();
                // Check who is waiting (Wt)
                Set<Transitions> waitingThreads = queue.getWaitingThreads();
                // set m to Vs&& Wt
                Set<Transitions> m = MathUtils.intersectSets(enabledTransitions, waitingThreads);
                // Do the alt(m) block
            }else{
                // Release lock and go to the queue to wait
                lock.unlock();
                queue.acquire(transition);
            }
        }
    }
}
