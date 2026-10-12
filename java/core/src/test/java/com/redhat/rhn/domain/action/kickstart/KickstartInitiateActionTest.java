/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.action.kickstart;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartTestUtils;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.domain.kickstart.KickstartableTreeTest;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import org.cobbler.CobblerConnection;
import org.cobbler.Distro;
import org.cobbler.Profile;
import org.cobbler.SystemRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Tests the kernel options composition of the kickstart initiate action.
 */
public class KickstartInitiateActionTest extends BaseTestCaseWithUser {

    private static final String HOST = "kickstart.example.com";
    private static final String SYSTEM_NAME = "system1";

    @BeforeEach
    public void setUp() throws Exception {
        KickstartTestUtils.setupTestConfiguration(user);
    }

    /**
     * Removes everything the test created on cobbler (real or mocked) -
     * mandatory per test: the fixtures reuse one fixed MAC address, which
     * real cobbler rejects on the next system record.
     */
    @AfterEach
    public void tearDown() {
        KickstartTestUtils.cleanUpCobbler();
    }

    /**
     * SLES15: the resolved options are adapted for AutoYaST (textmode,
     * no initrd) and autoyast= plus info= are appended.
     */
    @Test
    public void testSles15KernelOptions() throws Exception {
        KickstartableTree tree = treeOfType("sles15generic");
        SystemRecord system = cobblerSystem("suse", "sles15generic",
                "text console=ttyS0 initrd=extra", "ksdevice=eth0");

        String kopts = KickstartInitiateAction.buildKernelOptions(system, tree, HOST, true);

        assertOptions(List.of(
                "console=ttyS0",
                "ksdevice=eth0",
                "install=http://" + HOST + "/ty/token1",
                "textmode=1",
                "autoyast=http://" + HOST + "/cblr/svc/op/autoinstall/system/" + SYSTEM_NAME,
                "info=http://" + HOST + "/cblr/svc/op/nopxe/system/" + SYSTEM_NAME), kopts);
        assertFalse(kopts.contains("text "));
        assertFalse(kopts.contains("initrd=extra"));
    }

    /**
     * SLE16: routed to the Agama builder, inst.auto= is appended.
     */
    @Test
    public void testSles16KernelOptions() throws Exception {
        KickstartableTree tree = treeOfType("opensuse16.1");
        SystemRecord system = cobblerSystem("suse", "sles15generic",
                "text console=ttyS0", "ksdevice=eth0");

        String kopts = KickstartInitiateAction.buildKernelOptions(system, tree, HOST, true);

        assertOptions(List.of(
                "console=ttyS0",
                "ksdevice=eth0",
                "textmode=1",
                "inst.auto=http://" + HOST + "/cblr/svc/op/autoinstall/system/" + SYSTEM_NAME,
                "install=http://" + HOST + "/ty/token1",
                "info=http://" + HOST + "/cblr/svc/op/nopxe/system/" + SYSTEM_NAME), kopts);
    }

    /**
     * RHEL: routed to the RHEL builder, inst.ks.sendmac plus inst.ks= are
     * appended and no textmode transform happens.
     */
    @Test
    public void testRhelKernelOptions() throws Exception {
        KickstartableTree tree = treeOfType("rhel_7");
        SystemRecord system = cobblerSystem("redhat", "rhel7",
                "inst.repo=http://" + HOST + "/ty/token1", "ksdevice=eth0");

        String kopts = KickstartInitiateAction.buildKernelOptions(system, tree, HOST, true);

        assertOptions(List.of(
                "inst.repo=http://" + HOST + "/ty/token1",
                "install=http://" + HOST + "/ty/token1",
                "ksdevice=eth0",
                "inst.ks.sendmac",
                "inst.ks=http://" + HOST + "/cblr/svc/op/autoinstall/system/" + SYSTEM_NAME,
                "info=http://" + HOST + "/cblr/svc/op/nopxe/system/" + SYSTEM_NAME), kopts);
    }

    /**
     * With autoinstall=false no info= (nopxe) URL is appended.
     */
    @Test
    public void testNoInfoWithoutAutoinstall() throws Exception {
        KickstartableTree tree = treeOfType("sles15generic");
        SystemRecord system = cobblerSystem("suse", "sles15generic",
                "console=ttyS0", "ksdevice=eth0");

        String kopts = KickstartInitiateAction.buildKernelOptions(system, tree, HOST, false);

        assertFalse(kopts.contains("info="));
    }

