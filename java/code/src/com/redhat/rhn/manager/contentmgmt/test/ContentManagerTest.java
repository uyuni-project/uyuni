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

import static com.redhat.rhn.domain.contentmgmt.ProjectSource.State.ATTACHED;
import static com.redhat.rhn.domain.contentmgmt.ProjectSource.State.BUILT;
import static com.redhat.rhn.domain.contentmgmt.ProjectSource.State.DETACHED;
import static com.redhat.rhn.domain.contentmgmt.ProjectSource.Type.SW_CHANNEL;
import static com.redhat.rhn.domain.role.RoleFactory.ORG_ADMIN;
import static com.redhat.rhn.testing.RhnBaseTestCase.assertContains;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toSet;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentFilter;
import com.redhat.rhn.domain.contentmgmt.ContentFilter.EntityType;
import com.redhat.rhn.domain.contentmgmt.ContentFilter.Rule;
import com.redhat.rhn.domain.contentmgmt.ContentManagementException;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFilter;
import com.redhat.rhn.domain.contentmgmt.ContentProjectHistoryEntry;
import com.redhat.rhn.domain.contentmgmt.EnvironmentTarget;
import com.redhat.rhn.domain.contentmgmt.EnvironmentTarget.Status;
import com.redhat.rhn.domain.contentmgmt.FilterCriteria;
import com.redhat.rhn.domain.contentmgmt.FilterCriteria.Matcher;
import com.redhat.rhn.domain.contentmgmt.PackageFilter;
import com.redhat.rhn.domain.contentmgmt.ProjectSource;
import com.redhat.rhn.domain.contentmgmt.SoftwareEnvironmentTarget;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.InvalidChannelLabelException;
import com.redhat.rhn.manager.EntityExistsException;
import com.redhat.rhn.manager.EntityNotExistsException;
import com.redhat.rhn.manager.contentmgmt.ContentManager;
import com.redhat.rhn.manager.contentmgmt.DependencyResolutionException;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Tests for ContentManager
 */
public class ContentManagerTest extends JMockBaseTestCaseWithUser {

