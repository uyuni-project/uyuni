/**
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

package com.redhat.rhn.manager.rhnpackage;

import static com.redhat.rhn.manager.channel.CloneChannelCommand.CloneBehavior.CURRENT_STATE;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.errata.AdvisoryStatus;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactory;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.frontend.dto.PackageOverview;
import com.redhat.rhn.manager.channel.CloneChannelCommand;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ChannelTestUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Test various logic related to the retracted packages
 */
public class PackageManagerRetractedTest extends BaseTestCaseWithUser {

    private Server server;
    private Package oldPkg;
    private Package newerPkg;
    private Package newestPkg;
    private Channel channel;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        server = ServerFactoryTest.createTestServer(user);

        List<Package> generatedPackages = createSubsequentPackages();
        oldPkg = generatedPackages.get(0);
        newerPkg = generatedPackages.get(1);
        newestPkg = generatedPackages.get(2);

        channel = ChannelTestUtils.createBaseChannel(user);
        channel.setChecksumType(ChannelFactory.findChecksumTypeByLabel("sha256"));
    }

    /**
     * Scenario:
     * - channel with retracted patch and a package
     * - its clone with non-retracted patch and the same package
     *
     * If the system has the package installed and is subscribed to the original,
     * the package list should show the package as "retracted".
     *
     * If the system is subscribed to the cloned channel, the package shouldn't show up as "retracted".
     *
     * @throws Exception
     */
    public void testSystemPackageList() throws Exception {
        // create a null-org erratum with a package and add it to the channel
        Errata vendorErratum = ErrataFactoryTest.createTestErrata(null);
        vendorErratum.addPackage(oldPkg);
        vendorErratum.addChannel(channel);
        ErrataFactory.save(vendorErratum);

        channel.addPackage(oldPkg);

        // clone the channel
        CloneChannelCommand ccc = new CloneChannelCommand(CURRENT_STATE, channel);
        ccc.setUser(user);
        Channel clonedChannel = ccc.create();

        // set the erratum in original to retracted
        vendorErratum.setAdvisoryStatus(AdvisoryStatus.RETRACTED);
        ErrataFactory.save(vendorErratum);

        // install the package & subcribe the system to the original cnl (with retracted errata)
        installPackageOnServer(oldPkg, server);
        SystemManager.subscribeServerToChannel(user, server, channel);

        // verify the package is retracted
        DataResult<PackageListItem> pkgs = PackageManager.systemPackageList(server.getId(), null);
        pkgs.elaborate();
        PackageListItem pkg = assertSingleAndGet(pkgs);
        assertTrue(pkg.isRetracted());

        // now let's subscribe the system to the cloned channel, where the erratum is not retracted
        SystemManager.unsubscribeServerFromChannel(server, channel);
        SystemManager.subscribeServerToChannel(user, server, clonedChannel);
        pkgs = PackageManager.systemPackageList(server.getId(), null);
        pkgs.elaborate();
        pkg = assertSingleAndGet(pkgs);
        assertFalse(pkg.isRetracted()); // the package is now NOT retracted for this server!
    }

    public void testSystemAvailablePackages() throws Exception {
        // create a null-org erratum with a newest package and add it to the channel
        Errata vendorErratum = ErrataFactoryTest.createTestErrata(null);
        vendorErratum.addPackage(newestPkg);
        vendorErratum.addChannel(channel);
        ErrataFactory.save(vendorErratum);

        // channel has all 3 packages
        channel.getPackages().addAll(List.of(oldPkg, newerPkg, newestPkg));

        // clone the channel
        CloneChannelCommand ccc = new CloneChannelCommand(CURRENT_STATE, channel);
        ccc.setUser(user);
        Channel clonedChannel = ccc.create();

        // set the erratum in original to retracted
        vendorErratum.setAdvisoryStatus(AdvisoryStatus.RETRACTED);
        ErrataFactory.save(vendorErratum);

        // subscribe the system to the channel with the retracted patch
        // the newest installable package should be the "newerPkg", since the "newestPkg" is retracted
        SystemManager.subscribeServerToChannel(user, server, channel);
        PackageListItem pkg = assertSingleAndGet(PackageManager.systemAvailablePackages(server.getId(), null));
        assertEquals(newerPkg.getId(),pkg.getPackageId());

        // now subscribe to the clone, where the patch is not retracted
        // the newest package should be "newestPkg" now
        SystemManager.unsubscribeServerFromChannel(server, channel);
        SystemManager.subscribeServerToChannel(user, server, clonedChannel);
        pkg = assertSingleAndGet(PackageManager.systemAvailablePackages(server.getId(), null));
        assertEquals(newestPkg.getId(),pkg.getPackageId());
    }

    public void testListPackagesInChannelForList() throws Exception {
        Errata vendorErratum = ErrataFactoryTest.createTestErrata(null);
        vendorErratum.addPackage(newerPkg);
        vendorErratum.addChannel(channel);
        ErrataFactory.save(vendorErratum);

        // channel has all 3 packages
        channel.getPackages().addAll(List.of(oldPkg, newerPkg, newestPkg));

        // clone the channel
        CloneChannelCommand ccc = new CloneChannelCommand(CURRENT_STATE, channel);
        ccc.setUser(user);
        Channel clonedChannel = ccc.create();

        // set the erratum in original to retracted
        vendorErratum.setAdvisoryStatus(AdvisoryStatus.RETRACTED);
        ErrataFactory.save(vendorErratum);

        // only the "newerPkg" is retracted in the original channel
        DataResult<PackageOverview> pkgsOriginal = PackageManager.listPackagesInChannelForList(channel.getId());
        Map<Long, PackageOverview> pkgsOriginalMap = pkgsOriginal.stream().collect(Collectors.toMap(p -> p.getId(), p -> p));
        assertFalse(pkgsOriginalMap.get(oldPkg.getId()).isRetracted());
        assertTrue(pkgsOriginalMap.get(newerPkg.getId()).isRetracted());
        assertFalse(pkgsOriginalMap.get(newestPkg.getId()).isRetracted());

        // no package retracted in the cloned channel
        pkgsOriginal = PackageManager.listPackagesInChannelForList(clonedChannel.getId());
        pkgsOriginalMap = pkgsOriginal.stream().collect(Collectors.toMap(p -> p.getId(), p -> p));
        assertFalse(pkgsOriginalMap.get(oldPkg.getId()).isRetracted());
        assertFalse(pkgsOriginalMap.get(newerPkg.getId()).isRetracted());
        assertFalse(pkgsOriginalMap.get(newestPkg.getId()).isRetracted());
    }

    private <T> T assertSingleAndGet(Collection<T> items) {
        assertEquals(1, items.size());
        return items.iterator().next();
    }

    // todo move to utils
    private List<Package> createSubsequentPackages() throws Exception {
        Package pkg1 = PackageTest.createTestPackage(user.getOrg());
        PackageEvr evr = pkg1.getPackageEvr();
        evr.setVersion("1.0.0");

        Package pkg2 = PackageTest.createTestPackage(user.getOrg());
        pkg2.setPackageName(pkg1.getPackageName());
        pkg2.setPackageEvr(PackageEvrFactory.lookupOrCreatePackageEvr(evr.getEpoch(), "2.0.0", evr.getRelease(), pkg1.getPackageType()));

        Package pkg3 = PackageTest.createTestPackage(user.getOrg());
        pkg3.setPackageName(pkg1.getPackageName());
        pkg3.setPackageEvr(PackageEvrFactory.lookupOrCreatePackageEvr(evr.getEpoch(), "3.0.0", evr.getRelease(), pkg1.getPackageType()));

        return List.of(pkg1, pkg2, pkg3);
    }

    // todo extract
    private static void installPackageOnServer(Package pkg, Server server) {
        InstalledPackage installedNewerPkg = createInstalledPackage(pkg);
        installedNewerPkg.setServer(server);
        server.getPackages().add(installedNewerPkg);
    }

    // todo extract
    private static InstalledPackage createInstalledPackage(Package pkg) {
        InstalledPackage installedNewerPkg = new InstalledPackage();
        installedNewerPkg.setEvr(pkg.getPackageEvr());
        installedNewerPkg.setArch(pkg.getPackageArch());
        installedNewerPkg.setName(pkg.getPackageName());
        return installedNewerPkg;
    }
}
