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

package no.feide.mellon.jaas.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * This class is a callback handler for interactive use of MoriaLoginModule. 
 * The callback reads information from command line. In a real life application
 * the Moria authentication attributes should be read from properties and the
 * MoriaID should be extracted from the request object in a web application
 * @author Lars Preben S. Arnesen
 */
public class MoriaTestCallbackHandler implements CallbackHandler {
	

	/**
	 * @see javax.security.auth.callback.CallbackHandler#handle(javax.security.auth.callback.Callback[])
	 */
	public void handle(Callback callbacks[]) throws IOException, UnsupportedCallbackException {
		for(int i=0;i<callbacks.length;i++) {
			if(callbacks[i] instanceof NameCallback || callbacks[i] instanceof PasswordCallback) {
				getInput(callbacks[i]);
			} else {
				throw(new UnsupportedCallbackException(callbacks[i], "Callback handler not support"));
			}
		}
	}

	/**
	 * Get input from standard in for NameCallback and PasswordCallback.
	 * @param callback
	 * @throws IOException
	 */
	private void getInput(Callback callback) throws IOException {
		if (callback instanceof NameCallback) {
			NameCallback nc = (NameCallback) callback;
			System.err.print(nc.getPrompt());
			System.err.flush();
			String name = (new BufferedReader(new InputStreamReader(System.in))).readLine();
			nc.setName(name);
		}
		
		else if (callback instanceof PasswordCallback) {
			PasswordCallback pc = (PasswordCallback) callback;
			System.err.print(pc.getPrompt());
			System.err.flush();
			String password = (new BufferedReader(new InputStreamReader(System.in))).readLine();
			pc.setPassword(password.toCharArray());
						
		}
	}
	
	
}
