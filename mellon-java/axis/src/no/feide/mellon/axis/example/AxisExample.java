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
package no.feide.mellon.axis.example;

import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;

import no.feide.login.moria.Authentication.Attribute;
import no.feide.login.moria.Authentication.AuthenticationIFBindingStub;
import no.feide.login.moria.Authentication.AuthenticationLocator;
import no.feide.mellon.MoriaUserData;


/**
 * @author Lars Preben S. Arnesen
 * 
 * This class is an example on how to use Moria authentication. The example is
 * written to run from the command line only to make it as simple as possible
 * to test Moria- functionality without having to deploy a web service. Some of
 * this code can be reused in your own applications.
 * 
 * Call the main method from the command line. Moria will be contacted and an
 * authentication session will be created. The URL to the Moria login page is
 * returned and displayed. Paste this URL into a web browser and complete the
 * authentication, using your FEIDE-account. If you don't have a FEIDE-account
 * you can use "test@uninett.no" as username and "test" as password.
 * 
 * After a successful authetnication the browser will be redirected to a dummy
 * URL. Since this is not a web service we cannot use the regular Moria
 * mechanism, but instead cut the session ID from the URL (after the
 * authentication) and call this program with the session ID as parameter. This
 * must be done in a few seconds after the autnentication since Moria will
 * remove the session after about 30 seconds.
 * 
 * When running this program after the authentication, all user data will be
 * displayed on the console.
 */
public class AxisExample {

	/**
	 * The main method that is called when running from command line. When no
	 * parameters are given from the command line a new Moria session will be
	 * created and the session ID will be printed to standard out. When a
	 * session ID is supplied from the command line, Moria will be contacted
	 * and the user data is retrieved. Any problems will result in an exception
	 * beeing thrown all the way out to the console.
	 * 
	 * @param args SessionID from the command line.
	 * @throws RemoteException
	 * @throws ServiceException
	 */
	public static void main(String[] args) throws RemoteException, ServiceException {

		/* No arguments = no session ID = create a new Moria session */
		if (args.length == 0) {
			String sessionID = getSessionID();

			System.out.println("You now have successfully created a Moria session. To complete this test,");
			System.out.println("paste the URL below into a browser and complete the authentication.");
			System.out.println("After a successful authentication the browser will be redirected to");
			System.out.println("a location that does not exist. Cut the sessionID from the browser");
			System.out.println("and run the test program with the sessionID as parameter.");
			System.out.println("");
			System.out.println("Go to: " + sessionID);
		}

		/* A session ID is supplied, fetch the user data from Moria. */
		else if (args.length == 1) {
			MoriaUserData userData = getUserData(args[0]);
			
			/* Print some test data */
			userData.debugPrintUserData();
		}
	}

	/**
	 * Request an authentication session from Moria and returns the session ID.
	 * The parameters to Moria are hard coded. In a "real" application this
	 * probably should be read from properties.
	 * 
	 * @return SessionID The sessionID supplied from Moria.
	 * @throws ServiceException
	 * @throws RemoteException
	 */
	private static String getSessionID() throws ServiceException, RemoteException {
		AuthenticationIFBindingStub stub = getStub();
		return stub.requestSession(new String[] { "eduPersonAffiliation", "eduPersonOrgDN" },
			"http://localhost?sessionID=", "",false);
	}

	
	/**
	 * Retrieves the user data from an authenticated Moria session. The user
	 * data is converted from an array of attribute objects to a more
	 * convenient user data object. After retrieving the data everything is
	 * written to standard out.
	 * 
	 * @param sessionID
	 * @return moriaUserData
	 * @throws RemoteException
	 * @throws ServiceException
	 */
	private static MoriaUserData getUserData(String sessionID)
		throws RemoteException, ServiceException {
		Attribute[] attributes;
		attributes = getStub().getAttributes(sessionID);
		return new MoriaUserData(attributes);
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
	private static AuthenticationIFBindingStub getStub() throws ServiceException {
		AuthenticationIFBindingStub stub;
		AuthenticationLocator authnLocator = new AuthenticationLocator();
		stub = (AuthenticationIFBindingStub) authnLocator.getAuthenticationIFPort();

		/* Set the username password, should be read from properties. */
		stub._setProperty(javax.xml.rpc.Stub.USERNAME_PROPERTY, "demo");
		stub._setProperty(javax.xml.rpc.Stub.PASSWORD_PROPERTY, "demo");

		return stub;
	}
}
