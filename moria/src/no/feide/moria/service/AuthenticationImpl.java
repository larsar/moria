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

package no.feide.moria.service;

import java.rmi.RemoteException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.Vector;
import java.util.logging.Logger;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;
import javax.xml.rpc.server.ServletEndpointContext;

import no.feide.moria.BackendException;
import no.feide.moria.Configuration;
import no.feide.moria.ConfigurationException;
import no.feide.moria.Credentials;
import no.feide.moria.Session;
import no.feide.moria.SessionException;
import no.feide.moria.SessionStore;
import no.feide.moria.authorization.AuthorizationData;
import no.feide.moria.authorization.AuthorizationTask;
import no.feide.moria.authorization.WebService;
import no.feide.moria.stats.StatsStore;
import no.feide.moria.utils.URLValidator;

public class AuthenticationImpl
implements AuthenticationIF, ServiceLifecycle {

    /** Used for logging. */
    private static Logger log = Logger.getLogger(AuthenticationImpl.class.toString());

    /** Used to retrieve the client identity. */
    private ServletEndpointContext ctx;
    
    /** Session store. */
    private SessionStore sessionStore;

    /** Statistics module */
    private StatsStore stats = StatsStore.getInstance();
      
    /** Timer for updating the web service authorization module. */
    private Timer authTimer = new Timer(true);

    
    /**
     * Service endpoint destructor. Some basic housekeeping.
     */
    public void destroy() {
	log.finer("destroy()");

        authTimer.cancel();
	log = null;
	ctx = null;
    }


    /**
     * Service endpoint initialization. Will read the <code>Properties</code>
     * file found in the location given by the system property
     * <code>no.feide.moria.config.file</code>. If the property is not
     * set, the default filename is <code>/moria.properties</code>.
     * @param context The servlet context, used to find the user (client service)
     *                identity in later methods.
     * @throws ServiceException If a FileNotFoundException or IOException id
     *                          caught when reading the properties file. Also
     *                          thrown if the system property
     *                          <code>no.feide.moria.AuthorizationTimerDelay</code>
     *                          is not set, or if there is a problem getting
     *                          the session store instance.
     */
    public void init(Object context) 
    throws ServiceException {
	log.finer("init(Object)");

	ctx = (ServletEndpointContext)context;

        try {
            
            sessionStore = SessionStore.getInstance();
            
            int authDelay = new Integer(Configuration.getProperty("no.feide.moria.AuthorizationTimerDelay")).intValue()*1000; // Seconds to milliseconds
            log.config("Starting authorization update service with delay= "+authDelay+"ms");
            authTimer.scheduleAtFixedRate(new AuthorizationTask(), new Date(), authDelay);
            
            /* Sleep a short while. If not the authorization data will not
               be updated in time to authorize the first authentication request. */
            try { 
                int initialSleep = new Integer(Configuration.getProperty("no.feide.moria.AuthorizationTimerInitThreadSleep")).intValue(); 
                log.config("Sleep "+initialSleep+" seconds while reading auth. config.");
                Thread.sleep(initialSleep*1000); 
            } 
            catch (InterruptedException e) { 
                /* We didn't get any sleep. Don't care. If this is the
                 * case, the first web service authorization request will
                 * end in an exception. After that everything will be all
                 * right.
                 */
            }
            

        } catch (ConfigurationException e) {
            log.severe("ConfigurationException caught and re-thrown as ServiceException");
            throw new ServiceException("ConfigurationException caught", e);
        } catch (SessionException e) {
            log.severe("SessionException caught and re-thrown as ServiceException");
            throw new ServiceException("SessionException caught", e);
        }

    }




    /**
     * Request a new Moria session, with the option to turn off SSO even though
     * the web service authorization config would allow it.
     * @param attributes The requested user attributes, to be returned from
     *                   <code>verifySession()</code> once authentication is
     *                   complete. <code>null</code> value allowed.
     * @param prefix The prefix, used to build the <code>verifySession</code>
     *               return value. May be <code>null</code>.
     * @param postfix The postfix, used to build the
     *                <code>verifySession</code> return value. May be
     *                <code>null</code>.
     * @param denySSO If <code>true</code> SSO is disabled even though the
     *                web service config may allow SSO. If <code>false</code>,
     *                the config is used to determine if the web service can
     *                use SSO.
     * @return An URL to the authentication service.
     * @throws RemoteException If a SessionException or a
     *                         BackendException is caught. Also thrown if the
     *                         prefix/postfix doesn't combine into a valid
     *                         URL, or if a <code>ConfigurationException<code>
     *                         is caught.
     */
    public String requestSession(String[] attributes, String prefix, String postfix, boolean denySso)
    throws RemoteException {
        log.finer("requestSession(String[], String, String, boolean)");
        
        /* If no attributes are given, then create an empty attribute
         * array. */
        if (attributes == null) {
            attributes = new String[]{};
        }

        /* Look up service authorization data. */
        Principal p = ctx.getUserPrincipal();
        String serviceName = null;
        if (p != null)
            serviceName = p.getName();
        
        String log_prefix = "Session requested by "+serviceName+": ";


        /* Check if prefix and postfix, together with a possible
         * session ID, is a valid URL. */
        String simulatedURL = prefix+"MORIAID"+postfix;

        if (!URLValidator.isLegal(simulatedURL)) {
            log.warning(log_prefix+"DENIED, Invalid URL");
            stats.incStatsCounter(serviceName, "sessionDeniedURL");
            throw new RemoteException("Malformed URL: "+simulatedURL);
        }
            
        //Prepare attribute list and write to log.
        String s = new String();
        for (int i=0; i<attributes.length-1; i++)
            s = s + attributes[i] + ',';
        s = s + attributes[attributes.length-1];
        log.info("Service name: "+serviceName+"; attributes: "+s+"; deny SSO "+denySso+"; URL "+simulatedURL);

        WebService ws = AuthorizationData.getInstance().getWebService(serviceName);
        if (ws == null) {
            log.warning(log_prefix+"DENIED, Unauthorized");
            stats.incStatsCounter(serviceName, "sessionDeniedAuthN");
            throw new RemoteException("Web Service not authorized for use with Moria");
        } else if (!ws.allowAccessToAttributes(attributes)) {
            log.warning(log_prefix+"DENIED, Authorization faliure");
            stats.incStatsCounter(serviceName, "sessionDeniedAuthZ");
            throw new RemoteException("Access to one or more attributes prohibited");
        }

        try {
            Session session = sessionStore.createSession(attributes, prefix, postfix, p, ws);      
            log.info(log_prefix+"ACCEPTED, SID="+session.getID());

            /* Turn of SSO if required by web service. */
            if (denySso) 
                session.setDenySso();

            stats.incStatsCounter(serviceName, "createdSessions");
            stats.incStatsCounter(serviceName, "activeSessions");

            return session.getRedirectURL();
        } catch (SessionException e) {
            log.severe("SessionException caught and re-thrown as RemoteException");
            throw new RemoteException("SessionException caught", e);
        } catch (ConfigurationException e) {
            log.severe("ConfigurationException caught and re-thrown as RemoteException");
            throw new RemoteException("ConfigurationException caught", e);
        }
        
    }
    
    
    /**
     * 
     */
    public boolean authenticateUser(String id, String username, String password)
    throws RemoteException {
    	log.finer("authenticateUser(String, String, String)");
    	
    	// Basic parameter check.
    	if (id == null)
    		throw new RemoteException("Session ID cannot be NULL");
    	if (username == null)
    		throw new RemoteException("User name cannot be NULL");
    	if (password == null)
    		throw new RemoteException("Password cannot be NULL");
    	
    	try {

    		// Look up session and check the client identity.
    		Session session = sessionStore.getSessionAuthenticated(id);
    		validateClient(ctx.getUserPrincipal(), session);

    		// Is the client allowed to do direct authentication at all?
    		if (!session.getWebService().allowDirectAuthentication()) {
    			log.warning("Web service '"+ctx.getUserPrincipal()+"' not allowed to use direct authentication");
    			throw new RemoteException("Web service '"+ctx.getUserPrincipal()+"' not allowed to use direct authentication");
    		}
    		
    		// Create credentials and do authentication.
    		return session.authenticateUser(new Credentials(username, password));

    	} catch (BackendException e) {
    		log.severe("BackendException caught and re-thrown as RemoteException");
    		throw new RemoteException("BackendException caught and re-thrown as RemoteException");
    	} catch (SessionException e) {
    		log.severe("SessionException caught and re-thrown as RemoteException");
    		throw new RemoteException("SessionException caught and re-thrown as RemoteException");
    	}
	}


    /* Return the previously requested user attributes.
     * @param The session ID.
     * @return The previously requested user attributes.
     * @throws RemoteException If a SessionException or a
     *                         BackendException is caught. Also thrown if
     *                         the current client's identity (as found in
     *                         the context) is different from the identity
     *                         of the client service originally requesting
     *                         the session.
     */
    public Attribute[] getAttributes(String id)
    throws RemoteException {
        log.finer("getAttributes(String)");

        // Basic parameter check.
        if (id == null) 
            throw new RemoteException("Session ID cannot be NULL");
        
    	try {

            // Look up session and check the client identity.
            Session session = sessionStore.getSessionAuthenticated(id);
            validateClient(ctx.getUserPrincipal(), session);

            // Parse and return attributes.
            HashMap result = session.getAttributes();
            Iterator keys = result.keySet().iterator();
            ArrayList attributes = new ArrayList(result.size());
            while (keys.hasNext()) {
                String attrName = (String)keys.next();
                Vector oldValues = (Vector)result.get(attrName);
                Attribute newAttr = new Attribute();
                newAttr.setName(attrName);
                newAttr.setValues((String[])oldValues.toArray(new String[] {}));                
                attributes.add(newAttr);
            }
            return (Attribute[])attributes.toArray(new Attribute[] {});

        } catch (SessionException e) {
            log.severe("SessionException caught, and re-thrown as RemoteException");
            throw new RemoteException("SessionException caught", e);
        }
    }


    /**
     * Utility method, used to check if a given client service principal
     * matches the principal stored in the session; also if the session in
     * question is locked or not.
     * @param pCurrent The current client principal.
     * @param session The current session.
     * @throws SessionException If there's a problem getting the session from
     *                          the session store.
     * @throws RemoteException If the client principals didn't match, or if the
     *                         client is trying to use a locked session.
     */
    private static void validateClient(Principal pCurrent, Session session)
    throws SessionException, RemoteException {
	log.finer("assertPrincipals(Principal, String)");
	
		// Get current service name.
		String serviceName = null;
		if (pCurrent != null)
			serviceName = pCurrent.getName();
		if (serviceName == null) {
			log.severe("Empty service name");
			throw new RemoteException("Empty service name");
		}
	
		/* Check if the current web service is the one that originally created this
		 * session. */
		if (!session.getWebService().getId().equals(serviceName)) {
			log.warning("Web service '"+serviceName+"' failed authorization");
			throw new RemoteException("Web service '"+serviceName+"' failed authorization");
		}
	
		// Check if the current session is locked.
		if (session.isLocked()) {
			log.warning("Web service '"+serviceName+"' tries to use a locked session ("+session.getID()+')');
			throw new RemoteException("Web service '"+serviceName+"' tries to use a locked session ("+session.getID()+')');
		}
		
		// Seems the web service is validated after all.
	    log.info("Web service '"+serviceName+"' passed validation; session ID is "+session.getID());
    
    }
    
}
