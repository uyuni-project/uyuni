/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.redhat.rhn.manager.contentmgmt;

import static com.redhat.rhn.manager.contentmgmt.ContentManagerTestUtils.buildProjectWithEnvironments;
import static com.redhat.rhn.manager.contentmgmt.ContentManagerTestUtils.getRegularChannels;
import static java.util.Optional.empty;
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFactoryTest;
import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.ContentFilter.EntityType;
import com.redhat.rhn.domain.contentmgmt.ContentFilter.Rule;
import com.redhat.rhn.domain.contentmgmt.FilterCriteria;
import com.redhat.rhn.domain.contentmgmt.FilterCriteria.Matcher;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.errata.ErrataTestBuilder;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactoryTest;
import com.redhat.rhn.domain.rhnpackage.PackageTest;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.task.TaskFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.channel.manage.ErrataHelper;
import com.redhat.rhn.frontend.dto.ErrataCacheDto;
import com.redhat.rhn.frontend.xmlrpc.BaseHandlerTestCase;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelException;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchChannelException;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.manager.EntityNotExistsException;
import com.redhat.rhn.manager.errata.cache.ErrataCacheManager;
import com.redhat.rhn.manager.errata.cache.UpdateErrataCacheCommand;
import com.redhat.rhn.manager.errata.syncwithpackages.dto.SyncedErratumWithPackages;
import com.redhat.rhn.taskomatic.task.errata.ErrataCacheWorker;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.ErrataTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * Tests cloning an erratum into CLM hotfix channels with {@link ContentManager}.
 *
 * Common setup: channel {@code source} serves packages pkgA, pkgC and pkgS and holds two vendor errata:
 * - {@code errata}, declaring pkgA and pkgB (pkgB is not served by {@code source})
 * - {@code other}, declaring pkgC
 * pkgS belongs to no erratum.
 * Projects using {@code source}:
 * - "cp" with environments dev, test and prod
 * - "cp2" with environment dev
 * Project "cpother" uses an unrelated source
 * Every environment has a hotfix channel.
 */
class ContentManagerHotfixTest extends BaseHandlerTestCase {

    private static final String CP_DEV_HOTFIX = "cp-dev-hotfix";
    private static final String CP_TEST_HOTFIX = "cp-test-hotfix";
    private static final String CP_PROD_HOTFIX = "cp-prod-hotfix";
    private static final String CP2_DEV_HOTFIX = "cp2-dev-hotfix";
    private static final String CPOTHER_DEV_HOTFIX = "cpother-dev-hotfix";
    public static final String DEV = "dev";
    public static final String CP_PROJECT_LABEL = "cp";

    private final ContentManager contentManager = new ContentManager();

    private Package pkgA, pkgB, pkgC;
    private Channel source;
    private Errata errata, other;

    /**
     * Builds the setup described in the class documentation.
     *
     * @throws Exception if anything goes wrong
     */
    @BeforeEach
    void setUp() throws Exception {
        contentManager.setModulemdApi(new MockModulemdApi());

        pkgA = PackageTest.createTestPackage(admin.getOrg(), "pkgA");
        pkgB = PackageTest.createTestPackage(admin.getOrg(), "pkgB");
        pkgC = PackageTest.createTestPackage(admin.getOrg(), "pkgC");
        Package pkgS = PackageTest.createTestPackage(admin.getOrg(), "pkgS");
        source = createChannelWithPackages(admin, pkgA, pkgC, pkgS);
        errata = new ErrataTestBuilder().addPackages(pkgA, pkgB).addChannel(source).buildAndSave();
        other = new ErrataTestBuilder().addPackages(pkgC).addChannel(source).buildAndSave();

        buildProjectWithEnvironments(contentManager, admin, CP_PROJECT_LABEL, source, DEV, "test", "prod");
        buildProjectWithEnvironments(contentManager, admin, "cp2", source, DEV);
        buildProjectWithEnvironments(contentManager, admin, "cpother",
                createChannelWithPackages(admin, PackageTest.createTestPackage(admin.getOrg(), "cpother-pkg")), DEV);
    }

