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

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageCapability;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactory;
import com.redhat.rhn.domain.rhnpackage.PackageProvides;
import com.redhat.rhn.domain.rhnpackage.PackageRequires;
import com.redhat.rhn.domain.rhnpackage.PackageType;
import com.redhat.rhn.domain.rhnpackage.SpecialCapabilityNames;
import com.redhat.rhn.domain.rhnpackage.test.PackageNameTest;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;

import org.hibernate.Session;

import java.util.Collection;
import java.util.Date;
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
     */
    public static List<Package> createSubsequentPackages(Org org) {
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
     * Install given packages to the server
     *
     * @param pkgs the collection of packages
     * @param server the server
     */
    public static void installPackagesOnServer(Collection<Package> pkgs, Server server) {
        pkgs.forEach(p -> installPackageOnServer(p, server));
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

    /**
     * Create a package for Zypper with the specified version
     * @param version the version of zypper to create
     * @param user the user owning  the package
     * @return the zypper package
     */
    public static Package createZypperPackage(String version, User user) {
        Package zypperPackage = PackageTest.createTestPackage(user.getOrg(), "zypper");
        zypperPackage.setPackageEvr(PackageEvrFactory.lookupOrCreatePackageEvr(null, version, "0", PackageType.RPM));
        return  TestUtils.saveAndReload(zypperPackage);
    }

    /**
     * Create a new version of the same package.
     * @param original the original package
     * @param epoch the new epoch. If null, the epoch will remain the same
     * @param version the new version. If null, the version will remain the same
     * @param release the new release. If null, the release will remain the same
     * @param org the organization owning the package
     * @return a new version of the given package
     */
    public static Package newVersionOfPackage(Package original, String epoch, String version, String release, Org org) {
        PackageEvr evr = original.getPackageEvr();

        if (epoch == null && version == null && release == null) {
            throw new IllegalArgumentException("epoch, version and release cannot be all null");
        }

        Package newerPackage = PackageTest.createTestPackage(org);
        newerPackage.setPackageName(original.getPackageName());
        newerPackage.setPackageEvr(PackageEvrFactory.lookupOrCreatePackageEvr(
            epoch != null ? epoch : evr.getEpoch(),
            version != null ? version : evr.getVersion(),
            release != null ? release : evr.getRelease(),
            original.getPackageType()
        ));

        return TestUtils.saveAndReload(newerPackage);
    }

    /**
     * Create a ptf master package
     * @param ptfNumber the ptf number
     * @param ptfVersion the version
     * @param org the organization owning the package
     * @return a ptf master package
     */
    public static Package createPtfMaster(String ptfNumber, String ptfVersion, Org org) {
        Package master = PackageTest.createTestPackage(org);

        master.setPackageName(PackageNameTest.createTestPackageName("ptf-" + ptfNumber));
        master.setDescription("Master PTF package of ptf-" + ptfNumber + " for RHN-JAVA unit tests. Please disregard.");
        master.setPackageEvr(PackageEvrFactory.lookupOrCreatePackageEvr(null, ptfVersion, "0", PackageType.RPM));
        master.setIsPtfPackage(true);

        addProvidesHeader(master, findOrCreateCapability(SpecialCapabilityNames.PTF, ptfNumber + "-" + ptfVersion), 8L);
        addProvidesHeader(master, findOrCreateCapability("ptf-" + ptfNumber, ptfVersion + "-0"), 8L);

        return TestUtils.saveAndReload(master);
    }

    /**
     * Create a package part of a ptf
     * @param ptfNumber the ptf package
     * @param ptfVersion the ptf version
     * @param org the organization owning the package
     * @return a package part of a ptf
     */
    public static Package createPtfPackage(String ptfNumber, String ptfVersion, Org org) {
        Package ptfPackage = PackageTest.createTestPackage(org);

        ptfPackage.setDescription("Package part of ptf-" + ptfNumber + " for RHN-JAVA unit tests. Please disregard.");
        ptfPackage.setPackageEvr(PackageEvrFactory.lookupOrCreatePackageEvr(null, "1",
            "0" + "." + ptfNumber + "." + ptfVersion + ".PTF", PackageType.RPM));
        ptfPackage.setIsPartOfPtfPackage(true);

        addProvidesHeader(ptfPackage, findOrCreateCapability(SpecialCapabilityNames.PTF_PACKAGE), 0L);
        addProvidesHeader(ptfPackage, findOrCreateCapability(ptfPackage.getPackageName().getName(),
            ptfPackage.getPackageEvr().toUniversalEvrString()), 8L);

        return TestUtils.saveAndReload(ptfPackage);
    }

    /**
     * Create a package part of a ptf fixing an existing package
     * @param original the original this ptf package fixes
     * @param ptfNumber the ptf package
     * @param ptfVersion the ptf version
     * @param org the organization owning the package
     * @return a package part of a ptf
     */
    public static Package createPtfPackage(Package original, String ptfNumber, String ptfVersion, Org org) {
        Package ptfPackage = PackageTest.createTestPackage(org);

        ptfPackage.setPackageName(original.getPackageName());
        ptfPackage.setDescription("Package part of ptf-" + ptfNumber + " for RHN-JAVA unit tests. Please disregard.");
        PackageEvr evr = original.getPackageEvr();
        ptfPackage.setPackageEvr(PackageEvrFactory.lookupOrCreatePackageEvr(evr.getEpoch(), evr.getVersion(),
            evr.getRelease() + "." + ptfNumber + "." + ptfVersion + ".PTF", original.getPackageType()));
        ptfPackage.setIsPartOfPtfPackage(true);

        addProvidesHeader(ptfPackage, findOrCreateCapability(SpecialCapabilityNames.PTF_PACKAGE), 0L);
        addProvidesHeader(ptfPackage, findOrCreateCapability(ptfPackage.getPackageName().getName(),
            ptfPackage.getPackageEvr().toUniversalEvrString()), 8L);

        return TestUtils.saveAndReload(ptfPackage);
    }

    /**
     * Adds a link between a ptf package and its master ptf
     *
     * @param masterPtfPackage the master ptf
     * @param ptfPackage the package part of the ptf
     */
    public static void associatePackageToPtf(Package masterPtfPackage, Package ptfPackage) {
        addRequiresHeader(masterPtfPackage, findOrCreateCapability(ptfPackage.getPackageName().getName(),
            ptfPackage.getPackageEvr().toUniversalEvrString()), 8L);
        addRequiresHeader(ptfPackage, findOrCreateCapability(masterPtfPackage.getPackageName().getName(),
            masterPtfPackage.getPackageEvr().getVersion() + "-0"), 8L);

        TestUtils.saveAndFlush(masterPtfPackage);
        TestUtils.saveAndFlush(ptfPackage);
    }

    /**
     * Adds the specified capability to the list of requirements of the package
     * @param pack the package
     * @param capability the capability
     * @param sense the sense
     */
    public static void addRequiresHeader(Package pack, PackageCapability capability, Long sense) {
        PackageRequires packageProvides = new PackageRequires();
        packageProvides.setCapability(capability);
        packageProvides.setPack(pack);
        packageProvides.setSense(sense);
        TestUtils.saveAndFlush(packageProvides);
    }

    /**
     * Adds the specified capability to the list provided by a package
     * @param pack the package
     * @param capability the capability
     * @param sense the sense
     */
    public static void addProvidesHeader(Package pack, PackageCapability capability, Long sense) {
        PackageProvides packageProvides = new PackageProvides();
        packageProvides.setCapability(capability);
        packageProvides.setPack(pack);
        packageProvides.setSense(sense);
        TestUtils.saveAndFlush(packageProvides);
    }

    /**
     * Extracts or create a capability with the given name.
     * @param capabilityName the name
     * @return the PackageCapability object
     */
    public static PackageCapability findOrCreateCapability(String capabilityName) {
        return findOrCreateCapability(capabilityName, null);
    }

    /**
     * Extracts or create a capability with the given name and version.
     * @param capabilityName the name
     * @param version the version
     * @return the PackageCapability object
     */
    public static PackageCapability findOrCreateCapability(String capabilityName, String version) {
        Session session = HibernateFactory.getSession();
        return session.createQuery("SELECT pc FROM PackageCapability pc WHERE pc.name = :name", PackageCapability.class)
                      .setParameter("name", capabilityName)
                      .uniqueResultOptional()
                      .orElseGet(() -> {
                          PackageCapability pc = new PackageCapability();
                          pc.setName(capabilityName);
                          pc.setVersion(version);
                          pc.setCreated(new Date());
                          pc.setModified(new Date());

                          TestUtils.saveAndFlush(pc);
                          return pc;
                      });
    }
}
