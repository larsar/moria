package no.feide.moria.directory.index;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * The serializable index. Used for offline generation of a new index.
 */
public class SerializableIndex
implements Serializable {

    /**
     * Internal list of associaitons. Protected, for the benefit of
     * <code>WriteableSerializableIndex</code>.
     */
    protected HashMap associations = new HashMap();


    /**
     * Get the bases associated with a realm.
     * @param realm
     *            The realm associated with this base. cannot be
     *            <code>null</code>.
     * @return The bases associated with this realm, or <code>null</code> if no
     *         such association was found.
     */
    public List getAssociation(final String realm) {

        // Sanity check.
        if (realm == null)
            throw new IllegalArgumentException("Realm cannot be NULL");

        return (List)associations.get(realm);

    }


    /**
     * Get the association object. Used by the <code>equals(Object)</code>
     * method.
     * @return The associations in this index.
     */
    public HashMap getAssociations() {

        return new HashMap(associations);

    }


    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        // Check class.
        if (obj.getClass() != this.getClass())
            return false;
        final SerializableIndex other = (SerializableIndex) obj;

        // Verify contents.
        if (!other.getAssociations().equals(associations))
            return false;

        return true;

    }

}