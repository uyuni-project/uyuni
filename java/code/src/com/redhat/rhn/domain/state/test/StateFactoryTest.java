/**
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

import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.state.OrgStateRevision;
import com.redhat.rhn.domain.state.PackageState;
import com.redhat.rhn.domain.state.PackageStates;
import com.redhat.rhn.domain.state.CustomState;
import com.redhat.rhn.domain.state.ServerGroupStateRevision;
import com.redhat.rhn.domain.state.ServerStateRevision;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

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
    public void testLatestPackageStatesEmpty() throws Exception {
        Server server = ServerFactoryTest.createTestServer(user);
        assertFalse(StateFactory.latestPackageStates(server).isPresent());
    }

    /**
     * Get the latest package states for a server.
     *
     * @throws Exception in case of an error
     */
    public void testLatestPackageStates() throws Exception {
        // Create test packages and a server
        Package pkg1 = PackageTest.createTestPackage(user.getOrg());
        Package pkg2 = PackageTest.createTestPackage(user.getOrg());
        Server server = ServerFactoryTest.createTestServer(user);

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
    public void testAssignStates() throws Exception {
        Server server = ServerFactoryTest.createTestServer(user);
        ServerStateRevision serverState = new ServerStateRevision();
        serverState.setServer(server);
        serverState.setCreator(user);

        CustomState state1 = new CustomState();
        state1.setOrg(user.getOrg());
        state1.setStateName("foo");

        CustomState state2 = new CustomState();
        state2.setOrg(user.getOrg());
        state2.setStateName("bar");

        serverState.getCustomStates().add(state1);
        serverState.getCustomStates().add(state2);

        StateFactory.save(serverState);
        clearFlush();

        assertNotNull(serverState.getId());
        assertNotNull(state1.getId());
        assertNotNull(state2.getId());

        serverState = (ServerStateRevision) StateFactory.getSession().get(ServerStateRevision.class, serverState.getId());
        state1 = (CustomState)StateFactory.getSession().get(CustomState.class, state1.getId());
        state2 = (CustomState)StateFactory.getSession().get(CustomState.class, state2.getId());

        assertNotNull(serverState);
        assertNotNull(state1);
        assertNotNull(state2);
        assertEquals(2, serverState.getCustomStates().size());
        assertTrue(serverState.getCustomStates().contains(state1));
        assertTrue(serverState.getCustomStates().contains(state2));
    }

    /**
     * Test removing a state
     * @throws Exception
     */
    public void testRemoveAssignedStates() throws Exception {
        Server server = ServerFactoryTest.createTestServer(user);
        ServerStateRevision serverState = new ServerStateRevision();
        serverState.setServer(server);
        serverState.setCreator(user);

        CustomState state1 = new CustomState();
        state1.setOrg(user.getOrg());
        state1.setStateName("foo");

        CustomState state2 = new CustomState();
        state2.setOrg(user.getOrg());
        state2.setStateName("bar");

        serverState.getCustomStates().add(state1);
        serverState.getCustomStates().add(state2);

        StateFactory.save(serverState);
        clearFlush();

        serverState = (ServerStateRevision) StateFactory.getSession().get(ServerStateRevision.class, serverState.getId());
        state1 = (CustomState)StateFactory.getSession().get(CustomState.class, state1.getId());
        state2 = (CustomState)StateFactory.getSession().get(CustomState.class, state2.getId());

        serverState.getCustomStates().remove(state1);
        assertEquals(1, serverState.getCustomStates().size());

        StateFactory.save(serverState);
        clearFlush();

        serverState = (ServerStateRevision) StateFactory.getSession().get(ServerStateRevision.class, serverState.getId());
        state2 = (CustomState)StateFactory.getSession().get(CustomState.class, state2.getId());

        assertEquals(1, serverState.getCustomStates().size());
        assertTrue(serverState.getCustomStates().contains(state2));
    }

    public void testServerGroupCustomStates() {
        ManagedServerGroup group = ServerGroupFactory.create("testgroup-" +
                TestUtils.randomString(), "desc", user.getOrg());

        CustomState state1 = new CustomState();
        state1.setOrg(user.getOrg());
        state1.setStateName("foo");

        CustomState state2 = new CustomState();
        state2.setOrg(user.getOrg());
        state2.setStateName("bar");

        ServerGroupStateRevision groupRevision = new ServerGroupStateRevision();
        groupRevision.setGroup(group);
        groupRevision.setCreator(user);
        groupRevision.getCustomStates().add(state1);
        groupRevision.getCustomStates().add(state2);

        StateFactory.save(groupRevision);
        clearFlush();

        groupRevision = (ServerGroupStateRevision) StateFactory.getSession().get(ServerGroupStateRevision.class,
                groupRevision.getId());
        assertEquals(2, groupRevision.getCustomStates().size());
        assertTrue(groupRevision.getCustomStates().stream()
                .filter( s -> s.getId().equals(state1.getId())).findFirst().isPresent());
        assertTrue(groupRevision.getCustomStates().stream()
                .filter( s -> s.getId().equals(state2.getId())).findFirst().isPresent());
    }

    public void testOrgCustomStates() {

        CustomState state1 = new CustomState();
        state1.setOrg(user.getOrg());
        state1.setStateName("foo");

        CustomState state2 = new CustomState();
        state2.setOrg(user.getOrg());
        state2.setStateName("bar");

        OrgStateRevision orgRevision = new OrgStateRevision();
        orgRevision.setOrg(user.getOrg());
        orgRevision.setCreator(user);
        orgRevision.getCustomStates().add(state1);
        orgRevision.getCustomStates().add(state2);

        StateFactory.save(orgRevision);
        clearFlush();

        orgRevision = (OrgStateRevision) StateFactory.getSession().get(OrgStateRevision.class,
                orgRevision.getId());
        assertEquals(2, orgRevision.getCustomStates().size());
        assertTrue(orgRevision.getCustomStates().stream()
                .filter( s -> s.getId().equals(state1.getId())).findFirst().isPresent());
        assertTrue(orgRevision.getCustomStates().stream()
                .filter( s -> s.getId().equals(state2.getId())).findFirst().isPresent());
    }

    public void testLatestServerGroupCustomStates() throws Exception {
        ManagedServerGroup group = ServerGroupFactory.create("testgroup-" +
                TestUtils.randomString(), "desc", user.getOrg());

        // create revision 1
        CustomState state = new CustomState();
        state.setOrg(user.getOrg());
        state.setStateName("first");

        ServerGroupStateRevision groupRevision = new ServerGroupStateRevision();
        groupRevision.setGroup(group);
        groupRevision.setCreator(user);
        groupRevision.getCustomStates().add(state);

        StateFactory.save(groupRevision);

        clearFlush();

        // create revision 2
        state = new CustomState();
        state.setOrg(user.getOrg());
        state.setStateName("second");

        groupRevision = new ServerGroupStateRevision();
        groupRevision.setGroup(group);
        groupRevision.setCreator(user);
        groupRevision.getCustomStates().add(state);

        StateFactory.save(groupRevision);

        clearFlush();

        // Verify: Latest custom states contain only "bar"
        Optional<Set<CustomState>> states = StateFactory.latestCustomStates(user.getOrg());
        assertTrue(states.isPresent());
        assertEquals(1, states.get().size());
        assertTrue(states.get().stream()
                .filter(s -> s.getStateName().equals("second"))
                .findFirst().isPresent());

    }


    private void clearFlush() {
        StateFactory.getSession().flush();
        StateFactory.getSession().clear();
    }


}
