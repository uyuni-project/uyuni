# HANDOVER: Per-system CVE list (System Details → Audit → "CVE")

## 1. Objective

Add a **CVE** sub-tab under *System Details → Audit*. It lists every CVE that touches a package **installed** on the system, using **OVAL data only**:

- `suseOVALPlatform`: matched to `rhnServer.cpe` by prefix, in either direction.
- `suseOVALPlatformVulnerable`: links the platform to the CVE.
- `suseOVALVulnerablePackage`: the package name and its `fix_version` (NULL means no fix has been released).

Each CVE row gets exactly one status. When the CVE involves several packages, the worst status wins:

| Status (enum `SystemCVEStatus`) | Rule | UI icon / label |
|---|---|---|
| `AFFECTED_NO_FIX` | any installed vulnerable package has `fix_version IS NULL` | `fa-exclamation-circle text-danger` "Affected, no fix released" |
| `AFFECTED_FIX_AVAILABLE` | else, any installed package is older than its `fix_version` | `fa-shield text-warning` "Affected, fix released" |
| `PATCHED` | else | `fa-check-circle text-success` "Patched" |

"Not affected" CVEs (no vulnerable package installed) are never listed.

Page features:
- status filter checkboxes
- search by CVE id
- server-side paging and sorting
- for each CVE, links to **every synced advisory** that contains it (`rhnErrataCVE`). Only advisories the user's org can see are shown: vendor errata (`org_id IS NULL`) or `org_id = user's org`.
- no CSV export and no expandable rows

If OVAL is disabled (`java.cve_audit.enable_oval_metadata`), or the system's CPE is not covered by OVAL data, the page shows **only an info message**.

Clicking the top-level **Audit** tab now opens the new CVE sub-tab.

Stack and patterns:
- React page, mounted via jade + `spaImportReactPage`, following the CoCo settings page.
- Spark routes in the existing `CVEAuditController`.
- RBAC namespace `systems.audit.cve` (R).

Implementation notes. These two details were forced by the existing code; the agreed behaviour is unchanged:
- **Paged query in Java.** The paged CVE query is a Java constant run through `com.suse.manager.utils.PagedSqlQueryBuilder`, because that builder takes native SQL and cannot use XML datasource modes. The per-page errata lookup is an XML mode in `oval_queries.xml`.
- **Status filter in the URL path.** The selected statuses go in a **path segment**: `/manager/api/systems/:sid/details/audit/cve/:statuses`. A query parameter won't work, because the React `Table` async provider (`web/html/src/utils/data-providers/paged-data-endpoint.ts`, `getNewQuery()`) wipes the query string of the data URL.

---

## 2. Target Files

**Create**
- `java/core/src/main/java/com/redhat/rhn/manager/audit/SystemCVEStatus.java`
- `java/core/src/main/java/com/redhat/rhn/manager/audit/SystemCVEResult.java`
- `java/core/src/main/java/com/redhat/rhn/manager/audit/SystemCVEAuditManager.java`
- `java/core/src/main/java/com/suse/manager/webui/utils/gson/SystemCVEJson.java`
- `java/core/src/main/resources/com/suse/manager/webui/templates/minion/system-cve.jade`
- `java/core/src/test/java/com/redhat/rhn/manager/audit/SystemCVEAuditManagerTest.java`
- `java/core/src/test/java/com/suse/manager/webui/controllers/CVEAuditControllerSystemCveTest.java`
- `web/html/src/manager/minion/audit/system-cve.tsx`
- `web/html/src/manager/minion/audit/system-cve.renderer.tsx`
- `web/html/src/manager/minion/audit/system-cve.renderer.test.tsx`
- `web/html/src/manager/minion/audit/system-cve.test.tsx`
- `schema/spacewalk/upgrade/next/600-rbac-system-cve-audit.sql`
- `java/spacewalk-java.changes.rmateus.system-cve-audit`
- `web/spacewalk-web.changes.rmateus.system-cve-audit`
- `schema/spacewalk/susemanager-schema.changes.rmateus.system-cve-audit`

**Modify**
- `java/core/src/main/java/com/redhat/rhn/manager/audit/CVEAuditManagerOVAL.java`: make `isCpeCoveredByOval` package-private.
- `java/core/src/main/resources/com/redhat/rhn/common/db/datasource/xml/oval_queries.xml`: add mode `list_cve_errata`.
- `java/core/src/main/java/com/suse/manager/webui/controllers/CVEAuditController.java`: add 2 routes and their handlers.
- `java/webapp/src/main/webapp/WEB-INF/nav/system_detail.xml`: add the CVE sub-tab and change the Audit default URL.
- `java/core/src/main/resources/com/redhat/rhn/frontend/strings/nav/StringResource_en_US.xml`: add the label `nav.system.audit.cve`.
- `web/html/src/manager/minion/index.ts`: register the page.
- `schema/spacewalk/common/data/namespace.sql`
- `schema/spacewalk/common/data/endpoint.sql`
- `schema/spacewalk/common/data/endpointNamespace.sql`

**Delete**: none.

---

## 3. Step-by-Step Instructions

The branch is already based on `master`. Do not run `git checkout` or change branches.

Run commands from the worktree root `/home/rmateus/worktrees/server_uyuni_system_cve_analyzes`.

---

### Step 1: Make `isCpeCoveredByOval` package-private

File: `java/core/src/main/java/com/redhat/rhn/manager/audit/CVEAuditManagerOVAL.java`

Replace:
```java
        return result;
    }

    private static boolean isCpeCoveredByOval(String cpe, Set<String> ovalPlatformCpes) {
        return cpe != null && ovalPlatformCpes.stream()
                .anyMatch(ovalCpe -> cpe.startsWith(ovalCpe) || ovalCpe.startsWith(cpe));
    }
```
with:
```java
        return result;
    }

    /**
     * Check whether a server CPE is covered by any OVAL platform CPE (prefix match in either direction).
     *
     * @param cpe the server CPE, may be null
     * @param ovalPlatformCpes the OVAL platform CPEs
     * @return true if covered
     */
    static boolean isCpeCoveredByOval(String cpe, Set<String> ovalPlatformCpes) {
        return cpe != null && ovalPlatformCpes.stream()
                .anyMatch(ovalCpe -> cpe.startsWith(ovalCpe) || ovalCpe.startsWith(cpe));
    }
```

---

### Step 2: Add the errata lookup mode

File: `java/core/src/main/resources/com/redhat/rhn/common/db/datasource/xml/oval_queries.xml`

Replace the end of the file:
```xml
            DELETE FROM suseOVALPlatformVulnerable
            WHERE last_modified &lt; :start_date;
        </query>
    </write-mode>

