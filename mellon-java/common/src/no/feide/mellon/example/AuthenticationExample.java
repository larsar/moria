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
package no.feide.mellon.example;

import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;

import no.feide.mellon.MoriaConnector;
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


public class AuthenticationExample {

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

		MoriaConnector moria = null;

		String connectorClass = System.getProperty("no.feide.mellon.connector");
		if (connectorClass == null) {
			System.err.println("No connector specified.");
			return;
		}
		
		try {
			moria = (MoriaConnector)Class.forName(connectorClass).newInstance();
		} 
		catch (InstantiationException e1) {
			System.err.println("Error instantiating connector.");
			e1.printStackTrace();
			return;
		} 
		catch (IllegalAccessException e1) {
			System.err.println("Error instantiating connector.");
			e1.printStackTrace();
			return;
		} 
		catch (ClassNotFoundException e1) {
			System.err.println("Error instantiating connector.");
			e1.printStackTrace();
			return;
		}
		
		moria.connect("demo", "demo");

		
		/* No arguments = no session ID = create a new Moria session */
		if (args.length == 0) {
			String loginURL = moria.requestSession(new String[] { "eduPersonAffiliation", "eduPersonOrgDN" },
			                                        "http://localhost?sessionID=", "",false);

			System.out.println("You now have successfully created a Moria session. To complete this test,");
			System.out.println("paste the URL below into a browser and complete the authentication.");
			System.out.println("After a successful authentication the browser will be redirected to");
			System.out.println("a location that does not exist. Cut the sessionID from the browser");
			System.out.println("and run the test program with the sessionID as parameter.");
			System.out.println("");
			System.out.println("Go to: " + loginURL);
		}

		/* A session ID is supplied, fetch the user data from Moria. */
		else if (args.length == 1) {
			MoriaUserData userData = new MoriaUserData(moria.getAttributes(args[0]));
			
			/* Print some test data */
			userData.debugPrintUserData();
		}
	}
}
