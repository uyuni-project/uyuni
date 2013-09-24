/**
 * Copyright (c) 2013 SUSE
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.manager.audit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.SUSEProductDto;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.manager.distupgrade.DistUpgradeManager;

/**
 * CVESearchManager.
 *
 * @version $Rev$
 */
public class CVEAuditManager {

    /** The log. */
    private static Logger log = Logger.getLogger(CVEAuditManager.class);

    // Internal methods caches
    /** The SUSE product channel cache. */
    private static Map<Long, List<Channel>> suseProductChannelCache =
            new HashMap<Long, List<Channel>>();

    /** The target product cache. */
    private static Map<Long, List<SUSEProductDto>> targetProductCache =
            new HashMap<Long, List<SUSEProductDto>>();

    /** The source product cache. */
    private static Map<Long, List<SUSEProductDto>> sourceProductCache =
            new HashMap<Long, List<SUSEProductDto>>();

    /**
     * Not to be instantiated.
     */
    private CVEAuditManager() {
    }

    /**
     * Empty the suseCVEServerChannel table.
     */
    public static void deleteRelevantChannels() {
        WriteMode m = ModeFactory.getWriteMode("cve_audit_queries",
                "delete_relevant_channels");
        m.executeUpdate(new HashMap<String, Long>());
    }

    /**
     * Insert a set of relevant channels into the suseCVEServerChannel table.
     *
     * @param pairs the pairs
     */
    @SuppressWarnings("rawtypes")
    public static void insertRelevantChannels(Set<ServerChannelIdPair> pairs) {
        WriteMode m = ModeFactory.getWriteMode("cve_audit_queries",
                "insert_relevant_channel");

        List<Map> parameterList = new ArrayList<Map>(pairs.size());
        for (ServerChannelIdPair pair : pairs) {
            Map<String, Object> parameters = new HashMap<String, Object>(3);
            parameters.put("sid", pair.getSid());
            parameters.put("cid", pair.getCid());
            parameters.put("rank", pair.getChannelRank());
            parameterList.add(parameters);
        }

        m.executeUpdates(parameterList);
    }

    /**
     * Find channel product IDs given a list of vendor channel IDs.
     *
     * @param channelIDs list of vendor channel IDs
     * @return list of channel product IDs
     */
    @SuppressWarnings("unchecked")
    public static List<Long> findChannelProducts(List<Long> channelIDs) {
        SelectMode m = ModeFactory.getMode("cve_audit_queries",
                "find_relevant_products");
        List<Map<String, Long>> results = m.execute(channelIDs);
        List<Long> channelProductIDs = new LinkedList<Long>();
        for (Map<String, Long> result : results) {
            channelProductIDs.add(result.get("channel_product_id"));
        }
        return channelProductIDs;
    }

    /**
     * Find mandatory and optional channels belonging to a certain product
     * (given by a list of channel product IDs) and channel tree given by
     * the ID of the parent channel. Will return the base channel as well.
     *
     * TODO: Merge with {@link DistUpgradeManager#findProductChannels(long, String)}.
     *
     * @param channelProductIDs IDs of channel products (rhnChannelProduct rows)
     * @param parentChannelID ID of a parent channel
     * @return list of channels relevant for given channel products
     */
    @SuppressWarnings("unchecked")
    public static List<Channel> findProductChannels(List<Long> channelProductIDs,
            Long parentChannelID) {
        SelectMode m = ModeFactory.getMode("cve_audit_queries",
                "find_product_channels");
        Map<String, Long> params = new HashMap<String, Long>();
        params.put("parent", parentChannelID);
        return m.execute(params, channelProductIDs);
    }

