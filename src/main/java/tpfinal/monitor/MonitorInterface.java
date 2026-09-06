package tpfinal.monitor;
/**
 * Interfaz pública que define los métodos expuestos por el Monitor de Concurrencia.
 * Esta interfaz expone únicamente el método de disparo de transiciones para mantener
 * el desacoplamiento de la red.
 */
public interface MonitorInterface {

    /**
     * Intenta disparar de manera segura y sincronizada una transición en la Red de Petri.
     * Si la transición no está habilitada por marcado, el hilo se suspende pasivamente.
     * Si está habilitada pero no ha cumplido su tiempo mínimo (EFT), el hilo espera fuera del lock.
     * 
     * @param transition El índice de la transición que se desea disparar
     * @return true si la transición se disparó con éxito, false en caso contrario
     */
    public boolean fireTransition(int transition);
}
