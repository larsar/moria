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

package no.feide.moria.log;

import java.io.Serializable;

/**
 * This class represents access status type constants for the AccessLogger. It's an implementation of the "typesafe enum
 * pattern".
 *
 * @author Bjørn Ola Smievoll &lt;b.o.smievoll@conduct.no&gt;
 * @version $Revision$
 */
public final class AccessStatusType implements Serializable {

    /**
     * Description of status type.
     */
    private final String name;

    /**
     * Default private constructor.
     *
     * @param name The name of the status type. Used in the log and should be all caps.
     */
    private AccessStatusType(final String name) {
        this.name = name;
    }

    /**
     * Return string representation of object.
     *
     * @return name of object
     */
    public String toString() {
        return name;
    }

    /**
     * Access log type used to indicate that the user failed to login because of bad credentials.
     */
    public static final AccessStatusType BAD_USER_CREDENTIALS = new AccessStatusType("BAD USER CREDENTIALS");

    /**
     * Access log type used to indicate that the service failed to authenticate itself.
     */
    public static final AccessStatusType BAD_SERVICE_CREDENTIALS = new AccessStatusType("BAD SERVICE CREDENTIALS");

    /**
     * Access log type used to indicate that the service requests illegal operations.
     */
    public static final AccessStatusType OPERATIONS_NOT_PERMITTED = new AccessStatusType("OPERATIONS NOT PERMITTED");

    /**
     * Access log type used to indicate that the service requests illegal attributes.
     */
    public static final AccessStatusType ACCESS_DENIED_INITIATE_AUTH = new AccessStatusType(
            "ACCESS DENIED INITIATE AUTH");

    /**
     * Access log type used to indicate that the service requests illegal attributes.
     */
    public static final AccessStatusType ACCESS_DENIED_DIRECT_AUTH = new AccessStatusType("ACCESS DENIED DIRECT AUTH");

    /**
     * Access log type used to indicate that the service requests illegal attributes.
     */
    public static final AccessStatusType ACCESS_DENIED_VERIFY_USER_EXISTENCE = new AccessStatusType(
            "ACCESS DENIED VERIFY USER EXISTENCE");

    /**
     * Access log type used to indicate that the service requests illegal proxy authentication.
     */
    public static final AccessStatusType ACCESS_DENIED_PROXY_AUTH = new AccessStatusType(
            "ACCESS DENIED PROXY AUTH");
}