/*
 * Copyright (c) 2012--2018 SUSE LLC
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

package com.redhat.rhn.domain.product;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.util.RpmVersionComparator;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.scc.SCCRepository;
import com.redhat.rhn.domain.server.InstalledProduct;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

/**
 * SUSEProductFactory - the class used to fetch and store
 * {@link SUSEProduct} objects from the database.
 */
public class SUSEProductFactory extends HibernateFactory {

    private static Logger log = LogManager.getLogger(SUSEProductFactory.class);
    private static SUSEProductFactory singleton = new SUSEProductFactory();

    private static final RpmVersionComparator RPM_VERSION_COMPARATOR = new RpmVersionComparator();

    private SUSEProductFactory() {
        super();
    }

    /**
     * Insert or update a SUSEProduct.
     * @param product SUSE product to be inserted into the database.
     */
    public static void save(SUSEProduct product) {
        singleton.saveObject(product);
    }

    /**
     * Insert or update a {@link SUSEProductChannel}.
     * @param productChannel SUSE product channel relationship to be inserted.
     */
    public static void save(SUSEProductChannel productChannel) {
        singleton.saveObject(productChannel);
    }

    /**
     * Save a {@link SCCRepository}
     * @param repo the repository
     */
    public static void save(SCCRepository repo) {
        singleton.saveObject(repo);
    }

    /**
     * Save a {@link SUSEProductSCCRepository}
     * @param productRepo the productrepo
     */
    public static void save(SUSEProductSCCRepository productRepo) {
        singleton.saveObject(productRepo);
    }

    /**
     * @return a list of all {@link SUSEProductSCCRepository}
     */
    public static List<SUSEProductSCCRepository> allProductRepos() {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();

        CriteriaQuery<SUSEProductSCCRepository> query
                = cb.createQuery(SUSEProductSCCRepository.class);

        Root<SUSEProductSCCRepository> root = query.from(SUSEProductSCCRepository.class);

        query.select(root);

        return getSession().createQuery(query).getResultList();
    }

    /**
     * @return return true if any products are available, otherwise false
     */
    public static boolean hasProducts() {
        return getSession().createQuery("SELECT count(p) > 0 FROM SUSEProduct p", Boolean.class)
            .uniqueResult();
    }

    /**
     * @return map of all {@link SUSEProductSCCRepository} by ID triple
     */
    public static Map<Tuple3<Long, Long, Long>, SUSEProductSCCRepository> allProductReposByIds() {
        return allProductRepos().stream().collect(Collectors.toMap(
                e -> new Tuple3<>(e.getRootProduct().getProductId(),
                        e.getProduct().getProductId(), e.getRepository().getSccId()),
                e -> e));
    }

    /**
     * Return all {@link SUSEProductSCCRepository} with the given channel label.
     * In most cases the label is unique, but there are exceptions like SLES11 SP1/SP2 base channel
     * and products with rolling releases like CaaSP 1 and 2
     * @param channelLabel the channel label
     * @return list of {@link SUSEProductSCCRepository}
     */
    public static List<SUSEProductSCCRepository> lookupPSRByChannelLabel(
            String channelLabel) {

        CriteriaBuilder cb = getSession().getCriteriaBuilder();

        CriteriaQuery<SUSEProductSCCRepository> query
                = cb.createQuery(SUSEProductSCCRepository.class);

        Root<SUSEProductSCCRepository> root = query.from(SUSEProductSCCRepository.class);

        Predicate predicate = cb.equal(root.get("channelLabel"), channelLabel);

        query.select(root).where(predicate);

        return ((List<SUSEProductSCCRepository>) getSession().createQuery(query)
                .getResultList())
                .stream()
                .sorted((a, b) -> RPM_VERSION_COMPARATOR.compare(
                b.getProduct().getVersion(), a.getProduct().getVersion()))
                .collect(Collectors.toList());
    }

    /**
     * Insert or update a {@link SUSEProductExtension}.
     * @param productExtension migration target to be inserted.
     */
    public static void save(SUSEProductExtension productExtension) {
        singleton.saveObject(productExtension);
    }

