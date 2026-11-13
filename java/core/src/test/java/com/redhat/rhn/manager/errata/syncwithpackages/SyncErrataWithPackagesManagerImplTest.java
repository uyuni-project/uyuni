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
package com.redhat.rhn.manager.errata.syncwithpackages;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFactoryTest;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.errata.ErrataTestBuilder;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageTest;
import com.redhat.rhn.frontend.action.channel.manage.ErrataHelper;
import com.redhat.rhn.frontend.events.SyncErrataWithPackagesAction;
import com.redhat.rhn.frontend.events.SyncErrataWithPackagesEvent;
import com.redhat.rhn.frontend.xmlrpc.BaseHandlerTestCase;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.manager.errata.AsyncErrataCloneCounter;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.errata.syncwithpackages.dto.SyncErrataWithPackagesResult;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Tests for {@link SyncErrataWithPackagesManagerImpl}.
 *
 * Tests have a common setup, then only adds what its case needs:
 * - Channel {@code source} serves packages a1, b1, c1 and {@code unlisted}, and holds errata:
 *   - A, a vendor erratum declaring a1 and a2 (but a2 is not served by the source),
 *   - B, a vendor erratum declaring a1 and b1 (a1 is shared with A),
 *   - C, an erratum of the org declaring c1.
 * - {@code unlisted} is served by the {@code source} but declared by no erratum.
 * - {@code middle} is an empty clone of the {@code source}, and {@code target} an empty clone of {@code middle}.
 *   Channel clone chain is: source -> middle -> target
 * - {@code target} already holds erratum X of the org, declaring and serving package X.
 *
 * Each test asserts the complete content of the target channel, so anything synchronized beyond what was expected
 * makes it fail.
 */
public class SyncErrataWithPackagesManagerImplTest extends BaseHandlerTestCase {

    private final SyncErrataWithPackagesManager manager = new SyncErrataWithPackagesManagerImpl();

    private Package a1, a2, b1, c1, unlisted, x;
    private Channel source, middle, target;
    private Errata errataA, errataB, errataC, errataX;

    /**
     * Builds the setup described in the class documentation.
     */
    @BeforeEach
    public void setUp() {
        a1 = createPackage("a1");
        a2 = createPackage("a2");
        b1 = createPackage("b1");
        c1 = createPackage("c1");
        unlisted = createPackage("unlisted");
        x = createPackage("x");

        source = createChannelWithPackages("source", a1, b1, c1, unlisted);
        middle = ChannelFactoryTest.createTestClonedChannel(source, admin);
        target = ChannelFactoryTest.createTestClonedChannel(middle, admin);
        target.addPackages(List.of(x));
        target = ChannelFactory.save(target);

        errataA = createErrataWithPackages("errataA", null, source, a1, a2);
        errataB = createErrataWithPackages("errataB", null, source, a1, b1);
        errataC = createErrataWithPackages("errataC", admin.getOrg(), source, c1);
        errataX = createErrataWithPackages("errataX", admin.getOrg(), target, x);
    }

