# Hub Support Test Plan — v2

**Product:** SUSE Multi-Linux Manager 5.2 and later / Uyuni
**Feature:** Hub Deployment with Peripheral Servers (Hub Online Sync / ISSv3, Hub XMLRPC API, ISSv2, Hub Reporting)
**References:**
- https://documentation.suse.com/multi-linux-manager/5.2/en/docs/specialized-guides/large-deployments/hub-multi-server.html
- https://documentation.suse.com/multi-linux-manager/5.2/en/docs/specialized-guides/large-deployments/hub-online-sync.html
- https://documentation.suse.com/multi-linux-manager/5.2/en/docs/specialized-guides/large-deployments/hub-api.html
- https://documentation.suse.com/multi-linux-manager/5.2/en/docs/specialized-guides/large-deployments/hub-namespaces.html
- https://documentation.suse.com/multi-linux-manager/5.2/en/docs/specialized-guides/large-deployments/hub-auth.html
- https://documentation.suse.com/multi-linux-manager/5.2/en/docs/specialized-guides/large-deployments/hub-reporting.html
- https://documentation.suse.com/multi-linux-manager/5.2/en/docs/specialized-guides/large-deployments/hub-install.html
- https://documentation.suse.com/multi-linux-manager/5.2/en/docs/specialized-guides/large-deployments/iss_v2.html

**Environment (testsuite host roles → environment variables):**

| Role | Testsuite host | Env var | Notes |
|---|---|---|---|
| Hub | `server` | `SERVER` | Deployed with `mgradm install podman --hubxmlrpc-replicas 1` |
| Peripheral 1 | `peripheral1` | `SERVER2` | Same CA as the hub (certificate generated on the hub with `rhn-ssl-tool`) |
| Peripheral 2 | `peripheral2` | `SERVER3` | Its own CA — for the token + root CA scenario |
| Proxy | `proxy`, `proxy2`, `proxy3` | `PROXY`, `PROXY2`, `PROXY3` | Registered to a peripheral |
| Minion(s) | `sle_minion`, … | `MINION`, … (or the versioned fallbacks, e.g. `SLES15SP7_MINION`) | Bootstrapped to a peripheral, directly or via a proxy |
| Monitoring | `monitoring_server` | `MONITORING_SERVER` | Minion of the hub; runs Grafana + Prometheus via formula, hosts the hub reporting dashboards on port 3000 |

Below, `<hub>`, `<peripheral1>`, `<monitoring>` etc. stand for the FQDN of the host in that role.

**Run sets:**
- `run_sets/hub_server_to_server.yml` — Focus A (hub + peripherals only; C-01 needs a minion).
- `run_sets/hub_full_topology.yml` — everything above plus Focus B and C-01 (needs proxy and minion).
- `run_sets/hub_build_validation/*.yml` — per-peripheral BV features under `features/hub/build_validation/` (server setup, channel sync, activation key, bootstrap repo, proxy, minions, smoke tests).

**Terminology used in this plan:**
- **ISSv3 / Hub Online Synchronization** — the 5.2 primary mechanism: peripheral registration via web UI / API, tokens, channel sync from hub repositories.
- **ISSv2** — the `inter-server-sync` CLI export/import path (offline capable).
- **Hub XMLRPC API** — the Go `hub-xmlrpc-api` gateway (`/hub/rpc/api`) with `hub`, `unicast`, `multicast` namespaces.

**Automation status:** each test case has an **Automation** line — *automated* (all checks), *partial* (gaps listed), or *manual*.

---

# FOCUS AREA A — Server ↔ Server (Hub / Peripheral)

## A-01 — Hub Deployment & Configuration Verification

**Goal:** Confirm the hub XMLRPC container and `hub.conf` are correctly deployed.

**Steps:**
```bash
# On the hub host
podman ps | grep hub-xmlrpc
podman exec uyuni-hub-xmlrpc-0 cat /etc/hub/hub.conf
podman exec uyuni-hub-xmlrpc-0 env | grep -i hub
cat /etc/systemd/system/uyuni-hub-xmlrpc.service.d/local.conf 2>/dev/null || echo "no override"
curl -sk https://<hub>/hub/rpc/api
```

