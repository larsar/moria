package no.feide.moria.servlet.soap;


/**
 * Signals to the remote service that Moria was unable to reach the third-party
 * server responsible for user authentication. 
 * @author Cato Olsen
 */
public class AuthenticationUnavailableException
extends ServerException {

    
    /**
     * Default constructor.
     * @param msg The exception message.
     */
    public AuthenticationUnavailableException(final String msg) {
        
        super(msg);
        
    }
    
    
    /**
     * This exception's SOAP Faultstring.
     * @return The Faultstring, in this case
     *         <code>"AUTHENTICATION UNAVAILABLE"</code>.
     */
    public String getFaultstring() {
        
        return new String("AUTHENTICATION UNAVAILABLE");
        
    }

}
