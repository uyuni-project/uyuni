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

public class CVEAuditManagerOVALTest extends RhnBaseTestCase {
    private static final Logger log = LogManager.getLogger(CVEAuditManagerOVALTest.class);

    OvalParser ovalParser = new OvalParser();

    Package leap15_4_Pkg;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

    }

    @Test
    void testDoAuditSystemNotAffected() {

    }

    @Test
    void testDoAuditSystemPatched() throws Exception {
        OvalRootType ovalRoot = ovalParser.parse(TestUtils
                .findTestData("/com/redhat/rhn/manager/audit/test/oval/testDoAuditSystemNotAffected.oval.xml"));

        // TODO: Compute object hash to make sure we update tests whenever test OVAL files changes
        DefinitionType definitionType = ovalRoot.getDefinitions().get(0);

        saveAllOVALTests(ovalRoot);

        Cve cve = createTestCve("CVE-2022-2991");

        Set<Cve> cves = Set.of(cve);
        User user = createTestUser();

        Errata errata = createTestErrata(user, cves);
        Channel channel = createTestChannel(user, errata);
        Set<Channel> channels = Set.of(channel);

        Server server = createTestServer(user, channels);
        server.setCpe("cpe:/o:opensuse:leap:15.4");

        createOVALDefinition(definitionType);

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

    @Test
    void testDoAuditSystemAffectedPatchUnavailable() {

    }

    /**
     * This package is used to distinguish openSUSE Leap 15.4 distributions. We use very often in tests, so
     * it's abstracted here
     * */
    private static Package createLeap15_4_Package(User user, Errata errata, Channel channel) throws Exception {
        Package unpatched2 = createTestPackage(user, channel, "noarch");
        unpatched2.setPackageName(createTestPackageName("openSUSE-release"));
        Package patched2 = createLaterTestPackage(user, errata, channel, unpatched2,
                "0", "15.4", "0");
        return patched2;
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
