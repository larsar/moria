package no.feide.moria.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.logging.*;
import java.util.Timer;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;
import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;
import no.feide.moria.*;

import java.util.Date;


public class AuthenticationImpl
implements AuthenticationIF, ServiceLifecycle {

    /** Used for logging. */
    private static Logger log = Logger.getLogger(AuthenticationImpl.class.toString());

    /** Used to handle session store timeouts. */
    private Timer sessionTimer = new Timer(true);

    /** Used to retrieve the client identity. */
    private ServletEndpointContext ctx;
    
    /** Session store. */
    private SessionStore sessionStore;
      


    
    /**
     * Service endpoint destructor. Some basic housekeeping.
     */
    public void destroy() {
	log.finer("destroy()");

        sessionTimer.cancel();
	log = null;
	ctx = null;
    }


    /**
     * Service endpoint initialization. Will read the <code>Preferences</code>
     * file found in the location given by the system property
     * <code>no.feide.moria.config.file</code>. If the property is not
     * set, the default filename is <code>/Moria.xml</code>.
     * @param context The servlet context, used to find the user (client service)
     *                identity in later methods.
     * @throws ServiceException If a FileNotFoundException, IOException
     *                          or InvalidPreferencesFormatException is
     *                          caught when reading the preferences file.
     */
    public void init(Object context) 
    throws ServiceException {
	log.finer("init(Object)");

	ctx = (ServletEndpointContext)context;

        // Read preferences.
        try {
            if (System.getProperty("no.feide.moria.config.file") == null) {
                log.fine("no.feide.moria.config.file not set; default is \"/Moria.xml\"");
		Preferences.importPreferences(getClass().getResourceAsStream("/Moria.xml"));
            }
            else {
                log.fine("no.feide.moria.config.file set to \""+System.getProperty("no.feide.moria.config.file")+'\"');
		Preferences.importPreferences(getClass().getResourceAsStream(System.getProperty("no.feide.moria.config.file")));
            }
        } catch (FileNotFoundException e) {
            log.severe("FileNotFoundException caught and re-thrown as ServiceException");
            throw new ServiceException("FileNotFoundException caught", e);
        } catch (IOException e) {
            log.severe("IOException caught and re-thrown as ServiceException");
            throw new ServiceException("IOException caught", e);
        } catch (InvalidPreferencesFormatException e) {
            log.severe("InvalidPreferencesFormatException caught and re-thrown as ServiceException");
            throw new ServiceException("InvalidPreferencesException caught", e);
        }
        
        // Initialize periodical session sessionStore checks.
        sessionTimer.scheduleAtFixedRate(new SessionStoreTask(), new Date(), 2500);

    }


    /**
     * Request a new Moria session, asking for a set of user attributes at
     * the same time.
     * @param attributes The requested user attributes, to be returned from
     *                   <code>verifySession()</code> once authentication is
     *                   complete. <code>null</code> value allowed.
     * @param prefix The prefix, used to build the <code>verifySession</code>
     *               return value. May be <code>null</code>.
     * @param postfix The postfix, used to build the
     *                <code>verifySession</code> return value. May be
     *                <code>null</code>.
     * @return A new session descriptor.
     * @throws RemoteException If a SessionException or a
     *                         BackendException is caught.
     */
    public SessionDescriptor requestSession(String[] attributes, String prefix, String postfix)
    throws RemoteException {
        log.finer("requestSession(String[], String, String)");
        
        // TODO:
        // Make a test URL from pre/post and check URL validity. Throw if not.
        // Add a superclass to RemoteException to signal URL invalid?
        
        try {
	    Principal p = ctx.getUserPrincipal();
	    log.fine("Client service requesting session: "+p);
            Session session = sessionStore.createSession(attributes, prefix, postfix, p);
            return session.getDescriptor();
        } catch (SessionException e) {
            log.severe("SessionException caught and re-thrown as RemoteException");
            throw new RemoteException("SessionException caught", e);
        }
    }


    /**
     * Request a new Moria session, without asking for a set of user
     * attributes. Actually a simple wrapper for
     * <code>requestSession(String[], String)</code> with an empty
     * (<code>null</code>) attribute request array.
     * @param prefix The prefix, used to build the <code>verifySession</code>
     *               return value.
     * @param postfix The postfix, used to build the
     *                <code>verifySession</code> return value.
     * @return A new session descriptor, or <code>null</code> if a new session
               could not be established.
     * @throws RemoteException If a SessionException or a
     *                         BackendException is caught.
     */
    // TODO:
    // Obsolete. Return redirect URL as String Session ID not needed.
    public SessionDescriptor requestSession(String prefix, String postfix)
    throws RemoteException {
        log.finer("requestSession(String, String)");

	return requestSession(null, prefix, postfix);
    }
    
    
    /**
     * Verify a Moria session; that is, check that exists and has been
     * through authentication.
     * @param id The session ID.
     * @return The concatenated string <code>[prefix][id][postfix]</code>
     *         where <code>[prefix]</code> and <code>[postfix]</code> are the
     *         parameter strings given to <code>requestSession</code>.
     * @throws RemoteException If a SessionException or a
     *                         BackendException is caught. Also thrown if
     *                         the current client's identity (as found in
     *                         the context) is different from the identity
     *                         of the client service originally requesting
     *                         the session.
     */
    public String verifySession(String id)
    throws RemoteException {
        log.finer("verifySession(String)");
        
        try {

	    // Check the client identity.
	    assertPrincipals(ctx.getUserPrincipal(), id);
            
            // Look up session.
            Session session = sessionStore.getSession(id);
            if ( (session == null) || !session.isAuthenticated() ) {
                log.warning("No such session: "+id);
                return null;
            }

        } catch (SessionException e) {
            log.severe("SessionException caught, and re-thrown as RemoteException");
            throw new RemoteException("SessionException caught", e);
        }

        return ""; // Should return something...
    }


    /* Return the previously requested user attributes.
     * @param The session ID.
     * @return The previously requested user attributes.
     * @throws RemoteException If a SessionException or a
     *                         BackendException is caught. Also thrown if
     *                         the current client's identity (as found in
     *                         the context) is different from the identity
     *                         of the client service originally requesting
     *                         the session.
     */
    public UserAttribute[] getAttributes(String id)
    throws RemoteException {
        log.finer("getAttributes(String)");

	try {

	    // Check the client identity.
	    assertPrincipals(ctx.getUserPrincipal(), id);

	    // Return attributes.
            Session session = sessionStore.getSession(id);
            UserAttribute[] result = session.getAttributes();
            sessionStore.deleteSession(session);
            return result;

        } catch (SessionException e) {
            log.severe("SessionException caught, and re-thrown as RemoteException");
            throw new RemoteException("SessionException caught", e);
        }
    }
    
    
    /**
     * Authenticate a user.
     * @param id A valid (first-round) session ID.
     * @param username The username.
     * @param password The password.
     * @return A new session descriptor, containing a new second-round session
     *         ID, which from now on represents the authenticated session, and
     *         the new session redirect URL. <code>null</code> if the user
     *         was unable to successfully authenticate.
     * @throws RemoteException If an invalid session ID is used, or if a
     *                         <code>SessionException</code> or a
     *                         <code>BackendException</code> is caught. Also
     *                         thrown if the current client's identity (as 
     *                         found in the context) is different from the
     *                         identity of the client service originally
     *                         requesting the session.
     */
    // TODO:
    // For single-use, add attribute request and remove ID. Combines everything in AuthNServiceClient.
    public SessionDescriptor requestUserAuthentication(String id, String username, String password)
    throws RemoteException {
        log.finer("requestUserAuthentication(String, String, String)");

        try {

	    // Check the client identity.
	    assertPrincipals(ctx.getUserPrincipal(), id);

            // Look up session.
            Session session = sessionStore.getSession(id);
            if (session == null) {
                log.severe("Invalid session ID: "+id);
                throw new RemoteException("Invalid session ID: "+id);
            }
            
            // Authenticate through session.
            if (session.authenticateUser(new Credentials(username, password)))
                return session.getDescriptor();
            else
                return null;

        } catch (SessionException e) {
            log.severe("SessionException caught and re-thrown as RemoteException");
            throw new RemoteException("SessionException caught", e);
        } catch (BackendException e) {
            log.severe("BackendException caught and re-thrown as RemoteException");
            throw new RemoteException("BackendException caught", e);
        }
    }


    /**
     * Utility method, used to check if a given client service principal
     * matches the principal stored in the session. <code>null</code> values
     * are allowed.
     * @param p The current client principal.
     * @param id The session ID.
     * @throws SessionException If there's a problem getting the session from
     *                          the session sessionStore.
     * @throws RemoteException If the client principals didn't match.
     */
    // TODO:
    // Should take a session's principal (from a previous lookup) as the second par, not ID.
    // As: assertPrincipals(p, session.getclientPrincip());
    private static void assertPrincipals(Principal client, String id)
    throws SessionException, RemoteException {
	log.finer("assertPrincipals(Principal, String)");

	Principal p = SessionStore.getInstance().getSession(id).getClientPrincipal();
	if (client == null) {
	    if (p == null)
		return;
	}
	else if (client.toString().equals(p.toString()))
	    return;

        log.severe("Client service identity mismatch; "+client+" != "+p);
	throw new RemoteException("Client service identity mismatch");
    }

}
