package no.feide.moria.directory.index;


/**
 * This is the interface used to access an underlying index implementation.
 */
public interface DirectoryManagerIndex {

    /**
     * Looks up one or more backend references from a given logical ID,
     * typically a username.
     * @param id
     *            The logical ID.
     * @return One or more backend references , or <code>null</code> if no
     *         such reference was found.
     */
    public IndexedReference lookup(String id);

}