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
import com.redhat.rhn.domain.state.StateRevision;
import com.redhat.rhn.domain.state.StateFactory;
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

        // Clear all state revisions and package states (temporarily)
        StateFactory.clearStateRevisions();
    }

    /**
     * Save just one single package state and verify the attributes.
     *
     * @throws Exception in case of an error
     */
    public void testSavePackageState() throws Exception {
        // Create a test package
        Package pkg = PackageTest.createTestPackage(user.getOrg());

        // Every package state references a state revision
        StateRevision stateRevision = new StateRevision();
        StateFactory.save(stateRevision);

        // Setup and save a package state
        PackageState packageState = new PackageState();
        packageState.setStateRevision(stateRevision);
        packageState.setName(pkg.getPackageName());
        packageState.setEvr(pkg.getPackageEvr());
        packageState.setArch(pkg.getPackageArch());
        packageState.setPackageState(PackageStates.INSTALLED);
        packageState.setVersionConstraint(VersionConstraints.LATEST);
        StateFactory.save(packageState);

        // Verify the state attributes
        List<PackageState> packageStates = StateFactory.lookupPackageStates();
        assertEquals(1, packageStates.size());
        packageStates.forEach(pkgState -> {
            assertEquals(pkg.getPackageName(), pkgState.getName());
            assertEquals(pkg.getPackageEvr(), pkgState.getEvr());
            assertEquals(pkg.getPackageArch(), pkgState.getArch());
            assertEquals(stateRevision, pkgState.getStateRevision());
            assertEquals(PackageStates.INSTALLED, pkgState.getPackageState());
            assertEquals(VersionConstraints.LATEST, pkgState.getVersionConstraint());
        });
    }

    /**
     * Create a server state revision.
     *
     * @throws Exception in case of an error
     */
    public void testSaveServerStateRevision() throws Exception {
        // Create a test package and server
        Package pkg = PackageTest.createTestPackage(user.getOrg());
        Server server = ServerFactoryTest.createTestServer(user);

        // Create a new server state revision
        ServerStateRevision serverState = new ServerStateRevision();
        serverState.setServer(server);

        // Add a package state and save
        PackageState packageState = new PackageState();
        packageState.setStateRevision(serverState);
        packageState.setName(pkg.getPackageName());
        packageState.setEvr(pkg.getPackageEvr());
        packageState.setArch(pkg.getPackageArch());
        packageState.setPackageState(PackageStates.INSTALLED);
        packageState.setVersionConstraint(VersionConstraints.LATEST);
        serverState.addPackageState(packageState);
        StateFactory.save(serverState);

        // Verify the contents
        List<ServerStateRevision> serverStateRevisions =
                StateFactory.lookupServerStateRevisions(server);
        assertEquals(1, serverStateRevisions.size());
        serverStateRevisions.forEach(stateRevision -> {
            assertEquals(0, stateRevision.getRevision());
            assertEquals(server, stateRevision.getServer());
            assertEquals(1, stateRevision.getPackageStates().size());
            assertEquals(packageState, stateRevision.getPackageStates().iterator().next());
        });
    }
}
