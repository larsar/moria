/*
 * Created on Dec 1, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package no.feide.mellon.jaxrpc;

import java.rmi.RemoteException;
import java.util.Vector;

import javax.xml.rpc.ServiceException;
import javax.xml.rpc.Stub;

import no.feide.moria.service.Attribute;
import no.feide.moria.service.AuthenticationIF;
import no.feide.moria.service.Authentication_Impl;


import no.feide.mellon.MoriaConnector;
import no.feide.mellon.MoriaUserAttribute;




/**
 * @author Lars Preben S. Arnesen
 *
 */
public class MoriaJAXRPCConnector extends MoriaConnector {

	Stub stub = null;
	
	/* (non-Javadoc)
	 * @see no.feide.mellon.MoriaConnectorIF#requestSession(java.lang.String[], java.lang.String, java.lang.String, boolean)
	 */
	public String requestSession(String[] attributes, String urlPrefix, String urlPostfix, boolean denySso) throws RemoteException {
		AuthenticationIF service = (AuthenticationIF)stub;

		return service.requestSession(attributes, urlPrefix, urlPostfix, denySso);
	}

	/* (non-Javadoc)
	 * @see no.feide.mellon.MoriaConnectorIF#getAttributes(java.lang.String)
	 */
	public MoriaUserAttribute[] getAttributes(String ticket) throws RemoteException {
		AuthenticationIF service = (AuthenticationIF)stub;	
		Attribute[] attributes = service.getAttributes(ticket);
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

	/* (non-Javadoc)
	 * @see no.feide.mellon.MoriaConnectorIF#connect(java.lang.String, java.lang.String)
	 */
	public void connect(String username, String password) throws ServiceException {
		stub = (Stub)(new Authentication_Impl().getAuthenticationIFPort());
		stub._setProperty(javax.xml.rpc.Stub.USERNAME_PROPERTY, username);
		stub._setProperty(javax.xml.rpc.Stub.PASSWORD_PROPERTY, password);
		AuthenticationIF service = (AuthenticationIF)stub;
	}
}
