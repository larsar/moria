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
import java.util.logging.Logger;


/**
 * This class is used to generate a Stats singleton object to store
 * statistical information.
 */
public class WebServiceStats {
    
    Date firstUsed = null;
    Date lastUsed  = null;

    int loginAttemptFailed  = 0;
    int loginAttemptSuccess = 0;

    int createdSessions = 0;
    int deniedSessionsAuthorization  = 0;
    int deniedSessionsURL  = 0;

    int sessionsTimeoutSSO = 0;
    int sessionsTimeoutAUTH= 0;
    int sessionsTimeoutUSER= 0;

    /** Used for logging. */
    private static Logger log = Logger.getLogger(WebServiceStats.class.toString());

    /** Name of web service */
    private String name;
    
    /**
     * Constructor. 
     */
    public WebServiceStats(String name) {
        log.finer("WebServiceStats()");
        this.name = name;
        firstUsed = new Date();
        lastUsed  = firstUsed;
    }


    protected void loginAttempt(String result) {
        timeStamp();

        if (result.equals("SUCCESS"))
            loginAttemptSuccess++;

        else if (result.equals("FAILED")) 
            loginAttemptFailed++;

        else
            log.warning("Illegal result status: "+result);
    }
    
    protected void createSessionAttempt(String type) {
        timeStamp();
        
        if (type.equals("SUCCESS"))
            createdSessions++;

        else if (type.equals("URL"))
            deniedSessionsURL++;
        
        else if (type.equals("AUTHO")) 
            deniedSessionsAuthorization++;
            
    }

    protected void sessionTimeout(String type) {
        timeStamp();

        if (type.equals("SSO"))
            sessionsTimeoutSSO++;

        if (type.equals("AUTH")) 
            sessionsTimeoutAUTH++;

        if (type.equals("USER"))
            sessionsTimeoutUSER++;

        else
            log.warning("Illegal timeout type: "+type);
            
        
    }
        

    private void timeStamp() {
        lastUsed = new Date();
    }

    public int getSessionStats(String type) {

        if (type.equals("created"))
            return createdSessions;

        else if (type.equals("active"))
            return createdSessions-(sessionsTimeoutAUTH+sessionsTimeoutSSO+sessionsTimeoutUSER);

        else if (type.equals("deniedAuthorization"))
            return deniedSessionsAuthorization;
        
        else if (type.equals("deniedURL"))
            return deniedSessionsURL;

        else if (type.equals("timeoutAuth"))
            return sessionsTimeoutAUTH;

        else if (type.equals("timeoutSSO"))
            return sessionsTimeoutSSO;

        else if (type.equals("timeoutUser"))
            return sessionsTimeoutUSER;
        
        if (type.equals("authSuccess"))
            return loginAttemptSuccess;

        else if (type.equals("authFailed"))
            return loginAttemptFailed;

        else 
            return -1;
    }


 
}
