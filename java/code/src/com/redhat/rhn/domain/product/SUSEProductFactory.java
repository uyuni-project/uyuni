/**
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

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.server.InstalledProduct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * SUSEProductFactory - the class used to fetch and store
 * {@link SUSEProduct} objects from the database.
 * @version $Rev$
 */
public class SUSEProductFactory extends HibernateFactory {

    private static Logger log = Logger.getLogger(SUSEProductFactory.class);
    private static SUSEProductFactory singleton = new SUSEProductFactory();

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
     * Insert or update a {@link SUSEUpgradePath}.
     * @param upgradePath upgrade path to be inserted.
     */
    public static void save(SUSEUpgradePath upgradePath) {
        singleton.saveObject(upgradePath);
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
        Collection<Long> ids = CollectionUtils.collect(products, new Transformer() {
            @Override
            public Object transform(Object arg0In) {
                return ((SUSEProduct) arg0In).getId();
            }
        });

        Criteria c = getSession().createCriteria(SUSEProduct.class);
        c.add(Restrictions.not(Restrictions.in("id", ids)));

        for (SUSEProduct product : (List<SUSEProduct>) c.list()) {
            remove(product);
        }
    }

    /**
     * Merge all {@link SUSEUpgradePath} from existing ones
     * and the ones passed as parameter.
     * @param newUpgradePaths the new list of upgradePaths to keep stored
     */
    public static void mergeAllUpgradePaths(Collection<SUSEUpgradePath> newUpgradePaths) {
        List<SUSEUpgradePath> existingUpgradePaths = findAllSUSEUpgradePaths();
        for (SUSEUpgradePath upgradePath : existingUpgradePaths) {
            if (!newUpgradePaths.contains(upgradePath)) {
                SUSEProductFactory.remove(upgradePath);
            }
        }
        for (SUSEUpgradePath upgradePath : newUpgradePaths) {
            if (!existingUpgradePaths.contains(upgradePath)) {
                SUSEProductFactory.save(upgradePath);
            }
        }
    }