    /**
     * Delete a {@link SUSEProduct} from the database.
     * @param product SUSE product to be deleted.
     */
    public static void remove(SUSEProduct product) {
        singleton.removeObject(product);
    }

    /**
     * Removes all products except the ones passed as parameter.
     * @param products products to keep
     */
    @SuppressWarnings("unchecked")
    public static void removeAllExcept(Collection<SUSEProduct> products) {
        if (!products.isEmpty()) {
            Collection<Long> ids
                    = products.stream().map(SUSEProduct::getId).collect(Collectors.toList());

            CriteriaBuilder cb = getSession().getCriteriaBuilder();

            CriteriaQuery<SUSEProduct> query = cb.createQuery(SUSEProduct.class);

            Root<SUSEProduct> root = query.from(SUSEProduct.class);

            Predicate predicate = root.get("id").in(ids);

            query.select(root).where(predicate);

            for (SUSEProduct product : (List<SUSEProduct>) getSession().createQuery(query)
                    .list()) {
                remove(product);
            }
        }
    }


    /**
     * Lookup SUSEProductChannels by channel label
     * @param channelLabel the label
     * @return list of SUSEProductChannels
     */
    public static List<SUSEProductChannel> lookupSyncedProductChannelsByLabel(String channelLabel) {
        return Optional.ofNullable(ChannelFactory.lookupByLabel(channelLabel))
                .map(channel -> channel.getSuseProductChannels().stream())
                .orElseGet(Stream::empty).collect(Collectors.toList());
    }

    /**
     * Lookup SUSEProductChannels by channel label
     * @param channelLabel the label
     * @return list of SUSEProductChannels
     */
    public static List<SUSEProductSCCRepository> lookupByChannelLabel(String channelLabel) {
        Session session = HibernateFactory.getSession();
        return session.getNamedQuery("SUSEProductSCCRepository.lookupByLabel")
                .setParameter("label", channelLabel).list();
    }

    /**
     * Lookup SUSE Product Channels by channel name
     * @param name the channel name
     * @return list of found matches
     */
    public static List<SUSEProductSCCRepository> lookupByChannelName(String name) {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<SUSEProductSCCRepository> criteria = builder.createQuery(SUSEProductSCCRepository.class);
        Root<SUSEProductSCCRepository> root = criteria.from(SUSEProductSCCRepository.class);
        criteria.where(builder.equal(root.get("channelName"), name));
        return getSession().createQuery(criteria).getResultList();
    }

    /**
     * Finds a SUSEProductSCCRepository entry by channel label.
     * Note: this returns the entry with the newest product to make the result predictable.
     * @param channelLabel channel label
     * @return SUSEProductSCCRepository entry with the newest product
     */
    public static Optional<SUSEProductSCCRepository> lookupByChannelLabelFirst(String channelLabel) {
        return  lookupByChannelLabel(channelLabel)
                .stream()
                // sort so we always choose the latest version
                .sorted((a, b) ->  RPM_VERSION_COMPARATOR.compare(b.getProduct().getVersion(),
                        a.getProduct().getVersion()))

                // We take the first item since there can be more than one entry.
                // This only happens for sles11 sp1/2  and rolling release attempts like caasp 1/2
                .findFirst();
    }


    private static Stream<SUSEProductChannel> findSyncedMandatoryChannels(SUSEProduct product, SUSEProduct base,
                                                                      String baseChannelLabel) {
        Stream<SUSEProductChannel> concat = Stream.concat(
                product.getSuseProductChannels().stream().filter(
                        pc -> Optional.ofNullable(pc.getChannel().getParentChannel())
                                .map(c -> c.getLabel().equals(baseChannelLabel))
                                .orElseGet(() -> pc.getChannel().getLabel().equals(baseChannelLabel))
                ),
                SUSEProductFactory.findAllBaseProductsOf(product, base).stream().flatMap(
                        p -> findSyncedMandatoryChannels(p, base, baseChannelLabel)
                )
        );
        return concat;
    }