    /**
     * Tests syncFromSource by advisory names with one erratum in each situation that decides its representative:
     * - A, vendor, with no clone anywhere: a new clone of A owned by the target channel org, declaring a1 and a2;
     * - B, vendor, with a clone already in the target channel: that clone;
     * - C, of the org, already in the target channel (c1 not served): C itself;
     * - D, of the org, not in the target channel: D itself, linked without cloning;
     * - E, vendor, with two clones of the org, neither in the target channel: the oldest clone;
     * - V', a clone of vendor V held by the source, while the target channel holds V'', a sibling: V''.
     * Expects each representative served with the packages of its erratum that the source serves (a2 is not
     * served), no further clone created, and the target channel holding exactly X and the representatives.
     */
    @Test
    public void testSyncFromSourceWhenAllMatchWithDistinctRepresentatives() {
        // Arrange
        Package d1 = createPackage("d1");
        Package e1 = createPackage("e1");
        Package v1 = createPackage("v1");
        source.addPackages(List.of(d1, e1, v1));
        Errata cloneOfB = createCloneErrataOf(errataB, target);
        target.addErrata(errataC);
        Errata errataD = createErrataWithPackages("errataD", admin.getOrg(), source, d1);
        Errata errataE = createErrataWithPackages("errataE", null, source, e1);
        Errata oldestCloneOfE = createCloneErrataOf(errataE, null);
        createCloneErrataOf(errataE, null);
        Errata vendorV = createErrataWithPackages("vendorV", null, null, v1);
        Errata cloneOfVInSource = createCloneErrataOf(vendorV, source);
        Errata siblingInTarget = createCloneErrataOf(vendorV, target);

        assertEquals(Set.of(errataX, cloneOfB, errataC, siblingInTarget), target.getErratas());
        assertEquals(Set.of(x), target.getPackages());

        // Act
        SyncErrataWithPackagesResult result = manager.syncFromSource(admin, source.getLabel(), target.getLabel(),
                List.of(errataA.getAdvisoryName(), errataB.getAdvisoryName(), errataC.getAdvisoryName(),
                        errataD.getAdvisoryName(), errataE.getAdvisoryName(), cloneOfVInSource.getAdvisoryName()),
                true, false, true);

        // Assert
        Errata cloneOfA = getCloneOf(errataA);
        assertEquals(ids(a1, a2), ids(cloneOfA.getPackages()));
        assertEquals(Map.of(
                cloneOfA.getId(), ids(a1),
                cloneOfB.getId(), ids(a1, b1),
                errataC.getId(), ids(c1),
                errataD.getId(), ids(d1),
                oldestCloneOfE.getId(), ids(e1),
                siblingInTarget.getId(), ids(v1)
        ), servedByRepresentative(result));
        assertTrue(result.unmatchedAdvisoryNames().isEmpty());
        assertEquals(0, result.unmatchedRemaining());

        // no further clone created
        assertEquals(cloneOfB, getCloneOf(errataB));
        assertTrue(ErrataFactory.lookupErrataByOriginal(admin.getOrg(), errataC).isEmpty());
        assertTrue(ErrataFactory.lookupErrataByOriginal(admin.getOrg(), errataD).isEmpty());
        assertEquals(2, ErrataFactory.lookupErrataByOriginal(admin.getOrg(), errataE).size());
        assertEquals(2, ErrataFactory.lookupErrataByOriginal(admin.getOrg(), vendorV).size());

        assertEquals(
                Set.of(errataX, cloneOfA, cloneOfB, errataC, errataD, oldestCloneOfE, siblingInTarget),
                target.getErratas()
        );
        assertEquals(Set.of(x, a1, b1, c1, d1, e1, v1), target.getPackages());
    }

    /**
     * Tests syncFromSource by advisory names when vendor erratum A, not in the target channel, is requested
     * three times. Expects only the first call to have an effect, while subsequent calls should return the same result
     * without causing additional side effects.
     */
    @Test
    public void testSyncFromSourceWhenVendorErratumNotInTargetChannelOrgIsIdempotent() {
        // Arrange
        assertEquals(Set.of(errataX), target.getErratas());
        assertEquals(Set.of(x), target.getPackages());
        List<String> advisoryNames = List.of(errataA.getAdvisoryName());

        // Act
        SyncErrataWithPackagesResult result = manager.syncFromSource(admin, source.getLabel(), target.getLabel(),
                advisoryNames, true, false, true);

        // Assert 1st execution response and effects
        Errata cloneOfA = getCloneOf(errataA);
        assertEquals(Map.of(cloneOfA.getId(), ids(a1)), servedByRepresentative(result));
        assertEquals(Set.of(errataX, cloneOfA), target.getErratas());
        assertEquals(Set.of(x, a1), target.getPackages());

        // Repeated calls
        for (int i = 0; i < 2; i++) {
            SyncErrataWithPackagesResult again = manager.syncFromSource(admin, source.getLabel(), target.getLabel(),
                    advisoryNames, true, false, true);

            assertEquals(result, again);
            assertEquals(cloneOfA.getId(), getCloneOf(errataA).getId());
            assertEquals(Set.of(errataX, cloneOfA), target.getErratas());
            assertEquals(Set.of(x, a1), target.getPackages());
        }
    }

