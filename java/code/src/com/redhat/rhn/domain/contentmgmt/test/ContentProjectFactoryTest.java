/**
 * Copyright (c) 2018 SUSE LLC
 *
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

package com.redhat.rhn.domain.contentmgmt.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.ContentManagementException;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.domain.contentmgmt.ContentProjectHistoryEntry;
import com.redhat.rhn.domain.contentmgmt.PackageFilter;
import com.redhat.rhn.domain.contentmgmt.ProjectSource;
import com.redhat.rhn.domain.contentmgmt.SoftwareEnvironmentTarget;
import com.redhat.rhn.domain.contentmgmt.SoftwareProjectSource;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import java.util.List;
import java.util.Optional;

import static com.redhat.rhn.domain.contentmgmt.ProjectSource.Type.SW_CHANNEL;
import static com.redhat.rhn.domain.contentmgmt.ProjectSource.Type.lookupByLabel;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * Tests for {@link com.redhat.rhn.domain.contentmgmt.ContentProjectFactory}
 */
public class ContentProjectFactoryTest extends BaseTestCaseWithUser {

    public void testCreateEnvironments() {
        ContentProject cp = new ContentProject("project1", "Project 1", "This is project 1", user.getOrg());
        ContentProjectFactory.save(cp);

        ContentEnvironment envdev = new ContentEnvironment("dev", "Development", null, cp);
        ContentProjectFactory.save(envdev);
        cp.setFirstEnvironment(envdev);

        ContentEnvironment envtest = new ContentEnvironment("test", "Test", null, cp);
        ContentProjectFactory.save(envtest);
        ContentProjectFactory.insertEnvironment(envtest, of(envdev));

        ContentEnvironment envprod = new ContentEnvironment("prod", "Production", null, cp);
        ContentProjectFactory.save(envprod);
        ContentProjectFactory.insertEnvironment(envprod, of(envtest));

        ContentEnvironment envint = new ContentEnvironment("int", "Integration", null, cp);
        ContentProjectFactory.save(envint);
        ContentProjectFactory.insertEnvironment(envint, of(envdev));

        HibernateFactory.getSession().flush();

        ContentProject fromDb = ContentProjectFactory.lookupProjectByLabelAndOrg("project1", user.getOrg()).get();
        assertEquals("project1", fromDb.getLabel());
        assertEquals("This is project 1", fromDb.getDescription());

        ContentEnvironment first = fromDb.getFirstEnvironmentOpt().get();
        assertEquals("dev", first.getLabel());
        assertEquals("Development", first.getName());

        ContentEnvironment second = first.getNextEnvironmentOpt().get();
        assertEquals("int", second.getLabel());
        assertEquals("Integration", second.getName());

        ContentEnvironment third = second.getNextEnvironmentOpt().get();
        assertEquals("test", third.getLabel());
        assertEquals("Test", third.getName());

        ContentEnvironment fourth = third.getNextEnvironmentOpt().get();
        assertEquals("prod", fourth.getLabel());
        assertEquals("Production", fourth.getName());

        assertEquals(second, first.getNextEnvironmentOpt().get());
        assertEquals(empty(), first.getPrevEnvironmentOpt());

        assertEquals(second, third.getPrevEnvironmentOpt().get());

        assertEquals(first, second.getPrevEnvironmentOpt().get());
        assertEquals(empty(), fourth.getNextEnvironmentOpt());
    }

