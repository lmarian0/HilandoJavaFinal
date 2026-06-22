package tpfinal;

import tpfinal.utils.RELog;
import tpfinal.monitor.Monitor;
import tpfinal.policies.Policy;
import tpfinal.policies.PriorityFiring;
import tpfinal.policies.RandomPolicy;
import tpfinal.threads.ThreadSecuence;
import tpfinal.threads.TransitionThread;
import tpfinal.utils.Logger;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) {
        // Selección de política por argumento: "random" (default) | "priority"
        String policyName = (args.length > 0) ? args[0].toLowerCase() : "random";
        Policy policy;
        String logFile;
        if (policyName.equals("priority") || policyName.equals("prioritaria")) {
            policy = new PriorityFiring();
            logFile = "log_priority.txt";
            System.out.println("Politica: PRIORIZADA (modo simple)");
        } else {
            policy = new RandomPolicy();
            logFile = "log_random.txt";
            System.out.println("Politica: ALEATORIA");
        }

        Logger logger = new Logger();
        Monitor monitor = new Monitor(policy, logger);
        int totalInvariants = 200;

        AtomicInteger counter = new AtomicInteger(0);

        // Entrada y salida: cantidad fija de 200
        Thread th_in = new Thread(new TransitionThread(monitor, ThreadSecuence.getSecuenceFromThreadId(0), totalInvariants), "HiloEntrada");
        Thread th_out = new Thread(new TransitionThread(monitor, ThreadSecuence.getSecuenceFromThreadId(4), totalInvariants), "HiloSalida");

        // Procesamiento: comparten contador, 200 entre los 3
        Thread th_medium = new Thread(new TransitionThread(monitor, ThreadSecuence.getSecuenceFromThreadId(1), counter, totalInvariants), "HiloMedia");
        Thread th_simple = new Thread(new TransitionThread(monitor, ThreadSecuence.getSecuenceFromThreadId(2), counter, totalInvariants), "HiloSimple");
        Thread th_high = new Thread(new TransitionThread(monitor, ThreadSecuence.getSecuenceFromThreadId(3), counter, totalInvariants), "HiloAlta");

        long startTime = System.currentTimeMillis();

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

        logger.writeToFile(logFile);

        RELog reLog = new RELog();
        reLog.loadLog(logFile);
        reLog.checkInvariant();

        System.out.println("Programa finalizado. No quedan hilos activos.");
    }
}
