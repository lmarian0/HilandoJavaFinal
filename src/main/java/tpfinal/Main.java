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

        int threadCount = 5;
        if (args.length > 0 && args[0].equals("7")) {
            threadCount = 7;
            System.out.println("Ejecutando con 7 hilos...");
        } else {
            System.out.println("Ejecutando con 5 hilos...");
        }

        Long startTime = System.currentTimeMillis();
        
        th_in.start();
        th_out.start();
        th_medium.start();
        th_simple.start();
        th_high.start();
        
        Thread th_medium2 = null;
        Thread th_simple2 = null;
        if (threadCount == 7) {
            th_medium2 = new Thread(new TransitionThread(monitor, ThreadSecuence.getSecuenceFromThreadId(1), logger, counter, totalInvariants), "HiloMedia2" );
            th_simple2 = new Thread(new TransitionThread(monitor, ThreadSecuence.getSecuenceFromThreadId(2), logger, counter, totalInvariants), "HiloSimple2" );
            th_medium2.start();
            th_simple2.start();
        }

        try {
            th_in.join();
            th_out.join();
            th_medium.join();
            th_simple.join();
            th_high.join();
            if (threadCount == 7) {
                th_medium2.join();
                th_simple2.join();
            }
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