    /**
     * Tests syncFromSource by advisory names when the target channel holds a clone of A.
     * Then, A got package a3 (served by the source) AFTER it was cloned.
     * Expects the existing clone now to serve a1, a2 and a3.
     * The target channel holds exactly X and that clone, and serves exactly x, a1 and a3.
     * Note: mind a2 is excluded because its NOT served by the source channel.
     */
    @Test
    public void testSyncFromSourceWhenCloneInTargetButMissingNewPackages() {
        // Arrange
        Errata cloneOfA = createCloneErrataOf(errataA, target);
        Package a3 = createPackage("a3");
        source.addPackages(List.of(a3));
        errataA.addPackage(a3);
        assertEquals(Set.of(errataX, cloneOfA), target.getErratas());
        assertEquals(Set.of(x), target.getPackages());

        // Act
        SyncErrataWithPackagesResult result = manager.syncFromSource(admin, source.getLabel(), target.getLabel(),
                List.of(errataA.getAdvisoryName()), true, false, true);

        // Assert
        assertEquals(Map.of(cloneOfA.getId(), ids(a1, a3)), servedByRepresentative(result));
        assertEquals(ids(a1, a2, a3), ids(cloneOfA.getPackages()));
        assertEquals(Set.of(errataX, cloneOfA), target.getErratas());
        assertEquals(Set.of(x, a1, a3), target.getPackages());
        assertTrue(result.unmatchedAdvisoryNames().isEmpty());
        assertEquals(0, result.unmatchedRemaining());
    }

    /**
     * Tests syncFromSource by advisory names when the list of advisory names is empty.
     * In this scenario, source channel has a clone of A and serves a1.
     * Expects only the missing errata brought in: the existing clone of A is reused, B is cloned and C is linked.
     * The target channel holds exactly X, the existing clone of A, a clone of B and C, and serves exactly x, a1, b1
     * and c1.
     */
    @Test
    public void testSyncFromSourceWhenNoAdvisoryNamesAreProvided() {
        // Arrange
        Errata cloneOfA = createCloneErrataOf(errataA, target);
        target.addPackages(List.of(a1));
        assertEquals(Set.of(errataX, cloneOfA), target.getErratas());
        assertEquals(Set.of(x, a1), target.getPackages());

        // Act
        SyncErrataWithPackagesResult result = manager.syncFromSource(admin, source.getLabel(), target.getLabel(),
                List.of(), true, false, true);

        // Assert
        assertEquals(cloneOfA.getId(), getCloneOf(errataA).getId());
        Errata cloneOfB = getCloneOf(errataB);
        assertEquals(Map.of(
                cloneOfA.getId(), ids(a1),
                cloneOfB.getId(), ids(a1, b1),
                errataC.getId(), ids(c1)
        ), servedByRepresentative(result));
        assertEquals(Set.of(errataX, cloneOfA, cloneOfB, errataC), target.getErratas());
        assertEquals(Set.of(x, a1, b1, c1), target.getPackages());
    }

    /**
     * Tests syncFromSource by advisory names when the list of advisory names is empty and the source channel holds no
     * erratum. Expects a 2800 fault "No erratas were found" and the target channel unchanged: exactly X and x.
     */
    @Test
    public void testSyncFromSourceWhenSourceChannelHasNoErratas() {
        Channel emptySource = createChannelWithPackages("emptySourceChannel", createPackage("emptySourcePackage"));

        FaultException e = assertThrows(FaultException.class,
                () -> manager.syncFromSource(admin, emptySource.getLabel(), target.getLabel(), List.of(), true,
                        false, true));

        assertEquals(2800, e.getErrorCode());
        assertEquals("No erratas were found", e.getMessage());
        assertEquals(Set.of(errataX), target.getErratas());
        assertEquals(Set.of(x), target.getPackages());
    }

    /**
     * Tests syncFromSource by advisory names in strict mode when C is requested together with an unmatched name.
     * Expects a -208 fault naming the unmatched name, and the target channel unchanged: exactly X and x, without C.
     */
    @Test
    public void testSyncFromSourceWhenStrictAndNameUnmatchedThenFailsSyncingNothing() {
        FaultException e = assertThrows(FaultException.class,
                () -> manager.syncFromSource(admin, source.getLabel(), target.getLabel(),
                        List.of(errataC.getAdvisoryName(), "dummy"), true, false, true));

        assertEquals(-208, e.getErrorCode());
        assertEquals("Patches not found: dummy", e.getMessage());
        assertEquals(Set.of(errataX), target.getErratas());
        assertEquals(Set.of(x), target.getPackages());
    }

