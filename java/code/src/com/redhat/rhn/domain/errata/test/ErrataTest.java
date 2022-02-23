/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.errata.Bug;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.errata.ErrataFile;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.rhnpackage.test.PackageManagerTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.TestUtils;

import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;

/**
 * ErrataTest
 */
public class ErrataTest extends BaseTestCaseWithUser {

    public void testNotificationQueue() throws Exception {
        Channel c = ChannelFactoryTest.createBaseChannel(user);
        Errata e = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        e.addChannel(c);
        ErrataFactory.save(e);
        Long id = e.getId(); //get id for later
        e.addNotification(new Date()); //add one
        e.addNotification(new Date()); //add another
        assertEquals(1, e.getNotificationQueue().size()); //should be only 1
        //save errata and evict
        ErrataFactory.save(e);
        flushAndEvict(e);

        Errata e2 = ErrataManager.lookupErrata(id, user); //lookup the errata
        assertEquals(1, e2.getNotificationQueue().size()); //should be only 1
    }

    /**
     * Test the bugs set in the Errata class. Make sure we can
     * add and store bugs.
     * @throws Exception something bad happened
     */
    public void testBugs() throws Exception {
        Errata errata = ErrataFactoryTest.createTestErrata(user.getOrg().getId());

        Bug bug1 = new Bug();
        bug1.setId(1001L);
        bug1.setSummary("This is a test summary");

        Bug bug2 = new Bug();
        bug2.setId(1002L);
        bug2.setSummary("This is another test summary");

        errata.addBug(bug1);
        errata.addBug(bug2);

        assertEquals(errata.getBugs().size(), 2);
        ErrataFactory.save(errata);
        Long id = errata.getId();

        //Evict so we know we're going to the db for the next one
        flushAndEvict(errata);
        Errata errata2 = ErrataManager.lookupErrata(id, user);

        assertEquals(id, errata2.getId());
        assertEquals(errata2.getBugs().size(), 2);
        errata2.removeBug(bug1.getId());
        assertEquals(errata2.getBugs().size(), 1);
    }

    /**
     * Test the keywords set in the Errata class. Make sure we
     * can add and store keywords.
     * @throws Exception something bad happened
     */
    public void testKeywords() throws Exception {
        Errata errata = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        assertTrue(errata instanceof Errata);
        errata.addKeyword("yankee");
        errata.addKeyword("hotel");
        errata.addKeyword("foxtrot");

        // errata already has one keyword
        assertEquals(4, errata.getKeywords().size());
        ErrataFactory.save(errata);
    }

    /**
     * Test the packages set in
     * @throws Exception something bad happened
     */
    public void testPackages() throws Exception {
        Errata errata = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        assertTrue(errata instanceof Errata);
        Package pkg = PackageTest.createTestPackage(user.getOrg());
        errata.addPackage(pkg);

        assertEquals(2, errata.getPackages().size());
        ErrataFactory.save(errata);
    }

    public void testAddChannelsToErrata() throws Exception {
        Errata e = ErrataFactoryTest.createTestErrata(
                user.getOrg().getId());
        assertTrue(e.getFiles().size() > 0);
        assertTrue(e.getPackages().size() > 0);
        Channel c = ChannelTestUtils.createTestChannel(user);
        Package p = PackageManagerTest.addPackageToChannel("some-errata-package", c);
        c = (Channel) reload(c);

        // Add the package to an errataFile
        ErrataFile ef;
        ef = ErrataFactory.createErrataFile(ErrataFactory.
                lookupErrataFileType("RPM"),
                    "SOME FAKE CHECKSUM",
                    "testAddChannelsToErrata" + TestUtils.randomString(), new HashSet());
        ef.addPackage(p);
        e.addFile(ef);

        e.addPackage(p);
        e.addChannel(c);

        ErrataFactory.save(e);
        e = (Errata) reload(e);

        assertEquals(1, e.getChannels().size());



        // Now test clearing it out
        e.clearChannels();
        e = (Errata) TestUtils.saveAndReload(e);
        assertTrue(e.getChannels() == null || e.getChannels().size() == 0);
        Iterator i = e.getFiles().iterator();
        boolean matched = false;
        while (i.hasNext()) {
            ErrataFile f1 = (ErrataFile) i.next();
            assertNotNull(f1.getChannels());
            assertTrue(f1.getChannels() == null || f1.getChannels().size() == 0);
            matched = true;
        }
        assertTrue("didnt match the erratafile", matched);
    }