    /**
     * Tests cloning into one hotfix channel. Initially it has no erratas. After executing
     * {@link ContentManager#cloneIntoHotfixChannel}, expects the channel to hold a clone of
     * {@code errata} and serving all its packages.
     * The hotfix channels of the other environments and of the other projects get nothing.
     * Repeating the call changes nothing.
     */
    @Test
    void testCloneIntoHotfixChannel() {
        Org org = admin.getOrg();
        assertTrue(lookupChannel(org, CP_DEV_HOTFIX).getErratas().isEmpty());

        SyncedErratumWithPackages syncedErratumWithPackages =
                contentManager.cloneIntoHotfixChannel(admin, errata.getAdvisoryName(), CP_DEV_HOTFIX);

        Errata clone = getCloneOf(org, errata);
        assertEquals(clone.getId(), syncedErratumWithPackages.errata().getId());
        assertEquals(ids(List.of(pkgA, pkgB)), ids(syncedErratumWithPackages.packages()));

        assertChannelHasErratasAndPackages(org, CP_DEV_HOTFIX, Set.of(clone.getId()), ids(List.of(pkgA, pkgB)));
        assertTrue(lookupChannel(org, CP_TEST_HOTFIX).getErratas().isEmpty());
        assertTrue(lookupChannel(org, CP2_DEV_HOTFIX).getErratas().isEmpty());

        // idempotency check
        assertEquals(syncedErratumWithPackages,
                contentManager.cloneIntoHotfixChannel(admin, errata.getAdvisoryName(), CP_DEV_HOTFIX));
        assertEquals(clone.getId(), getCloneOf(org, errata).getId());
        assertChannelHasErratasAndPackages(org, CP_DEV_HOTFIX, Set.of(clone.getId()), ids(List.of(pkgA, pkgB)));
    }

    /**
     * Tests cloning into hotfix channels of several environments and projects at once: each holds the same clone
     * of {@code errata}, a label given twice is cloned into once, the hotfix channels not given are not changed.
     */
    @Test
    void testCloneIntoHotfixChannels() {
        Org org = admin.getOrg();
        Set<String> hotfixLabels = Set.of(CP_DEV_HOTFIX, CP_PROD_HOTFIX, CP2_DEV_HOTFIX);
        hotfixLabels.forEach(label -> assertTrue(lookupChannel(org, label).getErratas().isEmpty()));

        Map<String, SyncedErratumWithPackages> syncedErratumWithPackages = contentManager.cloneIntoHotfixChannels(admin,
                errata.getAdvisoryName(), List.of(CP_PROD_HOTFIX, CP2_DEV_HOTFIX, CP_DEV_HOTFIX, CP_DEV_HOTFIX));

        assertEquals(hotfixLabels, syncedErratumWithPackages.keySet());
        Errata clone = getCloneOf(org, errata);
        for (String label : hotfixLabels) {
            assertEquals(clone.getId(), syncedErratumWithPackages.get(label).errata().getId());
            assertChannelHasErratasAndPackages(org, label, Set.of(clone.getId()), ids(List.of(pkgA, pkgB)));
        }
        assertTrue(lookupChannel(org, CP_TEST_HOTFIX).getErratas().isEmpty());
    }

    /**
     * Tests cloning errata one after the other: a hotfix channel keeps what it already holds, and a hotfix
     * channel does not get the errata of the hotfix channel of the previous environment it was cloned from.
     */
    @Test
    void testCloneKeepsHotfixContent() {
        Org org = admin.getOrg();

        contentManager.cloneIntoHotfixChannel(admin, other.getAdvisoryName(), CP_DEV_HOTFIX);
        contentManager.cloneIntoHotfixChannel(admin, errata.getAdvisoryName(), CP_DEV_HOTFIX);
        contentManager.cloneIntoHotfixChannel(admin, errata.getAdvisoryName(), CP_TEST_HOTFIX);

        Long cloneOfErrata = getCloneOf(org, errata).getId();
        Long cloneOfOther = getCloneOf(org, other).getId();
        assertChannelHasErratasAndPackages(org, CP_DEV_HOTFIX, Set.of(cloneOfErrata, cloneOfOther),
                ids(List.of(pkgA, pkgB, pkgC)));
        assertChannelHasErratasAndPackages(org, CP_TEST_HOTFIX, Set.of(cloneOfErrata), ids(List.of(pkgA, pkgB)));
    }

