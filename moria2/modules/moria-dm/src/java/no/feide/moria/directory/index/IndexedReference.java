package no.feide.moria.directory.index;

/**
 * Represents an indexed reference to an external element. Used to differ
 * between explicitly and implicitly indexed references.
 */
public class IndexedReference {

    private final String myName;

    private final String[] myReferences;

    private final boolean explicit;


    /**
     * Constructor. Creates a new indexed reference.
     * @param name
     *            The index reference's name.
     * @param references
     *            One or more external references. Cannot be <code>null</code>.
     * @param explicitReference
     *            <code>true</code> if this is a fully qualified reference to
     *            an external element, otherwise <code>false</code>.
     */
    public IndexedReference(String name, String[] references, boolean explicitReference) {

        super();

        // Sanity checks.
        if (name == null)
            throw new IllegalArgumentException("Name cannot be NULL");
        if ((references == null) || (references.length == 0))
            throw new IllegalArgumentException("References cannot be NULL or an empty string");

        // Assignments.
        myName = new String(name);
        myReferences = (String[])references.clone();
        explicit = explicitReference;

    }


    /**
     * Get the index reference's name.
     * @return The index reference's name.
     */
    public String getName() {

        return myName;

    }


    /**
     * Get the external references.
     * @return One or more external references.
     */
    public String[] getReferences() {

        return (String[])myReferences.clone();

    }


    /**
     * Check whether this reference is an explicit reference to an external
     * element, or an implicit reference to a search base of some sort.
     * @return <code>true</code> if this is a fully qualified external
     *         reference to an element, otherwise <code>false</code>.
     */
    public boolean isExplicitlyIndexed() {

        return explicit;

    }

}