    /**
     * Tests syncFromSource by advisory names in strict mode when the number of unmatched names exceeds the limit.
     * Expects a -208 fault listing the first 20 names followed by "and 5 more".
     */
    @Test
    public void testSyncFromSourceWhenStrictAndUnmatchedNamesExceedsLimit() {
        List<String> names = IntStream.range(0, 25).mapToObj(i -> "MISSING-" + i).toList();

        FaultException e = assertThrows(FaultException.class,
                () -> manager.syncFromSource(admin, source.getLabel(), target.getLabel(), names, true, false, true));

        assertEquals(-208, e.getErrorCode());
        assertEquals("Patches not found: " + String.join(", ", names.subList(0, 20)) + " and 5 more",
                e.getMessage());
    }

    /**
     * Tests syncFromSource by advisory names in lenient mode when C is requested together with 25 unmatched names.
     * Expects C synchronized and the result listing the first 20 unmatched names, in request order, with 5 remaining.
     * The target channel holds exactly X and C, and serves exactly x and c1.
     */
    @Test
    public void testSyncFromSourceWhenLenientAndHasUnmatchedNames() {
        // Arrange
        assertEquals(Set.of(errataX), target.getErratas());
        assertEquals(Set.of(x), target.getPackages());

        // Act
        List<String> names = new ArrayList<>(List.of(errataC.getAdvisoryName()));
        IntStream.range(0, 25).mapToObj(i -> "MISSING-" + i).forEach(names::add);

        SyncErrataWithPackagesResult result = manager.syncFromSource(admin, source.getLabel(), target.getLabel(),
                names, false, false, true);

        // Assert
        assertEquals(Map.of(errataC.getId(), ids(c1)), servedByRepresentative(result));
        assertEquals(names.subList(1, 21), result.unmatchedAdvisoryNames());
        assertEquals(5, result.unmatchedRemaining());
        assertEquals(Set.of(errataX, errataC), target.getErratas());
        assertEquals(Set.of(x, c1), target.getPackages());
    }

    /**
     * Tests syncFromSource by advisory names in lenient mode when no advisory name matches. Expects a 2800 fault
     * "No erratas were found" and the target channel unchanged: exactly X and x.
     */
    @Test
    public void testSyncFromSourceWhenLenientButNoMatchingErratas() {
        FaultException e = assertThrows(FaultException.class,
                () -> manager.syncFromSource(admin, source.getLabel(), target.getLabel(),
                        List.of("NOT-THERE"), false, false, true));

        assertEquals(2800, e.getErrorCode());
        assertEquals("No erratas were found", e.getMessage());
        assertEquals(Set.of(errataX), target.getErratas());
        assertEquals(Set.of(x), target.getPackages());
    }

    /**
     * Tests syncFromSource by date range when A was last modified exactly at the start date, B exactly at the end date,
     * C one millisecond before the start date, and E, a vendor erratum of the source declaring e1, one millisecond
     * after the end date. Expects the bounds included and the rest excluded: A and B synchronized, not C nor E. The
     * target channel holds exactly X and clones of A and B, and serves exactly x, a1 and b1.
     */
    @Test
    public void testSyncFromSourceByDatesWhenErrataOnAndOutsideBounds() {
        // Arrange
        Date start = new Date(System.currentTimeMillis() - 3_600_000L);
        Date end = new Date(System.currentTimeMillis() + 3_600_000L);
        Package e1 = createPackage("e1");
        source.addPackages(List.of(e1));
        Errata errataE = createErrataWithPackages("errataE", null, source, e1);
        setLastModified(errataA, start);
        setLastModified(errataB, end);
        setLastModified(errataC, new Date(start.getTime() - 1));
        setLastModified(errataE, new Date(end.getTime() + 1));

        // Act
        SyncErrataWithPackagesResult result = manager.syncFromSource(admin, source.getLabel(), target.getLabel(),
                start, end, false, true);

        // Assert
        Errata cloneOfA = getCloneOf(errataA);
        Errata cloneOfB = getCloneOf(errataB);
        assertEquals(Map.of(cloneOfA.getId(), ids(a1), cloneOfB.getId(), ids(a1, b1)),
                servedByRepresentative(result));
        assertEquals(Set.of(errataX, cloneOfA, cloneOfB), target.getErratas());
        assertEquals(Set.of(x, a1, b1), target.getPackages());
    }

