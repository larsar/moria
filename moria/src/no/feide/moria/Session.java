package no.feide.moria;

import java.security.Principal;
import java.util.*;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.naming.directory.BasicAttributes;
import javax.servlet.ServletContext;
import no.feide.moria.service.SessionDescriptor;
import no.feide.moria.service.UserAttribute;

public class Session {
    
    /** Used for logging. */
    private static Logger log = Logger.getLogger(Session.class.toString());
    
    /** Used to store the current redirect URL for this session. */
    private String redirectURL;

    /** Holds the prefix value sent by the resource. */
    private String prefix;
    
    /** Holds the postfix value sent by the resource. */
    private String postfix;
       
    /** The object's current unique session ID. */
    private String sessionID;
    
    /** The attributes requested for this session. */
    private String[] request;

    /** Number of failed logins (wrong username/password). */
    private int failedLogins = 0;

    /** The user for this session, set after a successful authentication. */
    private static User user;
    
    /** Used to read preferences. */
    private Preferences prefs = Preferences.userNodeForPackage(Session.class);

    /** The identity of the client service requesting this session. */
    private Principal client;
    
    
    /**
     * Protected constructor, only to be used by
     * <code>SessionStore<code>. The session URL is set to the
     * authentication URL read from global preferences
     * (<code>no.feide.moria.LoginURL</code>).
     * @param sessionID The session's ID.
     * @param attributes The attributes requested for this session.
     *                   <code>null</code> allowed.
     * @param prefix The prefix, a value stored in the session and used to
     *               build the <code>SessionStore.verifySession</code> return
     *               value. May be <code>null</code>.
     * @param postfix The postfix, a value stored in the session and used to
     *                build the <code>SessionStore.verifySession</code> return
     *                value. May be <code>null</code>.
     * @param client The client service identifier.
     */
    protected Session(String sessionID, String[] attributes, String prefix, String postfix, Principal client) {
        log.finer("Session(String, String[], String)");
        
        this.sessionID = sessionID;
        this.request = attributes;
	this.prefix = prefix;
        this.postfix = postfix;
	this.client = client;
        redirectURL = prefs.get("LoginURL", null);
    }
    

    /**
     * Authenticates a user through the backend. The session gets a new ID
     * and the session URL is set to the session URL from the Moria
     * configuration (<code>no.feide.moria.SessionURL</code>). If the number
     * of failed logins for this session exceeds the maximum number
     * (<code>no.feide.moria.MaxFailedLogins</code>) the session is removed.
     * The caller should possibly verify the session ID after each failed
     * attempt to check if the session is still alive.<br>
     * The default number of failed logins is 3.
     * @param c User's credentials.
     * @return <code>true</code> if the user was authenticated, otherwise
     *         <code>false</code>.
     * @throws BackendException If an error occurs when accessing the backend.
     * @throws SessionException If an error occurs when accessing the
     *                          session store.
     */
    public boolean authenticateUser(Credentials c)
    throws BackendException, SessionException {
        log.finer("authenticateUser(Credentials)");
                
        // Authenticate user.
        user = User.authenticate(c);
        if (user != null) {
            // Update session ID and URL.
            sessionID = SessionStore.getInstance().confirmSession(sessionID);
            redirectURL = prefs.get("SessionURL", null);
            log.fine("Good authN; new session ID is "+sessionID+", new URL is "+redirectURL);
            return true;
        }
        
        // Authentication failed. Check if the session should be invalidated.
        log.fine("Authentication failed");
        failedLogins++;
        try {
            Integer maxFailures = Integer.decode(Preferences.userNodeForPackage(Session.class).get("MaxFailedLogins", "3"));
            if (failedLogins == maxFailures.intValue()) {
                // Remove ourselves from the session store.
                log.fine("Invalidating session: "+sessionID);
                SessionStore.getInstance().deleteSession(sessionID);
		log.info("Max number of authN attempts ("+maxFailures+") reached");
            }
        } catch (NumberFormatException e) {
            log.severe("NumberFormatException caught and re-thrown as SessionException");
            throw new SessionException(e);
        }
        return false;
    }
    
    
    /**
     * Checks if the session has been through authentication.
     * @return <code>true</code> if successfully authenticated, otherwise
     *         <code>false</code>.
     */
    public boolean isAuthenticated() {
        log.finer("isAuthenticated()");
        
        // The user variable is only set after a successful authentication.
        if (user == null)
            return false;
        else
            return true;
    }
       
       
    /**
     * Returns the session descriptor.
     * @return The session descriptor.
     */
    public SessionDescriptor getDescriptor() {
        log.finer("getDescriptor()");
        return new SessionDescriptor(sessionID, redirectURL);
    }    
    

    /**
     * Returns the concatenated prefix/id/postfix string.
     * @return The concatenated string <code>[prefix][id][postfix]</code>
     *         where <code>[prefix]</code> and <code>[postfix]</code> are the
     *         parameter strings given to the constructor.
     */
    public String getPrefixPostfixCompound() {
        String retval = "";
        if (prefix != null)
            retval = retval + prefix;
        retval = retval + sessionID;
        if (postfix != null)
            retval = retval + postfix;
	return retval;
    }

    
    /**
     * Retrieves user attributes from the backend. Requires a previous
     * successful authentication against the backend.
     * @param attributes User element attribute names.
     * @return The requested user attributes. May be an empty set if no
     *         attributes have been requested.
     * @throws SessionException If a BackendException is caught, or if
     *                          the user has yet to be authenticated.
     */
    public UserAttribute[] getAttributes()
    throws SessionException {
        log.finer("getAttributes()");
        
        // Check for authentication.
        if (user == null) {
            log.warning("User attribute request without previous authentication");
            throw new SessionException("User attribute request without previous authentication");
        }
        
        // If no attributes have been requested, return an empty array.
        if ( (request == null) ||
             (request.length == 0) )
            return new UserAttribute[] {};
        
        // Look up through backend.
        try {
            return user.lookup(request);
        } catch (BackendException e) {
            log.severe("BackendException caught and re-thrown as SessionException");
            throw new SessionException(e);
        }
    }
    
    
    /**
     * Updates the session's current ID. Note that this does not update the
     * <code>SessionStore</code> internal name for this session, which may
     * cause inconsistency if used incorrectly.
     * @param sessionID The new session ID.
     * @throws SessionException If the new session ID is not unique.
     */
    protected void setID(String sessionID) 
    throws SessionException {
        log.finer("setID(String)");
        
        // Check for uniqueness.
        if (SessionStore.getInstance().getSession(sessionID) != null) {
            log.severe("Session already exists: " + sessionID);
            throw new SessionException("Session already exists: " + sessionID);
        }
            
        this.sessionID = sessionID;
    }
    
    
    /**
     * Returns the session's current ID.
     * @return Current session ID.
     */
    protected String getID() {
        log.finer("getID()");
        
        return sessionID;
    }

    
    /**
     * Returns the identity of the client requesting this session.
     * @return The identity of the client service.
     */
    public Principal getClientPrincipal() {
	log.finer("getClientPrincipal()");

	return client;
    }

}
