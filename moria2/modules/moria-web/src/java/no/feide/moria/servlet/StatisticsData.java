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
import java.util.Vector;
import java.util.HashMap;

/**
 * @author Eva Indal
 * @version $Revision$
 *
 */
public class StatisticsData {
    
    private String name;
    private HashMap monthdata;
    
    //Constructor
    public StatisticsData () {
        name = null;
        monthdata = new HashMap();
    }
    
    public void setName(final String name) {
        this.name = name;
    }
    
    public void addMonth(final String month, final int count) {
        this.monthdata.put(month, new Integer(count));
    }
        
    public String getName() {
        return name;
    }
    int getCount(final String month) {
        final Integer val = (Integer) this.monthdata.get(month);
        if (val != null) return val.intValue();
        return 0;

    }

}
