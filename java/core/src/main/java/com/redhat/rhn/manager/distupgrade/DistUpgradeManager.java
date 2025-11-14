/*
 * Copyright (c) 2012--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.manager.distupgrade;

import static com.suse.utils.Lists.listOfListComparator;
import static java.util.stream.Collectors.toList;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.util.RpmVersionComparator;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.dup.DistUpgradeAction;
import com.redhat.rhn.domain.action.dup.DistUpgradeActionDetails;
import com.redhat.rhn.domain.action.dup.DistUpgradeChannelTask;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ClonedChannel;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductExtension;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.product.SUSEProductSet;
import com.redhat.rhn.domain.product.SUSEProductUpgrade;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.EssentialChannelDto;
import com.redhat.rhn.frontend.dto.SUSEProductDto;
import com.redhat.rhn.manager.BaseManager;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.content.ContentSyncManager;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.utils.Lists;
import com.suse.utils.Opt;

import org.apache.commons.collections.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.function.LongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Business logic for performing distribution upgrades.
 *
 */
public class DistUpgradeManager extends BaseManager {

    // Logger for this class
    private static final Logger LOG = LogManager.getLogger(DistUpgradeManager.class);

    private static final String DISTUPGRADE_QUERIES_STRING = "distupgrade_queries";

    /**
     * For a given system, return true if distribution upgrades are supported.
     *
     * @param server server
     * @param user user
     * @return true if distribution upgrades are supported, else false.
     */
    public static boolean isUpgradeSupported(Server server, User user) {
        SelectMode m = ModeFactory.getMode(DISTUPGRADE_QUERIES_STRING,
                "system_dup_supported");
        HashMap<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("sid", server.getId());
        DataResult<Map<String, ? extends Number>> dr = m.execute(params);
        return dr.get(0).get("count").intValue() > 0;
    }

    /**
     * Find migration target products for any given SUSE product ID.
     *
     * @param productId SUSE product ID
     * @return list of possible migration target product IDs
     */
    public static List<SUSEProductDto> findTargetProducts(long productId) {
        SelectMode m = ModeFactory.getMode(DISTUPGRADE_QUERIES_STRING, "find_target_products");
        HashMap<String, Object> params = new HashMap<>();
        params.put("product_id", productId);
        return m.execute(params);
    }

    /**
     * Find migration source products for any given SUSE product ID.
     *
     * @param productId SUSE product ID
     * @return list of possible migration source product IDs
     */
    public static List<SUSEProductDto> findSourceProducts(long productId) {
        SelectMode m = ModeFactory.getMode(DISTUPGRADE_QUERIES_STRING, "find_source_products");
        HashMap<String, Object> params = new HashMap<>();
        params.put("product_id", productId);
        return m.execute(params);
    }

    /**
     * Return all child channels *required* for migrating to a certain
     * {@link SUSEProductSet}.
     *
     * @param productSet product set
     * @param baseChannelID the base channel id for the product set
     * @return list of channel DTOs
     */
    public static List<EssentialChannelDto> getRequiredChannels(SUSEProductSet productSet, long baseChannelID) {
        List<Long> productIDs = productSet.getProductIDs();
        HashMap<String, Object> params = new HashMap<>();
        params.put("base_channel_id", baseChannelID);
        SelectMode m = ModeFactory.getMode(DISTUPGRADE_QUERIES_STRING, "channels_required_for_product_set");
        return m.execute(params, productIDs);
    }

    /**
     * For a given product, return the respective base channel as
     * {@link EssentialChannelDto}. The architecture is needed here to filter out base
     * channels with invalid architectures in case of a product without arch attribute.
     *
     * @param productID product ID
     * @param arch channel arch
     * @return base channel
     */
    public static EssentialChannelDto getProductBaseChannelDto(long productID, ChannelArch arch) {
        HashMap<String, Object> params = new HashMap<>();
        params.put("pid", productID);
        params.put("channel_arch_id", arch.getId());
        SelectMode m = ModeFactory.getMode("Channel_queries", "suse_base_channels_for_suse_product");
        List<EssentialChannelDto> channels = makeDataResult(params, null, null, m);
        EssentialChannelDto ret = null;
        if (!channels.isEmpty()) {
            ret = channels.get(0);
        }
        if (channels.size() > 1) {
            LOG.warn("More than one base channel found for product: {} (arch: {})", productID, arch.getName());
        }
        return ret;
    }

    /**
     * For a given product, return the respective base channel as {@link Channel}.
     *
     * @param productID product ID
     * @param arch channel arch
     * @param user user
     * @return base channel
     */
    public static Channel getProductBaseChannel(long productID, ChannelArch arch, User user) {
        Channel ret = null;
        EssentialChannelDto channelDto = getProductBaseChannelDto(productID, arch);
        if (channelDto != null) {
            ret = ChannelFactory.lookupByIdAndUser(channelDto.getId(), user);
            if (ret == null) {
                LOG.error("Channel lookup failure. No permissions for user {} on channel {}",
                        user.getLogin(), channelDto.getLabel());
            }
        }
        else {
            LOG.error("No Base Channel found for product id: {}", productID);
        }
        return ret;
    }