</datasource_modes>
```
with:
```xml
            DELETE FROM suseOVALPlatformVulnerable
            WHERE last_modified &lt; :start_date;
        </query>
    </write-mode>

    <mode name="list_cve_errata">
        <query params="org_id">
            SELECT errataCve.cve_id AS cve_id,
                errata.id AS errata_id,
                errata.advisory AS advisory
            FROM rhnErrataCVE errataCve
            JOIN rhnErrata errata
              ON errata.id = errataCve.errata_id
            WHERE (errata.org_id IS NULL OR errata.org_id = :org_id)
              AND errataCve.cve_id IN (%s)
            ORDER BY errataCve.cve_id, errata.advisory
        </query>
    </mode>

</datasource_modes>
```

The `%s` is replaced by a comma-separated list of **numeric** CVE ids. The in-clause list goes into the SQL text as-is, so only `Long` values may ever be passed.

---

### Step 3: Create the status enum

File (new): `java/core/src/main/java/com/redhat/rhn/manager/audit/SystemCVEStatus.java`
```java
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

/**
 * Status of a CVE on a single system, computed from OVAL data only.
 * The rank is used for sorting, lower means more severe.
 */
public enum SystemCVEStatus {
    AFFECTED_NO_FIX(0),
    AFFECTED_FIX_AVAILABLE(1),
    PATCHED(2);

    private final int rank;

    SystemCVEStatus(int rankIn) {
        rank = rankIn;
    }

    /**
     * @return the rank of the status, lower means more severe
     */
    public int getRank() {
        return rank;
    }
}
```

---

### Step 4: Create the result DTO

File (new): `java/core/src/main/java/com/redhat/rhn/manager/audit/SystemCVEResult.java`
```java
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

import com.redhat.rhn.frontend.dto.BaseTupleDto;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Tuple;

/**
 * One CVE affecting (or patched on) a single system, computed from OVAL data.
 */
public class SystemCVEResult extends BaseTupleDto {

    private Long id;
    private String cveName;
    private SystemCVEStatus status;
    private List<ErrataIdAdvisoryPair> errata = new ArrayList<>();

    /**
     * Constructor used by {@link com.suse.manager.utils.PagedSqlQueryBuilder}
     *
     * @param tuple JPA tuple with the columns id, cve_name and status
     */
    public SystemCVEResult(Tuple tuple) {
        setId(getTupleValue(tuple, "id", Number.class).map(Number::longValue).orElse(null));
        setCveName(getTupleValue(tuple, "cve_name", String.class).orElse(""));
        setStatus(getTupleValue(tuple, "status", String.class)
                .map(SystemCVEStatus::valueOf)
                .orElse(SystemCVEStatus.PATCHED));
    }

    /**
     * @return the CVE id (rhnCVE.id)
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * @param idIn the CVE id
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * @return the CVE name, e.g. CVE-2024-1234
     */
    public String getCveName() {
        return cveName;
    }

    /**
     * @param cveNameIn the CVE name
     */
    public void setCveName(String cveNameIn) {
        cveName = cveNameIn;
    }

    /**
     * @return the status of the CVE on the system
     */
    public SystemCVEStatus getStatus() {
        return status;
    }

    /**
     * @param statusIn the status
     */
    public void setStatus(SystemCVEStatus statusIn) {
        status = statusIn;
    }

    /**
     * @return the synced advisories containing this CVE
     */
    public List<ErrataIdAdvisoryPair> getErrata() {
        return errata;
    }

    /**
     * @param errataIn the advisories containing this CVE
     */
    public void setErrata(List<ErrataIdAdvisoryPair> errataIn) {
        errata = errataIn;
    }
}
```

---

### Step 5: Create the manager

File (new): `java/core/src/main/java/com/redhat/rhn/manager/audit/SystemCVEAuditManager.java`
```java
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

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.listview.PageControl;

import com.suse.manager.utils.PagedSqlQueryBuilder;
import com.suse.oval.OVALCachingFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Lists the CVEs affecting a single system, based on OVAL data only.
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

    /** UI column key to SQL column */
    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "cveName", "X.cve_name",
            "status", "X.status_rank"
    );

    private static final String DEFAULT_SORT_COLUMN = "X.status_rank";

    private static final String FILTER_COLUMN = "X.cve_name";

    /**
     * One row per CVE that involves at least one package installed on the server.
     * Worst status wins: no fix &gt; fix available &gt; patched.
     */
    private static final String SERVER_CVE_STATUS_FROM = """
            (SELECT A.cve_id AS id,
                    A.cve_name,
                    CASE WHEN A.has_no_fix THEN 'AFFECTED_NO_FIX'
                         WHEN A.has_fixable THEN 'AFFECTED_FIX_AVAILABLE'
                         ELSE 'PATCHED' END AS status,
                    CASE WHEN A.has_no_fix THEN 0
                         WHEN A.has_fixable THEN 1
                         ELSE 2 END AS status_rank
             FROM (SELECT cve.id AS cve_id,
                          cve.name AS cve_name,
                          bool_or(vp.fix_version IS NULL) AS has_no_fix,
                          bool_or(vp.fix_version IS NOT NULL AND vp.fix_version > pe.evr) AS has_fixable
                   FROM rhnServer s
                   JOIN suseOVALPlatform plat
                     ON (starts_with(s.cpe, plat.cpe) OR starts_with(plat.cpe, s.cpe))
                   JOIN suseOVALPlatformVulnerable pv
                     ON pv.platform_id = plat.id
                   JOIN rhnCVE cve
                     ON cve.id = pv.cve_id
                   JOIN suseOVALVulnerablePackage vp
                     ON vp.plat_vuln_id = pv.id
                   JOIN rhnPackageName pn
                     ON pn.name = vp.name
                   JOIN rhnServerPackage sp
                     ON sp.server_id = s.id AND sp.name_id = pn.id
                   JOIN rhnPackageEVR pe
                     ON pe.id = sp.evr_id
                   WHERE s.id = :sid
                   GROUP BY cve.id, cve.name) A) X""";

    private SystemCVEAuditManager() {
        // Utility class
    }

    /**
     * Get the OVAL availability for a server.
     *
     * @param server the server
     * @return DISABLED if OVAL CVE audit is disabled, NOT_COVERED if no OVAL platform matches the server CPE,
     * ENABLED otherwise
     */
    public static OvalState getOvalState(Server server) {
        if (!ConfigDefaults.get().isOvalEnabledForCveAudit()) {
            return OvalState.DISABLED;
        }
        if (!CVEAuditManagerOVAL.isCpeCoveredByOval(server.getCpe(), OVALCachingFactory.getOVALPlatformCpes())) {
            return OvalState.NOT_COVERED;
        }
        return OvalState.ENABLED;
    }

    /**
     * List the CVEs involving installed packages of the server, paged, filtered and sorted.
     *
     * @param user the current user (used to restrict the advisories to the visible ones)
     * @param server the server
     * @param statuses the statuses to include, an empty set returns nothing
     * @param pc the page control, filter is applied on the CVE name
     * @return the page of results, with total size set
     */
    public static DataResult<SystemCVEResult> listCves(User user, Server server, Set<SystemCVEStatus> statuses,
                                                       PageControl pc) {
        if (statuses.isEmpty() || getOvalState(server) != OvalState.ENABLED) {
            DataResult<SystemCVEResult> empty = new DataResult<>(new ArrayList<>());
            empty.setTotalSize(0);
            return empty;
        }

        if (pc.hasFilter()) {
            pc.setFilterColumn(FILTER_COLUMN);
        }
        String requestedSort = pc.getSortColumn() == null ? "" : pc.getSortColumn();
        pc.setSortColumn(SORT_COLUMNS.getOrDefault(requestedSort, DEFAULT_SORT_COLUMN));

        // Status names come from the enum, never from user input
        String statusList = statuses.stream()
                .map(s -> "'" + s.name() + "'")
                .sorted()
                .collect(Collectors.joining(", "));

        DataResult<SystemCVEResult> result = new PagedSqlQueryBuilder()
                .select("X.id, X.cve_name, X.status, X.status_rank")
                .from(SERVER_CVE_STATUS_FROM)
                .where("X.status IN (" + statusList + ")")
                .run(Map.of("sid", server.getId()), pc, PagedSqlQueryBuilder::parseFilterAsText,
                        SystemCVEResult.class);

        addErrata(user, result);
        return result;
    }

    private static void addErrata(User user, List<SystemCVEResult> cves) {
        if (cves.isEmpty()) {
            return;
        }
        List<Long> cveIds = cves.stream().map(SystemCVEResult::getId).collect(Collectors.toList());

        SelectMode mode = ModeFactory.getMode("oval_queries", "list_cve_errata");
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", user.getOrg().getId());
        DataResult<Row> rows = mode.execute(params, cveIds);

        Map<Long, List<ErrataIdAdvisoryPair>> errataByCve = new HashMap<>();
        for (Row row : rows) {
            long cveId = ((Number) row.get("cve_id")).longValue();
            errataByCve.computeIfAbsent(cveId, k -> new ArrayList<>())
                    .add(new ErrataIdAdvisoryPair(((Number) row.get("errata_id")).longValue(),
                            (String) row.get("advisory")));
        }
        cves.forEach(c -> c.setErrata(errataByCve.getOrDefault(c.getId(), new ArrayList<>())));
    }
}
```

---

### Step 6: Create the JSON DTO

File (new): `java/core/src/main/java/com/suse/manager/webui/utils/gson/SystemCVEJson.java`
```java
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

