/*
 * Copyright (c) 2012 SUSE LLC
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

package com.redhat.rhn.manager.distupgrade;

import static com.suse.utils.Lists.listOfListComparator;
import static java.util.stream.Collectors.toList;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.util.RpmVersionComparator;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.dup.DistUpgradeActionDetails;
import com.redhat.rhn.domain.action.dup.DistUpgradeChannelTask;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ClonedChannel;
import com.redhat.rhn.domain.product.SUSEProduct;
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
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Business logic for performing distribution upgrades.
 *
 */
public class DistUpgradeManager extends BaseManager {

    // Logger for this class
    private static Logger logger = LogManager.getLogger(DistUpgradeManager.class);

    /**
     * For a given system, return true if distribution upgrades are supported.
     *
     * @param server server
     * @param user user
     * @return true if distribution upgrades are supported, else false.
     */
    public static boolean isUpgradeSupported(Server server, User user) {
        SelectMode m = ModeFactory.getMode("distupgrade_queries",
                "system_dup_supported");
        HashMap<String, Object> params = new HashMap<>();
        params.put("user_id", user.getId());
        params.put("sid", server.getId());
        @SuppressWarnings("unchecked")
        DataResult<Map<String, ? extends Number>> dr = m.execute(params);
        return dr.get(0).get("count").intValue() > 0;
    }

    /**
     * Find migration target products for any given SUSE product ID.
     *
     * @param productId SUSE product ID
     * @return list of possible migration target product IDs
     */
    @SuppressWarnings("unchecked")
    public static List<SUSEProductDto> findTargetProducts(long productId) {
        SelectMode m = ModeFactory.getMode("distupgrade_queries", "find_target_products");
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
    @SuppressWarnings("unchecked")
    public static List<SUSEProductDto> findSourceProducts(long productId) {
        SelectMode m = ModeFactory.getMode("distupgrade_queries", "find_source_products");
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
    @SuppressWarnings("unchecked")
    public static List<EssentialChannelDto> getRequiredChannels(SUSEProductSet productSet, long baseChannelID) {
        List<Long> productIDs = productSet.getProductIDs();
        HashMap<String, Object> params = new HashMap<>();
        params.put("base_channel_id", baseChannelID);
        SelectMode m = ModeFactory.getMode("distupgrade_queries", "channels_required_for_product_set");
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
    @SuppressWarnings("unchecked")
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
            logger.warn("More than one base channel found for product: {} (arch: {})", productID, arch.getName());
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
                logger.error("Channel lookup failure. No permissions for user {} on channel {}",
                        user.getLogin(), channelDto.getLabel());
            }
        }
        else {
            logger.error("No Base Channel found for product id: {}", productID);
        }
        return ret;
    }

    public static final Comparator<SUSEProduct> PRODUCT_VERSION_COMPARATOR = (o1, o2) -> {
        int result = new RpmVersionComparator().compare(
                o1.getVersion(), o2.getVersion());
        if (result != 0) {
            return result;
        }
        return new RpmVersionComparator().compare(o1.getRelease(), o2.getRelease());
    };