    /**
     * Find *all* relevant channels for a given SUSE product with caching.
     *
     * @param suseProductID the SUSE product ID
     * @return list of mandatory and optional channels
     */
    public static List<Channel> findSUSEProductChannels(long suseProductID) {
        // Look it up in the cache
        List<Channel> result = suseProductChannelCache.get(suseProductID);
        if (result != null) {
            if (log.isDebugEnabled()) {
                log.debug("Product channels retrieved from cache for " + suseProductID);
            }
            return result;
        }

        // Convert a SUSE product ID into channel product IDs
        result = new ArrayList<Channel>();
        List<Long> relevantChannelProductIDs = convertProductId(suseProductID);

        // Find relevant channels
        if (relevantChannelProductIDs.size() > 0) {
            Long baseCID = DistUpgradeManager
                    .getProductBaseChannelDto(suseProductID).getId();

            List<Channel> productChannels = findProductChannels(
                    relevantChannelProductIDs, baseCID);
            if (productChannels != null) {
                result.addAll(productChannels);
            }
            if (log.isDebugEnabled()) {
                log.debug("Product channels for " + suseProductID + ": " + result);
            }
        }
        else if (log.isDebugEnabled()) {
            log.debug("No channels available for SUSE product: " + suseProductID);
        }

        // Put it in the cache before returning
        suseProductChannelCache.put(suseProductID, result);
        return result;
    }

    /**
     * Find all servers.
     *
     * @return all servers
     */
    @SuppressWarnings("unchecked")
    public static List<SystemOverview> listAllServers() {
        SelectMode m = ModeFactory.getMode("cve_audit_queries", "find_all_servers");
        return m.execute(Collections.EMPTY_MAP);
    }

    /**
     * Convert a SUSE product ID to a list of channel product IDs.
     *
     * @param suseProductId the SUSE product ID
     * @return list of channel product IDs
     */
    @SuppressWarnings("unchecked")
    public static List<Long> convertProductId(long suseProductId) {
        SelectMode m = ModeFactory.getMode("cve_audit_queries",
                "convert_suse_product_to_channel_products");
        Map<String, Long> params = new HashMap<String, Long>();
        params.put("suseProductId", suseProductId);
        DataResult<Map<String, Long>> results = m.execute(params);
        List<Long> ret = new ArrayList<Long>();
        for (Map<String, Long> result : results) {
            ret.add(result.get("channel_product_id"));
        }
        return ret;
    }

    /**
     * Recursively find the chain of SUSE products on a migration path going
     * forwards.
     *
     * @param suseProductID product to start with (ID of row in table suseProducts)
     * @return chain of SUSE products on a forward migration path
     */
    public static List<SUSEProductDto> findAllTargetProducts(Long suseProductID) {
        // Look it up in the cache
        List<SUSEProductDto> result = targetProductCache.get(suseProductID);
        if (result != null) {
            return result;
        }

        result = new LinkedList<SUSEProductDto>();
        List<SUSEProductDto> targets = DistUpgradeManager
                .findTargetProducts(suseProductID);
        while (targets.size() > 0) {
            // We assume that there is always only one target!
            if (targets.size() > 1) {
                log.warn("More than one migration target found for " + suseProductID);
            }
            SUSEProductDto target = targets.get(0);
            result.add(target);
            targets = DistUpgradeManager.findTargetProducts(target.getId());
        }

        // Put it in the cache before returning
        targetProductCache.put(suseProductID, result);
        return result;
    }

    /**
     * Recursively find the chain of SUSE products on a migration path going
     * backwards.
     *
     * @param suseProductID product to start with (ID of row in table suseProducts)
     * @return chain of SUSE products on a backward migration path
     */
    public static List<SUSEProductDto> findAllSourceProducts(Long suseProductID) {
        // Look it up in the cache
        List<SUSEProductDto> result = sourceProductCache.get(suseProductID);
        if (result != null) {
            return result;
        }

        result = new LinkedList<SUSEProductDto>();
        List<SUSEProductDto> targets = DistUpgradeManager
                .findSourceProducts(suseProductID);
        while (targets.size() > 0) {
            // We assume that there is always only one source!
            if (targets.size() > 1) {
                log.warn("More than one target product found!");
            }
            SUSEProductDto target = targets.get(0);
            result.add(target);
            targets = DistUpgradeManager.findSourceProducts(target.getId());
        }

        // Put it in the cache before returning
        sourceProductCache.put(suseProductID, result);
        return result;
    }

