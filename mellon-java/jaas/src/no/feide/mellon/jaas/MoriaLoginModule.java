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

package no.feide.mellon.jaas;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import javax.xml.rpc.ServiceException;

import no.feide.mellon.MoriaConnector;
import no.feide.mellon.MoriaUserData;


/**
 * This JAAS module provides retrieval of userdata from Moria. Prior to the use
 * of this module the user has to be redirected, by HTTP, to Morias login
 * service. When the user is redirected back to the service the Moria ID has to
 * be retrieved from the request and supplied to this module. Three callbacks 
 * are required: username and password for the Moria service, and finally
 * the Moria ID that identifies an authenticated Moria user.
 * 
 * To run this test you have to supply the name of a moria connector. Use the
 * following parameter when starting the Java VM:
 * -Dno.feide.mellon.connector=no.feide.mellon.jaxrpc.MoriaJAXRPCConnector
 * -Dno.feide.mellon.connector=no.feide.mellon.axis.MoriaAxisConnector
 * 
 * @author Lars Preben S. Arnesen
 */
public class MoriaLoginModule implements LoginModule {
	private Subject subject;
	private MoriaPrincipal entity;
	private CallbackHandler callbackhandler;
	private static final int NOT = 0;
	private static final int OK = 1;
	private static final int COMMIT = 2;
	private int status; 
	
	/**
	 * @see javax.security.auth.spi.LoginModule#initialize(javax.security.auth.Subject, javax.security.auth.callback.CallbackHandler, java.util.Map, java.util.Map)
	 */
	public void initialize(Subject subject, CallbackHandler//
						   callbackhandler, Map state, Map options) {
		status = NOT;
		entity = null;
		this.subject = subject;
		this.callbackhandler = callbackhandler;
	}
	
	/**
	 * Tries to fetch user data from Moria. Three callbacks are created: 
	 * Username and password for Moria to authenticate the service using
	 * the MoriaLoginModule. The third callback is for the MoriaID, which has to
	 * be retrieved from the HTTP request after a successful Moria authentication.
	 * 
	 * Only the eduPersonPrincipalName is requested from Moria. More attributes
	 * are available and should probably be added here later.
	 * 
	 * @see javax.security.auth.spi.LoginModule#login()
	 */
	public boolean login() throws LoginException {
		
		if(callbackhandler == null) {
			throw new LoginException("No callback handler is available");
		}
		Callback callbacks[] = new Callback[3];
		callbacks[0] = new NameCallback("Moria Account Name: ");
		callbacks[1] = new PasswordCallback("Moria Password: ", false);
		callbacks[2] = new NameCallback("Moria ID: ");
		String name = null;
		String principalName = null;
		
		try {
			callbackhandler.handle(callbacks);
			String moriaUser = ((NameCallback)callbacks[0]).getName();
			String moriaPassword = new String(((PasswordCallback)callbacks[1]).getPassword());
			name = ((NameCallback)callbacks[2]).getName();
			MoriaUserData mud = new MoriaUserData(getConnector(moriaUser, moriaPassword).getAttributes(name));
			mud.debugPrintUserData();
			principalName = (String) mud.getSingleValueAttribute("eduPersonPrincipalName");			
		} 	
		catch (RemoteException e) {
			throw new LoginException(e.toString());
		}	
		catch (ServiceException e) {
			throw new LoginException(e.toString());
		}	
		catch(java.io.IOException ioe) {
			throw new LoginException(ioe.toString());
		} catch(UnsupportedCallbackException ce) {
			throw new LoginException("Error: "+ce.getCallback().toString());
		}
		catch (Exception e) {
			throw new LoginException(e.toString());
		}
		
		
		if(principalName != null) {
			entity = new MoriaPrincipal(principalName);
			status = OK;
			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * @see javax.security.auth.spi.LoginModule#commit()
	 */
	public boolean commit() throws LoginException {
		if(status == NOT) {
			return false;
		}
		if(subject == null) {
			return false;
		}
		Set entities = subject.getPrincipals();
		if(!entities.contains(entity)) {
			entities.add(entity);
		}
		status = COMMIT;
		return true;
	}
	
	/**
	 * @see javax.security.auth.spi.LoginModule#abort()
	 */
	public boolean abort() throws LoginException {
		if((subject != null) && (entity != null)) {
			Set entities = subject.getPrincipals();
			if(entities.contains(entity)) {
				entities.remove(entity);
			}
		}
		subject = null;
		entity = null;
		status = NOT;
		return true;
	}
	
	/**
	 * @see javax.security.auth.spi.LoginModule#logout()
	 */
	public boolean logout() throws LoginException {
		subject.getPrincipals().remove(entity);
		status = NOT;
		subject = null;
		return true;
	}
	
	/**
	 * Return an instance of the Moria stub. The stub is the local instance of
	 * the Moria service. Since Moria requires authentication of all services
	 * that uses Moria, a username/password has to be sent along with the
	 * requests. In this example the username/password is hard coded, but in
	 * "real" application it should be read from properties.
	 * 
	 * @return stub The stub for the Moria web service.
	 * @throws ServiceException
	 */
	private  MoriaConnector getConnector(String moriaUsername, String moriaPassword) throws Exception {
		MoriaConnector moria = null;
		
		String connectorClass = System.getProperty("no.feide.mellon.connector");
		if (connectorClass == null) 
			throw new Exception("No connector specified.");
				
		moria = (MoriaConnector)Class.forName(connectorClass).newInstance();		
		moria.connect("demo", "demo");
		return moria;
	}	
	
}