/**
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
package com.redhat.rhn.domain.errata.test;

import static java.util.Optional.empty;

import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.errata.Bug;
import com.redhat.rhn.domain.errata.ClonedErrata;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.errata.ErrataFile;
import com.redhat.rhn.domain.errata.Severity;
import com.redhat.rhn.domain.errata.impl.PublishedErrata;
import com.redhat.rhn.domain.errata.impl.PublishedErrataFile;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactory;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.frontend.action.channel.manage.PublishErrataHelper;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.errata.test.ErrataManagerTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.utils.Opt;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

/**
 * ErrataFactoryTest
 */
public class ErrataFactoryTest extends BaseTestCaseWithUser {

    public static Bug createPublishedBug(Long longIn, String stringIn) {
        return ErrataFactory.createPublishedBug(longIn, stringIn,
                "https://bugzilla.redhat.com/show_bug.cgi?id=" + longIn);
    }

    public void testPublishToChannel()  throws Exception {
        Errata e = ErrataFactoryTest.createTestPublishedErrata(user.getOrg().getId());
        //add bugs, keywords, and packages so we have something to work with...
        e.addBug(ErrataManagerTest.createNewPublishedBug(42L, "test bug 1"));
        e.addBug(ErrataManagerTest.createNewPublishedBug(43L, "test bug 2"));
        e.addPackage(PackageTest.createTestPackage(user.getOrg()));
        e.addKeyword("foo");
        e.addKeyword("bar");
        ErrataManager.storeErrata(e); //save changes

        Channel channel = ChannelFactoryTest.createTestChannel(user);
        channel.setOrg(user.getOrg());

        Package errataPack = PackageTest.createTestPackage(user.getOrg());
        Package chanPack = PackageTest.createTestPackage(user.getOrg());
        //we have to set the 2nd package to a different EVR to not violate a
        //      unique constraint
        PackageEvr evr =  PackageEvrFactory.lookupOrCreatePackageEvr("45", "99", "983",
                errataPack.getPackageEvr().getPackageType());
        chanPack.setPackageName(errataPack.getPackageName());
        chanPack.setPackageEvr(evr);

        channel.addPackage(chanPack);
        e.addPackage(errataPack);

        List<Errata> errataList = new ArrayList<Errata>();
        errataList.add(e);
        List<Errata> publishedList = ErrataFactory.publishToChannel(errataList,
                channel, user, false);
        Errata published = publishedList.get(0);
        assertTrue(channel.getPackages().contains(errataPack));
        List<PublishedErrataFile> errataFile =
            ErrataFactory.lookupErrataFilesByErrataAndFileType(published.getId(), "RPM");
        assertTrue(errataFile.get(0).getPackages().contains(errataPack));

    }

    public void testCreateAndLookupVendorAndUserErrata() throws Exception {
        //create user published errata
        Errata userPublishedErrata = createTestPublishedErrata(user.getOrg().getId());
        assertTrue(userPublishedErrata instanceof PublishedErrata);
        assertNotNull(userPublishedErrata.getId());
        assertNotNull(userPublishedErrata.getAdvisory());

        //Lookup the user published errata
        Errata errata = ErrataFactory.lookupById(userPublishedErrata.getId());
        assertTrue(errata instanceof PublishedErrata);
        assertEquals(userPublishedErrata.getId(), errata.getId());
        assertEquals(userPublishedErrata.getAdvisory(), userPublishedErrata.getAdvisory());

        List<Errata> erratas = ErrataFactory.lookupVendorAndUserErrataByAdvisoryAndOrg(
                userPublishedErrata.getAdvisory(), user.getOrg());

        assertEquals(erratas.size(), 1);
        assertTrue(erratas.stream().allMatch(e -> e.getId().equals(userPublishedErrata.getId())));
        assertTrue(erratas.stream().allMatch(e -> e.getAdvisoryName().equals(userPublishedErrata.getAdvisoryName())));
        assertTrue(erratas.stream().allMatch(e -> e instanceof PublishedErrata));

        //create vendor published errata with same name as user published errata
        Errata vendorPublishedErrata = createTestPublishedErrata(null,
                Optional.of(userPublishedErrata.getAdvisory()));
        assertTrue(vendorPublishedErrata instanceof PublishedErrata);
        assertNotNull(vendorPublishedErrata.getId());
        assertNotNull(vendorPublishedErrata.getAdvisory());

        //Lookup the vendor published errata
        errata = ErrataFactory.lookupById(vendorPublishedErrata.getId());
        assertTrue(errata instanceof PublishedErrata);
        assertEquals(vendorPublishedErrata.getId(), errata.getId());
        assertEquals(vendorPublishedErrata.getAdvisory(), errata.getAdvisory());
        assertEquals(vendorPublishedErrata.getAdvisory(), userPublishedErrata.getAdvisory());

        //Lookup vendor and user published errata with the same name
        erratas = ErrataFactory.lookupVendorAndUserErrataByAdvisoryAndOrg(userPublishedErrata.getAdvisory(),
                user.getOrg());

        assertEquals(erratas.size(), 2);
        assertTrue(erratas.stream().allMatch(e -> e.getId().equals(vendorPublishedErrata.getId())
                || e.getId().equals(userPublishedErrata.getId())));
        assertTrue(erratas.stream().allMatch(e -> e.getAdvisoryName().equals(userPublishedErrata.getAdvisory())));
        assertTrue(erratas.stream().allMatch(e -> e instanceof PublishedErrata));
    }