    public void testRemoveEnvironments() {
        ContentProject cp = new ContentProject("project1", "Project 1", "This is project 1", user.getOrg());
        ContentProjectFactory.save(cp);

        ContentEnvironment envdev = new ContentEnvironment("dev", "Development", null, cp);
        ContentProjectFactory.save(envdev);
        cp.setFirstEnvironment(envdev);

        ContentEnvironment envtest = new ContentEnvironment("test", "Test", null, cp);
        ContentProjectFactory.save(envtest);
        ContentProjectFactory.insertEnvironment(envtest, of(envdev));

        ContentEnvironment envprod = new ContentEnvironment("prod", "Production", null, cp);
        ContentProjectFactory.save(envprod);
        ContentProjectFactory.insertEnvironment(envprod, of(envtest));

        HibernateFactory.getSession().flush();
        envtest = TestUtils.reload(envtest);

        ContentProjectFactory.removeEnvironment(envtest);

        HibernateFactory.getSession().flush();

        ContentProject fromDb = ContentProjectFactory.lookupProjectByLabelAndOrg("project1", user.getOrg()).get();
        assertEquals("project1", fromDb.getLabel());
        assertEquals("This is project 1", fromDb.getDescription());

        ContentEnvironment first = fromDb.getFirstEnvironmentOpt().get();
        assertEquals("dev", first.getLabel());
        assertEquals("Development", first.getName());

        ContentEnvironment second = first.getNextEnvironmentOpt().get();
        assertEquals("prod", second.getLabel());
        assertEquals("Production", second.getName());

        assertEquals(second, first.getNextEnvironmentOpt().get());
        assertEquals(empty(), first.getPrevEnvironmentOpt());

        ContentProjectFactory.removeEnvironment(envdev);

        HibernateFactory.getSession().flush();

        fromDb = ContentProjectFactory.lookupProjectByLabelAndOrg("project1", user.getOrg()).get();
        ContentEnvironment newfirst = fromDb.getFirstEnvironmentOpt().get();
        assertEquals("prod", newfirst.getLabel());
        assertEquals("Production", newfirst.getName());
    }

    /**
     * Test removing a single environment and check that the reference
     * to the first environment of the project is updated
     */
    public void testRemoveSingleEnvironment() {
        ContentProject cp = new ContentProject("project1", "Project 1", "This is project 1", user.getOrg());
        ContentProjectFactory.save(cp);

        ContentEnvironment envdev = new ContentEnvironment("dev", "Development", null, cp);
        ContentProjectFactory.save(envdev);
        cp.setFirstEnvironment(envdev);

        ContentProjectFactory.removeEnvironment(envdev);
        assertFalse(ContentProjectFactory.lookupEnvironmentByLabelAndProject("dev", cp).isPresent());
        assertFalse(cp.getFirstEnvironmentOpt().isPresent());
    }

    /**
     * Tests saving a ContentProject and listing it from the DB.
     */
    public void testSaveAndList() {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);

        Org org2 = OrgFactory.createOrg();
        org2.setName("test org for content project");
        HibernateFactory.getSession().save(org2);
        ContentProject cp2 = new ContentProject("cplabel2", "cpname2", "cpdesc2", org2);
        ContentProjectFactory.save(cp2);

