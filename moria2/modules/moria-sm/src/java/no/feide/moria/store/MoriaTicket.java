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

package no.feide.moria.store;

import java.io.Serializable;
import java.util.Date;

/**
 * This class represents the tickets used as identificators in Moria. Each
 * ticket has an unique key, a type and an assosiated service.
 * 
 * These attributes are used for the validation and authorization of incoming
 * requests.
 * 
 * @author Bj�rn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public final class MoriaTicket implements Serializable {

    /*
     * The ticket types, defined as constants.
     */
    public static final int LOGIN_TICKET = 1;
    public static final int SERVICE_TICKET = 2;
    public static final int SSO_TICKET = 3;
    public static final int PROXY_TICKET = 4;
    public static final int TICKET_GRANTING_TICKET = 5;

    private final String ticketId;
    private final int ticketType;
    private final String servicePrincipal;
    /* The time when this ticket expires, stored as seconds since epoch */
    private final long expiryTime;

    /**
     * Construct a new ticket with autogenerated ticket id.
     * 
     * @param ticketType the type of ticket
     * @param servicePrincipal the id of the service this ticket relates to
     * @param timeToLive the number of seconds this ticket should be considered
     *            valid
     * @throws IllegalArgumentException if any of the arguments are null
     * @throws InvalidTicketException if an illegal ticket type is specified
     */
    public MoriaTicket(int ticketType, String servicePrincipal, long timeToLive)
        throws IllegalArgumentException, InvalidTicketException {
        this(MoriaTicket.newId(), ticketType, servicePrincipal, timeToLive);
    }

    /**
     * Construct a new ticket with the given arguments.
     * 
     * @param ticketId a key identifying this ticket
     * @param ticketType the type of ticket
     * @param servicePrincipal the id of the service this ticket relates to
     * @param timeToLive the number of seconds this ticket should be considered
     *            valid
     * @throws IllegalArgumentException if any of the arguments are null
     * @throws InvalidTicketException if an illegl ticket type is spesified
     */
    public MoriaTicket(String ticketId, int ticketType, String servicePrincipal, long timeToLive)
        throws IllegalArgumentException, InvalidTicketException {

        /* Sanity checks on inputs before assignment */
        if (ticketId == null || ticketId.equals(""))
            throw new IllegalArgumentException("ticketId cannot be null or an empty string");
        this.ticketId = ticketId;

        if (ticketType < 1 || ticketType > 5)
            throw new InvalidTicketException("The given ticket type is not valid. See constants defined in this class");
        this.ticketType = ticketType;

        /* Undefined servicePrincipal is only allowed for SSO tickets */
        if (ticketType != SSO_TICKET && (servicePrincipal == null || servicePrincipal.equals("")))
            throw new IllegalArgumentException("servicePrincipal cannot be null or empty string");
        if (ticketType == SSO_TICKET && servicePrincipal != null)
            throw new IllegalArgumentException("servicePrincipal must be null when creating a SSO ticket");
        this.servicePrincipal = servicePrincipal;

        if (timeToLive < 0)
            throw new IllegalArgumentException("expiryTime must be a positive integer");
        this.expiryTime = new Date().getTime() + timeToLive * 1000;
    }

    /**
     * Get the ticket identificator for a object.
     * 
     * @return the key identifying the ticket
     */
    public String getTicketId() {
        return ticketId;
    }

    /**
     * Get the the value of the ticket type of this ticket.
     * 
     * @return the type of ticket
     */
    public int getTicketType() {
        return ticketType;
    }

    /**
     * Get the value of the service principal assosiated with this ticket.
     * 
     * @return the service principal
     */
    public String getServicePrincipal() {
        return servicePrincipal;
    }

    /**
     * Equality is defined on basis of the ticketId value. Same id, same ticket.
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     * @return true if equal
     */
    public boolean equals(Object o) {
        return (o instanceof MoriaTicket && ((MoriaTicket) o).getTicketId().equals(this.ticketId));
    }

    /**
     * The hashcode is the hashcode of the ticketId String
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return ticketId.hashCode();
    }

    /* TOOD: Do we want this method? */
    /**
     * Give a sensible output from toString().
     * 
     * @return a comma separated string of all the internal values
     */
    public String toString() {
        return "ticketId: "
            + ticketId
            + ", ticketType: "
            + ticketType
            + ", servicePrincipal: "
            + servicePrincipal;
    }

    /**
     * Creates an new key that can be used as an identificator for a ticket.
     * 
     * @return a new unique identificator
     */
    static String newId() {
        return RandomId.newId();
    }

    /**
     * Checks the tickets expiry time versus the current time
     * 
     * @return true if the ticket has exceeded its time to live
     */
    public boolean hasExpired() {
        long now = new Date().getTime();

        /*
         * If the expiry time is in the future return false, the ticket has not
         * expired
         */
        if (expiryTime > now)
            return false;

        return true;
    }
}
