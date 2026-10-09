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

package com.redhat.rhn.manager.kickstart.cobbler;

import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartDataTest;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartInstallType;
import com.redhat.rhn.domain.kickstart.KickstartTestUtils;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.TestStatics;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.autoinstallation.KernelOptionsList;

import org.cobbler.CobblerConnection;
import org.cobbler.Distro;
import org.cobbler.SystemRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Optional;

public class CobblerSystemCreateCommandTest extends BaseTestCaseWithUser {

    private static final String KICKSTART_HOST = "kickstart.example.com";
    private static final String MEDIA_PATH = "/ty/token1";

    @BeforeEach
    public void setUp() throws Exception {
        KickstartTestUtils.setupTestConfiguration(user);
    }

    @Test
    public void testConstructorDbPersistence() {
        // Arrange
        CobblerConnection connection = CobblerXMLRPCHelper.getConnection(user);
        connection.invokeMethod("new_profile");
        var profileId = ((LinkedList<HashMap>) connection.invokeMethod("get_profiles")).get(0).get("uid");
        KickstartData k = KickstartDataTest.createTestKickstartData(user.getOrg());
        k.setCobblerId(profileId.toString());
        Server s = ServerFactoryTest.createTestServer(user, false);

        // Act
        new CobblerSystemCreateCommand(user, k, s);

        // Assert
    }

    @Test
    public void testConstructorUnknown1() {
        // Arrange
        CobblerConnection connection = CobblerXMLRPCHelper.getConnection(user);
        connection.invokeMethod("new_profile");
        var profileId = ((LinkedList<HashMap>) connection.invokeMethod("get_profiles")).get(0).get("uid");
        KickstartData k = KickstartDataTest.createTestKickstartData(user.getOrg());
        k.setCobblerId(profileId.toString());
        Server s = ServerFactoryTest.createTestServer(user, false);

        // Act
        new CobblerSystemCreateCommand(user, s, k, "mediaPathIn", "activationKeysIn");

        // Assert
    }

    @Test
    public void testConstructorReactivation() {
        // Arrange
        User admin = new UserTestUtils.UserBuilder()
                .userName(TestStatics.TEST_ADMIN_USER)
                .orgAdmin(true)
                .build();
        KickstartData k = KickstartDataTest.createTestKickstartData(admin.getOrg());
        k.setCobblerId("test-id");
        Server s = ServerFactoryTest.createTestServer(admin, false);

        // Act
        new CobblerSystemCreateCommand(user, s, "cobblerProfileName", k);

        // Assert
    }

    @Test
    public void testConstructorCobblerPassthrough() {
        // Arrange
        KickstartData k = KickstartDataTest.createTestKickstartData(user.getOrg());
        Server s = ServerFactoryTest.createTestServer(user, false);

        // Act
        new CobblerSystemCreateCommand(user, "cobblerProfileName", k, s.getName(), 0L);

        // Assert
    }

    @Test
    public void testConstructorUnknown2() {
        // Arrange
        Server s = ServerFactoryTest.createTestServer(user, false);

        // Act
        new CobblerSystemCreateCommand(user, s, "nameIn");

        // Assert
    }

    @Test
    public void testStore() {
        // Arrange
        CobblerConnection connection = CobblerXMLRPCHelper.getConnection(user);
        connection.invokeMethod("new_profile");
        String profileName = ((LinkedList<HashMap>) connection.invokeMethod("get_profiles"))
                .get(0).get("name").toString();
        Server s = ServerFactoryTest.createTestServer(user, false);
        CobblerSystemCreateCommand cobblerSystemCreateCommand = new CobblerSystemCreateCommand(user, s, profileName);

        // Act
        ValidatorError error = cobblerSystemCreateCommand.store();

        // Assert
        Assertions.assertNull(error);
    }

    /**
     * SLES15: the system record gets the session-tracked media URL as
     * install= plus the installer-updates child URL as self_update=; the
     * user options are preserved in front.
     */
    @Test
    public void testSles15SystemKernelOptions() throws Exception {
        // Arrange
        KickstartData ksdata = ksDataOfType("sles15generic");
        Channel child = addInstallerUpdatesChild(ksdata);

        // Act
        SystemRecord rec = storedSystemRecord(ksdata, "ksdevice=eth0 console=ttyS0");

        // Assert
        Assertions.assertEquals("ksdevice=eth0 console=ttyS0"
                        + " install=http://" + KICKSTART_HOST + MEDIA_PATH
                        + " self_update=http://" + KICKSTART_HOST + "/ks/dist/child/"
                        + child.getLabel() + "/" + ksdata.getTree().getLabel(),
                rec.getKernelOptions().get());
    }

