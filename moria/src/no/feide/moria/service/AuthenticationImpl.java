package no.feide.moria.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.Principal;
import java.util.HashMap;
import java.util.logging.*;
import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;
import no.feide.moria.*;
import no.feide.moria.authorization.WebService;
import no.feide.moria.authorization.AuthorizationData;


public class AuthenticationImpl
implements AuthenticationIF, ServiceLifecycle {

    /** Used for logging. */
    private static Logger log = Logger.getLogger(AuthenticationImpl.class.toString());

    /** Used to retrieve the client identity. */
    private ServletEndpointContext ctx;
    
    /** Session store. */
    private SessionStore sessionStore = SessionStore.getInstance();
      


    
    /**
     * Service endpoint destructor. Some basic housekeeping.
     */
    public void destroy() {
	log.finer("destroy()");

	log = null;
	ctx = null;
    }


    /**
     * Service endpoint initialization. Will read the <code>Properties</code>
     * file found in the location given by the system property
     * <code>no.feide.moria.config.file</code>. If the property is not
     * set, the default filename is <code>/moria.properties</code>.
     * @param context The servlet context, used to find the user (client service)
     *                identity in later methods.
     * @throws ServiceException If a FileNotFoundException or IOException id
     *                          caught when reading the properties file.
     */
    public void init(Object context) 
    throws ServiceException {
	log.finer("init(Object)");

	ctx = (ServletEndpointContext)context;

        // Read properties.
        try {
            if (System.getProperty("no.feide.moria.config.file") == null) {
                log.fine("no.feide.moria.config.file not set; default is \"/moria.properties\"");
                System.getProperties().load(getClass().getResourceAsStream("/moria.properties"));
            }
            else {
                log.fine("no.feide.moria.config.file set to \""+System.getProperty("no.feide.moria.config.file")+'\"');
                System.getProperties().load(getClass().getResourceAsStream(System.getProperty("no.feide.moria.config.file")));
            }
        } catch (FileNotFoundException e) {
            log.severe("FileNotFoundException caught and re-thrown as ServiceException");
            throw new ServiceException("FileNotFoundException caught", e);
        } catch (IOException e) {
            log.severe("IOException caught and re-thrown as ServiceException");
            throw new ServiceException("IOException caught", e);
        }

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
     * @return An URL to the authentication service.
     * @throws RemoteException If a SessionException or a
     *                         BackendException is caught.
     */
    public String requestSession(String[] attributes, String prefix, String postfix)
    throws RemoteException {
        log.finer("requestSession(String[], String, String)");
        
        // TODO:
        // Make a test URL from pre/post and check URL validity. Throw if not.
        // Add a subclass to RemoteException to signal URL invalid?

        WebService ws = AuthorizationData.getInstance().getWebService("foo");
        
        if (ws == null) {
            log.warning("Unauthorized service access: "+"foo");
            throw new RemoteException("Web Service not authorized for use with Moria.");
        }
        
        else if (!ws.allowAccessToAttributes(attributes)) {
            log.warning("Access to attributes denied: "+ws.getId()+" "+attributes);
            throw new RemoteException("Access to attributes prohibited.");
        }

        try {
	    Principal p = ctx.getUserPrincipal();
	    log.fine("Client service requesting session: "+p);
            Session session = sessionStore.createSession(attributes, prefix, postfix, p);
            return session.getRedirectURL();
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
     * @return An URL to the authentication service.
     * @throws RemoteException If a SessionException or a
     *                         BackendException is caught.
     */
    public String requestSession(String prefix, String postfix)
    throws RemoteException {
        log.finer("requestSession(String, String)");
	return requestSession(null, prefix, postfix);
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
    public HashMap getAttributes(String id)
    throws RemoteException {
        log.finer("getAttributes(String)");

	try {

	    // Look up session and check the client identity.
            Session session = sessionStore.getSession(id);
	    assertPrincipals(ctx.getUserPrincipal(), session.getClientPrincipal());

	    // Return attributes.
            HashMap result = session.getAttributes();
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
     * @return An URL constructed from the combination of prefix, session ID and
     *         postfix, where pre- and postfix were given as parameters to
     *         <code>requestSession</code>.
     * @throws RemoteException If an invalid session ID is used, or if a
     *                         <code>SessionException</code> or a
     *                         <code>BackendException</code> is caught. Also
     *                         thrown if the current client's identity (as 
     *                         found in the context) is different from the
     *                         identity of the client service originally
     *                         requesting the session.
     * @deprecated
     */
    // TODO:
    // For single-use, add attribute request and remove ID. Combines everything in AuthNServiceClient.
    // Only included to support the example code!
     public String requestUserAuthentication(String id, String username, String password)
     throws RemoteException {
         log.finer("requestUserAuthentication(String, String, String)");

         try {
             
             // Look up session and check the client identity.
             Session session = sessionStore.getSession(id);
 	     assertPrincipals(ctx.getUserPrincipal(), session.getClientPrincipal());

             // Authenticate through session.
             if (session.authenticateUser(new Credentials(username, password)))
                 return session.getRedirectURL();
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
     * @param pCurrent The current client principal.
     * @param pStored The principal stored in the session.
     * @throws SessionException If there's a problem getting the session from
     *                          the session sessionStore.
     * @throws RemoteException If the client principals didn't match.
     */
    private static void assertPrincipals(Principal pCurrent, Principal pStored)
    throws SessionException, RemoteException {
	log.finer("assertPrincipals(Principal, String)");

	if (pCurrent == null) {
	    if (pStored == null)
		return;
	}
	else if (pCurrent.toString().equals(pStored.toString()))
	    return;

        log.severe("Client service identity mismatch; "+pCurrent+" != "+pStored);
	throw new RemoteException("Client service identity mismatch");
    }

}
