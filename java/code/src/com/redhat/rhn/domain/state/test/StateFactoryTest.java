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
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.domain.state.PackageStates;
import com.redhat.rhn.domain.state.ServerState;
import com.redhat.rhn.domain.state.VersionConstraints;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import java.util.List;

/**
 * Tests for {@link StateFactory}.
 */
public class StateFactoryTest extends BaseTestCaseWithUser {

    @Override
    public void setUp() throws Exception {
        super.setUp();

        // Clear all package states that might be there already (temporarily)
        StateFactory.clearPackageStates();
    }

    /**
     * Save just one single package state and verify the attributes.
     *
     * @throws Exception in case of an error
     */
    public void testSavePackageState() throws Exception {
        // Create a test package
        Package pkg = PackageTest.createTestPackage(user.getOrg());

        // Setup and save a state for this package
        PackageState packageState = new PackageState();
        packageState.setGroupId(1L);
        packageState.setName(pkg.getPackageName());
        packageState.setEvr(pkg.getPackageEvr());
        packageState.setArch(pkg.getPackageArch());
        packageState.setPackageState(PackageStates.INSTALLED);
        packageState.setVersionConstraint(VersionConstraints.LATEST);
        StateFactory.savePackageState(packageState);

        // Verify the state attributes
        List<PackageState> packageStates = StateFactory.lookupPackageStates(1L);
        assertEquals(1, packageStates.size());
        packageStates.forEach(state -> {
            assertEquals(pkg.getPackageName(), state.getName());
            assertEquals(pkg.getPackageEvr(), state.getEvr());
            assertEquals(pkg.getPackageArch(), state.getArch());
            assertEquals(PackageStates.INSTALLED, state.getPackageState());
            assertEquals(VersionConstraints.LATEST, state.getVersionConstraint());
        });
    }

    /**
     * Assign a group of package states to a server.
     *
     * @throws Exception in case of an error
     */
    public void testSaveServerState() throws Exception {
        // Create a test package and a server
        Package pkg = PackageTest.createTestPackage(user.getOrg());
        Server server = ServerFactoryTest.createTestServer(user);

        // Setup and save a package state
        PackageState packageState = new PackageState();
        packageState.setGroupId(1L);
        packageState.setName(pkg.getPackageName());
        packageState.setEvr(pkg.getPackageEvr());
        packageState.setArch(pkg.getPackageArch());
        packageState.setPackageState(PackageStates.INSTALLED);
        packageState.setVersionConstraint(VersionConstraints.LATEST);
        StateFactory.savePackageState(packageState);

        // Create server state and assign the package state
        ServerState serverState = new ServerState();
        serverState.setServer(server);
        serverState.setPackageStateGroupId(packageState.getGroupId());
        StateFactory.save(serverState);

        // Verify the server state
        List<ServerState> serverStates = StateFactory.lookupServerStates(server);
        assertEquals(1, serverStates.size());
        serverStates.forEach(state -> {
            assertEquals(0, state.getRevision());
            assertEquals(server, state.getServer());
            assertEquals(packageState.getGroupId(), state.getPackageStateGroupId());
        });
    }
}
