package no.feide.moria.directory.index;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Adds updater methods to the <code>SerializableIndex</code> object. Index
 * generators should use this class to create the index; the Directory Manager
 * should use the superclass for lookups.
 */
public class WriteableSerializableIndex
extends SerializableIndex
implements Serializable {

    /**
     * Add a new realm-to-base association to the index. Should only be used to
     * build a new index, not to update an existing index.
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

        // New association or updating an existing?
        if (associations.containsKey(realm)) {

            // Update existing association.
            List oldBase = (List) associations.get(realm);
            oldBase.add(base);
            associations.put(realm, oldBase);

        } else {

            // Create new association.
            LinkedList newBase = new LinkedList();
            newBase.add(base);
            associations.put(realm, newBase);

        }

    }


    /**
     * Add a new search exception to this index. Should only be used to build a
     * new index, not to update an existing index.
     * @param id
     *            The identificator for this exception, typically a user ID.
     *            Cannot be <code>null</code>.
     * @param reference
     *            The reference. In practical use this will be an LDAP search
     *            base on the form
     *            <code>ldap://some.ldap.server:636/dc=search,dc=base</code>.
     *            Cannot be <code>null</code>.
     */
    public void addException(final String id, final String reference) {

        // Sanity checks.
        if (id == null)
            throw new IllegalArgumentException("ID cannot be NULL");
        if (reference == null)
            throw new IllegalArgumentException("Reference cannot be NULL");

        exceptions.put(id, reference);

    }

}