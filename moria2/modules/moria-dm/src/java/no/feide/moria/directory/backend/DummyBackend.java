package no.feide.moria.directory.backend;

import no.feide.moria.directory.Credentials;
import no.feide.moria.directory.IllegalAttributeException;
import no.feide.moria.directory.UserAttribute;
import no.feide.moria.log.MessageLogger;

/**
 * Hard-coded dummy backend, for testing. Does not require an actual backend
 * source.
 */
public class DummyBackend
implements DirectoryManagerBackend {

    /** Message logger. */
    private MessageLogger log = new MessageLogger(DummyBackend.class);


    /**
     * Pro-forma implementation of the <code>open()</code> method. Does
     * nothing.
     * @param reference
     *            Ignored.
     */
    public void open(String reference) {

        // Does nothing.

    }


    /**
     * Will authenticate a user, if the user name is
     * <code>user@some.realm</code> and the password is <code>password</code>.
     * @see no.feide.moria.directory.backend.DirectoryManagerBackend#authenticate(Credentials,
     *      String[])
     */
    public UserAttribute[] authenticate(Credentials userCredentials, String[] attributeRequest)
    throws BackendException {

        // Sanity check.
        if (userCredentials == null) {

            // Bad authentication.
            log.logWarn("Attempt to authenticate anonymous user");
            throw new AuthenticationFailedException("Anonymous user cannot be authenticated");

        }

        // "Authentication", sort of.
        String username = userCredentials.getUsername();
        String password = userCredentials.getPassword();
        if ((username.equalsIgnoreCase("user@some.realm")) && (password.equals("password"))) {

            // Successful authentication.
            return prepareAttributes(attributeRequest);

        } else {

            // Bad authentication.
            log.logWarn('\"' + username + "\" failed authentication");
            throw new AuthenticationFailedException('\"' + username + "\" failed authentication");

        }

    }


    /**
     * Prepare and return a proper test attribute set.
     * @param attributeRequest
     *            Only the attribute <code>someAttribute</code> is considered.
     *            Not case-sensitive.
     * @return If the attribute is at all requested, will contain the attribute
     *         <code>someAttribute</code> with the value
     *         <code>someValue</code>. If not, will contain an empty array.
     */
    private UserAttribute[] prepareAttributes(String[] attributeRequest) {

        // Sanity check.
        if (attributeRequest == null)
            return new UserAttribute[] {};

        // Check whether someAttribute exists in the attribute request.
        for (int i = 0; i < attributeRequest.length; i++)
            if (attributeRequest[i].equalsIgnoreCase("someAttribute")) {

                // Return the attribute value.
                String[] attributeValues = new String[] {"someValue"};
                try {
                    return new UserAttribute[] {new UserAttribute("someAttribute", attributeValues)};
                } catch (IllegalAttributeException e) {
                    // Unexpected exception.
                    log.logCritical("Unexpected exception when creating user attribute", e);
                }

            }

        // Return an empty array.
        return new UserAttribute[] {};

    }


    /**
     * Pro-forma declaration of the <code>close()</code> method. Does nothing
     * in this case.
     */
    public void close() {

        // Does nothing.

    }

}