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
import com.redhat.rhn.domain.state.ServerStateRevision;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

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
}
