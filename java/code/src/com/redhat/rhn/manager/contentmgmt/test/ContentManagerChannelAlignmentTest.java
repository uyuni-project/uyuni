/*
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

package com.redhat.rhn.manager.contentmgmt.test;

import static com.redhat.rhn.domain.contentmgmt.ContentFilter.EntityType.ERRATUM;
import static com.redhat.rhn.domain.contentmgmt.ContentFilter.EntityType.PACKAGE;
import static com.redhat.rhn.domain.contentmgmt.ContentFilter.Rule.ALLOW;
import static com.redhat.rhn.domain.contentmgmt.ContentFilter.Rule.DENY;
import static com.redhat.rhn.domain.role.RoleFactory.ORG_ADMIN;
import static com.redhat.rhn.manager.channel.CloneChannelCommand.CloneBehavior.CURRENT_STATE;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.FilterCriteria;
import com.redhat.rhn.domain.errata.AdvisoryStatus;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactory;
import com.redhat.rhn.domain.rhnpackage.test.PackageEvrFactoryTest;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ErrataCacheDto;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.manager.channel.ChannelManager;
import com.redhat.rhn.manager.channel.CloneChannelCommand;
import com.redhat.rhn.manager.contentmgmt.ContentManager;
import com.redhat.rhn.manager.errata.cache.ErrataCacheManager;
import com.redhat.rhn.manager.org.OrgManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ChannelTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Test for the content-management related methods in {@link ChannelManager}
 */
public class ContentManagerChannelAlignmentTest extends BaseTestCaseWithUser {

    private Channel srcChannel;
    private Channel tgtChannel;
    private Errata errata;
    private Package pkg;
    private ContentManager contentManager;

