/*
 * Copyright (c) 2004 UNINETT FAS A/S
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
 */
package no.feide.moria.directory.index;

/**
 * @author Cato Olsen
 */
public final class AffiliationElement
extends Element {

    /** Internal representation of the affiliation's identifying name. */
    private String name;

    /** Internal representation of the affiliation's external reference. */
    private String reference;


    /**
     * Constructor.
     * @param name
     *            The affiliation's name. May not be <code>null</code>.
     * @param reference
     *            The affiliation's external reference. May not be <code>null</code>.
     */
    public AffiliationElement(final String name, final String reference) {

        // Validity checks.
        if (name == null)
            throw new IllegalArgumentException("Name cannot be NULL");
        if (reference == null)
            throw new IllegalArgumentException("reference cannot be NULL");

        this.name = name;
        this.reference = reference;
    }


    /**
     * Get the affiliation's name.
     * @return The affiliation's name. May not be <code>null</code>.
     */
    public String getName() {
        return name;
    }


    /**
     * Get the affiliaton's external reference.
     * @return The affiliation's external reference. May not be <code>null</code>.
     */
    public String getReference() {
        return reference;
    }

}
