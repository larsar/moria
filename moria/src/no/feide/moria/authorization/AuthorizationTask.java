package no.feide.moria.authorization;

import java.util.TimerTask;
import java.util.logging.Logger;
import no.feide.moria.Configuration;
import no.feide.moria.ConfigurationException;

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

        try {
            
            int timeout = new Integer(Configuration.getProperty("no.feide.moria.SessionTimeout")).intValue()*60*1000; // Minutes to milliseconds
            AuthorizationData.getInstance().upToDate();
            
        } catch (ConfigurationException e) {
            log.warning("ConfigurationException caught, message is \""+e.getMessage()+'\"');
        }
        
    }    
    
}
