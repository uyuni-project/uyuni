/**
 * Copyright (c) 2009--2017 Red Hat, Inc.
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
package com.redhat.rhn.manager.errata.test;

import static com.redhat.rhn.domain.role.RoleFactory.ORG_ADMIN;
import static com.redhat.rhn.testing.ErrataTestUtils.createLaterTestPackage;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestInstalledPackage;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestPackage;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestServer;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.errata.ErrataAction;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.errata.Bug;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.errata.Keyword;
import com.redhat.rhn.domain.errata.impl.PublishedBug;
import com.redhat.rhn.domain.errata.impl.PublishedErrata;
import com.redhat.rhn.domain.errata.impl.PublishedKeyword;
import com.redhat.rhn.domain.errata.impl.UnpublishedBug;
import com.redhat.rhn.domain.errata.impl.UnpublishedErrata;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.rhnpackage.test.PackageEvrFactoryTest;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.session.WebSession;
import com.redhat.rhn.domain.session.WebSessionFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ErrataOverview;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.frontend.xmlrpc.system.test.SystemHandlerTest;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.errata.cache.ErrataCacheManager;
import com.redhat.rhn.manager.errata.cache.test.ErrataCacheManagerTest;
import com.redhat.rhn.manager.rhnpackage.PackageManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.ErrataTestUtils;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.hibernate.criterion.Restrictions;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Tests {@link ErrataManager}.
 */