    /**
     * Test bean methods of Errata class
     * @throws Exception something bad happened
     */
    public void testBeanMethodsPublished() throws Exception {
        Errata err = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        assertTrue(err instanceof Errata);
        runBeanMethodsTest(err, 1);
    }

    private void runBeanMethodsTest(Errata err, int idOffset) throws Exception {
        Long one = 3475L + idOffset;
        Long two = 5438L + idOffset;
        String foo = "foo";
        String product = "Product Enhancement Advisory";
        String security = "Security Advisory";
        String bug = "Bug Fix Advisory";
        Date now = new Date();

        err.setAdvisory(foo);
        assertTrue(err.getAdvisory().equals("foo"));
        err.setAdvisory(null);
        assertNull(err.getAdvisory());

        err.setAdvisoryName(foo);
        assertTrue(err.getAdvisoryName().equals("foo"));
        err.setAdvisoryName(null);
        assertNull(err.getAdvisoryName());

        err.setAdvisoryRel(one);
        assertTrue(err.getAdvisoryRel().equals(one));
        assertFalse(err.getAdvisoryRel().equals(two));
        err.setAdvisoryRel(null);
        assertNull(err.getAdvisoryRel());

        err.setAdvisoryType(foo);
        assertTrue(err.getAdvisoryType().equals("foo"));
        assertFalse(err.isBugFix());
        assertFalse(err.isProductEnhancement());
        assertFalse(err.isSecurityAdvisory());
        err.setAdvisoryType(bug);
        assertTrue(err.isBugFix());
        assertFalse(err.isProductEnhancement());
        err.setAdvisoryType(product);
        assertTrue(err.isProductEnhancement());
        assertFalse(err.isSecurityAdvisory());
        err.setAdvisoryType(security);
        assertTrue(err.isSecurityAdvisory());
        assertFalse(err.isBugFix());
        err.setAdvisoryType(null);
        assertNull(err.getAdvisoryType());
        assertFalse(err.isSecurityAdvisory());

        err.setDescription(foo);
        assertTrue(err.getDescription().equals("foo"));
        err.setDescription(null);
        assertNull(err.getDescription());

        err.setIssueDate(now);
        assertTrue(err.getIssueDate().equals(now));
        err.setIssueDate(null);
        assertNull(err.getIssueDate());

        err.setLastModified(now);
        assertTrue(err.getLastModified().equals(now));
        err.setLastModified(null);
        assertNull(err.getLastModified());

        err.setLocallyModified(Boolean.TRUE);
        assertTrue(err.getLocallyModified());
        err.setLocallyModified(Boolean.FALSE);
        assertFalse(err.getLocallyModified());

        err.setNotes(foo);
        assertTrue(err.getNotes().equals("foo"));
        err.setNotes(null);
        assertNull(err.getNotes());

        err.setProduct(foo);
        assertTrue(err.getProduct().equals("foo"));
        err.setProduct(null);
        assertNull(err.getProduct());

        err.setRefersTo(foo);
        assertTrue(err.getRefersTo().equals("foo"));
        err.setRefersTo(null);
        assertNull(err.getRefersTo());

        err.setSolution(foo);
        assertTrue(err.getSolution().equals("foo"));
        err.setSolution(null);
        assertNull(err.getSolution());

        err.setSynopsis(foo);
        assertTrue(err.getSynopsis().equals("foo"));
        err.setSynopsis(null);
        assertNull(err.getSynopsis());

        err.setTopic(foo);
        assertTrue(err.getTopic().equals("foo"));
        err.setTopic(null);
        assertNull(err.getTopic());

        err.setUpdateDate(now);
        assertTrue(err.getUpdateDate().equals(now));
        err.setUpdateDate(null);
        assertNull(err.getUpdateDate());

        Org org1 = user.getOrg();

        err.setOrg(org1);
        assertTrue(err.getOrg().equals(org1));
        err.setOrg(null);
        assertNull(err.getOrg());

        Channel c1 = ChannelFactoryTest.createTestChannel(user.getOrg());
        err.addChannel(c1);
        assertEquals(1, err.getChannels().size());
        err.setChannels(null);
        assertNull(err.getChannels());
    }
}
