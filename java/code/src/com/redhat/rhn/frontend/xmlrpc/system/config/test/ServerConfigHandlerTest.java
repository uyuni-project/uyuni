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
package com.redhat.rhn.frontend.xmlrpc.system.config.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.config.ConfigAction;
import com.redhat.rhn.domain.action.config.ConfigRevisionAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesActionDetails;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.config.ConfigChannelType;
import com.redhat.rhn.domain.config.ConfigFile;
import com.redhat.rhn.domain.config.ConfigFileState;
import com.redhat.rhn.domain.config.ConfigFileType;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.config.ConfigurationFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.frontend.dto.ConfigFileNameDto;
import com.redhat.rhn.frontend.dto.ScheduledAction;
import com.redhat.rhn.frontend.xmlrpc.serializer.ConfigRevisionSerializer;
import com.redhat.rhn.frontend.xmlrpc.system.XmlRpcSystemHelper;
import com.redhat.rhn.frontend.xmlrpc.system.config.ServerConfigHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.test.SystemManagerTest;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.ConfigTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.cloud.CloudPaygManager;
import com.suse.cloud.test.TestCloudPaygManagerBuilder;
import com.suse.manager.attestation.AttestationManager;
import com.suse.manager.webui.controllers.bootstrap.RegularMinionBootstrapper;
import com.suse.manager.webui.controllers.bootstrap.SSHMinionBootstrapper;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.manager.webui.services.test.TestSystemQuery;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.junit5.JUnit5Mockery;
import org.jmock.lib.concurrent.Synchroniser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * SystemConfigHandlerTest
 */
@ExtendWith(JUnit5Mockery.class)
public class ServerConfigHandlerTest extends BaseHandlerTestCase {
    private final TaskomaticApi taskomaticApi = new TaskomaticApi();
    private final SaltApi saltApi = new TestSaltApi();
    private final SystemQuery systemQuery = new TestSystemQuery();
    private final CloudPaygManager paygManager = new TestCloudPaygManagerBuilder().build();
    private final AttestationManager attestationManager = new AttestationManager();
    private final RegularMinionBootstrapper regularMinionBootstrapper =
            new RegularMinionBootstrapper(systemQuery, saltApi, paygManager, attestationManager);
    private final SSHMinionBootstrapper sshMinionBootstrapper =
            new SSHMinionBootstrapper(systemQuery, saltApi, paygManager, attestationManager);
    private final XmlRpcSystemHelper xmlRpcSystemHelper = new XmlRpcSystemHelper(
            regularMinionBootstrapper,
            sshMinionBootstrapper
    );
    private final ServerConfigHandler handler = new ServerConfigHandler(taskomaticApi, xmlRpcSystemHelper);

    @RegisterExtension
    protected final Mockery mockContext = new JUnit5Mockery() {{
        setThreadingPolicy(new Synchroniser());
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }};