    /**
     * SLES15 without an accessible installer-updates child disables the
     * self update with 0.
     */
    @Test
    public void testSles15SystemKernelOptionsWithoutChild() throws Exception {
        // Arrange
        KickstartData ksdata = ksDataOfType("sles15generic");

        // Act
        SystemRecord rec = storedSystemRecord(ksdata, "ksdevice=eth0");

        // Assert
        Assertions.assertEquals("ksdevice=eth0"
                        + " install=http://" + KICKSTART_HOST + MEDIA_PATH
                        + " self_update=0",
                rec.getKernelOptions().get());
    }

    /**
     * SLE16: the Agama form inst.self_update= is used instead of the YaST
     * form self_update=.
     */
    @Test
    public void testSles16SystemKernelOptions() throws Exception {
        // Arrange
        KickstartData ksdata = ksDataOfType("opensuse16.1");
        addInstallerUpdatesChild(ksdata);

        // Act
        SystemRecord rec = storedSystemRecord(ksdata, "ksdevice=eth0");

        // Assert
        KernelOptionsList options = new KernelOptionsList(kernelOptionsOf(rec));
        Assertions.assertEquals("http://" + KICKSTART_HOST + MEDIA_PATH,
                options.getValue("inst.install_url"));
        Assertions.assertEquals("http://" + KICKSTART_HOST + "/ks/dist/child/"
                        + firstInstallerUpdatesChild(ksdata) + "/" + ksdata.getTree().getLabel(),
                options.getValue("inst.self_update"));
        Assertions.assertFalse(options.hasOption("self_update"));
        Assertions.assertFalse(options.hasOption("install"));
    }

    /**
     * A resolved inst.self_update=0 opt-out (Agama form) suppresses the
     * computed installer-updates option.
     */
    @Test
    public void testSles16ResolvedOptOut() throws Exception {
        // Arrange
        KickstartData ksdata = ksDataOfType("opensuse16.1");
        setDistroKernelOptions(ksdata, "inst.self_update=0");

        // Act
        SystemRecord rec = storedSystemRecord(ksdata, "ksdevice=eth0");

        // Assert
        KernelOptionsList options = new KernelOptionsList(kernelOptionsOf(rec));
        Assertions.assertEquals("http://" + KICKSTART_HOST + MEDIA_PATH,
                options.getValue("inst.install_url"));
        Assertions.assertFalse(options.hasOption("inst.self_update"));
        Assertions.assertFalse(options.hasOption("self_update"));
    }

    /**
     * A resolved self_update=0 opt-out (YaST form) suppresses the computed
     * installer-updates option.
     */
    @Test
    public void testSles15ResolvedOptOut() throws Exception {
        // Arrange
        KickstartData ksdata = ksDataOfType("sles15generic");
        setDistroKernelOptions(ksdata, "self_update=0");

        // Act
        SystemRecord rec = storedSystemRecord(ksdata, "ksdevice=eth0");

        // Assert
        KernelOptionsList options = new KernelOptionsList(kernelOptionsOf(rec));
        Assertions.assertEquals("http://" + KICKSTART_HOST + MEDIA_PATH,
                options.getValue("install"));
        Assertions.assertFalse(options.hasOption("self_update"));
        Assertions.assertFalse(options.hasOption("inst.self_update"));
    }

    /**
     * User-typed install= and self_update= options win over the computed
     * ones; nothing computed is added.
     */
    @Test
    public void testUserTypedOptionsWin() throws Exception {
        // Arrange
        KickstartData ksdata = ksDataOfType("sles15generic");
        addInstallerUpdatesChild(ksdata);
        String userOptions = "ksdevice=eth0 install=http://custom self_update=0";

        // Act
        SystemRecord rec = storedSystemRecord(ksdata, userOptions);

        // Assert
        Assertions.assertEquals(userOptions, rec.getKernelOptions().get());
    }

    /**
     * RHEL: the system record gets the session-tracked media URL as
     * inst.repo= and no installer-updates option.
     */
    @Test
    public void testRhelSystemKernelOptions() throws Exception {
        // Arrange
        KickstartData ksdata = ksDataOfType(KickstartInstallType.RHEL_7);

        // Act
        SystemRecord rec = storedSystemRecord(ksdata, "ksdevice=eth0");

        // Assert
        KernelOptionsList options = new KernelOptionsList(kernelOptionsOf(rec));
        Assertions.assertEquals("http://" + KICKSTART_HOST + MEDIA_PATH,
                options.getValue("inst.repo"));
        Assertions.assertFalse(options.hasOption("install"));
        Assertions.assertFalse(options.hasOption("self_update"));
        Assertions.assertFalse(options.hasOption("inst.self_update"));
    }

    /**
     * Without a media path nothing is computed and the user options are
     * stored verbatim.
     */
    @Test
    public void testNoMediaPathStoresUserOptionsVerbatim() throws Exception {
        // Arrange
        KickstartData ksdata = ksDataOfType("sles15generic");
        Server s = ServerFactoryTest.createTestServer(user, false);
        CobblerSystemCreateCommand cmd = new CobblerSystemCreateCommand(user, ksdata, s);
        cmd.setKickstartHost(KICKSTART_HOST);
        cmd.setKernelOptions("opt=1");

        // Act
        Assertions.assertNull(cmd.store());
        SystemRecord rec = SystemRecord.lookupById(
                CobblerXMLRPCHelper.getConnection(user), s.getCobblerId());

        // Assert
        Assertions.assertEquals("opt=1", rec.getKernelOptions().get());
    }

