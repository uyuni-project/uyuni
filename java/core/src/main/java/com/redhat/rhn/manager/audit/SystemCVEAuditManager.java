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

package com.redhat.rhn.manager.audit;

import static com.redhat.rhn.manager.audit.CVEAuditManager.PREDECESSOR_PRODUCT_RANK_BOUNDARY;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.domain.audit.SystemCVEAuditFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.frontend.listview.PageControl;

import com.suse.oval.OvalSeverity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.persistence.Tuple;

/**
 * Lists the CVEs affecting a single system, computed in a single SQL query from OVAL data and the CVE channels
 * of the system.
 * <p>
 * The status follows the OVAL rules of {@link CVEAuditManagerOVAL} as closely as possible, with these differences:
 * <ul>
 *     <li>OVAL platforms are matched on the exact server CPE and on the CPEs of its installed products (modules).
 *     The CVE audit prefix match is used only when none of them is known to OVAL.</li>
 *     <li>Only the newest installed version of a package is considered.</li>
 *     <li>Packages are counted only when affected and fixed by OVAL, live patching is not taken into account.</li>
 * </ul>
 */
public class SystemCVEAuditManager {

    /**
     * Availability of OVAL data for a system.
     */
    public enum OvalState {
        ENABLED,
        DISABLED,
        NOT_COVERED
    }

    /** Statuses for which an advisory fixing the CVE exists in a channel relevant for the system */
    private static final Set<PatchStatus> PATCH_AVAILABLE_STATUSES = EnumSet.of(
            PatchStatus.AFFECTED_FULL_PATCH_APPLICABLE,
            PatchStatus.AFFECTED_PARTIAL_PATCH_APPLICABLE,
            PatchStatus.AFFECTED_PATCH_INAPPLICABLE,
            PatchStatus.AFFECTED_PATCH_INAPPLICABLE_SUCCESSOR_PRODUCT);

    private SystemCVEAuditManager() {
        // Utility class
    }

    /**
     * Get the OVAL availability for a server.
     *
     * @param server the server
     * @return DISABLED if OVAL CVE audit is disabled, NOT_COVERED if no OVAL platform matches the server
     * (see {@link SystemCVEAuditFactory#isCoveredByOval}),
     * ENABLED otherwise
     */
    public static OvalState getOvalState(Server server) {
        if (!ConfigDefaults.get().isOvalEnabledForCveAudit()) {
            return OvalState.DISABLED;
        }
        return SystemCVEAuditFactory.isCoveredByOval(server) ? OvalState.ENABLED : OvalState.NOT_COVERED;
    }

    /**
     * List the CVEs involving packages installed on the server, paged, filtered and sorted.
     * The default sort is by CVE number, newest first.
     *
     * @param server the server, the caller must have checked that the user can access it
     * @param statuses the statuses to include, an empty set returns nothing
     * @param severities the severities to include, an empty set includes all of them
     * @param pc the page control, filter is applied on the CVE name
     * @return the page of results, with total size set
     */
    public static DataResult<SystemCVEResult> listCves(Server server, Set<PatchStatus> statuses,
                                                       Set<OvalSeverity> severities, PageControl pc) {
        if (statuses.isEmpty() || getOvalState(server) != OvalState.ENABLED) {
            DataResult<SystemCVEResult> empty = new DataResult<>(new ArrayList<>());
            empty.setTotalSize(0);
            return empty;
        }

        List<Tuple> rows = SystemCVEAuditFactory.listCveRows(server, statuses, severities, pc);
        DataResult<SystemCVEResult> result =
                new DataResult<>(rows.stream().map(SystemCVEResult::new).collect(Collectors.toList()));
        // The total comes from the window function, it is unknown if the page is past the last row
        result.setTotalSize(rows.isEmpty() ? 0 : rows.get(0).get("total", Number.class).intValue());

        addErrata(server, result);
        return result;
    }

    private static void addErrata(Server server, List<SystemCVEResult> cves) {
        List<Long> cveIds = cves.stream()
                .filter(c -> PATCH_AVAILABLE_STATUSES.contains(c.getStatus()))
                .map(SystemCVEResult::getId)
                .toList();
        if (cveIds.isEmpty()) {
            return;
        }

        SelectMode mode = ModeFactory.getMode("oval_queries", "list_system_cve_errata");
        Map<String, Object> params = new HashMap<>();
        params.put("sid", server.getId());
        params.put("max_rank", PREDECESSOR_PRODUCT_RANK_BOUNDARY);
        DataResult<Row> rows = mode.execute(params, cveIds);

        Map<Long, List<SystemCVEResult.Errata>> errataByCve = new HashMap<>();
        for (Row row : rows) {
            errataByCve.computeIfAbsent(((Number) row.get("cve_id")).longValue(), k -> new ArrayList<>())
                    .add(new SystemCVEResult.Errata(((Number) row.get("errata_id")).longValue(),
                            (String) row.get("advisory"), (String) row.get("channel_label"),
                            Boolean.TRUE.equals(row.get("assigned"))));
        }
        cves.forEach(c -> c.setErrata(errataByCve.getOrDefault(c.getId(), new ArrayList<>()).stream()
                .sorted(Comparator.comparing(SystemCVEResult.Errata::assigned).reversed()
                        .thenComparing(SystemCVEResult.Errata::advisory))
                .toList()));
    }
}
