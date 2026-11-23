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
import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFactoryTest;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.errata.ErrataTestBuilder;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageTest;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandlerTestCase;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelException;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchChannelException;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.manager.EntityNotExistsException;
import com.redhat.rhn.manager.errata.syncwithpackages.dto.SyncedErratumWithPackages;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collection;
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
public class ContentManagerHotfixTest extends BaseHandlerTestCase {

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
    public void setUp() throws Exception {
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
    public void testCloneIntoHotfixChannel() {
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
    public void testCloneIntoHotfixChannels() {
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
    public void testCloneKeepsHotfixContent() {
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
    public void testCloneIntoInvalidChannels() {
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
    public void testCloneIntoHotfixChannelOfAnotherOrg() {
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
    public void testCloneErratumSpreadOverChildChannels(List<String> targetEnvLabels) throws Exception {
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
     * @param spread the erratum, declaring three packages: two served by the first child, one by the second
     * @param spreadPackageIds the ids of the packages of the erratum
     * @param initialPackagesBySource the package every source channel held when the project was built, by channel id
     */
    private record SpreadProject(Errata spread, Set<Long> spreadPackageIds,
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

        return new SpreadProject(spread, ids(List.of(pkg1, pkg2, pkg3)), Map.of(
                base.getId(), initialBasePkg,
                child1.getId(), initialChild1Pkg,
                child2.getId(), initialChild2Pkg));
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