    /**
     * Tests that invalid requests are rejected, without cloning {@code errata} into the valid hotfix channel of
     * the request.
     */
    @Test
    void testCloneIntoInvalidChannels() {
        String advisoryName = "does-not-exist";
        String regularChannel =
                getRegularChannels(ContentManager.lookupEnvironment(DEV, CP_PROJECT_LABEL, admin).orElseThrow())
                        .iterator().next().getLabel();
        String unrelated = new ErrataTestBuilder().addPackages(pkgA)
                .addChannel(createChannelWithPackages(admin, pkgA)).buildAndSave().getAdvisoryName();
        String advisory = errata.getAdvisoryName();

        // no hotfix channel labels
        assertThrows(InvalidParameterException.class, () ->
                contentManager.cloneIntoHotfixChannels(admin, advisory, List.of()));
        // not hotfix channel
        assertThrows(InvalidChannelException.class, () ->
                contentManager.cloneIntoHotfixChannel(admin, advisory, regularChannel));
        assertThrows(InvalidChannelException.class, () ->
                contentManager.cloneIntoHotfixChannel(admin, advisory, source.getLabel()));
        // source doesn't have the erratum
        assertThrows(InvalidChannelException.class, () ->
                contentManager.cloneIntoHotfixChannels(admin, advisory, List.of(CPOTHER_DEV_HOTFIX, CP_DEV_HOTFIX)));
        assertThrows(InvalidChannelException.class, () ->
                contentManager.cloneIntoHotfixChannels(admin, advisory, List.of(CP_DEV_HOTFIX, CPOTHER_DEV_HOTFIX)));
        // channel does not exist
        assertThrows(NoSuchChannelException.class, () ->
                contentManager.cloneIntoHotfixChannels(admin, advisory, List.of(CP_DEV_HOTFIX, advisoryName)));
        // erratum is in no project source
        assertThrows(InvalidChannelException.class, () ->
                contentManager.cloneIntoHotfixChannel(admin, unrelated, CP_DEV_HOTFIX));
        // advisory does not exist
        assertThrows(EntityNotExistsException.class, () ->
                contentManager.cloneIntoHotfixChannel(admin, advisoryName, CP_DEV_HOTFIX));
        // user is not a channel administrator
        assertThrows(PermissionCheckFailureException.class, () ->
                contentManager.cloneIntoHotfixChannel(regular, advisory, CP_DEV_HOTFIX));

        assertTrue(lookupChannel(admin.getOrg(), CP_DEV_HOTFIX).getErratas().isEmpty());
    }

    /**
     * Tests organizations are isolated: another organization with a project using a channel holding
     * {@code errata} gets its own clone of {@code errata} in its own hotfix channel, the hotfix channel of the
     * first organization gets nothing, and neither organization can clone into the hotfix channels of the other.
     */
    @Test
    void testCloneIntoHotfixChannelOfAnotherOrg() {
        String hotfixLabel = "o2-dev-hotfix";
        User otherAdmin = new UserTestUtils.UserBuilder().orgAdmin(true).build();
        Org otherOrg = otherAdmin.getOrg();
        otherOrg.addRole(RoleFactory.CHANNEL_ADMIN);
        Channel otherSource = createChannelWithPackages(otherAdmin, pkgA);
        otherSource.addErrata(errata);
        buildProjectWithEnvironments(contentManager, otherAdmin, "o2", otherSource, DEV);
        String advisory = errata.getAdvisoryName();

        assertThrows(NoSuchChannelException.class, () ->
                contentManager.cloneIntoHotfixChannel(admin, advisory, hotfixLabel));
        assertThrows(NoSuchChannelException.class, () ->
                contentManager.cloneIntoHotfixChannel(otherAdmin, advisory, CP_DEV_HOTFIX));
        assertTrue(lookupChannel(otherOrg, hotfixLabel).getErratas().isEmpty());

        SyncedErratumWithPackages synced = contentManager.cloneIntoHotfixChannel(otherAdmin, advisory, hotfixLabel);

        Errata clone = getCloneOf(otherOrg, errata);
        assertEquals(clone.getId(), synced.errata().getId());
        assertEquals(ids(List.of(pkgA, pkgB)), ids(synced.packages()));
        assertChannelHasErratasAndPackages(otherOrg, hotfixLabel, Set.of(clone.getId()), ids(List.of(pkgA, pkgB)));
        assertTrue(lookupChannel(admin.getOrg(), CP_DEV_HOTFIX).getErratas().isEmpty());
    }

    /**
     * Provides the environments of project "cpchild" an erratum is cloned into.
     *
     * @return the environment labels
     */
    private static Stream<Arguments> hotfixEnvironments() {
        return Stream.of(
                Arguments.of(List.of(DEV)),
                Arguments.of(List.of("test")),
                Arguments.of(List.of(DEV, "prod")),
                Arguments.of(List.of(DEV, "test", "prod")));
    }

