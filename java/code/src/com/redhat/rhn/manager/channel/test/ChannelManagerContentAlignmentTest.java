/**
 * Copyright (c) 2019 SUSE LLC
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

package com.redhat.rhn.manager.channel.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.test.PackageEvrFactoryTest;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.errata.cache.ErrataCacheManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ChannelTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.redhat.rhn.domain.role.RoleFactory.ORG_ADMIN;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toSet;

/**
 * Test for the content-management related methods in {@link ChannelManager}
 */
public class ChannelManagerContentAlignmentTest extends BaseTestCaseWithUser {

    private Channel srcChannel;
    private Channel tgtChannel;
    private Errata errata;
    private Package pkg;

    /**
     * In test we prefabricate a content project with single environment and source (sw channel).
     * The source channel has single package and errata.
     * In the tests we'll aligning EnvironmentTarget to this source
     *
     * @throws Exception if anything goes wrong
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        user.addPermanentRole(ORG_ADMIN);

        // create objects needed in all tests:
        // we need at least a package, errata associated with it and a channel
        pkg = PackageTest.createTestPackage(user.getOrg());
        errata = ErrataFactoryTest.createTestPublishedErrata(user.getOrg().getId());
        errata.addPackage(pkg);

        srcChannel = ChannelFactoryTest.createTestChannel(user, false);
        srcChannel.addPackage(pkg);
        srcChannel.addErrata(errata);
        srcChannel = (Channel) HibernateFactory.reload(srcChannel);
        errata = (Errata) HibernateFactory.reload(errata);

        tgtChannel = ChannelTestUtils.createBaseChannel(user);
    }

    /**
     * Test that the packages and errata of the target are aligned to the source.
     *
     * @throws Exception if anything goes wrong
     */
    public void testAlignEntities() throws Exception {
        // let's add a package to the target. it should be removed after aligning
        tgtChannel.getPackages().add(PackageTest.createTestPackage(user.getOrg()));
        ChannelManager.alignChannelsSync(srcChannel, tgtChannel, user);

        // check that newest packages cache has been updated
        assertEquals(
                srcChannel.getPackages().stream().map(p -> p.getId()).collect(toSet()),
                ChannelManager.latestPackagesInChannel(tgtChannel).stream()
                        .map(m -> m.get("id"))
                        .collect(toSet()));

        // check that packages and errata have been aligned
        tgtChannel = (Channel) HibernateFactory.reload(tgtChannel);
        assertEquals(srcChannel.getPackages(), tgtChannel.getPackages());
        assertEquals(srcChannel.getErratas(), tgtChannel.getErratas());
    }

    /**
     * Test that the cache of newest packages in the channel is refreshed
     *
     * @throws Exception if anything goes wrong
     */
    public void testNewestPackagesCacheRefreshed() throws Exception {
        Package pack2 = PackageTest.createTestPackage(user.getOrg());

        tgtChannel.getPackages().add(pack2);
        ChannelFactory.refreshNewestPackageCache(tgtChannel, "java::alignPackages");
        assertEquals(pack2.getId(), ChannelManager.getLatestPackageEqual(tgtChannel.getId(), pack2.getPackageName().getName()));
        assertNull(ChannelManager.getLatestPackageEqual(tgtChannel.getId(), pkg.getPackageName().getName()));

        ChannelManager.alignChannelsSync(srcChannel, tgtChannel, user);
        assertEquals(pkg.getId(), ChannelManager.getLatestPackageEqual(tgtChannel.getId(), pkg.getPackageName().getName()));
        assertNull(ChannelManager.getLatestPackageEqual(tgtChannel.getId(), pack2.getPackageName().getName()));
    }

    /**
     * Test that the cache of needed packages by server is refreshed.
     *
     * @throws Exception if anything goes wrong
     */
    public void testServerNeededCacheRefreshed() throws Exception {
        Server server = ServerFactoryTest.createTestServer(user);
        // server will have an older package installed -> after the alignment, the rhnServerNeededCache
        // must contain the entry corresponding to the the newer package
        InstalledPackage olderPack = copyPackage(pkg, of("0.9.9"));
        olderPack.setServer(server);
        server.getPackages().add(olderPack);

        SystemManager.subscribeServerToChannel(user, server, tgtChannel);

        ChannelManager.alignChannelsSync(srcChannel, tgtChannel, user);
        assertEquals(errata.getId(), SystemManager.relevantErrata(user, server.getId()).get(0).getId());
    }

