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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.Vector;

import no.feide.moria.log.MessageLogger;

import org.jboss.cache.CacheException;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;
import org.jboss.cache.TreeCache;
import org.jboss.cache.eviction.EvictionAlgorithm;
import org.jboss.cache.eviction.EvictionTimerTask;
import org.jboss.cache.eviction.LRUPolicy;
import org.jboss.cache.eviction.Region;
import org.jboss.cache.eviction.RegionManager;
import org.jboss.cache.eviction.RegionNameConflictException;
import org.jboss.cache.lock.LockingException;
import org.jboss.cache.lock.TimeoutException;
import org.jgroups.MergeView;
import org.jgroups.View;
import org.jgroups.stack.IpAddress;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * This eviction policy evicts tickets after a fixed period, aka Time To Live.
 *
 * @author Bj&oslash;rn Ola Smievoll &lt;b.o.smievoll@conduct.no&gt;
 * @version $Revision$
 */

public final class TicketTTLEvictionPolicy extends LRUPolicy {

    /** The logger used by this class. */
    private MessageLogger messageLogger = new MessageLogger(TicketTTLEvictionPolicy.class);

    /** Manages the regions for each ticket type. */
    private RegionManager regionManager;

    /**  The root node. */
    private static final Fqn ROOT = new Fqn("");

    /** Default interval value for running the evictions. */
    private static final int WAKEUP_INTERVAL_DEFAULT = 5;

    /** Default value for max nodes in each region. */
    private static final int MAX_NODES_DEFAULT = 0x186a0;

    /** Constant representing the region name key. */
    private static final String REGION_NAME = "region";

    /** Constant representing the attribute name key. */
    private static final String ATTRIBUTE_NAME = "attribute";

    /** Constant representing the name attribute key. */
    private static final String NAME_ATTRIBUTE_NAME = "name";

    /** Constant representing the wakeup interval key. */
    private static final String WAKEUP_INTERVAL_NAME = "wakeUpIntervalSeconds";

    /** Constant representing the max nodes key. */
    private static final String MAX_NODES_NAME = "maxNodes";

    /** Constant representing the ttl attribute key. */
    private static final String TTL_ATTRIBUTE_NAME = "timeToLive";

    /** Constant holding default error message. */
    private static final String OPERATION_FAILED_MESSAGE = "Operation failed";

    /** Contains the different regions registered for this policy. */
    private RegionValue[] regionValues;

    /** The interval for the eviction threads to run. */
    private int wakeUpIntervalSeconds;

    /** Maximal number of nodes in a region. Not really used, should be set high. */
    private int maxNodes;

    /** The timer responsible for scheduling the eviction threads. */
    private Timer evictionTimer;

    /** The cache the evictions are done on. */
    private TreeCache cache;

    /**
     * Creates a new instance.
     */
    public TicketTTLEvictionPolicy() {
    }

    /**
     * @see org.jboss.cache.eviction.EvictionPolicy#configure(org.jboss.cache.TreeCache)
     */
    public void configure(final TreeCache cache) {
        parseConfig(cache.getEvictionPolicyConfig());
        regionManager = new RegionManager(this);
        this.cache = cache;

        for (int i = 0; i < regionValues.length; i++) {
            EvictionAlgorithm algorithm = new TicketTTLEvictionAlgorithm();

            try {
                Region region = regionManager.createRegion(ROOT.toString() + regionValues[i].regionName, algorithm);
                region.setMaxNodes(maxNodes);
                region.setTimeToLiveSeconds(wakeUpIntervalSeconds);
            } catch (RegionNameConflictException e) {
                messageLogger.logWarn("Name conflict in region naming.", e);
                throw new EvictionConfigurationException("Unable to create region " + regionValues[i].regionName, e);
            }
        }

    }

    /**
     * @see org.jboss.cache.eviction.EvictionPolicy#getRegions()
     */
    public Region[] getRegions() {
        return regionManager.getRegions();
    }

