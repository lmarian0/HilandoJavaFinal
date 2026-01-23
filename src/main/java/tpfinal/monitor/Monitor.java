package tpfinal.monitor;
import java.util.concurrent.locks.ReentrantLock;

import tpfinal.Exceptions.InvalidFireException;
import tpfinal.rdp.PetriNet;
import tpfinal.rdp.Transitions;


public class Monitor implements MonitorInterface{
    // Implementación del Monitor
    private final ReentrantLock lock = new ReentrantLock(true);

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
                // Check who is waiting (Wt)
                // set m to Vs&& Wt
                // Do the alt(m) block
            }else{
                // Release lock and go to the queue to wait
            }
        }
    }
}
