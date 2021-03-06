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
package no.feide.mellon;

import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;

public interface MoriaConnectorIF {
	
	/** 
	 * @see no.feide.mellon.MoriaConnector#requestSession(java.lang.String[], java.lang.String, java.lang.String, boolean)
	 */
	public  String requestSession(String[] attributes, String urlPrefix, String urlPostfix, 
								 boolean denySso) throws RemoteException;
	
	/** 
	 * @see no.feide.mellon.MoriaConnector#getAttributes(java.lang.String)
	 */
	public MoriaUserAttribute[] getAttributes(String ticket) throws RemoteException;
	
	
	/** 
	 * @see no.feide.mellon.MoriaConnector#connect(java.lang.String, java.lang.String)
	 */
	public void connect(String username, String password) throws ServiceException;

}
