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
	
	/** The test user name. */
	private static final String username = "test@uninett.no";
	
	/** The test group name. */
	private static final String groupname = "FOOBAR";
	

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
			System.err.println("Service authentication: demo/demo");
	
			// Check whether the user exists at all.
			AuthenticationIF service = (AuthenticationIF)stub;
			System.err.println("User "+username+" exists: "+service.verifyUserExistence(username));
			System.err.println("Group "+groupname+" exists: "+service.verifyGroupExistence(groupname));
			System.err.println("User "+username+" in group "+groupname+": "+service.verifyUserInGroup(username, groupname));
			
			// Request a new session.
			String[] attributeRequest = {"eduPersonAffiliation", "eduPersonOrgDN"};
			System.err.println("Attributes requested: "+attributeRequest);
			/*
	        String sessionID = service.initiateMoriaAuthentication(attributeRequest, "http://www.feide.no?id=", "", false);
			sessionID = sessionID.substring(sessionID.indexOf("id=")+3);
			*/
	        
			// Direct non-interactive authentication.
			System.err.println("User authentication: "+username);
			String sessionID = service.directNonInteractiveAuthentication(attributeRequest, username, "test");
			System.err.println("Authenticated session: "+sessionID);
	
			// Get and display user attributes.
			System.err.println("Requesting attributes");
	        Attribute[] attributes = service.getUserAttributes(sessionID);
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
