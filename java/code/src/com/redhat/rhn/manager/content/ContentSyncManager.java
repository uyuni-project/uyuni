/**
 * Copyright (c) 2014--2018 SUSE LLC
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
import com.redhat.rhn.common.util.TimeUtils;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.channel.ContentSource;
import com.redhat.rhn.domain.channel.PublicChannelFamily;
import com.redhat.rhn.domain.common.ManagerInfoFactory;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.product.MgrSyncChannelDto;
import com.redhat.rhn.domain.product.Tuple2;
import com.redhat.rhn.domain.product.Tuple3;
import com.redhat.rhn.domain.product.ReleaseStage;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductChannel;
import com.redhat.rhn.domain.product.SUSEProductExtension;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.product.SUSEProductSCCRepository;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCOrderItem;
import com.redhat.rhn.domain.scc.SCCRepository;
import com.redhat.rhn.domain.scc.SCCRepositoryAuth;
import com.redhat.rhn.domain.scc.SCCRepositoryBasicAuth;
import com.redhat.rhn.domain.scc.SCCRepositoryNoAuth;
import com.redhat.rhn.domain.scc.SCCRepositoryTokenAuth;
import com.redhat.rhn.domain.scc.SCCSubscription;
import com.redhat.rhn.manager.channel.ChannelManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.suse.mgrsync.MgrSyncStatus;
import com.suse.salt.netapi.parser.JsonParser;
import com.suse.scc.client.SCCClient;
import com.suse.scc.client.SCCClientException;
import com.suse.scc.client.SCCClientFactory;
import com.suse.scc.client.SCCClientUtils;
import com.suse.scc.client.SCCWebClient;
import com.suse.scc.model.SCCRepositoryJson;
import com.suse.scc.model.ChannelFamilyJson;
import com.suse.scc.model.SCCOrderItemJson;
import com.suse.scc.model.SCCOrderJson;
import com.suse.scc.model.SCCProductJson;
import com.suse.scc.model.SCCSubscriptionJson;
import com.suse.scc.model.UpgradePathJson;
import com.suse.utils.Opt;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Content synchronization logic.
 */
public class ContentSyncManager {

    // Logger instance
    private static Logger log = Logger.getLogger(ContentSyncManager.class);

    /**
     * OES channel family name, this is used to distinguish supported non-SUSE
     * repos that are served directly from NCC instead of SCC.
     */
    public static final String OES_CHANNEL_FAMILY = "OES2";
    private static final String OES_URL = "https://nu.novell.com/repo/$RCE/" +
            "OES11-SP2-Pool/sle-11-x86_64/";


    // Static JSON files we parse
    private static File upgradePathsJson = new File(
            "/usr/share/susemanager/scc/upgrade_paths.json");
    private static File channelFamiliesJson = new File(
            "/usr/share/susemanager/scc/channel_families.json");
    private static File oesProductsJson = new File(
            "/usr/share/susemanager/scc/additional_products.json");
    private static File rhelRepositoriesJson = new File(
            "/usr/share/susemanager/scc/additional_repositories.json");
    private static File sumaProductTreeJson = new File(
            "/usr/share/susemanager/scc/product_tree.json");

    // File to parse this system's UUID from
    private static final File UUID_FILE =
            new File("/etc/zypp/credentials.d/SCCcredentials");
    private static String uuid;

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
     * Set the upgrade_paths.json {@link File} to read from.
     * @param file the upgrade_paths.json file
     */
    public void setUpgradePathsJson(File file) {
        upgradePathsJson = file;
    }

    /**
     * Set the product_tree.json {@link File} to read from.
     * @param file the product_tree.json file
     */
    public void setSumaProductTreeJson(File file) {
        sumaProductTreeJson = file;
    }

    /**
     * Read the channel_families.json file.
     *
     * @return List of parsed channel families
     */
    public List<ChannelFamilyJson> readChannelFamilies() {
        Gson gson = new GsonBuilder().create();
        List<ChannelFamilyJson> channelFamilies = new ArrayList<>();
        try {
            channelFamilies = gson.fromJson(new BufferedReader(new InputStreamReader(
                    new FileInputStream(channelFamiliesJson))),
                    SCCClientUtils.toListType(ChannelFamilyJson.class));
        }
        catch (IOException e) {
            log.error(e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Read " + channelFamilies.size() + " channel families from " +
                    channelFamiliesJson.getAbsolutePath());
        }
        return channelFamilies;
    }

