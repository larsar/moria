package no.feide.mellon;

/**
 * Used to signal an exception from the Moria interface.
 * @author Cato Olsen
 */
public class MoriaException
extends java.lang.Exception {
    
    /**
     * Basic constructor.
     */
    public MoriaException() {
    }
    
    
    /**
     * Message constructor.
     * @param message The exception message.
     */
    public MoriaException(String message) {
        super(message);
    }
    
    
    /**
     * Throwable constructor.
     * @param cause The original cause.
     */
    public MoriaException(Throwable cause) {
        super(cause);
    }
    
    
    /**
     * Message and throwable constructor.
     * @param message The exception message.
     * @param cause The original cause.
     */
    public MoriaException(String message, Throwable cause) {
        super(message, cause);
    }
}
