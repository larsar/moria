package no.feide.moria.directory.index;

/**
 * The interface to an underlying index implementation.
 */
public interface DirectoryManagerIndex {

    /**
     * Looks up the backend reference from a given logical ID.
     * @param id
     *            The logical ID.
     * @return The backend reference, or <code>NULL</code> if no such
     *         reference was found.
     */
    public String lookup(String id);

}