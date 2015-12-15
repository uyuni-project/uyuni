/**
 * Copyright (c) 2014 SUSE LLC
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

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.channel.ContentSource;
import com.redhat.rhn.domain.channel.DistChannelMap;
import com.redhat.rhn.domain.channel.PrivateChannelFamily;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.iss.IssFactory;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductChannel;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.product.SUSEUpgradePath;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCRepository;

import com.suse.mgrsync.MgrSyncStatus;
import com.suse.mgrsync.XMLChannel;
import com.suse.mgrsync.XMLChannelFamilies;
import com.suse.mgrsync.XMLChannelFamily;
import com.suse.mgrsync.XMLChannels;
import com.suse.mgrsync.XMLDistribution;
import com.suse.mgrsync.XMLProduct;
import com.suse.mgrsync.XMLUpgradePath;
import com.suse.mgrsync.XMLUpgradePaths;
import com.suse.scc.client.SCCClient;
import com.suse.scc.client.SCCClientException;
import com.suse.scc.client.SCCClientFactory;
import com.suse.scc.model.SCCProduct;
import com.suse.scc.model.SCCSubscription;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.simpleframework.xml.core.Persister;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

/**
 * Content synchronization logic.
 */
public class ContentSyncManager {

    // Logger instance
    private static Logger log = Logger.getLogger(ContentSyncManager.class);

    // Base channels have "BASE" as their parent in channels.xml
    public static final String BASE_CHANNEL = "BASE";

    /**
     * OES channel family name, this is used to distinguish supported non-SUSE
     * repos that are served directly from NCC instead of SCC.
     */
    public static final String OES_CHANNEL_FAMILY = "OES2";
    private static final String OES_URL = "https://nu.novell.com/repo/$RCE/" +
            "OES11-SP2-Pool/sle-11-x86_64/repodata/repomd.xml";

    // Source URL handling
    private static final String OFFICIAL_NOVELL_UPDATE_HOST = "nu.novell.com";
    private static final String MIRRCRED_QUERY = "credentials=mirrcred";

    // Static XML files we parse
    private static File channelsXML = new File(
            "/usr/share/susemanager/scc/channels.xml");
    private static File channelFamiliesXML = new File(
            "/usr/share/susemanager/scc/channel_families.xml");
    private static File upgradePathsXML = new File(
            "/usr/share/susemanager/scc/upgrade_paths.xml");

    // File to parse this system's UUID from
    private static final File UUID_FILE =
            new File("/etc/zypp/credentials.d/SCCcredentials");
    private static String uuid;

    // Cached OES SCCRepository as returned by isMirrorable() in order
    // to avoid repeated HEAD requests
    private SCCRepository cachedOESRepo = null;

    // Mirror URL read from rhn.conf
    public static final String MIRROR_CFG_KEY = "server.susemanager.mirror";

    // SCC JSON files location in rhn.conf
    public static final String RESOURCE_PATH = "server.susemanager.fromdir";

    /**
     * Default constructor.
     */
    public ContentSyncManager() {
    }

