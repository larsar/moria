package no.feide.moria.directory.backend;

import java.util.HashMap;

import no.feide.moria.directory.Credentials;
import no.feide.moria.directory.index.IndexedReference;
import no.feide.moria.log.MessageLogger;

/**
 * Hard-coded dummy backend, for testing. Does not require an actual backend
 * source.
 */
public class DummyBackend
implements DirectoryManagerBackend {

    /** Message logger. */
    private final MessageLogger log = new MessageLogger(DummyBackend.class);

    /** The external reference. */
    private String myReference;


    /**
     * Sets the external reference.
     * @param reference
     *            The external reference. Must include at least one actual
     *            reference, equal to
     *            <code>ldap://my.ldap.server:636/dc=search,dc=base</code>,
     *            or any later authentication will fail.
     */
    public void open(final IndexedReference reference) {

        myReference = new String(reference.getReferences()[0]);

    }


    /**
     * Will authenticate a user successfully, if the user name is
     * <code>user@some.realm</code> and the password is <code>password</code>.
     * @see no.feide.moria.directory.backend.DirectoryManagerBackend#authenticate(Credentials,
     *      String[])
     */
    public HashMap authenticate(final Credentials userCredentials, final String[] attributeRequest)
    throws AuthenticationFailedException {

        // Sanity check.
        if (userCredentials == null)
            throw new IllegalArgumentException("Credentials cannot be NULL");

        // "Authentication", sort of.
        final String username = userCredentials.getUsername();
        final String password = userCredentials.getPassword();
        if (myReference.equalsIgnoreCase("ldap://my.ldap.server:636/dc=search,dc=base") && (username.equalsIgnoreCase("user@some.realm")) && (password.equals("password"))) {

            // Successful authentication.
            return prepareAttributes(attributeRequest);

        } else {

            // Bad authentication.
            throw new AuthenticationFailedException('\"' + username + "\" failed authentication");

        }

    }


    /**
     * Prepare and return a proper test attribute set.
     * @param attributeRequest
     *            Only the attribute <code>someAttribute</code> is considered.
     *            Not case-sensitive and may be <code>null</code>.
     * @return If the attribute is at all requested, will contain the attribute
     *         <code>someAttribute</code> with the value
     *         <code>someValue</code>. If not, will contain an empty
     *         <code>HashMap</code>.
     */
    private HashMap prepareAttributes(final String[] attributeRequest) {

        // Sanity check.
        if (attributeRequest == null)
            return new HashMap();

        // Check whether someAttribute exists in the attribute request.
        for (int i = 0; i < attributeRequest.length; i++)
            if (attributeRequest[i].equalsIgnoreCase("someAttribute")) {

                // Return a new attribute.
                HashMap attributes = new HashMap();
                attributes.put("someAttribute", new String[] {"someValue"});
                return attributes;

            }

        // Return an empty HashMap.
        return new HashMap();

    }


    /**
     * Pro-forma declaration of the <code>close()</code> method. Does nothing
     * in this case.
     */
    public void close() {

        // Does nothing.

    }

}