    /**
     * @see org.jboss.cache.eviction.EvictionPolicy#evict(org.jboss.cache.Fqn)
     */
    public void evict(final Fqn fqn)
            throws Exception {
        cache.evict(fqn);
    }

    /**
     * @see org.jboss.cache.eviction.EvictionPolicy#getChildrenNames(org.jboss.cache.Fqn)
     */
    public Set getChildrenNames(final Fqn fqn) {
        /* Here the API forces us to use RuntimeException. */
        try {
            return cache.getChildrenNames(fqn);
        } catch (LockingException le) {
            messageLogger.logWarn(OPERATION_FAILED_MESSAGE, le);
            throw new RuntimeException(le);
        } catch (TimeoutException te) {
            messageLogger.logWarn(OPERATION_FAILED_MESSAGE, te);
            throw new RuntimeException(te);
        } catch (CacheException ce) {
            messageLogger.logWarn(OPERATION_FAILED_MESSAGE, ce);
            throw new RuntimeException(ce);
        }
    }

    /**
     * @see org.jboss.cache.eviction.EvictionPolicy#hasChild(org.jboss.cache.Fqn)
     */
    public boolean hasChild(final Fqn fqn) {
        return cache.hasChild(fqn);
    }

    /**
     * @see org.jboss.cache.eviction.EvictionPolicy#getCacheData(org.jboss.cache.Fqn, java.lang.Object)
     */
    public Object getCacheData(final Fqn fqn, final Object key) {
        /* Here the API forces us to use RuntimeException. */
        try {
            return cache.get(fqn, key);
        } catch (LockingException le) {
            messageLogger.logWarn(OPERATION_FAILED_MESSAGE, le);
            throw new RuntimeException(le);
        } catch (TimeoutException te) {
            messageLogger.logWarn(OPERATION_FAILED_MESSAGE, te);
            throw new RuntimeException(te);
        } catch (CacheException ce) {
            messageLogger.logWarn(OPERATION_FAILED_MESSAGE, ce);
            throw new RuntimeException(ce);
        }
    }

    /**
     * @see org.jboss.cache.eviction.EvictionPolicy#getWakeupIntervalSeconds()
     */
    public int getWakeupIntervalSeconds() {
        return wakeUpIntervalSeconds;
    }

    /**
     * @see org.jboss.cache.TreeCacheListener#nodeAdded(org.jboss.cache.Fqn)
     */
    public void nodeCreated(final Fqn fqn) {
        if (fqn.equals(ROOT))
            return;

        if (regionManager != null) {
            Region region = regionManager.getRegion(fqn.toString());

            if (region != null) {
                region.setAddedNode(fqn);
                messageLogger.logDebug("Node created: " + fqn);
            } else {
                messageLogger.logInfo("No region returned for created node: " + fqn);
            }
        } else {
            messageLogger.logWarn("regionManager is null");
        }
    }

    /**
     * @see org.jboss.cache.TreeCacheListener#nodeLoaded(org.jboss.cache.Fqn)
     */
    public void nodeLoaded(final Fqn fqn) {
        if (fqn.equals(ROOT))
            return;

        if (regionManager != null) {
            Region region = regionManager.getRegion(fqn.toString());

            if (region != null) {
                region.setAddedNode(fqn);
                messageLogger.logDebug("Node loaded: " + fqn);
            } else {
                messageLogger.logInfo("No region returned for loaded node: " + fqn);
            }
        } else {
            messageLogger.logWarn("regionManager is null");
        }
    }

    /**
     * @see org.jboss.cache.TreeCacheListener#nodeRemoved(org.jboss.cache.Fqn)
     */
    public void nodeRemoved(final Fqn fqn) {
        if (fqn.equals(ROOT))
            return;

        if (regionManager != null) {
            Region region = regionManager.getRegion(fqn.toString());

            if (region != null) {
                region.setRemovedNode(fqn);
                messageLogger.logDebug("Listener got node removed: " + fqn);
            } else {
                messageLogger.logInfo("No region returned for removed node: " + fqn);
            }
        } else {
            messageLogger.logWarn("regionManager is null");
        }
    }