package com.suse.manager.webui.utils.gson;

import com.redhat.rhn.manager.audit.ErrataIdAdvisoryPair;
import com.redhat.rhn.manager.audit.SystemCVEResult;

import java.util.List;

/**
 * JSON row of the system CVE list page
 */
public class SystemCVEJson {

    private final long id;
    private final String cveName;
    private final String status;
    private final List<ErrataIdAdvisoryPair> errata;

    /**
     * @param result the manager result to convert
     */
    public SystemCVEJson(SystemCVEResult result) {
        id = result.getId();
        cveName = result.getCveName();
        status = result.getStatus().name();
        errata = result.getErrata();
    }

    /**
     * @return the CVE id
     */
    public long getId() {
        return id;
    }

    /**
     * @return the CVE name
     */
    public String getCveName() {
        return cveName;
    }

    /**
     * @return the status name
     */
    public String getStatus() {
        return status;
    }

    /**
     * @return the advisories containing the CVE
     */
    public List<ErrataIdAdvisoryPair> getErrata() {
        return errata;
    }
}
```

---

### Step 7: Add the Spark routes and handlers

File: `java/core/src/main/java/com/suse/manager/webui/controllers/CVEAuditController.java`

**7a. Imports.** Replace the import block from `import static com.suse.manager.webui.utils.SparkApplicationHelper.result;` down to `import com.suse.manager.webui.utils.gson.ResultJson;`:
```java
import static com.suse.manager.webui.utils.SparkApplicationHelper.result;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.audit.CVEAuditImage;
import com.redhat.rhn.manager.audit.CVEAuditManagerOVAL;
import com.redhat.rhn.manager.audit.CVEAuditServer;
import com.redhat.rhn.manager.audit.CVEAuditSystem;
import com.redhat.rhn.manager.audit.PatchStatus;
import com.redhat.rhn.manager.audit.UnknownCVEIdentifierException;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;

import com.suse.manager.webui.utils.gson.ResultJson;
```
with:
```java
import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static com.suse.manager.webui.utils.SparkApplicationHelper.json;
import static com.suse.manager.webui.utils.SparkApplicationHelper.result;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withCsrfToken;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withDocsLocale;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserAndServer;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUserPreferences;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.manager.audit.CVEAuditImage;
import com.redhat.rhn.manager.audit.CVEAuditManagerOVAL;
import com.redhat.rhn.manager.audit.CVEAuditServer;
import com.redhat.rhn.manager.audit.CVEAuditSystem;
import com.redhat.rhn.manager.audit.PatchStatus;
import com.redhat.rhn.manager.audit.SystemCVEAuditManager;
import com.redhat.rhn.manager.audit.SystemCVEResult;
import com.redhat.rhn.manager.audit.SystemCVEStatus;
import com.redhat.rhn.manager.audit.UnknownCVEIdentifierException;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;

import com.suse.manager.webui.utils.PageControlHelper;
import com.suse.manager.webui.utils.gson.PagedDataResultJson;
import com.suse.manager.webui.utils.gson.ResultJson;
import com.suse.manager.webui.utils.gson.SystemCVEJson;
```
The `java.util.*` imports already in the file (`Collections`, `EnumSet`, `HashMap`, `List`, `Map`, `Set`, `Collectors`) cover the new code. Do not duplicate them.

**7b. Routes.** Replace:
```java
        post("/manager/api/audit/cve", withUser(CVEAuditController::cveAudit));
        get("/manager/api/audit/cve.csv", withUser(CVEAuditController::cveAuditCSV));
    }
