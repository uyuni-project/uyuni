/**
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
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
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ChildChannelDto;
import com.redhat.rhn.frontend.dto.EssentialChannelDto;
import com.redhat.rhn.frontend.dto.SUSEProductDto;
import com.redhat.rhn.manager.BaseManager;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.system.SystemManager;

/**
 * Business logic for performing distribution upgrades.
 *
 * @version $Rev$
 */
public class DistUpgradeManager extends BaseManager {

    // Logger for this class
    private static Logger logger = Logger.getLogger(DistUpgradeManager.class);

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
        HashMap<String, Object> params = new HashMap<String, Object>();
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
        SelectMode m = ModeFactory.getMode("distupgrade_queries",
                "find_target_products");
        HashMap<String, Object> params = new HashMap<String, Object>();
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
        SelectMode m = ModeFactory.getMode("distupgrade_queries",
                "find_source_products");
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("product_id", productId);
        return m.execute(params);
    }

    /**
     * Find all *mandatory* child channels for a given product ID with base channel.
     *
     * @param productId product ID
     * @param baseChannelLabel base channel label
     * @return DataResult containing product channel IDs
     */
    @SuppressWarnings("unchecked")
    private static List<ChildChannelDto> findProductChannels(long productId,
            String baseChannelLabel) {
        SelectMode m = ModeFactory.getMode("distupgrade_queries",
                "channels_required_for_product");
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("product_id", productId);
        params.put("base_channel_label", baseChannelLabel);
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
    public static List<EssentialChannelDto> getRequiredChannels(
            SUSEProductSet productSet, long baseChannelID) {
        List<Long> productIDs = productSet.getProductIDs();
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("base_channel_id", baseChannelID);
        SelectMode m = ModeFactory.getMode("distupgrade_queries",
                    "channels_required_for_product_set");
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
    public static EssentialChannelDto getProductBaseChannelDto(long productID,
            ChannelArch arch) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("pid", productID);
        params.put("channel_arch_id", arch.getId());
        SelectMode m = ModeFactory.getMode("Channel_queries",
                    "suse_base_channels_for_suse_product");
        List<EssentialChannelDto> channels = makeDataResult(params, null, null, m);
        EssentialChannelDto ret = null;
        if (channels.size() > 0) {
            ret = channels.get(0);
        }
        if (channels.size() > 1) {
            logger.warn("More than one base channel found for product: " + productID +
                    " (arch: " + arch.getName() + ")");
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
    public static Channel getProductBaseChannel(long productID, ChannelArch arch,
            User user) {
        Channel ret = null;
        EssentialChannelDto channelDto = getProductBaseChannelDto(productID, arch);
        if (channelDto != null) {
            ret = ChannelFactory.lookupByIdAndUser(channelDto.getId(), user);
        }
        return ret;
    }

    /**
     * For a given list of installed products, return all available migration targets
     * as {@link SUSEProductSet} objects.
     *
     * @param installedProducts set of products currently installed on the system
     * @param arch channel arch
     * @param user user
     * @return list of product sets
     */
    public static List<SUSEProductSet> getTargetProductSets(
            SUSEProductSet installedProducts, ChannelArch arch, User user) {
        ArrayList<SUSEProductSet> ret = new ArrayList<SUSEProductSet>();
        if (installedProducts == null) {
            return null;
        }

        // Find migration targets for the base product
        List<SUSEProductDto> targetBaseProducts = findTargetProducts(
                installedProducts.getBaseProduct().getId());

        for (SUSEProductDto targetBaseProduct : targetBaseProducts) {
            // Create a new product set
            SUSEProductSet targetSet = new SUSEProductSet();
            SUSEProduct targetProduct = SUSEProductFactory.getProductById(
                    targetBaseProduct.getId());
            targetSet.setBaseProduct(targetProduct);

            // Look for the target product's base channel
            Channel baseChannel = getProductBaseChannel(
                    targetBaseProduct.getId(), arch, user);
            if (baseChannel == null) {
                continue;
            }

            // Look for mandatory child channels
            List<ChildChannelDto> productChannels = findProductChannels(
                    targetBaseProduct.getId(), baseChannel.getLabel());
            Long parentChannelID = getParentChannelId(productChannels);
            if (parentChannelID == null) {
                // Found a channel that's not synced
                targetSet.addMissingChannels(getMissingChannels(productChannels));
            }

            // Look for addon product targets
            for (SUSEProduct addonProduct : installedProducts.getAddonProducts()) {
                List<SUSEProductDto> targetAddonProducts = findTargetProducts(
                        addonProduct.getId());

                // Take the first target in case there is more than one
                Long targetAddonProductID = null;
                if (targetAddonProducts.size() > 0) {
                    if (targetAddonProducts.size() > 1) {
                        logger.warn("More than one migration target found for addon " +
                                "product: " + addonProduct.getFriendlyName());
                    }
                    targetAddonProductID = targetAddonProducts.get(0).getId();
                }
                else {
                    // No target found, try to keep the source addon (bnc#802144)
                    targetAddonProductID = addonProduct.getId();
                }

                if (targetAddonProductID != null) {
                    targetSet.addAddonProduct(SUSEProductFactory.getProductById(
                            targetAddonProductID));

                    // Look for mandatory channels
                    List<ChildChannelDto> drChannelsChild = findProductChannels(
                            targetAddonProductID, baseChannel.getLabel());
                    if (!isParentChannel(parentChannelID, drChannelsChild)) {
                        // Some channels are missing
                        targetSet.addMissingChannels(getMissingChannels(drChannelsChild));
                    }
                }
            }
            // Return this product set only if all addons can be migrated
            if (installedProducts.getAddonProducts().size() ==
                    targetSet.getAddonProducts().size()) {
                ret.add(targetSet);
            }
        }
        if (ret.size() > 1) {
            logger.error("Found " + ret.size() + " migration targets " + "for: " +
                    installedProducts.toString());
        }
        return ret;
    }

    /**
     * Given a list of channels, return the labels of those where cid == null.
     */
    private static List<String> getMissingChannels(List<ChildChannelDto> channels) {
        List<String> ret = new ArrayList<String>();
        for (ChildChannelDto channel : channels) {
            Long cid = channel.getId();
            if (cid == null) {
                ret.add(channel.getLabel());
            }
        }
        return ret;
    }

    /**
     * Return null if any of the given channels is not actually available (cid == null).
     * Otherwise find the common parent channel and return its ID.
     *
     * @param productChannels product channels
     * @return ID of the parent channel or null
     */
    private static Long getParentChannelId(List<ChildChannelDto> productChannels) {
        Long baseChannelID = null;
        for (ChildChannelDto channel : productChannels) {
            Long cid = channel.getId();
            // Check if this channel is actually available
            if (cid == null) {
                return null;
            }
            // Channel is synced
            if (channel.getParentId() == null) {
                // Parent channel is null -> this is the base channel
                baseChannelID = cid;
            }
        }
        return baseChannelID;
    }

    /**
     * For a list of channels, check if they all have the same given parent.
     * @param parentChannelID parent channel ID
     * @param channels channels
     * @return true if all channels have the same given parent, else false
     */
    private static boolean isParentChannel(Long parentChannelID,
            List<ChildChannelDto> channels) {
        boolean ret = true;
        for (ChildChannelDto channel : channels) {
            Long cid = channel.getId();
            if (cid == null) {
                // Channel is not synced, abort
                return false;
            }
            // Channel is synced
            if (channel.getParentId() != parentChannelID) {
                // Found channel not matching given parent, return
                ret = false;
                break;
            }
        }
        return ret;
    }

    /**
     * Return *all* clones of a given channel.
     *
     * @param channel channel
     * @return list of cloned channels
     */
    public static List<ClonedChannel> getAllClones(Channel channel) {
        List<ClonedChannel> ret = new ArrayList<ClonedChannel>();
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
    public static HashMap<ClonedChannel, List<Long>> getAlternatives(
            SUSEProductSet targetProducts, ChannelArch arch, User user) {
        // This list will be returned
        HashMap<ClonedChannel, List<Long>> alternatives =
                new HashMap<ClonedChannel, List<Long>>();
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
            List<Long> requiredChannelIDs = new ArrayList<Long>();

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
    public static Server performServerChecks(Long sid, User user)
            throws DistUpgradeException {
        Server server = SystemManager.lookupByIdAndUser(sid, user);

        // Check if server supports distribution upgrades
        boolean supported = DistUpgradeManager.isUpgradeSupported(server, user);
        if (!supported) {
            throw new DistUpgradeException(
                    "Dist upgrade not supported for server: " + sid);
        }

        // Check if zypp-plugin-spacewalk is installed
        boolean zyppPluginInstalled = PackageFactory.lookupByNameAndServer(
                "zypp-plugin-spacewalk", server) != null;
        if (!zyppPluginInstalled) {
            throw new DistUpgradeException(
                    "Package zypp-plugin-spacewalk is not installed: " + sid);
        }

        // Check if there is already a migration in the schedule
        if (ActionFactory.isMigrationScheduledForServer(server.getId()) != null) {
            throw new DistUpgradeException(
                    "Another dist upgrade is in the schedule for server: " + sid);
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
        List<Channel> childChannels = new ArrayList<Channel>();
        for (String label : channelLabels) {
            Channel channel = ChannelManager.lookupByLabelAndUser(label, user);
            if (channel.isBaseChannel()) {
                if (baseChannel != null) {
                    throw new DistUpgradeException(
                            "More than one base channel given for dist upgrade");
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
        Set<Long> channelIDs = new HashSet<Long>();
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
     *
     * @param user the user who is scheduling
     * @param server the server to migrate
     * @param targetSet set of target products (base product and addons)
     * @param channelIDs IDs of all channels to subscribe
     * @param dryRun perform a dry run
     * @param earliest earliest schedule date
     * @return the action ID
     */
    public static Long scheduleDistUpgrade(User user, Server server,
            SUSEProductSet targetSet, Collection<Long> channelIDs,
            boolean dryRun, Date earliest) {
        // Create action details
        DistUpgradeActionDetails details = new DistUpgradeActionDetails();

        // Add product upgrades
        // Note: product upgrades are relevant for SLE 10 only!
        if (targetSet != null) {
            SUSEProductSet installedProducts = server.getInstalledProductSet();
            SUSEProductUpgrade upgrade = new SUSEProductUpgrade(
                    installedProducts.getBaseProduct(), targetSet.getBaseProduct());
            details.addProductUpgrade(upgrade);

            // Find matching targets for every addon
            for (SUSEProduct addon : installedProducts.getAddonProducts()) {
                upgrade = new SUSEProductUpgrade(addon,
                        DistUpgradeManager.findMatch(addon, targetSet.getAddonProducts()));
                details.addProductUpgrade(upgrade);
            }
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
        details.setDryRun(dryRun ? 'Y' : 'N');
        details.setFullUpdate('Y');

        // Return the ID of the scheduled action
        return ActionManager.scheduleDistUpgrade(user, server, details, earliest).getId();
    }
}
