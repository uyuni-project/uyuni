/*
 * Copyright (c) 2012--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.hibernate.type.StandardBasicTypes;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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
     * Save a {@link ChannelTemplate}
     * @param productRepo the productrepo
     */
    public static void save(ChannelTemplate productRepo) {
        singleton.saveObject(productRepo);
    }

    /**
     * @return a list of all {@link ChannelTemplate}
     */
    public static List<ChannelTemplate> allChannelTemplates() {
        return getSession().createQuery("FROM ChannelTemplate ct", ChannelTemplate.class).getResultList();
    }

    /**
     * @return return true if any products are available, otherwise false
     */
    public static boolean hasProducts() {
        return getSession().createQuery("SELECT count(p) > 0 FROM SUSEProduct p", Boolean.class)
            .uniqueResult();
    }

    /**
     * @return map of all {@link ChannelTemplate} by ID triple
     */
    public static Map<Tuple3<Long, Long, Long>, ChannelTemplate> allChannelTemplatesByIds() {
        return allChannelTemplates().stream().collect(
                Collectors.toMap(
                        e -> new Tuple3<>(
                                e.getRootProduct().getProductId(),
                                e.getProduct().getProductId(),
                                e.getRepository().getSccId()
                        ),
                        e -> e
                )
        );
    }

    /**
     * Return all {@link ChannelTemplate} with the given channel label.
     * In most cases the label is unique, but there are exceptions like SLES11 SP1/SP2 base channel
     * and products with rolling releases like CaaSP 1 and 2
     * @param channelLabel the channel label
     * @return list of {@link ChannelTemplate}
     */
    public static List<ChannelTemplate> lookupChannelTemplateByChannelLabel(String channelLabel) {
        return getSession().createQuery("FROM ChannelTemplate pr WHERE pr.channelLabel = :label",
                        ChannelTemplate.class)
                .setParameter("label", channelLabel)
                .stream()
                .sorted((a, b) ->
                        RPM_VERSION_COMPARATOR.compare(b.getProduct().getVersion(), a.getProduct().getVersion()))
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
    public static void removeAllExcept(Collection<SUSEProduct> products) {
        if (!products.isEmpty()) {
            List<Long> ids = products.stream().map(SUSEProduct::getId).collect(Collectors.toList());

            List<SUSEProduct> productIds =  getSession().createNativeQuery("""
                                      SELECT * from suseProducts
                                      WHERE id NOT IN (:ids)
                                      """, SUSEProduct.class)
                    .setParameterList("ids", ids, StandardBasicTypes.LONG)
                    .getResultList();

            for (SUSEProduct product : productIds) {
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
     * Lookup SUSE Product Channels by channel name
     * @param name the channel name
     * @return list of found matches
     */
    public static List<ChannelTemplate> lookupByChannelName(String name) {
        return getSession().createQuery("FROM ChannelTemplate ct WHERE ct.channelName = :name", ChannelTemplate.class)
                .setParameter("name", name)
                .getResultList();
    }

    /**
     * Finds a ChannelTemplate entry by channel label.
     * Note: this returns the entry with the newest product to make the result predictable.
     * @param channelLabel channel label
     * @return ChannelTemplate entry with the newest product
     */
    public static Optional<ChannelTemplate> lookupByChannelLabelFirst(String channelLabel) {
        // We take the first item since there can be more than one entry.
        // This only happens for sles11 sp1/2  and rolling release attempts like caasp 1/2
        return  lookupChannelTemplateByChannelLabel(channelLabel).stream().findFirst();
    }


    private static Stream<SUSEProductChannel> findSyncedMandatoryChannels(SUSEProduct product, SUSEProduct base,
                                                                      String baseChannelLabel) {
        return Stream.concat(
                product.getSuseProductChannels().stream().filter(
                        pc -> Optional.ofNullable(pc.getChannel().getParentChannel())
                                .map(c -> c.getLabel().equals(baseChannelLabel))
                                .orElseGet(() -> pc.getChannel().getLabel().equals(baseChannelLabel))
                ),
                SUSEProductFactory.findAllBaseProductsOf(product, base).stream().flatMap(
                        p -> findSyncedMandatoryChannels(p, base, baseChannelLabel)
                )
        );
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
                            .toList();

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
     * @return a stream of ChannelTemplate since only synced channels have a channel instance
     */
    public static Stream<ChannelTemplate> findAllMandatoryChannels(SUSEProduct product, SUSEProduct root) {
        return Stream.concat(
                product.getChannelTemplates()
                        .stream()
                        .filter(ChannelTemplate::isMandatory)
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
                .map(ChannelTemplate::getProduct);
    }

    /**
     * Finds all mandetory channels for a given channel label.
     *
     * @param channelLabel channel label
     * @return a stream of suse product channels which are required by the channel
     */
    public static Stream<ChannelTemplate> findAllMandatoryChannels(String channelLabel) {
        return lookupByChannelLabelFirst(channelLabel).map(spsr -> findAllMandatoryChannels(
                spsr.getProduct(),
                spsr.getRootProduct()
        )).orElseGet(Stream::empty);
    }

    /**
     * Find not synced mandatory channels for a given channel label and return them as stream
     * @param channelLabel channel label
     * @return stream of required {@link ChannelTemplate} representing channels
     */
    public static Stream<ChannelTemplate> findNotSyncedMandatoryChannels(String channelLabel) {
        return findAllMandatoryChannels(channelLabel).
                filter(spsr -> Objects.nonNull(ChannelFactory.lookupByLabel(spsr.getChannelLabel())))
                .sorted(Comparator.comparing(ChannelTemplate::getParentChannelLabel,
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
     * Delete a {@link ChannelTemplate} from the database.
     * @param productRepo product repository to be deleted.
     */
    public static void remove(ChannelTemplate productRepo) {
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

        StringBuilder sqlQuery = new StringBuilder("SELECT * FROM suseProducts WHERE LOWER(name) = :name");

        if (version == null) {
            sqlQuery.append(" AND version is NULL");
        }
        else if (imprecise) {
            sqlQuery.append(" AND (version IS NULL OR LOWER(version) = :version)");
        }
        else { // (!imprecise)
            sqlQuery.append(" AND LOWER(version) = :version");
        }

        if (release == null) {
            sqlQuery.append(" AND release is NULL");
        }
        else if (imprecise) {
            sqlQuery.append(" AND (release IS NULL OR LOWER(release) = :release)");
        }
        else { // (!imprecise)
            sqlQuery.append(" AND LOWER(release) = :release");
        }

        PackageArch parch = PackageFactory.lookupPackageArchByLabel(arch);
        Long archTypeId = (long) -1;
        if (parch != null) {
            archTypeId = parch.getId();
        }
        if (imprecise || archTypeId == -1) {
            sqlQuery.append(" AND (arch_type_id IS NULL OR arch_type_id = :arch)");
        }
        else {
            sqlQuery.append(" AND arch_type_id = :arch");
        }

        // Add ordering
        sqlQuery.append(" ORDER BY name ASC, version ASC, release ASC, arch_type_id ASC");

        // Execute the query
        Query<SUSEProduct> query = getSession().createNativeQuery(sqlQuery.toString(), SUSEProduct.class)
                .setParameter("name", name.toLowerCase(), StandardBasicTypes.STRING)
                .setParameter("arch", archTypeId, StandardBasicTypes.LONG);
        if (version != null) {
            query.setParameter("version", version.toLowerCase(), StandardBasicTypes.STRING);
        }
        if (release != null) {
            query.setParameter("release", release.toLowerCase(), StandardBasicTypes.STRING);
        }


        List<SUSEProduct> result = query.getResultList();
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Lookup a {@link SUSEProduct} by a given ID.
     * @param id the id to search for
     * @return the product found
     */
    public static SUSEProduct getProductById(Long id) {
        Session session = HibernateFactory.getSession();
        return session.get(SUSEProduct.class, id);
    }

    /**
     * Lookup a {@link SUSEProduct} object for given productId.
     * @param productId the product
     * @return SUSE product for given productId
     */
    public static SUSEProduct lookupByProductId(long productId) {
        return getSession().createNativeQuery("""
                                  SELECT * from suseProducts
                                  WHERE product_id = :product
                                  """, SUSEProduct.class)
                .setParameter("product", productId, StandardBasicTypes.LONG)
                .uniqueResult();
    }

    /**
     * Lookup all {@link SUSEProduct} objects and provide a map with productId as key.
     * @return map of SUSE products with productId as key
     */
    public static Map<Long, SUSEProduct> productsByProductIds() {
        return getSession().createNativeQuery("SELECT * from suseProducts ", SUSEProduct.class)
                .getResultList()
                .stream()
                .collect(Collectors.toMap(SUSEProduct::getProductId, p -> p));
    }

    /**
     * Return all {@link SUSEProductExtension} which are recommended
     * @return SUSEProductExtensions which are recommended
     */
    public static List<SUSEProductExtension> allRecommendedExtensions() {
        return getSession().createNativeQuery("""
                                      SELECT * from suseProductExtension
                                      WHERE recommended = 'Y'
                                      """, SUSEProductExtension.class)
                .getResultList();
    }

    /**
     * Return all {@link SUSEProductExtension} of the given root product which are recommended
     * @param root the root product
     * @return SUSEProductExtensions which are recommended for the given root product
     */
    public static List<SUSEProductExtension> allRecommendedExtensionsOfRoot(SUSEProduct root) {
        return getSession().createQuery("""
                FROM SUSEProductExtension
               WHERE recommended = true
                 AND rootProduct = :root
               """, SUSEProductExtension.class)
                .setParameter("root", root)
                .list();
    }

    /**
     * Return all {@link SUSEProductExtension} of the given root product
     * @param root the root product
     * @return SUSEProductExtensions for the given root product
     */
    public static List<SUSEProductExtension> allExtensionsOfRoot(SUSEProduct root) {
        return getSession().createQuery("""
                FROM SUSEProductExtension
               WHERE rootProduct = :root
               """, SUSEProductExtension.class)
            .setParameter("root", root)
            .list();
    }

    /**
     * Find all {@link SUSEProductChannel} relationships.
     * @return list of SUSE product channel relationships
     */
    public static List<SUSEProductChannel> findAllSUSEProductChannels() {
        return getSession().createNativeQuery("SELECT * from SUSEProductChannel", SUSEProductChannel.class)
                .getResultList();
    }

    /**
     * Find extensions for the product
     * @param root the root product
     * @param base the base product
     * @param ext the extension product
     * @return the Optional of {@link SUSEProductExtension} product
     */
    public static Optional<SUSEProductExtension> findSUSEProductExtension(SUSEProduct root,
                                                           SUSEProduct base,
                                                           SUSEProduct ext) {
        return getSession().createNativeQuery("""
                                  SELECT * from suseProductExtension
                                  WHERE base_pdid = :baseid
                                  AND ext_pdid = :extid
                                  AND root_pdid = :rootid
                                  """, SUSEProductExtension.class)
                .setParameter("baseid", base.getId(), StandardBasicTypes.LONG)
                .setParameter("extid", ext.getId(), StandardBasicTypes.LONG)
                .setParameter("rootid", root.getId(), StandardBasicTypes.LONG)
                .uniqueResultOptional();
    }

    /**
     * Find all {@link SUSEProductExtension}.
     * @return list of product extension
     */
    public static List<SUSEProductExtension> findAllSUSEProductExtensions() {
        return getSession().createNativeQuery("SELECT * from suseProductExtension", SUSEProductExtension.class)
                .getResultList();
    }

    /**
     * Find all {@link SUSEProductExtension} of a product for the given root product.
     * @param base product to find extensions of
     * @param root root product
     * @return list of product extension of the given product and root
     */
    public static List<SUSEProduct> findAllExtensionProductsForRootOf(SUSEProduct base, SUSEProduct root) {
        return getSession().createQuery("""
                SELECT ext
                FROM   SUSEProductExtension pe
                JOIN   SUSEProduct ext ON pe.extensionProduct = ext
                WHERE  pe.baseProduct = :base
                AND    pe.rootProduct = :root
                """, SUSEProduct.class)
                .setParameter("base", base)
                .setParameter("root", root)
                .list();
    }

    /**
     * Find all {@link SUSEProductExtension} of a product for a given root.
     * @param product product to find extensions of
     * @param root root product to find extensions in
     * @return list of product extension of the given product
     */
    public static List<SUSEProductExtension> findAllProductExtensionsOf(SUSEProduct product, SUSEProduct root) {
        return getSession().createNativeQuery("""
                                      SELECT * from suseProductExtension
                                      WHERE base_pdid = :baseid
                                      AND root_pdid = :rootid
                                      """, SUSEProductExtension.class)
                    .setParameter("baseid", product.getId(), StandardBasicTypes.LONG)
                    .setParameter("rootid", root.getId(), StandardBasicTypes.LONG)
                    .getResultList();
    }

    /**
     * Find all base products of a product in the tree of a specified root product.
     * @param ext product to find bases for
     * @param root the root product
     * @return list of base products of the given product and root
     */
    public static List<SUSEProduct> findAllBaseProductsOf(SUSEProduct ext, SUSEProduct root) {
        return getSession().createQuery("""
                SELECT base
                FROM   SUSEProductExtension pe
                JOIN   SUSEProduct base ON base = pe.baseProduct
                WHERE  pe.extensionProduct = :ext
                AND    pe.rootProduct = :root
                """, SUSEProduct.class)
                .setParameter("ext", ext)
                .setParameter("root", root)
                .list();
    }

    /**
     * Find all root products of a product.
     * @param prd product to find roots for
     * @return list of root products of the given product
     */
    public static List<SUSEProduct> findAllRootProductsOf(SUSEProduct prd) {
        return getSession().createQuery("""
                SELECT root
                FROM SUSEProductExtension pe
                JOIN SUSEProduct root ON pe.rootProduct = root
                where pe.extensionProduct = :prd
                """, SUSEProduct.class)
                .setParameter("prd", prd)
                .list();
    }

    /**
     * Find all {@link SUSEProduct}.
     * @return list of all known products
     */
    public static List<SUSEProduct> findAllSUSEProducts() {
        return getSession().createQuery("FROM SUSEProduct", SUSEProduct.class)
                .getResultList();
    }

    /**
     * Find all extensions of a given root product. When the given product
     * is not a root product, the result is empty.
     * @param root the root product
     * @return List of {@link SUSEProduct} extensions
     */
    public static List<SUSEProduct> findAllExtensionsOfRootProduct(SUSEProduct root) {
        return getSession().createQuery("""
                SELECT ext
                FROM SUSEProductExtension e
                JOIN SUSEProduct ext ON e.extensionProduct = ext
                WHERE e.rootProduct = :root
                """, SUSEProduct.class)
                .setParameter("root", root)
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


        StringBuilder sqlQuery = new StringBuilder("SELECT * FROM suseInstalledProduct WHERE LOWER(name) = :name");

        if (version == null) {
            sqlQuery.append(" AND (version IS NULL OR LOWER(version) = :version");
            version = "";
        }
        else {
            sqlQuery.append(" AND LOWER(version) = :version");
        }


        if (release == null) {
            sqlQuery.append(" AND (release IS NULL OR LOWER(release) = :release)");
            release = "";
        }
        else {
            sqlQuery.append(" AND LOWER(release) = :release");
        }

        Long archTypeId = (long) -1;
        if (arch != null) {
            archTypeId = arch.getId();
        }
        sqlQuery.append(" AND arch_type_id = :arch");

        if (isBaseProduct) {
            sqlQuery.append(" AND is_baseproduct = 'Y'");
        }
        else {
            sqlQuery.append(" AND is_baseproduct = 'N'");
        }


        // Add ordering
        sqlQuery.append(" ORDER BY name ASC, version ASC, release ASC, arch_type_id ASC");

        // Execute the query
        Query<InstalledProduct> query = getSession().createNativeQuery(sqlQuery.toString(), InstalledProduct.class)
                .setParameter("name", name.toLowerCase(), StandardBasicTypes.STRING)
                .setParameter("version", version.toLowerCase(), StandardBasicTypes.STRING)
                .setParameter("release", release.toLowerCase(), StandardBasicTypes.STRING)
                .setParameter("arch", archTypeId, StandardBasicTypes.LONG);

        List<InstalledProduct> result = query.getResultList();

        return result.stream().findFirst();
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
        return getSession().createQuery("""
                SELECT root FROM SUSEProduct root
                JOIN SUSEProductExtension x ON x.rootProduct = root
                JOIN SUSEProduct ext ON x.extensionProduct = ext
                JOIN ChannelFamily cf ON ext.channelFamily = cf
                WHERE cf.label LIKE 'SLE-LP%'
                AND EXISTS (FROM SUSEProductChannel WHERE product = root)
                """, SUSEProduct.class)
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
                "JOIN SUSEProduct ext ON x.baseProduct = ext " +
                "JOIN SUSEProductChannel pc ON pc.product = ext " +
                "JOIN pc.channel.packages pkg " +
                "WHERE pkg.packageName.name = 'kernel-default' " +
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