    /**
     * In test we prefabricate a content project with single environment and source (sw channel).
     * The source channel has single package and errata.
     * In the tests we'll aligning EnvironmentTarget to this source
     *
     * @throws Exception if anything goes wrong
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        contentManager = new ContentManager();
        user.addPermanentRole(ORG_ADMIN);

        // create objects needed in all tests:
        // we need at least a package, errata associated with it and a channel
        pkg = PackageTest.createTestPackage(user.getOrg());
        errata = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        errata.addPackage(pkg);

        srcChannel = ChannelFactoryTest.createTestChannel(user, false);
        srcChannel.setChecksumType(ChannelFactory.findChecksumTypeByLabel("sha256"));
        srcChannel.addPackage(pkg);
        srcChannel.addErrata(errata);
        srcChannel = HibernateFactory.reload(srcChannel);
        errata = HibernateFactory.reload(errata);

        tgtChannel = ChannelTestUtils.createBaseChannel(user);
    }

    /**
     * Test that the packages and errata of the target are aligned to the source.
     *
     */
    @Test
    public void testAlignEntities() {
        // let's add a package to the target. it should be removed after aligning
        tgtChannel.getPackages().add(PackageTest.createTestPackage(user.getOrg()));
        contentManager.alignEnvironmentTargetSync(emptyList(), srcChannel, tgtChannel, user);

        // check that newest packages cache has been updated
        assertEquals(
                srcChannel.getPackages().stream().map(Package::getId).collect(toSet()),
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
     */
    @Test
    public void testNewestPackagesCacheRefreshed() {
        Package pack2 = PackageTest.createTestPackage(user.getOrg());

        tgtChannel.getPackages().add(pack2);
        ChannelFactory.refreshNewestPackageCache(tgtChannel, "java::alignPackages");
        assertEquals(pack2.getId(),
                ChannelManager.getLatestPackageEqual(tgtChannel.getId(), pack2.getPackageName().getName()));
        assertNull(ChannelManager.getLatestPackageEqual(tgtChannel.getId(), pkg.getPackageName().getName()));

        contentManager.alignEnvironmentTargetSync(emptyList(), srcChannel, tgtChannel, user);
        assertEquals(pkg.getId(),
                ChannelManager.getLatestPackageEqual(tgtChannel.getId(), pkg.getPackageName().getName()));
        assertNull(ChannelManager.getLatestPackageEqual(tgtChannel.getId(), pack2.getPackageName().getName()));
    }

    /**
     * Test that the cache of newest packages in the channel is refreshed when filters are used
     *
     * Package-channel settings:
     * Source channel: package 1 (see setUp()), 2, 4, 5
     * Target channel: package 2, 3, 5, 6
     * Packages that pass filter: 4, 5
     *
     * Expected result: Target channel newest packages cache contains packages 4 and 5
     *
     */
    @Test
    public void testNewestPackagesCacheRefreshedWithFilter() {
        Package pack2 = PackageTest.createTestPackage(user.getOrg());
        Package pack3 = PackageTest.createTestPackage(user.getOrg());
        Package pack4 = PackageTest.createTestPackage(user.getOrg());
        Package pack5 = PackageTest.createTestPackage(user.getOrg());
        Package pack6 = PackageTest.createTestPackage(user.getOrg());

        srcChannel.addPackage(pack2);
        srcChannel.addPackage(pack4);
        srcChannel.addPackage(pack5);

        tgtChannel.getPackages().add(pack2);
        tgtChannel.getPackages().add(pack3);
        tgtChannel.getPackages().add(pack5);
        tgtChannel.getPackages().add(pack6);

        ChannelFactory.refreshNewestPackageCache(tgtChannel, "java::alignPackages");

        FilterCriteria criteria = new FilterCriteria(
                FilterCriteria.Matcher.CONTAINS, "name", pkg.getPackageName().getName());
        ContentFilter filter = contentManager.createFilter("test-filter-1234", DENY, PACKAGE, criteria, user);
        FilterCriteria criteria2 = new FilterCriteria(
                FilterCriteria.Matcher.CONTAINS, "name", pack2.getPackageName().getName());
        ContentFilter filter2 = contentManager.createFilter("test-filter-12345", DENY, PACKAGE, criteria2, user);

        contentManager.alignEnvironmentTargetSync(Arrays.asList(filter, filter2), srcChannel, tgtChannel, user);
        List<Long> ids = ChannelManager.latestPackagesInChannel(tgtChannel).stream()
                .map(p -> (Long) p.get("id"))
                .collect(Collectors.toList());
        assertEquals(2, ids.size());
        assertContains(ids, pack4.getId());
        assertContains(ids, pack5.getId());
    }

    /**
     * Test that the cache of needed packages by server is refreshed.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testServerNeededCacheRefreshed() throws Exception {
        Server server = ServerFactoryTest.createTestServer(user);
        // server will have an older package installed -> after the alignment, the rhnServerNeededCache
        // must contain the entry corresponding to the the newer package
        InstalledPackage olderPack = copyPackage(pkg, of("0.9.9"));
        setInstalledPackage(server, olderPack);

        SystemManager.subscribeServerToChannel(user, server, tgtChannel);

        contentManager.alignEnvironmentTargetSync(emptyList(), srcChannel, tgtChannel, user);
        assertEquals(errata.getId(), SystemManager.relevantErrata(user, server.getId()).get(0).getId());
    }

    /**
     * Test that aligning channels with packages and no errata adds the needed package to the cache.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testServerNeededCacheAddedNoErrata() throws Exception {
        Server server = ServerFactoryTest.createTestServer(user);
        SystemManager.subscribeServerToChannel(user, server, tgtChannel);
        // we want to test the cache update when channel with no errata is used
        srcChannel.getErratas().clear();

        InstalledPackage olderPkg = copyPackage(pkg, of("0.9.9"));
        setInstalledPackage(server, olderPkg);

        List<SystemOverview> systemsWithNeededPackage = SystemManager.listSystemsWithNeededPackage(user, pkg.getId());
        assertTrue(systemsWithNeededPackage.isEmpty());

        contentManager.alignEnvironmentTargetSync(emptyList(), srcChannel, tgtChannel, user);
        systemsWithNeededPackage = SystemManager.listSystemsWithNeededPackage(user, pkg.getId());
        assertEquals(1, systemsWithNeededPackage.size());
        assertEquals(server.getId(), systemsWithNeededPackage.get(0).getId());
    }

    /**
     * Test that aligning channels with no errata removes package from the cache.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testServerNeededCacheRemovedNoErrata() throws Exception {
        Server server = ServerFactoryTest.createTestServer(user);
        // we want to test the cache update when channel with no errata is used
        srcChannel.getErratas().clear();

        Package otherPkg = PackageTest.createTestPackage(user.getOrg());
        tgtChannel.addPackage(otherPkg);
        SystemManager.subscribeServerToChannel(user, server, tgtChannel);

        InstalledPackage olderPkg = copyPackage(otherPkg, of("0.9.9"));
        setInstalledPackage(server, olderPkg);

        // we fake the cache entry here
        ErrataCacheManager.insertCacheForChannelPackages(
                tgtChannel.getId(), null, Collections.singletonList(otherPkg.getId()));
        List<SystemOverview> systemsWithNeededPackage =
                SystemManager.listSystemsWithNeededPackage(user, otherPkg.getId());
        assertEquals(1, systemsWithNeededPackage.size());
        assertEquals(server.getId(), systemsWithNeededPackage.get(0).getId());

        contentManager.alignEnvironmentTargetSync(emptyList(), srcChannel, tgtChannel, user);
        systemsWithNeededPackage = SystemManager.listSystemsWithNeededPackage(user, otherPkg.getId());
        assertTrue(systemsWithNeededPackage.isEmpty());
    }

    /**
     * Tests that aligning channels with filters that remove a package is reflected in the rhnServerNeededCache
     *
     * Configuration:
     * - source channel - 2 packages
     * - system with 2 packages installed (lower versions than the ones in source channel), subscribed to target channel
     * - Test 1: align target channel to the source with no filters
     * - Test 2: align target channel to the source with a filter that filters out a package
     *
     * @throws if anything goes wrong
     */
    @Test
    public void testServerNeededCacheWithFilters() throws Exception {
        // setup
        Package pack1 = PackageTest.createTestPackage(user.getOrg());
        Package pack2 = PackageTest.createTestPackage(user.getOrg());

        Channel srcChan = ChannelFactoryTest.createTestChannel(user, false);
        srcChan.addPackage(pack1);
        srcChan.addPackage(pack2);

        Server server = ServerFactoryTest.createTestServer(user);
        SystemManager.subscribeServerToChannel(user, server, tgtChannel);
        InstalledPackage olderPkg = copyPackage(pack1, of("0.9.9"));
        setInstalledPackage(server, olderPkg);
        InstalledPackage olderPkg2 = copyPackage(pack2, of("0.9.9"));
        setInstalledPackage(server, olderPkg2);

        // 1. align without filters
        contentManager.alignEnvironmentTargetSync(emptyList(), srcChan, tgtChannel, user);
        DataResult<ErrataCacheDto> needingUpdates = ErrataCacheManager.packagesNeedingUpdates(server.getId());
        // both packages are in the cache for the server
        assertEquals(2, needingUpdates.size());
        assertTrue(needingUpdates.stream()
                        .map(ErrataCacheDto::getPackageId)
                        .collect(Collectors.toList())
                .containsAll(Arrays.asList(pack1.getId(), pack2.getId())));

        // 2. align with a filter - 1 package should be filtered out and removed from the cache
        FilterCriteria criteria = new FilterCriteria(
                FilterCriteria.Matcher.CONTAINS, "name", pack1.getPackageName().getName());
        ContentFilter filter = contentManager.createFilter("test-filter-1234", DENY, PACKAGE, criteria, user);
        contentManager.alignEnvironmentTargetSync(Arrays.asList(filter), srcChan, tgtChannel, user);

        // only one package is in the cache for the server
        needingUpdates = ErrataCacheManager.packagesNeedingUpdates(server.getId());
        assertEquals(1, needingUpdates.size());
        assertEquals(pack2.getId(), needingUpdates.get(0).getPackageId());
    }

    /**
     * Tests that filtering errata from a channel when aligning channels
     * removes the errata and its package from the rhnServerNeededCache.
     *
     * Configuration:
     * - 2 channels: source (contains packages pack1 and pack2) and target
     * - 2 errata
     *   - errata1 contains pack1
     *   - errata2 contains pack2
     * - a system with 2 installed packages (lower versions of pack1 and pack2) subscribed to the target channel
     *
     * Aligning target channel to the source one _without_ filters leads to 4 cache entries.
     * Aligning target channel to the source one with filtering out errata1 leads to 2 cache entries.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testServerNeededCacheErrataWithFilters() throws Exception {
        // setup
        Package pack1 = PackageTest.createTestPackage(user.getOrg());
        Package pack2 = PackageTest.createTestPackage(user.getOrg());

        Channel srcChan = ChannelFactoryTest.createTestChannel(user, false);
        Channel tgtChan = ChannelFactoryTest.createTestChannel(user, false);
        srcChan.addPackage(pack1);
        srcChan.addPackage(pack2);

        Errata errata1 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        errata1.addPackage(pack1);
        Errata errata2 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        errata2.addPackage(pack2);

        srcChan.addErrata(errata1);
        srcChan.addErrata(errata2);

        Server server = ServerFactoryTest.createTestServer(user);
        InstalledPackage olderPkg = copyPackage(pack1, of("0.9.9"));
        setInstalledPackage(server, olderPkg);
        InstalledPackage olderPkg2 = copyPackage(pack2, of("0.9.9"));
        setInstalledPackage(server, olderPkg2);

        SystemManager.subscribeServerToChannel(user, server, tgtChan);

        // 1. let's align the channels without filters
        contentManager.alignEnvironmentTargetSync(emptyList(), srcChan, tgtChan, user);
        // let's check that errata cache contains all entries
        DataResult<ErrataCacheDto> needingUpdates = ErrataCacheManager.packagesNeedingUpdates(server.getId());
        assertEquals(2, needingUpdates.size());
        assertTrue(needingUpdates.stream()
                .anyMatch(errataCache -> errataCache.getPackageId().equals(pack1.getId()) &&
                        errata1.getId().equals(errataCache.getErrataId())));
        assertTrue(needingUpdates.stream()
                .anyMatch(errataCache -> errataCache.getPackageId().equals(pack2.getId()) &&
                        errata2.getId().equals(errataCache.getErrataId())));

        FilterCriteria criteria = new FilterCriteria(
                FilterCriteria.Matcher.EQUALS, "advisory_name", errata1.getAdvisoryName());
        ContentFilter filter = contentManager.createFilter("test-filter-1234", DENY, ERRATUM, criteria, user);

        // 2. let's align the channel again and check that the errata1 and its package is not in the cache anymore
        contentManager.alignEnvironmentTargetSync(Arrays.asList(filter), srcChan, tgtChan, user);
        needingUpdates = ErrataCacheManager.packagesNeedingUpdates(server.getId());
        assertEquals(1, needingUpdates.size());
        assertTrue(needingUpdates.stream()
                .anyMatch(errataCache -> errataCache.getPackageId().equals(pack2.getId()) &&
                        errata2.getId().equals(errataCache.getErrataId())));
    }

    /**
     * Test that aligning a target channel with erratum A to a source channel
     * that does not have this erratum will remove erratum A from the target.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testErrataRemoved() throws Exception {
        // this errata is in the target channel and is supposed to be removed after align
        Errata toRemove = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        tgtChannel.addErrata(toRemove);

        contentManager.alignEnvironmentTargetSync(emptyList(), srcChannel, tgtChannel, user);

        // check that packages and errata have been aligned
        assertEquals(srcChannel.getPackages(), tgtChannel.getPackages());
        assertContains(errata.getChannels(), srcChannel);
        assertContains(errata.getChannels(), tgtChannel);
    }

    /**
     * Test filtering out errata after channel alignment
     */
    @Test
    public void testErrataFilters() {
        FilterCriteria criteria = new FilterCriteria(
                FilterCriteria.Matcher.EQUALS, "advisory_name", errata.getAdvisoryName());
        ContentFilter filter = contentManager.createFilter("test-filter-123", DENY, ERRATUM, criteria, user);

        contentManager.alignEnvironmentTargetSync(singleton(filter), srcChannel, tgtChannel, user);

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
    @Test
    public void testErrataFiltersComplex() throws Exception {
        Channel srcChan = ChannelFactoryTest.createTestChannel(user, false);
        Channel tgtChan = ChannelFactoryTest.createTestChannel(user, false);

        Errata e1 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        Errata e2 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        Errata e3 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        Errata e4 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        Errata e5 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        Errata e6 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());

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

        FilterCriteria criteria = new FilterCriteria(
                FilterCriteria.Matcher.EQUALS, "advisory_name", e2.getAdvisoryName());
        ContentFilter filter = contentManager.createFilter("test-filter-1234", DENY, ERRATUM, criteria, user);

        FilterCriteria criteria2 = new FilterCriteria(
                FilterCriteria.Matcher.EQUALS, "advisory_name", e6.getAdvisoryName());
        ContentFilter filter2 = contentManager.createFilter("test-filter-1235", DENY, ERRATUM, criteria2, user);

        contentManager.alignEnvironmentTargetSync(Arrays.asList(filter, filter2), srcChan, tgtChan, user);

        assertEquals(2, tgtChan.getErrataCount());
        tgtChan = (Channel) HibernateFactory.reload(tgtChan);
        assertContains(tgtChan.getErratas(), e1);
        assertContains(tgtChan.getErratas(), e5);
    }

    // nothing exciting, allow filter should have no effect
    @Test
    public void testSimpleAllowFilter() {
        FilterCriteria criteria = new FilterCriteria(FilterCriteria.Matcher.EQUALS, "nevra", pkg.getNameEvra());
        ContentFilter filter = contentManager.createFilter("filter123", ALLOW, PACKAGE, criteria, user);

        contentManager.alignEnvironmentTargetSync(List.of(filter), srcChannel, tgtChannel, user);

        assertEquals(srcChannel.getPackages(), tgtChannel.getPackages());
    }

    /**
     * Test combination of ALLOW and DENY filter on a package
     */
    @Test
    public void testAllowDenyFilters() {
        FilterCriteria criteria = new FilterCriteria(FilterCriteria.Matcher.EQUALS, "nevra", pkg.getNameEvra());
        ContentFilter denyFilter = contentManager.createFilter("denyfilter123", DENY, PACKAGE, criteria, user);

        // using only DENY filter filters the package out
        contentManager.alignEnvironmentTargetSync(List.of(denyFilter), srcChannel, tgtChannel, user);
        assertFalse(tgtChannel.getPackages().contains(pkg));

        // but in combination of DENY & ALLOW filter, the ALLOW filter makes the package unfiltered
        ContentFilter allowFilter = contentManager.createFilter("allowfilter123", ALLOW, PACKAGE, criteria, user);
        contentManager.alignEnvironmentTargetSync(List.of(allowFilter, denyFilter), srcChannel, tgtChannel, user);
        assertTrue(tgtChannel.getPackages().contains(pkg));
    }

    /**
     * Test combination of ALLOW and DENY filter on an erratum
     */
    @Test
    public void testAllowDenyFiltersErrata() {
        FilterCriteria criteria = new FilterCriteria(
                FilterCriteria.Matcher.EQUALS, "advisory_name", errata.getAdvisoryName());
        ContentFilter denyFilter = contentManager.createFilter("denyfilter123", DENY, ERRATUM, criteria, user);

        // using only DENY filter filters the erratum out
        contentManager.alignEnvironmentTargetSync(List.of(denyFilter), srcChannel, tgtChannel, user);
        tgtChannel = (Channel) HibernateFactory.reload(tgtChannel);
        assertFalse(tgtChannel.getErratas().contains(errata));

        // but in combination of DENY & ALLOW filter, the ALLOW filter makes the erratum unfiltered
        ContentFilter allowFilter = contentManager.createFilter("allowfilter123", ALLOW, ERRATUM, criteria, user);
        contentManager.alignEnvironmentTargetSync(List.of(allowFilter, denyFilter), srcChannel, tgtChannel, user);
        tgtChannel = (Channel) HibernateFactory.reload(tgtChannel);
        assertTrue(tgtChannel.getErratas().contains(errata));
    }

    /**
     * Test combination of ALLOW and DENY filter on an erratum
     *
     * We use a DENY filter to filter out bunch of errata and then we re-include an erratum using an
     * ALLOW filter
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testAllowDenyFiltersErrata2() throws Exception {
        Channel srcChan = ChannelFactoryTest.createTestChannel(user, false);

        Errata e1 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        e1.setIssueDate(new Date(1000));
        Errata e2 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        e2.setIssueDate(new Date(2000));
        String advisoryName = "my-advisory-name";
        Errata e3 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        e3.setIssueDate(new Date(3000));
        e3.setAdvisoryName(advisoryName);
        Errata e4 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        e4.setIssueDate(new Date(4000));

        srcChan.addErrata(e1);
        srcChan.addErrata(e2);
        srcChan.addErrata(e3);

        FilterCriteria criteria = new FilterCriteria(FilterCriteria.Matcher.GREATEREQ, "issue_date",
                "1970-01-01T01:00:02+01:00");
        ContentFilter denyFilter = contentManager.createFilter("denyfilter123", DENY, ERRATUM, criteria, user);
        FilterCriteria criteria2 = new FilterCriteria(FilterCriteria.Matcher.EQUALS, "advisory_name", advisoryName);
        ContentFilter allowFilter = contentManager.createFilter("allowfilter123", ALLOW, ERRATUM, criteria2, user);

        contentManager.alignEnvironmentTargetSync(List.of(allowFilter, denyFilter), srcChan, tgtChannel, user);

        tgtChannel = (Channel) HibernateFactory.reload(tgtChannel);
        assertTrue(tgtChannel.getErratas().contains(e1));
        assertFalse(tgtChannel.getErratas().contains(e2));
        assertTrue(tgtChannel.getErratas().contains(e3));
        assertFalse(tgtChannel.getErratas().contains(e4));
    }

    /**
     * Tests that packages of an {@link Errata} are removed when this Erratum is filtered out.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testPackagesRemovedOnErratumRemoval() throws Exception {
        // we assume version 1.0.0
        assertEquals("1.0.0", pkg.getPackageEvr().getVersion());

        Package olderPkg = copyPackage(pkg, user, "0.9.9");
        srcChannel.addPackage(olderPkg);

        FilterCriteria criteria = new FilterCriteria(
                FilterCriteria.Matcher.EQUALS, "advisory_name", errata.getAdvisoryName());
        ContentFilter filter = contentManager.createFilter("test-filter-1234", DENY, ERRATUM, criteria, user);

        contentManager.alignEnvironmentTargetSync(singleton(filter), srcChannel, tgtChannel, user);

        tgtChannel = (Channel) HibernateFactory.reload(tgtChannel);

        assertEquals(1, tgtChannel.getPackageCount());
        assertEquals(0, tgtChannel.getErrataCount());
        assertContains(tgtChannel.getPackages(), olderPkg);
    }

    /**
     * Tests that aligning a channel containing a retracted patch, the caches
     * related to the target channel (rhnServerNeededCache and rhnChannelNewestPackage)
     * do NOT contain the retracted packages.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testCachesAlignmentRetractedPackages() throws Exception {
        // server has the pkg installed and is subscribed to the target channel
        Server server = ServerFactoryTest.createTestServer(user);
        SystemManager.subscribeServerToChannel(user, server, tgtChannel);
        InstalledPackage installedPkg = copyPackage(pkg, empty());
        setInstalledPackage(server, installedPkg);

        // pkg2 is a part of a retracted patch
        Package pkg2 = PackageTest.createTestPackage(user.getOrg());
        pkg2.setPackageName(pkg.getPackageName());
        PackageEvr evr = pkg.getPackageEvr();
        pkg2.setPackageEvr(PackageEvrFactory.lookupOrCreatePackageEvr(
                evr.getEpoch(), "2.0.0", evr.getRelease(), pkg.getPackageType()));
        Errata retracted = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        retracted.setAdvisoryStatus(AdvisoryStatus.RETRACTED);
        retracted.addPackage(pkg2);
        srcChannel.addPackage(pkg2);
        srcChannel.addErrata(retracted);

        contentManager.alignEnvironmentTargetSync(emptyList(), srcChannel, tgtChannel, user);
        // after aligning the channels, the retracted package shouldn't appear in the rhnServerNeeded cache
        DataResult needingUpdates = ErrataCacheManager.packagesNeedingUpdates(server.getId());
        assertTrue(needingUpdates.isEmpty());
        // nor should it be reported as the newest package in the channel
        assertEquals(pkg.getId(),
                ChannelManager.getLatestPackageEqual(tgtChannel.getId(), pkg.getPackageName().getName()));
    }

    /**
     * Similar as testCachesAlignmentRetractedPackages, but we're using a cloned channel as a source.
     * In this clone, there is a stable patch that has been cloned from another patch, which
     * has been retracted in the meantime.
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testCachesAlignmentRetractedPackagesClones() throws Exception {
        // server has the pkg installed and is subscribed to the target channel
        Server server = ServerFactoryTest.createTestServer(user);
        SystemManager.subscribeServerToChannel(user, server, tgtChannel);
        InstalledPackage installedPkg = copyPackage(pkg, empty());
        setInstalledPackage(server, installedPkg);

        // clear the existing patch
        srcChannel.getErratas().clear();

        // pkg2 is a part of a retracted patch
        Package pkg2 = PackageTest.createTestPackage(user.getOrg());
        pkg2.setPackageName(pkg.getPackageName());
        PackageEvr evr = pkg.getPackageEvr();
        pkg2.setPackageEvr(PackageEvrFactory.lookupOrCreatePackageEvr(
                evr.getEpoch(), "2.0.0", evr.getRelease(), pkg.getPackageType()));
        Errata patch = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        patch.addPackage(pkg2);
        srcChannel.addPackage(pkg2);
        srcChannel.addErrata(patch);

        // we clone the source and retract the patch in the original afterwards
        CloneChannelCommand ccc = new CloneChannelCommand(CURRENT_STATE, srcChannel);
        ccc.setUser(user);
        Channel sourceClone = ccc.create();
        patch.setAdvisoryStatus(AdvisoryStatus.RETRACTED);

        contentManager.alignEnvironmentTargetSync(emptyList(), sourceClone, tgtChannel, user);
        // after aligning the channels, the package in the cloned channel should appear in the cache
        DataResult<ErrataCacheDto> needingUpdates = ErrataCacheManager.packagesNeedingUpdates(server.getId());
        assertEquals(1, needingUpdates.size());
        assertTrue(needingUpdates.stream()
                .anyMatch(c -> c.getPackageId().equals(pkg2.getId()) &&
                        sourceClone.getErratas().iterator().next().getId().equals(c.getErrataId())));
        // and it should be reported as the newest package in the channel
        assertEquals(pkg2.getId(),
                ChannelManager.getLatestPackageEqual(tgtChannel.getId(), pkg.getPackageName().getName()));
    }

    /**
     * 3 versions of a package: 1.0.0, 2.0.0, 3.0.0.
     * 2 non-retracted patches: for 2.0.0 and 3.0.0
     * We align the channels check the caches.
     * Then vendor retracts the patch 3.0.0.
     * We align the channels again.
     * Result: The caches should not contain any traces of retracted patch/package.
     * After that, the vendor makes the patch final again.
     * We align the channels again.
     * Result: The caches should contain previously retracted patch/package.
     */
    @Test
    public void testLaterRetractedPatch() throws Exception {
        // server has the pkg installed and is subscribed to the target channel
        Server server = ServerFactoryTest.createTestServer(user);
        SystemManager.subscribeServerToChannel(user, server, tgtChannel);

        // let's make the tgt clone of source (as it should be in CLM scenario)
        ChannelManager.addCloneInfo(srcChannel.getId(), tgtChannel.getId());
        tgtChannel = HibernateFactory.reload(tgtChannel);

        InstalledPackage installedPkg = copyPackage(pkg, empty());
        setInstalledPackage(server, installedPkg);

        // create a (non-retracted) patch that upgrades pkg to 2.0.0
        Package pkg2 = PackageTest.createTestPackage(user.getOrg());
        pkg2.setPackageName(pkg.getPackageName());
        PackageEvr evr = pkg.getPackageEvr();
        pkg2.setPackageEvr(PackageEvrFactory.lookupOrCreatePackageEvr(
                evr.getEpoch(), "2.0.0", evr.getRelease(), pkg.getPackageType()));
        Errata patch2 = ErrataFactoryTest.createTestErrata(null);
        patch2.addPackage(pkg2);
        srcChannel.addPackage(pkg2);
        srcChannel.addErrata(patch2);

        // create a (non-retracted) patch that upgrades pkg to 3.0.0
        Package pkg3 = PackageTest.createTestPackage(user.getOrg());
        pkg3.setPackageName(pkg.getPackageName());
        pkg3.setPackageEvr(PackageEvrFactory.lookupOrCreatePackageEvr(
                evr.getEpoch(), "3.0.0", evr.getRelease(), pkg.getPackageType()));
        Errata patch3 = ErrataFactoryTest.createTestErrata(null);
        patch3.addPackage(pkg3);
        srcChannel.addPackage(pkg3);
        srcChannel.addErrata(patch3);

        // assumptions
        DataResult<ErrataCacheDto> needingUpdates = ErrataCacheManager.packagesNeedingUpdates(server.getId());
        assertEquals(0, needingUpdates.size());

        // tests
        contentManager.alignEnvironmentTargetSync(emptyList(), srcChannel, tgtChannel, user);

        // after building he cache should contain 4 entries (2 for each package (one of them with errata, one without))
        needingUpdates = ErrataCacheManager.packagesNeedingUpdates(server.getId());
        assertEquals(2, needingUpdates.size());
        assertTrue(needingUpdates.stream()
                .anyMatch(ne -> ne.getPackageId().equals(pkg2.getId()) && ne.getErrataId() != null));
        assertTrue(needingUpdates.stream()
                .anyMatch(ne -> ne.getPackageId().equals(pkg3.getId()) && ne.getErrataId() != null));
        assertEquals(pkg3.getId(),
                ChannelManager.getLatestPackageEqual(tgtChannel.getId(), pkg.getPackageName().getName()));

        // now we retract the patch
        // after building, the cache should not contain any entries related to the retracted patch and package
        patch3.setAdvisoryStatus(AdvisoryStatus.RETRACTED);
        contentManager.alignEnvironmentTargetSync(emptyList(), srcChannel, tgtChannel, user);
        needingUpdates = ErrataCacheManager.packagesNeedingUpdates(server.getId());
        assertEquals(1, needingUpdates.size());
        assertFalse(needingUpdates.stream()
                .anyMatch(ne -> ne.getPackageId().equals(pkg3.getId()) && ne.getErrataId() != null));
        assertEquals(pkg2.getId(),
                ChannelManager.getLatestPackageEqual(tgtChannel.getId(), pkg.getPackageName().getName()));

        // now we make the patch final again
        // after building, the cache should contain entries related to the previously retracted patch and package
        patch3.setAdvisoryStatus(AdvisoryStatus.FINAL);
        contentManager.alignEnvironmentTargetSync(emptyList(), srcChannel, tgtChannel, user);
        needingUpdates = ErrataCacheManager.packagesNeedingUpdates(server.getId());
        assertEquals(2, needingUpdates.size());
        assertTrue(needingUpdates.stream()
                .anyMatch(ne -> ne.getPackageId().equals(pkg2.getId()) && ne.getErrataId() != null));
        assertTrue(needingUpdates.stream()
                .anyMatch(ne -> ne.getPackageId().equals(pkg3.getId()) && ne.getErrataId() != null));
        assertEquals(pkg3.getId(),
                ChannelManager.getLatestPackageEqual(tgtChannel.getId(), pkg.getPackageName().getName()));
    }

    /**
     * Tests that the ORG-wide sync patches options is respected when aligning the channels.
     */
    @Test
    public void testClmSyncOption() {
        assertEquals(1, srcChannel.getErratas().size()); // assumption
        srcChannel.getErratas().iterator().next().setOrg(null);  // turn this into a vendor patch
        // let's make the tgt clone of source (as it should be in CLM scenario)
        ChannelManager.addCloneInfo(srcChannel.getId(), tgtChannel.getId());
        tgtChannel = HibernateFactory.reload(tgtChannel);

        // let's align the channels first
        contentManager.alignEnvironmentTargetSync(emptyList(), srcChannel, tgtChannel, user);

        // disable patch sync in my org, retract patch, align again
        OrgManager.setClmSyncPatchesConfig(user, user.getOrg().getId(), false);
        srcChannel.getErratas().iterator().next().setAdvisoryStatus(AdvisoryStatus.RETRACTED);
        contentManager.alignEnvironmentTargetSync(emptyList(), srcChannel, tgtChannel, user);

        assertEquals(1, tgtChannel.getErratas().size()); // assumption
        assertNotSame(tgtChannel.getErratas().iterator().next().getAdvisoryStatus(), AdvisoryStatus.RETRACTED);

        // enable patch sync in my org, retract patch, align again
        OrgManager.setClmSyncPatchesConfig(user, user.getOrg().getId(), true);
        contentManager.alignEnvironmentTargetSync(emptyList(), srcChannel, tgtChannel, user);
        assertEquals(1, tgtChannel.getErratas().size()); // assumption
        assertEquals(AdvisoryStatus.RETRACTED, tgtChannel.getErratas().iterator().next().getAdvisoryStatus());
    }

    private static Package copyPackage(Package fromPkg, User user, String version) {
        Package olderPkg = PackageTest.createTestPackage(user.getOrg());
        PackageEvr packageEvr = fromPkg.getPackageEvr();
        olderPkg.setPackageEvr(PackageEvrFactoryTest.createTestPackageEvr(
                packageEvr.getEpoch(),
                version,
                packageEvr.getRelease(),
                packageEvr.getPackageType()
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
                packageEvr.getRelease(),
                packageEvr.getPackageType()
        ));
        olderPkg.setArch(otherPkg.getPackageArch());
        olderPkg.setName(otherPkg.getPackageName());
        return olderPkg;
    }

    private void setInstalledPackage(Server server, InstalledPackage olderPack) {
        olderPack.setServer(server);
        server.getPackages().add(olderPack);
    }
}