    @Test
    public void testDeployConfiguration() {
        // Create  global config channels
        ConfigChannel gcc1 = ConfigTestUtils.createConfigChannel(admin.getOrg(),
                ConfigChannelType.normal());
        ConfigChannel gcc2 = ConfigTestUtils.createConfigChannel(admin.getOrg(),
                ConfigChannelType.normal());
        List<ConfigChannel> gccList = new ArrayList<>();
        gccList.add(gcc1);
        gccList.add(gcc2);

        Long ver = 2L;

        // gcc1 only
        Server srv1 = ServerFactoryTest.createTestServer(regular, true);
        srv1.subscribeConfigChannels(gccList, regular);

        ServerFactory.save(srv1);

        Set<ConfigRevision> revisions = new HashSet<>();

        ConfigFile g1f1 = gcc1.createConfigFile(
                ConfigFileState.normal(), "/etc/foo1");
        revisions.add(ConfigTestUtils.createConfigRevision(g1f1));

        ConfigurationFactory.commit(gcc1);

        ConfigFile g1f2 = gcc1.createConfigFile(
                ConfigFileState.normal(), "/etc/foo2");
        revisions.add(ConfigTestUtils.createConfigRevision(g1f2));
        ConfigurationFactory.commit(gcc2);

        ConfigFile g2f2 = gcc2.createConfigFile(
                ConfigFileState.normal(), "/etc/foo4");
        revisions.add(ConfigTestUtils.createConfigRevision(g2f2));
        ConfigurationFactory.commit(gcc2);

        ConfigFile g2f3 = gcc2.createConfigFile(
                ConfigFileState.normal(), "/etc/foo3");
        revisions.add(ConfigTestUtils.createConfigRevision(g2f3));
        ConfigurationFactory.commit(gcc2);


        // System 1 - both g1f1 and g1f2 should deploy here
        List<Number> systems  = new ArrayList<>();
        systems.add(srv1.getId());
        Date date = new Date();

        try {
            // validate that system must have config deployment capability
            // in order to deploy config files...
            handler.deployAll(regular, systems, date);

            fail("Shouldn't be permitted to deploy without config deploy capability.");
        }
        catch (Exception e) {
            // Success
        }

        SystemManagerTest.giveCapability(srv1.getId(),
                SystemManager.CAP_CONFIGFILES_DEPLOY, ver);

        handler.deployAll(regular, systems, date);

        DataResult<ScheduledAction> actions = ActionManager.
                                    recentlyScheduledActions(regular, null, 1);
        ConfigAction ca = null;
        for (ScheduledAction action : actions) {
            if (ActionFactory.TYPE_CONFIGFILES_DEPLOY.getName().
                    equals(action.getTypeName())) {
                ca = (ConfigAction)ActionManager.lookupAction(regular,
                        action.getId());
            }
        }
        assertNotNull(ca);
        assertEquals(revisions.size(), ca.getConfigRevisionActions().size());
        for (ConfigRevisionAction cra : ca.getConfigRevisionActions()) {
            assertTrue(revisions.contains(cra.getConfigRevision()));
        }
    }

    @Test
    public void testConfigAddChannelToTop() {
        // Create  global config channels
        ConfigChannel gcc1 = ConfigTestUtils.createConfigChannel(admin.getOrg(), ConfigChannelType.normal());
        ConfigChannel gcc2 = ConfigTestUtils.createConfigChannel(admin.getOrg(), ConfigChannelType.normal());

        Server srv1 = ServerFactoryTest.createTestServer(regular, true);

        List<Number> serverIds = List.of(srv1.getId());

        List<ConfigChannel> channels = List.of(gcc1, gcc2);

        srv1.setConfigChannels(List.of(gcc2), regular);

        //test add channels
        handler.addChannels(admin, serverIds, List.of(gcc1.getLabel()), true);

        TestUtils.saveAndFlush(srv1);
        HibernateFactory.getSession().detach(srv1);

        assertEquals(channels, handler.listChannels(regular, srv1.getId().intValue()));
    }

    @Test
    public void testConfigSetChannels() {
        // Create  global config channels
        ConfigChannel gcc1 = ConfigTestUtils.createConfigChannel(admin.getOrg(), ConfigChannelType.normal());
        ConfigChannel gcc2 = ConfigTestUtils.createConfigChannel(admin.getOrg(), ConfigChannelType.normal());

        Server srv1 = ServerFactoryTest.createTestServer(regular, true);

        List<Number> serverIds = List.of(srv1.getId());

        List<ConfigChannel> channels = List.of(gcc1, gcc2);

        List<String> channelLabels = channels.stream().map(ConfigChannel::getLabel).collect(Collectors.toList());

        //test set channels
        handler.setChannels(admin, serverIds, channelLabels);

        TestUtils.saveAndFlush(srv1);
        HibernateFactory.getSession().detach(srv1);

        assertEquals(channels, handler.listChannels(regular, srv1.getId().intValue()));
    }

    @Test
    public void testConfigAddChannelsToBottom() {
        // Create  global config channels
        ConfigChannel gcc1 = ConfigTestUtils.createConfigChannel(admin.getOrg(), ConfigChannelType.normal());
        ConfigChannel gcc2 = ConfigTestUtils.createConfigChannel(admin.getOrg(), ConfigChannelType.normal());

        Server srv1 = ServerFactoryTest.createTestServer(regular, true);

        List<Number> serverIds = List.of(srv1.getId());

        List<ConfigChannel> channels = List.of(gcc1, gcc2);

        srv1.setConfigChannels(List.of(gcc1), regular);

        //test add channels
        handler.addChannels(admin, serverIds, List.of(gcc2.getLabel()), false);

        TestUtils.saveAndFlush(srv1);
        HibernateFactory.getSession().detach(srv1);

        assertEquals(channels, handler.listChannels(regular, srv1.getId().intValue()));
    }

