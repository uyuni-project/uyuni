/**
 * Copyright (c) 2014 SUSE
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
package com.redhat.rhn.manager.content;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.channel.ContentSource;
import com.redhat.rhn.domain.channel.PrivateChannelFamily;
import com.redhat.rhn.domain.iss.IssFactory;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductChannel;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.product.SUSEUpgradePath;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.manager.setup.MirrorCredentialsDto;
import com.redhat.rhn.manager.setup.MirrorCredentialsManager;
import com.redhat.rhn.manager.setup.SubscriptionDto;

import com.suse.mgrsync.MgrSyncChannel;
import com.suse.mgrsync.MgrSyncChannelFamilies;
import com.suse.mgrsync.MgrSyncChannelFamily;
import com.suse.mgrsync.MgrSyncChannels;
import com.suse.mgrsync.MgrSyncProduct;
import com.suse.mgrsync.MgrSyncUpgradePath;
import com.suse.mgrsync.MgrSyncUpgradePaths;
import com.suse.scc.client.SCCClient;
import com.suse.scc.client.SCCClientException;
import com.suse.scc.model.SCCProduct;
import com.suse.scc.model.SCCRepository;
import com.suse.scc.model.SCCSubscription;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Content synchronization logic.
 */
public class ContentSyncManager {

    // Logger instance
    private static final Logger log = Logger.getLogger(ContentSyncManager.class);

    // This was a guesswork and we so far *have* to stay on this value.
    // https://github.com/SUSE/spacewalk/blob/Manager/susemanager/src/mgr_ncc_sync_lib.py#L69
    private static final Integer RESET_ENTITLEMENT = 10;

    // The "limitless or endless in space" at SUSE is 200000. Of type Long.
    // https://github.com/SUSE/spacewalk/blob/Manager/susemanager/src/mgr_ncc_sync_lib.py#L43
    public static final Long INFINITE = 200000L;
    private static final String PROVISIONAL_TYPE = "PROVISIONAL";

    // Not yet used
    @SuppressWarnings("unused")
    private static final String FULL_TYPE = "FULL";

    // Base channels have "BASE" as their parent in channels.xml
    private static final String BASE_CHANNEL = "BASE";

    // Static XML files we parse
    private static File channelsXML = new File(
            "/usr/share/susemanager/channels.xml");
    private static File channelFamiliesXML = new File(
            "/usr/share/susemanager/channel_families.xml");
    private static File upgradePathsXML = new File(
            "/usr/share/susemanager/upgrade_paths.xml");

    /**
     * Default constructor.
     */
    public ContentSyncManager() {
    }

    /**
     * Set the channels.xml {@link File} to read from.
     * @param channelsXML the channels.xml file
     */
    public void setChannelsXML(File file) {
        channelsXML = file;
    }

    /**
     * Set the channels_families.xml {@link File} to read from.
     * @param file the channel_families.xml file
     */
    public void setChannelFamiliesXML(File file) {
        channelFamiliesXML = file;
    }

    /**
     * Set the upgrade_paths.xml {@link File} to read from.
     * @param file the upgrade_paths.xml file
     */
    public void setUpgradePathsXML(File file) {
        upgradePathsXML = file;
    }

    /**
     * Read the channels.xml file.
     *
     * @return List of parsed channels
     * @throws ContentSyncException in case of an error
     */
    public List<MgrSyncChannel> readChannels() throws ContentSyncException {
        try {
            Persister persister = new Persister();
            List<MgrSyncChannel> channels = persister.read(
                    MgrSyncChannels.class, channelsXML).getChannels();
            return channels;
        }
        catch (Exception e) {
            throw new ContentSyncException(e);
        }
    }

    /**
     * Read the channel_families.xml file.
     *
     * @return List of parsed channel families
     * @throws ContentSyncException in case of an error
     */
    public List<MgrSyncChannelFamily> readChannelFamilies() throws ContentSyncException {
        try {
            Persister persister = new Persister();
            List<MgrSyncChannelFamily> channelFamilies = persister.read(
                    MgrSyncChannelFamilies.class, channelFamiliesXML).getFamilies();
            return channelFamilies;
        }
        catch (Exception e) {
            throw new ContentSyncException(e);
        }
    }

