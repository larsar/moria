package no.feide.moria;

/**
 * Represents an exception caused by illegal or missing Moria configuration.
 */
public class ConfigurationException
extends Exception {
       
    /**
     * Message constructor.
     * @param msg Exception description.
     */
    public ConfigurationException(String msg) {
        super(msg);
    }
    
    
    /**
     * Message and cause constructor.
     * @param msg Exception description.
     * @param cause Exception cause.
     */
    public ConfigurationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
