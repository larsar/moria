package no.feide.moria.directory.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import no.feide.moria.directory.Credentials;
import no.feide.moria.directory.index.IndexedReference;

import org.jdom.Element;

/**
 * Hard-coded dummy backend, for testing. Does not require an actual backend
 * source.
 */
public class DummyBackend
implements DirectoryManagerBackend {

    /**
     * Maps user names (converted to lowercase) to <code>DummyUser</code>
     * elements.
     */
    private HashMap users;


    /**
     * Set the configuration used by this instance.
     * @param config
     *            A <code>Backend</code> configuration element. Must contain a
     *            <code>Dummy</code> element (if more than one only the first
     *            is considered), which is expected to contain one or more
     *            <code>User</code> elements, each with one or more
     *            <code>Attribute</code> elements, which again have
     *            <code>Value</code> elements with exactly one value child.
     *            Allows for easy configuration of test cases, without having to
     *            rely on an external backend source. See the supplied dummy
     *            configuration for a workable example. Note that attribute and
     *            user names are case-insensitive, while attribute values are
     *            stored as specified in the configuration.
     */
    protected void setConfig(final Element config) {

        // Get Dummy element.
        final Element dummy = config.getChild("Dummy");

        // Parse any user elements.
        users = new HashMap();
        final Iterator userElements = dummy.getChildren("User").iterator();
        while (userElements.hasNext()) {

            // Parse any attribute elements.
            HashMap attributes = new HashMap();
            final Element user = (Element) userElements.next();
            final Iterator attributeElements = user.getChildren("Attribute").iterator();
            while (attributeElements.hasNext()) {

                // Parse any value elements.
                ArrayList values = new ArrayList();
                final Element attribute = (Element) attributeElements.next();
                final Iterator valueElements = attribute.getChildren("Value").iterator();
                while (valueElements.hasNext()) {

                    // Parse the attribute values.
                    final Element value = (Element) valueElements.next();
                    values.add(value.getText());

                }

                // Map an attribute to its values.
                attributes.put(attribute.getAttribute("name").getValue().toLowerCase(), values);

            }

            // Add a new user.
            DummyUser newUser = new DummyUser(user.getAttribute("name").getValue(), user.getAttribute("password").getValue(), attributes);
            users.put(user.getAttribute("name").getValue().toLowerCase(), newUser);

        }

    }


    /**
     * Does nothing, but needed to fulfill the
     * <code>DirectoryManagerBackend</code> interface.
     * @param reference
     *            Ignored.
     * @see DirectoryManagerBackend#open(IndexedReference)
     */
    public void open(final IndexedReference reference) {

        // Does nothing.

    }


    /**
     * Will check whether a user exists.
     * @param username
     *            The username. Case is ignored.
     * @see DirectoryManagerBackend#userExists(String)
     */
    public boolean userExists(final String username) {

        if (username == null)
            return false;
        return users.containsKey(username.toLowerCase());

    }


    /**
     * Will authenticate a user, if the user exists and the username equals the
     * password.
     * @see DirectoryManagerBackend#authenticate(Credentials, String[])
     */
    public HashMap authenticate(final Credentials userCredentials, final String[] attributeRequest)
    throws AuthenticationFailedException {

        // Sanity check.
        if (userCredentials == null)
            throw new IllegalArgumentException("Credentials cannot be NULL");

        // Find and authenticate user.
        DummyUser user = (DummyUser) users.get(userCredentials.getUsername());
        if ((user != null) && (user.authenticate(userCredentials.getUsername(), userCredentials.getPassword()))) {

            // Successful authentication; return any requested user attributes.
            return user.getAttributes(attributeRequest);

        } else {

            // Bad authentication.
            throw new AuthenticationFailedException("User \"" + userCredentials.getUsername() + "\" failed authentication");

        }

    }

    /**
     * Does nothing, but needed to fulfill the
     * <code>DirectoryManagerBackend</code> interface.
     * @see DirectoryManagerBackend#close()
     */
    public void close() {

        // Does nothing.

    }

}