    /**
     * Tests an erratum whose packages are spread over two child channels of the project base channel. The base
     * channel and both child channels hold an initial package and erratum, and the project, with the environments
     * dev, test and prod, is built with them. Afterwards the source child channels get a new erratum: pkg1 and pkg2
     * are served by the first child, pkg3 by the second one. After cloning it into the hotfix channels of the given
     * environments, only these hold the clone and all three packages. The hotfix channels of the other
     * environments get nothing, and the cloned channels of every environment keep their initial content.
     *
     * @param targetEnvLabels the labels of the environments whose hotfix channel the erratum is cloned into
     * @throws Exception if anything goes wrong
     */
    @ParameterizedTest(name = "clone into {0}")
    @MethodSource("hotfixEnvironments")
    void testCloneErratumSpreadOverChildChannels(List<String> targetEnvLabels) throws Exception {
        Org org = admin.getOrg();
        List<String> envLabels = List.of(DEV, "test", "prod");
        SpreadProject project = buildSpreadProject("cpchild", envLabels.toArray(String[]::new));
        Function<String, String> hotfixLabel = envLabel -> "cpchild-" + envLabel + "-hotfix";
        List<String> targetLabels = targetEnvLabels.stream().map(hotfixLabel).toList();

        Map<String, SyncedErratumWithPackages> synced = contentManager.cloneIntoHotfixChannels(admin,
                project.spread().getAdvisoryName(), targetLabels);

        Errata clone = getCloneOf(org, project.spread());
        assertEquals(Set.copyOf(targetLabels), synced.keySet());
        synced.values().forEach(s -> {
            assertEquals(clone.getId(), s.errata().getId());
            assertEquals(project.spreadPackageIds(), ids(s.packages()));
        });
        assertEquals(Set.copyOf(targetLabels), clone.getChannels().stream().map(Channel::getLabel).collect(toSet()));
        for (String envLabel : envLabels) {
            if (targetEnvLabels.contains(envLabel)) {
                assertChannelHasErratasAndPackages(org, hotfixLabel.apply(envLabel), Set.of(clone.getId()),
                        project.spreadPackageIds());
            }
            else {
                assertChannelHasErratasAndPackages(org, hotfixLabel.apply(envLabel), Set.of(), Set.of());
            }
            assertClonedChannelsHoldInitialContent(org, "cpchild", envLabel, project);
        }
    }

