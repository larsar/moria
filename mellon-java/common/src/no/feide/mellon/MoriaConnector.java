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

/**
 * A Moria connector is required for each SOAP implementation. This class should be
 * subclassed for every SOAP implementation and the subclass should convert SOAP
 * implementation specific attributes to general Moria attributes.
 * 
 * @author Lars Preben S. Arnesen
 */
public abstract class MoriaConnector implements MoriaConnectorIF {
	
	/** 
	 * Request an authentication session from Moria. A URL is returned from 
	 * Moria and the user should be redirectet there to perform the
	 * authentication.
	 * @param attributes An array of attributes to fetch from Moria (strings).
	 * @param urlPrefix The first part of the URL to the web service
	 * @param urlPostfix The last part of the URL (moria ID in between)
	 * @param denySso Forces Moria to to a full user authentication
	 * @return url The URL that the user should be redirected to for authentication
	 */
	public abstract String requestSession(String[] attributes, String urlPrefix, String urlPostfix, 
								 boolean denySso) throws RemoteException;
	
	/**
	 * Return attributes retrieved from Moria. The attributes are converted
	 * from a SOAP specific class to a universal MoriaUserAttribute class.
	 * @param ticket The ticket/sessionID that is retrieved from the users request
	 * @return moriaUserAttributes The attributes retrieved from Moria
	 */
	public abstract MoriaUserAttribute[] getAttributes(String ticket) throws RemoteException;

	
	/**
	 * Generate stubs and set username/password for the request.
	 * @param username The username for the Moria account
	 * @param password The password for the Moria account
	 */
	public abstract void connect(String username, String password) throws ServiceException;
}
