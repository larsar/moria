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

package no.feide.moria.servlet;

import java.util.HashMap;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 * @author Eva Indal
 * @version $Revision$
 *
 * The BackendStatusHandler class extends the xml DefaultHandler
 * to read the status xml file.
 */
public class BackendStatusHandler extends DefaultHandler {

    /** Hash map of parsed users. */
    private HashMap bsdata;

    /** The user being parsed. */
    private BackendStatusUser currentuser;

    /** String value of current attribute. */
    private String currentchars;

    /**
     * Constructor.
     *
     */
    public BackendStatusHandler()  {
        currentuser = null;
        bsdata = new HashMap();
    }
    
    
    /**
     * Implements callback that is called at start of document. Empty for now.
     *
     * @throws SAXException
     *          Required by interface.
     */
    public void startDocument() throws SAXException {
    }
    
    /**
     * Implements callback that is called at end of document. Empty for now.
     *
     * @throws SAXException
     *          Required by interface.
     */
    public void endDocument() throws SAXException {
    }
    
    public final void startElement(final String namespaceURI, final String sName, final String qName, final Attributes attrs)
    throws SAXException {
    String eName = sName;
    if (eName.equals("")) eName = qName;

    /* Look for <user> and allocate a new BackendStatusUser if found */
    if (eName.equals("User")) {
        currentuser = new BackendStatusUser();
        currentchars = null;
    } else if (currentuser != null) {
        currentchars = "";
    }
}

    /**
     * Implements callback that is called at end of an xml element.
     *
     * @param namespaceURI  Namespace URI.
     * @param sName  The local name (without prefix), or the empty string if Namespace processing is not being performed.
     * @param qName  The qualified XML 1.0 name (with prefix), or the empty string if qualified names are not available.
     * @throws SAXException
     *          Required by interface.
     *
     * @see org.xml.sax.helpers.DefaultHandler#endElement
     *          for information about the parameters
     */
    public final void endElement(final String namespaceURI, final String sName, final String qName) throws SAXException {
        String eName = sName;
        if (eName.equals("")) eName = qName;

        /* Wait for </user> */
        if (eName.equals("User")) {
            bsdata.put(currentuser.getName(), currentuser);
            currentuser = null;
        } else if (currentuser != null ) {
            if (eName.equals("Name")) {
                currentuser.setName(currentchars);
            }
            else if (eName.equals("Password")) {
                currentuser.setPassword(currentchars);
            }
           else if (eName.equals("Organization")) {
               currentuser.setOrganization(currentchars);
           }
           else if (eName.equals("Contact")) {
               currentuser.setContact(currentchars);
           }
        }
    }
    
    /**
     * Implements callback that is called to process data for an element.
     *
     * @param buf  The characters.
     * @param offset  The start position in the character array.
     * @param len  The number of characters to use from the character array.
     * @throws SAXException
     *          Required by interface.
     *
     * @see org.xml.sax.helpers.DefaultHandler#characters
     *          for information about the parameters
     */
    public final void characters(final char[] buf, final int offset, final int len) throws SAXException {
        String s = new String(buf, offset, len);
        currentchars += s;
    }

    /**
     * Gets parsed attributes.
     *
     * Each element in the returned HashMap is an AttribsData instance
     * @return The parsed attributes.
     */
    public final HashMap getAttribs() {
        return bsdata;
    }
}