    /**
     * Populate the suseCVEServerChannel table.
     */
    public static void populateCVEServerChannels() {
        // Empty the table first
        deleteRelevantChannels();

        // Init result
        Set<ServerChannelIdPair> relevantChannels = new HashSet<ServerChannelIdPair>();

        // Empty caches
        suseProductChannelCache.clear();
        sourceProductCache.clear();
        targetProductCache.clear();

        // Get a list of *all* servers
        List<SystemOverview> result = listAllServers();
        if (log.isDebugEnabled()) {
            log.debug("Number of servers found: " + result.size());
        }

        // For each server: find the set of relevant channels
        for (SystemOverview s : result) {
            Long sid = s.getId();
            List<Long> vendorChannelIDs = new LinkedList<Long>();
            Long parentChannelID = null;

            // Load the server object
            Server server = ServerFactory.lookupById(sid);

            // Get assigned channels
            Set<Channel> assignedChannels = server.getChannels();
            if (log.isDebugEnabled()) {
                log.debug("Server '" + server.getName() + "' has "  +
                    assignedChannels.size() + " channels assigned");
            }

            // All assigned channels are relevant (rank = 0)
            int maxRank = 0;
            for (Channel c : assignedChannels) {
                relevantChannels.add(new ServerChannelIdPair(sid, c.getId(), 0));

                // All originals in the cloning chain are relevant, channel
                // ranking should increase with every layer.
                int i = 0;
                Channel original = c;
                while (original.isCloned()) {
                    original = original.getOriginal();
                    // Revert the index if no channel has actually been added
                    i = relevantChannels.add(
                            new ServerChannelIdPair(sid, original.getId(), ++i)) ? i : --i;
                }
                // Remember the longest cloning chain as 'maxRank'
                if (i > maxRank) {
                    maxRank = i;
                }

                // Store vendor channels and the parent channel ID
                if (original.getOrg() == null &&
                        !vendorChannelIDs.contains(original.getId())) {
                    vendorChannelIDs.add(original.getId());

                    if (original.getParentChannel() == null) {
                        parentChannelID = original.getId();
                    }
                }
            }

            // Find all channels relevant for the currently installed products
            if (!vendorChannelIDs.isEmpty() && parentChannelID != null) {

                // Find IDs of relevant channel products
                List<Long> relevantChannelProductIDs =
                        findChannelProducts(vendorChannelIDs);

                // Find all relevant channels
                List<Channel> productChannels = findProductChannels(
                        relevantChannelProductIDs, parentChannelID);

                if (log.isDebugEnabled()) {
                    log.debug("Found " + vendorChannelIDs.size() + " vendor channels -> " +
                            "channel products: " + relevantChannelProductIDs);
                }

                // Increase ranking index for unassigned product channels, but revert the
                // index if no channel has actually been added
                maxRank = addRelevantChannels(relevantChannels, server, productChannels,
                        ++maxRank) ? maxRank : --maxRank;
            }

            // Look at installed products and previous/future SP migrations
            List<Long> suseProductIDs = SUSEProductFactory
                    .getInstalledProductIds(server);

            // For *each* installed product
            for (long suseProductID : suseProductIDs) {
                // Start channel ranking with the current maxRank
                int i = maxRank;

                // Find all possible migration *targets* recursively
                List<SUSEProductDto> targets = findAllTargetProducts(suseProductID);
                if (log.isDebugEnabled() && targets.size() <= 0) {
                    log.debug("No target products found for " + suseProductID);
                }
                for (SUSEProductDto target : targets) {
                    if (log.isDebugEnabled()) {
                        log.debug("Target found for " + suseProductID + ": " +
                                target.getId());
                    }
                    List<Channel> productChannels = findSUSEProductChannels(target.getId());
                    addRelevantChannels(relevantChannels, server, productChannels, ++i);
                }

                // Find all possible migration *sources* recursively
                List<SUSEProductDto> sources = findAllSourceProducts(suseProductID);
                if (log.isDebugEnabled() && sources.size() <= 0) {
                    log.debug("No source products found for " + suseProductID);
                }
                for (SUSEProductDto source : sources) {
                    if (log.isDebugEnabled()) {
                        log.debug("Source found for " + suseProductID + ": " +
                                source.getId());
                    }
                    List<Channel> productChannels = findSUSEProductChannels(source.getId());
                    addRelevantChannels(relevantChannels, server, productChannels, ++i);
                }
            }
        }

        // Insert relevant channels into the database
        insertRelevantChannels(relevantChannels);
    }

