/*
 * Copyright (c) 2004 UNINETT FAS A/S
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
 */
package no.feide.moria.directory.index;

/**
 * Manages the internal representation of affiliations; separate sets for user
 * and group affiliations.
 * @author Cato Olsen
 */
public class RegisterManager {

    private static RegisterManager me = null;

    private static Register users;

    private static Register groups;

    private static Register affiliations;


    /**
     * Private constructor. Creates the internal lists for user and group
     * affiliations.
     */
    private RegisterManager() {
        users = new Register();
        groups = new Register();
        affiliations = new Register();
    }


    /**
     * Provides access to the affiliation manager.
     * @return The running instance of the affiliation manager.
     */
    public static synchronized RegisterManager getInstance() {
        if (me == null)
            me = new RegisterManager();
        return me;
    }


    /**
     * Get the list of user affiliations.
     * @return A reference to the user affiliation object.
     */
    public static Register getusers() {
        return users;
    }


    /**
     * Get the list of group affiliations.
     * @return A reference to the group affiliation object.
     */
    public static Register getgroups() {
        return groups;
    }


    /**
     * @return
     */
    public static Register getAffiliations() {
        return affiliations;
    }

}
