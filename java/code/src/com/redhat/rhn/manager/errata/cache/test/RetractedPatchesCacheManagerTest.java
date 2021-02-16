/**
 * Copyright (c) 2021 SUSE LLC
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
package com.redhat.rhn.manager.errata.cache.test;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.errata.AdvisoryStatus;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactory;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.frontend.dto.ErrataCacheDto;
import com.redhat.rhn.manager.errata.cache.ErrataCacheManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ChannelTestUtils;

import java.util.List;

/**
 * ErrataFactoryTest
 */
public class RetractedPatchesCacheManagerTest extends BaseTestCaseWithUser {

    private Server server;
    private Channel subscribedChannel;

    // 3 ascending versions of the same package
    private Package oldPkg;
    private Package newerPkg;
    private Package newestPkg;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        server = ServerFactoryTest.createTestServer(user);

        List<Package> generatedPackages = createSubsequentPackages();
        oldPkg = generatedPackages.get(0);
        newerPkg = generatedPackages.get(1);
        newestPkg = generatedPackages.get(2);

        subscribedChannel = ChannelTestUtils.createBaseChannel(user);
        SystemManager.subscribeServerToChannel(user, server, subscribedChannel);
    }

    /**
     * Test that inserting a retracted package with higher version to the cache has no effect.
     *
     * @throws Exception if anything goes wrong
     */
    public void testRetractedPatchesInCache() throws Exception {
        // channel has all packages
        subscribedChannel.getPackages().addAll(List.of(oldPkg, newerPkg, newestPkg));

        // oldest is installed on the server
        installPackageOnServer(oldPkg, server);

        // newest is retracted
        Errata retracted = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        retracted.setAdvisoryStatus(AdvisoryStatus.RETRACTED);
        retracted.addPackage(newestPkg);
        subscribedChannel.addErrata(retracted);

        // insert newer & newest
        ErrataCacheManager.insertCacheForChannelPackages(subscribedChannel.getId(), retracted.getId(), List.of(newestPkg.getId()));
        ErrataCacheManager.insertCacheForChannelPackages(subscribedChannel.getId(), null, List.of(newerPkg.getId()));

        // only the newer should be in the cache since newest is retracted
        DataResult needingUpdates = ErrataCacheManager.packagesNeedingUpdates(server.getId());
        assertEquals(1, needingUpdates.size());
        assertEquals(newerPkg.getId(), ((ErrataCacheDto) needingUpdates.get(0)).getPackageId());

        // just to be sure, recompute the cache and verify that the results are same
        ServerFactory.updateServerNeededCache(server.getId());
        needingUpdates = ErrataCacheManager.packagesNeedingUpdates(server.getId());
        assertEquals(1, needingUpdates.size());
        assertEquals(newerPkg.getId(), ((ErrataCacheDto) needingUpdates.get(0)).getPackageId());
    }

    /**
     * Similar as testRetractedPatchesInCache, but here we also attempt to insert the retracted errata id to the cache.
     * The result should be the same: no new row in the cache.
     *
     * @throws Exception if anything goes wrong
     */
    public void testRetractedPackagesInCache() throws Exception {
        // channel has all packages
        subscribedChannel.getPackages().addAll(List.of(oldPkg, newerPkg, newestPkg));

        // oldest is installed on the server
        installPackageOnServer(oldPkg, server);

        // newest is retracted
        Errata retracted = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        retracted.setAdvisoryStatus(AdvisoryStatus.RETRACTED);
        retracted.addPackage(newestPkg);
        subscribedChannel.addErrata(retracted);

        // insert newer & newest packages with no reference to errata
        ErrataCacheManager.insertCacheForChannelPackages(subscribedChannel.getId(), null, List.of(newestPkg.getId()));
        ErrataCacheManager.insertCacheForChannelPackages(subscribedChannel.getId(), null, List.of(newerPkg.getId()));

        // only the newer should be in the cache since newest is retracted
        DataResult needingUpdates = ErrataCacheManager.packagesNeedingUpdates(server.getId());
        assertEquals(1, needingUpdates.size());
        assertEquals(newerPkg.getId(), ((ErrataCacheDto) needingUpdates.get(0)).getPackageId());

        // just to be sure, recompute the cache and verify that the results are same
        ServerFactory.updateServerNeededCache(server.getId());
        needingUpdates = ErrataCacheManager.packagesNeedingUpdates(server.getId());
        assertEquals(1, needingUpdates.size());
        assertEquals(newerPkg.getId(), ((ErrataCacheDto) needingUpdates.get(0)).getPackageId());
    }

    private static void installPackageOnServer(Package pkg, Server server) {
        InstalledPackage installedNewerPkg = createInstalledPackage(pkg);
        installedNewerPkg.setServer(server);
        server.getPackages().add(installedNewerPkg);
    }

    private static InstalledPackage createInstalledPackage(Package pkg) {
        InstalledPackage installedNewerPkg = new InstalledPackage();
        installedNewerPkg.setEvr(pkg.getPackageEvr());
        installedNewerPkg.setArch(pkg.getPackageArch());
        installedNewerPkg.setName(pkg.getPackageName());
        return installedNewerPkg;
    }

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
}
