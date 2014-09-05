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

import com.redhat.rhn.common.hibernate.HibernateFactory;
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
import com.redhat.rhn.domain.rhnpackage.PackageArch;
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

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.simpleframework.xml.core.Persister;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    // Source URL handling
    private static final String OFFICIAL_REPO_HOST = "nu.novell.com";
    private static final String MIRRCRED_QUERY = "credentials=mirrcred";

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

    // This file is touched once the server has been migrated to SCC
    public static final String SCC_MIGRATED = "/var/lib/spacewalk/scc/migrated";

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
                    // Check for missing attributes
                    String missing = verifySCCProduct(product);
                    if (!StringUtils.isBlank(missing)) {
                        log.warn("Broken product: " + product.getName() +
                                ", Version: " + product.getVersion() +
                                ", Identifier: " + product.getIdentifier() +
                                ", Product ID: " + product.getId() +
                                " ### Missing attributes: " + missing);
                    }

                    // Add product in any case
                    productList.add(product);
                }
            }
            catch (SCCClientException e) {
                log.error(e.getMessage(), e);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Found " + productList.size() + " available products.");
        }

        // HACK: some SCC products do not have correct data
        // to be removed when SCC team fixes this
        addDirtyFixes(productList);

        return productList;
    }

    /**
     * HACK: fixes data in SCC products that do not have it correct
     * To be removed when SCC team fixes this
     * @param products the products
     */
    @SuppressWarnings("unchecked")
    public void addDirtyFixes(Collection<SCCProduct> products) {
        for (SCCProduct product : products) {
            // make version numbering consistent
            String version = product.getVersion();
            if (version != null) {
                Matcher spMatcher =
                        Pattern.compile(".*SP([0-9]).*").matcher(product.getFriendlyName());
                Matcher versionMatcher =
                        Pattern.compile("([0-9]+)").matcher(product.getVersion());
                if (spMatcher.matches() && versionMatcher.matches()) {
                    product.setVersion(versionMatcher.group(1) + "." + spMatcher.group(1));
                }
            }

            // make naming consistent: strip arch or VMWARE suffix
            String friendlyName = product.getFriendlyName();
            List<PackageArch> archs =
                    HibernateFactory.getSession().createCriteria(PackageArch.class).list();
            for (PackageArch arch:archs) {
                if (friendlyName.endsWith(" " + arch.getLabel())) {
                    friendlyName = friendlyName.substring(0, friendlyName.length() -
                        arch.getLabel().length() - 1);
                }
            }
            if (friendlyName.endsWith(" VMWARE")) {
                friendlyName = friendlyName.substring(0, friendlyName.length() - 7);
            }

            // make naming consistent: add VMWare suffix where appropriate
            String productClass = product.getProductClass();
            if (productClass != null && productClass.toLowerCase().contains("vmware")) {
                friendlyName += " VMWare";
            }
            product.setFriendlyName(friendlyName);

            // Fix missing SLE12 product classes
            if (StringUtils.isBlank(product.getProductClass()) &&
                    product.getVersion().equals("12")) {
                int productId = product.getId();
                switch (productId) {
                    case 1117: // SLES (x86_64)
                    case 1150: // sle-module-legacy (x86_64)
                    case 1153: // sle-module-web-scripting (x86_64)
                    case 1212: // sle-module-adv-systems-management (x86_64)
                    case 1220: // sle-module-public-cloud (x86_64)
                        product.setProductClass("7261");
                        break;
                    case 1118: // SLED (x86_64)
                    case 1222: // sle-we (x86_64)
                        product.setProductClass("7260");
                        break;
                    case 1156: // sle-ha-geo (s390x)
                    case 1157: // sle-ha-geo (x86_64)
                        product.setProductClass("SLE-HAE-GEO");
                        break;
                    case 1244: // sle-ha (s390x)
                        product.setProductClass("SLE-HAE-Z");
                        break;
                    case 1245: // sle-ha (x86_64)
                        product.setProductClass("SLE-HAE-X86");
                        break;
                    case 1116: // SLES (ppc)
                    case 1148: // sle-module-legacy (ppc)
                    case 1151: // sle-module-web-scripting (ppc)
                    case 1218: // sle-module-public-cloud (ppc)
                        product.setProductClass("SLES-PPC");
                        break;
                    case 1115: // SLES (s390x)
                    case 1149: // sle-module-legacy (s390x)
                    case 1152: // sle-module-web-scripting (s390x)
                    case 1219: // sle-module-public-cloud (s390x)
                        product.setProductClass("SLES-Z");
                        break;
                    case 1145: // sle-sdk (ppc)
                    case 1146: // sle-sdk (s390x)
                    case 1223: // sle-sdk (x86_64)
                        product.setProductClass("SLE-SDK");
                        break;
                    default:
                        break;
                }
            }
        }

        // add OES
        products.add(new SCCProduct(1232, "Open_Enterprise_Server", "11", null, null,
                "Novell Open Enterprise Server 2 11", "OES2"));
        products.add(new SCCProduct(1241, "Open_Enterprise_Server", "11.1", null, null,
                "Novell Open Enterprise Server 2 11.1", "OES2"));
        products.add(new SCCProduct(1242, "Open_Enterprise_Server", "11.2", null, null,
                "Novell Open Enterprise Server 2 11.2", "OES2"));
    }

    /**
     * Returns all available products in user-friendly format.
     * @param availableChannels list of available channels
     * @return list of all available products
     * @throws ContentSyncException in case of an error
     */
    public Collection<ListedProduct> listProducts(List<MgrSyncChannel> availableChannels)
        throws ContentSyncException {
        // get all products in the DB
        Collection<MgrSyncProduct> dbProducts = new HashSet<MgrSyncProduct>();
        for (SUSEProduct product : SUSEProductFactory.findAllSUSEProducts()) {
            dbProducts.add(new MgrSyncProduct(product.getFriendlyName(), product
                    .getProductId(), product.getVersion()));
        }

        // get all the channels we have an entitlement for
        Map<MgrSyncProduct, Set<MgrSyncChannel>> productToChannelMap =
                getProductToChannelMap(availableChannels);

        // get all the products we have an entitlement for based on the assumption:
        // at least one channel is entitled -> corresponding product is entitled
        Collection<MgrSyncProduct> availableProducts = productToChannelMap.keySet();

        // filter result with only available products
        dbProducts.retainAll(availableProducts);

        // convert to Collection<ListedProduct> and return
        return toListedProductList(dbProducts, productToChannelMap);
    }

    /**
     * Converts a list of MgrSyncProduct to a collection of ListedProduct objects.
     *
     * @param products the products
     * @param productToChannelMap the product to channel map
     * @return the sorted set
     */
    private Collection<ListedProduct> toListedProductList(
            Collection<MgrSyncProduct> products,
            Map<MgrSyncProduct, Set<MgrSyncChannel>> productToChannelMap) {

        // get a map from channel labels to channels
        Map<String, MgrSyncChannel> labelsTochannels =
                new HashMap<String, MgrSyncChannel>();
        for (Set<MgrSyncChannel> channels : productToChannelMap.values()) {
            for (MgrSyncChannel channel : channels) {
                labelsTochannels.put(channel.getLabel(), channel);
            }
        }

        // get a map from every channel to its base channel
        Map<MgrSyncChannel, MgrSyncChannel> baseChannels =
                new HashMap<MgrSyncChannel, MgrSyncChannel>();
        for (MgrSyncChannel channel : labelsTochannels.values()) {
            MgrSyncChannel parent = channel;
            while (!parent.getParent().equals("BASE")) {
                parent = labelsTochannels.get(parent.getParent());
            }
            baseChannels.put(channel, parent);
        }

        // convert every MgrSyncProduct to ListedProducts objects (one per base channel)
        SortedSet<ListedProduct> all = new TreeSet<ListedProduct>();
        for (MgrSyncProduct product : products) {
            Set<MgrSyncChannel> channels = productToChannelMap.get(product);
            Map<MgrSyncChannel, ListedProduct> baseMap =
                    new HashMap<MgrSyncChannel, ListedProduct>();
            for (MgrSyncChannel channel : channels) {
                MgrSyncChannel base = baseChannels.get(channel);

                ListedProduct listedProduct = baseMap.get(base);
                // if this is a new product
                if (listedProduct == null) {
                    SUSEProduct dbProduct =
                            SUSEProductFactory.lookupByProductId(product.getId());
                    PackageArch arch = dbProduct.getArch();
                    // and if the base channel arch matches the product arch
                    if (arch == null || arch.getName().equals(base.getArch())) {
                        // add it to the product map
                        listedProduct =
                                new ListedProduct(product.getName(), product.getId(),
                                        product.getVersion(), base);
                        listedProduct.addChannel(channel);
                        baseMap.put(base, listedProduct);
                    }
                }
                else {
                    listedProduct.addChannel(channel);
                }
            }
            all.addAll(baseMap.values());
        }

        // divide base from extension products
        Collection<ListedProduct> bases = new LinkedList<ListedProduct>();
        Collection<ListedProduct> extensions = new LinkedList<ListedProduct>();
        for (ListedProduct product : all) {
            boolean isBase = false;
            for (MgrSyncChannel channel : product.getChannels()) {
                if (channel.getParent().equals(BASE_CHANNEL)) {
                    isBase = true;
                    break;
                }
            }
            if (isBase) {
                bases.add(product);
            }
            else {
                extensions.add(product);
            }
        }

        // add base-extension relationships
        for (ListedProduct base : bases) {
            for (ListedProduct extension : extensions) {
                for (MgrSyncChannel baseChannel : base.getChannels()) {
                    for (MgrSyncChannel extensionChannel : extension.getChannels()) {
                        if (extensionChannel.getParent().equals(baseChannel.getLabel())) {
                            base.addExtension(extension);
                        }
                    }
                }
            }
        }

        // HACK: add dirty fixes
        addDirtyFixes(all, bases);

        return bases;
    }

    /**
     * HACK: fixes the data set. This code should really not be here, rather on
     * the SCC side.
     *
     * @param all all the products
     * @param bases base products
     */
    private void addDirtyFixes(Collection<ListedProduct> all,
            Collection<ListedProduct> bases) {

        // remove SP1 extensions from SP2 base products and vice versa
        for (ListedProduct product : bases) {
            String productVersion = product.getVersion();
            if (productVersion.startsWith("11.")) {
                Collection<ListedProduct> wrongSP = new LinkedList<ListedProduct>();
                for (ListedProduct extension : product.getExtensions()) {
                    String extensionVersion = extension.getVersion();
                    if (extension.getFriendlyName().startsWith("SUSE") &&
                        extensionVersion.startsWith("11.") &&
                        !extensionVersion.equals(productVersion)) {
                        wrongSP.add(extension);
                    }
                }
                product.getExtensions().removeAll(wrongSP);
            }
        }

        // remove SP2 only extensions from SP1 base products
        for (ListedProduct product : bases) {
            if (product.getVersion().equals("11.1")) {
                Collection<ListedProduct> sp2ProductsInSp1 =
                        new LinkedList<ListedProduct>();
                for (ListedProduct extension : product.getExtensions()) {
                    String friendlyName = extension.getFriendlyName();
                    if (friendlyName.equals("SUSE Cloud 1.0") ||
                        friendlyName.equals("SUSE Lifecycle Management Server 1.3") ||
                        friendlyName.equals("SUSE WebYaST 1.3") ||
                        friendlyName.equals("Novell Open Enterprise Server 2 11.1")) {
                        sp2ProductsInSp1.add(extension);
                    }
                }
                product.getExtensions().removeAll(sp2ProductsInSp1);
            }
        }

        // remove SP2 only extensions from SP1 base products
        for (ListedProduct product : bases) {
            if (product.getVersion().equals("11.2")) {
                Collection<ListedProduct> sp1ProductsInSp2 =
                        new LinkedList<ListedProduct>();
                for (ListedProduct extension : product.getExtensions()) {
                    String friendlyName = extension.getFriendlyName();
                    if (friendlyName.equals("Novell Open Enterprise Server 2 11") ||
                        friendlyName.equals(
                                "SUSE Linux Enterprise Subscription Management Tool 11")) {
                        sp1ProductsInSp2.add(extension);
                    }
                }
                product.getExtensions().removeAll(sp1ProductsInSp2);
            }
        }
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
                // Add the mirror credentials ID to all returned repos
                int credsId = c.getId().intValue();
                for (SCCRepository r : repos) {
                    r.setCredentialsId(credsId);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Found " + repos.size() +
                            " repos with credentials: " + credsId);
                }
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
    public void updateChannels(Collection<SCCRepository> repos) throws ContentSyncException {
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
                MgrSyncChannel channel = channelsXML.get(cs.getLabel());
                Integer credsId = isMirrorable(channel, repos);
                if (credsId != null) {
                    String sourceURL = setupSourceURL(channel.getSourceUrl(), credsId);
                    if (!cs.getSourceUrl().equals(sourceURL)) {
                        cs.setSourceUrl(sourceURL);
                        ChannelFactory.save(cs);
                    }
                }
                else {
                    // Channel is no longer mirrorable
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
        if (verifyOESRepo() != null) {
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
                if (satelliteOrgPrivateChannelFamily != null) {
                    satelliteOrgPrivateChannelFamily
                            .setMaxMembers(INFINITE - sumMaxMembers);
                }
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

                PackageArch pArch = PackageFactory.lookupPackageArchByLabel(p.getArch());
                if (pArch == null && p.getArch() != null) {
                    // unsupported architecture, skip the product
                    log.error("Unknown architecture '" + p.getArch()
                            + "'. This may be caused by a missing database migration");
                    continue;
                }

                product.setArch(pArch);
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

                // TODO: make the repo source URL point to local path in 'fromdir',
                // but only in case no 'mirror' is given.
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
            else if (isMirrorable(c, repositories) != null) {
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
     * For a given channel, check if it is mirrorable and return the ID of the first pair of
     * mirror credentials with access to the given channel.
     * @param channel Channel
     * @param repos list of repos from SCC to match against
     * @return mirror credentials ID or null if the channel is not mirrorable
     */
    public Integer isMirrorable(MgrSyncChannel channel, Collection<SCCRepository> repos) {
        // No source URL means it's mirrorable (return 0 in this case)
        String sourceUrl = channel.getSourceUrl();
        if (StringUtils.isBlank(sourceUrl)) {
            return 0;
        }

        // Check OES availability by sending an HTTP HEAD request
        if (channel.getFamily().equals(OES_CHANNEL_FAMILY)) {
            return verifyOESRepo();
        }

        // Remove trailing slashes before matching URLs
        sourceUrl = removeTrailingSlashes(sourceUrl);

        // Match the channel source URL against URLs we got from SCC
        for (SCCRepository repo : repos) {
            if (sourceUrl.equals(removeTrailingSlashes(repo.getUrl()))) {
                return repo.getCredentialsId();
            }
        }
        return null;
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
            throw new ContentSyncException("Channel is unknown: " + label);
        }

        // Check if channel is mirrorable
        Integer mirrcredsID = isMirrorable(channel, repositories);
        if (mirrcredsID == null) {
            throw new ContentSyncException("Channel is not mirrorable: " + label);
        }

        // Lookup all related products
        List<SUSEProduct> products = new ArrayList<SUSEProduct>();
        for (MgrSyncProduct p : channel.getProducts()) {
            SUSEProduct product = SUSEProductFactory.lookupByProductId(p.getId());
            if (product == null) {
                throw new ContentSyncException("Related product (" + p.getId() +
                        ") could not be found, please refresh!");
            }
            products.add(product);
        }

        // Create the channel
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
        dbChannel.setProduct(MgrSyncUtils.findOrCreateChannelProduct(channel));
        dbChannel.setProductName(MgrSyncUtils.findOrCreateProductName(channel));
        dbChannel.setSummary(channel.getSummary());
        dbChannel.setUpdateTag(channel.getUpdateTag());

        // Create or link the content source
        String url = channel.getSourceUrl();
        if (!StringUtils.isBlank(url)) {
            url = setupSourceURL(url, mirrcredsID);
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

            // Create the product/channel relations
            for (SUSEProduct product : products) {
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
        return StringUtils.join(missingAttributes, ", ");
    }

    /**
     * Check if OES repos are available by sending a HEAD request to one of them. Once
     * we have access with at least one of the available credentials, it means that the
     * customer has bought the product.
     *
     * @return mirror credentials ID or null if OES channels are not mirrorable
     */
    private Integer verifyOESRepo() {
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
                    return creds.getId().intValue();
                }
            } catch (ContentSyncException e) {
                log.error(e.getMessage());
            }
        }
        return null;
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

    /**
     * Setup the source URL of a repository correctly before saving it, particularly
     * add the mirror credentials query string to the end of the URL.
     *
     * TODO: Check if alternative mirror URL is set and consider it here!
     *
     * @param url the original source URL
     * @return the URL with query string including mirror credentials
     */
    private String setupSourceURL(String url, int credsId) {
        if (StringUtils.isBlank(url)) {
            return url;
        }
        String ret = url;
        try {
            URI uri = new URI(url);
            // Official repos need the mirror credentials query string
            if (uri.getHost().equals(OFFICIAL_REPO_HOST)) {
                String separator = uri.getQuery() == null ? "?" : "&";
                ret = url + separator + MIRRCRED_QUERY;
                if (credsId > 0) {
                    ret += "_" + credsId;
                }
            }
        } catch (URISyntaxException e) {
            log.warn(e.getMessage());
        }
        return ret;
    }
}