**Expected:**
- Container `uyuni-hub-xmlrpc-0` running.
- `hub.conf` contains `HUB_API_URL`, `HUB_CONNECT_TIMEOUT`, `HUB_REQUEST_TIMEOUT`, `HUB_CONNECT_USING_SSL`; env vars from the systemd unit override the file.
- Endpoint answers `rpc: POST method required, received GET`.

**Automation:** automated — `features/hub/srv_hub_setup_registration.feature`.

---

## A-02 — Peripheral Registration: Method 1 — Administrator User/Password (direct from hub)

**Goal:** Register a peripheral directly from the hub with no interaction on the peripheral.

**Steps (Web UI):**
1. Hub: **Admin → Hub Configuration → Peripherals Configuration → Add peripheral**.
2. `Peripheral Server FQDN` = peripheral 1 FQDN.
3. `Registration mode` = **Administrator User/Password**; enter the administrator credentials of the peripheral.
4. `Root CA certificate` = **Not needed** (same CA).
5. Click **Register**.

**Expected:**
- Peripheral appears in **Peripherals Configuration**.
- Peripheral appears in hub **Systems → System List** as `Foreign` system type (until bootstrapped as minion).
- On the peripheral, **Admin → Hub Configuration → Hub Details** is populated (hub FQDN, registration date, mirror credentials).
- Peripheral **Admin → Setup Wizard** pages (Organization Credentials / Products / PAYG) show the "managed via hub" notification.

**Negative cases:**
- Wrong password → registration rejected with clear error; no partial state left behind.
- Non-admin user credentials → rejected.
- Re-registering an already-registered peripheral → proper error (a peripheral can only have one hub).

**Automation:** automated — `features/hub/srv_hub_setup_registration.feature` (also installs peripheral 1 with hub-signed certificates).

---

## A-03 — Peripheral Registration: Method 2 — Existing Token (same CA)

**Goal:** Register using a token generated on the peripheral, with `Root CA certificate = Not needed`.

**Steps:**
1. Peripheral: **Admin → Hub Configuration → Access Tokens → Add token → Issue new token**; `Server FQDN` = hub FQDN; **Issue**; copy the token (only shown once).
2. Hub: **Peripherals Configuration → Add peripheral**; `Registration mode` = **Existing token**; paste token; Root CA = **Not needed**; **Register**.

**Expected:**
- Registration succeeds; token shows as consumed/issued correctly on both sides (**Access Tokens** screens).
- Channel sync and reporting work afterwards (verified in A-06 / C-01).

**Negative cases:**
- Token issued for a different FQDN → rejected.
- Reusing an already-consumed token → rejected.
- Invalidated token → rejected.

**Automation:** automated — `features/hub/srv_hub_token_registration.feature`.

---

## A-04 — Peripheral Registration: Method 3 — Token + Root CA certificate (different CA)

**Goal:** Register peripheral 2 (different CA than the hub) providing the root CA during registration.

