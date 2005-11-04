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

/**
 * This class sorts the months in the correct order.
 * 
 * @author Eva Indal
 * @version $Revision$
 *
 */
public class StatisticsMonth implements Comparable {
    private String monthname;
    private int monthnum;
    
    StatisticsMonth(final String name) {
        this.monthname = name;
        this.monthnum = findMonthNum(name);        
    }
    public int hashCode() {
        return this.monthname.hashCode();
    }

    public int compareTo(Object obj) {
        StatisticsMonth in = (StatisticsMonth) obj;
        return this.monthnum - in.monthnum;
    }
    public boolean equals(Object obj) {
        StatisticsMonth in = (StatisticsMonth) obj;
        return this.monthname.equals(in.monthname);
    }
    public int getMonthNum() {
        return this.monthnum;
    }
    public String getMonthName() {
        return this.monthname;
    }
    
    private static int findMonthNum(final String name) {
        if (name.equals("January")) return 1;
        if (name.equals("February")) return 2;
        if (name.equals("Mars")) return 3;
        if (name.equals("March")) return 3;
        if (name.equals("April")) return 4;
        if (name.equals("May")) return 5;
        if (name.equals("June")) return 6;
        if (name.equals("July")) return 7;
        if (name.equals("August")) return 8;
        if (name.equals("September")) return 9;
        if (name.equals("October")) return 10;
        if (name.equals("November")) return 11;
        if (name.equals("December")) return 12;
        return 0;
    }
}
