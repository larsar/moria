package no.feide.moria.directory.backend;

import no.feide.moria.directory.Credentials;
import no.feide.moria.directory.UserAttributes;

/**
 * 
 */
public interface DirectoryManagerBackend {

    /**
     * Will attempt to authenticate a user and retrieve a set of user
     * attributes.
     * @param userCredentials
     *            The user's credentials.
     * @param attributeRequest
     *            A list of requested attributes from the user object. May be
     *            <code>null</code>, or an empty array.
     * @return The requested user attributes, if any are requested and if they
     *         can be retrieved from the backend following a successful
     *         authentication. Otherwise, an empty data structure.
     * @throws AuthenticationFailedException
     *             If the authentication fails. 
     */
    public UserAttributes authenticate(Credentials userCredentials, String[] attributeRequest)
    throws BackendException;

}