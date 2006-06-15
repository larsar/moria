/*
 * Copyright (c) 2004 UNINETT FAS
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */

package no.feide.moria.directory.backend;

import java.util.TimerTask;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import no.feide.moria.log.MessageLogger;

/**
 * Utility class that will <code>close</code> a given
 * <code>InitialContext</code> after some time, to ensure proper client-side
 * timeout value during LDAP searches. This will be necessary for LDAP servers
 * that doesn't respect the LDAP search <code>timeLimit</code>, as defined in
 * RFC2251.
 */
final class JNDISearchInterruptor
extends TimerTask {

    /** The message logger. */
    private final MessageLogger log = new MessageLogger(JNDISearchInterruptor.class);

    /** The LDAP connection that will ble closed. */
    private InitialContext myLDAP = null;

    /**
     * Used for logging which LDAP connection was terminated.<br>
     * <br>
     * Default value is <code>"unknown backend"</code>, unless explicitly set
     * by <code>setURL(String)</code>.
     * @see #setURL(String)
     */
    private String myURL = "unknown backend";

    /**
     * Used for logging which session's search was terminated.
     */
    private final String mySessionTicket;

    /**
     * First used to log how many milliseconds passed between instantiation and
     * running (an approximate estimate of the timeout value), then used to
     * signal that the interrupt has taken place (by assigning <code>-1</code>).
     */
    public long created;


    /**
     * Constructor. Will note the current system time, for later logging.
     * @param ldap
     *            The LDAP connection, or context, that will later ble closed.
     * @param sessionTicket
     *            The session ticket, for later logging.
     */
    public JNDISearchInterruptor(InitialContext ldap, final String sessionTicket) {

        myLDAP = ldap;
        mySessionTicket = sessionTicket;
        created = System.currentTimeMillis();

    }


    /**
     * Will interrupt the search by closing the LDAP connection, or context.
     */
    public synchronized void run() {

        log.logDebug("Interrupting search on " + myURL + " after ~" + (System.currentTimeMillis() - created) + "ms", mySessionTicket);
        if (myLDAP != null)
            try {
                myLDAP.close();
            } catch (NamingException e) {
                log.logWarn("Unable to close the backend connection to " + myURL + " - ignoring", mySessionTicket, e);
            }
        created = -1;

    }


    /**
     * Used to set the LDAP connection URL, for logging.
     * @param url
     *            The LDAP connection URL.
     */
    public void setURL(final String url) {

        myURL = url;

    }


    /**
     * Used to check whether the <code>run()</code> method has closed the LDAP
     * connection, or context.
     * @return <code>true</code> if it has, otherwise <code>false</code>.
     */
    public synchronized boolean finished() {

        return (created == -1);

    }

}