    private static Optional<SUSEProductChannel> findSyncProductChannelByLabel(String channelLabel) {
        List<SUSEProductChannel> suseProducts = SUSEProductFactory.lookupSyncedProductChannelsByLabel(channelLabel);
        if (suseProducts.isEmpty()) {
            return Optional.empty();
        }
        else {
            // We take the first item since there can be more than one entry.
            // All entries should point to the same "product" with only arch differences.
            // The only exception to this is sles11 sp1/2 but they are out of maintenance
            // and we decided to ignore this inconsistency until the great rewrite.
            return Optional.of(suseProducts.get(0));
        }
    }

    /**
     * Find all synced mandatory channels for the given channel label
     * @param channelLabel the channel label
     * @return a stream of synced {@link Channel}
     */
    public static Stream<Channel> findSyncedMandatoryChannels(String channelLabel) {
        Channel channel = ChannelFactory.lookupByLabel(channelLabel);
        if (channel == null) {
            throw new NoSuchElementException("Broken channel " + channelLabel);
        }
        Channel baseChannel = Optional.ofNullable(channel.getParentChannel()).orElse(channel);
        if (channel.isCloned()) {
            if (ConfigDefaults.get().getClonedChannelAutoSelection()) {
                return channel.originChain().filter(c -> !c.isCloned()).findFirst().map(original -> {
                    // There is a problem that filtering cloned channels by the unique parts does not filter
                    // out chained channels created via CLM. If you have a CLM project based on another CLM
                    // project the names of the chained channels contain all the unique parts and are thus
                    // not filtered out although they should (See bsc#1204270 for more info).
                    List<String> originalParts = List.of(original.getLabel().split("-"));
                    List<String> selectedParts = List.of(channel.getLabel().split("-"));
                    List<String> uniqueParts = selectedParts.stream()
                            .filter(s -> !originalParts.contains(s))
                            .collect(Collectors.toList());

                    if (channel.isBaseChannel()) {
                        return Stream.<Channel>empty();
                    }
                    else {
                        return findSyncedMandatoryChannels(original.getLabel())
                                .flatMap(c -> {
                                    return c.allClonedChannels().filter(clone -> {
                                        List<String> cloneParts = List.of(clone.getLabel().split("-"));
                                        return cloneParts.containsAll(uniqueParts) &&
                                                Objects.equals(clone.getParentChannel(), channel.getParentChannel()) &&
                                                Objects.equals(clone.getOrg(), channel.getOrg());
                                    });
                                })
                                .map(c -> (Channel) c);
                    }
                }).orElse(Stream.empty());
            }
            else {
                return Stream.empty();
            }
        }
        else {
            return findSyncProductChannelByLabel(channelLabel).map(suseProductChannel -> {
                        SUSEProductChannel baseProductChannel = findSyncProductChannelByLabel(baseChannel.getLabel())
                                .orElseThrow(() -> new NoSuchElementException("No product channel found for " +
                                        baseChannel + " of " + channel));
                        Stream<SUSEProductChannel> suseProductChannelStream = findSyncedMandatoryChannels(
                                suseProductChannel.getProduct(),
                                baseProductChannel.getProduct(),
                                baseChannel.getLabel()
                        );
                        return Stream.concat(Stream.of(suseProductChannel), suseProductChannelStream)
                                .filter(pc -> pc.getChannel().getChannelArch().equals(channel.getChannelArch()));
                    }).orElse(Stream.empty())
                    .filter(SUSEProductChannel::isMandatory)
                    .map(SUSEProductChannel::getChannel);

        }
    }

    /**
     * Find all mandatory channels for a given product / root product combination
     * @param product product for which we want the channels
     * @param root root product under which the product sits (this is for disambiguation since a product by itself
     *             can have different channels depending in what root product it sits)
     * @return a stream of SUSEProductSCCRepository since only synced channels have a channel instance
     */
    public static Stream<SUSEProductSCCRepository> findAllMandatoryChannels(SUSEProduct product, SUSEProduct root) {
        return Stream.concat(
                product.getRepositories()
                        .stream()
                        .filter(SUSEProductSCCRepository::isMandatory)
                        .filter(p -> p.getRootProduct().equals(root)),
                SUSEProductFactory.findAllBaseProductsOf(product, root).stream()
                .flatMap(p -> findAllMandatoryChannels(p, root))
        );
    }

