package no.feide.moria;

import java.util.TimerTask;
import java.util.logging.Logger;    


/**
 * Represents a periodic task to manipulate the session store.
 */
public class SessionStoreTask
extends TimerTask {
    
    /** Used for logging. */
    private static Logger log = Logger.getLogger(SessionStoreTask.class.toString());
    
    /**
     * Do something clever.
     **/
    public void run() {
        log.fine("run()");

        int timeout = new Integer(System.getProperty("no.feide.moria.SessionTimeout")).intValue()*60*1000; // Minutes to milliseconds

        SessionStore.getInstance().checkTimeout(timeout);
    }    
    
}