```
with:
```java
        post("/manager/api/audit/cve", withUser(CVEAuditController::cveAudit));
        get("/manager/api/audit/cve.csv", withUser(CVEAuditController::cveAuditCSV));

        get("/manager/systems/details/audit/cve",
                withCsrfToken(withDocsLocale(withUserAndServer(CVEAuditController::systemCveView))), jade);
        get("/manager/api/systems/:sid/details/audit/cve/:statuses",
                asJson(withUserAndServer(CVEAuditController::systemCves)));
    }

    /**
     * Returns the system CVE list page
     *
     * @param request the request object
     * @param response the response object
     * @param user the authorized user
     * @param server the server
     * @return the model and view
     */
    public static ModelAndView systemCveView(Request request, Response response, User user, Server server) {
        Map<String, Object> data = new HashMap<>();
        data.put("ovalState", SystemCVEAuditManager.getOvalState(server).name());
        return new ModelAndView(data, "templates/minion/system-cve.jade");
    }

    /**
     * Returns a page of the CVEs affecting the system, as paged JSON.
     * Path param 'statuses' is a comma separated list of {@link SystemCVEStatus} names ('none' for no status).
     *
     * @param request the request object
     * @param response the response object
     * @param user the authorized user
     * @param server the server
     * @return the JSON page
     */
    public static String systemCves(Request request, Response response, User user, Server server) {
        Set<SystemCVEStatus> statuses = parseSystemCveStatuses(request.params("statuses"));
        PageControlHelper pageHelper = new PageControlHelper(request, "cveName");
        PageControl pc = pageHelper.getPageControl();

        DataResult<SystemCVEResult> cves = SystemCVEAuditManager.listCves(user, server, statuses, pc);
        List<SystemCVEJson> items = cves.stream().map(SystemCVEJson::new).collect(Collectors.toList());

        return json(response, new PagedDataResultJson<>(items, cves.getTotalSize(), Collections.emptySet()),
                new TypeToken<>() { });
    }

    /**
     * Parse a comma separated list of status names, ignoring unknown values.
     *
     * @param value the raw value, may be null
     * @return the parsed statuses
     */
    static Set<SystemCVEStatus> parseSystemCveStatuses(String value) {
        Set<SystemCVEStatus> statuses = EnumSet.noneOf(SystemCVEStatus.class);
        if (value == null) {
            return statuses;
        }
        for (String token : value.split(",")) {
            try {
                statuses.add(SystemCVEStatus.valueOf(token.trim()));
            }
            catch (IllegalArgumentException e) {
                log.debug("Ignoring unknown system CVE status: {}", token);
            }
        }
        return statuses;
    }
```
`CVEAuditController.initRoutes(jade)` is already called from `Router.java`. Do **not** touch `Router.java`.

---

### Step 8: Create the jade template

File (new): `java/core/src/main/resources/com/suse/manager/webui/templates/minion/system-cve.jade`
```jade
include ./minion-header.jade

#system-cve

script(type='text/javascript').
    window.csrfToken = "#{csrf_token}";

script(type='text/javascript').
    spaImportReactPage('minion/audit/system-cve')
        .then(function(module) {
            module.renderer('system-cve', {
                serverId: #{server.id},
                ovalState: "#{ovalState}"
            })
        });
```

---

### Step 9: Update the navigation

File: `java/webapp/src/main/webapp/WEB-INF/nav/system_detail.xml`

Replace:
```xml
  <rhn-tab name="Audit" url="/rhn/manager/systems/details/coco/settings">
    <rhn-tab name="OpenSCAP" acl="system_has_salt_entitlement_and_contact_method(default)" url="/rhn/systems/details/audit/ListScap.do">
      <rhn-tab-url>/rhn/systems/details/audit/ListScap.do</rhn-tab-url>
```
with:
```xml
  <rhn-tab name="Audit" url="/rhn/manager/systems/details/audit/cve">
    <rhn-tab name="nav.system.audit.cve" acl="authorized_for(systems.audit.cve)" url="/rhn/manager/systems/details/audit/cve">
      <rhn-tab-url>/rhn/manager/systems/details/audit/cve</rhn-tab-url>
    </rhn-tab>
    <rhn-tab name="OpenSCAP" acl="system_has_salt_entitlement_and_contact_method(default)" url="/rhn/systems/details/audit/ListScap.do">
      <rhn-tab-url>/rhn/systems/details/audit/ListScap.do</rhn-tab-url>
```

---

### Step 10: Add the tab label

File: `java/core/src/main/resources/com/redhat/rhn/frontend/strings/nav/StringResource_en_US.xml`

Replace:
```xml
      <trans-unit id="nav.system.audit.schedule">
          <source>Schedule</source>
        <context-group name="ctx">
          <context context-type="sourcefile">/rhn/manager/systems/details/schedule-scap-scan</context>
        </context-group>
      </trans-unit>
```
with:
```xml
      <trans-unit id="nav.system.audit.schedule">
          <source>Schedule</source>
        <context-group name="ctx">
          <context context-type="sourcefile">/rhn/manager/systems/details/schedule-scap-scan</context>
        </context-group>
      </trans-unit>
      <trans-unit id="nav.system.audit.cve">
          <source>CVE</source>
        <context-group name="ctx">
          <context context-type="sourcefile">/rhn/manager/systems/details/audit/cve</context>
        </context-group>
      </trans-unit>
```
Change only the `en_US` file. The other locales are managed by translation tooling.

---

### Step 11: Add the RBAC rows for fresh installs

**11a.** File: `schema/spacewalk/common/data/namespace.sql`

Replace:
```sql
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.audit.coco', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
```
with:
```sql
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.audit.coco', 'R', NULL)
    ON CONFLICT (namespace, access_mode) DO NOTHING;
INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.audit.cve', 'R', 'View the CVEs affecting a system')
    ON CONFLICT (namespace, access_mode) DO NOTHING;
```

**11b.** File: `schema/spacewalk/common/data/endpoint.sql`

Replace:
```sql
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/coco/list', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
```
with:
```sql
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/coco/list', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/audit/cve', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/details/audit/cve/:statuses', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;
```

**11c.** File: `schema/spacewalk/common/data/endpointNamespace.sql`

Replace:
```sql
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.coco' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/coco/list' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
```
with:
```sql
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.coco' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/coco/list' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.cve' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/audit/cve' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.cve' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systems/:sid/details/audit/cve/:statuses' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;
```

Do **not** edit `accessGroupNamespace.sql`. Its existing rule `ns.namespace LIKE 'systems.audit.%'` already grants the new namespace to every access group on fresh installs.

---

### Step 12: Add the changelog entries

**12a.** File (new): `java/spacewalk-java.changes.rmateus.system-cve-audit`
```
- Add CVE sub-tab to system details Audit tab listing the CVEs
  affecting the system based on OVAL data
```

**12b.** File (new): `web/spacewalk-web.changes.rmateus.system-cve-audit`
```
- Add CVE list page to system details Audit tab
```

**12c.** File (new): `schema/spacewalk/susemanager-schema.changes.rmateus.system-cve-audit`
```
- Add RBAC namespace and endpoints for the system CVE list page
```

---

### Step 13: Add the schema upgrade script

File (new): `schema/spacewalk/upgrade/next/600-rbac-system-cve-audit.sql`

```sql
--
-- Copyright (c) 2026 SUSE LLC
--
-- This software is licensed to you under the GNU General Public License,
-- version 2 (GPLv2). There is NO WARRANTY for this software, express or
-- implied, including the implied warranties of MERCHANTABILITY or FITNESS
-- FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
-- along with this software; if not, see
-- http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
--
-- SPDX-License-Identifier: GPL-2.0-only
--

