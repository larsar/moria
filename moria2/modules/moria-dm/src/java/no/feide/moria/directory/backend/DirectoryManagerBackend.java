package no.feide.moria.directory.backend;

import java.util.HashMap;

import no.feide.moria.directory.Credentials;

/**
 * 
 */
public interface DirectoryManagerBackend {

    /**
     * Opens a new backend connection.
     * @param reference
     *            The backend reference in question. Cannot be <code>null</code>.
     * @throws BackendException
     *             If the backend connection could not be made.
     */
    public void open(String reference) throws BackendException;


    /**
     * Will attempt to authenticate a user and retrieve a set of user
     * attributes.
     * @param userCredentials
     *            The user's credentials. Cannot be <code>null</code>.
     * @param attributeRequest
     *            A list of requested attributes from the user object. May be
     *            <code>null</code>, or an empty array. Not case-sensitive.
     * @return The requested user attributes, if any are requested and if they
     *         can be retrieved from the backend following a successful
     *         authentication. Otherwise, an empty <code>HashMap</code>.
     * @throws AuthenticationFailedException
     *             If the authentication fails.
     */
    public HashMap authenticate(Credentials userCredentials, String[] attributeRequest)
    throws AuthenticationFailedException;


    /**
     * Will close the current backend and release any resources.
     */
    public void close();

}