        List<ContentProject> contentProjects = ContentProjectFactory.listProjects(user.getOrg());
        assertEquals(1, contentProjects.size());
        ContentProject fromDb = contentProjects.get(0);
        assertNotNull(fromDb.getId());
        assertEquals(cp.getLabel(), fromDb.getLabel());
        assertEquals(cp.getDescription(), fromDb.getDescription());
        assertEquals(cp.getName(), fromDb.getName());
        assertEquals(cp.getOrg(), fromDb.getOrg());
        assertEquals(empty(), cp.getFirstEnvironmentOpt());
    }

    /**
     * Tests a Content Project lookup when different Orgs are involved
     */
    public void testLookupCrossOrg() {
        Org org1 = user.getOrg();
        ContentProject cp1 = new ContentProject("cplabel", "cpname", "cpdesc", org1);
        ContentProjectFactory.save(cp1);
        Org org2 = UserTestUtils.createNewOrgFull("testOrg2");
        UserTestUtils.createUser("testUserIn2ndOrg", org2.getId());
        ContentProject cp2 = new ContentProject("cplabel2", "cpname2", "cpdesc2", org2);
        ContentProjectFactory.save(cp2);

        // happy paths
        Optional<ContentProject> fromDb1 = ContentProjectFactory.lookupProjectByLabelAndOrg("cplabel", org1);
        assertEquals(cp1, fromDb1.get());
        Optional<ContentProject> fromDb2 = ContentProjectFactory.lookupProjectByLabelAndOrg("cplabel2", org2);
        assertEquals(cp2, fromDb2.get());

        // cross-org access test
        fromDb1 = ContentProjectFactory.lookupProjectByLabelAndOrg("cplabel", org2);
        assertFalse(fromDb1.isPresent());
        fromDb2 = ContentProjectFactory.lookupProjectByLabelAndOrg("cplabel2", org1);
        assertFalse(fromDb2.isPresent());
    }

    public void testListEnvironments() {
        ContentProject cp = new ContentProject("project1", "Project 1", "This is project 1", user.getOrg());
        ContentProjectFactory.save(cp);

        ContentEnvironment envdev = new ContentEnvironment("dev", "Development", null, cp);
        ContentProjectFactory.save(envdev);
        cp.setFirstEnvironment(envdev);

        ContentEnvironment envtest = new ContentEnvironment("test", "Test", null, cp);
        ContentProjectFactory.save(envtest);
        ContentProjectFactory.insertEnvironment(envtest, of(envdev));

        ContentEnvironment envprod = new ContentEnvironment("prod", "Production", null, cp);
        ContentProjectFactory.save(envprod);
        ContentProjectFactory.insertEnvironment(envprod, of(envtest));

        ContentEnvironment envint = new ContentEnvironment("int", "Integration", null, cp);
        ContentProjectFactory.save(envint);
        ContentProjectFactory.insertEnvironment(envint, of(envdev));

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        ContentProject fromDb = ContentProjectFactory.lookupProjectByLabelAndOrg("project1", user.getOrg()).get();
        List<ContentEnvironment> envs = ContentProjectFactory.listProjectEnvironments(fromDb);
        assertEquals("dev", envs.get(0).getLabel());
        assertEquals("int", envs.get(1).getLabel());
        assertEquals("test", envs.get(2).getLabel());
        assertEquals("prod", envs.get(3).getLabel());
    }

    /**
     * Tests adding/removing sources on a project
     *
     * @throws Exception if anything goes wrong
     */
    public void testProjectSource() throws Exception {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);

        Channel baseChannel = ChannelTestUtils.createBaseChannel(user);
        ProjectSource swSource = new SoftwareProjectSource(cp, baseChannel);
        cp.addSource(swSource);
        ContentProjectFactory.save(swSource);

        List<ProjectSource> fromDb = ContentProjectFactory.listProjectSourcesByProject(cp);
        assertEquals(singletonList(swSource), fromDb);

        Channel childChannel = ChannelTestUtils.createChildChannel(user, baseChannel);
        ProjectSource swSource2 = new SoftwareProjectSource(cp, childChannel);
        cp.addSource(swSource2);
        ContentProjectFactory.save(swSource2);

        fromDb = ContentProjectFactory.listProjectSourcesByProject(cp);
        assertEquals(asList(swSource, swSource2), fromDb);

        cp.removeSource(swSource2);
        fromDb = ContentProjectFactory.listProjectSourcesByProject(cp);
        assertEquals(singletonList(swSource), fromDb);
    }

    /**
     * Tests adding/removing filters
     */
    public void testFilter() {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);

        ContentFilter packageFilter = new PackageFilter();
        packageFilter.setName("No-kernel-package filter");
        packageFilter.setCriteria("criteria1");
        packageFilter.setOrg(user.getOrg());
        ContentProjectFactory.save(packageFilter);

        ContentFilter packageFilter2 = new PackageFilter();
        packageFilter2.setName("No-kernel-package filter2");
        packageFilter2.setCriteria("criteria2");
        packageFilter2.setOrg(user.getOrg());
        ContentProjectFactory.save(packageFilter2);

        cp.addFilter(packageFilter);
        cp.addFilter(packageFilter2);

        ContentProject fromDb = ContentProjectFactory.lookupProjectByLabelAndOrg(cp.getLabel(), user.getOrg()).get();
        assertEquals(asList(packageFilter, packageFilter2), fromDb.getFilters());

        cp.removeFilter(packageFilter);
        fromDb = ContentProjectFactory.lookupProjectByLabelAndOrg(cp.getLabel(), user.getOrg()).get();
        assertEquals(asList(packageFilter2), fromDb.getFilters());

        // check if the order is honored if filters are "swapped"
        cp.addFilter(packageFilter);
        fromDb = ContentProjectFactory.lookupProjectByLabelAndOrg(cp.getLabel(), user.getOrg()).get();
        assertEquals(asList(packageFilter2, packageFilter), fromDb.getFilters());
    }

    /**
     * Tests adding/removing filters in wrong organization
     */
    public void testFilterWrongOrg() {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", OrgFactory.createOrg());
        ContentProjectFactory.save(cp);

        ContentFilter packageFilter = new PackageFilter();
        packageFilter.setName("No-kernel-package filter");
        packageFilter.setCriteria("criteria1");
        packageFilter.setOrg(user.getOrg());
        ContentProjectFactory.save(packageFilter);

        try {
            cp.addFilter(packageFilter);
            fail("A ContentManagementException should have been thrown.");
        } catch (ContentManagementException e) {
            // no op
        }

        try {
            cp.removeFilter(packageFilter);
            fail("A ContentManagementException should have been thrown.");
        } catch (ContentManagementException e) {
            // no op
        }
    }

    /**
     * Test saving environment target
     * @throws Exception if anything goes wrong
     */
    public void testEnvironmentTarget() throws Exception {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        ContentEnvironment envdev = new ContentEnvironment("dev", "Development", null, cp);
        ContentProjectFactory.save(envdev);
        cp.setFirstEnvironment(envdev);
        Channel channel = ChannelTestUtils.createBaseChannel(user);

        SoftwareEnvironmentTarget target = new SoftwareEnvironmentTarget();
        target.setContentEnvironment(envdev);
        target.setChannel(channel);
        ContentProjectFactory.save(target);

        assertEquals(target, ContentProjectFactory.lookupEnvironmentTargetByChannel(channel).get());
    }

    /**
     * Test saving Content Project history entries
     */
    public void testProjectHistory() {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);

        ContentProjectHistoryEntry fstEntry = new ContentProjectHistoryEntry();
        fstEntry.setMessage("First Content Project build");
        fstEntry.setUser(user);

        ContentProjectHistoryEntry sndEntry = new ContentProjectHistoryEntry();
        sndEntry.setMessage("Second Content Project build");
        sndEntry.setUser(user);

        ContentProjectFactory.addHistoryEntryToProject(cp, fstEntry);
        ContentProjectFactory.addHistoryEntryToProject(cp, sndEntry);

        ContentProject fromDb = ContentProjectFactory.lookupProjectByLabelAndOrg(cp.getLabel(), user.getOrg()).get();
        ContentProjectHistoryEntry fstEntryFromDb = fromDb.getHistoryEntries().get(0);
        ContentProjectHistoryEntry sndEntryFromDb = fromDb.getHistoryEntries().get(1);

        assertEquals(1L, fstEntryFromDb.getVersion().longValue());
        assertEquals(2L, sndEntryFromDb.getVersion().longValue());
        assertEquals(fstEntry.getMessage(), fstEntryFromDb.getMessage());
        assertEquals(sndEntry.getMessage(), sndEntryFromDb.getMessage());

        assertTrue(!sndEntryFromDb.getCreated().before(fstEntryFromDb.getCreated()));

        UserFactory.deleteUser(user.getId());
        fstEntryFromDb = (ContentProjectHistoryEntry) HibernateFactory.reload(fstEntryFromDb);
        sndEntryFromDb = (ContentProjectHistoryEntry) HibernateFactory.reload(sndEntryFromDb);
        assertNull(fstEntryFromDb.getUser());
        assertNull(sndEntryFromDb.getUser());
    }

    /**
     * Test looking up Source by label
     */
    public void testSourceTypeLookup() {
        assertEquals(SW_CHANNEL, lookupByLabel("software"));

        try {
            lookupByLabel("thisdoesntexist");
            fail("An exception should have been thrown.");
        }
        catch (IllegalArgumentException e) {
            // expected
        }
    }
}
