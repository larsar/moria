package no.feide.moria.service;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.net.URL;
import java.security.Principal;
import java.rmi.RemoteException;
import java.net.MalformedURLException;

import java.util.Date;
import java.util.Timer;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import no.feide.moria.Session;
import no.feide.moria.SessionStore;
import no.feide.moria.SessionException;
import no.feide.moria.BackendException;
import no.feide.moria.Credentials;
import no.feide.moria.authorization.WebService;
import no.feide.moria.authorization.AuthorizationData;
import no.feide.moria.authorization.AuthorizationTask;


public class AuthenticationImpl
implements AuthenticationIF, ServiceLifecycle {

    /** Used for logging. */
    private static Logger log = Logger.getLogger(AuthenticationImpl.class.toString());

    /** Used to retrieve the client identity. */
    private ServletEndpointContext ctx;
    
    /** Session store. */
    private SessionStore sessionStore;
      
    /** Timer for updating the web service authorization module. */
    private Timer authTimer = new Timer();

    
    /**
     * Service endpoint destructor. Some basic housekeeping.
     */
    public void destroy() {
	log.finer("destroy()");

        authTimer.cancel();
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
     *                          caught when reading the properties file. Also
     *                          thrown if the system property
     *                          <code>no.feide.moria.AuthorizationTimerDelay</code>
     *                          is not set.
     */
    public void init(Object context) 
    throws ServiceException {
	log.finer("init(Object)");

	ctx = (ServletEndpointContext)context;

        try {
            
            // Read properties.
            if (System.getProperty("no.feide.moria.config.file") == null) {
                log.config("no.feide.moria.config.file not set; default is \"/moria.properties\"");
                System.getProperties().load(getClass().getResourceAsStream("/moria.properties"));
            }
            else {
                log.config("no.feide.moria.config.file set to \""+System.getProperty("no.feide.moria.config.file")+'\"');
                System.getProperties().load(getClass().getResourceAsStream(System.getProperty("no.feide.moria.config.file")));
            }
            
            // Set local pointer to session store.
            sessionStore = SessionStore.getInstance();
            
        } catch (FileNotFoundException e) {
            log.severe("FileNotFoundException caught and re-thrown as ServiceException");
            throw new ServiceException("FileNotFoundException caught", e);
        } catch (IOException e) {
            log.severe("IOException caught and re-thrown as ServiceException");
            throw new ServiceException("IOException caught", e);
        } catch (SessionException e) {
            log.severe("SessionException caught and re-thrown as ServiceException");
            throw new ServiceException("SessionException caught", e);
        }

        // Initialize authorization data timer, with sanity check.
        String s = System.getProperty("no.feide.moria.AuthorizationTimerDelay");
        if (s == null) {
            log.severe("Missed require system attribute: no.feide.moria.AuthorizationTimerDelay");
            throw new ServiceException("Missed require system attribute: no.feide.moria.AuthorizationTimerDelay");
        }
        int authDelay = new Integer(s).intValue()*1000; // Seconds to milliseconds
        log.config("Starting authorization update service with delay= "+authDelay+"ms");
        authTimer.scheduleAtFixedRate(new AuthorizationTask(), new Date(), authDelay);

        /* Sleep a short while. If not the authorization data will not
           be updated in time to authorize the first authentication request. */
        try { 
            int initialSleep = new Integer(System.getProperty("no.feide.moria.AuthorizationTimerInitThreadSleep")).intValue()*1000; // Seconds to milliseconds
            Thread.sleep(initialSleep); 
        } 
        catch (InterruptedException e) { 
            /* We didn't get any sleep. Don't care. If this is the
             * case, the first web service authorization request will
             * end in an exception. After that everythin will be all
             * right.
             */
        }
    }


    /**
     * Request a new Moria session, asking for a set of user attributes at
     * the same time. The web service asking for the session will be
     * checked against the W3LS web service authorization data.
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
     *                         BackendException is caught. Also thrown if the
     *                         prefix/postfix doesn't combine into a valid
     *                         URL, or if the 
     */
    public String requestSession(String[] attributes, String prefix, String postfix)
    throws RemoteException {
        log.finer("requestSession(String[], String, String)");
        
        /* Check if prefix and postfix, together with a possible
         * session ID, is a valid URL. */
        String simulatedURL = prefix+"MORIAID"+postfix;
        try {
            new URL(simulatedURL);
        } catch (MalformedURLException e) {
            log.severe("Malformed URL: "+simulatedURL);
            throw new RemoteException("Malformed URL: "+simulatedURL);
        }


        // Look up service authorization data.
        Principal p = ctx.getUserPrincipal();
        String serviceName = null;
        if (p != null)
            serviceName = p.getName();
	log.info("Client service requesting session: "+serviceName);
        WebService ws = AuthorizationData.getInstance().getWebService(serviceName);
        
        if (ws == null) {
            log.warning("Unauthorized service access: "+serviceName);
            throw new RemoteException("Web Service not authorized for use with Moria");
        } else if (!ws.allowAccessToAttributes(attributes)) {
            log.warning("Attribute request from service "+serviceName+" refused");
            throw new RemoteException("Access to one or more attributes prohibited");
        }

        try {
            Session session = sessionStore.createSession(attributes, prefix, postfix, p);
            session.setWebService(ws);
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
