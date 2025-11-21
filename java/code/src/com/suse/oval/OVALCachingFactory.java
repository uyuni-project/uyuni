/*
 * Copyright (c) 2023 SUSE LLC
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

package com.suse.oval;

import com.redhat.rhn.common.db.datasource.CallableMode;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.manager.audit.CVEAuditManagerOVAL;

import com.suse.oval.manager.OVALResourcesCache;
import com.suse.oval.ovaltypes.DefinitionType;
import com.suse.oval.ovaltypes.OvalRootType;
import com.suse.oval.vulnerablepkgextractor.ProductVulnerablePackages;
import com.suse.oval.vulnerablepkgextractor.VulnerablePackage;
import com.suse.oval.vulnerablepkgextractor.VulnerablePackagesExtractor;
import com.suse.oval.vulnerablepkgextractor.VulnerablePackagesExtractors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OVALCachingFactory extends HibernateFactory {
    private static final Logger LOG = LogManager.getLogger(OVALCachingFactory.class);

    private OVALCachingFactory() {
        // Left empty on purpose
    }

    /**
     * Clears the OVAL metadata for the given OS product, meaning:
     * <ul>
     *     <li>Clears all entries in the suseOVALPlatformVulnerablePackage table that correspond
     *     to the given OS product.</li>
     *     <li>Deletes the OS product from the suseOVALOsProduct table.</li>
     * </ul>
     *
     * @param osProduct the OS product to clear the OVAL metadata for.
     * */
    public static void clearOVALMetadataByOsProduct(CVEAuditManagerOVAL.OVALOsProduct osProduct) {
        WriteMode mode = ModeFactory.getWriteMode("oval_queries", "clear_oval_metadata_by_os_product");
        Map<String, String> params = new HashMap<>();
        params.put("os_product_family", osProduct.getOsFamily().toString());
        params.put("os_product_version", osProduct.getOsVersion());
        mode.executeUpdate(params);
    }

    /**
     * Extracts and save the list of vulnerable packages from {@code rootType}
     *
     * @param rootType the OVAL root to extract from
     * */
    public static void savePlatformsVulnerablePackages(OvalRootType rootType) {
        CallableMode mode = ModeFactory.getCallableMode("oval_queries", "add_product_vulnerable_package");

        OVALResourcesCache ovalResourcesCache = new OVALResourcesCache(rootType);
        CVEAuditManagerOVAL.OVALOsProduct ovalOsProduct =
            new CVEAuditManagerOVAL.OVALOsProduct(rootType.getOsFamily(), rootType.getOsVersion());

        List<ProductVulnerablePackages> productVulnerablePackages = new ArrayList<>();
        for (DefinitionType definition : rootType.getDefinitions()) {
            VulnerablePackagesExtractor vulnerablePackagesExtractor =
                    VulnerablePackagesExtractors.create(definition, rootType.getOsFamily(), ovalResourcesCache);

            productVulnerablePackages.addAll(vulnerablePackagesExtractor.extract());
        }

        // Write OVAL metadata in batches
        DataResult<Map<String, Object>> batch = new DataResult<>(new ArrayList<>(1000));
        for (ProductVulnerablePackages pvp : productVulnerablePackages) {
            for (String cve : pvp.getCves()) {
                for (VulnerablePackage vulnerablePackage : pvp.getVulnerablePackages()) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("os_product_family", ovalOsProduct.getOsFamily().toString());
                    params.put("os_product_version", ovalOsProduct.getOsVersion());
                    params.put("platform_cpe", pvp.getProductCpe());
                    params.put("cve_name", cve);
                    params.put("package_name", vulnerablePackage.getName());
                    params.put("fix_version", vulnerablePackage.getFixVersion().orElse(null));

                    batch.add(params);

                    if (batch.size() % 1000 == 0) {
                        mode.getQuery().executeBatchUpdates(batch);
                        batch.clear();
                        commitTransaction();

                        Session session = getSession();
                        if (!inTransaction()) {
                            session.beginTransaction();
                        }
                    }
                }
            }
        }

        mode.getQuery().executeBatchUpdates(new DataResult<>(batch));
    }

    /**
     * Lookup the list of vulnerable packages by the pair of cpe and cve
     *
     * @param cve the cve
     * @param productCpe the product cpe
     * @return the list of vulnerable packages
     * */
    public static List<VulnerablePackage> getVulnerablePackagesByProductAndCve(String productCpe, String cve) {
        SelectMode mode = ModeFactory.getMode("oval_queries", "get_vulnerable_packages");

        Map<String, Object> params = new HashMap<>();
        params.put("cve_name", cve);
        params.put("product_cpe", productCpe);

        DataResult<Row> result = mode.execute(params);

        return result.stream().map(row -> {
            VulnerablePackage vulnerablePackage = new VulnerablePackage();
            vulnerablePackage.setName((String) row.get("vulnerablepkgname"));
            vulnerablePackage.setFixVersion((String) row.get("vulnerablepkgfixversion"));
            return vulnerablePackage;
        }).collect(Collectors.toList());
    }

    /**
     * Verify the presence of OVAL data in the database for the given CVE to determine whether an audit of the CVE
     * can be conducted.
     *
     * @param cve the cve to check for
     * @return {@code True} if we can audit for CVE and {@code False} otherwise
     * */
    public static boolean canAuditCVE(String cve) {
        SelectMode m = ModeFactory.getMode("oval_queries", "can_audit_cve");
        Map<String, Object> params = new HashMap<>();
        params.put("cve_name", cve);

        DataResult<Row> result = m.execute(params);

        return !result.isEmpty();
    }

    /**
     * Check if we have any OVAL vulnerability records for the given client OS in the database.
     *
     * @param cpe the cpe representing of the OS of servers to check for
     * @return {@code True} if OVAL is available for servers with {@code cpe} and {@code False} otherwise.
     */
    public static boolean checkOVALAvailability(String cpe) {
        SelectMode m = ModeFactory.getMode("oval_queries", "check_oval_availability");
        Map<String, Object> params = new HashMap<>();
        params.put("cpe", cpe);

        DataResult<Integer> result = m.execute(params);

        return !result.isEmpty();
    }

    /**
     * Check if we have any erratas assigned to the client's CVE channels.
     *
     * @param serverId the id of the client to check for
     * @return {@code True} if the CVE channels of the server contains erratas. and {@code False} otherwise.
     * */
    public static boolean checkChannelsErrataAvailability(Long serverId) {
        SelectMode m = ModeFactory.getMode("oval_queries", "check_errata_availability");
        Map<String, Object> params = new HashMap<>();
        params.put("server_id", serverId);

        DataResult<Integer> result = m.execute(params);

        return !result.isEmpty();
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