    /**
     * Composing over already-composed options (the system record carries
     * the media override and the installer selection from a previous
     * composition) duplicates nothing.
     */
    @Test
    public void testKernelOptionsIdempotent() throws Exception {
        KickstartableTree tree = treeOfType("sles15generic");
        SystemRecord system = cobblerSystem("suse", "sles15generic",
                "console=ttyS0",
                "ksdevice=eth0 install=http://" + HOST + "/ty/token1"
                        + " autoyast=http://" + HOST + "/cblr/svc/op/autoinstall/system/"
                        + SYSTEM_NAME);

        String kopts = KickstartInitiateAction.buildKernelOptions(system, tree, HOST, true);

        assertOptions(List.of(
                "console=ttyS0",
                "ksdevice=eth0",
                "install=http://" + HOST + "/ty/token1",
                "autoyast=http://" + HOST + "/cblr/svc/op/autoinstall/system/" + SYSTEM_NAME,
                "info=http://" + HOST + "/cblr/svc/op/nopxe/system/" + SYSTEM_NAME), kopts);
        assertEquals(1, kopts.split("autoyast=", -1).length - 1);
        assertEquals(1, kopts.split("install=", -1).length - 1);
    }

    /**
     * RHEL: a resolved text option survives untransformed (the textmode
     * transform is suse-breed only). Cobbler resolves a bare flag to an
     * empty value, which the mock renders as text= and real cobbler (blank
     * values round-trip through its YAML storage) as text=~ - legacy parity
     * in both cases.
     */
    @Test
    public void testRhelTextSurvivesUntransformed() throws Exception {
        KickstartableTree tree = treeOfType("rhel_7");
        SystemRecord system = cobblerSystem("redhat", "rhel7",
                "text console=ttyS0", "ksdevice=eth0");

        String kopts = KickstartInitiateAction.buildKernelOptions(system, tree, HOST, true);

        String expectedText = KickstartTestUtils.useRealCobbler() ? "text=~" : "text=";
        assertOptions(List.of(
                expectedText,
                "console=ttyS0",
                "ksdevice=eth0",
                "install=http://" + HOST + "/ty/token1",
                "inst.ks.sendmac",
                "inst.ks=http://" + HOST + "/cblr/svc/op/autoinstall/system/" + SYSTEM_NAME,
                "info=http://" + HOST + "/cblr/svc/op/nopxe/system/" + SYSTEM_NAME), kopts);
        assertFalse(kopts.contains("textmode"));
    }

    private KickstartableTree treeOfType(String installTypeLabel) throws Exception {
        KickstartableTree tree = KickstartableTreeTest.createTestKickstartableTree();
        tree.setInstallType(KickstartFactory.lookupKickstartInstallTypeByLabel(installTypeLabel));
        return TestUtils.saveAndFlush(tree);
    }

    private SystemRecord cobblerSystem(String breed, String osVersion, String distroOptions,
                                       String profileOptions) {
        CobblerConnection con = CobblerXMLRPCHelper.getConnection("test");
        Distro distro = new Distro.Builder<String>()
                .setName("tree1:1")
                .setKernel("/tmp/kickstart/images/images/pxeboot/vmlinuz")
                .setInitrd("/tmp/kickstart/images/images/pxeboot/initrd.img")
                .setBreed(breed)
                .setOsVersion(osVersion)
                .setKernelOptions(Optional.of(distroOptions))
                .build(con);
        Profile profile = Profile.create(con, "profile1", distro);
        profile.setKernelOptions(Optional.of(profileOptions));
        profile.save();
        SystemRecord system = SystemRecord.create(con, SYSTEM_NAME, profile);
        system.setKernelOptions(Optional.of("install=http://" + HOST + "/ty/token1"));
        system.save();
        return SystemRecord.lookupByName(con, SYSTEM_NAME);
    }

    private void assertOptions(List<String> expected, String actual) {
        List<String> expectedTokens = expected.stream().sorted().toList();
        List<String> actualTokens = Arrays.stream(actual.split(" ")).sorted().toList();
        assertEquals(expectedTokens, actualTokens);
    }
}
