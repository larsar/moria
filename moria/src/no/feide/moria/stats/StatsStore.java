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

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;


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

    /** Global counters */
    private HashMap counters = new HashMap();



    /**
     * Constructor. 
     */
    private StatsStore() {
        log.finer("StatsStore()");
        started = new Date();
        wsStats = Collections.synchronizedMap(new HashMap());
        Integer defCounter = new Integer(0);
        counters.put("deniedSessionAuthN", defCounter);
        counters.put("sessionsSSOLogout", defCounter);
        counters.put("sessionsSSOTimeout", defCounter);
        counters.put("sessionsSSOActive", defCounter);
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

            for (Iterator it2 = statsValues.keySet().iterator(); it2.hasNext(); ) {
                String counter = (String) it2.next();
                Integer value = (Integer) statsValues.get(counter);
                Integer sum = (Integer) totalStats.get(counter);
                
                if (sum != null)
                    sum = new Integer(sum.intValue() + value.intValue());
                else
                    sum = value;
                
                totalStats.put(counter, sum);
            }
            
            stats.put("All", totalStats);
            
        }        
        


        return stats;
    }


    /**
     * Get global stats counters. 
     * @return HashMap with Integer counters
     */
    public HashMap getCounters() {
        return counters;
    }


    /**
     * Increase a counter by one.
     * @param counter Name of the counter
     */
    public void increaseCounter(String counter) {
        Integer value = (Integer) counters.get(counter);

        if (value == null) {
            log.info("Creating new counter: "+counter);
            counters.put(counter, new Integer(1));
        }

        else
            counters.put(counter, new Integer(value.intValue()+1));

    }



    /**
     * Decrease a counter by one.
     * @param counter Name of the counter
     */
    public void decreaseCounter(String counter) {
        Integer value = (Integer) counters.get(counter);

        if (value == null) {
            log.warning("Decreasing counter that was not initiated.");
            counters.put(counter, new Integer(-1));
        }

        else
            counters.put(counter, new Integer(value.intValue()-1));
    }



    /**
     * Wrapper for increasing a counter (by one) in a web service stats object.
     * @param wsID Web Service ID
     * @param counter Name of counter to increase by one
     */
    public void incStatsCounter(String wsID, String counter) {
        getWSStats(wsID).increaseCounter(counter);
    }



    /**
     * Wrapper for decreasing a counter (by one)in a web service stats object.
     * @param wsID Web Service ID
     * @param counter Name of counter to decrease by one
     */
    public void decStatsCounter(String wsID, String counter) {
        getWSStats(wsID).decreaseCounter(counter);
    }



    /**
     * Wrapper for resetting a counter in a web service stats object.
     * @param wsID Web Service ID
     * @param counter Name of counter to be reset
     */
    public void resetStatsCounter(String wsID, String counter) {
        getWSStats(wsID).resetCounter(counter);
    }        

}
