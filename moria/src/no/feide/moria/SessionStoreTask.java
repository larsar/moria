package no.feide.moria;

import java.util.TimerTask;
import java.util.logging.Logger;    


/**
 * Represents a periodic task to manipulate the session store.
 */
public class SessionStoreTask
extends TimerTask {

    /** Session time out value. */
    private int timeoutSec;

    /** SSO Session time out value. */
    private int ssoTimeoutMin;

    /** Authenticated session time out value. */
    private int authTimeoutSec;

    /** Used for logging. */
    private static Logger log = Logger.getLogger(SessionStoreTask.class.toString());
    
    /** Local pointer to session store. */
    private SessionStore sessionStore;
    
    
    /**
     * Constructor. Sets the local pointer to the session store.
     * @throws SessionException If there's a problem getting the session store
     *                          pointer, or if any of the required
     *                          configuration settings are undefined.
     */
    public SessionStoreTask()
    throws SessionException {
        log.info("SessionStoreTask()");
        
        sessionStore = SessionStore.getInstance();
       
        // Sets some properties.
        try {
            
            timeoutSec = new Integer(Configuration.getProperty("no.feide.moria.SessionTimeout")).intValue();
            log.config("Session time out set to "+timeoutSec+" seconds.");
            ssoTimeoutMin = new Integer(Configuration.getProperty("no.feide.moria.SessionSSOTimeout")).intValue();
            log.config("Session SSO time out set to "+ssoTimeoutMin+" minutes.");
            authTimeoutSec = new Integer(Configuration.getProperty("no.feide.moria.AuthenticatedSessionTimeout")).intValue();
            log.config("Authenticated session time out set to "+authTimeoutSec+" seconds.");
            
        } catch (ConfigurationException e) {
            log.severe("ConfigurationException caught and re-thrown as SessionException");
            throw new SessionException("ConfigurationException caught", e);
        }
    }
     
    
    /**
     * Called periodically by the timer.
     **/
    public void run() {
        log.fine("run()");
        sessionStore.checkTimeout(timeoutSec*1000, authTimeoutSec*1000, ssoTimeoutMin*60*1000);
    }    
    
}
