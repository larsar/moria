/*
 * BackendException.java
 *
 * Created on November 20, 2002, 3:40 PM
 */

package no.feide.moria;

/**
 * Used to throw exceptions from the backend.
 * @author Cato Olsen
 */
public class BackendException
extends java.lang.Exception {
    
    /**
     * Basic constructor.
     * @param message Exception message.
     */
    public BackendException(String message) {
        super(message);
    }
    
    /**
     * Constructor. Used to encapsulate another exception.
     * @param message Exception message.
     * @param cause Cause of exception.
     */
    public BackendException(Throwable cause) {
        super(cause);
    }
    
    /**
     * Constructor. Used to pass on another exception with a message.
     * @param message Exception message.
     * @param cause Cause of exception.
     */
    public BackendException(String message, Throwable cause) {
        super(message, cause);
    }
}
