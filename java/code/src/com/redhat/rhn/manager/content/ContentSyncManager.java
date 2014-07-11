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
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.manager.setup.MirrorCredentialsDto;
import com.redhat.rhn.manager.setup.MirrorCredentialsManager;
import com.redhat.rhn.manager.setup.SubscriptionDto;

import com.suse.contentsync.SUSEChannel;
import com.suse.contentsync.SUSEChannelFamilies;
import com.suse.contentsync.SUSEChannelFamily;
import com.suse.contentsync.SUSEChannels;
import com.suse.contentsync.SUSEUpgradePath;
import com.suse.contentsync.SUSEUpgradePaths;
import com.suse.scc.client.SCCClient;
import com.suse.scc.client.SCCClientException;
import com.suse.scc.model.SCCProduct;
import com.suse.scc.model.SCCRepository;
import com.suse.scc.model.SCCSubscription;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    // Static XML files we parse
    private static String channelsXML = "/usr/share/susemanager/channels.xml";
    private static String channelFamiliesXML = "/usr/share/susemanager/channel_families.xml";
    private static String upgradePathsXML = "/usr/share/susemanager/upgrade_paths.xml";

    /**
     * SUSE Manager system entitlements
     */
    public enum SystemEntitlement {
        SM_ENT_MON_S("monitoring_entitled"),
        SM_ENT_PROV_S("provisioning_entitled"),
        SM_ENT_MGM_S("enterprise_entitled",
                     "bootstrap_entitled"),
        SM_ENT_MGM_V("virtualization_host_platform",
                     "enterprise_entitled",
                     "bootstrap_entitled"),
        SM_ENT_MON_V("monitoring_entitled" ),
        SM_ENT_PROV_V("provisioning_entitled"),
        SM_ENT_MON_Z("monitoring_entitled"),
        SM_ENT_PROV_Z("provisioning_entitled"),
        SM_ENT_MGM_Z("enterprise_entitled",
                     "bootstrap_entitled");

        private final List<String> entitlements;

        SystemEntitlement(String ... entitlements) {
            this.entitlements = Collections.unmodifiableList(
                    new ArrayList<String>(Arrays.asList(entitlements)));
        }

        /**
         * Get entitlements assigned to the product class.
         * @return List of entitlement flags.
         */
        public List<String> getEntitlements() {
            return this.entitlements;
        }
    }

    /**
     * Default constructor.
     */
    public ContentSyncManager() {
    }

    /**
     * Set the path where to find channels.xml.
     * @param pathToChannelsXML the path where to find channels.xml
     */
    public void setChannelsXML(String pathToChannelsXML) {
        channelsXML = pathToChannelsXML;
    }

    /**
     * Set the path where to find channels_families.xml.
     * @param pathToChannelsXML the path where to find channel_families.xml
     */
    public void setChannelFamiliesXML(String pathToChannelsXML) {
        channelFamiliesXML = pathToChannelsXML;
    }

    /**
     * Set the path where to find upgrade_paths.xml.
     * @param pathToXMLFile the path where to find upgrade_paths.xml
     */
    public void setUpgradePathsXML(String pathToXMLFile) {
        upgradePathsXML = pathToXMLFile;
    }

    /**
     * Read the channels.xml file.
     *
     * @return List of parsed channels
     * @throws ContentSyncException in case of an error
     */
    public List<SUSEChannel> readChannels() throws ContentSyncException {
        try {
            Persister persister = new Persister();
            return persister.read(SUSEChannels.class,
                    new File(channelsXML)).getChannels();
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
    public List<SUSEChannelFamily> readChannelFamilies() throws ContentSyncException {
        try {
            Persister persister = new Persister();
            return persister.read(SUSEChannelFamilies.class,
                    new File(channelFamiliesXML)).getFamilies();
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
    public List<SUSEUpgradePath> readUpgradePaths() throws ContentSyncException {
        try {
            Persister persister = new Persister();
            return persister.read(SUSEUpgradePaths.class,
                    new File(upgradePathsXML)).getPaths();
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
        syncSUSEProductChannels();
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
        Map<String, SUSEChannel> channelsXML = new HashMap<String, SUSEChannel>();
        for (SUSEChannel c : readChannels()) {
            channelsXML.put(c.getLabel(), c);
        }

        // Get all vendor channels from the database
        List<Channel> channelsDB = ChannelFactory.listVendorChannels();
        for (Channel c : channelsDB) {
            if (channelsXML.containsKey(c.getLabel())) {
                SUSEChannel channel = channelsXML.get(c.getLabel());
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
                SUSEChannel channel = channelsXML.get(cs.getLabel());
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
        for (SUSEChannelFamily scf : this.readChannelFamilies()) {
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

        for (SUSEChannelFamily family : this.readChannelFamilies()) {
            if (family.getDefaultNodeCount() < 0) {
                sc.put(family.getLabel(), new SubscriptionDto(family.getName(),
                        family.getLabel(), 0, null, null));
            }
        }

        ModeFactory.getWriteMode("System_queries", "delete_scc_subscriptions")
                .executeUpdate(new HashMap<String, Object>(),
                               new ArrayList<String>(sc.keySet()));

        return new ArrayList<SubscriptionDto>(sc.values());
    }

    /**
     * The value of "10" comes from the part, where testers needed about this amount of
     * subscriptions. It turns out that the customers are also have this number and dealing
     * with it on their installations.
     */
    private void resetEntitlementsToDefault() {
        Map<String, Object> params = new HashMap<String, Object>();
        // XXX: Move the SQL query away separately!!
        ModeFactory.getWriteMode("SystemGroup_queries",
                                 "reset_entitlements_bc").executeUpdate(params);
        params.put("max_members", ContentSyncManager.RESET_ENTITLEMENT);
        ModeFactory.getWriteMode("SystemGroup_queries",
                                 "reset_entitlements").executeUpdate(params);
    }

    /**
     * Subscription members counter.
     */
    private static class SubscrCounter {
        private Long maxMembers;
        private final Long currentMembers;
        private Boolean dirty;

        public SubscrCounter(PrivateChannelFamily pcf) {
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
    private Map<Long, SubscrCounter> calculateSubscriptions(
            Map<Long, SubscrCounter> allSubs, Long total, SubscriptionDto subscr, Long oid) {
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
        Map<Long, SubscrCounter> calculated = new HashMap<Long, SubscrCounter>();
        for (Map.Entry<Long, SubscrCounter> item : allSubs.entrySet()) {
            SubscrCounter cnt = item.getValue();
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
            for (Map.Entry<Long, SubscrCounter> item : allSubs.entrySet()) {
                SubscrCounter cnt = item.getValue();
                Long free = cnt.getMaxMembers();
                if (free > 0 && needed <= free) {
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

        Map<Long, SubscrCounter> allSubs = new HashMap();
        Long total = 0L;
        for (PrivateChannelFamily pcf : family.getPrivateChannelFamilies()) {
            Long orgId = pcf.getOrg().getId();
            if (!allSubs.containsKey(orgId)) {
                allSubs.put(orgId, new SubscrCounter(pcf));
            } else {
                allSubs.get(orgId).incMaxMembers(pcf.getMaxMembers());
            }
            total += pcf.getMaxMembers();
        }

        for(final Map.Entry<Long, SubscrCounter> item :
                this.calculateSubscriptions(allSubs, total,
                                            sub, family.getId()).entrySet()) {
            if (item.getValue().isDirty()) {
                HashMap<String, Object> p = new HashMap<String, Object>();
                p.put("members", item.getValue().getMaxMembers());
                p.put("cfid", family.getId());
                p.put("orgid", item.getKey());
                ModeFactory.getWriteMode("SystemGroup_queries",
                                         "set_subscr_max_members").executeUpdate(p);
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
            for (Iterator iter = ModeFactory.getMode(
                    "System_queries", "entitlement_id", Map.class)
                    .execute(new HashMap<String, Object>(){{put("label", ent);}})
                    .iterator(); iter.hasNext();) {
                final Long entId = (Long) ((Map<String, Object>) iter.next()).get("id");
                if (now.compareTo(subscription.getEndDate()) <= 0) {
                    ModeFactory.getWriteMode("SystemGroup_queries",
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
        //this.resetEntitlementsToDefault();
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
            // TODO: Rename that method
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
     * Synchronization of the {@link SUSEProductChannel} relationships.
     */
    public void syncSUSEProductChannels() throws ContentSyncException {
        // Get all currently existing product channel relations
        List<SUSEProductChannel> existingProductChannels =
                SUSEProductFactory.findAllSUSEProductChannels();

        // Create a map containing all installed vendor channels
        Map<String, Channel> installedChannels = new HashMap<String, Channel>();
        for (Channel channel : ChannelFactory.listVendorChannels()) {
            installedChannels.put(channel.getLabel(), channel);
        }

        // Get all available channels (channels.xml) and iterate
        // TODO: Filter this list as in get_available_channels()
        List<SUSEChannel> availableChannels = readChannels();
        for (SUSEChannel availableChannel : availableChannels) {
            // We store only non-optional channels
            if (availableChannel.isOptional()) {
                continue;
            }

            // Set parent channel to null for base channels
            String parentChannelLabel = availableChannel.getParent();
            if (parentChannelLabel.equals("BASE")) {
                parentChannelLabel = null;
            }

            // Lookup every product and insert/update relationships accordingly
            for (com.suse.contentsync.SUSEProduct p : availableChannel.getProducts()) {
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
    public void updateUpgradePaths() {
        // TODO: Implement this as in update_upgrade_pathes_by_config()
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
