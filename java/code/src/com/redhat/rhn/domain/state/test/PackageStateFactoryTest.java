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
import com.redhat.rhn.domain.rhnpackage.PackageNevra;
import com.redhat.rhn.domain.rhnpackage.PackageNevraFactory;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.state.PackageState;
import com.redhat.rhn.domain.state.PackageStates;
import com.redhat.rhn.domain.state.PackageStateFactory;
import com.redhat.rhn.domain.state.VersionConstraints;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

/**
 * Tests for {@link PackageStateFactory}.
 */
public class PackageStateFactoryTest extends BaseTestCaseWithUser {

    /**
     * Save a single package state and verify the attributes.
     * @throws Exception in case of an error
     */
    public void testSavePackageState() throws Exception {
        // Save a test package + NEVRA
        Package pkg = PackageTest.createTestPackage(user.getOrg());
        PackageNevra nevra = new PackageNevra();
        nevra.setArch(pkg.getPackageArch());
        nevra.setEvr(pkg.getPackageEvr());
        nevra.setName(pkg.getPackageName());
        PackageNevraFactory.savePackageNevra(nevra);

        // Setup and save a package state
        PackageState packageState = new PackageState();
        packageState.setStateId(1L);
        packageState.setName(pkg.getPackageName());
        packageState.setEvr(pkg.getPackageEvr());
        packageState.setArch(pkg.getPackageArch());
        packageState.setPackageState(PackageStates.INSTALLED);
        packageState.setVersionConstraint(VersionConstraints.LATEST);
        PackageStateFactory.savePackageState(packageState);

        // Verify the attributes
        PackageStateFactory.lookupPackageStates(1L).forEach(state -> {
            assertEquals(pkg.getPackageName(), state.getName());
            assertEquals(pkg.getPackageArch(), state.getArch());
            assertEquals(pkg.getPackageEvr(), state.getEvr());
            assertEquals(PackageStates.INSTALLED, state.getPackageState());
            assertEquals(VersionConstraints.LATEST, state.getVersionConstraint());
        });
    }
}
