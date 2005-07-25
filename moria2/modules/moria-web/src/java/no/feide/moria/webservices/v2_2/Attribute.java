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

    /**
     * Serial version UID (generated).
     */
    private static final long serialVersionUID = -3264658154719205385L;

    /** The name of this Attribute. */
    private String name = null;

    /**
     * The actual separator character(s) used for encoding. Added as a field to
     * allow it to be visible across SOAP calls.
     */
    private String separator = null;

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
     * Get the actual separator value used for encoding the attribute values.
     * @return The separator character(s).
     */
    public String getSeparator() {

        return separator;

    }
    
    
    /**
     * Set the actual separator value used for encoding the attribute values.
     * @param separator The separator character(s).
     */
    public void setSeparator(final String separator) {
        
        this.separator = separator;
        
    }


    /**
     * Get the encoded <code>String</code> containing the values of the
     * attribute, separated by <code>separator</code>. All natural occurences
     * of <code>separator</code> in the original values will show up as two
     * <code>separator</code> character sequences. Be sure to check the actual
     * separator value using <code>getSeparator()</code>.
     * @return The value array.
     * @see #getSeparator()
     */
    public String getValues() {

        return values;

    }


    /**
     * Sets the values for the attribute. Must use the separator (given by
     * <code>getSeparator()</code>) between the values, and any natural
     * occurences of the separator in an attribute value must be replaced with
     * two subsequent occurences of the separator. 
     * @param values
     *            The values to set.
     * @see #getSeparator()
     */
    public void setValues(final String values) {

        this.values = values;
    }

}
