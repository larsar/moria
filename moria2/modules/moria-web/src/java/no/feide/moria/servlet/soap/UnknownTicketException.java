package no.feide.moria.servlet.soap;


/**
 * Signals to the remote service that the ticket used in the SOAP operation
 * was unknown to Moria.
 * @author Cato Olsen
 */
public class UnknownTicketException
extends ClientException {
    
   
    /**
     * Default constructor.
     * @param msg The exception message.
     */
    public UnknownTicketException(final String msg) {
        
        super(msg);
        
    }
    
    /**
     * This exception's SOAP Faultstring.
     * @return The Faultstring, in this case
     *         <code>"UNKNOWN TICKET"</code>.
     */
    public String getFaultstring() {
        
        return new String("UNKNOWN TICKET");
        
    }

}