    /**
     * Set the channels.xml {@link File} to read from.
     * @param file the channels.xml file
     */
    public static void setChannelsXML(File file) {
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
    public List<XMLChannel> readChannels() throws ContentSyncException {
        try {
            Persister persister = new Persister();
            List<XMLChannel> channels = persister.read(
                    XMLChannels.class, channelsXML).getChannels();
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
    public List<XMLChannelFamily> readChannelFamilies() throws ContentSyncException {
        try {
            Persister persister = new Persister();
            List<XMLChannelFamily> channelFamilies = persister.read(
                    XMLChannelFamilies.class, channelFamiliesXML).getFamilies();
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
    public List<XMLUpgradePath> readUpgradePaths() throws ContentSyncException {
        try {
            Persister persister = new Persister();
            List<XMLUpgradePath> upgradePaths = persister.read(
                    XMLUpgradePaths.class, upgradePathsXML).getPaths();
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
     * There can be no network credentials, but still can read the local files
     * As well as we do need to read the file only once.
     * If /etc/rhn/rhn.conf contains local path URL, then the SCCClient will read
     * from the local file instead of the network.
     * @return List of {@link Credentials}
     */
    private List<Credentials> filterCredentials() {
        // if repos are read with "fromdir", no credentials are used. We signal this
        // with one null Credentials object
        if (Config.get().getString(ContentSyncManager.RESOURCE_PATH) != null) {
            return new ArrayList<Credentials>() { { add(null); } };
        }

        return CredentialsFactory.lookupSCCCredentials();
    }

    /**
     * Returns all products available to all configured credentials.
     * @return list of all available products
     * @throws ContentSyncException in case of an error
     */
    public Collection<SCCProduct> getProducts() throws ContentSyncException {
        Set<SCCProduct> productList = new HashSet<SCCProduct>();
        List<Credentials> credentials = filterCredentials();
        Iterator<Credentials> i = credentials.iterator();

        // stop as soon as a credential pair works
        while (i.hasNext() && productList.size() == 0) {
            Credentials c = i.next();
            try {
                SCCClient scc = getSCCClient(c);
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
                throw new ContentSyncException(e);
            }
            catch (URISyntaxException e) {
                throw new ContentSyncException(e);
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
            // make naming consistent: strip arch or VMWARE suffix
            String friendlyName = product.getFriendlyName();
            List<PackageArch> archs =
                    HibernateFactory.getSession().createCriteria(PackageArch.class).list();
            for (PackageArch arch : archs) {
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
    public Collection<MgrSyncProductDto> listProducts(List<XMLChannel> availableChannels)
        throws ContentSyncException {
        // get all products in the DB
        Collection<XMLProduct> dbProducts = new HashSet<XMLProduct>();
        for (SUSEProduct product : SUSEProductFactory.findAllSUSEProducts()) {
            dbProducts.add(new XMLProduct(product.getFriendlyName(), product
                    .getProductId(), product.getVersion()));
        }

        // get all the channels we have an entitlement for
        Map<XMLProduct, Set<XMLChannel>> productToChannelMap =
                getProductToChannelMap(availableChannels);

        // get all the products we have an entitlement for based on the assumption:
        // at least one channel is entitled -> corresponding product is entitled
        Collection<XMLProduct> availableProducts = productToChannelMap.keySet();

        // filter result with only available products
        dbProducts.retainAll(availableProducts);

        // convert to Collection<MgrSyncProductDto> and return
        return toDtoList(dbProducts, productToChannelMap);
    }

    /**
     * Converts a list of {@link XMLProduct} to a collection of
     * {@link MgrSyncProductDto} objects.
     *
     * @param products the products
     * @param productToChannelMap the product to channel map
     * @return the sorted set
     */
    private Collection<MgrSyncProductDto> toDtoList(
            Collection<XMLProduct> products,
            Map<XMLProduct, Set<XMLChannel>> productToChannelMap) {

        // get a map from channel labels to channels
        Map<String, XMLChannel> labelsTochannels =
                new HashMap<String, XMLChannel>();
        for (Set<XMLChannel> channels : productToChannelMap.values()) {
            for (XMLChannel channel : channels) {
                labelsTochannels.put(channel.getLabel(), channel);
            }
        }

        // get a map from every channel to its base channel
        Map<XMLChannel, XMLChannel> baseChannels =
                new HashMap<XMLChannel, XMLChannel>();
        for (XMLChannel channel : labelsTochannels.values()) {
            XMLChannel parent = channel;
            while (!parent.getParent().equals("BASE")) {
                parent = labelsTochannels.get(parent.getParent());
            }
            baseChannels.put(channel, parent);
        }

        // convert every XMLProduct to dto objects (one per base channel)
        SortedSet<MgrSyncProductDto> all = new TreeSet<MgrSyncProductDto>();
        for (XMLProduct product : products) {
            Set<XMLChannel> channels = productToChannelMap.get(product);
            Map<XMLChannel, MgrSyncProductDto> baseMap =
                    new HashMap<XMLChannel, MgrSyncProductDto>();
            for (XMLChannel channel : channels) {
                XMLChannel base = baseChannels.get(channel);

                MgrSyncProductDto productDto = baseMap.get(base);
                // if this is a new product
                if (productDto == null) {
                    SUSEProduct dbProduct =
                            SUSEProductFactory.lookupByProductId(product.getId());
                    PackageArch arch = dbProduct.getArch();
                    // and if the base channel arch matches the product arch
                    if (arch == null || arch.getName().equals(base.getArch())) {
                        // add it to the product map
                        productDto =
                                new MgrSyncProductDto(product.getName(), product.getId(),
                                        product.getVersion(), base);
                        productDto.addChannel(channel);
                        baseMap.put(base, productDto);
                    }
                }
                else {
                    productDto.addChannel(channel);
                }
            }
            all.addAll(baseMap.values());
        }

        // divide base from extension products
        Collection<MgrSyncProductDto> bases = new LinkedList<MgrSyncProductDto>();
        Collection<MgrSyncProductDto> extensions = new LinkedList<MgrSyncProductDto>();
        for (MgrSyncProductDto product : all) {
            boolean isBase = false;
            for (XMLChannel channel : product.getChannels()) {
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
        for (MgrSyncProductDto base : bases) {
            for (MgrSyncProductDto extension : extensions) {
                for (XMLChannel baseChannel : base.getChannels()) {
                    for (XMLChannel extensionChannel : extension.getChannels()) {
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
    private void addDirtyFixes(Collection<MgrSyncProductDto> all,
            Collection<MgrSyncProductDto> bases) {

        // remove SP1 extensions from SP2 base products and vice versa
        for (MgrSyncProductDto product : bases) {
            String productVersion = product.getVersion();
            if (productVersion.matches("^11.[12]")) {
                Collection<MgrSyncProductDto> wrongSP = new LinkedList<MgrSyncProductDto>();
                for (MgrSyncProductDto extension : product.getExtensions()) {
                    String extensionVersion = extension.getVersion();
                    if (extension.getFriendlyName().startsWith("SUSE") &&
                        extensionVersion.matches("^11.[12]") &&
                        !extensionVersion.equals(productVersion)) {
                        wrongSP.add(extension);
                    }
                }
                product.getExtensions().removeAll(wrongSP);
            }
        }

        // remove SP2 only extensions from SP1 base products
        for (MgrSyncProductDto product : bases) {
            if (product.getVersion().equals("11.1")) {
                Collection<MgrSyncProductDto> sp2ProductsInSp1 =
                        new LinkedList<MgrSyncProductDto>();
                for (MgrSyncProductDto extension : product.getExtensions()) {
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
        for (MgrSyncProductDto product : bases) {
            if (product.getVersion().equals("11.2")) {
                Collection<MgrSyncProductDto> sp1ProductsInSp2 =
                        new LinkedList<MgrSyncProductDto>();
                for (MgrSyncProductDto extension : product.getExtensions()) {
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
    private Map<XMLProduct, Set<XMLChannel>> getProductToChannelMap(
            Collection<XMLChannel> channels) {
        Map<XMLProduct, Set<XMLChannel>> result =
                new TreeMap<XMLProduct, Set<XMLChannel>>();

        for (final XMLChannel channel : channels) {
            for (XMLProduct product : channel.getProducts()) {
                if (result.containsKey(product)) {
                    result.get(product).add(channel);
                }
                else {
                    result.put(product,
                            new HashSet<XMLChannel>() { { add(channel); } });
                }
            }
        }
        return result;
    }

    /**
     * Refresh the repositories cache by reading repos from SCC for all available mirror
     * credentials, consolidating and inserting into the database.
     *
     * @throws ContentSyncException in case of an error
     */
    public void refreshRepositoriesCache() throws ContentSyncException {
        Set<SCCRepository> reposList = new HashSet<SCCRepository>();
        List<Credentials> credentials = filterCredentials();

        // Query repos for all mirror credentials and consolidate
        for (Credentials c : credentials) {
            try {
                log.debug("Getting repos for: " + c);
                SCCClient scc = getSCCClient(c);
                List<SCCRepository> repos = scc.listRepositories();

                // Add mirror credentials to all repos
                for (SCCRepository r : repos) {
                    r.setCredentials(c);
                }
                if (log.isDebugEnabled()) {
                    log.debug("Found " + repos.size() +
                            " repos with credentials: " + c);
                }
                reposList.addAll(repos);
            }
            catch (SCCClientException e) {
                throw new ContentSyncException(e);
            }
            catch (URISyntaxException e) {
                throw new ContentSyncException(e);
            }
        }

        // Update the repositories cache
        if (log.isDebugEnabled()) {
            log.debug("Populating cache with " + reposList.size() + " repositories.");
        }
        refreshRepositoriesCache(reposList);
    }

    /**
     * Deletes all repositories stored in the database and inserts the specified ones.
     * @param repositories the new repositories
     */
    public void refreshRepositoriesCache(Collection<SCCRepository> repositories) {
        SCCCachingFactory.clearRepositories();
        for (SCCRepository repo : repositories) {
            SCCCachingFactory.saveRepository(repo);
        }
    }

    /**
     * Refresh the subscription cache by reading subscriptions from SCC for all available mirror
     * credentials, consolidating and inserting into the database.
     *
     * @throws ContentSyncException in case of an error
     */
    public void refreshSubscriptionCache(List<SCCSubscription> subscriptions,
            Credentials c) {

        SCCCachingFactory.clearSubscriptions(c);
        for (SCCSubscription s : subscriptions) {
            SCCCachingFactory.saveJsonSubscription(s, c);
        }
        if (log.isDebugEnabled()) {
            log.debug("Found " + subscriptions.size() +
                    " subscriptions with credentials: " + c);
        }
    }

    /**
     * Get subscriptions from SCC for a single pair of mirror credentials.
     * @param credentials username/password pair
     * @return list of subscriptions as received from SCC.
     * @throws SCCClientException in case of an error
     */
    public List<SCCSubscription> getSubscriptions(Credentials credentials)
            throws SCCClientException {
        try {
            SCCClient scc = this.getSCCClient(credentials);
            List<SCCSubscription> subscriptions = scc.listSubscriptions();
            refreshSubscriptionCache(subscriptions, credentials);
            return subscriptions;
        }
        catch (URISyntaxException e) {
            log.error("Invalid URL:" + e.getMessage());
            return new ArrayList<SCCSubscription>();
        }
    }

    /**
     * Returns all subscriptions available to all configured credentials.
     * @return list of all available subscriptions
     * @throws ContentSyncException in case of an error
     */
    public Collection<SCCSubscription> getSubscriptions() throws ContentSyncException {
        Set<SCCSubscription> subscriptions = new HashSet<SCCSubscription>();
        List<Credentials> credentials = filterCredentials();
        // Query subscriptions for all mirror credentials
        for (Credentials c : credentials) {
            try {
                subscriptions.addAll(getSubscriptions(c));
            }
            catch (SCCClientException e) {
                throw new ContentSyncException(e);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Found " + subscriptions.size() + " available subscriptions.");
        }
        return subscriptions;
    }

    /**
     * Update the repositories cache and the channel information in the database.
     * @param mirrorUrl optional mirror URL that can be null
     * @throws com.redhat.rhn.manager.content.ContentSyncException if channels
     * can't be updated
     */
    public void updateChannels(String mirrorUrl) throws ContentSyncException {
        refreshRepositoriesCache();
        updateChannelsInternal(mirrorUrl);
    }

    /**
     * Update channel information in the database. This is only public for testing purposes
     * and should apart from that only be called internally.
     * @param mirrorUrl optional mirror URL that can be null
     * @throws com.redhat.rhn.manager.content.ContentSyncException if channels
     * can't be updated
     */
    public void updateChannelsInternal(String mirrorUrl) throws ContentSyncException {
        if (StringUtils.isBlank(mirrorUrl)) {
            mirrorUrl = Config.get().getString(ContentSyncManager.MIRROR_CFG_KEY);
        }

        // If this is an ISS slave then do nothing
        if (IssFactory.getCurrentMaster() != null) {
            return;
        }

        // Read contents of channels.xml into a map
        Map<String, XMLChannel> channelsXMLData = new HashMap<String, XMLChannel>();
        for (XMLChannel c : readChannels()) {
            channelsXMLData.put(c.getLabel(), c);
        }

        // Get all vendor channels from the database
        List<Channel> channelsDB = ChannelFactory.listVendorChannels();
        for (Channel c : channelsDB) {
            if (channelsXMLData.containsKey(c.getLabel())) {
                XMLChannel channel = channelsXMLData.get(c.getLabel());
                if (!channel.getDescription().equals(c.getDescription()) ||
                        !channel.getName().equals(c.getName()) ||
                        !channel.getSummary().equals(c.getSummary()) ||
                        !channel.getUpdateTag().equals(c.getUpdateTag())) {
                    // There is a difference, copy channel attributes and save
                    c.setDescription(channel.getDescription());
                    c.setName(channel.getName());
                    c.setSummary(channel.getSummary());
                    c.setUpdateTag(channel.getUpdateTag());
                    c.setChannelFamily(ChannelFamilyFactory.lookupByLabel(
                            channel.getFamily(), null));
                    ChannelFactory.save(c);
                }
            }
            else {
                // Channel is no longer mirrorable, we can return those and warn about it
            }
        }

        // Update content source URLs
        List<SCCRepository> repos = SCCCachingFactory.lookupRepositories();
        List<ContentSource> contentSources = ChannelFactory.listVendorContentSources();
        for (ContentSource cs : contentSources) {
            if (channelsXMLData.containsKey(cs.getLabel())) {
                XMLChannel channel = channelsXMLData.get(cs.getLabel());
                SCCRepository repo = isMirrorable(channel, repos);
                if (repo != null) {
                    String sourceURL = setupSourceURL(repo, mirrorUrl);
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
     * @throws ContentSyncException in case of an error
     */
    public void updateChannelFamilies(Collection<XMLChannelFamily> channelFamilies)
            throws ContentSyncException {
        for (XMLChannelFamily channelFamily : channelFamilies) {
            ChannelFamily family = createOrUpdateChannelFamily(
                    channelFamily.getLabel(), channelFamily.getName());
            // Create rhnPrivateChannelFamily entry if it doesn't exist
            if (family != null && family.getPrivateChannelFamilies().isEmpty()) {
                PrivateChannelFamily pcf = new PrivateChannelFamily();
                pcf.setCreated(new Date());
                // Set the default organization (id = 1)
                pcf.setOrg(OrgFactory.getSatelliteOrg());

                // save the private channel family
                pcf.setChannelFamily(family);
                ChannelFamilyFactory.save(pcf);

                // Update the channel family accordingly
                family.addPrivateChannelFamily(pcf);
                ChannelFamilyFactory.save(family);
            }
        }
    }

    /**
     * Sync subscriptions from SCC to the database after consolidation.
     * @param subscriptions list of subscriptions as we get them from SCC
     * @throws ContentSyncException in case of an error
     */
    public void updateSubscriptions(Collection<SCCSubscription> subscriptions)
            throws ContentSyncException {
    }

    /**
     * Creates or updates entries in the SUSEProducts database table with a given list of
     * {@link SCCProduct} objects.
     *
     * @param products list of products
     * @throws ContentSyncException in case of an error
     */
    public void updateSUSEProducts(Collection<SCCProduct> products)
            throws ContentSyncException {
        Collection<SUSEProduct> processed = new LinkedList<SUSEProduct>();
        for (SCCProduct p : products) {
            // Create the channel family if it is not available
            String productClass = p.getProductClass();
            if (!StringUtils.isBlank(productClass)) {
                createOrUpdateChannelFamily(productClass, null);
            }

            // Update this product in the database if it is there
            SUSEProduct product = SUSEProductFactory.findSUSEProduct(p.getIdentifier(),
                    p.getVersion(), p.getReleaseType(), p.getArch(), false);
            if (product != null) {
                // it is not guaranteed for this ID to be stable in time, as it
                // depends on IBS
                product.setProductId(p.getId());
                product.setFriendlyName(p.getFriendlyName());
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
                    log.error("Unknown architecture '" + p.getArch() +
                            "'. This may be caused by a missing database migration");
                    continue;
                }

                product.setArch(pArch);
            }
            SUSEProductFactory.save(product);
            processed.add(product);
        }

        SUSEProductFactory.removeAllExcept(processed);
        updateUpgradePaths(products);
    }

    /**
     * Get a list of all actually available channels based on available channel families
     * as well as some other criteria.
     * @param allChannels List of {@link XMLChannel}
     * @return list of available channels
     * @throws ContentSyncException in case of an error
     */
    public List<XMLChannel> getAvailableChannels(List<XMLChannel> allChannels)
            throws ContentSyncException {
        // Get all channels from channels.xml and filter
        List<XMLChannel> availableChannels = new ArrayList<XMLChannel>();

        List<SCCRepository> repositories = SCCCachingFactory.lookupRepositories();

        // Filter in all channels which we can mirror
        for (XMLChannel c : allChannels) {
            if (isMirrorable(c, repositories) != null) {
                availableChannels.add(c);
            }
        }

        // Reassign lists to variables to continue the filtering
        allChannels = availableChannels;
        availableChannels = new ArrayList<XMLChannel>();

        // Remember channel labels in a list for convenient lookup
        List<String> availableChannelLabels = new ArrayList<String>();
        for (XMLChannel c : allChannels) {
            availableChannelLabels.add(c.getLabel());
        }

        // Filter in channels with available parents only (or base channels)
        for (XMLChannel c : allChannels) {
            String parent = c.getParent();
            if (parent.equals(BASE_CHANNEL) || availableChannelLabels.contains(parent)) {
                availableChannels.add(c);

                // Update tag can be empty string which is not allowed in the DB
                if (StringUtils.isBlank(c.getUpdateTag())) {
                    c.setUpdateTag(null);
                }
            }
        }

        return availableChannels;
    }

    /**
     * Synchronization of the {@link SUSEProductChannel} relationships.
     * @param availableChannels List of {@link XMLChannel}
     * @throws ContentSyncException in case of an error
     */
    public void updateSUSEProductChannels(List<XMLChannel> availableChannels)
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
        for (XMLChannel availableChannel : availableChannels) {
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
            for (XMLProduct p : availableChannel.getProducts()) {
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
     * Update contents of the suseUpgradePaths table with values from upgrade_paths.xml
     * and predecessor_ids from SCC
     *
     * @param products Collection of SCC Products
     * @throws ContentSyncException in case of an error
     */
    public void updateUpgradePaths(Collection<SCCProduct> products)
            throws ContentSyncException {
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
        for (XMLUpgradePath path : readUpgradePaths()) {
            SUSEProduct fromProduct = SUSEProductFactory.lookupByProductId(
                    path.getFromProductId());
            SUSEProduct toProduct = SUSEProductFactory.lookupByProductId(
                    path.getToProductId());
            updateUpgradePath(fromProduct, path.getFromProductId(),
                    toProduct, path.getToProductId(), pathsToRemove);
        }

        for (SCCProduct p : products) {
            if (p.getPredecessorIds() != null) {
                SUSEProduct toProduct = SUSEProductFactory.lookupByProductId(p.getId());
                for (Integer predecessorId : p.getPredecessorIds()) {
                    SUSEProduct fromProduct =
                            SUSEProductFactory.lookupByProductId(predecessorId);
                    updateUpgradePath(fromProduct, predecessorId, toProduct, p.getId(),
                            pathsToRemove);
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
     * Updates a single upgrade path.
     *
     * @param fromProduct the source product
     * @param fromSCCProductid the souce product's SCC ID
     * @param toProduct the destination product
     * @param toSCCProductid the destination product's SCC ID
     * @param pathsToRemove Map of paths to remove, will be updated by this method
     */
    private void updateUpgradePath(SUSEProduct fromProduct, Integer fromSCCProductid,
            SUSEProduct toProduct, Integer toSCCProductid,
            Map<String, SUSEUpgradePath> pathsToRemove) {
        if (fromProduct != null && toProduct != null) {

            // Dirty Hack: prevent major version update from 11.X to 12.X
            if (fromProduct.getVersion().matches("^11(\\.\\d+)*$") &&
                    toProduct.getVersion().matches("^12(\\.\\d+)*$")) {
                return;
            }

            // Products found, get the existing path object from map by removing
            String identifier = String.format("%s-%s",
                    fromSCCProductid, toSCCProductid);
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

    /**
     * Return the list of available channels with their status.
     *
     * @return list of channels
     * @throws ContentSyncException in case of an error
     */
    public List<XMLChannel> listChannels()
            throws ContentSyncException {
        // This list will be returned
        List<XMLChannel> channels = new ArrayList<XMLChannel>();
        List<String> installedChannelLabels = getInstalledChannelLabels();

        // Reset the cached OES credentials (OES will be queried only once)
        cachedOESRepo = null;

        // Get the cached list of repositories
        List<SCCRepository> repositories = SCCCachingFactory.lookupRepositories();

        // Determine the channel status
        for (XMLChannel c : getAvailableChannels(readChannels())) {
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
     * For a given channel, check if it is mirrorable and return the repository object
     * with the ID of the first pair of mirror credentials with access to the given channel.
     * @param channel Channel
     * @param repos list of repos from SCC to match against
     * @return repository object or null if the channel is not mirrorable
     * @throws ContentSyncException in case of an IO error while verifying OES
     */
    public SCCRepository isMirrorable(XMLChannel channel,
            Collection<SCCRepository> repos) throws ContentSyncException {
        // No source URL means it's mirrorable (return 0 in this case)
        String sourceUrl = channel.getSourceUrl();
        if (StringUtils.isBlank(sourceUrl)) {
            return new SCCRepository();
        }

        // Check OES availability by sending an HTTP HEAD request
        if (channel.getFamily().equals(OES_CHANNEL_FAMILY)) {
            if (cachedOESRepo == null) {
                Credentials oesCreds = verifyOESRepo();
                cachedOESRepo = new SCCRepository();
                cachedOESRepo.setUrl(channel.getSourceUrl());
                cachedOESRepo.setCredentials(oesCreds);
                return cachedOESRepo;
            }
            else if (cachedOESRepo.getCredentials() != null) {
                cachedOESRepo.setUrl(channel.getSourceUrl());
                if (log.isDebugEnabled()) {
                    log.debug("Return cached OES availablity");
                }
                return cachedOESRepo;
            }
            return null;
        }

        return findMatchingRepo(repos, sourceUrl);
    }

    /**
     * Finds the repo corresponding to an URL.
     *
     * @param repos the repos
     * @param url the source url
     * @return the repository
     */
    public SCCRepository findMatchingRepo(Collection<SCCRepository> repos, String url) {
        String noTrailingSlashUrl = url.replaceFirst("/+$", "");
        Pattern p = Pattern.compile(Pattern.quote(noTrailingSlashUrl) + "/*(?:\\?.*)?$");

        for (SCCRepository repo : repos) {
            if (p.matcher(repo.getUrl()).matches()) {
                return repo;
            }
        }
        return null;
    }

    /**
     * Add a new channel to the database.
     * @param label the label of the channel to be added.
     * @param mirrorUrl repo mirror passed by cli
     * @throws ContentSyncException in case of problems
     */
    public void addChannel(String label, String mirrorUrl)
            throws ContentSyncException {
        // Return immediately if the channel is already there
        if (ChannelFactory.doesChannelLabelExist(label)) {
            if (log.isDebugEnabled()) {
                log.debug("Channel exists (" + label + "), returning...");
            }
            return;
        }

        // Lookup the channel in available channels
        XMLChannel channel = null;
        List<XMLChannel> channels = getAvailableChannels(readChannels());
        for (XMLChannel c : channels) {
            if (c.getLabel().equals(label)) {
                channel = c;
                break;
            }
        }
        if (channel == null) {
            throw new ContentSyncException("Channel is unknown: " + label);
        }

        // Check if channel is mirrorable
        SCCRepository repo = isMirrorable(channel, SCCCachingFactory.lookupRepositories());
        if (repo == null) {
            throw new ContentSyncException("Channel is not mirrorable: " + label);
        }

        // Lookup all related products
        List<SUSEProduct> products = new ArrayList<SUSEProduct>();
        for (XMLProduct p : channel.getProducts()) {
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
        // Checksum type is only a dummy here. spacewalk-repo-sync will update it
        // and set it to the type used in the (last) repo to hash the primary file
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
        String url = repo.getUrl();
        if (!StringUtils.isBlank(url)) {
            url = setupSourceURL(repo, mirrorUrl);
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
        XMLDistribution dist = channel.getDistribution();
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
            family.setName(StringUtils.isBlank(name) ? label : name);
            family.setProductUrl("some url");
            ChannelFamilyFactory.save(family);
        }
        else if (family != null && !StringUtils.isBlank(name)) {
            family.setName(name);
            ChannelFamilyFactory.save(family);
        }
        return family;
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
     * @return mirror credentials or null if OES channels are not mirrorable
     * @throws ContentSyncException in case of an IO error while sending HEAD request
     */
    private Credentials verifyOESRepo() throws ContentSyncException {
        // Look for local file in case of from-dir
        if (Config.get().getString(RESOURCE_PATH) != null) {
            try {
                if (new File(urlToFSPath(OES_URL)).canRead()) {
                    return new Credentials();
                }
            }
            catch (MalformedURLException e) {
                log.error(e.getMessage());
            }
            catch (ContentSyncException e) {
                log.error(e.getMessage());
            }
            return null;
        }

        // Query OES repo for all mirror credentials until success
        List<Credentials> credentials = CredentialsFactory.lookupSCCCredentials();
        for (Credentials creds : credentials) {
            int responseCode;
            try {
                responseCode =
                        MgrSyncUtils
                                .sendHeadRequest(OES_URL, creds.getUsername(),
                                        creds.getPassword())
                                .getStatusLine().getStatusCode();
                if (log.isDebugEnabled()) {
                    log.debug("OES repo response code for " +
                            creds.getUsername() + ": " + responseCode);
                }
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    return creds;
                }
            }
            catch (NoRouteToHostException e) {
                String proxy = ConfigDefaults.get().getProxyHost();
                throw new ContentSyncException("No route to the OES repository" +
                        (proxy != null ? " or the Proxy: " + proxy : ""));
            }
            catch (IOException e) {
                throw new ContentSyncException(e);
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
                reader = new BufferedReader(new FileReader(UUID_FILE));
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
     * Convert network URL to file system URL.
     * @param urlString
     * @return
     * @throws MalformedURLException
     * @throws ContentSyncException
     */
    private URI urlToFSPath(String urlString)
            throws MalformedURLException, ContentSyncException {
        URL url = new URL(urlString);
        String sccDataPath = Config.get().getString(ContentSyncManager.RESOURCE_PATH, null);
        File dataPath = new File(sccDataPath);

        if (!dataPath.canRead()) {
            throw new ContentSyncException(
                    String.format("Path \"%s\" does not exists or cannot be read",
                                  sccDataPath));
        }

        return new File(dataPath.getAbsolutePath() + url.getPath()).toURI();
    }

    /**
     * Setup the source URL of a repository correctly before saving it, particularly
     * add the mirror credentials query string to the end of the URL.
     *
     * @param repo {@link SCCRepository}
     * @param mirrorUrl optional mirror URL that can be null
     * @return the URL with query string including mirror credentials or null
     */
    public String setupSourceURL(SCCRepository repo, String mirrorUrl) {
        String url = repo.getUrl();

        if (Config.get().getString(ContentSyncManager.RESOURCE_PATH, null) != null) {
            try {
                return this.urlToFSPath(url).toASCIIString();
            }
            catch (MalformedURLException e) {
                log.error(e.getMessage());
                return null;
            }
            catch (ContentSyncException e) {
                log.error(e.getMessage());
                return null;
            }
        }

        if (StringUtils.isBlank(url)) {
            return null;
        }

        // Setup the source URI
        URI sourceUri = null;
        try {
            sourceUri = new URI(url);
        }
        catch (URISyntaxException e) {
            log.warn(e.getMessage());
            return null;
        }

        // Try to read mirror from config if not given
        if (StringUtils.isBlank(mirrorUrl)) {
            mirrorUrl = Config.get().getString(MIRROR_CFG_KEY);
        }

        // See if we have a mirror URL and try to set it up
        if (mirrorUrl != null) {
            try {
                URI mirrorUri = new URI(mirrorUrl);
                String username = null;
                String password = null;
                if (mirrorUri.getUserInfo() != null) {
                    String userInfo = mirrorUri.getUserInfo();
                    username = userInfo.substring(0, userInfo.indexOf(':'));
                    password = userInfo.substring(userInfo.indexOf(':') + 1);
                }

                // Setup the path
                String mirrorPath = StringUtils.defaultString(mirrorUri.getRawPath());
                String combinedPath = new File(StringUtils.stripToEmpty(mirrorPath),
                        sourceUri.getRawPath()).getPath();

                // SMT doesn't do dir listings, so we try to get the metadata
                String testUrlPath =
                        new File(combinedPath, "/repodata/repomd.xml").getPath();

                // Build full URL to test
                URI testUri = new URI(mirrorUri.getScheme(), null, mirrorUri.getHost(),
                        mirrorUri.getPort(), testUrlPath, mirrorUri.getQuery(), null);

                // Verify the mirrored repo by sending a HEAD request
                int mirrorStatus = MgrSyncUtils.sendHeadRequest(testUri.toString(),
                        username, password).getStatusLine().getStatusCode();
                if (mirrorStatus == HttpURLConnection.HTTP_OK) {
                    // Build URL combining the mirror and N/SCC parts
                    String[] mirrorParams = StringUtils.split(mirrorUri.getQuery(), '&');
                    String[] sourceParams = StringUtils.split(sourceUri.getQuery(), '&');
                    String combinedQuery = StringUtils.join(
                            ArrayUtils.addAll(mirrorParams, sourceParams), '&');
                    URI completeMirrorUri = new URI(mirrorUri.getScheme(),
                            mirrorUri.getUserInfo(), mirrorUri.getHost(),
                            mirrorUri.getPort(), combinedPath, combinedQuery, null);
                    return completeMirrorUri.toString();
                }
                else {
                    log.warn("Mirror status " + mirrorStatus + " for: " + testUri);
                }
            }
            catch (IOException e) {
                log.warn(e.getMessage());
            }
            catch (URISyntaxException e) {
                log.warn(e.getMessage());
            }
            catch (NullPointerException e) {
                log.debug(e.getMessage());
            }
        }

        // If we are still here there was no mirror given or mirror doesn't have repo.
        // For the official novell update host we do basic auth with mirror credentials
        if (sourceUri.getHost().equals(OFFICIAL_NOVELL_UPDATE_HOST)) {
            String separator = sourceUri.getQuery() == null ? "?" : "&";
            StringBuilder credUrl =
                    new StringBuilder(url).append(separator).append(MIRRCRED_QUERY);
            Long credsId = repo.getCredentials().getId();
            credUrl.append("_").append(credsId);
            return credUrl.toString();
        }
        else {
            // This is especially the case for updates.suse.com (token auth or no auth)
            return sourceUri.toString();
        }
    }

    /**
     * Get an instance of {@link SCCWebClient} and configure it to use localpath, if
     * such is setup in /etc/rhn/rhn.conf
     *
     * @param credentials username/password pair
     * @throws URISyntaxException if the URL in configuration file is malformed
     * @throws SCCClientException
     * @return {@link SCCWebClient}
     */
    private SCCClient getSCCClient(Credentials credentials)
            throws URISyntaxException, SCCClientException {
        // check that URL is valid
        URI url = new URI(Config.get().getString(ConfigDefaults.SCC_URL));

        String localPath = Config.get().getString(ContentSyncManager.RESOURCE_PATH, null);
        String localAbsolutePath = null;
        if (localPath != null) {
            File localFile = new File(localPath);
            localAbsolutePath = localFile.getAbsolutePath();

            if (!localFile.canRead()) {
                throw new SCCClientException(
                        String.format("Unable to access resource at \"%s\" location.",
                                localAbsolutePath));
            }
            else if (!localFile.isDirectory()) {
                throw new SCCClientException(
                        String.format("Path \"%s\" must be a directory.",
                                localAbsolutePath));
            }
        }

        String username = credentials == null ? null : credentials.getUsername();
        String password = credentials == null ? null : credentials.getPassword();

        return SCCClientFactory.getInstance(url, username, password, localAbsolutePath,
                getUUID());
    }

    /**
     * Returns true if the given label is reserved: eg. used by a vendor channel
     *
     * For a channel label to be reserved, {@link ContentSyncManager} needs
     * access to a channels.xml file. If channels.xml is not available, all
     * channel labels will be available.
     *
     * @param label Label
     * @return true if the given label reserved.
     * @throws ContentSyncException in case of error when parsing channels.xml
     */
    public static boolean isChannelLabelReserved(String label) throws ContentSyncException {
        if (!Files.exists(channelsXML.toPath())) {
            return false;
        }
        ContentSyncManager csm = new ContentSyncManager();
        List<XMLChannel> channels = csm.readChannels();
        for (XMLChannel msc : channels) {
            if (msc.getLabel().equals(label)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns true if the given name reserved. eg. used by a vendor channel
     *
     * For a channel name to be reserved, {@link ContentSyncManager} needs
     * access to a channels.xml file. If channels.xml is not available, all
     * channel names will be available.
     *
     * eg: name of vendor channel
     * @param name name
     * @return true if the given name reserved.
     * @throws ContentSyncException in case of error when parsing channels.xml
     */
    public static boolean isChannelNameReserved(String name) throws ContentSyncException {
        if (!Files.exists(channelsXML.toPath())) {
            return false;
        }
        ContentSyncManager csm = new ContentSyncManager();
        List<XMLChannel> channels = csm.readChannels();
        for (XMLChannel msc : channels) {
            if (msc.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }
}
