/**
 * Copyright (c) 2013 SUSE LLC
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

import static com.redhat.rhn.common.hibernate.HibernateFactory.getSession;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageInfoFactory;
import com.redhat.rhn.domain.product.CachingSUSEProductFactory;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.EssentialChannelDto;
import com.redhat.rhn.frontend.dto.SUSEProductDto;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.manager.distupgrade.DistUpgradeManager;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

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
            new HashMap<>();

    /** The target product cache. */
    private static Map<Long, List<SUSEProductDto>> targetProductCache =
            new HashMap<>();

    /** The source product cache. */
    private static Map<Long, List<SUSEProductDto>> sourceProductCache =
            new HashMap<>();

    private static final String KERNEL_DEFAULT_NAME = "kernel-default";

    private static final String KERNEL_XEN_NAME = "kernel-xen";

    /**
     * Not to be instantiated.
     */
    private CVEAuditManager() {
    }

    /**
     * Empty the suseCVEServerChannel and suseCVEImageChannel table.
     */
    public static void deleteRelevantChannels() {
        WriteMode m = ModeFactory.getWriteMode("cve_audit_queries",
                "delete_relevant_server_channels");
        m.executeUpdate(new HashMap<String, Long>());
        m = ModeFactory.getWriteMode("cve_audit_queries",
                "delete_relevant_image_channels");
        m.executeUpdate(new HashMap<String, Long>());
    }

    /**
     * Insert a set of relevant channels into the suseCVEImageChannel table.
     *
     * @param rankedChannels the ranked channels
     */
    public static void insertRelevantImageChannels(Map<ImageInfo,
            List<RankedChannel>> rankedChannels) {
        WriteMode m = ModeFactory.getWriteMode("cve_audit_queries",
                "insert_relevant_image_channel");

        List<Map<String, Object>> parameterList = rankedChannels.entrySet().stream()
                .flatMap(entry -> {
                    ImageInfo imageInfo = entry.getKey();
                    return entry.getValue().stream().map(chan -> {
                        Map<String, Object> parameters = new HashMap<>(3);
                        parameters.put("iid", imageInfo.getId());
                        parameters.put("cid", chan.getChannelId());
                        parameters.put("rank", chan.getRank());
                        return parameters;
                    });
        }).collect(Collectors.toList());

        m.executeUpdates(parameterList);
    }

    /**
     * Insert a set of relevant channels into the suseCVEServerChannel table.
     *
     * @param rankedChannels the ranked channels
     */
    public static void insertRelevantServerChannels(Map<Server,
            List<RankedChannel>> rankedChannels) {
        WriteMode m = ModeFactory.getWriteMode("cve_audit_queries",
                "insert_relevant_server_channel");

        List<Map<String, Object>> parameterList = rankedChannels.entrySet().stream()
                .flatMap(entry -> {
                    Server server = entry.getKey();
                    return entry.getValue().stream().map(chan -> {
                        Map<String, Object> parameters = new HashMap<>(3);
                        parameters.put("sid", server.getId());
                        parameters.put("cid", chan.getChannelId());
                        parameters.put("rank", chan.getRank());
                        return parameters;
                    });
        }).collect(Collectors.toList());

        m.executeUpdates(parameterList);
    }

    /**
     * Find channel product IDs given a list of vendor channel IDs.
     *
     * @param channelIDs list of vendor channel IDs
     * @return list of channel product IDs
     */
    public static List<Long> findChannelProducts(List<Long> channelIDs) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);

        Root<Channel> root = query.from(Channel.class);
        query.select(root.get("product").get("id"))
             .distinct(true)
             .where(
                 builder.and(
                     root.get("id").in(channelIDs)
                 )
             );
        return getSession().createQuery(query).list();
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
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<Channel> query = builder.createQuery(Channel.class);

        Root<Channel> root = query.from(Channel.class);
        query.where(builder.and(
                root.get("product").get("id").in(channelProductIDs),
                builder.or(
                        builder.equal(root.get("id"), parentChannelID),
                        builder.equal(root.get("parentChannel"), parentChannelID)
                )
        ));
        return getSession().createQuery(query).list();
    }

    /**
     * Find *all* relevant channels for a given SUSE product with caching.
     *
     * @param suseProductID the SUSE product ID
     * @param baseChannelId id of the base channel
     * @return list of mandatory and optional channels
     */
    public static List<Channel> findSUSEProductChannels(long suseProductID,
            long baseChannelId) {
        // Look it up in the cache
        List<Channel> result = suseProductChannelCache.get(suseProductID);
        if (result != null) {
            if (log.isDebugEnabled()) {
                log.debug("Product channels retrieved from cache for " + suseProductID);
            }
            return result;
        }

        // Convert a SUSE product ID into channel product IDs
        result = new ArrayList<>();
        List<Long> relevantChannelProductIDs = convertProductId(suseProductID);

        // Find relevant channels
        if (relevantChannelProductIDs.size() > 0) {
            List<Channel> productChannels = findProductChannels(
                    relevantChannelProductIDs, baseChannelId);
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
        Map<String, Long> params = new HashMap<>();
        params.put("suseProductId", suseProductId);
        DataResult<Map<String, Long>> results = m.execute(params);
        List<Long> ret = new ArrayList<>();
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

        result = new LinkedList<>();
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

        result = new LinkedList<>();
        List<SUSEProductDto> sources = DistUpgradeManager
                .findSourceProducts(suseProductID);
        while (sources.size() > 0) {
            // We assume that there is always only one source!
            if (sources.size() > 1) {
                SUSEProduct product = SUSEProductFactory.getProductById(suseProductID);
                log.warn("More than one migration source product found for " +
                        product.getFriendlyName() + " (" + product.getProductId() + "):");
                for (SUSEProductDto source : sources) {
                    SUSEProduct p = SUSEProductFactory.getProductById(source.getId());
                    log.warn("- " + p.getFriendlyName() + " (" + p.getProductId() + ")");
                }
            }
            SUSEProductDto source = sources.get(0);
            result.add(source);
            sources = DistUpgradeManager.findSourceProducts(source.getId());
        }

        // Put it in the cache before returning
        sourceProductCache.put(suseProductID, result);
        return result;
    }

    /**
     * Populate channels for CVE Audit
     * @param auditTarget the target
     * @return set of channels
     */
    public static List<RankedChannel> populateCVEChannels(AuditTarget auditTarget) {
        List<RankedChannel> relevantChannels = new ArrayList<>();
        List<Long> vendorChannelIDs = new LinkedList<>();
        Long parentChannelID = null;

        // All assigned channels are relevant (rank = 0)
        int maxRank = 0;
        for (Channel c : auditTarget.getAssignedChannels()) {
            relevantChannels.add(new RankedChannel(c.getId(), 0));

            // All originals in the cloning chain are relevant, channel
            // ranking should increase with every layer.
            int i = 0;
            Channel original = c;
            while (original.isCloned()) {
                original = original.getOriginal();
                // Revert the index if no channel has actually been added
                i = relevantChannels.add(
                        new RankedChannel(original.getId(), ++i)) ? i : --i;
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
            maxRank = addRelevantChannels(relevantChannels, productChannels,
                    ++maxRank) ? maxRank : --maxRank;
        }

        // Find all channels relevant for past and future migrations
        List<RankedChannel> migrationChannels = addRelevantMigrationProductChannels(
                auditTarget, maxRank);
        relevantChannels.addAll(migrationChannels);


        return relevantChannels.stream()
                .collect(
                    Collectors.groupingBy(
                        RankedChannel::getChannelId,
                        // take only the lowest rank for each channel
                        Collectors.reducing((a, b) -> a.getRank() <= b.getRank() ? a : b)
                    )
                ).entrySet()
                 .stream()
                 // its safe to call get here since groupingBy will not produce empty lists
                 .map(s -> s.getValue().get()).collect(Collectors.toList());
    }

    /**
     * Populate channels for CVE Audit
     */
    public static void populateCVEChannels() {
        // Empty the table first
        deleteRelevantChannels();

        // Empty caches
        suseProductChannelCache.clear();
        sourceProductCache.clear();
        targetProductCache.clear();

        // Get a list of *all* servers
        List<Server> servers = ServerFactory.list(false, false);
        if (log.isDebugEnabled()) {
            log.debug("Number of servers found: " + servers.size());
        }

        CachingSUSEProductFactory productFactory = new CachingSUSEProductFactory();

        Map<Server, List<RankedChannel>> relevantServerChannels = servers.stream()
            .collect(Collectors.toMap(
                Function.identity(),
                server -> populateCVEChannels(new ServerAuditTarget(server, productFactory))
            ));

        insertRelevantServerChannels(relevantServerChannels);

        Map<ImageInfo, List<RankedChannel>> relevantImageChannels =
                ImageInfoFactory.list().stream().collect(Collectors.toMap(
                    Function.identity(),
                    imageInfo -> populateCVEChannels(new ImageAuditTarget(imageInfo, productFactory))
                ));

        insertRelevantImageChannels(relevantImageChannels);

    }

    /**
     * Looks at installed products on the server and their previous and future
     * SP migrations, adding channels to relevantChannels when they are found.
     *
     * @param auditTarget the audit target object
     * @param maxRank starting rank for new channels found
     * @return set of channels
     */
    public static List<RankedChannel> addRelevantMigrationProductChannels(
            AuditTarget auditTarget, int maxRank) {

        final List<RankedChannel> result = new ArrayList<>();

        List<SUSEProduct> products = auditTarget.getSUSEProducts();

        if (products.isEmpty()) {
            return Collections.emptyList();
        }

        Optional<SUSEProduct> baseProduct = products.stream()
                .filter(SUSEProduct::isBase)
                .findFirst();
        if (!baseProduct.isPresent()) {
            return Collections.emptyList();
        }

        Long baseProductID = baseProduct.get().getId();
        List<SUSEProductDto> baseProductTargets = findAllTargetProducts(baseProductID);
        List<SUSEProductDto> baseProductSources = findAllSourceProducts(baseProductID);
        List<Long> suseProductIDs = products.stream()
                .map(SUSEProduct::getId)
                .collect(Collectors.toList());

        ChannelArch arch = auditTarget.getCompatibleChannelArch();
        int currentRank = maxRank;

        // for each base product target...
        for (SUSEProductDto baseProductTarget : baseProductTargets) {
            Long baseProductChannelId = getBaseProductChannelId(baseProductTarget, arch);

            // ...if a base channel exists and is synced...
            if (baseProductChannelId != null) {
                // ...and for each installed product...
                for (long suseProductID : suseProductIDs) {

                    // ...if it has a target with that base product...
                    List<SUSEProductDto> targets = findAllTargetProducts(suseProductID);
                    if (log.isDebugEnabled() && targets.size() <= 0) {
                        log.debug("No target products found for " + suseProductID);
                    }
                    for (SUSEProductDto target : targets) {
                        if (log.isDebugEnabled()) {
                            log.debug("Target found for " + suseProductID + ": " +
                                    target.getId());
                        }

                        // ...add its channel to the relevant list
                        List<Channel> productChannels =
                                findSUSEProductChannels(target.getId(),
                                        baseProductChannelId);
                        addRelevantChannels(result, productChannels,
                                ++currentRank);
                    }
                }
            }
        }

        // Increase the rank for indication of older products (previous SPs)
        currentRank = 99999;

        // for each base product source...
        for (SUSEProductDto baseProductSource : baseProductSources) {
            Long baseProductChannelId = getBaseProductChannelId(baseProductSource, arch);

            // ...if a base channel exists and is synced...
            if (baseProductChannelId != null) {
                // ...and for each installed product...
                for (long suseProductID : suseProductIDs) {

                    // ...if it has a source with that base product...
                    List<SUSEProductDto> sources = findAllSourceProducts(suseProductID);
                    if (log.isDebugEnabled() && sources.size() <= 0) {
                        log.debug("No source products found for " + suseProductID);
                    }
                    for (SUSEProductDto source : sources) {
                        if (log.isDebugEnabled()) {
                            log.debug("Source found for " + suseProductID + ": " +
                                    source.getId());
                        }

                        // ...add its channel to the relevant list
                        List<Channel> productChannels =
                                findSUSEProductChannels(source.getId(),
                                        baseProductChannelId);
                        addRelevantChannels(result, productChannels,
                                ++currentRank);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Returns a channel ID from a base product.
     *
     * @param baseProduct the base product
     * @param arch product channel architecture (as a product might support more than one)
     * @return the channel ID
     */
    public static Long getBaseProductChannelId(SUSEProductDto baseProduct,
            ChannelArch arch) {
        Long baseProductID = baseProduct.getId();
        if (baseProductID != null) {
            EssentialChannelDto channel =
                    DistUpgradeManager.getProductBaseChannelDto(baseProductID, arch);
            if (channel != null) {
                return channel.getId();
            }
        }
        return null;
    }

    /**
     * TODO: only packageInstalled and channelAssigned can be null on an affected entry
     * TODO: this should be refactored at some point
     */
    private static class Wrapper {

        private final long id;
        private final String name;
        private final Optional<Long> errataId;
        private final String errataAdvisory;
        private final Optional<Long> packageId;
        private final Optional<String> packageName;
        private final boolean packageInstalled;
        private final Optional<Long> channelId;
        private final String channelName;
        private final String channelLabel;
        private final boolean channelAssigned;
        private final Optional<Long> channelRank;

        /**
         * constructor
         * @param idIn id
         * @param nameIn name
         * @param errataIdIn errataID
         * @param errataAdvisoryIn errataAdvisory
         * @param packageIdIn packageId
         * @param packageNameIn packageName
         * @param packageInstalledIn packageInstalled
         * @param channelIdIn channelId
         * @param channelNameIn channelName
         * @param channelLabelIn channelLabel
         * @param channelAssignedIn channelAssigned
         * @param channelRankIn channelRank
         */
        Wrapper(long idIn, String nameIn,
                Optional<Long> errataIdIn, String errataAdvisoryIn,
                Optional<Long> packageIdIn, Optional<String> packageNameIn,
                boolean packageInstalledIn, Optional<Long> channelIdIn,
                String channelNameIn, String channelLabelIn,
                boolean channelAssignedIn, Optional<Long> channelRankIn) {
            this.id = idIn;
            this.name = nameIn;
            this.errataId = errataIdIn;
            this.errataAdvisory = errataAdvisoryIn;
            this.packageId = packageIdIn;
            this.packageName = packageNameIn;
            this.packageInstalled = packageInstalledIn;
            this.channelId = channelIdIn;
            this.channelName = channelNameIn;
            this.channelLabel = channelLabelIn;
            this.channelAssigned = channelAssignedIn;
            this.channelRank = channelRankIn;
        }

        /**
         *  Id
         * @return the Id
         */
        public long getId() {
            return id;
        }

        /**
         * Name
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * ErrataId
         * @return the errateId
         */
        public Optional<Long> getErrataId() {
            return errataId;
        }

        /**
         * ErrataAdvisory
         * @return the errataAdvisory
         */
        public String getErrataAdvisory() {
            return errataAdvisory;
        }

        /**
         * PackageId
         * @return the packageId
         */
        public Optional<Long> getPackageId() {
            return packageId;
        }

        /**
         * PackageName
         * @return the packageName
         */
        public Optional<String> getPackageName() {
            return packageName;
        }

        /**
         * PackageInstalled
         * @return the packageInstalled
         */
        public boolean isPackageInstalled() {
            return packageInstalled;
        }

        /**
         * ChannelAssigned
         * @return the channelAssigned
         */
        public boolean isChannelAssigned() {
            return channelAssigned;
        }

        /**
         * ChannelId
         * @return the channelId
         */
        public Optional<Long> getChannelId() {
            return channelId;
        }

        /**
         * ChannelName
         * @return the channelName
         */
        public String getChannelName() {
            return channelName;
        }

        /**
         * ChannelLabel
         * @return the channelLabel
         */
        public String getChannelLabel() {
            return channelLabel;
        }

        /**
         * ChannelRank
         * @return the channelRank
         */
        public Optional<Long> getChannelRank() {
            return channelRank;
        }
    }

    private static Stream<Wrapper> listImagesByPatchStatus(User user,
        String cveIdentifier) {
        SelectMode m = ModeFactory.getMode("cve_audit_queries",
                "list_images_by_patch_status");

        Map<String, Object> params = new HashMap<>();
        params.put("cve_identifier", cveIdentifier);
        params.put("user_id", user.getId());
        DataResult<Map<String, Object>> results = m.execute(params);

        return StreamSupport.stream(results.spliterator(), false)
                .map(row -> new Wrapper(
                        (long) row.get("image_info_id"),
                        (String) row.get("image_name"),
                        Optional.ofNullable((Long)row.get("errata_id")),
                        (String) row.get("errata_advisory"),
                        Optional.ofNullable((Long)row.get("package_id")),
                        Optional.ofNullable((String)row.get("package_name")),
                        getBooleanValue(row, "package_installed"),
                        Optional.ofNullable((Long)row.get("channel_id")),
                        (String) row.get("channel_name"),
                        (String) row.get("channel_label"),
                        getBooleanValue(row, "channel_assigned"),
                        Optional.ofNullable((Long)row.get("channel_rank"))
                ));

    }

    private static Stream<Wrapper> listSystemsByPatchStatus(User user,
        String cveIdentifier) {
        SelectMode m = ModeFactory.getMode("cve_audit_queries",
                "list_systems_by_patch_status");

        Map<String, Object> params = new HashMap<>();
        params.put("cve_identifier", cveIdentifier);
        params.put("user_id", user.getId());
        DataResult<Map<String, Object>> results = m.execute(params);

        return StreamSupport.stream(results.spliterator(), false)
                .map(row -> new Wrapper(
                    (long) row.get("system_id"),
                    (String) row.get("system_name"),
                    Optional.ofNullable((Long)row.get("errata_id")),
                    (String) row.get("errata_advisory"),
                    Optional.ofNullable((Long)row.get("package_id")),
                    Optional.ofNullable((String)row.get("package_name")),
                    getBooleanValue(row, "package_installed"),
                    Optional.ofNullable((Long)row.get("channel_id")),
                    (String) row.get("channel_name"),
                    (String) row.get("channel_label"),
                    getBooleanValue(row, "channel_assigned"),
                    Optional.ofNullable((Long)row.get("channel_rank"))
                ));

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
    public static List<CVEAuditServer> listSystemsByPatchStatus(User user,
        String cveIdentifier, EnumSet<PatchStatus> patchStatuses)
            throws UnknownCVEIdentifierException {
        if (isCVEIdentifierUnknown(cveIdentifier)) {
            throw new UnknownCVEIdentifierException();
        }

        List<Wrapper> results = listSystemsByPatchStatus(user, cveIdentifier)
                .collect(Collectors.toList());

        return listSystemsByPatchStatus(results, patchStatuses)
                .stream()
                .map(system -> new CVEAuditServer(
                        system.getId(),
                        system.getSystemName(),
                        system.getPatchStatus(),
                        system.getChannels(),
                        system.getErratas()
                )).collect(Collectors.toList());
    }

    /**
     * List visible images with their patch status regarding a given CVE identifier.
     *
     * @param user the calling user
     * @param cveIdentifier the CVE identifier to lookup
     * @param patchStatuses the patch statuses
     * @return list of images records with patch status
     * @throws UnknownCVEIdentifierException if the CVE number is not known
     */
    @SuppressWarnings("unchecked")
    public static List<CVEAuditImage> listImagesByPatchStatus(User user,
            String cveIdentifier, EnumSet<PatchStatus> patchStatuses)
            throws UnknownCVEIdentifierException {
        if (isCVEIdentifierUnknown(cveIdentifier)) {
            throw new UnknownCVEIdentifierException();
        }

        List<Wrapper> results = listImagesByPatchStatus(user, cveIdentifier)
                .collect(Collectors.toList());

        return listSystemsByPatchStatus(results, patchStatuses)
                .stream()
                .map(system -> new CVEAuditImage(
                        system.getId(),
                        system.getSystemName(),
                        system.getPatchStatus(),
                        system.getChannels(),
                        system.getErratas()
                )).collect(Collectors.toList());
    }


    /**
     * List visible systems with their patch status regarding a given CVE identifier.
     *
     * @param results raw patchstatus query
     * @param patchStatuses the patch statuses
     * @return list of system records with patch status
     * @throws UnknownCVEIdentifierException if the CVE number is not known
     */
    @SuppressWarnings("unchecked")
    public static List<CVEAuditSystemBuilder> listSystemsByPatchStatus(
       List<Wrapper> results, EnumSet<PatchStatus> patchStatuses)
            throws UnknownCVEIdentifierException {

        List<CVEAuditSystemBuilder> ret = new LinkedList<>();

        // Hold the system and errata we are currently looking at
        CVEAuditSystemBuilder currentSystem = null;
        Optional<Long> currentErrata = Optional.empty();
        // Holds the list of patched packages for the CVE we are looking at
        Map<String, Boolean> patchedPackageNames = new HashMap<>();
        // Holds wether the channel for a certain package name is assigned
        Map<String, Boolean> channelAssignedPackageNames = new HashMap<>();

        // Flags
        boolean hasErrata = false;
        boolean ignoreOldProducts = true;
        boolean usesLivePatchingDefault = false;
        boolean usesLivePatchingXen = false;
        Optional<Long> lowestRank = Optional.empty();
        List<Wrapper> sorted = results.stream()
                .sorted(Comparator.comparing(s -> s.getChannelRank().orElse(Long.MAX_VALUE)))
                .collect(Collectors.toList());
        for (Wrapper result : sorted) {
            // Get the server id first
            final long systemID = result.getId();
            final String packageName = result.getPackageName().orElse(null);
            if (packageName != null && packageName.startsWith("kgraft-patch")) {
                usesLivePatchingDefault |= packageName.endsWith("-default");
                usesLivePatchingXen |= packageName.endsWith("-xen");
            }

            // Is this a new system?
            if (currentSystem == null || systemID != currentSystem.getId()) {
                // Finish up work on the last one
                if (currentSystem != null) {
                    if (usesLivePatchingDefault &&
                            patchedPackageNames.containsKey(KERNEL_DEFAULT_NAME)) {
                        patchedPackageNames.remove(KERNEL_DEFAULT_NAME);
                        channelAssignedPackageNames.remove(KERNEL_DEFAULT_NAME);
                    }
                    if (usesLivePatchingXen &&
                            patchedPackageNames.containsKey(KERNEL_XEN_NAME)) {
                        patchedPackageNames.remove(KERNEL_XEN_NAME);
                        channelAssignedPackageNames.remove(KERNEL_XEN_NAME);
                    }
                    boolean allPackagesForAllErrataInstalled = true;
                    for (Boolean isPatched : patchedPackageNames.values()) {
                        allPackagesForAllErrataInstalled &= isPatched;
                    }

                    boolean oneChannelForPackageAssigned = true;
                    for (Boolean isAssigned : channelAssignedPackageNames.values()) {
                        oneChannelForPackageAssigned &= isAssigned;
                    }

                    currentSystem.setPatchStatus(
                        getPatchStatus(allPackagesForAllErrataInstalled,
                                oneChannelForPackageAssigned, hasErrata)
                    );

                    // clear up the package list for the next system
                    patchedPackageNames.clear();
                    channelAssignedPackageNames.clear();

                    // Check if the patch status is contained in the filter
                    if (patchStatuses.contains(currentSystem.getPatchStatus())) {
                        ret.add(currentSystem);
                    }
                }

                // Start working on the new system
                currentSystem = new CVEAuditSystemBuilder(systemID);
                currentSystem.setSystemName(result.getName());

                // Ignore old products as long as there is a patch available elsewhere
                ignoreOldProducts = true;

                // First assignment
                patchedPackageNames.put(packageName,
                        result.isPackageInstalled());
                if (!result.isPackageInstalled()) {
                    channelAssignedPackageNames.put(packageName,
                            result.isChannelAssigned());
                }

                // Get errata and channel ID
                currentErrata = result.getErrataId();
                final Optional<Long> channelID = result.getChannelId();

                // Add these to the current system
                hasErrata = currentErrata.isPresent();
                if (hasErrata) {
                    // We have an errata
                    ErrataIdAdvisoryPair errata = new ErrataIdAdvisoryPair(
                            currentErrata.get(), result.getErrataAdvisory());
                    if ((lowestRank.isPresent() &&
                            result.getChannelRank().isPresent() &&
                            lowestRank.get() >= result.getChannelRank().get()) || !lowestRank.isPresent()
                    ) {
                        currentSystem.addErrata(errata);
                        lowestRank = result.getChannelRank();
                    }
                }
                if (channelID.isPresent()) {
                    ChannelIdNameLabelTriple channel =
                            new ChannelIdNameLabelTriple(channelID.get(),
                                    result.getChannelName(),
                                    result.getChannelLabel());


                    if ((lowestRank.isPresent() &&
                            result.getChannelRank().isPresent() &&
                            lowestRank.get() >= result.getChannelRank().get()) || !lowestRank.isPresent()
                    ) {
                        currentSystem.addChannel(channel);
                        lowestRank = result.getChannelRank();
                    }
                }
            }
            else {
                // Consider old products only if there is no patch in current or future
                // products, channel rank >= 100000 indicates older products
                final Long channelRank = result.getChannelRank().orElse(null);
                if (channelRank >= 100000 && currentSystem.getErratas().isEmpty()) {
                    ignoreOldProducts = false;
                }
                if (channelRank >= 100000 && ignoreOldProducts) {
                    continue;
                }

                // NOT a new system, check if we are still looking at the same errata
                final Optional<Long> errataID = result.getErrataId();
                if (errataID.equals(currentErrata)) {
                    // At the end the entry should be true if the package name
                    // is patched once false otherwise (but should still appear
                    // in the map)
                    Boolean patched = false;
                    if (patchedPackageNames.containsKey(packageName)) {
                        patched = patchedPackageNames.get(packageName);
                    }
                    patchedPackageNames.put(packageName, patched ||
                            result.isPackageInstalled());

                    // similar, if for each package name, at least one has an assigned
                    // channel, we are fine
                    if (!result.isPackageInstalled()) {
                        Boolean assigned = false;
                        if (channelAssignedPackageNames.containsKey(packageName)) {
                            assigned = channelAssignedPackageNames.get(packageName);
                        }
                        channelAssignedPackageNames.put(packageName, assigned ||
                                result.isChannelAssigned());
                    }
                }
                else {
                    // Switch to the new errata
                    currentErrata = errataID;
                    // At the end the entry should be true if the package name is
                    // patched once false otherwise (but should still appear in the map)
                    Boolean patched = false;
                    if (patchedPackageNames.containsKey(packageName)) {
                        patched = patchedPackageNames.get(packageName);
                    }
                    patchedPackageNames.put(packageName, patched ||
                            result.isPackageInstalled());

                    // similar, if for each package name, at least one has an assigned
                    // channel, we are fine
                    if (!result.isPackageInstalled()) {
                        Boolean assigned = false;
                        if (channelAssignedPackageNames.containsKey(packageName)) {
                            assigned = channelAssignedPackageNames.get(packageName);
                        }
                        channelAssignedPackageNames.put(packageName, assigned ||
                                result.isChannelAssigned());
                    }
                }

                // Add errata and channel ID
                if (errataID.isPresent()) {
                    ErrataIdAdvisoryPair errata = new ErrataIdAdvisoryPair(
                            errataID.get(), result.getErrataAdvisory());
                    if ((lowestRank.isPresent() &&
                        result.getChannelRank().isPresent() &&
                        lowestRank.get() >= result.getChannelRank().get()) || !lowestRank.isPresent()
                    ) {
                        currentSystem.addErrata(errata);
                        lowestRank = result.getChannelRank();
                    }
                }
                ChannelIdNameLabelTriple channel =
                        new ChannelIdNameLabelTriple(result.getChannelId().get(),
                                result.getChannelName(),
                                result.getChannelLabel());
                if ((lowestRank.isPresent() &&
                    result.getChannelRank().isPresent() &&
                    lowestRank.get() >= result.getChannelRank().get()) || !lowestRank.isPresent()
                ) {
                    currentSystem.addChannel(channel);
                    lowestRank = result.getChannelRank();
                }
            }
        }

        // Finish up the *very* last system record
        if (currentSystem != null) {
            if (usesLivePatchingDefault &&
                    patchedPackageNames.containsKey(KERNEL_DEFAULT_NAME)) {
                patchedPackageNames.remove(KERNEL_DEFAULT_NAME);
                channelAssignedPackageNames.remove(KERNEL_DEFAULT_NAME);
            }
            if (usesLivePatchingXen &&
                    patchedPackageNames.containsKey(KERNEL_XEN_NAME)) {
                patchedPackageNames.remove(KERNEL_XEN_NAME);
                channelAssignedPackageNames.remove(KERNEL_XEN_NAME);
            }
            boolean allPackagesForAllErrataInstalled = true;
            for (Boolean isPatched : patchedPackageNames.values()) {
                allPackagesForAllErrataInstalled &= isPatched;
            }

            boolean oneChannelForPackageAssigned = true;
            for (Boolean isAssigned : channelAssignedPackageNames.values()) {
                oneChannelForPackageAssigned &= isAssigned;
            }

            currentSystem.setPatchStatus(
                    getPatchStatus(allPackagesForAllErrataInstalled,
                            oneChannelForPackageAssigned, hasErrata)
            );

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
        return result.get(key) != null && ((Number) result.get(key)).intValue() == 1;
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
        Map<String, Object> params = new HashMap<>();
        params.put("cve_identifier", cveIdentifier);
        DataResult<Map<String, Object>> results = m.execute(params);
        Long count = (Long) results.get(0).get("count");
        return count == 0;
    }

    /**
     * Gets the patch status for a set of flags
     *
     * @param allPackagesForAllErrataInstalled true if system has all relevant
     * packages installed
     * @param allChannelsForOneErrataAssigned the true if system has all
     * channels for at least one relevant errata assigned
     * @param hasErrata true if query row has an errata ID
     * @return the patch status
     */
    public static PatchStatus getPatchStatus(
            boolean allPackagesForAllErrataInstalled,
            boolean allChannelsForOneErrataAssigned, boolean hasErrata) {
        if (hasErrata) {
            if (allPackagesForAllErrataInstalled) {
                return PatchStatus.PATCHED;
            }
            else if (allChannelsForOneErrataAssigned) {
                return PatchStatus.AFFECTED_PATCH_APPLICABLE;
            }
            else {
                return PatchStatus.AFFECTED_PATCH_INAPPLICABLE;
            }
        }
        else {
            return PatchStatus.NOT_AFFECTED;
        }
    }

    /**
     * Add a list of channels to the set of channels relevant for a given server.
     *
     * @param relevantChannels the relevant channels
     * @param channels the channels
     * @param ranking the ranking
     * @return true as soon as at least one channel record has been added
     */
    private static boolean addRelevantChannels(List<RankedChannel> relevantChannels,
        List<Channel> channels, int ranking) {
        boolean added = false;
        for (Channel c : channels) {
            boolean result = relevantChannels.add(new RankedChannel(c.getId(), ranking));
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
    private static void debugLog(List<CVEAuditSystemBuilder> results) {
        if (log.isDebugEnabled()) {
            log.debug("Returning " + results.size() + " results");
            for (CVEAuditSystemBuilder s : results) {
                String errata = "";
                for (ErrataIdAdvisoryPair e : s.getErratas()) {
                    errata += " " + e.getId();
                }
                String channels = "";
                for (ChannelIdNameLabelTriple c : s.getChannels()) {
                    channels += " " + c.getId();
                }
                log.debug(s.getId() + ": " + s.getPatchStatus() +
                        " (patches: " + errata + ")" +
                        " (channels: " + channels + ")");
            }
        }
    }
}
