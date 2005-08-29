package no.feide.mellon.jaas.callbackhandlers;


import java.io.*;

import javax.security.auth.callback.*;

/**
 * ConsoleCallbackHandler prompts and reads from the 
 * command line for username and password.
 *
 * @author Rikke Amilde Løvlid
 * 
 */

public class CommandlineCallbackHandler implements CallbackHandler {
 
    public CommandlineCallbackHandler() {
    }

    /**
     * Handles the specified set of callbacks.
     * This class supports NameCallback and PasswordCallback.
     *
     * @param   callbacks the callbacks to handle
     * @throws  IOException if an input or output error occurs.
     * @throws  UnsupportedCallbackException if the callback is not an
     * 			instance of NameCallback or PasswordCallback
     */
    public void handle(Callback[] callbacks) throws java.io.IOException, UnsupportedCallbackException {

        for (int i = 0; i < callbacks.length; i++) {

            if (callbacks[i] instanceof NameCallback) {
            	NameCallback nc = (NameCallback)callbacks[i];
            	
                System.out.print("\t\t" +nc.getPrompt());
                String name=(new BufferedReader(new InputStreamReader(System.in))).readLine();
                nc.setName(name);
            } 
            else if (callbacks[i] instanceof PasswordCallback) {
            	PasswordCallback pc = (PasswordCallback)callbacks[i];
            	
                System.out.print("\t\t" + pc.getPrompt());
                String password=(new BufferedReader(new InputStreamReader(System.in))).readLine();
                pc.setPassword(password.toCharArray());
            }   
            else {
                throw(new UnsupportedCallbackException(callbacks[i], "Callback class not supported"));
            }
        }
    }
}