package no.feide.moria.directory.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import no.feide.moria.directory.Credentials;
import no.feide.moria.directory.index.IndexedReference;
import no.feide.moria.log.MessageLogger;

import org.jdom.Element;

/**
 * Hard-coded dummy backend, for testing. Does not require an actual backend
 * source.
 */
public class DummyBackend
implements DirectoryManagerBackend {

    /** Message logger. */
    private final MessageLogger log = new MessageLogger(DummyBackend.class);

    /** Internal representation of user elements and their attributes. */
    private HashMap userMap;


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
        userMap = new HashMap();
        final Iterator users = dummy.getChildren("User").iterator();
        while (users.hasNext()) {

            // Parse any attribute elements.
            HashMap attributeMap = new HashMap();
            final Element user = (Element) users.next();
            final Iterator attributes = user.getChildren("Attribute").iterator();
            while (attributes.hasNext()) {

                // Parse any value elements.
                ArrayList valueList = new ArrayList();
                final Element attribute = (Element) attributes.next();
                final Iterator values = attribute.getChildren("Value").iterator();
                while (values.hasNext()) {

                    // Parse the attribute values.
                    final Element value = (Element) values.next();
                    valueList.add(value.getText());

                }

                // Map an attribute to its values.
                attributeMap.put(attribute.getAttribute("name").getValue().toLowerCase(), valueList);

            }

            // Map an attribute to a user.
            userMap.put(user.getAttribute("name").getValue().toLowerCase(), attributeMap);

        }

    }


    /**
     * Does nothing.
     * @see DirectoryManagerBackend#open(IndexedReference)
     */
    public void open(final IndexedReference reference) {

        // Ignored.

    }


    /**
     * Will check whether a user exists.
     * @param username
     *            The username. Case is ignored.
     * @see DirectoryManagerBackend#userExists(String)
     */
    public boolean userExists(final String username) {

        // Fake a test.
        if (username == null)
            return false;
        return userMap.containsKey(username.toLowerCase());

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

        // "Authentication", sort of.
        final String username = userCredentials.getUsername().toLowerCase();
        final String password = userCredentials.getPassword();
        if ((userMap.containsKey(username)) && password.equals(username)) {

            // Successful authentication; user found.
            HashMap requestedAttributes = new HashMap();
            if ((attributeRequest != null) && (attributeRequest.length > 0)) {

                // Some attributes were requested.
                HashMap allAttributes = (HashMap) userMap.get(username);
                for (int i = 0; i < attributeRequest.length; i++)
                    if (allAttributes.containsKey(attributeRequest[i].toLowerCase())) {

                        // Requested attribute found.
                        List requestedValues = (List) allAttributes.get(attributeRequest[i].toLowerCase());
                        requestedAttributes.put(attributeRequest[i], requestedValues.toArray(new String[] {}));

                    }
            }

            // Return found attributes.
            return requestedAttributes;

        } else {

            // Bad authentication.
            throw new AuthenticationFailedException('\"' + username + "\" failed authentication");

        }

    }


    /**
     * Pro-forma declaration of the <code>close()</code> method. Does nothing
     * in this case.
     */
    public void close() {

        // Does nothing.

    }

}