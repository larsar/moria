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

import java.util.Set;
import java.util.Timer;

import no.feide.moria.log.MessageLogger;

import org.jboss.cache.Fqn;
import org.jboss.cache.TreeCache;
import org.jboss.cache.TreeCacheListener;
import org.jboss.cache.eviction.EvictionAlgorithm;
import org.jboss.cache.eviction.EvictionPolicy;
import org.jboss.cache.eviction.EvictionTimerTask;
import org.jboss.cache.eviction.Region;
import org.jboss.cache.eviction.RegionManager;
import org.jboss.cache.eviction.RegionNameConflictException;
import org.jboss.cache.lock.LockingException;
import org.jboss.cache.lock.TimeoutException;
import org.jgroups.View;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 * This eviction policy evicts tickets after a fixed period, aka Time To Live.
 *
 * @author Bj&oslash;rn Ola Smievoll &lt;b.o.smievoll@conduct.no&gt;
 * @version $Revision$
 */

public class TicketTTLEvictionPolicy implements EvictionPolicy, TreeCacheListener {

    /** The logger used by this class. */
    private MessageLogger messageLogger;

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

    /** Contains the different regions registered for this policy. */
    private RegionValue regionValues[];

    /** The interval for the eviction threads to run. */
    private int wakeUpIntervalSeconds;

    /** Maximal number of nodes in a region. Not really used, should be set high. */
    private int maxNodes;

    /** The timer responsible for scheduling the eviction threads. */
    private Timer evictionTimer;

    /** The cache the evictions are done on. */
    private TreeCache cache;

    /**
     * Create new instance.
     */
    public TicketTTLEvictionPolicy() {
        messageLogger = new MessageLogger(TicketTTLEvictionPolicy.class);
        regionManager = null;
    }

    /**
     * @see org.jboss.cache.eviction.EvictionPolicy#getRegions()
     */
    public final Region[] getRegions() {
        return regionManager.getRegions();
    }

    /**
     * @see org.jboss.cache.eviction.EvictionPolicy#evict(org.jboss.cache.Fqn)
     */
    public final void evict(final Fqn fqn)
            throws Exception {
        cache.evict(fqn);
    }

    /**
     * @see org.jboss.cache.eviction.EvictionPolicy#getChildrenNames(org.jboss.cache.Fqn)
     */
    public final Set getChildrenNames(final Fqn fqn) {
        try {
            return cache.getChildrenNames(fqn);
        } catch (LockingException le) {
            throw new RuntimeException(le);
        } catch (TimeoutException te) {
            throw new RuntimeException(te);
        }
    }

    /**
     * @see org.jboss.cache.eviction.EvictionPolicy#hasChild(org.jboss.cache.Fqn)
     */
    public final boolean hasChild(final Fqn fqn) {
        return cache.hasChild(fqn);
    }

    /**
     * @see org.jboss.cache.eviction.EvictionPolicy#getCacheData(org.jboss.cache.Fqn, java.lang.Object)
     */
    public final Object getCacheData(final Fqn fqn, final Object key) {
        try {
            return cache.get(fqn, key);
        } catch (LockingException le) {
            throw new RuntimeException(le);
        } catch (TimeoutException te) {
            throw new RuntimeException(te);
        }
    }

    /**
     * @see org.jboss.cache.eviction.EvictionPolicy#getWakeupIntervalSeconds()
     */
    public final int getWakeupIntervalSeconds() {
        return wakeUpIntervalSeconds;
    }

    /**
     * @see org.jboss.cache.TreeCacheListener#nodeAdded(org.jboss.cache.Fqn)
     */
    public final void nodeAdded(final Fqn fqn) {
        if (!fqn.equals(ROOT)) {
            Region region = regionManager.getRegion(fqn.toString());
            if (region != null) {
                region.setAddedNode(fqn);
            } else {
                messageLogger.logDebug("No region returned for FQN: " + fqn);
            }
        }
    }

    /**
     * @see org.jboss.cache.TreeCacheListener#nodeRemoved(org.jboss.cache.Fqn)
     */
    public final void nodeRemoved(final Fqn fqn) {
        Region region = regionManager.getRegion(fqn.toString());
        if (region != null) {
            region.setAddedNode(fqn);
        } else {
            messageLogger.logDebug("No region returned for FQN: " + fqn);
        }
    }