    public void testCreateAndLookupErrata() throws Exception {
        Errata published = createTestPublishedErrata(user.getOrg().getId());
        assertTrue(published instanceof PublishedErrata);
        assertNotNull(published.getId());
        Long pubid = published.getId();
        String pubname = published.getAdvisoryName();

        //Lookup the errata
        Errata errata = ErrataFactory.lookupById(pubid);
        assertTrue(errata instanceof PublishedErrata);
        assertEquals(pubid, errata.getId());
        errata = ErrataFactory.lookupByAdvisoryAndOrg(pubname, user.getOrg());
        assertTrue(errata instanceof PublishedErrata);
        assertEquals(pubname, errata.getAdvisoryName());
    }

    public void testCreateAndLookupErrataNullOrg() throws Exception {
        //create an errata with null Org
        Errata published = createTestPublishedErrata(null);
        assertTrue(published instanceof PublishedErrata);
        assertNotNull(published.getId());
        Long pubid = published.getId();
        String pubname = published.getAdvisoryName();

        //Lookup the published errata by null Org
        Errata errata = ErrataFactory.lookupById(pubid);
        assertTrue(errata instanceof PublishedErrata);
        assertEquals(pubid, errata.getId());
        errata = ErrataFactory.lookupByAdvisoryAndOrg(pubname, null);
        assertTrue(errata instanceof PublishedErrata);
        assertEquals(pubname, errata.getAdvisoryName());

        //Lookup the published errata by user's Org
        errata = ErrataFactory.lookupByAdvisoryAndOrg(pubname, user.getOrg());
        assertNull(errata);
    }

    public void testLastModified() throws Exception {
        Errata published = createTestPublishedErrata(user.getOrg().getId());
        published = reload(published);
        assertNotNull(published.getLastModified());
    }

    public void testBugs() throws Exception {
        var e = createTestPublishedErrata(user.getOrg().getId());
        assertTrue(e.getBugs() == null || e.getBugs().size() == 0);
        e.addBug(ErrataFactoryTest.createPublishedBug(123L, "test bug"));
        assertEquals(1, e.getBugs().size());
    }

    public void testFiles() throws Exception {
        Set errataFilePackages = new HashSet();
        errataFilePackages.add(PackageTest.createTestPackage(user.getOrg()));
        ErrataFile ef = ErrataFactory.createUnpublishedErrataFile(ErrataFactory.
                                                              lookupErrataFileType("RPM"),
                                                                  "SOME FAKE CHECKSUM",
                                                                  "test erratafile " +
                                                                  TestUtils.randomString(),
                                                                  errataFilePackages);

        var e = createTestPublishedErrata(user.getOrg().getId());
        ef = ErrataFactory.createPublishedErrataFile(ErrataFactory.
                                                     lookupErrataFileType("RPM"),
                                                     "SOME FAKE CHECKSUM",
                                                     "test erratafile " +
                                                     TestUtils.randomString(),
                                                     errataFilePackages);
        assertEquals(1, e.getFiles().size());
        assertNull(ef.getId());
        assertNotNull(ef.getPackages());
        assertEquals(1, ef.getPackages().size());

        e.addFile(ef);
        TestUtils.saveAndFlush(e);

        assertNotNull(ef.getId());
        assertEquals(2, e.getFiles().size());
    }
    /**
     * Create an Errata for testing and commit it to the DB.
     * @param orgId the Org who owns this Errata
     * @return Errata created
     * @throws Exception something bad happened
     */
    public static Errata createTestErrata(Long orgId) throws Exception {
        return createTestErrata(orgId, empty());
    }

    /**
     * Create an Errata for testing and commit it to the DB.
     * @param orgId the Org who owns this Errata
     * @param advisory if specified, the advisory name
     * @return Errata created
     * @throws Exception something bad happened
     */
    public static Errata createTestErrata(Long orgId, Optional<String> advisory) throws Exception {
        Errata e = ErrataFactory.createPublishedErrata();
        fillOutErrata(e, orgId, advisory);
        ErrataFactory.save(e);
        return e;
    }