public class ErrataManagerTest extends JMockBaseTestCaseWithUser {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ClassImposteriser.INSTANCE);
    }

    public static Bug createNewPublishedBug(Long id, String summary) {
        return ErrataManager.createNewPublishedBug(id, summary,
                "https://bugzilla.redhat.com/show_bug.cgi?id=" + id);
    }

    public static Bug createNewUnpublishedBug(Long id, String summary) {
        return ErrataManager.createNewUnpublishedBug(id, summary,
                "https://bugzilla.redhat.com/show_bug.cgi?id=" + id);
    }

    public void testPublish() throws Exception {
        User user = UserTestUtils.findNewUser();
        Errata e = ErrataFactoryTest.createTestUnpublishedErrata(user.getOrg().getId());
        assertFalse(e.isPublished()); //should be unpublished
      //publish errata and store back into self
        e = ErrataManager.publish(e, new HashSet(), user);
        assertTrue(e.isPublished());  //should be published
    }

    public void testStore() throws Exception {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        Errata e = ErrataFactoryTest.createTestErrata(user.getOrg().getId());

        e.setAdvisoryName(TestUtils.randomString());
        ErrataManager.storeErrata(e);

        Errata e2 = ErrataManager.lookupErrata(e.getId(), user);
        assertEquals(e.getAdvisoryName(), e2.getAdvisoryName());
    }

    public void testCreate() {
        Errata e = ErrataManager.createNewErrata();
        assertTrue(e instanceof UnpublishedErrata);

        Bug b = createNewUnpublishedBug(87L, "test bug");
        assertTrue(b instanceof UnpublishedBug);

        Bug b2 = ErrataManagerTest.createNewPublishedBug(42L, "test bug");
        assertTrue(b2 instanceof PublishedBug);
    }

    public void testSearchByPackagesIds() throws Exception {
        searchByPackagesIdsHelper(
                Optional.empty(),
                (pids) -> ErrataManager.searchByPackageIds(pids));
    }

    public void testSearchByPackagesIdsInOrg() throws Exception {
        Channel channel = ChannelTestUtils.createTestChannel(user);
        searchByPackagesIdsHelper(
                Optional.of(channel),
                (pids) -> ErrataManager.searchByPackageIdsWithOrg(pids, user.getOrg()));
    }

    private void searchByPackagesIdsHelper(Optional<Channel> channel, Function<List, List<ErrataOverview>> errataSearchFn) throws Exception {
        Package p = PackageTest.createTestPackage(user.getOrg());
        // errata search is done by the search-server. The search
        // in ErrataManager is to load ErrataOverview objects from
        // the results of the search-server searches.
        Bug b1 = ErrataManagerTest.createNewPublishedBug(42L, "test bug");
        assertTrue(b1 instanceof PublishedBug);
        Errata e = ErrataManager.createNewErrata();
        assertTrue(e instanceof UnpublishedErrata);
        e.setAdvisory("ZEUS-2007");
        e.setAdvisoryName("ZEUS-2007");
        e.setAdvisoryRel(1L);
        e.setAdvisoryType("Security Advisory");
        e.setProduct("Red Hat Enterprise Linux");
        e.setSynopsis("Just a test errata");
        e.setSolution("This errata fixes nothing, it's just a damn test.");
        e.setIssueDate(new Date());
        e.setUpdateDate(e.getIssueDate());
        e.addPackage(p);
        Errata publishedErrata = ErrataManager.publish(e);
        assertTrue(publishedErrata instanceof PublishedErrata);

        channel.ifPresent(c -> publishedErrata.addChannel(c));

        WebSession session = WebSessionFactory.createSession();
        WebSessionFactory.save(session);
        assertNotNull(session.getId());

        // for package search, we need to insert an entry into rhnVisibleObjects
        WriteMode mode = ModeFactory.getWriteMode(
                "test_queries", "insert_into_visibleobjects");
        Map<String, Object> params = new HashMap<String, Object>();
        //"sessionid, obj_id, obj_type"
        params.put("sessionid", session.getId());
        params.put("obj_id", publishedErrata.getId());
        params.put("obj_type", "errata");
        mode.executeUpdate(params);

        // now test for errata
        List pids = new ArrayList();
        pids.add(p.getId());
        List<ErrataOverview> eos = errataSearchFn.apply(pids);
        assertNotNull(eos);
        assertEquals(1, eos.size());
        ErrataOverview eo = eos.get(0);
        assertNotNull(eo);
        assertEquals(publishedErrata.getAdvisory(), eo.getAdvisory());
    }

    public void testSearch() throws Exception {
        // errata search is done by the search-server. The search
        // in ErrataManager is to load ErrataOverview objects from
        // the results of the search-server searches.
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        Package p = PackageTest.createTestPackage(user.getOrg());
        Errata e = ErrataManager.createNewErrata();
        assertTrue(e instanceof UnpublishedErrata);
        e.setAdvisory("ZEUS-2007");
        e.setAdvisoryName("ZEUS-2007");
        e.setAdvisoryRel(1L);
        e.setAdvisoryType("Security Advisory");
        e.setProduct("Red Hat Enterprise Linux");
        e.setSynopsis("Just a test errata");
        e.setSolution("This errata fixes nothing, it's just a damn test.");
        e.setIssueDate(new Date());
        e.setUpdateDate(e.getIssueDate());
        e.addPackage(p);

        Channel baseChannel = ChannelTestUtils.createBaseChannel(user);
        List<Errata> errataList = new ArrayList<Errata>();
        errataList.add(e);
        List<Errata> publishedList = ErrataFactory.publishToChannel(errataList,
                baseChannel, user, false);
        Errata publish = publishedList.get(0);
        assertTrue(publish instanceof PublishedErrata);

        List eids = new ArrayList();
        eids.add(publish.getId());
        List<ErrataOverview> eos = ErrataManager.search(eids, user.getOrg());
        assertNotNull(eos);
        assertEquals(1, eos.size());
    }

    public void testRelevantErrataList() throws Exception {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        ErrataCacheManagerTest.createServerNeededCache(user,
                ErrataFactory.ERRATA_TYPE_BUG);
        DataResult errata = ErrataManager.relevantErrata(user);
        assertNotNull(errata);
        assertTrue(errata.size() >= 1);
    }

    public void testRelevantErrataByTypeList() throws Exception {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        ErrataCacheManagerTest.createServerNeededCache(user,
                ErrataFactory.ERRATA_TYPE_BUG);
        PageControl pc = new PageControl();
        pc.setStart(1);
        pc.setPageSize(20);
        DataResult errata =
            ErrataManager.relevantErrataByType(user, pc, ErrataFactory.ERRATA_TYPE_BUG);
        assertNotNull(errata);
        assertTrue(errata.size() >= 1);
    }

    public void testUnpublishedErrata() {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        DataResult errata = ErrataManager.unpublishedOwnedErrata(user);
        assertNotNull(errata);
        assertTrue(errata.size() <= 20);
    }

    public void testUnpublishedInSet() {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        PageControl pc = new PageControl();
        pc.setStart(1);
        pc.setPageSize(20);
        DataResult errata = ErrataManager.unpublishedInSet(user, pc, "errata_to_delete");
        assertNotNull(errata);
        assertTrue(errata.isEmpty());
        assertFalse(errata.size() > 0);
    }

    public void testLookupErrata() throws Exception {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        Errata errata = ErrataFactoryTest.createTestErrata(user.getOrg().getId());

        // Check for the case where the errata belongs to the users org
        Errata check = ErrataManager.lookupErrata(errata.getId(), user);
        assertTrue(check.getAdvisory().equals(errata.getAdvisory()));
        assertTrue(check.getId().equals(errata.getId()));

        /*
         * Bugzilla: 168292
         * Make sure we handle the case when returnedErrata.getOrg == null without throwing
         * an NPE.
         */
        errata.setOrg(null);
        ErrataManager.storeErrata(errata);

        try {
            check = ErrataManager.lookupErrata(errata.getId(), user);
            fail();
        }
        catch (LookupException e) {
            //This means we hit the returnedErrata.getOrg == null path successfully
        }
        Org org2 = OrgFactory.lookupById(UserTestUtils.createOrg("testOrg2"));
        errata.setOrg(org2);
        ErrataManager.storeErrata(errata);

        try {
            check = ErrataManager.lookupErrata(errata.getId(), user);
        }
        catch (LookupException e) {
            //This means we hit returnedErrata.getOrg().getId() != user.getOrg().getId()
        }

        // Check for non-existant errata
        try {
            check = ErrataManager.lookupErrata((long) -1234, user);
            fail();
        }
        catch (LookupException e) {
            //This means we hit the returnedErrata == null path successfully
        }
    }

    public void testSystemsAffected() throws Exception {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        PageControl pc = new PageControl();
        pc.setStart(1);
        pc.setPageSize(5);

        Errata a = ErrataFactoryTest.createTestErrata(UserTestUtils.createOrg("testOrg" +
                    this.getClass().getSimpleName()));

        DataResult systems = ErrataManager.systemsAffected(user, a.getId(), pc);
        assertNotNull(systems);
        assertTrue(systems.isEmpty());
        assertFalse(systems.size() > 0);

        DataResult systems2 = ErrataManager.systemsAffected(user, (long) -2, pc);
        assertTrue(systems2.isEmpty());
    }

    public void testAdvisoryNameUnique() throws Exception {
        Errata e1 = ErrataFactoryTest.createTestErrata(UserTestUtils.createOrg("testOrg" +
                    this.getClass().getSimpleName()));
        Thread.sleep(100); //sleep for a bit to make sure we get unique advisoryNames
        Errata e2 = ErrataFactoryTest.createTestErrata(UserTestUtils.createOrg("testOrg" +
                    this.getClass().getSimpleName()));

        assertFalse(e1.getId().equals(e2.getId())); //make sure adv names are different
        //assertTrue(ErrataManager.advisoryNameIsUnique(e2.getId(), e2.getAdvisoryName()));
        //assertFalse(ErrataManager.advisoryNameIsUnique(e2.getId(), e1.getAdvisoryName()));
    }

    // Don't need this test to actually run right now.  Its experimental.
    public void xxxxLookupErrataByAdvisoryType() throws IOException {

        String bugfix = "Bug Fix Advisory";
        String pea = "Product Enhancement Advisory";
        String security = "Security Advisory";

        StopWatch st = new StopWatch();
        st.start();
        List erratas = ErrataManager.lookupErrataByType(bugfix);
        outputErrataList(erratas);
        System.out.println("Got bugfixes: "  + erratas.size() + " time: " + st);
        assertTrue(erratas.size() > 0);
        erratas = ErrataManager.lookupErrataByType(pea);
        outputErrataList(erratas);
        System.out.println("Got pea enhancments: "  + erratas.size() + " time: " + st);
        assertTrue(erratas.size() > 0);
        erratas = ErrataManager.lookupErrataByType(security);
        outputErrataList(erratas);
        assertTrue(erratas.size() > 0);
        System.out.println("Got security advisories: "  + erratas.size() + " time: " + st);
        st.stop();
        System.out.println("TIME: " + st.getTime());
    }

    private void outputErrataList(List erratas) throws IOException {
        StringBuffer output = new StringBuffer();
        Iterator i = erratas.iterator();
        while (i.hasNext()) {
            Errata e = (Errata) i.next();
            output.append(e.toString());
        }
        FileWriter fr = new FileWriter(new File("errataout" + erratas.size() +  ".txt"));
        fr.write(output.toString());
        fr.close();
    }

    public void testErrataInSet() throws Exception {
        User user = UserTestUtils.findNewUser();

        Errata e = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        e = TestUtils.saveAndReload(e);
        RhnSet set = RhnSetDecl.ERRATA_TO_REMOVE.get(user);
        set.add(e.getId());
        RhnSetManager.store(set);

        List<ErrataOverview> list = ErrataManager.errataInSet(user, set.getLabel());
        boolean found = false;
        for (ErrataOverview item : list) {
            if (item.getId().equals(e.getId())) {
                found = true;
            }
        }
        assertTrue(found);

    }

    public void testCloneChannelErrata() throws Exception {
        Channel original = ChannelFactoryTest.createTestChannel(user);
        final Errata errata1 =
                ErrataFactoryTest.createTestPublishedErrata(user.getOrg().getId());
        final Errata errata2 =
                ErrataFactoryTest.createTestPublishedErrata(user.getOrg().getId());
        final Errata errata3 =
                ErrataFactoryTest.createTestPublishedErrata(user.getOrg().getId());

        original.addErrata(errata1);
        original.addErrata(errata2);
        original.addErrata(errata3);

        // clone it
        Channel cloned = ChannelFactoryTest.createTestClonedChannel(original, user);
        cloned.addErrata(errata1);
        cloned.addErrata(errata3);

        List<ErrataOverview> toClone = ErrataFactory
                .relevantToOneChannelButNotAnother(original.getId(), cloned.getId());
        Set<Long> eids = ErrataManager.cloneChannelErrata(toClone, cloned.getId(), user);
        assertTrue(eids.size() > 0);
        assertTrue(new HashSet<Long>(eids).size() == eids.size());
    }

    /**
     * Tests applyErrata(), note that the onlyRelevant flag is always set in
     * this case. {@link SystemHandlerTest#testApplyIrrelevantErrata} covers the
     * case in which the flag is false.
     *
     * @throws Exception if something goes wrong
     */
    @SuppressWarnings("unchecked")
    public void testApplyErrata() throws Exception {

        Errata errata1 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        TestUtils.saveAndFlush(errata1);
        Errata errata2 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        TestUtils.saveAndFlush(errata2);
        Errata errata3 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        TestUtils.saveAndFlush(errata3);

        Channel channel1 = ChannelFactoryTest.createTestChannel(user);
        Channel channel2 = ChannelFactoryTest.createTestChannel(user);
        Channel channel3 = ChannelFactoryTest.createTestChannel(user);

        Set<Channel> server1Channels = new HashSet<Channel>();
        server1Channels.add(channel1);
        server1Channels.add(channel3);
        Server server1 = createTestServer(user, server1Channels);

        Set<Channel> server2Channels = new HashSet<Channel>();
        server2Channels.add(channel2);
        server2Channels.add(channel3);
        Server server2 = createTestServer(user, server2Channels);

        // server 1 has an errata for package1 available
        com.redhat.rhn.domain.rhnpackage.Package package1 =
                createTestPackage(user, channel1, "noarch");
        createTestInstalledPackage(package1, server1);
        createLaterTestPackage(user, errata1, channel1, package1);

        // server 2 has an errata for package2 available
        Package package2 = createTestPackage(user, channel2, "noarch");
        createTestInstalledPackage(package2, server2);
        createLaterTestPackage(user, errata2, channel2, package2);

        // errata in common for both servers
        Package package3 = createTestPackage(user, channel3, "noarch");
        createTestInstalledPackage(package3, server1);
        createTestInstalledPackage(package3, server2);
        createLaterTestPackage(user, errata3, channel3, package3);

        ErrataCacheManager.insertNeededErrataCache(
                server1.getId(), errata1.getId(), package1.getId());
        ErrataCacheManager.insertNeededErrataCache(
                server2.getId(), errata2.getId(), package2.getId());
        // Erata 3 is common to server 1 and server 2
        ErrataCacheManager.insertNeededErrataCache(
                server1.getId(), errata3.getId(), package3.getId());
        ErrataCacheManager.insertNeededErrataCache(
                server2.getId(), errata3.getId(), package3.getId());
        HibernateFactory.getSession().flush();

        List<Long> errataIds = new ArrayList<Long>();
        errataIds.add(errata1.getId());
        errataIds.add(errata2.getId());
        errataIds.add(errata3.getId());

        List<Long> serverIds = new ArrayList<Long>();
        serverIds.add(server1.getId());
        serverIds.add(server2.getId());

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ErrataManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            allowing(taskomaticMock).scheduleMinionActionExecutions(with(any(List.class)),with(any(Boolean.class)));
        } });

        ErrataManager.applyErrata(user, errataIds, new Date(), serverIds);

        // we want to check that no matter how many actions were scheduled for
        // server1, all the erratas included in those scheduled actions for
        // server1 do not contain the erratas for server2

        List<Action> actionsServer1 = ActionFactory.listActionsForServer(user, server1);
        Set<Long> server1ScheduledErrata = new HashSet<Long>();
        for (Action a : actionsServer1) {
            ErrataAction errataAction = errataActionFromAction(a);
            for (Errata e : errataAction.getErrata()) {
                server1ScheduledErrata.add(e.getId());
            }
        }

        List<Action> actionsServer2 = ActionFactory.listActionsForServer(user, server2);
        Set<Long> server2ScheduledErrata = new HashSet<Long>();
        for (Action a : actionsServer2) {
            ErrataAction errataAction = errataActionFromAction(a);
            for (Errata e : errataAction.getErrata()) {
                server2ScheduledErrata.add(e.getId());
            }
        }

        assertEquals("Server 1 Scheduled Erratas has 2 erratas (errata1 and errata3)",
                2, server1ScheduledErrata.size());
        assertFalse("Server 1 Scheduled Erratas do not include other server's errata",
                server1ScheduledErrata.contains(errata2.getId()));
        assertTrue("Server 1 Scheduled Erratas contain relevant erratas",
                server1ScheduledErrata.contains(errata1.getId()));
        assertTrue("Server 1 Scheduled Erratas contain relevant erratas",
                server1ScheduledErrata.contains(errata3.getId()));

        assertEquals("Server 2 Scheduled Erratas has 2 erratas (errata2 and errata3)",
                2, server2ScheduledErrata.size());
        assertFalse("Server 2 Scheduled Erratas do not include other server's errata",
                server2ScheduledErrata.contains(errata1.getId()));
        assertTrue("Server 2 Scheduled Erratas contain relevant erratas",
                server2ScheduledErrata.contains(errata2.getId()));
        assertTrue("Server 2 Scheduled Erratas contain relevant erratas",
                server2ScheduledErrata.contains(errata3.getId()));
    }

    /**
     * Test that with 2 software management stack erratas, where one system
     * is affected by one of them, and the other by both, they are scheduled
     * before other erratas, and bundled in a single action.
     *
     * @throws Exception the exception
     */
    @SuppressWarnings("unchecked")
    public void testApplyErrataOnManagementStack() throws Exception {

        Errata errata1 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        TestUtils.saveAndFlush(errata1);
        Errata errata2 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        TestUtils.saveAndFlush(errata2);
        Errata errata3 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        TestUtils.saveAndFlush(errata2);

        // software management stack erratas
        Errata yumErrata1 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        yumErrata1.addKeyword("restart_suggested");
        TestUtils.saveAndFlush(yumErrata1);
        Errata yumErrata2 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        yumErrata2.addKeyword("restart_suggested");
        TestUtils.saveAndFlush(yumErrata2);


        Channel channel1 = ChannelFactoryTest.createTestChannel(user);
        Channel channel2 = ChannelFactoryTest.createTestChannel(user);
        Channel channel3 = ChannelFactoryTest.createTestChannel(user);

        Set<Channel> server1Channels = new HashSet<Channel>();
        server1Channels.add(channel1);
        server1Channels.add(channel3);
        Server server1 = createTestServer(user, server1Channels);

        Set<Channel> server2Channels = new HashSet<Channel>();
        server2Channels.add(channel2);
        server2Channels.add(channel3);
        Server server2 = createTestServer(user, server2Channels);

        // server 1 has an errata for package1 available
        com.redhat.rhn.domain.rhnpackage.Package package1 =
                createTestPackage(user, channel1, "noarch");
        createTestInstalledPackage(package1, server1);
        createLaterTestPackage(user, errata1, channel1, package1);

        // server 2 has an errata for package2 available
        Package package2 = createTestPackage(user, channel2, "noarch");
        createTestInstalledPackage(package2, server2);
        createLaterTestPackage(user, errata2, channel2, package2);

        // errata does not affect any system
        Package package3 = createTestPackage(user, channel3, "noarch");
        // they systems do not have package3 installed
        createLaterTestPackage(user, errata3, channel3, package3);

        // server1 is affected by both yum erratas
        // server2 only by one
        Package yumPackage1 = createTestPackage(user, channel3, "noarch");
        Package yumPackage2 = createTestPackage(user, channel3, "noarch");

        createTestInstalledPackage(yumPackage1, server1);
        createTestInstalledPackage(yumPackage2, server1);

        createTestInstalledPackage(yumPackage1, server2);

        // they systems do not have package3 installed
        createLaterTestPackage(user, yumErrata1, channel3, yumPackage1);
        createLaterTestPackage(user, yumErrata2, channel3, yumPackage2);


        ErrataCacheManager.insertNeededErrataCache(
                server1.getId(), errata1.getId(), package1.getId());
        ErrataCacheManager.insertNeededErrataCache(
                server2.getId(), errata2.getId(), package2.getId());

        ErrataCacheManager.insertNeededErrataCache(
                server1.getId(), yumErrata1.getId(), yumPackage1.getId());
        ErrataCacheManager.insertNeededErrataCache(
                server1.getId(), yumErrata2.getId(), yumPackage2.getId());
        ErrataCacheManager.insertNeededErrataCache(
                server2.getId(), yumErrata1.getId(), yumPackage1.getId());
        HibernateFactory.getSession().flush();

        List<Long> errataIds = new ArrayList<Long>();
        errataIds.add(errata1.getId());
        errataIds.add(errata2.getId());
        errataIds.add(errata3.getId());
        errataIds.add(yumErrata1.getId());
        errataIds.add(yumErrata2.getId());

        List<Long> serverIds = new ArrayList<Long>();
        serverIds.add(server1.getId());
        serverIds.add(server2.getId());

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ErrataManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
        } });

        ErrataManager.applyErrata(user, errataIds, new Date(), serverIds);

        // we want to check that no matter how many actions were scheduled for
        // server1, all the erratas included in those scheduled actions for
        // server1 do not contain the erratas for server2

        List<Action> actionsServer1 = ActionFactory.listActionsForServer(user, server1);
        Set<Long> server1ScheduledErrata = new HashSet<Long>();
        for (Action a : actionsServer1) {
            ErrataAction errataAction = errataActionFromAction(a);
            for (Errata e : errataAction.getErrata()) {
                server1ScheduledErrata.add(e.getId());
            }
        }

        List<Action> actionsServer2 = ActionFactory.listActionsForServer(user, server2);
        Set<Long> server2ScheduledErrata = new HashSet<Long>();
        for (Action a : actionsServer2) {
            ErrataAction errataAction = errataActionFromAction(a);
            for (Errata e : errataAction.getErrata()) {
                server2ScheduledErrata.add(e.getId());
            }
        }

        assertEquals("Server 1 Scheduled has 3 scheduled actions",
                3, actionsServer1.size());
        assertEquals("Server 1 Scheduled Erratas has 3 erratas (errata1 and both" +
                " yumErratas)", 3, server1ScheduledErrata.size());
        assertFalse("Server 1 Scheduled Erratas do not include irrelevant errata",
                server1ScheduledErrata.contains(errata3.getId()));
        assertTrue("Server 1 Scheduled Erratas contain relevant erratas",
                server1ScheduledErrata.contains(errata1.getId()));
        assertTrue("Server 1 Scheduled Erratas contain both yum erratas",
                server1ScheduledErrata.contains(yumErrata1.getId()));
        assertTrue("Server 1 Scheduled Erratas contain both yum erratas",
                server1ScheduledErrata.contains(yumErrata2.getId()));
        assertTrue("Server 1 Scheduled Erratas contain both yum erratas",
                server1ScheduledErrata.contains(yumErrata2.getId()));

        List<Action> updateStackErrataActions1 = actionsServer1.stream()
            .filter(a -> errataActionFromAction(a).getErrata().stream()
                .anyMatch(ErrataManagerTest::doesUpdateStack))
            .collect(Collectors.toList());
        List<Action> nonUpdateStackErrataActions1 = actionsServer1.stream()
            .filter(a -> !updateStackErrataActions1.contains(a))
            .collect(Collectors.toList());

        assertTrue("Action does not mix update-stack and non-update stack erratas",
            updateStackErrataActions1.stream()
                .flatMap(a -> errataActionFromAction(a).getErrata().stream())
                .allMatch(ErrataManagerTest::doesUpdateStack));

        assertTrue("Actions without update-stack erratas come after those that have",
            nonUpdateStackErrataActions1.stream()
                .allMatch(a -> updateStackErrataActions1.stream()
                    .allMatch(b -> b.getId() < a.getId())));

        assertEquals("Server 2 Scheduled has 2 scheduled actions",
                2, actionsServer2.size());
        assertEquals("Server 2 Scheduled Erratas has 2 erratas (errata2 and yumErrata1)",
                2, server2ScheduledErrata.size());
        assertFalse("Server 2 Scheduled Erratas do not include irrelevant errata",
                server2ScheduledErrata.contains(errata3.getId()));
        assertTrue("Server 2 Scheduled Erratas contain relevant erratas",
                server2ScheduledErrata.contains(errata2.getId()));
        assertTrue("Server 2 Scheduled Erratas contain one yum errata",
                server2ScheduledErrata.contains(yumErrata1.getId()));

        List<Action> updateStackErrataActions2 = actionsServer2.stream()
                .filter(a -> errataActionFromAction(a).getErrata().stream()
                    .anyMatch(ErrataManagerTest::doesUpdateStack))
                .collect(Collectors.toList());
        List<Action> nonUpdateStackErrataActions2 = actionsServer2.stream()
            .filter(a -> !updateStackErrataActions2.contains(a))
            .collect(Collectors.toList());

        assertTrue("Action does not mix update-stack and non-update stack erratas",
            updateStackErrataActions2.stream()
                .flatMap(a -> errataActionFromAction(a).getErrata().stream())
                .allMatch(ErrataManagerTest::doesUpdateStack));

        assertTrue("Actions without update-stack erratas come after those that have",
            nonUpdateStackErrataActions2.stream()
                .allMatch(a -> updateStackErrataActions2.stream()
                    .allMatch(b -> b.getId() < a.getId())));
    }

    private static boolean doesUpdateStack(Errata e) {
        return e.getKeywords().stream()
            .anyMatch(k -> ((PublishedKeyword) k).getKeyword().equals("restart_suggested"));
    }

    /**
     * Test that with 2 software management stack erratas, where one system
     * is affected by one of them, and the other by both, they are scheduled
     * before other erratas, and bundled in a single action.
     * The used package manager is zypp
     *
     * @throws Exception the exception
     */
    @SuppressWarnings("unchecked")
    public void testApplyErrataOnManagementStackForZypp() throws Exception {

        Errata errata1 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        TestUtils.saveAndFlush(errata1);
        Errata errata2 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        TestUtils.saveAndFlush(errata2);
        Errata errata3 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        TestUtils.saveAndFlush(errata2);

        // software management stack erratas
        Errata yumErrata1 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        yumErrata1.addKeyword("restart_suggested");
        TestUtils.saveAndFlush(yumErrata1);
        Errata yumErrata2 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        yumErrata2.addKeyword("restart_suggested");
        TestUtils.saveAndFlush(yumErrata2);


        Channel channel1 = ChannelFactoryTest.createTestChannel(user);
        Channel channel2 = ChannelFactoryTest.createTestChannel(user);
        Channel channel3 = ChannelFactoryTest.createTestChannel(user);

        Set<Channel> server1Channels = new HashSet<Channel>();
        server1Channels.add(channel1);
        server1Channels.add(channel3);
        Server server1 = createTestServer(user, server1Channels);

        Set<Channel> server2Channels = new HashSet<Channel>();
        server2Channels.add(channel2);
        server2Channels.add(channel3);
        Server server2 = createTestServer(user, server2Channels);

        // add zypper as installed package
        Package zypperPkg = PackageTest.createTestPackage(user.getOrg());
        PackageName name = PackageManager.lookupPackageName("zypper");
        if (name == null) {
            name = zypperPkg.getPackageName();
            name.setName("zypper");
            TestUtils.saveAndFlush(name);
        }
        else {
            // Handle the case that the package name exists in the DB
            zypperPkg.setPackageName(name);
            TestUtils.saveAndFlush(zypperPkg);
        }
        createTestInstalledPackage(zypperPkg, server1);
        createTestInstalledPackage(zypperPkg, server2);

        // server 1 has an errata for package1 available
        com.redhat.rhn.domain.rhnpackage.Package package1 =
                createTestPackage(user, channel1, "noarch");
        createTestInstalledPackage(package1, server1);
        createLaterTestPackage(user, errata1, channel1, package1);

        // server 2 has an errata for package2 available
        Package package2 = createTestPackage(user, channel2, "noarch");
        createTestInstalledPackage(package2, server2);
        createLaterTestPackage(user, errata2, channel2, package2);

        // errata does not affect any system
        Package package3 = createTestPackage(user, channel3, "noarch");
        // they systems do not have package3 installed
        createLaterTestPackage(user, errata3, channel3, package3);

        // server1 is affected by both yum erratas
        // server2 only by one
        Package yumPackage1 = createTestPackage(user, channel3, "noarch");
        Package yumPackage2 = createTestPackage(user, channel3, "noarch");

        createTestInstalledPackage(yumPackage1, server1);
        createTestInstalledPackage(yumPackage2, server1);

        createTestInstalledPackage(yumPackage1, server2);

        // they systems do not have package3 installed
        createLaterTestPackage(user, yumErrata1, channel3, yumPackage1);
        createLaterTestPackage(user, yumErrata2, channel3, yumPackage2);


        ErrataCacheManager.insertNeededErrataCache(
                server1.getId(), errata1.getId(), package1.getId());
        ErrataCacheManager.insertNeededErrataCache(
                server2.getId(), errata2.getId(), package2.getId());

        ErrataCacheManager.insertNeededErrataCache(
                server1.getId(), yumErrata1.getId(), yumPackage1.getId());
        ErrataCacheManager.insertNeededErrataCache(
                server1.getId(), yumErrata2.getId(), yumPackage2.getId());
        ErrataCacheManager.insertNeededErrataCache(
                server2.getId(), yumErrata1.getId(), yumPackage1.getId());
        HibernateFactory.getSession().flush();

        List<Long> errataIds = new ArrayList<Long>();
        errataIds.add(errata1.getId());
        errataIds.add(errata2.getId());
        errataIds.add(errata3.getId());
        errataIds.add(yumErrata1.getId());
        errataIds.add(yumErrata2.getId());

        List<Long> serverIds = new ArrayList<Long>();
        serverIds.add(server1.getId());
        serverIds.add(server2.getId());

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ErrataManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
        } });

        ErrataManager.applyErrata(user, errataIds, new Date(), serverIds);

        // we want to check that no matter how many actions were scheduled for
        // server1, all the erratas included in those scheduled actions for
        // server1 do not contain the erratas for server2

        List<Action> actionsServer1 = ActionFactory.listActionsForServer(user, server1);
        Set<Long> server1ScheduledErrata = new HashSet<Long>();
        for (Action a : actionsServer1) {
            ErrataAction errataAction = errataActionFromAction(a);
            for (Errata e : errataAction.getErrata()) {
                server1ScheduledErrata.add(e.getId());
            }
        }

        List<Action> actionsServer2 = ActionFactory.listActionsForServer(user, server2);
        Set<Long> server2ScheduledErrata = new HashSet<Long>();
        for (Action a : actionsServer2) {
            ErrataAction errataAction = errataActionFromAction(a);
            for (Errata e : errataAction.getErrata()) {
                server2ScheduledErrata.add(e.getId());
            }
        }

        assertEquals("Server 1 Scheduled has 2 scheduled actions",
                2, actionsServer1.size());
        assertEquals("Server 1 Scheduled Erratas has 3 erratas (errata1 and both" +
                " yumErratas)", 3, server1ScheduledErrata.size());
        assertFalse("Server 1 Scheduled Erratas do not include irrelevant errata",
                server1ScheduledErrata.contains(errata3.getId()));
        assertTrue("Server 1 Scheduled Erratas contain relevant erratas",
                server1ScheduledErrata.contains(errata1.getId()));
        assertTrue("Server 1 Scheduled Erratas contain both yum erratas",
                server1ScheduledErrata.contains(yumErrata1.getId()));
        assertTrue("Server 1 Scheduled Erratas contain both yum erratas",
                server1ScheduledErrata.contains(yumErrata2.getId()));


        assertEquals("Server 2 Scheduled has 2 scheduled actions",
                2, actionsServer2.size());
        assertEquals("Server 2 Scheduled Erratas has 2 erratas (errata2 and yumErrata1)",
                2, server2ScheduledErrata.size());
        assertFalse("Server 2 Scheduled Erratas do not include irrelevant errata",
                server2ScheduledErrata.contains(errata3.getId()));
        assertTrue("Server 2 Scheduled Erratas contain relevant erratas",
                server2ScheduledErrata.contains(errata2.getId()));
        assertTrue("Server 2 Scheduled Erratas contain one yum errata",
                server2ScheduledErrata.contains(yumErrata1.getId()));
    }

    /**
     * Tests applyErrata() with an empty system list
     *
     * @throws Exception if something goes wrong
     */
    public void testApplyErrataNoSystems() throws Exception {

        Errata errata1 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        TestUtils.saveAndFlush(errata1);

        List<Long> errataIds = new ArrayList<Long>();
        errataIds.add(errata1.getId());

        List<Long> serverIds = new ArrayList<Long>();

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ErrataManager.setTaskomaticApi(taskomaticMock);

        List<Long> result =
                ErrataManager.applyErrata(user, errataIds, new Date(), serverIds);

        assertEquals("No Actions have been produced", 0, result.size());
    }

    /**
     * Tests applyErrata() with an empty errata list
     *
     * @throws Exception if something goes wrong
     */
    public void testApplyErrataNoErrata() throws Exception {
        List<Long> errataIds = new ArrayList<Long>();

        List<Long> serverIds = new ArrayList<Long>();
        Server server1 = MinionServerFactoryTest.createTestMinionServer(user);
        serverIds.add(server1.getId());

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ErrataManager.setTaskomaticApi(taskomaticMock);

        List<Long> result =
                ErrataManager.applyErrata(user, errataIds, new Date(), serverIds);

        assertEquals("No Actions have been produced", 0, result.size());
    }

    /**
     * Tests applyErrata() with an empty errata and system list
     *
     * @throws Exception if something goes wrong
     */
    public void testApplyErrataNoErrataNoSystems() throws Exception {
        List<Long> errataIds = new ArrayList<Long>();
        List<Long> serverIds = new ArrayList<Long>();

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ErrataManager.setTaskomaticApi(taskomaticMock);

        List<Long> result =
                ErrataManager.applyErrata(user, errataIds, new Date(), serverIds);

        assertEquals("No Actions have been produced", 0, result.size());
    }

    /**
     * Tests applyErrata() with one inapplicable errata
     *
     * @throws Exception if something goes wrong
     */
    public void testApplyErrataInapplicable() throws Exception {
        Errata errata1 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        TestUtils.saveAndFlush(errata1);

        List<Long> errataIds = new ArrayList<Long>();
        errataIds.add(errata1.getId());

        List<Long> serverIds = new ArrayList<Long>();
        Server server1 = MinionServerFactoryTest.createTestMinionServer(user);
        serverIds.add(server1.getId());

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ErrataManager.setTaskomaticApi(taskomaticMock);

        List<Long> result =
                ErrataManager.applyErrata(user, errataIds, new Date(), serverIds);

        assertEquals("No Actions have been produced", 0, result.size());
    }

    /**
     * Tests applyErrata() with 2 identical yum systems and 2 errata applicable to both.
     * This should result in 4 Actions being created
     *
     * @throws Exception if something goes wrong
     */
    @SuppressWarnings("unchecked")
    public void testApplyErrataMultipleErrataYum() throws Exception {
        Errata errata1 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        TestUtils.saveAndFlush(errata1);
        Errata errata2 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        TestUtils.saveAndFlush(errata2);

        Channel channel = ChannelFactoryTest.createTestChannel(user);

        Set<Channel> serverChannels = new HashSet<Channel>();
        serverChannels.add(channel);
        Server server1 = createTestServer(user, serverChannels);
        Server server2 = createTestServer(user, serverChannels);

        // both servers have two errata for two packages available
        Package package1 = createTestPackage(user, channel, "noarch");
        Package package2 = createTestPackage(user, channel, "noarch");
        createTestInstalledPackage(package1, server1);
        createTestInstalledPackage(package1, server2);
        createTestInstalledPackage(package2, server1);
        createTestInstalledPackage(package2, server2);
        createLaterTestPackage(user, errata1, channel, package1);
        createLaterTestPackage(user, errata2, channel, package2);

        ErrataCacheManager.insertNeededErrataCache(
                server1.getId(), errata1.getId(), package1.getId());
        ErrataCacheManager.insertNeededErrataCache(
                server1.getId(), errata2.getId(), package2.getId());
        ErrataCacheManager.insertNeededErrataCache(
                server2.getId(), errata1.getId(), package1.getId());
        ErrataCacheManager.insertNeededErrataCache(
                server2.getId(), errata2.getId(), package2.getId());
        HibernateFactory.getSession().flush();

        List<Long> errataIds = new ArrayList<Long>();
        errataIds.add(errata1.getId());
        errataIds.add(errata2.getId());

        List<Long> serverIds = new ArrayList<Long>();
        serverIds.add(server1.getId());
        serverIds.add(server2.getId());

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ErrataManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
        } });

        ErrataManager.applyErrata(user, errataIds, new Date(), serverIds);

        // 4 Actions should be created

        List<Action> actionsServer1 = ActionFactory.listActionsForServer(user, server1);
        assertEquals("2 actions have been scheduled for server 1", 2,
                actionsServer1.size());
        Set<Long> server1ScheduledErrata = new HashSet<Long>();
        for (Action a : actionsServer1) {
            ErrataAction errataAction = errataActionFromAction(a);
            for (Errata e : errataAction.getErrata()) {
                server1ScheduledErrata.add(e.getId());
            }
        }

        List<Action> actionsServer2 = ActionFactory.listActionsForServer(user, server2);
        assertEquals("2 actions have been scheduled for server 2", 2,
                actionsServer2.size());
        Set<Long> server2ScheduledErrata = new HashSet<Long>();
        for (Action a : actionsServer2) {
            ErrataAction errataAction = errataActionFromAction(a);
            for (Errata e : errataAction.getErrata()) {
                server2ScheduledErrata.add(e.getId());
            }
        }

        assertEquals("Server 1 Scheduled Erratas has 2 erratas (errata1 and errata2)",
                2, server1ScheduledErrata.size());
        assertTrue("Server 1 Scheduled Erratas contain relevant erratas",
                server1ScheduledErrata.contains(errata1.getId()));
        assertTrue("Server 1 Scheduled Erratas contain relevant erratas",
                server1ScheduledErrata.contains(errata2.getId()));

        assertEquals("Server 2 Scheduled Erratas has 2 erratas (errata1 and errata2)",
                2, server2ScheduledErrata.size());
        assertTrue("Server 2 Scheduled Erratas contain relevant erratas",
                server2ScheduledErrata.contains(errata1.getId()));
        assertTrue("Server 2 Scheduled Erratas contain relevant erratas",
                server2ScheduledErrata.contains(errata2.getId()));
    }

    /**
     * Tests applyErrata() with 2 identical minions and 2 errata applicable to both.
     * This should result in only one Action being created
     *
     * @throws Exception if something goes wrong
     */
    @SuppressWarnings("unchecked")
    public void testApplyErrataMultipleErrataMinions() throws Exception {
        Errata errata1 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        TestUtils.saveAndFlush(errata1);
        Errata errata2 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        TestUtils.saveAndFlush(errata2);

        Channel channel = ChannelFactoryTest.createTestChannel(user);

        Set<Channel> serverChannels = new HashSet<Channel>();
        serverChannels.add(channel);
        Server server1 = MinionServerFactoryTest.createTestMinionServer(user);
        server1.addChannel(channel);
        Server server2 = MinionServerFactoryTest.createTestMinionServer(user);
        server2.addChannel(channel);

        // both servers have two errata for two packages available
        Package package1 = createTestPackage(user, channel, "noarch");
        Package package2 = createTestPackage(user, channel, "noarch");
        createTestInstalledPackage(package1, server1);
        createTestInstalledPackage(package1, server2);
        createTestInstalledPackage(package2, server1);
        createTestInstalledPackage(package2, server2);
        createLaterTestPackage(user, errata1, channel, package1);
        createLaterTestPackage(user, errata2, channel, package2);

        ErrataCacheManager.insertNeededErrataCache(
                server1.getId(), errata1.getId(), package1.getId());
        ErrataCacheManager.insertNeededErrataCache(
                server1.getId(), errata2.getId(), package2.getId());
        ErrataCacheManager.insertNeededErrataCache(
                server2.getId(), errata1.getId(), package1.getId());
        ErrataCacheManager.insertNeededErrataCache(
                server2.getId(), errata2.getId(), package2.getId());
        HibernateFactory.getSession().flush();

        List<Long> errataIds = new ArrayList<Long>();
        errataIds.add(errata1.getId());
        errataIds.add(errata2.getId());

        List<Long> serverIds = new ArrayList<Long>();
        serverIds.add(server1.getId());
        serverIds.add(server2.getId());

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ErrataManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            allowing(taskomaticMock).scheduleMinionActionExecutions(with(any(List.class)),with(any(Boolean.class)));
        } });

        ErrataManager.applyErrata(user, errataIds, new Date(), serverIds);

        // only one Action should be created

        List<Action> actionsServer1 = ActionFactory.listActionsForServer(user, server1);
        assertEquals("1 action has been scheduled for server 1", 1, actionsServer1.size());
        Set<Long> server1ScheduledErrata = new HashSet<Long>();
        for (Action a : actionsServer1) {
            ErrataAction errataAction = errataActionFromAction(a);
            for (Errata e : errataAction.getErrata()) {
                server1ScheduledErrata.add(e.getId());
            }
        }

        List<Action> actionsServer2 = ActionFactory.listActionsForServer(user, server2);
        assertEquals("1 action has been scheduled for server 2", 1, actionsServer2.size());
        assertEquals(
                "action created for server 1 is actually the same as the one for server 2",
                actionsServer1.get(0), actionsServer2.get(0));
        assertEquals("action actually has 2 servers", 2,
                actionsServer1.get(0).getServerActions().size());
        Set<Long> server2ScheduledErrata = new HashSet<Long>();
        for (Action a : actionsServer2) {
            ErrataAction errataAction = errataActionFromAction(a);
            for (Errata e : errataAction.getErrata()) {
                server2ScheduledErrata.add(e.getId());
            }
        }

        assertEquals("Server 1 Scheduled Erratas has 2 erratas (errata1 and errata2)",
                2, server1ScheduledErrata.size());
        assertTrue("Server 1 Scheduled Erratas contain relevant erratas",
                server1ScheduledErrata.contains(errata1.getId()));
        assertTrue("Server 1 Scheduled Erratas contain relevant erratas",
                server1ScheduledErrata.contains(errata2.getId()));

        assertEquals("Server 2 Scheduled Erratas has 2 erratas (errata1 and errata2)",
                2, server2ScheduledErrata.size());
        assertTrue("Server 2 Scheduled Erratas contain relevant erratas",
                server2ScheduledErrata.contains(errata1.getId()));
        assertTrue("Server 2 Scheduled Erratas contain relevant erratas",
                server2ScheduledErrata.contains(errata2.getId()));
    }

    /**
     * Tests applyErrata() with 2 identical minions and 2 errata applicable to both.
     * This should result in only one Action being created, even if one of the erratas
     * affects the management stack.
     *
     * @throws Exception if something goes wrong
     */
    @SuppressWarnings("unchecked")
    public void testApplyErrataMultipleManagementStackErrataMinions() throws Exception {
        Errata errata1 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        TestUtils.saveAndFlush(errata1);
        Errata errata2 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        errata2.addKeyword("restart_suggested");
        TestUtils.saveAndFlush(errata2);

        Channel channel = ChannelFactoryTest.createTestChannel(user);

        Set<Channel> serverChannels = new HashSet<Channel>();
        serverChannels.add(channel);
        Server server1 = MinionServerFactoryTest.createTestMinionServer(user);
        server1.addChannel(channel);
        Server server2 = MinionServerFactoryTest.createTestMinionServer(user);
        server2.addChannel(channel);

        // both servers have two errata for two packages available
        Package package1 = createTestPackage(user, channel, "noarch");
        Package package2 = createTestPackage(user, channel, "noarch");
        createTestInstalledPackage(package1, server1);
        createTestInstalledPackage(package1, server2);
        createTestInstalledPackage(package2, server1);
        createTestInstalledPackage(package2, server2);
        createLaterTestPackage(user, errata1, channel, package1);
        createLaterTestPackage(user, errata2, channel, package2);

        ErrataCacheManager.insertNeededErrataCache(
                server1.getId(), errata1.getId(), package1.getId());
        ErrataCacheManager.insertNeededErrataCache(
                server1.getId(), errata2.getId(), package2.getId());
        ErrataCacheManager.insertNeededErrataCache(
                server2.getId(), errata1.getId(), package1.getId());
        ErrataCacheManager.insertNeededErrataCache(
                server2.getId(), errata2.getId(), package2.getId());
        HibernateFactory.getSession().flush();

        List<Long> errataIds = new ArrayList<Long>();
        errataIds.add(errata1.getId());
        errataIds.add(errata2.getId());

        List<Long> serverIds = new ArrayList<Long>();
        serverIds.add(server1.getId());
        serverIds.add(server2.getId());

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ErrataManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            allowing(taskomaticMock).scheduleMinionActionExecutions(with(any(List.class)),with(any(Boolean.class)));
        } });

        ErrataManager.applyErrata(user, errataIds, new Date(), serverIds);

        // only one Action should be created

        List<Action> actionsServer1 = ActionFactory.listActionsForServer(user, server1);
        assertEquals("1 action has been scheduled for server 1", 1, actionsServer1.size());
        Set<Long> server1ScheduledErrata = new HashSet<Long>();
        for (Action a : actionsServer1) {
            ErrataAction errataAction = errataActionFromAction(a);
            for (Errata e : errataAction.getErrata()) {
                server1ScheduledErrata.add(e.getId());
            }
        }

        List<Action> actionsServer2 = ActionFactory.listActionsForServer(user, server2);
        assertEquals("1 action has been scheduled for server 2", 1, actionsServer2.size());
        assertEquals(
                "action created for server 1 is actually the same as the one for server 2",
                actionsServer1.get(0), actionsServer2.get(0));
        assertEquals("action actually has 2 servers", 2,
                actionsServer1.get(0).getServerActions().size());
        Set<Long> server2ScheduledErrata = new HashSet<Long>();
        for (Action a : actionsServer2) {
            ErrataAction errataAction = errataActionFromAction(a);
            for (Errata e : errataAction.getErrata()) {
                server2ScheduledErrata.add(e.getId());
            }
        }

        assertEquals("Server 1 Scheduled Erratas has 2 erratas (errata1 and errata2)",
                2, server1ScheduledErrata.size());
        assertTrue("Server 1 Scheduled Erratas contain relevant erratas",
                server1ScheduledErrata.contains(errata1.getId()));
        assertTrue("Server 1 Scheduled Erratas contain relevant erratas",
                server1ScheduledErrata.contains(errata2.getId()));

        assertEquals("Server 2 Scheduled Erratas has 2 erratas (errata1 and errata2)",
                2, server2ScheduledErrata.size());
        assertTrue("Server 2 Scheduled Erratas contain relevant erratas",
                server2ScheduledErrata.contains(errata1.getId()));
        assertTrue("Server 2 Scheduled Erratas contain relevant erratas",
                server2ScheduledErrata.contains(errata2.getId()));
    }
    /**
     * Tests applyErrata() with 2 identical clients and 2 errata applicable to both,
     * to an Action Chain.
     * 2 Actions should be created.
     *
     * @throws Exception if something goes wrong
     */
    @SuppressWarnings("unchecked")
    public void testApplyErrataMultipleErrataActionChain() throws Exception {
        Errata errata1 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        TestUtils.saveAndFlush(errata1);
        Errata errata2 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        TestUtils.saveAndFlush(errata2);

        Channel channel = ChannelFactoryTest.createTestChannel(user);

        Set<Channel> serverChannels = new HashSet<Channel>();
        serverChannels.add(channel);
        Server server1 = createTestServer(user, serverChannels);
        Server server2 = createTestServer(user, serverChannels);

        // add zypper as installed package
        Package zypperPkg = PackageTest.createTestPackage(user.getOrg());
        PackageName name = PackageManager.lookupPackageName("zypper");
        if (name == null) {
            name = zypperPkg.getPackageName();
            name.setName("zypper");
            TestUtils.saveAndFlush(name);
        }
        else {
            // Handle the case that the package name exists in the DB
            zypperPkg.setPackageName(name);
            TestUtils.saveAndFlush(zypperPkg);
        }
        createTestInstalledPackage(zypperPkg, server1);
        createTestInstalledPackage(zypperPkg, server2);

        // both servers have two errata for two packages available
        Package package1 = createTestPackage(user, channel, "noarch");
        Package package2 = createTestPackage(user, channel, "noarch");
        createTestInstalledPackage(package1, server1);
        createTestInstalledPackage(package1, server2);
        createTestInstalledPackage(package2, server1);
        createTestInstalledPackage(package2, server2);
        createLaterTestPackage(user, errata1, channel, package1);
        createLaterTestPackage(user, errata2, channel, package2);

        ErrataCacheManager.insertNeededErrataCache(
                server1.getId(), errata1.getId(), package1.getId());
        ErrataCacheManager.insertNeededErrataCache(
                server1.getId(), errata2.getId(), package2.getId());
        ErrataCacheManager.insertNeededErrataCache(
                server2.getId(), errata1.getId(), package1.getId());
        ErrataCacheManager.insertNeededErrataCache(
                server2.getId(), errata2.getId(), package2.getId());
        HibernateFactory.getSession().flush();

        List<Long> errataIds = new ArrayList<Long>();
        errataIds.add(errata1.getId());
        errataIds.add(errata2.getId());

        List<Long> serverIds = new ArrayList<Long>();
        serverIds.add(server1.getId());
        serverIds.add(server2.getId());

        String label = TestUtils.randomString();
        ActionChain actionChain = ActionChainFactory.createActionChain(label, user);

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ErrataManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
        } });

        ErrataManager.applyErrata(user, errataIds, new Date(), actionChain, serverIds);

        // only one Action should be created

        List<Action> actionsServer1 = ActionFactory.listActionsForServer(user, server1);
        assertEquals("no actions have been scheduled for server 1", 0, actionsServer1.size());
        assertTrue("server 1 has been added to the chain", actionChain.getEntries().stream().anyMatch(e -> e.getServer().equals(server1)));
        Set<Long> server1ScheduledErrata = actionChain.getEntries().stream()
            .filter(e -> e.getServer().equals(server1))
            .map(e -> e.getAction())
            .map(a -> errataActionFromAction(a))
            .flatMap(a -> a.getErrata().stream())
            .map(e -> e.getId())
            .collect(Collectors.toSet());

        List<Action> actionsServer2 = ActionFactory.listActionsForServer(user, server2);
        assertEquals("no actions have been scheduled for server 2", 0, actionsServer2.size());
        assertTrue("server 2 has been added to the chain", actionChain.getEntries().stream().anyMatch(e -> e.getServer().equals(server2)));
        assertEquals("action chain actually has 2 entries", 2, actionChain.getEntries().size());
        assertEquals("action chain points to 2 actions only", 2, actionChain.getEntries().stream().map(e -> e.getActionId()).distinct().count());
        assertEquals("action chain points to 2 servers", 2, actionChain.getEntries().stream().map(e -> e.getServerId()).distinct().count());
        Set<Long> server2ScheduledErrata = actionChain.getEntries().stream()
            .filter(e -> e.getServer().equals(server2))
            .map(e -> e.getAction())
            .map(a -> errataActionFromAction(a))
            .flatMap(a -> a.getErrata().stream())
            .map(e -> e.getId())
            .collect(Collectors.toSet());

        assertEquals("Server 1 Scheduled Erratas has 2 erratas (errata1 and errata2)",
                2, server1ScheduledErrata.size());
        assertTrue("Server 1 Scheduled Erratas contain relevant erratas",
                server1ScheduledErrata.contains(errata1.getId()));
        assertTrue("Server 1 Scheduled Erratas contain relevant erratas",
                server1ScheduledErrata.contains(errata2.getId()));

        assertEquals("Server 2 Scheduled Erratas has 2 erratas (errata1 and errata2)",
                2, server2ScheduledErrata.size());
        assertTrue("Server 2 Scheduled Erratas contain relevant erratas",
                server2ScheduledErrata.contains(errata1.getId()));
        assertTrue("Server 2 Scheduled Erratas contain relevant erratas",
                server2ScheduledErrata.contains(errata2.getId()));
    }

    /**
     * Tests applyErrata() with 2 identical yum clients and 2 errata applicable to both,
     * to an Action Chain.
     * 4 Actions should be created.
     *
     * @throws Exception if something goes wrong
     */
    @SuppressWarnings("unchecked")
    public void testApplyErrataMultipleErrataActionChainYum() throws Exception {
        Errata errata1 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        TestUtils.saveAndFlush(errata1);
        Errata errata2 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        TestUtils.saveAndFlush(errata2);

        Channel channel = ChannelFactoryTest.createTestChannel(user);

        Set<Channel> serverChannels = new HashSet<Channel>();
        serverChannels.add(channel);
        Server server1 = createTestServer(user, serverChannels);
        Server server2 = createTestServer(user, serverChannels);

        // both servers have two errata for two packages available
        Package package1 = createTestPackage(user, channel, "noarch");
        Package package2 = createTestPackage(user, channel, "noarch");
        createTestInstalledPackage(package1, server1);
        createTestInstalledPackage(package1, server2);
        createTestInstalledPackage(package2, server1);
        createTestInstalledPackage(package2, server2);
        createLaterTestPackage(user, errata1, channel, package1);
        createLaterTestPackage(user, errata2, channel, package2);

        ErrataCacheManager.insertNeededErrataCache(
                server1.getId(), errata1.getId(), package1.getId());
        ErrataCacheManager.insertNeededErrataCache(
                server1.getId(), errata2.getId(), package2.getId());
        ErrataCacheManager.insertNeededErrataCache(
                server2.getId(), errata1.getId(), package1.getId());
        ErrataCacheManager.insertNeededErrataCache(
                server2.getId(), errata2.getId(), package2.getId());
        HibernateFactory.getSession().flush();

        List<Long> errataIds = new ArrayList<Long>();
        errataIds.add(errata1.getId());
        errataIds.add(errata2.getId());

        List<Long> serverIds = new ArrayList<Long>();
        serverIds.add(server1.getId());
        serverIds.add(server2.getId());

        String label = TestUtils.randomString();
        ActionChain actionChain = ActionChainFactory.createActionChain(label, user);

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ErrataManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
        } });

        ErrataManager.applyErrata(user, errataIds, new Date(), actionChain, serverIds);

        // only one Action should be created

        List<Action> actionsServer1 = ActionFactory.listActionsForServer(user, server1);
        assertEquals("no actions have been scheduled for server 1", 0, actionsServer1.size());
        assertTrue("server 1 has been added to the chain", actionChain.getEntries().stream().anyMatch(e -> e.getServer().equals(server1)));
        Set<Long> server1ScheduledErrata = actionChain.getEntries().stream()
            .filter(e -> e.getServer().equals(server1))
            .map(e -> e.getAction())
            .map(a -> errataActionFromAction(a))
            .flatMap(a -> a.getErrata().stream())
            .map(e -> e.getId())
            .collect(Collectors.toSet());

        List<Action> actionsServer2 = ActionFactory.listActionsForServer(user, server2);
        assertEquals("no actions have been scheduled for server 2", 0, actionsServer2.size());
        assertTrue("server 2 has been added to the chain", actionChain.getEntries().stream().anyMatch(e -> e.getServer().equals(server2)));
        assertEquals("action chain actually has 4 entries", 4, actionChain.getEntries().size());
        assertEquals("action chain points to 4 actions", 4, actionChain.getEntries().stream().map(e -> e.getActionId()).distinct().count());
        assertEquals("action chain points to 2 servers", 2, actionChain.getEntries().stream().map(e -> e.getServerId()).distinct().count());
        Set<Long> server2ScheduledErrata = actionChain.getEntries().stream()
            .filter(e -> e.getServer().equals(server2))
            .map(e -> e.getAction())
            .map(a -> errataActionFromAction(a))
            .flatMap(a -> a.getErrata().stream())
            .map(e -> e.getId())
            .collect(Collectors.toSet());

        assertEquals("Server 1 Scheduled Erratas has 2 erratas (errata1 and errata2)",
                2, server1ScheduledErrata.size());
        assertTrue("Server 1 Scheduled Erratas contain relevant erratas",
                server1ScheduledErrata.contains(errata1.getId()));
        assertTrue("Server 1 Scheduled Erratas contain relevant erratas",
                server1ScheduledErrata.contains(errata2.getId()));

        assertEquals("Server 2 Scheduled Erratas has 2 erratas (errata1 and errata2)",
                2, server2ScheduledErrata.size());
        assertTrue("Server 2 Scheduled Erratas contain relevant erratas",
                server2ScheduledErrata.contains(errata1.getId()));
        assertTrue("Server 2 Scheduled Erratas contain relevant erratas",
                server2ScheduledErrata.contains(errata2.getId()));
    }

    /**
     * Get an ErrataAction from an Action.
     * @param action the action
     * @return the errata action
     */
    private ErrataAction errataActionFromAction(Action action) {
        return (ErrataAction) HibernateFactory.getSession().createCriteria(ErrataAction.class)
                .add(Restrictions.idEq(action.getId())).uniqueResult();
    }

    /**
     * Test updateStackUpdateNeeded
     *
     * @throws Exception the exception
     */
    public void testUpdateStackUpdateNeeded() throws Exception {

        Errata errata1 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        TestUtils.saveAndFlush(errata1);
        Errata errata2 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        TestUtils.saveAndFlush(errata2);
        Errata errata3 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        TestUtils.saveAndFlush(errata3);

        Channel channel1 = ChannelFactoryTest.createTestChannel(user);

        Set<Channel> serverChannels = new HashSet<Channel>();
        serverChannels.add(channel1);
        Server server = createTestServer(user, serverChannels);

        // server 1 has an errata for package1 available
        com.redhat.rhn.domain.rhnpackage.Package package1 =
                createTestPackage(user, channel1, "noarch");
        createTestInstalledPackage(package1, server);
        createLaterTestPackage(user, errata1, channel1, package1);

        // server 2 has an errata for package2 available
        Package package2 = createTestPackage(user, channel1, "noarch");
        createTestInstalledPackage(package2, server);
        createLaterTestPackage(user, errata2, channel1, package2);

        // errata in common for both servers
        Package package3 = createTestPackage(user, channel1, "noarch");
        createTestInstalledPackage(package3, server);
        createLaterTestPackage(user, errata3, channel1, package3);

        ErrataCacheManager.insertNeededErrataCache(
                server.getId(), errata1.getId(), package1.getId());
        ErrataCacheManager.insertNeededErrataCache(
                server.getId(), errata2.getId(), package2.getId());
        ErrataCacheManager.insertNeededErrataCache(
                server.getId(), errata3.getId(), package3.getId());
        HibernateFactory.getSession().flush();

        assertFalse(ErrataManager.updateStackUpdateNeeded(user, server));

        Set<Keyword> kw = new HashSet<Keyword>();
        Keyword k = new PublishedKeyword();
        k.setKeyword("restart_suggested");
        k.setErrata(errata3);
        kw.add(k);
        errata3.setKeywords(kw);
        TestUtils.saveAndFlush(errata3);

        assertTrue(ErrataManager.updateStackUpdateNeeded(user, server));
    }

    /**
     * Tests truncating errata - simple case (overlap of errata in channels)
     *
     * @throws Exception if anything goes wrong
     */
    public void testTruncateErrataSimple() throws Exception {
        user.addPermanentRole(ORG_ADMIN);
        Errata errata1 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        TestUtils.saveAndFlush(errata1);
        Errata errata2 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        TestUtils.saveAndFlush(errata2);

        Channel src = ChannelFactoryTest.createTestChannel(user);
        Channel tgt = ChannelFactoryTest.createTestChannel(user);

        ErrataFactory.publishToChannel(Arrays.asList(errata1), src, user, false);
        ErrataFactory.publishToChannel(Arrays.asList(errata1, errata2), tgt, user, false);

        ErrataManager.truncateErrata(src.getErratas(), tgt, user);

        assertEquals(src.getErratas(), tgt.getErratas());
    }

    /**
     * Tests truncating errata - overlap of erratum and its clone in channels
     *
     * @throws Exception if anything goes wrong
     */
    public void testTruncateErrataCloned() throws Exception {
        user.addPermanentRole(ORG_ADMIN);
        Errata errata1 = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        TestUtils.saveAndFlush(errata1);
        Package pack = PackageTest.createTestPackage(user.getOrg());
        Errata errata1Clone = ErrataTestUtils.createTestClonedErrata(user, errata1, new HashSet<>(), pack);
        TestUtils.saveAndFlush(errata1Clone);
        Errata errataInTgt = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        TestUtils.saveAndFlush(errataInTgt);

        Channel src = ChannelFactoryTest.createTestChannel(user);
        Channel tgt = ChannelFactoryTest.createTestChannel(user);

        ErrataFactory.publishToChannel(Arrays.asList(errata1), src, user, false);
        ErrataFactory.publishToChannel(Arrays.asList(errata1Clone, errataInTgt), tgt, user, false);

        ErrataManager.truncateErrata(src.getErratas(), tgt, user);

        assertEquals(1, tgt.getErratas().size());
        // the clone will "survive" in the tgt channel as it has original in the src
        assertEquals(errata1Clone, tgt.getErratas().iterator().next());
    }

    /**
     * Tests that {@link Package}s are removed from {@link Channel} when {@link Errata} is removed from it.
     *
     * @throws Exception if anything goes wrong
     */
    public void testPackagesOnTruncateErrata() throws Exception {
        user.addPermanentRole(ORG_ADMIN);
        Errata errata = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        TestUtils.saveAndFlush(errata);
        Package errataPackage = errata.getPackages().iterator().next();
        // we assume version 1.0.0 for the test
        assertEquals("1.0.0", errataPackage.getPackageEvr().getVersion());

        Channel chan = ChannelFactoryTest.createTestChannel(user);
        Package olderPack = copyPackage(errataPackage, user, chan, "0.9.9");

        ErrataFactory.publishToChannel(Arrays.asList(errata), chan, user, false);

        ErrataManager.truncateErrata(Collections.emptySet(), chan, user);

        assertTrue(chan.getErratas().isEmpty());
        assertTrue(chan.getPackages().contains(olderPack));
        assertFalse(chan.getPackages().contains(errataPackage));
    }

    private static Package copyPackage(Package fromPkg, User user, Channel channel, String version) throws Exception {
        Package olderPkg = createTestPackage(user, channel, fromPkg.getPackageArch().getLabel());
        PackageEvr packageEvr = fromPkg.getPackageEvr();
        olderPkg.setPackageEvr(PackageEvrFactoryTest.createTestPackageEvr(
                packageEvr.getEpoch(),
                version,
                packageEvr.getRelease()
        ));
        olderPkg.setPackageName(fromPkg.getPackageName());
        return olderPkg;
    }
}