    public static final Comparator<SUSEProduct> PRODUCT_VERSION_COMPARATOR =
        Comparator.comparing(SUSEProduct::getVersion, new RpmVersionComparator())
            .thenComparing(SUSEProduct::getRelease, new RpmVersionComparator());

    public static final Comparator<List<SUSEProduct>> PRODUCT_LIST_VERSION_COMPARATOR =
        listOfListComparator(PRODUCT_VERSION_COMPARATOR);

    public static final Comparator<SUSEProductSet> PRODUCT_SET_VERSION_COMPARATOR =
        Comparator.comparing(SUSEProductSet::getBaseProduct, PRODUCT_VERSION_COMPARATOR.reversed())
            .thenComparing(SUSEProductSet::getAddonProducts, PRODUCT_LIST_VERSION_COMPARATOR.reversed());

    /**
     * Calculate the valid migration targets for a given server
     * @param server the server
     * @param user the user
     * @return valid migration targets
     */
    public static List<SUSEProductSet> getTargetProductSets(Server server, User user) {
        Optional<SUSEProductSet> installedProductSet = server.getInstalledProductSet();
        ChannelArch compatibleChannelArch = server.getServerArch().getCompatibleChannelArch();

        return getTargetProductSets(installedProductSet, compatibleChannelArch, user);
    }

    /**
     * Calculate the valid migration targets for a given product set.
     *
     * @param installedProducts current product set
     * @param arch the channel architecture
     * @param user the user
     * @return valid migration targets
     */
    public static List<SUSEProductSet> getTargetProductSets(
            Optional<SUSEProductSet> installedProducts, ChannelArch arch, User user) {
        List<SUSEProductSet> migrationTargets = migrationTargets(installedProducts);
        migrationTargets.sort(PRODUCT_SET_VERSION_COMPARATOR);
        return addMissingChannels(migrationTargets, arch, user);
    }

    private static List<SUSEProductSet> addMissingChannels(
            List<SUSEProductSet> migrationTargets, ChannelArch arch, User user) {
        for (SUSEProductSet target : migrationTargets) {
            // Look for the target product's base channel
            Channel baseChannel = getProductBaseChannel(target.getBaseProduct().getId(), arch, user);

            if (baseChannel == null) {
                // No base channel found
                target.addMissingChannel(target.getBaseProduct().getFriendlyName());
                LOG.warn("Missing Base Channels for {}", target.getBaseProduct().getFriendlyName());
            }
            else {
                // Check for addon product channels only if base channel is synced
                if (target.getIsEveryChannelSynced()) {
                    for (SUSEProduct addonProduct : target.getAddonProducts()) {
                        // Look for mandatory child channels
                        List<String> missing =
                                SUSEProductFactory.findAllMandatoryChannels(addonProduct, target.getBaseProduct())
                                        .filter(pr -> ChannelFactory.lookupByLabel(pr.getChannelLabel()) == null)
                                        .map(pr -> {
                                            LOG.warn("Mandatory channel not synced: {}", pr.getChannelLabel());
                                            return pr.getChannelLabel();
                                        }).toList();
                        target.addMissingChannels(missing);
                    }
                }
            }
        }
        return migrationTargets;
    }

    /**
     * Get all available migration targets from the installed products
     * on the system
     * Please note that ruby syntax comments in the code are referred
     * to the private project source at:
     * - https://github.com/SUSE/happy-customer/blob/
     *       761eaad2bcb0fcc506c545442ea860a041debf27/glue/app/models/migration_engine.rb
     *
     * @param installedProducts all the installed products on the migrating system
     * @return list of available migration targets
     */
    private static List<SUSEProductSet> migrationTargets(Optional<SUSEProductSet> installedProducts) {
        return Opt.fold(installedProducts,
                () -> {
                    LOG.warn("No products installed on this system");
                    return new LinkedList<>();
                },
                prd -> {
                    SUSEProduct baseProduct = prd.getBaseProduct();
                    if (baseProduct == null) {
                        LOG.warn("No base product found");
                        return new LinkedList<>();
                    }
                    List<SUSEProduct> installedExtensions = prd.getAddonProducts();
                    final List<SUSEProduct> baseSuccessors = getSuccessorsForBaseProduct(baseProduct);
                    final List<SUSEProduct> currentCombination = new ArrayList<>(installedExtensions.size() + 1);
                    currentCombination.add(baseProduct);
                    currentCombination.addAll(installedExtensions);
                    final List<List<SUSEProduct>> extSuccessors = getExtSuccessorsForInstalledExt(installedExtensions);
                    List<List<SUSEProduct>> combinations = baseSuccessors.stream()
                            .flatMap(baseSucc -> combineCompatibleExtensionSuccessor(extSuccessors, baseSucc).stream())
                            .filter(comb -> !comb.equals(List.of(baseProduct)) && !comb.equals(currentCombination))
                            .collect(toList());
                    return getMigrationTargetProductSets(combinations);
                }
        );
    }