    /**
     * Tests syncFromSource by date range when the end date is before the start date, and when no erratum of the source
     * was modified within the range. Expects a 2800 fault in both cases, with the matching message, and the target
     * channel unchanged: exactly X and x.
     */
    @Test
    public void testSyncFromSourceByDatesWhenRangeReversedOrEmptyThenFails() {
        Date now = new Date();
        FaultException reversed = assertThrows(FaultException.class,
                () -> manager.syncFromSource(admin, source.getLabel(), target.getLabel(), now,
                        new Date(now.getTime() - 1000), false, true));
        assertEquals(2800, reversed.getErrorCode());
        assertEquals("End date cannot be before start date", reversed.getMessage());

        // a range long before the errata of the source were created
        Date longAgo = new Date(946_684_800_000L);
        FaultException empty = assertThrows(FaultException.class,
                () -> manager.syncFromSource(admin, source.getLabel(), target.getLabel(), longAgo,
                        new Date(longAgo.getTime() + 86_400_000L), false, true));
        assertEquals(2800, empty.getErrorCode());
        assertEquals("No erratas were found", empty.getMessage());
        assertEquals(Set.of(errataX), target.getErratas());
        assertEquals(Set.of(x), target.getPackages());
    }

    /**
     * Tests syncByCriteria in lenient mode with one advisory name in each situation that decides where it is found:
     * - A, held by middle (serving only a2) and by the source: found in middle, the nearest ancestor;
     * - B, held only by the source: found in the source, further up the clone chain;
     * - D, of the org, in no ancestor: found without source;
     * - a name shared by a vendor erratum (declaring v1) and an erratum of the org (declaring o1), in no ancestor:
     *   the erratum of the org, without source;
     * - V, vendor, in no ancestor: found without source;
     * - an erratum of another org: unmatched.
     * Expects each name resolved on its own: errata found in an ancestor served with the packages that ancestor
     * serves (A with a2 only), errata found without source served with all their packages, the other org's erratum
     * reported as unmatched, and the target channel holding exactly X and the representatives.
     */
    @Test
    public void testSyncByCriteriaWhenNamesResolveToDifferentSources() {
        // Arrange
        middle.addPackages(List.of(a2));
        middle.addErrata(errataA);
        Package d1 = createPackage("d1");
        Package d2 = createPackage("d2");
        Errata errataD = createErrataWithPackages("errataD", admin.getOrg(), null, d1, d2);
        String sharedName = "SHARED-" + TestUtils.randomString();
        Package v1 = createPackage("v1");
        Package o1 = createPackage("o1");
        createNamedErrataWithPackages(sharedName, "shared", null, v1);
        Errata own = createNamedErrataWithPackages(sharedName, "own", admin.getOrg(), o1);
        Package w1 = createPackage("w1");
        Errata vendorV = createErrataWithPackages("vendorV", null, null, w1);
        Errata foreign = createErrataWithPackages("foreign", UserTestUtils.createOrg(), null, createPackage("foreign"));

        assertEquals(Set.of(errataX), target.getErratas());
        assertEquals(Set.of(x), target.getPackages());

        // Act
        SyncErrataWithPackagesResult result = manager.syncByCriteria(admin, target.getLabel(),
                List.of(errataA.getAdvisoryName(), errataB.getAdvisoryName(), errataD.getAdvisoryName(), sharedName,
                        vendorV.getAdvisoryName(), foreign.getAdvisoryName()),
                false, false, true);

        // Assert
        Errata cloneOfA = getCloneOf(errataA);
        Errata cloneOfB = getCloneOf(errataB);
        Errata cloneOfV = getCloneOf(vendorV);
        assertEquals(Map.of(
                cloneOfA.getId(), ids(a2),
                cloneOfB.getId(), ids(a1, b1),
                errataD.getId(), ids(d1, d2),
                own.getId(), ids(o1),
                cloneOfV.getId(), ids(w1)
        ), servedByRepresentative(result));
        assertEquals(List.of(foreign.getAdvisoryName()), result.unmatchedAdvisoryNames());
        assertEquals(0, result.unmatchedRemaining());
        assertEquals(Set.of(errataX, cloneOfA, cloneOfB, errataD, own, cloneOfV), target.getErratas());
        assertEquals(Set.of(x, a2, a1, b1, d1, d2, o1, w1), target.getPackages());
    }

