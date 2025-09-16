/*
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

import static com.redhat.rhn.manager.channel.CloneChannelCommand.CloneBehavior.CURRENT_STATE;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.errata.AdvisoryStatus;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.frontend.dto.ErrataCacheDto;
import com.redhat.rhn.manager.channel.CloneChannelCommand;
import com.redhat.rhn.manager.errata.cache.ErrataCacheManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.PackageTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        server = ServerFactoryTest.createTestServer(user);

        List<Package> generatedPackages = PackageTestUtils.createSubsequentPackages(user.getOrg());
        oldPkg = generatedPackages.get(0);
        newerPkg = generatedPackages.get(1);
        newestPkg = generatedPackages.get(2);

        subscribedChannel = ChannelTestUtils.createBaseChannel(user);
        subscribedChannel.setChecksumType(ChannelFactory.findChecksumTypeByLabel("sha256"));
        SystemManager.subscribeServerToChannel(user, server, subscribedChannel);
    }

    /**
     * Test that inserting a retracted package with higher version to the cache has no effect.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testRetractedPatchesInCache() throws Exception {
        // channel has all packages
        subscribedChannel.getPackages().addAll(List.of(oldPkg, newerPkg, newestPkg));

        // oldest is installed on the server
        PackageTestUtils.installPackageOnServer(oldPkg, server);

        // newest is retracted
        Errata retracted = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        retracted.setAdvisoryStatus(AdvisoryStatus.RETRACTED);
        retracted.addPackage(newestPkg);
        subscribedChannel.addErrata(retracted);

        // insert newer & newest
        ErrataCacheManager.insertCacheForChannelPackages(
                subscribedChannel.getId(), retracted.getId(), List.of(newestPkg.getId()));
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
    @Test
    public void testRetractedPackagesInCache() throws Exception {
        // channel has all packages
        subscribedChannel.getPackages().addAll(List.of(oldPkg, newerPkg, newestPkg));

        // oldest is installed on the server
        PackageTestUtils.installPackageOnServer(oldPkg, server);

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

    /**
     * Tests updating the cache with a package that belongs to a retracted patch in one channel,
     * but belongs to a stable patch in another.
     */
    @Test
    public void testRetractedPackagesCacheClonedChannel() throws Exception {
        // create a null-org patch with a newest package and add it to the channel
        Errata vendorPatch = ErrataFactoryTest.createTestErrata(null);
        vendorPatch.addPackage(newestPkg);
        vendorPatch.addChannel(subscribedChannel);
        ErrataFactory.save(vendorPatch);

        // channel has all 3 packages
        subscribedChannel.getPackages().addAll(List.of(oldPkg, newerPkg, newestPkg));

        // clone the channel
        CloneChannelCommand ccc = new CloneChannelCommand(CURRENT_STATE, subscribedChannel);
        ccc.setUser(user);
        Channel clonedChannel = ccc.create();

        // set the patch in original to retracted
        vendorPatch.setAdvisoryStatus(AdvisoryStatus.RETRACTED);

        // the system is already subscribed to the channel with the retracted patch
        // system has the oldest version installed
        PackageTestUtils.installPackageOnServer(oldPkg, server);

        // insert "newer" into cache should be ok
        ErrataCacheManager.insertCacheForChannelPackages(subscribedChannel.getId(), null, List.of(newerPkg.getId()));
        ErrataCacheDto needingUpdates = (ErrataCacheDto) assertSingleAndGet(
                ErrataCacheManager.packagesNeedingUpdates(server.getId()));
        assertEquals(newerPkg.getId(), needingUpdates.getPackageId());

        // insert "newest" into cache should be a no-op
        ErrataCacheManager.insertCacheForChannelPackages(subscribedChannel.getId(), null, List.of(newestPkg.getId()));
        needingUpdates = (ErrataCacheDto) assertSingleAndGet(ErrataCacheManager.packagesNeedingUpdates(server.getId()));
        assertEquals(newerPkg.getId(), needingUpdates.getPackageId()); // the "newer" is still in cache

        // but if we subscribe to a channel, where the "newest" is not retracted, it should make it to the cache
        SystemManager.unsubscribeServerFromChannel(server, subscribedChannel);
        SystemManager.subscribeServerToChannel(user, server, clonedChannel);
        ErrataCacheManager.insertCacheForChannelPackages(clonedChannel.getId(), null, List.of(newestPkg.getId()));
        DataResult<ErrataCacheDto> result = ErrataCacheManager.packagesNeedingUpdates(server.getId());
        assertEquals(2, result.size()); // both should be in the cache
        assertEquals(
                Set.of(newerPkg.getId(), newestPkg.getId()),
                result.stream().map(ErrataCacheDto::getPackageId).collect(Collectors.toSet()));
    }

    private <T> T assertSingleAndGet(Collection<T> items) {
        assertEquals(1, items.size());
        return items.iterator().next();
    }
}
