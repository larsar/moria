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
 * $Id$
 */

package no.feide.moria.webservices.v2_2;

import java.io.Serializable;

/**
 * @author Bj&oslash;rn Ola Smievoll &lt;b.o.smievoll@conduct.no&gt;
 * @version $Revision$
 */
public final class Attribute
implements Serializable {

    /** The name of this Attribute. */
    private String name = null;

    /**
     * The separator character(s) for attribute values, for encoding.<br>
     * <br>
     * Current value is <code>":"</code>.
     */
    public static final String separator = ":";

    /** The encoded values of this Attribute. */
    private String values = null;


    /**
     * Gets the name of this attribute.
     * @return Returns the name.
     */
    public String getName() {

        return name;
    }


    /**
     * Sets the name of the attribute.
     * @param name
     *            The name to set.
     */
    public void setName(final String name) {

        this.name = name;
    }


    /**
     * Get the encoded <code>String</code> containing the values of the
     * attribute, separated by <code>separator</code>.
     * @return The value array.
     * @see #separator
     */
    public String getValues() {

        return values;

    }


    /**
     * Sets the values for the attribute. Must use the <code>separator</code>
     * between attribute values.
     * @param values
     *            The values to set.
     * @see #separator
     */
    public void setValues(final String values) {

        this.values = values;
    }


    /**
     * Encode a <code>String</code> array into a single string, using the
     * <code>separator</code> between attribute values.
     * @param values
     *            The values to be encoded.
     * @return The encoded values.
     */
    protected static String encodeValues(final String[] values) {

        String encoded = new String();
        for (int i = 0; i < values.length; i++)
            encoded = encoded + values[i] + separator;
        encoded = encoded.substring(0, encoded.length() - separator.length());
        return new String(encoded);

    }
}