    /**
     * Read the upgrade_paths.xml file.
     *
     * @return List of upgrade paths
     * @throws ContentSyncException in case of an error
     */
    public List<MgrSyncUpgradePath> readUpgradePaths() throws ContentSyncException {
        try {
            Persister persister = new Persister();
            List<MgrSyncUpgradePath> upgradePaths = persister.read(
                    MgrSyncUpgradePaths.class, upgradePathsXML).getPaths();
            return upgradePaths;
        }
        catch (Exception e) {
            throw new ContentSyncException(e);
        }
    }

    /**
     * Returns all products available to all configured credentials.
     * @return list of all available products
     */
    public Collection<SCCProduct> getProducts() {
        Set<SCCProduct> productList = new HashSet<SCCProduct>();
        List<MirrorCredentialsDto> credentials =
                new MirrorCredentialsManager().findMirrorCredentials();
        // Query products for all mirror credentials
        for (MirrorCredentialsDto c : credentials) {
            SCCClient scc = new SCCClient(c.getUser(), c.getPassword());
            try {
                List<SCCProduct> products = scc.listProducts();
                productList.addAll(products);
            }
            catch (SCCClientException e) {
                log.error(e.getMessage(), e);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Found " + productList.size() + " available products.");
        }
        return productList;
    }

    /**
     * Returns all repositories available to all configured credentials.
     * @return list of all available repositories
     */
    public Collection<SCCRepository> getRepositories() {
        Set<SCCRepository> reposList = new HashSet<SCCRepository>();
        List<MirrorCredentialsDto> credentials =
                new MirrorCredentialsManager().findMirrorCredentials();
        // Query repos for all mirror credentials
        for (MirrorCredentialsDto c : credentials) {
            SCCClient scc = new SCCClient(c.getUser(), c.getPassword());
            try {
                List<SCCRepository> repos = scc.listRepositories();
                reposList.addAll(repos);
            }
            catch (SCCClientException e) {
                log.error(e.getMessage(), e);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Found " + reposList.size() + " available repositories.");
        }
        return reposList;
    }

    /**
     * Returns all subscriptions available to all configured credentials.
     * @return list of all available subscriptions
     */
    public Collection<SCCSubscription> getSubscriptions() {
        Set<SCCSubscription> subscriptions = new HashSet<SCCSubscription>();
        List<MirrorCredentialsDto> credentials =
                new MirrorCredentialsManager().findMirrorCredentials();
        // Query subscriptions for all mirror credentials
        for (MirrorCredentialsDto c : credentials) {
            SCCClient scc = new SCCClient(c.getUser(), c.getPassword());
            try {
                List<SCCSubscription> subs = scc.listSubscriptions();
                subscriptions.addAll(subs);
            }
            catch (SCCClientException e) {
                log.error(e.getMessage(), e);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Found " + subscriptions.size() + " available subscriptions.");
        }
        return subscriptions;
    }

    /**
     * XXX: This will go away as XMLRPC handler will call the single methods.
     * Refresh functionality doing the same thing as --refresh in mgr-ncc-sync.
     * @throws ContentSyncException
     */
    public void refresh() throws ContentSyncException {
        updateChannels();
        updateChannelFamilies();
        updateSUSEProducts(getProducts());
        updateSubscriptions(getSubscriptions());
        updateSUSEProductChannels(getAvailableChannels(readChannels()));
        updateUpgradePaths();
    }

    /**
     * Update channel information in the database.
     * @throws com.redhat.rhn.manager.content.ContentSyncException
     */
    public void updateChannels() throws ContentSyncException {
        // If this is an ISS slave then do nothing
        if (IssFactory.getCurrentMaster() != null) {
            return;
        }

        // Read contents of channels.xml into a map
        Map<String, MgrSyncChannel> channelsXML = new HashMap<String, MgrSyncChannel>();
        for (MgrSyncChannel c : readChannels()) {
            channelsXML.put(c.getLabel(), c);
        }

        // Get all vendor channels from the database
        List<Channel> channelsDB = ChannelFactory.listVendorChannels();
        for (Channel c : channelsDB) {
            if (channelsXML.containsKey(c.getLabel())) {
                MgrSyncChannel channel = channelsXML.get(c.getLabel());
                if (!channel.getDescription().equals(c.getDescription()) ||
                        !channel.getName().equals(c.getName()) ||
                        !channel.getSummary().equals(c.getSummary()) ||
                        !channel.getUpdateTag().equals(c.getUpdateTag())) {
                    // There is a difference, copy channel attributes and save
                    c.setDescription(channel.getDescription());
                    c.setName(channel.getName());
                    c.setSummary(channel.getSummary());
                    c.setUpdateTag(channel.getUpdateTag());
                    ChannelFactory.save(c);
                }
            }
            else {
                // Channel is no longer mirrorable, we can return those and warn about it
            }
        }

        // Update content source URLs
        List<ContentSource> contentSources = ChannelFactory.listVendorContentSources();
        for (ContentSource cs : contentSources) {
            if (channelsXML.containsKey(cs.getLabel())) {
                // TODO: Check if alternative mirror URL is set and consider it here
                MgrSyncChannel channel = channelsXML.get(cs.getLabel());
                if (!channel.getSourceUrl().equals(cs.getSourceUrl())) {
                    cs.setSourceUrl(channel.getSourceUrl());
                    ChannelFactory.save(cs);
                }
            }
        }
    }

    /**
     * Update channel families in DB with data from the channel_families.xml file.
     * @throws ContentSyncException
     */
    public void updateChannelFamilies() throws ContentSyncException {
        List<MgrSyncChannelFamily> channelFamilies = readChannelFamilies();
        for (MgrSyncChannelFamily channelFamily : channelFamilies) {
            ChannelFamily family = createOrUpdateChannelFamily(
                    channelFamily.getLabel(), channelFamily.getName());
            if (family != null && family.getPrivateChannelFamilies().isEmpty()) {
                // No entry in rhnPrivateChannelFamily, create it
                PrivateChannelFamily pcf = new PrivateChannelFamily();
                pcf.setCreated(new Date());
                pcf.setCurrentMembers(0L);
                pcf.setMaxMembers(0L);
                // Set the default organization (id = 1)
                pcf.setOrg(OrgFactory.lookupById(1L));
                // Set INFINITE max_members if default_nodecount = -1
                if (channelFamily.getDefaultNodeCount() < 0) {
                    pcf.setMaxMembers(ContentSyncManager.INFINITE);
                }
                pcf.setChannelFamily(family);
                family.addPrivateChannelFamily(pcf);
                ChannelFamilyFactory.save(family);
            }
        }
    }

    /**
     * Returns a list of available {@link SubscriptionDto}s. If there are families which
     * have subscriptions in the database, but are not in the subscription list from SCC,
     * node count is set to zero.
     *
     * @param subscriptions subscriptions as we get them from SCC
     * @return list of {@link SubscriptionDto}
     * @throws ContentSyncException
     */
    private List<SubscriptionDto> consolidateSubscriptions(
            Collection<SCCSubscription> subscriptions) throws ContentSyncException {
        Map<String, SubscriptionDto> sc = new HashMap<String, SubscriptionDto>();
        Date now = new Date();
        for (SCCSubscription subscription : subscriptions) {
            Date start = subscription.getStartsAt() == null ?
                         new Date() : subscription.getStartsAt();
            Date end = subscription.getExpiresAt();
            for (String productClass : subscription.getProductClasses()) {
                if ((now.compareTo(start) >= 0 &&
                     (end == null || now.compareTo(end) <= 0)) &&
                    !subscription.getType().equals(ContentSyncManager.PROVISIONAL_TYPE)) {
                    sc.put(productClass, new SubscriptionDto(subscription.getName(),
                            productClass, subscription.getSystemsCount(), start, end));
                }
            }
        }

        for (MgrSyncChannelFamily family : this.readChannelFamilies()) {
            if (family.getDefaultNodeCount() < 0) {
                sc.put(family.getLabel(), new SubscriptionDto(family.getName(),
                        family.getLabel(), 0, null, null));
            }
        }

        ModeFactory.getWriteMode("mgr_sync_queries", "invalidate_scc_subscriptions")
                .executeUpdate(new HashMap<String, Object>(),
                               new ArrayList<String>(sc.keySet()));

        return new ArrayList<SubscriptionDto>(sc.values());
    }

    /**
     * Updates max_members based on a given {@link SubscriptionDto}.
     * @param subscription
     */
    private void updateSubscription(SubscriptionDto sub) {
        final ChannelFamily family = createOrUpdateChannelFamily(
                sub.getProductClass(), sub.getName());

        // Remember all orgs bound to this channel family
        Set<Long> orgIds = new HashSet<Long>();
        for (PrivateChannelFamily pcf : family.getPrivateChannelFamilies()) {
            orgIds.add(pcf.getOrg().getId());
        }

        // Set max_members to INFINITE for all those channel families and orgs
        Date now = new Date();
        for(Long orgId : orgIds) {
            if (sub.getEndDate() == null || now.compareTo(sub.getEndDate()) < 0) {
                HashMap<String, Object> params = new HashMap<String, Object>();
                params.put("max_members", INFINITE);
                params.put("cfid", family.getId());
                params.put("orgid", orgId);
                ModeFactory.getWriteMode("mgr_sync_queries", "set_subscription_max_members")
                        .executeUpdate(params);
            }
        }
    }

    /**
     * Updates max_members based on a given {@link SubscriptionDto}.
     * @param subscription
     */
    private void updateEntitlement(SubscriptionDto subscription) {
        Date now = new Date();

        // Loop over assigned entitlements
        for (final String entitlement : SystemEntitlement.valueOf(
                subscription.getProductClass()).getEntitlements()) {
            // Get this entitlement's ID from rhnServerGroupType
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("label", entitlement);
            @SuppressWarnings("unchecked")
            DataResult<Map<String, Object>> result = ModeFactory.getMode(
                    "mgr_sync_queries", "entitlement_id", Map.class).execute(params);
            Long entitlementId = (Long) result.get(0).get("id");

            // Set max_members to our interpretation of INFINITE
            if (now.compareTo(subscription.getEndDate()) <= 0) {
                params.clear();
                params.put("max_members", INFINITE);
                params.put("group_type", entitlementId);
                ModeFactory.getWriteMode("mgr_sync_queries", "set_entitlement_max_members")
                        .executeUpdate(params);
            }
        }
    }

    /**
     * Sync subscriptions from SCC to the database.
     * @param subscriptions
     * @throws ContentSyncException
     */
    public void updateSubscriptions(Collection<SCCSubscription> subscriptions)
            throws ContentSyncException {
        /**
         * Reset entitlements.
         * The value of "10" comes from the part, where testers needed about this amount of
         * subscriptions. It turns out that the customers are also have this number and dealing
         * with it on their installations.
         */
        Map<String, Object> params = new HashMap<String, Object>();
        ModeFactory.getWriteMode("mgr_sync_queries",
                                 "reset_entitlements_bnc740813").executeUpdate(params);
        params.put("max_members", ContentSyncManager.RESET_ENTITLEMENT);
        ModeFactory.getWriteMode("mgr_sync_queries",
                                 "reset_entitlements").executeUpdate(params);

        for (SubscriptionDto subscription : consolidateSubscriptions(subscriptions)) {
            if (isEntitlement(subscription.getProductClass())) {
                updateEntitlement(subscription);
            } else {
                updateSubscription(subscription);
            }
        }
    }

    /**
     * Creates or updates entries in the SUSEProducts database table with a given list of
     * {@link SCCProduct} objects.
     *
     * @param products list of products
     */
    public void updateSUSEProducts(Collection<SCCProduct> products) {
        for (SCCProduct p : products) {
            // Get channel family if it is available, otherwise create it
            String productClass = p.getProductClass();
            ChannelFamily channelFamily = createOrUpdateChannelFamily(
                    productClass, productClass);

            // Update this product in the database if it is there
            SUSEProduct product = SUSEProductFactory.findSUSEProduct(
                    p.getName(), p.getVersion(), p.getReleaseType(), p.getArch());
            if (product != null) {
                product.setFriendlyName(p.getFriendlyName());
                // TODO: Remove this attribute from database if it is not used anywhere
                product.setProductList('Y');
                if (channelFamily != null) {
                    product.setChannelFamilyId(channelFamily.getId().toString());
                }
            }
            else {
                // Otherwise create a new SUSE product and save it
                product = new SUSEProduct();
                product.setProductId(p.getId());
                // Convert those to lower case for case insensitive operating
                product.setName(p.getName().toLowerCase());
                product.setVersion(p.getVersion().toLowerCase());
                product.setRelease(p.getReleaseType().toLowerCase());
                product.setFriendlyName(p.getFriendlyName());
                product.setArch(PackageFactory.lookupPackageArchByLabel(p.getArch()));
                product.setProductList('Y');
                if (channelFamily != null) {
                    product.setChannelFamilyId(channelFamily.getId().toString());
                }
            }
            SUSEProductFactory.save(product);
        }
    }

    /**
     * Get a list of all actually available channels based on available channel families
     * as well as some other criteria.
     * @return list of available channels
     * @throws ContentSyncException
     */
    public List<MgrSyncChannel> getAvailableChannels(List<MgrSyncChannel> allChannels)
            throws ContentSyncException {
        // Get all channels from channels.xml and filter
        List<MgrSyncChannel> availableChannels = new ArrayList<MgrSyncChannel>();

        // Filter in all channels where channel families are available
        List<String> availableChannelFamilies =
                ChannelFamilyFactory.getAvailableChannelFamilyLabels();
        for (MgrSyncChannel c : allChannels) {
            if (availableChannelFamilies.contains(c.getFamily())) {
                availableChannels.add(c);
            }
        }

        // Reassign lists to variables to continue the filtering
        allChannels = availableChannels;
        availableChannels = new ArrayList<MgrSyncChannel>();

        // Remember channel labels in a list for convenient lookup
        List<String> availableChannelLabels = new ArrayList<String>();
        for (MgrSyncChannel c : allChannels) {
            availableChannelLabels.add(c.getLabel());
        }

        // Filter in channels with available parents only (or base channels)
        for (MgrSyncChannel c : allChannels) {
            String parent = c.getParent();
            if (parent.equals(BASE_CHANNEL) || availableChannelLabels.contains(parent)) {
                availableChannels.add(c);

                // Update tag can be empty string which is not allowed in the DB
                if (StringUtils.isBlank(c.getUpdateTag())) {
                    c.setUpdateTag(null);
                }

                // TODO: support "fromdir" and "mirror" to set sourceUrl correctly
            }
        }

        return availableChannels;
    }

    /**
     * Synchronization of the {@link SUSEProductChannel} relationships.
     * @throws ContentSyncException
     */
    public void updateSUSEProductChannels(List<MgrSyncChannel> availableChannels)
            throws ContentSyncException {
        // Get all currently existing product channel relations
        List<SUSEProductChannel> existingProductChannels =
                SUSEProductFactory.findAllSUSEProductChannels();

        // Create a map containing all installed vendor channels
        Map<String, Channel> installedChannels = new HashMap<String, Channel>();
        for (Channel channel : ChannelFactory.listVendorChannels()) {
            installedChannels.put(channel.getLabel(), channel);
        }

        // Get all available channels and iterate
        for (MgrSyncChannel availableChannel : availableChannels) {
            // We store only non-optional channels
            if (availableChannel.isOptional()) {
                continue;
            }

            // Set parent channel to null for base channels
            String parentChannelLabel = availableChannel.getParent();
            if (BASE_CHANNEL.equals(parentChannelLabel)) {
                parentChannelLabel = null;
            }

            // Lookup every product and insert/update relationships accordingly
            for (MgrSyncProduct p : availableChannel.getProducts()) {
                SUSEProduct product = SUSEProductFactory.lookupByProductId(p.getId());

                // Get the channel in case it is installed
                Channel channel = null;
                if (installedChannels.containsKey(availableChannel.getLabel())) {
                    channel = installedChannels.get(availableChannel.getLabel());
                }

                // Update or insert the product/channel relationship
                SUSEProductChannel spc = new SUSEProductChannel();
                spc.setProduct(product);
                spc.setChannelLabel(availableChannel.getLabel());
                spc.setParentChannelLabel(parentChannelLabel);
                spc.setChannel(channel);
                SUSEProductFactory.save(spc);

                // Remove from the list of existing relations
                if (existingProductChannels.contains(spc)) {
                    existingProductChannels.remove(spc);
                }
            }
        }

        // Drop the remaining ones (existing but not updated)
        for (SUSEProductChannel spc : existingProductChannels) {
            SUSEProductFactory.remove(spc);
        }
    }

    /**
     * Update contents of the suseUpgradePaths table with values read from upgrade_paths.xml.
     */
    public void updateUpgradePaths() throws ContentSyncException {
        // Get all DB content and create a map that eventually will hold the ones to remove
        List<SUSEUpgradePath> upgradePathsDB = SUSEProductFactory.findAllSUSEUpgradePaths();
        Map<String, SUSEUpgradePath> paths = new HashMap<String, SUSEUpgradePath>();
        for (SUSEUpgradePath path : upgradePathsDB) {
            String identifier = String.format("%s-%s",
                    path.getFromProduct().getProductId(),
                    path.getToProduct().getProductId());
            paths.put(identifier, path);
        }

        // Read upgrade paths from the file
        List<MgrSyncUpgradePath> upgradePaths = readUpgradePaths();
        for (MgrSyncUpgradePath path : upgradePaths) {
            // Remove from all paths so we end up with the ones to remove
            String identifier = String.format("%s-%s",
                    path.getFromProductId(), path.getToProductId());
            if (paths.keySet().contains(identifier)) {
                paths.remove(identifier);
            }

            // Insert or update after looking up the products
            SUSEProduct fromProduct = SUSEProductFactory.lookupByProductId(
                    path.getFromProductId());
            SUSEProduct toProduct = SUSEProductFactory.lookupByProductId(
                    path.getToProductId());
            if (fromProduct != null && toProduct != null) {
                SUSEProductFactory.save(new SUSEUpgradePath(fromProduct, toProduct));
            }
        }

        // Remove all the ones that were not inserted or updated
        for (Map.Entry<String, SUSEUpgradePath> entry : paths.entrySet()) {
            SUSEProductFactory.remove(entry.getValue());
        }
    }

    /**
     * Checks if a given string is one of the existing SUSE Manager entitlement.
     * @param s
     * @return true if s is a SUSE Manager entitlement, else false.
     */
    private boolean isEntitlement(String s) {
        for (SystemEntitlement ent : SystemEntitlement.values()) {
            if (ent.name().equals(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Updates an existing channel family or creates and returns a new one if no channel
     * family exists with the given label.
     * @return {@link ChannelFamily}
     */
    private ChannelFamily createOrUpdateChannelFamily(String label, String name) {
        ChannelFamily family = ChannelFamilyFactory.lookupByLabel(label, null);
        if (family == null && !isEntitlement(label)) {
            family = new ChannelFamily();
            family.setLabel(label);
            family.setOrg(null);
            family.setName(name);
            family.setProductUrl("some url");
            ChannelFamilyFactory.save(family);
        }
        return family;
    }
}