    private static List<SUSEProduct> getSuccessorsForBaseProduct(SUSEProduct baseProduct) {
        final List<SUSEProduct> baseSuccessors = new ArrayList<>(baseProduct.getUpgrades().size() + 1);
        baseSuccessors.add(baseProduct);
        baseSuccessors.addAll(baseProduct.getUpgrades());
        if (baseProduct.getUpgrades().isEmpty()) {
            LOG.warn("No upgdrades found for base product {}", baseProduct.getFriendlyName());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Found '{}' successors for the base product.", baseSuccessors.size());
            baseSuccessors.forEach(bp -> LOG.debug(bp.getFriendlyName()));
        }
        return baseSuccessors;
    }

    private static List<List<SUSEProduct>> getExtSuccessorsForInstalledExt(List<SUSEProduct> installedExtensions) {
        final List<List<SUSEProduct>> extensionSuccessors = new ArrayList<>(installedExtensions.size());
        for (SUSEProduct e : installedExtensions) {
            final List<SUSEProduct> s = new ArrayList<>(e.getUpgrades().size() + 1);
            s.add(e);
            s.addAll(e.getUpgrades());
            extensionSuccessors.add(s);
            if (e.getUpgrades().isEmpty()) {
                LOG.warn("No upgdrades found for installed extension {}", e.getFriendlyName());
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Extension: {}", e.getFriendlyName());
                e.getUpgrades().forEach(ex -> LOG.debug("Extension successor: {}", ex.getFriendlyName()));
                LOG.debug("-----------------------");
            }
        }
        return extensionSuccessors;
    }