    @Test
    public void testConfigChannelRemove() {
        // Create  global config channels
        ConfigChannel gcc1 = ConfigTestUtils.createConfigChannel(admin.getOrg(),
                ConfigChannelType.normal());
        ConfigChannel gcc2 = ConfigTestUtils.createConfigChannel(admin.getOrg(),
                ConfigChannelType.normal());

        Server srv1 = ServerFactoryTest.createTestServer(regular, true);

        List<Number> serverIds = List.of(srv1.getId());

        List<ConfigChannel> channels = List.of(gcc1, gcc2);
        List<String> channelLabels = channels.stream()
                .map(ConfigChannel::getLabel)
                .collect(Collectors.toList());

        srv1.setConfigChannels(channels, regular);

        assertEquals(1, handler.removeChannels(admin, serverIds, channelLabels));

        TestUtils.reload(srv1);

        assertEquals(0, handler.listChannels(admin, srv1.getId().intValue()).size());
    }

    @Test
    public void testConfigChannelsRemoveNonExistingConfigChannels() {
        // Create  global config channels
        ConfigChannel gcc1 = ConfigTestUtils.createConfigChannel(admin.getOrg(),
                ConfigChannelType.normal());
        ConfigChannel gcc2 = ConfigTestUtils.createConfigChannel(admin.getOrg(),
                ConfigChannelType.normal());

        Server srv1 = ServerFactoryTest.createTestServer(regular, true);

        List<Number> serverIds = List.of(srv1.getId());

        srv1.setConfigChannels(List.of(gcc2), regular);

        // Test removing nonexisting channels
        assertEquals(0, handler.removeChannels(admin, serverIds, List.of(gcc1.getLabel(), gcc2.getLabel())));

        TestUtils.reload(srv1);

        // The other channel is removed even though the result is 0
        assertEquals(0, handler.listChannels(admin, srv1.getId().intValue()).size());
    }

    private ConfigRevision createRevision(String path, String contents,
            String group, String owner,
                String perms, boolean isDir,
                Server server, boolean commitToLocal, String selinuxCtx)
                        throws ValidatorException {
            Map<String, Object> data = new HashMap<>();
            data.put(ConfigRevisionSerializer.GROUP, group);
            data.put(ConfigRevisionSerializer.OWNER, owner);
            data.put(ConfigRevisionSerializer.PERMISSIONS, perms);
            data.put(ConfigRevisionSerializer.SELINUX_CTX, selinuxCtx);
            String start = "#@";
            String end = "@#";
            if (!isDir) {
                data.put(ConfigRevisionSerializer.CONTENTS, contents);
                data.put(ConfigRevisionSerializer.MACRO_START, start);
                data.put(ConfigRevisionSerializer.MACRO_END, end);
            }

            ConfigRevision rev = handler.createOrUpdatePath(
                        admin, server.getId().intValue(),
                        path, isDir, data, commitToLocal);

            server = (Server) HibernateFactory.reload(server);
            ConfigChannel cc = commitToLocal ? server.getLocalOverride() :
                                                     server.getSandboxOverride();
            assertRev(rev, path, contents, group, owner, perms, isDir, cc, start, end,
                    selinuxCtx);

            assertRevNotChanged(rev, server, commitToLocal);

            return rev;
    }



    private ConfigRevision createSymlinkRevision(String path, String targetPath,
            Server server, boolean commitToLocal, String selinuxCtx)
                        throws ValidatorException {
        Map<String, Object> data = new HashMap<>();
        data.put(ConfigRevisionSerializer.TARGET_PATH, targetPath);
        data.put(ConfigRevisionSerializer.SELINUX_CTX, selinuxCtx);
        ConfigRevision rev = handler.createOrUpdateSymlink(admin,
                    server.getId().intValue(), path, data, commitToLocal);
        server = (Server) HibernateFactory.reload(server);
        ConfigChannel cc = commitToLocal ? server.getLocalOverride() :
            server.getSandboxOverride();

        assertRevNotChanged(rev, server, commitToLocal);

        assertEquals(path, rev.getConfigFile().getConfigFileName().getPath());
        assertEquals(ConfigFileType.symlink(), rev.getConfigFileType());
        assertEquals(targetPath, rev.getConfigInfo().getTargetFileName().getPath());
        assertEquals(selinuxCtx, rev.getConfigInfo().getSelinuxCtx());
        assertEquals(cc, rev.getConfigFile().getConfigChannel());

        assertRevNotChanged(rev, server, commitToLocal);

        return rev;
    }

