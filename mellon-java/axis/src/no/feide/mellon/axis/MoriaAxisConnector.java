/*
 * Created on Nov 19, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package no.feide.mellon.axis;

import no.feide.mellon.MoriaConnector;
import no.feide.mellon.MoriaUserAttribute;

import java.rmi.RemoteException;
import java.util.Vector;

import javax.xml.rpc.ServiceException;

import no.feide.login.moria.Authentication.Attribute;
import no.feide.login.moria.Authentication.AuthenticationIFBindingStub;
import no.feide.login.moria.Authentication.AuthenticationLocator;


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
		return stub.requestSession(attributes, urlPrefix, urlPostfix, denySso);
	}

	/** 
	 * @see no.feide.mellon.jaxrpc.MoriaConnector#getAttributes(java.lang.String)
	 */
	public MoriaUserAttribute[] getAttributes(String ticket) throws RemoteException {
		Attribute[] attributes = stub.getAttributes(ticket);
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