INSERT INTO access.namespace (namespace, access_mode, description)
    VALUES ('systems.audit.cve', 'R', 'View the CVEs affecting a system')
    ON CONFLICT (namespace, access_mode) DO NOTHING;

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/systems/details/audit/cve', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;

INSERT INTO access.endpoint (class_method, endpoint, http_method, scope, auth_required)
    VALUES ('', '/manager/api/systems/:sid/details/audit/cve/:statuses', 'GET', 'W', True)
    ON CONFLICT (endpoint, http_method) DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.cve' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/systems/details/audit/cve' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

INSERT INTO access.endpointNamespace (namespace_id, endpoint_id)
    SELECT ns.id, ep.id FROM access.namespace ns, access.endpoint ep
    WHERE ns.namespace = 'systems.audit.cve' AND ns.access_mode = 'R'
    AND ep.endpoint = '/manager/api/systems/:sid/details/audit/cve/:statuses' AND ep.http_method = 'GET'
    ON CONFLICT DO NOTHING;

INSERT INTO access.accessGroupNamespace (group_id, namespace_id)
    SELECT ag.id, ns.id
    FROM access.accessGroup ag, access.namespace ns
    WHERE ns.namespace = 'systems.audit.cve'
    ON CONFLICT (group_id, namespace_id) DO NOTHING;
```

---

### Step 14: Register the React page

File: `web/html/src/manager/minion/index.ts`

Replace:
```ts
  "minion/audit/schedule-scap-scan": () => import("./audit/schedule-scap-scan"),
  "minion/audit/rule-result-detail": () => import("./audit/rule-result-detail"),
};
```
with:
```ts
  "minion/audit/schedule-scap-scan": () => import("./audit/schedule-scap-scan"),
  "minion/audit/rule-result-detail": () => import("./audit/rule-result-detail"),
  "minion/audit/system-cve": () => import("./audit/system-cve.renderer"),
};
```

---

### Step 15: Create the React page component

File (new): `web/html/src/manager/minion/audit/system-cve.tsx`
```tsx
/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { useState } from "react";

import { pageSize } from "core/user-preferences";

import { Messages, Utils as MessagesUtils } from "components/messages/messages";
import { Column } from "components/table/Column";
import { SearchField } from "components/table/SearchField";
import { Table } from "components/table/Table";

// See java/core/src/main/java/com/redhat/rhn/manager/audit/SystemCVEAuditManager.java (OvalState)
export type OvalState = "ENABLED" | "DISABLED" | "NOT_COVERED";

// See java/core/src/main/java/com/redhat/rhn/manager/audit/SystemCVEStatus.java
export type SystemCveStatus = "AFFECTED_NO_FIX" | "AFFECTED_FIX_AVAILABLE" | "PATCHED";

export type SystemCveErrata = {
  id: number;
  advisory: string;
};

export type SystemCveRow = {
  id: number;
  cveName: string;
  status: SystemCveStatus;
  errata: SystemCveErrata[];
};

export const ALL_STATUSES: SystemCveStatus[] = ["AFFECTED_NO_FIX", "AFFECTED_FIX_AVAILABLE", "PATCHED"];

export const STATUS_LABEL: Record<SystemCveStatus, { className: string; label: string }> = {
  AFFECTED_NO_FIX: {
    className: "fa-exclamation-circle text-danger",
    label: t("Affected, no fix released"),
  },
  AFFECTED_FIX_AVAILABLE: {
    className: "fa-shield text-warning",
    label: t("Affected, fix released"),
  },
  PATCHED: {
    className: "fa-check-circle text-success",
    label: t("Patched"),
  },
};

export const getSystemCveDataUrl = (serverId: number, statuses: SystemCveStatus[]): string => {
  const statusSegment = statuses.length > 0 ? statuses.join(",") : "none";
  return `/rhn/manager/api/systems/${serverId}/details/audit/cve/${statusSegment}`;
};

type Props = {
  serverId: number;
  ovalState: OvalState;
};

export function SystemCve({ serverId, ovalState }: Props) {
  const [statuses, setStatuses] = useState<SystemCveStatus[]>(ALL_STATUSES);

  if (ovalState === "DISABLED") {
    return (
      <Messages
        items={MessagesUtils.info(
          t(
            "OVAL based CVE auditing is disabled. Set 'java.cve_audit.enable_oval_metadata = true' in the server configuration and synchronize the OVAL data to list the CVEs affecting this system."
          )
        )}
      />
    );
  }

  if (ovalState === "NOT_COVERED") {
    return (
      <Messages
        items={MessagesUtils.info(t("No OVAL data is available for the operating system of this system."))}
      />
    );
  }

  const toggleStatus = (status: SystemCveStatus) => {
    setStatuses((prev) =>
      prev.includes(status)
        ? prev.filter((x) => x !== status)
        : ALL_STATUSES.filter((x) => prev.includes(x) || x === status)
    );
  };

  return (
    <div>
      <div>
        {ALL_STATUSES.map((status) => (
          <div className="checkbox" key={status}>
            <label>
              <input type="checkbox" checked={statuses.includes(status)} onChange={() => toggleStatus(status)} />
              <i className={"fa fa-big " + STATUS_LABEL[status].className} title={STATUS_LABEL[status].label} />
              <span>{" " + STATUS_LABEL[status].label}</span>
            </label>
          </div>
        ))}
      </div>
      <Table
        data={getSystemCveDataUrl(serverId, statuses)}
        identifier={(row: SystemCveRow) => row.id}
        selectable={false}
        initialItemsPerPage={pageSize}
        initialSortColumnKey="status"
        defaultSearchField="cveName"
        searchField={<SearchField placeholder={t("Filter by CVE")} />}
        emptyText={t("No CVEs found for this system.")}
      >
        <Column
          columnKey="status"
          header={t("Status")}
          sortable
          cell={(row: SystemCveRow) => (
            <span>
              <i
                className={"fa fa-big " + STATUS_LABEL[row.status].className}
                title={STATUS_LABEL[row.status].label}
              />{" "}
              {STATUS_LABEL[row.status].label}
            </span>
          )}
        />
        <Column columnKey="cveName" header={t("CVE")} sortable cell={(row: SystemCveRow) => row.cveName} />
        <Column
          columnKey="errata"
          header={t("Patches")}
          cell={(row: SystemCveRow) =>
            row.errata.length === 0
              ? "-"
              : row.errata.map((errata, index) => (
                  <span key={errata.id}>
                    {index > 0 && ", "}
                    <a href={"/rhn/errata/details/Details.do?eid=" + errata.id}>{errata.advisory}</a>
                  </span>
                ))
          }
        />
      </Table>
    </div>
  );
}
```

---

### Step 16: Create the React renderer

File (new): `web/html/src/manager/minion/audit/system-cve.renderer.tsx`
```tsx
/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import SpaRenderer from "core/spa/spa-renderer";

