package no.feide.moria.directory.index;

import java.io.Serializable;

/**
 * Adds updater methods to the <code>SerializableIndex</code> object. Index
 * generators should use this class to create the index; the Directory Manager
 * should use the superclass for lookups.
 */
public class WriteableSerializableIndex
extends SerializableIndex
implements Serializable {

    /**
     * Add a new realm-to-base association to the index.
     * @param realm
     *            The realm (typically user realm) related to this base. Cannot
     *            be <code>null</code>.
     * @param base
     *            The association. In practical use this will be an LDAP search
     *            base on the form
     *            <code>ldap://some.ldap.server:636/dc=search,dc=base</code>.
     *            Cannot be <code>null</code>.
     */
    public void addAssociation(final String realm, final String base) {

        // Sanity checks.
        if (realm == null)
            throw new IllegalArgumentException("Realm cannot be NULL");
        if (base == null)
            throw new IllegalArgumentException("Base cannot be NULL");

        associations.put(realm, base);

    }

}