    /**
     * Post kernel options are stored verbatim (no normalization).
     */
    @Test
    public void testPostKernelOptionsVerbatim() throws Exception {
        // Arrange
        KickstartData ksdata = ksDataOfType("sles15generic");
        Server s = ServerFactoryTest.createTestServer(user, false);
        CobblerSystemCreateCommand cmd = new CobblerSystemCreateCommand(user, s, ksdata, MEDIA_PATH, "keys");
        cmd.setKickstartHost(KICKSTART_HOST);
        cmd.setKernelOptions("ksdevice=eth0");
        cmd.setPostKernelOptions("  post=1   post2  ");

        // Act
        Assertions.assertNull(cmd.store());
        SystemRecord rec = SystemRecord.lookupById(
                CobblerXMLRPCHelper.getConnection(user), s.getCobblerId());

        // Assert
        Assertions.assertEquals("  post=1   post2  ", rec.getKernelOptionsPost().get());
    }

    /**
     * Storing twice recomputes the same options (idempotent result).
     */
    @Test
    public void testStoreTwiceIsIdempotent() throws Exception {
        // Arrange
        KickstartData ksdata = ksDataOfType("sles15generic");
        Server s = ServerFactoryTest.createTestServer(user, false);

        // Act
        CobblerSystemCreateCommand cmd = new CobblerSystemCreateCommand(user, s, ksdata, MEDIA_PATH, "keys");
        cmd.setKickstartHost(KICKSTART_HOST);
        cmd.setKernelOptions("ksdevice=eth0");
        Assertions.assertNull(cmd.store());
        String first = kernelOptionsOf(SystemRecord.lookupById(
                CobblerXMLRPCHelper.getConnection(user), s.getCobblerId()));

        CobblerSystemCreateCommand cmd2 = new CobblerSystemCreateCommand(user, s, ksdata, MEDIA_PATH, "keys");
        cmd2.setKickstartHost(KICKSTART_HOST);
        cmd2.setKernelOptions("ksdevice=eth0");
        Assertions.assertNull(cmd2.store());
        String second = kernelOptionsOf(SystemRecord.lookupById(
                CobblerXMLRPCHelper.getConnection(user), s.getCobblerId()));

        // Assert
        Assertions.assertEquals(first, second);
    }

    private String kernelOptionsOf(SystemRecord rec) {
        return String.valueOf(rec.getKernelOptions().get());
    }

    private KickstartData ksDataOfType(String installTypeLabel) throws Exception {
        KickstartData ksdata = KickstartDataTest.createKickstartWithDefaultKey(user.getOrg());
        KickstartInstallType installType = KickstartFactory.lookupKickstartInstallTypeByLabel(installTypeLabel);
        ksdata.getTree().setInstallType(installType);
        TestUtils.saveAndFlush(ksdata.getTree());
        Distro distro = Distro.lookupById(CobblerXMLRPCHelper.getConnection(user), ksdata.getTree().getCobblerId());
        distro.setBreed(installType.getCobblerBreed());
        distro.setOsVersion(installType.getCobblerOsVersion());
        return ksdata;
    }

    private void setDistroKernelOptions(KickstartData ksdata, String kernelOptions) {
        Distro distro = Distro.lookupById(CobblerXMLRPCHelper.getConnection(user), ksdata.getTree().getCobblerId());
        distro.setKernelOptions(Optional.ofNullable(kernelOptions));
    }

    private Channel addInstallerUpdatesChild(KickstartData ksdata) throws Exception {
        Channel child = ChannelTestUtils.createChildChannel(user, ksdata.getTree().getChannel());
        child.setInstallerUpdates(true);
        return TestUtils.saveAndFlush(child);
    }

    private String firstInstallerUpdatesChild(KickstartData ksdata) {
        return ksdata.getTree().getChannel().getAccessibleChildrenFor(user).stream()
                .filter(Channel::isInstallerUpdates)
                .findFirst()
                .map(Channel::getLabel)
                .orElseThrow();
    }

    private SystemRecord storedSystemRecord(KickstartData ksdata, String kernelOptions) {
        Server s = ServerFactoryTest.createTestServer(user, false);
        CobblerSystemCreateCommand cmd = new CobblerSystemCreateCommand(user, s, ksdata, MEDIA_PATH, "keys");
        cmd.setKickstartHost(KICKSTART_HOST);
        cmd.setKernelOptions(kernelOptions);
        Assertions.assertNull(cmd.store());
        return SystemRecord.lookupById(CobblerXMLRPCHelper.getConnection(user), s.getCobblerId());
    }
}