    /**
     * @see org.jboss.cache.TreeCacheListener#nodeEvicted(org.jboss.cache.Fqn)
     */
    public void nodeEvicted(final Fqn fqn) {
        if (fqn.equals(ROOT))
            return;

        messageLogger.logDebug("Listener got node evicted: " + fqn);
    }

    /**
     * @see org.jboss.cache.TreeCacheListener#nodeModified(org.jboss.cache.Fqn)
     */
    public void nodeModified(final Fqn fqn) {
        if (fqn.equals(ROOT))
            return;

        if (regionManager != null) {
            Region region = regionManager.getRegion(fqn.toString());

            if (region != null) {
                region.setVisitedNode(fqn);
                messageLogger.logDebug("Listener got node modified: " + fqn);
            } else {
                messageLogger.logInfo("No region returned for modified node: " + fqn);
            }
        } else {
            messageLogger.logWarn("regionManager is null");
        }
    }

    /**
     * @see org.jboss.cache.TreeCacheListener#nodeVisited(org.jboss.cache.Fqn)
     */
    public void nodeVisited(final Fqn fqn) {
        if (fqn.equals(ROOT))
            return;

        if (regionManager != null) {
            Region region = regionManager.getRegion(fqn.toString());

            if (region != null) {
                region.setVisitedNode(fqn);
                messageLogger.logDebug("Listener got node visited: " + fqn);
            } else {
                messageLogger.logInfo("No region returned for visited node: " + fqn);
            }
        } else {
            messageLogger.logWarn("regionManager is null");
        }
    }

    /**
     * @see org.jboss.cache.TreeCacheListener#cacheStarted(org.jboss.cache.TreeCache)
     */
    public void cacheStarted(final TreeCache cache) {
        messageLogger.logInfo("Starting eviction policy using provider: " + this.getClass().getName());

        if (!this.cache.equals(cache)) {
            messageLogger.logWarn("Cache instance given on start not equal to configured cache.");
        }

        evictionTimer = new Timer();
        evictionTimer.schedule(new EvictionTimerTask(this), 1000, wakeUpIntervalSeconds * 1000);
    }

    /**
     * @see org.jboss.cache.TreeCacheListener#cacheStopped(org.jboss.cache.TreeCache)
     */
    public void cacheStopped(final TreeCache cache) {

        if (!this.cache.equals(cache)) {
            messageLogger.logWarn("Cache instance given on stop not equal to configured cache.");
        }

        if (evictionTimer != null) {
            evictionTimer.cancel();
            messageLogger.logInfo("Stopped eviction policy timer.");
        } else {
            messageLogger.logInfo("Cache stop called with uninitialized eviction timer.");
        }
    }

