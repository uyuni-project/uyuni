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

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.channel.ContentSource;
import com.redhat.rhn.domain.channel.DistChannelMap;
import com.redhat.rhn.domain.channel.PrivateChannelFamily;
import com.redhat.rhn.domain.iss.IssFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductChannel;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.product.SUSEUpgradePath;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.server.EntitlementServerGroup;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.ServerGroupType;
import com.redhat.rhn.manager.setup.MirrorCredentialsDto;
import com.redhat.rhn.manager.setup.MirrorCredentialsManager;

import com.suse.mgrsync.MgrSyncChannel;
import com.suse.mgrsync.MgrSyncChannelFamilies;
import com.suse.mgrsync.MgrSyncChannelFamily;
import com.suse.mgrsync.MgrSyncChannels;
import com.suse.mgrsync.MgrSyncDistribution;
import com.suse.mgrsync.MgrSyncProduct;
import com.suse.mgrsync.MgrSyncStatus;
import com.suse.mgrsync.MgrSyncUpgradePath;
import com.suse.mgrsync.MgrSyncUpgradePaths;
import com.suse.scc.client.SCCClient;
import com.suse.scc.client.SCCClientException;
import com.suse.scc.model.SCCProduct;
import com.suse.scc.model.SCCRepository;
import com.suse.scc.model.SCCSubscription;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.simpleframework.xml.core.Persister;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Content synchronization logic.
 */
public class ContentSyncManager {

    // Logger instance
    private static final Logger log = Logger.getLogger(ContentSyncManager.class);

    // This was a guesswork and we so far *have* to stay on this value.
    // https://github.com/SUSE/spacewalk/blob/Manager/susemanager/src/mgr_ncc_sync_lib.py#L69
    private static final Long RESET_ENTITLEMENT = 10L;

    // The "limitless or endless in space" at SUSE is 200000. Of type Long.
    // https://github.com/SUSE/spacewalk/blob/Manager/susemanager/src/mgr_ncc_sync_lib.py#L43
    public static final Long INFINITE = 200000L;
    private static final String PROVISIONAL_TYPE = "PROVISIONAL";

    // Base channels have "BASE" as their parent in channels.xml
    public static final String BASE_CHANNEL = "BASE";

    // Make exceptions for the OES channel family that is still hosted with NCC
    private static final String OES_CHANNEL_FAMILY = "OES2";
    private static final String OES_URL = "https://nu.novell.com/repo/$RCE/" +
            "OES11-SP2-Pool/sle-11-x86_64/repodata/repomd.xml";

    // Static XML files we parse
    private static File channelsXML = new File(
            "/usr/share/susemanager/scc/channels.xml");
    private static File channelFamiliesXML = new File(
            "/usr/share/susemanager/scc/channel_families.xml");
    private static File upgradePathsXML = new File(
            "/usr/share/susemanager/scc/upgrade_paths.xml");

    // File to parse this system's UUID from
    private static final File uuidFile = new File("/etc/zypp/credentials.d/NCCcredentials");
    private static String uuid;

    /**
     * Default constructor.
     */
    public ContentSyncManager() {
    }