    /**
     * Tests the cleanup of hotfix channels: an erratum cloned into the hotfix channels leaves the hotfix channel of
     * an environment once the regular channels of that environment get it, by build or promote. Three errata are
     * added to the sources of a built and promoted project with dev, test and prod: one spread over both child
     * channels, one with packages only in the base channel, one with packages only in a child channel. They are
     * cloned into all hotfix channels, then the project is built and promoted environment by environment.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testHotfixChannelCleanupOnBuildAndPromote() throws Exception {
        Org org = admin.getOrg();
        List<String> envLabels = List.of(DEV, "test", "prod");
        SpreadProject project = buildSpreadProject("cpchild", envLabels.toArray(String[]::new));
        Package basePkg = PackageTest.createTestPackage(org, "baseOnly");
        Package childPkg = PackageTest.createTestPackage(org, "childOnly");
        project.base().addPackages(List.of(basePkg));
        project.child1().addPackages(List.of(childPkg));
        Errata baseOnly = new ErrataTestBuilder().addPackages(basePkg).addChannel(project.base()).buildAndSave();
        Errata childOnly = new ErrataTestBuilder().addPackages(childPkg).addChannel(project.child1()).buildAndSave();
        List<Errata> newErrata = List.of(project.spread(), baseOnly, childOnly);
        Set<Long> newPackageIds = new HashSet<>(project.spreadPackageIds());
        newPackageIds.addAll(ids(List.of(basePkg, childPkg)));

        for (Errata erratum : newErrata) {
            contentManager.cloneIntoHotfixChannels(admin, erratum.getAdvisoryName(),
                    envLabels.stream().map(ContentManagerHotfixTest::cpchildHotfixLabel).toList());
        }
        Set<Long> cloneIds = newErrata.stream().map(e -> getCloneOf(org, e).getId()).collect(toSet());
        assertEquals(3, cloneIds.size());
        for (String envLabel : envLabels) {
            assertChannelHasErratasAndPackages(org, cpchildHotfixLabel(envLabel), cloneIds, newPackageIds);
        }

        // build: the first environment gets the errata, its hotfix channel is cleaned up, the others are not
        contentManager.buildProject("cpchild", empty(), false, admin);
        assertRegularChannelsHold(org, DEV, cloneIds, newPackageIds);
        assertRegularChannelsHold(org, "test", Set.of(), Set.of());
        assertHotfixChannelsHold(org, Map.of(DEV, false, "test", true, "prod", true), cloneIds, newPackageIds);

        // promote dev
        contentManager.promoteProject("cpchild", DEV, false, admin);
        assertRegularChannelsHold(org, "test", cloneIds, newPackageIds);
        assertRegularChannelsHold(org, "prod", Set.of(), Set.of());
        assertHotfixChannelsHold(org, Map.of(DEV, false, "test", false, "prod", true), cloneIds, newPackageIds);

        // promote test
        contentManager.promoteProject("cpchild", "test", false, admin);
        assertRegularChannelsHold(org, "prod", cloneIds, newPackageIds);
        assertHotfixChannelsHold(org, Map.of(DEV, false, "test", false, "prod", false), cloneIds, newPackageIds);
    }

    /**
     * Tests the cleanup of the hotfix channel with a cloned channel as project source: the source holds a clone
     * of a vendor erratum, owned by the organization. Cloning it into the hotfix channel uses that clone itself,
     * and so does building. After the build the clone left the hotfix channel with its packages, but it still
     * exists and is still in the source and in the regular channel.
     */
    @Test
    public void testHotfixChannelCleanupWithClonedSource() {
        Org org = admin.getOrg();
        Package initialPkg = PackageTest.createTestPackage(org, "initialCloned");
        Channel original = createChannelWithPackages(admin, initialPkg);
        Channel clonedSource = ChannelFactoryTest.createTestClonedChannel(original, admin);
        clonedSource.setChecksumType(ChannelFactory.findChecksumTypeByLabel("sha1"));
        clonedSource.addPackages(List.of(initialPkg));
        ChannelFactory.save(clonedSource);
        buildProjectWithEnvironments(contentManager, admin, "cpcloned", clonedSource, DEV);
        String hotfixLabel = "cpcloned-dev-hotfix";

        // a vendor erratum arrives in the original channel and its clone in the cloned source
        Package pkg = PackageTest.createTestPackage(org, "clonedSourcePkg");
        original.addPackages(List.of(pkg));
        Errata vendor = new ErrataTestBuilder().addPackages(pkg).addChannel(original).buildAndSave();
        Errata clone = ErrataFactory.lookupById(ErrataHelper.cloneErrataFaster(vendor.getId(), org));
        clonedSource.addPackages(List.of(pkg));
        clonedSource.addErrata(clone);
        ChannelFactory.save(clonedSource);

        // cloning into the hotfix channel uses the clone itself
        SyncedErratumWithPackages synced =
                contentManager.cloneIntoHotfixChannel(admin, clone.getAdvisoryName(), hotfixLabel);
        assertEquals(clone.getId(), synced.errata().getId());
        assertTrue(ErrataFactory.lookupErrataByOriginal(org, clone).isEmpty(), "no clone of the clone");
        assertChannelHasErratasAndPackages(org, hotfixLabel, Set.of(clone.getId()), ids(List.of(pkg)));

        contentManager.buildProject("cpcloned", empty(), false, admin);

        assertChannelHasErratasAndPackages(org, hotfixLabel, Set.of(), Set.of());
        Channel regular = lookupSingleRegularChannel(org, "cpcloned", DEV);
        assertEquals(Set.of(clone.getId()), ids(regular.getErratas(), Errata::getId), "regular errata");
        assertEquals(ids(List.of(initialPkg, pkg)), ids(regular.getPackages(), Package::getId), "regular packages");
        assertTrue(ids(lookupChannel(org, clonedSource.getLabel()).getErratas(), Errata::getId)
                .contains(clone.getId()), "the clone stays in the source");
        assertEquals(List.of(clone.getId()),
                ErrataFactory.lookupErrataByOriginal(org, vendor).stream().map(Errata::getId).toList());
    }

