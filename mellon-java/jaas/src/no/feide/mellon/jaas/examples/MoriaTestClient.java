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

package no.feide.mellon.jaas.examples;

import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;


/**
 * @author Lars Preben S. Arnesen
 * Command line test client for MoriaLoginModule. A moriaID is required to be
 * supplied from another source, thus this example does NOT cover the entire
 * Moria authentication loop. 
 */
public class MoriaTestClient {
	
	
	/**
	 * Attempts to retrive principal information from Moria via MoriaLoginModule.
	 * Prints a status message to standard out (success/failed).
	 * @param argv
	 */
	public static void main(String argv[]) {
		LoginContext ctx = null;
		try {
			ctx = new LoginContext("MoriaLogin", new MoriaTestCallbackHandler());
		} catch(LoginException le) {
			System.err.println("LoginContext cannot be created. "+ le.getMessage());
			System.exit(-1);
		} catch(SecurityException se) {
			System.err.println("LoginContext cannot be created. "+ se.getMessage());
		}
		try {
			ctx.login();
		} catch(LoginException le) {
			System.out.println("Authentication failed. " + le.getMessage());
			System.exit(-1);
		}
		System.out.println("Authentication succeeded.");
		System.exit(0);
	}
}
