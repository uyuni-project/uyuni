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

import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.channel.ContentSource;
import com.redhat.rhn.domain.channel.PrivateChannelFamily;
import com.redhat.rhn.domain.iss.IssFactory;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.simpleframework.xml.core.Persister;

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
    private static final String FULL_TYPE = "FULL";
    private static final String PROVISIONAL_TYPE = "PROVISIONAL";

    // Base channels have "BASE" as their parent in channels.xml
    private static final String BASE = "BASE";

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
            return persister.read(MgrSyncChannels.class, channelsXML).getChannels();
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
            return persister.read(MgrSyncChannelFamilies.class,
                                  channelFamiliesXML).getFamilies();
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
            return persister.read(MgrSyncUpgradePaths.class, upgradePathsXML).getPaths();
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
     * Refresh functionality doing the same thing as --refresh in mgr-ncc-sync.
     * @throws com.redhat.rhn.manager.content.ContentSyncException
     */
    public void refresh() throws ContentSyncException {
        updateChannels();
        updateChannelFamilies();
        updateSUSEProducts(getProducts());
        updateSubscriptions(this.getSubscriptions());
        syncSUSEProductChannels(getAvailableChannels(readChannels()));
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
     * Create new or update an existing channel family.
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

    private PrivateChannelFamily newPrivateChannelFamily(ChannelFamily family) {
        PrivateChannelFamily pf = new PrivateChannelFamily();
        pf.setCreated(new Date());
        pf.setCurrentMembers(0L);
        pf.setMaxMembers(0L);
        pf.setOrg(null);
        pf.setChannelFamily(family);

        return pf;
    }

    /**
     * Update channel families in the database.
     * @throws com.redhat.rhn.manager.content.ContentSyncException
     */
    public void updateChannelFamilies() throws ContentSyncException {
        for (MgrSyncChannelFamily scf : this.readChannelFamilies()) {
            ChannelFamily family = this.createOrUpdateChannelFamily(
                    scf.getLabel(), scf.getName());
            if (family != null && family.getPrivateChannelFamilies().isEmpty()) {
                PrivateChannelFamily pf = this.newPrivateChannelFamily(family);
                if (scf.getDefaultNodeCount() < 0) {
                    pf.setMaxMembers(ContentSyncManager.INFINITE);
                }

                family.addPrivateChannelFamily(pf);
                ChannelFamilyFactory.save(family);
            }
        }
    }

    /**
     * Returns a mapping of the subscriptions quantity for each channel family.
     * If there are families which have subscriptions in the database,
     * but are not in the subscription list from SCC, node count is set to zero.
     *
     * @param subscriptions
     * @return
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
     * Subscription members counter.
     */
    private static class MembersCounter {
        private Long maxMembers;
        private final Long currentMembers;
        private Boolean dirty;

        public MembersCounter(PrivateChannelFamily pcf) {
            this.dirty = Boolean.FALSE;
            this.maxMembers = pcf.getMaxMembers();
            this.currentMembers = pcf.getCurrentMembers();
        }

        public Boolean isDirty() {
            return dirty;
        }

        public Long getCurrentMembers() {
            return currentMembers;
        }

        public Long getMaxMembers() {
            return maxMembers;
        }

        public void incMaxMembers(Long value) {
            this.maxMembers += value;
        }

        public void decMaxMembers(Long value) {
            this.maxMembers -= value;
            this.dirty = Boolean.TRUE;
        }
    }

    /**
     * Calculate subscriptions.
     *
     * @param allSubs
     * @param total
     * @param subscs
     * @param oid
     * @return Calculated subscriptions.
     */
    private Map<Long, MembersCounter> calculateSubscriptions(
            Map<Long, MembersCounter> allSubs, Long total,
            SubscriptionDto subscr, Long oid) {
        /*
        Two things can happen:
        1. There are more subscriptions in NCC than in DB
           Then add (substract a negative value) from org_id=1 max_members

        2. NCC allows less subscriptions than in the DB
           - Reduce the max_members of some org's
           - Substract the max_members of org_id=1
             until needed_subscriptions=0 or max_members=current_members
           - Substract the max_members of org_id++ until
             needed_subscriptions=0 or max_members=current_members
           - Reduce the max_members of org_id=1 until
             needed_subscriptions=0 or max_members=0 (!!!)
           - Reduce the max_members of org_id++ until
             needed_subscriptions=0 or max_members=0 (!!!)
        */
        Long needed = total - subscr.getNodeCount();
        Map<Long, MembersCounter> calculated = new HashMap<Long, MembersCounter>();
        for (Map.Entry<Long, MembersCounter> item : allSubs.entrySet()) {
            MembersCounter cnt = item.getValue();
            Long free = cnt.getMaxMembers() - cnt.getCurrentMembers();
            if ((free >= 0 && needed <= free) || (free < 0 && needed < 0)) {
                cnt.decMaxMembers(needed);
                needed = 0L;
                break;
            }
            else if (free > 0 && needed > free) {
                cnt.decMaxMembers(free);
                needed -= free;
            }
            calculated.put(item.getKey(), cnt);
        }

        // If not reduced all max_members are not enough, there are still leftovers in DB.
        if (needed > 0) {
            for (Map.Entry<Long, MembersCounter> item : allSubs.entrySet()) {
                MembersCounter cnt = item.getValue();
                Long free = cnt.getMaxMembers();
                if (free > 0 && needed <= free) {
                    cnt.decMaxMembers(needed);
                    break;
                }
                else if (free > 0 && needed > free) {
                    cnt.decMaxMembers(free);
                    needed -= free;
                }
                calculated.put(item.getKey(), cnt);
            }
        }

        return calculated;
    }

    /**
     * Update subscription.
     */
    private void updateSubscription(SubscriptionDto sub) {
        final ChannelFamily family = this.createOrUpdateChannelFamily(sub.getProductClass(),
                                                                      sub.getName());
        if (family == null) {
            return;
        }

        Map<Long, MembersCounter> allSubs = new HashMap<Long, MembersCounter>();
        Long total = 0L;
        for (PrivateChannelFamily pcf : family.getPrivateChannelFamilies()) {
            Long orgId = pcf.getOrg().getId();
            if (!allSubs.containsKey(orgId)) {
                allSubs.put(orgId, new MembersCounter(pcf));
            } else {
                allSubs.get(orgId).incMaxMembers(pcf.getMaxMembers());
            }
            total += pcf.getMaxMembers();
        }

        for(final Map.Entry<Long, MembersCounter> item :
                this.calculateSubscriptions(allSubs, total,
                                            sub, family.getId()).entrySet()) {
            if (item.getValue().isDirty()) {
                HashMap<String, Object> p = new HashMap<String, Object>();
                p.put("members", item.getValue().getMaxMembers());
                p.put("cfid", family.getId());
                p.put("orgid", item.getKey());
                ModeFactory.getWriteMode("mgr_sync_queries",
                                         "set_subscription_max_members").executeUpdate(p);
            }
        }
    }

    /**
     * Update entitlement.
     */
    private void updateEntitlement(SubscriptionDto subscription) {
        Date now = new Date();
        for (final String ent : SystemEntitlement.valueOf(
                subscription.getProductClass()).getEntitlements()) {
            for (Iterator<Map<String, Object>> iter = ModeFactory.getMode(
                    "mgr_sync_queries", "entitlement_id", Map.class)
                    .execute(new HashMap<String, Object>(){{put("label", ent);}})
                    .iterator(); iter.hasNext();) {
                final Long entId = (Long) (iter.next()).get("id");
                if (now.compareTo(subscription.getEndDate()) <= 0) {
                    ModeFactory.getWriteMode("mgr_sync_queries",
                                             "set_entitlement_max_members")
                            .executeUpdate(new HashMap<String, Object>(){
                                {put("members", INFINITE);put("group", entId);}});
                }
            }
        }
    }

    /**
     * Sync subscriptions from the SCC to the database.
     * @param subscriptions
     * @throws com.redhat.rhn.manager.content.ContentSyncException
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
                                 "reset_entitlements_bc").executeUpdate(params);
        params.put("max_members", ContentSyncManager.RESET_ENTITLEMENT);
        ModeFactory.getWriteMode("mgr_sync_queries",
                                 "reset_entitlements").executeUpdate(params);

        for (SubscriptionDto meta : this.consolidateSubscriptions(subscriptions)) {
            if (this.isEntitlement(meta.getProductClass())) {
                this.updateEntitlement(meta);
            } else {
                this.updateSubscription(meta);
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
            if (parent.equals(BASE) || availableChannelLabels.contains(parent)) {
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
     */
    public void syncSUSEProductChannels(List<MgrSyncChannel> availableChannels)
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
            if (BASE.equals(parentChannelLabel)) {
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
     * Update the suseUpgradePaths table with values read from upgrade_paths.xml
     */
    public void updateUpgradePaths() throws ContentSyncException {
        Map<String, SUSEUpgradePath> paths = new HashMap<String, SUSEUpgradePath>();
        for (SUSEUpgradePath sup : SUSEProductFactory.findAllSUSEUpgradePaths()) {
            paths.put(String.format("%s-%s",
                                    sup.getFromProduct().getProductId(),
                                    sup.getToProduct().getProductId()), sup);
        }

        for (MgrSyncUpgradePath mup : this.readUpgradePaths()) {
            if (paths.remove(String.format("%s-%s",
                                           mup.getFromProductId(),
                                           mup.getToProductId())) != null) {
                SUSEProduct fromProduct = SUSEProductFactory.lookupByProductId(
                        mup.getFromProductId());
                SUSEProduct toProduct = SUSEProductFactory.lookupByProductId(
                        mup.getToProductId());
                if (fromProduct != null && toProduct != null) {
                    SUSEProductFactory.save(new SUSEUpgradePath(fromProduct, toProduct));
                }
            }
        }

        for (Map.Entry<String, SUSEUpgradePath> entry : paths.entrySet()) {
            SUSEProductFactory.remove(entry.getValue());
        }
    }

    /**
     * Check if a given string is a SUSE Manager entitlement.
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
}
