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

package no.feide.moria;

import java.util.TimerTask;
import java.util.logging.Logger;


/**
 * Represents a periodic task to manipulate the session store.
 */
public class SessionStoreTask
extends TimerTask {

    /** Session time out value. */
    private int timeoutSec;

    /** SSO Session time out value. */
    private int ssoTimeoutMin;

    /** Authenticated session time out value. */
    private int authTimeoutSec;

    /** Used for logging. */
    private static Logger log = Logger.getLogger(SessionStoreTask.class.toString());
    
    /** Local pointer to session store. */
    private SessionStore sessionStore;
    
    
    /**
     * Constructor. Sets the local pointer to the session store.
     * @throws SessionException If there's a problem getting the session store
     *                          pointer, or if any of the required
     *                          configuration settings are undefined.
     */
    public SessionStoreTask() throws SessionException {
        log.info("SessionStoreTask()");
        
        sessionStore = SessionStore.getInstance();
    }
     
    
    /**
     * Called periodically by the timer.
     **/
    public void run() {
        log.fine("run()");
        sessionStore.checkTimeout();
    }    
    
}