    /**
     * @see org.jboss.cache.TreeCacheListener#nodeEvicted(org.jboss.cache.Fqn)
     */
    public final void nodeEvicted(final Fqn fqn) {
    }

    /**
     * @see org.jboss.cache.TreeCacheListener#nodeModified(org.jboss.cache.Fqn)
     */
    public final void nodeModified(final Fqn fqn) {
        nodeVisited(fqn);
    }

    /**
     * @see org.jboss.cache.TreeCacheListener#nodeVisited(org.jboss.cache.Fqn)
     */
    public final void nodeVisited(final Fqn fqn) {
        Region region = regionManager.getRegion(fqn.toString());
        if (region != null) {
            region.setVisitedNode(fqn);
        } else {
            messageLogger.logDebug("No region returned for FQN: " + fqn);
        }
    }

    /**
     * @see org.jboss.cache.TreeCacheListener#cacheStarted(org.jboss.cache.TreeCache)
     */
    public final void cacheStarted(final TreeCache treeCache) {
        messageLogger.logInfo("Starting eviction policy using provider: " + this.getClass().getName());
        parseConfig(treeCache.getEvictionPolicyConfig());
        regionManager = new RegionManager(this);
        cache = treeCache;

        for (int i = 0; i < regionValues.length; i++) {
            EvictionAlgorithm algorithm = new TicketTTLEvictionAlgorithm();

            try {
                Region region = regionManager.createRegion(ROOT.toString() + regionValues[i].regionName, algorithm);
                region.setMaxNodes(maxNodes);
                region.setTimeToIdleSeconds(wakeUpIntervalSeconds);
            } catch (RegionNameConflictException rnce) {
                throw new EvictionConfigurationException("Unable to create region " + regionValues[i].regionName, rnce);
            }
        }

        evictionTimer = new Timer();
        evictionTimer.schedule(new EvictionTimerTask(this), wakeUpIntervalSeconds * 1000, wakeUpIntervalSeconds * 1000);
    }

    /**
     * @see org.jboss.cache.TreeCacheListener#cacheStopped(org.jboss.cache.TreeCache)
     */
    public final void cacheStopped(final TreeCache treeCache) {
        evictionTimer.cancel();
    }

    /**
     * @see org.jboss.cache.TreeCacheListener#viewChange(org.jgroups.View)
     */
    public final void viewChange(final View view1) {
    }

    /**
     * Parse the config retrieved from TreeCache.getEvictionPolicyConfig(). Populates
     * the regionValues array.
     *
     * @param config configuration for this eviction policy
     */
    final void parseConfig(final Element config) {
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
            Node node = regions.item(i);

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
    final String getAttribute(final Element element, final String attributeName) {
        NodeList nodes = element.getElementsByTagName(ATTRIBUTE_NAME);

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);

            if (node.getNodeType() != Node.ELEMENT_NODE)
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
    final String getElementContent(final Element element, final boolean trim) {
        NodeList nodes = element.getChildNodes();
        String attributeText = "";

        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            if (node instanceof Text)
                attributeText = attributeText + ((Text) node).getData();
        }

        if (trim)
            attributeText = attributeText.trim();

        return attributeText;
    }

    /**
     * Get the region values.
     *
     * @return Array containing the region values.
     */
    final RegionValue[] getRegionValues() {
        return regionValues;
    }

    /**
     * Get a region value.
     *
     * @param fqn Fully quailified name.
     * @return A region value.
     */
    final RegionValue getRegionValue(final String fqn) {

        for (int i = 0; i < regionValues.length; i++) {
            if (fqn.equals(ROOT.toString() + regionValues[i].regionName))
                return regionValues[i];
        }

        return null;
    }

    /**
     * Simple data container for region values.
     */
    class RegionValue {

        /** Region name. */
        String regionName;

        /** Time to live. */
        long timeToLive;

        /** String representation.
         * @return The string representation. */
        public String toString() {
            return "[" + regionName + ", " + timeToLive + "]";
        }
    }
}
