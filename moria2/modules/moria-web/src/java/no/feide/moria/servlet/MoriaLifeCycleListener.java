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
 * $Id$
 */

package no.feide.moria.servlet;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import no.feide.moria.controller.InoperableStateException;
import no.feide.moria.controller.MoriaController;
import no.feide.moria.log.MessageLogger;

/**
 *
 * @author Bj&oslash;rn Ola Smievoll &lt;b.o.smievoll@conduct.no&gt;
 * @version $Revision$
 */
public final class MoriaLifeCycleListener implements ServletContextListener {

    /** Logger used by this class. */
    private MessageLogger messageLogger = null;

    /**
     * Is called when the servlet context is ready to process requests.
     * @param event The notification event.
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public synchronized void contextInitialized(final ServletContextEvent event) {
        if (messageLogger == null)
            messageLogger = new MessageLogger(MoriaLifeCycleListener.class);

        messageLogger.logWarn("Starting initialization of Moria.");

        try {
            MoriaController.initController(event.getServletContext());
            messageLogger.logWarn("New servlet context created. Controller initialized.");
        } catch (InoperableStateException ise) {
            final String message = "New servlet context. Unable to start controller.";
            messageLogger.logCritical(message);
            throw new RuntimeException(message, ise);
        }
    }

    /**
     * Is called when the servlet context is about to be shut down.
     * @param event The notification event.
     * @see javax.servlet.ServletContextListener#contextDestroyed(javax.servlet.ServletContextEvent)
     */
    public synchronized void contextDestroyed(final ServletContextEvent event) {
        if (messageLogger == null)
            messageLogger = new MessageLogger(MoriaLifeCycleListener.class);

        MoriaController.stopController();
        messageLogger.logWarn("Servlet context destroyed. Controller stopped.");
    }
}
