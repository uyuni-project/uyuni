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
import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.FilterCriteria;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.test.PackageEvrFactoryTest;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.contentmgmt.ContentManager;
import com.redhat.rhn.manager.errata.cache.ErrataCacheManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ChannelTestUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.redhat.rhn.domain.contentmgmt.ContentFilter.EntityType.ERRATUM;
import static com.redhat.rhn.domain.contentmgmt.ContentFilter.Rule.DENY;
import static com.redhat.rhn.domain.role.RoleFactory.ORG_ADMIN;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
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
        ChannelManager.alignEnvironmentTargetSync(emptyList(), srcChannel, tgtChannel, user);

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

        ChannelManager.alignEnvironmentTargetSync(emptyList(), srcChannel, tgtChannel, user);
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

        ChannelManager.alignEnvironmentTargetSync(emptyList(), srcChannel, tgtChannel, user);
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

        ChannelManager.alignEnvironmentTargetSync(emptyList(), srcChannel, tgtChannel, user);
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

        ChannelManager.alignEnvironmentTargetSync(emptyList(), srcChannel, tgtChannel, user);
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

        ChannelManager.alignEnvironmentTargetSync(emptyList(), srcChannel, tgtChannel, user);

        // check that packages and errata have been aligned
        assertEquals(srcChannel.getPackages(), tgtChannel.getPackages());
        assertContains(errata.getChannels(), srcChannel);
        assertContains(errata.getChannels(), tgtChannel);
    }

    /**
     * Test filtering out errata after channel alignment
     */
    public void testErrataFilters() {
        Errata erratum = srcChannel.getErratas().iterator().next();
        FilterCriteria criteria = new FilterCriteria(FilterCriteria.Matcher.EQUALS, "advisory_name", erratum.getAdvisoryName());
        ContentFilter filter = ContentManager.createFilter("test-filter-123", DENY, ERRATUM, criteria, user);

        ChannelManager.alignEnvironmentTargetSync(singleton(filter), srcChannel, tgtChannel, user);

        assertTrue(tgtChannel.getErratas().isEmpty());
    }

    /**
     * Test complex scenario of aligning errata (e1 - e6) when filters are involved
     *
     * CHANNEL-ERRATUM MAPPING:
     * Only in src channel: e1 (passes the filter), e2 (does not pass the filter)
     * Only in tgt channel: e3, e4
     * In both channels: e5 (passes the filter), e6 (does not pass the filter)
     *
     * After aligning, the target channel should only contain errata that are in source and that passed the filters.
     *
     * @throws Exception if anything goes wrong
     */
    public void testErrataFiltersComplex() throws Exception {
        Channel srcChan = ChannelFactoryTest.createTestChannel(user, false);
        Channel tgtChan = ChannelFactoryTest.createTestChannel(user, false);

        Errata e1 = ErrataFactoryTest.createTestPublishedErrata(user.getOrg().getId());
        Errata e2 = ErrataFactoryTest.createTestPublishedErrata(user.getOrg().getId());
        Errata e3 = ErrataFactoryTest.createTestPublishedErrata(user.getOrg().getId());
        Errata e4 = ErrataFactoryTest.createTestPublishedErrata(user.getOrg().getId());
        Errata e5 = ErrataFactoryTest.createTestPublishedErrata(user.getOrg().getId());
        Errata e6 = ErrataFactoryTest.createTestPublishedErrata(user.getOrg().getId());

        // src
        srcChan.addErrata(e1);
        srcChan.addErrata(e2);
        // tgt
        tgtChan.addErrata(e3);
        tgtChan.addErrata(e4);
        // both
        srcChan.addErrata(e5);
        srcChan.addErrata(e6);
        tgtChan.addErrata(e5);
        tgtChan.addErrata(e6);

        FilterCriteria criteria = new FilterCriteria(FilterCriteria.Matcher.EQUALS, "advisory_name", e2.getAdvisoryName());
        ContentFilter filter = ContentManager.createFilter("test-filter-1234", DENY, ERRATUM, criteria, user);

        FilterCriteria criteria2 = new FilterCriteria(FilterCriteria.Matcher.EQUALS, "advisory_name", e6.getAdvisoryName());
        ContentFilter filter2 = ContentManager.createFilter("test-filter-1235", DENY, ERRATUM, criteria2, user);

        ChannelManager.alignEnvironmentTargetSync(Arrays.asList(filter, filter2), srcChan, tgtChan, user);

        assertEquals(2, tgtChan.getErrataCount());
        tgtChan = (Channel) HibernateFactory.reload(tgtChan);
        assertContains(tgtChan.getErratas(), e1);
        assertContains(tgtChan.getErratas(), e5);
    }

    /**
     * Tests that packages of an {@link Errata} are removed when this Erratum is filtered out.
     *
     * @throws Exception if anything goes wrong
     */
    public void testPackagesRemovedOnErratumRemoval() throws Exception {
        // we assume version 1.0.0
        assertEquals("1.0.0", pkg.getPackageEvr().getVersion());

        Package olderPkg = copyPackage(pkg, user, "0.9.9");
        srcChannel.addPackage(olderPkg);

        FilterCriteria criteria = new FilterCriteria(FilterCriteria.Matcher.EQUALS, "advisory_name", errata.getAdvisoryName());
        ContentFilter filter = ContentManager.createFilter("test-filter-1234", DENY, ERRATUM, criteria, user);

        ChannelManager.alignEnvironmentTargetSync(singleton(filter), srcChannel, tgtChannel, user);

        tgtChannel = (Channel) HibernateFactory.reload(tgtChannel);

        assertEquals(1, tgtChannel.getPackageCount());
        assertEquals(0, tgtChannel.getErrataCount());
        assertContains(tgtChannel.getPackages(), olderPkg);
    }

    private static Package copyPackage(Package fromPkg, User user, String version) throws Exception {
        Package olderPkg = PackageTest.createTestPackage(user.getOrg());
        PackageEvr packageEvr = fromPkg.getPackageEvr();
        olderPkg.setPackageEvr(PackageEvrFactoryTest.createTestPackageEvr(
                packageEvr.getEpoch(),
                version,
                packageEvr.getRelease()
        ));
        olderPkg.setPackageName(fromPkg.getPackageName());
        return olderPkg;
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