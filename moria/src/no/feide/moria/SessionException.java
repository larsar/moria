package no.feide.moria;

public class SessionException extends Exception{
    
     public SessionException(String message) {
        super(message);
    }
    
    /**
     * Constructor that takes a cause.
     * @param cause The cause to encapsulate.
     */
    public SessionException(Throwable cause) {
        super(cause);
    }
    
    
    /**
     * Description and cause constructor.
     * @param msg The exception message.
     * @param cause The exception cause.
     */
    public SessionException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
