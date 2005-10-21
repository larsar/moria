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

import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import java.util.Vector;


/**
 * @author Eva Indal
 * @version $Revision$
 *
 */
public class StatisticsHandler extends DefaultHandler {
    
    private String currentchars = null;
    private String statname = "";
    private Vector ignorevector = new Vector();
    private StatisticsCollection accumstats = new StatisticsCollection("");
    private Vector orgstats = new Vector();
    
    /**
     * Constructor
     *
     */
    public StatisticsHandler()  {
    }
    
    public StatisticsCollection getAccumStatistics() {
        return this.accumstats;
    }
    public int getNumStatisticsCollections() {
        return this.orgstats.size();
    }
    public StatisticsCollection getStatisticsCollection(final int idx) {
        return (StatisticsCollection) this.orgstats.get(idx);
    }
    
    public void addIgnoreService(final String servicename) {
        this.ignorevector.add(servicename);
    }
    
    private boolean shouldIgnore(final String servicename) {
        for (int i = 0; i < this.ignorevector.size(); i++) {
            String s = (String) this.ignorevector.get(i);
            if (s.equals(servicename)) return true;
        }
        return false;
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
    
    /**
     * 
     * @param namespaceURI
     * @param sName
     * @param qName
     * @param attrs
     * @throws SAXException
     */
    public final void startElement(final String namespaceURI, final String sName, final String qName, final Attributes attrs)
    throws SAXException {
        String eName = sName;
        if (eName.equals("")) eName = qName;

        /* Look for <Service> and allocate a new StatisticsData if found */
        if (eName.equals("Service")) {
            this.statname = "";
        }
        else if (eName.equals("Name")) {
            currentchars = "";
        }
        else if (eName.equals("Month")) {
            if (!this.shouldIgnore(this.statname)) {
                final String monthname = attrs.getValue(new String("name"));
                final String countstring = attrs.getValue(new String("count"));
                String orgname = attrs.getValue(new String("org"));
                
                // in case an old statistics.xml file is parsed
                if (orgname == null) orgname = "Unknown Organization";
                
                if (monthname != null && countstring != null) {
                    try {
                        Integer tmp = new Integer(countstring);
                        this.accumstats.addMonth(this.statname, monthname, tmp.intValue());
                        this.findStatisticsCollection(orgname).addMonth(this.statname,
                                                      monthname,
                                                      tmp.intValue());
                    } 
                    catch (NumberFormatException e) {
                        // something is wrong in the xml file
                    }
                }
            }
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
        
        if (eName.equals("Service")) {
        }
        else if (eName.equals("Name")) {
            this.statname = currentchars;
            currentchars = null;
        }
    }
    public final void characters(final char[] buf, final int offset, final int len) throws SAXException {
        if (currentchars != null) {
            String s = new String(buf, offset, len);
            currentchars += s;
        }
    }
    private StatisticsCollection findStatisticsCollection(final String orgname) {
        for (int i = 0; i < this.orgstats.size(); i++) {
            StatisticsCollection col = (StatisticsCollection) this.orgstats.get(i);
            if (col.getOrgName().equals(orgname)) return col;
        }
        StatisticsCollection col = new StatisticsCollection(orgname);
        this.orgstats.add(col);
        return col;
    }

    
    
    

}
