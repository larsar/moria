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

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

import no.feide.moria.log.MessageLogger;
import no.feide.moria.store.TicketTTLEvictionPolicy.RegionValue;

import org.jboss.cache.Fqn;
import org.jboss.cache.eviction.EvictionAlgorithm;
import org.jboss.cache.eviction.EvictionException;
import org.jboss.cache.eviction.EvictionPolicy;
import org.jboss.cache.eviction.Region;

import EDU.oswego.cs.dl.util.concurrent.ReentrantWriterPreferenceReadWriteLock;
import EDU.oswego.cs.dl.util.concurrent.Sync;
import EDU.oswego.cs.dl.util.concurrent.SyncList;
import EDU.oswego.cs.dl.util.concurrent.SyncMap;
import EDU.oswego.cs.dl.util.concurrent.WriterPreferenceReadWriteLock;

/**
 * This eviction algorithm expires cache elements after a fixed period, aka Time To Live.
 *
 * @author Bj&oslash;rn Ola Smievoll &lt;b.o.smievoll@conduct.no&gt;
 * @version $Revision$
 */
public class TicketTTLEvictionAlgorithm implements EvictionAlgorithm {

    /** */
    private final MessageLogger messageLogger = new MessageLogger(TicketTTLEvictionAlgorithm.class);

    /** */
    private SyncList nodeList;

    /** */
    private SyncMap nodeMap;

    /**
     *
     */
    public TicketTTLEvictionAlgorithm() {
        super();
        nodeList = new SyncList(new LinkedList(), new ReentrantWriterPreferenceReadWriteLock());
        nodeMap = new SyncMap(new HashMap(), new WriterPreferenceReadWriteLock());
    }

    /**
     * @see org.jboss.cache.eviction.EvictionAlgorithm#process(org.jboss.cache.eviction.Region)
     */
    public final void process(final Region region)
            throws EvictionException {

        while (true) {
            Fqn fqn = region.takeLastNode();

            /* Exit if queue is empty. */
            if (fqn == null)
                break;

            Integer event = region.takeLastEvent();

            /* Something's fishy, but we'll just ignore this node and keep on truckin. */
            if (event == null) {
                messageLogger.logWarn("Last event is null for FQN: " + fqn);
                continue;
            }

            if (event.equals(Region.ADD_EVENT)) {
                processAddedNodes(region, fqn);
            } else if (event.equals(Region.REMOVE_EVENT)) {
                processRemovedNodes(fqn);
            } else if (event.equals(Region.VISIT_EVENT)) {
                /* Node visits are of no interest. */
            } else {
                throw new RuntimeException("Illegal event type: " + event);
            }
        }

        /* Do the evicto-motion. */
        prune(region);
    }

    /**
     * @param region Region of tree.
     * @param fqn    Fully qualified name.
     */
    private void processAddedNodes(final Region region, final Fqn fqn) {
        TicketTTLEvictionPolicy policy = (TicketTTLEvictionPolicy) region.getEvictionPolicy();
        RegionValue regionValue = policy.getRegionValue(region.getFqn());

        /* Now pluss TTL for region gives the time when the ticket should be evicted. */
        Long nodeEvictionTime = new Long(new Date().getTime() + regionValue.timeToLive);
        NodeEntry node = new NodeEntry(nodeEvictionTime, fqn);

        nodeList.add(node);
        nodeMap.put(fqn, node);
    }

    /**
     * @param fqn Fully qualified name.
     */
    private void processRemovedNodes(final Fqn fqn) {
        NodeEntry node = (NodeEntry) nodeMap.remove(fqn);
        nodeList.remove(node);
    }

    /**
     * @param region Region of tree.
     * @throws  EvictionException
     *            If eviction is interrupted or fails.
     */
    private void prune(final Region region)
            throws EvictionException {
        /* Don't need the time each iteration. Rocket science this ain't. */
        long now = new Date().getTime();

        EvictionPolicy policy = region.getEvictionPolicy();

        Sync lock = nodeList.writerSync();
        try {
            lock.acquire();
            int counter = 0;
            for (ListIterator i = nodeList.listIterator(); i.hasNext();) {
                NodeEntry node = (NodeEntry) i.next();

                if (now > node.evictionTime.longValue()) {
                    nodeMap.remove(node.fqn);
                    i.remove();
                    counter++;
                    try {
                        policy.evict(node.fqn);
                    } catch (Exception e) {
                        messageLogger.logWarn("Eviction failed for FQN: " + node.fqn, e);
                        throw new EvictionException("Eviction failed for fqn: " + node.fqn, e);
                    }
                } else {
                    break;
                }
            }
            messageLogger.logDebug("Number of nodes evicted in " + region.getFqn() + ": " + counter);
        } catch (InterruptedException ie) {
            throw new EvictionException("Node list pruning interrupted", ie);
        } finally {
            lock.release();
        }
    }

    /**
     *
     */
    private class NodeEntry {

        private final Long evictionTime;

        private final Fqn fqn;

        public NodeEntry(final Long evictionTime, final Fqn fqn) {
            this.evictionTime = evictionTime;
            this.fqn = fqn;
        }
    }
}
