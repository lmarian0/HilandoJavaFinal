package tpfinal;

import tpfinal.utils.RELog;
import tpfinal.monitor.Monitor;
import tpfinal.rdp.PetriNet;
import tpfinal.Exceptions.InvalidFireException;
import tpfinal.threads.ThreadSecuence;
import tpfinal.threads.TransitionThread;
import tpfinal.utils.Logger;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) {
        Monitor monitor = new Monitor();
        Logger logger = new Logger();
        int totalInvariants = 200;

        AtomicInteger counter = new AtomicInteger(0);

        // Entrada y salida: cantidad fija de 200
        Thread th_in = new Thread(new TransitionThread(monitor, ThreadSecuence.getSecuenceFromThreadId(0), logger, totalInvariants), "HiloEntrada" );
        Thread th_out = new Thread(new TransitionThread(monitor, ThreadSecuence.getSecuenceFromThreadId(4), logger, totalInvariants), "HiloSalida" );
    
        // Procesamiento: comparten contador, 200 entre los 3
        Thread th_medium = new Thread(new TransitionThread(monitor, ThreadSecuence.getSecuenceFromThreadId(1), logger, counter, totalInvariants), "HiloMedia" );
        Thread th_simple = new Thread(new TransitionThread(monitor, ThreadSecuence.getSecuenceFromThreadId(2), logger, counter, totalInvariants), "HiloSimple" );
        Thread th_high = new Thread(new TransitionThread(monitor, ThreadSecuence.getSecuenceFromThreadId(3), logger, counter, totalInvariants), "HiloAlta" );

        Long startTime = System.currentTimeMillis();

        th_in.start();
        th_out.start();
        th_medium.start();
        th_simple.start();
        th_high.start();

        try {
            th_in.join();
            th_out.join();
            th_medium.join();
            th_simple.join();
            th_high.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("Tiempo de ejecucion: " + duration + " ms");

        logger.writeToFile("log.txt");

        RELog reLog = new RELog();
        reLog.loadLog("log.txt");
        reLog.checkInvariant();
        
        System.out.println("Programa finalizado. No quedan hilos activos.");
    }
}