    /**
     * Test that aligning channels with packages and no errata adds the needed package to the cache.
     *
     * @throws Exception if anything goes wrong
     */
    public void testServerNeededCacheAddedNoErrata() throws Exception {
        Server server = ServerFactoryTest.createTestServer(user);
        SystemManager.subscribeServerToChannel(user, server, tgtChannel);
        // we want to test the cache update when channel with no errata is used
        srcChannel.getErratas().clear();

        InstalledPackage olderPkg = copyPackage(pkg, of("0.9.9"));
        olderPkg.setServer(server);
        server.getPackages().add(olderPkg);

        List<SystemOverview> systemsWithNeededPackage = SystemManager.listSystemsWithNeededPackage(user, pkg.getId());
        assertTrue(systemsWithNeededPackage.isEmpty());

        ChannelManager.alignChannelsSync(srcChannel, tgtChannel, user);
        systemsWithNeededPackage = SystemManager.listSystemsWithNeededPackage(user, pkg.getId());
        assertEquals(1, systemsWithNeededPackage.size());
        assertEquals(server.getId(), systemsWithNeededPackage.get(0).getId());
    }

    /**
     * Test that aligning channels with no errata removes package from the cache.
     *
     * @throws Exception if anything goes wrong
     */
    public void testServerNeededCacheRemovedNoErrata() throws Exception {
        Server server = ServerFactoryTest.createTestServer(user);
        // we want to test the cache update when channel with no errata is used
        srcChannel.getErratas().clear();

        Package otherPkg = PackageTest.createTestPackage(user.getOrg());
        tgtChannel.addPackage(otherPkg);
        SystemManager.subscribeServerToChannel(user, server, tgtChannel);

        InstalledPackage olderPkg = copyPackage(otherPkg, of("0.9.9"));
        olderPkg.setServer(server);
        server.getPackages().add(olderPkg);

        // we fake the cache entry here
        ErrataCacheManager.insertCacheForChannelPackages(tgtChannel.getId(), null, Collections.singletonList(otherPkg.getId()));
        List<SystemOverview> systemsWithNeededPackage = SystemManager.listSystemsWithNeededPackage(user, otherPkg.getId());
        assertEquals(1, systemsWithNeededPackage.size());
        assertEquals(server.getId(), systemsWithNeededPackage.get(0).getId());

        ChannelManager.alignChannelsSync(srcChannel, tgtChannel, user);
        systemsWithNeededPackage = SystemManager.listSystemsWithNeededPackage(user, otherPkg.getId());
        assertTrue(systemsWithNeededPackage.isEmpty());
    }

    /**
     * Test that aligning a target channel with erratum A to a source channel
     * that does not have this erratum will remove erratum A from the target.
     *
     * @throws Exception if anything goes wrong
     */
    public void testErrataRemoved() throws Exception {
        // this errata is in the target channel and is supposed to be removed after align
        Errata toRemove = ErrataFactoryTest.createTestPublishedErrata(user.getOrg().getId());;
        tgtChannel.addErrata(toRemove);

        ChannelManager.alignChannelsSync(srcChannel, tgtChannel, user);

        // check that packages and errata have been aligned
        assertEquals(srcChannel.getPackages(), tgtChannel.getPackages());
        assertContains(errata.getChannels(), srcChannel);
        assertContains(errata.getChannels(), tgtChannel);
    }

    private static InstalledPackage copyPackage(Package otherPkg, Optional<String> overrideVersion) {
        InstalledPackage olderPkg = new InstalledPackage();
        PackageEvr packageEvr = otherPkg.getPackageEvr();
        olderPkg.setEvr(PackageEvrFactoryTest.createTestPackageEvr(
                packageEvr.getEpoch(),
                overrideVersion.orElse(packageEvr.getVersion()),
                packageEvr.getRelease()
        ));
        olderPkg.setArch(otherPkg.getPackageArch());
        olderPkg.setName(otherPkg.getPackageName());
        return olderPkg;
    }
}