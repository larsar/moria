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

package no.feide.moria.webservices.v2_1;

import java.io.Serializable;

import javax.xml.namespace.QName;

import org.apache.axis.description.ElementDesc;
import org.apache.axis.description.TypeDesc;
import org.apache.axis.encoding.Deserializer;
import org.apache.axis.encoding.Serializer;
import org.apache.axis.encoding.ser.BeanDeserializer;
import org.apache.axis.encoding.ser.BeanSerializer;

/**
 * @author Bj&oslash;rn Ola Smievoll &lt;b.o.smievoll@conduct.no&gt;
 * @version $Revision$
 */
public final class Attribute implements Serializable {

    /** The name of this Attribute. */
    private String name = null;

    /** The values of this Attribute. */
    private String[] values = null;

    /**
     * Gets the name of this attribute.
     *
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the attribute.
     *
     * @param name The name to set.
     */
    public void setName(final String name) {
        this.name = name;
    }

    /**
     * Gets a String array containing the values of the attribute.
     *
     * @return The value array.
     */
    public String[] getValues() {
        return values;
    }

    /**
     * Sets the values for the attribute.
     *
     * @param values The values to set.
     */
    public void setValues(final String[] values) {
        this.values = values;
    }
}