    /**
     * Tests syncByCriteria when the target channel is a clone of a channel owned by another org (ie, user
     * cannot access to it). That original channel holds vendor erratum E, which declares e1 and e2, but serves only
     * e1.
     * Expects the original channel not to be used as source: the target channel holds exactly a clone of E and serves
     * exactly e1 and e2 (only e1 if the original channel had been used).
     */
    @Test
    public void testSyncByCriteriaWhenAncestorNotAccessibleThenSkipsIt() {
        // Arrange
        Package e1 = createPackage("e1");
        Package e2 = createPackage("e2");
        Org otherOrg = UserTestUtils.createOrg();
        Channel foreignChannel = ChannelFactoryTest.createTestChannel(otherOrg);
        foreignChannel.setLabel("foreign");
        foreignChannel.addPackages(List.of(e1));
        ChannelFactory.save(foreignChannel);
        Errata errataE = createErrataWithPackages("errataE", null, foreignChannel, e1, e2);
        Channel foreignClone = ChannelFactoryTest.createTestClonedChannel(foreignChannel, admin);

        assertTrue(foreignClone.getErratas().isEmpty());
        assertTrue(foreignClone.getPackages().isEmpty());

        // Act
        SyncErrataWithPackagesResult result = manager.syncByCriteria(admin, foreignClone.getLabel(),
                List.of(errataE.getAdvisoryName()), true, false, true);

        // Assert
        Errata cloneOfE = getCloneOf(errataE);
        assertEquals(Map.of(cloneOfE.getId(), ids(e1, e2)), servedByRepresentative(result));
        assertEquals(Set.of(cloneOfE), foreignClone.getErratas());
        assertEquals(Set.of(e1, e2), foreignClone.getPackages());
    }

    /**
     * Tests syncByCriteria by errata A when the org owns a clone of A that is missing package a2.
     * The target channel is not a cloned channel. Errata A is found without a source channel.
     * Expects getting the existing clone, with a1 and a2. This means a2 is added to the clone.
     */
    @Test
    public void testSyncByCriteriaWhenCloneOutsideTargetMissingPackages() {
        // Arrange
        Errata errataAClone = createCloneErrataOf(errataA, null);
        errataAClone.removePackage(a2);
        Channel plain = createChannelWithPackages("plain");

        // Act
        SyncErrataWithPackagesResult result = manager.syncByCriteria(admin, plain.getLabel(),
                List.of(errataA.getAdvisoryName()), true, false, true);

        // Assert
        assertEquals(errataAClone, getCloneOf(errataA));
        assertEquals(Set.of(a1, a2), errataAClone.getPackages());
        assertEquals(Set.of(a1, a2), plain.getPackages());
        assertTrue(plain.getErratas().contains(errataAClone));
        assertEquals(Map.of(errataAClone.getId(), ids(a1, a2)), servedByRepresentative(result));
        assertEquals(Set.of(errataAClone), plain.getErratas());
    }

    /**
     * Tests syncByCriteria in strict mode when the advisory name is unmatched, and when the list of advisory names is
     * empty. Expects a -208 fault in the first case and an invalid parameter error in the second, and the target
     * channel unchanged: exactly X and x.
     */
    @Test
    public void testSyncByCriteriaWhenNameUnmatchedOrNamesEmptyThenFails() {
        FaultException strict = assertThrows(FaultException.class,
                () -> manager.syncByCriteria(admin, target.getLabel(), List.of("NOT-THERE"), true, false, true));
        assertEquals(-208, strict.getErrorCode());

        assertThrows(InvalidParameterException.class,
                () -> manager.syncByCriteria(admin, target.getLabel(), List.of(), true, false, true));
        assertEquals(Set.of(errataX), target.getErratas());
        assertEquals(Set.of(x), target.getPackages());
    }

