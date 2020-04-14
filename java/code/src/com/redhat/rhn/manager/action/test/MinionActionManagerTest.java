/**
 * Copyright (c) 2017 SUSE LLC
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
package com.redhat.rhn.manager.action.test;

import static com.redhat.rhn.testing.ErrataTestUtils.createTestInstalledPackage;
import static com.redhat.rhn.testing.ErrataTestUtils.createTestPackage;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.dto.ScheduledAction;
import com.redhat.rhn.frontend.xmlrpc.system.SystemHandler;
import com.redhat.rhn.frontend.xmlrpc.system.XmlRpcSystemHelper;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.action.MinionActionManager;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.errata.cache.ErrataCacheManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerTestUtils;

import com.suse.manager.webui.controllers.utils.RegularMinionBootstrapper;
import com.suse.manager.webui.controllers.utils.SSHMinionBootstrapper;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.impl.SaltService;
import org.hamcrest.Matcher;
import org.hamcrest.collection.IsMapContaining;
import org.hamcrest.core.AllOf;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * JUnit test case for the MinionActionManager
 *  class.
 */
public class MinionActionManagerTest extends JMockBaseTestCaseWithUser {

    private final String SALT_CONTENT_STAGING_WINDOW = "salt_content_staging_window";
    private final String SALT_CONTENT_STAGING_ADVANCE = "salt_content_staging_advance";

