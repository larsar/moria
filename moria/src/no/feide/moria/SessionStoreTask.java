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

    /** Used for logging. */
    private static Logger log = Logger.getLogger(SessionStoreTask.class.toString());
    
    /** Local pointer to session store. */
    private SessionStore sessionStore;
    
    
    /**
     * Constructor. Sets the local pointer to the session store.
     * @throws SessionException If there's a problem getting the session store
     *                          pointer.
     */
    public SessionStoreTask()
    throws SessionException {
        sessionStore = SessionStore.getInstance();
        
        timeoutSec = new Integer(System.getProperty("no.feide.moria.SessionTimeout")).intValue(); // Minutes to milliseconds
        ssoTimeoutMin = new Integer(System.getProperty("no.feide.moria.SessionSSOTimeout")).intValue(); // Minutes to milliseconds
        log.config("Session time out set to "+timeoutSec+" seconds.");
        log.config("Session SSO time out set to "+ssoTimeoutMin+" minutes.");
    }
     
    
    /**
     * Called periodically by the timer.
     **/
    public void run() {
        log.fine("run()");
        sessionStore.checkTimeout(timeoutSec*1000, ssoTimeoutMin*60*1000);
    }    
    
}
