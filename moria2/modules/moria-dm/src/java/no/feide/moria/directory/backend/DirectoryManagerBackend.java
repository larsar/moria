package no.feide.moria.directory.backend;

import java.util.HashMap;

import no.feide.moria.directory.Credentials;
import no.feide.moria.directory.index.IndexedReference;

/**
 * 
 */
public interface DirectoryManagerBackend {

    /**
     * Opens a new backend connection.
     * @param reference
     *            The backend reference in question. Cannot be <code>null</code>.
     */
    public void open(IndexedReference reference);


    /**
     * Checks whether a given user actually exists.
     * @param username
     *            The username to check for.
     * @return <code>true</code> if we can find a user element with the given
     *         username, otherwise <code>false</code>.
     * @throws UnknownException
     *             If an exception occurs that we do not know how to explicitly
     *             handle.
     */
    public boolean userExists(String username) throws UnknownException;


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
     * @throws UnknownException
     *             If an exception occurs that we do not know how to explicitly
     *             handle. 
     */
    public HashMap authenticate(Credentials userCredentials, String[] attributeRequest)
    throws AuthenticationFailedException, UnknownException;


    /**
     * Will close the current backend and release any resources.
     */
    public void close();

}