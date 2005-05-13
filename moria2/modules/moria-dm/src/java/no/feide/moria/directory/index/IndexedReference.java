/*
 * Copyright (c) 2004 UNINETT FAS
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 */

package no.feide.moria.directory.index;

/**
 * Represents an indexed reference to an external element. Used to distinguish
 * between explicitly and implicitly indexed references.
 */
public class IndexedReference {

    /** External references. */
    private final String[] myReferences;

    /**
     * Usernames, where each element matches the same index in
     * <code>myReferences</code>.
     */
    private final String[] myUsernames;

    /**
     * Passwords, where each element matches the same index in
     * <code>myReferences</code>.
     */
    private final String[] myPasswords;

    /** true if this is a fully qualified reference to an external element. */
    private final boolean explicit;


    /**
     * Constructor. Creates a new indexed reference.
     * @param references
     *            One or more external references. Cannot be <code>null</code>.
     * @param usernames
     *            Usernames for each external reference. Cannot be
     *            <code>null</code>, and must have the same number of
     *            elements as <code>references</code>.
     * @param passwords
     *            Passwords for each external reference. Cannot be
     *            <code>null</code>, and must have the same number of
     *            elements as <code>references</code>.
     * @param explicitReference
     *            <code>true</code> if this is a fully qualified reference to
     *            an external element, otherwise <code>false</code>.
     * @throws NullPointerException
     * @throws IllegalArgumentException
     *             If <code>references</code> is <code>null</code> or an
     *             empty array, or if <code>usernames</code> or
     *             <code>passwords</code> are <code>null</code> or contain a
     *             different number of elements than <code>references</code>.
     */
    public IndexedReference(final String[] references, final String[] usernames, final String[] passwords, final boolean explicitReference)
    throws IllegalArgumentException {

        super();

        // Sanity checks.
        if ((references == null) || (references.length == 0))
            throw new NullPointerException("References cannot be NULL or an empty array");
        if (usernames == null)
            throw new IllegalArgumentException("Usernames cannot be NULL");
        if (passwords == null)
            throw new IllegalArgumentException("Passwords cannot be NULL");
        if ((usernames.length != references.length) || (passwords.length != references.length))
            throw new IllegalArgumentException("References, usernames and passwords must have the same number of elements");

        // Assignments.
        myReferences = (String[]) references.clone();
        explicit = explicitReference;
        myUsernames = (String[]) usernames.clone();
        myPasswords = (String[]) passwords.clone();

    }


    /**
     * Gets the external references.
     * @return One or more external references.
     */
    public final String[] getReferences() {

        return (String[]) myReferences.clone();

    }
    
    
    /**
     * Gets the usernames.
     * @return One or more usernames.
     */
    public final String[] getUsernames() {

        return (String[]) myUsernames.clone();

    }
    
    
    /**
     * Gets the external passwords.
     * @return One or more passwords.
     */
    public final String[] getPasswords() {

        return (String[]) myPasswords.clone();

    }


    /**
     * Checks whether this reference is an explicit reference to an external
     * element, or an implicit reference to a search base of some sort.
     * @return <code>true</code> if this is a fully qualified external
     *         reference to an element, otherwise <code>false</code>.
     */
    public final boolean isExplicitlyIndexed() {

        return explicit;

    }

}