    /**
     * Merge all {@link SUSEProductExtension} from existing ones
     * and the ones passed as parameter.
     * @param newProductExtensions the new list of ProductExtensions to keep stored
     */
    public static void mergeAllProductExtension(
            Collection<SUSEProductExtension> newProductExtensions) {
        List<SUSEProductExtension> existingProductExtensions =
                findAllSUSEProductExtensions();
        for (SUSEProductExtension productExtension : existingProductExtensions) {
            if (!newProductExtensions.contains(productExtension)) {
                SUSEProductFactory.remove(productExtension);
            }
        }
        for (SUSEProductExtension productExtension : newProductExtensions) {
            if (!existingProductExtensions.contains(productExtension)) {
                SUSEProductFactory.save(productExtension);
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
     * Delete a {@link SUSEUpgradePath} from the database.
     * @param upgradePath upgrade path to be deleted.
     */
    public static void remove(SUSEUpgradePath upgradePath) {
        singleton.removeObject(upgradePath);
    }

    /**
     * Delete a {@link SUSEProductExtension} from the database.
     * @param productExtension productExtension to be deleted.
     */
    public static void remove(SUSEProductExtension productExtension) {
        singleton.removeObject(productExtension);
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

        Criteria c = getSession().createCriteria(SUSEProduct.class);
        c.add(Restrictions.eq("name", name.toLowerCase()));

        Disjunction versionCriterion = Restrictions.disjunction();
        if (imprecise || version == null) {
            versionCriterion.add(Restrictions.isNull("version"));
        }
        if (version != null) {
            versionCriterion.add(Restrictions.eq("version", version.toLowerCase()));
        }
        c.add(versionCriterion);

        Disjunction releaseCriterion = Restrictions.disjunction();
        if (imprecise || release == null) {
            releaseCriterion.add(Restrictions.isNull("release"));
        }
        if (release != null) {
            releaseCriterion.add(Restrictions.eq("release", release.toLowerCase()));
        }
        c.add(releaseCriterion);

        Disjunction archCriterion = Restrictions.disjunction();
        if (imprecise || arch == null) {
            archCriterion.add(Restrictions.isNull("arch"));
        }
        if (arch != null) {
            archCriterion.add(Restrictions.eq("arch",
                    PackageFactory.lookupPackageArchByLabel(arch)));
        }
        c.add(archCriterion);

        c.addOrder(Order.asc("name")).addOrder(Order.asc("version"))
                .addOrder(Order.asc("release")).addOrder(Order.asc("arch"));

        List<SUSEProduct> result = c.list();
        return result.isEmpty() ? null : result.get(0);
    }

    /**
     * Lookup a {@link SUSEProduct} by a given ID.
     * @param id the id to search for
     * @return the product found
     */
    public static SUSEProduct getProductById(Long id) {
        Session session = HibernateFactory.getSession();
        SUSEProduct p = (SUSEProduct) session.get(SUSEProduct.class, id);
        return p;
    }

    /**
     * Lookup a {@link SUSEProduct} object for given productId.
     * @param productId the product
     * @return SUSE product for given productId
     */
    public static SUSEProduct lookupByProductId(long productId) {
        Session session = getSession();
        Criteria c = session.createCriteria(SUSEProduct.class);
        c.add(Restrictions.eq("productId", productId));
        return (SUSEProduct) c.uniqueResult();
    }

    /**
     * Find all {@link SUSEProductChannel} relationships.
     * @return list of SUSE product channel relationships
     */
    @SuppressWarnings("unchecked")
    public static List<SUSEProductChannel> findAllSUSEProductChannels() {
        Session session = getSession();
        Criteria c = session.createCriteria(SUSEProductChannel.class);
        return c.list();
    }

    /**
     * Find SUSE Product Channel by label and product_id from the product table.
     * @param channelLabel the label of the channel.
     * @param productId product id.
     * @return SUSE Product Channel if it is there.
     */
    public static SUSEProductChannel lookupSUSEProductChannel(
            String channelLabel, Long productId) {

        Criteria c = HibernateFactory.getSession()
                .createCriteria(SUSEProductChannel.class)
                .add(Restrictions.eq("channelLabel", channelLabel))
                .createCriteria("product")
                .add(Restrictions.eq("productId", productId))
                .setFetchMode("product", FetchMode.SELECT);

        return (SUSEProductChannel) c.uniqueResult();
    }

    /**
     * Find all {@link SUSEUpgradePath}.
     * @return list of upgrade paths
     */
    @SuppressWarnings("unchecked")
    public static List<SUSEUpgradePath> findAllSUSEUpgradePaths() {
        Session session = getSession();
        Criteria c = session.createCriteria(SUSEUpgradePath.class);
        return c.list();
    }

    /**
     * Find a {@link SUSEUpgradePath} given by source and target {@link SUSEProduct}s.
     * @param fromProduct the source product
     * @param toProduct the target product
     * @return SUSEUpgradePath if it is there
     */
    public static SUSEUpgradePath findSUSEUpgradePath(SUSEProduct fromProduct,
            SUSEProduct toProduct) {
        Session session = getSession();

        Criteria c = session.createCriteria(SUSEUpgradePath.class)
                .add(Restrictions.eq("fromProduct", fromProduct))
                .add(Restrictions.eq("toProduct", toProduct));

        return (SUSEUpgradePath) c.uniqueResult();
    }

    /**
     * Find all {@link SUSEProductExtension}.
     * @return list of product extension
     */
    @SuppressWarnings("unchecked")
    public static List<SUSEProductExtension> findAllSUSEProductExtensions() {
        Session session = getSession();
        Criteria c = session.createCriteria(SUSEProductExtension.class);
        return c.list();
    }

    /**
     * Find all {@link SUSEProduct}.
     * @return list of all known products
     */
    @SuppressWarnings("unchecked")
    public static List<SUSEProduct> findAllSUSEProducts() {
        return getSession().createCriteria(SUSEProduct.class).list();
    }

    /**
     * Find an {@link InstalledProduct} given by name, version,
     * release, arch and isBaseProduct flag.
     * @param name name
     * @param version version
     * @param release release
     * @param arch arch
     * @param isBaseProduct is base product flag
     * @return installedProduct or null if it is not found
     */
    @SuppressWarnings("unchecked")
    public static Optional<InstalledProduct> findInstalledProduct(String name,
            String version, String release, PackageArch arch, boolean isBaseProduct) {

        Criteria c = getSession().createCriteria(InstalledProduct.class);
        c.add(Restrictions.eq("name", name));
        c.add(Restrictions.eq("version", version));
        c.add(Restrictions.eq("release", release));
        c.add(Restrictions.eq("arch", arch));
        c.add(Restrictions.eq("baseproduct", isBaseProduct));
        c.addOrder(Order.asc("name")).addOrder(Order.asc("version"))
                .addOrder(Order.asc("release")).addOrder(Order.asc("arch"));

        return c.list().stream().findFirst();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return log;
    }

    public static void updateRecommendedExtensions(SCCProduct baseProduct, List<Integer> recommendedExtensions) {
        List<SUSEProductRecommend> newRecommendedExts = new LinkedList<>();
        SUSEProduct bprd = SUSEProductFactory.lookupByProductId(baseProduct.getId());
        for (Integer extid : recommendedExtensions) {
            SUSEProduct ext = SUSEProductFactory.lookupByProductId(extid);
            newRecommendedExts.add(new SUSEProductRecommend(bprd, ext));
        }

        List<SUSEProductRecommend> existingRecommendedProducts = findRecommendedProducts(bprd);
        for (SUSEProductRecommend recommended : existingRecommendedProducts) {
            if (!newRecommendedExts.contains(recommended)) {
                SUSEProductFactory.remove(recommended);
            }
        }
        for (SUSEProductRecommend recommended : newRecommendedExts) {
            if (!existingRecommendedProducts.contains(recommended)) {
                SUSEProductFactory.save(recommended);
            }
        }
    }
}
