package no.feide.moria.directory.index;

import java.util.List;

/**
 * The interface to an underlying index implementation.
 */
public interface DirectoryManagerIndex {

    /**
     * Looks up the backend reference from a given logical ID.
     * @param id
     *            The logical ID.
     * @return A list of backend references, or <code>null</code> if no such
     *         reference was found.
     */
    public List lookup(String id);

}