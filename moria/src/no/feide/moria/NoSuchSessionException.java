package no.feide.moria;

public class NoSuchSessionException extends SessionException{
    
     public NoSuchSessionException(String message) {
        super(message);
    }
    
    /**
     * Constructor that takes a cause.
     * @param cause The cause to encapsulate.
     */
    public NoSuchSessionException(Throwable cause) {
        super(cause);
    }

}
