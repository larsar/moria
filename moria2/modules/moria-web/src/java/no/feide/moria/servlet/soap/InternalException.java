package no.feide.moria.servlet.soap;


/**
 * Signals to the remote service that Moria has internal problems. 
 * @author Cato Olsen
 */
public class InternalException
extends ServerException {
    
    
    /**
     * Default constructor.
     * @param msg The exception message.
     */
    public InternalException(final String msg) {
        
        super(msg);
        
    }
    
    
    /**
     * This exception's SOAP Faultstring.
     * @return The Faultstring, in this case
     *         <code>"MORIA INTERNAL"</code>.
     */
    public String getFaultstring() {
        
        return new String("MORIA INTERNAL");
        
    }

}