    /**
     * Finds the suse product channel for a given channel label.
     * Note: does not work for all channel labels see comment in source.
     * @param channelLabel channel label
     * @return suse product channel
     */
    public static Optional<SUSEProduct> findProductByChannelLabel(String channelLabel) {
        return lookupByChannelLabelFirst(channelLabel)
                .map(SUSEProductSCCRepository::getProduct);
    }

    /**
     * Finds all mandetory channels for a given channel label.
     *
     * @param channelLabel channel label
     * @return a stream of suse product channels which are required by the channel
     */
    public static Stream<SUSEProductSCCRepository> findAllMandatoryChannels(String channelLabel) {
        return lookupByChannelLabelFirst(channelLabel).map(spsr -> findAllMandatoryChannels(
                spsr.getProduct(),
                spsr.getRootProduct()
        )).orElseGet(Stream::empty);
    }

    /**
     * Find not synced mandatory channels for a given channel label and return them as stream
     * @param channelLabel channel label
     * @return stream of required {@link SUSEProductSCCRepository} representing channels
     */
    public static Stream<SUSEProductSCCRepository> findNotSyncedMandatoryChannels(String channelLabel) {
        return findAllMandatoryChannels(channelLabel).
                filter(spsr -> Objects.nonNull(ChannelFactory.lookupByLabel(spsr.getChannelLabel())))
                .sorted(Comparator.comparing(SUSEProductSCCRepository::getParentChannelLabel,
                        Comparator.nullsFirst(Comparator.naturalOrder())));
    }

    /**
     * Merge all {@link SUSEProductExtension} from existing ones
     * and the ones passed as parameter.
     * @param newExtensions the new list of ProductExtensions to keep stored
     */
    public static void mergeAllProductExtension(List<SUSEProductExtension> newExtensions) {
        List<SUSEProductExtension> existingExtensions = findAllSUSEProductExtensions();
        for (SUSEProductExtension existingExtension : existingExtensions) {
            if (!newExtensions.contains(existingExtension)) {
                SUSEProductFactory.remove(existingExtension);
            }
            else {
                SUSEProductExtension newExtension = newExtensions.get(newExtensions.indexOf(existingExtension));
                if (newExtension.isRecommended() != existingExtension.isRecommended()) {
                    existingExtension.setRecommended(newExtension.isRecommended());
                    SUSEProductFactory.save(existingExtension);
                }
            }
        }
        for (SUSEProductExtension newExtension : newExtensions) {
            if (!existingExtensions.contains(newExtension)) {
                SUSEProductFactory.save(newExtension);
            }
        }
    }

    /**
     * Delete a {@link SUSEProductChannel} from the database.
     * @param productChannel SUSE product channel relationship to be deleted.
     */
    public static void remove(SUSEProductChannel productChannel) {
        singleton.removeObject(productChannel);
    }

    /**
     * Delete a {@link SUSEProductExtension} from the database.
     * @param productExtension productExtension to be deleted.
     */
    public static void remove(SUSEProductExtension productExtension) {
        singleton.removeObject(productExtension);
    }

    /**
     * Delete a {@link SUSEProductSCCRepository} from the database.
     * @param productRepo product repository to be deleted.
     */
    public static void remove(SUSEProductSCCRepository productRepo) {
        singleton.removeObject(productRepo);
    }

