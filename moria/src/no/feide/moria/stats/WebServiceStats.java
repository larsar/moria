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

import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;


/**
 * This class is used to generate a Stats singleton object to store
 * statistical information.
 */
public class WebServiceStats {
    
    /** Timestamp - first use of web service */
    private Date firstUsed = null;

    /** Timestamp - last use of web service */
    private Date lastUsed  = null;

    private HashMap counters = new HashMap();

    /** Used for logging. */
    private static Logger log = Logger.getLogger(WebServiceStats.class.toString());

    /** Name/ID of web service */
    private String name;
    

    /** Known counters */
    private String[] knownCounters = new String[] {
        "activeSessions",
        "createdSessions",
        "loginSuccessful",
        "loginFailed",
        "loginSSO",
        "timeoutUser",
        "timeoutSSO",
        "timeoutMellon",
        "sessionDeniedURL",
        "sessionDeniedAuthZ",
        "logout"
    };



    /**
     * Constructor. Updates timestamps and sets the web service name.
     * @param name The web service Name/ID
     */
    public WebServiceStats(String name) {
        log.finer("WebServiceStats()");
        this.name = name;
        firstUsed = new Date();
        lastUsed  = firstUsed;

        /** Reset known counters */
        for (int i = 0; i < knownCounters.length; i++) {
            counters.put(knownCounters[i], new Integer(0));
        }
        
    }


    
    /**
     * Returns a HashMap of all statistical data.
     * @return The HashMap with all statistical data
     */
    HashMap getStats() {
        return counters;
    }
   

    void increaseCounter(String counter) {
        Integer value = (Integer) counters.get(counter);

        if (value == null)
            counters.put(counter, new Integer(1));

        else
            counters.put(counter, new Integer(value.intValue()+1));

    }

    void decreaseCounter(String counter) {
        Integer value = (Integer) counters.get(counter);

        if (value == null) {
            log.warning("Decreasing counter that was not initiated.");
            counters.put(counter, new Integer(-1));
        }

        else
            counters.put(counter, new Integer(value.intValue()-1));
    }
    
    
    /** 
     * Updated timestamp
     */
    private void timeStamp() {
        lastUsed = new Date();
    }
}
