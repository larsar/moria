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
    
    private Vector stats;
    private Vector allmonths;
    private StatisticsData currentStat;
    private String currentchars;
    private Vector ignorevector;
    
    /**
     * Constructor
     *
     */
    public StatisticsHandler()  {
        this.currentStat = null;
        this.stats = new Vector();
        this.allmonths = new Vector();
        this.currentchars = null;
        this.ignorevector = new Vector();
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
            currentStat = new StatisticsData();
        }
        else if (eName.equals("Name")) {
            currentchars = "";
        }
        else if (eName.equals("Month")) {
            if ((currentStat != null) && !this.shouldIgnore(currentStat.getName())) {
                final int n = attrs.getLength();
                final String monthname = attrs.getValue(new String("name"));
                final String countstring = attrs.getValue(new String("count"));
            
                if (monthname != null && countstring != null) {
                    try {
                        Integer tmp = new Integer(countstring);
                        currentStat.addMonth(monthname, tmp.intValue());
                        this.addUniqueMonth(monthname);
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
            if (!this.shouldIgnore(currentStat.getName())) {
                this.stats.add(currentStat);
            }
            this.currentStat = null;
        }
        else if (eName.equals("Name")) {
            if (currentStat != null) {
                currentStat.setName(currentchars);
            }
            currentchars = null;
        }
    }
    public final void characters(final char[] buf, final int offset, final int len) throws SAXException {
        if (currentchars != null) {
            String s = new String(buf, offset, len);
            currentchars += s;
        }
    }

    
    public int getNumMonths() {
        return this.allmonths.size();
    }
    
    public String getMonthName(final int idx) {
        return (String) this.allmonths.get(idx);
    }
    public int getNumStatisticsData() {
        return this.stats.size();
    }
    public StatisticsData getStatisticsData(final int idx) {
        return (StatisticsData) this.stats.get(idx);
    }
    
    /**
     * @param monthname
     * 
     * Adds a month to the list of unique months (if not already in list)
     */
    private void addUniqueMonth(final String monthname) {
        final int n = this.allmonths.size();
        for (int i = 0; i < n; i++) {
            String tmp = (String) this.allmonths.get(i);
            if (monthname.equals(tmp)) return;
        }
        this.allmonths.add(monthname);
    }
}
