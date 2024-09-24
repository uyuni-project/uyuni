/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.errata.test;

import static com.redhat.rhn.testing.ErrataTestUtils.createLaterTestPackage;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestChannel;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestCve;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestInstalledPackage;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestPackage;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestServer;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.errata.AdvisoryStatus;
import com.redhat.rhn.domain.errata.Bug;
import com.redhat.rhn.domain.errata.ClonedErrata;
import com.redhat.rhn.domain.errata.Cve;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.errata.Keyword;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactory;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.InvalidAdvisoryReleaseException;
import com.redhat.rhn.frontend.xmlrpc.errata.ErrataHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.errata.cache.UpdateErrataCacheCommand;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ErrataHandlerTest extends BaseHandlerTestCase {

    private ErrataHandler handler = new ErrataHandler();
    private User user;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        user = UserTestUtils.createUser("testUser", admin.getOrg().getId());
    }

    @Test
    public void testCloneAsOriginal() throws Exception {
        // clone a channel with its errata, and the errata's packages are NOT EMPTY
        Errata errata = ErrataFactoryTest.createTestErrata(admin.getOrg().getId());

        Errata foundErrata = ErrataFactory.lookupById(errata.getId());
        assertNotNull(foundErrata);
        assertEquals(foundErrata.getId(), errata.getId());
        assertNotNull(foundErrata.getPackages());
        assertFalse(foundErrata.getPackages().isEmpty());

        Channel original = createTestChannel(admin, errata);

        Channel clonedChannel = ChannelFactoryTest.createTestClonedChannel(original, admin);

        Object[] cloned = handler.cloneAsOriginal(admin, clonedChannel.getLabel(),
                Arrays.asList(errata.getAdvisoryName()));
        assertNotNull(cloned);
        assertEquals(1, cloned.length);

        ClonedErrata clonedErrata = (ClonedErrata) cloned[0];
        assertEquals(clonedErrata.getAdvisoryName(),
                "CL-" + errata.getAdvisoryName());
        assertEquals(clonedErrata.getPackages().size(), errata.getPackages().size());
        assertTrue(clonedErrata.getPackages().stream()
                .allMatch(p -> errata.getPackages().stream()
                        .anyMatch(ep -> ep.getPath().equals(p.getPath()))));

        // clone a channel with its errata, and the errata's packages are EMPTY
        Errata emptyErrata = ErrataFactoryTest.createTestErrata(admin.getOrg().getId());

        emptyErrata.setPackages(new HashSet<>());
        ErrataFactory.save(emptyErrata);

        foundErrata = ErrataFactory.lookupById(emptyErrata.getId());
        assertNotNull(foundErrata);
        assertEquals(foundErrata.getId(), emptyErrata.getId());
        assertNotNull(foundErrata.getPackages());
        assertTrue(foundErrata.getPackages().isEmpty());

        Channel originalForEmptyErrata = createTestChannel(admin, emptyErrata);

        Channel clonedChannelForEmptyErrata =
                ChannelFactoryTest.createTestClonedChannel(originalForEmptyErrata, admin);

        Object[] clonedForEmptyErrata =
                handler.cloneAsOriginal(admin, clonedChannelForEmptyErrata.getLabel(),
                        Arrays.asList(emptyErrata.getAdvisoryName()));
        assertNotNull(clonedForEmptyErrata);
        assertNotNull(clonedForEmptyErrata);
        assertEquals(1, clonedForEmptyErrata.length);

        ClonedErrata clonedEmptyErrata = (ClonedErrata) clonedForEmptyErrata[0];
        assertEquals(clonedEmptyErrata.getAdvisoryName(),
                "CL-" + emptyErrata.getAdvisoryName());
        assertEquals(clonedEmptyErrata.getPackages().size(), emptyErrata.getPackages().size());
        assertTrue(clonedEmptyErrata.getPackages().isEmpty());
    }

    @Test
    public void testGetDetails() throws Exception {
        Errata errata = ErrataFactoryTest.createTestErrata(user.getOrg().getId());

        Errata check = ErrataManager.lookupErrata(errata.getId(), user);
        assertEquals(check.getAdvisory(), errata.getAdvisory());
        assertEquals(check.getId(), errata.getId());

        Map<String, Object> details = handler.getDetails(admin, errata.getAdvisory());
        assertNotNull(details);

        try {
            handler.getDetails(admin, "foo" + TestUtils.randomString());
            fail("found invalid errata");
        }
        catch (FaultException e) {
            //success
        }
    }

    /**
     * Do not fail in case orgId is null: it means errata is just a vendor one (bsc#1128228)
     */
    @Test
    public void testGetVendorErrataDetails() throws Exception {
        Errata vendorErrata = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        vendorErrata.setOrg(null);

        Map<String, Object> details = handler.getDetails(admin, vendorErrata.getAdvisory());
        assertNotNull(details);
    }

    /**
     * Make sure that if two erratas exist with the same advisoryName,
     * but one with current user's orgId and one with orgId null (vendor org)
     * it returns the one with the user org over the vendor one.
     */
    @Test
    public void testGetUserErrataOverVendorErrata() throws Exception {
        Errata vendorErrata = ErrataFactoryTest.createTestErrata(null);
        vendorErrata.setOrg(null);

        Errata userErrata = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        userErrata.setAdvisoryName(vendorErrata.getAdvisoryName());

        Map<String, Object> details = handler.getDetails(admin, vendorErrata.getAdvisory());
        assertEquals(details.get("id"), userErrata.getId());
    }

    @Test
    public void testSetDetailsAdvRelAboveMax() throws Exception {
        // setup
        Errata errata = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        Map<String, Object> details = new HashMap<>();
        details.put("advisory_release", 10000);
        try {
            handler.setDetails(admin, errata.getAdvisory(), details);
            fail("invalid advisory of 10000 accepted");
        }
        catch (InvalidAdvisoryReleaseException iare) {
            // we expect this test to fail
            assertTrue(true);
        }
    }

    @Test
    public void testSetDetails() throws Exception {
        // setup
        Errata errata = ErrataFactoryTest.createTestErrata(user.getOrg().getId());

        // execute
        Map<String, Object> details = new HashMap<>();
        details.put("synopsis", "synopsis-1");
        details.put("advisory_name", "advisory-1");
        details.put("advisory_release", 123);
        details.put("advisory_type", "Security Advisory");
        details.put("product", "product text");
        details.put("topic", "topic text");
        details.put("description", "description text");
        details.put("references", "references text");
        details.put("notes", "notes text");
        details.put("solution", "solution text");

        List<Map<String, Object>> bugs = new ArrayList<>();

        Map<String, Object> bug1 = new HashMap<>();
        bug1.put("id", 1);
        bug1.put("summary", "bug1 summary");
        bugs.add(bug1);

        Map<String, Object> bug2 = new HashMap<>();
        bug2.put("id", 2);
        bug2.put("summary", "bug2 summary");
        bugs.add(bug2);

        details.put("bugs", bugs);

        List<String> keywords = new ArrayList<>();
        keywords.add("keyword1");
        keywords.add("keyword2");
        details.put("keywords", keywords);

        int result = handler.setDetails(admin, errata.getAdvisory(), details);

        // verify
        assertEquals(1, result);

        Errata updatedErrata = ErrataManager.lookupErrata(errata.getId(), user);

        assertEquals(errata.getSynopsis(), "synopsis-1");
        assertEquals(errata.getAdvisory(), "advisory-1-123");
        assertEquals(errata.getAdvisoryName(), "advisory-1");
        assertEquals(errata.getAdvisoryRel(), Long.valueOf(123));
        assertEquals(errata.getAdvisoryType(), "Security Advisory");
        assertEquals(errata.getProduct(), "product text");
        assertEquals(errata.getTopic(), "topic text");
        assertEquals(errata.getDescription(), "description text");
        assertEquals(errata.getRefersTo(), "references text");
        assertEquals(errata.getNotes(), "notes text");
        assertEquals(errata.getSolution(), "solution text");

        boolean foundBug1 = false, foundBug2 = false;
        for (Bug bug : errata.getBugs()) {
            if (bug.getId().equals(1L) &&
                bug.getSummary().equals("bug1 summary")) {
                foundBug1 = true;
            }
            if (bug.getId().equals(2L) &&
                bug.getSummary().equals("bug2 summary")) {
                foundBug2 = true;
            }
        }
        assertTrue(foundBug1);
        assertTrue(foundBug2);

        boolean foundKeyword1 = false, foundKeyword2 = false;
        for (Keyword keyword : errata.getKeywords()) {
            if (keyword.getKeyword().equals("keyword1")) {
                foundKeyword1 = true;
            }
            if (keyword.getKeyword().equals("keyword2")) {
                foundKeyword2 = true;
            }
        }
        assertTrue(foundKeyword1);
        assertTrue(foundKeyword2);
    }

    @Test
    public void testSetDetailsDates() throws Exception {
        Errata errata = ErrataFactoryTest.createTestErrata(user.getOrg().getId());

        Map<String, Object> details = new HashMap<>();
        Date expectedDate = Date.from(LocalDate.of(1989, 4, 1).atStartOfDay().toInstant(ZoneOffset.UTC));
        // Set using Date instance (XMLRPC)
        details.put("issue_date", expectedDate);
        // Set using ISO-8601 String (JSON over HTTP)
        details.put("update_date", "1989-04-01T00:00:00Z");

        int result = handler.setDetails(user, errata.getAdvisoryName(), details);

        assertEquals(1, result);

        Errata updatedErrata = ErrataManager.lookupErrata(errata.getId(), user);
        assertEquals(expectedDate, updatedErrata.getIssueDate());
        assertEquals(expectedDate, updatedErrata.getUpdateDate());
    }

    @Test
    public void testListAffectedSystems() throws Exception {
        //no affected systems
        Errata userErrata = ErrataFactoryTest.createTestErrata(user.getOrg().getId());

        Object[] systems = handler.listAffectedSystems(user, userErrata.getAdvisory());
        assertNotNull(systems);
        assertEquals(0, systems.length);

        //affected systems with an unique errata, for user's org
        Channel channel1 = createTestChannel(user, userErrata);
        Package unpatched1 = createTestPackage(user, channel1, "noarch");
        createLaterTestPackage(user, userErrata, channel1, unpatched1);

        Server server1 = createTestServer(user, Stream.of(channel1).collect(Collectors.toList()));
        createTestInstalledPackage(unpatched1, server1);

        UpdateErrataCacheCommand uECC = new UpdateErrataCacheCommand();
        uECC.updateErrataCacheForServer(server1.getId(), false);

        systems = handler.listAffectedSystems(user, userErrata.getAdvisory());
        assertNotNull(systems);
        assertEquals(systems.length, 1);
        assertTrue(Arrays.stream(systems).allMatch(sys -> ((Map)sys).get("id").equals(server1.getId())));

        //affected systems with both vendor's and user's errata with same advisoryName
        Errata vendorErrata = ErrataFactoryTest.createTestErrata(null, Optional.of(userErrata.getAdvisory()));

        Errata vendorErrataCheck = ErrataManager.lookupByAdvisoryAndOrg(vendorErrata.getAdvisory(), null);
        assertEquals(vendorErrataCheck.getAdvisory(), userErrata.getAdvisory());
        assertNotEquals(vendorErrataCheck.getId(), userErrata.getId());

        Channel channel2 = createTestChannel(admin, vendorErrata);

        Package unpatchedPkg2 = createTestPackage(admin, channel2, "noarch");
        createLaterTestPackage(admin, vendorErrata, channel2, unpatchedPkg2);

        Server server2 = createTestServer(admin, Stream.of(channel2).collect(Collectors.toList()));
        createTestInstalledPackage(unpatchedPkg2, server2);

        uECC.updateErrataCacheForServer(server2.getId(), false);

        systems = handler.listAffectedSystems(admin, vendorErrata.getAdvisory());
        assertNotNull(systems);
        assertEquals(systems.length, 2);
        assertTrue(Arrays.stream(systems).allMatch(sys -> ((Map)sys).get("id").equals(server1.getId()) ||
                ((Map)sys).get("id").equals(server2.getId())));
    }

    @Test
    public void testBugzillaFixes() throws Exception {
        //unique errata, for user's org
        Errata userErrata = ErrataFactoryTest.createTestErrata(user.getOrg().getId());

        Bug bug1 = new Bug();
        bug1.setId(1001L);
        bug1.setSummary("This is a test summary for bug1");

        userErrata.addBug(bug1);

        ErrataFactory.save(userErrata);

        Errata userErrataCheck = ErrataManager.lookupErrata(userErrata.getId(), user);
        assertEquals(userErrataCheck.getAdvisory(), userErrata.getAdvisory());
        assertEquals(userErrataCheck.getId(), userErrata.getId());

        int userErrataBugsCount = userErrata.getBugs().size();

        Map<Long, String> bugs = handler.bugzillaFixes(admin, userErrata.getAdvisory());

        assertEquals(userErrataBugsCount, bugs.size());

        //map should contain an 'id' key only
        Set<Long> keys = bugs.keySet();
        assertEquals(1, keys.size());
        assertEquals("This is a test summary for bug1", bugs.get(1001L));

        //two erratas, for user's and vendor's org, with the same advisoryName
        Errata vendorErrata = ErrataFactoryTest.createTestErrata(null, Optional.of(userErrata.getAdvisory()));

        Bug bug2 = new Bug();
        bug2.setId(1002L);
        bug2.setSummary("This is a test summary for bug2");

        vendorErrata.addBug(bug2);

        ErrataFactory.save(vendorErrata);

        Errata vendorErrataCheck = ErrataManager.lookupByAdvisoryAndOrg(vendorErrata.getAdvisory(), null);
        assertEquals(vendorErrataCheck.getAdvisory(), vendorErrata.getAdvisory());
        assertEquals(vendorErrataCheck.getId(), vendorErrata.getId());

        assertEquals(vendorErrataCheck.getAdvisory(), userErrata.getAdvisory());
        assertNotEquals(vendorErrataCheck.getId(), userErrata.getId());

        int vendorErrataBugsCount = vendorErrata.getBugs().size();

        bugs = handler.bugzillaFixes(admin, vendorErrata.getAdvisory());

        assertEquals(vendorErrataBugsCount + userErrataBugsCount, bugs.size());

        //map should contain one only element
        assertEquals(2, bugs.size());
        assertEquals("This is a test summary for bug1", bugs.get(1001L));
        assertEquals("This is a test summary for bug2", bugs.get(1002L));
    }

    @Test
    public void testListKeywords() throws Exception {
        //unique errata, only for the user's org
        Errata userErrata = ErrataFactoryTest.createTestErrata(user.getOrg().getId());

        userErrata.addKeyword("user_foo");
        userErrata.addKeyword("user_bar");

        ErrataFactory.save(userErrata);

        Errata userErrataCheck = ErrataManager.lookupErrata(userErrata.getId(), user);
        assertEquals(userErrataCheck.getAdvisory(), userErrata.getAdvisory());
        assertEquals(userErrataCheck.getId(), userErrata.getId());

        Object[] keywords = handler.listKeywords(admin, userErrata.getAdvisory());

        Set<String> userKeywords = userErrata.getKeywords().stream()
                .map(Keyword::getKeyword).collect(Collectors.toSet());
        assertEquals(userKeywords.size(), keywords.length);
        //two errata, for the user's and vendor's orgs with same advisoryName
        Errata vendorErrata = ErrataFactoryTest.createTestErrata(null, Optional.of(userErrata.getAdvisory()));

        vendorErrata.addKeyword("vendor_foo");
        vendorErrata.addKeyword("vendor_bar");

        ErrataFactory.save(vendorErrata);

        Errata vendorErrataCheck = ErrataManager.lookupByAdvisoryAndOrg(vendorErrata.getAdvisory(), null);
        assertEquals(vendorErrataCheck.getAdvisory(), vendorErrata.getAdvisory());
        assertEquals(vendorErrataCheck.getId(), vendorErrata.getId());

        assertEquals(vendorErrataCheck.getAdvisory(), userErrata.getAdvisory());
        assertNotEquals(vendorErrataCheck.getId(), userErrata.getId());

        keywords = handler.listKeywords(admin, vendorErrata.getAdvisory());

        Set<String> vendorKeywords = vendorErrata.getKeywords().stream()
                .map(Keyword::getKeyword).collect(Collectors.toSet());

        Set<String> allKeywords = new HashSet<>();
        allKeywords.addAll(vendorKeywords);
        allKeywords.addAll(userKeywords);

        assertEquals(allKeywords.size(), keywords.length);
    }

    @Test
    public void testApplicableToChannels() throws Exception {
        //unique errata for user's org. No channels
        Errata userErrata = ErrataFactoryTest.createTestErrata(user.getOrg().getId());

        ErrataFactory.save(userErrata);

        Errata userErrataCheck = ErrataManager.lookupErrata(userErrata.getId(), user);
        assertEquals(userErrataCheck.getAdvisory(), userErrata.getAdvisory());
        assertEquals(userErrataCheck.getId(), userErrata.getId());

        DataResult<Row> dr = ErrataManager.applicableChannels(Collections.singletonList(userErrata.getId()),
                            user.getOrg().getId());

        Object[] channels = handler.applicableToChannels(admin, userErrata.getAdvisory());
        assertEquals(dr.size(), channels.length);
        assertEquals(channels.length, 0);

        //unique errata for user's org. Applicable to one user channel
        Channel channel1 = ChannelFactoryTest.createTestChannel(admin);
        userErrata.addChannel(channel1);

        ErrataFactory.save(userErrata);

        DataResult<Row> applicableChannelsForUserErrata = ErrataManager.applicableChannels(
                Collections.singletonList(userErrata.getId()), user.getOrg().getId());

        channels = handler.applicableToChannels(admin, userErrata.getAdvisory());

        assertEquals(applicableChannelsForUserErrata.size(), channels.length);
        assertEquals(channels.length, 1);
        assertTrue(Arrays.stream(channels).allMatch(chn1 -> applicableChannelsForUserErrata.stream()
                .anyMatch(chn2 ->((Map) chn1).get("id").equals(chn2.get("id")))));

        //two errata, for user's and vendor's org, with the same AdvisoryName. Applicable to user and vendor channels
        Errata vendorErrata = ErrataFactoryTest.createTestErrata(null, Optional.of(userErrata.getAdvisory()));

        Channel channel2 = ChannelFactoryTest.createTestChannel(admin);
        vendorErrata.addChannel(channel2);

        ErrataFactory.save(vendorErrata);

        Errata vendorErrataCheck = ErrataManager.lookupByAdvisoryAndOrg(vendorErrata.getAdvisory(), null);
        assertEquals(vendorErrataCheck.getAdvisory(), vendorErrata.getAdvisory());
        assertEquals(vendorErrataCheck.getId(), vendorErrata.getId());

        assertEquals(vendorErrataCheck.getAdvisory(), userErrata.getAdvisory());
        assertNotEquals(vendorErrataCheck.getId(), userErrata.getId());

        DataResult<Row> applicableChannelsForUserAndVendorErrata = ErrataManager.applicableChannels(
                Arrays.asList(vendorErrata.getId(), userErrata.getId()), user.getOrg().getId());

        channels = handler.applicableToChannels(admin, vendorErrata.getAdvisory());
        assertEquals(applicableChannelsForUserAndVendorErrata.size(), channels.length);
        assertEquals(channels.length, 2);

        assertTrue(Arrays.stream(channels).allMatch(chn1 -> applicableChannelsForUserAndVendorErrata.stream()
                .anyMatch(chn2 ->((Map) chn1).get("id").equals(chn2.get("id")))));
    }

    @Test
    public void testListCves() throws Exception {
        //unique errata for user's org
        Errata userErrata = ErrataFactoryTest.createTestErrata(user.getOrg().getId());

        Cve userCommonNameCve = createTestCve("userErrataCveName");
        userErrata.setCves(Stream.of(userCommonNameCve).collect(Collectors.toSet()));

        ErrataFactory.save(userErrata);

        Errata userErrataCheck = ErrataManager.lookupErrata(userErrata.getId(), user);
        assertEquals(userErrataCheck.getAdvisory(), userErrata.getAdvisory());
        assertEquals(userErrataCheck.getId(), userErrata.getId());
        assertEquals(userErrataCheck.getCves().size(), userErrata.getCves().size());
        assertEquals(userErrataCheck.getCves().size(), 1);
        assertTrue(userErrataCheck.getCves().stream().allMatch(cve -> userErrata.getCves().stream()
                .anyMatch(userCV -> cve.getId().equals(userCV.getId()))));

        List<String> cves = handler.listCves(admin, userErrata.getAdvisory());

        assertEquals(cves.size(), 1);
        assertTrue(cves.stream().allMatch(cve -> userErrata.getCves().stream()
                .anyMatch(userCve -> cve.equals(userCve.getName()))));

        //two errata for user's and vendor's org, with the same advisoryName, same CVE name on both errata
        Errata vendorErrata = ErrataFactoryTest.createTestErrata(null, Optional.of(userErrata.getAdvisory()));

        Cve vendorCommonNameCve = createTestCve("vendorErrataCveName");
        vendorErrata.setCves(Stream.of(vendorCommonNameCve).collect(Collectors.toSet()));

        ErrataFactory.save(vendorErrata);

        Errata vendorErrataCheck = ErrataManager.lookupByAdvisoryAndOrg(vendorErrata.getAdvisory(), null);
        assertEquals(vendorErrataCheck.getAdvisory(), vendorErrata.getAdvisory());
        assertEquals(vendorErrataCheck.getId(), vendorErrata.getId());

        assertEquals(vendorErrataCheck.getAdvisory(), userErrata.getAdvisory());
        assertNotEquals(vendorErrataCheck.getId(), userErrata.getId());

        assertEquals(vendorErrataCheck.getCves().size(), vendorErrata.getCves().size());
        assertEquals(vendorErrataCheck.getCves().size(), 1);
        assertTrue(vendorErrataCheck.getCves().stream().allMatch(cve -> vendorErrata.getCves().stream()
                .anyMatch(vendorCve -> cve.getId().equals(vendorCve.getId()))));

        cves = handler.listCves(admin, vendorErrata.getAdvisory());

        assertEquals(cves.size(), 2);
        assertTrue(cves.stream().allMatch(cve -> userErrata.getCves().stream()
                .anyMatch(userCve -> cve.equals(userCve.getName())) || vendorErrata.getCves().stream()
                .anyMatch(vendorCve -> cve.equals(vendorCve.getName()))));
    }

    @Test
    public void testPackages() throws Exception {
        //unique errata for user's org
        Errata userErrata = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        ErrataFactory.save(userErrata);

        Errata userErrataCheck = ErrataManager.lookupErrata(userErrata.getId(), user);
        assertEquals(userErrataCheck.getAdvisory(), userErrata.getAdvisory());
        assertEquals(userErrataCheck.getId(), userErrata.getId());

        Set<Package> userStoredPackages = userErrataCheck.getPackages();
        assertEquals(userStoredPackages.stream()
                .filter(storedPkg -> userErrata.getPackages().stream()
                        .anyMatch(userPkg -> userPkg.getId().equals(storedPkg.getId()))).count(), 1);

        List<Map<String, Object>> pkgs = handler.listPackages(admin, userErrata.getAdvisory());

        assertNotNull(pkgs);
        assertEquals(userErrata.getPackages().size(), pkgs.size());
        assertEquals(1, pkgs.size());

        assertEquals(pkgs.stream()
                .filter(outerMap -> userStoredPackages.stream().allMatch(pkg -> outerMap.get("id").equals(pkg.getId())))
                .count(), 1);

        //two errata for user's and vendor's org, with the same advisoryName
        Errata vendorErrata = ErrataFactoryTest.createTestErrata(null, Optional.of(userErrata.getAdvisory()));
        ErrataFactory.save(vendorErrata);

        Errata vendorErrataCheck = ErrataManager.lookupByAdvisoryAndOrg(vendorErrata.getAdvisory(), null);
        assertEquals(vendorErrataCheck.getAdvisory(), vendorErrata.getAdvisory());
        assertEquals(vendorErrataCheck.getId(), vendorErrata.getId());
        assertEquals(vendorErrataCheck.getAdvisory(), userErrata.getAdvisory());
        assertNotEquals(vendorErrataCheck.getId(), userErrata.getId());

        Set<Package> vendorStoredPackages = vendorErrataCheck.getPackages();
        assertEquals(vendorStoredPackages.stream()
                .filter(storedPkg -> vendorErrata.getPackages().stream()
                        .anyMatch(vendorPkg -> vendorPkg.getId().equals(storedPkg.getId()))).count(), 1);

        pkgs = handler.listPackages(admin, userErrata.getAdvisory());

        assertNotNull(pkgs);
        assertEquals(userStoredPackages.size() + vendorStoredPackages.size(), pkgs.size());
        assertEquals(2, pkgs.size());

        assertEquals(pkgs.stream()
                .filter(outerMap ->userStoredPackages.stream()
                        .anyMatch(usrPkg -> outerMap.get("id").equals(usrPkg.getId()) || vendorStoredPackages.stream()
                                .anyMatch(vndrPkg -> outerMap.get("id").equals(vndrPkg.getId()))))
                .count(), 2);
    }

    @Test
    public void testAddPackages() throws Exception {
        // setup
        Errata errata = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        ErrataFactory.save(errata);

        int initialNumPkgs = handler.listPackages(admin, errata.getAdvisory()).size();

        Package pkg1 = PackageTest.createTestPackage(user.getOrg());
        Package pkg2 = PackageTest.createTestPackage(user.getOrg());

        // execute
        List<Integer> pkgIds = new ArrayList<>();
        pkgIds.add(pkg1.getId().intValue());
        pkgIds.add(pkg2.getId().intValue());
        int numPkgsAdded = handler.addPackages(admin, errata.getAdvisory(), pkgIds);

        // verify
        assertEquals(2, numPkgsAdded);

        int resultNumPkgs = handler.listPackages(admin, errata.getAdvisory()).size();
        assertEquals(initialNumPkgs + 2, resultNumPkgs);

        boolean found1 = false, found2 = false;
        for (Package pkg : errata.getPackages()) {
            if (pkg.getId().equals(pkg1.getId())) {
                found1 = true;
            }
            if (pkg.getId().equals(pkg2.getId())) {
                found2 = true;
            }
        }
        assertTrue(found1);
        assertTrue(found2);
    }

    @Test
    public void testRemovePackages() throws Exception {
        // setup
        Errata errata = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        Package pkg1 = PackageTest.createTestPackage(user.getOrg());
        Package pkg2 = PackageTest.createTestPackage(user.getOrg());
        errata.addPackage(pkg1);
        errata.addPackage(pkg2);
        ErrataFactory.save(errata);

        int initialNumPkgs = handler.listPackages(admin, errata.getAdvisory()).size();

        // execute
        List<Integer> pkgIds = new ArrayList<>();
        pkgIds.add(pkg2.getId().intValue());
        int numPkgsRemoved = handler.removePackages(admin, errata.getAdvisory(), pkgIds);

        // verify
        assertEquals(1, numPkgsRemoved);

        int resultNumPkgs = handler.listPackages(admin, errata.getAdvisory()).size();
        assertEquals(initialNumPkgs - 1, resultNumPkgs);

        boolean found1 = false, found2 = false;
        for (Package pkg : errata.getPackages()) {
            if (pkg.getId().equals(pkg1.getId())) {
                found1 = true;
            }
            if (pkg.getId().equals(pkg2.getId())) {
                found2 = true;
            }
        }
        assertTrue(found1);
        assertFalse(found2);
    }

    @Test
    public void testCloneErrata() throws Exception {
        cloneErrataTest(false);
    }

    @Test
    public void testCloneVendorErrata() throws Exception {
        cloneErrataTest(true);
    }

    private void cloneErrataTest(boolean vendor) throws Exception {
        Channel channel = ChannelFactoryTest.createTestChannel(admin);
        channel.setOrg(admin.getOrg());

        Package errataPack = PackageTest.createTestPackage(admin.getOrg());
        Package chanPack = PackageTest.createTestPackage(admin.getOrg());
        //we have to set the 2nd package to a different EVR to not violate a
        //      unique constraint
        PackageEvr evr =  PackageEvrFactory.lookupOrCreatePackageEvr("45", "99", "983",
                chanPack.getPackageType());
        chanPack.setPackageName(errataPack.getPackageName());
        chanPack.setPackageEvr(evr);

        channel.addPackage(chanPack);

        Long orgId = admin.getOrg().getId();
        if (vendor) {
            orgId = null;
        }
        Errata toClone = ErrataFactoryTest.createTestErrata(orgId);
        toClone.addPackage(errataPack);

        List<String> errata = List.of(toClone.getAdvisory());

        Object[] returnValue = handler.clone(admin, channel.getLabel(), errata);
        assertEquals(1, returnValue.length);

        Errata cloned = ErrataFactory.lookupById(((Errata)returnValue[0]).getId());
        assertNotSame(toClone.getId(), cloned.getId());

        Set<Channel> channels = cloned.getChannels();
        assertEquals(1, channels.size());

        Channel sameChannel = ((Channel)channels.toArray()[0]);
        assertEquals(channel, sameChannel);

        Set<Package> packs = sameChannel.getPackages();
        assertEquals(packs.size(), 2);

       assertTrue(packs.contains(errataPack));
       assertTrue(packs.contains(chanPack));

    }

    @Test
    public void testCreate() throws Exception {

        Channel channel = ChannelFactoryTest.createBaseChannel(admin);
        channel.setOrg(admin.getOrg());

        Map<String, Object> errataInfo = new HashMap<>();

        String advisoryName = TestUtils.randomString();
        populateErrataInfo(errataInfo);
        errataInfo.put("advisory_name", advisoryName);

        List<Integer> packages = new ArrayList<>();
        List<Map<String, Object>> bugs = new ArrayList<>();
        List<String> keywords = new ArrayList<>();
        List<String> channels = List.of(channel.getLabel());

        Errata errata = handler.create(admin, errataInfo,
                bugs, keywords, packages, channels);

        Errata result = ErrataFactory.lookupByAdvisoryAndOrg(advisoryName, admin.getOrg());
        assertEquals(errata, result);
        assertEquals(advisoryName + "-" + errata.getAdvisoryRel(),
                result.getAdvisory());

    }

    @Test
    public void testDelete() throws Exception {
        Errata errata = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        Errata check = ErrataManager.lookupErrata(errata.getId(), user);
        assertEquals(check.getAdvisory(), errata.getAdvisory());
        assertEquals(check.getId(), errata.getId());

        // delete an errata
        int result = handler.delete(admin, errata.getAdvisory());
        assertEquals(1, result);
        errata = TestUtils.reload(errata);
        assertNull(errata);
    }

    private void populateErrataInfo(Map<String, Object> errataInfo) {
        errataInfo.put("synopsis", TestUtils.randomString());
        errataInfo.put("advisory_release", 2);
        errataInfo.put("advisory_type", "Bug Fix Advisory");
        errataInfo.put("advisory_status", AdvisoryStatus.FINAL.getMetadataValue());
        errataInfo.put("product", TestUtils.randomString());
        errataInfo.put("topic", TestUtils.randomString());
        errataInfo.put("description", TestUtils.randomString());
        errataInfo.put("solution", TestUtils.randomString());
        errataInfo.put("references", TestUtils.randomString());
        errataInfo.put("notes", TestUtils.randomString());
        errataInfo.put("severity", "important");
    }

    @Test
    public void testAdvisoryLength() throws Exception {
        Channel channel = ChannelFactoryTest.createBaseChannel(admin);

        Map<String, Object> errataInfo = new HashMap<>();


        String advisoryName = RandomStringUtils.randomAscii(101);
        populateErrataInfo(errataInfo);
        errataInfo.put("advisory_name", advisoryName);

        List<Integer> packages = new ArrayList<>();
        List<Map<String, Object>> bugs = new ArrayList<>();
        List<String> keywords = new ArrayList<>();
        List<String> channels = List.of(channel.getLabel());

        try {
            handler.create(admin, errataInfo, bugs, keywords, packages, channels);
            fail("large advisory name was accepted");
        }
        catch (Exception e) {
            // we expect this to fail
            assertTrue(true);
        }
    }

    @Test
    public void testAdvisoryReleaseAboveMax() throws Exception {
        Channel channel = ChannelFactoryTest.createBaseChannel(admin);
        channel.setOrg(admin.getOrg());
        Map<String, Object> errataInfo = new HashMap<>();

        String advisoryName = TestUtils.randomString();
        populateErrataInfo(errataInfo);
        errataInfo.put("advisory_name", advisoryName);
        errataInfo.put("advisory_release", 10000);

        List<Integer> packages = new ArrayList<>();
        List<Map<String, Object>> bugs = new ArrayList<>();
        List<String> keywords = new ArrayList<>();
        List<String> channels = List.of(channel.getLabel());

        try {
            handler.create(admin, errataInfo, bugs, keywords, packages, channels);
            fail("large advisory release was accepted");
        }
        catch (InvalidAdvisoryReleaseException iare) {
            // we expect this to fail
            assertTrue(true);
        }
    }

    @Test
    public void testAdvisoryReleaseAtMax() throws Exception {
        Channel channel = ChannelFactoryTest.createBaseChannel(admin);
        channel.setOrg(admin.getOrg());
        Map<String, Object> errataInfo = new HashMap<>();

        String advisoryName = TestUtils.randomString();
        populateErrataInfo(errataInfo);
        errataInfo.put("advisory_name", advisoryName);
        errataInfo.put("advisory_release", 9999);

        List<Integer> packages = new ArrayList<>();
        List<Map<String, Object>> bugs = new ArrayList<>();
        List<String> keywords = new ArrayList<>();
        List<String> channels = List.of(channel.getLabel());

        Errata errata = handler.create(admin, errataInfo, bugs, keywords, packages, channels);

        assertEquals(ErrataManager.MAX_ADVISORY_RELEASE,
                errata.getAdvisoryRel().longValue());
    }

    /**
     * Note:
     * custom errata --> orgId != null
     * vendor errata --> orgId == null
     **/
    @Test
    public void testPublishCustomErrata() throws Exception {
        Errata e = ErrataFactoryTest.createTestErrata(admin.getOrg().getId());
        Channel channel = ChannelFactoryTest.createBaseChannel(admin);
        channel.setOrg(admin.getOrg());
        List<String> channels = List.of(channel.getLabel());
        Errata published = handler.publish(admin, e.getAdvisoryName(), channels);

        assertEquals(e.getAdvisory(), published.getAdvisory());
    }

    /**
     * Note:
     * custom errata --> orgId != null
     * vendor errata --> orgId == null
     **/
    @Test
    public void testPublishVendorErrata() throws Exception {
        Errata e = ErrataFactoryTest.createTestErrata(admin.getOrg().getId());
        e.setOrg(null); // let the errata be a vendor one
        Channel channel = ChannelFactoryTest.createBaseChannel(admin);
        channel.setOrg(admin.getOrg());
        List<String> channels = List.of(channel.getLabel());
        Errata published = handler.publish(admin, e.getAdvisoryName(), channels);

        assertEquals(e.getAdvisory(), published.getAdvisory());
    }

    @Test
    public void testCreateErrataAdvisoryStatus() throws Exception {
        Channel channel = ChannelFactoryTest.createBaseChannel(admin);
        channel.setOrg(admin.getOrg());
        Map<String, Object> errataInfo = new HashMap<>();

        String advisoryName = TestUtils.randomString();
        errataInfo.put("advisory_name", advisoryName);
        errataInfo.put("synopsis", TestUtils.randomString());
        errataInfo.put("advisory_release", 2);
        errataInfo.put("advisory_type", "Bug Fix Advisory");
        errataInfo.put("product", TestUtils.randomString());
        errataInfo.put("topic", TestUtils.randomString());
        errataInfo.put("description", TestUtils.randomString());
        errataInfo.put("solution", TestUtils.randomString());
        errataInfo.put("references", TestUtils.randomString());
        errataInfo.put("notes", TestUtils.randomString());
        errataInfo.put("severity", "important");

        List<Integer> packages = new ArrayList<>();
        List<Map<String, Object>> bugs = new ArrayList<>();
        List<String> keywords = new ArrayList<>();
        List<String> channels = List.of(channel.getLabel());

        // default path
        Errata errata = handler.create(admin, errataInfo, bugs, keywords, packages, channels);
        assertEquals(AdvisoryStatus.FINAL, errata.getAdvisoryStatus());

        // explicit status
        errataInfo.put("advisory_name", advisoryName + "-2");
        errataInfo.put("advisory_status", "testing");
        Errata testing = handler.create(admin, errataInfo, bugs, keywords, packages, channels);
        assertEquals(AdvisoryStatus.TESTING, testing.getAdvisoryStatus());
    }
}