    private static List<List<SUSEProduct>> combineCompatibleExtensionSuccessor(
            List<List<SUSEProduct>> extensionSuccessors, SUSEProduct baseSucc
    ) {
        // first compute extensions successors compatible with the base successor
        List<List<SUSEProduct>> compatibleExtensionSuccessors = extensionSuccessors.stream()
                .map(extensionSucc -> extensionSucc.stream()
                        .filter(succ -> extAvailableForRoot(succ, baseSucc))
                        .collect(toList()))
                .filter(list -> !list.isEmpty())
                .collect(toList());
        if (compatibleExtensionSuccessors.isEmpty()) {
            LOG.warn("No extension successors for base successor {}", baseSucc.getFriendlyName());
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Found extension successors for base successor {}:",
                    baseSucc.getFriendlyName());
            // let's print out list of list with friendly names
            compatibleExtensionSuccessors.stream()
                    .map(css -> css.stream().map(SUSEProduct::getFriendlyName).collect(toList()))
                    .forEach(LOG::debug);
            LOG.debug("-----------------------");
        }
        // the base successor will be always on the 1st position in the combinations below
        compatibleExtensionSuccessors.add(0, List.of(baseSucc));
        return Lists.combinations(compatibleExtensionSuccessors);
    }

    private static List<SUSEProductSet> getMigrationTargetProductSets(List<List<SUSEProduct>> combinations) {
        final List<SUSEProductSet> result = new LinkedList<>();
        for (List<SUSEProduct> combination : combinations) {
            result.addAll(processCombination(combination));
        }
        return result;
    }

    private static List<SUSEProductSet> processCombination(List<SUSEProduct> combination) {
        final List<SUSEProductSet> result = new LinkedList<>();
        ContentSyncManager mgr = new ContentSyncManager();
        SUSEProduct base = combination.get(0);
        if (!mgr.isProductAvailable(base, base)) {
            LOG.warn("No SUSE Product Channels for {}. Skipping", base.getFriendlyName());
            return result;
        }
        // No Product Channels means, no subscription to access the channels
        if (combination.size() == 1) {
            LOG.debug("Found Target: {}", base.getFriendlyName());
            result.add(new SUSEProductSet(base, Collections.emptyList()));
        }
        else {
            List<SUSEProduct> addonProducts = ensureRecommendedAddons(base,
                    combination.subList(1, combination.size()));
            addLibertyLinuxAddonIfMissing(base, addonProducts);
            // No Product Channels means, no subscription to access the channels
            if (addonProducts.stream().anyMatch(ap -> !mgr.isProductAvailable(ap, base))) {
                logUnavailableAddons(addonProducts, base, mgr);
                return result;
            }
            LOG.debug("Found Target: {}", base.getFriendlyName());
            addonProducts.forEach(ext -> LOG.debug("   - {}", ext.getFriendlyName()));
            result.add(new SUSEProductSet(base, addonProducts));
        }
        return result;
    }

    private static void logUnavailableAddons(List<SUSEProduct> addonProducts, SUSEProduct base,
                                             ContentSyncManager mgr) {
        addonProducts.stream()
                .filter(ap -> !mgr.isProductAvailable(ap, base))
                .forEach(ap -> LOG.warn("No SUSE Product Channels for {}. Skipping {}",
                        ap.getFriendlyName(), base.getFriendlyName()));
    }

    private static List<SUSEProduct> ensureRecommendedAddons(SUSEProduct baseIn, List<SUSEProduct> addonProducts) {
        return  Stream.concat(
                addonProducts.stream(),
                SUSEProductFactory.allRecommendedExtensionsOfRoot(baseIn)
                        .stream()
                        .map(SUSEProductExtension::getExtensionProduct)
                        .filter(p -> !addonProducts.contains(p))
        ).collect(Collectors.toList());
    }

    /**
     * Liberty Migration should always add the Liberty Products
     * If the migration ends in Liberty linux, add Suse Liberty Linux extension (if not already present)
     *
     * @param baseProduct   the base product
     * @param addonProducts the list of addon products
     */
    private static void addLibertyLinuxAddonIfMissing(SUSEProduct baseProduct, List<SUSEProduct> addonProducts) {
        List<SUSEProduct> liberties = new ArrayList<>();

        if (baseProduct.getName().equals("rhel-base") &&
                baseProduct.getVersion().equals("8")) {
            liberties = SUSEProductFactory.findAllExtensionsOfRootProduct(baseProduct)
                    .stream()
                    .filter(e -> e.getName().equals("res"))
                    .collect(Collectors.toList());
        }
        else if (baseProduct.getName().equals("el-base") &&
                baseProduct.getVersion().equals("9")) {
            liberties = SUSEProductFactory.findAllExtensionsOfRootProduct(baseProduct)
                    .stream()
                    .filter(e -> e.getName().equals("sll"))
                    .collect(Collectors.toList());
        }

        for (SUSEProduct liberty : liberties) {
            if (addonProducts.stream().noneMatch(e -> e.getId() == liberty.getId())) {
                addonProducts.add(liberty);
            }
        }
    }

    /**
     * Returns true if the extension is linked with given root product.
     *
     * @param extension the extension product
     * @param root the root product
     * @return true if the extension is linked with given root product, false otherwise
     */
    private static boolean extAvailableForRoot(SUSEProduct extension, SUSEProduct root) {
        return !SUSEProductFactory.findAllBaseProductsOf(extension, root).isEmpty();
    }

    /**
     * Return *all* clones of a given channel.
     *
     * @param channel channel
     * @return list of cloned channels
     */
    public static List<ClonedChannel> getAllClones(Channel channel) {
        List<ClonedChannel> ret = new ArrayList<>();
        if (channel != null) {
            Set<ClonedChannel> clones = channel.getClonedChannels();
            ret.addAll(clones);
            for (ClonedChannel clone : clones) {
                ret.addAll(getAllClones(clone));
            }
        }
        return ret;
    }

    /**
     * Return all available alternatives (cloned channels) for a given migration target
     * {@link SUSEProductSet}. These will be returned as a {@link HashMap} where keys are
     * the {@link ClonedChannel}s itself and values are the list of child channel IDs
     * required for migrating to the given {@link SUSEProductSet}.
     *
     * @param targetProducts target product set
     * @param arch channel arch
     * @param user user
     * @return alternative target product sets
     */
    public static SortedMap<ClonedChannel, List<Long>> getAlternatives(
            SUSEProductSet targetProducts, ChannelArch arch, User user) {
        // This list will be returned
        TreeMap<ClonedChannel, List<Long>> alternatives = new TreeMap<>();
        // Get base channel
        Channel suseBaseChannel = getProductBaseChannel(
                targetProducts.getBaseProduct().getId(), arch, user);
        // Get all clones
        List<ClonedChannel> allClones = getAllClones(suseBaseChannel);
        // Get required channels for this product set
        List<EssentialChannelDto> requiredChildChannels =
                getRequiredChannels(targetProducts, suseBaseChannel.getId());
        // For all possible alternatives (clones of base channel)
        for (ClonedChannel clone : allClones) {
            // SKip this channel if it's not a base channel
            if (!clone.isBaseChannel()) {
                continue;
            }
            // Init the list of required channel IDs
            // if the required channels ids is empty no valid alternative has been found
            // if the target product has no required child channels add the clone as
            // valid alternative regardless
            if (!requiredChildChannels.isEmpty()) {
                List<Long> requiredChannelIDs =
                        getValidRequiredChannelIDs(user, clone, requiredChildChannels, suseBaseChannel);
                if (!requiredChannelIDs.isEmpty()) {
                    alternatives.put(clone, requiredChannelIDs);
                }
            }
            else {
                alternatives.put(clone, Collections.emptyList());
            }
        }
        return alternatives;
    }

    private static List<Long> getValidRequiredChannelIDs(
            User user,
            Channel clone,
            List<EssentialChannelDto> requiredChildChannels,
            Channel suseBaseChannel) {
        // Init the list of required channel IDs
        List<Long> requiredChannelIDs = new ArrayList<>();
        // Get all child channels of this clone
        List<Channel> children = clone.getAccessibleChildrenFor(user);
        // All product channels need to be available as clones!
        for (EssentialChannelDto c : requiredChildChannels) {
            boolean foundChild = false;
            long id = c.getId();
            for (Channel child : children) {
                // Go back to the original channel and compare the IDs
                Channel childOriginal = ChannelManager.getOriginalChannel(child);
                if (childOriginal.getId() == id) {
                    // This child's checkbox needs to be selected!
                    requiredChannelIDs.add(child.getId());
                    foundChild = true;
                    break;
                }
            }
            if (!foundChild) {
                LOG.debug("""
                              Discarding cloned channel '{}' of base channel '{}' as a migration alternative. \
                              The cloned channel doesn't have required child channels. Required child channels: '{}', \
                              accessible child channels of the clone: '{}'.""",
                        clone, suseBaseChannel, requiredChildChannels, children);
                break;
            }
        }
        return requiredChannelIDs;
    }

    /**
     * Look for the migration target of a given source within a list of products.
     *
     * @param source source product
     * @param targets target products
     * @return matching target product
     */
    public static SUSEProduct findTarget(SUSEProduct source, List<SUSEProduct> targets) {
        return findMatch(source, targets, DistUpgradeManager::findTargetProducts);
    }

    /**
     * Look for the source of a given migration target within a list of products.
     *
     * @param target the target product
     * @param sources all the sources of the migration
     * @return matching source product
     */
    public static SUSEProduct findSource(SUSEProduct target, List<SUSEProduct> sources) {
        return findMatch(target, sources, DistUpgradeManager::findSourceProducts);
    }

    private static SUSEProduct findMatch(SUSEProduct productToMatch, List<SUSEProduct> productPool,
                                         LongFunction<List<SUSEProductDto>> matchingFunction) {
        // Match found if the targets contain the source product itself
        if (productPool.contains(productToMatch)) {
            return productToMatch;
        }

        List<SUSEProductDto> results = matchingFunction.apply(productToMatch.getId());
        SUSEProduct matchingProduct = null;

        for (SUSEProduct product : productPool) {
            for (SUSEProductDto result : results) {
                if (result.getId().equals(product.getId())) {
                    // Found the matching product
                    matchingProduct = product;
                    break;
                }
            }
        }

        return matchingProduct;
    }

    /**
     * Perform dist upgrade related checks on a server to throw exceptions accordingly.
     * Will return a {@link Server} object for a given ID.
     *
     * @param sid ID of the server to check
     * @param user the calling user
     * @return server object
     * @throws DistUpgradeException in case checks fail
     */
    public static Server performServerChecks(Long sid, User user) throws DistUpgradeException {
        Server server = SystemManager.lookupByIdAndUser(sid, user);

        if (server.asMinionServer().isEmpty()) {
            // Check if server supports distribution upgrades
            boolean supported = DistUpgradeManager.isUpgradeSupported(server, user);
            if (!supported) {
                throw new DistUpgradeException("Dist upgrade not supported for server: " + sid);
            }

            // Check if zypp-plugin-spacewalk is installed
            boolean zyppPluginInstalled = PackageFactory.lookupByNameAndServer(
                    "zypp-plugin-spacewalk", server) != null;
            if (!zyppPluginInstalled) {
                throw new DistUpgradeException("Package zypp-plugin-spacewalk is not installed: " + sid);
            }
        }
        else {
            Optional<MinionServer> minion = MinionServerFactory.lookupById(server.getId());
            if (minion.isEmpty() || !minion.get().isOsFamilySuse()) {
                throw new DistUpgradeException("Dist upgrade only supported for SUSE systems");
            }
        }

        // Check if the newest update stack is installed (for traditional clients only)
        if (ErrataManager.updateStackUpdateNeeded(user, server)) {
            throw new DistUpgradeException("There are outstanding Package Management " +
                    "updates available for this system.");
        }

        // Check if there is already a migration in the schedule
        if (ActionFactory.isMigrationScheduledForServer(server.getId()) != null) {
            throw new DistUpgradeException("Another dist upgrade is in the schedule for server: " + sid);
        }

        return server;
    }

    /**
     * Validate a given list of channel labels and convert to a list of IDs.
     *
     * @param channelLabels list of channel labels
     * @param user the calling user
     * @return list od channels IDs
     * @throws DistUpgradeException in case of errors
     */
    public static Set<Long> performChannelChecks(List<String> channelLabels, User user)
            throws DistUpgradeException {
        // Make sure we have exactly one base channel
        Channel baseChannel = null;
        List<Channel> childChannels = new ArrayList<>();
        for (String label : channelLabels) {
            Channel channel = ChannelManager.lookupByLabelAndUser(label, user);
            if (channel.isBaseChannel()) {
                if (baseChannel != null) {
                    throw new DistUpgradeException("More than one base channel given for dist upgrade");
                }
                baseChannel = channel;
            }
            else {
                childChannels.add(channel);
            }
        }
        if (baseChannel == null) {
            throw new DistUpgradeException("No base channel given for dist upgrade");
        }

        // Check validity of child channels
        Set<Long> channelIDs = new HashSet<>();
        channelIDs.add(baseChannel.getId());
        for (Channel channel : childChannels) {
            if (!channel.getParentChannel().getLabel().equals(baseChannel.getLabel())) {
                throw new DistUpgradeException(
                        "Channel has incompatible base channel: " + channel.getLabel());
            }
            channelIDs.add(channel.getId());
        }
        return channelIDs;
    }

    /**
     * Schedule a distribution upgrade for a given server.
     * (private as it does not take PAYG into account)
     *
     * @param user the user who is scheduling
     * @param serverList the list of servers to migrate
     * @param targetSet set of target products (base product and addons)
     * @param channelIDs IDs of all channels to subscribe
     * @param dryRun perform a dry run
     * @param allowVendorChange allow vendor change during dist upgrade
     * @param earliest earliest schedule date
     * @param chain the action chain
     * @return the action ID
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    private static List<DistUpgradeAction> scheduleDistUpgrade(User user, List<? extends Server> serverList,
                                                               SUSEProductSet targetSet, Collection<Long> channelIDs,
                                                               boolean dryRun, boolean allowVendorChange,
                                                               Date earliest, ActionChain chain)
            throws TaskomaticApiException, NoInstalledProductException {

        Map<Long, DistUpgradeActionDetails> detailsMap = new HashMap<>();
        for (Server server : serverList) {
            // Create action details
            var details = createDistUpgradeActionDetails(server, targetSet, channelIDs, allowVendorChange, dryRun);
            detailsMap.put(server.getId(), details);
        }

        // Return the singleton list of the scheduled action
        return ActionManager.scheduleDistUpgrade(user, earliest, chain, dryRun, detailsMap);
    }

    private static DistUpgradeActionDetails createDistUpgradeActionDetails(Server server, SUSEProductSet targetSet,
                                                                           Collection<Long> channelIDs,
                                                                           boolean allowVendorChange, boolean dryRun)
        throws NoInstalledProductException {

        DistUpgradeActionDetails details = new DistUpgradeActionDetails();

        // Set additional attributes
        details.setServer(server);
        details.setDryRun(dryRun);
        details.setAllowVendorChange(allowVendorChange);
        details.setFullUpdate(true);

        // Add product upgrades
        // Note: product upgrades are relevant for SLE 10 only!
        List<String> missingSuccessors = new ArrayList<>();
        SUSEProductSet installedProduct = server.getInstalledProductSet().orElseThrow(NoInstalledProductException::new);
        List<SUSEProductUpgrade> productUpgrades = getProductUpgrades(installedProduct, targetSet, missingSuccessors);

        productUpgrades.forEach(upgrade -> details.addProductUpgrade(upgrade));
        details.setMissingSuccessors(String.join(",", missingSuccessors));

        // Add individual channel tasks
        List<DistUpgradeChannelTask> channelTasks = getChannelTasks(server, channelIDs);
        channelTasks.forEach(task -> details.addChannelTask(task));

        return details;
    }

    private static List<SUSEProductUpgrade> getProductUpgrades(SUSEProductSet installedProduct,
                                                               SUSEProductSet targetSet,
                                                               List<String> missingSuccessors) {
        if (targetSet == null) {
            return List.of();
        }

        List<SUSEProductUpgrade> result = new ArrayList<>();

        // Upgrade of the base product
        result.add(new SUSEProductUpgrade(installedProduct.getBaseProduct(), targetSet.getBaseProduct()));

        // Find matching targets for every addon
        for (SUSEProduct addon : installedProduct.getAddonProducts()) {
            SUSEProduct match = DistUpgradeManager.findTarget(addon, targetSet.getAddonProducts());
            if (match != null) {
                result.add(new SUSEProductUpgrade(addon, match));
            }
            else {
                missingSuccessors.add(addon.getName());
            }
        }

        return result;
    }

    private static List<DistUpgradeChannelTask> getChannelTasks(Server server, Collection<Long> channelIDs) {
        List<DistUpgradeChannelTask> result = new ArrayList<>();

        for (Channel c : server.getChannels()) {
            // Remove channels we already subscribed
            if (channelIDs.contains(c.getId())) {
                channelIDs.remove(c.getId());
            }
            else {
                // Unsubscribe from this channel
                DistUpgradeChannelTask task = new DistUpgradeChannelTask();
                task.setChannel(c);
                task.setTask(DistUpgradeChannelTask.UNSUBSCRIBE);
                result.add(task);
            }
        }
        // Subscribe to all the remaining channels
        for (Long cid : channelIDs) {
            DistUpgradeChannelTask task = new DistUpgradeChannelTask();
            task.setChannel(ChannelFactory.lookupById(cid));
            task.setTask(DistUpgradeChannelTask.SUBSCRIBE);
            result.add(task);
        }

        return result;
    }

    /**
     * Schedule a distribution upgrade for a given server, allowing passing the PAYG flag.
     * @param user the user who is scheduling
     * @param serverList the list of servers to migrate
     * @param targetSet set of target products (base product and addons)
     * @param channelIDs IDs of all channels to subscribe
     * @param dryRun perform a dry run
     * @param allowVendorChange allow vendor change during dist upgrade
     * @param isPayg tells the method how to behave if SUMA is PAYG
     * @param earliest earliest schedule date
     * @param chain the action chain
     * @return the list of scheduled actions
     * @throws TaskomaticApiException   if there was a Taskomatic error
     * @throws DistUpgradePaygException if the SUSE Manager instance is PAYG.
     */
    public static List<DistUpgradeAction> scheduleDistUpgrade(User user, List<? extends Server> serverList,
                                                              SUSEProductSet targetSet, Collection<Long> channelIDs,
                                                              boolean dryRun, boolean allowVendorChange, boolean isPayg,
                                                              Date earliest, ActionChain chain)
            throws TaskomaticApiException, NoInstalledProductException, DistUpgradePaygException {

        if (isPayg) {
            /*
            Changing product family I.e
              - SLES 15 SP5 to SLES for SAP 15 SP4 or
              - from OpenSUSE Leap 15.4 to SLES 15 SP4
            is not allowed.
            Only SP migrations should be possible.
            Also, individual assigning channels to perform a migration is forbidden
            */
            if (targetSet != null) {
                for (Server server : serverList) {
                    SUSEProduct installedBaseProduct = server.getInstalledProductSet()
                        .map(SUSEProductSet::getBaseProduct)
                        .orElseThrow(NoInstalledProductException::new);

                    SUSEProduct targetBaseProduct = targetSet.getBaseProduct();
                    if (targetBaseProduct.getChannelFamily() == null ||
                        installedBaseProduct.getChannelFamily() == null ||
                        !targetBaseProduct.getChannelFamily().equals(installedBaseProduct.getChannelFamily())) {
                        throw new DistUpgradePaygException(
                            "In PAYG SUMA instances, changing the product family is forbidden");
                    }
                }
            }
            else {
                throw new DistUpgradePaygException("In PAYG SUMA instances, individual migrations are forbidden");
            }
        }

        return scheduleDistUpgrade(user, serverList, targetSet, channelIDs, dryRun, allowVendorChange, earliest, chain);
    }

    /**
     * Remove incompatible migration targets compared to the installed products.
     * Write the failed products in the missingSuccesorExtensions set in case it should
     * be shown somewhere.
     *
     * @param installedProducts Optional set of the installed products
     * @param allMigrationTargets all calculated migration targets
     * @return list of valid migration targets
     */
    public static List<SUSEProductSet> removeIncompatibleTargets(Optional<SUSEProductSet> installedProducts,
                                                                  List<SUSEProductSet> allMigrationTargets) {
        return removeIncompatibleTargets(installedProducts, allMigrationTargets, Optional.empty());
    }

    /**
     * Remove incompatible migration targets compared to the installed products.
     * Write the failed products in the missingSuccesorExtensions set in case it should
     * be shown somewhere.
     *
     * @param installedProducts Optional set of the installed products
     * @param allMigrationTargets all calculated migration targets
     * @param missingSuccessors OUT: info about installed extensions missing a successor
     * @return list of valid migration targets
     */
    public static List<SUSEProductSet> removeIncompatibleTargets(Optional<SUSEProductSet> installedProducts,
                                                                  List<SUSEProductSet> allMigrationTargets,
                                                                  Set<SUSEProduct> missingSuccessors) {
        return removeIncompatibleTargets(installedProducts, allMigrationTargets, Optional.of(missingSuccessors));
    }

    private static List<SUSEProductSet> removeIncompatibleTargets(Optional<SUSEProductSet> installedProducts,
                                                                 List<SUSEProductSet> allMigrationTargets,
                                                                 Optional<Set<SUSEProduct>> missingSuccessors) {
        List<SUSEProductSet> migrationTargets = new LinkedList<>();
        for (SUSEProductSet t : allMigrationTargets) {
            if (installedProducts.isPresent() && installedProducts.get().getAddonProducts().isEmpty()) {
                migrationTargets.add(t);
                LOG.debug("Found valid migration target: {}", t);
                continue;
            }
            List<SUSEProduct> missingAddonSuccessors = installedProducts.orElse(new SUSEProductSet()).getAddonProducts()
                    .stream()
                    .filter(addon -> DistUpgradeManager.findTarget(addon, t.getAddonProducts()) == null)
                    .toList();

            if (missingAddonSuccessors.isEmpty()) {
                LOG.debug("Found valid migration target: {}", t);
                migrationTargets.add(t);
            }
            else {
                if (LOG.isWarnEnabled()) {
                    String missing = missingAddonSuccessors.stream()
                        .map(SUSEProduct::getFriendlyName)
                        .collect(Collectors.joining(", "));
                    LOG.warn("No migration target found for '{}'. Skipping", missing);
                }
                missingSuccessors.ifPresent(l -> l.addAll(missingAddonSuccessors));
            }
        }
        return migrationTargets;
    }

    /**
     * Compute the most common base product among the given list of servers
     *
     * @param serverList the list of servers to evaluate
     *
     * @return the base product which has the most number of occurrences among the given servers
     */
    public static Optional<SUSEProductSet> getCommonSourceProduct(List<? extends Server> serverList) {
        // No source if no servers are specified
        if (CollectionUtils.isEmpty(serverList)) {
            return Optional.empty();
        }

        // If the list is a singleton, the result matches with its installed product set
        if (serverList.size() == 1) {
            return serverList.get(0).getInstalledProductSet();
        }

        // Compute the frequency map of every base product
        Map<SUSEProduct, Long> productSetFrequencyMap = serverList.stream()
            .flatMap(server -> server.getInstalledProductSet().stream())
            .map(SUSEProductSet::getBaseProduct)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        // Extract the most common base product
        Optional<SUSEProduct> mostCommonBaseProduct = productSetFrequencyMap.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey);

        // Merge all the addons to build the "super" base that covers all systems
        return mostCommonBaseProduct.map(
            baseProduct -> serverList.stream()
                .flatMap(server -> server.getInstalledProductSet().stream())
                .filter(productSet -> baseProduct.equals(productSet.getBaseProduct()))
                .reduce(new SUSEProductSet(baseProduct, new ArrayList<>()), SUSEProductSet::merge)
        );
    }

    /**
     * Compute all the possible targets for the migration of the given servers to the specified common source product.
     *
     * @param user the user attempting the migration
     * @param serverList the list of servers to migrate
     * @param sourceProductSet the common base product of all the systems
     *
     * @return all the possible targets for the migration. if the given servers do not share the same base product the
     * list will be empty.
     */
    public static List<SUSEProductSet> getTargetProductSets(User user, List<? extends Server> serverList,
                                                            Optional<SUSEProductSet> sourceProductSet) {
        // Ensure the base product is available
        SUSEProduct sourceBaseProduct = sourceProductSet.map(SUSEProductSet::getBaseProduct).orElse(null);
        if (sourceBaseProduct == null) {
            return List.of();
        }

        // Short-circuit the calculation if the list is a singleton
        if (serverList.size() == 1) {
            Server singletonServer = serverList.get(0);

            // Ensure the given base product matches what's installed
            if (!serverHasBaseProduct(singletonServer, sourceBaseProduct)) {
                return List.of();
            }

            return getTargetProductSets(singletonServer, user);
        }

        // Compute the possible targets aggregating all the server targets
        Map<SUSEProduct, SUSEProductSet> resultMap = serverList.stream()
            // Consider only the systems with the correct base
            .filter(server -> serverHasBaseProduct(server, sourceBaseProduct))
            // Compute the target product sets for this server
            .map(server -> DistUpgradeManager.getTargetProductSets(server, user))
            // Build a map where the key is the target base product and the value is the full product set
            .map(targetsList -> targetsList.stream()
                .collect(Collectors.toMap(productSet -> productSet.getBaseProduct(), Function.identity()))
            )
            // Combine the maps in a single one, merging the addons products and the missing channels
            .reduce(new HashMap<>(), (accumulatedMap, targetProductsMap) -> {
                targetProductsMap.values().forEach(productSet ->
                    accumulatedMap.merge(productSet.getBaseProduct(), productSet, SUSEProductSet::merge)
                );

                return accumulatedMap;
            });

        // Extract only the values to build the final result list
        return resultMap.values().stream()
            .sorted(DistUpgradeManager.PRODUCT_SET_VERSION_COMPARATOR)
            .toList();
    }

    // Returns true if the given server has the specified base product installed
    private static boolean serverHasBaseProduct(Server server, SUSEProduct sourceBaseProduct) {
        return server.getInstalledProductSet()
            .map(SUSEProductSet::getBaseProduct)
            .filter(installedBaseProduct -> sourceBaseProduct.equals(installedBaseProduct))
            .isPresent();
    }

    /**
     * List all the products that are potentially going to be installed using the specified targets
     * @param installedProductSet the currently installed product set
     * @param migrationTargets all the possible migration targets
     * @return the aggregated list of products that are not currently part of the installed product set and are going
     * to be installed when migrating to one of the specified targets
     */
    public static Set<SUSEProduct> listInstalledTargetAddons(Optional<SUSEProductSet> installedProductSet,
                                                             List<SUSEProductSet> migrationTargets) {

        Set<SUSEProduct> result = new HashSet<>();
        List<SUSEProduct> installedAddons = installedProductSet.map(SUSEProductSet::getAddonProducts).orElse(List.of());

        // Find all the targets that don't have a matching base
        migrationTargets.stream()
            .flatMap(target -> target.getAddonProducts().stream())
            .filter(targetAddon -> DistUpgradeManager.findSource(targetAddon, installedAddons) == null)
            .forEach(result::add);

        return result;
    }
}