    public static final Comparator<List<SUSEProduct>> PRODUCT_LIST_VERSION_COMPARATOR =
            listOfListComparator(PRODUCT_VERSION_COMPARATOR);


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
        migrationTargets.sort((tgt1, tgt2) -> {
            int i = PRODUCT_VERSION_COMPARATOR.compare(tgt2.getBaseProduct(), tgt1.getBaseProduct());
            if (i != 0) {
                return i;
            }
            else {
                return PRODUCT_LIST_VERSION_COMPARATOR.compare(tgt2.getAddonProducts(), tgt1.getAddonProducts());
            }
        });
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
                logger.debug("Missing Base Channels for {}", target.getBaseProduct().getFriendlyName());
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
                                    logger.warn("Mandatory channel not synced: {}", pr.getChannelLabel());
                                    return pr.getChannelLabel();
                                }).collect(toList());
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
     *
     * Please note that ruby syntax comments in the code are referred
     * to the private project source at:
     * - https://github.com/SUSE/happy-customer/blob/
     *       761eaad2bcb0fcc506c545442ea860a041debf27/glue/app/models/migration_engine.rb
     *
     * @param installedProducts all the installed products on the migrating system
     * @return list of available migration targets
     */
    private static List<SUSEProductSet> migrationTargets(Optional<SUSEProductSet> installedProducts) {
        final List<SUSEProductSet> result = new LinkedList<>();
        return Opt.fold(installedProducts,
            () -> {
                logger.warn("No products installed on this system");
                return result;
            },
            prd -> {
                SUSEProduct baseProduct = prd.getBaseProduct();
                if (baseProduct == null) {
                    logger.warn("No base product found");
                    return result;
                }

                // installed_extensions = @installed_products - [base_product]
                List<SUSEProduct> installedExtensions = prd.getAddonProducts();
                // base_successors = [base_product] + base_product.successors
                final List<SUSEProduct> baseSuccessors = new ArrayList<>(baseProduct.getUpgrades().size() + 1);
                baseSuccessors.add(baseProduct);
                baseSuccessors.addAll(baseProduct.getUpgrades());
                if (logger.isDebugEnabled()) {
                    logger.debug("Found '{}' successors for the base product.", baseSuccessors.size());
                    baseSuccessors.stream().forEach(bp -> logger.debug(bp.getFriendlyName()));
                }

                final List<SUSEProduct> currentCombination = new ArrayList<>(installedExtensions.size() + 1);
                currentCombination.add(baseProduct);
                currentCombination.addAll(installedExtensions);

                // Liberty Migration should always add the Liberty Products
                for (SUSEProduct successorProduct : baseSuccessors) {
                    if (successorProduct.getName().equals("rhel-base") &&
                            successorProduct.getVersion().equals("8")) {
                        Set<SUSEProduct> liberties = SUSEProductFactory.findAllExtensionsOfRootProduct(successorProduct)
                                .stream()
                                .filter(e -> e.getName().equals("res"))
                                .collect(Collectors.toSet());
                        installedExtensions.addAll(liberties);
                    }
                    else if (successorProduct.getName().equals("el-base") &&
                            successorProduct.getVersion().equals("9")) {
                        Set<SUSEProduct> liberties = SUSEProductFactory.findAllExtensionsOfRootProduct(successorProduct)
                                .stream()
                                .filter(e -> e.getName().equals("sll"))
                                .collect(Collectors.toSet());
                        installedExtensions.addAll(liberties);
                    }
                }

                // extension_successors = installed_extensions.map {|e| [e] + e.successors }
                final List<List<SUSEProduct>> extensionSuccessors = new ArrayList<>(installedExtensions.size());
                for (SUSEProduct e : installedExtensions) {
                    final List<SUSEProduct> s = new ArrayList<>(e.getUpgrades().size() + 1);
                    s.add(e);
                    s.addAll(e.getUpgrades());
                    extensionSuccessors.add(s);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Extension: {}", e.getFriendlyName());
                        e.getUpgrades().forEach(ex -> logger.debug("Extension successor: {}", ex.getFriendlyName()));
                        logger.debug("-----------------------");
                    }
                }

                // base_successors.each do |base|
                //   available_extensions = installed_extensions.map do |ext|
                //     options = ext.successors.merge(migration_path_scope)
                //     options += [ext] if migration_kind == :online
                //     options.select { |succ| succ.available_for?(base) }
                //   end
                //   combinations += [base].product(*available_extensions)
                // end
                List<List<SUSEProduct>> combinations = baseSuccessors.stream().flatMap(baseSucc -> {
                    // first compute extensions successors compatible with the base successor
                    List<List<SUSEProduct>> compatibleExtensionSuccessors = extensionSuccessors.stream()
                            .map(extensionSucc -> extensionSucc.stream()
                                    .filter(succ -> extAvailableForRoot(succ, baseSucc))
                                    .collect(toList()))
                            .filter(list -> !list.isEmpty())
                            .collect(toList());

                    if (logger.isDebugEnabled()) {
                        if (compatibleExtensionSuccessors.isEmpty()) {
                            logger.debug("No extension successors for base successor {}", baseSucc.getFriendlyName());
                        }
                        else {
                            logger.debug("Found extension successors for base successor {}:",
                                    baseSucc.getFriendlyName());
                            // let's print out list of list with friendly names
                            compatibleExtensionSuccessors.stream()
                                    .map(css -> css.stream().map(SUSEProduct::getFriendlyName).collect(toList()))
                                    .forEach(css -> logger.debug(css));
                            logger.debug("-----------------------");
                        }
                    }

                    // the base successor will be always on the 1st position in the combinations below
                    compatibleExtensionSuccessors.add(0, List.of(baseSucc));

                    return Lists.combinations(compatibleExtensionSuccessors).stream();
                })
                .filter(comb -> !comb.equals(List.of(baseProduct)) && !comb.equals(currentCombination))
                .collect(toList());

                for (List<SUSEProduct> combination : combinations) {
                    SUSEProduct base = combination.get(0);
                    if (!ContentSyncManager.isProductAvailable(base, base)) {
                        // No Product Channels means, no subscription to access the channels
                        logger.warn("No SUSE Product Channels for {}. Skipping", base.getFriendlyName());
                        continue;
                    }
                    if (combination.size() == 1) {
                        logger.debug("Found Target: {}", base.getFriendlyName());
                        result.add(new SUSEProductSet(base, Collections.emptyList()));
                    }
                    else {
                        List<SUSEProduct> addonProducts = combination.subList(1, combination.size());
                        //No Product Channels means, no subscription to access the channels
                        if (addonProducts.stream()
                                .anyMatch(ap -> !ContentSyncManager.isProductAvailable(ap, base))) {
                            if (logger.isDebugEnabled()) {
                                addonProducts.stream()
                                        .filter(ap -> !ContentSyncManager.isProductAvailable(ap, base))
                                        .forEach(ap -> logger.warn("No SUSE Product Channels for {}. Skipping {}",
                                                ap.getFriendlyName(), base.getFriendlyName()));
                            }
                            continue;
                        }
                        logger.debug("Found Target: {}", base.getFriendlyName());
                        addonProducts.forEach(ext -> logger.debug("   - {}", ext.getFriendlyName()));
                        result.add(new SUSEProductSet(base, addonProducts));
                    }
                }
                return result;
            }
        );
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
        TreeMap<ClonedChannel, List<Long>> alternatives =
                new TreeMap<>();
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

            // Set this to false as soon as we know this alternative is invalid
            boolean isValidAlternative = true;

            // Get all child channels of this clone
            List<Channel> children = clone.getAccessibleChildrenFor(user);

            // Init the list of required channel IDs
            List<Long> requiredChannelIDs = new ArrayList<>();

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
                    logger.debug("Discarding cloned channel '{}' of base channel '{}' as a migration alternative. " +
                            "The cloned channel doesn't have required child channels. Required child channels: '{}', " +
                            "accessible child channels of the clone: '{}'.", clone, suseBaseChannel,
                            requiredChildChannels, children);
                    isValidAlternative = false;
                    break;
                }
            }

            // Is this a valid alternative?
            if (isValidAlternative) {
                alternatives.put(clone, requiredChannelIDs);
            }
        }
        return alternatives;
    }

    /**
     * Look for the migration target of a given source within a list of products.
     *
     * @param source source product
     * @param targets target products
     * @return matching target product
     */
    public static SUSEProduct findMatch(SUSEProduct source, List<SUSEProduct> targets) {
        SUSEProduct matchingProduct = null;
        // Match found if the targets contain the source product itself
        if (targets.contains(source)) {
            matchingProduct = source;
        }
        else {
            List<SUSEProductDto> results = findTargetProducts(source.getId());
            for (SUSEProduct target : targets) {
                for (SUSEProductDto result : results) {
                    if (result.getId().equals(target.getId())) {
                        // Found the matching product
                        matchingProduct = target;
                        break;
                    }
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
            if (minion.isEmpty() || !minion.get().getOsFamily().equals("Suse")) {
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
     * @param server the server to migrate
     * @param targetSet set of target products (base product and addons)
     * @param channelIDs IDs of all channels to subscribe
     * @param dryRun perform a dry run
     * @param allowVendorChange allow vendor change during dist upgrade
     * @param earliest earliest schedule date
     * @return the action ID
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    private static Long scheduleDistUpgrade(User user, Server server,
            SUSEProductSet targetSet, Collection<Long> channelIDs,
            boolean dryRun, boolean allowVendorChange, Date earliest) throws TaskomaticApiException {
        // Create action details
        DistUpgradeActionDetails details = new DistUpgradeActionDetails();
        List<String>  missingSuccessors = new ArrayList<>();

        // Add product upgrades
        // Note: product upgrades are relevant for SLE 10 only!
        if (targetSet != null) {
            SUSEProductSet installedProducts = server.getInstalledProductSet().orElseThrow(() ->
                    new FaultException(-1, "listMigrationTargetError", "Server has no Products installed."));

            SUSEProductUpgrade upgrade = new SUSEProductUpgrade(
                    installedProducts.getBaseProduct(), targetSet.getBaseProduct());
            details.addProductUpgrade(upgrade);

            // Find matching targets for every addon
            for (SUSEProduct addon : installedProducts.getAddonProducts()) {
                SUSEProduct match = DistUpgradeManager.findMatch(addon, targetSet.getAddonProducts());
                if (Objects.nonNull(match)) {
                    upgrade = new SUSEProductUpgrade(addon,
                            DistUpgradeManager.findMatch(addon, targetSet.getAddonProducts()));
                    details.addProductUpgrade(upgrade);
                }
                else {
                    missingSuccessors.add(addon.getName());
               }
            }
            details.setMissingSuccessors(String.join(",", missingSuccessors));
        }

        // Add individual channel tasks
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
                details.addChannelTask(task);
            }
        }
        // Subscribe to all of the remaining channels
        for (Long cid : channelIDs) {
            DistUpgradeChannelTask task = new DistUpgradeChannelTask();
            task.setChannel(ChannelFactory.lookupById(cid));
            task.setTask(DistUpgradeChannelTask.SUBSCRIBE);
            details.addChannelTask(task);
        }

        // Set additional attributes
        details.setDryRun(dryRun);
        details.setAllowVendorChange(allowVendorChange);
        details.setFullUpdate(true);

        // Return the ID of the scheduled action
        return ActionManager.scheduleDistUpgrade(user, server, details, earliest).getId();
    }

    /**
     * Schedule a distribution upgrade for a given server, allowing passing the PAYG flag.
     *
     * @param user the user who is scheduling
     * @param server the server to migrate
     * @param targetSet set of target products (base product and addons)
     * @param channelIDs IDs of all channels to subscribe
     * @param dryRun perform a dry run
     * @param allowVendorChange allow vendor change during dist upgrade
     * @param earliest earliest schedule date
     * @param isPayg tells the method how to behave if SUMA is PAYG
     * @return the action ID
     * @throws TaskomaticApiException if there was a Taskomatic error
     * @throws DistUpgradePaygException if the SUSE Manager instance is PAYG.
     */
    public static Long scheduleDistUpgrade(User user, Server server,
                                           SUSEProductSet targetSet, Collection<Long> channelIDs,
                                           boolean dryRun, boolean allowVendorChange, Date earliest,
                                           boolean isPayg)
            throws TaskomaticApiException, DistUpgradePaygException {

        if (isPayg) {
            /*
            Changing product family I.e
              - SLES 15 SP5 to SLES for SAP 15 SP4 or
              - from OpenSUSE Leap 15.4 to SLES 15 SP4
            is not allowed.
            Only SP migrations should be possible.
            Also individual assigning channels to perform a migration is forbidden
            */
            SUSEProduct installedBaseProduct = server.getInstalledProductSet()
                    .map(SUSEProductSet::getBaseProduct)
                    .orElseThrow(() ->
                            new FaultException(-1, "listMigrationTargetError", "Server has no Products installed."));
            if (targetSet != null) {
                SUSEProduct targetBaseProduct = targetSet.getBaseProduct();
                if (targetBaseProduct.getChannelFamily() == null ||
                        installedBaseProduct.getChannelFamily() == null ||
                        !targetBaseProduct.getChannelFamily().equals(installedBaseProduct.getChannelFamily())) {
                    throw new DistUpgradePaygException(
                            "In PAYG SUMA instances, changing the product family is forbidden");
                }
            }
            else {
                throw new DistUpgradePaygException("In PAYG SUMA instances, individual migrations are forbidden");
            }
        }

        return scheduleDistUpgrade(user, server, targetSet, channelIDs, dryRun, allowVendorChange, earliest);
    }

    /**
     * Remove incompatible migration targets compared to the installed products.
     * Write the failed products in the missingSuccesorExtensions set in case it should
     * be shown somewhere.
     *
     * @param installedProducts Optional set of the installed products
     * @param allMigrationTargets all calculated migration targets
     * @param missingSuccessorExtensions OUT: info about installed extensions missing a successor
     * @return list of valid migration targets
     */
    public static List<SUSEProductSet> removeIncompatibleTargets(Optional<SUSEProductSet> installedProducts,
            List<SUSEProductSet> allMigrationTargets, Optional<Set<String>> missingSuccessorExtensions) {

        List<SUSEProductSet> migrationTargets = new LinkedList<>();
        for (SUSEProductSet t : allMigrationTargets) {
            if (installedProducts.isPresent() && installedProducts.get().getAddonProducts().isEmpty()) {
                migrationTargets.add(t);
                logger.debug("Found valid migration target: {}", t);
                continue;
            }
            List<SUSEProduct> missingAddonSuccessors = installedProducts.orElse(new SUSEProductSet()).getAddonProducts()
                    .stream()
                    .filter(addon -> DistUpgradeManager.findMatch(addon, t.getAddonProducts()) == null)
                    .collect(Collectors.toList());

            if (missingAddonSuccessors.isEmpty()) {
                logger.debug("Found valid migration target: {}", t);
                migrationTargets.add(t);
            }
            else {
                List<String> missing = missingAddonSuccessors.stream().map(SUSEProduct::getFriendlyName)
                        .collect(Collectors.toList());
                logger.warn("No migration target found for '{}'. Skipping", String.join(", ", missing));
                missingSuccessorExtensions.ifPresent(l -> l.addAll(missing));
            }
        }
        return migrationTargets;
    }
}
