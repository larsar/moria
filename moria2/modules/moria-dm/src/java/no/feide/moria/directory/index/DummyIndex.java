package no.feide.moria.directory.index;

/**
 * A simple hardcoded index, for testing purposes. Contains one mapping, from
 * the ID <code>test@feide.no</code> to the reference
 * <code>ldap://ldap.feide.no:636/ou=people,dc=feide,dc=no</code>.
 */
public class DummyIndex
implements DirectoryManagerIndex {

    /*
     * Maps the ID <code> test@feide.no </code> to the reference <code>
     * ldap://ldap.feide.no:636/ou=people,dc=feide,dc=no</code>. All other IDs
     * will be mapped to a <code> null </code> value.
     * @see no.feide.moria.directory.index.DirectoryManagerIndex#lookup(java.lang.String)
     */
    public String lookup(String id) {

		if (id == "test@feide.no")
			return new String("ldap://ldap.feide.no:636/ou=people,dc=feide,dc=no");
        return null;
    }

}