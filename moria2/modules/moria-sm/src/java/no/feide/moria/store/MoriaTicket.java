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
import java.util.Date;

/**
 * This class represents the tickets used as identifiers in Moria. Each
 * ticket has a unique key, a type and an associated service.
 *
 * These attributes are used for validation and authorization of incoming
 * requests.
 *
 * @author Bj&oslash;rn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
final class MoriaTicket implements Serializable {

    /** The unique identifier of this ticket. */
    private final String ticketId;

    /** The type of this ticket. */
    private final MoriaTicketType ticketType;

    /** The id of the service associated with this ticket. */
    private final String servicePrincipal;

    /** The time when this ticket expires, stored as milliseconds since epoch. */
    private final Long expiryTime;

    /** The data associated with this ticket. */
    private final MoriaStoreData data;

    /** The userorg associated with this ticket. */
    private String userorg;

    /**
     * Constructs a new ticket with auto-generated ticket id.
     *
     * @param ticketType
     *          the type of ticket
     * @param servicePrincipal
     *          the id of the service this ticket relates to
     * @param expiryTime
     *          the time when this ticket expires (in milliseconds since Epoch)
     * @param data
     *          the data object associated with this ticket. May be null
     * @param userorg
     *          the userorg associated with this ticket. Can be null if unknown.
     */
    MoriaTicket(final MoriaTicketType ticketType, final String servicePrincipal, final Long expiryTime, final MoriaStoreData data, final String userorg) {
        this(MoriaTicket.newId(), ticketType, servicePrincipal, expiryTime, data, userorg);
    }

    /**
     * Constructs a new ticket with the given arguments.
     *
     * @param ticketId
     *            A key identifying this ticket.
     * @param ticketType
     *            The type of ticket.
     * @param servicePrincipal
     *            The id of the service this ticket relates to. Must be null
     *            for SSO tickets, but not null or zero length for other
     *            ticket types.
     * @param expiryTime
     *          The time when this ticket expires (in milliseconds since Epoch).
     * @param data
     *          The data object associated with this ticket. May be null.
     *          Must be MoriaAuthnAttempt for login and service tickets, and
     *          CachedUserData for SSO, TGT and proxy tickets.
     * @param userorg
     *          The userorg associated with this ticket. Can be null if unknown.
     * @throws IllegalArgumentException
     *          If ticketId is null or zero length, if ticketType is null, if
     *          servicePrincipal or data is inappropriate for the ticketType
     *          or if expiryTime is in the past.
     */
    MoriaTicket(final String ticketId, final MoriaTicketType ticketType, final String servicePrincipal, final Long expiryTime,
                final MoriaStoreData data, final String userorg) {

        /* Sanity checks on inputs before assignment. */
        if (ticketId == null || ticketId.equals(""))
            throw new IllegalArgumentException("ticketId cannot be null or an empty string");
        this.ticketId = ticketId;

        if (ticketType == null)
            throw new IllegalArgumentException("ticketType cannot be null");
        this.ticketType = ticketType;

        /* Undefined servicePrincipal is only allowed for SSO tickets. */
        if (!ticketType.equals(MoriaTicketType.SSO_TICKET) && (servicePrincipal == null || servicePrincipal.equals("")))
            throw new IllegalArgumentException("servicePrincipal cannot be null or empty string");

        if (ticketType.equals(MoriaTicketType.SSO_TICKET) && servicePrincipal != null)
            throw new IllegalArgumentException("servicePrincipal must be null when creating an SSO ticket");
        this.servicePrincipal = servicePrincipal;

        /* 107291520000L equals Thu Jan  1 00:00:00 UTC 2004. */
        if (expiryTime == null || expiryTime.longValue() < 1072915200000L)
            throw new IllegalArgumentException("expiryTime must a time in the future");
        this.expiryTime = expiryTime;

        /* Verify that data type matches ticket type. */
        if (data != null) {
            if (ticketType.equals(MoriaTicketType.LOGIN_TICKET) || ticketType.equals(MoriaTicketType.SERVICE_TICKET)) {
                if (!(data instanceof MoriaAuthnAttempt)) {
                    throw new IllegalArgumentException("For login and service tickets, data must be MoriaAuthnAttempt");
                }
            } else {
                if (!(data instanceof CachedUserData)) {
                    throw new IllegalArgumentException("For sso, tg and proxy ticket data must be CachedUserData");
                }
            }
        }
        /* The data object may be null so we just assign. */
        this.data = data;
        this.userorg = userorg;
    }

    /**
     * Gets the ticket identifier of this ticket.
     *
     * @return The key identifying the ticket.
     */
    String getTicketId() {
        return ticketId;
    }

    /**
     * Gets the the ticket type of this ticket.
     *
     * @return The type of ticket.
     */
    MoriaTicketType getTicketType() {
        return ticketType;
    }

    /**
     * Gets the value of the service principal associated with this ticket.
     *
     * @return The service principal.
     */
    String getServicePrincipal() {
        return servicePrincipal;
    }

    /**
     * Checks the ticket's expiry time versus the current time.
     *
     * @return True if the ticket has exceeded its time to live.
     */
    boolean hasExpired() {
        final long now = new Date().getTime();

        /*
         * If the expiry time is in the future return false, the ticket has not
         * expired.
         */
        if (expiryTime.longValue() > now)
            return false;

        return true;
    }

    /**
     * Gets the the expiry time for this ticket.
     *
     * @return The expiry time in milliseconds since epoch.
     */
    Long getExpiryTime() {
        return expiryTime;
    }

    /**
     * Gets the data object of this ticket.
     *
     * @return An instance of MoriaStoreData, or null if no data
     *         object is associated with this ticket.
     */
    MoriaStoreData getData() {
        return data;
    }

    /**
     * Tests if ticket is equal to another ticket.
     * Equality is defined on basis of the ticketId value. Same id, same
     * ticket.
     *
     * @param object
     *          The object to compare with.
     * @return True if equal.
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(final Object object) {
        return (object instanceof MoriaTicket && ((MoriaTicket) object).getTicketId().equals(this.ticketId));
    }

    /**
     * Gets the hash code of the ticket.
     * The hash code is the hash code of the ticketId String.
     *
     * @return The hash code.
     *
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        return ticketId.hashCode();
    }

    /**
     * Gets a string representation of the ticket.
     *
     * @return A comma separated string of all the internal values.
     */
    public String toString() {
        return "ticketId: " + ticketId + ", ticketType: " + ticketType + ", servicePrincipal: " + servicePrincipal;
    }

    /**
     * Creates a new key that can be used as an identifier of the ticket.
     *
     * @return A new unique identifier.
     */
    static String newId() {
        return RandomId.newId();
    }

    /**
     * Returns the userorg associated with this ticket, or null if none.
     * @return The userorg.
     */
    public String getUserorg() {
        return userorg;
    }

    /**
     * Associates a userorg with this ticket.
     * @param org The userorg.
     */
    public void setUserorg(String org) {
        userorg = org;
    }

}