    /**
     * Tests the cleanup of the hotfix channel when a build brings only one of two errata in the hotfix channel to
     * the regular channel: a filter denies the other one. Only the built erratum leaves the hotfix channel, the
     * denied one stays there with its packages, including a package both errata declare.
     */
    @Test
    public void testHotfixChannelCleanupKeepsFilteredErrata() {
        Org org = admin.getOrg();
        Channel filteredSource = createChannelWithPackages(admin, PackageTest.createTestPackage(org, "initialFlt"));
        buildProjectWithEnvironments(contentManager, admin, "cpfilter", filteredSource, DEV);
        String hotfixLabel = "cpfilter-dev-hotfix";

        Package builtPkg = PackageTest.createTestPackage(org, "builtPkg");
        Package deniedPkg = PackageTest.createTestPackage(org, "deniedPkg");
        Package sharedPkg = PackageTest.createTestPackage(org, "sharedPkg");
        filteredSource.addPackages(List.of(builtPkg, deniedPkg, sharedPkg));
        Errata built = new ErrataTestBuilder().addPackages(builtPkg, sharedPkg).addChannel(filteredSource)
                .buildAndSave();
        Errata denied = new ErrataTestBuilder().addPackages(deniedPkg, sharedPkg).addChannel(filteredSource)
                .buildAndSave();
        contentManager.cloneIntoHotfixChannel(admin, built.getAdvisoryName(), hotfixLabel);
        contentManager.cloneIntoHotfixChannel(admin, denied.getAdvisoryName(), hotfixLabel);
        Long builtClone = getCloneOf(org, built).getId();
        Long deniedClone = getCloneOf(org, denied).getId();
        assertChannelHasErratasAndPackages(org, hotfixLabel, Set.of(builtClone, deniedClone),
                ids(List.of(builtPkg, deniedPkg, sharedPkg)));

        FilterCriteria criteria = new FilterCriteria(Matcher.EQUALS, "advisory_name", denied.getAdvisoryName());
        ContentFilter filter = contentManager.createFilter("deny-hotfix", Rule.DENY, EntityType.ERRATUM, criteria,
                admin);
        contentManager.attachFilter("cpfilter", filter.getId(), admin);
        contentManager.buildProject("cpfilter", empty(), false, admin);

        assertChannelHasErratasAndPackages(org, hotfixLabel, Set.of(deniedClone), ids(List.of(deniedPkg, sharedPkg)));
        Channel regular = lookupSingleRegularChannel(org, "cpfilter", DEV);
        Set<Long> regularErrata = ids(regular.getErratas(), Errata::getId);
        Set<Long> regularPackages = ids(regular.getPackages(), Package::getId);
        assertTrue(regularErrata.contains(builtClone), "regular channel holds the built erratum");
        assertFalse(regularErrata.contains(deniedClone), "regular channel does not hold the denied erratum");
        assertTrue(regularPackages.containsAll(ids(List.of(builtPkg, sharedPkg))), "regular built packages");
        assertFalse(regularPackages.contains(deniedPkg.getId()), "regular channel does not hold the denied package");
    }

    /**
     * Tests the cleanup of the hotfix channel with a system subscribed to the regular and the hotfix channel: the
     * cleanup drops the needed cache entries of the system, so the system gets queued for recomputing its cache,
     * which then holds the update from the regular channel.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    void testHotfixChannelCleanupRefreshesNeededCache() throws Exception {
        Org org = admin.getOrg();
        Channel cacheSource = createChannelWithPackages(admin, PackageTest.createTestPackage(org, "initialCache"));
        buildProjectWithEnvironments(contentManager, admin, "cpcache", cacheSource, DEV);
        String hotfixLabel = "cpcache-dev-hotfix";

        Package pkg = PackageTest.createTestPackage(org, "cachePkg");
        cacheSource.addPackages(List.of(pkg));
        Errata erratum = new ErrataTestBuilder().addPackages(pkg).addChannel(cacheSource).buildAndSave();
        contentManager.cloneIntoHotfixChannel(admin, erratum.getAdvisoryName(), hotfixLabel);

        // the system has an older version of the package installed
        String regularLabel = lookupSingleRegularChannel(org, "cpcache", DEV).getLabel();
        Server server = ErrataTestUtils.createTestServer(admin, List.of(
                ChannelFactory.lookupByLabel(org, regularLabel), ChannelFactory.lookupByLabel(org, hotfixLabel)));
        Package reloadedPkg = TestUtils.reload(pkg);
        PackageEvr evr = reloadedPkg.getPackageEvr();
        InstalledPackage installed = new InstalledPackage();
        installed.setEvr(PackageEvrFactoryTest.createTestPackageEvr(evr.getEpoch(), "0.9.9", evr.getRelease(),
                evr.getPackageType()));
        installed.setArch(reloadedPkg.getPackageArch());
        installed.setName(reloadedPkg.getPackageName());
        installed.setServer(server);
        server.getPackages().add(installed);
        TaskFactory.deleteByNameData(ErrataCacheWorker.FOR_SERVER, server.getId());

        contentManager.buildProject("cpcache", empty(), false, admin);

        assertNotNull(TaskFactory.lookup(org, ErrataCacheWorker.FOR_SERVER, server.getId()), "system is queued");
        new UpdateErrataCacheCommand().updateErrataCacheForServer(server.getId(), false);
        Set<Long> regularErrata =
                ids(lookupSingleRegularChannel(org, "cpcache", DEV).getErratas(), Errata::getId);
        List<ErrataCacheDto> needed = ErrataCacheManager.packagesNeedingUpdates(server.getId());
        assertEquals(List.of(pkg.getId()), needed.stream().map(ErrataCacheDto::getPackageId).toList());
        assertTrue(regularErrata.contains(needed.get(0).getErrataId()), "update from the regular channel");
    }

    // Utility methods

    /**
     * Returns a channel of an organization, read from the database.
     *
     * @param org the organization owning the channel
     * @param label the channel label
     * @return the channel
     */
    private static Channel lookupChannel(Org org, String label) {
        TestUtils.flushAndClearSession();
        return ChannelFactory.lookupByLabel(org, label);
    }

