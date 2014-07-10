/**
 * Copyright (c) 2012 Novell
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

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.Server;

import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * Delete a {@link SUSEProductChannel} from the database.
     * @param productChannel SUSE product channel relationship to be deleted.
     */
    public static void remove(SUSEProductChannel productChannel) {
        singleton.removeObject(productChannel);
    }

    /**
     * Return a {@link SUSEProductSet} containing all products installed on a server.
     * @param server server
     * @return products installed on the given server
     */
    @SuppressWarnings("unchecked")
    public static SUSEProductSet getInstalledProducts(Server server) {
        SUSEProductSet products = new SUSEProductSet();

        // Find base product
        SelectMode m = ModeFactory.getMode("System_queries",
                "system_installed_base_product");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("sid", server.getId());
        DataResult<Map<String, String>> result = m.execute(params);
        if (result.size() > 0) {
            Map<String, String> row = result.get(0);
            SUSEProduct baseProduct = findSUSEProduct(row.get("name"), row.get("version"),
                    row.get("release"), row.get("arch"));
            products.setBaseProduct(baseProduct);
        }

        // Find addon products
        m = ModeFactory.getMode("System_queries", "system_installed_child_products");
        result = m.execute(params);
        for (Map<String, String> row : result) {
            SUSEProduct childProduct = findSUSEProduct(row.get("name"), row.get("version"),
                    row.get("release"), row.get("arch"));
            // Ignore unknown addon products
            if (childProduct != null) {
                products.addAddonProduct(childProduct);
            }
        }

        return products;
    }

    /**
     * Find a {@link SUSEProduct} given by name, version, release and arch.
     * @param name name
     * @param version version
     * @param release release
     * @param arch arch
     * @return product
     */
    @SuppressWarnings("unchecked")
    public static SUSEProduct findSUSEProduct(String name, String version, String release,
            String arch) {
        SUSEProduct product = null;
        SelectMode mode = ModeFactory.getMode("System_queries", "find_suse_product_id");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", name.toLowerCase());
        params.put("version", version != null ? version.toLowerCase() : "");
        params.put("release", release != null ? release.toLowerCase() : "");
        params.put("arch", arch != null ? arch.toLowerCase() : "");
        DataResult<Map<String, Object>> result = mode.execute(params);
        if (!result.isEmpty()) {
            Map<String, Object> firstrow = result.get(0);
            product = getProductById((Long) firstrow.get("id"));
        }
        return product;
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
     * @return SUSE product for given productId
     */
    public static SUSEProduct lookupByProductId(int productId) {
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
     * {@inheritDoc}
     */
    @Override
    protected Logger getLogger() {
        return log;
    }
}
