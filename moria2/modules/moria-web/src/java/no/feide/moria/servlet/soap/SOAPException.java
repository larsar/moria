package no.feide.moria.servlet.soap;

/**
 * The base class for all exceptions signalled through the SOAP interface.
 */
public abstract class SOAPException
extends Exception {
    
    /**
     * Default constructor.
     * @param msg The exception message.
     */
    public SOAPException(final String msg) {
        
        super(msg);
        
    }

    /**
     * Gives the SOAP Faultcode for the exception (the value of the
     * <code>faultCode</code> element in the SOAP reply).
     * @return The SOAP Faultcode for the exception.
     */
    public abstract String getFaultcode();


    /**
     * Gives the SOAP Faultstring for the exception (the value of the
     * <code>faultString</code> element in the SOAP reply).
     * @return The SOAP Faultstring for the exception.
     */
    public abstract String getFaultstring();

}