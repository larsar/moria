package no.feide.moria.directory.index;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * A simple hardcoded index, for testing purposes. Contains one mapping, from
 * the ID <code>user@some.realm</code> to the reference
 * <code>ldap://my.ldap.server:636/dc=search,dc=base</code>.
 */
public class DummyIndex
implements DirectoryManagerIndex {

    /**
     * Needed to satisfy the <code>DirectoryManagerIndex</code> interface.
     * Does nothing.
     * @see no.feide.moria.directory.index.DirectoryManagerIndex#setConfig(java.util.Properties)
     */
    public void setConfig(Properties config) {

        // Does nothing.

    }


    /*
     * Maps the ID <code> user@some.realm </code> to the reference <code>
     * ldap://my.ldap.server:636/dc=search,dc=base </code> . All other IDs will
     * be mapped to a <code> null </code> value.
     * @see no.feide.moria.directory.index.DirectoryManagerIndex#lookup(java.lang.String)
     */
    public List lookup(final String id) {

        // Sanity check.
        if (id == null)
            return null;

        if (id.equalsIgnoreCase("user@some.realm")) {
            LinkedList value = new LinkedList();
            value.add(new String("ldap://my.ldap.server:636/dc=search,dc=base"));
            return value;
        }
        return null;
    }

}