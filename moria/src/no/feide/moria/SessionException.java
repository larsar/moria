package no.feide.moria;

import java.util.logging.Logger;

public class SessionException extends Exception{
    
    /** Used for logging. */
    private Logger log = Logger.getLogger(SessionException.class.toString());

    public SessionException(String message) {
        super(message);
        log.finer("SessionException(String)");
    }
    
    /**
     * Constructor that takes a cause.
     * @param cause The cause to encapsulate.
     */
    public SessionException(Throwable cause) {
        super(cause);
        log.finer("SessionException(Throwable)");
    }

}