import { OvalState, SystemCve } from "./system-cve";

// See java/core/src/main/resources/com/suse/manager/webui/templates/minion/system-cve.jade
export interface SystemCveRendererProps {
  serverId: number;
  ovalState: OvalState;
}

export const renderer = (id: string, props?: Partial<SystemCveRendererProps>) => {
  if (props?.serverId === undefined || props?.ovalState === undefined) {
    return;
  }

  SpaRenderer.renderNavigationReact(
    <SystemCve serverId={props.serverId} ovalState={props.ovalState} />,
    document.getElementById(id)
  );
};
```

---

### Step 17: Create the frontend tests

**17a.** File (new): `web/html/src/manager/minion/audit/system-cve.renderer.test.tsx`
```tsx
/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import SpaRenderer from "core/spa/spa-renderer";

import { renderer } from "./system-cve.renderer";

jest.mock("core/spa/spa-renderer", () => ({
  __esModule: true,
  default: {
    renderNavigationReact: jest.fn(),
  },
}));

jest.mock("./system-cve", () => ({
  SystemCve: () => null,
}));

const renderNavigationReact = SpaRenderer.renderNavigationReact as jest.Mock;

describe("System CVE renderer", () => {
  beforeEach(() => {
    document.body.innerHTML = '<div id="system-cve"></div>';
    renderNavigationReact.mockClear();
  });

  test("uses renderer properties", () => {
    renderer("system-cve", { serverId: 123, ovalState: "ENABLED" });

    const [element, container] = renderNavigationReact.mock.calls[0];
    expect(element.props).toMatchObject({ serverId: 123, ovalState: "ENABLED" });
    expect(container).toBe(document.getElementById("system-cve"));
  });

  test("does not render without properties", () => {
    renderer("system-cve");

    expect(renderNavigationReact).not.toHaveBeenCalled();
  });
});
```

**17b.** File (new): `web/html/src/manager/minion/audit/system-cve.test.tsx`
```tsx
/*
 * SPDX-FileCopyrightText: 2026 SUSE LLC
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

import { render } from "@testing-library/react";

import { getSystemCveDataUrl, SystemCve } from "./system-cve";

describe("System CVE page", () => {
  test("shows only an info message when OVAL is disabled", () => {
    const view = render(<SystemCve serverId={1} ovalState="DISABLED" />);

    expect(view.getByText(/OVAL based CVE auditing is disabled/)).toBeTruthy();
    expect(view.queryByRole("table")).toBeNull();
  });

  test("shows only an info message when the system is not covered by OVAL", () => {
    const view = render(<SystemCve serverId={1} ovalState="NOT_COVERED" />);

    expect(view.getByText(/No OVAL data is available/)).toBeTruthy();
    expect(view.queryByRole("table")).toBeNull();
  });

  test("builds the data URL with the statuses as path segment", () => {
    expect(getSystemCveDataUrl(42, ["AFFECTED_NO_FIX", "PATCHED"])).toBe(
      "/rhn/manager/api/systems/42/details/audit/cve/AFFECTED_NO_FIX,PATCHED"
    );
    expect(getSystemCveDataUrl(42, [])).toBe("/rhn/manager/api/systems/42/details/audit/cve/none");
  });
});
```

---

### Step 18: Create the manager unit test

File (new): `java/core/src/test/java/com/redhat/rhn/manager/audit/SystemCVEAuditManagerTest.java`
```java
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

import static com.redhat.rhn.testing.ErrataTestUtils.createTestChannel;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestCve;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestErrata;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestInstalledPackage;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestPackage;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestServer;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestUser;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.CallableMode;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.errata.Cve;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.testing.BaseTestCase;
import com.redhat.rhn.testing.SaltTestCaseExtension;
import com.redhat.rhn.testing.TestUtils;

import com.suse.oval.OsFamily;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@ExtendWith(SaltTestCaseExtension.class)
public class SystemCVEAuditManagerTest extends BaseTestCase {

    private static final Set<SystemCVEStatus> ALL = EnumSet.allOf(SystemCVEStatus.class);

    private User user;
    private Channel channel;
    private Server server;
    private String cpe;

    @BeforeEach
    public void setUpSystemCVEAuditManagerTest() throws Exception {
        Config.get().setString(ConfigDefaults.CVE_AUDIT_ENABLE_OVAL_METADATA, "true");
        user = createTestUser();
        channel = createTestChannel(user);
        server = createTestServer(user, Set.of(channel));
        cpe = "cpe:/o:uyunitest:" + TestUtils.randomString(8).toLowerCase() + ":1";
        server.setCpe(cpe);
        server = TestUtils.saveAndFlush(server);
    }

    @AfterEach
    public void tearDownSystemCVEAuditManagerTest() {
        Config.get().setString(ConfigDefaults.CVE_AUDIT_ENABLE_OVAL_METADATA, "false");
    }

    /**
     * Insert one OVAL vulnerable package row through the same procedure used by the OVAL sync.
     *
     * @param productCpe the OVAL platform CPE
     * @param cveName the CVE name
     * @param packageName the package name
     * @param fixVersion the fix version, null for "no fix released"
     * @param fixRelease the fix release, ignored when fixVersion is null
     */
    public static void addOvalVulnerablePackage(String productCpe, String cveName, String packageName,
                                                String fixVersion, String fixRelease) {
        CallableMode mode = ModeFactory.getCallableMode("oval_queries", "add_product_vulnerable_package");
        Map<String, Object> params = new HashMap<>();
        params.put("product_name", productCpe);
        params.put("os_product_family", OsFamily.LEAP.toString());
        params.put("os_product_version", "15.4");
        params.put("cve_name", cveName);
        params.put("package_name", packageName);
        params.put("fix_epoch", fixVersion == null ? null : "0");
        params.put("fix_version", fixVersion);
        params.put("fix_release", fixVersion == null ? null : fixRelease);
        params.put("fix_type", fixVersion == null ? null : "rpm");
        DataResult<Map<String, Object>> batch = new DataResult<>(new ArrayList<>());
        batch.add(params);
        mode.getQuery().executeBatchUpdates(batch);
        TestUtils.flushSession();
    }

    private static String randomCveName() {
        return "CVE-2099-" + TestUtils.randomNumeric(7);
    }

    private static String randomPackageName() {
        return "uyuni-cve-test-" + TestUtils.randomString(8).toLowerCase();
    }

    private void installPackage(String name, String version, String release) {
        Package pkg = createTestPackage(user, channel, "x86_64", name, "0", version, release);
        createTestInstalledPackage(pkg, server);
        TestUtils.flushSession();
    }

    private List<SystemCVEResult> listAll(Set<SystemCVEStatus> statuses) {
        return SystemCVEAuditManager.listCves(user, server, statuses, new PageControl(1, 0));
    }

    @Test
    public void testAffectedNoFix() {
        String cve = randomCveName();
        String pkg = randomPackageName();
        installPackage(pkg, "1.0", "1");
        addOvalVulnerablePackage(cpe, cve, pkg, null, null);

        List<SystemCVEResult> result = listAll(ALL);

        assertEquals(1, result.size());
        assertEquals(cve, result.get(0).getCveName());
        assertEquals(SystemCVEStatus.AFFECTED_NO_FIX, result.get(0).getStatus());
    }

    @Test
    public void testAffectedFixAvailable() {
        String cve = randomCveName();
        String pkg = randomPackageName();
        installPackage(pkg, "1.0", "1");
        addOvalVulnerablePackage(cpe, cve, pkg, "1.0", "2");

        List<SystemCVEResult> result = listAll(ALL);

        assertEquals(1, result.size());
        assertEquals(SystemCVEStatus.AFFECTED_FIX_AVAILABLE, result.get(0).getStatus());
    }

    @Test
    public void testPatched() {
        String cve = randomCveName();
        String pkg = randomPackageName();
        installPackage(pkg, "1.0", "2");
        addOvalVulnerablePackage(cpe, cve, pkg, "1.0", "2");

        List<SystemCVEResult> result = listAll(ALL);

        assertEquals(1, result.size());
        assertEquals(SystemCVEStatus.PATCHED, result.get(0).getStatus());
    }

    @Test
    public void testNotInstalledPackageIsNotListed() {
        addOvalVulnerablePackage(cpe, randomCveName(), randomPackageName(), null, null);

        assertTrue(listAll(ALL).isEmpty());
    }

    @Test
    public void testWorstStatusWins() {
        String pkgA = randomPackageName();
        String pkgB = randomPackageName();
        installPackage(pkgA, "1.0", "1");
        installPackage(pkgB, "1.0", "1");

        String cveNoFix = randomCveName();
        addOvalVulnerablePackage(cpe, cveNoFix, pkgA, null, null);
        addOvalVulnerablePackage(cpe, cveNoFix, pkgB, "1.0", "2");

        String cveFixable = randomCveName();
        addOvalVulnerablePackage(cpe, cveFixable, pkgA, "1.0", "1");
        addOvalVulnerablePackage(cpe, cveFixable, pkgB, "1.0", "2");

        Map<String, SystemCVEStatus> byName = new HashMap<>();
        listAll(ALL).forEach(r -> byName.put(r.getCveName(), r.getStatus()));

        assertEquals(SystemCVEStatus.AFFECTED_NO_FIX, byName.get(cveNoFix));
        assertEquals(SystemCVEStatus.AFFECTED_FIX_AVAILABLE, byName.get(cveFixable));
    }

    @Test
    public void testStatusFilter() {
        String pkg = randomPackageName();
        installPackage(pkg, "1.0", "2");
        String cvePatched = randomCveName();
        String cveFixable = randomCveName();
        addOvalVulnerablePackage(cpe, cvePatched, pkg, "1.0", "2");
        addOvalVulnerablePackage(cpe, cveFixable, pkg, "1.0", "3");

        List<SystemCVEResult> patched = listAll(EnumSet.of(SystemCVEStatus.PATCHED));
        assertEquals(1, patched.size());
        assertEquals(cvePatched, patched.get(0).getCveName());

        assertTrue(listAll(EnumSet.noneOf(SystemCVEStatus.class)).isEmpty());
    }

    @Test
    public void testCveSearchAndPaging() {
        String pkg = randomPackageName();
        installPackage(pkg, "1.0", "1");
        String prefix = "CVE-2098-" + TestUtils.randomNumeric(5);
        for (int i = 0; i < 3; i++) {
            addOvalVulnerablePackage(cpe, prefix + i, pkg, null, null);
        }
        addOvalVulnerablePackage(cpe, randomCveName(), pkg, null, null);

        PageControl pc = new PageControl(1, 2);
        pc.setFilter(true);
        pc.setFilterData(prefix);
        pc.setFilterColumn("cveName");
        pc.setSortColumn("cveName");

        DataResult<SystemCVEResult> page = SystemCVEAuditManager.listCves(user, server, ALL, pc);

        assertEquals(2, page.size());
        assertEquals(3, page.getTotalSize());
        assertEquals(prefix + "0", page.get(0).getCveName());
        assertEquals(prefix + "1", page.get(1).getCveName());
    }

    @Test
    public void testOvalDisabled() {
        String pkg = randomPackageName();
        installPackage(pkg, "1.0", "1");
        addOvalVulnerablePackage(cpe, randomCveName(), pkg, null, null);
        Config.get().setString(ConfigDefaults.CVE_AUDIT_ENABLE_OVAL_METADATA, "false");

        assertEquals(SystemCVEAuditManager.OvalState.DISABLED, SystemCVEAuditManager.getOvalState(server));
        assertTrue(listAll(ALL).isEmpty());
    }

    @Test
    public void testCpeNotCovered() {
        assertEquals(SystemCVEAuditManager.OvalState.NOT_COVERED, SystemCVEAuditManager.getOvalState(server));
        assertTrue(listAll(ALL).isEmpty());
    }

    @Test
    public void testOvalEnabled() {
        addOvalVulnerablePackage(cpe, randomCveName(), randomPackageName(), null, null);

        assertEquals(SystemCVEAuditManager.OvalState.ENABLED, SystemCVEAuditManager.getOvalState(server));
    }

    @Test
    public void testErrataLinksIncludeAllSyncedErrataOfTheOrgOnly() throws Exception {
        String cveName = randomCveName();
        Cve cve = createTestCve(cveName);
        Errata ownErrata = createTestErrata(user, Set.of(cve)); // not in any channel of the server
        User otherUser = createTestUser();
        createTestErrata(otherUser, Set.of(cve)); // other organization, must not be listed

        String pkg = randomPackageName();
        installPackage(pkg, "1.0", "1");
        addOvalVulnerablePackage(cpe, cveName, pkg, "1.0", "2");

        List<SystemCVEResult> result = listAll(ALL);

        assertEquals(1, result.size());
        assertEquals(List.of(new ErrataIdAdvisoryPair(ownErrata.getId(), ownErrata.getAdvisory())),
                result.get(0).getErrata());
    }
}
```

---

### Step 19: Create the controller test

File (new): `java/core/src/test/java/com/suse/manager/webui/controllers/CVEAuditControllerSystemCveTest.java`
```java
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

