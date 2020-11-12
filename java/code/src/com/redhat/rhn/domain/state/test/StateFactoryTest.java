/*
 * Copyright (c) 2015 SUSE LLC
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
package com.redhat.rhn.domain.state.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.state.OrgStateRevision;
import com.redhat.rhn.domain.state.PackageState;
import com.redhat.rhn.domain.state.PackageStates;
import com.redhat.rhn.domain.state.ServerGroupStateRevision;
import com.redhat.rhn.domain.state.ServerStateRevision;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ConfigTestUtils;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Tests for {@link StateFactory}.
 */
public class StateFactoryTest extends BaseTestCaseWithUser {

    /**
     * No package states should be returned for a new server.
     *
     * @throws Exception in case of an error
     */
    @Test
    public void testLatestPackageStatesEmpty() throws Exception {
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        assertFalse(StateFactory.latestPackageStates(server).isPresent());
    }

    /**
     * Get the latest package states for a server.
     *
     * @throws Exception in case of an error
     */
    @Test
    public void testLatestPackageStates() throws Exception {
        // Create test packages and a server
        Package pkg1 = PackageTest.createTestPackage(user.getOrg());
        Package pkg2 = PackageTest.createTestPackage(user.getOrg());
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);

        // Create a server state revision
        ServerStateRevision serverState = new ServerStateRevision();
        serverState.setServer(server);
        serverState.setCreator(user);

        // Add a package state and save: pkg1 -> INSTALLED
        PackageState packageState = new PackageState();
        packageState.setStateRevision(serverState);
        packageState.setName(pkg1.getPackageName());
        packageState.setPackageState(PackageStates.INSTALLED);
        serverState.addPackageState(packageState);
        StateFactory.save(serverState);

        // Create a new server state revision
        serverState = new ServerStateRevision();
        serverState.setServer(server);
        serverState.setCreator(user);

        // Add a different package state: pkg2 -> REMOVED
        packageState = new PackageState();
        packageState.setStateRevision(serverState);
        packageState.setName(pkg2.getPackageName());
        packageState.setPackageState(PackageStates.REMOVED);
        serverState.addPackageState(packageState);
        StateFactory.save(serverState);

