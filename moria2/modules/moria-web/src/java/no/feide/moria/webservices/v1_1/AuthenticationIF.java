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

package no.feide.moria.webservices.v1_1;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author Bjørn Ola Smievoll &lt;b.o.smievoll@conduct.no&gt;
 * @version $Revision$
 */
public interface AuthenticationIF extends Remote {

    public String initiateAuthentication(String[] attributes, String returnURLPrefix, String returnURLPostfix,
            boolean forceInteractiveAuthentication) throws RemoteException;

    public Attribute[] directNonInteractiveAuthentication(String[] attributes, String username, String password) throws RemoteException;
    
    public Attribute[] proxyAuthentication(String[] attributes, String proxyTicket) throws RemoteException;

    public String getProxyTicket(String ticketGrantingTicket, String servicePrincipal) throws RemoteException;

    public Attribute[] getUserAttributes(String serviceTicket) throws RemoteException;

    public Attribute[] getGroupAttributes(String groupname) throws RemoteException;

    public boolean verifyUserExistence(String username) throws RemoteException;

    public boolean verifyGroupExistence(String groupname) throws RemoteException;

    public boolean verifyUserMemberOfGroup(String username, String groupname) throws RemoteException;
}
