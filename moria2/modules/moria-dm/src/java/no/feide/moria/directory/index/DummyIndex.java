package no.feide.moria.directory.index;

/**
 * A simple hardcoded index, for testing purposes. Contains one mapping, from
 * the ID <code>user@some.realm</code> to the reference
 * <code>ldap://my.ldap.server:636/dc=search,dc=base</code>.
 */
public class DummyIndex
implements DirectoryManagerIndex {

    /*
     * Maps the ID <code> user@some.realm </code> to the reference <code>
     * ldap://my.ldap.server:636/dc=search,dc=base </code> . All other IDs will
     * be mapped to a <code> null </code> value.
     * @see no.feide.moria.directory.index.DirectoryManagerIndex#lookup(java.lang.String)
     */
    public String lookup(String id) {

        // Sanity check.
        if (id == null)
            return null;

        if (id.equalsIgnoreCase("user@some.realm"))
            return new String("ldap://my.ldap.server:636/dc=search,dc=base");
        return null;
    }

}