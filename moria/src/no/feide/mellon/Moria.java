package no.feide.mellon;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.logging.Logger;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;
import java.util.HashMap;
import javax.xml.rpc.Stub;
import no.feide.moria.service.*;

/**
 * Represents the interface to Moria, hiding the internals of the
 * Mellon-Moria communication.
 * @author Cato Olsen
 */
// TODO:
// Rewrite because of changes in AuthenticationImpl.java.
public class Moria {
    
    /** Used for logging. */
    private static Logger log = Logger.getLogger(Moria.class.toString());
    
    /** Singleton self. */
    private static Moria me;
    
    /** The JAX-RPC Moria interface proxy stub. */
    private Stub stub;
    
    
    /**
     * Private constructor. Will read the <code>Preferences</code>
     * file found in the location given by the system property
     * <code>no.feide.mellon.config.file</code>. If the property is not
     * set, the default filename is <code>/Mellon.xml</code>.
     * @throws MoriaException If a FileNotFoundException, IOException or
     *                        InvalidPreferencesFormatException is caught
     *                        trying to read the preferences file.
     */
    private Moria() 
    throws MoriaException {
        log.finer("Moria()");
        
        // Read preferences.
        try {
            if (System.getProperty("no.feide.mellon.config.file") == null) {
                log.fine("no.feide.mellon.config.file not set; default is \"/Mellon.xml\"");
		Preferences.importPreferences(getClass().getResourceAsStream("/Mellon.xml"));
            } else {
                log.fine("no.feide.mellon.config.file set to \""+System.getProperty("no.feide.mellon.config.file")+'\"');
		Preferences.importPreferences(getClass().getResourceAsStream(System.getProperty("no.feide.mellon.config.file")));      
            }
        } catch (FileNotFoundException e) {
            log.severe("FileNotFoundException caught and re-thrown as MoriaException ");
            throw new MoriaException("FileNotFoundException caught", e);
        } catch (IOException e) {
            log.severe("IOException caught and re-thrown as MoriaException");
            throw new MoriaException("IOException caught", e);
        } catch (InvalidPreferencesFormatException e) {
            log.severe("InvalidPreferencesFormatException caught and re-thrown as MoriaException");
            throw new MoriaException("InvalidPreferencesException caught", e);
        }
        
        // Fix some properties.
        Preferences prefs = Preferences.userNodeForPackage(Moria.class);
        System.setProperty("javax.net.ssl.trustStore", prefs.get("trustStore", null));
        System.setProperty("javax.net.ssl.trustStorePassword", prefs.get("trustStorePassword", null));
        
        // Create service proxy and set credentials.
        stub = (Stub)(new FeideAuthentication_Impl().getAuthenticationIFPort());
        stub._setProperty(javax.xml.rpc.Stub.USERNAME_PROPERTY, prefs.get("serviceUsername", null));
        stub._setProperty(javax.xml.rpc.Stub.PASSWORD_PROPERTY, prefs.get("servicePassword", null));
	AuthenticationIF service = (AuthenticationIF)stub;
	
    }

    
    /**
     * Get an instance of Moria.
     * @return An instance of the Moria interface.
     * @throws MoriaException If the singleton object couldn't be constructed.
     */
    public static Moria getInstance()
    throws MoriaException {
        log.finer("getInstance()");
        
        // Return singleton, creating it if necessary.
        if (me == null)
            me = new Moria();
        return me;
    }
    
    
    /**
     * Wrapper for same JAX-RPC stub of same name.
     * @param attributes The names of requested attributes, to be returned
     *                   later throught <code>getAttributes</code>.
     * @param prefix The prefix, used to build the <code>verifySession</code>
     *               return value.
     * @param postfix The postfix, used to build the
     *                <code>verifySession</code> return value.
     * @return A Moria session descriptor.
     * @throws MoriaException If a RemoteException is caught.
     */
    public String requestSession(String[] attributes, String prefix, String postfix) 
    throws MoriaException {
        log.finer("requestSession(String[], String, String)");
        
	AuthenticationIF service = (AuthenticationIF)stub;
        try {
            return service.requestSession(attributes, prefix, postfix);
        } catch (RemoteException e) {
            log.severe("RemoteException caught and re-thrown as MoriaException");
            throw new MoriaException("RemoteException caught", e);
        }
    }
    
    
    /**
     * Wrapper for same JAX-RPC stub of same name.
     * @param id
     * @return
     * @throws MoriaException If a RemoteException is caught.
     */
    public HashMap getAttributes(String id)
    throws MoriaException {
        log.finer("getAttributes(String)");
        
	AuthenticationIF service = (AuthenticationIF)stub;
        try {
            return service.getAttributes(id);
        } catch (RemoteException e) {
            log.severe("RemoteException caught and re-thrown as MoriaException");
            throw new MoriaException("RemoteException caught", e);
        }
    }
    

    
}