    /**
     * Creates and persists an errata that will be flagged as critical.
     *
     * @param orgId the org under which the errata exists
     * @return created errata
     * @throws Exception if the errata cannot be created
     */
    public static Errata createCriticalTestErrata(Long orgId) throws Exception {
        Errata e = ErrataFactory.createPublishedErrata();
        fillOutErrata(e, orgId, empty());
        e.setAdvisoryType(ErrataFactory.ERRATA_TYPE_SECURITY);
        ErrataFactory.save(e);
        return e;
    }

    public static Errata createTestPublishedErrata(Long orgId) throws Exception {
        return createTestPublishedErrata(orgId, empty());
    }

    public static Errata createTestPublishedErrata(Long orgId, Optional<String> advisory) throws Exception {
        //just pass to createTestErrata since published is the default
        return createTestErrata(orgId, advisory);
    }

    private static void fillOutErrata(Errata e, Long orgId, Optional<String> advisory) throws Exception {
        String name = Opt.fold(advisory, () -> "JAVA Test " + TestUtils.randomString(), Function.identity());
        Org org = null;
        if (orgId != null) {
            org = OrgFactory.lookupById(orgId);
            e.setOrg(org);
        }
        e.setAdvisory(name);
        e.setAdvisoryType(ErrataFactory.ERRATA_TYPE_BUG);
        e.setProduct("Red Hat Linux");
        e.setDescription("Test desc ..");
        e.setSynopsis("Test synopsis");
        e.setSolution("Test solution");
        e.setNotes("Test notes for test errata");
        e.setTopic("test topic");
        e.setRefersTo("rhn unit tests");
        e.setUpdateDate(new Date());
        e.setIssueDate(new Date());
        e.setAdvisoryName(name);
        e.setAdvisoryRel(2L);
        e.setLocallyModified(Boolean.FALSE);
        e.addKeyword("keyword");
        Package testPackage = PackageTest.createTestPackage(org);

        ErrataFile ef;
        Set errataFilePackages = new HashSet();
        errataFilePackages.add(testPackage);
        e.addPackage(testPackage);
        if (e.isPublished()) {
            ef = ErrataFactory.createPublishedErrataFile(ErrataFactory.
                    lookupErrataFileType("RPM"),
                        "SOME FAKE CHECKSUM",
                        "test errata file" + TestUtils.randomString(), errataFilePackages);
        }
        else {
            ef = ErrataFactory.createUnpublishedErrataFile(ErrataFactory.
                    lookupErrataFileType("RPM"),
                        "SOME FAKE CHECKSUM",
                        "test errata file", errataFilePackages);
        }

        e.addFile(ef);
        Severity s = new Severity();
        s.setLabel(Severity.IMPORTANT_LABEL);
        s.setRank(1);
        e.setSeverity(s);
    }

    public static void updateNeedsErrataCache(Long packageId, Long serverId,
            Long errataId) {
        WriteMode m =
            ModeFactory.
                getWriteMode("test_queries", "insert_into_rhnServerNeededCache");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("package_id", packageId);
        params.put("server_id", serverId);
        params.put("errata_id", errataId);
        m.executeUpdate(params);
    }

    public static void testLookupByOriginal() throws Exception {
        Long orgId = UserTestUtils.createOrg("testOrgLookupByOriginal");
        Org org = OrgFactory.lookupById(orgId);
        Errata published = createTestPublishedErrata(orgId);

        Long ceid = PublishErrataHelper.cloneErrataFaster(published.getId(), org);

        List list = ErrataFactory.lookupByOriginal(org, published);

        assertEquals(1, list.size());
        var clone = (ClonedErrata) list.get(0);
        assertTrue(clone.getOriginal().equals(published));
    }

    public void testListErrataChannelPackages() {
        try {
            Channel chan = ChannelTestUtils.createBaseChannel(user);
            Errata e = ErrataFactoryTest.createTestErrata(user.getId());
            Package p = PackageTest.createTestPackage(user.getOrg());
            chan.getErratas().add(e);
            chan.getPackages().add(p);
            e.getPackages().add(p);
            ChannelFactory.save(chan);

            chan = TestUtils.saveAndReload(chan);
            e = TestUtils.saveAndReload(e);
            p = TestUtils.saveAndReload(p);


            List<Long> list = ErrataFactory.listErrataChannelPackages(chan.getId(),
                    e.getId());
            assertContains(list, p.getId());

        }
        catch (Exception e) {
            assertTrue(false);
        }
    }

    /**
     * Test listing errata by channel
     *
     * @throws Exception if anything goes wrong
     */
    public void testListErrataByChannel() throws Exception {
        Channel chan = ChannelTestUtils.createBaseChannel(user);
        Errata e = ErrataFactoryTest.createTestErrata(user.getId());
        chan.getErratas().add(e);

        List<PublishedErrata> errata = ErrataFactory.listByChannel(user.getOrg(), chan);
        assertEquals(1, errata.size());
        assertEquals(e, errata.iterator().next());
    }
}

