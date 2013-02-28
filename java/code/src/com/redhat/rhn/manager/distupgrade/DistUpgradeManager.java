/**
 * Copyright (c) 2012 Novell
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ClonedChannel;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.product.SUSEProductSet;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.EssentialChannelDto;
import com.redhat.rhn.manager.BaseManager;

/**
 * Business logic for performing distribution upgrades.
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
     * Find possible migration target products for a given product ID.
     *
     * @param productId product ID
     * @return DataResult containing target product IDs
     */
    public static DataResult findTargetProducts(long productId) {
        SelectMode m = ModeFactory.getMode("distupgrade_queries",
                "find_target_products");
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("product_id", productId);
        return m.execute(params);
    }

    /**
     * Find all channels required by a given product with base channel.
     *
     * @param productId product ID
     * @param baseChannelLabel base channel label
     * @return DataResult containing product channel IDs
     */
    public static DataResult findProductChannels(long productId, String baseChannelLabel) {
        SelectMode m = ModeFactory.getMode("distupgrade_queries",
                "channels_required_for_product");
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("product_id", productId);
        params.put("base_channel_label", baseChannelLabel);
        return m.execute(params);
    }

    /**
     * Return all channels *required* for migrating to a certain {@link SUSEProductSet}.
     *
     * @param productSet product set
     * @return list of channel DTOs
     */
    @SuppressWarnings("unchecked")
    public static List<EssentialChannelDto> getRequiredChannels(SUSEProductSet productSet) {
        List<Long> productIDs = productSet.getProductIDs();
        HashMap<String, Object> params = new HashMap<String, Object>();
        SelectMode m = ModeFactory.getMode("distupgrade_queries",
                    "channels_required_for_product_set");
        return m.execute(params, productIDs);
    }

    /**
     * For a given product, return the respective base channel as
     * {@link EssentialChannelDto}.
     *
     * @param productID product ID
     * @return base channel
     */
    public static EssentialChannelDto getProductBaseChannelDto(long productID) {
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("pid", productID);
        SelectMode m = ModeFactory.getMode("Channel_queries",
                    "suse_base_channels_for_suse_product");
        @SuppressWarnings("unchecked")
        List<EssentialChannelDto> channels = makeDataResult(params, null, null, m);
        EssentialChannelDto ret = null;
        if (channels.size() > 0) {
            ret = channels.get(0);
        }
        return ret;
    }

    /**
     * For a given product, return the respective base channel as {@link Channel}.
     *
     * @param productID product ID
     * @param user user
     * @return base channel
     */
    public static Channel getProductBaseChannel(long productID, User user) {
        Channel ret = null;
        EssentialChannelDto channelDto = getProductBaseChannelDto(productID);
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
     * @param user user
     * @return list of product sets
     */
    @SuppressWarnings("unchecked")
    public static List<SUSEProductSet> getTargetProductSets(
            SUSEProductSet installedProducts, User user) {
        ArrayList<SUSEProductSet> ret = new ArrayList<SUSEProductSet>();
        if (installedProducts == null) {
            return null;
        }

        // Find migration targets for the base product
        DataResult<Map<String, Object>> targetBaseProducts = findTargetProducts(
                installedProducts.getBaseProduct().getId());

        for (Map<String, Object> targetBaseProduct : targetBaseProducts) {
            // Create a new product set
            SUSEProductSet targetSet = new SUSEProductSet();
            long targetBaseProductID = (Long) targetBaseProduct.get("product_id");
            SUSEProduct targetProduct = SUSEProductFactory.getProductById(
                    targetBaseProductID);
            targetSet.setBaseProduct(targetProduct);

            // Look for the target product's base channel
            Channel baseChannel = getProductBaseChannel(targetBaseProductID, user);
            if (baseChannel == null) {
                continue;
            }

            // Look for mandatory child channels
            DataResult<Map<String, Object>> productChannels = findProductChannels(
                    targetBaseProductID, baseChannel.getLabel());
            Long parentChannelID = getParentChannelId(productChannels);
            if (parentChannelID == null) {
                // Found a channel that's not synced
                targetSet.addMissingChannels(getMissingChannels(productChannels));
            }

            // Look for addon product targets
            for (SUSEProduct addonProduct : installedProducts.getAddonProducts()) {
                DataResult<Map<String, Object>> targetAddonProducts = findTargetProducts(
                        addonProduct.getId());

                // Take the first target in case there is more than one
                Long targetAddonProductID = null;
                if (targetAddonProducts.size() > 0) {
                    if (targetAddonProducts.size() > 1) {
                        logger.warn("More than one migration target found for addon " +
                                "product: " + addonProduct.getFriendlyName());
                    }
                    targetAddonProductID = (Long) targetAddonProducts.get(0).
                            get("product_id");
                }
                else {
                    // No target found, try to keep the source addon (bnc#802144)
                    targetAddonProductID = addonProduct.getId();
                }

                if (targetAddonProductID != null) {
                    targetSet.addAddonProduct(SUSEProductFactory.getProductById(
                            targetAddonProductID));

                    // Look for mandatory channels
                    DataResult<Map<String, Object>> drChannelsChild = findProductChannels(
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
     * Given a list of channels as a {@link DataResult}, return the labels of those
     * where cid == null.
     */
    private static List<String> getMissingChannels(
            DataResult<Map<String, Object>> channels) {
        List<String> ret = new ArrayList<String>();
        for (Map<String, Object> channel : channels) {
            Long cid = (Long) channel.get("cid");
            if (cid == null) {
                ret.add((String) channel.get("channel_label"));
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
    public static Long getParentChannelId(DataResult<Map<String, Object>> productChannels) {
        Long baseChannelID = null;
        for (Map<String, Object> channel : productChannels) {
            Long cid = (Long) channel.get("cid");
            // Check if this channel is actually available
            if (cid == null) {
                return null;
            }
            // Channel is synced
            if (channel.get("parent_cid") == null) {
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
    public static boolean isParentChannel(Long parentChannelID,
            DataResult<Map<String, Object>> channels) {
        boolean ret = true;
        for (Map<String, Object> channelRow : channels) {
            Long cid = (Long) channelRow.get("cid");
            if (cid == null) {
                // Channel is not synced, abort
                return false;
            }
            // Channel is synced
            if ((Long) channelRow.get("parent_cid") != parentChannelID) {
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
     * @param user user
     * @return alternative target product sets
     */
    public static HashMap<ClonedChannel, List<Long>> getAlternatives(
            SUSEProductSet targetProducts, User user) {
        // This list will be returned
        HashMap<ClonedChannel, List<Long>> alternatives =
                new HashMap<ClonedChannel, List<Long>>();
        // Get base channel
        Channel suseBaseChannel = getProductBaseChannel(
                targetProducts.getBaseProduct().getId(), user);
        // Get all clones
        List<ClonedChannel> allClones = getAllClones(suseBaseChannel);
        // Get required channels for this product set
        List<EssentialChannelDto> requiredChildChannels =
                getRequiredChannels(targetProducts);

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
                    Channel childOriginal = getOriginalChannel(child);
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
     * For a given {@link Channel}, determine the original {@link Channel}.
     *
     * @param channel channel
     * @return original channel
     */
    public static Channel getOriginalChannel(Channel channel) {
        while (channel.isCloned()) {
            channel = ((ClonedChannel) channel).getOriginal();
        }
        return channel;
    }

    /**
     * Look for the migration target of a given source within a list of products.
     *
     * @param source source product
     * @param targets target products
     * @return matching target product
     */
    @SuppressWarnings("unchecked")
    public static SUSEProduct findMatch(SUSEProduct source, List<SUSEProduct> targets) {
        SUSEProduct matchingProduct = null;
        // Match found if the targets contain the source product itself
        if (targets.contains(source)) {
            matchingProduct = source;
        }
        else {
            DataResult<Map<String, Object>> results = findTargetProducts(source.getId());
            for (SUSEProduct target : targets) {
                for (Map<String, Object> result : results) {
                    if (result.get("product_id").equals(target.getId())) {
                        // Found the matching product
                        matchingProduct = target;
                        break;
                    }
                }
            }
        }
        return matchingProduct;
    }
}
