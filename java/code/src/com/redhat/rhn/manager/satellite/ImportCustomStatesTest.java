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

package com.redhat.rhn.manager.satellite;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.config.ConfigChannelType;
import com.redhat.rhn.domain.config.ConfigContent;
import com.redhat.rhn.domain.config.ConfigFile;
import com.redhat.rhn.domain.config.ConfigFileState;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.task.Task;
import com.redhat.rhn.domain.task.TaskFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ConfigTestUtils;
import com.suse.manager.webui.services.ConfigChannelSaltManager;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static com.suse.manager.webui.services.SaltConstants.ORG_STATES_DIRECTORY_PREFIX;

/**
 * Tests importing the legacy custom states contents into the custom channels.
 */
public class ImportCustomStatesTest extends BaseTestCaseWithUser {

    private Path legacyStatesBackupDirectory;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        createTask(UpgradeCommand.UPGRADE_CUSTOM_STATES);
        legacyStatesBackupDirectory = Files.createTempDirectory("legacy_states");
    }

    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        assertTaskDoesNotExist(UpgradeCommand.UPGRADE_CUSTOM_STATES);
        FileUtils.deleteDirectory(legacyStatesBackupDirectory.toFile());
    }

    /**
     * Tests that when various tasks are queued, they are also cleaned up after the
     * upgrade process.
     */
    public void testMultipleTasksDone() {
        createTask(UpgradeCommand.UPGRADE_KS_PROFILES);

        UpgradeCommand command = new UpgradeCommand(tmpSaltRoot, legacyStatesBackupDirectory);
        command.upgrade();

        assertTaskDoesNotExist(UpgradeCommand.UPGRADE_KS_PROFILES);
    }

    /**
     * Tests that a single legacy custom state is imported to the database.
     *
     * @throws IOException if anything goes wrong
     */
    public void testProcessCustomStates() throws IOException {
        ConfigChannelSaltManager.getInstance().setBaseDirPath(tmpSaltRoot.toAbsolutePath().toString());

        ConfigChannel stateChannel = createTestStateChannel();
        ConfigFile configFile = stateChannel.createConfigFile(ConfigFileState.normal(), "/init.sls");
        ConfigTestUtils.createConfigRevision(configFile, 1L);

        Path statePath = tmpSaltRoot
                .resolve(ORG_STATES_DIRECTORY_PREFIX + user.getOrg().getId())
                .resolve(stateChannel.getLabel() + ".sls");
        statePath.toFile().getParentFile().mkdirs();

        String stateContents = "my-state:\n    ....";
        FileUtils.writeStringToFile(statePath.toFile(), stateContents);

        assertEquals(
                "",
                configFile.getLatestConfigRevision().getConfigContent().getContentsString());

        UpgradeCommand cmd = new UpgradeCommand(tmpSaltRoot, legacyStatesBackupDirectory);
        cmd.upgrade();

        assertEquals(Long.valueOf(1L), configFile.getLatestConfigRevision().getRevision());
        assertEquals(
                stateContents,
                configFile.getLatestConfigRevision().getConfigContent().getContentsString());

        Path channelDirectory = tmpSaltRoot
                .resolve(ORG_STATES_DIRECTORY_PREFIX + user.getOrg().getId())
                .resolve(stateChannel.getLabel());
        File stateFile = channelDirectory.resolve("init.sls").toFile();
        assertTrue(channelDirectory.toFile().isDirectory());
        assertTrue(stateFile.exists());
        assertEquals(stateContents, FileUtils.readFileToString(stateFile));
        assertFalse(statePath.toFile().exists());
    }

    /**
     * Tests single legacy state import test passes even if there are multiple upgrade tasks
     * in the queue.
     *
     * @throws IOException if anything goes wrong
     */
    public void testMultipleTasksInQueue() throws IOException {
        // create one task here, another one is created in setUp
        createTask(UpgradeCommand.UPGRADE_CUSTOM_STATES);
        testProcessCustomStates();
    }

    /**
     * Tests that custom state content is not overwritten by the import in case it already
     * exists.
     *
     * @throws IOException if anything goes wrong
     */
    public void testProcessCustomStatesContentExistsInDb() throws IOException {
        ConfigChannel stateChannel = createTestStateChannel();
        ConfigFile configFile = stateChannel.createConfigFile(ConfigFileState.normal(), "/init.sls");
        ConfigRevision configRevision = ConfigTestUtils.createConfigRevision(configFile, 1L);
        String originalContent = "test-content";
        configRevision.getConfigContent().setContents(originalContent.getBytes());

        Path statePath = tmpSaltRoot
                .resolve(ORG_STATES_DIRECTORY_PREFIX + user.getOrg().getId())
                .resolve(stateChannel.getLabel() + ".sls");
        statePath.toFile().getParentFile().mkdirs();

        String stateContents = "my-state:\n    ....";
        FileUtils.writeStringToFile(statePath.toFile(), stateContents);
        UpgradeCommand cmd = new UpgradeCommand(tmpSaltRoot, legacyStatesBackupDirectory);
        cmd.upgrade();

        assertEquals(Long.valueOf(1L), configFile.getLatestConfigRevision().getRevision());
        assertEquals(
                originalContent,
                configFile.getLatestConfigRevision().getConfigContent().getContentsString());
    }

    /**
     * Tests that custom state content is not overwritten by the import in case it has already
     * more revisions than 1.
     *
     * @throws IOException if anything goes wrong
     */
    public void testProcessCustomStatesMoreRevisions() throws IOException {
        ConfigChannel stateChannel = createTestStateChannel();
        ConfigFile configFile = stateChannel.createConfigFile(ConfigFileState.normal(), "/init.sls");
        ConfigRevision configRevision = ConfigTestUtils.createConfigRevision(configFile, 2L);
        ConfigContent configContent = ConfigTestUtils.createConfigContent();
        configContent.setContents("original_content".getBytes());
        configRevision.setConfigContent(configContent);

        Path statePath = tmpSaltRoot
                .resolve(ORG_STATES_DIRECTORY_PREFIX + user.getOrg().getId())
                .resolve(stateChannel.getLabel() + ".sls");
        statePath.toFile().getParentFile().mkdirs();

        String stateContents = "my-state:\n    ....";
        FileUtils.writeStringToFile(statePath.toFile(), stateContents);

        UpgradeCommand cmd = new UpgradeCommand(tmpSaltRoot, legacyStatesBackupDirectory);
        cmd.upgrade();

        assertEquals(Long.valueOf(2L), configFile.getLatestConfigRevision().getRevision());
        assertEquals(
                "original_content",
                configFile.getLatestConfigRevision().getConfigContent().getContentsString());
    }

    /**
     * Tests that custom file content in a 'normal' channel is not overwritten by the import
     * even if the legacy custom state name matches to the channel name.
     *
     * @throws IOException if anything goes wrong
     */
    public void testProcessCustomStatesNormalChannelSkipped() throws IOException {
        ConfigChannel normalChannel = ConfigTestUtils.createConfigChannel(user.getOrg(), ConfigChannelType.normal());
        ConfigFile configFile = normalChannel.createConfigFile(ConfigFileState.normal(), "/init.sls");
        ConfigRevision configRevision = ConfigTestUtils.createConfigRevision(configFile, 1L);
        ConfigContent configContent = ConfigTestUtils.createConfigContent();
        configContent.setContents("original_content".getBytes());
        configRevision.setConfigContent(configContent);

        Path statePath = tmpSaltRoot
                .resolve(ORG_STATES_DIRECTORY_PREFIX + user.getOrg().getId())
                .resolve(normalChannel.getLabel() + ".sls");
        statePath.toFile().getParentFile().mkdirs();

        String stateContents = "my-state:\n    ....";
        FileUtils.writeStringToFile(statePath.toFile(), stateContents);

        UpgradeCommand cmd = new UpgradeCommand(tmpSaltRoot, legacyStatesBackupDirectory);
        cmd.upgrade();

        assertEquals(Long.valueOf(1L), configFile.getLatestConfigRevision().getRevision());
        assertEquals(
                "original_content",
                configFile.getLatestConfigRevision().getConfigContent().getContentsString());
    }

    /**
     * Tests that import procedure fails when the legacy custom state file can't be found on
     * the disk.
     *
     * @throws IOException if anything goes wrong
     */
    public void testProcessCustomStatesMissingFile() {
        ConfigChannel stateChannel = createTestStateChannel();
        ConfigFile configFile = stateChannel.createConfigFile(ConfigFileState.normal(), "/init.sls");
        ConfigTestUtils.createConfigRevision(configFile, 1L);

        UpgradeCommand cmd = new UpgradeCommand(tmpSaltRoot, legacyStatesBackupDirectory);
        try {
            cmd.upgrade();
            fail("A runtime exception should have been thrown.");
        }
        catch (RuntimeException e) {
            assertEquals(Long.valueOf(1L), configFile.getLatestConfigRevision().getRevision());
            assertTrue(configFile.getLatestConfigRevision().getConfigContent()
                    .getContentsString().isEmpty());
            assertNotEmpty(TaskFactory.getTaskListByNameLike(UpgradeCommand.UPGRADE_CUSTOM_STATES));
        }
    }

    /**
     * Tests that the backing up and the cleanup parts of the process.
     *
     * @throws IOException if anything goes wrong
     */
    public void testBackupAndCleanup() throws IOException {
        assertTrue(legacyStatesBackupDirectory.toFile().list().length == 0);

        // this dir must be imported with all its contents
        File dir1 = tmpSaltRoot.resolve(ORG_STATES_DIRECTORY_PREFIX + "1").toFile();
        dir1.mkdirs();
        File state1 = dir1.toPath().resolve("customstate1.sls").toFile();
        state1.createNewFile();
        FileUtils.writeStringToFile(state1, "contents");
        File state2 = dir1.toPath().resolve("customstate2.sls").toFile();
        state2.createNewFile();
        FileUtils.writeStringToFile(state1, "contents2");

        // empty directory
        File dir2 = tmpSaltRoot.resolve(ORG_STATES_DIRECTORY_PREFIX + "87654321").toFile();
        dir2.mkdirs();

        // this dir must not be imported
        File noStatesHereDir = tmpSaltRoot.resolve(ORG_STATES_DIRECTORY_PREFIX + "no_org_id").toFile();
        noStatesHereDir.mkdirs();

        // legacy configuration channels directories
        Path cfgDir = tmpSaltRoot.resolve("mgr_cfg_org_123").resolve("config_channel_321");
        cfgDir.toFile().mkdirs();
        FileUtils.writeStringToFile(cfgDir.resolve("init.sls").toFile(), "state-content");

        UpgradeCommand cmd = new UpgradeCommand(tmpSaltRoot, legacyStatesBackupDirectory);
        cmd.upgrade();

        assertTrue(dir1.exists());
        assertFalse(state1.exists());
        assertFalse(state2.exists());
        File backedUpDir1 = legacyStatesBackupDirectory.resolve(dir1.getName()).toFile();
        assertTrue(backedUpDir1.exists());
        List<String> backedUpStateNames1 = Arrays.asList(backedUpDir1.list());
        assertEquals(2, backedUpStateNames1.size());
        assertContains(backedUpStateNames1, "customstate1.sls");
        assertContains(backedUpStateNames1, "customstate2.sls");

        assertTrue(dir2.exists());
        assertTrue(legacyStatesBackupDirectory.resolve(dir2.getName()).toFile().exists());

        assertTrue(noStatesHereDir.exists());
        assertFalse(legacyStatesBackupDirectory.resolve(noStatesHereDir.getName()).toFile().exists());

        assertFalse(cfgDir.toFile().exists());
    }

    private ConfigChannel createTestStateChannel() {
        ConfigChannel cc = ConfigTestUtils.createConfigChannel(user.getOrg(), ConfigChannelType.state());
        return (ConfigChannel) HibernateFactory.reload(cc);
    }

    private void createTask(String taskName) {
        TaskFactory.createTask(user.getOrg(), taskName, 0L);
        List l = TaskFactory.getTaskListByNameLike(taskName);
        assertTrue(l.get(0) instanceof Task);
    }

    private void assertTaskDoesNotExist(String taskName) {
        List<Task> tasks = TaskFactory.getTaskListByNameLike(taskName);
        assertTrue((tasks == null || tasks.isEmpty()));
    }
}
