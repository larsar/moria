package no.feide.moria;

import java.security.Principal;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Collections;
import java.util.Map;
import java.util.Vector;
import java.util.Enumeration;
import java.util.logging.Logger;
import org.doomdark.uuid.UUIDGenerator;
import java.io.FileNotFoundException;
import java.io.IOException;
import no.feide.moria.authorization.WebService;

public class SessionStore {
    
    /** Used for logging. */
    private static Logger log = Logger.getLogger(SessionStore.class.toString());

    /** Static pointer to singleton object. */
    static private SessionStore me;
    
    /** Contains all active session objects. Key is current session ID. */
    //private Hashtable sessions = new Hashtable();
    Map sessions = null;
    
    
    
    /**
     * Constructor. Kicks off the session store maintenance thread, to handle
     * session timeouts. Will read the Moria property file named in the
     * system property <code>no.feide.moria.config.file</code>, or
     * <code>/moria.properties</code> if the property is not set.
     * @throws SessionException If the properties file doesn't include the
     *                          properties
     *                          <code>no.feide.moria.SessionStoreInitMapSize</code>
     *                          or
     *                          code>no.feide.moria.SessionStoreMapLoadFactor</code>.
     *                          Also thrown if a
     *                          <code>FileNotFoundException</code> or
     *                          <code>IOException</code> is caught while
     *                          reading the property file.
     */
    private SessionStore()
    throws SessionException {
        log.finer("SessionStore()");

        // Read properties.
        try {
            if (System.getProperty("no.feide.moria.config.file") == null) {
                log.config("no.feide.moria.config.file not set; default is \"/moria.properties\"");
                System.getProperties().load(getClass().getResourceAsStream("/moria.properties"));
            }
            else {
                log.config("no.feide.moria.config.file set to \""+System.getProperty("no.feide.moria.config.file")+'\"');
                System.getProperties().load(getClass().getResourceAsStream(System.getProperty("no.feide.moria.config.file")));
            }
        } 
        catch (FileNotFoundException e) {
            log.severe("FileNotFoundException during system properties import");
            throw new SessionException("FileNotFoundException during system properties import");
        } 
        catch (IOException e) {
            log.severe("IOException during system properties import");
            throw new SessionException("IOException during system properties import");
        }
        
        // Setting properties, with sanity checks.
        String s = System.getProperty("no.feide.moria.SessionStoreInitMapSize");
        if (s == null) {
            log.severe("Missing required system property: no.feide.moria.SessionStoreInitMapSize");
            throw new SessionException("Missing required system property: no.feide.moria.SessionStoreInitMapSize");
        }
        int initialSize = new Integer(s).intValue();
        s = System.getProperty("no.feide.moria.SessionStoreMapLoadFactor");
        if (s == null) {
            log.severe("Missing required system property: no.feide.moria.SessionStoreMapLoadFactor");
            throw new SessionException("Missing required system property: no.feide.moria.SessionStoreMapLoadFactor");
        }
        float loadFactor = new Float(s).floatValue();
        
        sessions = Collections.synchronizedMap(new HashMap(initialSize, loadFactor));
        log.config("Session register initialized. Initial size="+initialSize+" loadFactor="+loadFactor);
    }
    
    
    /** 
     * Returns a pointer to the SessionStore singleton object.
     * @return SessionStore
     * @throws SessionException If an error occurs creating the singleton
     *                          instance.
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
        //SessionStore sessionStore = SessionStore.getInstance();
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
    public Session createSession(String[] attributes, String prefix, String postfix, Principal client, WebService ws)
    throws SessionException {
        log.finer("createSession(String[], String, String, Principal)");

        // TODO:
        // Authorize client service; attribute request valid?
        
        String sessionID = generateSessionID();
        Session session = new Session(sessionID, attributes, prefix, postfix, client, ws);
        synchronized (sessions) {
            sessions.put(sessionID, session);
        }

        return session;
    }


    /**
     * Returns the session for a given session ID.
     * @param sessionID Current ID for the Moria session.
     * @throws SessionException If the session wasn't found.
     */
    // TODO: Maybe use another type of exception to distinguish other
    // session exceptions from not found. NoSuchSessionException?
    public Session getSession(String sessionID) 
    throws SessionException {
        log.finer("getSession(String)");
        
        if (sessionID == null || !sessions.containsKey(sessionID)) {
            log.severe("No such session: "+sessionID);
            throw new NoSuchSessionException("No such session: "+sessionID);
        } else
            return (Session)sessions.get(sessionID);
    }

  
    /**
     * Removes a Moria session.
     * @param session The session.
     */     
    public synchronized void deleteSession(Session session) {
        log.finer("deleteSession(Session)");
        
        synchronized (sessions) {
            sessions.remove(session.getID());
        }
    } 
    
    
    /**
     * Confirms a Moria session, that is, gives it a new second-level session
     * ID after a successful authentication.
     * @param sessionID The old session ID.
     * @throws SessionExcepion If the old session ID cannot be found.
     */
    public void renameSession(Session session)
    throws SessionException {
        log.finer("renameSession(String)");
        
        // Generate a new session ID.
        String oldID = session.getID();

        synchronized (sessions) {
            session.setID(generateSessionID());
            
            // Remove old session.
            sessions.remove(oldID);
            sessions.put(session.getID(), session);
        }

        log.fine("Session renamed to "+session.getID());
    }

    
    protected void checkTimeout(int timeout) {

        Vector invalidatedSessions = new Vector();
        Date start = new Date();

       // Find all timedout sessions.
        synchronized (sessions) {
            for (Iterator iterator = sessions.keySet().iterator(); iterator.hasNext();) {
                String key = (String) iterator.next();
                Session session = (Session) sessions.get(key);
            
                if (!session.isValid(new Date().getTime()-timeout)) {
                    log.fine("Invalidating session (timeout): "+session.getID());
                    invalidatedSessions.add(session);
                }
            }

            // Invalidate sessions
            for (Enumeration enum = invalidatedSessions.elements(); enum.hasMoreElements(); ) {
                deleteSession((Session)enum.nextElement());
            }
        }
        log.info(invalidatedSessions.size()+" of "+sessions.size()+" sessions invalidated in "+(new Date().getTime()-start.getTime())+ " ms.");

    }

}
