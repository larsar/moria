/*
 * BackendException.java
 *
 * Created on November 20, 2002, 3:40 PM
 */

package no.feide.moria;

import java.util.logging.Logger;

/**
 * Used to throw exceptions from the backend.
 * @author Cato Olsen
 */
public class BackendException
extends java.lang.Exception {
    
    /** Used for logging. */
    private static Logger log = Logger.getLogger(User.class.toString());
    
    /**
     * Basic constructor.
     * @param message Exception message.
     */
    public BackendException(String message) {
        super(message);
        log.finer("BackendException(String)");
    }
    
    /**
     * Constructor. Used to encapsulate another exception.
     * @param message Exception message.
     * @param cause Cause of exception.
     */
    public BackendException(Throwable cause) {
        super(cause);
        log.finer("BackendException(Throwable)");
    }
    
    /**
     * Constructor. Used to pass on another exception with a message.
     * @param message Exception message.
     * @param cause Cause of exception.
     */
    public BackendException(String message, Throwable cause) {
        super(message, cause);
        log.finer("BackendException(String, Throwable)");
    }
}
