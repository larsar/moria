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
import no.feide.moria.Session;


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

    /** Number of failed attempts to create sesion (authentication of
     * web service failed */
    int deniedSessionsAuthentication = 0;

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
    
    

    /**
     * Return a WebServiceStat object for a given web service. If it
     * doesn't exist (the web service hasn't used Moria in this
     * session), a new stats-object is created and inserted in the register.
     * @param wsName The name/id of the webservice
     * @return The stats object for the given web service
     */
    private WebServiceStats getWSStats(String wsName) {
        WebServiceStats wsStat = (WebServiceStats) wsStats.get(wsName);

        if (wsStat == null) {
            wsStat =  new WebServiceStats(wsName);
            wsStats.put(wsName, wsStat);
        }
        
        return wsStat;
    }

    

    /**
     * Calculates Morias uptime and returns a HashMap with uptime in
     * date string along with days, hours and minutes.
     * @return The HashMap with calculated uptime data
     */
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



    /**
     * Gathers statistics for all web services into one HashMap. The
     * HashMap also contains an entry for the sum of all stats.
     * @return The HashMap with all statisical data
     */
    public HashMap getStats() {
        HashMap stats = new HashMap();
        HashMap totalStats = new HashMap();

        for (Iterator it = wsStats.keySet().iterator(); it.hasNext(); ) {
            String wsName = (String) it.next();
            WebServiceStats ws = (WebServiceStats) wsStats.get(wsName);
            HashMap statsValues = ws.getStats();

            stats.put(wsName, statsValues);

            for (Iterator it2 = wsStats.keySet().iterator(); it2.hasNext(); ) {
                String counter = (String) it2.next();
                Integer value = (Integer) statsValues.get(counter);
                Integer sum = (Integer) totalStats.get(counter);
                
                if (sum != null)
                    sum = new Integer(sum.intValue() + value.intValue());
                else
                    sum = value;
                
                totalStats.put(counter, sum);
            }
            
        }        
        
        return stats;
    }


    
    /**
     * Log a login attempt for a given web service.
     * @param wsname The name of the web service
     * @param result The result of the login attempt: "SUCCESS",
     * "FAILED" or "SSO"
     */
    public void loginAttempt(String wsID, String result) {
        getWSStats(wsID).loginAttempt(result);
    }
    


    /**
     * Log a "create session" attempt for a given web service.
     * @param wsname The name of the web service
     * @param result The result of the attempt: "SUCCESS", "URL", "AUTHO"
     */
    public void createSessionAttempt(String wsID, String result) {
        if (result.equals("AUTHN")) {
            deniedSessionsAuthentication++;
        }
        else 
            getWSStats(wsID).createSessionAttempt(result);
    }



    /**
     * Log when a session times out, for a given web service.
     * @param wsname The name of the web service
     * @param result Type of TIMEOUT: "SSO", "AUTH", "USER"
     */
    public void sessionTimeout(String wsID, String type) {
        getWSStats(wsID).sessionTimeout(type);
    }



    /**
     * Returns the number of times unauthorized web services tries to
     * access Moria. These web services has been let through the web
     * server but arnen't configured in Moria.
     * @return The number of denied (authentication) attempts to
     * create a session.
     */
    public int getDeniedSessionsAuthentication() {
        return deniedSessionsAuthentication;
    }
}
