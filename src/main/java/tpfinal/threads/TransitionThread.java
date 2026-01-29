package tpfinal.threads;

import tpfinal.monitor.Monitor;
import tpfinal.rdp.Transitions;

public class TransitionThread implements Runnable{

    private Monitor monitor;
    private Transitions[] secuencia;
    private int fireAmount;

    public void run(){

        for(int i = 0; i < fireAmount; i++){
            for(Transitions t : secuencia){
                monitor.fireTransition(t.getIndex());
            }
        }
    }
}