    /**
     * Find a {@link SUSEProduct} given by name, version, release and arch.
     * @param name name
     * @param version version or null
     * @param release release or null
     * @param arch arch or null
     * @param imprecise if true, allow returning products with NULL name, version or
     * release even if the corresponding parameters are not null
     * @return product or null if it is not found
     */
    @SuppressWarnings("unchecked")
    public static SUSEProduct findSUSEProduct(String name, String version, String release,
            String arch, boolean imprecise) {

        CriteriaBuilder cb = getSession().getCriteriaBuilder();

        CriteriaQuery<SUSEProduct> query = cb.createQuery(SUSEProduct.class);

        Root<SUSEProduct> root = query.from(SUSEProduct.class);

        Predicate predicate = cb.equal(root.get("name"), name.toLowerCase());

        if (imprecise || version == null) {
            predicate = cb.and(predicate, cb.isNull(root.get("version")));
        }
        if (version != null) {
            predicate
                    = cb.and(predicate, cb.equal(root.get("version"), version.toLowerCase()));
        }

        if (imprecise || release == null) {
            predicate = cb.and(predicate, cb.isNull(root.get("release")));
        }
        if (release != null) {
            predicate
                    = cb.and(predicate, cb.equal(root.get("release"), release.toLowerCase()));
        }

        if (imprecise || arch == null) {
            predicate = cb.and(predicate, cb.isNull(root.get("arch")));
        }
        if (arch != null) {
            predicate = cb.and(predicate, cb.equal(root.get("arch"),
                    PackageFactory.lookupPackageArchByLabel(arch)));
        }

        query.select(root).where(predicate);

        query.orderBy(cb.asc(root.get("name")), cb.asc(root.get("version")),
                cb.asc(root.get("release")), cb.asc(root.get("arch")));

        List<SUSEProduct> result = getSession().createQuery(query).getResultList();

        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Lookup a {@link SUSEProduct} by a given ID.
     * @param id the id to search for
     * @return the product found
     */
    public static SUSEProduct getProductById(Long id) {
        Session session = HibernateFactory.getSession();
        SUSEProduct p = session.get(SUSEProduct.class, id);
        return p;
    }

    /**
     * Lookup a {@link SUSEProduct} object for given productId.
     * @param productId the product
     * @return SUSE product for given productId
     */
    public static SUSEProduct lookupByProductId(long productId) {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();

        CriteriaQuery<SUSEProduct> query = cb.createQuery(SUSEProduct.class);

        Root<SUSEProduct> root = query.from(SUSEProduct.class);

        Predicate predicate = cb.equal(root.get("productId"), productId);

        query.select(root).where(predicate);

        return (SUSEProduct) getSession().createQuery(query).uniqueResult();
    }

    /**
     * Lookup all {@link SUSEProduct} objects and provide a map with productId as key.
     * @return map of SUSE products with productId as key
     */
    public static Map<Long, SUSEProduct> productsByProductIds() {
        Session session = getSession();
        return session.createQuery("from com.redhat.rhn.domain.product.SUSEProduct", SUSEProduct.class)
                .stream()
                .collect(Collectors.toMap(SUSEProduct::getProductId, p -> p));
    }

    /**
     * Return all {@link SUSEProductExtension} which are recommended
     * @return SUSEProductExtensions which are recommended
     */
    public static List<SUSEProductExtension> allRecommendedExtensions() {
        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<SUSEProductExtension> criteria = builder.createQuery(SUSEProductExtension.class);
        Root<SUSEProductExtension> root = criteria.from(SUSEProductExtension.class);
        criteria.where(builder.equal(root.get("recommended"), true));
        return getSession().createQuery(criteria).getResultList();
    }

    /**
     * Find extensions for the product
     * @param rootProduct the rootProduct product
     * @param base the base product
     * @param ext the extension product
     * @return the Optional of {@link SUSEProductExtension} product
     */
    public static Optional<SUSEProductExtension> findSUSEProductExtension(
            SUSEProduct rootProduct, SUSEProduct base, SUSEProduct ext) {

        CriteriaBuilder cb = getSession().getCriteriaBuilder();

        CriteriaQuery<SUSEProductExtension> query
                = cb.createQuery(SUSEProductExtension.class);

        Root<SUSEProductExtension> root = query.from(SUSEProductExtension.class);

        Predicate predicate = cb.equal(root.get("rootProduct"), rootProduct);
        predicate = cb.and(predicate, cb.equal(root.get("baseProduct"), base));
        predicate = cb.and(predicate, cb.equal(root.get("extensionProduct"), ext));

        query.select(root).where(predicate);

        SUSEProductExtension result = getSession().createQuery(query).uniqueResult();

        if (result == null) {
            return Optional.empty();
        }
        else {
            return Optional.of(result);
        }
    }

    /**
     * Find all {@link SUSEProductExtension}.
     * @return list of product extension
     */
    @SuppressWarnings("unchecked")
    public static List<SUSEProductExtension> findAllSUSEProductExtensions() {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();

        CriteriaQuery<SUSEProductExtension> query
                = cb.createQuery(SUSEProductExtension.class);

        Root<SUSEProductExtension> root = query.from(SUSEProductExtension.class);

        query.select(root);

        return getSession().createQuery(query).getResultList();

    }

    /**
     * Find all {@link SUSEProductExtension} of a product for the given root product.
     * @param base product to find extensions of
     * @param root root product
     * @return list of product extension of the given product and root
     */
    public static List<SUSEProduct> findAllExtensionProductsForRootOf(SUSEProduct base, SUSEProduct root) {
        Map<String, Object> params = new HashMap<>();
        params.put("baseId", base.getId());
        params.put("rootId", root.getId());
        return singleton.listObjectsByNamedQuery("SUSEProductExtension.findAllExtensionProductsForRootOf", params);
    }

    /**
     * Find all {@link SUSEProductExtension} of a product for a given root.
     * @param product product to find extensions of
     * @param rootProduct root product to find extensions in
     * @return list of product extension of the given product
     */
    @SuppressWarnings("unchecked")
    public static List<SUSEProductExtension> findAllProductExtensionsOf(SUSEProduct product, SUSEProduct rootProduct) {

        CriteriaBuilder cb = getSession().getCriteriaBuilder();

        CriteriaQuery<SUSEProductExtension> query
                = cb.createQuery(SUSEProductExtension.class);

        Root<SUSEProductExtension> root = query.from(SUSEProductExtension.class);

        Predicate predicate = cb.equal(root.get("rootProduct"), rootProduct);
        predicate = cb.and(predicate, cb.equal(root.get("baseProduct"), product));

        query.select(root).where(predicate);

        return getSession().createQuery(query).getResultList();

    }

    /**
     * Find all base products of a product.
     * @param ext product to find bases for
     * @return list of base products of the given product
     */
    public static List<SUSEProduct> findAllBaseProductsOf(SUSEProduct ext) {
        Map<String, Object> params = Map.of("extId", ext.getId());
        return singleton.listObjectsByNamedQuery("SUSEProductExtension.findAllBaseProductsOf", params);
    }

    /**
     * Find all base products of a product in the tree of a specified root product.
     * @param ext product to find bases for
     * @param root the root product
     * @return list of base products of the given product and root
     */
    public static List<SUSEProduct> findAllBaseProductsOf(SUSEProduct ext, SUSEProduct root) {
        Map<String, Object> params = new HashMap<>();
        params.put("extId", ext.getId());
        params.put("rootId", root.getId());
        return singleton.listObjectsByNamedQuery("SUSEProductExtension.findAllBaseProductsForRootOf", params);
    }

    /**
     * Find all root products of a product.
     * @param prd product to find roots for
     * @return list of root products of the given product
     */
    public static List<SUSEProduct> findAllRootProductsOf(SUSEProduct prd) {
        return singleton.listObjectsByNamedQuery("SUSEProductExtension.findAllRootProductsOf",
                Map.of("extId", prd.getId()));
    }

    /**
     * Find all {@link SUSEProduct}.
     * @return list of all known products
     */
    @SuppressWarnings("unchecked")
    public static List<SUSEProduct> findAllSUSEProducts() {
        CriteriaBuilder cb = getSession().getCriteriaBuilder();

        CriteriaQuery<SUSEProduct> query = cb.createQuery(SUSEProduct.class);

        Root<SUSEProduct> root = query.from(SUSEProduct.class);

        query.select(root);

        return getSession().createQuery(query).getResultList();
    }

    /**
     * Find all extensions of a given root product. When the given product
     * is not a root product, the result is empty.
     * @param root the root product
     * @return List of {@link SUSEProduct} extensions
     */
    public static List<SUSEProduct> findAllExtensionsOfRootProduct(SUSEProduct root) {
        return getSession()
                .createNamedQuery("SUSEProductExtension.findAllExtensionsOfRootProduct", SUSEProduct.class)
                .setParameter("rootId", root.getId())
                .list();
    }

    /**
     * Find an {@link InstalledProduct} given by name, version,
     * release, arch and isBaseProduct flag.
     * @param name name
     * @param version version
     * @param release release
     * @param arch arch
     * @param isBaseProduct is base product flag
     * @return {@link Optional} of installed product or {@link Optional#empty()} if not found
     */
    @SuppressWarnings("unchecked")
    public static Optional<InstalledProduct> findInstalledProduct(String name,
            String version, String release, PackageArch arch, boolean isBaseProduct) {

        CriteriaBuilder cb = getSession().getCriteriaBuilder();

        CriteriaQuery<InstalledProduct> query = cb.createQuery(InstalledProduct.class);

        Root<InstalledProduct> root = query.from(InstalledProduct.class);

        Predicate predicate = cb.equal(root.get("name"), name.toLowerCase());
        predicate = cb.and(predicate, cb.equal(root.get("version"), version.toLowerCase()));
        predicate = cb.and(predicate, cb.equal(root.get("arch"), arch));
        predicate = cb.and(predicate, cb.equal(root.get("baseproduct"), isBaseProduct));
        if (StringUtils.isEmpty(release)) {
            predicate = cb.and(predicate, cb.isNull(root.get("release")));
        }
        else {
            predicate = cb.and(predicate, cb.equal(root.get("release"), release));
        }

        query.select(root).where(predicate);
        query.orderBy(cb.asc(root.get("name")), cb.asc(root.get("version")),
                cb.asc(root.get("release")), cb.asc(root.get("arch")));

        return getSession().createQuery(query).getResultList().stream().findFirst();
    }

    /**
     * Find an {@link InstalledProduct} given by a {@link SUSEProduct} data
     * @param product SUSE product
     * @return {@link Optional} of installed product or {@link Optional#empty()} if not found
     */
    public static Optional<InstalledProduct> findInstalledProduct(SUSEProduct product) {
        return findInstalledProduct(product.getName(), product.getVersion(), product.getRelease(), product.getArch(),
                product.isBase());
    }

    /**
     * Get all root SUSE products that support live patching
     *
     * The query selects all the root products that has a live patching extension product.
     * It selects only installed products by checking if they have a channel present.
     *
     * @return the stream of products
     */
    public static Stream<SUSEProduct> getLivePatchSupportedProducts() {
        return HibernateFactory.getSession().createQuery(
                "SELECT root FROM SUSEProduct root " +
                "JOIN SUSEProductExtension x ON x.rootProduct = root " +
                "JOIN SUSEProduct ext ON x.extensionProduct = ext " +
                "JOIN ChannelFamily cf ON ext.channelFamily = cf " +
                "WHERE cf.label LIKE 'SLE-LP%' " +
                "AND EXISTS (FROM SUSEProductChannel WHERE product = root)", SUSEProduct.class)
                .getResultStream();
    }

    /**
     * Get all kernel versions contained in a product tree
     *
     * The query selects the EVRs of every kernel package that is included in any channel of the product tree.
     *
     * @param product the root product
     * @return the stream of EVRs
     */
    public static Stream<PackageEvr> getKernelVersionsInProduct(SUSEProduct product) {
        return HibernateFactory.getSession().createQuery(
                "SELECT DISTINCT pkg.packageEvr " +
                "FROM SUSEProductExtension x " +
                "JOIN SUSEProduct ext ON x.extensionProduct = ext " +
                "JOIN SUSEProductChannel pc ON pc.product = ext " +
                "JOIN pc.channel.packages pkg " +
                "WHERE pkg.packageName.name LIKE 'kernel-default%' " +
                "AND x.rootProduct = :product", PackageEvr.class)
                .setParameter("product", product)
                .getResultStream();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return log;
    }
}