    /**
     * Tests syncFromSource by advisory names when the target channel does not exist, when the source channel does not
     * exist, and when the user is not a channel administrator of the target channel. Expects a -210 fault for the
     * missing channels and a -23 fault for the missing permission.
     */
    @Test
    public void testSyncFromSourceWhenChannelMissingOrUserNotAdminThenFails() {
        FaultException noTarget = assertThrows(FaultException.class,
                () -> manager.syncFromSource(admin, source.getLabel(), "no-such-channel", List.of(), true,
                        false, true));
        assertEquals(-210, noTarget.getErrorCode());
        assertEquals("No such channel: no-such-channel", noTarget.getMessage());

        FaultException noSource = assertThrows(FaultException.class,
                () -> manager.syncFromSource(admin, "no-such-channel", target.getLabel(), List.of(), true,
                        false, true));
        assertEquals(-210, noSource.getErrorCode());

        FaultException noAccess = assertThrows(FaultException.class,
                () -> manager.syncFromSource(regular, source.getLabel(), source.getLabel(), List.of(), true,
                        false, true));
        assertEquals(-23, noAccess.getErrorCode());
        assertEquals("User " + regular.getLogin() + " does not have access to channel " + source.getName(),
                noAccess.getMessage());
    }

    /**
     * Tests syncFromSource by advisory names when the target channel has pending asynchronous errata clone jobs.
     * Expects a -211 fault and the target channel unchanged: exactly X and x.
     */
    @Test
    public void testSyncFromSourceWhenAsyncJobsPendingThenFails() {
        AsyncErrataCloneCounter.getInstance().addAsyncErrataCloneJob(target.getId());
        try {
            FaultException e = assertThrows(FaultException.class,
                    () -> manager.syncFromSource(admin, source.getLabel(), target.getLabel(),
                            List.of(errataC.getAdvisoryName()), true, false, true));
            assertEquals(-211, e.getErrorCode());
            assertEquals("Channel " + target.getName() + " has pending asynchronous errata clone jobs. " +
                    "You must wait until asynchronous errata clone jobs are done.", e.getMessage());
        }
        finally {
            AsyncErrataCloneCounter.getInstance().removeAsyncErrataCloneJob(target.getId());
        }
        assertEquals(Set.of(errataX), target.getErratas());
        assertEquals(Set.of(x), target.getPackages());
    }

    /**
     * Tests syncFromSource by advisory names in asynchronous mode, first with an unmatched advisory name and then with
     * C. Expects a fault and no pending job in the first case; a pending job, an empty list of errata and the target
     * channel not yet changed (exactly X and x) in the second.
     */
    @Test
    public void testSyncFromSourceWhenAsyncThenValidatesAndSchedulesJob() {
        assertThrows(FaultException.class, () -> manager.syncFromSource(admin, source.getLabel(),
                target.getLabel(), List.of("NOT-THERE"), true, true, true));
        assertFalse(ErrataManager.channelHasPendingAsyncCloneJobs(target));

        try {
            SyncErrataWithPackagesResult result = manager.syncFromSource(admin, source.getLabel(),
                    target.getLabel(), List.of(errataC.getAdvisoryName()), true, true, true);
            assertEquals(List.of(), result.erratas());
            assertTrue(ErrataManager.channelHasPendingAsyncCloneJobs(target));
        }
        finally {
            AsyncErrataCloneCounter.getInstance().removeAsyncErrataCloneJob(target.getId());
        }
        assertEquals(Set.of(errataX), target.getErratas());
        assertEquals(Set.of(x), target.getPackages());
    }

    /**
     * Tests the asynchronous action, which calls syncValidated, when it runs an event carrying A and the source.
     * Expects the pending job released and A synchronized through a clone: the target channel holds exactly X and
     * the clone of A, and serves exactly x and a1.
     */
    @Test
    public void testAsyncActionWhenEventRunsThenSyncsErrataAndReleasesJob() {
        SyncErrataWithPackagesEvent event =
                new SyncErrataWithPackagesEvent(target, Map.of(errataA, source), true, admin);
        event.register();

        new SyncErrataWithPackagesAction().execute(event);

        assertFalse(ErrataManager.channelHasPendingAsyncCloneJobs(target));
        assertEquals(Set.of(errataX, getCloneOf(errataA)), target.getErratas());
        assertEquals(Set.of(x, a1), target.getPackages());
    }

