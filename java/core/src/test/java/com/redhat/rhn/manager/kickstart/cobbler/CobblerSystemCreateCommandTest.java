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

import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartDataTest;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartInstallType;
import com.redhat.rhn.domain.kickstart.KickstartTestUtils;
import com.redhat.rhn.domain.org.Org;
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
import org.cobbler.Profile;
import org.cobbler.SystemRecord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CobblerSystemCreateCommandTest extends BaseTestCaseWithUser {

    private static final String KICKSTART_HOST = "kickstart.example.com";
    private static final String MEDIA_PATH = "/ty/token1";

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

    @Test
    public void testConstructorDbPersistence() {
        // Arrange
        CobblerConnection connection = CobblerXMLRPCHelper.getConnection(user);
        String profileId = createProfile(connection).get("uid").toString();
        KickstartData k = KickstartDataTest.createTestKickstartData(user.getOrg());
        k.setCobblerId(profileId);
        Server s = ServerFactoryTest.createTestServer(user, false);

        // Act
        new CobblerSystemCreateCommand(user, k, s);

        // Assert
    }

    @Test
    public void testConstructorUnknown1() {
        // Arrange
        CobblerConnection connection = CobblerXMLRPCHelper.getConnection(user);
        String profileId = createProfile(connection).get("uid").toString();
        KickstartData k = KickstartDataTest.createTestKickstartData(user.getOrg());
        k.setCobblerId(profileId);
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
        String profileName = createProfile(connection).get("name").toString();
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
     * user options are preserved in front. Compared as sorted tokens:
     * real cobbler returns the options as an unordered map.
     */
    @Test
    public void testSles15SystemKernelOptions() throws Exception {
        // Arrange
        KickstartData ksdata = ksDataOfType("sles15generic");
        Channel child = addInstallerUpdatesChild(ksdata);

        // Act
        SystemRecord rec = storedSystemRecord(ksdata, "ksdevice=eth0 console=ttyS0");

        // Assert
        assertOptionTokens(rec,
                        List.of("install=http://" + KICKSTART_HOST + MEDIA_PATH,
                        "ksdevice=eth0",
                        "console=ttyS0",
                        "self_update=http://" + KICKSTART_HOST + "/ks/dist/child/" +
                                child.getLabel() + "/" + ksdata.getTree().getLabel()));
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
        assertOptionTokens(rec,
                        List.of("install=http://" + KICKSTART_HOST + MEDIA_PATH,
                        "ksdevice=eth0",
                        "self_update=0"));
    }

    /**
     * SLE16: the Agama form inst.self_update= is used instead of the YaST
     * form self_update=.
     */
    @Test
    public void testSles16SystemKernelOptions() throws Exception {
        // A cobbler without the Agama signature (PR #177) rejects
        // opensuse16.1 distros - skip the SLE16 fixtures there.
        assumeTrue(KickstartTestUtils.cobblerSupportsSles16());
        // Arrange
        KickstartData ksdata = ksDataOfType("opensuse16.1");
        addInstallerUpdatesChild(ksdata);

        // Act
        SystemRecord rec = storedSystemRecord(ksdata, "ksdevice=eth0");

        // Assert
        KernelOptionsList options = new KernelOptionsList(kernelOptionsOf(rec));
        Assertions.assertEquals("http://" + KICKSTART_HOST + MEDIA_PATH,
                options.getValue("inst.install_url"));
        Assertions.assertEquals("http://" + KICKSTART_HOST + "/ks/dist/child/" +
                        firstInstallerUpdatesChild(ksdata) + "/" + ksdata.getTree().getLabel(),
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
        // A cobbler without the Agama signature (PR #177) rejects
        // opensuse16.1 distros - skip the SLE16 fixtures there.
        assumeTrue(KickstartTestUtils.cobblerSupportsSles16());
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
        assertOptionTokens(rec,
                        List.of("install=http://custom",
                        "ksdevice=eth0",
                        "self_update=0"));
    }

    /**
     * SLE16 exact-string parity: the user options plus the computed
     * inst.install_url= and inst.self_update= options, nothing else.
     * Compared as sorted tokens: real cobbler returns the options as an
     * unordered map.
     */
    @Test
    public void testSles16SystemKernelOptionsExactParity() throws Exception {
        // A cobbler without the Agama signature (PR #177) rejects
        // opensuse16.1 distros - skip the SLE16 fixtures there.
        assumeTrue(KickstartTestUtils.cobblerSupportsSles16());
        // Arrange
        KickstartData ksdata = ksDataOfType("opensuse16.1");
        Channel child = addInstallerUpdatesChild(ksdata);

        // Act
        SystemRecord rec = storedSystemRecord(ksdata, "ksdevice=eth0");

        // Assert
        assertOptionTokens(rec,
                        List.of("inst.install_url=http://" + KICKSTART_HOST + MEDIA_PATH,
                        "inst.self_update=http://" + KICKSTART_HOST + "/ks/dist/child/" +
                                child.getLabel() + "/" + ksdata.getTree().getLabel(),
                        "ksdevice=eth0"));
    }

    /**
     * RHEL exact-string parity: the session-tracked media URL as inst.repo=
     * and nothing computed besides it.
     */
    @Test
    public void testRhelSystemKernelOptionsExactParity() throws Exception {
        // Arrange
        KickstartData ksdata = ksDataOfType(KickstartInstallType.RHEL_7);

        // Act
        SystemRecord rec = storedSystemRecord(ksdata, "ksdevice=eth0");

        // Assert
        assertOptionTokens(rec,
                        List.of("inst.repo=http://" + KICKSTART_HOST + MEDIA_PATH,
                        "ksdevice=eth0"));
    }

    /**
     * An installer-updates child of another org is invisible to the
     * schedule user: the user-scoped lookup misses it and the self update
     * is disabled with 0 (spec §5.5).
     */
    @Test
    public void testScheduleUserScopesInstallerUpdatesLookup() throws Exception {
        // Arrange
        KickstartData ksdata = ksDataOfType("sles15generic");
        Org otherOrg = UserTestUtils.createOrg("installerUpdatesOrg");
        User otherUser = UserTestUtils.createUser("otherOrgAdmin", otherOrg.getId());
        Channel child = ChannelTestUtils.createChildChannel(otherUser,
                ksdata.getTree().getChannel());
        child.setInstallerUpdates(true);
        TestUtils.saveAndFlush(child);

        // Act
        SystemRecord rec = storedSystemRecord(ksdata, "ksdevice=eth0");

        // Assert
        assertOptionTokens(rec,
                        List.of("install=http://" + KICKSTART_HOST + MEDIA_PATH,
                        "ksdevice=eth0",
                        "self_update=0"));
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
        Assertions.assertEquals("opt=1", renderedOptions(rec, rec.getKernelOptions()));
    }

    /**
     * Post kernel options are stored verbatim (no normalization). On a real
     * cobbler the options come back as a parsed map, so only the content
     * can be asserted there; the whitespace preservation is covered by the
     * mock-mode run.
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
        if (KickstartTestUtils.useRealCobbler()) {
            assertOptionTokens(rec, rec.getKernelOptionsPost(),
                    List.of("post=1", "post2"));
        }
        else {
            Assertions.assertEquals("  post=1   post2  ", rec.getKernelOptionsPost().get());
        }
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
        List<String> first = sortedOptionTokens(SystemRecord.lookupById(
                CobblerXMLRPCHelper.getConnection(user), s.getCobblerId()));

        CobblerSystemCreateCommand cmd2 = new CobblerSystemCreateCommand(user, s, ksdata, MEDIA_PATH, "keys");
        cmd2.setKickstartHost(KICKSTART_HOST);
        cmd2.setKernelOptions("ksdevice=eth0");
        Assertions.assertNull(cmd2.store());
        List<String> second = sortedOptionTokens(SystemRecord.lookupById(
                CobblerXMLRPCHelper.getConnection(user), s.getCobblerId()));

        // Assert
        Assertions.assertEquals(first, second);
    }

    /**
     * Creates and saves a cobbler profile, returning its name and uid (a
     * real cobbler requires the profile to reference a distro at save).
     */
    private Map<String, Object> createProfile(CobblerConnection connection) {
        String name = "test-profile-" + TestUtils.randomString();
        Distro distro = new Distro.Builder<String>()
                .setName(name + "-distro")
                .setKernel("/tmp/kickstart/images/images/pxeboot/vmlinuz")
                .setInitrd("/tmp/kickstart/images/images/pxeboot/initrd.img")
                .build(connection);
        Profile profile = Profile.create(connection, name, distro);
        return Map.of("uid", profile.getUid(), "name", profile.getName());
    }

    private List<String> sortedOptionTokens(SystemRecord rec) {
        return Arrays.stream(renderedOptions(rec, rec.getKernelOptions()).trim().split("\\s+"))
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Compares the record options with the expectation as sorted tokens
     * (real cobbler returns the options as an unordered map, so the option
     * order is not part of the contract).
     */
    private void assertOptionTokens(SystemRecord rec, List<String> expected) {
        Assertions.assertEquals(
                expected.stream().sorted().collect(Collectors.toList()),
                sortedOptionTokens(rec));
    }

    private void assertOptionTokens(SystemRecord rec, Optional<?> options, List<String> expected) {
        Assertions.assertEquals(
                expected.stream().sorted().collect(Collectors.toList()),
                sortedOptionTokens(rec, options));
    }

    private List<String> sortedOptionTokens(SystemRecord rec, Optional<?> options) {
        return Arrays.stream(renderedOptions(rec, options).trim().split("\\s+"))
                .sorted()
                .collect(Collectors.toList());
    }

    /**
     * Renders the options as a string: the mock keeps the raw string, a
     * real cobbler returns an unordered map.
     */
    private String renderedOptions(SystemRecord rec, Optional<?> options) {
        Object value = options.get();
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            // real cobbler round-trips blank values as "~" - normalize
            // them back to bare flags
            return rec.convertOptionsMap(map).replaceAll("=~(?=(\\s|$))", "");
        }
        return String.valueOf(value);
    }

    private String kernelOptionsOf(SystemRecord rec) {
        return renderedOptions(rec, rec.getKernelOptions());
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
        distro.save();
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
