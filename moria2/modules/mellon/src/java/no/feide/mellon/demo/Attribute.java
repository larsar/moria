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

package no.feide.moria.demo;

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

    /** Type metadata. */
    private static TypeDesc typeDesc = new TypeDesc(Attribute.class);

    static {
        typeDesc.setXmlType(new QName("http://localhost:8080/moria/v2_1/Authentication", "Attribute"));
        ElementDesc elemField = new org.apache.axis.description.ElementDesc();

        elemField.setFieldName("name");
        elemField.setXmlName(new QName("", "name"));
        elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);

        elemField = new ElementDesc();
        elemField.setFieldName("values");
        elemField.setXmlName(new QName("", "values"));
        elemField.setXmlType(new QName("http://www.w3.org/2001/XMLSchema", "string"));
        typeDesc.addFieldDesc(elemField);
    }

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

    /**
     * Returns type metadata object.
     *
     * @return The metadata
     */
    public static TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Gets custom serializer.
     *
     * @param mechType XML processing mechanism type.
     * @param javaType Class of the Java type.
     * @param xmlType Qualified name of the XML data type.
     * @return A serializer for the specified XML processing mechanism type.
     */
    public static Serializer getSerializer(final String mechType, final Class javaType, final QName xmlType) {
        return new BeanSerializer(javaType, xmlType, typeDesc);
    }

    /**
     * Gets custom Deserializer.
     *
     * @param mechType XML processing mechanism type.
     * @param javaType Class of the Java type.
     * @param xmlType Qualified name of the XML data type.
     * @return A deserializer for the specified XML processing mechanism type.
     */
    public static Deserializer getDeserializer(final String mechType, final Class javaType, final QName xmlType) {
        return new BeanDeserializer(javaType, xmlType, typeDesc);
    }
}