    /**
     * List visible systems with their patch status regarding a given CVE identifier.
     *
     * @param user the calling user
     * @param cveIdentifier the CVE identifier to lookup
     * @param patchStatuses the patch statuses
     * @return list of system records with patch status
     * @throws UnknownCVEIdentifierException if the CVE number is not known
     */
    @SuppressWarnings("unchecked")
    public static List<CVEAuditSystem> listSystemsByPatchStatus(User user,
            String cveIdentifier, EnumSet<PatchStatus> patchStatuses)
            throws UnknownCVEIdentifierException {

        if (isCVEIdentifierUnknown(cveIdentifier)) {
            throw new UnknownCVEIdentifierException();
        }

        SelectMode m = ModeFactory.getMode("cve_audit_queries",
            "list_systems_by_patch_status");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("cve_identifier", cveIdentifier);
        params.put("user_id", user.getId());
        DataResult<Map<String, Object>>results = m.execute(params);
        List<CVEAuditSystem> ret = new LinkedList<CVEAuditSystem>();

        // Hold the system and errata we are currently looking at
        CVEAuditSystem currentSystem = null;
        Long currentErrata = null;

        // Flags
        boolean errataChannelsAssigned = false;
        boolean errataInstalled = false;
        boolean assigned = false;
        boolean installed = false;
        boolean hasErrata = false;

        for (Map<String, Object> result : results) {
            // Get the server id first
            Long systemID = (Long) result.get("system_id");

            // Is this a new system?
            if (currentSystem == null || !systemID.equals(currentSystem.getSystemID())) {
                // Finish up work on the last one
                if (currentSystem != null) {
                    setPatchStatus(currentSystem, installed, assigned, hasErrata);
                    // Check if the patch status is contained in the filter
                    if (patchStatuses.contains(currentSystem.getPatchStatus())) {
                        ret.add(currentSystem);
                    }
                }

                // Start working on the new system
                currentSystem = new CVEAuditSystem(systemID);
                currentSystem.setSystemName((String) result.get("system_name"));

                // First assignment
                assigned = getBooleanValue(result, "channel_assigned");
                installed = getBooleanValue(result, "package_installed");

                // Get errata and channel ID
                currentErrata = (Long) result.get("errata_id");
                Long channelID = (Long) result.get("channel_id");

                // Add these to the current system
                hasErrata = currentErrata != null;
                if (hasErrata) {
                    // We have an errata
                    errataChannelsAssigned = assigned;
                    errataInstalled = installed;
                    ErrataIdAdvisoryPair errata = new ErrataIdAdvisoryPair(
                            currentErrata, (String) result.get("errata_advisory"));
                    currentSystem.addErrata(errata);
                }
                if (channelID != null) {
                    ChannelIdNameLabelTriple channel =
                            new ChannelIdNameLabelTriple(channelID,
                                    (String) result.get("channel_name"),
                                    (String) result.get("channel_label"));
                    currentSystem.addChannel(channel);
                }
            }
            else {
                // NOT a new system, check if we are still looking at the same errata
                Long errataID = (Long) result.get("errata_id");
                if (errataID.equals(currentErrata)) {
                    // Combine flags with &
                    errataChannelsAssigned &= getBooleanValue(result, "channel_assigned");
                    errataInstalled &= getBooleanValue(result, "package_installed");
                }
                else {
                    // Finish work on old errata
                    assigned |= errataChannelsAssigned;
                    installed |= errataInstalled;

                    // Switch to the new errata
                    currentErrata = errataID;
                    errataChannelsAssigned = getBooleanValue(result, "channel_assigned");
                    errataInstalled = getBooleanValue(result, "package_installed");
                }

                // Add errata and channel ID
                ErrataIdAdvisoryPair errata = new ErrataIdAdvisoryPair(
                        errataID, (String) result.get("errata_advisory"));
                currentSystem.addErrata(errata);
                ChannelIdNameLabelTriple channel =
                        new ChannelIdNameLabelTriple((Long) result.get("channel_id"),
                                (String) result.get("channel_name"),
                                (String) result.get("channel_label"));
                currentSystem.addChannel(channel);
            }
        }

        // Finish up the *very* last system record
        if (currentSystem != null) {
            setPatchStatus(currentSystem, installed, assigned, hasErrata);
            // Check if the patch status is contained in the filter
            if (patchStatuses.contains(currentSystem.getPatchStatus())) {
                ret.add(currentSystem);
            }
        }

        debugLog(ret);
        return ret;
    }

