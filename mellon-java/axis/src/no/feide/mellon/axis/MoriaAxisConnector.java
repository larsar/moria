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
package no.feide.mellon.axis;

import no.feide.mellon.MoriaConnector;
import no.feide.mellon.MoriaUserAttribute;

import java.rmi.RemoteException;
import java.util.Vector;

import javax.xml.rpc.ServiceException;

import no.feide.login.moria.v1_0.Authentication.Attribute;
import no.feide.login.moria.v1_0.Authentication.AuthenticationIFBindingStub;
import no.feide.login.moria.v1_0.Authentication.AuthenticationLocator;


/**
 * @author Lars Preben S. Arnesen
 *
 */
public class MoriaAxisConnector extends MoriaConnector {

	AuthenticationIFBindingStub stub = null;

	
	/** 
	 * @see no.feide.mellon.jaxrpc.MoriaConnector#connect(java.lang.String, java.lang.String)
	 */
	public void connect(String username, String password) throws ServiceException {
		
		AuthenticationLocator authnLocator = new AuthenticationLocator();
		stub = (AuthenticationIFBindingStub) authnLocator.getAuthenticationIFPort();

		/* Set the username password, should be read from properties. */
		stub._setProperty(javax.xml.rpc.Stub.USERNAME_PROPERTY, username);
		stub._setProperty(javax.xml.rpc.Stub.PASSWORD_PROPERTY, password);
	}

	/** 
	 * @see no.feide.mellon.jaxrpc.MoriaConnector#requestSession(java.lang.String[], java.lang.String, java.lang.String, boolean)
	 */
	public String requestSession(String[] attributes, String urlPrefix, String urlPostfix, boolean denySso) throws RemoteException {
		return stub.initiateAuthentication(attributes, urlPrefix, urlPostfix, denySso);
	}

	/** 
	 * @see no.feide.mellon.jaxrpc.MoriaConnector#getAttributes(java.lang.String)
	 */
	public MoriaUserAttribute[] getAttributes(String ticket) throws RemoteException {
		Attribute[] attributes = stub.getUserAttributes(ticket);
		Vector moriaUserAttributes = new Vector();
		
		for (int i = 0; i < attributes.length; i++) {
			MoriaUserAttribute ma = new MoriaUserAttribute();
			Attribute a = attributes[i];
			ma.setName(a.getName());
			ma.setValues(a.getValues());
			moriaUserAttributes.add(ma);
		}
		
		return (MoriaUserAttribute[]) moriaUserAttributes.toArray(new MoriaUserAttribute[moriaUserAttributes.size()]);
	}


	
	
}
