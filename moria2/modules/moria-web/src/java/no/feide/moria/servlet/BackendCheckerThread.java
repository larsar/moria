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

import java.util.ResourceBundle;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * 
 * This is a helper class for the StatusServlet.
 *
 */
public class BackendCheckerThread extends Thread {
    StatusServlet servlet;
    ResourceBundle bundle;
    Map msgmap;
    String key;
    CountDownLatch donesignal;
    
    /**
     * Constructor
     * 
     * @param key_in
     * @param servlet_in
     * @param bundle_in
     * @param msgmap_in
     * @param donesignal_in
     */
    BackendCheckerThread(String key_in, StatusServlet servlet_in, 
                         ResourceBundle bundle_in, Map msgmap_in, CountDownLatch donesignal_in) {
        this.key = key_in;
        this.servlet = servlet_in;
        this.bundle = bundle_in;
        this.msgmap = msgmap_in;
        this.donesignal = donesignal_in;
    }
    
    /**
     * 
     */
    public void run() {
        this.servlet.doSingleBackendCheck(key, msgmap, bundle);
        this.donesignal.countDown();
    }

}
