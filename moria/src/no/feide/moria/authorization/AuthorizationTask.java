package no.feide.moria.authorization;

import java.util.TimerTask;
import java.util.logging.Logger;    


/**
 * Represents a periodic task to manipulate the session store.
 */
public class AuthorizationTask
extends TimerTask {
    
    /** Used for logging. */
    private static Logger log = Logger.getLogger(AuthorizationTask.class.toString());
    
    /**
     * Called periodically by the timer. 
     **/
    public void run() {
        log.finer("run()");

        int timeout = new Integer(System.getProperty("no.feide.moria.SessionTimeout")).intValue()*60*1000; // Minutes to milliseconds

        AuthorizationData.getInstance().upToDate();
    }    
    
}
