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
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.state.PackageState;
import com.redhat.rhn.domain.state.PackageStates;
import com.redhat.rhn.domain.state.SaltState;
import com.redhat.rhn.domain.state.ServerStateRevision;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import org.apache.commons.lang.StringUtils;

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

        SaltState state1 = new SaltState();
        state1.setOrg(user.getOrg());
        state1.setStateName("foo");

        SaltState state2 = new SaltState();
        state2.setOrg(user.getOrg());
        state2.setStateName("bar");

        serverState.getAssignedStates().add(state1);
        serverState.getAssignedStates().add(state2);

        StateFactory.save(serverState);
        clearFlush();

        assertNotNull(serverState.getId());
        assertNotNull(state1.getId());
        assertNotNull(state2.getId());

        serverState = (ServerStateRevision) StateFactory.getSession().get(ServerStateRevision.class, serverState.getId());
        state1 = (SaltState)StateFactory.getSession().get(SaltState.class, state1.getId());
        state2 = (SaltState)StateFactory.getSession().get(SaltState.class, state2.getId());

        assertNotNull(serverState);
        assertNotNull(state1);
        assertNotNull(state2);
        assertEquals(2, serverState.getAssignedStates().size());
        assertTrue(serverState.getAssignedStates().contains(state1));
        assertTrue(serverState.getAssignedStates().contains(state2));
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

        SaltState state1 = new SaltState();
        state1.setOrg(user.getOrg());
        state1.setStateName("foo");

        SaltState state2 = new SaltState();
        state2.setOrg(user.getOrg());
        state2.setStateName("bar");

        serverState.getAssignedStates().add(state1);
        serverState.getAssignedStates().add(state2);

        StateFactory.save(serverState);
        clearFlush();

        serverState = (ServerStateRevision) StateFactory.getSession().get(ServerStateRevision.class, serverState.getId());
        state1 = (SaltState)StateFactory.getSession().get(SaltState.class, state1.getId());
        state2 = (SaltState)StateFactory.getSession().get(SaltState.class, state2.getId());

        serverState.getAssignedStates().remove(state1);
        assertEquals(1, serverState.getAssignedStates().size());

        StateFactory.save(serverState);
        clearFlush();

        serverState = (ServerStateRevision) StateFactory.getSession().get(ServerStateRevision.class, serverState.getId());
        state2 = (SaltState)StateFactory.getSession().get(SaltState.class, state2.getId());

        assertEquals(1, serverState.getAssignedStates().size());
        assertTrue(serverState.getAssignedStates().contains(state2));
    }

    private void clearFlush() {
        StateFactory.getSession().flush();
        StateFactory.getSession().clear();
    }


}