    /**
     * @see org.jboss.cache.TreeCacheListener#viewChange(org.jgroups.View)
     */
    public void viewChange(final View view) {
        messageLogger.logDebug("Listener got a view change.");

        Vector subGroupMembers = null;
        final Object myAddress = cache.getLocalAddress();

        /* Exit unless this is a merge. */
        if (view instanceof MergeView) {
            messageLogger.logDebug("View is a MergeView!");
            MergeView mergeView = (MergeView) view;

            Vector subgroups = mergeView.getSubgroups();

            for (int v = 0; v < subgroups.size(); v++) {
                subGroupMembers = ((View) subgroups.get(v)).getMembers();
                /* We run until we find a the subgroup we were coordinating. */
                if (subGroupMembers.get(0).equals(myAddress)) {
                    break;
                } else {
                    subGroupMembers = null;
                }
            }
        } else {
            return;
        }

        /* If we're one of the old coords we get workin'. Basically we
         * recommit all nodes that belonged to us. */
        if (subGroupMembers != null) {
            messageLogger.logDebug("This was previously a coordinator.");

            /* Identify our group members. */
            ArrayList memberIds = new ArrayList();

            for (Iterator i = subGroupMembers.iterator(); i.hasNext();) {
                /* Create ticket id prefix. */
                String nodeId;

                Object memberAddress = i.next();

                if (memberAddress instanceof IpAddress) {
                    IpAddress ipAddress = (IpAddress) memberAddress;
                    InetAddress inetAddress = ipAddress.getIpAddress();
                    nodeId = inetAddress.getHostAddress() + ":" + ipAddress.getPort();
                } else {
                    messageLogger.logWarn("Skipping a group member. Address not an IpAddress: " + memberAddress);
                    continue;
                }

                memberIds.add(RandomId.pseudoBase64Encode(RandomId.nodeIdToByteArray(nodeId)));
            }

            /* Get data. */

            List branches = MoriaTicketType.TICKET_TYPES;

            int ticketCount = 0;

            for (Iterator i = branches.iterator(); i.hasNext();) {

                Node branch;
                Map tickets;

                try {
                    branch = cache.get(new Fqn(i.next()));
                } catch (LockingException le) {
                    messageLogger.logCritical("Locking cache failed.", le);
                    continue;
                } catch (TimeoutException te) {
                    messageLogger.logCritical("Got timeout from cache.", te);
                    continue;
                } catch (CacheException ce) {
                    messageLogger.logCritical("Got exception from cache.", ce);
                    continue;
                }

                if (branch != null) {
                    tickets = branch.getChildren();
                } else {
                    continue;
                }

                /* In case a null value is returned instead of a empty Map. */
                if (tickets == null)
                    continue;

                for (Iterator j = tickets.keySet().iterator(); j.hasNext();) {
                    Object ticketId = j.next();
                    String nodeId = ticketId.toString().substring(0, 7);

                    for (Iterator k = memberIds.iterator(); k.hasNext();) {
                        String memberId = (String) k.next();

                        if (nodeId.equals(memberId)) {
                            Node ticket = (Node) tickets.get(ticketId);

                            if (ticket != null) {
                                try {
                                    cache.put(ticket.getFqn(), ticket.getData());
                                    ticketCount++;
                                } catch (RuntimeException re) {
                                    /* We do this to weed out RuntimeExceptions from the catch below. */
                                    throw re;
                                } catch (Exception e) {
                                    messageLogger.logCritical("Insertion into store failed. [" + ticket.getName() + "]", e);
                                    continue;
                                }

                                /* Throttle redistribution somewhat. */
                                try {
                                    Thread.sleep(30);
                                } catch (InterruptedException ie) {
                                    messageLogger.logWarn("Thread sleep interrupted", ie);
                                }
                            }
                        }
                    }
                }
            }
            messageLogger.logDebug("Number of redistributed tickets: " + ticketCount);
        } else {
            messageLogger.logDebug("Got a MergeView, but this wasn't a coordinator before.");
            return;
        }
    }

