package tpfinal.Exceptions;

/**
 * Excepción lanzada cuando se intenta realizar un disparo no válido en la Red de Petri
 * (por ejemplo, si el disparo de la transición resulta en marcas negativas).
 */
public class InvalidFireException extends Exception {
    
    /**
     * Crea una nueva excepción con el mensaje de error especificado.
     * @param message Mensaje que detalla la causa de la invalidez del disparo
     */
    public InvalidFireException(String message) {
        super(message);
    }
}
