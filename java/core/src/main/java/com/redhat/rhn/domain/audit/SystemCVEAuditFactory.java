/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.redhat.rhn.domain.audit;

import static com.redhat.rhn.manager.audit.CVEAuditManager.PREDECESSOR_PRODUCT_RANK_BOUNDARY;
import static com.redhat.rhn.manager.audit.CVEAuditManager.SUCCESSOR_PRODUCT_RANK_BOUNDARY;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.manager.audit.PatchStatus;

import com.suse.manager.utils.PagedSqlQueryBuilder;
import com.suse.manager.utils.PagedSqlQueryBuilder.FilterWithValue;
import com.suse.oval.OvalSeverity;

import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.FlushModeType;
import jakarta.persistence.Tuple;

/**
 * Queries computing the CVEs affecting a single system from OVAL data and the CVE channels of the system.
 */
public class SystemCVEAuditFactory {

    /** UI column key to SQL column */
    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "cveName", "cve_number",
            "status", "status_rank",
            "severity", "severity_rank"
    );

    private static final String DEFAULT_SORT_COLUMN = "cve_number";

    private static final String FILTER_COLUMN = "cve_name";

    /**
     * OVAL platforms matching the server: the ones with the exact CPE of the server or of one of its installed
     * products (modules), or, if there is none, the ones matched by the CVE audit CPE prefix rule.
     * <p>
     * Kept inside the page query on purpose: with the platform ids passed as literals, PostgreSQL switches the
     * join on suseOVALVulnerablePackage (millions of rows) from index lookups to a sequential scan.
     */
    private static final String PLATFORM_CTES = """
            product_cpe AS (
                SELECT s.cpe
                FROM rhnServer s
                WHERE s.id = :sid AND s.cpe IS NOT NULL
                UNION
                SELECT 'cpe:/o:suse:' || lower(ip.name) || ':' || split_part(ip.version, '.', 1)
                       || CASE WHEN split_part(ip.version, '.', 2) IN ('', '0') THEN ''
                               ELSE ':sp' || split_part(ip.version, '.', 2) END
                FROM suseServerInstalledProduct sip
                JOIN suseInstalledProduct ip
                  ON ip.id = sip.suse_installed_product_id
                WHERE sip.rhn_server_id = :sid AND ip.version IS NOT NULL
            ),
            exact_platform AS (
                SELECT plat.id
                FROM suseOVALPlatform plat
                JOIN product_cpe pc
                  ON pc.cpe = plat.cpe
            ),
            platform AS (
                SELECT id FROM exact_platform
                UNION ALL
                SELECT plat.id
                FROM suseOVALPlatform plat
                JOIN rhnServer s
                  ON s.id = :sid
                 AND (starts_with(s.cpe, plat.cpe) OR starts_with(plat.cpe, s.cpe))
                WHERE NOT EXISTS (SELECT 1 FROM exact_platform)
            )
            """;

    /**
     * One row per CVE with at least one vulnerable package installed on the server.
     * The status filter, the CVE name filter, the sort and the paging are appended by {@link #listCveRows}.
     * <p>
     * Versions are compared once per row on purpose: evr_t comparisons are PL/pgSQL function calls.
     */
    private static final String SERVER_CVE_QUERY = "WITH " + PLATFORM_CTES + """
            ,
            installed AS (
                SELECT sp.name_id, max(pe.evr) AS evr
                FROM rhnServerPackage sp
                JOIN rhnPackageEVR pe
                  ON pe.id = sp.evr_id
                WHERE sp.server_id = :sid
                GROUP BY sp.name_id
            ),
            vulnerable AS (
                SELECT pv.cve_id,
                       inst.name_id,
                       bool_and(vp.fix_version IS NULL) AS no_fix,
                       coalesce(bool_or(vp.fix_version > inst.evr), FALSE) AS fix_newer,
                       max(%s) AS severity_rank
                FROM platform
                JOIN suseOVALPlatformVulnerable pv
                  ON pv.platform_id = platform.id
                JOIN suseOVALVulnerablePackage vp
                  ON vp.plat_vuln_id = pv.id
                JOIN rhnPackageName pn
                  ON pn.name = vp.name
                JOIN installed inst
                  ON inst.name_id = pn.id
                GROUP BY pv.cve_id, inst.name_id
            ),
            candidate AS (
                SELECT ec.cve_id,
                       p.name_id,
                       bool_or(sc.channel_id IS NOT NULL) AS assigned,
                       min(cc.channel_rank) FILTER (WHERE sc.channel_id IS NULL) AS unassigned_rank
                FROM suseCVEServerChannel cc
                JOIN rhnChannelErrata ce
                  ON ce.channel_id = cc.channel_id
                JOIN rhnErrataPackage ep
                  ON ep.errata_id = ce.errata_id
                JOIN rhnChannelPackage cp
                  ON cp.channel_id = cc.channel_id
                 AND cp.package_id = ep.package_id
                JOIN rhnPackage p
                  ON p.id = ep.package_id
                JOIN installed inst
                  ON inst.name_id = p.name_id
                JOIN rhnPackageEVR pe
                  ON pe.id = p.evr_id
                JOIN rhnErrataCVE ec
                  ON ec.errata_id = ce.errata_id
                LEFT JOIN rhnServerChannel sc
                  ON sc.server_id = :sid
                 AND sc.channel_id = cc.channel_id
                WHERE cc.server_id = :sid
                  AND cc.channel_rank < %d
                  AND pe.evr > inst.evr
                  AND EXISTS (SELECT 1
                              FROM rhnServerPackage sp
                              JOIN rhnPackageUpgradeArchCompat ac
                                ON ac.package_arch_id = sp.package_arch_id
                              WHERE sp.server_id = :sid
                                AND sp.name_id = p.name_id
                                AND ac.package_upgrade_arch_id = p.package_arch_id)
                GROUP BY ec.cve_id, p.name_id
            ),
            package_status AS (
                SELECT v.cve_id,
                       v.no_fix,
                       v.no_fix OR v.fix_newer AS affected,
                       v.severity_rank,
                       c.assigned,
                       c.unassigned_rank
                FROM vulnerable v
                LEFT JOIN candidate c
                  ON c.cve_id = v.cve_id
                 AND c.name_id = v.name_id
                 AND v.fix_newer
            ),
            cve_status AS (
                SELECT cve_id,
                       bool_and(no_fix) AS all_no_fix,
                       bool_and(NOT affected) AS all_patched,
                       count(*) FILTER (WHERE affected AND NOT no_fix) AS fixable,
                       count(*) FILTER (WHERE assigned) AS with_assigned,
                       count(*) FILTER (WHERE unassigned_rank IS NOT NULL) AS with_unassigned,
                       min(unassigned_rank) AS min_unassigned_rank,
                       max(severity_rank) AS severity_rank
                FROM package_status
                GROUP BY cve_id
            ),
            cve_result AS (
                SELECT cve.id,
                       cve.name AS cve_name,
                       CASE WHEN cs.all_no_fix OR (cs.fixable = 0 AND NOT cs.all_patched)
                                 THEN 'AFFECTED_PATCH_UNAVAILABLE'
                            WHEN cs.all_patched THEN 'PATCHED'
                            WHEN cs.with_assigned = cs.fixable THEN 'AFFECTED_FULL_PATCH_APPLICABLE'
                            WHEN cs.with_assigned > 0 THEN 'AFFECTED_PARTIAL_PATCH_APPLICABLE'
                            WHEN cs.with_unassigned = cs.fixable AND cs.min_unassigned_rank >= %d
                                 THEN 'AFFECTED_PATCH_INAPPLICABLE_SUCCESSOR_PRODUCT'
                            WHEN cs.with_unassigned = cs.fixable THEN 'AFFECTED_PATCH_INAPPLICABLE'
                            ELSE 'AFFECTED_PATCH_UNAVAILABLE_IN_UYUNI' END AS status,
                       CASE WHEN cve.name ~ '^CVE-[0-9][0-9][0-9][0-9]-[0-9]+$'
                            THEN CAST(split_part(cve.name, '-', 2) AS BIGINT) * 10000000000
                                 + CAST(split_part(cve.name, '-', 3) AS BIGINT)
                            ELSE 0 END AS cve_number,
                       coalesce(cs.severity_rank, 0) AS severity_rank
                FROM cve_status cs
                JOIN rhnCVE cve
                  ON cve.id = cs.cve_id
            ),
            ranked AS (
                SELECT r.*, %s AS status_rank, %s AS severity
                FROM cve_result r
            )
            SELECT id, cve_name, status, severity, count(*) OVER () AS total
            FROM ranked
            """.formatted(severityRankCase(), PREDECESSOR_PRODUCT_RANK_BOUNDARY, SUCCESSOR_PRODUCT_RANK_BOUNDARY,
            statusRankCase(), severityNameCase());

    private SystemCVEAuditFactory() {
        // Utility class
    }

    private static String statusRankCase() {
        return Arrays.stream(PatchStatus.values())
                .map(s -> "WHEN '" + s.name() + "' THEN " + s.getRank())
                .collect(Collectors.joining(" ", "CASE r.status ", " END"));
    }

    /** Rank of the severity stored in suseOVALPlatformVulnerable, NULL when unknown */
    private static String severityRankCase() {
        return Arrays.stream(OvalSeverity.values())
                .filter(s -> s != OvalSeverity.UNKNOWN)
                .map(s -> "WHEN '" + s.name() + "' THEN " + s.getRank())
                .collect(Collectors.joining(" ", "CASE pv.severity ", " END"));
    }

    private static String severityNameCase() {
        return Arrays.stream(OvalSeverity.values())
                .filter(s -> s != OvalSeverity.UNKNOWN)
                .map(s -> "WHEN " + s.getRank() + " THEN '" + s.name() + "'")
                .collect(Collectors.joining(" ", "CASE r.severity_rank ",
                        " ELSE '" + OvalSeverity.UNKNOWN.name() + "' END"));
    }

    private static Query<Tuple> createQuery(String sql, Server server) {
        Session session = HibernateFactory.getSession();
        // Make sure the database sees the pending changes of the hibernate session
        if (session.getFlushMode().equals(FlushModeType.AUTO)) {
            session.flush();
        }
        return session.createNativeQuery(sql, Tuple.class).setParameter("sid", server.getId());
    }

    /**
     * Check if at least one OVAL platform matches the server
     * (see {@link #PLATFORM_CTES} for the matching rules).
     *
     * @param server the server
     * @return true if the server is covered by OVAL data
     */
    public static boolean isCoveredByOval(Server server) {
        return !createQuery("WITH " + PLATFORM_CTES + " SELECT id FROM platform", server)
                .setMaxResults(1)
                .list()
                .isEmpty();
    }

    /**
     * List the CVEs involving packages installed on the server, paged, filtered and sorted.
     * The default sort is by CVE number, newest first.
     * <p>
     * Columns: id, cve_name, status, severity and total, the number of rows before paging.
     *
     * @param server the server
     * @param statuses the statuses to include, must not be empty
     * @param severities the severities to include, an empty set includes all of them
     * @param pc the page control, filter is applied on the CVE name
     * @return the rows of the requested page
     */
    public static List<Tuple> listCveRows(Server server, Set<PatchStatus> statuses, Set<OvalSeverity> severities,
                                          PageControl pc) {
        // Status and severity names come from the enums, never from user input
        StringBuilder sql = new StringBuilder(SERVER_CVE_QUERY)
                .append("WHERE status IN (")
                .append(statuses.stream().map(s -> "'" + s.name() + "'").sorted().collect(Collectors.joining(", ")))
                .append(")");
        if (!severities.isEmpty()) {
            sql.append(" AND severity IN (")
                    .append(severities.stream().map(s -> "'" + s.name() + "'").sorted()
                            .collect(Collectors.joining(", ")))
                    .append(")");
        }

        Object filterValue = "";
        if (pc.hasFilter()) {
            pc.setFilterColumn(FILTER_COLUMN);
            FilterWithValue filter = PagedSqlQueryBuilder.parseFilterAsText(Optional.of(pc));
            filterValue = filter.getValue();
            if (!"".equals(filterValue)) {
                sql.append(" AND").append(filter.getFilter());
            }
        }

        String sortColumn = SORT_COLUMNS.get(pc.getSortColumn() == null ? "" : pc.getSortColumn());
        boolean descending = pc.isSortDescending();
        if (sortColumn == null) {
            sortColumn = DEFAULT_SORT_COLUMN;
            descending = true;
        }
        sql.append(" ORDER BY ").append(sortColumn).append(descending ? " DESC" : " ASC").append(", id");

        Query<Tuple> query = createQuery(sql.toString(), server);
        if (!"".equals(filterValue)) {
            query.setParameter("filter_value", filterValue);
        }
        query.setFirstResult(Math.max(pc.getStart(), 1) - 1);
        if (pc.getPageSize() > 0) {
            query.setMaxResults(pc.getPageSize());
        }
        return query.list();
    }
}