package com.suse.manager.webui.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.manager.audit.SystemCVEAuditManagerTest;
import com.redhat.rhn.manager.audit.SystemCVEStatus;
import com.redhat.rhn.testing.ErrataTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import spark.ModelAndView;
import spark.Request;

public class CVEAuditControllerSystemCveTest extends BaseControllerTestCase {

    private static final String API_URL = "/manager/api/systems/:sid/details/audit/cve/:statuses";

    @AfterEach
    public void tearDownCVEAuditControllerSystemCveTest() {
        Config.get().setString(ConfigDefaults.CVE_AUDIT_ENABLE_OVAL_METADATA, "false");
    }

    private Server createServerWithAffectedCve(String cveName) throws Exception {
        Channel channel = ErrataTestUtils.createTestChannel(user);
        Server server = ErrataTestUtils.createTestServer(user, Set.of(channel));
        String cpe = "cpe:/o:uyunitest:" + TestUtils.randomString(8).toLowerCase() + ":1";
        server.setCpe(cpe);
        server = TestUtils.saveAndFlush(server);

        String pkgName = "uyuni-cve-test-" + TestUtils.randomString(8).toLowerCase();
        Package pkg = ErrataTestUtils.createTestPackage(user, channel, "x86_64", pkgName, "0", "1.0", "1");
        ErrataTestUtils.createTestInstalledPackage(pkg, server);
        TestUtils.flushSession();

        SystemCVEAuditManagerTest.addOvalVulnerablePackage(cpe, cveName, pkgName, "1.0", "2");
        return server;
    }

