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

package no.feide.mellon.jaxrpc.test;

import javax.xml.rpc.Stub;
import no.feide.moria.service.Attribute;
import no.feide.moria.service.AuthenticationIF;
import no.feide.moria.service.Authentication_Impl;


/**
 * Moria client test application. 
 */
public class MellonTest {

	/** A valid URL prefix. */
	private static final String urlPrefix = "http://www.feide.no?id=";

	/** A valid URL postfix. */
	private static final String urlPostfix = "";
	
	/** The test user name. */
	private static final String username = "test@uninett.no";
	

	/**
	 * The main (and only) method.
	 * @param args Ignored.
	 */
	public static void main(String[] args) {
		
		System.err.println("Starting");
		try {
	
			// Create service proxy and set credentials.
			Stub stub = (Stub)(new Authentication_Impl().getAuthenticationIFPort());
			stub._setProperty(javax.xml.rpc.Stub.USERNAME_PROPERTY, "demo");
			stub._setProperty(javax.xml.rpc.Stub.PASSWORD_PROPERTY, "demo");
			System.err.println("Client authentication: demo/demo");
	
			// Check whether the user exists at all.
			AuthenticationIF service = (AuthenticationIF)stub;
			System.err.println("User "+username+" exists: "+service.userExists(username));
			
			// Request a new session.
			String[] attributeRequest = {"eduPersonAffiliation", "eduPersonOrgDN"};
			System.err.println("Session request:  {eduPersonAffiliation, eduPersonOrgDN}");
	        String sessionID = service.requestSession(attributeRequest, urlPrefix, urlPostfix, false);
			sessionID = sessionID.substring(sessionID.indexOf("id=")+3);
			System.err.println("Session established: "+sessionID);
	        
			// Authenticate.
			System.err.println("User authentication: "+username);
			sessionID = service.authenticateUser(sessionID, username, "test");
			System.err.println("User authenticated: "+sessionID);
	
			// Get and display user attributes.
			System.err.println("Requesting attributes");
	        Attribute[] attributes = service.getAttributes(sessionID);
	        for (int i=0; i<attributes.length; i++) {
	            String[] values = attributes[i].getValues();
	            for (int j=0; j<values.length; j++)
					System.err.println('\t'+attributes[i].getName()+": "+values[j]);
	        }
	
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.err.println("Finished");


	}
	
}
