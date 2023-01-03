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
package com.redhat.rhn.frontend.xmlrpc.configchannel.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.config.ConfigAction;
import com.redhat.rhn.domain.action.config.ConfigRevisionAction;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.config.ConfigChannelType;
import com.redhat.rhn.domain.config.ConfigFile;
import com.redhat.rhn.domain.config.ConfigFileState;
import com.redhat.rhn.domain.config.ConfigFileType;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.config.ConfigurationFactory;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.frontend.dto.ConfigChannelDto;
import com.redhat.rhn.frontend.dto.ConfigFileDto;
import com.redhat.rhn.frontend.dto.ScheduledAction;
import com.redhat.rhn.frontend.xmlrpc.ConfigFileErrorException;
import com.redhat.rhn.frontend.xmlrpc.InvalidOperationException;
import com.redhat.rhn.frontend.xmlrpc.InvalidParameterException;
import com.redhat.rhn.frontend.xmlrpc.NoSuchChannelException;
import com.redhat.rhn.frontend.xmlrpc.configchannel.ConfigChannelHandler;
import com.redhat.rhn.frontend.xmlrpc.serializer.ConfigRevisionSerializer;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.configuration.ConfigChannelCreationHelper;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.test.SystemManagerTest;
import com.redhat.rhn.testing.ConfigTestUtils;
import com.redhat.rhn.testing.ServerGroupTestUtils;
import com.redhat.rhn.testing.TestUtils;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ConfigChannelHandlerTest
 */
public class ConfigChannelHandlerTest extends BaseHandlerTestCase {
    private ConfigChannelHandler handler = new ConfigChannelHandler();
    private static final String LABEL = "LABEL" + TestUtils.randomString();
    private static final String NAME = "NAME" + TestUtils.randomString();
    private static final String DESCRIPTION = "DESCRIPTION" + TestUtils.randomString();

    @Test
    public void testCreate() {
        try {
            handler.create(regular, LABEL, NAME, DESCRIPTION);
            String msg = "Needs to be a config admin.. perm error not detected.";
            fail(msg);
        }
        catch (Exception e) {
            //Cool perm error!
        }
        ConfigChannel cc = handler.create(admin, LABEL, NAME, DESCRIPTION);
        assertEquals(LABEL, cc.getLabel());
        assertEquals(NAME, cc.getName());
        assertEquals(DESCRIPTION, cc.getDescription());
        assertEquals(admin.getOrg(), cc.getOrg());

        try {
            cc = handler.create(admin, LABEL + "/", NAME, DESCRIPTION);
            String msg = "Invalid character / not detected:(";
            fail(msg);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            //Cool invalid check works!..
        }
    }

    @Test
    public void testCreateStateChannel() {

        ConfigChannel cc = handler.create(admin, LABEL, NAME, DESCRIPTION, "state");
        assertEquals(LABEL, cc.getLabel());
        assertEquals(NAME, cc.getName());
        assertEquals(DESCRIPTION, cc.getDescription());
        assertEquals(admin.getOrg(), cc.getOrg());
        assertEquals(ConfigChannelType.state(), cc.getConfigChannelType());
        assertEquals("/init.sls", cc.getConfigFiles().first().getConfigFileName().getPath());

    }

    @Test
    public void testUpdate() {
        ConfigChannel cc = handler.create(admin, LABEL, NAME, DESCRIPTION);
        String newName = NAME + TestUtils.randomString();
        String desc = DESCRIPTION + TestUtils.randomString();
        try {
            handler.update(regular, LABEL, newName, desc);
            String msg = "Needs to be a config admin/have access.. " +
                            "perm error not detected.";
            fail(msg);
        }
        catch (Exception e) {
            //Cool perm error!
        }
        cc = handler.update(admin, LABEL, newName, desc);
        assertEquals(LABEL, cc.getLabel());
        assertEquals(newName, cc.getName());
        assertEquals(desc, cc.getDescription());
        assertEquals(admin.getOrg(), cc.getOrg());
        try {
            String name = RandomStringUtils.randomAlphanumeric(
                    ConfigChannelCreationHelper.MAX_NAME_LENGTH + 1);
            cc = handler.update(admin, LABEL, name, DESCRIPTION);
            String msg = "Max length reached for name- not detected :(";
            fail(msg);
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
            //Cool invalid check works!..
        }
    }

