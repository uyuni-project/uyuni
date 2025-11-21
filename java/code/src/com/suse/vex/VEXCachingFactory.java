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

package com.suse.vex;

import com.redhat.rhn.common.db.datasource.*;
import com.redhat.rhn.common.hibernate.HibernateFactory;

import com.suse.oval.vulnerablepkgextractor.VulnerablePackage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Provides access to VEX data stored in the database.
 */
public class VEXCachingFactory extends HibernateFactory {
    private static final Logger LOG = LogManager.getLogger(VEXCachingFactory.class);

    private VEXCachingFactory() {
        // Utility class
    }

    /**
     * Lookup vulnerable packages by product CPE and CVE name.
     *
     * @param productCpe the product's CPE
     * @param cve the CVE name
     * @return list of vulnerable packages
     */
    public static List<VulnerablePackage> getVulnerablePackagesByProductAndCve(String productCpe, String cve) {
        SelectMode mode = ModeFactory.getMode("vex_queries", "get_vulnerable_packages");

        Map<String, Object> params = new HashMap<>();
        params.put("product_cpe", productCpe);
        params.put("cve_name", cve);

        DataResult<Row> result = mode.execute(params);

        return result.stream().map(row -> {
            VulnerablePackage pkg = new VulnerablePackage();
            pkg.setName((String) row.get("vulnerablepkgname"));
            pkg.setFixVersion((String) row.get("vulnerablepkgfixversion"));
            return pkg;
        }).collect(Collectors.toList());
    }

    /**
     * Check if we can audit this CVE with VEX data.
     *
     * @param cve the CVE name
     * @return true if we can audit
     */
    public static boolean canAuditCVE(String cve) {
        SelectMode m = ModeFactory.getMode("vex_queries", "can_audit_cve");

        Map<String, Object> params = Map.of("cve_name", cve);
        DataResult<Row> result = m.execute(params);

        return !result.isEmpty();
    }

    /**
     * Check if VEX annotations are available for a given product CPE.
     *
     * @param cpe the product's CPE
     * @return true if data is available
     */
    public static boolean checkVEXAvailability(String cpe) {
        SelectMode m = ModeFactory.getMode("vex_queries", "check_vex_availability");

        Map<String, Object> params = Map.of("cpe", cpe);
        DataResult<Row> result = m.execute(params);

        return !result.isEmpty();
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
