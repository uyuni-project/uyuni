/*
 * Copyright (c) 2009--2015 Red Hat, Inc.
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
package com.redhat.rhn.domain.kickstart.builder.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.kickstart.KickstartCommand;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartInstallType;
import com.redhat.rhn.domain.kickstart.KickstartRawData;
import com.redhat.rhn.domain.kickstart.KickstartScript;
import com.redhat.rhn.domain.kickstart.KickstartVirtualizationType;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.domain.kickstart.builder.KickstartBuilder;
import com.redhat.rhn.domain.kickstart.builder.KickstartParser;
import com.redhat.rhn.domain.kickstart.builder.KickstartParsingException;
import com.redhat.rhn.domain.kickstart.test.KickstartDataTest;
import com.redhat.rhn.domain.kickstart.test.KickstartRawDataTest;
import com.redhat.rhn.domain.kickstart.test.KickstartableTreeTest;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.frontend.action.kickstart.KickstartTreeUpdateType;
import com.redhat.rhn.frontend.xmlrpc.kickstart.InvalidVirtualizationTypeException;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class KickstartBuilderTest extends BaseTestCaseWithUser {


    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        UserTestUtils.addUserRole(user, RoleFactory.ORG_ADMIN);
    }


    private KickstartParser createKickstartParser(String filename) throws IOException, ClassNotFoundException {
        String kickstartFileContents = TestUtils.readAll(
                TestUtils.findTestData(filename));
        return new KickstartParser(kickstartFileContents);
    }

    @Test
    public void testCreate() throws Exception {
        KickstartBuilder builder = new KickstartBuilder(user);

        KickstartableTree tree = KickstartableTreeTest.createTestKickstartableTree();
        tree.setInstallType(KickstartFactory.
                lookupKickstartInstallTypeByLabel(KickstartInstallType.RHEL_6));
        KickstartData data =
                builder.create(TestUtils.randomString(), tree,
                        KickstartVirtualizationType.XEN_PARAVIRT,
                        "http://localhost/ks", "redhat",
                KickstartTreeUpdateType.NONE);
        assertNotNull(data);
    }

    @Test
    public void testDirector() throws IOException, ClassNotFoundException {
        KickstartParser parser = createKickstartParser("samplekickstart1.ks");
        assertEquals(27, parser.getOptionLines().size());
        assertEquals(102, parser.getPackageLines().size());
        assertTrue((parser.getPackageLines().get(0)).startsWith("%packages"));
        assertEquals(40, parser.getPreScriptLines().size());
        assertEquals(0, parser.getPostScriptLines().size());
    }

    @Test
    public void testBuildCommands() throws Exception {
        KickstartParser parser = createKickstartParser("samplekickstart1.ks");
        KickstartBuilder builder = new KickstartBuilder(user);
        KickstartableTree tree = KickstartableTreeTest.createTestKickstartableTree();

        List<String> lines = parser.getOptionLines();

        KickstartData ksData = createBareKickstartData();

        builder.buildCommands(ksData, lines, tree);

        KickstartCommand rootpw = ksData.getCommand("rootpw");
        assertFalse(rootpw.getArguments().contains("--iscrypted"));
        assertTrue(rootpw.getArguments().startsWith("$1$"));
    }


    private KickstartData createBareKickstartData() throws Exception {
        KickstartData ksData = new KickstartData();
        ksData.setOrg(user.getOrg());
        ksData.setLabel("testlabel");
        ksData.setActive(Boolean.TRUE);
        ksData.setOrgDefault(false);
        ksData.setKickstartDefaults(KickstartDataTest.createDefaults(ksData, user));
        KickstartFactory.saveKickstartData(ksData);
        return ksData;
    }


    @Test
    public void testKickstartRawData() throws Exception {


        KickstartableTree tree = KickstartableTreeTest.createTestKickstartableTree();
        try {
            KickstartRawDataTest.createRawData(user,
                    "badvirttype", tree, "some contents", "whatever");
            fail();
        }
        catch (InvalidVirtualizationTypeException e) {
            // expected
        }
        KickstartRawDataTest.createRawData(user, "decent", tree,
                "some contents", KickstartVirtualizationType.XEN_PARAVIRT);
        KickstartRawData data = KickstartRawDataTest.createRawData(user,
                "boring", tree, "some contents",
                KickstartVirtualizationType.PARA_HOST);
        assertNotNull(data);
    }

    @Test
    public void testEncryptRootpw() throws Exception {
        KickstartBuilder builder = new KickstartBuilder(user);
        KickstartableTree tree = KickstartableTreeTest.createTestKickstartableTree();

        List<String> lines = new LinkedList<>();
        lines.add("rootpw blahblah");

        KickstartData ksData = createBareKickstartData();
        builder.buildCommands(ksData, lines, tree);
        KickstartCommand rootpw = ksData.getCommand("rootpw");
        assertTrue(rootpw.getArguments().startsWith("--iscrypted $5$"));
    }

    @Test
    public void testBuildPackages() throws Exception {
        KickstartBuilder builder = new KickstartBuilder(user);

        List<String> lines = new LinkedList<>();

        lines.add("%packages");
        lines.add("@office");
        lines.add("@admin-tools");
        lines.add("@editors");
        lines.add("fuse");
        lines.add("-zsh");
        lines.add("awstats");

        KickstartData ksData = createBareKickstartData();
        builder.buildPackages(ksData, lines);
        assertEquals(6, ksData.getKsPackages().size());
    }

    @Test
    public void testBuidEmptyPackages() throws Exception {
        // No idea if this is valid or not but I see no reason why the builder shouldn't
        // be ready for it:
        KickstartBuilder builder = new KickstartBuilder(user);
        List<String> lines = new LinkedList<>();
        KickstartData ksData = createBareKickstartData();
        builder.buildPackages(ksData, lines);
        assertEquals(0, ksData.getKsPackages().size());
    }

    @Test
    public void testBuildPreScripts() throws Exception {
        KickstartParser parser = createKickstartParser("samplekickstart1.ks");
        KickstartBuilder builder = new KickstartBuilder(user);

        List<String> lines = parser.getPreScriptLines();

        KickstartData ksData = createBareKickstartData();
        builder.buildPreScripts(ksData, lines);
        assertEquals(1, ksData.getScripts().size());
        KickstartScript script = ksData.getScripts().iterator().next();
        assertNull(script.getInterpreter());
        assertEquals(KickstartScript.TYPE_PRE, script.getScriptType());
        assertEquals("Y", script.getChroot());
    }

    @Test
    public void testBuildPreScriptWithInterpreter() throws Exception {
        KickstartBuilder builder = new KickstartBuilder(user);

        List<String> lines = new LinkedList<>();
        lines.add("%pre --interpreter /usr/bin/python");

        KickstartData ksData = createBareKickstartData();
        builder.buildPreScripts(ksData, lines);
        assertEquals(1, ksData.getScripts().size());
        KickstartScript script = ksData.getScripts().iterator().next();
        assertEquals("/usr/bin/python", script.getInterpreter());
    }

    @Test
    public void testBuildPreScriptNewlines() throws Exception {
        KickstartBuilder builder = new KickstartBuilder(user);

        List<String> lines = new LinkedList<>();
        lines.add("%pre --interpreter /usr/bin/python");
        lines.add("a");
        lines.add("b");

        KickstartData ksData = createBareKickstartData();
        builder.buildPreScripts(ksData, lines);
        assertEquals(1, ksData.getScripts().size());
        KickstartScript script = ksData.getScripts().iterator().next();
        assertEquals("a\nb", script.getDataContents());
    }

    @Test
    public void testBuildPreScriptWithMissingInterpreter() throws Exception {
        KickstartBuilder builder = new KickstartBuilder(user);

        List<String> lines = new LinkedList<>();
        lines.add("%pre --interpreter");

        KickstartData ksData = createBareKickstartData();
        try {
            builder.buildPreScripts(ksData, lines);
            fail();
        }
        catch (KickstartParsingException e) {
            // expected
        }
    }

    @Test
    public void testBuildMultiplePreScripts() throws Exception {
        KickstartBuilder builder = new KickstartBuilder(user);

        List<String> lines = new LinkedList<>();
        lines.add("%pre");
        lines.add("echo hello");
        lines.add("");
        lines.add("%pre");
        lines.add("echo world");

        KickstartData ksData = createBareKickstartData();
        builder.buildPreScripts(ksData, lines);
        assertEquals(2, ksData.getScripts().size());
    }

    @Test
    public void testBuidPreScriptWithNochroot() throws Exception {
        KickstartBuilder builder = new KickstartBuilder(user);

        // Should not parse:
        List<String> lines = new LinkedList<>();
        lines.add("%pre --nochroot");

        KickstartData ksData = createBareKickstartData();
        try {
            builder.buildPreScripts(ksData, lines);
            fail();
        }
        catch (KickstartParsingException e) {
            // expected
        }
    }

    @Test
    public void testBuildPostScriptWithNochroot() throws Exception {
        KickstartBuilder builder = new KickstartBuilder(user);

        List<String> lines = new LinkedList<>();
        lines.add("%post --interpreter blah --nochroot");
        lines.add("echo hello");

        KickstartData ksData = createBareKickstartData();
        builder.buildPostScripts(ksData, lines);
        assertEquals(1, ksData.getScripts().size());
        KickstartScript script = ksData.getScripts().iterator().next();
        assertEquals(KickstartScript.TYPE_POST, script.getScriptType());
        assertEquals("N", script.getChroot());
    }

    @Test
    public void testBuildScriptWithEmptyLines() throws Exception {
        KickstartBuilder builder = new KickstartBuilder(user);

        List<String> lines = new LinkedList<>();

        KickstartData ksData = createBareKickstartData();
        builder.buildPostScripts(ksData, lines);
        assertEquals(0, ksData.getScripts().size());
    }

    @Test
    public void testConstruct() throws Exception {
        KickstartParser parser = createKickstartParser("samplekickstart1.ks");
        KickstartBuilder builder = new KickstartBuilder(user);

        KickstartableTree tree = KickstartableTreeTest.createTestKickstartableTree();
        KickstartData ksData = builder.createFromParser(parser, "mykslabel",
                KickstartVirtualizationType.XEN_PARAVIRT, tree,
                KickstartTreeUpdateType.NONE);
        assertEquals(100, ksData.getKsPackages().size());
        assertEquals(1, ksData.getScripts().size());

        KickstartScript preScript = ksData.getScripts().iterator().next();
        assertEquals(KickstartScript.TYPE_PRE, preScript.getScriptType());
    }

    @Test
    public void testConstructWithExistingLabel() throws Exception {
        KickstartParser parser = createKickstartParser("samplekickstart1.ks");
        KickstartBuilder builder = new KickstartBuilder(user);

        KickstartableTree tree = KickstartableTreeTest.createTestKickstartableTree();
        builder.createFromParser(parser, "mykslabel",
                KickstartVirtualizationType.XEN_PARAVIRT, tree,
                KickstartTreeUpdateType.NONE);
        try {
            builder.createFromParser(parser, "mykslabel",
                    KickstartVirtualizationType.XEN_PARAVIRT, tree,
                    KickstartTreeUpdateType.NONE);
            fail();
        }
        catch (ValidatorException e) {
            // expected
        }
    }

    @Test
    public void testConstructWithInvaidVirtType() throws Exception {
        KickstartParser parser = createKickstartParser("samplekickstart1.ks");
        KickstartBuilder builder = new KickstartBuilder(user);

        KickstartableTree tree = KickstartableTreeTest.createTestKickstartableTree();
        try {
            builder.createFromParser(parser, "badvirttype", "whatever", tree,
                    KickstartTreeUpdateType.NONE);
            fail();
        }
        catch (InvalidVirtualizationTypeException e) {
            // expected
        }
    }

    @Test
    public void testConstructUpgradeKickstart() throws Exception {
        KickstartParser parser = createKickstartParser("upgrade.ks");
        KickstartBuilder builder = new KickstartBuilder(user);

        KickstartableTree tree = KickstartableTreeTest.createTestKickstartableTree();
        KickstartData data = builder.createFromParser(parser, "upgrade-ks",
                KickstartVirtualizationType.XEN_PARAVIRT, tree,
                KickstartTreeUpdateType.NONE);

        assertNotNull(data.getCommand("upgrade"));
        assertNull(data.getCommand("install"));
    }

    @Test
    public void testImportUseDefautDownloadLocation() throws Exception {
        KickstartParser parser = createKickstartParser("samplekickstart1.ks");
        KickstartBuilder builder = new KickstartBuilder(user);
        KickstartableTree tree = KickstartableTreeTest.createTestKickstartableTree();
        KickstartData data = builder.createFromParser(parser,
                "testing-profile", KickstartVirtualizationType.XEN_PARAVIRT,
                tree, KickstartTreeUpdateType.NONE);

        assertNull(data.getCommand("nfs"));
        KickstartCommand urlCmd = data.getCommand("url");
        assertNotNull(urlCmd);
        assertTrue(urlCmd.getArguments().startsWith("/ks/dist/org/"));
    }



}
