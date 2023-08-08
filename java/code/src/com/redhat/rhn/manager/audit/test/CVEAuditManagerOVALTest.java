package com.redhat.rhn.manager.audit.test;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.errata.Cve;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.audit.CVEAuditManager;
import com.redhat.rhn.manager.audit.CVEAuditManagerOVAL;
import com.redhat.rhn.manager.audit.CVEAuditSystemBuilder;
import com.redhat.rhn.manager.audit.PatchStatus;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;
import com.suse.oval.OVALCachingFactory;
import com.suse.oval.OsFamily;
import com.suse.oval.OvalParser;
import com.suse.oval.ovaltypes.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.redhat.rhn.domain.rhnpackage.test.PackageNameTest.createTestPackageName;
import static com.redhat.rhn.testing.ErrataTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

// TODO: Test for AFFECTED_PATCH_INAPPLICABLE_SUCCESSOR_PRODUCT
public class CVEAuditManagerOVALTest extends RhnBaseTestCase {
    private static final Logger log = LogManager.getLogger(CVEAuditManagerOVALTest.class);

    OvalParser ovalParser = new OvalParser();

    @Test
    void testDoAuditSystemNotAffected() throws Exception {
        OvalRootType ovalRoot = ovalParser.parse(TestUtils
                .findTestData("/com/redhat/rhn/manager/audit/test/oval/oval-def-1.xml"));

        DefinitionType definitionType = ovalRoot.getDefinitions().get(0);
        setupDefinition(definitionType, OsFamily.openSUSE_LEAP, "15.4");

        Cve cve = createTestCve("CVE-2022-2991");
        saveAllOVALTests(ovalRoot);
        extractAndSaveVulnerablePackages(definitionType);

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
     * */
    @Test
    void testDoAuditSystemNotAffected_WhenOSIsAffected() throws Exception {
        OvalRootType ovalRoot = ovalParser.parse(TestUtils
                .findTestData("/com/redhat/rhn/manager/audit/test/oval/oval-def-1.xml"));


        DefinitionType definitionType = ovalRoot.getDefinitions().get(0);
        setupDefinition(definitionType, OsFamily.openSUSE_LEAP, "15.4");

        Cve cve = createTestCve("CVE-2022-2991");
        saveAllOVALTests(ovalRoot);

        Set<Cve> cves = Set.of(cve);
        User user = createTestUser();

        Errata errata = createTestErrata(user, cves);
        Channel channel = createTestChannel(user, errata);
        Set<Channel> channels = Set.of(channel);

        Server server = createTestServer(user, channels);
        server.setCpe("cpe:/o:opensuse:leap:15.4"); // openSUSE Leap 15.4, same as the affected OS in OVAL

        createTestInstalledPackage(createLeap15_4_Package(user, errata, channel), server);

        createOVALDefinition(definitionType, OsFamily.openSUSE_LEAP, "15.4");

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

        // TODO: Compute object hash to make sure we update tests whenever test OVAL files changes
        DefinitionType definitionType = ovalRoot.getDefinitions().get(0);
        setupDefinition(definitionType, OsFamily.openSUSE_LEAP, "15.4");

        Cve cve = createTestCve("CVE-2022-2991");
        saveAllOVALTests(ovalRoot);
        extractAndSaveVulnerablePackages(definitionType);

        Set<Cve> cves = Set.of(cve);
        User user = createTestUser();

        Errata errata = createTestErrata(user, cves);
        Channel channel = createTestChannel(user, errata);
        Set<Channel> channels = Set.of(channel);

        Server server = createTestServer(user, channels);
        server.setCpe("cpe:/o:opensuse:leap:15.4");

        Package unpatched = createTestPackage(user, channel, "noarch");
        unpatched.setPackageName(createTestPackageName("kernel-debug-base"));
        Package patched = createLaterTestPackage(user, errata, channel, unpatched,
                "0", "4.12.14", "150100.197.137.2");

        createTestInstalledPackage(createLeap15_4_Package(user, errata, channel), server);
        createTestInstalledPackage(patched, server);

        CVEAuditManager.populateCVEChannels();

        List<CVEAuditManager.CVEPatchStatus> results = CVEAuditManager.listSystemsByPatchStatus(user, cve.getName())
                .collect(Collectors.toList());

        CVEAuditSystemBuilder systemAuditResult = CVEAuditManagerOVAL.doAuditSystem(cve.getName(), results, server);

        assertEquals(PatchStatus.PATCHED, systemAuditResult.getPatchStatus());
    }

    private void setupDefinition(DefinitionType definitionType, OsFamily osFamily, String osVersion) {
        OVALCachingFactory.cleanupDefinition(definitionType, osFamily, osVersion);
    }

    @Test
    void testDoAuditSystemAffectedFullPatchAvailable() throws Exception {
        OvalRootType ovalRoot = ovalParser.parse(TestUtils
                .findTestData("/com/redhat/rhn/manager/audit/test/oval/oval-def-1.xml"));

        // TODO: Compute object hash to make sure we update tests whenever test OVAL files changes
        DefinitionType definitionType = ovalRoot.getDefinitions().get(0);
        setupDefinition(definitionType, OsFamily.openSUSE_LEAP, "15.4");

        Cve cve = createTestCve("CVE-2022-2991");
        saveAllOVALTests(ovalRoot);
        extractAndSaveVulnerablePackages(definitionType);

        Set<Cve> cves = Set.of(cve);
        User user = createTestUser();

        Errata errata = createTestErrata(user, cves);
        Channel channel = createTestChannel(user, errata);
        Set<Channel> channels = Set.of(channel);

        Server server = createTestServer(user, channels);
        server.setCpe("cpe:/o:opensuse:leap:15.4");

        Package unpatched = createTestPackage(user, channel, "noarch",
                "kernel-debug-base", "0", "4.12.13", "150100.197.137.2");

        Package patched = createTestPackage(user, errata, channel, "noarch",
                "kernel-debug-base", "0", "4.12.14", "150100.197.137.2");

        log.error(unpatched.getPackageEvr().toUniversalEvrString());

        createTestInstalledPackage(createLeap15_4_Package(user, errata, channel), server);
        createTestInstalledPackage(unpatched, server);

        server.getPackages().forEach(p -> log.error(p.getName().getName() + "--" + p.getEvr().toUniversalEvrString()));

        CVEAuditManager.populateCVEChannels();

        server.getPackages().forEach(p -> log.error(p.getName().getName() + "--" + p.getEvr().toUniversalEvrString()));

        List<CVEAuditManager.CVEPatchStatus> results = CVEAuditManager.listSystemsByPatchStatus(user, cve.getName())
                .collect(Collectors.toList());

        log.error(server.getName());
        results.forEach(r -> log.error(r.getPackageName() + ":" + r.getPackageEvr() + ":" + r.isPackageInstalled() + ":" + r.getSystemName()));

        CVEAuditSystemBuilder systemAuditResult = CVEAuditManagerOVAL.doAuditSystem(cve.getName(), results, server);

        assertEquals(PatchStatus.AFFECTED_FULL_PATCH_APPLICABLE, systemAuditResult.getPatchStatus());
    }


    @Test
    void testDoAuditSystemAffectedPatchUnavailable() throws Exception {
        OvalRootType ovalRoot = ovalParser.parse(TestUtils
                .findTestData("/com/redhat/rhn/manager/audit/test/oval/oval-def-2.xml"));

        DefinitionType definitionType = ovalRoot.getDefinitions().get(0);
        setupDefinition(definitionType, OsFamily.openSUSE_LEAP, "15.4");
        Cve cve = createTestCve("CVE-2008-2934");
        saveAllOVALTests(ovalRoot);
        extractAndSaveVulnerablePackages(definitionType);

        Set<Cve> cves = Set.of(cve);
        User user = createTestUser();

        Errata errata = createTestErrata(user, cves);
        Channel channel = createTestChannel(user, errata);
        Set<Channel> channels = Set.of(channel);

        Server server = createTestServer(user, channels);
        server.setCpe("cpe:/o:opensuse:leap:15.4");

        Package affected =  createTestPackage(user, channel, "noarch", "MozillaFirefox");
        createTestPackage(user, channel, "noarch", "MozillaFirefox-devel");

        createTestInstalledPackage(createLeap15_4_Package(user, errata, channel), server);
        createTestInstalledPackage(affected, server);

        CVEAuditManager.populateCVEChannels();

        List<CVEAuditManager.CVEPatchStatus> results = CVEAuditManager.listSystemsByPatchStatus(user, cve.getName())
                .collect(Collectors.toList());

        results.forEach(r -> log.error(r.getPackageName() + ":" + r.getPackageEvr() + ":" + r.isPackageInstalled() + ":" + r.getSystemName()));

        CVEAuditSystemBuilder systemAuditResult = CVEAuditManagerOVAL.doAuditSystem(cve.getName(), results, server);

        assertEquals(PatchStatus.AFFECTED_PATCH_UNAVAILABLE, systemAuditResult.getPatchStatus());
    }

    @Test
    void testDoAuditSystemAffectedPartialPatchAvailable() throws Exception {
        OvalRootType ovalRoot = ovalParser.parse(TestUtils
                .findTestData("/com/redhat/rhn/manager/audit/test/oval/oval-def-3.xml"));
        log.warn("Partial Aavailable patch");

        DefinitionType definitionType = ovalRoot.getDefinitions().get(0);
        setupDefinition(definitionType, OsFamily.openSUSE_LEAP, "15.4");
        Cve cve = createTestCve("CVE-2008-2934");
        saveAllOVALTests(ovalRoot);
        extractAndSaveVulnerablePackages(definitionType);

        Set<Cve> cves = Set.of(cve);
        User user = createTestUser();

        Errata errata = createTestErrata(user, cves);
        Channel channel = createTestChannel(user, errata);
        Set<Channel> channels = Set.of(channel);

        Server server = createTestServer(user, channels);
        server.setCpe("cpe:/o:opensuse:leap:15.4");

        // Only package 'MozillaFirefox' has a patch in the assigned channels
        createTestPackage(user, errata, channel, "noarch", "MozillaFirefox", "0", "2.4.0", "150400.1.12");

        Package unpatched1 =  createTestPackage(user, channel, "noarch", "MozillaFirefox", "0", "2.3.0", "150400.1.12");
        Package unpatched2 =  createTestPackage(user, channel, "noarch", "MozillaFirefox-devel", "0", "2.3.0", "150400.1.12");

        createTestInstalledPackage(createLeap15_4_Package(user, errata, channel), server);
        createTestInstalledPackage(unpatched1, server);
        createTestInstalledPackage(unpatched2, server);

        CVEAuditManager.populateCVEChannels();

        List<CVEAuditManager.CVEPatchStatus> results = CVEAuditManager.listSystemsByPatchStatus(user, cve.getName())
                .collect(Collectors.toList());

        results.forEach(r -> log.error(r.getPackageName() + ":" + r.getPackageEvr() + ":" + r.isPackageInstalled() + ":" + r.getSystemName()));

        CVEAuditSystemBuilder systemAuditResult = CVEAuditManagerOVAL.doAuditSystem(cve.getName(), results, server);

        assertEquals(PatchStatus.AFFECTED_PARTIAL_PATCH_APPLICABLE, systemAuditResult.getPatchStatus());
    }

    @Test
    void testDoAuditSystemAffectedPartialPatchAvailable_FalsePositive() throws Exception {
        OvalRootType ovalRoot = ovalParser.parse(TestUtils
                .findTestData("/com/redhat/rhn/manager/audit/test/oval/oval-def-3.xml"));

        DefinitionType definitionType = ovalRoot.getDefinitions().get(0);
        setupDefinition(definitionType, OsFamily.openSUSE_LEAP, "15.4");
        Cve cve = createTestCve("CVE-2008-2934");
        saveAllOVALTests(ovalRoot);
        extractAndSaveVulnerablePackages(definitionType);

        Set<Cve> cves = Set.of(cve);
        User user = createTestUser();

        Errata errata = createTestErrata(user, cves);
        Channel channel = createTestChannel(user, errata);
        Set<Channel> channels = Set.of(channel);

        Server server = createTestServer(user, channels);
        server.setCpe("cpe:/o:opensuse:leap:15.4");

        createTestPackage(user, errata, channel, "noarch", "MozillaFirefox", "0", "2.4.0", "150400.1.12");
        Package unpatched =  createTestPackage(user, channel, "noarch", "MozillaFirefox", "0", "2.3.0", "150400.1.12");

        // The 'MozillaFirefox-devel' package is identified as vulnerable based on the OVAL data,
        // but it is not currently installed on the system.
        // Although 'MozillaFirefox-devel' does not have a patch available in the assigned channels,
        // the algorithm should return AFFECTED_FULL_PATCH_APPLICABLE instead of AFFECTED_PARTIAL_PATCH_APPLICABLE.
        // This decision is made because the 'MozillaFirefox' package,
        // which is installed, has a patch that can be applied.
        createTestPackage(user, channel, "noarch", "MozillaFirefox-devel");

        createTestInstalledPackage(createLeap15_4_Package(user, errata, channel), server);
        createTestInstalledPackage(unpatched, server);

        CVEAuditManager.populateCVEChannels();

        List<CVEAuditManager.CVEPatchStatus> results = CVEAuditManager.listSystemsByPatchStatus(user, cve.getName())
                .collect(Collectors.toList());

        results.forEach(r -> log.error(r.getPackageName() + ":" + r.getPackageEvr() + ":" + r.isPackageInstalled() + ":" + r.getSystemName()));

        CVEAuditSystemBuilder systemAuditResult = CVEAuditManagerOVAL.doAuditSystem(cve.getName(), results, server);

        assertEquals(PatchStatus.AFFECTED_FULL_PATCH_APPLICABLE, systemAuditResult.getPatchStatus());
    }

    @Test
    void testDoAuditSystemAffectedPatchInapplicable() throws Exception {
        log.warn("testDoAuditSystemAffectedPatchInapplicable");
        OvalRootType ovalRoot = ovalParser.parse(TestUtils
                .findTestData("/com/redhat/rhn/manager/audit/test/oval/oval-def-1.xml"));

        // TODO: Compute object hash to make sure we update tests whenever test OVAL files changes
        DefinitionType definitionType = ovalRoot.getDefinitions().get(0);
        setupDefinition(definitionType, OsFamily.openSUSE_LEAP, "15.4");

        Cve cve = createTestCve("CVE-2022-2991");
        saveAllOVALTests(ovalRoot);
        extractAndSaveVulnerablePackages(definitionType);

        Set<Cve> cves = Set.of(cve);
        User user = createTestUser();

        Errata errata = createTestErrata(user, cves);
        Channel channel = createTestChannel(user);

        // This channel is not assigned to server
        Channel otherChannel = createTestChannel(user, errata);
        Set<Channel> assignedChannels = Set.of(channel);
        Server server = createTestServer(user, assignedChannels);
        server.setCpe("cpe:/o:opensuse:leap:15.4");

        Package unpatched = createTestPackage(user, channel, "noarch",
                "kernel-debug-base", "0", "4.12.13", "150100.197.137.2");

        // Patch exists in an unassigned channel
        createTestPackage(user, errata, otherChannel, "noarch",
                "kernel-debug-base", "0", "4.12.14", "150100.197.137.2");

        log.error(unpatched.getPackageEvr().toUniversalEvrString());

        createTestInstalledPackage(createLeap15_4_Package(user, errata, channel), server);
        createTestInstalledPackage(unpatched, server);

        server.getPackages().forEach(p -> log.error(p.getName().getName() + "--" + p.getEvr().toUniversalEvrString()));

        CVEAuditManager.populateCVEChannels();

        server.getPackages().forEach(p -> log.error(p.getName().getName() + "--" + p.getEvr().toUniversalEvrString()));

        List<CVEAuditManager.CVEPatchStatus> results = CVEAuditManager.listSystemsByPatchStatus(user, cve.getName())
                .collect(Collectors.toList());

        log.error(server.getName());
        results.forEach(r -> log.error(r.getPackageName() + ":" + r.getPackageEvr() + ":" + r.isPackageInstalled() + ":" + r.getSystemName()));

        CVEAuditSystemBuilder systemAuditResult = CVEAuditManagerOVAL.doAuditSystem(cve.getName(), results, server);

        assertEquals(PatchStatus.AFFECTED_PATCH_INAPPLICABLE, systemAuditResult.getPatchStatus());
    }

    /**
     * This package is used to distinguish openSUSE Leap 15.4 distributions. We use very often in tests, so
     * it's abstracted here
     * */
    private static Package createLeap15_4_Package(User user, Errata errata, Channel channel) throws Exception {
        return createTestPackage(user, channel, "noarch", "openSUSE-release",
                "0", "15.4", "0");
    }

    /**
     * Using this method requires that each triple of OVAL (test, object, state) be located at the same position in
     * the OVAL file under their own category.
     * */
    private static void saveAllOVALTests(OvalRootType ovalRoot) {
        for (int i = 0; i < ovalRoot.getTests().getTests().size(); i++) {
            TestType testType = ovalRoot.getTests().getTests().get(i);
            ObjectType objectType = ovalRoot.getObjects().getObjects().get(i);
            StateType stateType = ovalRoot.getStates().getStates().get(i);

            createOVALTest(testType, objectType, stateType);
        }
    }
}
