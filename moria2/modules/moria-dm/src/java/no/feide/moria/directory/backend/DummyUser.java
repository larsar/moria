package no.feide.moria.directory.backend;

import java.util.HashMap;
import java.util.List;

/**
 * Used to internally represent a user in the <code>DummyBackend</code> class.
 */
public class DummyUser {

    /**
     * This user's attributes; the keys are attribute names as
     * <code>String</code> s and the values are attribute values as
     * <code>List</code>s. private HashMap myAttributes; /**
     */
    private HashMap myAttributes;

    /** This user's username. */
    private String myUsername;

    /** This user's password. */
    private String myPassword;


    /**
     * Constructor.
     * @param username
     *            The username. Cannot be <code>null</code>.
     * @param password
     *            The password. Cannot be <code>null</code>.
     * @param attributes
     *            The user's attributes, if any. Should contain attribute names
     *            as keys (<code>String</code>s) and attribute values as
     *            values (<code>List</code> s).
     * @throws IllegalArgumentException
     *             If <code>username</code> or <code>password</code> is
     *             <code>null</code>.
     */
    public DummyUser(final String username, final String password, final HashMap attributes) {

        super();

        // Sanity checks.
        if (username == null)
            throw new IllegalArgumentException("Username cannot be NULL");
        if (password == null)
            throw new IllegalArgumentException("Password cannot be NULL");

        // Assignments.
        myUsername = username;
        myPassword = password;
        if (attributes == null)
            myAttributes = new HashMap();
        else
            myAttributes = new HashMap(attributes);
    }


    /**
     * "Authenticate" this user, by doing a case-insensitive match on username
     * and case-sensitive match on password.
     * @param username
     *            The username to match. Cannot be <code>null</code>.
     * @param password
     *            The password to match. Cannot be <code>null</code>.
     * @return
     */
    public boolean authenticate(final String username, final String password) {

        // Sanity checks.
        if (username == null)
            throw new IllegalArgumentException("Username cannot be NULL");
        if (password == null)
            throw new IllegalArgumentException("Password cannot be NULL");

        // "Authentication".
        return ((username.equalsIgnoreCase(myUsername)) && (password.equals(myPassword)));

    }


    /**
     * Get this user's attributes.
     * @param The
     *            attribute request. Case is ignored.
     * @return The requested user's attributes, if any were found. Note that the
     *         attribute names returned will match the case of the attribute
     *         names in the request.
     */
    public HashMap getAttributes(String[] request) {

        HashMap requestedAttributes = new HashMap();

        // Do we have a non-empty attribute request?
        if ((request != null) && (request.length > 0)) {

            // Some attributes were requested.
            for (int i = 0; i < request.length; i++)
                if (myAttributes.containsKey(request[i].toLowerCase())) {

                    // Requested attribute found.
                    List requestedValues = (List) myAttributes.get(request[i].toLowerCase());
                    requestedAttributes.put(request[i], requestedValues.toArray(new String[] {}));

                }
        }

        // Return requested attributes.
        return requestedAttributes;

    }

}