    @Test
    public void testListGlobal() throws Exception {
        ConfigChannel cc = ConfigTestUtils.createConfigChannel(admin.getOrg());
        ConfigTestUtils.giveUserChanAccess(regular, cc);
        List<ConfigChannelDto> list = handler.listGlobals(regular);
        assertTrue(contains(cc, list));
    }

    @Test
    public void testLookupGlobal() throws Exception {
        List<String> channelLabels = new LinkedList<>();
        List<ConfigChannel> channels = new LinkedList<>();

        for (int i = 0; i < 10; i++) {
            ConfigChannel cc = ConfigTestUtils.createConfigChannel(admin.getOrg());
            ConfigTestUtils.giveUserChanAccess(regular, cc);
            channels.add(cc);
            channelLabels.add(cc.getLabel());
        }

        List<ConfigChannel> list = handler.lookupChannelInfo(regular,
                                                                    channelLabels);
        assertEquals(channels, list);
    }

    @Test
    public void testGetDetailsByLabel() throws Exception {
        ConfigChannel cc = ConfigTestUtils.createConfigChannel(admin.getOrg());

        ConfigTestUtils.giveUserChanAccess(regular, cc);

        ConfigChannel channel = handler.getDetails(regular, cc.getLabel());

        assertEquals(channel, cc);
    }

    @Test
    public void testGetDetailsById() throws Exception {
        ConfigChannel cc = ConfigTestUtils.createConfigChannel(admin.getOrg());

        ConfigTestUtils.giveUserChanAccess(regular, cc);

        ConfigChannel channel = handler.getDetails(regular, cc.getId().intValue());

        assertEquals(channel, cc);
    }

    @Test
    public void testDelete() {
        ConfigChannel cc = handler.create(admin, LABEL, NAME, DESCRIPTION);
        List<String> labels = new LinkedList<>();
        labels.add(cc.getLabel());
        List<ConfigChannel> channels = handler.lookupChannelInfo(admin, labels);
        assertEquals(1, channels.size());
        handler.deleteChannels(admin, labels);
        try {
            handler.lookupChannelInfo(admin, labels);
            fail("Lookup exception not raised!");
        }
        catch (NoSuchChannelException e) {
            // Cool could not find the item!..
        }
     }

    @Test
    public void testUpdateInitSls() {

        ConfigChannel cc = handler.create(admin, LABEL, NAME, DESCRIPTION, "state");
        assertEquals(LABEL, cc.getLabel());
        assertEquals(NAME, cc.getName());
        assertEquals(DESCRIPTION, cc.getDescription());
        assertEquals(admin.getOrg(), cc.getOrg());
        assertEquals(ConfigChannelType.state(), cc.getConfigChannelType());
        ConfigFile initSls = cc.getConfigFiles().first();
        assertEquals("/init.sls", initSls.getConfigFileName().getPath());
        assertTrue(initSls.getLatestConfigRevision().getConfigContent().getContentsString().isEmpty());
        Map<String, Object> data = new HashMap<>();
        String newContents = "new contents";
        data.put("contents", newContents);
        handler.updateInitSls(admin, cc.getLabel(), data);
        assertEquals(newContents, initSls.getLatestConfigRevision().getConfigContent().getContentsString());

    }

    @Test
    public void testSyncSaltFiles() {

        // Remove all channels
        var deleted = removeAllGlobals();
        assertEquals(1, deleted);

        // Create a normal channel with a file attached
        ConfigChannel channel = handler.create(admin, LABEL, NAME, DESCRIPTION, "normal");
        ConfigFile file = ConfigTestUtils.createConfigFile(channel);
        ConfigTestUtils.createConfigRevision(file);

        assertEquals(LABEL, channel.getLabel());
        assertEquals(NAME, channel.getName());
        assertEquals(DESCRIPTION, channel.getDescription());
        assertEquals(admin.getOrg(), channel.getOrg());
        assertEquals(ConfigChannelType.normal(), channel.getConfigChannelType());
        assertEquals(1, channel.getConfigFiles().size());
        assertNotNull(file.getLatestConfigRevision());

        // Remove files from the disk only
        ConfigTestUtils.removeChannelFiles(channel);
        assertFalse(ConfigTestUtils.lookUpChannelFiles(channel));

        // Sync files on the disk.
        handler.syncSaltFilesOnDisk(admin, List.of(channel.getLabel()));
        assertTrue(ConfigTestUtils.lookUpChannelFiles(channel));

    }

