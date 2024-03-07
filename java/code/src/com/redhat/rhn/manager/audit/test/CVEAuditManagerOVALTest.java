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

package com.redhat.rhn.manager.audit.test;

import static com.redhat.rhn.domain.rhnpackage.test.PackageNameTest.createTestPackageName;
import static com.redhat.rhn.testing.ErrataTestUtils.createLaterTestPackage;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestChannel;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestCve;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestErrata;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestInstalledPackage;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestPackage;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestServer;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestUser;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.errata.Cve;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.audit.CVEAuditManager;
import com.redhat.rhn.manager.audit.CVEAuditManagerOVAL;
import com.redhat.rhn.manager.audit.CVEAuditServer;
import com.redhat.rhn.manager.audit.CVEAuditSystemBuilder;
import com.redhat.rhn.manager.audit.PatchStatus;
import com.redhat.rhn.manager.audit.RankedChannel;
import com.redhat.rhn.manager.audit.UnknownCVEIdentifierException;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;

import com.suse.oval.OVALCachingFactory;
import com.suse.oval.OVALCleaner;
import com.suse.oval.OsFamily;
import com.suse.oval.OvalParser;
import com.suse.oval.ovaltypes.OvalRootType;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class CVEAuditManagerOVALTest extends RhnBaseTestCase {
    public static final String CPE_OPENSUSE_LEAP_15_4 = "cpe:/o:opensuse:leap:15.4";
    private OvalParser ovalParser = new OvalParser();

    @Test
    void testDoAuditSystemNotAffected() throws Exception {
        OvalRootType ovalRoot = ovalParser.parse(TestUtils
                .findTestData("/com/redhat/rhn/manager/audit/test/oval/oval-def-1.xml"));

        Cve cve = createTestCve("CVE-2022-2991");

        extractAndSaveVulnerablePackages(ovalRoot);

        Set<Cve> cves = Set.of(cve);
        User user = createTestUser();

        Errata errata = createTestErrata(user, cves);
        Channel channel = createTestChannel(user, errata);
        Set<Channel> channels = Set.of(channel);

        Server server = createTestServer(user, channels);
        server.setCpe("cpe:/o:opensuse:leap:15.5"); // Not Leap 15.4

        CVEAuditManager.populateCVEChannels();

        List<CVEAuditManager.CVEPatchStatus> results = CVEAuditManager.listSystemsByPatchStatus(user, cve.getName())
                .collect(Collectors.toList());

        CVEAuditSystemBuilder systemAuditResult = CVEAuditManagerOVAL.doAuditSystem(cve.getName(), results, server);

        assertEquals(PatchStatus.NOT_AFFECTED, systemAuditResult.getPatchStatus());
    }

    /**
     * If the system's OS is flagged as vulnerable based on OVAL, it doesn't necessarily mean that the system
     * is vulnerable. For example, if none of the vulnerable packages are installed on the system,
     * it means that the system is by definition NOT_AFFECTED.
     * This test verifies this scenario.
     */
    @Test
    void testDoAuditSystemNotAffectedWhenOSIsAffected() throws Exception {
        OvalRootType ovalRoot = ovalParser.parse(TestUtils
                .findTestData("/com/redhat/rhn/manager/audit/test/oval/oval-def-1.xml"));

        Cve cve = createTestCve("CVE-2022-2991");

        extractAndSaveVulnerablePackages(ovalRoot);

        Set<Cve> cves = Set.of(cve);
        User user = createTestUser();

        Errata errata = createTestErrata(user, cves);
        Channel channel = createTestChannel(user, errata);
        Set<Channel> channels = Set.of(channel);

        Server server = createTestServer(user, channels);
        server.setCpe(CPE_OPENSUSE_LEAP_15_4); // openSUSE Leap 15.4, same as the affected OS in OVAL

        CVEAuditManager.populateCVEChannels();

        List<CVEAuditManager.CVEPatchStatus> results = CVEAuditManager.listSystemsByPatchStatus(user, cve.getName())
                .collect(Collectors.toList());

        CVEAuditSystemBuilder systemAuditResult = CVEAuditManagerOVAL.doAuditSystem(cve.getName(), results, server);

        assertEquals(PatchStatus.NOT_AFFECTED, systemAuditResult.getPatchStatus());
    }

    @Test
    void testDoAuditSystemPatched() throws Exception {
        OvalRootType ovalRoot = ovalParser.parse(TestUtils
                .findTestData("/com/redhat/rhn/manager/audit/test/oval/oval-def-1.xml"));

        Cve cve = createTestCve("CVE-2022-2991");

        extractAndSaveVulnerablePackages(ovalRoot);

        User user = createTestUser();

        Channel channel = createTestChannel(user);
        Set<Channel> channels = Set.of(channel);

        Server server = createTestServer(user, channels);
        server.setCpe(CPE_OPENSUSE_LEAP_15_4);

        Package unpatched = createTestPackage(user, channel, "noarch");
        unpatched.setPackageName(createTestPackageName("kernel-debug-base"));
        Package patched = createLaterTestPackage(user, null, channel, unpatched,
                "0", "4.12.14", "150100.197.137.2");

        createTestInstalledPackage(patched, server);

        CVEAuditManager.populateCVEChannels();

        List<CVEAuditManager.CVEPatchStatus> results = CVEAuditManager.listSystemsByPatchStatus(user, cve.getName())
                .collect(Collectors.toList());

        CVEAuditSystemBuilder systemAuditResult = CVEAuditManagerOVAL.doAuditSystem(cve.getName(), results, server);

        assertEquals(PatchStatus.PATCHED, systemAuditResult.getPatchStatus());
    }

    @Test
    void testDoAuditSystemAffectedFullPatchAvailable() throws Exception {
        OvalRootType ovalRoot = ovalParser.parse(TestUtils
                .findTestData("/com/redhat/rhn/manager/audit/test/oval/oval-def-1.xml"));

        Cve cve = createTestCve("CVE-2022-2991");

        extractAndSaveVulnerablePackages(ovalRoot);

        Set<Cve> cves = Set.of(cve);
        User user = createTestUser();

        Errata errata = createTestErrata(user, cves);
        Channel channel = createTestChannel(user, errata);
        Set<Channel> channels = Set.of(channel);

        Server server = createTestServer(user, channels);
        server.setCpe(CPE_OPENSUSE_LEAP_15_4);

        Package unpatched = createTestPackage(user, channel, "noarch",
                "kernel-debug-base", "0", "4.12.13", "150100.197.137.2");

        // Add patched package to assigned channel
        createTestPackage(user, errata, channel, "noarch",
                "kernel-debug-base", "0", "4.12.14", "150100.197.137.2");

        createTestInstalledPackage(unpatched, server);

        CVEAuditManager.populateCVEChannels();

        List<CVEAuditManager.CVEPatchStatus> results = CVEAuditManager.listSystemsByPatchStatus(user, cve.getName())
                .collect(Collectors.toList());

        CVEAuditSystemBuilder systemAuditResult = CVEAuditManagerOVAL.doAuditSystem(cve.getName(), results, server);

        assertEquals(PatchStatus.AFFECTED_FULL_PATCH_APPLICABLE, systemAuditResult.getPatchStatus());
    }


    @Test
    void testDoAuditSystemAffectedPatchUnavailable() throws Exception {
        OvalRootType ovalRoot = ovalParser.parse(TestUtils
                .findTestData("/com/redhat/rhn/manager/audit/test/oval/oval-def-2.xml"));

        Cve cve = createTestCve("CVE-2008-2934");

        extractAndSaveVulnerablePackages(ovalRoot);

        Set<Cve> cves = Set.of(cve);
        User user = createTestUser();

        Errata errata = createTestErrata(user, cves);
        Channel channel = createTestChannel(user, errata);
        Set<Channel> channels = Set.of(channel);

        Server server = createTestServer(user, channels);
        server.setCpe(CPE_OPENSUSE_LEAP_15_4);

        Package affected = createTestPackage(user, channel, "noarch", "MozillaFirefox");
        createTestPackage(user, channel, "noarch", "MozillaFirefox-devel");

        createTestInstalledPackage(affected, server);

        CVEAuditManager.populateCVEChannels();

        List<CVEAuditManager.CVEPatchStatus> results = CVEAuditManager.listSystemsByPatchStatus(user, cve.getName())
                .collect(Collectors.toList());

        CVEAuditSystemBuilder systemAuditResult = CVEAuditManagerOVAL.doAuditSystem(cve.getName(), results, server);

        assertEquals(PatchStatus.AFFECTED_PATCH_UNAVAILABLE, systemAuditResult.getPatchStatus());
    }

    @Test
    void testDoAuditSystemAffectedPartialPatchAvailable() throws Exception {
        OvalRootType ovalRoot = ovalParser.parse(TestUtils
                .findTestData("/com/redhat/rhn/manager/audit/test/oval/oval-def-3.xml"));

        Cve cve = createTestCve("CVE-2008-2934");

        extractAndSaveVulnerablePackages(ovalRoot);

        Set<Cve> cves = Set.of(cve);
        User user = createTestUser();

        Errata errata = createTestErrata(user, cves);
        Channel channel = createTestChannel(user, errata);
        Set<Channel> channels = Set.of(channel);

        Server server = createTestServer(user, channels);
        server.setCpe(CPE_OPENSUSE_LEAP_15_4);

        // Only package 'MozillaFirefox' has a patch in the assigned channels
        createTestPackage(user, errata, channel, "noarch", "MozillaFirefox", "0", "2.4.0",
                "150400.1.12");

        Package unpatched1 =
                createTestPackage(user, channel, "noarch", "MozillaFirefox", "0", "2.3.0",
                        "150400.1.12");
        Package unpatched2 =
                createTestPackage(user, channel, "noarch", "MozillaFirefox-devel", "0",
                        "2.3.0", "150400.1.12");

        createTestInstalledPackage(unpatched1, server);
        createTestInstalledPackage(unpatched2, server);

        CVEAuditManager.populateCVEChannels();

        List<CVEAuditManager.CVEPatchStatus> results = CVEAuditManager.listSystemsByPatchStatus(user, cve.getName())
                .collect(Collectors.toList());

        CVEAuditSystemBuilder systemAuditResult = CVEAuditManagerOVAL.doAuditSystem(cve.getName(), results, server);

        assertEquals(PatchStatus.AFFECTED_PARTIAL_PATCH_APPLICABLE, systemAuditResult.getPatchStatus());
    }

    @Test
    void testDoAuditSystemAffectedPartialPatchAvailableFalsePositive() throws Exception {
        OvalRootType ovalRoot = ovalParser.parse(TestUtils
                .findTestData("/com/redhat/rhn/manager/audit/test/oval/oval-def-3.xml"));

        Cve cve = createTestCve("CVE-2008-2934");

        extractAndSaveVulnerablePackages(ovalRoot);

        Set<Cve> cves = Set.of(cve);
        User user = createTestUser();

        Errata errata = createTestErrata(user, cves);
        Channel channel = createTestChannel(user, errata);
        Set<Channel> channels = Set.of(channel);

        Server server = createTestServer(user, channels);
        server.setCpe(CPE_OPENSUSE_LEAP_15_4);

        createTestPackage(user, errata, channel, "noarch", "MozillaFirefox", "0", "2.4.0", "150400.1.12");
        Package unpatched =  createTestPackage(user, channel, "noarch", "MozillaFirefox", "0", "2.3.0", "150400.1.12");

        // The 'MozillaFirefox-devel' package is identified as vulnerable based on the OVAL data,
        // but it is not currently installed on the system.
        // Although 'MozillaFirefox-devel' does not have a patch available in the assigned channels,
        // the algorithm should return AFFECTED_FULL_PATCH_APPLICABLE instead of AFFECTED_PARTIAL_PATCH_APPLICABLE.
        // This decision is made because the 'MozillaFirefox' package,
        // which is installed, has a patch that can be applied.
        createTestPackage(user, channel, "noarch", "MozillaFirefox-devel");

        createTestInstalledPackage(unpatched, server);

        CVEAuditManager.populateCVEChannels();

        List<CVEAuditManager.CVEPatchStatus> results = CVEAuditManager.listSystemsByPatchStatus(user, cve.getName())
                .collect(Collectors.toList());

        CVEAuditSystemBuilder systemAuditResult = CVEAuditManagerOVAL.doAuditSystem(cve.getName(), results, server);

        assertEquals(PatchStatus.AFFECTED_FULL_PATCH_APPLICABLE, systemAuditResult.getPatchStatus());
    }

    @Test
    void testDoAuditSystemAffectedPatchInapplicable() throws Exception {
        OvalRootType ovalRoot = ovalParser.parse(TestUtils
                .findTestData("/com/redhat/rhn/manager/audit/test/oval/oval-def-1.xml"));

        Cve cve = createTestCve("CVE-2022-2991");

        extractAndSaveVulnerablePackages(ovalRoot);

        Set<Cve> cves = Set.of(cve);
        User user = createTestUser();

        Errata errata = createTestErrata(user, cves);
        Channel channel = createTestChannel(user);

        // This channel is not assigned to server
        Channel otherChannel = createTestChannel(user, errata);
        Set<Channel> assignedChannels = Set.of(channel);
        Server server = createTestServer(user, assignedChannels);
        server.setCpe(CPE_OPENSUSE_LEAP_15_4);

        Package unpatched = createTestPackage(user, channel, "noarch",
                "kernel-debug-base", "0", "4.12.13", "150100.197.137.2");

        // Patch exists in an unassigned channel
        createTestPackage(user, errata, otherChannel, "noarch",
                "kernel-debug-base", "0", "4.12.14", "150100.197.137.2");

        createTestInstalledPackage(unpatched, server);

        CVEAuditManager.populateCVEChannels();
        // We don't have to care about the internals of populateCVEChannels and weather it will assign otherChannel as
        // a relevant channel to server; we add it ourselves.
        RankedChannel otherChannelRanked = new RankedChannel(otherChannel.getId(), 0);
        Map<Server, List<RankedChannel>> relevantChannels = new HashMap<>();
        relevantChannels.put(server, List.of(otherChannelRanked));
        CVEAuditManager.insertRelevantServerChannels(relevantChannels);
        HibernateFactory.getSession().flush();

        List<CVEAuditManager.CVEPatchStatus> results = CVEAuditManager.listSystemsByPatchStatus(user, cve.getName())
                .collect(Collectors.toList());

        CVEAuditSystemBuilder systemAuditResult = CVEAuditManagerOVAL.doAuditSystem(cve.getName(), results, server);

        assertEquals(PatchStatus.AFFECTED_PATCH_INAPPLICABLE, systemAuditResult.getPatchStatus());
    }

    @Test
    void testDoAuditSystemAffectedPatchInapplicableSuccessorProduct() throws Exception {
        OvalRootType ovalRoot = ovalParser.parse(TestUtils
                .findTestData("/com/redhat/rhn/manager/audit/test/oval/oval-def-1.xml"));

        Cve cve = createTestCve("CVE-2022-2991");

        extractAndSaveVulnerablePackages(ovalRoot);

        Set<Cve> cves = Set.of(cve);
        User user = createTestUser();

        Errata errata = createTestErrata(user, cves);
        Channel channel = createTestChannel(user);

        // This channel is not assigned to server
        Channel otherChannel = createTestChannel(user, errata);
        Set<Channel> assignedChannels = Set.of(channel);
        Server server = createTestServer(user, assignedChannels);
        server.setCpe(CPE_OPENSUSE_LEAP_15_4);

        Package unpatched = createTestPackage(user, channel, "noarch",
                "kernel-debug-base", "0", "4.12.13", "150100.197.137.2");

        // Patch exists in an unassigned channel
        createTestPackage(user, errata, otherChannel, "noarch",
                "kernel-debug-base", "0", "4.12.14", "150100.197.137.2");

        createTestInstalledPackage(unpatched, server);

        CVEAuditManager.populateCVEChannels();
        // We set the rank to SUCCESSOR_PRODUCT_RANK_BOUNDARY in order to imply that it's a successor product
        // migration channel
        RankedChannel otherChannelRanked = new RankedChannel(otherChannel.getId(),
                CVEAuditManager.SUCCESSOR_PRODUCT_RANK_BOUNDARY);
        // We don't have to care about the internals of populateCVEChannels and weather it will assign otherChannel as
        // a relevant channel to server; we add it ourselves.
        Map<Server, List<RankedChannel>> relevantChannels = new HashMap<>();
        relevantChannels.put(server, List.of(otherChannelRanked));
        CVEAuditManager.insertRelevantServerChannels(relevantChannels);
        HibernateFactory.getSession().flush();

        List<CVEAuditManager.CVEPatchStatus> results = CVEAuditManager.listSystemsByPatchStatus(user, cve.getName())
                .collect(Collectors.toList());

        CVEAuditSystemBuilder systemAuditResult = CVEAuditManagerOVAL.doAuditSystem(cve.getName(), results, server);

        assertEquals(PatchStatus.AFFECTED_PATCH_INAPPLICABLE_SUCCESSOR_PRODUCT, systemAuditResult.getPatchStatus());
    }

    /**
     * Verify that bnc#833783 is fixed:
     * Test that irrelevant packages do not alter a system's PATCHED status
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testDoAuditSystemPatchedWithIrrelevantErrata() throws Exception {
        OvalRootType ovalRoot = ovalParser.parse(TestUtils
                .findTestData("/com/redhat/rhn/manager/audit/test/oval/oval-def-1.xml"));

        Cve cve = createTestCve("CVE-2022-2991");

        extractAndSaveVulnerablePackages(ovalRoot);

        Set<Cve> cves = Set.of(cve);
        User user = createTestUser();

        Errata errata = createTestErrata(user, cves);
        Channel channel = createTestChannel(user, errata);
        Set<Channel> channels = Set.of(channel);

        Server server = createTestServer(user, channels);
        server.setCpe(CPE_OPENSUSE_LEAP_15_4);

        Package unpatched = createTestPackage(user, channel, "noarch");
        unpatched.setPackageName(createTestPackageName("kernel-debug-base"));
        Package patched = createLaterTestPackage(user, errata, channel, unpatched,
                "0", "4.12.14", "150100.197.137.2");

        createTestInstalledPackage(patched, server);

        CVEAuditManager.populateCVEChannels();

        List<CVEAuditManager.CVEPatchStatus> results = CVEAuditManager.listSystemsByPatchStatus(user, cve.getName())
                .collect(Collectors.toList());

        CVEAuditSystemBuilder systemAuditResult = CVEAuditManagerOVAL.doAuditSystem(cve.getName(), results, server);

        assertEquals(PatchStatus.PATCHED, systemAuditResult.getPatchStatus());
    }

    @Test
    void testDoAuditSystemAffectedPatchUnavailableInUyuni() throws Exception {
        OvalRootType ovalRoot = ovalParser.parse(TestUtils
                .findTestData("/com/redhat/rhn/manager/audit/test/oval/oval-def-1.xml"));

        Cve cve = createTestCve("CVE-2022-2991");

        extractAndSaveVulnerablePackages(ovalRoot);

        User user = createTestUser();

        Channel channel = createTestChannel(user);

        Set<Channel> assignedChannels = Set.of(channel);
        Server server = createTestServer(user, assignedChannels);
        server.setCpe(CPE_OPENSUSE_LEAP_15_4);

        // assigned channel contains an unpatched package
        Package unpatched = createTestPackage(user, channel, "noarch",
                "kernel-debug-base", "0", "4.12.13", "150100.197.137.2");

        createTestInstalledPackage(unpatched, server);

        CVEAuditManager.populateCVEChannels();

        List<CVEAuditManager.CVEPatchStatus> results = CVEAuditManager.listSystemsByPatchStatus(user, cve.getName())
                .collect(Collectors.toList());

        CVEAuditSystemBuilder systemAuditResult = CVEAuditManagerOVAL.doAuditSystem(cve.getName(), results, server);

        assertEquals(PatchStatus.AFFECTED_PATCH_UNAVAILABLE_IN_UYUNI, systemAuditResult.getPatchStatus());
    }

    @Test
    public void testListSystemsByPatchStatusUnknownCVE() {
        String unknownCVE = TestUtils.randomString().substring(0, 13);
        // Although we add the CVE to rhnCve, the exception should be still thrown because the CVE is not linked to any
        // OVAL vulnerability in the database .i.e. not present in suseOVALPlatformVulnerablePackage
        createTestCve(unknownCVE);

        User user = createTestUser();

        assertThrows(UnknownCVEIdentifierException.class,
                () -> CVEAuditManagerOVAL.listSystemsByPatchStatus(user, unknownCVE,
                        EnumSet.allOf(PatchStatus.class)));
    }

    @Test
    public void testListSystemsByPatchStatusKnownCVE() throws IOException, ClassNotFoundException {
        User user = createTestUser();

        OvalRootType ovalRoot = ovalParser.parse(TestUtils
                .findTestData("/com/redhat/rhn/manager/audit/test/oval/oval-def-1.xml"));

        Cve knownCve = createTestCve("CVE-2022-2991");

        extractAndSaveVulnerablePackages(ovalRoot);

        assertDoesNotThrow(() -> CVEAuditManagerOVAL.listSystemsByPatchStatus(user, knownCve.getName(),
                EnumSet.allOf(PatchStatus.class)));
    }

    @Test
    public void testListSystemsByPatchStatusAffectedPatchInapplicable() throws Exception {
        OvalRootType ovalRoot = ovalParser.parse(TestUtils
                .findTestData("/com/redhat/rhn/manager/audit/test/oval/oval-def-1.xml"));

        Cve cve = createTestCve("CVE-2022-2991");

        extractAndSaveVulnerablePackages(ovalRoot);

        Set<Cve> cves = Set.of(cve);
        User user = createTestUser();

        Errata errata = createTestErrata(user, cves);
        Channel channel = createTestChannel(user);

        // This channel is not assigned to server
        Channel otherChannel = createTestChannel(user, errata);
        Set<Channel> assignedChannels = Set.of(channel);
        Server server = createTestServer(user, assignedChannels);
        server.setCpe(CPE_OPENSUSE_LEAP_15_4);

        Package unpatched = createTestPackage(user, channel, "noarch",
                "kernel-debug-base", "0", "4.12.13", "150100.197.137.2");

        // Patch exists in an unassigned channel
        createTestPackage(user, errata, otherChannel, "noarch",
                "kernel-debug-base", "0", "4.12.14", "150100.197.137.2");

        createTestInstalledPackage(unpatched, server);

        CVEAuditManager.populateCVEChannels();
        // We don't have to care about the internals of populateCVEChannels and weather it will assign otherChannel as
        // a relevant channel to server; we add it ourselves.
        RankedChannel otherChannelRanked = new RankedChannel(otherChannel.getId(), 0);
        Map<Server, List<RankedChannel>> relevantChannels = new HashMap<>();
        relevantChannels.put(server, List.of(otherChannelRanked));
        CVEAuditManager.insertRelevantServerChannels(relevantChannels);
        HibernateFactory.getSession().flush();

        List<CVEAuditServer> auditServers = CVEAuditManagerOVAL.listSystemsByPatchStatus(user, cve.getName(),
                EnumSet.allOf(PatchStatus.class));
        CVEAuditServer auditServer = auditServers.stream().findAny().get();

        assertNotEmpty(auditServer.getChannels());
        assertNotEmpty(auditServer.getErratas());
        assertEquals(PatchStatus.AFFECTED_PATCH_INAPPLICABLE, auditServer.getPatchStatus());
    }

    private static void extractAndSaveVulnerablePackages(OvalRootType rootType) {
        OVALCleaner.cleanup(rootType, OsFamily.LEAP, "15.4");
        OVALCachingFactory.savePlatformsVulnerablePackages(rootType);

        HibernateFactory.getSession().flush();
    }
}