    private SystemQuery systemQuery = new SaltService();
    private RegularMinionBootstrapper regularMinionBootstrapper = RegularMinionBootstrapper.getInstance(systemQuery);
    private SSHMinionBootstrapper sshMinionBootstrapper = SSHMinionBootstrapper.getInstance(systemQuery);
    private XmlRpcSystemHelper xmlRpcSystemHelper = new XmlRpcSystemHelper(
            regularMinionBootstrapper,
            sshMinionBootstrapper
    );

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ClassImposteriser.INSTANCE);
    }

    /**
     * Test package install with staging when inside the staging window
     * @throws Exception when Taskomatic service is down
     */
    public void testPackageInstallWithStagingInsideWindow() throws Exception {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        UserFactory.save(user);

        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        minion1.setOrg(user.getOrg());

        Package pkg = PackageTest.createTestPackage(user.getOrg());
        List packageIds = new LinkedList();
        packageIds.add(pkg.getId().intValue());

        user.getOrg().getOrgConfig().setStagingContentEnabled(true);
        Config.get().setString(SALT_CONTENT_STAGING_WINDOW, "48");
        Config.get().setString(SALT_CONTENT_STAGING_ADVANCE, "48");

        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime scheduledActionTime = now.plusHours(24);

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);
        MinionActionManager.setTaskomaticApi(taskomaticMock);

        SystemHandler handler = new SystemHandler(taskomaticMock, xmlRpcSystemHelper);
        context().checking(new Expectations() { {
            Matcher<Map<Long, ZonedDateTime>> minionMatcher =
                    AllOf.allOf(IsMapContaining.hasKey(minion1.getId()));
            Matcher<Map<Long, Map<Long, ZonedDateTime>>> actionsMatcher =
                    AllOf.allOf(IsMapContaining.hasEntry(any(Long.class), minionMatcher));
            exactly(1).of(taskomaticMock)
                    .scheduleActionExecution(with(any(Action.class)));
            exactly(1).of(taskomaticMock).scheduleStagingJobs(with(actionsMatcher));
        } });

        DataResult dr = ActionManager.recentlyScheduledActions(user, null, 30);
        int preScheduleSize = dr.size();

        handler.schedulePackageInstall(user, minion1.getId().intValue(), packageIds,
                Date.from(scheduledActionTime.toInstant()));

        dr = ActionManager.recentlyScheduledActions(user, null, 30);
        assertEquals(1, dr.size() - preScheduleSize);
        assertEquals("Package Install", ((ScheduledAction)dr.get(0)).getTypeName());
    }

    /**
     * Test package install with staging before entering the staging window
     * @throws Exception when Taskomatic service is down
     */
    public void testPackageInstallWithStagingBeforeWindow() throws Exception {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);

        UserFactory.save(user);
        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        minion1.setOrg(user.getOrg());

        Package pkg = PackageTest.createTestPackage(user.getOrg());
        List packageIds = new LinkedList();
        packageIds.add(pkg.getId().intValue());

        user.getOrg().getOrgConfig().setStagingContentEnabled(true);
        Config.get().setString(SALT_CONTENT_STAGING_WINDOW, "48");
        Config.get().setString(SALT_CONTENT_STAGING_ADVANCE, "48");

        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime scheduledActionTime = now.plusHours(76);

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);
        MinionActionManager.setTaskomaticApi(taskomaticMock);


        SystemHandler handler = new SystemHandler(taskomaticMock, xmlRpcSystemHelper);

        context().checking(new Expectations() { {
            Matcher<Map<Long, ZonedDateTime>> minionMatcher =
                    AllOf.allOf(IsMapContaining.hasKey(minion1.getId()));
            Matcher<Map<Long, Map<Long, ZonedDateTime>>> actionsMatcher =
                    AllOf.allOf(IsMapContaining.hasEntry(any(Long.class), minionMatcher));
            exactly(1).of(taskomaticMock)
                    .scheduleActionExecution(with(any(Action.class)));
            exactly(1).of(taskomaticMock).scheduleStagingJobs(with(actionsMatcher));
        } });

        DataResult dr = ActionManager.recentlyScheduledActions(user, null, 30);
        int preScheduleSize = dr.size();

        handler.schedulePackageInstall(user, minion1.getId().intValue(), packageIds,
                Date.from(scheduledActionTime.toInstant()));

        dr = ActionManager.recentlyScheduledActions(user, null, 30);
        assertEquals(1, dr.size() - preScheduleSize);
        assertEquals("Package Install", ((ScheduledAction)dr.get(0)).getTypeName());
    }

    /**
     * Test package install with staging when the staging window
     * is already passed (there will be no staging)
     * @throws Exception when Taskomatic service is down
     */
    public void testPackageInstallWithStagingAfterWindow() throws Exception {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);

        UserFactory.save(user);
        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        minion1.setOrg(user.getOrg());

        Package pkg = PackageTest.createTestPackage(user.getOrg());
        List packageIds = new LinkedList();
        packageIds.add(pkg.getId().intValue());

        user.getOrg().getOrgConfig().setStagingContentEnabled(true);
        Config.get().setString(SALT_CONTENT_STAGING_WINDOW, "1");
        Config.get().setString(SALT_CONTENT_STAGING_ADVANCE, "48");

        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime scheduledActionTime = now.plusHours(24);

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);
        MinionActionManager.setTaskomaticApi(taskomaticMock);

        SystemHandler handler = new SystemHandler(taskomaticMock, xmlRpcSystemHelper);

        context().checking(new Expectations() { {
            exactly(1).of(taskomaticMock)
                    .scheduleActionExecution(with(any(Action.class)));
            never(taskomaticMock).scheduleStagingJob(with(any(Long.class)),
                    with(minion1.getId()),
                    with(any(Date.class)));
        } });

        DataResult dr = ActionManager.recentlyScheduledActions(user, null, 30);
        int preScheduleSize = dr.size();

        handler.schedulePackageInstall(user, minion1.getId().intValue(), packageIds,
                Date.from(scheduledActionTime.toInstant()));

        dr = ActionManager.recentlyScheduledActions(user, null, 30);
        assertEquals(1, dr.size() - preScheduleSize);
        assertEquals("Package Install", ((ScheduledAction)dr.get(0)).getTypeName());
    }

    /**
     * Test package install with staging when the staging window duration
     * is after the action execution
     * @throws Exception when Taskomatic service is down
     */
    public void testPackageInstallWithStagingBeforeWindowExceedingDuration()
        throws Exception {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);

        UserFactory.save(user);
        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        minion1.setOrg(user.getOrg());

        Package pkg = PackageTest.createTestPackage(user.getOrg());
        List packageIds = new LinkedList();
        packageIds.add(pkg.getId().intValue());

        user.getOrg().getOrgConfig().setStagingContentEnabled(true);
        Config.get().setString(SALT_CONTENT_STAGING_WINDOW, "90");
        Config.get().setString(SALT_CONTENT_STAGING_ADVANCE, "48");

        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime scheduledActionTime = now.plusHours(24);

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);
        MinionActionManager.setTaskomaticApi(taskomaticMock);

        SystemHandler handler = new SystemHandler(taskomaticMock, xmlRpcSystemHelper);
        context().checking(new Expectations() {{
            Matcher<Map<Long, ZonedDateTime>> minionMatcher =
                    AllOf.allOf(IsMapContaining.hasKey(minion1.getId()));
            Matcher<Map<Long, Map<Long, ZonedDateTime>>> actionsMatcher =
                    AllOf.allOf(IsMapContaining.hasEntry(any(Long.class), minionMatcher));
            exactly(1).of(taskomaticMock)
                    .scheduleActionExecution(with(any(Action.class)));
            exactly(1).of(taskomaticMock).scheduleStagingJobs(with(actionsMatcher));
        }});

        DataResult dr = ActionManager.recentlyScheduledActions(user, null, 30);
        int preScheduleSize = dr.size();

        handler.schedulePackageInstall(user, minion1.getId().intValue(), packageIds,
                Date.from(scheduledActionTime.toInstant()));

        dr = ActionManager.recentlyScheduledActions(user, null, 30);
        assertEquals(1, dr.size() - preScheduleSize);
        assertEquals("Package Install", ((ScheduledAction)dr.get(0)).getTypeName());
    }

    /**
     * Test package install without staging (staging functionality
     * should not be called).
     * @throws Exception when Taskomatic service is down
     */
    public void testPackageInstallWithoutStaging() throws Exception {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);

        UserFactory.save(user);
        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        minion1.setOrg(user.getOrg());

        Package pkg = PackageTest.createTestPackage(user.getOrg());
        List packageIds = new LinkedList();
        packageIds.add(pkg.getId().intValue());

        user.getOrg().getOrgConfig().setStagingContentEnabled(false);
        Config.get().setString(SALT_CONTENT_STAGING_WINDOW, "48");
        Config.get().setString(SALT_CONTENT_STAGING_ADVANCE, "48");

        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime scheduledActionTime = now.plusHours(24);

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);
        MinionActionManager.setTaskomaticApi(taskomaticMock);

        SystemHandler handler = new SystemHandler(taskomaticMock, xmlRpcSystemHelper);

        context().checking(new Expectations() { {
            exactly(1).of(taskomaticMock)
                    .scheduleActionExecution(with(any(Action.class)));
            never(taskomaticMock).scheduleStagingJob(with(any(Long.class)),
                    with(minion1.getId()),
                    with(any(Date.class)));
        } });

        DataResult dr = ActionManager.recentlyScheduledActions(user, null, 30);
        int preScheduleSize = dr.size();

        handler.schedulePackageInstall(user, minion1.getId().intValue(), packageIds,
                Date.from(scheduledActionTime.toInstant()));

        dr = ActionManager.recentlyScheduledActions(user, null, 30);
        assertEquals(1, dr.size() - preScheduleSize);
        assertEquals("Package Install", ((ScheduledAction)dr.get(0)).getTypeName());
    }

    /**
     * Test Action chain package install with staging when inside the staging window
     * @throws Exception when Taskomatic service is down
     */
    public void testChainPackageInstallWithStagingInsideWindow() throws Exception {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);

        UserFactory.save(user);
        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        minion1.setOrg(user.getOrg());
        MinionServer minion2 = MinionServerFactoryTest.createTestMinionServer(user);
        minion2.setOrg(user.getOrg());

        Package pkg1 = PackageTest.createTestPackage(user.getOrg());
        List packageIds = new LinkedList();
        packageIds.add(pkg1.getId().intValue());

        Package pkg2 = PackageTest.createTestPackage(user.getOrg());
        packageIds.add(pkg2.getId().intValue());

        user.getOrg().getOrgConfig().setStagingContentEnabled(true);
        Config.get().setString(SALT_CONTENT_STAGING_WINDOW, "0.5");
        Config.get().setString(SALT_CONTENT_STAGING_ADVANCE, "1");

        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime scheduledActionTime = now.plusHours(1);

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionChainManager.setTaskomaticApi(taskomaticMock);
        MinionActionManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            Matcher<Map<Long, ZonedDateTime>> minionMatcher =
                    AllOf.allOf(IsMapContaining.hasKey(minion1.getId()), IsMapContaining.hasKey(minion2.getId()));
            Matcher<Map<Long, Map<Long, ZonedDateTime>>> actionsMatcher =
                    AllOf.allOf(IsMapContaining.hasEntry(any(Long.class), minionMatcher));
            exactly(1).of(taskomaticMock)
                    .scheduleActionExecution(with(any(Action.class)));
            exactly(1).of(taskomaticMock).scheduleStagingJobs(with(actionsMatcher));
       } });

        ActionChainManager.schedulePackageInstalls(user,
                Arrays.asList(minion1.getId(), minion2.getId()), null,
                Date.from(scheduledActionTime.toInstant()), null);
    }

    /**
     * Test patch/errata install with staging when inside the staging window
     * @throws Exception when Taskomatic service is down
     */
    public void testPatchInstallWithStagingInsideWindow() throws Exception {
        Channel channel1 = ChannelFactoryTest.createTestChannel(user);
        final String updateTag = "SLE-SERVER";
        channel1.setUpdateTag(updateTag);

        Server server1 = MinionServerFactoryTest.createTestMinionServer(user);
        server1.setOrg(user.getOrg());
        server1.addChannel(channel1);

        // server 1 has an errata for package1 available
        com.redhat.rhn.domain.rhnpackage.Package package1 =
                createTestPackage(user, channel1, "noarch");
        createTestInstalledPackage(package1, server1);

        Errata e1 = ErrataFactoryTest.createTestPublishedErrata(user.getId());
        channel1.addErrata(e1);
        e1.setAdvisoryName("SUSE-2016-1234");
        e1.getPackages().add(createTestPackage(user, channel1, "noarch"));

        ChannelFactory.save(channel1);

        ErrataCacheManager.insertNeededErrataCache(
                server1.getId(), e1.getId(), package1.getId());

        List<Long> errataIds = new ArrayList<Long>();
        errataIds.add(e1.getId());

        List<Long> serverIds = new ArrayList<Long>();
        serverIds.add(server1.getId());

        user.getOrg().getOrgConfig().setStagingContentEnabled(true);
        Config.get().setString(SALT_CONTENT_STAGING_WINDOW, "48");
        Config.get().setString(SALT_CONTENT_STAGING_ADVANCE, "48");

        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime scheduledActionTime = now.plusHours(24);

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ErrataManager.setTaskomaticApi(taskomaticMock);
        MinionActionManager.setTaskomaticApi(taskomaticMock);
        context().checking(new Expectations() { {
            Matcher<Map<Long, ZonedDateTime>> minionMatcher =
                    AllOf.allOf(IsMapContaining.hasKey(server1.getId()));
            Matcher<Map<Long, Map<Long, ZonedDateTime>>> actionsMatcher =
                    AllOf.allOf(IsMapContaining.hasEntry(any(Long.class), minionMatcher));
            exactly(1).of(taskomaticMock)
                    .scheduleMinionActionExecutions(with(any(List.class)),with(any(Boolean.class)));
            exactly(1).of(taskomaticMock).scheduleStagingJobs(with(actionsMatcher));
        } });
        HibernateFactory.getSession().flush();
        ErrataManager.applyErrata(user, errataIds,
                Date.from(scheduledActionTime.toInstant()), serverIds);
    }

    /**
     * Test patch/errata install with staging before entering the staging window
     * @throws Exception when Taskomatic service is down
     */
    public void testPatchInstallWithStagingBeforeWindow() throws Exception {
        Channel channel1 = ChannelFactoryTest.createTestChannel(user);
        final String updateTag = "SLE-SERVER";
        channel1.setUpdateTag(updateTag);

        Server server1 = MinionServerFactoryTest.createTestMinionServer(user);
        server1.setOrg(user.getOrg());
        server1.addChannel(channel1);

        // server 1 has an errata for package1 available
        com.redhat.rhn.domain.rhnpackage.Package package1 =
                createTestPackage(user, channel1, "noarch");
        createTestInstalledPackage(package1, server1);

        Errata e1 = ErrataFactoryTest.createTestPublishedErrata(user.getId());
        channel1.addErrata(e1);
        e1.setAdvisoryName("SUSE-2016-1234");
        e1.getPackages().add(createTestPackage(user, channel1, "noarch"));

        ChannelFactory.save(channel1);
        ErrataCacheManager.insertNeededErrataCache(
                server1.getId(), e1.getId(), package1.getId());

        List<Long> errataIds = new ArrayList<Long>();
        errataIds.add(e1.getId());

        List<Long> serverIds = new ArrayList<Long>();
        serverIds.add(server1.getId());

        user.getOrg().getOrgConfig().setStagingContentEnabled(true);
        Config.get().setString(SALT_CONTENT_STAGING_WINDOW, "2");
        Config.get().setString(SALT_CONTENT_STAGING_ADVANCE, "1");

        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime scheduledActionTime = now.plusHours(24);

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ErrataManager.setTaskomaticApi(taskomaticMock);
        MinionActionManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            Matcher<Map<Long, ZonedDateTime>> minionMatcher =
                    AllOf.allOf(IsMapContaining.hasKey(server1.getId()));
            Matcher<Map<Long, Map<Long, ZonedDateTime>>> actionsMatcher =
                    AllOf.allOf(IsMapContaining.hasEntry(any(Long.class), minionMatcher));
            exactly(1).of(taskomaticMock)
                    .scheduleMinionActionExecutions(with(any(List.class)),with(any(Boolean.class)));
            exactly(1).of(taskomaticMock).scheduleStagingJobs(with(actionsMatcher));
        } });
        HibernateFactory.getSession().flush();
        ErrataManager.applyErrata(user, errataIds,
                Date.from(scheduledActionTime.toInstant()), serverIds);
    }

    /**
     * Test patch/errata install with staging when the staging window duration
     * is after the action execution
     * @throws Exception when Taskomatic service is down
     */
    public void testPatchInstallWithStagingBeforeWindowExceedingDuration()
        throws Exception {
        Channel channel1 = ChannelFactoryTest.createTestChannel(user);

        final String updateTag = "SLE-SERVER";
        channel1.setUpdateTag(updateTag);

        Server server1 = MinionServerFactoryTest.createTestMinionServer(user);
        server1.setOrg(user.getOrg());
        server1.addChannel(channel1);

        // server 1 has an errata for package1 available
        com.redhat.rhn.domain.rhnpackage.Package package1 =
                createTestPackage(user, channel1, "noarch");
        createTestInstalledPackage(package1, server1);

        Errata e1 = ErrataFactoryTest.createTestPublishedErrata(user.getId());
        channel1.addErrata(e1);
        e1.setAdvisoryName("SUSE-2016-1234");
        e1.getPackages().add(createTestPackage(user, channel1, "noarch"));

        ChannelFactory.save(channel1);

        ErrataCacheManager.insertNeededErrataCache(
                server1.getId(), e1.getId(), package1.getId());

        List<Long> errataIds = new ArrayList<Long>();
        errataIds.add(e1.getId());

        List<Long> serverIds = new ArrayList<Long>();
        serverIds.add(server1.getId());

        user.getOrg().getOrgConfig().setStagingContentEnabled(true);
        Config.get().setString(SALT_CONTENT_STAGING_WINDOW, "90");
        Config.get().setString(SALT_CONTENT_STAGING_ADVANCE, "48");

        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime scheduledActionTime = now.plusHours(24);

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ErrataManager.setTaskomaticApi(taskomaticMock);
        MinionActionManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            Matcher<Map<Long, ZonedDateTime>> minionMatcher =
                    AllOf.allOf(IsMapContaining.hasKey(server1.getId()));
            Matcher<Map<Long, Map<Long, ZonedDateTime>>> actionsMatcher =
                    AllOf.allOf(IsMapContaining.hasEntry(any(Long.class), minionMatcher));
            exactly(1).of(taskomaticMock)
                    .scheduleMinionActionExecutions(with(any(List.class)),with(any(Boolean.class)));
            exactly(1).of(taskomaticMock).scheduleStagingJobs(with(actionsMatcher));
        } });
        HibernateFactory.getSession().flush();
        ErrataManager.applyErrata(user, errataIds,
                Date.from(scheduledActionTime.toInstant()), serverIds);
    }

    /**
     * Test patch/errata install with staging when the staging window
     * is already passed (there will be no staging)
     *
     * @throws Exception when Taskomatic service is down
     */
    public void testPatchInstallWithStagingAfterWindow() throws Exception {
        Channel channel1 = ChannelFactoryTest.createTestChannel(user);
        Channel channel2 = ChannelFactoryTest.createTestChannel(user);
        Channel channel3 = ChannelFactoryTest.createTestChannel(user);
        final String updateTag = "SLE-SERVER";
        channel1.setUpdateTag(updateTag);
        channel2.setUpdateTag(updateTag);
        channel3.setUpdateTag(updateTag);

        Server server1 = MinionServerFactoryTest.createTestMinionServer(user);
        server1.setOrg(user.getOrg());
        server1.addChannel(channel1);
        server1.addChannel(channel3);

        // server 1 has an errata for package1 available
        com.redhat.rhn.domain.rhnpackage.Package package1 =
                createTestPackage(user, channel1, "noarch");
        createTestInstalledPackage(package1, server1);

        Errata e1 = ErrataFactoryTest.createTestPublishedErrata(user.getId());
        channel1.addErrata(e1);
        e1.setAdvisoryName("SUSE-2016-1234");
        e1.getPackages().add(createTestPackage(user, channel1, "noarch"));

        ChannelFactory.save(channel1);
        ChannelFactory.save(channel2);
        ChannelFactory.save(channel3);
        ErrataCacheManager.insertNeededErrataCache(
                server1.getId(), e1.getId(), package1.getId());

        List<Long> errataIds = new ArrayList<Long>();
        errataIds.add(e1.getId());

        List<Long> serverIds = new ArrayList<Long>();
        serverIds.add(server1.getId());

        user.getOrg().getOrgConfig().setStagingContentEnabled(true);
        Config.get().setString(SALT_CONTENT_STAGING_WINDOW, "1");
        Config.get().setString(SALT_CONTENT_STAGING_ADVANCE, "48");

        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime scheduledActionTime = now.plusHours(24);

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ErrataManager.setTaskomaticApi(taskomaticMock);
        MinionActionManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            Matcher<Map<Long, ZonedDateTime>> minionMatcher =
                    AllOf.allOf(IsMapContaining.hasKey(server1.getId()));
            Matcher<Map<Long, Map<Long, ZonedDateTime>>> actionsMatcher =
                    AllOf.allOf(IsMapContaining.hasEntry(any(Long.class), minionMatcher));
            exactly(1).of(taskomaticMock)
                    .scheduleMinionActionExecutions(with(any(List.class)),with(any(Boolean.class)));
            never(taskomaticMock).scheduleStagingJobs(with(actionsMatcher));
        } });
        HibernateFactory.getSession().flush();
        ErrataManager.applyErrata(user, errataIds,
                Date.from(scheduledActionTime.toInstant()), serverIds);
    }

    /**
     * Test patch/errata install without staging
     * @throws Exception when Taskomatic service is down
     */
    public void testPatchInstallWithoutStaging() throws Exception {
        Channel channel1 = ChannelFactoryTest.createTestChannel(user);
        final String updateTag = "SLE-SERVER";
        channel1.setUpdateTag(updateTag);

        Server server1 = MinionServerFactoryTest.createTestMinionServer(user);
        server1.setOrg(user.getOrg());

        // server 1 has an errata for package1 available
        com.redhat.rhn.domain.rhnpackage.Package package1 =
                createTestPackage(user, channel1, "noarch");
        createTestInstalledPackage(package1, server1);

        Errata e1 = ErrataFactoryTest.createTestPublishedErrata(user.getId());
        channel1.addErrata(e1);
        e1.setAdvisoryName("SUSE-2016-1234");
        e1.getPackages().add(createTestPackage(user, channel1, "noarch"));

        ChannelFactory.save(channel1);
        ErrataCacheManager.insertNeededErrataCache(
                server1.getId(), e1.getId(), package1.getId());

        List<Long> errataIds = new ArrayList<Long>();
        errataIds.add(e1.getId());

        List<Long> serverIds = new ArrayList<Long>();
        serverIds.add(server1.getId());

        user.getOrg().getOrgConfig().setStagingContentEnabled(false);
        Config.get().setString(SALT_CONTENT_STAGING_WINDOW, "1");
        Config.get().setString(SALT_CONTENT_STAGING_ADVANCE, "48");

        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime scheduledActionTime = now.plusHours(24);

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ErrataManager.setTaskomaticApi(taskomaticMock);
        MinionActionManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            exactly(1).of(taskomaticMock)
                    .scheduleMinionActionExecutions(with(any(List.class)), with(any(Boolean.class)));
            never(taskomaticMock).scheduleStagingJob(with(any(Long.class)),
                    with(server1.getId()),
                    with(any(Date.class)));
        } });
        HibernateFactory.getSession().flush();
        ErrataManager.applyErrata(user, errataIds,
                Date.from(scheduledActionTime.toInstant()), serverIds);
    }

    /**
     * Test scheduled time of staging jobs (must be before execution)
     * @throws Exception when Taskomatic service is down
     */
    public void testStagingJobsScheduleTime() throws Exception {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);

        UserFactory.save(user);
        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        minion1.setOrg(user.getOrg());
        MinionServer minion2 = MinionServerFactoryTest.createTestMinionServer(user);
        minion2.setOrg(user.getOrg());

        user.getOrg().getOrgConfig().setStagingContentEnabled(true);
        Config.get().setString(SALT_CONTENT_STAGING_WINDOW, "48");
        Config.get().setString(SALT_CONTENT_STAGING_ADVANCE, "48");

        final ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        final ZonedDateTime executionTime = now.plusHours(24);

        Action action = ActionManager.createAction(user, ActionFactory.TYPE_PACKAGES_UPDATE,
                "test action", Date.from(executionTime.toInstant()));
        ActionManager.scheduleForExecution(action,
                new HashSet<Long>(Arrays.asList(minion1.getId(), minion2.getId())));
        ActionFactory.save(action);

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        MinionActionManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            Matcher<Map<Long, ZonedDateTime>> minionMatcher =
                    AllOf.allOf(IsMapContaining.hasKey(minion1.getId()),IsMapContaining.hasKey(minion2.getId()));
            Matcher<Map<Long, Map<Long, ZonedDateTime>>> actionsMatcher =
                    AllOf.allOf(IsMapContaining.hasEntry(any(Long.class), minionMatcher));
            exactly(1).of(taskomaticMock).scheduleStagingJobs(with(actionsMatcher));
        } });

        Map<Long, Map<Long, ZonedDateTime>> actionsDataMap =
                MinionActionManager.scheduleStagingJobsForMinions(Collections.singletonList(action), user);
        List<ZonedDateTime> scheduleTimes =
                actionsDataMap.values().stream().map(s -> new ArrayList<>(s.values()))
                        .flatMap(List::stream).collect(Collectors.toList());

        assertEquals(2,
                scheduleTimes.stream()
                    .filter(scheduleTime -> scheduleTime.isAfter(now))
                    .filter(scheduleTime -> scheduleTime.isBefore(executionTime))
                    .collect(Collectors.toList()).size());
    }

    /**
     * Test schedule time of staging jobs when staging window is already
     * passed
     *
     * @throws Exception when Taskomatic service is down
     */
    public void testStagingJobsScheduleTimeOutsideWindow() throws Exception {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);

        UserFactory.save(user);
        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        minion1.setOrg(user.getOrg());
        MinionServer minion2 = MinionServerFactoryTest.createTestMinionServer(user);
        minion2.setOrg(user.getOrg());

        user.getOrg().getOrgConfig().setStagingContentEnabled(true);
        Config.get().setString(SALT_CONTENT_STAGING_WINDOW, "1");
        Config.get().setString(SALT_CONTENT_STAGING_ADVANCE, "48");

        final ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        final ZonedDateTime executionTime = now.plusHours(24);

        Action action = ActionManager.createAction(user, ActionFactory.TYPE_PACKAGES_UPDATE,
                "test action", Date.from(executionTime.toInstant()));
        ActionManager.scheduleForExecution(action,
                new HashSet<Long>(Arrays.asList(minion1.getId(), minion2.getId())));
        ActionFactory.save(action);

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        MinionActionManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            never(taskomaticMock).scheduleStagingJob(with(action.getId()),
                    with(minion1.getId()),
                    with(any(Date.class)));
            never(taskomaticMock).scheduleStagingJob(with(action.getId()),
                    with(minion2.getId()),
                    with(any(Date.class)));
        } });
        Map<Long, Map<Long, ZonedDateTime>> actionsDataMap =
                MinionActionManager.scheduleStagingJobsForMinions(Collections.singletonList(action), user);
        List<ZonedDateTime> scheduleTimes =
                actionsDataMap.values().stream().map(s -> new ArrayList<>(s.values()))
                        .flatMap(List::stream).collect(Collectors.toList());

        assertEquals(0,
                scheduleTimes.stream()
                    .filter(scheduleTime -> scheduleTime.isAfter(now))
                    .filter(scheduleTime -> scheduleTime.isBefore(executionTime))
                    .collect(Collectors.toList()).size());
    }

    /**
     * Test that no staging jobs are scheduled when action
     * is executed now
     *
     * @throws Exception when Taskomatic service is down
     */
    public void testNoStagingJobsWhenImmediateExecution() throws Exception {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);

        UserFactory.save(user);
        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        minion1.setOrg(user.getOrg());
        MinionServer minion2 = MinionServerFactoryTest.createTestMinionServer(user);
        minion2.setOrg(user.getOrg());

        user.getOrg().getOrgConfig().setStagingContentEnabled(true);
        Config.get().setString(SALT_CONTENT_STAGING_WINDOW, "1");
        Config.get().setString(SALT_CONTENT_STAGING_ADVANCE, "48");

        final ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        final ZonedDateTime executionTime = now.plusHours(24);

        Action action = ActionManager.createAction(user, ActionFactory.TYPE_PACKAGES_UPDATE,
                "test action", Date.from(now.toInstant()));
        ActionManager.scheduleForExecution(action,
                new HashSet<Long>(Arrays.asList(minion1.getId(), minion2.getId())));
        ActionFactory.save(action);

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        MinionActionManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            never(taskomaticMock).scheduleStagingJob(with(action.getId()),
                    with(minion1.getId()),
                    with(any(Date.class)));
            never(taskomaticMock).scheduleStagingJob(with(action.getId()),
                    with(minion2.getId()),
                    with(any(Date.class)));
        } });
        Map<Long, Map<Long, ZonedDateTime>> actionsDataMap =
                MinionActionManager.scheduleStagingJobsForMinions(Collections.singletonList(action), user);
        List<ZonedDateTime> scheduleTimes =
                actionsDataMap.values().stream().map(s -> new ArrayList<>(s.values()))
                        .flatMap(List::stream).collect(Collectors.toList());
        assertEquals(0,
                scheduleTimes.stream()
                    .filter(scheduleTime -> scheduleTime.isAfter(now))
                    .filter(scheduleTime -> scheduleTime.isBefore(executionTime))
                    .collect(Collectors.toList()).size());
    }

    /**
     * Test that no staging jobs are scheduled when config parameters
     * are scheduled to design a staging window in which start equals
     * end
     *
     * @throws Exception when Taskomatic service is down
     */
    public void testNoStagingJobsWhenWindowStartEqualsFinish() throws Exception {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);

        UserFactory.save(user);
        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        minion1.setOrg(user.getOrg());
        MinionServer minion2 = MinionServerFactoryTest.createTestMinionServer(user);
        minion2.setOrg(user.getOrg());

        user.getOrg().getOrgConfig().setStagingContentEnabled(true);
        Config.get().setString(SALT_CONTENT_STAGING_WINDOW, "0");
        Config.get().setString(SALT_CONTENT_STAGING_ADVANCE, "0");

        final ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        final ZonedDateTime executionTime = now.plusHours(24);

        Action action = ActionManager.createAction(user, ActionFactory.TYPE_PACKAGES_UPDATE,
                "test action", Date.from(now.toInstant()));
        ActionManager.scheduleForExecution(action,
                new HashSet<Long>(Arrays.asList(minion1.getId(), minion2.getId())));
        ActionFactory.save(action);

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        MinionActionManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            never(taskomaticMock).scheduleStagingJob(with(action.getId()),
                    with(minion1.getId()),
                    with(any(Date.class)));
            never(taskomaticMock).scheduleStagingJob(with(action.getId()),
                    with(minion2.getId()),
                    with(any(Date.class)));
        } });

        Map<Long, Map<Long, ZonedDateTime>> actionsDataMap =
                MinionActionManager.scheduleStagingJobsForMinions(Collections.singletonList(action), user);
        List<ZonedDateTime> scheduleTimes =
                actionsDataMap.values().stream().map(s -> new ArrayList<>(s.values()))
                        .flatMap(List::stream).collect(Collectors.toList());

        assertEquals(0,
                scheduleTimes.stream()
                    .filter(scheduleTime -> scheduleTime.isAfter(now))
                    .filter(scheduleTime -> scheduleTime.isBefore(executionTime))
                    .collect(Collectors.toList()).size());
    }

    /**
     * Test that no staging jobs are scheduled when the staging
     * window has zero hours in advance to start
     *
     * @throws Exception when Taskomatic service is down
     */
    public void testNoStagingJobsWhenWindowIsZeroAdvance() throws Exception {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);

        UserFactory.save(user);
        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        minion1.setOrg(user.getOrg());
        MinionServer minion2 = MinionServerFactoryTest.createTestMinionServer(user);
        minion2.setOrg(user.getOrg());

        user.getOrg().getOrgConfig().setStagingContentEnabled(true);
        Config.get().setString(SALT_CONTENT_STAGING_WINDOW, "1");
        Config.get().setString(SALT_CONTENT_STAGING_ADVANCE, "0");

        final ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        final ZonedDateTime executionTime = now.plusHours(24);

        Action action = ActionManager.createAction(user, ActionFactory.TYPE_PACKAGES_UPDATE,
                "test action", Date.from(now.toInstant()));
        ActionManager.scheduleForExecution(action,
                new HashSet<Long>(Arrays.asList(minion1.getId(), minion2.getId())));
        ActionFactory.save(action);

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        MinionActionManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            never(taskomaticMock).scheduleStagingJob(with(action.getId()),
                    with(minion1.getId()),
                    with(any(Date.class)));
            never(taskomaticMock).scheduleStagingJob(with(action.getId()),
                    with(minion2.getId()),
                    with(any(Date.class)));
        } });

        Map<Long, Map<Long, ZonedDateTime>> actionsDataMap =
                MinionActionManager.scheduleStagingJobsForMinions(Collections.singletonList(action), user);
        List<ZonedDateTime> scheduleTimes =
                actionsDataMap.values().stream().map(s -> new ArrayList<>(s.values()))
                        .flatMap(List::stream).collect(Collectors.toList());
        assertEquals(0,
                scheduleTimes.stream()
                    .filter(scheduleTime -> scheduleTime.isAfter(now))
                    .filter(scheduleTime -> scheduleTime.isBefore(executionTime))
                    .collect(Collectors.toList()).size());
    }

    /**
     * Test that no staging jobs are scheduled when staging
     * window is zero hours length
     *
     * @throws Exception when Taskomatic service is down
     */
    public void testNoStagingJobsWhenWindowIsZeroLength() throws Exception {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);

        UserFactory.save(user);
        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        minion1.setOrg(user.getOrg());
        MinionServer minion2 = MinionServerFactoryTest.createTestMinionServer(user);
        minion2.setOrg(user.getOrg());

        user.getOrg().getOrgConfig().setStagingContentEnabled(true);
        Config.get().setString(SALT_CONTENT_STAGING_WINDOW, "0");
        Config.get().setString(SALT_CONTENT_STAGING_ADVANCE, "1");

        final ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        final ZonedDateTime executionTime = now.plusHours(24);

        Action action = ActionManager.createAction(user, ActionFactory.TYPE_PACKAGES_UPDATE,
                "test action", Date.from(now.toInstant()));
        ActionManager.scheduleForExecution(action,
                new HashSet<Long>(Arrays.asList(minion1.getId(), minion2.getId())));
        ActionFactory.save(action);

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        MinionActionManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            never(taskomaticMock).scheduleStagingJob(with(action.getId()),
                    with(minion1.getId()),
                    with(any(Date.class)));
            never(taskomaticMock).scheduleStagingJob(with(action.getId()),
                    with(minion2.getId()),
                    with(any(Date.class)));
        } });

        Map<Long, Map<Long, ZonedDateTime>> actionsDataMap =
                MinionActionManager.scheduleStagingJobsForMinions(Collections.singletonList(action), user);
        List<ZonedDateTime> scheduleTimes =
                actionsDataMap.values().stream().map(s -> new ArrayList<>(s.values()))
                        .flatMap(List::stream).collect(Collectors.toList());
        assertEquals(0,
                scheduleTimes.stream()
                    .filter(scheduleTime -> scheduleTime.isAfter(now))
                    .filter(scheduleTime -> scheduleTime.isBefore(executionTime))
                    .collect(Collectors.toList()).size());
    }

    /**
     * Test that no staging jobs are scheduled when scheduling action on a
     * traditional client
     *
     * @throws Exception when Taskomatic service is down
     */
    public void testNoStagingJobsWhenTraditionalClient() throws Exception {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);

        UserFactory.save(user);
        Server s1 = ServerTestUtils.createTestSystem(user);
        s1.setOrg(user.getOrg());
        Server s2 = ServerTestUtils.createTestSystem(user);
        s2.setOrg(s2.getOrg());

        user.getOrg().getOrgConfig().setStagingContentEnabled(true);
        Config.get().setString(SALT_CONTENT_STAGING_WINDOW, "0");
        Config.get().setString(SALT_CONTENT_STAGING_ADVANCE, "1");

        final ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        final ZonedDateTime executionTime = now.plusHours(24);

        Action action = ActionManager.createAction(user, ActionFactory.TYPE_PACKAGES_UPDATE,
                "test action", Date.from(now.toInstant()));
        ActionManager.scheduleForExecution(action,
                new HashSet<Long>(Arrays.asList(s1.getId(), s2.getId())));
        ActionFactory.save(action);

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        MinionActionManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            never(taskomaticMock).scheduleStagingJob(with(action.getId()),
                    with(s1.getId()),
                    with(any(Date.class)));
            never(taskomaticMock).scheduleStagingJob(with(action.getId()),
                    with(s2.getId()),
                    with(any(Date.class)));
        } });
        Map<Long, Map<Long, ZonedDateTime>> actionsDataMap =
                MinionActionManager.scheduleStagingJobsForMinions(Collections.singletonList(action), user);
        List<ZonedDateTime> scheduleTimes =
                actionsDataMap.values().stream().map(s -> new ArrayList<>(s.values()))
                        .flatMap(List::stream).collect(Collectors.toList());
        assertEquals(0,
                scheduleTimes.stream()
                    .filter(scheduleTime -> scheduleTime.isAfter(now))
                    .filter(scheduleTime -> scheduleTime.isBefore(executionTime))
                    .collect(Collectors.toList()).size());
    }

 }