    private ContentManager contentManager;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        contentManager = new ContentManager();
        contentManager.setModulemdApi(new MockModulemdApi());
        user.addPermanentRole(ORG_ADMIN);
    }

    /**
     * Test creating & looking up Content Project
     */
    public void testLookupContentProject() {
        ContentProject cp = contentManager.createProject("cplabel", "cpname", "description", user);
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
        ContentProject cp1 = contentManager.createProject("cplabel1", "cpname1", "description1", user);
        ContentProject cp2 = contentManager.createProject("cplabel2", "cpname2", "description2", user);
        Org rangersOrg = UserTestUtils.createNewOrgFull("rangers");
        User anotherAdmin = UserTestUtils.createUser("Chuck", rangersOrg.getId());
        anotherAdmin.addPermanentRole(ORG_ADMIN);
        ContentProject cp3 = contentManager.createProject("cplabel3", "cpname3", "description3", anotherAdmin);

        assertEquals(Arrays.asList(cp1, cp2), ContentManager.listProjects(user));
        assertEquals(singletonList(cp3), ContentManager.listProjects(anotherAdmin));
    }

    /**
     * Test multiple creating Content Project with same label
     */
    public void testMultipleCreateContentProject() {
        contentManager.createProject("cplabel", "cpname", "description", user);
        try {
            contentManager.createProject("cplabel", "differentname", null, user);
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
        ContentProject cp = contentManager.createProject("cplabel", "cpname", "description", user);
        ContentProject updated = contentManager.updateProject(cp.getLabel(), of("new name"), of("new desc"), user);

        ContentProject fromDb = ContentManager.lookupProject("cplabel", user).get();
        assertEquals(fromDb, updated);
        assertEquals("new name", fromDb.getName());
        assertEquals("new desc", fromDb.getDescription());
    }

    /**
     * Test removing Content Project
     */
    public void testRemoveContentProject() {
        ContentProject cp = contentManager.createProject("cplabel", "cpname", "description", user);
        int entitiesAffected = contentManager.removeProject(cp.getLabel(), user);
        assertEquals(1, entitiesAffected);
        Optional<ContentProject> fromDb = ContentManager.lookupProject("cplabel", user);
        assertFalse(fromDb.isPresent());
    }

    /**
     * Tests various operations performed by a user from different organization
     */
    public void testContentProjectCrossOrg() {
        ContentProject cp = contentManager.createProject("cplabel", "cpname", "description", user);

        Org rangersOrg = UserTestUtils.createNewOrgFull("rangers");
        User anotherUser = UserTestUtils.createUser("Chuck", rangersOrg.getId());
        anotherUser.addPermanentRole(ORG_ADMIN);

        assertFalse(ContentManager.lookupProject(cp.getLabel(), anotherUser).isPresent());

        try {
            contentManager.updateProject(cp.getLabel(), of("new name"), of("new desc"), anotherUser);
            fail("An exception should have been thrown");
        }
        catch (EntityNotExistsException e) {
            // no-op
        }

        try {
            contentManager.removeProject(cp.getLabel(), anotherUser);
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
            contentManager.createProject("cplabel", "cpname", "description", guy);
            fail("An exception should have been thrown");
        }
        catch (PermissionException e) {
            // expected
        }
        try {
            contentManager.updateProject("cplabel", empty(), empty(), guy);
            fail("An exception should have been thrown");
        }
        catch (PermissionException e) {
            // expected
        }
        try {
            contentManager.removeProject("cplabel", guy);
            fail("An exception should have been thrown");
        }
        catch (PermissionException e) {
            // expected
        }

        ContentProject project = contentManager.createProject("cplabel", "cpname", "description", user);
        assertEquals(project, ContentManager.lookupProject("cplabel", guy).get());
    }

    /**
     * Tests creating, looking up and removing Environments in a Project
     */
    public void testContentEnvironmentLifecycle() {
        ContentProject cp = contentManager.createProject("cplabel", "cpname", "description", user);

        ContentEnvironment fst = contentManager.
                createEnvironment(cp.getLabel(), empty(), "fst", "first env", "desc", false, user);
        ContentEnvironment snd = contentManager
                .createEnvironment(cp.getLabel(), of(fst.getLabel()), "snd", "second env", "desc2", false, user);
        assertEquals(asList(fst, snd), ContentProjectFactory.listProjectEnvironments(cp));

        ContentEnvironment mid = contentManager.
                createEnvironment(cp.getLabel(), of(fst.getLabel()), "mid", "middle env", "desc", false, user);
        assertEquals(asList(fst, mid, snd), ContentProjectFactory.listProjectEnvironments(cp));

        int numRemoved = contentManager.removeEnvironment(fst.getLabel(), cp.getLabel(), user);
        assertEquals(1, numRemoved);
        assertEquals(asList(mid, snd), ContentProjectFactory.listProjectEnvironments(cp));
        assertEquals(mid, cp.getFirstEnvironmentOpt().get());
    }

    /**
     * Test that removing a Environment also removes its targets
     *
     * @throws Exception if anything goes wrong
     */
    public void testRemoveEnvironmentTargets() throws Exception {
        ContentProject cp = contentManager.createProject("cplabel", "cpname", "description", user);
        ContentEnvironment env = contentManager.createEnvironment(cp.getLabel(), empty(), "fst", "first env", "desc", false, user);
        Channel channel = createChannelInEnvironment(env, empty());

        SoftwareEnvironmentTarget tgt = new SoftwareEnvironmentTarget(env, channel);
        ContentProjectFactory.save(tgt);
        env.addTarget(tgt);

        contentManager.removeEnvironment("fst", "cplabel", user);
        // the target is removed
        assertFalse(HibernateFactory.getSession()
                .createQuery("select t from SoftwareEnvironmentTarget t where t.channel = :channel")
                .setParameter("channel", channel)
                .uniqueResultOptional()
                .isPresent());

        // channel is also removed
        assertNull(ChannelFactory.lookupById(channel.getId()));
    }

    /**
     * Test behavior when appending a Content Environment behind non-existing Content Environment
     */
    public void testAddingEnvironmentAfterMismatchedPredecessor() {
        ContentProject cp = contentManager.createProject("cplabel", "cpname", "description", user);
        contentManager.createEnvironment(cp.getLabel(), empty(), "fst", "first env", "desc", false, user);
        try {
            contentManager.createEnvironment(cp.getLabel(), of("NONEXISTING"), "snd", "snd env", "desc",
                    false, user);
            fail("An exception should have been thrown");
        }
        catch (EntityNotExistsException e) {
            // expected
        }
    }

    /**
     * Tests populating newly inserted environment with content
     *
     * @throws Exception if anything goes wrong
     */
    public void testPopulateNewEnvironment() throws Exception {
        ContentProject cp = contentManager.createProject("cplabel", "cpname", "description", user);

        ContentEnvironment fst = contentManager.
                createEnvironment(cp.getLabel(), empty(), "fst", "first env", "desc", false, user);
        fst.setVersion(3L);
        Channel channel = createChannelInEnvironment(fst, empty());
        fst.addTarget(new SoftwareEnvironmentTarget(fst, channel));
        contentManager.createEnvironment(cp.getLabel(), of(fst.getLabel()), "last", "last env", "desc2", false, user);

        ContentEnvironment mid = contentManager.
                createEnvironment(cp.getLabel(), of(fst.getLabel()), "mid", "middle env", "desc", false, user);
        assertEquals(1, mid.getTargets().size());
        Channel newChannel = mid.getTargets().get(0).asSoftwareTarget().get().getChannel();
        assertEquals(channel, newChannel.getOriginal());
        assertTrue(newChannel.getLabel().startsWith("cplabel-mid-"));
        assertEquals(fst.getVersion(), mid.getVersion());
    }

    /**
     * Test updating a Content Environment
     */
    public void testUpdateContentEnvironment() {
        ContentProject cp = contentManager.createProject("cplabel", "cpname", "description", user);
        contentManager
                .createEnvironment(cp.getLabel(), empty(), "fst", "first env", "desc", false , user);
        contentManager.updateEnvironment("fst", "cplabel", of("new env name"),
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
        contentManager.createProject("cplabel", "cpname", "description", user);
        ContentEnvironment env =
                contentManager.createEnvironment("cplabel", empty(), "dev", "dev env", "...", false, user);

        User guy = UserTestUtils.createUser("Regular user", user.getOrg().getId());
        assertEquals(env, ContentManager.lookupEnvironment("dev", "cplabel", guy).get());
        assertEquals(singletonList(env), ContentManager.listProjectEnvironments("cplabel", guy));

        try {
            contentManager.createEnvironment("cplabel", empty(), "dev", "dev env", "...", false, guy);
            fail("An exception should have been thrown");
        }
        catch (PermissionException e) {
            // expected
        }
        try {
            contentManager.updateEnvironment("dev", "cplabel", empty(), empty(), guy);
            fail("An exception should have been thrown");
        }
        catch (PermissionException e) {
            // expected
        }
        try {
            contentManager.removeEnvironment("dev", "cplabel", guy);
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

        ProjectSource source = contentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);
        ProjectSource fromDb = ContentManager.lookupProjectSource("cplabel", SW_CHANNEL, channel.getLabel(), user).get();
        assertEquals(source, fromDb);
        assertEquals(channel, fromDb.asSoftwareSource().get().getChannel());
        assertEquals(singletonList(source), cp.getSources());

        source.setState(BUILT); // programmatically set to BUILT (normally this happens after building project)
        contentManager.detachSource("cplabel", SW_CHANNEL, channel.getLabel(), user);
        ProjectSource projectSource = ContentProjectFactory.lookupProjectSource(cp, SW_CHANNEL, channel.getLabel(), user).get();
        assertEquals(DETACHED, projectSource.getState());
        assertEquals(singletonList(source), cp.getSources());
    }

    /**
     * Test attaching Source
     *
     * @throws Exception - if anything goes wrong
     */
    public void testAttachProjectSource() throws Exception {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        Channel channel = ChannelTestUtils.createBaseChannel(user);

        contentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);

        assertEquals(1, cp.getSources().size());
        assertEquals(ATTACHED, cp.getSources().get(0).getState());
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

        contentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);
        contentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);

        assertEquals(1, cp.getSources().size());
        assertEquals(ATTACHED, cp.getSources().get(0).getState());
    }

    /**
     * Test attaching built Source multiple times
     *
     * @throws Exception - if anything goes wrong
     */
    public void testAttachBuiltProjectSource() throws Exception {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        Channel channel = ChannelTestUtils.createBaseChannel(user);

        ProjectSource src = contentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);
        src.setState(BUILT); // programmatically set to BUILT (normally this happens after building project)

        // attach the same source
        contentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);

        // it should stay in BUILT state
        assertEquals(1, cp.getSources().size());
        assertEquals(BUILT, cp.getSources().get(0).getState());
    }

    /**
     * Test attaching detached Source multiple times
     *
     * @throws Exception - if anything goes wrong
     */
    public void testAttachDetachedProjectSource() throws Exception {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        Channel channel = ChannelTestUtils.createBaseChannel(user);

        ProjectSource src = contentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);
        src.setState(DETACHED); // programmatically set to DETACHED (normally this happens when detaching BUILT source))

        // attach the same source
        contentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);

        // it should go back to BUILT state
        assertEquals(1, cp.getSources().size());
        assertEquals(BUILT, cp.getSources().get(0).getState());
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
            contentManager.attachSource("cplabel", SW_CHANNEL, "notthere", empty(), user);
            fail("An exception should have been thrown");
        }
        catch (EntityNotExistsException e) {
            // expected
        }

        try {
            contentManager.attachSource("idontexist", SW_CHANNEL, channel.getLabel(), empty(), user);
            fail("An exception should have been thrown");
        }
        catch (EntityNotExistsException e) {
            // expected
        }
    }

    /**
     * Test detaching a {@link ProjectSource}
     *
     * @throws Exception - if anything goes wrong
     */
    public void testDetachProjectSource() throws Exception {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        Channel channel = ChannelTestUtils.createBaseChannel(user);

        ProjectSource src = contentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);
        src.setState(BUILT); // programmatically set to BUILT (normally this happens after building project)
        contentManager.detachSource("cplabel", SW_CHANNEL, channel.getLabel(), user);

        assertEquals(1, cp.getSources().size());
        assertEquals(DETACHED, cp.getSources().get(0).getState());
    }

    /**
     * Test detaching a {@link ProjectSource}
     *
     * @throws Exception - if anything goes wrong
     */
    public void testDetachAttachedProjectSource() throws Exception {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        Channel channel = ChannelTestUtils.createBaseChannel(user);

        contentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);
        contentManager.detachSource("cplabel", SW_CHANNEL, channel.getLabel(), user);

        // freshly attached, non-built source should be removed after detaching
        assertEquals(0, cp.getSources().size());
    }

    /**
     * Test detaching same Source multiple times
     *
     * @throws Exception - if anything goes wrong
     */
    public void testMultipleDetachingProjectSource() throws Exception {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        Channel channel = ChannelTestUtils.createBaseChannel(user);

        ProjectSource src = contentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);
        src.setState(BUILT); // programmatically set to BUILT (normally this happens after building project)
        contentManager.detachSource("cplabel", SW_CHANNEL, channel.getLabel(), user);
        contentManager.detachSource("cplabel", SW_CHANNEL, channel.getLabel(), user);

        assertEquals(1, cp.getSources().size());
        assertEquals(DETACHED, cp.getSources().get(0).getState());
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
        contentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);

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
        contentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);

        assertTrue(ContentManager.lookupProjectSource("cplabel", SW_CHANNEL, channel.getLabel(), user).isPresent());
        contentManager.removeProject("cplabel", user);
        // we can't use ContentManager.lookupSource because the project does not exist
        assertTrue(HibernateFactory.getSession()
                .createQuery("SELECT 1 FROM SoftwareProjectSource s where s.contentProject = :cp")
                .setParameter("cp", cp)
                .list()
                .isEmpty());
    }

    public void testCreateAndListFilter() {
        FilterCriteria criteria = new FilterCriteria(Matcher.CONTAINS, "name", "aaa");
        ContentFilter filter = contentManager.createFilter("my-filter", Rule.DENY, EntityType.PACKAGE, criteria, user);

        List<ContentFilter> filters = ContentManager.listFilters(user);
        assertEquals(1, filters.size());
        ContentFilter fromDb = filters.get(0);
        assertTrue(fromDb instanceof PackageFilter);
        assertEquals(filter, fromDb);
        assertEquals(Rule.DENY, fromDb.getRule());
        assertEquals(criteria, fromDb.getCriteria());
    }

    public void testLookupNonexistingFilter() {
        long id = -1234565L;
        // cleanup first
        try {
            contentManager.removeFilter(id, user);
        }
        catch (EntityNotExistsException e) {
            // pass
        }
        assertFalse(ContentManager.lookupFilterById(id, user).isPresent());
    }

    public void testLookupFilter() {
        FilterCriteria criteria = new FilterCriteria(Matcher.CONTAINS, "name", "aaa");
        ContentFilter filter = contentManager.createFilter("my-filter", Rule.DENY, EntityType.PACKAGE, criteria, user);

        ContentFilter fromDb = ContentManager.lookupFilterById(filter.getId(), user).get();
        assertEquals(filter, fromDb);
        assertEquals(Rule.DENY, fromDb.getRule());
    }

    public void testLookupFilterNonAuthorizedUser() {
        FilterCriteria criteria = new FilterCriteria(Matcher.CONTAINS, "name", "aaa");
        ContentFilter filter = contentManager.createFilter("my-filter", Rule.DENY, EntityType.PACKAGE, criteria, user);

        Org rangersOrg = UserTestUtils.createNewOrgFull("rangers");
        User anotherAdmin = UserTestUtils.createUser("Chuck", rangersOrg.getId());
        anotherAdmin.addPermanentRole(ORG_ADMIN);
        assertFalse(ContentManager.lookupFilterById(filter.getId(), anotherAdmin).isPresent());
        assertTrue(ContentManager.listFilters(anotherAdmin).isEmpty());
    }

    public void testUpdateFilter() {
        FilterCriteria criteria = new FilterCriteria(Matcher.CONTAINS, "name", "aaa");
        ContentFilter filter = contentManager.createFilter("my-filter", Rule.DENY, EntityType.PACKAGE, criteria, user);

        FilterCriteria newCriteria = new FilterCriteria(Matcher.CONTAINS, "name", "bbb");
        contentManager.updateFilter(filter.getId(), of("newname"), empty(), of(newCriteria), user);
        ContentFilter fromDb = ContentManager.lookupFilterById(filter.getId(), user).get();
        assertEquals("newname", fromDb.getName());
        assertEquals(newCriteria, fromDb.getCriteria());
    }

    public void testRemoveFilter() {
        FilterCriteria criteria = new FilterCriteria(Matcher.CONTAINS, "name", "aaa");
        ContentFilter filter = contentManager.createFilter("my-filter", Rule.DENY, EntityType.PACKAGE, criteria, user);

        Long id = filter.getId();
        contentManager.removeFilter(id, user);
        assertFalse(ContentManager.lookupFilterById(id, user).isPresent());
    }

    public void testAttachFilter() {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        FilterCriteria criteria = new FilterCriteria(Matcher.CONTAINS, "name", "aaa");
        ContentFilter filter = contentManager.createFilter("my-filter", Rule.DENY, EntityType.PACKAGE, criteria, user);

        contentManager.attachFilter("cplabel", filter.getId(), user);

        ContentProject fromDb = ContentManager.lookupProject("cplabel", user).get();
        assertEquals(1, fromDb.getProjectFilters().size());
        ContentProjectFilter projectFilter = fromDb.getProjectFilters().get(0);
        assertEquals(ContentProjectFilter.State.ATTACHED, projectFilter.getState());

        // attaching a DETACHED filter should result in BUILT filter
        projectFilter.setState(ContentProjectFilter.State.DETACHED);
        contentManager.attachFilter("cplabel", filter.getId(), user);
        fromDb = ContentManager.lookupProject("cplabel", user).get();
        assertEquals(ContentProjectFilter.State.BUILT, fromDb.getProjectFilters().get(0).getState());
    }

    public void testDetachNoFilter() {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        FilterCriteria criteria = new FilterCriteria(Matcher.CONTAINS, "name", "aaa");
        ContentFilter filter = contentManager.createFilter("my-filter", Rule.DENY, EntityType.PACKAGE, criteria, user);
        contentManager.detachFilter("cplabel", filter.getId(), user);
    }

    public void testDetachFilter() {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        FilterCriteria criteria = new FilterCriteria(Matcher.CONTAINS, "name", "aaa");
        ContentFilter filter = contentManager.createFilter("my-filter", Rule.DENY, EntityType.PACKAGE, criteria, user);

        contentManager.attachFilter("cplabel", filter.getId(), user);
        contentManager.detachFilter("cplabel", filter.getId(), user);
        ContentProject fromDb = ContentManager.lookupProject("cplabel", user).get();
        assertTrue(fromDb.getProjectFilters().isEmpty());
    }

    public void testDetachBuiltFilter() {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        FilterCriteria criteria = new FilterCriteria(Matcher.CONTAINS, "name", "aaa");
        ContentFilter filter = contentManager.createFilter("my-filter", Rule.DENY, EntityType.PACKAGE, criteria, user);

        contentManager.attachFilter("cplabel", filter.getId(), user);
        cp.getProjectFilters().get(0).setState(ContentProjectFilter.State.BUILT);
        contentManager.detachFilter("cplabel", filter.getId(), user);
        ContentProject fromDb = ContentManager.lookupProject("cplabel", user).get();
        assertEquals(1, fromDb.getProjectFilters().size());
        assertEquals(ContentProjectFilter.State.DETACHED, fromDb.getProjectFilters().get(0).getState());
    }

    /**
     * Test building project with no environments
     */
    public void testBuildProjectNoEnvs() {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        try {
            contentManager.buildProject("cplabel", empty(), false, user);
            fail("An exception should have been thrown");
        }
        catch (ContentManagementException e) {
            // expected
        }
    }

    /**
     * Test building project with no sources assigned
     */
    public void testBuildProjectNoSources() {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        ContentEnvironment env = contentManager.createEnvironment(cp.getLabel(), empty(), "fst", "first env", "desc", false, user);
        try {
            contentManager.buildProject("cplabel", empty(), false, user);
            fail("An exception should have been thrown");
        }
        catch (ContentManagementException e) {
            // expected
        }
        assertEquals(Long.valueOf(0), env.getVersion());
    }

    /**
     * Test building project - complex scenario with multiple builds and changing sources
     *
     * @throws Exception if anything goes wrong
     */
    public void testBuildProject() throws Exception {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        ContentEnvironment env = contentManager.createEnvironment(cp.getLabel(), empty(), "fst", "first env", "desc", false, user);
        assertEquals(Long.valueOf(0), env.getVersion());

        // 1. build the project with a source
        Channel channel = createPopulatedChannel();
        contentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);
        assertEquals(channel, cp.lookupSwSourceLeader().get().getChannel());
        contentManager.buildProject("cplabel", empty(), false, user);

        List<EnvironmentTarget> tgts = env.getTargets();
        assertEquals(1, tgts.size());
        EnvironmentTarget target = tgts.get(0);
        Channel tgtChannel = target.asSoftwareTarget().get().getChannel();
        assertEquals(Status.GENERATING_REPODATA, target.getStatus());
        assertEquals("cplabel-fst-" + channel.getLabel(), tgtChannel.getLabel());
        assertTrue(channel.getClonedChannels().contains(tgtChannel));
        assertEquals(channel, tgtChannel.getOriginal());
        assertEquals(channel.getPackages(), tgtChannel.getPackages());
        assertEquals(channel.getErratas(), tgtChannel.getErratas());
        assertEquals(Long.valueOf(1), env.getVersion());

        // 2. change the project source and rebuild
        Channel newChannel = createPopulatedChannel();
        contentManager.attachSource("cplabel", SW_CHANNEL, newChannel.getLabel(), empty(), user);
        assertEquals(channel, cp.lookupSwSourceLeader().get().getChannel()); // leader is the same
        contentManager.buildProject("cplabel", empty(), false, user);
        assertEquals(Long.valueOf(2), env.getVersion());

        tgts.forEach(t -> assertEquals(Status.GENERATING_REPODATA, t.asSoftwareTarget().get().getStatus()));
        assertEquals(2, tgts.size());
        Set<String> tgtLabels = tgts.stream().map(tgt -> tgt.asSoftwareTarget().get().getChannel().getLabel()).collect(toSet());
        assertContains(tgtLabels, "cplabel-fst-" + channel.getLabel());
        assertContains(tgtLabels, "cplabel-fst-" + newChannel.getLabel());
        Channel base = tgts.stream()
                .filter(t -> t.asSoftwareTarget().get().getChannel().getLabel().equals("cplabel-fst-" + channel.getLabel()))
                .findFirst().get().asSoftwareTarget().get().getChannel();
        Channel child = tgts.stream()
                .filter(t -> t.asSoftwareTarget().get().getChannel().getLabel().equals("cplabel-fst-" + newChannel.getLabel()))
                .findFirst().get().asSoftwareTarget().get().getChannel();
        assertNull(base.getParentChannel());
        assertEquals(base, child.getParentChannel());
        assertEquals(channel.getPackages(), tgtChannel.getPackages());
        assertEquals(channel.getErratas(), tgtChannel.getErratas());

        // 3. remove a source and rebuild
        contentManager.detachSource("cplabel", SW_CHANNEL, channel.getLabel(), user);
        cp = (ContentProject) HibernateFactory.reload(cp);
        contentManager.buildProject("cplabel", empty(), false, user);
        assertEquals(Long.valueOf(3), env.getVersion());

        assertEquals(newChannel, cp.lookupSwSourceLeader().get().getChannel()); // leader is changed
        assertEquals(Status.GENERATING_REPODATA, tgts.get(0).asSoftwareTarget().get().getStatus());
        assertEquals(1, tgts.size());
        assertEquals("cplabel-fst-" + newChannel.getLabel(), tgts.get(0).asSoftwareTarget().get().getChannel().getLabel());
        assertEquals(channel.getPackages(), tgtChannel.getPackages());
        assertEquals(channel.getErratas(), tgtChannel.getErratas());
    }

    /**
     * Test that if a matching target channel already exists, it is used when creating a new environment target.
     *
     * @throws Exception if anything goes wrong
     */
    public void testBuildProjectExistingChannel() throws Exception {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        ContentEnvironment env = contentManager.createEnvironment(cp.getLabel(), empty(), "fst", "first env", "desc", false, user);

        Channel channel = createPopulatedChannel();
        contentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);

        Channel alreadyExistingTgt = createChannelInEnvironment(env, of(channel.getLabel()));

        contentManager.buildProject("cplabel", empty(), false, user);
        List<EnvironmentTarget> environmentTargets = env.getTargets();
        assertEquals(1, environmentTargets.size());
        assertEquals(alreadyExistingTgt, environmentTargets.get(0).asSoftwareTarget().get().getChannel());
    }

    /**
     * Test that if a matching target channel already exists in a different organization, an exception is thrown
     *
     * @throws Exception if anything goes wrong
     */
    public void testBuildProjectExistingChannelCrossOrg() throws Exception {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        ContentEnvironment env = contentManager.createEnvironment(cp.getLabel(), empty(), "fst", "first env", "desc", false, user);

        Channel channel = createPopulatedChannel();
        contentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);

        Org otherOrg = UserTestUtils.createNewOrgFull("testOrg2");
        Channel alreadyExistingTgt = createChannelInEnvironment(env, of(channel.getLabel()));
        alreadyExistingTgt.setOrg(otherOrg);

        try {
            contentManager.buildProject("cplabel", empty(), false, user);
            fail("An exception should have been thrown");
        }
        catch (InvalidChannelLabelException e) {
            // expected
        }
        assertEquals(Long.valueOf(0), env.getVersion());
    }

    /**
     * Test multiple users interacting with a Project
     *
     * @throws Exception if anything goes wrong
     */
    public void testBuildProjectTwoUsers() throws Exception {
        User adminSameOrg = UserTestUtils.createUser("adminInSameOrg", user.getOrg().getId());
        adminSameOrg.addPermanentRole(ORG_ADMIN);
        User userSameOrg = UserTestUtils.createUser("userInSameOrg", user.getOrg().getId());
        Org otherOrg = UserTestUtils.createNewOrgFull("testOrg2");
        User userOtherOrg = UserTestUtils.createUser("userInOtherOrg", otherOrg.getId());

        // project is created by one user
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        ContentEnvironment env = contentManager.createEnvironment(cp.getLabel(), empty(), "fst", "first env", "desc", false, user);

        // ... build by another user
        Channel channel = createPopulatedChannel();
        contentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);
        contentManager.buildProject("cplabel", empty(), false, adminSameOrg);
        assertEquals(Long.valueOf(1), env.getVersion());

        ContentEnvironment envUser1 = ContentManager.lookupEnvironment("fst", "cplabel", userSameOrg).get();
        List<EnvironmentTarget> tgts = envUser1.getTargets();
        assertEquals(1, tgts.size());
        Channel tgtChannel = tgts.get(0).asSoftwareTarget().get().getChannel();
        // its assets should be accessible to a regular user in the org
        assertTrue(ChannelFactory.isAccessibleByUser(tgtChannel.getLabel(), userSameOrg.getId()));
        // but not for a user in another org
        assertFalse(ChannelFactory.isAccessibleByUser(tgtChannel.getLabel(), userOtherOrg.getId()));
    }

    /**
     * Tests that the project history entry is added when a project is built
     *
     * @throws Exception if anything goes wrong
     */
    public void testBuildProjectHistoryAdded() throws Exception {
        User adminSameOrg = UserTestUtils.createUser("adminInSameOrg", user.getOrg().getId());
        adminSameOrg.addPermanentRole(ORG_ADMIN);
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        ContentEnvironment env = contentManager.createEnvironment(cp.getLabel(), empty(), "fst", "first env", "desc", false, user);

        Channel channel = createPopulatedChannel();
        contentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);
        contentManager.buildProject("cplabel", empty(), false, user);
        assertEquals(Long.valueOf(1), env.getVersion());

        List<ContentProjectHistoryEntry> history = ContentManager.lookupProject("cplabel", user).get().getHistoryEntries();
        assertEquals(1, history.size());
        assertEquals(Long.valueOf(1), history.get(0).getVersion());
        assertEquals(user, history.get(0).getUser());

        contentManager.buildProject("cplabel", empty(), false, adminSameOrg);
        assertEquals(Long.valueOf(2), env.getVersion());
        history = ContentManager.lookupProject("cplabel", user).get().getHistoryEntries();
        assertEquals(2, history.size());
        assertEquals(Long.valueOf(2), history.get(1).getVersion());
        assertEquals(adminSameOrg, history.get(1).getUser());
    }

    /**
     * Test building project having modular sources
     */
    public void testBuildProjectModularSources() throws Exception {
        Channel channel = MockModulemdApi.createModularTestChannel(user);

        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        ContentEnvironment env = contentManager.createEnvironment(cp.getLabel(), empty(), "fst", "first env", "desc", false, user);
        assertEquals(Long.valueOf(0), env.getVersion());

        contentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);
        assertEquals(channel, cp.lookupSwSourceLeader().get().getChannel());
        contentManager.buildProject("cplabel", empty(), false, user);

        // When no module filter is applied, modular channels must be cloned as-is.
        // Assert that the target channel is also modular
        Channel targetChannel = env.getTargets().get(0).asSoftwareTarget().get().getChannel();
        assertTrue(targetChannel.isModular());
        assertEquals(channel.getPackageCount(), targetChannel.getPackageCount());
    }

    /**
     * Test promoting a project, complex happy-path scenario
     *
     * @throws Exception if anything goes wrong
     */
    public void testPromoteProject() throws Exception {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        ContentEnvironment devEnv = contentManager.createEnvironment(cp.getLabel(), empty(), "dev", "dev env", "desc", false, user);

        // 1. build the project with a source
        Channel channel1 = createPopulatedChannel();
        Channel channel2 = createPopulatedChannel();
        contentManager.attachSource("cplabel", SW_CHANNEL, channel1.getLabel(), empty(), user);
        contentManager.attachSource("cplabel", SW_CHANNEL, channel2.getLabel(), empty(), user);
        contentManager.buildProject("cplabel", empty(), false, user);

        // 2. add new environments
        ContentEnvironment testEnv = contentManager.createEnvironment(cp.getLabel(), of("dev"), "test", "test env", "desc", false, user);
        ContentEnvironment prodEnv = contentManager.createEnvironment(cp.getLabel(), of("test"), "prod", "prod env", "desc", false, user);

        // 3. promote
        contentManager.promoteProject("cplabel", "dev", false, user);
        assertEquals(devEnv.getVersion(), testEnv.getVersion());
        List<EnvironmentTarget> testTgts = testEnv.getTargets();
        testTgts.forEach(t -> assertEquals(Status.GENERATING_REPODATA, t.getStatus()));
        assertEquals(2, testTgts.size());
        Channel tgtChannel1 = testTgts.get(0).asSoftwareTarget().get().getChannel();
        Channel tgtChannel2 = testTgts.get(1).asSoftwareTarget().get().getChannel();
        assertEquals("cplabel-test-" + channel1.getLabel(), tgtChannel1.getLabel());
        assertEquals("cplabel-test-" + channel2.getLabel(), tgtChannel2.getLabel());

        // 3. change project sources (this shouldn't effect the next promotion and should be effective only after build)
        contentManager.detachSource("cplabel", SW_CHANNEL, channel1.getLabel(), user);

        // 4. promote further
        contentManager.promoteProject("cplabel", "test", false, user);
        assertEquals(devEnv.getVersion(), prodEnv.getVersion());
        List<EnvironmentTarget> prodTgts = prodEnv.getTargets();
        prodTgts.forEach(t -> assertEquals(Status.GENERATING_REPODATA, t.getStatus()));
        assertEquals(2, prodTgts.size());
        tgtChannel1 = prodTgts.get(0).asSoftwareTarget().get().getChannel();
        tgtChannel2 = prodTgts.get(1).asSoftwareTarget().get().getChannel();
        assertEquals("cplabel-prod-" + channel1.getLabel(), tgtChannel1.getLabel());
        assertEquals("cplabel-prod-" + channel2.getLabel(), tgtChannel2.getLabel());

        // 5. build with changed sources
        contentManager.buildProject("cplabel", empty(), false, user);
        assertEquals(Long.valueOf(2), devEnv.getVersion());
        assertEquals(1, devEnv.getTargets().size());
        assertEquals(2, testEnv.getTargets().size());
        assertEquals(2, prodEnv.getTargets().size());
        assertNull(ChannelFactory.lookupByLabel("cplabel-dev-" + channel1.getLabel())); // channel has been deleted

        // 6. next promotion cycle
        contentManager.promoteProject("cplabel", "dev", false, user);
        assertEquals(devEnv.getVersion(), testEnv.getVersion());
        testTgts = testEnv.getTargets();
        testTgts.forEach(t -> assertEquals(Status.GENERATING_REPODATA, t.getStatus()));
        assertEquals(1, testTgts.size());
        Channel tgtChannel = testTgts.get(0).asSoftwareTarget().get().getChannel();
        assertEquals("cplabel-test-" + channel2.getLabel(), tgtChannel.getLabel());
        assertNull(ChannelFactory.lookupByLabel("cplabel-test-" + channel1.getLabel())); // channel has been deleted

        // 7. last promotion cycle
        contentManager.promoteProject("cplabel", "test", false, user);
        assertEquals(devEnv.getVersion(), prodEnv.getVersion());
        prodTgts = prodEnv.getTargets();
        prodTgts.forEach(t -> assertEquals(Status.GENERATING_REPODATA, t.getStatus()));
        assertEquals(1, prodTgts.size());
        tgtChannel = prodTgts.get(0).asSoftwareTarget().get().getChannel();
        assertEquals("cplabel-prod-" + channel2.getLabel(), tgtChannel.getLabel());
        assertNull(ChannelFactory.lookupByLabel("cplabel-prod-" + channel1.getLabel())); // channel has been deleted
    }

    /**
     * Test promoting a project with no environments
     */
    public void testPromoteEmptyProject() {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        try {
            contentManager.promoteProject("cplabel", "idontexist", false, user);
            fail("An exception should have been thrown");
        }
        catch (EntityNotExistsException e) {
            // expected
        }
    }

    /**
     * Tests promoting a project with single environment
     *
     * @throws Exception
     */
    public void testPromoteSingleEnv() throws Exception {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        contentManager.createEnvironment(cp.getLabel(), empty(), "dev", "dev env", "desc", false, user);

        Channel channel1 = createPopulatedChannel();
        contentManager.attachSource("cplabel", SW_CHANNEL, channel1.getLabel(), empty(), user);
        contentManager.buildProject("cplabel", empty(), false, user);

        try {
            contentManager.promoteProject("cplabel", "dev", false, user);
            fail("An exception should have been thrown");
        }
        catch (ContentManagementException e) {
            // expected
        }
    }

    /**
     * Tests promoting a project with single environment
     */
    public void testPromoteWithNoBuild() throws Exception {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        contentManager.createEnvironment(cp.getLabel(), empty(), "dev", "dev env", "desc", false, user);
        contentManager.createEnvironment(cp.getLabel(), of("dev"), "test", "test env", "desc", false, user);

        Channel channel1 = createPopulatedChannel();
        contentManager.attachSource("cplabel", SW_CHANNEL, channel1.getLabel(), empty(), user);

        try {
            contentManager.promoteProject("cplabel", "dev", false, user);
            fail("An exception should have been thrown");
        }
        catch (IllegalStateException e) {
            // expected
        }
    }

    /**
     * Test that building a Project sets the correct state of assigned Filters
     *
     * @throws Exception if anything goes wrong
     */
    public void testBuildProjectWithFilters() throws Exception {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        ContentEnvironment env = contentManager.createEnvironment(cp.getLabel(), empty(), "fst", "first env", "desc", false, user);
        assertEquals(Long.valueOf(0), env.getVersion());
        FilterCriteria criteria = new FilterCriteria(FilterCriteria.Matcher.CONTAINS, "name", "aaa");
        ContentFilter filter = contentManager.createFilter("my-filter", Rule.DENY, ContentFilter.EntityType.PACKAGE, criteria, user);
        Channel channel = createPopulatedChannel();
        contentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);

        // ATTACHED filters should get BUILT
        contentManager.attachFilter("cplabel", filter.getId(), user);
        contentManager.buildProject("cplabel", empty(), false, user);
        assertEquals(1, cp.getProjectFilters().size());
        assertEquals(ContentProjectFilter.State.BUILT, cp.getProjectFilters().get(0).getState());

        // DETACHED filters should get removed
        contentManager.detachFilter("cplabel", filter.getId(), user);
        contentManager.buildProject("cplabel", empty(), false, user);
        assertTrue(cp.getProjectFilters().isEmpty());
    }

    /**
     * Test building a Project and filtering out Package using NEVR Filters
     *
     * @throws Exception if anything goes wrong
     */
    public void testBuildProjectWithNevrFilters() throws Exception {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        ContentEnvironment env = contentManager.createEnvironment(cp.getLabel(), empty(), "fst", "first env", "desc", false, user);
        Channel channel = createPopulatedChannel();
        contentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);

        // build without filters
        contentManager.buildProject("cplabel", empty(), false, user);
        assertEquals(1, env.getTargets().get(0).asSoftwareTarget().get().getChannel().getPackageCount());

        // build with filters
        Package pack = channel.getPackages().iterator().next();
        FilterCriteria criteria = new FilterCriteria(Matcher.EQUALS, "nevr", pack.getNameEvr());
        ContentFilter filter = contentManager.createFilter("my-filter", Rule.DENY, ContentFilter.EntityType.PACKAGE, criteria, user);
        contentManager.attachFilter("cplabel", filter.getId(), user);
        contentManager.buildProject("cplabel", empty(), false, user);
        assertEquals(0, env.getTargets().get(0).asSoftwareTarget().get().getChannel().getPackageCount());
    }

    /**
     * Test building a project with AppStream filters
     */
    public void testBuildProjectWithModuleFilters() throws Exception {
        Channel channel = MockModulemdApi.createModularTestChannel(user);

        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        ContentEnvironment env = contentManager.createEnvironment(cp.getLabel(), empty(), "fst", "first env", "desc", false, user);
        contentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);

        // build with unmatching filters
        FilterCriteria criteria = new FilterCriteria(Matcher.EQUALS, "module_stream", "postgresql:notexists");
        ContentFilter filter = contentManager.createFilter("my-filter-1", Rule.ALLOW, EntityType.MODULE, criteria, user);
        contentManager.attachFilter("cplabel", filter.getId(), user);
        try {
            contentManager.buildProject("cplabel", empty(), false, user);
            fail("An exception must be thrown.");
        }
        catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof DependencyResolutionException);
        }

        contentManager.detachFilter("cplabel", filter.getId(), user);

        // build with matching filters
        criteria = new FilterCriteria(Matcher.EQUALS, "module_stream", "postgresql:10");
        filter = contentManager.createFilter("my-filter-2", Rule.ALLOW, EntityType.MODULE, criteria, user);
        contentManager.attachFilter("cplabel", filter.getId(), user);
        contentManager.buildProject("cplabel", empty(), false, user);
        Channel targetChannel = env.getTargets().get(0).asSoftwareTarget().get().getChannel();

        // Only the packages from postgresql:10 should be in the target
        assertEquals(MockModulemdApi.getPackageCount("postgresql", "10"), targetChannel.getPackageCount());
        assertFalse(targetChannel.isModular());
    }

    /**
     * Test building with AppStream filters without any modular sources
     */
    public void testBuildProjectRegularSourcesModuleFilters() throws Exception {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        ContentEnvironment env = contentManager.createEnvironment(cp.getLabel(), empty(), "fst", "first env", "desc", false, user);
        Channel channel = createPopulatedChannel();
        contentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);

        FilterCriteria criteria = new FilterCriteria(Matcher.EQUALS, "module_stream", "postgresql:10");
        ContentFilter filter = contentManager.createFilter("my-filter-1", Rule.ALLOW, EntityType.MODULE, criteria, user);
        contentManager.attachFilter("cplabel", filter.getId(), user);
        contentManager.buildProject("cplabel", empty(), false, user);
        Channel targetChannel = env.getTargets().get(0).asSoftwareTarget().get().getChannel();

        // Nothing should be filtered
        assertEquals(1, targetChannel.getPackageCount());
        assertFalse(targetChannel.isModular());
    }

    /**
     * Test building a Project and filtering out Package using NEVRA Filters
     *
     * @throws Exception if anything goes wrong
     */
    public void testBuildProjectWithNevraFilters() throws Exception {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        ContentEnvironment env = contentManager.createEnvironment(cp.getLabel(), empty(), "fst", "first env", "desc", false, user);
        Channel channel = createPopulatedChannel();
        contentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);

        // build without filters
        contentManager.buildProject("cplabel", empty(), false, user);
        assertEquals(1, env.getTargets().get(0).asSoftwareTarget().get().getChannel().getPackageCount());

        // build with filters
        Package pack = channel.getPackages().iterator().next();
        FilterCriteria criteria = new FilterCriteria(Matcher.EQUALS, "nevra", pack.getNameEvra());
        ContentFilter filter = contentManager.createFilter("my-filter", Rule.DENY, ContentFilter.EntityType.PACKAGE, criteria, user);
        contentManager.attachFilter("cplabel", filter.getId(), user);
        contentManager.buildProject("cplabel", empty(), false, user);
        assertEquals(0, env.getTargets().get(0).asSoftwareTarget().get().getChannel().getPackageCount());
    }

    public void testBuildProjectWithErrataFilter() throws Exception {
        ContentProject cp = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(cp);
        ContentEnvironment env = contentManager.createEnvironment(cp.getLabel(), empty(), "fst", "first env", "desc", false, user);
        Channel channel = createPopulatedChannel();
        Errata erratum = channel.getErratas().iterator().next();
        contentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);

        // build without filters
        contentManager.buildProject("cplabel", empty(), false, user);
        assertEquals(1, env.getTargets().get(0).asSoftwareTarget().get().getChannel().getErrataCount());
        assertEquals(erratum, env.getTargets().get(0).asSoftwareTarget().get().getChannel().getErratas().iterator().next());

        // build with filters
        FilterCriteria criteria = new FilterCriteria(Matcher.EQUALS, "advisory_name", erratum.getAdvisoryName());
        ContentFilter filter = contentManager.createFilter("my-filter", Rule.DENY, EntityType.ERRATUM, criteria, user);
        contentManager.attachFilter("cplabel", filter.getId(), user);
        contentManager.buildProject("cplabel", empty(), false, user);
        assertEquals(0, env.getTargets().get(0).asSoftwareTarget().get().getChannel().getErrataCount());
    }

    /**
     * Tests building a project for which build is already in progress
     *
     * @throws Exception if anything goes wrong
     */
    public void testBuildAlreadyBuildingProject() throws Exception {
        var project = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(project);
        var env = contentManager.createEnvironment(project.getLabel(), empty(), "fst", "first env", "desc", false, user);
        var channel = createPopulatedChannel();
        contentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);

        contentManager.buildProject("cplabel", empty(), false, user);
        env.getTargets().iterator().next().setStatus(Status.BUILDING);
        HibernateFactory.getSession().flush();

        try {
            contentManager.buildProject("cplabel", empty(), false, user);
            fail("An exception should have been thrown");
        } catch (ContentManagementException e) {
            // should happen
        }
    }

    /**
     * This scenario tests that the original-clone relation is maintained between CLM channels
     * during {@link ContentProject} building/promoting
     *
     * @throws Exception if anything goes wrong
     */
    public void testClonedChannelLinks() throws Exception {
        var project = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(project);
        var devEnv = contentManager.createEnvironment(project.getLabel(), empty(), "dev", "dev env", "desc", false, user);
        var testEnv = contentManager.createEnvironment(project.getLabel(), of("dev"), "test", "test env", "desc", false, user);

        // build the project with 2 channels
        var channel1 = createPopulatedChannel();
        var channel2 = createPopulatedChannel();
        contentManager.attachSource("cplabel", SW_CHANNEL, channel1.getLabel(), empty(), user);
        contentManager.attachSource("cplabel", SW_CHANNEL, channel2.getLabel(), empty(), user);
        contentManager.buildProject("cplabel", empty(), false, user);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        // check that built channels have the originals set correctly
        assertEquals(Set.of(channel1, channel2), getOriginalChannels(getEnvChannels(devEnv)));

        // promote project
        contentManager.promoteProject("cplabel", "dev", false, user);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        // check the originals of the test environment correspond to channels in the dev environment
        devEnv = ContentManager.lookupEnvironment(devEnv.getLabel(), "cplabel", user).get();
        testEnv = ContentManager.lookupEnvironment(testEnv.getLabel(), "cplabel", user).get();
        assertEquals(getEnvChannels(devEnv), getOriginalChannels(getEnvChannels(testEnv)));

        // delete a source && build the project
        contentManager.detachSource("cplabel", SW_CHANNEL, channel1.getLabel(), user);
        contentManager.buildProject("cplabel", empty(), false, user);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        // check the originals of the "test" channels: one should point to a channel in the "dev" environment,
        // the other should point to the "source" channel1
        var expectedTestOriginals = getEnvChannels(devEnv);
        expectedTestOriginals.add(channel1);
        assertEquals(expectedTestOriginals, getOriginalChannels(getEnvChannels(testEnv)));

        // add the channel again && build
        contentManager.attachSource("cplabel", SW_CHANNEL, channel1.getLabel(), empty(), user);
        contentManager.buildProject("cplabel", empty(), false, user);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        // check the originals of the test environment correspond to channels in the dev environment again
        devEnv = ContentManager.lookupEnvironment(devEnv.getLabel(), "cplabel", user).get();
        testEnv = ContentManager.lookupEnvironment(testEnv.getLabel(), "cplabel", user).get();
        assertEquals(Set.of(channel1, channel2), getOriginalChannels(getEnvChannels(devEnv)));
        assertEquals(getEnvChannels(devEnv), getOriginalChannels(getEnvChannels(testEnv)));
    }

    /**
     * This scenario tests that the original-clone relation is maintained between CLM channels even after inserting
     * a new Environment in the middle and then removing it
     *
     * @throws Exception if anything goes wrong
     */
    public void testClonedChannelLinksInEnvPath() throws Exception {
        var project = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(project);
        var devEnv = contentManager.createEnvironment(project.getLabel(), empty(), "dev", "dev env", "desc", false, user);
        var testEnv = contentManager.createEnvironment(project.getLabel(), of("dev"), "test", "test env", "desc", false, user);

        // build the project with 1 channel
        var channel1 = createPopulatedChannel();
        contentManager.attachSource("cplabel", SW_CHANNEL, channel1.getLabel(), empty(), user);
        contentManager.buildProject("cplabel", empty(), false, user);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        // promote project
        contentManager.promoteProject("cplabel", "dev", false, user);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        // check the originals of the test environment correspond to channels in the dev environment
        devEnv = ContentManager.lookupEnvironment(devEnv.getLabel(), "cplabel", user).get();
        testEnv = ContentManager.lookupEnvironment(testEnv.getLabel(), "cplabel", user).get();
        assertEquals(getEnvChannels(devEnv), getOriginalChannels(getEnvChannels(testEnv)));

        // create a new environment "in the middle"
        var middleEnv = contentManager.createEnvironment(project.getLabel(), of("dev"), "mid", "mid env", "desc", false, user);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();
        devEnv = ContentManager.lookupEnvironment(devEnv.getLabel(), "cplabel", user).get();
        testEnv = ContentManager.lookupEnvironment(testEnv.getLabel(), "cplabel", user).get();

        // check the originals of the mid environment correspond to channels in the dev environment
        assertEquals(getEnvChannels(devEnv), getOriginalChannels(getEnvChannels(middleEnv)));
        // check the originals of the test environment correspond to channels in the mid environment
        assertEquals(getEnvChannels(middleEnv), getOriginalChannels(getEnvChannels(testEnv)));

        // check that after removing the middle env, the originals of the test point to dev channels again
        contentManager.removeEnvironment("mid", "cplabel", user);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();
        devEnv = ContentManager.lookupEnvironment(devEnv.getLabel(), "cplabel", user).get();
        testEnv = ContentManager.lookupEnvironment(testEnv.getLabel(), "cplabel", user).get();
        // check the originals of the mid environment correspond to channels in the dev environment
        assertEquals(getEnvChannels(devEnv), getOriginalChannels(getEnvChannels(testEnv)));
    }

    /**
     * Similar as testClonedChannelLinks, this scenario tests that the original-clone relation
     * is maintained between CLM channels during {@link ContentProject} building/promoting.
     *
     * The difference is that the original state of the links between channel is broken (existing user data
     * from previous versions).
     * The original-clone relation should be fixed by building/promoting the project.
     *
     * @throws Exception if anything goes wrong
     */
    public void testFixingClonedChannelLinks() throws Exception {
        var project = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(project);
        var devEnv = contentManager.createEnvironment(project.getLabel(), empty(), "dev", "dev env", "desc", false, user);
        var testEnv = contentManager.createEnvironment(project.getLabel(), of("dev"), "test", "test env", "desc", false, user);

        // build the project
        var channel1 = createPopulatedChannel();
        contentManager.attachSource("cplabel", SW_CHANNEL, channel1.getLabel(), empty(), user);
        contentManager.buildProject("cplabel", empty(), false, user);

        // check that built channel has the original set correctly
        assertEquals(Set.of(channel1), getOriginalChannels(getEnvChannels(devEnv)));

        // promote project
        contentManager.promoteProject("cplabel", "dev", false, user);

        // now "break" the testEnv channel
        getEnvChannels(testEnv).forEach(chan -> chan.asCloned().get().setOriginal(channel1));
        assertEquals(Set.of(channel1), getOriginalChannels(getEnvChannels(testEnv)));

        // promote project again and check that the original of testEnv channel was fixed
        contentManager.promoteProject("cplabel", "dev", false, user);

        assertEquals(getEnvChannels(devEnv), getOriginalChannels(getEnvChannels(testEnv)));
    }

    /**
     * Similar as testClonedChannelLinks, but in this case we setup the project targets
     * with completely crafted (non-clone) channels.
     *
     * @throws Exception if anything goes wrong
     */
    public void testFixingClonedChannelLinks2() throws Exception {
        var project = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(project);
        var srcChan = createPopulatedChannel();
        contentManager.attachSource("cplabel", SW_CHANNEL, srcChan.getLabel(), empty(), user);

        // 2 environments, 1 channel in each
        var devEnv = contentManager.createEnvironment(project.getLabel(), empty(), "dev", "dev env", "desc", false, user);
        var testEnv = contentManager.createEnvironment(project.getLabel(), of("dev"), "test", "test env", "desc", false, user);

        var devChan = createChannelInEnvironment(devEnv, of(srcChan.getLabel()));
        var devTarget = new SoftwareEnvironmentTarget(devEnv, devChan);
        ContentProjectFactory.save(devTarget);
        devEnv.addTarget(devTarget);

        var testChan = createChannelInEnvironment(testEnv, of(srcChan.getLabel()));
        var testTarget = new SoftwareEnvironmentTarget(testEnv, testChan);
        ContentProjectFactory.save(testTarget);
        testEnv.addTarget(testTarget);

        // let's just check the env. channels are not cloned
        assertFalse(devChan.isCloned());
        assertFalse(testChan.isCloned());

        // let's build the project and check that the procedure fixed the channel in the dev environment
        contentManager.buildProject("cplabel", empty(), false, user);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        devChan = ChannelFactory.lookupById(devChan.getId());
        testChan = ChannelFactory.lookupById(testChan.getId());
        assertEquals(srcChan, devChan.getOriginal());
        assertFalse(testChan.isCloned());

        // let's promote the project and check that the procedure fixed the channel in the test environment as well
        contentManager.promoteProject("cplabel", "dev", false, user);
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        devChan = ChannelFactory.lookupById(devChan.getId());
        testChan = ChannelFactory.lookupById(testChan.getId());
        assertEquals(srcChan, devChan.getOriginal());
        assertEquals(devChan, testChan.getOriginal());
    }

    // extract original channels from given channels
    private Set<Channel> getOriginalChannels(Collection<Channel> channels) {
        return channels.stream().map(Channel::getOriginal).collect(toSet());
    }

    // get channels of given environment
    private Set<Channel> getEnvChannels(ContentEnvironment testEnv) {
        return testEnv.getTargets().stream()
                .flatMap(tgt -> tgt.asSoftwareTarget().stream())
                .map(tgt -> tgt.getChannel())
                .collect(toSet());
    }

    /**
     * Tests building a project for which build is already in progress
     *
     * @throws Exception if anything goes wrong
     */
    public void testPromotingBuildingProject() throws Exception {
        var project = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(project);
        var fstEnv = contentManager.createEnvironment(project.getLabel(), empty(), "fst", "first env", "fst", false, user);
        var sndEnv = contentManager.createEnvironment(project.getLabel(), of("fst"), "snd", "second env", "snd", false, user);
        var channel = createPopulatedChannel();
        contentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);

        contentManager.buildProject("cplabel", empty(), false, user);

        // 1st promote runs ok
        contentManager.promoteProject("cplabel", "fst", false, user);

        // now we change the target from the 1st environment to BUILDING
        fstEnv.getTargets().iterator().next().setStatus(Status.BUILDING);

        // now the promote must fail
        try {
            contentManager.promoteProject("cplabel", "fst", false, user);
            fail("An exception should have been thrown");
        }
        catch (ContentManagementException e) {
            // should happen
        }
    }

    /**
     * Tests building a project for which build is already in progress
     *
     * @throws Exception if anything goes wrong
     */
    public void testPromotingPromotingProject() throws Exception {
        var project = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(project);
        var fstEnv = contentManager.createEnvironment(project.getLabel(), empty(), "fst", "first env", "fst", false, user);
        var sndEnv = contentManager.createEnvironment(project.getLabel(), of("fst"), "snd", "second env", "snd", false, user);
        var channel = createPopulatedChannel();
        contentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);

        contentManager.buildProject("cplabel", empty(), false, user);

        // 1st promote runs ok
        contentManager.promoteProject("cplabel", "fst", false, user);

        // now we change the target from the 2nd environment to BUILDING
        sndEnv.getTargets().iterator().next().setStatus(Status.BUILDING);

        // now the promote must fail
        try {
            contentManager.promoteProject("cplabel", "fst", false, user);
            fail("An exception should have been thrown");
        }
        catch (ContentManagementException e) {
            // should happen
        }
    }

    /**
     * Complex scenario for testing building/promoting a project in which build/promote operations are in progress.
     *
     * @throws Exception if anything goes wrong
     */
    public void testBuildPromoteInProgress() throws Exception {
        var project = new ContentProject("cplabel", "cpname", "cpdesc", user.getOrg());
        ContentProjectFactory.save(project);
        var env1 = contentManager.createEnvironment(project.getLabel(), empty(), "env1", "env 1", "1", false, user);
        var env2 = contentManager.createEnvironment(project.getLabel(), of("env1"), "env2", "env 2", "2", false, user);
        var env3 = contentManager.createEnvironment(project.getLabel(), of("env2"), "env3", "env 3", "3", false, user);
        var env4 = contentManager.createEnvironment(project.getLabel(), of("env3"), "env4", "env 4", "4", false, user);
        var env5 = contentManager.createEnvironment(project.getLabel(), of("env4"), "env5", "env 5", "5", false, user);
        var channel = createPopulatedChannel();
        contentManager.attachSource("cplabel", SW_CHANNEL, channel.getLabel(), empty(), user);

        // build & promote everything possible
        contentManager.buildProject("cplabel", empty(), false, user);
        List.of(1, 2, 3, 4).forEach(i ->
                contentManager.promoteProject("cplabel", "env" + i, false, user));

        // PHASE 1: Test building
        // 1st target BUILDING -> requested build should fail
        getFirstTarget(env1).setStatus(Status.BUILDING);
        assertBuildFails("cplabel");
        getFirstTarget(env1).setStatus(Status.BUILT); // revert

        // 2nd target BUILDING -> requested build should fail
        getFirstTarget(env2).setStatus(Status.BUILDING);
        assertBuildFails("cplabel");
        getFirstTarget(env2).setStatus(Status.BUILT); // revert

        // 3rd target BUILDING -> build passes
        getFirstTarget(env3).setStatus(Status.BUILDING);
        try {
            contentManager.buildProject("cplabel", empty(), false, user);
        }
        catch (ContentManagementException e) {
            fail("No ContentManagementException expected");
        }
        getFirstTarget(env3).setStatus(Status.BUILT); // revert

        // PHASE 2: Test promoting of environment env2
        // 1st promote is OK
        try {
            contentManager.promoteProject("cplabel", "env2", false, user);
        }
        catch (ContentManagementException e) {
            fail("No ContentManagementException expected");
        }

        // env2 itself is building -> requested promote should fail
        getFirstTarget(env2).setStatus(Status.BUILDING);
        assertPromoteFails("cplabel", "env2");
        getFirstTarget(env2).setStatus(Status.BUILT); // revert

        // env3 is building -> requested promote should fail
        getFirstTarget(env3).setStatus(Status.BUILDING);
        assertPromoteFails("cplabel", "env2");
        getFirstTarget(env3).setStatus(Status.BUILT); // revert

        // env4 is building -> requested promote should fail
        getFirstTarget(env4).setStatus(Status.BUILDING);
        assertPromoteFails("cplabel", "env2");
        getFirstTarget(env4).setStatus(Status.BUILT); // revert

        // env5 is building -> requested promote should be ok
        getFirstTarget(env5).setStatus(Status.BUILDING);
        try {
            contentManager.promoteProject("cplabel", "env2", false, user);
        }
        catch (ContentManagementException e) {
            fail("No ContentManagementException expected");
        }
        getFirstTarget(env5).setStatus(Status.BUILT); // revert
    }

    private void assertBuildFails(String projectLabel) {
        try {
            contentManager.buildProject(projectLabel, empty(), false, user);
            fail("An exception should have been thrown");
        }
        catch (ContentManagementException e) {
            // should happen
        }
    }

    private void assertPromoteFails(String projectLabel, String envLabel) {
        try {
            contentManager.promoteProject(projectLabel, envLabel, false, user);
            fail("An exception should have been thrown");
        }
        catch (ContentManagementException e) {
            // should happen
        }
    }

    private EnvironmentTarget getFirstTarget(ContentEnvironment env) {
        return env.getTargets().iterator().next();
    }

    private Channel createPopulatedChannel() throws Exception {
        Channel channel = TestUtils.reload(ChannelFactoryTest.createTestChannel(user, false));
        channel.setChecksumType(ChannelFactory.findChecksumTypeByLabel("sha1"));
        Package pack = PackageTest.createTestPackage(user.getOrg());
        Errata errata = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        channel.addPackage(pack);
        channel.addErrata(errata);
        ChannelFactory.save(channel);
        return channel;
    }

    // simulates a Channel in an Environment
    private Channel createChannelInEnvironment(ContentEnvironment env, Optional<String> label) throws Exception {
        Channel channel = createPopulatedChannel();
        String prefix = env.getContentProject().getLabel() + "-" + env.getLabel() + "-";
        channel.setLabel(prefix + label.orElse(channel.getLabel()));
        channel.setName(prefix + label.orElse(channel.getName()));
        return channel;
    }
}
