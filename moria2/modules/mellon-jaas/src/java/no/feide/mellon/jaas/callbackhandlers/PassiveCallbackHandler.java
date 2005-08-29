package no.feide.mellon.jaas.callbackhandlers;

import java.io.*;

import javax.security.auth.callback.*;

/**
 * @author Rikke Amilde Løvlid
 *
 */
public class PassiveCallbackHandler implements CallbackHandler {

    private String username;
    char[] password;

    /**
     * Creates a callback handler with the given username and password.
     */
    public PassiveCallbackHandler(String username, String password) {
        this.username = username;
        this.password = password.toCharArray();
    }

    /**
     * Handles the specified set of Callbacks. Uses the
     * username and password that were given to the constructor
     *
     * This class supports NameCallback and PasswordCallback.
     *
     * @param  	callbacks the callbacks to handle
     * @throws  IOException if an input or output error occurs.
     * @throws  UnsupportedCallbackException if the callback is not an
     * 			instance of NameCallback or PasswordCallback
     */
    public void handle(Callback[] callbacks) throws java.io.IOException, UnsupportedCallbackException
    {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof NameCallback) {
                ((NameCallback)callbacks[i]).setName(username);
            } 
            else if (callbacks[i] instanceof PasswordCallback) {
                ((PasswordCallback)callbacks[i]).setPassword(password);
            } 
            else {
                throw new UnsupportedCallbackException(
                            callbacks[i], "Callback class not supported");
            }
        }
    }

    /**
     * Clears out the password state.
     */
    public void clearPassword() {
        if (password != null) {
            for (int i = 0; i < password.length; i++)
                password[i] = ' ';
            password = null;
        }
    }

}