    /**
     * Extracts a boolean value out of a 0-1 integer column in a result object.
     * @param result a query Map result
     * @param key the key corresponding to the boolean value to extract
     * @return true if the integer value was 1, false otherwise
     */
    public static boolean getBooleanValue(Map<String, Object> result, String key) {
        return result.get(key) == null ? false : ((Number) result.get(key)).intValue() == 1;
    }

    /**
     * Checks if a CVE identifier is known or not.
     *
     * @param cveIdentifier the CVE identifier
     * @return true if it is unknown
     */
    @SuppressWarnings("unchecked")
    public static boolean isCVEIdentifierUnknown(String cveIdentifier) {
        SelectMode m = ModeFactory.getMode("cve_audit_queries", "count_cve_identifiers");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("cve_identifier", cveIdentifier);
        DataResult<Map<String, Object>> results = m.execute(params);
        Long count = (Long) results.get(0).get("count");
        return count == 0;
    }

    /**
     * Set the patch status of a system record.
     *
     * @param system the system
     * @param installed true if system has the relevant package installed
     * @param assigned the true if system has the relevant channel assigned
     * @param hasErrata true if query row has an errata ID
     */
    public static void setPatchStatus(CVEAuditSystem system, boolean installed,
            boolean assigned, boolean hasErrata) {
        if (hasErrata) {
            if (installed) {
                system.setPatchStatus(PatchStatus.PATCHED);
            }
            else if (assigned) {
                system.setPatchStatus(PatchStatus.AFFECTED_PATCH_APPLICABLE);
            }
            else {
                system.setPatchStatus(PatchStatus.AFFECTED_PATCH_INAPPLICABLE);
            }
        }
        else {
            system.setPatchStatus(PatchStatus.NOT_AFFECTED);
        }
    }

    /**
     * Add a list of channels to the set of channels relevant for a given server.
     *
     * @param relevantChannels the relevant channels
     * @param s the server
     * @param channels the channels
     * @param ranking the ranking
     * @return true as soon as at least one channel record has been added
     */
    private static boolean addRelevantChannels(Set<ServerChannelIdPair> relevantChannels,
            Server s, List<Channel> channels, int ranking) {
        boolean added = false;
        for (Channel c : channels) {
            boolean result = relevantChannels.add(new ServerChannelIdPair(
                    s.getId(), c.getId(), ranking));
            if (result) {
                added = true;
            }
        }
        return added;
    }

    /**
     * Log out the set of results for debugging purposes.
     *
     * @param results the results
     */
    private static void debugLog(List<CVEAuditSystem> results) {
        if (log.isDebugEnabled()) {
            log.debug("Returning " + results.size() + " results");
            for (CVEAuditSystem s : results) {
                String errata = "";
                for (ErrataIdAdvisoryPair e : s.getErratas()) {
                    errata += " " + e.getId();
                }
                String channels = "";
                for (ChannelIdNameLabelTriple c : s.getChannels()) {
                    channels += " " + c.getId();
                }
                log.debug(s.getSystemID() + ": " + s.getPatchStatus() +
                        " (patches: " + errata + ")" +
                        " (channels: " + channels + ")");
            }
        }
    }
}
