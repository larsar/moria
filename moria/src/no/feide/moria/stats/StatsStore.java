/**
 * Copyright (C) 2003 FEIDE
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package no.feide.moria.stats;

import java.util.logging.Logger;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;


/**
 * This class is used to generate a Stats singleton object to store
 * statistical information.
 */
public class StatsStore {
    
    /** Used for logging. */
    private static Logger log = Logger.getLogger(StatsStore.class.toString());

    /** Static pointer to singleton object. */
    private static StatsStore me;
    
    /** Time when Moria was started */
    private Date started = null;


    /** Contains all Web Service statistics. */
    Map wsStats = null;


    /**
     * Constructor. 
     */
    private StatsStore() {
        log.finer("StatsStore()");
        started = new Date();
        wsStats = Collections.synchronizedMap(new HashMap());
    }
    
    
    /** 
     * Returns a pointer to the StatsStore singleton object.
     * @return StatsStore The singleton object
     * @throws SessionException If an error occurs creating the singleton
     *                          instance.
     */
    public static StatsStore getInstance() {
        log.finer("getInstance()");
        
        if (me == null) 
            me = new StatsStore();
        return me;
    }
    
    
    private WebServiceStats getWSStats(String wsName) {
        WebServiceStats wsStat = (WebServiceStats) wsStats.get(wsName);

        // Create new stats object if the WebService doesn't have one
        if (wsStat == null) {
            wsStat =  new WebServiceStats(wsName);
            wsStats.put(wsName, wsStat);
        }
        
        return wsStat;
    }

    
    public HashMap upTime() {
        HashMap upTime = new HashMap();
        long upMSec  = new Date().getTime()-started.getTime();
        
        /* Constans for calculation */
        int mSecMin  = 60*1000;
        int mSecHour = 60*mSecMin;
        int mSecDay = 24*mSecHour;

        /* Uptime calculations */
        long days    = upMSec/mSecDay;
        long hours   = (upMSec-(days*mSecDay))/mSecHour;
        long min     = (upMSec-(days*mSecDay+hours*mSecHour))/mSecMin;
        
        /* Return a HashMap with data */
        upTime.put("upStartDate", started.toString());
        upTime.put("upDays", ""+days);
        upTime.put("upHours", ""+hours);
        upTime.put("upMin", ""+min);
                        
        return upTime;
    }

    public HashMap sessions() {
        int loginAttemptFailed  = 0;
        int loginAttemptSuccess = 0;
        
        int createdSessions = 0;
        int deniedSessionsAuthentication = 0;
        int deniedSessionsAuthorization  = 0;
        
        int sessionsTimeoutSSO = 0;
        int sessionsTimeoutAUTH= 0;
        int sessionsTimeoutUSER= 0;

        HashMap stats = new HashMap();

        for (Iterator it = wsStats.keySet().iterator(); it.hasNext(); ) {
            WebServiceStats ws = (WebServiceStats) wsStats.get(it.next());
            
            createdSessions += ws.getSessionStats("created");
            deniedSessionsAuthentication += ws.getSessionStats("deniedAuthentication");
            deniedSessionsAuthorization += ws.getSessionStats("deniedAuthorization");
            sessionsTimeoutSSO += ws.getSessionStats("timeoutSSO");
            sessionsTimeoutAUTH += ws.getSessionStats("timeoutAuth");
            sessionsTimeoutUSER += ws.getSessionStats("timeoutUser");
            loginAttemptFailed += ws.getSessionStats("authFailed");
            loginAttemptFailed += ws.getSessionStats("authSuccess");
        }

        stats.put("loginAttemptFailed", ""+loginAttemptFailed);
        stats.put("loginAttemptSuccess", ""+loginAttemptSuccess);
        stats.put("createdSessions", ""+createdSessions);
        stats.put("deniedSessionsAuthentication", ""+deniedSessionsAuthentication);
        stats.put("deniedSessionsAuthorization", ""+deniedSessionsAuthorization);
        stats.put("sessionsTimeoutSSO", ""+sessionsTimeoutSSO);
        stats.put("sessionsTimeoutAUTH", ""+sessionsTimeoutAUTH);
        stats.put("sessionsTimeoutUSER", ""+sessionsTimeoutUSER);

        stats.put("activeSessions", ""+(createdSessions-(sessionsTimeoutSSO+sessionsTimeoutAUTH+sessionsTimeoutUSER)));
        
        return stats;
    }

    public void loginAttempt(String wsName, String result) {
        getWSStats(wsName).loginAttempt(result);
    }
    
    public void createSessionAttempt(String wsName, String result) {
        getWSStats(wsName).createSessionAttempt(result);
    }

    public void sessionTimeout(String wsName, String type) {
        getWSStats(wsName).sessionTimeout(type);
    }

}
