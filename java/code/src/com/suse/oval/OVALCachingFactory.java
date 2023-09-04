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
import com.redhat.rhn.common.hibernate.HibernateFactory;

import com.suse.oval.manager.OVALLookupHelper;
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
    private static OVALCachingFactory instance = new OVALCachingFactory();

    private OVALCachingFactory() {
        // Left empty on purpose
    }

    public static void savePlatformsVulnerablePackages(OvalRootType rootType) {
        CallableMode mode = ModeFactory.getCallableMode("oval_queries", "add_product_vulnerable_package");

        OVALLookupHelper ovalLookupHelper = new OVALLookupHelper(rootType);

        DataResult<Map<String, Object>> batch = new DataResult<>(new ArrayList<>(1000));

        for (DefinitionType definition : rootType.getDefinitions()) {
            VulnerablePackagesExtractor vulnerablePackagesExtractor =
                    VulnerablePackagesExtractors.create(definition, rootType.getOsFamily(), ovalLookupHelper);

            List<ProductVulnerablePackages> extractionResult = vulnerablePackagesExtractor.extract();
            for (ProductVulnerablePackages productVulnerablePackages : extractionResult) {
                for (String cve : productVulnerablePackages.getCves()) {
                    for (VulnerablePackage vulnerablePackage : productVulnerablePackages.getVulnerablePackages()) {
                        Map<String, Object> params = new HashMap<>();
                        params.put("product_name", productVulnerablePackages.getProductCpe());
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
        }

        mode.getQuery().executeBatchUpdates(new DataResult<>(batch));

        LOG.warn("Ending...");
    }

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

    public static boolean canAuditCVE(String cve) {
        SelectMode m = ModeFactory.getMode("oval_queries", "can_audit_cve");
        Map<String, Object> params = new HashMap<>();
        params.put("cve_name", cve);

        DataResult result = m.execute(params);

        return !result.isEmpty();
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
