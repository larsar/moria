/**
 * Copyright (C) 2003 FEIDE
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package no.feide.moria.utils;


/**
 * Common utilities
 */
public class URLValidator {


    /**
     * Validate URL. Uses blacklist to indicate whether the URL should
     * be accepted or not.
     */
    public static boolean isLegal(String url) {
        
        String[] illegal = new String[]{
            "\n",
            "\r"
        };

        if (url.indexOf("http://") != 0 && url.indexOf("https://") != 0)
            return false;

        for (int i = 0; i < illegal.length; i++) {
            if (url.indexOf(illegal[i]) != -1) {
                System.out.println("Contains: "+illegal[i]);
                return false;
            }
        }

        return true;
    }
}