    private void assertRev(ConfigRevision rev, String path, String contents,
                                    String group, String owner,
                                String perms, boolean isDir, ConfigChannel cc,
                                String macroStart, String macroEnd, String selinuxCtx) {
            assertEquals(path, rev.getConfigFile().getConfigFileName().getPath());

            assertEquals(group, rev.getConfigInfo().getGroupname());
            assertEquals(owner, rev.getConfigInfo().getUsername());
            assertEquals(perms, String.valueOf(rev.getConfigInfo().getFilemode()));
            assertEquals(selinuxCtx, rev.getConfigInfo().getSelinuxCtx());
            if (isDir) {
                assertEquals(ConfigFileType.dir(), rev.getConfigFileType());
            }
            else {
                if (ConfigFileType.file().equals(rev.getConfigFileType())) {
                    assertEquals(contents, rev.getConfigContent().getContentsString());
                    assertEquals(macroStart, rev.getConfigContent().getDelimStart());
                    assertEquals(macroEnd, rev.getConfigContent().getDelimEnd());
                }
            }
            assertEquals(cc,
                        rev.getConfigFile().getConfigChannel());
    }

    private void assertRev(ConfigRevision rev, String path, Server server,
                                                        boolean lookLocal) {
        List<String> paths = new ArrayList<>(1);
        paths.add(path);
        assertTrue(rev.matches(handler.lookupFileInfo(admin, server.getId().intValue(),
                paths, lookLocal).get(0)));
    }

    @Test
    public void testLookupFileInfoNoData() {
        Server srv1 = ServerFactoryTest.createTestServer(regular, true);
        List<String> paths = new LinkedList<>();
        paths.add("/no/such/file.txt");

        // Should not throw a NullPointerException (anymore):
        handler.lookupFileInfo(admin, srv1.getId().intValue(),
                paths, true);
    }

    private void assertRevNotChanged(ConfigRevision rev,
                                            Server server, boolean local) {
        assertRev(rev, rev.getConfigFile().getConfigFileName().getPath(),
                                                    server, local);
    }

    @Test
    public void testAddPath() {
        Server srv1 = ServerFactoryTest.createTestServer(regular, true);

        String path = "/tmp/foo/path" + TestUtils.randomString();
        String contents = "HAHAHAHA";

        ConfigRevision rev = createRevision(path, contents,
                                    "group" + TestUtils.randomString(),
                                    "owner" + TestUtils.randomString(),
                                    "777",
                                    false, srv1, true, "unconfined_u:object_r:tmp_t");
        try {
            createRevision(path, contents,
                    "group" + TestUtils.randomString(),
                    "owner" + TestUtils.randomString(),
                    "744",
                    true, srv1, true, "unconfined_u:object_r:tmp_t");
            fail("Can't change the path from file to directory.");
        }
        catch (Exception e) {
            // Can;t change.. Won't allow...
            assertRevNotChanged(rev, srv1, true);
        }

        try {
            createRevision(path + TestUtils.randomString() + "/" , contents,
                    "group" + TestUtils.randomString(),
                    "owner" + TestUtils.randomString(),
                    "744",
                    true, srv1, false, "unconfined_u:object_r:tmp_t");
            fail("Validation error on the path.");
        }
        catch (Exception e) {
            // Can;t change.. Won't allow...
            assertRevNotChanged(rev, srv1, true);
        }
        createRevision(path + TestUtils.randomString(), "",
                "group" + TestUtils.randomString(),
                "owner" + TestUtils.randomString(),
                "744",
                true, srv1, false, "unconfined_u:object_r:tmp_t");
        createSymlinkRevision(path + TestUtils.randomString(),
                path + TestUtils.randomString(), srv1, false, "root:root");

    }

