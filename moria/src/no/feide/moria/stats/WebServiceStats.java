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

    /** Number of failed (wrong username/password) login attempts */
    private int loginAttemptFailed  = 0;

    /** Number of successful login attempts */
    private int loginAttemptSuccess = 0;

    /** Number of login with SSO */
    private int loginAttemptSSO     = 0;

    /** Total number of created sessions */
    private int createdSessions     = 0;
    
    /** Number of times a web service has been denied session due to
     * unauthorized request for user attributes */
    private int deniedSessionsAuthorization  = 0;

    /** Number of times a web service has been denied session due to
     * invalid return URL (redirect back to web service after login) */
    private int deniedSessionsURL   = 0;

    /** Number of sessions that has timed out (Single Sign On) */
    private int sessionsTimeoutSSO  = 0;

    /** Number of sessions that has timed out (web service didn't
     * fetch user attributes in time) */
    private int sessionsTimeoutAUTH = 0;

    /** Number of sessions that has timed out (user didn't supply
     * username/password in time */
    private int sessionsTimeoutUSER = 0;

    /** Used for logging. */
    private static Logger log = Logger.getLogger(WebServiceStats.class.toString());

    /** Name/ID of web service */
    private String name;
    


    /**
     * Constructor. Updates timestamps and sets the web service name.
     * @param name The web service Name/ID
     */
    public WebServiceStats(String name) {
        log.finer("WebServiceStats()");
        this.name = name;
        firstUsed = new Date();
        lastUsed  = firstUsed;
    }


    
    /**
     * Returns a HashMap of all statistical data.
     * @return The HashMap with all statistical data
     */
    public HashMap getStats() {
        HashMap stats = new HashMap();
        
        // TODO: Only integers are expected to be in the HashMap (StatsServlet)
        // stats.put("firstUsed", new Integer(firstUsed));
        // stats.put("lastUsed", new Integer(lastUsed));
        stats.put("loginAttemptFailed", new Integer(loginAttemptFailed));
        stats.put("loginAttemptSuccess", new Integer(loginAttemptSuccess));
        stats.put("loginAttemptSSO", new Integer(loginAttemptSSO));
        stats.put("createdSessions", new Integer(createdSessions));
        stats.put("deniedSessionsAuthorization", new Integer(deniedSessionsAuthorization));
        stats.put("deniedSessionsURL", new Integer(deniedSessionsURL));
        stats.put("sessionsTimeoutSSO", new Integer(sessionsTimeoutSSO));
        stats.put("sessionsTimeoutAUTH", new Integer(sessionsTimeoutAUTH));
        stats.put("sessionsTimeoutUSER", new Integer(sessionsTimeoutUSER));
        stats.put("activeSessions", new Integer(createdSessions - sessionsTimeoutSSO - sessionsTimeoutAUTH - sessionsTimeoutUSER));

        return stats;
    }
    


    /**
     * Log a login attempt.
     * @param result The result of the login attempt: "SUCCESS",
     * "FAILED" or "SSO"
     */
    protected void loginAttempt(String result) {
        timeStamp();

        if (result.equals("SUCCESS"))
            loginAttemptSuccess++;

        else if (result.equals("FAILED")) 
            loginAttemptFailed++;

        else if (result.equals("SSO")) 
            loginAttemptSSO++;

        else
            log.warning("Illegal result status: "+result);
    }
    


    /**
     * Log a "create session" attempt.
     * @param result The result of the attempt: "SUCCESS", "URL", "AUTHO"
     */
    protected void createSessionAttempt(String type) {
        timeStamp();
        
        if (type.equals("SUCCESS"))
            createdSessions++;

        else if (type.equals("URL"))
            deniedSessionsURL++;
        
        else if (type.equals("AUTHO")) 
            deniedSessionsAuthorization++;
            
    }



    /**
     * Log when a session times out.
     * @param result Type of TIMEOUT: "SSO", "AUTH", "USER"
     */
    protected void sessionTimeout(String type) {
        timeStamp();

        if (type.equals("SSO"))
            sessionsTimeoutSSO++;

        else if (type.equals("AUTH")) 
            sessionsTimeoutAUTH++;

        else if (type.equals("USER"))
            sessionsTimeoutUSER++;

        else
            log.warning("Illegal timeout type: "+type);
            
        
    }

    
    
    /** 
     * Updated timestamp
     */
    private void timeStamp() {
        lastUsed = new Date();
    }
}
