package tpfinal.utils;

import java.util.Arrays;

/**
 * Clase utilitaria pasiva para el trazado detallado de ejecución de la Red de Petri.
 * Mapea los nombres de los hilos a sus etiquetas funcionales y da formato a los
 * mensajes para proporcionar observabilidad en tiempo real sobre la consola.
 */
public class TraceLogger {

    /**
     * Mapea el nombre del hilo actual a su etiqueta funcional.
     * 
     * @return Etiqueta del hilo (ej: "[Entrada]")
     */
    public String getLabel() {
        String threadName = Thread.currentThread().getName();
        switch (threadName) {
            case "HiloEntrada":
                return "[Entrada]";
            case "HiloMedia":
                return "[Media]";
            case "HiloSimple":
                return "[Simple]";
            case "HiloAlta":
                return "[Alta]";
            case "HiloSalida":
                return "[Salida]";
            default:
                return "[" + threadName + "]";
        }
    }

    public void logEnterMonitor(String label) {
        System.out.println(label + ": Entrando al monitor.");
    }

    public void logAttempt(String label, String transName) {
        System.out.println(label + ": Intentando disparar transición " + transName + ".");
    }

    public void logFired(String label, String transName) {
        System.out.println(label + ": Transición " + transName + " disparada.");
    }

    public void logNoTokens(String label, String transName) {
        System.out.println(label + ": Faltan tokens para " + transName + ". A dormir la siesta.");
    }

    public void logTimeWait(String label) {
        System.out.println(label + ": Falta una ventana de tiempo.");
        System.out.println(label + ": A dormir con sleep() y liberar el mutex.");
    }

    public void logWokeFromSleep(String label) {
        System.out.println(label + ": Me desperté del sleep().");
    }

    public void logWokeFromQueue(String label) {
        System.out.println(label + ": Se despertó de la siesta.");
    }

    public void logState(int[] marking, int[] enabled, int[] sleeping) {
        System.out.println(Arrays.toString(marking) + ": Marcado");
        System.out.println(Arrays.toString(enabled) + ": Sensibilizadas");
        System.out.println(Arrays.toString(sleeping) + ": Dormidos");
    }

    public void logWakeUp(String label, int targetTransition) {
        System.out.println(label + ": Levanta el hilo de la cama " + targetTransition + " y abandona el monitor.");
    }

    public void logNoWakeUp(String label) {
        System.out.println(label + ": Ningún hilo para levantar.");
    }

    public void logPolicyDecision(String label, String policyMessage) {
        System.out.println(label + ": " + policyMessage);
    }

    public void logLeaveMonitor(String label) {
        System.out.println(label + ": Abandona el monitor.");
    }

    public void logCompleted(String label) {
        System.out.println(label + ": Se han completado los 200 T invariantes.");
    }

    public void logCompletedAbandoned(String label) {
        System.out.println(label + ": Invariantes completados. Abandonando monitor.");
    }

    public void logFinished(String label) {
        System.out.println(label + ": Ejecución finalizada.");
    }

    public void logMainDuration(long duration) {
        System.out.println("[Main]: Tiempo de ejecución: " + duration + " ms.");
    }

    public void logMainFinished() {
        System.out.println("[Main]: Ejecución finalizada.");
    }
}