    @Test
    public void testSystemCveViewUsesTemplate() throws Exception {
        Config.get().setString(ConfigDefaults.CVE_AUDIT_ENABLE_OVAL_METADATA, "false");
        Server server = ErrataTestUtils.createTestServer(user);
        Request request = getRequestWithCsrf("/manager/systems/details/audit/cve");

        ModelAndView mv = CVEAuditController.systemCveView(request, response, user, server);

        assertEquals("templates/minion/system-cve.jade", mv.getViewName());
        assertEquals("DISABLED", ((Map<?, ?>) mv.getModel()).get("ovalState"));
    }

    @Test
    public void testSystemCvesReturnsPagedJson() throws Exception {
        Config.get().setString(ConfigDefaults.CVE_AUDIT_ENABLE_OVAL_METADATA, "true");
        String cveName = "CVE-2099-" + TestUtils.randomNumeric(7);
        Server server = createServerWithAffectedCve(cveName);

        Request request = getRequestWithCsrfAndParams(API_URL,
                Map.of("p", "1", "ps", "10", "s", "1", "sc", "status"),
                server.getId(), "AFFECTED_FIX_AVAILABLE,PATCHED");
        String json = CVEAuditController.systemCves(request, response, user, server);

        JsonObject result = JsonParser.parseString(json).getAsJsonObject();
        assertEquals(1, result.get("total").getAsLong());
        JsonObject item = result.getAsJsonArray("items").get(0).getAsJsonObject();
        assertEquals(cveName, item.get("cveName").getAsString());
        assertEquals("AFFECTED_FIX_AVAILABLE", item.get("status").getAsString());
    }

    @Test
    public void testSystemCvesStatusFilterExcludesRows() throws Exception {
        Config.get().setString(ConfigDefaults.CVE_AUDIT_ENABLE_OVAL_METADATA, "true");
        Server server = createServerWithAffectedCve("CVE-2099-" + TestUtils.randomNumeric(7));

        Request request = getRequestWithCsrfAndParams(API_URL,
                Map.of("p", "1", "ps", "10"), server.getId(), "PATCHED");
        String json = CVEAuditController.systemCves(request, response, user, server);

        assertEquals(0, JsonParser.parseString(json).getAsJsonObject().get("total").getAsLong());
    }

    @Test
    public void testParseSystemCveStatuses() {
        assertEquals(EnumSet.of(SystemCVEStatus.PATCHED, SystemCVEStatus.AFFECTED_NO_FIX),
                CVEAuditController.parseSystemCveStatuses("PATCHED, AFFECTED_NO_FIX,bogus"));
        assertEquals(EnumSet.noneOf(SystemCVEStatus.class), CVEAuditController.parseSystemCveStatuses("none"));
        assertEquals(EnumSet.noneOf(SystemCVEStatus.class), CVEAuditController.parseSystemCveStatuses(null));
    }
}
```

---

## 4. Verification

Run every command from the worktree root unless a `cd` is shown.

**4.1 Static checks (Java)**
```bash
cd java && ant -f manager-build.xml checkstyle
```
This must finish with `BUILD SUCCESSFUL` and no new checkstyle violations in the files touched above.

**4.2 Java unit tests.** These need the Uyuni PostgreSQL test database or test container, as for any `BaseTestCase` test.
```bash
printf '**/SystemCVEAuditManagerTest.class\n**/CVEAuditControllerSystemCveTest.class\n**/CVEAuditManagerOVALTest.class\n' > "$TMPDIR/cve-tests"
cd java && ant -f manager-build.xml -Dtests.includes="$TMPDIR/cve-tests" test
```
All tests in these 3 classes must pass. `CVEAuditManagerOVALTest` is a regression check for the Step 1 visibility change.

**4.3 Frontend.**
```bash
cd web && npm run tsc
cd web && npm run lint
cd web && npm test -- system-cve
```
- `tsc` must report no errors.
- `lint` auto-fixes formatting. If it changes any of the new files, keep those changes.
- The jest suites `system-cve.renderer.test.tsx` and `system-cve.test.tsx` must pass.

**4.4 Manual check on a dev server.** Deploy with `cd java && ant -f manager-build.xml deploy-restart` and `cd java && ant -f manager-build.xml deploy-static-resources`.
1. Set `java.cve_audit.enable_oval_metadata = true` and run the `oval-data-sync` taskomatic bunch.
2. Open a SLES/Leap 15 minion's System Details. Clicking **Audit** must open **CVE**, which is listed first, before OpenSCAP and Confidential Computing. You should see:
   - three status checkboxes, all checked
   - the "Filter by CVE" search
   - a paged table with Status / CVE / Patches columns
   - Patches links that open `/rhn/errata/details/Details.do?eid=...`
3. Unchecking a status reloads the table without those rows. Typing `CVE-2024-` filters the rows. Sorting by Status and CVE works.
4. With OVAL disabled, or for a system whose CPE is not covered, the page shows only the info message.
5. The Tomcat startup log has no `RbacRouteValidator` warnings about `/manager/systems/details/audit/cve` or `/manager/api/systems/:sid/details/audit/cve/:statuses`. A non-admin user in a default access group can open the tab.

**Definition of done**
- Every file in section 2 is created or modified exactly as described.
- Checkstyle, the 3 Java test classes, `tsc`, `lint` and the 2 jest suites all pass.
- The manual check in 4.4 behaves as described.
- No files outside section 2 have been changed. Check with `git status`.