    /**
     * Set the channels.xml {@link File} to read from.
     * @param file the channels.xml file
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
            if (log.isDebugEnabled()) {
                log.debug("Read " + channels.size() + " channels from " +
                        channelsXML.getAbsolutePath());
            }
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
            if (log.isDebugEnabled()) {
                log.debug("Read " + channelFamilies.size() + " channel families from " +
                        channelFamiliesXML.getAbsolutePath());
            }
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
            if (log.isDebugEnabled()) {
                log.debug("Read " + upgradePaths.size() + " upgrade paths from " +
                        upgradePathsXML.getAbsolutePath());
            }
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
            scc.setProxySettings(MgrSyncUtils.getRhnProxySettings());
            scc.setUUID(getUUID());
            try {
                List<SCCProduct> products = scc.listProducts();
                for (SCCProduct product : products) {
                    String missing = verifySCCProduct(product);
                    if (StringUtils.isBlank(missing)) {
                        productList.add(product);
                    }
                    else {
                        log.warn("Broken product: " + product.getName() +
                                ", Version: " + product.getVersion() +
                                ", Identifier: " + product.getIdentifier() +
                                " ### Missing attributes: " + missing);
                    }
                }
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
     * Returns all products available to all configured credentials.
     * @param allChannels list of all channels
     * @return list of all available products
     * @throws ContentSyncException in case of an error
     */
    @SuppressWarnings("unchecked")
    public SortedSet<ListedProduct> listProducts(List<MgrSyncChannel> allChannels)
        throws ContentSyncException {
        Collection<MgrSyncProduct> allProducts = new HashSet<MgrSyncProduct>();

        for (SUSEProduct product : SUSEProductFactory.findAllSUSEProducts()) {
            allProducts.add(new MgrSyncProduct(product.getName(), product.getProductId(),
                    product.getVersion()));
        }

        List<MgrSyncChannel> availableChannels = getAvailableChannels(allChannels);
        Map<MgrSyncProduct, Set<MgrSyncChannel>> productToChannelMap =
                getProductToChannelMap(availableChannels);

        // at least one channel available -> corresponding product available
        Collection<MgrSyncProduct> availableProducts = productToChannelMap.keySet();

        // for a product to be installable it must be available and present in our DB
        Collection<MgrSyncProduct> results =
                CollectionUtils.intersection(availableProducts, allProducts);

        return toListedProductList(results, productToChannelMap);
    }

    /**
     * Converts a list of MgrSyncProduct to a list of ListedProduct objects.
     *
     * @param products the products
     * @param productToChannelMap the product to channel map
     * @return the sorted set
     */
    private SortedSet<ListedProduct> toListedProductList(
            Collection<MgrSyncProduct> products,
            Map<MgrSyncProduct, Set<MgrSyncChannel>> productToChannelMap) {

        List<String> installedChannelLabels = getInstalledChannelLabels();

        SortedSet<ListedProduct> result = new TreeSet<ListedProduct>();
        for (MgrSyncProduct product : products) {
            Set<MgrSyncChannel> allChannels = productToChannelMap.get(product);

            Set<MgrSyncChannel> baseChannels = new HashSet<MgrSyncChannel>();
            for (MgrSyncChannel channel : allChannels) {
                if (channel.getParent().equals(BASE_CHANNEL)) {
                    baseChannels.add(channel);
                }
            }

            for (MgrSyncChannel baseChannel : baseChannels) {
                boolean installed =
                        isInstalled(baseChannel,
                                getChildChannels(baseChannel, allChannels),
                                installedChannelLabels);

                result.add(new ListedProduct(product.getName(), product.getId(), product
                        .getVersion(), installed ? MgrSyncStatus.INSTALLED :
                         MgrSyncStatus.AVAILABLE, baseChannel.getArch()));
            }
        }
        return result;
    }

    /**
     * Gets the child channels of a base channel.
     *
     * @param baseChannel the base channel
     * @param allChannels all the channels
     * @return the subset of allChannels which are baseChannel's child channels
     */
    private Collection<MgrSyncChannel> getChildChannels(MgrSyncChannel baseChannel,
            Collection<MgrSyncChannel> allChannels) {
        Collection<MgrSyncChannel> result = new HashSet<MgrSyncChannel>();
        for (MgrSyncChannel channel : allChannels) {
            if (channel.getParent().equals(baseChannel.getLabel())) {
                result.add(channel);
            }
        }
        return result;
    }