    /**
     * Parses the config retrieved from TreeCache.getEvictionPolicyConfig(). Populates
     * the regionValues array.
     *
     * @param config configuration for this eviction policy
     */
    synchronized void parseConfig(final Element config) {

        if (config == null)
            throw new IllegalArgumentException("config cannot be null");

        /* Get wakeup interval from config. */
        wakeUpIntervalSeconds = Integer.parseInt(getAttribute(config, WAKEUP_INTERVAL_NAME));

        if (wakeUpIntervalSeconds < 1) {
            wakeUpIntervalSeconds = 5;
            messageLogger.logWarn("Illegal value for config parameter wakeUpIntervalSeconds. Using default: "
                                  + wakeUpIntervalSeconds);
        }

        /* Get max nodes from config. */
        maxNodes = Integer.parseInt(getAttribute(config, MAX_NODES_NAME));

        if (maxNodes < 1) {
            maxNodes = MAX_NODES_DEFAULT;
            messageLogger.logWarn("Illegal value for config parameter maxNodes. Using default: " + maxNodes);
        }

        /* Get and create regions. */
        NodeList regions = config.getElementsByTagName(REGION_NAME);
        regionValues = new RegionValue[regions.getLength()];

        for (int i = 0; i < regionValues.length; i++) {
            RegionValue regionValue = new RegionValue();
            org.w3c.dom.Node node = regions.item(i);

            if (node.getNodeType() != 1)
                continue;

            Element region = (Element) node;
            regionValue.regionName = region.getAttribute(NAME_ATTRIBUTE_NAME);
            regionValue.timeToLive = Long.parseLong(getAttribute(region, TTL_ATTRIBUTE_NAME)) * 1000L;

            if (regionValue.regionName == null || regionValue.regionName.equals(""))
                throw new EvictionConfigurationException("Illegal value for region name: " + regionValue.regionName);

            if (regionValue.timeToLive < 1000L)
                throw new EvictionConfigurationException("Illegal value for time to live: " + regionValue.timeToLive);

            regionValues[i] = regionValue;
            messageLogger.logDebug("Added region: " + regionValue);
        }
    }

    /**
     * Retrieves the value of the first occurance of the given
     * attribute in the given element.
     *
     * @param element the element containing an named attribute.
     * @param attributeName the name of the requested attribute.
     * @return the value of the requested attribute, null if no attribute exists in element.
     */
    String getAttribute(final Element element, final String attributeName) {
        NodeList nodes = element.getElementsByTagName(ATTRIBUTE_NAME);

        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);

            if (node.getNodeType() != org.w3c.dom.Node.ELEMENT_NODE)
                continue;

            Element subElement = (Element) node;
            String name = subElement.getAttribute(NAME_ATTRIBUTE_NAME);

            if (name.equals(attributeName))
                return getElementContent(subElement, true);
        }

        return null;
    }

    /**
     * Retrieves the textual content of an xml element.
     *
     * @param element The element containing the data to be retrieved.
     * @param trim Whether or not to trim whitespace before returing.
     * @return A concatenated string of the text of the child nodes of the element.
     */
    String getElementContent(final Element element, boolean trim) {
        NodeList nodes = element.getChildNodes();
        String attributeText = "";

        for (int i = 0; i < nodes.getLength(); i++) {
            org.w3c.dom.Node node = nodes.item(i);
            if (node instanceof Text)
                attributeText = attributeText + ((Text) node).getData();
        }

        if (trim)
            attributeText = attributeText.trim();

        return attributeText;
    }

    /**
     * Gets the region values.
     *
     * @return Array containing the region values.
     */
    RegionValue[] getRegionValues() {
        return regionValues;
    }

    /**
     * Gets a region value.
     *
     * @param fqn Fully quailified name.
     * @return A region value.
     */
    final RegionValue getRegionValue(final String fqn) {

        for (int i = 0; i < regionValues.length; i++) {
            if (fqn.startsWith(ROOT.toString() + regionValues[i].regionName))
                return regionValues[i];
        }

        return null;
    }

    /**
     * Simple data container for region values.
     */
    static final class RegionValue {

        /** Region name. */
        private String regionName;

        /** Time to live. */
        private long timeToLive;

        /**
         * Returns a string representation.
         * @return The string representation.
         */
        public String toString() {
            return "[" + regionName + ", " + timeToLive + "]";
        }

        /**
         * Gets region name.
         * @return The region name.
         */
        String getRegionName() {
            return regionName;
        }

        /**
         * Gets time to live.
         * @return Time to live.
         */
        long getTimeToLive() {
            return timeToLive;
        }
    }
}