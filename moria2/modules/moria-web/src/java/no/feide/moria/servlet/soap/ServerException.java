package no.feide.moria.servlet.soap;


/**
 * The superclass for all exceptions that should result in a SOAP Fault message
 * with <code>faultcode</code> <i>Server</i>
 * @author Cato Olsen
 */
public abstract class ServerException
extends SOAPException {
    
    /**
     * Default constructor.
     * @param msg The exception message.
     */
    public ServerException(final String msg) {
        
        super(msg);
        
    }
    
    
    /**
     * Gives the SOAP Faultcode for the exception (the value of the
     * <code>faultCode</code> element in the SOAP reply).
     * @return The SOAP Faultcode for the exception, in this case always
     *         <code>"Server"</code>.
     */
    public String getFaultcode() {

        return new String("Server");

    }

}
