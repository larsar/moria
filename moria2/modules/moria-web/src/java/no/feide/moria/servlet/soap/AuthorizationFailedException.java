package no.feide.moria.servlet.soap;


/**
 * Signals to the remote service that an operation was attempted, or parameters
 * were used, that the service was not authorized for.
 * @author Cato Olsen
 */
public class AuthorizationFailedException
extends ClientException {
    

    /**
     * Default constructor.
     * @param msg The exception message.
     */
    public AuthorizationFailedException(final String msg) {
        
        super(msg);
        
    }
    
    /**
     * This exception's SOAP Faultstring.
     * @return The Faultstring, in this case
     *         <code>"AUTHORIZATION FAILED"</code>.
     */
    public String getFaultstring() {
        
        return new String("AUTHORIZATION FAILED");
        
    }

}