    /**
     * Tests the asynchronous action when an erratum of the event, C, is deleted before the action runs.
     * Expects C skipped and A still synchronized: the target channel holds exactly X and the clone of A.
     */
    @Test
    public void testAsyncActionWhenErratumDeletedThenSkipsIt() {
        SyncErrataWithPackagesEvent event =
                new SyncErrataWithPackagesEvent(target, Map.of(errataA, source, errataC, source), true, admin);
        event.register();
        ErrataManager.deleteErratum(admin, errataC);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        new SyncErrataWithPackagesAction().execute(event);

        Channel reloaded = ChannelFactory.lookupById(target.getId());
        assertFalse(ErrataManager.channelHasPendingAsyncCloneJobs(reloaded));
        assertEquals(Set.of(errataX.getId(), getCloneOf(errataA).getId()),
                reloaded.getErratas().stream().map(Errata::getId).collect(Collectors.toSet()));
    }

    // Utility methods

    private Package createPackage(String packageName) {
        return PackageTest.createTestPackage(admin.getOrg(), packageName);
    }

    private Channel createChannelWithPackages(String channelLabel, Package... packages) {
        Channel channel = ChannelFactoryTest.createTestChannel(admin);
        channel.setLabel(channelLabel);
        channel.addPackages(List.of(packages));
        return ChannelFactory.save(channel);
    }

    private Errata createErrataWithPackages(String description, Org org, Channel channel, Package... packages) {
        return createNamedErrataWithPackages(null, description, org, channel, packages);
    }

    private Errata createNamedErrataWithPackages(
            String advisoryName, String description, Org org, Package... packages
    ) {
        return createNamedErrataWithPackages(advisoryName, description, org, null, packages);
    }

    private Errata createNamedErrataWithPackages(
            String advisoryName, String description, Org org, Channel channel, Package... packages
    ) {
        ErrataTestBuilder builder = new ErrataTestBuilder()
                .orgId(org == null ? null : org.getId())
                .advisory(advisoryName)
                .description(description)
                .addPackages(packages);
        if (channel != null) {
            builder.addChannel(channel);
        }
        return builder.buildAndSave();
    }

    private Errata createCloneErrataOf(Errata original, Channel channel) {
        Errata clone = ErrataHelper.cloneErrataFast(original, admin.getOrg());
        if (channel != null) {
            channel.addErrata(clone);
        }
        return clone;
    }

    /**
     * Returns the clone of the given erratum owned by the admin's organization.
     * Expects exactly one clone to exist.
     */
    private Errata getCloneOf(Errata original) {
        List<Errata> clones = ErrataFactory.lookupErrataByOriginal(admin.getOrg(), original);
        assertEquals(1, clones.size(), "clones of " + original.getAdvisoryName());
        return clones.get(0);
    }

    /**
     * Sets the last modified date of an erratum in the database, then refreshes it so a later flush does not write
     * the previous value back.
     */
    private static void setLastModified(Errata errata, Date date) {
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession()
                .createNativeMutationQuery("UPDATE rhnErrata SET last_modified = :date WHERE id = :id")
                .setParameter("date", date)
                .setParameter("id", errata.getId())
                .executeUpdate();
        HibernateFactory.getSession().refresh(errata);
    }

    /**
     * Maps the id of each representative in the result to the ids of the packages served for it.
     */
    private static Map<Long, Set<Long>> servedByRepresentative(SyncErrataWithPackagesResult result) {
        return result.erratas().stream().collect(
                Collectors.toMap(s -> s.errata().getId(), s -> ids(s.packages()))
        );
    }

    private static Set<Long> ids(Package... packages) {
        return ids(List.of(packages));
    }

    private static Set<Long> ids(Collection<Package> packages) {
        return packages.stream().map(Package::getId).collect(Collectors.toSet());
    }
}
