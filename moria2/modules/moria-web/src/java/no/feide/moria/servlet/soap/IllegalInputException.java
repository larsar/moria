package no.feide.moria.servlet.soap;


/**
 * Signals to the remote service that a parameter did not have the proper
 * syntax.
 * @author Cato Olsen
 */
public class IllegalInputException
extends ClientException {
    
 
    /**
     * Default constructor.
     * @param msg The exception message.
     */
    public IllegalInputException(final String msg) {
        
        super(msg);
        
    }
    
    /**
     * This exception's SOAP Faultstring.
     * @return The Faultstring, in this case
     *         <code>"ILLEGAL INPUT"</code>.
     */
    public String getFaultstring() {
        
        return new String("ILLEGAL INPUT");
        
    }

}