    /**
     * Checks if a product is installed.
     *
     * @param baseChannel the product's base channel
     * @param childChannels the product's child channels
     * @param installedChannelLabels the installed channel labels
     * @return true, if the product is installed
     */
    private boolean isInstalled(MgrSyncChannel baseChannel,
            Collection<MgrSyncChannel> childChannels,
            Collection<String> installedChannelLabels) {
        if (!installedChannelLabels.contains(baseChannel.getLabel())) {
            return false;
        }
        for (MgrSyncChannel channel : childChannels) {
            if (!channel.isOptional() &&
                !installedChannelLabels.contains(channel.getLabel())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Gets the product to channel map.
     *
     * @param channels the channels
     * @return the product to channel map
     */
    private Map<MgrSyncProduct, Set<MgrSyncChannel>> getProductToChannelMap(
            Collection<MgrSyncChannel> channels) {
        Map<MgrSyncProduct, Set<MgrSyncChannel>> result =
                new HashMap<MgrSyncProduct, Set<MgrSyncChannel>>();

        for (final MgrSyncChannel channel : channels) {
            for (MgrSyncProduct product : channel.getProducts()) {
                if (result.containsKey(product)) {
                    result.get(product).add(channel);
                }
                else {
                    result.put(product,
                            new HashSet<MgrSyncChannel>() { { add(channel); } });
                }
            }
        }
        return result;
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
            scc.setProxySettings(MgrSyncUtils.getRhnProxySettings());
            scc.setUUID(getUUID());
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
            scc.setProxySettings(MgrSyncUtils.getRhnProxySettings());
            scc.setUUID(getUUID());
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
     * @param channelFamilies List of families.
     * @throws ContentSyncException
     */
    public void updateChannelFamilies(Collection<MgrSyncChannelFamily> channelFamilies)
            throws ContentSyncException {
        for (MgrSyncChannelFamily channelFamily : channelFamilies) {
            ChannelFamily family = createOrUpdateChannelFamily(
                    channelFamily.getLabel(), channelFamily.getName());
            // Create rhnPrivateChannelFamily entry if it doesn't exist
            if (family != null && family.getPrivateChannelFamilies().isEmpty()) {
                PrivateChannelFamily pcf = new PrivateChannelFamily();
                pcf.setCreated(new Date());
                pcf.setCurrentMembers(0L);
                pcf.setMaxMembers(0L);
                // Set the default organization (id = 1)
                pcf.setOrg(OrgFactory.getSatelliteOrg());
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
     * Returns two lists of product classes that we have a subscription for, representing
     * entitlements and channel subscriptions separately.
     *
     * @param subscriptions subscriptions as we get them from SCC
     * @return consolidated subscriptions
     * @throws ContentSyncException
     */
    public ConsolidatedSubscriptions consolidateSubscriptions(
            Collection<SCCSubscription> subscriptions) throws ContentSyncException {
        ConsolidatedSubscriptions consolidated = new ConsolidatedSubscriptions();
        Date now = new Date();
        for (SCCSubscription subscription : subscriptions) {
            Date start = subscription.getStartsAt() == null ?
                    new Date() : subscription.getStartsAt();
            Date end = subscription.getExpiresAt();
            for (String productClass : subscription.getProductClasses()) {
                if ((now.compareTo(start) >= 0
                        && (end == null || now.compareTo(end) <= 0))
                        && !subscription.getType().equals(PROVISIONAL_TYPE)) {
                    // Distinguish between subscriptions and entitlements here
                    if (isEntitlement(productClass)) {
                        consolidated.addSystemEntitlement(productClass);
                    }
                    else {
                        consolidated.addChannelSubscription(productClass);
                    }
                }
            }
        }

        // Add free product classes (default_node_count = -1) as subscriptions
        for (MgrSyncChannelFamily family : readChannelFamilies()) {
            if (family.getDefaultNodeCount() < 0) {
                consolidated.addChannelSubscription(family.getLabel());
            }
        }

        // Add OES if one of the OES repos is available via HEAD request
        if (verifyOESRepo()) {
            consolidated.addChannelSubscription(OES_CHANNEL_FAMILY);
        }

        return consolidated;
    }

    /**
     * Updates max_members for channel subscriptions given a list of product classes.
     * @param productClasses list of product classes we have a subscription for.
     */
    public void updateChannelSubscriptions(List<String> productClasses) {
        // These are product classes we have a subscription for
        List<ChannelFamily> allChannelFamilies =
                ChannelFamilyFactory.getAllChannelFamilies();
        for (ChannelFamily channelFamily : allChannelFamilies) {
            Set<PrivateChannelFamily> privateFamilies =
                    channelFamily.getPrivateChannelFamilies();

            // Match with subscribed product classes
            if (productClasses.contains(channelFamily.getLabel())) {
                // We have a subscription
                int sumMaxMembers = 0;
                PrivateChannelFamily satelliteOrgPrivateChannelFamily = null;
                for (PrivateChannelFamily pcf : privateFamilies) {
                    if (pcf.getOrg().getId() == 1) {
                        satelliteOrgPrivateChannelFamily = pcf;
                    }
                    else if (pcf.getOrg().getId() > 1) {
                        sumMaxMembers += pcf.getMaxMembers();
                    }
                }
                satelliteOrgPrivateChannelFamily.setMaxMembers(INFINITE - sumMaxMembers);
            }
            else {
                // No subscription, reset to 0
                for (PrivateChannelFamily pcf : privateFamilies) {
                    pcf.setMaxMembers(0L);
                }
            }

            ChannelFamilyFactory.save(channelFamily);
        }
    }

    /**
     * Updates max_members for all relevant system entitlements.
     * @param productClasses list of product classes we have a subscription for.
     */
    public void updateSystemEntitlements(List<String> productClasses) {
        // For all relevant system entitlements
        for (String systemEntitlement : SystemEntitlement.getAllEntitlements()) {
            ServerGroupType sgt = ServerFactory.lookupServerGroupTypeByLabel(
                    systemEntitlement);

            // Get the product classes for a given entitlement
            List<String> productClassesEnt = SystemEntitlement.getProductClasses(
                    systemEntitlement);
            if (!Collections.disjoint(productClasses, productClassesEnt)) {
                // There is a subscription: set (INFINITE - maxMembers) to org one
                int maxMembers = sumMaxMembersAllNonSatelliteOrgs(sgt);
                EntitlementServerGroup serverGroup = ServerGroupFactory.lookupEntitled(
                        OrgFactory.getSatelliteOrg(), sgt);
                serverGroup.setMaxMembers(INFINITE - maxMembers);
                ServerGroupFactory.save(serverGroup);
            }
            else {
                // Set to RESET_ENTITLEMENT in org one
                EntitlementServerGroup serverGroup = ServerGroupFactory.lookupEntitled(
                        OrgFactory.getSatelliteOrg(), sgt);
                serverGroup.setMaxMembers(RESET_ENTITLEMENT);
                ServerGroupFactory.save(serverGroup);

                // Reset max_members to null for all other orgs
                List<Org> allOrgs = OrgFactory.lookupAllOrgs();
                for (Org org : allOrgs) {
                    if (org.getId() != 1) {
                        serverGroup = ServerGroupFactory.lookupEntitled(org, sgt);
                        if (serverGroup != null) {
                            serverGroup.setMaxMembers(null);
                            ServerGroupFactory.save(serverGroup);
                        }
                    }
                }
            }
        }
    }

    /**
     * Sync subscriptions from SCC to the database after consolidation.
     * @param subscriptions list of subscriptions as we get them from SCC
     * @throws ContentSyncException
     */
    public void updateSubscriptions(Collection<SCCSubscription> subscriptions)
            throws ContentSyncException {
        ConsolidatedSubscriptions consolidated = consolidateSubscriptions(subscriptions);
        updateSystemEntitlements(consolidated.getSystemEntitlements());
        updateChannelSubscriptions(consolidated.getChannelSubscriptions());
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
            ChannelFamily channelFamily = null;
            String productClass = p.getProductClass();

            if (productClass != null) {
                channelFamily = createOrUpdateChannelFamily(productClass, productClass);
            }

            // Update this product in the database if it is there
            SUSEProduct product = SUSEProductFactory.findSUSEProduct(
                    p.getIdentifier(), p.getVersion(), p.getReleaseType(), p.getArch());
            if (product != null) {
                // it is not guaranteed for this ID to be stable in time, as it
                // depends on IBS
                product.setProductId(p.getId());
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
                // Convert those to lower case to match channels.xml format
                product.setName(p.getIdentifier().toLowerCase());
                // Version rarely can be null.
                product.setVersion(p.getVersion() != null ?
                                   p.getVersion().toLowerCase() : null);
                // Release Type often can be null.
                product.setRelease(p.getReleaseType() != null ?
                                   p.getReleaseType().toLowerCase() : null);
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
     * @param allChannels List of {@link MgrSyncChannel}
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
     * @param availableChannels List of {@link MgrSyncChannel}
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
            // We store relationships only for mandatory channels
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
                // Product can be null, because previously it was skipped due to broken
                // data in the SCC. In this case we skip them all.
                if (product == null) {
                    continue;
                }

                // Get the channel in case it is installed
                Channel channel = null;
                if (installedChannels.containsKey(availableChannel.getLabel())) {
                    channel = installedChannels.get(availableChannel.getLabel());
                }

                // Update or insert the product/channel relationship
                SUSEProductChannel spc = SUSEProductFactory.lookupSUSEProductChannel(
                        availableChannel.getLabel(), product.getProductId());
                if (spc == null) {
                    spc = new SUSEProductChannel();
                    spc.setChannelLabel(availableChannel.getLabel());
                }
                spc.setProduct(product);
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
     * @throws com.redhat.rhn.manager.content.ContentSyncException
     */
    public void updateUpgradePaths() throws ContentSyncException {
        // Get all paths from DB and create map that eventually will hold the ones to remove
        List<SUSEUpgradePath> upgradePathsDB = SUSEProductFactory.findAllSUSEUpgradePaths();
        Map<String, SUSEUpgradePath> pathsToRemove = new HashMap<String, SUSEUpgradePath>();
        for (SUSEUpgradePath path : upgradePathsDB) {
            String identifier = String.format("%s-%s",
                    path.getFromProduct().getProductId(),
                    path.getToProduct().getProductId());
            pathsToRemove.put(identifier, path);
        }

        // Iterate through all paths in the XML file and lookup both products first
        for (MgrSyncUpgradePath path : readUpgradePaths()) {
            SUSEProduct fromProduct = SUSEProductFactory.lookupByProductId(
                    path.getFromProductId());
            SUSEProduct toProduct = SUSEProductFactory.lookupByProductId(
                    path.getToProductId());
            if (fromProduct != null && toProduct != null) {
                // Products found, get the existing path object from map by removing
                String identifier = String.format("%s-%s",
                        path.getFromProductId(), path.getToProductId());
                SUSEUpgradePath existingPath = null;
                if (pathsToRemove.keySet().contains(identifier)) {
                    existingPath = pathsToRemove.remove(identifier);
                }

                // Insert or update the existing path
                if (existingPath == null) {
                    SUSEProductFactory.save(new SUSEUpgradePath(fromProduct, toProduct));
                }
                else {
                    SUSEProductFactory.save(existingPath);
                }
            }
        }

        // Remove all those paths that were not inserted or updated
        if (log.isDebugEnabled()) {
            log.debug("Removing " + pathsToRemove.size() + " upgrade paths.");
        }
        for (Map.Entry<String, SUSEUpgradePath> entry : pathsToRemove.entrySet()) {
            SUSEProductFactory.remove(entry.getValue());
        }
    }

    /**
     * Return the list of available channels with their status.
     * @param repositories list of repos {@link SCCRepository} from SCC to match against
     * @return list of channels
     * @throws com.redhat.rhn.manager.content.ContentSyncException
     */
    public List<MgrSyncChannel> listChannels(Collection<SCCRepository> repositories)
            throws ContentSyncException {
        // This list will be returned
        List<MgrSyncChannel> channels = new ArrayList<MgrSyncChannel>();
        List<String> installedChannelLabels = getInstalledChannelLabels();

        // Determine the channel status
        for (MgrSyncChannel c : getAvailableChannels(readChannels())) {
            if (installedChannelLabels.contains(c.getLabel())) {
                c.setStatus(MgrSyncStatus.INSTALLED);
            }
            else if (isMirrorable(c, repositories)) {
                c.setStatus(MgrSyncStatus.AVAILABLE);
            }
            else {
                c.setStatus(MgrSyncStatus.UNAVAILABLE);
            }
            channels.add(c);
        }

        return channels;
    }

    /**
     * For a given channel, check if it is mirrorable.
     * @param channel Channel
     * @param repos list of repos from SCC to match against
     * @return true if channel is mirrorable, false otherwise
     */
    public boolean isMirrorable(MgrSyncChannel channel, Collection<SCCRepository> repos) {
        // No source URL means it's mirrorable
        String sourceUrl = channel.getSourceUrl();
        if (StringUtils.isBlank(sourceUrl)) {
            return true;
        }

        // Check OES availability via sending an HTTP HEAD request
        if (channel.getFamily().equals(OES_CHANNEL_FAMILY)) {
            return verifyOESRepo();
        }

        // Remove trailing slashes before matching URLs
        sourceUrl = removeTrailingSlashes(sourceUrl);

        // Setup a list holding the URLs of all accessible repos
        List<String> sourceURLs = new ArrayList<String>();
        for (SCCRepository repo : repos) {
            sourceURLs.add(removeTrailingSlashes(repo.getUrl()));
        }

        // Match the channel source URL against URLs we got from SCC
        boolean mirrorable = false;
        if (sourceURLs.contains(sourceUrl)) {
            mirrorable = true;
        }
        return mirrorable;
    }

    /**
     * Add a new channel to the database.
     * @param label the label of the channel to be added.
     * @param repositories list of repos to use for the availability check
     * @throws ContentSyncException in case of problems
     */
    public void addChannel(String label, Collection<SCCRepository> repositories)
            throws ContentSyncException {
        // Return immediately if the channel is already there
        if (ChannelFactory.doesChannelLabelExist(label)) {
            if (log.isDebugEnabled()) {
                log.debug("Channel exists: " + label);
            }
            return;
        }

        // Lookup the channel in available channels
        MgrSyncChannel channel = null;
        List<MgrSyncChannel> channels = getAvailableChannels(readChannels());
        for (MgrSyncChannel c : channels) {
            if (c.getLabel().equals(label)) {
                channel = c;
                break;
            }
        }
        if (channel == null) {
            throw new ContentSyncException("Channel is not available: " + label);
        }
        else if (!isMirrorable(channel, repositories)) {
            throw new ContentSyncException("Channel is not mirrorable: " + label);
        }

        // Create channel and save it in the database
        Channel dbChannel = ChannelFactory.createChannel();
        dbChannel.setBaseDir("/dev/null");
        dbChannel.setChannelArch(MgrSyncUtils.getChannelArch(channel));
        dbChannel.setChannelFamily(ChannelFamilyFactory.lookupByLabel(
                channel.getFamily(), null));
        dbChannel.setChecksumType(ChannelFactory.findChecksumTypeByLabel("sha1"));
        dbChannel.setDescription(channel.getDescription());
        dbChannel.setLabel(label);
        dbChannel.setName(channel.getName());
        dbChannel.setParentChannel(MgrSyncUtils.getParentChannel(channel));
        // TODO: dbChannel.setProduct(productIn);
        // TODO: dbChannel.setProductName(productNameIn);
        dbChannel.setSummary(channel.getSummary());
        dbChannel.setUpdateTag(channel.getUpdateTag());

        // Create or link the content source
        // TODO: Handle the alternative mirror URL feature (see _alternativeMirrorUrl())
        // TODO: Add the mirrorcred query string to the URL?
        String url = channel.getSourceUrl();
        if (!StringUtils.isBlank(url)) {
            ContentSource source = ChannelFactory.findVendorContentSourceByRepo(url);
            if (source == null) {
                source = ChannelFactory.createRepo();
                source.setLabel(channel.getLabel());
                source.setMetadataSigned(channel.isSigned());
                source.setOrg(null);
                source.setSourceUrl(url);
                source.setType(ChannelFactory.CONTENT_SOURCE_TYPE_YUM);
                ChannelFactory.save(source);
            }
            dbChannel.getSources().add(source);
        }

        // Save the channel
        ChannelFactory.save(dbChannel);

        // Create product to channel relationships (for mandatory channels only)
        if (!channel.getOptional()) {
            // Set parent channel to null for base channels
            String parentChannelLabel = channel.getParent();
            if (BASE_CHANNEL.equals(parentChannelLabel)) {
                parentChannelLabel = null;
            }

            // Lookup products and create the relations
            for (MgrSyncProduct p : channel.getProducts()) {
                SUSEProduct product = SUSEProductFactory.lookupByProductId(p.getId());
                if (product == null) {
                    throw new ContentSyncException(
                            "Product not found (" + p.getId() + ") please refresh!");
                }

                // Update or insert the product/channel relationship
                SUSEProductChannel spc = SUSEProductFactory.lookupSUSEProductChannel(
                        channel.getLabel(), product.getProductId());
                if (spc == null) {
                    spc = new SUSEProductChannel();
                    spc.setChannelLabel(channel.getLabel());
                }
                spc.setProduct(product);
                spc.setParentChannelLabel(parentChannelLabel);
                spc.setChannel(dbChannel);
                SUSEProductFactory.save(spc);
            }
        }

        // Create DistChannelMap if channels.xml contains a distribution
        MgrSyncDistribution dist = channel.getDistribution();
        if (dist != null) {
            DistChannelMap dcm = new DistChannelMap(null, dist.getOs(), dist.getRelease(),
                    dbChannel.getChannelArch(), dbChannel);
            ChannelFactory.save(dcm);
        }
    }

    /**
     * Check if a given string is a product class representing a system entitlement.
     * @param s string to check if it represents a system entitlement
     * @return true if s is a system entitlement, else false.
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

    /**
     * Sum up the max_members over all orgs for any given {@link ServerGroupType}.
     * @param serverGroupType
     * @return sum of max_members for all orgs but org one
     */
    private static int sumMaxMembersAllNonSatelliteOrgs(ServerGroupType serverGroupType) {
        int sum = 0;
        List<Org> allOrgs = OrgFactory.lookupAllOrgs();
        for (Org org : allOrgs) {
            if (org.getId() != 1) {
                EntitlementServerGroup serverGroup = ServerGroupFactory
                        .lookupEntitled(org, serverGroupType);
                if (serverGroup != null) {
                    Long maxMembers = serverGroup.getMaxMembers();
                    sum += maxMembers == null ? 0 : maxMembers;
                }
            }
        }
        return sum;
    }

    /**
     * Method for verification of the data consistency and report what is missing.
     * Verify if SCCProduct has correct data that meets database constraints.
     * @param product {@link SCCProduct}
     * @return comma separated list of missing attribute names
     */
    private String verifySCCProduct(SCCProduct product) {
        List<String> missingAttributes = new ArrayList<String>();
        if (product.getProductClass() == null) {
            missingAttributes.add("Product Class");
        }
        if (product.getName() == null) {
            missingAttributes.add("Name");
        }
        if (product.getVersion() == null) {
            missingAttributes.add("Version");
        }
        if (product.getVersion() == null) {
            missingAttributes.add("Product ID");
        }
        return StringUtils.join(missingAttributes, ", ");
    }

    /**
     * Check if OES repos are available by sending a HEAD request to one of them. Once
     * we have access with at least one of the available credentials, it means that the
     * customer has bought the product.
     *
     * @return true if there is access to an OES repository, false otherwise
     */
    private boolean verifyOESRepo() {
        List<MirrorCredentialsDto> credentials =
                new MirrorCredentialsManager().findMirrorCredentials();
        // Query OES repo for all mirror credentials until success
        for (MirrorCredentialsDto creds : credentials) {
            int responseCode;
            try {
                responseCode = MgrSyncUtils.sendHeadRequest(
                        OES_URL, creds.getUser(), creds.getPassword());
                if (log.isDebugEnabled()) {
                    log.debug("OES repo response code for " +
                            creds.getUser() + ": " + responseCode);
                }
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return true;
                }
            } catch (SCCClientException e) {
                log.error(e.getMessage());
            }
        }
        return false;
    }

    /**
     * Try to read this system's UUID from file or return a cached value. The UUID will
     * be sent to SCC for debugging purposes.
     * @return this system's UUID
     */
    private String getUUID() {
        if (uuid == null) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(uuidFile));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("username")) {
                        uuid = line.substring(line.lastIndexOf('=') + 1);
                    }
                }
            }
            catch (FileNotFoundException e) {
                log.warn("Error reading UUID: " + e.getMessage());
            }
            catch (IOException e) {
                log.warn("Error reading UUID: " + e.getMessage());
            }
            finally {
                if (reader != null) {
                    try {
                        reader.close();
                    }
                    catch (IOException e) {
                        log.warn("Error reading UUID: " + e.getMessage());
                    }
                }
            }
        }
        return uuid;
    }

    /**
     * Gets the installed channel labels.
     *
     * @return the installed channel labels
     */
    private List<String> getInstalledChannelLabels() {
        List<Channel> installedChannels = ChannelFactory.listVendorChannels();
        List<String> installedChannelLabels = new ArrayList<String>();
        for (Channel c : installedChannels) {
            installedChannelLabels.add(c.getLabel());
        }
        return installedChannelLabels;
    }

    /**
     * Strip off trailing slashes from a given string.
     * @param url the string to remove trailing slashes from
     * @return string without trailing slashes
     */
    private String removeTrailingSlashes(String url) {
        while (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }
}
