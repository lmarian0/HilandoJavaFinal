package tpfinal;

import tpfinal.utils.RELog;
import tpfinal.monitor.Monitor;
import tpfinal.policies.Policy;
import tpfinal.policies.PriorityFiring;
import tpfinal.policies.RandomPolicy;
import tpfinal.threads.ThreadSecuence;
import tpfinal.threads.TransitionThread;
import tpfinal.utils.Logger;
import tpfinal.utils.TraceLogger;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Clase de orquestación principal (Punto de entrada).
 * Inicializa la Red de Petri, inyecta la política de disparo seleccionada por argumento,
 * crea y lanza los hilos agentes de la simulación, espera su finalización con join()
 * y finalmente exporta el archivo de log para la verificación formal de T-invariantes.
 */
public class Main {
    public static void main(String[] args) {
        boolean traceEnabled = false;
        String policyName = "random";
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--trace")) {
                traceEnabled = true;
            } else {
                policyName = arg.toLowerCase();
            }
        }

        Policy policy;
        String logFile;
        if (policyName.equals("priority") || policyName.equals("prioritaria")) {
            policy = new PriorityFiring();
            logFile = "log_priority.txt";
            if (!traceEnabled) {
                System.out.println("Politica: PRIORIZADA (modo de complejidad simple)");
            }
        } else {
            policy = new RandomPolicy();
            logFile = "log_random.txt";
            if (!traceEnabled) {
                System.out.println("Politica: ALEATORIA");
            }
        }

        TraceLogger traceLogger = traceEnabled ? new TraceLogger() : null;
        Logger logger = new Logger();
        Monitor monitor = new Monitor(policy, logger, traceLogger);
        int totalInvariants = 200;

        AtomicInteger counter = new AtomicInteger(0);

        // Entrada y salida: cantidad fija de 200
        Thread th_in = new Thread(new TransitionThread(monitor, ThreadSecuence.getSecuenceFromThreadId(0), totalInvariants, traceLogger), "HiloEntrada");
        Thread th_out = new Thread(new TransitionThread(monitor, ThreadSecuence.getSecuenceFromThreadId(4), totalInvariants, traceLogger), "HiloSalida");

        // Procesamiento: comparten contador, 200 entre los 3
        Thread th_medium = new Thread(new TransitionThread(monitor, ThreadSecuence.getSecuenceFromThreadId(1), counter, totalInvariants, traceLogger), "HiloMedia");
        Thread th_simple = new Thread(new TransitionThread(monitor, ThreadSecuence.getSecuenceFromThreadId(2), counter, totalInvariants, traceLogger), "HiloSimple");
        Thread th_high = new Thread(new TransitionThread(monitor, ThreadSecuence.getSecuenceFromThreadId(3), counter, totalInvariants, traceLogger), "HiloAlta");

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
        if (traceLogger != null) {
            traceLogger.logMainDuration(duration);
        } else {
            System.out.println("Tiempo de ejecucion: " + duration + " ms");
        }

        logger.writeToFile(logFile);

        if (traceLogger == null) {
            RELog reLog = new RELog();
            reLog.loadLog(logFile);
            reLog.checkInvariant();
            System.out.println("Programa finalizado. No quedan hilos activos.");
        } else {
            traceLogger.logMainFinished();
        }
    }
}