        // Verify: Latest package states contain only pkg2 -> REMOVED
        Optional<Set<PackageState>> states = StateFactory.latestPackageStates(server);
        assertTrue(states.isPresent());
        assertEquals(1, states.get().size());
        assertTrue(states.get().contains(packageState));
    }

    /**
     * Test assigning a state
     * @throws Exception
     */
    @Test
    public void testAssignConfigChannelsToServer() throws Exception {
        Server server = ServerFactoryTest.createTestServer(user);
        ServerStateRevision serverState = new ServerStateRevision();
        serverState.setServer(server);
        serverState.setCreator(user);

        ConfigChannel channel1 = ConfigTestUtils.createConfigChannel(user.getOrg(), "Foo", "foo");
        ConfigChannel channel2 = ConfigTestUtils.createConfigChannel(user.getOrg(), "Bar", "bar");

        serverState.getConfigChannels().add(channel1);
        serverState.getConfigChannels().add(channel2);

        StateFactory.save(serverState);
        clearFlush();

        assertNotNull(serverState.getId());
        assertNotNull(channel1.getId());
        assertNotNull(channel2.getId());

        serverState = StateFactory.getSession().get(ServerStateRevision.class, serverState.getId());
        channel1 = StateFactory.getSession().get(ConfigChannel.class, channel1.getId());
        channel2 = StateFactory.getSession().get(ConfigChannel.class, channel2.getId());

        assertNotNull(serverState);
        assertNotNull(channel1);
        assertNotNull(channel2);
        assertEquals(2, serverState.getConfigChannels().size());
        assertTrue(serverState.getConfigChannels().contains(channel1));
        assertTrue(serverState.getConfigChannels().contains(channel2));
    }

    /**
     * Test removing a state
     * @throws Exception
     */
    @Test
    public void testRemoveAssignedStatesFromServer() throws Exception {
        Server server = ServerFactoryTest.createTestServer(user);
        ServerStateRevision serverState = new ServerStateRevision();
        serverState.setServer(server);
        serverState.setCreator(user);

        ConfigChannel channel1 = ConfigTestUtils.createConfigChannel(user.getOrg(), "Foo", "foo");
        ConfigChannel channel2 = ConfigTestUtils.createConfigChannel(user.getOrg(), "Bar", "bar");

        serverState.getConfigChannels().add(channel1);
        serverState.getConfigChannels().add(channel2);

        StateFactory.save(serverState);
        clearFlush();

        serverState = StateFactory.getSession().get(ServerStateRevision.class, serverState.getId());
        channel1 = StateFactory.getSession().get(ConfigChannel.class, channel1.getId());
        channel2 = StateFactory.getSession().get(ConfigChannel.class, channel2.getId());

        serverState.getConfigChannels().remove(channel1);
        assertEquals(1, serverState.getConfigChannels().size());

        StateFactory.save(serverState);
        clearFlush();

        serverState = StateFactory.getSession().get(ServerStateRevision.class, serverState.getId());
        channel2 = StateFactory.getSession().get(ConfigChannel.class, channel2.getId());

        assertEquals(1, serverState.getConfigChannels().size());
        assertTrue(serverState.getConfigChannels().contains(channel2));
    }

    @Test
    public void testServerGroupConfigChannels() {
        ManagedServerGroup group = ServerGroupFactory.create("testgroup-" +
                TestUtils.randomString(), "desc", user.getOrg());

        ConfigChannel channel1 = ConfigTestUtils.createConfigChannel(user.getOrg(), "Foo", "foo");
        ConfigChannel channel2 = ConfigTestUtils.createConfigChannel(user.getOrg(), "Bar", "bar");

        ServerGroupStateRevision groupRevision = new ServerGroupStateRevision();
        groupRevision.setGroup(group);
        groupRevision.setCreator(user);
        groupRevision.getConfigChannels().add(channel1);
        groupRevision.getConfigChannels().add(channel2);

        StateFactory.save(groupRevision);
        clearFlush();

        groupRevision = StateFactory.getSession().get(ServerGroupStateRevision.class, groupRevision.getId());
        assertEquals(2, groupRevision.getConfigChannels().size());
        assertTrue(groupRevision.getConfigChannels().stream().anyMatch(s -> s.getId().equals(channel1.getId())));
        assertTrue(groupRevision.getConfigChannels().stream().anyMatch(s -> s.getId().equals(channel2.getId())));
    }

    @Test
    public void testOrgConfigChannels() {

        ConfigChannel channel1 = ConfigTestUtils.createConfigChannel(user.getOrg(), "Foo", "foo");
        ConfigChannel channel2 = ConfigTestUtils.createConfigChannel(user.getOrg(), "Bar", "bar");

        OrgStateRevision orgRevision = new OrgStateRevision();
        orgRevision.setOrg(user.getOrg());
        orgRevision.setCreator(user);
        orgRevision.getConfigChannels().add(channel1);
        orgRevision.getConfigChannels().add(channel2);

        StateFactory.save(orgRevision);
        clearFlush();

        orgRevision = StateFactory.getSession().get(OrgStateRevision.class, orgRevision.getId());
        assertEquals(2, orgRevision.getConfigChannels().size());
        assertTrue(orgRevision.getConfigChannels().stream().anyMatch(s -> s.getId().equals(channel1.getId())));
        assertTrue(orgRevision.getConfigChannels().stream().anyMatch(s -> s.getId().equals(channel2.getId())));
    }

    @Test
    public void testLatestServerGroupConfigChannels() throws Exception {
        ManagedServerGroup group = ServerGroupFactory.create("testgroup-" +
                TestUtils.randomString(), "desc", user.getOrg());

        // create revision 1
        ConfigChannel channel = ConfigTestUtils.createConfigChannel(user.getOrg(), "First", "first");

        ServerGroupStateRevision groupRevision = new ServerGroupStateRevision();
        groupRevision.setGroup(group);
        groupRevision.setCreator(user);
        groupRevision.getConfigChannels().add(channel);

        StateFactory.save(groupRevision);
        clearFlush();

        // create revision 2
        channel = ConfigTestUtils.createConfigChannel(user.getOrg(), "Second", "second");

        groupRevision = new ServerGroupStateRevision();
        groupRevision.setGroup(group);
        groupRevision.setCreator(user);
        groupRevision.getConfigChannels().add(channel);

        StateFactory.save(groupRevision);
        clearFlush();

        // Verify: Latest config channels contain only "second"
        Optional<List<ConfigChannel>> states = StateFactory.latestConfigChannels(group);
        assertTrue(states.isPresent());
        assertEquals(1, states.get().size());
        assertTrue(states.get().stream()
                .filter(s -> s.getName().equals("Second"))
                .filter(s -> s.getLabel().equals("second"))
                .findFirst().isPresent());
    }

    @Test
    public void testLatestOrgConfigChannels() {
        // create revision 1
        ConfigChannel channel = ConfigTestUtils.createConfigChannel(user.getOrg(), "First", "first");

        OrgStateRevision orgRevision = new OrgStateRevision();
        orgRevision.setOrg(user.getOrg());
        orgRevision.setCreator(user);
        orgRevision.getConfigChannels().add(channel);

        StateFactory.save(orgRevision);
        clearFlush();

        channel = ConfigTestUtils.createConfigChannel(user.getOrg(), "Second", "second");

        orgRevision = new OrgStateRevision();
        orgRevision.setOrg(user.getOrg());
        orgRevision.setCreator(user);
        orgRevision.getConfigChannels().add(channel);

        StateFactory.save(orgRevision);
        clearFlush();

        // Verify: Latest config channels contain only "second"
        Optional<List<ConfigChannel>> channels = StateFactory.latestConfigChannels(user.getOrg());
        assertTrue(channels.isPresent());
        assertEquals(1, channels.get().size());
        assertTrue(channels.get().stream()
                .filter(s -> s.getName().equals("Second"))
                .filter(s -> s.getLabel().equals("second"))
                .findFirst().isPresent());

    }

    @Test
    public void testLatestConfigChannels() throws Exception {
        // Create revision 1
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);

        ServerStateRevision serverRevision = new ServerStateRevision();
        serverRevision.setServer(server);
        serverRevision.setCreator(user);
        ConfigChannel channel = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Test Channel 1", "test-channel-1");
        serverRevision.getConfigChannels().add(channel);
        StateFactory.save(serverRevision);

        // Create revision 2
        serverRevision = new ServerStateRevision();
        serverRevision.setServer(server);
        serverRevision.setCreator(user);
        channel = ConfigTestUtils.createConfigChannel(user.getOrg(), "Test Channel 2",
                "test-channel-2");
        serverRevision.getConfigChannels().add(channel);
        channel = ConfigTestUtils.createConfigChannel(user.getOrg(), "Test Channel 3",
                "test-channel-3");
        serverRevision.getConfigChannels().add(channel);
        StateFactory.save(serverRevision);

        // Assert
        Optional<List<ConfigChannel>> latestChannels =
                StateFactory.latestConfigChannels(server);

        assertTrue(latestChannels.isPresent());
        assertEquals(2, latestChannels.get().size());
        assertTrue(latestChannels.get().stream()
                .noneMatch(s -> "test-channel-1".equals(s.getLabel())));
        assertTrue(latestChannels.get().stream()
                .anyMatch(s -> "test-channel-2".equals(s.getLabel())));
        assertTrue(latestChannels.get().stream()
                .anyMatch(s -> "test-channel-3".equals(s.getLabel())));
    }

    @Test
    public void testLatestStateRevisionsByConfigChannel() throws Exception {
        ConfigChannel channel1 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Test Channel 1", "test-channel-1");

        ConfigChannel channel2 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Test Channel 2", "test-channel-2");

        // Server usage
        Server srv = ServerFactoryTest.createTestServer(user);
        ServerStateRevision srvRevision = new ServerStateRevision();
        srvRevision.setServer(srv);
        srvRevision.setCreator(user);
        srvRevision.getConfigChannels().add(channel1);

        OrgStateRevision org1Revision = new OrgStateRevision();
        org1Revision.setOrg(user.getOrg());
        org1Revision.setCreator(user);
        org1Revision.getConfigChannels().add(channel2);

        StateFactory.save(org1Revision);
        StateFactory.save(srvRevision);
        clearFlush();

        StateFactory.StateRevisionsUsage usage =
                StateFactory.latestStateRevisionsByConfigChannel(channel1);
        assertEquals(1, usage.getServerStateRevisions().size());
        assertEquals(0, usage.getOrgStateRevisions().size());
        assertEquals(0, usage.getServerGroupStateRevisions().size());
        assertTrue(usage.getServerStateRevisions().stream()
                .anyMatch(r -> srvRevision.getId() == r.getId()));

        // Org usage
        OrgStateRevision org2Revision = new OrgStateRevision();
        org2Revision.setOrg(user.getOrg());
        org2Revision.setCreator(user);
        org2Revision.getConfigChannels().add(channel1);
        org2Revision.getConfigChannels().add(channel2);
        StateFactory.save(org2Revision);
        clearFlush();

        usage = StateFactory.latestStateRevisionsByConfigChannel(channel1);
        assertEquals(1, usage.getServerStateRevisions().size());
        assertEquals(0, usage.getServerGroupStateRevisions().size());
        assertEquals(1, usage.getOrgStateRevisions().size());
        assertTrue(usage.getServerStateRevisions().stream()
                .anyMatch(r -> srvRevision.getId() == r.getId()));
        assertTrue(usage.getOrgStateRevisions().stream()
                .anyMatch(r -> org2Revision.getId() == r.getId()));

        // Server group usage
        ManagedServerGroup grp1 =
                ServerGroupFactory.create("test-group-1", "Test Group 1", user.getOrg());
        ServerGroupStateRevision grp1Revision = new ServerGroupStateRevision();
        grp1Revision.setGroup(grp1);
        grp1Revision.setCreator(user);
        grp1Revision.getConfigChannels().add(channel1);

        ManagedServerGroup grp2 =
                ServerGroupFactory.create("test-group-2", "Test Group 2", user.getOrg());
        ServerGroupStateRevision grp2Revision = new ServerGroupStateRevision();
        grp2Revision.setGroup(grp2);
        grp2Revision.setCreator(user);
        grp2Revision.getConfigChannels().add(channel1);

        StateFactory.save(grp1Revision);
        StateFactory.save(grp2Revision);
        clearFlush();

        usage = StateFactory
                .latestStateRevisionsByConfigChannel(channel1);
        assertEquals(1, usage.getServerStateRevisions().size());
        assertEquals(2, usage.getServerGroupStateRevisions().size());
        assertEquals(1, usage.getOrgStateRevisions().size());
        assertTrue(usage.getServerStateRevisions().stream()
                .anyMatch(r -> srvRevision.getId() == r.getId()));
        assertTrue(usage.getOrgStateRevisions().stream()
                .anyMatch(r -> org2Revision.getId() == r.getId()));
        assertTrue(usage.getServerGroupStateRevisions().stream()
                .anyMatch(r -> grp1Revision.getId() == r.getId()));
        assertTrue(usage.getServerGroupStateRevisions().stream()
                .anyMatch(r -> grp2Revision.getId() == r.getId()));
    }

    private void clearFlush() {
        StateFactory.getSession().flush();
        StateFactory.getSession().clear();
    }

}
