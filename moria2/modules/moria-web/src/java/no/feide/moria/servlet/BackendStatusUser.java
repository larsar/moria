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
 *
 * @author Eva Indal
 * @version $Revision$
 *
 * Stores attributes about a user.
 */

public class BackendStatusUser {

    private String name;
    private String password; 
    private String organization;
    private String contact;
    
    /**
     * Constructor.
     * 
     * @param name The user name
     * @param password The user password
     */
    public BackendStatusUser() {
       name = null;
       password = null;
       organization = null;
       contact = null;
    }
    
    /**
     * Set the user name.
     * @param name the user name
     */
    public void setName(final String name) {
        this.name = name;
    }
    
    /**
     * Set the user password
     * @param password the user password
     */
    public void setPassword(final String password) {
        this.password = password;
    }
    
    /**
     *  Set the organization
     * @param organization the organization
     */
    public void setOrganization(final String organization) {
        this.organization = organization;
    }
    
    /**
     * Set the contact
     * @param contact
     */
    public void setContact(final String contact) {
        this.contact = contact;
    }
    
    /**
     * Returns the user name
     * @return the user name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Returns the user password
     * @return the user password
     */
    public String getPassword() {
        return password;
    }
    
    /**
     * Returns the organization
     * @return the organization
     */
    public String getOrganization() {
        return organization;
    }
    
    /**
     * Returns the contact
     * @return the contact
     */
    public String getContact() {
        return contact;
    }
  }
