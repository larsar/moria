package no.feide.moria.directory.index;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * The serializable index. Used for offline generation of a new index.
 */
public class SerializableIndex
implements Serializable, DirectoryManagerIndex {

    /**
     * Internal list of associaitons. Protected, for the benefit of
     * <code>WriteableSerializableIndex</code>.
     */
    protected HashMap associations = new HashMap();
    
    protected HashMap exceptions = new HashMap();


    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        // Check class.
        if (obj.getClass() != this.getClass())
            return false;
        final SerializableIndex other = (SerializableIndex) obj;

        // Verify associations.
        if (!other.associations.equals(associations))
            return false;
        
        // Verify exceptions.
        if (!other.exceptions.equals(exceptions))
            return false;

        return true;

    }


    /**
     * Look up an element from the index.
     * @param id
     *            The identificator (on the form
     *            <code>identificator@realm</code>) to lookup.
     * @return A list of one or more references matching the given
     *         identificator, or <code>null</code> if no such reference was
     *         found.
     * @see no.feide.moria.directory.index.DirectoryManagerIndex#lookup(java.lang.String)
     */
    public List lookup(final String id) {

        // Sanity check.
        if (id == null)
            return null;

        // Do we have an explicit match? That is, an exception from the association rule?
        if (exceptions.containsKey(id)) {
            LinkedList list = new LinkedList();
            list.add((String)exceptions.get(id));
            return (List)list;
        }
        
        // Extract the realm, with sanity check.
        int i = id.lastIndexOf('@');
        if (i < 0)
            return null;
        String realm = id.substring(i);
        return (List) associations.get(realm);

    }
    
    
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
    
    
    /**
     * Gives a string representation of the object.
     * @return The object represented as a <code>String</code>; includes associations and exceptions.
     */
    public String toString() {
        
        String s = "\tAssociations: " + associations.toString().replaceAll("], ", "\n\t               ");
        return s = s + "\tExceptions: " + exceptions.toString().replaceAll(", ", "\n\t             ");
        
    }

}