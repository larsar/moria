/*
 * Copyright (c) 2004 FEIDE
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

package no.feide.moria.authorization;

import org.jdom.Element;

/**
 * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
 * @version $Revision$
 */
public class AuthorizationManager {

	AuthorizationAttribute parseAuthAttrElement(Element element) throws IllegalConfigException {
		String name = null, secLevel = null, allowSSOStr = null;

		if (element.getAttribute("name") != null)
			name = element.getAttribute("name").getValue();

		if (element.getAttribute("sso") == null) {
			throw new IllegalConfigException("allowSSO has to be set.");
		}
		else {
			allowSSOStr = element.getAttribute("sso").getValue();
			if (!(allowSSOStr.equals("true") || allowSSOStr.equals("false")))
				throw new IllegalConfigException("allowSSO has to be 'true' or 'false'");
		}

		if (element.getAttribute("secLevel") != null)
			secLevel = element.getAttribute("secLevel").getValue();

		try {
			return new AuthorizationAttribute(name, new Boolean(allowSSOStr).booleanValue(), secLevel);
		} catch (IllegalArgumentException e) {
			throw new IllegalConfigException("Illegal attributes: " + e.getMessage());
		} 
	}
}
