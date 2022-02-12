/*
 * Copyright (c) 2020 SUSE LLC
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

package com.redhat.rhn.testing;

import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactory;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.Server;

import java.util.List;

/**
 * Utils functions useful for testing packages
 */
public class PackageTestUtils {

    private PackageTestUtils() { }

    /**
     * Create 3 subsequent versions (1.0.0, 2.0.0, 3.0.0) of a package with the same name, epoch, release and arch
     *
     * @param org the org
     * @return lists of the packages, sorted by version
     * @throws Exception if anything goes wrong
     */
    public static List<Package> createSubsequentPackages(Org org) throws Exception {
        Package pkg1 = PackageTest.createTestPackage(org);
        PackageEvr evr = pkg1.getPackageEvr();
        evr.setVersion("1.0.0");

        Package pkg2 = PackageTest.createTestPackage(org);
        pkg2.setPackageName(pkg1.getPackageName());
        pkg2.setPackageEvr(PackageEvrFactory.lookupOrCreatePackageEvr(
                evr.getEpoch(), "2.0.0", evr.getRelease(), pkg1.getPackageType()));

        Package pkg3 = PackageTest.createTestPackage(org);
        pkg3.setPackageName(pkg1.getPackageName());
        pkg3.setPackageEvr(PackageEvrFactory.lookupOrCreatePackageEvr(
                evr.getEpoch(), "3.0.0", evr.getRelease(), pkg1.getPackageType()));

        return List.of(pkg1, pkg2, pkg3);
    }

    /**
     * Install given package to the server
     *
     * @param pkg the package
     * @param server the server
     */
    public static void installPackageOnServer(Package pkg, Server server) {
        InstalledPackage installedNewerPkg = createInstalledPackage(pkg);
        installedNewerPkg.setServer(server);
        server.getPackages().add(installedNewerPkg);
    }

    /**
     * Creates an InstalledPackage instance based on Package
     * @param pkg the Package
     * @return the InstalledPackage
     */
    public static InstalledPackage createInstalledPackage(Package pkg) {
        InstalledPackage installedNewerPkg = new InstalledPackage();
        installedNewerPkg.setEvr(pkg.getPackageEvr());
        installedNewerPkg.setArch(pkg.getPackageArch());
        installedNewerPkg.setName(pkg.getPackageName());
        return installedNewerPkg;
    }
}