    @Test
    public void testSyncSaltFilesNoChannels() {

        // Remove all channels.
        var deleted = removeAllGlobals();
        assertEquals(1, deleted);

        // Attempt restoring files on the disk while there are no channels.
        var synced = handler.syncSaltFilesOnDisk(admin, List.of());
        assertEquals(1, synced);

    }

    /**
     * Deletes a list of  global channels.
     *
     * @return 1 if successful with the operation errors out otherwise.
     */
    private int removeAllGlobals() {
        var channels = handler.listGlobals(admin);
        var labels = channels.stream().map(ConfigChannelDto::getLabel).collect(Collectors.toList());
        return handler.deleteChannels(admin, labels);
    }

    /**
     * Checks if a given config channel is present in a list.
     * @param cc Config channel
     * @param list list of type COnfigChannelDto
     * @return true if the List contains it , false other wise
     */
    private boolean contains(ConfigChannel cc, List<ConfigChannelDto> list) {
        for (ConfigChannelDto dto : list) {
            if (dto.getLabel().equals(cc.getLabel())) {
                return true;
            }
        }
        return false;
    }

    private ConfigRevision createRevision(String path, String contents,
                            String group, String owner,
                            String perms, boolean isDir,
                            ConfigChannel cc, String selinuxCtx)
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
                            admin, cc.getLabel(), path, isDir, data);

        assertEquals(path, rev.getConfigFile().getConfigFileName().getPath());
        assertEquals(group, rev.getConfigInfo().getGroupname());
        assertEquals(owner, rev.getConfigInfo().getUsername());
        assertEquals(perms, String.valueOf(rev.getConfigInfo().getFilemode()));
        assertEquals(selinuxCtx, rev.getConfigInfo().getSelinuxCtx());
        if (isDir) {
            assertEquals(ConfigFileType.dir(), rev.getConfigFileType());
        }
        else if (ConfigFileType.file().equals(rev.getConfigFileType())) {
                assertEquals(contents, rev.getConfigContent().getContentsString());
                assertEquals(start, rev.getConfigContent().getDelimStart());
                assertEquals(end, rev.getConfigContent().getDelimEnd());
        }
        assertEquals(cc, rev.getConfigFile().getConfigChannel());

        assertRevNotChanged(rev, cc);

        return rev;
    }


    private ConfigRevision createSymlinkRevision(String path, String targetPath,
            ConfigChannel cc, String selinuxCtx)
                        throws ValidatorException {
        Map<String, Object> data = new HashMap<>();
        data.put(ConfigRevisionSerializer.TARGET_PATH, targetPath);
        data.put(ConfigRevisionSerializer.SELINUX_CTX, selinuxCtx);
        ConfigRevision rev = handler.createOrUpdateSymlink(admin,
                                                cc.getLabel(), path, data);
        assertEquals(path, rev.getConfigFile().getConfigFileName().getPath());
        assertEquals(ConfigFileType.symlink(), rev.getConfigFileType());
        assertEquals(targetPath, rev.getConfigInfo().getTargetFileName().getPath());
        assertEquals(selinuxCtx, rev.getConfigInfo().getSelinuxCtx());
        assertEquals(cc, rev.getConfigFile().getConfigChannel());

        assertRevNotChanged(rev, cc);

        return rev;
    }

    private void assertRev(ConfigRevision rev, String path, ConfigChannel cc) {
        List<String> paths = new ArrayList<>(1);
        paths.add(path);
        assertTrue(rev.matches(handler.lookupFileInfo(admin, cc.getLabel(), paths)
                    .get(0)));

    }

    private void assertRevNotChanged(ConfigRevision rev, ConfigChannel cc) {
        assertRev(rev, rev.getConfigFile().getConfigFileName().getPath(), cc);
    }

    @Test
    public void testAddPath() {
        ConfigChannel cc = handler.create(admin, LABEL, NAME, DESCRIPTION);

        String path = "/tmp/foo/path" + TestUtils.randomString();
        String contents = "HAHAHAHA";

        ConfigRevision rev = createRevision(path, contents,
                                    "group" + TestUtils.randomString(),
                                    "owner" + TestUtils.randomString(),
                                    "777",
                                    false, cc, "unconfined_u:object_r:tmp_t");
        try {
            createRevision(path, contents,
                    "group" + TestUtils.randomString(),
                    "owner" + TestUtils.randomString(),
                    "744",
                    true, cc, "unconfined_u:object_r:tmp_t");
            fail("Can't change the path from file to directory.");
        }
        catch (Exception e) {
            // Can;t change.. Won't allow...
            assertRevNotChanged(rev, cc);
        }

        try {
            createRevision(path + TestUtils.randomString() + "/" , contents,
                    "group" + TestUtils.randomString(),
                    "owner" + TestUtils.randomString(),
                    "744",
                    true, cc, "unconfined_u:object_r:tmp_t");
            fail("Validation error on the path.");
        }
        catch (Exception e) {
            // Can;t change.. Won't allow...
            assertRevNotChanged(rev, cc);
        }
        createRevision(path + TestUtils.randomString(), "",
                "group" + TestUtils.randomString(),
                "owner" + TestUtils.randomString(),
                "744",
                true, cc, "unconfined_u:object_r:tmp_t");

        createSymlinkRevision(path + TestUtils.randomString(),
                path + TestUtils.randomString(), cc, "root:root");
    }

    @Test
    public void testAddPathStateChannel() {
        ConfigChannel cc = handler.create(admin, LABEL, NAME, DESCRIPTION, "state");

        String path = "/tmp/foo/path" + TestUtils.randomString();
        String contents = "some contents";

        ConfigRevision rev = createRevision(path, contents,
                "group" + TestUtils.randomString(),
                "owner" + TestUtils.randomString(),
                "777",
                false, cc, "unconfined_u:object_r:tmp_t");

        //Path cannot end with "/"
        try {
            createRevision(path + TestUtils.randomString() + "/" , contents,
                    "group" + TestUtils.randomString(),
                    "owner" + TestUtils.randomString(),
                    "744",
                    false, cc, "unconfined_u:object_r:tmp_t");
            fail("Validation error on the path.");
        }
        catch (ConfigFileErrorException e) {
            // Can;t change.. Won't allow...
            assertRevNotChanged(rev, cc);
        }
        //Directories are not allowed for state channel
        try {
            createRevision(path + TestUtils.randomString(), "",
                    "group" + TestUtils.randomString(),
                    "owner" + TestUtils.randomString(),
                    "744",
                    true, cc, "unconfined_u:object_r:tmp_t");
            fail("InvalidOperationException exception not raised, **Directories/Symlinks are not " +
                    "supported in state channel.**");

        }
        catch (InvalidOperationException e) {

        }
        //Symlinks are not allowed for state channel
        try {
            createSymlinkRevision(path + TestUtils.randomString(),
                    path + TestUtils.randomString(), cc, "root:root");
            fail("InvalidOperationException exception not raised, **Directories/Symlinks are " +
                    "not supported in state channel.**");

        }
        catch (InvalidOperationException e) {

        }
        //init.sls should not be possible to update through createOrUpdatePath methods.
        path = "/init.sls";
        try {
            createRevision(path, "",
                    "group" + TestUtils.randomString(),
                    "owner" + TestUtils.randomString(),
                    "744",
                    false, cc, "unconfined_u:object_r:tmp_t");
            fail("Exception should be thrown, for init.sls file updateInitSls method should be used");
        }
        catch (InvalidParameterException e) {

        }


    }

    @Test
    public void testListFiles() {
        ConfigChannel cc = handler.create(admin, LABEL, NAME, DESCRIPTION);

        List<String> paths = new LinkedList<>();
        Map<String, ConfigRevision> revisions = new HashMap<>();

        setupPathsAndRevisions(cc, paths, revisions);

        List<ConfigFileDto> files = handler.listFiles(admin, LABEL);
        for (ConfigFileDto dto : files) {
            assertTrue(revisions.containsKey(dto.getPath()));
            ConfigRevision rev = revisions.get(dto.getPath());
            assertEquals(rev.getConfigFileType().getLabel(), dto.getType());
            assertNotNull(dto.getModified());
        }
    }

    /**
     * @param cc the channel
     * @param paths a list holder for paths
     * @param revisions a holder of revisions
     */
    private void setupPathsAndRevisions(ConfigChannel cc, List<String> paths,
            Map<String, ConfigRevision> revisions) {
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
                                                    isDir, cc,
                                                    "unconfined_u:object_r:tmp_t"));
        }
    }

    @Test
    public void testRemovePaths() {
        ConfigChannel cc = handler.create(admin, LABEL, NAME, DESCRIPTION);
        List<String> paths = new LinkedList<>();
        Map<String, ConfigRevision> revisions = new HashMap<>();

        setupPathsAndRevisions(cc, paths, revisions);
        paths.remove(paths.size() - 1);
        handler.deleteFiles(admin, LABEL, paths);
        List<ConfigFileDto> files = handler.listFiles(admin, LABEL);
        assertEquals(1, files.size());
    }

    @Test
    public void testScheduleFileComparisons() {
        Server server = ServerFactoryTest.createTestServer(admin, true);

        ConfigChannel cc = handler.create(admin, LABEL, NAME, DESCRIPTION);

        // create a config file
        String path = "/tmp/foo/path" + TestUtils.randomString();
        String contents = "HAHAHAHA";
        ConfigRevision rev = createRevision(path, contents,
                                    "group" + TestUtils.randomString(),
                                    "owner" + TestUtils.randomString(),
                                    "777",
                                    false, cc, "unconfined_u:object_r:tmp_t");

        DataResult dr = ActionManager.recentlyScheduledActions(admin, null, 30);
        int preScheduleSize = dr.size();

        // schedule file comparison action
        List<Integer> serverIds = new ArrayList<>();
        serverIds.add(server.getId().intValue());

        Integer actionId = handler.scheduleFileComparisons(admin, LABEL, path,
                serverIds);

        // was the action scheduled?
        dr = ActionManager.recentlyScheduledActions(admin, null, 30);
        assertEquals(1, dr.size() - preScheduleSize);
        assertEquals(
                "Show differences between profiled config files and deployed config files",
                ((ScheduledAction)dr.get(0)).getTypeName());
        assertEquals(actionId, Integer.valueOf(
                ((ScheduledAction)dr.get(0)).getId().intValue()));
    }

    @Test
    public void testChannelExists() {
        handler.create(admin, LABEL, NAME, DESCRIPTION);

        int validChannel = handler.channelExists(admin, LABEL);
        int invalidChannel = handler.channelExists(admin, "dummy");

        assertEquals(validChannel, 1);
        assertEquals(invalidChannel, 0);
    }


    @Test
    public void testDeployAllSystems()  throws Exception {
        // Create  global config channels
        List<ConfigChannel> gccList = new ArrayList<>();
        ConfigChannel gcc1 = ConfigTestUtils.createConfigChannel(admin.getOrg(),
                ConfigChannelType.normal());
        ConfigChannel gcc2 = ConfigTestUtils.createConfigChannel(admin.getOrg(),
                ConfigChannelType.normal());
        gccList.add(gcc1);
        gccList.add(gcc2);

        Long ver = 2L;

        // gcc1 only
        Server srv1 = ServerFactoryTest.createTestServer(regular, true);

        srv1.subscribeConfigChannels(gccList, regular);
        ServerFactory.save(srv1);

        Map<Long, Set<ConfigRevision>> revisions =
                new HashMap<>();

        ConfigFile g1f1 = gcc1.createConfigFile(
                ConfigFileState.normal(), "/etc/foo1");
        store(revisions, gcc1.getId(), ConfigTestUtils.createConfigRevision(g1f1));

        ConfigurationFactory.commit(gcc1);

        ConfigFile g1f2 = gcc1.createConfigFile(
                ConfigFileState.normal(), "/etc/foo2");
        store(revisions, gcc1.getId(), ConfigTestUtils.createConfigRevision(g1f2));
        ConfigurationFactory.commit(gcc2);

        ConfigFile g2f2 = gcc2.createConfigFile(
                ConfigFileState.normal(), "/etc/foo4");
        store(revisions, gcc2.getId(), ConfigTestUtils.createConfigRevision(g2f2));
        ConfigurationFactory.commit(gcc2);

        ConfigFile g2f3 = gcc2.createConfigFile(
                ConfigFileState.normal(), "/etc/foo3");
        store(revisions, gcc2.getId(), ConfigTestUtils.createConfigRevision(g2f3));
        ConfigurationFactory.commit(gcc2);


        // System 1 - both g1f1 and g1f2 should deploy here
        List<Number> systems  = new ArrayList<>();
        systems.add(srv1.getId());
        Date date = new Date();

        try {
            // validate that system must have config deployment capability
            // in order to deploy config files... (e.g. rhncfg* pkgs installed)
            handler.deployAllSystems(regular, gcc1.getLabel(), date);

            fail("Shouldn't be permitted to deploy without config deploy capability.");
        }
        catch (Exception e) {
            // Success
        }

        SystemManagerTest.giveCapability(srv1.getId(),
                SystemManager.CAP_CONFIGFILES_DEPLOY, ver);

        handler.deployAllSystems(regular, gcc1.getLabel(), date);

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
        assertEquals(revisions.get(gcc1.getId()).size(),
                ca.getConfigRevisionActions().size());
        for (ConfigRevisionAction cra : ca.getConfigRevisionActions()) {
            assertTrue(revisions.get(gcc1.getId()).contains(cra.getConfigRevision()));
        }

    }

    @Test
    public void testListAssignedGroups() {
        String ccLabel1 = "CC-LABEL-1-" + TestUtils.randomString();
        String ccLabel2 = "CC-LABEL-2-" + TestUtils.randomString();

        ConfigChannel cc1 = handler.create(admin,
                ccLabel1,
                "CC NAME " + TestUtils.randomString(),
                "CC DESCRIPTION " + TestUtils.randomString(),
                "state");
        ConfigChannel cc2 = handler.create(admin,
                ccLabel2,
                "CC NAME " + TestUtils.randomString(),
                "CC DESCRIPTION " + TestUtils.randomString(),
                "state");

        ManagedServerGroup group1 = ServerGroupTestUtils.createManaged(admin);
        ManagedServerGroup group2 = ServerGroupTestUtils.createManaged(admin);

        group1.subscribeConfigChannels(Arrays.asList(cc1), admin);
        group2.subscribeConfigChannels(Arrays.asList(cc2, cc1), admin);

        List<ManagedServerGroup> systemGroups = handler.listAssignedSystemGroups(admin, ccLabel1);
        assertContains(systemGroups, group1);
        assertContains(systemGroups, group2);

        systemGroups = handler.listAssignedSystemGroups(admin, ccLabel2);
        assertFalse(systemGroups.contains(group1), "Unexpected group found");
        assertContains(systemGroups, group2);

        group1.subscribeConfigChannels(Arrays.asList(cc2), admin);
        group2.unsubscribeConfigChannels(Arrays.asList(cc1), admin);

        systemGroups = handler.listAssignedSystemGroups(admin, ccLabel1);
        assertContains(systemGroups, group1);
        assertFalse(systemGroups.contains(group2), "Unexpected group found");

        systemGroups = handler.listAssignedSystemGroups(admin, ccLabel2);
        assertContains(systemGroups, group1);
        assertContains(systemGroups, group2);
    }

    private void store(Map<Long, Set<ConfigRevision>> revisions, Long ccid,
            ConfigRevision crev) {
        if (!revisions.containsKey(ccid)) {
            revisions.put(ccid, new HashSet<>());
        }
        revisions.get(ccid).add(crev);
    }

}
