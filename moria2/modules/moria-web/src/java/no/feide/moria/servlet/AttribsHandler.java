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

package no.feide.moria.servlet;

import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;
import java.util.HashMap;

/**
 * 
 * @author Eva Indal
 * @version %I%
 *
 * The AttribsHandler class extends the xml DefaultHandler
 * to read the simple feideattribs xml file.
 */
public class AttribsHandler extends DefaultHandler {
    
    /**
     * Constructor
     *
     */
    public AttribsHandler()  {
        currentattribute = null;
        currentitem = null;
        adata = new HashMap();
        indexcounter = 0;
    }
    
    private HashMap adata;
    private AttribsData currentattribute;
    private String currentchars;
    private String currentitem;
    private int indexcounter;
    
    /**
     * Implements callback that is called at start of document. Empty for now.
     */
    public void startDocument() throws SAXException {
    }
    
    /**
     * Implements callback that is called at end of document. Empty for now.
     */
    public void endDocument() throws SAXException {
    }
    
    /**
     * Implements callback that is called at start of an xml element.
     *
     * @param namespaceURI  namespace URI
     * @param sName  The local name (without prefix), or the empty string if Namespace processing is not being performed.
     * @param qName  The qualified name (with prefix), or the empty string if qualified names are not available. 
     * @param attrs  The specified or defaulted attributes.
     *  
     * @see DefaultHandler.startElement for information about the parameters
     */
    public void startElement(String namespaceURI, String sName, String qName, Attributes attrs) throws SAXException {
        String eName = sName;
        if ("".equals(eName)) eName = qName;
        
        /* look for <attribute> and allocate a new AttribsData if found */
        if (eName.equals("attribute")) {
            currentattribute = new AttribsData(indexcounter);
            indexcounter++;
            currentitem = null;
            currentchars = null;
        }
        else if (currentattribute != null){
            currentitem = eName;
            currentchars = "";        
        }
    }
    
    /**
     * Implements callback that is called at end of an xml element
     * @param namespaceURI  namespace URI
     * @param sName  The local name (without prefix), or the empty string if Namespace processing is not being performed
     * @param qName  The qualified XML 1.0 name (with prefix), or the empty string if qualified names are not available. 
     * 
     * @see DefaultHandler.endElement for information about the parameters
     */
    public void endElement(String namespaceURI, String sName, String qName) throws SAXException {
        String eName = sName;
        if ("".equals(eName)) eName = qName;
        
        /* wait for </attribute> */
        if (eName.equals("attribute")) {
            adata.put(currentattribute.getData("key"), currentattribute);
            currentattribute = null;
        }
        else if (currentattribute != null){
            currentattribute.addData(eName, currentchars);
        }
        
    }
    
    /**
     * Implements callback that is called to soup up data for an element
     * 
     * @param buf  The characters.
     * @param offset  The start position in the character array.
     * @param len  The number of characters to use from the character array. 
     * 
     * @see DefaultHandler.characters for information about the parameters
     */
    public void characters(char buf[], int offset, int len) throws SAXException {
        String s = new String(buf, offset, len);
        currentchars += s;
    }
    
    /**
     * Each element in the returned HashMap is an AttribsData instance
     * @return The parsed attributes.
     */
    public HashMap getAttribs() {
        return adata;
    }
    
}
