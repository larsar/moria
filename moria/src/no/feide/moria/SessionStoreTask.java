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
        log.finer("run()");

        SessionStore.getInstance().checkTimeout(1);
        
        // TODO: Add session store maintenance operations here.
        //log.info("Cleaning up SessionStore - not.");
    }    
    
}