    /**
     * Asserts channel holds given erratas and packages.
     *
     * @param org the organization owning the channel
     * @param label the channel label
     * @param errataIds the ids of the expected errata
     * @param packageIds the ids of the expected packages
     */
    private static void assertChannelHasErratasAndPackages(
            Org org,
            String label,
            Set<Long> errataIds,
            Set<Long> packageIds
    ) {
        Channel channel = lookupChannel(org, label);
        assertEquals(errataIds, ids(channel.getErratas(), Errata::getId), label + " errata");
        assertEquals(packageIds, ids(channel.getPackages(), Package::getId), label + " packages");
    }

    /**
     * Returns the ids of entities.
     *
     * @param entities the entities
     * @param id the id getter
     * @param <T> the entity type
     * @return the ids
     */
    private static <T> Set<Long> ids(Collection<T> entities, Function<T, Long> id) {
        return entities.stream().map(id).collect(toSet());
    }

    /**
     * Returns the ids of packages.
     *
     * @param packages the packages
     * @return the ids
     */
    private static Set<Long> ids(List<Package> packages) {
        return ids(packages, Package::getId);
    }

    /**
     * Returns the clone of an erratum owned by an organization, asserting there is exactly one.
     *
     * @param org the organization owning the clone
     * @param original the original erratum
     * @return the clone
     */
    private Errata getCloneOf(Org org, Errata original) {
        List<Errata> clones = ErrataFactory.lookupErrataByOriginal(org, original);
        assertEquals(1, clones.size(), "clones of " + original.getAdvisoryName());
        return clones.get(0);
    }

    /**
     * Creates a channel of the organization of the user, serving the given packages.
     *
     * @param user the user, whose organization owns the channel
     * @param packages the packages the channel serves
     * @return the channel
     */
    private Channel createChannelWithPackages(User user, Package... packages) {
        Channel channel = TestUtils.reload(ChannelFactoryTest.createTestChannel(user, false));
        channel.setChecksumType(ChannelFactory.findChecksumTypeByLabel("sha1"));
        channel.addPackages(List.of(packages));
        return ChannelFactory.save(channel);
    }

    /**
     * Creates a child channel of the organization of the user, serving the given packages.
     *
     * @param user the user, whose organization owns the channel
     * @param parent the parent channel
     * @param packages the packages the channel serves
     * @return the channel
     */
    private Channel createChildChannelWithPackages(User user, Channel parent, Package... packages)
            throws Exception {
        Channel channel = TestUtils.reload(ChannelTestUtils.createChildChannel(user, parent));
        channel.addPackages(List.of(packages));
        return ChannelFactory.save(channel);
    }

    /**
     * A project with a base channel and two child channels as sources, and an erratum added to the sources after
     * the project was built.
     *
     * @param base the base source channel
     * @param child1 the first child source channel
     * @param child2 the second child source channel
     * @param spread the erratum, declaring three packages: two served by the first child, one by the second
     * @param spreadPackageIds the ids of the packages of the erratum
     * @param initialPackagesBySource the package every source channel held when the project was built, by channel id
     */
    private record SpreadProject(Channel base, Channel child1, Channel child2, Errata spread,
                                 Set<Long> spreadPackageIds,
                                 Map<Long, Package> initialPackagesBySource) { }

    /**
     * Builds a project from a base channel and two child channels, each with an initial package and erratum, then
     * adds the spread erratum to the source child channels.
     *
     * @param projectLabel the project label
     * @param envLabels the environment labels, from the first to the last
     * @return the project and the spread erratum
     * @throws Exception if anything goes wrong
     */
    private SpreadProject buildSpreadProject(String projectLabel, String... envLabels) throws Exception {
        Org org = admin.getOrg();
        Package initialBasePkg = PackageTest.createTestPackage(org, "initialBase");
        Package initialChild1Pkg = PackageTest.createTestPackage(org, "initialChild1");
        Package initialChild2Pkg = PackageTest.createTestPackage(org, "initialChild2");
        Channel base = createChannelWithPackages(admin, initialBasePkg);
        Channel child1 = createChildChannelWithPackages(admin, base, initialChild1Pkg);
        Channel child2 = createChildChannelWithPackages(admin, base, initialChild2Pkg);
        new ErrataTestBuilder().addPackages(initialBasePkg).addChannel(base).buildAndSave();
        new ErrataTestBuilder().addPackages(initialChild1Pkg).addChannel(child1).buildAndSave();
        new ErrataTestBuilder().addPackages(initialChild2Pkg).addChannel(child2).buildAndSave();
        buildProjectWithEnvironments(contentManager, admin, projectLabel, List.of(base, child1, child2), envLabels);

        // the new erratum arrives in the source channels after the build
        Package pkg1 = PackageTest.createTestPackage(org, "spread1");
        Package pkg2 = PackageTest.createTestPackage(org, "spread2");
        Package pkg3 = PackageTest.createTestPackage(org, "spread3");
        child1.addPackages(List.of(pkg1, pkg2));
        child2.addPackages(List.of(pkg3));
        Errata spread = new ErrataTestBuilder().addPackages(pkg1, pkg2, pkg3)
                .addChannel(child1).addChannel(child2).buildAndSave();

        return new SpreadProject(base, child1, child2, spread, ids(List.of(pkg1, pkg2, pkg3)), Map.of(
                base.getId(), initialBasePkg,
                child1.getId(), initialChild1Pkg,
                child2.getId(), initialChild2Pkg));
    }

