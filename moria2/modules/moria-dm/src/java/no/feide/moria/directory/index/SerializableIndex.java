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
     * Get the association object. Used by the <code>equals(Object)</code>
     * method.
     * @return The associations in this index.
     */
    public HashMap getAssociations() {

        return new HashMap(associations);

    }
    
    
    /**
     * Get the exception object. Used by the <code>equals(Object)</code>
     * method.
     * @return The exceptions in this index.
     */
    public HashMap getExceptions() {
        
        return new HashMap(exceptions);
        
    }


    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {

        // Check class.
        if (obj.getClass() != this.getClass())
            return false;
        final SerializableIndex other = (SerializableIndex) obj;

        // Verify associations.
        if (!other.getAssociations().equals(associations))
            return false;
        
        // Verify exceptions.
        if (!other.getExceptions().equals(exceptions))
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
        
        // Extract the realm.
        String realm = id.substring(id.lastIndexOf('@'));
        return (List) associations.get(realm);

    }

}