    @Test
    public void testListFiles() {
        Server srv1 = ServerFactoryTest.createTestServer(regular, true);

        for (int j = 0; j < 2; j++) {
            boolean local = j % 2 == 0;

            List<String> paths = new LinkedList<>();
            Map<String, ConfigRevision> revisions = new HashMap<>();
            setupPathsAndRevisions(srv1, paths, revisions, local);

            List<ConfigFileNameDto> files = handler.listFiles(admin,
                                                srv1.getId().intValue(), local);
            for (ConfigFileNameDto dto : files) {
                assertTrue(revisions.containsKey(dto.getPath()));
                ConfigRevision rev = revisions.get(dto.getPath());
                assertEquals(rev.getConfigFileType().getLabel(),
                                        dto.getConfigFileType());
                assertNotNull(dto.getLastModifiedDate());
            }
        }
    }

    /**
     * @param srv1 server
     * @param paths list holder of paths
     * @param revisions list holder of revisions
     * @param local is local revision or sandbox
     */
    private void setupPathsAndRevisions(Server srv1, List<String> paths,
            Map<String, ConfigRevision> revisions, boolean local) {
        String path = "/tmp/foo/path/";
        for (int i = 0; i < 10; i++) {
            boolean isDir = i % 2 == 0;
            String newPath = path + TestUtils.randomString();
            String contents = isDir ? "" : TestUtils.randomString();
            paths.add(newPath);
            revisions.put(newPath, createRevision(newPath,
                                                contents,
                                                "group" + TestUtils.randomString(),
                                                "owner" + TestUtils.randomString(),
                                                "744",
                                                isDir, srv1, local,
                                                "unconfined_u:object_r:tmp_t"));
        }
    }

    @Test
    public void testRemovePaths() {
        Server srv1 = ServerFactoryTest.createTestServer(regular, true);

        for (int i = 0; i < 2; i++) {
            boolean isLocal = i % 2 == 0;
            List<String> paths = new LinkedList<>();
            Map<String, ConfigRevision> revisions = new HashMap<>();

            setupPathsAndRevisions(srv1, paths, revisions, isLocal);
            paths.remove(paths.size() - 1);
            handler.deleteFiles(admin, srv1.getId().intValue(), paths, isLocal);
            List<ConfigFileNameDto> files = handler.listFiles(admin,
                                            srv1.getId().intValue(), isLocal);
            assertEquals(1, files.size());
        }
    }

    @Test
    public void testScheduleApplyConfigChannel() throws Exception {
        Server testServer = MinionServerFactoryTest.createTestMinionServer(admin);
        int preScheduleSize = ActionManager.recentlyScheduledActions(admin, null, 30).size();
        Date scheduleDate = new Date();

        Long actionId = getMockedHandler().scheduleApplyConfigChannel(
                admin, Collections.singletonList(testServer.getId().intValue()), scheduleDate, false);
        assertNotNull(actionId);

        DataResult schedule = ActionManager.recentlyScheduledActions(admin, null, 30);
        assertEquals(1, schedule.size() - preScheduleSize);
        assertEquals(actionId, ((ScheduledAction) schedule.get(0)).getId());

        // Look up the action and verify the details
        ApplyStatesAction action = (ApplyStatesAction) ActionFactory.lookupByUserAndId(admin, actionId);
        assertNotNull(action);
        assertEquals(ActionFactory.TYPE_APPLY_STATES, action.getActionType());
        assertEquals(scheduleDate, action.getEarliestAction());

        ApplyStatesActionDetails details = action.getDetails();
        assertNotNull(details);
        assertNotNull(details.getStates());
        assertEquals(1, details.getMods().size());
        assertFalse(details.isTest());
    }

    private ServerConfigHandler getMockedHandler() throws Exception {
        TaskomaticApi taskomaticMock = mockContext.mock(TaskomaticApi.class);
        ServerConfigHandler serverConfigHandler = new ServerConfigHandler(taskomaticMock, xmlRpcSystemHelper);

        mockContext.checking(new Expectations() {{
            allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
        }});

        return serverConfigHandler;
    }

}
