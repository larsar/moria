package no.feide.moria.servlet.soap;

/**
 * Signals to the remote service that the user authentication failed.
 * @author Cato Olsen
 */
public class AuthenticationFailedException
extends ClientException {

    /**
     * Default constructor.
     * @param msg
     *            The exception message.
     */
    public AuthenticationFailedException(final String msg) {

        super(msg);

    }


    /**
     * This exception's SOAP Faultstring.
     * @return The Faultstring, in this case
     *         <code>"AUTHENTICATION FAILED"</code>.
     */
    public String getFaultstring() {
        
        return new String("AUTHENTICATION FAILED");
        
    }

}