**Steps:**
1. Generate a token on peripheral 2 (as in A-03).
2. Hub: **Add peripheral** → `Existing token` → for `Root CA certificate` test both:
   - **Upload a file** (peripheral 2's `RHN-ORG-TRUSTED-SSL-CERT`)
   - **Paste the data**
3. Register.

**Expected:**
- Registration succeeds with no TLS errors; hub↔peripheral communication (channel sync, reporting) works.
- Peripheral Details on the hub shows `Root Certificate Authority` populated; **Download Root CA** works and matches the uploaded cert.
- Editing/deleting the certificate behaves as documented (deleting breaks the connection — verify and restore).

**Negative cases:**
- Registering peripheral 2 with Root CA = **Not needed** → TLS failure with a clear error.
- Uploading a wrong/garbage certificate → registration or subsequent sync fails cleanly.

**Automation:** partial — `features/hub/srv_hub_token_registration.feature` covers upload, paste and the missing-CA negative case. Not automated: **Download Root CA** comparison, editing/deleting the certificate, garbage certificate.

---

## A-05 — Access Token Lifecycle

**Goal:** Verify token operations: issue, consume, invalidate, reactivate, delete.

**Steps / Expected:**
- Tokens are listed under **Access Tokens** on both servers with correct `Consumed` / `Issued` perspective.
- **Invalidate** an active token → hub↔peripheral communication stops until reactivated; reactivation restores it.
- **Delete** is only possible when the associated server is not registered as hub or peripheral; delete is permanent.
- Token value is only displayed at creation time.

**Automation:** partial — `features/hub/srv_hub_token_lifecycle.feature` covers consumed listing, invalidation and its effect on communication, and re-registration with a fresh token. Not automated: reactivation (the feature deregisters instead), delete rules.

---

## A-06 — Channel Synchronization Hub → Peripheral (ISSv3 online sync)

**Goal:** Verify vendor and custom channels sync from the hub to peripherals.

**Prerequisites:** Products synced on the hub (e.g. SLES 15 SP7); one custom channel with a few packages.

**Method A — Hub web UI:** Peripheral details → **Synchronized Channels → Edit Channels** → select product/custom channel → for custom channels select target organization → **Apply → Confirm**.
**Method B — Peripheral web UI:** **Admin → Hub Configuration → Hub Details → Sync Channels → Schedule**.
There is no `sync.hub.*` API for this in 5.2: sync is UI/event-driven under ISSv3.

**Expected:**
- Channels created on the peripheral and mirrored on next repo sync; packages downloaded **from the hub**, not from SCC/original custom URL.
- Custom channel lands in the selected peripheral organization; org dropdown only offered for channels not yet existing on the peripheral.
- `N. of synced channels` / `N. of synced organizations` columns update on the hub.
- **Regenerate Credentials** (mirror credentials) works and sync still succeeds afterwards.
- Long repository URLs: `rhnContentSource.source_url` is `VARCHAR(2048)`; a longer URL must not cause an HTTP 500.

**Automation:** partial — `features/hub/srv_hub_channel_synchronization.feature` covers Method A, cloned/vendor/custom channels, org mapping and credential regeneration. Not automated: synced channel/org counters, long `source_url`.

---

## A-07 — Hub XMLRPC API: Authentication Modes

**Goal:** Verify the three auth modes against `/hub/rpc/api`.

**Expected:**
- `hub.login` → session key.
- `hub.loginWithAuthRelayMode` → session key.
- `hub.loginWithAutoconnectMode` → `{"SessionKey": ..., "Successful": {...}, "Failed": {...}}`.

**Automation:** automated — `features/hub/srv_hub_xmlrpc_operations.feature`.

---

## A-08 — Hub XMLRPC API: Namespaces

**Goal:** Verify `hub`, `multicast`, `unicast` namespaces and direct pass-through.

**Expected:**
- `hub.listServerIds` returns all registered peripherals.
- `multicast.system.list_systems` returns `Successful`/`Failed` buckets covering all server IDs.
- `unicast.system.list_systems` returns data for one peripheral.
- `system.list_systems` on `/rpc/api` returns the hub's own systems.

**Automation:** automated — `features/hub/srv_hub_xmlrpc_operations.feature`.

---

## A-09 — ISSv2: `inter-server-sync` Export / Import

**Goal:** Verify CLI-based content export from the hub and import on a peripheral.

**Prerequisites:** `inter-server-sync` installed in both server containers; identical MLM versions; at least one org with the exact same (case-sensitive) name on both; one synced channel; `/var/spacewalk` bind-mounted.

**Steps (manual reference):**
```bash
# Hub
podman exec uyuni-server inter-server-sync export -c <channel-label> -o /var/spacewalk/iss-export-test
rsync -avz /var/spacewalk/iss-export-test/ root@<peripheral1>:/var/spacewalk/iss-import-test/
# Peripheral
podman exec uyuni-server inter-server-sync import -d /var/spacewalk/iss-import-test
```

**Expected:**
- Export/rsync/import all exit 0; channel appears in `channel.listAllChannels` on the peripheral.
- Version parity warning if versions differ; org-name case mismatch reported as prerequisite failure.

**Automation:** partial — `features/hub/srv_hub_issv2_export_import.feature` covers the positive path. Not automated: version-mismatch and org-case-mismatch negative cases.

---

## A-10 — Deregistration (both directions)

**Goal:** Verify clean deregistration and its side effects.

**Steps / Expected:**
- Peripheral side: **Hub Details → Deregister** → Hub Details page becomes empty; the hub no longer lists the peripheral.
- Hub side: **Peripherals Configuration → Deregister** → peripheral removed from list; peripheral's Hub Details cleared.
- After deregistration: synced channels remain on the peripheral but stop mirroring from the hub; the peripheral can be re-registered (to the same or another hub) without leftovers blocking it.
- Tokens associated with the deregistered pair become deletable.

**Automation:** partial — `features/hub/srv_hub_verification_cleanup.feature` (currently commented out in `hub_full_topology.yml`). Not automated: token deletability after deregistration.

---

# FOCUS AREA B — Server + Peripheral + Proxy + Minion

## B-01 — Peripheral Host Managed as Hub Minion

**Goal:** Verify the documented ordering: bootstrap the peripheral host as a hub minion **before** peripheral registration, and the duplicate-entry behavior when done after.

**Steps / Expected:**
- Bootstrap the peripheral host as a minion of the hub, then register it as peripheral → single system entry, correct system type.
- Register a peripheral first (shows as `Foreign`), then bootstrap it → **two entries** appear in the systems list (documented behavior) — record actual behavior and any cleanup path.

**Automation:** partial — `features/hub/srv_hub_peripheral_as_minion.feature` covers minion-before-registration. Not automated: registration-before-minion (duplicate entries).

---

## B-02 — Proxy Attached to a Peripheral

**Goal:** Verify a containerized proxy can be deployed and registered against a peripheral server in a hub topology.

**Steps:**
1. Deploy the proxy with `mgrpxy` using a config generated on the peripheral (**Systems → Proxy Configuration**), with the correct CA chain (hub CA for peripheral 1).
2. Verify the proxy appears in the peripheral's system list with proxy system type.

**Expected:** Proxy registers to the peripheral; TLS chain valid end-to-end (minion → proxy → peripheral → hub for content).

**Automation:** automated (BV) — `features/hub/build_validation/prh1_proxy2.feature`, `prh2_proxy3.feature`.

---

## B-03 — Minion Bootstrap to Peripheral (direct and via proxy)

**Goal:** Verify minions register to a peripheral, with channels that were synced from the hub.

**Steps:**
1. Peripheral: create an activation key with hub-synced channels.
2. Bootstrap a minion directly to the peripheral (**Systems → Bootstrapping**).
3. Bootstrap a second minion (or re-bootstrap) through the proxy from B-02.

**Expected:**
- Minion(s) appear in the peripheral's system list, managed independently from the hub; the hub does **not** list them.
- Bootstrap repo on the peripheral is populated from hub-synced channels.
- Via-proxy minion shows the proxy in its connection path.

**Automation:** automated — direct: `features/hub/srv_hub_minion_on_peripheral.feature` (hub not listing the minion: `srv_hub_xmlrpc_operations.feature`); via proxy: `features/hub/build_validation/prh*_*_minion.feature`.

---

## B-04 — End-to-End Client Operations on Hub-Synced Content

**Goal:** Verify the full content path hub → peripheral → (proxy) → minion.

**Steps / Expected:**
- Install a package on the minion from a hub-synced channel → package downloads succeed (served via peripheral/proxy).
- Apply a patch/errata available in the synced channel → action completes.
- Run a remote command and highstate from the peripheral → success.
- Package hashes on the minion match those on the hub (content integrity through the chain).

**Automation:** automated — `features/hub/srv_hub_minion_on_peripheral.feature`; per-OS smoke tests in `features/hub/build_validation/prh*_*_smoke_tests.feature`.

---

## B-05 — Resilience: Hub Unavailable

**Goal:** Verify peripherals and their clients keep working when the hub is down.

**Steps / Expected:**
- Stop hub services → minion operations against the peripheral (package install from already-mirrored channel, remote commands) still succeed.
- Channel sync from the hub fails gracefully with a clear error/notification, and recovers automatically once the hub is back.

**Automation:** partial — `features/hub/srv_hub_outage_resilience.feature` (must run last: it stops and starts hub services) covers sync failure and recovery. Not automated: minion operations while the hub is down.

---

# FOCUS AREA C — Hub Reporting

## C-01 — reportdb Aggregation from Peripherals

**Goal:** Verify reporting data aggregates from all peripherals into the hub's reportdb.

**Prerequisites:** Peripherals registered (A-02/A-03/A-04); port 5432 reachable hub → peripherals.

**Steps:**
1. Trigger `update-reporting-default` on each peripheral, then `update-reporting-hub-default` on the hub (**Admin → Task Schedules**).

**Expected:**
- `reportdb` schema present on the hub (≈35 tables).
- `SELECT mgm_id, count(*) FROM system GROUP BY mgm_id` → one row per peripheral plus the hub's own `mgm_id`; counts match each peripheral's system list (including minions from Focus B).
- `synced_date` recent; re-running the schedule refreshes it.
- Registration method (password vs token vs token+CA) makes no difference to reporting.

**Automation:** partial — `features/hub/srv_hub_reporting.feature` covers per-peripheral rows and `synced_date` freshness. Not automated: per-peripheral count comparison, registration-method independence.

---

## C-02 — Monitoring Server: Grafana Formula Setup (Hub mode)

**Goal:** Deploy the reporting stack on the monitoring server via the Grafana formula, with hub-mode Report DB dashboards.

**Prerequisites:** Monitoring host bootstrapped as a minion of the hub; C-01 passed (hub reportdb populated).

**Steps:**
1. Hub web UI: **Systems → `<monitoring>` → Formulas** → enable **Grafana** → **Save**.
2. **Formulas → Grafana** tab, configure:
   - `Enabled` ✓, default administrator user/password set.
   - **Datasources → Prometheus data source 1**: name `Prometheus`, URL `http://<monitoring>:9090`.
   - **Report DB**: `Enabled` ✓ **and** `This Report DB is for a Hub server` ✓ — this is the hub-specific switch under test.
   - **Dashboards**: MLM server, MLM clients, PostgreSQL, Apache HTTPD ✓ (Kubernetes / SAP unchecked).
3. **Save Formula → Apply Highstate**; wait for the highstate action to complete.

**Expected:**
- Highstate succeeds; `grafana-server` running on the monitoring host; `http://<monitoring>:3000/api/health` returns `{"database": "ok"}`.
- Report DB datasource is provisioned automatically (no manual `uyuni-setup-reportdb-user` / datasource creation needed) and points at the **hub's** reportdb; **Save & Test** passes.
- Port 5432 reachable from the monitoring host to the hub.

**Negative/variant checks:**
- Re-applying the highstate is idempotent (no duplicate datasources/dashboards).
- Changing the admin password in the formula and re-applying updates Grafana.

**Automation:** partial — `features/hub/srv_hub_grafana_setup.feature` (needs a monitoring node; not in any run set yet). Not automated: admin password change.

---

## C-03 — Dashboard Provisioning (Hub mode)

**Goal:** Verify the three hub reporting dashboards are auto-provisioned.

**Steps:** Browse `http://<monitoring>:3000` → **Dashboards → Reporting**.

**Expected — exactly these three dashboards present:**
1. **SUSE Multi-Linux Manager Fleet Overview & Security**
2. **SUSE Multi-Linux Manager Hub Overview**
3. **SUSE Multi-Linux Manager Reports & History**

Also verify: cross-dashboard navigation links work (Hub Overview → `Fleet Security (Hub)` and `Reports (Hub)` buttons; Fleet Overview → `Reports & History`), and each dashboard loads without datasource errors.

**Automation:** partial — `features/hub/srv_hub_grafana_dashboards.feature`. Not automated: cross-dashboard navigation links.

---

## C-04 — Fleet Overview & Security: Data Validation

**Goal:** Verify every populated panel matches the source of truth (hub reportdb / hub & peripheral web UIs).

**Cross-check method:** for each stat, compare the Grafana value against a reportdb query on the hub (`podman exec uyuni-db psql -U pythia_susemanager -d reportdb -c "..."`) and/or the web UI.

**Checks:**
- **Executive Overview** stat tiles: `Total Systems` (= `SELECT count(*) FROM system`), `Total Proxies`, `Total Channels`, `Outstanding Patches`, `Systems Needing Update`, `Inactive Systems (>7d)`, `Critical Security Patches`, `SCAP Compliance %` — each matches its reportdb query and is consistent with the fleet (hub + peripherals + their minions).
- **Fleet Composition**: OS family / architecture / virtual-vs-physical / hardware-vendor distributions and Top-15 kernel versions sum to `Total Systems`; `Systems by Organization` matches org counts. Investigate any `Unknown` buckets — determine whether missing hardware/OS data is expected (e.g. foreign/peripheral entries) or a bug.
- **Security & CVE Analysis**: pending patch counters by severity, `Patches Requiring Reboot`, `Total CVEs Referenced`, `Errata by Type`, `Security Errata by Severity`, `Top 20 Most Exposed Systems`, `Errata with CVE Details`, `Systems Affected by Security Errata`, `Critical Security Patches — Full Detail`. Panels must be consistent with the stat tiles: an empty panel next to a non-zero tile (e.g. Critical Security Patches = 1) points to missing data, a broken query or a filter mismatch.
- **System Currency Score**: ranking table populated; per-system critical/important/moderate/low/bugfix/enhancement counts match the system currency report; score ordering correct.
- **System Health & Patch Compliance**: `Outdated Systems Overview` and `Systems Requiring Reboot` rows match per-system patch status; `Inactive Systems (>7d)` entries match last check-in times.
- **Collapsed sections** (`Proxy Infrastructure`, `Virtualization`) expand and populate once a proxy exists (Focus B) — with `Total Proxies = 0` before B-02 and `1` after.
- **Variables/filters**: `Managed Server` (This Server vs a peripheral), `Organization`, `System Group`, `Proxy` filter all panels consistently; time range changes (Last 30 days → other) refresh data.

**Automation:** partial — `features/hub/srv_hub_grafana_data_validation.feature` checks the hub reportdb behind the panels (system and channel counts are positive). Comparing panel values with reportdb is manual.

---

## C-05 — Hub Overview: Data Validation

**Goal:** Verify the hub-level aggregation dashboard.

**Checks:**
- **Hub Executive Summary**: `Peripheral Servers` = number of peripherals registered on the hub; `Total Managed Systems`, `Total Outstanding Patches`, `Critical Security Patches (Fleet)`, `Inactive Systems Fleet-wide` match reportdb.
- **Per-Peripheral Overview** table: one row per registered peripheral with `total_systems`, `inactive_7d`, `outstanding_patches`, `critical_patches`; `Total` row sums correctly; peripheral FQDN links behave correctly.
- **System Inventory (All Systems)**: `type` column distinguishes `Peripheral` vs `System`; `managed_by` shows `Hub` for hub-managed entries and the **peripheral FQDN** for minions bootstrapped to a peripheral; `last_checkin_time` and `registration_time` plausible and consistent with the web UI.
- **Dynamics**: register/deregister a peripheral (A-02/A-10) and bootstrap/remove a minion (B-03) → counts and rows update after the next reporting sync (C-01 schedules).
- **Fleet-wide Security Analysis** collapsed section expands and populates.

**Automation:** partial — `features/hub/srv_hub_grafana_data_validation.feature` checks the hub reportdb behind the panels (systems from peripherals, hub- and peripheral-managed entries). Not automated: comparing panel values, fleet totals, dynamics, Fleet-wide Security Analysis.

---

## C-06 — Reports & History: Data Validation

**Goal:** Verify the legacy-reports-parity dashboard.

**Checks (spot-check each section against hub reportdb / web UI):**
- **Channels & Packages**: channel overview counts, Top-20 channels by package count, installed packages, available package updates, custom channels, cloned channels — match `Software → Channel List` and reportdb.
- **Errata & Channel Cross-Reference**: errata-in-channels and errata-affecting-systems (with IP) rows match the patches views; extra packages list plausible.
- **Actions & Operations**: `100 Latest Actions` shows recent actions (highstates, package installs from Focus B) with correct status; `Action Status Distribution` percentages match; `Failed Actions (Recent)` lists real failures; `Last 100 Package/Patch Operations` matches the schedule history. **This is the "list last tasks" check** — trigger a fresh action (e.g. a highstate) and verify it appears after refresh/reporting sync.
- **System Inventory (Full Reports)** + **Hardware Details**: rows per system with IP, registered_by, check-in/boot times, kernel; hardware (cpus/sockets/model/ram) matches the systems' hardware pages.
- **User Management & Audit**: user accounts, roles, creation/last-login times match **Users** on the hub; `User System Access Map` consistent; MD5-password panel expected empty.
- **System History**: system event history (registrations, entitlements, credential changes), package operation history, OpenSCAP scan history — entries match events pages; other history panels ("No data") populate after the corresponding action type is executed (channel subscription change, config mgmt, autoinstallation).
- **SCAP Compliance**: scan results overview, compliance rate per profile, rule failures — match the OpenSCAP scan details on the peripheral/hub.
- **Empty-state behavior**: panels with no data show a clean "No data" (no query errors).
- **Filters**: `Managed Server`, `Organization`, `System Group`, `Proxy` variables and time range behave as in C-04.

**Automation:** partial — `features/hub/srv_hub_grafana_data_validation.feature` checks the hub reportdb for the fresh highstate action and the admin account. Everything else is manual.

---

## C-07 — Grafana Reporting: Single-Server Mode (non-hub)

**Goal:** Same formula on a standalone server with `This Report DB is for a Hub server` **unchecked** — validate the non-hub variant.

**Steps / Expected:**
- Formula applies; Report DB datasource targets the local reportdb; `SELECT DISTINCT mgm_id FROM system` → single row `mgm_id = 1`.
- Verify which dashboards are provisioned in this mode (Hub Overview should be absent or empty — record actual behavior); Fleet Overview and Reports & History render local data only.
- **Help → Report Database Schema** accessible and complete on the server web UI.

**Automation:** manual.

---

# 10 Hub Features to Test (split by focus)

**Server ↔ Server focus (5):**
1. **Peripheral registration matrix** — all three methods (admin user/password, existing token, token + root CA upload/paste) with positive and negative paths (wrong password, consumed/invalidated token, wrong FQDN, missing CA on cross-CA setup). → A-02/03/04
2. **Access token lifecycle** — issue/consume/invalidate/reactivate/delete semantics and their effect on hub↔peripheral communication. → A-05
3. **Hub XMLRPC API** — three authentication modes and the hub/unicast/multicast/pass-through namespaces against multiple peripherals. → A-07/08
4. **ISSv3 channel synchronization** — vendor + custom channels via hub UI and peripheral UI; org mapping for custom channels; mirror credential regeneration; hub replacing SCC as content source. → A-06
5. **ISSv2 CLI export/import** — offline path with version/org parity prerequisites; channel present on the peripheral post-import. → A-09

**Server + Peripheral + Proxy + Minion focus (3):**
6. **Peripheral-as-minion topology** — bootstrap ordering (minion before/after peripheral registration), Foreign vs minion system types, duplicate-entry behavior. → B-01
7. **Full content chain** — proxy attached to a peripheral, minions bootstrapped directly and via proxy, package/patch/highstate operations using hub-synced channels only. → B-02/03/04
8. **Deregistration & hub-outage resilience** — deregister from both sides with clean state, and peripheral/minion operations continuing while the hub is down. → A-10 / B-05

**Hub Reporting focus (2):**
9. **reportdb aggregation** — per-peripheral `mgm_id` rows, task-schedule-driven refresh, `synced_date` freshness, counts matching peripheral system lists regardless of registration method. → C-01
10. **Grafana reporting frontend** — formula-based deployment on the monitoring server (Report DB hub mode), the three provisioned dashboards (Fleet Overview & Security, Hub Overview, Reports & History) with data validation against reportdb, and single-server (`mgm_id = 1`) mode. → C-02..C-07
