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

package no.feide.moria.webservices.v1_0;

import java.rmi.RemoteException;

/**
 * @author Bj�rn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public class Authentication implements AuthenticationIF {

    /**
     * @see no.feide.login.moria.v1_0.Authentication.AuthenticationIF#initiateAuthentication(java.lang.String[],
     *      java.lang.String, java.lang.String, boolean)
     */
    public String initiateAuthentication(String[] attributes, String returnURLPrefix, String returnURLPostfix,
            boolean forceInteractiveAuthentication) throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see no.feide.login.moria.v1_0.Authentication.AuthenticationIF#directNonInteractiveAuthentication(java.lang.String[],
     *      java.lang.String, java.lang.String)
     */
    public String directNonInteractiveAuthentication(String[] attributes, String username, String password) throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see no.feide.login.moria.v1_0.Authentication.AuthenticationIF#getUserAttributes(java.lang.String)
     */
    public Attribute[] getUserAttributes(String ticketId) throws RemoteException {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see no.feide.login.moria.v1_0.Authentication.AuthenticationIF#verifyUserExistence(java.lang.String)
     */
    public boolean verifyUserExistence(String username) throws RemoteException {
        // TODO Auto-generated method stub
        return false;
    }
}