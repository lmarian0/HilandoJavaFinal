package tpfinal.monitor;
import java.util.concurrent.locks.ReentrantLock;

import tpfinal.Exceptions.InvalidFireException;
import tpfinal.rdp.PetriNet;


public class Monitor implements MonitorInterface{
    // Implementación del Monitor
    private final ReentrantLock lock = new ReentrantLock(true);

    @Override
    public boolean fireTransition(int transition){
        lock.lock();
        boolean key = true;
        boolean validFiring;
        
        while(key){
            validFiring = PetriNet.fire(transition);
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
