package no.feide.moria;

import java.security.Principal;
import java.util.Hashtable;
import java.util.logging.Logger;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;
import no.feide.moria.service.SessionDescriptor;
import no.feide.moria.service.UserAttribute;
import org.doomdark.uuid.UUIDGenerator;

public class SessionStore {
    
    /** Used for logging. */
    private static Logger log = Logger.getLogger(SessionStore.class.toString());

    /** Static pointer to singleton object. */
    static private SessionStore me;
    
    /** Contains all active session objects. Key is current session ID. */
    private Hashtable sessions = new Hashtable();
   
    
    /** 
     * Returns a pointer to the SessionStore singelton object.
     * @return SessionStore
     * @throws SessionException If an error occurs creating the singleton
     *                               instance.
     */
    public static SessionStore getInstance()
    throws SessionException {
        log.finer("getInstance()");
        
        if (me == null)
            me = new SessionStore();
        return me;
    }
    
    
    /**
     * Generates a unique session ID.
     * @param sessions The list of sessions.
     * @return A new session ID.
     * @throws SessionException If an unique (that is, non-existing) ID
     *                          cannot be generated in 20 tries.
     */
    protected String generateSessionID()
    throws SessionException {
        log.finer("generateSessionID()");

        // Try 20 times to generate a unique session ID, then give up.
        SessionStore store = SessionStore.getInstance();
        String generated = null;
        int count = 0;
        do {
            if (count++ == 20) {
                log.severe("Unable to create unique session ID");
                throw new SessionException("Unable to create unique session ID");
            }          
            generated = UUIDGenerator.getInstance().generateRandomBasedUUID().toString();
        } while (sessions.containsKey(generated));

        return generated;
    }    


    /**
     * Creates a new session, with an attribute request.
     * @param attributes The list of requested attribute names, possibly
     *                   <code>null</code>.
     * @param prefix The prefix, used to build the <code>verifySession</code>
     *               return value. May be <code>null</code>.
     * @param postfix The postfix, used to build the
     *                <code>verifySession</code> return value.
     *                May be <code>null</code>.
     * @param client Identifies the client service initiating the session.
     *               May be <code>null</code>.
     * @return A new session.
     */
    public Session createSession(String[] attributes, String prefix, String postfix, Principal client)
    throws SessionException {
        log.finer("createSession(String[], String, String, Principal)");

        // TODO:
        // Authorize client service; attribute request valid?
        
        String sessionID = generateSessionID();
        Session session = new Session(sessionID, attributes, prefix, postfix, client);
        sessions.put(sessionID, session);
        return session;
    }


    /**
     * Returns the session for a given session ID.
     * @param sessionID Current ID for the Moria session, or <code>null</code>
     *                  if the session either didn't exist, or the session ID
     *                  also was <code>null</code>.
     */
    public Session getSession(String sessionID) 
    throws SessionException {
        log.finer("getSession(String)");
        
        // Sanity check.
        if (sessionID == null)
            return null;
        
        if (!sessions.containsKey(sessionID)) {
            log.fine("No such session: "+sessionID);
            return null;
        } else {
            return (Session)sessions.get(sessionID);
        }
    }

  
    /**
     * Removes a Moria session.
     * @param session The session.
     */     
    public void deleteSession(Session session) {
        log.finer("deleteSession(Session)");
        
        sessions.remove(session.getID());
    } 
    
    
    /**
     * Confirms a Moria session, that is, gives it a new second-level session
     * ID after a successful authentication.
     * @param sessionID The old session ID.
     * @throws SessionExcepion If the old session ID cannot be found.
     */
    protected void renameSession(Session session)
    throws SessionException {
        log.finer("renameSession(String)");
        
        // Generate a new session ID.
        String oldID = session.getID();
        session.setID(generateSessionID());
        
        // Remove old session.
        sessions.remove(oldID);
        sessions.put(newID, session);
        
        log.fine("Session renamed to "+newID);
    }

}