    private Channel lookupSingleRegularChannel(Org org, String projectLabel, String envLabel) {
        Channel regular = getRegularChannels(ContentManager.lookupEnvironment(envLabel, projectLabel, admin)
                .orElseThrow()).iterator().next();
        return lookupChannel(org, regular.getLabel());
    }

    private static String cpchildHotfixLabel(String envLabel) {
        return "cpchild-" + envLabel + "-hotfix";
    }

    /**
     * Asserts the regular (non hotfix) channels of an environment of project "cpchild" hold the given errata and
     * packages, or none of them when the given sets are empty. The initial content is ignored.
     *
     * @param org the organization owning the project
     * @param envLabel the environment label
     * @param errataIds the ids of the errata, empty to expect none of the errata in play
     * @param packageIds the ids of the packages
     */
    private void assertRegularChannelsHold(Org org, String envLabel, Set<Long> errataIds, Set<Long> packageIds) {
        Set<Long> channelErrataIds = new HashSet<>();
        Set<Long> channelPackageIds = new HashSet<>();
        for (Channel regular : getRegularChannels(
                ContentManager.lookupEnvironment(envLabel, "cpchild", admin).orElseThrow())) {
            Channel channel = lookupChannel(org, regular.getLabel());
            channelErrataIds.addAll(ids(channel.getErratas(), Errata::getId));
            channelPackageIds.addAll(ids(channel.getPackages(), Package::getId));
        }
        if (errataIds.isEmpty()) {
            assertTrue(channelPackageIds.stream().noneMatch(packageIds::contains), envLabel + " has no new packages");
            return;
        }
        assertTrue(channelErrataIds.containsAll(errataIds), envLabel + " regular channels hold the errata");
        assertTrue(channelPackageIds.containsAll(packageIds), envLabel + " regular channels hold the packages");
    }

    /**
     * Asserts which hotfix channels of project "cpchild" still hold the given errata and packages: all of them,
     * or nothing.
     *
     * @param org the organization owning the project
     * @param holdsContent for each environment label, whether its hotfix channel holds the content
     * @param errataIds the ids of the errata
     * @param packageIds the ids of the packages
     */
    private static void assertHotfixChannelsHold(Org org, Map<String, Boolean> holdsContent, Set<Long> errataIds,
                                                 Set<Long> packageIds) {
        holdsContent.forEach((envLabel, holds) -> assertChannelHasErratasAndPackages(org,
                cpchildHotfixLabel(envLabel), holds ? errataIds : Set.of(), holds ? packageIds : Set.of()));
    }

    /**
     * Asserts the cloned source channels of an environment hold only their initial package and one erratum.
     *
     * @param org the organization owning the project
     * @param projectLabel the project label
     * @param envLabel the environment label
     * @param project the project
     */
    private void assertClonedChannelsHoldInitialContent(Org org, String projectLabel, String envLabel,
                                                        SpreadProject project) {
        Set<Channel> clonedChannels =
                getRegularChannels(ContentManager.lookupEnvironment(envLabel, projectLabel, admin).orElseThrow());
        assertEquals(3, clonedChannels.size(), envLabel + " channels");
        for (Channel cloned : clonedChannels) {
            Channel reloaded = lookupChannel(org, cloned.getLabel());
            Channel root = reloaded.originChain().reduce((first, last) -> last).orElseThrow();
            assertEquals(Set.of(project.initialPackagesBySource().get(root.getId()).getId()),
                    ids(reloaded.getPackages(), Package::getId), reloaded.getLabel() + " packages");
            assertEquals(1, reloaded.getErratas().size(), reloaded.getLabel() + " errata");
        }
    }

}
