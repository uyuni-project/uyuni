/*
 * Copyright (c) 2022 SUSE LLC
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

package com.redhat.rhn.manager.errata.cache;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactoryTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.PackageTest;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerFactoryTest;
import com.redhat.rhn.frontend.dto.ErrataCacheDto;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.PackageTestUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class PtfPackagesCacheManagerTest extends BaseTestCaseWithUser {

    private Server server;
    private Channel subscribedChannel;

    // 3 ascending versions of the same package
    private Package originalPackage;

    private Package ptfPackage;

    private Package newerPackage;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        server = ServerFactoryTest.createTestServer(user);

        originalPackage = PackageTest.createTestPackage(user.getOrg());
        ptfPackage = PackageTestUtils.createPtfPackage(originalPackage, "123456", "1", user.getOrg());
        newerPackage = PackageTestUtils.newVersionOfPackage(originalPackage, null, "2.0.0", null, user.getOrg());

        subscribedChannel = ChannelTestUtils.createBaseChannel(user);
        subscribedChannel.setChecksumType(ChannelFactory.findChecksumTypeByLabel("sha256"));
        SystemManager.subscribeServerToChannel(user, server, subscribedChannel);
    }

    /**
     * Test that inserting a ptf package with higher version to the cache has no effect.
     *
     */
    @Test
    public void ensurePtfPackagesNotAreInsertedByPackageInNeededCache() {
        // channel has original and ptf packages
        subscribedChannel.getPackages().addAll(List.of(originalPackage, ptfPackage, newerPackage));

        // original is installed on the server
        PackageTestUtils.installPackageOnServer(originalPackage, server);

        // insert newer & newest
        ErrataCacheManager.insertCacheForChannelPackages(subscribedChannel.getId(), null, List.of(ptfPackage.getId()));

        // Verify that no ptf are in the needed cache
        DataResult<ErrataCacheDto> needingUpdates = ErrataCacheManager.packagesNeedingUpdates(server.getId());
        Assertions.assertEquals(0L, needingUpdates.stream()
                                                  .map(e -> PackageFactory.lookupByIdAndUser(e.getPackageId(), user))
                                                  .filter(Package::isPartOfPtf)
                                                  .count());

        // just to be sure, recompute the cache and verify that the results are same
        ServerFactory.updateServerNeededCache(server.getId());
        needingUpdates = ErrataCacheManager.packagesNeedingUpdates(server.getId());
        Assertions.assertEquals(0L, needingUpdates.stream()
                                                  .map(e -> PackageFactory.lookupByIdAndUser(e.getPackageId(), user))
                                                  .filter(Package::isPartOfPtf)
                                                  .count());
    }

    /**
     * Test that inserting an errata with ptf package with higher version to the cache has no effect.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void ensurePtfPackagesNotAreInsertedByErrataInNeededCache() throws Exception {
        // channel has original and ptf packages
        subscribedChannel.getPackages().addAll(List.of(originalPackage, ptfPackage, newerPackage));

        Errata errataPtf = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        errataPtf.addPackage(ptfPackage);
        subscribedChannel.addErrata(errataPtf);

        // original is installed on the server
        PackageTestUtils.installPackageOnServer(originalPackage, server);

        // insert newer & newest
        ErrataCacheManager.insertCacheForChannelPackages(subscribedChannel.getId(), errataPtf.getId(),
            List.of(ptfPackage.getId()));

        // Verify that no ptf are in the needed cache
        DataResult<ErrataCacheDto> needingUpdates = ErrataCacheManager.packagesNeedingUpdates(server.getId());
        Assertions.assertEquals(0L, needingUpdates.stream()
                                                  .map(e -> PackageFactory.lookupByIdAndUser(e.getPackageId(), user))
                                                  .filter(Package::isPartOfPtf)
                                                  .count());

        // just to be sure, recompute the cache and verify that the results are same
        ServerFactory.updateServerNeededCache(server.getId());
        needingUpdates = ErrataCacheManager.packagesNeedingUpdates(server.getId());
        Assertions.assertEquals(0L, needingUpdates.stream()
                                                  .map(e -> PackageFactory.lookupByIdAndUser(e.getPackageId(), user))
                                                  .filter(Package::isPartOfPtf)
                                                  .count());
    }

    /**
     * Test that inserting higher version are respected even when PTF are involved
     *
     */
    @Test
    public void ensureHigherVersionCalculationIsCorrectWithPtfPackages() {
        // channel has original and ptf packages
        subscribedChannel.getPackages().addAll(List.of(originalPackage, ptfPackage, newerPackage));

        // original is installed on the server
        PackageTestUtils.installPackageOnServer(originalPackage, server);

        // insert newer & newest
        ErrataCacheManager.insertCacheForChannelPackages(subscribedChannel.getId(), null,
            List.of(ptfPackage.getId(), newerPackage.getId()));

        // Verify that no ptf are in the needed cache
        DataResult<ErrataCacheDto> needingUpdates = ErrataCacheManager.packagesNeedingUpdates(server.getId());
        Assertions.assertEquals(1L, needingUpdates.size());
        Assertions.assertEquals(0L, needingUpdates.stream()
                                                  .map(e -> PackageFactory.lookupByIdAndUser(e.getPackageId(), user))
                                                  .filter(Package::isPartOfPtf)
                                                  .count());

        // just to be sure, recompute the cache and verify that the results are same
        ServerFactory.updateServerNeededCache(server.getId());
        needingUpdates = ErrataCacheManager.packagesNeedingUpdates(server.getId());
        Assertions.assertEquals(1L, needingUpdates.size());
        Assertions.assertEquals(0L, needingUpdates.stream()
                                                  .map(e -> PackageFactory.lookupByIdAndUser(e.getPackageId(), user))
                                                  .filter(Package::isPartOfPtf)
                                                  .count());
    }
}
