/*
 * Copyright (c) 2004 UNINETT FAS
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * $Id$
 */

package no.feide.moria.webservices.v1_1;

import java.io.Serializable;

import javax.xml.namespace.QName;

import org.apache.axis.description.ElementDesc;
import org.apache.axis.description.TypeDesc;
import org.apache.axis.encoding.Deserializer;
import org.apache.axis.encoding.Serializer;
import org.apache.axis.encoding.ser.BeanDeserializer;
import org.apache.axis.encoding.ser.BeanSerializer;

/**
 * @author Bjørn Ola Smievoll &lt;b.o.smievoll@conduct.no&gt;
 * @version $Revision$
 */
public class Attribute implements Serializable {

    private String name;

    private String[] values;

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the values.
     */
    public String[] getValues() {
        return values;
    }

    /**
     * @param values
     *            The values to set.
     */
    public void setValues(String[] values) {
        this.values = values;
    }

    /* Type metadata */
    private static TypeDesc typeDesc = new TypeDesc(Attribute.class);

    static {
        typeDesc.setXmlType(new QName("https://login.feide.no/moria/v1_1/Authentication", "Attribute"));

        ElementDesc elemField = new ElementDesc();
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
     * Return type metadata object
     * 
     * @return
     */
    public static TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     * 
     * @param mechType
     * @param javaType
     * @param xmlType
     * @return
     */
    public static Serializer getSerializer(String mechType, Class javaType, QName xmlType) {
        return new BeanSerializer(javaType, xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     * 
     * @param mechType
     * @param javaType
     * @param xmlType
     * @return
     */
    public static Deserializer getDeserializer(String mechType, Class javaType, QName xmlType) {
        return new BeanDeserializer(javaType, xmlType, typeDesc);
    }
}