    /**
     * Read the upgrade_paths.xml file.
     *
     * @return List of upgrade paths
     * @throws ContentSyncException in case of an error
     */
    public List<UpgradePathJson> readUpgradePaths() throws ContentSyncException {
        Gson gson = new GsonBuilder().create();
        List<UpgradePathJson> upPaths = new ArrayList<>();
        try {
            upPaths = gson.fromJson(new BufferedReader(new InputStreamReader(
                    new FileInputStream(upgradePathsJson))),
                    SCCClientUtils.toListType(UpgradePathJson.class));
        }
        catch (Exception e) {
            throw new ContentSyncException(e);
        }
        if (log.isDebugEnabled()) {
            log.debug("Read " + upPaths.size() + " upgrade paths from " +
                    upgradePathsJson.getAbsolutePath());
        }
        return upPaths;
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
    public List<SCCProductJson> getProducts() throws ContentSyncException {
        Set<SCCProductJson> productList = new HashSet<SCCProductJson>();
        List<Credentials> credentials = filterCredentials();
        Iterator<Credentials> i = credentials.iterator();

        // stop as soon as a credential pair works
        while (i.hasNext() && productList.size() == 0) {
            Credentials c = i.next();
            try {
                SCCClient scc = getSCCClient(c);
                List<SCCProductJson> products = scc.listProducts();
                for (SCCProductJson product : products) {
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
            catch (SCCClientException | URISyntaxException e) {
                throw new ContentSyncException(e);
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Found " + productList.size() + " available products.");
        }

        return new ArrayList<>(productList);
    }

    /*
     * Return static list of RHEL repositories
     */
    private static List<SCCRepositoryJson> getRHELRepositories() {
        Gson gson = new GsonBuilder().create();
        List<SCCRepositoryJson> repos = new ArrayList<>();
        try {
            repos = gson.fromJson(new BufferedReader(new InputStreamReader(
                            new FileInputStream(rhelRepositoriesJson))),
                    SCCClientUtils.toListType(SCCRepositoryJson.class));
        }
        catch (IOException e) {
            log.error(e);
        }
        return repos;
    }

    /*
     * Return static list or OES products
     */
    private static List<SCCProductJson> getOESProducts() {
        Gson gson = new GsonBuilder().create();
        List<SCCProductJson> oes = new ArrayList<>();
        try {
            oes = gson.fromJson(new BufferedReader(new InputStreamReader(
                            new FileInputStream(oesProductsJson))),
                    SCCClientUtils.toListType(SCCProductJson.class));
        }
        catch (IOException e) {
            log.error(e);
        }
        return oes;
    }

    /**
     * Returns all available products in user-friendly format.
     * @return list of all available products
     */
    public List<MgrSyncProductDto> listProducts() {
        return HibernateFactory.doWithoutAutoFlushing(() -> listProductsImpl());
    }

    /**
     * Returns all available products in user-friendly format.
     * @return list of all available products
     */
    private List<MgrSyncProductDto> listProductsImpl() {
        List<String> installedChannelLabels = getInstalledChannelLabels();

        List<Tuple2<SUSEProductSCCRepository, MgrSyncStatus>> availableChannels =
                TimeUtils.logTime(log, "getAvailableCHannels", () -> getAvailableChannels()).stream().map(e -> {
                    MgrSyncStatus status = installedChannelLabels.contains(e.getChannelLabel()) ?
                            MgrSyncStatus.INSTALLED : MgrSyncStatus.AVAILABLE;
                    return new Tuple2<>(e, status);
                }).collect(Collectors.toList());

        List<SUSEProduct> allSUSEProducts = SUSEProductFactory.findAllSUSEProducts();

        List<SUSEProduct> roots = allSUSEProducts.stream()
                .filter(p -> p.isBase())
                .collect(Collectors.toList());

        Map<Long, List<Long>> recommendedForBase = SUSEProductFactory.allRecommendedExtensions().stream().collect(
                Collectors.groupingBy(
                        s -> s.getExtensionProduct().getProductId(),
                        Collectors.mapping(s -> s.getRootProduct().getProductId(), Collectors.toList())
                )
        );


        Map<Long, List<Tuple2<SUSEProductSCCRepository, MgrSyncStatus>>> byProductId = availableChannels.stream()
                .collect(Collectors.groupingBy(p -> p.getA().getProduct().getId()));
        Map<Long, List<Tuple2<SUSEProductSCCRepository, MgrSyncStatus>>> byRootId = availableChannels.stream()
                .collect(Collectors.groupingBy(p -> p.getA().getRootProduct().getId()));

        List<MgrSyncProductDto> productDtos = roots.stream()
                .filter(p -> byProductId.containsKey(p.getId()))
                .map(root -> {

            List<Tuple2<SUSEProductSCCRepository, MgrSyncStatus>> exts = byRootId.get(root.getId()).stream()
                    .filter(p -> !p.getA().getProduct().equals(root))
                    .collect(Collectors.toList());

            List<Tuple2<SUSEProductSCCRepository, MgrSyncStatus>> repos = byProductId.get(root.getId());

            Map<Boolean, List<Tuple2<SUSEProductSCCRepository, MgrSyncStatus>>> partitionBaseRepo = repos.stream()
                    .collect(Collectors.partitioningBy(p -> p.getA().getParentChannelLabel() == null));

            List<Tuple2<SUSEProductSCCRepository, MgrSyncStatus>> baseRepos = partitionBaseRepo.get(true).stream()
                    // for RHEL and Vmware which have multiple base channels for a product
                    .sorted((a, b) -> {
                        return a.getA().getChannelLabel().compareTo(b.getA().getChannelLabel());
                    }).collect(Collectors.toList());
            List<Tuple2<SUSEProductSCCRepository, MgrSyncStatus>> childRepos = partitionBaseRepo.get(false);

            Set<MgrSyncChannelDto> allChannels = childRepos.stream().map(c -> new MgrSyncChannelDto(
                    c.getA().getRepository().getName() +
                    (c.getA().getRootProduct().getArch() != null ?
                            " for " + c.getA().getRootProduct().getArch().getLabel() : ""),
                    c.getA().getChannelLabel(),
                    c.getA().getProduct().getFriendlyName(),
                    c.getA().getRepository().getDescription(),
                    c.getA().isMandatory(),
                    Optional.ofNullable(c.getA().getProduct().getArch()),
                    c.getA().getParentChannelLabel(),
                    c.getA().getProduct().getChannelFamily().getLabel(),
                    c.getA().getProduct().getName(),
                    c.getA().getProduct().getVersion(),
                    c.getB(),
                    c.getA().getRepository().isSigned(),
                    c.getA().getRepository().getUrl(),
                    c.getA().getUpdateTag()
            )).collect(Collectors.toSet());

            List<MgrSyncChannelDto> baseChannels = baseRepos.stream().map(baseRepo -> new MgrSyncChannelDto(
                    baseRepo.getA().getRepository().getName() +
                    (baseRepo.getA().getRootProduct().getArch() != null ?
                            " for " + baseRepo.getA().getRootProduct().getArch().getLabel() : ""),
                    baseRepo.getA().getChannelLabel(),
                    baseRepo.getA().getProduct().getFriendlyName(),
                    baseRepo.getA().getRepository().getDescription(),
                    baseRepo.getA().isMandatory(),
                    Optional.ofNullable(baseRepo.getA().getProduct().getArch()),
                    baseRepo.getA().getParentChannelLabel(),
                    baseRepo.getA().getProduct().getChannelFamily().getLabel(),
                    baseRepo.getA().getProduct().getName(),
                    baseRepo.getA().getProduct().getVersion(),
                    baseRepo.getB(),
                    baseRepo.getA().getRepository().isSigned(),
                    baseRepo.getA().getRepository().getUrl(),
                    baseRepo.getA().getUpdateTag()
            )).collect(Collectors.toList());
            allChannels.addAll(baseChannels);
            MgrSyncChannelDto baseChannel = baseChannels.get(0);

            Map<SUSEProduct, List<Tuple2<SUSEProductSCCRepository, MgrSyncStatus>>> byExtension = exts.stream()
                    .collect(Collectors.groupingBy(e -> e.getA().getProduct()));

            Set<MgrSyncProductDto> extensions = byExtension.entrySet().stream().map(e -> {
                SUSEProduct ext = e.getKey();


                Set<MgrSyncChannelDto> extChildChannels = e.getValue().stream().map(c -> new MgrSyncChannelDto(
                        c.getA().getRepository().getName() +
                        (c.getA().getRootProduct().getArch() != null ?
                                " for " + c.getA().getRootProduct().getArch().getLabel() : ""),
                        c.getA().getChannelLabel(),
                        c.getA().getProduct().getFriendlyName(),
                        c.getA().getRepository().getDescription(),
                        c.getA().isMandatory(),
                        Optional.ofNullable(c.getA().getProduct().getArch()),
                        c.getA().getParentChannelLabel(),
                        c.getA().getProduct().getChannelFamily().getLabel(),
                        c.getA().getProduct().getName(),
                        c.getA().getProduct().getVersion(),
                        c.getB(),
                        c.getA().getRepository().isSigned(),
                        c.getA().getRepository().getUrl(),
                        c.getA().getUpdateTag()
                )).collect(Collectors.toSet());

                boolean isRecommended = Optional.ofNullable(recommendedForBase.get(ext.getProductId()))
                        .map(s -> s.contains(root.getProductId()))
                        .orElse(false);

                MgrSyncProductDto productDto = new MgrSyncProductDto(
                        ext.getFriendlyName(), ext.getProductId(), ext.getVersion(), isRecommended,
                        baseChannel, extChildChannels, Collections.emptySet()
                );

                return productDto;
            }).collect(Collectors.toSet());


            MgrSyncProductDto rootProductDto = new MgrSyncProductDto(
                    root.getFriendlyName(), root.getProductId(), root.getVersion(), false,
                    baseChannel, allChannels, extensions
            );

            return rootProductDto;
        }).collect(Collectors.toList());


        return productDtos;
    }

    /**
     * Refresh the repositories cache by reading repos from SCC for all available mirror
     * credentials, consolidating and inserting into the database.
     *
     * Two possible modes:
     * 1. Online - mirror from SCC or a specified optional mirror
     * 2. Offline - "fromdir" configured - everything must come from there
     *
     * Credential NULL defines Offline mode
     *
     * @throws ContentSyncException in case of an error
     */
    private void refreshRepositoriesAuthentication(String mirrorUrl) throws ContentSyncException {
        List<Credentials> credentials = filterCredentials();

        ChannelFactory.cleanupOrphanVendorContentSource();

        // Query repos for all mirror credentials and consolidate
        for (Credentials c : credentials) {
            try {
                log.debug("Getting repos for: " + c);
                SCCClient scc = getSCCClient(c);
                List<SCCRepositoryJson> repos = scc.listRepositories();
                repos.addAll(getRHELRepositories());
                refreshRepositoriesAuthentication(repos, c, mirrorUrl);
            }
            catch (SCCClientException | URISyntaxException e) {
                throw new ContentSyncException(e);
            }
        }
        linkAndRefreshContentSource(mirrorUrl);
        ManagerInfoFactory.setLastMgrSyncRefresh();
    }

    /**
     * Create or update a ContentSource.
     * @param auth a repository authentication object to use
     * @param channel the channel
     * @param mirrorUrl optional mirror URL
     */
    public void createOrUpdateContentSource(SCCRepositoryAuth auth, Channel channel, String mirrorUrl) {
        ContentSource source = auth.getContentSource();

        if (source == null) {
            String url = contentSourceUrlOverwrite(auth.getRepository(), auth.getUrl(), mirrorUrl);
            source = Optional.ofNullable(ChannelFactory.findVendorContentSourceByRepo(url))
                    .orElse(new ContentSource());
            source.setLabel(channel.getLabel());
            source.setMetadataSigned(auth.getRepository().isSigned());
            source.setOrg(null);
            source.setSourceUrl(url);
            source.setType(ChannelFactory.lookupContentSourceType("yum"));
            ChannelFactory.save(source);
        }
        Set<ContentSource> css = channel.getSources();
        css.add(source);
        channel.setSources(css);
        ChannelFactory.save(channel);
        auth.setContentSource(source);
    }

    /**
     * Search for orphan contentsource or channels and try to find
     * available repositories. In case they are found they get linked.
     *
     * @param mirrorUrl optional mirror url
     */
    public void linkAndRefreshContentSource(String mirrorUrl) {
        // flush needed to let the next queries find something
        HibernateFactory.getSession().flush();
        // find all CountentSource with org id == NULL which do not have a sccrepositoryauth
        List<ContentSource> orphan = ChannelFactory.lookupOrphanVendorContentSources();
        if (orphan != null) {
            log.debug("found orphan vendor content sources: " + orphan.size());
            // find sccrepositoryauth and link
            orphan.forEach(cs ->
                cs.getChannels().forEach(c ->
                    Opt.consume(ChannelFactory.findVendorRepositoryByChannel(c),
                    () -> {
                        log.debug("No repository found for channel: '" + cs.getLabel() + "' - remove content source");
                        ChannelFactory.remove(cs);
                    },
                    repo ->
                        Opt.consume(repo.getBestAuth(),
                        () -> {
                            log.debug("No auth anymore - remove content source: " + cs.getLabel());
                            ChannelFactory.remove(cs);
                        }, auth -> {
                            log.debug("Has new auth: " + cs.getLabel());
                            auth.setContentSource(cs);
                        })
                    )
                )
            );
        }
        // find all rhnChannel with org id == null and no content source
        List<Channel> orphanChannels = ChannelFactory.lookupOrphanVendorChannels();
        if (orphanChannels != null) {
            log.debug("found orphan vendor channels: " + orphanChannels.size());
            // find sccrepository auth and create content source and link
            orphanChannels.forEach(c -> {
                Opt.consume(ChannelFactory.findVendorRepositoryByChannel(c),
                    () -> log.error("No repository found for channel: '" + c.getLabel() + "'"),
                    repo -> repo.getBestAuth().ifPresent(a -> createOrUpdateContentSource(a, c, mirrorUrl))
                );
            });
        }
        // update URL if needed
        SCCCachingFactory.lookupRepositoryAuthWithContentSource().stream()
            .forEach(a -> {
                ContentSource cs = a.getContentSource();
                String overwriteUrl = contentSourceUrlOverwrite(a.getRepository(), a.getUrl(), mirrorUrl);
                if (!cs.getSourceUrl().equals(overwriteUrl)) {
                    cs.setSourceUrl(overwriteUrl);
                }
            });
    }

    /**
     * Update authentication for all repos of the given credential.
     * Removes authentication if they have expired
     *
     * @param repositories the new repositories
     * @param c the credentials
     * @param mirrorUrl optional mirror url
     */
    public void refreshRepositoriesAuthentication(
            Collection<SCCRepositoryJson> repositories, Credentials c, String mirrorUrl) {
        List<Long> repoIdsFromCredential = new LinkedList<>();
        Map<Long, SCCRepository> availableRepos = SCCCachingFactory.lookupRepositories().stream()
                .collect(Collectors.toMap(r -> r.getSccId(), r -> r));
        List<SCCRepositoryAuth> allRepoAuths = SCCCachingFactory.lookupRepositoryAuth();
        if (c == null) {
            // cleanup if we come from scc
            allRepoAuths.stream()
                .filter(a -> a.getOptionalCredentials().isPresent())
                .forEach(a -> SCCCachingFactory.deleteRepositoryAuth(a));
        }
        else {
            // cleanup if we come from "fromdir"
            allRepoAuths.stream()
                .filter(a -> !a.getOptionalCredentials().isPresent())
                .forEach(a -> SCCCachingFactory.deleteRepositoryAuth(a));
        }
        allRepoAuths = null;
        for (SCCRepositoryJson jrepo : repositories) {
            SCCRepository repo = availableRepos.get(jrepo.getSCCId());
            if (repo == null) {
                log.error("No repository with ID '" + jrepo.getSCCId() + "' found");
                continue;
            }
            Set<SCCRepositoryAuth> allAuths = repo.getRepositoryAuth();
            Set<SCCRepositoryAuth> authsThisCred = allAuths.stream()
                    .filter(a -> {
                        if (c == null) {
                            return !a.getOptionalCredentials().isPresent();
                        }
                        else {
                            Optional<Credentials> oc = a.getOptionalCredentials();
                            return oc.isPresent() && oc.get().equals(c);
                        }
                    })
                    .collect(Collectors.toSet());
            if (authsThisCred.size() > 1) {
                log.error("More than 1 authentication found for one credential - removing all unused");
                authsThisCred.stream().forEach(a -> {
                    allAuths.remove(a);
                    authsThisCred.remove(a);
                    repo.setRepositoryAuth(allAuths);
                    SCCCachingFactory.deleteRepositoryAuth(a);
                });
            }
            String url = jrepo.getUrl();
            if (c == null) {
                // "fromdir" - convert into local file URL
                url = MgrSyncUtils.urlToFSPath(url, repo.getName()).toString();
            }

            Optional<String> tokenOpt = getTokenFromURL(url);

            // find out the auth type of the URL
            SCCRepositoryAuth newAuth = null;
            if (tokenOpt.isPresent()) {
                newAuth = new SCCRepositoryTokenAuth(tokenOpt.get());
            }
            else {
                try {
                    String fullUrl = buildRepoFileUrl(url, repo);
                    if (accessibleUrl(fullUrl)) {
                        URI uri = new URI(url);
                        if (uri.getUserInfo() == null) {
                            newAuth = new SCCRepositoryNoAuth();
                        }
                        else {
                            // we do not handle the case where the credentials are part of the URL
                            log.error("URLs with credentials not supported");
                            continue;
                        }
                    }
                    else if (c != null && accessibleUrl(fullUrl, c.getUsername(), c.getPassword())) {
                        newAuth = new SCCRepositoryBasicAuth();
                    }
                    else {
                        // typical happens with fromdir where not all repos are synced
                        continue;
                    }
                }
                catch (URISyntaxException e) {
                    log.warn("Unable to parse URL");
                    continue;
                }
            }
            repoIdsFromCredential.add(jrepo.getSCCId());
            if (authsThisCred.isEmpty()) {
                // We need to create a new auth for this repo
                newAuth.setCredentials(c);
                newAuth.setRepository(repo);
                allAuths.add(newAuth);
                repo.setRepositoryAuth(allAuths);
                SCCCachingFactory.saveRepositoryAuth(newAuth);
            }
            else {
                // Auth exists - check if we need to update it
                SCCRepositoryAuth exAuth = authsThisCred.iterator().next();
                if (exAuth instanceof SCCRepositoryTokenAuth && newAuth instanceof SCCRepositoryTokenAuth) {
                    SCCRepositoryTokenAuth tauth = (SCCRepositoryTokenAuth) exAuth;
                    if (!tauth.getAuth().equals(tokenOpt.get())) {
                        tauth.setAuth(tokenOpt.get());
                        SCCCachingFactory.saveRepositoryAuth(tauth);
                        ContentSource updateCS = tauth.getContentSource();
                        if (updateCS != null) {
                            updateCS.setMetadataSigned(repo.isSigned());
                            updateCS.setSourceUrl(contentSourceUrlOverwrite(repo, tauth.getUrl(), mirrorUrl));
                            ChannelFactory.save(updateCS);
                        }
                    }
                }
                // other types are basic and no auth which differ only in the type
                else if (!exAuth.getClass().equals(newAuth.getClass())) {
                    // class differ => remove and later add
                    newAuth.setCredentials(c);
                    newAuth.setRepository(repo);
                    SCCCachingFactory.saveRepositoryAuth(newAuth);
                    allAuths.add(newAuth);
                    allAuths.remove(exAuth);
                    repo.setRepositoryAuth(allAuths);
                    SCCCachingFactory.deleteRepositoryAuth(exAuth);
                }
                // else => unchanged, nothing to do
            }
        }

        // OES
        repoIdsFromCredential.addAll(refreshOESRepositoryAuth(c, mirrorUrl));

        // check if we have to remove auths which exists before
        List<SCCRepositoryAuth> authList = SCCCachingFactory.lookupRepositoryAuthByCredential(c);
        authList.stream()
            .filter(repoAuth -> !repoIdsFromCredential.contains(repoAuth.getRepository().getSccId()))
            .forEach(repoAuth -> SCCCachingFactory.deleteRepositoryAuth(repoAuth));
    }

    /**
     * Special Handling for OES.
     * We expect that all OES products are accessible with the same subscription identified
     * by the product class aka channel family.
     *
     * This means we check accessibility of just one OES URL and enable/disable all
     * OES products depending on that result.
     *
     * @param c credential to use for the check
     * @param mirrorUrl optional mirror url
     * @return list of available repository ids
     */
    public List<Long> refreshOESRepositoryAuth(Credentials c, String mirrorUrl) {
        List<Long> oesRepoIds = new LinkedList<>();
        if (!(c == null || accessibleUrl(OES_URL, c.getUsername(), c.getPassword()))) {
            return oesRepoIds;
        }
        List<SCCRepository> oesRepos = SCCCachingFactory.lookupRepositoriesByChannelFamily(OES_CHANNEL_FAMILY);
        for (SCCRepository repo : oesRepos) {
            Set<SCCRepositoryAuth> allAuths = repo.getRepositoryAuth();
            Set<SCCRepositoryAuth> authsThisCred = allAuths.stream()
                    .filter(a -> {
                        if (c == null) {
                            return !a.getOptionalCredentials().isPresent();
                        }
                        else {
                            Optional<Credentials> oc = a.getOptionalCredentials();
                            return oc.isPresent() && oc.get().equals(c);
                        }
                    })
                    .collect(Collectors.toSet());
            if (authsThisCred.size() > 1) {
                log.error("More than 1 authentication found for one credential - removing all");
                authsThisCred.forEach(a -> {
                    allAuths.remove(a);
                    authsThisCred.remove(a);
                    repo.setRepositoryAuth(allAuths);
                    SCCCachingFactory.deleteRepositoryAuth(a);
                });
            }
            SCCRepositoryAuth newAuth = new SCCRepositoryBasicAuth();
            if (c == null) {
                // we need to check every repo if it is available
                String url = MgrSyncUtils.urlToFSPath(repo.getUrl(), repo.getName()).toString();
                try {
                    if (!accessibleUrl(buildRepoFileUrl(url, repo))) {
                        continue;
                    }
                }
                catch (URISyntaxException e) {
                    log.error("Failed to parse URL", e);
                    continue;
                }
                newAuth = new SCCRepositoryNoAuth();
            }
            // this repo exists and is accessible
            oesRepoIds.add(repo.getSccId());
            if (authsThisCred.isEmpty()) {
                // We need to create a new auth for this repo
                newAuth.setCredentials(c);
                newAuth.setRepository(repo);
                allAuths.add(newAuth);
                repo.setRepositoryAuth(allAuths);
                SCCCachingFactory.saveRepositoryAuth(newAuth);
            }
            else {
                // else: Auth exists - check, if URL still match
                SCCRepositoryAuth exAuth = authsThisCred.iterator().next();
                ContentSource updateCS = exAuth.getContentSource();
                if (updateCS != null) {
                    updateCS.setMetadataSigned(repo.isSigned());
                    updateCS.setSourceUrl(contentSourceUrlOverwrite(repo, exAuth.getUrl(), mirrorUrl));
                    ChannelFactory.save(updateCS);
                }
            }
        }
        return oesRepoIds;
    }

    /**
     * Check for configured overwrite scenarios for the provides repo URL.
     * Check if "fromdir" configuration is in place or a mirror is configured.
     * Return the correct path/url if this is the case.
     *
     * If mirror url is not NULL or "mirror" is configured via configuration file,
     * it check if the requested repository is available on that mirror.
     * In case it is, it returns the URL to the mirror.
     *
     * In case nothing special is configured, return defaultUrl
     *
     * @param repo {@link SCCRepository}
     * @param defaultUrl URL which will be returns if no special condition match
     * @param mirrorUrl optional mirror to check
     * @return URL to use for the provided repo
     */
    public String contentSourceUrlOverwrite(SCCRepository repo, String defaultUrl, String mirrorUrl) {
        String url = repo.getUrl();
        if (StringUtils.isBlank(url)) {
            return defaultUrl;
        }
        // if fromdir is set, defaultURL contains already correct file URL
        if (Config.get().getString(ContentSyncManager.RESOURCE_PATH, null) != null) {
            return defaultUrl;
        }

        // check if a mirror url is specified
        if (StringUtils.isBlank(mirrorUrl)) {
            mirrorUrl = Config.get().getString(MIRROR_CFG_KEY);
            if (StringUtils.isBlank(mirrorUrl)) {
                return defaultUrl;
            }
        }

        try {
            URI mirrorUri = new URI(mirrorUrl);
            URI sourceUri = new URI(url);

            // Setup the path
            String mirrorPath = StringUtils.defaultString(mirrorUri.getRawPath());
            String combinedPath = new File(StringUtils.stripToEmpty(mirrorPath),
                    sourceUri.getRawPath()).getPath();

            // Build full URL to test
            URI testUri = new URI(mirrorUri.getScheme(), mirrorUri.getUserInfo(), mirrorUri.getHost(),
                    mirrorUri.getPort(), combinedPath, mirrorUri.getQuery(), null);

            if (accessibleUrl(buildRepoFileUrl(testUri.toString(), repo))) {
                return testUri.toString();
            }
        }
        catch (URISyntaxException e) {
            log.warn(e.getMessage());
        }
        return defaultUrl;
    }

    /**
     * Build URL pointing to a file to test availablity of a repository.
     * Support either repomd or Debian style repos.
     *
     * @param url the repo url
     * @param repo the repo object
     * @return full URL pointing to a file which should be available depending on the repo type
     * @throws URISyntaxException in case of an error
     */
    public String buildRepoFileUrl(String url, SCCRepository repo) throws URISyntaxException {
        URI uri = new URI(url);
        String relFile = "/repodata/repomd.xml";

        // Debian repo
        if (repo.getDistroTarget() != null && repo.getDistroTarget().equals("amd64")) {
            relFile = "Release";
        }
        Path urlPath = new File(StringUtils.defaultString(uri.getRawPath(), "/"), relFile).toPath();
        return new URI(uri.getScheme(), null, uri.getHost(), uri.getPort(), urlPath.toString(),
                uri.getQuery(), null).toString();
    }

    /**
     * Refresh the subscription cache by reading subscriptions from SCC for all available
     * mirror credentials, consolidating and inserting into the database.
     *
     * @param subscriptions list of scc subscriptions
     * @param c credentials
     * @throws ContentSyncException in case of an error
     */
    public void refreshSubscriptionCache(List<SCCSubscriptionJson> subscriptions,
            Credentials c) {
        List<Long> cachedSccIDs = SCCCachingFactory.listSubscriptionsIdsByCredentials(c);
        Map<Long, SCCSubscription> subscriptionsBySccId = SCCCachingFactory.lookupSubscriptions()
                .stream().collect(Collectors.toMap(s -> s.getSccId(), s -> s));
        Map<Long, SUSEProduct> productsBySccId = SUSEProductFactory.productsByProductIds();
        for (SCCSubscriptionJson s : subscriptions) {
            SCCSubscription ns = SCCCachingFactory.saveJsonSubscription(s, c, productsBySccId, subscriptionsBySccId);
            subscriptionsBySccId.put(ns.getSccId(), ns);
            cachedSccIDs.remove(s.getId());
        }
        if (log.isDebugEnabled()) {
            log.debug("Found " + subscriptions.size() +
                    " subscriptions with credentials: " + c);
        }
        for (Long subId : cachedSccIDs) {
            log.debug("Delete Subscription with sccId: " + subId);
            SCCCachingFactory.deleteSubscriptionBySccId(subId);
        }
    }

    /**
     * Get subscriptions from SCC for a single pair of mirror credentials
     * and update the DB.
     * Additionally order items are fetched and put into the DB.
     * @param credentials username/password pair
     * @return list of subscriptions as received from SCC.
     * @throws SCCClientException in case of an error
     */
    public List<SCCSubscriptionJson> updateSubscriptions(Credentials credentials)
            throws SCCClientException {
        try {
            SCCClient scc = this.getSCCClient(credentials);
            List<SCCSubscriptionJson> subscriptions = scc.listSubscriptions();
            refreshSubscriptionCache(subscriptions, credentials);
            refreshOrderItemCache(credentials);
            generateOEMOrderItems(subscriptions, credentials);
            return subscriptions;
        }
        catch (URISyntaxException e) {
            log.error("Invalid URL:" + e.getMessage());
            return new ArrayList<SCCSubscriptionJson>();
        }
    }

    /**
     * Returns all subscriptions available to all configured credentials.
     * Update the DB with new fetched subscriptions and Order Items
     * @return list of all available subscriptions
     * @throws ContentSyncException in case of an error
     */
    public Collection<SCCSubscriptionJson> updateSubscriptions() throws ContentSyncException {
        log.info("ContentSyncManager.getSubscriptions called");
        Set<SCCSubscriptionJson> subscriptions = new HashSet<SCCSubscriptionJson>();
        List<Credentials> credentials = filterCredentials();
        // Query subscriptions for all mirror credentials
        for (Credentials c : credentials) {
            try {
                subscriptions.addAll(updateSubscriptions(c));
            }
            catch (SCCClientException e) {
                throw new ContentSyncException(e);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Found " + subscriptions.size() + " available subscriptions.");
        }
        log.info("ContentSyncManager.getSubscriptions finished");
        return subscriptions;
    }

    /**
     * Fetch new Order Items from SCC for the given credentials,
     * deletes all order items stored in the database below the given credentials
     * and inserts the new ones.
     * @param c the credentials
     * @throws SCCClientException  in case of an error
     */
    public void refreshOrderItemCache(Credentials c) throws SCCClientException  {
        try {
            SCCClient scc = this.getSCCClient(c);
            List<SCCOrderJson> orders = scc.listOrders();
            List<SCCOrderItem> existingOI = SCCCachingFactory.listOrderItemsByCredentials(c);
            for (SCCOrderJson order : orders) {
                for (SCCOrderItemJson j : order.getOrderItems()) {
                    SCCOrderItem oi = SCCCachingFactory.lookupOrderItemBySccId(j.getSccId())
                            .orElse(new SCCOrderItem());
                    oi.update(j, c);
                    SCCCachingFactory.saveOrderItem(oi);
                    existingOI.remove(oi);
                }
            }
            existingOI.stream()
                .filter(item -> item.getSccId() >= 0)
                .forEach(item -> SCCCachingFactory.deleteOrderItem(item));
        }
        catch (URISyntaxException e) {
            log.error("Invalid URL:" + e.getMessage());
        }
    }

    /**
     * Generates OrderItems for OEM subscriptions.
     *
     * @param subscriptions the subscriptions
     * @param credentials the credentials
     */
    private void generateOEMOrderItems(List<SCCSubscriptionJson> subscriptions,
            Credentials credentials) {
        List<SCCOrderItem> existingOI = SCCCachingFactory.listOrderItemsByCredentials(credentials);
        subscriptions.stream()
                .filter(sub -> "oem".equals(sub.getType()))
                .forEach(sub -> {
                    if (sub.getSkus().size() == 1) {
                        log.debug("Generating order item for OEM subscription " +
                                sub.getName() + ", SCC ID: " + sub.getId());
                        long subscriptionSccId = sub.getId();
                        SCCOrderItem oemOrder = SCCCachingFactory.lookupOrderItemBySccId(-subscriptionSccId)
                                .orElse(new SCCOrderItem());
                        // HACK: use inverted subscription id as new the order item id
                        oemOrder.setSccId(-subscriptionSccId);
                        oemOrder.setQuantity(sub.getSystemLimit().longValue());
                        oemOrder.setCredentials(credentials);
                        oemOrder.setStartDate(sub.getStartsAt());
                        oemOrder.setEndDate(sub.getExpiresAt());
                        oemOrder.setSku(sub.getSkus().get(0));
                        oemOrder.setSubscriptionId(subscriptionSccId);
                        SCCCachingFactory.saveOrderItem(oemOrder);
                        existingOI.remove(oemOrder);
                    }
                    else {
                        log.warn("Subscription " + sub.getName() + ", SCC ID: " +
                                sub.getId() + " does not have a single SKU. " +
                                "Not generating Order Item for it."
                        );
                    }
                });
        existingOI.stream()
            .filter(item -> item.getSccId() < 0)
            .forEach(item -> SCCCachingFactory.deleteOrderItem(item));
    }

    /**
     * Update repositories and its available authentications.
     * If mirrorUrl is given, the method search for available repositories
     * and prefer them over the official repository urls.
     * Set to NULL if no mirror should be used.
     *
     * @param mirrorUrl optional URL string to search for available repositories
     * @throws ContentSyncException in case of an error
     */
    public void updateRepositories(String mirrorUrl) throws ContentSyncException {
        log.info("ContentSyncManager.updateRepository called");
        refreshRepositoriesAuthentication(mirrorUrl);
        log.info("ContentSyncManager.updateRepository finished");
    }

    /**
     * Update channel families in DB with data from the channel_families.json file.
     * @param channelFamilies List of families.
     * @throws ContentSyncException in case of an error
     */
    public void updateChannelFamilies(Collection<ChannelFamilyJson> channelFamilies)
            throws ContentSyncException {
        log.info("ContentSyncManager.updateChannelFamilies called");
        List<String> suffixes = Arrays.asList("", "ALPHA", "BETA");

        for (ChannelFamilyJson channelFamily : channelFamilies) {
            for (String suffix : suffixes) {
                ChannelFamily family = createOrUpdateChannelFamily(
                        channelFamily.getLabel(), channelFamily.getName(), suffix);
                // Create rhnPublicChannelFamily entry if it doesn't exist
                if (family.getPublicChannelFamily() == null) {
                    PublicChannelFamily pcf = new PublicChannelFamily();

                    // save the public channel family
                    pcf.setChannelFamily(family);
                    ChannelFamilyFactory.save(pcf);
                    family.setPublicChannelFamily(pcf);
                }
            }
        }
        log.info("ContentSyncManager.updateChannelFamilies finished");
    }

    /**
     * Update a product in DB
     * @param p the SCC product
     * @param product the database product whcih should be updated
     * @param channelFamilyByLabel lookup map for channel family by label
     * @return the updated product
     */
    public static SUSEProduct updateProduct(SCCProductJson p, SUSEProduct product,
            Map<String, ChannelFamily> channelFamilyByLabel) {
        // it is not guaranteed for this ID to be stable in time, as it
        // depends on IBS
        product.setProductId(p.getId());
        product.setFriendlyName(p.getFriendlyName());
        product.setDescription(p.getDescription());
        product.setFree(p.isFree());
        product.setReleaseStage(p.getReleaseStage());

        product.setName(p.getIdentifier().toLowerCase());
        product.setVersion(p.getVersion() != null ? p.getVersion().toLowerCase() : null);
        product.setRelease(p.getReleaseType() != null ? p.getReleaseType().toLowerCase() : null);
        product.setBase(p.isBaseProduct());
        // Create the channel family if it is not available
        String productClass = p.getProductClass();
        product.setChannelFamily(
                !StringUtils.isBlank(productClass) ?
                        createOrUpdateChannelFamily(productClass, null, channelFamilyByLabel) : null);
        return product;
    }

    /**
     * Create a new product in DB
     * @param p product from SCC
     * @param channelFamilyMap lookup map for channel family by label
     * @param packageArchMap lookup map for package arch by label
     * @return the new product
     */
    public static SUSEProduct createNewProduct(SCCProductJson p, Map<String, ChannelFamily> channelFamilyMap,
            Map<String, PackageArch> packageArchMap) {
        // Otherwise create a new SUSE product and save it
        SUSEProduct product = new SUSEProduct();

        String productClass = p.getProductClass();

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
        product.setDescription(p.getDescription());
        product.setFree(p.isFree());
        product.setReleaseStage(p.getReleaseStage());
        product.setBase(p.isBaseProduct());


        product.setChannelFamily(
                !StringUtils.isBlank(productClass) ?
                        channelFamilyMap.computeIfAbsent(productClass,
                                pc -> createOrUpdateChannelFamily(pc, null, channelFamilyMap)) : null);

        PackageArch pArch = packageArchMap.computeIfAbsent(p.getArch(), PackageFactory::lookupPackageArchByLabel);
        if (pArch == null && p.getArch() != null) {
            // unsupported architecture, skip the product
            log.error("Unknown architecture '" + p.getArch() +
                    "'. This may be caused by a missing database migration");
        }
        else {
            product.setArch(pArch);
        }
        return product;
    }

    /*
     * load the static tree from file
     */
    private static List<StaticInfoEntry> loadStaticTree() {
        List<StaticInfoEntry> tree = new ArrayList<>();
        try {
            tree = JsonParser.GSON.fromJson(new BufferedReader(new InputStreamReader(
                            new FileInputStream(sumaProductTreeJson))),
                    SCCClientUtils.toListType(StaticInfoEntry.class));
        }
        catch (IOException e) {
            log.error(e);
        }
        return tree;
    }

    /**
     * Return a distinct flat list of products
     * @param products product tree
     * @return flat list of products
     */
    public static Stream<SCCProductJson> flattenProducts(List<SCCProductJson> products) {
        return products.stream().flatMap(p -> Stream.concat(
                Stream.of(p),
                flattenProducts(p.getExtensions())
                ))
                .distinct();
    }

    /**
     * Collect all repositories from the product list and return them as list
     * @param products the product list
     * @return a list of repositories
     */
    public static List<SCCRepositoryJson> collectRepos(List<SCCProductJson> products) {
       return products.stream().flatMap(p -> p.getRepositories().stream()).collect(Collectors.toList());
    }

    /**
     * Update Products, Repositories and relation ship table in DB.
     * @param productsById map of scc products by id
     * @param reposById map of scc repositories by id
     * @param tree the static suse product tree
     */
    public static void updateProducts(Map<Long, SCCProductJson> productsById, Map<Long, SCCRepositoryJson> reposById,
            List<StaticInfoEntry> tree) {
        Map<String, PackageArch> packageArchMap = PackageFactory.lookupPackageArch()
                .stream().collect(Collectors.toMap(a -> a.getLabel(), a -> a));
        Map<String, ChannelFamily> channelFamilyMap = ChannelFamilyFactory.getAllChannelFamilies()
                .stream().collect(Collectors.toMap(cf -> cf.getLabel(), cf -> cf));
        Map<Tuple3<Long, Long, Long>, SUSEProductSCCRepository> dbProductReposByIds =
                SUSEProductFactory.allProductReposByIds();
        Map<Long, SUSEProduct> dbProductsById = SUSEProductFactory.findAllSUSEProducts().stream()
                .collect(Collectors.toMap(p -> p.getProductId(), p -> p));
        Map<Long, SCCRepository> dbReposById = SCCCachingFactory.lookupRepositories().stream()
                .collect(Collectors.toMap(r -> r.getSccId(), r -> r));
        Map<Tuple3<Long, Long, Long>, SUSEProductExtension> dbSUSEProductExtensionsByIds = SUSEProductFactory
                .findAllSUSEProductExtensions().stream().collect(Collectors.toMap(
                        e -> new Tuple3<>(
                                e.getBaseProduct().getProductId(),
                                e.getExtensionProduct().getProductId(),
                                e.getRootProduct().getProductId()),
                        e -> e
                ));
        Set<Long> productIdsSwitchedToReleased = new HashSet<>();

        Map<Long, SUSEProduct> productMap = productsById.values().stream().map(productJson -> {
                // If the product is release the id should be stable
                // so we don't do the fuzzy matching to reduce unexpected behaviour
                if (productJson.getReleaseStage() == ReleaseStage.released) {
                    return Opt.fold(
                            Optional.ofNullable(dbProductsById.get(productJson.getId())),
                            () -> {
                                SUSEProduct prod = createNewProduct(productJson, channelFamilyMap, packageArchMap);
                                dbProductsById.put(prod.getProductId(), prod);
                                return prod;
                            },
                            prod -> {
                                if (prod.getReleaseStage() != ReleaseStage.released) {
                                    // product switched from beta to released.
                                    // tag for later cleanup all assosicated repositories
                                    productIdsSwitchedToReleased.add(prod.getProductId());
                                }
                                updateProduct(productJson, prod, channelFamilyMap);
                                dbProductsById.put(prod.getProductId(), prod);
                                return prod;
                            }
                    );
                }
                else {
                    return Opt.fold(
                            Opt.or(
                                    Optional.ofNullable(dbProductsById.get(productJson.getId())),
                                    Optional.ofNullable(SUSEProductFactory.findSUSEProduct(
                                            productJson.getIdentifier(),
                                            productJson.getVersion(),
                                            productJson.getReleaseType(),
                                            productJson.getArch(),
                                            false
                                    ))
                            ),
                            () -> {
                                SUSEProduct prod = createNewProduct(productJson, channelFamilyMap, packageArchMap);
                                dbProductsById.put(prod.getProductId(), prod);
                                return prod;
                            },
                            prod -> {
                                updateProduct(productJson, prod, channelFamilyMap);
                                dbProductsById.put(prod.getProductId(), prod);
                                return prod;
                            }
                    );
                }
        }).collect(Collectors.toMap(SUSEProduct::getProductId, p -> p));


        Map<Long, SCCRepository> repoMap = reposById.values().stream().map(repoJson -> {
                return Opt.fold(Optional.ofNullable(dbReposById.get(repoJson.getSCCId())),
                    () -> {
                        SCCRepository r = new SCCRepository();
                        r.update(repoJson);
                        dbReposById.put(r.getSccId(), r);
                        return r;
                    },
                    r -> {
                        r.setName(repoJson.getName());
                        r.setDescription(repoJson.getDescription());
                        r.setUrl(repoJson.getUrl());
                        dbReposById.put(r.getSccId(), r);
                        return r;
                    });
        }).collect(Collectors.toMap(p -> p.getSccId(), p -> p));

        Map<Tuple3<Long, Long, Long>, SUSEProductSCCRepository> productReposToSave = new HashMap<>();
        Map<Tuple3<Long, Long, Long>, SUSEProductExtension> extensionsToSave = new HashMap<>();
        Set<String> channelsToCleanup = new HashSet<>();

        tree.stream().forEach(entry -> {
            SCCProductJson productJson = productsById.get(entry.getProductId());

            SCCRepositoryJson repoJson = reposById.get(entry.getRepositoryId());

            SCCProductJson rootJson = productsById.get(entry.getRootProductId());

            Optional<Optional<SCCProductJson>> parentJson = entry.getParentProductId()
                    .map(id -> Optional.ofNullable(productsById.get(id)));

            if (productJson != null  && repoJson != null && rootJson != null &&
                    (!parentJson.isPresent() || parentJson.get().isPresent())) {

                Tuple3<Long, Long, Long> ids = new Tuple3<>(rootJson.getId(), productJson.getId(), repoJson.getSCCId());
                SUSEProduct product = productMap.get(productJson.getId());
                SUSEProduct root = productMap.get(rootJson.getId());
                //FIXME: this is not pretty and should be changed if somebody has the time
                Optional<SUSEProduct> parent = parentJson.flatMap(Function.identity())
                        .map(p -> productMap.get(p.getId()));

                SUSEProductSCCRepository productRepo = Opt.fold(Optional.ofNullable(dbProductReposByIds.get(ids)),
                        () -> {
                            SCCRepository repo = repoMap.get(repoJson.getSCCId());
                            repo.setSigned(entry.isSigned());

                            SUSEProductSCCRepository prodRepoLink = new SUSEProductSCCRepository();
                            prodRepoLink.setUpdateTag(entry.getUpdateTag().orElse(null));
                            prodRepoLink.setChannelLabel(entry.getChannelLabel());
                            prodRepoLink.setParentChannelLabel(entry.getParentChannelLabel().orElse(null));
                            prodRepoLink.setChannelName(entry.getChannelName());
                            prodRepoLink.setMandatory(entry.isMandatory());
                            prodRepoLink.setProduct(product);
                            prodRepoLink.setRepository(repo);
                            prodRepoLink.setRootProduct(root);
                            dbProductReposByIds.put(ids, prodRepoLink);

                            if (productIdsSwitchedToReleased.contains(entry.getProductId())) {
                                channelsToCleanup.add(entry.getChannelLabel());
                            }
                            return prodRepoLink;
                        }, prodRepoLink -> {
                            if (entry.getReleaseStage() != ReleaseStage.released) {
                                prodRepoLink.setUpdateTag(entry.getUpdateTag().orElse(null));
                                prodRepoLink.setChannelLabel(entry.getChannelLabel());
                                prodRepoLink.setParentChannelLabel(entry.getParentChannelLabel().orElse(null));
                            }
                            else {
                                if (!entry.getParentChannelLabel()
                                        .equals(Optional.ofNullable(prodRepoLink.getParentChannelLabel()))) {
                                    log.error("parent_channel_label changed from '" +
                                            prodRepoLink.getParentChannelLabel() +
                                            "' to '" + entry.getParentChannelLabel() +
                                            "' but its not allowed to change.");
                                }

                                if (!entry.getUpdateTag()
                                        .equals(Optional.ofNullable(prodRepoLink.getUpdateTag()))) {
                                    log.error("updatetag changed from '" +
                                            prodRepoLink.getUpdateTag() +
                                            "' to '" + entry.getUpdateTag() +
                                            "' but its not allowed to change.");
                                }

                                if (!entry.getChannelLabel().equals(prodRepoLink.getChannelLabel())) {
                                    log.error("channel_label changed from '" +
                                            prodRepoLink.getChannelLabel() +
                                            "' to '" + entry.getChannelLabel() +
                                            "' but its not allowed to change.");
                                }
                            }
                            prodRepoLink.setChannelName(entry.getChannelName());
                            prodRepoLink.setMandatory(entry.isMandatory());

                            if (productIdsSwitchedToReleased.contains(entry.getProductId())) {
                                channelsToCleanup.add(entry.getChannelLabel());
                            }
                            return prodRepoLink;
                        });

                parent.ifPresent(p -> {
                    Tuple3<Long, Long, Long> peId = new Tuple3<>(
                            p.getProductId(), product.getProductId(), root.getProductId());

                    SUSEProductExtension pe = Opt.fold(Optional.ofNullable(dbSUSEProductExtensionsByIds.get(peId)),
                            () -> new SUSEProductExtension(p, product, root, entry.isRecommended()),
                            existingPe -> {
                                existingPe.setRecommended(entry.isRecommended());
                                return existingPe;
                            }
                    );
                    extensionsToSave.put(peId, pe);
                });

                productReposToSave.put(ids, productRepo);
            }
        });


        dbSUSEProductExtensionsByIds.entrySet().stream()
                .filter(e -> !extensionsToSave.containsKey(e.getKey()))
                .map(e -> e.getValue())
                .forEach(SUSEProductFactory::remove);


        dbProductReposByIds.entrySet().stream()
                .filter(e -> !productReposToSave.containsKey(e.getKey()))
                .map(e -> e.getValue())
                .forEach(SUSEProductFactory::remove);

        dbReposById.entrySet().stream()
            .filter(e -> !repoMap.containsKey(e.getKey()))
            .map(e -> e.getValue())
            .forEach(r -> {
                r.getRepositoryAuth().forEach(SCCCachingFactory::deleteRepositoryAuth);
                SCCCachingFactory.deleteRepository(r);
            });

        productMap.values().forEach(SUSEProductFactory::save);
        extensionsToSave.values().forEach(SUSEProductFactory::save);
        repoMap.values().forEach(SUSEProductFactory::save);
        productReposToSave.values().forEach(SUSEProductFactory::save);

        ChannelFactory.listVendorChannels().stream().forEach(c -> {
            updateChannel(c);
            if (channelsToCleanup.contains(c.getLabel())) {
                ChannelManager.disassociateChannelEntries(c);
            }
        });
    }

    /**
     * Update SUSE Products from SCC.
     * @param products the scc products
     * @throws ContentSyncException in case of an error
     */
    public void updateSUSEProducts(List<SCCProductJson> products) throws ContentSyncException {
        updateSUSEProducts(
                Stream.concat(
                        products.stream(),
                        getOESProducts().stream()
                ).collect(Collectors.toList()),
                readUpgradePaths(), loadStaticTree(),
                getRHELRepositories());
    }

    /**
     * Creates or updates entries in the SUSEProducts database table with a given list of
     * {@link SCCProductJson} objects.
     *
     * @param products list of products
     * @param upgradePathJsons list of available upgrade path
     * @param staticTree suse product tree with fixes and additional data
     * @param additionalRepos list of additional static repos
     */
    public void updateSUSEProducts(List<SCCProductJson> products, List<UpgradePathJson> upgradePathJsons,
                                   List<StaticInfoEntry> staticTree, List<SCCRepositoryJson> additionalRepos) {
        log.info("ContentSyncManager.updateSUSEProducts called");
        Map<Long, SUSEProduct> processed = new HashMap<>();

        List<SCCProductJson> allProducts = flattenProducts(products).collect(Collectors.toList());

        Map<Long, SCCProductJson> productsById = allProducts.stream().collect(Collectors.toMap(
                SCCProductJson::getId,
                Function.identity(),
                (x, y) -> x
        ));


        Map<Long, SCCRepositoryJson> reposById = Stream.concat(
                collectRepos(allProducts).stream(),
                additionalRepos.stream()
        ).collect(Collectors.toMap(
                SCCRepositoryJson::getSCCId,
                Function.identity(),
                (x, y) -> x
        ));

        updateProducts(productsById, reposById, staticTree);

        SUSEProductFactory.removeAllExcept(processed.values());

        updateUpgradePaths(products, upgradePathJsons);
        log.info("ContentSyncManager.updateSUSEProducts finished");
    }

    /**
     * Check if all mandatory repositories of product for the given root are accessible.
     * No recursive checking if bases are accessible too.
     * @param product the product to check
     * @param root the root we check for
     * @return true in case of all mandatory repos could be mirrored, otherwise false
     */
    public static boolean isProductAvailable(SUSEProduct product, SUSEProduct root) {
        Set<SUSEProductSCCRepository> entries =
                Optional.ofNullable(product.getRepositories())
                .orElseGet(Collections::emptySet);
        return entries.stream()
                .filter(e -> e.getRootProduct().equals(root))
                .filter(e -> e.isMandatory())
                .allMatch(entry ->
             entry.getProduct().getChannelFamily().isPublic() &&
             // isMirrorable
             entry.getRepository().isAccessible());
    }

    /**
     * Find all available repositories for product and all extensions of product
     * @param root root product of product
     * @param product product to get available repositories from
     * @return stream of available repositories of product
     */
    private Stream<SUSEProductSCCRepository> getAvailableRepositories(SUSEProduct root, SUSEProduct product) {
        List<SUSEProductSCCRepository> allEntries = SUSEProductFactory.allProductRepos();
        List<Long> repoIdsWithAuth = SCCCachingFactory.lookupRepositoryIdsWithAuth();

        Map<Tuple2<SUSEProduct, SUSEProduct>, List<SUSEProductSCCRepository>> entriesByProducts = allEntries.stream()
                .collect(Collectors.groupingBy(e -> new Tuple2<>(e.getRootProduct(), e.getProduct())));
        return getAvailableRepositories(root, product, entriesByProducts, repoIdsWithAuth);
    }

    /**
     * Find all available repositories for product and all extensions of product
     * @param root root product of product
     * @param product product to get available repositories from
     * @param allEntries lookup map for repositories by product and root product
     * @param repoIdsWithAuth lookup list for all authenticated repositories by id
     * @return stream of available repositories of product
     */
    private Stream<SUSEProductSCCRepository> getAvailableRepositories(SUSEProduct root, SUSEProduct product,
            Map<Tuple2<SUSEProduct, SUSEProduct>, List<SUSEProductSCCRepository>> allEntries,
            List<Long> repoIdsWithAuth) {

            List<SUSEProductSCCRepository> entries =
                    Optional.ofNullable(allEntries.get(new Tuple2<>(root, product)))
                    .orElseGet(Collections::emptyList);
            boolean isAccessible = entries.stream()
                    .filter(e -> e.isMandatory())
                    .allMatch(entry ->
                 entry.getProduct().getChannelFamily().isPublic() &&
                 // isMirrorable
                 repoIdsWithAuth.contains(entry.getRepository().getId())
            );

            log.debug(isAccessible + " " + entries.stream().map(s -> s.getId().toString())
                    .collect(Collectors.joining(",")));

             if (isAccessible) {
                 return Stream.concat(
                     entries.stream(),
                     SUSEProductFactory.findAllExtensionProductsForRootOf(product, root).stream()
                             .flatMap(nextProduct ->
                                     getAvailableRepositories(root, nextProduct, allEntries, repoIdsWithAuth))
                 );
             }
             else {
                 return Stream.empty();
             }
    }

    /**
     * Get a list of all actually available channels based on available channel families
     * as well as some other criteria.
     * @return list of available channels
     */
    public List<SUSEProductSCCRepository> getAvailableChannels() {
        List<SUSEProductSCCRepository> allEntries = SUSEProductFactory.allProductRepos();
        List<Long> repoIdsWithAuth = SCCCachingFactory.lookupRepositoryIdsWithAuth();

        Map<Tuple2<SUSEProduct, SUSEProduct>, List<SUSEProductSCCRepository>> entriesByProducts = allEntries.stream()
                .collect(Collectors.groupingBy(e -> new Tuple2<>(e.getRootProduct(), e.getProduct())));

        return allEntries.stream()
                .filter(e -> e.isRoot())
                .map(e -> e.getProduct())
                .distinct()
                .flatMap(p -> getAvailableRepositories(p, p, entriesByProducts, repoIdsWithAuth))
                .collect(Collectors.toList());
    }

    /**
     * Recreate contents of the suseUpgradePaths table with values from upgrade_paths.json
     * and predecessor_ids from SCC
     *
     * @param products Collection of SCC Products
     * @param upgradePathJsons list of static upgrade paths
     */
    public void updateUpgradePaths(Collection<SCCProductJson> products, List<UpgradePathJson> upgradePathJsons) {
        List<SUSEProduct> allSUSEProducts = SUSEProductFactory.findAllSUSEProducts();
        Map<Long, SUSEProduct> productsById = allSUSEProducts
                .stream().collect(Collectors.toMap(p -> p.getProductId(), p -> p));

        Map<Long, Set<Long>> newPaths = Stream.concat(
                upgradePathJsons.stream().map(u -> new Tuple2<>(u.getFromProductId(), u.getToProductId())),
                products.stream()
                        .flatMap(p -> p.getOnlinePredecessorIds().stream().map(pre -> new Tuple2<>(pre, p.getId())))
        ).collect(Collectors.groupingBy(t -> t.getA(), Collectors.mapping(t -> t.getB(), Collectors.toSet())));

        allSUSEProducts.forEach(p -> {
            Set<SUSEProduct> successors = newPaths.getOrDefault(p.getProductId(), Collections.emptySet()).stream()
                    .flatMap(sId -> Opt.stream(Optional.ofNullable(productsById.get(sId))))
                    .collect(Collectors.toSet());
            Set<SUSEProduct> existingSuccessors = p.getUpgrades();
            existingSuccessors.retainAll(successors);
            existingSuccessors.addAll(successors);
            SUSEProductFactory.save(p);
        });
    }

    /**
     * Return the list of available channels with their status.
     *
     * @return list of channels
     */
    public List<MgrSyncChannelDto> listChannels() {

        List<MgrSyncChannelDto> collect = listProducts().stream().flatMap(p -> {
            return Stream.concat(
                    p.getChannels().stream(),
                    p.getExtensions().stream().flatMap(e -> e.getChannels().stream())
                    );
        }).collect(Collectors.toList());
        return collect;
    }

    private Optional<String> getTokenFromURL(String url) {
        Optional<String> token = Optional.empty();
        Pattern p = Pattern.compile("/?\\?([^?&=]+)$");
        Matcher m = p.matcher(url);
        if (m.find()) {
            token = Optional.of(m.group(1));
        }
        return token;
    }

    /**
     * Update Channel database object with new data from SCC.
     * @param dbChannel channel to update
     */
    public static void updateChannel(Channel dbChannel) {
        if (dbChannel == null) {
            log.error("Channel does not exist");
            return;
        }
        String label = dbChannel.getLabel();
        List<SUSEProductSCCRepository> suseProductSCCRepositories = SUSEProductFactory.lookupPSRByChannelLabel(label);

        Opt.consume(suseProductSCCRepositories.stream().findFirst(),
                () -> log.error("No product tree entry found for label: '" + label + "'"),
                productrepo -> {
                    SUSEProduct product = productrepo.getProduct();

                    // update only the fields which are save to be updated
                    dbChannel.setChannelFamily(product.getChannelFamily());
                    dbChannel.setName(productrepo.getChannelName());
                    dbChannel.setSummary(product.getFriendlyName());
                    dbChannel.setDescription(
                            Optional.ofNullable(product.getDescription())
                                    .orElse(product.getFriendlyName()));
                    dbChannel.setProduct(MgrSyncUtils.findOrCreateChannelProduct(product));
                    dbChannel.setProductName(MgrSyncUtils.findOrCreateProductName(product.getName()));
                    dbChannel.setUpdateTag(productrepo.getUpdateTag());
                    ChannelFactory.save(dbChannel);

                    // update Mandatory Flag
                    dbChannel.getSuseProductChannels().forEach(pc -> {
                        suseProductSCCRepositories.forEach(pr -> {
                            if (pr.getProduct().equals(pc.getProduct()) && pr.isMandatory() != pc.isMandatory()) {
                                pc.setMandatory(pr.isMandatory());
                                SUSEProductFactory.save(pc);
                            }
                        });
                    });
                });
    }

    /**
     * Add a new channel to the database.
     * @param label the label of the channel to be added.
     * @param mirrorUrl repo mirror passed by cli
     * @throws ContentSyncException in case of problems
     */
    public void addChannel(String label, String mirrorUrl) throws ContentSyncException {
        // Return immediately if the channel is already there
        if (ChannelFactory.doesChannelLabelExist(label)) {
            if (log.isDebugEnabled()) {
                log.debug("Channel exists (" + label + "), returning...");
            }
            return;
        }
        List<SUSEProductSCCRepository> suseProductSCCRepositories = SUSEProductFactory.lookupPSRByChannelLabel(label);
        List<SUSEProduct> products = suseProductSCCRepositories.stream()
                .map(s -> s.getProduct()).collect(Collectors.toList());
        SUSEProductSCCRepository productrepo = suseProductSCCRepositories.stream().findFirst().get();
        SUSEProduct product = productrepo.getProduct();

        if (getAvailableRepositories(productrepo.getRootProduct(), product)
                .noneMatch(e -> e.getChannelLabel().equals(label))) {
            throw new ContentSyncException("Channel is not available: " + label);
        }

        SCCRepository repository = productrepo.getRepository();
        if (!repository.isAccessible()) {
            throw new ContentSyncException("Channel is not mirrorable: " + label);
        }

        // Create the channel
        Channel dbChannel = new Channel();
        dbChannel.setBaseDir("/dev/null");
        // from product
        dbChannel.setChannelArch(MgrSyncUtils.getChannelArch(product.getArch(), label));
        dbChannel.setChannelFamily(product.getChannelFamily());
        // Checksum type is only a dummy here. spacewalk-repo-sync will update it
        // and set it to the type used in the (last) repo to hash the primary file
        dbChannel.setChecksumType(ChannelFactory.findChecksumTypeByLabel("sha1"));
        // channel['summary'] = product.get('uiname')
        // channel['description'] = product.find('description').text or channel['summary']
        dbChannel.setLabel(label);
        dbChannel.setName(productrepo.getChannelName());
        dbChannel.setSummary(product.getFriendlyName());
        dbChannel.setDescription(Optional.ofNullable(product.getDescription()).orElse(product.getFriendlyName()));
        dbChannel.setParentChannel(MgrSyncUtils.getChannel(productrepo.getParentChannelLabel()));
        dbChannel.setProduct(MgrSyncUtils.findOrCreateChannelProduct(product));
        dbChannel.setProductName(MgrSyncUtils.findOrCreateProductName(product.getName()));
        dbChannel.setUpdateTag(productrepo.getUpdateTag());

        // Create or link the content source
        Optional<SCCRepositoryAuth> auth = repository.getBestAuth();
        if (auth.isPresent()) {
            String url = contentSourceUrlOverwrite(repository, auth.get().getUrl(), mirrorUrl);
            ContentSource source = ChannelFactory.findVendorContentSourceByRepo(url);
            if (source == null) {
                source = new ContentSource();
                source.setLabel(productrepo.getChannelLabel());
                source.setMetadataSigned(repository.isSigned());
                source.setOrg(null);
                source.setSourceUrl(url);
                source.setType(ChannelFactory.lookupContentSourceType("yum"));
            }
            else {
                // update the URL as the token might have changed
                source.setSourceUrl(url);
            }
            ChannelFactory.save(source);
            dbChannel.getSources().add(source);
            auth.get().setContentSource(source);
        }

        // Save the channel
        ChannelFactory.save(dbChannel);

         // Create the product/channel relations
         for (SUSEProduct p : products) {
             SUSEProductChannel spc = new SUSEProductChannel();
             spc.setProduct(p);
             spc.setChannel(dbChannel);
             spc.setMandatory(productrepo.isMandatory());
             SUSEProductFactory.save(spc);
         }
    }

    /**
     * Check if a given string is a product class representing a system entitlement.
     * @param s string to check if it represents a system entitlement
     * @return true if s is a system entitlement, else false.
     */
    private static boolean isEntitlement(String s) {
        for (SystemEntitlement ent : SystemEntitlement.values()) {
            if (ent.name().equals(s)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Use original product classes and create additional with a suffix.
     * Can be used for ALPHA, BETA, TEST, etc.
     * @param label the label from the original product class
     * @param name the name from the original product class
     * @param suffix a suffix
     * @return the new created or updated channel family
     */
    private ChannelFamily createOrUpdateChannelFamily(String label, String name, String suffix) {
        // to create ALPHA and BETA families
        if (!StringUtils.isBlank(suffix)) {
            label = label + "-" + suffix;
            name = name + " (" + suffix.toUpperCase() + ")";
        }
        return createOrUpdateChannelFamily(label, name, new HashMap<>());
    }

    /**
     * Updates an existing channel family or creates and returns a new one if no channel
     * family exists with the given label.
     * @return {@link ChannelFamily}
     */
    private static ChannelFamily createOrUpdateChannelFamily(String label, String name,
            Map<String, ChannelFamily> channelFamilyByLabel) {
        ChannelFamily family = Optional.ofNullable(channelFamilyByLabel).orElse(new HashMap<>()).get(label);
        if (family == null) {
            family = ChannelFamilyFactory.lookupByLabel(label, null);
        }
        if (family == null && !isEntitlement(label)) {
            family = new ChannelFamily();
            family.setLabel(label);
            family.setOrg(null);
            family.setName(StringUtils.isBlank(name) ? label : name);
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
     * Verify if SCCProductJson has correct data that meets database constraints.
     * @param product {@link SCCProductJson}
     * @return comma separated list of missing attribute names
     */
    private String verifySCCProduct(SCCProductJson product) {
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
     * Check if the given URL can be reached.
     * @param url the url
     * @return Returns true in case we can access this URL, otherwise false
     */
    private boolean accessibleUrl(String url) {
        try {
            URI uri = new URI(url);
            String username = null;
            String password = null;
            if (uri.getUserInfo() != null) {
                String userInfo = uri.getUserInfo();
                username = userInfo.substring(0, userInfo.indexOf(':'));
                password = userInfo.substring(userInfo.indexOf(':') + 1);
            }
            return accessibleUrl(url, username, password);
        }
        catch (URISyntaxException e) {
            log.warn(e.getMessage());
        }
        return false;
    }
    /**
     * Check if the given URL can be reached using provided username and password
     * @param url the url
     * @param user the username
     * @param password the password
     * @return Returns true in case we can access this URL, otherwise false
     */
    private boolean accessibleUrl(String url, String user, String password) {
        try {
            URI uri = new URI(url);

            // SMT doesn't do dir listings, so we try to get the metadata
            Path testUrlPath = new File(StringUtils.defaultString(uri.getRawPath(), "/")).toPath();

            // Build full URL to test
            if (uri.getScheme().equals("file")) {
                return Files.isReadable(testUrlPath);
            }
            else {
                URI testUri = new URI(uri.getScheme(), null, uri.getHost(),
                        uri.getPort(), testUrlPath.toString(), uri.getQuery(), null);
                // Verify the mirrored repo by sending a HEAD request
                int status = MgrSyncUtils.sendHeadRequest(testUri.toString(),
                        user, password).getStatusLine().getStatusCode();
                if (status == HttpURLConnection.HTTP_OK) {
                    return true;
                }
            }
        }
        catch (IOException e) {
            log.warn(e.getMessage());
        }
        catch (URISyntaxException e) {
            log.warn(e.getMessage());
        }
        return false;
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
     * @param label Label
     * @return true if the given label reserved.
     */
    public static boolean isChannelLabelReserved(String label) {
        return SUSEProductFactory.lookupByChannelLabelFirst(label).isPresent();
    }

    /**
     * Returns true if the given name reserved. eg. used by a vendor channel
     *
     * eg: name of vendor channel
     * @param name name
     * @return true if the given name reserved.
     */
    public static boolean isChannelNameReserved(String name) {
        return !SUSEProductFactory.lookupByChannelName(name).isEmpty();
    }
}
