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
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
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

    // Static files we parse
    private static final String CHANNELS_XML = "channels.xml";
    private static final String CHANNEL_FAMILIES_XML = "channel_families.xml";
    private static final String UPGRADE_PATHS_XML = "upgrade_paths.xml";

    // This was a guesswork and we so far *have* to stay on this value.
    // https://github.com/SUSE/spacewalk/blob/Manager/susemanager/src/mgr_ncc_sync_lib.py#L69
    private static final Integer RESET_ENTITLEMENT = 10;

    // The "limitless or endless in space" at SUSE is 200000. Of type Long.
    // https://github.com/SUSE/spacewalk/blob/Manager/susemanager/src/mgr_ncc_sync_lib.py#L43
    public static final Long INFINITE = 200000L;
    private static final String FULL_TYPE = "FULL";
    private static final String PROVISIONAL_TYPE = "PROVISIONAL";

    // The default path where to find those
    private String pathPrefix = "/usr/share/susemanager/";

    /**
     * Default constructor.
     */
    public ContentSyncManager() {
    }

    /**
     * Set a directory where to find channels.xml etc.
     * @param path the path prefix to set
     */
    public void setPathPrefix(String path) {
        this.pathPrefix = path;
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
                    new File(pathPrefix + CHANNELS_XML)).getChannels();
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
                    new File(pathPrefix + CHANNEL_FAMILIES_XML)).getFamilies();
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
                    new File(pathPrefix + UPGRADE_PATHS_XML)).getPaths();
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
        updateSubscriptions();
    }

    /**
     * Update channel information in the database.
     * @throws com.redhat.rhn.manager.content.ContentSyncException
     */
    public void updateChannels() throws ContentSyncException {
        // TODO: If this is an ISS slave then do nothing
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
    private ChannelFamily getChannelFamily(String label, String name) {
        ChannelFamily family = ChannelFamilyFactory.lookupByLabel(label, null);
        if (family == null) {
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
            ChannelFamily family = this.getChannelFamily(scf.getLabel(), scf.getName());
            if (family.getPrivateChannelFamilies().isEmpty()) {
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
    private List<SubscriptionDto> consolidateSubscriptions(Collection<SCCSubscription> subscriptions)
            throws ContentSyncException {
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
                    sc.put(productClass, new SubscriptionDto(subscription.getName(), productClass,
                            subscription.getSystemsCount(), start, end));
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
        ModeFactory.getWriteMode("SystemGroup_queries",
                                 "reset_entitlements_bc").executeUpdate(params);
        params.put("max_members", ContentSyncManager.RESET_ENTITLEMENT);
        ModeFactory.getWriteMode("SystemGroup_queries",
                                 "reset_entitlements").executeUpdate(params);
    }

    /**
     * Sync subscriptions from the SCC to the database.
     * @throws com.redhat.rhn.manager.content.ContentSyncException
     */
    public void updateSubscriptions() throws ContentSyncException {
        this.resetEntitlementsToDefault();
        for (SubscriptionDto meta : this.consolidateSubscriptions(this.getSubscriptions())) {
            // Mapping for entitlements
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
            ChannelFamily channelFamily = ChannelFamilyFactory.lookupByLabel(
                    productClass, null);
            // TODO: Implement ncc_rhn_ent_mapping and check it here
            if (channelFamily == null) {
                channelFamily = getChannelFamily(productClass, productClass);
            }

            // Update this product in the database if it is there
            SUSEProduct product = SUSEProductFactory.findSUSEProduct(
                    p.getName(), p.getVersion(), p.getReleaseType(), p.getArch());
            if (product != null) {
                product.setFriendlyName(p.getFriendlyName());
                product.setChannelFamilyId(channelFamily.getId().toString());
                // TODO: Remove this attribute from database if it is not used anywhere
                product.setProductList('Y');
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
                product.setChannelFamilyId(channelFamily.getId().toString());
                PackageArch arch = PackageFactory.lookupPackageArchByLabel(p.getArch());
                product.setArch(arch);
                product.setProductList('Y');
            }
            SUSEProductFactory.save(product);
        }
    }
}
