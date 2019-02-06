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

package com.redhat.rhn.manager.contentmgmt.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.domain.contentmgmt.ProjectSource;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.EntityExistsException;
import com.redhat.rhn.manager.EntityNotExistsException;
import com.redhat.rhn.manager.contentmgmt.ContentManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import java.util.Arrays;
import java.util.Optional;

import static com.redhat.rhn.domain.contentmgmt.ProjectSource.Type.SW_CHANNEL;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;

/**
 * Tests for ContentManager
 */
public class ContentManagerTest extends BaseTestCaseWithUser {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
    }

    /**
     * Test creating & looking up Content Project
     */
    public void testLookupContentProject() {
        ContentProject cp = ContentManager.createProject("cplabel", "cpname", "description", user);
        assertNotNull(cp.getId());

        Optional<ContentProject> fromDb = ContentManager.lookupProject("cplabel", user);
        assertEquals(cp, fromDb.get());
    }

    /**
     * Test creating & looking up nonexisting Content Project
     */
    public void testLookupNonexistingProject() {
        assertFalse(ContentManager.lookupProject("idontexist", user).isPresent());
    }

    /**
     * Test creating & listing Content Projects
     */
    public void testListContentProjects() {
        ContentProject cp1 = ContentManager.createProject("cplabel1", "cpname1", "description1", user);
        ContentProject cp2 = ContentManager.createProject("cplabel2", "cpname2", "description2", user);
        Org rangersOrg = UserTestUtils.createNewOrgFull("rangers");
        User anotherAdmin = UserTestUtils.createUser("Chuck", rangersOrg.getId());
        anotherAdmin.addPermanentRole(RoleFactory.ORG_ADMIN);
        ContentProject cp3 = ContentManager.createProject("cplabel3", "cpname3", "description3", anotherAdmin);

        assertEquals(Arrays.asList(cp1, cp2), ContentManager.listProjects(user));
        assertEquals(singletonList(cp3), ContentManager.listProjects(anotherAdmin));
    }

    /**
     * Test multiple creating Content Project with same label
     */
    public void testMultipleCreateContentProject() {
        ContentManager.createProject("cplabel", "cpname", "description", user);
        try {
            ContentManager.createProject("cplabel", "differentname", null, user);
            fail("An exception should have been thrown");
        }
        catch (EntityExistsException e) {
            // expected
        }
    }

    /**
     * Test updating Content Project
     */
    public void testUpdateContentProject() {
        ContentProject cp = ContentManager.createProject("cplabel", "cpname", "description", user);
        ContentProject updated = ContentManager.updateProject(cp.getLabel(), of("new name"), of("new desc"), user);

        ContentProject fromDb = ContentManager.lookupProject("cplabel", user).get();
        assertEquals(fromDb, updated);
        assertEquals("new name", fromDb.getName());
        assertEquals("new desc", fromDb.getDescription());
    }

    /**
     * Test removing Content Project
     */
    public void testRemoveContentProject() {
        ContentProject cp = ContentManager.createProject("cplabel", "cpname", "description", user);
        int entitiesAffected = ContentManager.removeProject(cp.getLabel(), user);
        assertEquals(1, entitiesAffected);
        Optional<ContentProject> fromDb = ContentManager.lookupProject("cplabel", user);
        assertFalse(fromDb.isPresent());
    }

    /**
     * Tests various operations performed by a user from different organization
     */
    public void testContentProjectCrossOrg() {
        ContentProject cp = ContentManager.createProject("cplabel", "cpname", "description", user);

        Org rangersOrg = UserTestUtils.createNewOrgFull("rangers");
        User anotherUser = UserTestUtils.createUser("Chuck", rangersOrg.getId());
        anotherUser.addPermanentRole(RoleFactory.ORG_ADMIN);

        assertFalse(ContentManager.lookupProject(cp.getLabel(), anotherUser).isPresent());

        try {
            ContentManager.updateProject(cp.getLabel(), of("new name"), of("new desc"), anotherUser);
            fail("An exception should have been thrown");
        }
        catch (EntityNotExistsException e) {
            // no-op
        }

        try {
            ContentManager.removeProject(cp.getLabel(), anotherUser);
            fail("An exception should have been thrown");
        }
        catch (EntityNotExistsException e) {
            // no-op
        }
    }

    /**
     * Tests permissions for Content Project CRUD
     */
    public void testContentProjectPermissions() {
        User guy = UserTestUtils.createUser("Regular user", user.getOrg().getId());

        try {
            ContentManager.createProject("cplabel", "cpname", "description", guy);
            fail("An exception should have been thrown");
        }
        catch (PermissionException e) {
            // expected
        }
        try {
            ContentManager.updateProject("cplabel", empty(), empty(), guy);
            fail("An exception should have been thrown");
        }
        catch (PermissionException e) {
            // expected
        }
        try {
            ContentManager.removeProject("cplabel", guy);
            fail("An exception should have been thrown");
        }
        catch (PermissionException e) {
            // expected
        }

        ContentProject project = ContentManager.createProject("cplabel", "cpname", "description", user);
        assertEquals(project, ContentManager.lookupProject("cplabel", guy).get());
    }

    /**
     * Tests creating, looking up and removing Environments in a Project
     */
    public void testContentEnvironmentLifecycle() {
        ContentProject cp = ContentManager.createProject("cplabel", "cpname", "description", user);

        ContentEnvironment fst = ContentManager.
                createEnvironment(cp.getLabel(), empty(), "fst", "first env", "desc", user);
        ContentEnvironment snd = ContentManager
                .createEnvironment(cp.getLabel(), of(fst.getLabel()), "snd", "second env", "desc2", user);
        assertEquals(asList(fst, snd), ContentProjectFactory.listProjectEnvironments(cp));

        ContentEnvironment mid = ContentManager.
                createEnvironment(cp.getLabel(), of(fst.getLabel()), "mid", "middle env", "desc", user);
        assertEquals(asList(fst, mid, snd), ContentProjectFactory.listProjectEnvironments(cp));

        int numRemoved = ContentManager.removeEnvironment(fst.getLabel(), cp.getLabel(), user);
        assertEquals(1, numRemoved);
        assertEquals(asList(mid, snd), ContentProjectFactory.listProjectEnvironments(cp));
        assertEquals(mid, cp.getFirstEnvironmentOpt().get());
    }

    /**
     * Test behavior when appending a Content Environment behind non-existing Content Environment
     */
    public void testAddingEnvironmentAfterMismatchedPredecessor() {
        ContentProject cp = ContentManager.createProject("cplabel", "cpname", "description", user);
        ContentManager.createEnvironment(cp.getLabel(), empty(), "fst", "first env", "desc", user);
        try {
            ContentManager.createEnvironment(cp.getLabel(), of("NONEXISTING"), "snd", "snd env", "desc",
                    user);
            fail("An exception should have been thrown");
        }
        catch (EntityNotExistsException e) {
            // expected
        }
    }

    /**
     * Test updating a Content Environment
     */
    public void testUpdateContentEnvironment() {
        ContentProject cp = ContentManager.createProject("cplabel", "cpname", "description", user);
        ContentManager
                .createEnvironment(cp.getLabel(), empty(), "fst", "first env", "desc", user);
        ContentManager.updateEnvironment("fst", "cplabel", of("new env name"),
                of("new description"), user);
        ContentEnvironment fromDb = ContentManager
                .lookupEnvironment("fst", "cplabel", user).get();

        assertEquals("new env name", fromDb.getName());
        assertEquals("new description", fromDb.getDescription());
    }

    /**
     * Tests permissions for Environment CRUD
     */
    public void testEnvironmentPermissions() {
        ContentProject project = ContentManager.createProject("cplabel", "cpname", "description", user);
        ContentEnvironment env =
                ContentManager.createEnvironment("cplabel", empty(), "dev", "dev env", "...", user);

        User guy = UserTestUtils.createUser("Regular user", user.getOrg().getId());
        assertEquals(env, ContentManager.lookupEnvironment("dev", "cplabel", guy).get());
        assertEquals(singletonList(env), ContentManager.listProjectEnvironments("cplabel", guy));

        try {
            ContentManager.createEnvironment("cplabel", empty(), "dev", "dev env", "...", guy);
            fail("An exception should have been thrown");
        }
        catch (PermissionException e) {
            // expected
        }
        try {
            ContentManager.updateEnvironment("dev", "cplabel", empty(), empty(), guy);
            fail("An exception should have been thrown");
        }
        catch (PermissionException e) {
            // expected
        }
        try {
            ContentManager.removeEnvironment("dev", "cplabel", guy);
            fail("An exception should have been thrown");
        }
        catch (PermissionException e) {
            // expected
        }
    }

    /**
     * Test attaching/detaching Project Sources, the happy path scenario
     *
     * @throws Exception - if anything goes wrong
     */
    public void testChangeProjectSource() throws Exception {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        Channel channel = ChannelTestUtils.createBaseChannel(user);

        ProjectSource source = ContentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), user);
        ProjectSource fromDb = ContentManager.lookupProjectSource("cplabel", SW_CHANNEL, channel.getLabel(), user).get();
        assertEquals(source, fromDb);
        assertEquals(channel, fromDb.asSoftwareSource().get().getChannel());
        assertEquals(singletonList(source), cp.getSources());

        ContentManager.detachSource("cplabel", SW_CHANNEL, channel.getLabel(), user);
        assertFalse(ContentProjectFactory.lookupProjectSource(cp, SW_CHANNEL, channel.getLabel(), user).isPresent());
        assertTrue(cp.getSources().isEmpty());
    }

    /**
     * Test attaching same Source multiple times
     *
     * @throws Exception - if anything goes wrong
     */
    public void testMultipleAttachProjectSource() throws Exception {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        Channel channel = ChannelTestUtils.createBaseChannel(user);

        ContentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), user);
        try {
            ContentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), user);
            fail("An exception should have been thrown");
        }
        catch (EntityExistsException e) {
            // expected
        }
        assertEquals(1, cp.getSources().size());
    }

    /**
     * Test attaching source to with some missing data (e.g. missing channel)
     *
     * @throws Exception - if anything goes wrong
     */
    public void testAttachSourceMissingEntities() throws Exception {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        Channel channel = ChannelTestUtils.createBaseChannel(user);

        try {
            ContentManager.attachSource("cplabel", SW_CHANNEL, "notthere", user);
            fail("An exception should have been thrown");
        }
        catch (EntityNotExistsException e) {
            // expected
        }

        try {
            ContentManager.attachSource("idontexist", SW_CHANNEL, channel.getLabel(), user);
            fail("An exception should have been thrown");
        }
        catch (EntityNotExistsException e) {
            // expected
        }
    }

    /**
     * Test deleting underlying channel of a Source.
     * In this case, the Source is supposed to be deleted too.
     *
     * @throws Exception - if anything goes wrong
     */
    public void testDeleteSourceChannel() throws Exception {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        Channel channel = ChannelTestUtils.createBaseChannel(user);
        ContentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), user);

        assertTrue(ContentManager.lookupProjectSource("cplabel", SW_CHANNEL, channel.getLabel(), user).isPresent());
        ChannelFactory.remove(channel);
        assertFalse(ContentManager.lookupProjectSource("cplabel", SW_CHANNEL, channel.getLabel(), user).isPresent());
    }

    /**
     * Test deleting underlying Project of a Source.
     * In this case, the Source is supposed to be deleted too.
     *
     * @throws Exception - if anything goes wrong
     */
    public void testDeleteSourceProject() throws Exception {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        Channel channel = ChannelTestUtils.createBaseChannel(user);
        ContentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), user);

        assertTrue(ContentManager.lookupProjectSource("cplabel", SW_CHANNEL, channel.getLabel(), user).isPresent());
        ContentManager.removeProject("cplabel", user);
        // we can't use ContentManager.lookupProjectSource because the project does not exist
        assertTrue(HibernateFactory.getSession()
                .createQuery("SELECT 1 FROM SoftwareProjectSource s where s.contentProject = :cp")
                .setParameter("cp", cp)
                .list()
                .isEmpty());
    }
}