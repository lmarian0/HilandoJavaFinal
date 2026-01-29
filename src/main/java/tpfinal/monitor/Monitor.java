package tpfinal.monitor;
import tpfinal.policies.*;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import tpfinal.rdp.PetriNet;
import tpfinal.rdp.Transitions;
import tpfinal.utils.MathUtils;


public class Monitor implements MonitorInterface{
    // Implementación del Monitor
    private final ReentrantLock lock = new ReentrantLock(true);
    private final Queue queue;
    private final Policy policy;

    public Monitor() {
        this.queue = new Queue(PetriNet.NUM_TRANSITIONS, lock);
        this.policy = new RandomPolicy();
    }

    public Monitor(Policy policy) {
        this.queue = new Queue(PetriNet.NUM_TRANSITIONS, lock);
        this.policy = policy;
    }

    @Override
    public boolean fireTransition(int transition){
        lock.lock();
        boolean key = true;
        boolean validFiring;
        Transitions transitionEnum = Transitions.fromIndex(transition);
        
        try{
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
                    if(!m.isEmpty()){
                        int selectedTransition = policy.selectTransition(m);
                        queue.release(selectedTransition);
                    }
                    // Exit the loop after successful firing
                    key = false;
                }else{
                    // Invalid firing. Release lock and go to the queue to wait
                    queue.acquire(transition); // release lock inside acquire method
                }
            }
        }finally{
            lock.unlock();
        }
        
        // Sleep OUTSIDE the monitor (lock already released)
        try {
            Thread.sleep(transitionEnum.getTime());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
        return true;
    }
}
