package no.feide.moria.directory.backend;

import no.feide.moria.directory.Credentials;
import no.feide.moria.directory.IllegalAttributeException;
import no.feide.moria.directory.UserAttribute;

/**
 * 
 */
public class DummyBackend
implements DirectoryManagerBackend {

    /**
     * Will authenticate a user, if the user name is <code>test@feide.no</code>
     * and the password is <code>test</code>.
     * @see no.feide.moria.directory.backend.DirectoryManagerBackend#authenticate(Credentials,
     *      String[])
     */
    public UserAttribute[] authenticate(Credentials userCredentials, String[] attributeRequest)
    throws BackendException {

        // "Authentication", sort of.
        String username = userCredentials.getUsername();
        String password = userCredentials.getPassword();
        if ((username == "test@feide.no") && (password == "test")) {

            // Successful authentication.
            return prepareAttributes(attributeRequest);

        } else {

            // Bad authentication.
            throw new AuthenticationFailedException(username + " failed authentication");

        }

    }


    /**
     * Prepare and return a proper test attribute set.
     * @param attributeRequest
     *            Only the attribute <code>eduPersonAffiliation</code> is
     *            considered. Not case-sensitive.
     * @return If the attribute is at all requested, will contain the attribute
     *         <code>eduPersonAffiliation</code> with the value
     *         <code>Affiliation</code>. If not, will contain an empty array.
     */
    private UserAttribute[] prepareAttributes(String[] attributeRequest) {

        // Check whether eduPersonAffiliation exists in the attribute request.
        for (int i = 0; i < attributeRequest.length; i++)
            if (attributeRequest[i].toUpperCase() == "eduPersonAffiliation") {

                // Return the attribute value.
                String[] attributeValues = new String[] {"eduPersonAffiliation"};
                try {
                    return new UserAttribute[] {new UserAttribute("eduPersonAffiliation", new String[] {"eduPersonAffiliation"})};
                } catch (IllegalAttributeException e) {
                    // TODO: Add logging; unexpected exception.
                }

            }

        // Return an empty array.
        return new UserAttribute[] {};

    }

}