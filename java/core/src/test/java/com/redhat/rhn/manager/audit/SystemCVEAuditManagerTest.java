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
import com.redhat.rhn.domain.channel.ChannelFactoryTest;
import com.redhat.rhn.domain.channel.ChannelTestUtility;
import com.redhat.rhn.domain.errata.Cve;
import com.redhat.rhn.domain.errata.CveFactory;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.testing.BaseTestCase;
import com.redhat.rhn.testing.SaltTestCaseExtension;
import com.redhat.rhn.testing.TestUtils;

import com.suse.oval.OsFamily;
import com.suse.oval.OvalSeverity;

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

    private static final Set<PatchStatus> ALL = EnumSet.allOf(PatchStatus.class);

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
        addOvalVulnerablePackage(productCpe, cveName, packageName, fixVersion, fixRelease, OvalSeverity.UNKNOWN);
    }

    /**
     * Insert one OVAL vulnerable package row with a severity, through the procedure used by the OVAL sync.
     *
     * @param productCpe the OVAL platform CPE
     * @param cveName the CVE name
     * @param packageName the package name
     * @param fixVersion the fix version, null for "no fix released"
     * @param fixRelease the fix release, ignored when fixVersion is null
     * @param severity the OVAL severity of the CVE
     */
    public static void addOvalVulnerablePackage(String productCpe, String cveName, String packageName,
                                                String fixVersion, String fixRelease, OvalSeverity severity) {
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
        params.put("severity", severity.toDbValue());
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

    private Package installPackage(String name, String version, String release) {
        Package pkg = createTestPackage(user, channel, "x86_64", name, "0", version, release);
        createTestInstalledPackage(pkg, server);
        TestUtils.flushSession();
        return pkg;
    }

    /**
     * Publish an errata fixing the CVE with a newer package in the given channel, then refresh the CVE channels.
     */
    private Errata publishFix(Channel target, String cveName, String packageName, String version, String release)
            throws Exception {
        // The OVAL sync procedure has already created the CVE, once the vulnerable package was added
        Cve cve = CveFactory.lookupByName(cveName);
        if (cve == null) {
            cve = createTestCve(cveName);
        }
        Errata errata = createTestErrata(user, Set.of(cve));
        target.addErrata(errata);
        TestUtils.saveAndFlush(target);
        createTestPackage(user, errata, target, "x86_64", packageName, "0", version, release);
        CVEAuditManager.populateCVEChannels();
        TestUtils.flushSession();
        return errata;
    }

    private List<SystemCVEResult> listAll(Set<PatchStatus> statuses) {
        return SystemCVEAuditManager.listCves(server, statuses, Set.of(), new PageControl(1, 0));
    }

    private SystemCVEResult single() {
        List<SystemCVEResult> result = listAll(ALL);
        assertEquals(1, result.size());
        return result.get(0);
    }

    @Test
    public void testAffectedNoFix() {
        String cve = randomCveName();
        String pkg = randomPackageName();
        installPackage(pkg, "1.0", "1");
        addOvalVulnerablePackage(cpe, cve, pkg, null, null);

        SystemCVEResult result = single();
        assertEquals(cve, result.getCveName());
        assertEquals(PatchStatus.AFFECTED_PATCH_UNAVAILABLE, result.getStatus());
    }

    @Test
    public void testFixNotInSyncedChannels() {
        String pkg = randomPackageName();
        installPackage(pkg, "1.0", "1");
        addOvalVulnerablePackage(cpe, randomCveName(), pkg, "1.0", "2");

        SystemCVEResult result = single();
        assertEquals(PatchStatus.AFFECTED_PATCH_UNAVAILABLE_IN_UYUNI, result.getStatus());
        assertTrue(result.getErrata().isEmpty());
    }

    @Test
    public void testPatched() {
        String pkg = randomPackageName();
        installPackage(pkg, "1.0", "2");
        addOvalVulnerablePackage(cpe, randomCveName(), pkg, "1.0", "2");

        assertEquals(PatchStatus.PATCHED, single().getStatus());
    }

    @Test
    public void testNotInstalledPackageIsNotListed() {
        addOvalVulnerablePackage(cpe, randomCveName(), randomPackageName(), null, null);

        assertTrue(listAll(ALL).isEmpty());
    }

    @Test
    public void testFixAvailableInAssignedChannel() throws Exception {
        String cve = randomCveName();
        String pkg = randomPackageName();
        installPackage(pkg, "1.0", "1");
        addOvalVulnerablePackage(cpe, cve, pkg, "1.0", "2");
        Errata errata = publishFix(channel, cve, pkg, "1.0", "2");

        SystemCVEResult result = single();
        assertEquals(PatchStatus.AFFECTED_FULL_PATCH_APPLICABLE, result.getStatus());
        assertEquals(List.of(new SystemCVEResult.Errata(errata.getId(), errata.getAdvisory(), channel.getLabel(),
                true)), result.getErrata());
    }

    @Test
    public void testStatusMatchesCveAudit() throws Exception {
        String cve = randomCveName();
        String pkg = randomPackageName();
        installPackage(pkg, "1.0", "1");
        addOvalVulnerablePackage(cpe, cve, pkg, "1.0", "2");
        publishFix(channel, cve, pkg, "1.0", "2");

        CVEAuditServer audit = CVEAuditManagerOVAL.listSystemsByPatchStatus(user, cve, ALL).stream()
                .filter(s -> s.getId() == server.getId())
                .findFirst()
                .orElseThrow();

        assertEquals(audit.getPatchStatus(), single().getStatus());
    }

    @Test
    public void testFixOnlyInOriginalOfClonedChannel() throws Exception {
        String cve = randomCveName();
        String pkg = randomPackageName();
        Channel original = createTestChannel(user);
        Package installed = createTestPackage(user, original, "x86_64", pkg, "0", "1.0", "1");
        Channel clone = ChannelFactoryTest.createTestClonedChannel(original, user);
        ChannelTestUtility.testAddPackage(clone, installed);
        clone = TestUtils.saveAndFlush(clone);
        server = createTestServer(user, Set.of(clone));
        server.setCpe(cpe);
        server = TestUtils.saveAndFlush(server);
        createTestInstalledPackage(installed, server);
        addOvalVulnerablePackage(cpe, cve, pkg, "1.0", "2");
        Errata errata = publishFix(original, cve, pkg, "1.0", "2");

        SystemCVEResult result = single();
        assertEquals(PatchStatus.AFFECTED_PATCH_INAPPLICABLE, result.getStatus());
        assertEquals(List.of(new SystemCVEResult.Errata(errata.getId(), errata.getAdvisory(), original.getLabel(),
                false)), result.getErrata());
    }

    @Test
    public void testErrataWithoutInstalledPackageIsIgnored() throws Exception {
        String cve = randomCveName();
        String pkg = randomPackageName();
        installPackage(pkg, "1.0", "1");
        addOvalVulnerablePackage(cpe, cve, pkg, "1.0", "2");
        // The only errata for the CVE fixes a package which is not installed on the system
        publishFix(channel, cve, randomPackageName(), "1.0", "2");

        SystemCVEResult result = single();
        assertEquals(PatchStatus.AFFECTED_PATCH_UNAVAILABLE_IN_UYUNI, result.getStatus());
        assertTrue(result.getErrata().isEmpty());
    }

    @Test
    public void testNoLinkWhenPatchUnavailable() throws Exception {
        String cve = randomCveName();
        String pkg = randomPackageName();
        installPackage(pkg, "1.0", "1");
        addOvalVulnerablePackage(cpe, cve, pkg, null, null);
        publishFix(channel, cve, pkg, "1.0", "2");

        SystemCVEResult result = single();
        assertEquals(PatchStatus.AFFECTED_PATCH_UNAVAILABLE, result.getStatus());
        assertTrue(result.getErrata().isEmpty());
    }

    @Test
    public void testInstalledModulePlatformWinsOverPrefixMatch() {
        String product = "uyunitest" + TestUtils.randomString(6).toLowerCase();
        server.setCpe("cpe:/o:suse:" + product + ":15:sp7");
        InstalledProduct module = new InstalledProduct(product + "-module", "15.7",
                PackageFactory.lookupPackageArchByLabel("x86_64"), null, false);
        server.addInstalledProduct(module);
        server = TestUtils.saveAndFlush(server);

        String cve = randomCveName();
        String pkg = randomPackageName();
        installPackage(pkg, "1.0", "1");
        // GA platform: prefix of the server CPE, no fix released
        addOvalVulnerablePackage("cpe:/o:suse:" + product + ":15", cve, pkg, null, null);
        // Module platform of the installed product: fix released
        addOvalVulnerablePackage("cpe:/o:suse:" + product + "-module:15:sp7", cve, pkg, "1.0", "2");

        assertEquals(PatchStatus.AFFECTED_PATCH_UNAVAILABLE_IN_UYUNI, single().getStatus());
    }

    @Test
    public void testPrefixMatchWhenNoExactPlatform() {
        String product = "uyunitest" + TestUtils.randomString(6).toLowerCase();
        server.setCpe("cpe:/o:suse:" + product + ":15:sp7");
        server = TestUtils.saveAndFlush(server);

        String pkg = randomPackageName();
        installPackage(pkg, "1.0", "1");
        addOvalVulnerablePackage("cpe:/o:suse:" + product + ":15", randomCveName(), pkg, null, null);

        assertEquals(PatchStatus.AFFECTED_PATCH_UNAVAILABLE, single().getStatus());
    }

    @Test
    public void testStatusFilter() {
        String pkg = randomPackageName();
        installPackage(pkg, "1.0", "2");
        String cvePatched = randomCveName();
        addOvalVulnerablePackage(cpe, cvePatched, pkg, "1.0", "2");
        addOvalVulnerablePackage(cpe, randomCveName(), pkg, "1.0", "3");

        List<SystemCVEResult> patched = listAll(EnumSet.of(PatchStatus.PATCHED));
        assertEquals(1, patched.size());
        assertEquals(cvePatched, patched.get(0).getCveName());

        assertTrue(listAll(EnumSet.noneOf(PatchStatus.class)).isEmpty());
    }

    @Test
    public void testDefaultSortByCveNumberNewestFirst() {
        String pkg = randomPackageName();
        installPackage(pkg, "1.0", "1");
        addOvalVulnerablePackage(cpe, "CVE-2097-9999", pkg, null, null);
        addOvalVulnerablePackage(cpe, "CVE-2097-38822", pkg, null, null);
        addOvalVulnerablePackage(cpe, "CVE-2096-99999", pkg, null, null);

        List<String> names = listAll(ALL).stream().map(SystemCVEResult::getCveName).toList();

        assertEquals(List.of("CVE-2097-38822", "CVE-2097-9999", "CVE-2096-99999"), names);
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

        DataResult<SystemCVEResult> page = SystemCVEAuditManager.listCves(server, ALL, Set.of(), pc);

        assertEquals(2, page.size());
        assertEquals(3, page.getTotalSize());
        assertEquals(prefix + "0", page.get(0).getCveName());
        assertEquals(prefix + "1", page.get(1).getCveName());
    }

    @Test
    public void testSeverityFilterAndUnknown() {
        String pkg = randomPackageName();
        installPackage(pkg, "1.0", "1");
        String critical = randomCveName();
        String unknown = randomCveName();
        addOvalVulnerablePackage(cpe, critical, pkg, null, null, OvalSeverity.CRITICAL);
        addOvalVulnerablePackage(cpe, randomCveName(), pkg, null, null, OvalSeverity.LOW);
        addOvalVulnerablePackage(cpe, unknown, pkg, null, null);

        List<SystemCVEResult> criticalOnly =
                SystemCVEAuditManager.listCves(server, ALL, Set.of(OvalSeverity.CRITICAL), new PageControl(1, 0));
        assertEquals(List.of(critical), criticalOnly.stream().map(SystemCVEResult::getCveName).toList());
        assertEquals(OvalSeverity.CRITICAL, criticalOnly.get(0).getSeverity());

        List<SystemCVEResult> unknownOnly =
                SystemCVEAuditManager.listCves(server, ALL, Set.of(OvalSeverity.UNKNOWN), new PageControl(1, 0));
        assertEquals(List.of(unknown), unknownOnly.stream().map(SystemCVEResult::getCveName).toList());

        assertEquals(3, listAll(ALL).size());
    }

    @Test
    public void testHighestSeverityWinsAcrossPlatforms() {
        String product = "uyunitest" + TestUtils.randomString(6).toLowerCase();
        server.addInstalledProduct(new InstalledProduct(product, "15.7",
                PackageFactory.lookupPackageArchByLabel("x86_64"), null, false));
        server = TestUtils.saveAndFlush(server);

        String cve = randomCveName();
        String pkg = randomPackageName();
        installPackage(pkg, "1.0", "1");
        addOvalVulnerablePackage(cpe, cve, pkg, null, null, OvalSeverity.MODERATE);
        addOvalVulnerablePackage("cpe:/o:suse:" + product + ":15:sp7", cve, pkg, null, null,
                OvalSeverity.IMPORTANT);

        assertEquals(OvalSeverity.IMPORTANT, single().getSeverity());
    }

    @Test
    public void testSortBySeverity() {
        String pkg = randomPackageName();
        installPackage(pkg, "1.0", "1");
        String low = randomCveName();
        String critical = randomCveName();
        String unknown = randomCveName();
        addOvalVulnerablePackage(cpe, low, pkg, null, null, OvalSeverity.LOW);
        addOvalVulnerablePackage(cpe, critical, pkg, null, null, OvalSeverity.CRITICAL);
        addOvalVulnerablePackage(cpe, unknown, pkg, null, null);

        PageControl pc = new PageControl(1, 0);
        pc.setSortColumn("severity");
        pc.setSortDescending(true);
        List<String> names = SystemCVEAuditManager.listCves(server, ALL, Set.of(), pc).stream()
                .map(SystemCVEResult::getCveName)
                .toList();

        assertEquals(List.of(critical, low, unknown), names);
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
}
