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

package no.feide.moria.store;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class represents the different types of tickets used in Moria.
 * It's an implementation of the "typesafe enum pattern".
 *
 * @author Bjørn Ola Smievoll &lt;b.o.smievoll@conduct.no&gt;
 * @version $Revision$
 */
public final class MoriaTicketType implements Serializable {

    /** Description of ticket type. */
    private final String name;

    /** Ordinal of next ticket type to be created. */
    private static int nextOrdinal = 0;

    /** Assigns an ordinal to this ticket type. */
    private final int ordinal = nextOrdinal++;

    /**
     * Default private constructor.
     *
     * @param name
     *          The name of the ticket type.
     */
    private MoriaTicketType(final String name) {
        this.name = name;
    }

    /**
     * Returns string representation of object.
     *
     * @return Name of object.
     */
    public String toString() {
        return name;
    }

    /** Initially issued to the requesting service for use by the client. */
    public static final MoriaTicketType LOGIN_TICKET = new MoriaTicketType("Login Ticket");

    /** For use by the service upon retrieval of user data. */
    public static final MoriaTicketType SERVICE_TICKET = new MoriaTicketType("Service Ticket");

    /** To be set as a cookie in the client browser for later re-authentication. */
    public static final MoriaTicketType SSO_TICKET = new MoriaTicketType("Single Sign-On Ticket");

    /** For use by services in proxy authentication scheme. */
    public static final MoriaTicketType TICKET_GRANTING_TICKET = new MoriaTicketType("Ticket Granting Ticket");

    /** Issued to a service when a valid TGT is presented. */
    public static final MoriaTicketType PROXY_TICKET = new MoriaTicketType("Proxy Ticket");

    /**
     * Static array that holds all objects. Used by readResolve() to
     * return correct object after de-serialization.
     */
    private static final MoriaTicketType[] PRIVATE_TICKET_TYPES = {LOGIN_TICKET, SERVICE_TICKET, SSO_TICKET,
            TICKET_GRANTING_TICKET, PROXY_TICKET};

    /** Public representation of the ticket types. */
    public static final List TICKET_TYPES = Collections.unmodifiableList(Arrays.asList(PRIVATE_TICKET_TYPES));

    /**
     * Returns the local classloader representation of the object.
     * Needed for serialization to work.
     *
     * @return The local classloader representation of the object.
     */
    Object readResolve() {
        return PRIVATE_TICKET_TYPES[ordinal];
    }
}
