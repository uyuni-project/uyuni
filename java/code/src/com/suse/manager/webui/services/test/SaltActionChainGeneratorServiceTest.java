/*
 * Copyright (c) 2018 SUSE LLC
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

package com.suse.manager.webui.services.test;

import static com.suse.manager.webui.services.SaltActionChainGeneratorService.ACTIONCHAIN_SLS_FOLDER;
import static com.suse.manager.webui.services.SaltActionChainGeneratorService.ACTION_STATE_ID_PREFIX;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.webui.services.SaltActionChainGeneratorService;
import com.suse.manager.webui.utils.SaltModuleRun;
import com.suse.manager.webui.utils.SaltState;
import com.suse.manager.webui.utils.SaltSystemReboot;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;

public class SaltActionChainGeneratorServiceTest extends BaseTestCaseWithUser {

    @Test
    public void testCreateActionChainSLSFilesOneChunk() throws Exception {
        String label = TestUtils.randomString();

        ActionChain actionChain = ActionChainFactory.createActionChain(label, user);

        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        SystemManager.giveCapability(minion1.getId(), SystemManager.CAP_SCRIPT_RUN, 1L);

        MinionSummary minionSummary1 = new MinionSummary(minion1);

        List<SaltState> states = new ArrayList<>();
        states.add(new SaltModuleRun(
                ACTION_STATE_ID_PREFIX + actionChain.getId() + "_action_" + 1,
                "state.apply",
                1,
                singletonMap("mods", "remotecommands"),
                singletonMap("pillar",
                        // Use a TreeMap to keep the keys order or they may break the assert
                        new TreeMap<String, String>() {{
                                put("mgr_remote_cmd_script", "salt://scripts/script_1.sh");
                                put("mgr_remote_cmd_runas", "foobar");
                        }}
                )
        ));
        states.add(new SaltModuleRun(
                ACTION_STATE_ID_PREFIX + actionChain.getId() + "_action_" + 2,
                "state.apply",
                2,
                null,
                null
        ));

        Path stateFilesRoot = Files.createTempDirectory("actionchaingentest");

        SaltActionChainGeneratorService service = new SaltActionChainGeneratorService();
        service.setSuseManagerStatesFilesRoot(stateFilesRoot);
        service.setSkipSetOwner(true);
        service.createActionChainSLSFiles(actionChain, minionSummary1, states, Optional.empty());

        String fileContent = FileUtils
                .readFileToString(stateFilesRoot
                        .resolve(ACTIONCHAIN_SLS_FOLDER)
                        .resolve(service
                                .getActionChainSLSFileName(actionChain.getId(), minionSummary1, 1))
                        .toFile());
        assertEquals(("mgr_actionchain_131_action_1_chunk_1:\n" +
                        "    mgrcompat.module_run:\n" +
                        "    -   name: state.apply\n" +
                        "    -   mods: remotecommands\n" +
                        "    -   kwargs:\n" +
                        "            pillar:\n" +
                        "                mgr_remote_cmd_runas: foobar\n" +
                        "                mgr_remote_cmd_script: salt://scripts/script_1.sh\n" +
                        "mgr_actionchain_131_action_2_chunk_1:\n" +
                        "    mgrcompat.module_run:\n" +
                        "    -   name: state.apply\n" +
                        "    -   require:\n" +
                        "        -   mgrcompat: mgr_actionchain_131_action_1_chunk_1\n")
                        .replaceAll("131", actionChain.getId() + ""),
                fileContent);

        assertFalse(stateFilesRoot
                .resolve(ACTIONCHAIN_SLS_FOLDER)
                .resolve(service
                        .getActionChainSLSFileName(actionChain.getId(), minionSummary1, 2))
                .toFile().exists());
    }

    @Test
    public void testCreateActionChainSLSFilesTwoChunks() throws Exception {
        String label = TestUtils.randomString();

        ActionChain actionChain = ActionChainFactory.createActionChain(label, user);

        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        MinionSummary minionSummary1 = new MinionSummary(minion1);

        SystemManager.giveCapability(minion1.getId(), SystemManager.CAP_SCRIPT_RUN, 1L);

        List<SaltState> states = new ArrayList<>();
        states.add(new SaltModuleRun(
                ACTION_STATE_ID_PREFIX + actionChain.getId() + "_action_" + 1,
                "state.apply",
                1,
                singletonMap("mods", "remotecommands"),
                singletonMap("pillar",
                        // Use a TreeMap to keep the keys order or they may break the assert
                        new TreeMap<String, String>() {{
                                put("mgr_remote_cmd_script", "salt://scripts/script_1.sh");
                                put("mgr_remote_cmd_runas", "foobar");
                        }}
                )
        ));
        states.add(new SaltSystemReboot(
                ACTION_STATE_ID_PREFIX + actionChain.getId() + "_action_" + 2,
                2,
                1
        ));
        states.add(new SaltModuleRun(
                ACTION_STATE_ID_PREFIX + actionChain.getId() + "_action_" + 3,
                "state.apply",
                3,
                null,
                null
        ));

        Path stateFilesRoot = Files.createTempDirectory("actionchaingentest");

        SaltActionChainGeneratorService service = new SaltActionChainGeneratorService();
        service.setSuseManagerStatesFilesRoot(stateFilesRoot);
        service.setSkipSetOwner(true);
        service.createActionChainSLSFiles(actionChain, minionSummary1, states, Optional.empty());

        String fileContent = FileUtils
                .readFileToString(stateFilesRoot
                        .resolve(ACTIONCHAIN_SLS_FOLDER)
                        .resolve(service
                                .getActionChainSLSFileName(actionChain.getId(), minionSummary1, 1))
                        .toFile());
        assertEquals(("mgr_actionchain_131_action_1_chunk_1:\n" +
                        "    mgrcompat.module_run:\n" +
                        "    -   name: state.apply\n" +
                        "    -   mods: remotecommands\n" +
                        "    -   kwargs:\n" +
                        "            pillar:\n" +
                        "                mgr_remote_cmd_runas: foobar\n" +
                        "                mgr_remote_cmd_script: salt://scripts/script_1.sh\n" +
                        "mgr_actionchain_131_action_2_chunk_1:\n" +
                        "    mgrcompat.module_run:\n" +
                        "    -   name: system.reboot\n" +
                        "    -   at_time: 1\n" +
                        "    -   require:\n" +
                        "        -   mgrcompat: mgr_actionchain_131_action_1_chunk_1\n" +
                        "schedule_next_chunk:\n" +
                        "    mgrcompat.module_run:\n" +
                        "    -   name: mgractionchains.next\n" +
                        "    -   actionchain_id: 131\n" +
                        "    -   chunk: 2\n" +
                        "    -   next_action_id: 3\n" +
                        "    -   require:\n" +
                        "        -   mgrcompat: mgr_actionchain_131_action_2_chunk_1\n")
                        .replaceAll("131", actionChain.getId() + ""),
                fileContent);
        fileContent = FileUtils
                .readFileToString(stateFilesRoot
                        .resolve(ACTIONCHAIN_SLS_FOLDER)
                        .resolve(service
                                .getActionChainSLSFileName(actionChain.getId(), minionSummary1, 2))
                        .toFile());
        assertEquals(("mgr_actionchain_131_action_3_chunk_2:\n" +
                "    mgrcompat.module_run:\n" +
                "    -   name: state.apply\n").replaceAll("131", actionChain.getId() + ""), fileContent);
    }

    @Test
    public void testCreateActionChainSLSFilesOneChunksTransactionalUpdate() throws Exception {
        String label = TestUtils.randomString();

        ActionChain actionChain = ActionChainFactory.createActionChain(label, user);

        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        minion1.setOs(ServerConstants.SLEMICRO);
        minion1.setRelease("5.5");
        MinionSummary minionSummary1 = new MinionSummary(minion1);

        SystemManager.giveCapability(minion1.getId(), SystemManager.CAP_SCRIPT_RUN, 1L);

        List<SaltState> states = new ArrayList<>();
        states.add(new SaltModuleRun(
                ACTION_STATE_ID_PREFIX + actionChain.getId() + "_action_" + 1,
                "state.apply",
                1,
                singletonMap("mods", "remotecommands"),
                singletonMap("pillar",
                        // Use a TreeMap to keep the keys order or they may break the assert
                        new TreeMap<String, String>() {{
                                put("mgr_remote_cmd_script", "salt://scripts/script_1.sh");
                                put("mgr_remote_cmd_runas", "foobar");
                        }}
                )
        ));
        states.add(new SaltSystemReboot(
                ACTION_STATE_ID_PREFIX + actionChain.getId() + "_action_" + 2,
                2,
                1
        ));

        Path stateFilesRoot = Files.createTempDirectory("actionchaingentest");

        SaltActionChainGeneratorService service = new SaltActionChainGeneratorService();
        service.setSuseManagerStatesFilesRoot(stateFilesRoot);
        service.setSkipSetOwner(true);
        service.createActionChainSLSFiles(actionChain, minionSummary1, states, Optional.empty());

        String fileContent = FileUtils
                .readFileToString(stateFilesRoot
                        .resolve(ACTIONCHAIN_SLS_FOLDER)
                        .resolve(service
                                .getActionChainSLSFileName(actionChain.getId(), minionSummary1, 1))
                        .toFile());
        assertEquals(("mgr_actionchain_131_action_1_chunk_1:\n" +
                        "    mgrcompat.module_run:\n" +
                        "    -   name: state.apply\n" +
                        "    -   mods: remotecommands\n" +
                        "    -   kwargs:\n" +
                        "            pillar:\n" +
                        "                mgr_remote_cmd_runas: foobar\n" +
                        "                mgr_remote_cmd_script: salt://scripts/script_1.sh\n" +
                        "schedule_next_chunk:\n" +
                        "    mgrcompat.module_run:\n" +
                        "    -   name: mgractionchains.clean\n" +
                        "    -   actionchain_id: 131\n" +
                        "    -   current_action_id: 2\n" +
                        "    -   reboot_required: true\n")
                        .replaceAll("131", actionChain.getId() + ""),
                fileContent);
    }

    @Test
    public void testCreateActionChainSLSFilesTwoChunksTransactionalUpdate() throws Exception {
        String label = TestUtils.randomString();

        ActionChain actionChain = ActionChainFactory.createActionChain(label, user);

        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        minion1.setOs(ServerConstants.SLEMICRO);
        minion1.setRelease("6.0");
        MinionSummary minionSummary1 = new MinionSummary(minion1);

        SystemManager.giveCapability(minion1.getId(), SystemManager.CAP_SCRIPT_RUN, 1L);

        List<SaltState> states = new ArrayList<>();
        states.add(new SaltModuleRun(
                ACTION_STATE_ID_PREFIX + actionChain.getId() + "_action_" + 1,
                "state.apply",
                1,
                singletonMap("mods", "remotecommands"),
                singletonMap("pillar",
                        // Use a TreeMap to keep the keys order or they may break the assert
                        new TreeMap<String, String>() {{
                                put("mgr_remote_cmd_script", "salt://scripts/script_1.sh");
                                put("mgr_remote_cmd_runas", "foobar");
                        }}
                )
        ));
        states.add(new SaltSystemReboot(
                ACTION_STATE_ID_PREFIX + actionChain.getId() + "_action_" + 2,
                2,
                1
        ));
        states.add(new SaltModuleRun(
                ACTION_STATE_ID_PREFIX + actionChain.getId() + "_action_" + 3,
                "state.apply",
                3,
                null,
                null
        ));

        Path stateFilesRoot = Files.createTempDirectory("actionchaingentest");

        SaltActionChainGeneratorService service = new SaltActionChainGeneratorService();
        service.setSuseManagerStatesFilesRoot(stateFilesRoot);
        service.setSkipSetOwner(true);
        service.createActionChainSLSFiles(actionChain, minionSummary1, states, Optional.empty());

        String fileContent = FileUtils
                .readFileToString(stateFilesRoot
                        .resolve(ACTIONCHAIN_SLS_FOLDER)
                        .resolve(service
                                .getActionChainSLSFileName(actionChain.getId(), minionSummary1, 1))
                        .toFile());
        assertEquals(("mgr_actionchain_131_action_1_chunk_1:\n" +
                        "    mgrcompat.module_run:\n" +
                        "    -   name: state.apply\n" +
                        "    -   mods: remotecommands\n" +
                        "    -   kwargs:\n" +
                        "            pillar:\n" +
                        "                mgr_remote_cmd_runas: foobar\n" +
                        "                mgr_remote_cmd_script: salt://scripts/script_1.sh\n" +
                        "schedule_next_chunk:\n" +
                        "    mgrcompat.module_run:\n" +
                        "    -   name: mgractionchains.next\n" +
                        "    -   actionchain_id: 131\n" +
                        "    -   chunk: 2\n" +
                        "    -   next_action_id: 3\n" +
                        "    -   current_action_id: 2\n" +
                        "    -   reboot_required: true\n" +
                        "    -   require:\n" +
                        "        -   mgrcompat: mgr_actionchain_131_action_1_chunk_1\n")
                        .replaceAll("131", actionChain.getId() + ""),
                fileContent);
        fileContent = FileUtils
                .readFileToString(stateFilesRoot
                        .resolve(ACTIONCHAIN_SLS_FOLDER)
                        .resolve(service
                                .getActionChainSLSFileName(actionChain.getId(), minionSummary1, 2))
                        .toFile());
        assertEquals(("mgr_actionchain_131_action_3_chunk_2:\n" +
                "    mgrcompat.module_run:\n" +
                "    -   name: state.apply\n").replaceAll("131", actionChain.getId() + ""), fileContent);
    }


    @Test
    public void testCreateActionChainSLSFilesSaltUpgrade() throws Exception {
        String label = TestUtils.randomString();
        ActionChain actionChain = ActionChainFactory.createActionChain(label, user);

        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        MinionSummary minionSummary1 = new MinionSummary(minion1);
        SystemManager.giveCapability(minion1.getId(), SystemManager.CAP_SCRIPT_RUN, 1L);

        List<SaltState> states = new ArrayList<>();
        states.add(new SaltModuleRun(
                ACTION_STATE_ID_PREFIX + actionChain.getId() + "_action_" + 1,
                "state.apply",
                1,
                singletonMap("mods", Arrays.asList("packages.pkginstall")),
                singletonMap("pillar",
                        singletonMap("param_pkgs",
                            Arrays.asList(
                                    Arrays.asList("salt", "x86_64", "2018.3.0-4.1"),
                                    Arrays.asList("salt-minion", "x86_64", "2018.3.0-4.1")
                            )
                        )
                )
        ));

        Path stateFilesRoot = Files.createTempDirectory("actionchaingentest");

        SaltActionChainGeneratorService service = new SaltActionChainGeneratorService();
        service.setSuseManagerStatesFilesRoot(stateFilesRoot);
        service.setSkipSetOwner(true);
        service.createActionChainSLSFiles(actionChain, minionSummary1, states, Optional.empty());

        String fileContent = FileUtils
                .readFileToString(stateFilesRoot
                        .resolve(ACTIONCHAIN_SLS_FOLDER)
                        .resolve(service
                                .getActionChainSLSFileName(actionChain.getId(), minionSummary1, 1))
                        .toFile());

        assertEquals(("schedule_next_chunk:\n" +
                        "    mgrcompat.module_run:\n" +
                        "    -   name: mgractionchains.next\n" +
                        "    -   actionchain_id: 142\n" +
                        "    -   chunk: 2\n" +
                        "mgr_actionchain_142_action_1_chunk_1:\n" +
                        "    mgrcompat.module_run:\n" +
                        "    -   name: state.apply\n" +
                        "    -   mods:\n" +
                        "        - packages.pkginstall\n" +
                        "    -   kwargs:\n" +
                        "            pillar:\n" +
                        "                param_pkgs:\n" +
                        "                -   - salt\n" +
                        "                    - x86_64\n" +
                        "                    - 2018.3.0-4.1\n" +
                        "                -   - salt-minion\n" +
                        "                    - x86_64\n" +
                        "                    - 2018.3.0-4.1\n" +
                        "clean_action_chain_if_previous_failed:\n" +
                        "    mgrcompat.module_run:\n" +
                        "    -   name: mgractionchains.clean\n" +
                        "    -   onfail:\n" +
                        "        -   mgrcompat: mgr_actionchain_142_action_1_chunk_1\n" +
                        "force_restart_if_needed:\n" +
                        "    cmd.script:\n" +
                        "    -   source: salt://actionchains/force_restart_minion.sh\n" +
                        "    -   require:\n" +
                        "        -   mgrcompat: mgr_actionchain_142_action_1_chunk_1\n")
                        .replaceAll("142", actionChain.getId() + ""),
                fileContent);

        fileContent = FileUtils
                .readFileToString(stateFilesRoot
                        .resolve(ACTIONCHAIN_SLS_FOLDER)
                        .resolve(service
                                .getActionChainSLSFileName(actionChain.getId(), minionSummary1, 2))
                        .toFile());

        assertEquals(("pkg_installed:\n" +
                        "    pkg.installed:\n" +
                        "    -   refresh: true\n" +
                        "    -   pkgs:\n" +
                        "        -   salt.x86_64: 2018.3.0-4.1\n" +
                        "        -   salt-minion.x86_64: 2018.3.0-4.1\n"), fileContent);
    }

    @Test
    public void testRemoveAllActionChainSLSFilesForMinion() throws Exception {
        String label = TestUtils.randomString();
        ActionChain actionChain = ActionChainFactory.createActionChain(label, user);
        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        MinionSummary minionSummary1 = new MinionSummary(minion1);

        Path stateFilesRoot = Files.createTempDirectory("actionchaingentest");
        SaltActionChainGeneratorService service = new SaltActionChainGeneratorService();
        service.setSuseManagerStatesFilesRoot(stateFilesRoot);
        service.setSkipSetOwner(true);

        List<SaltState> states = new ArrayList<>();
        states.add(new SaltModuleRun(
                ACTION_STATE_ID_PREFIX + actionChain.getId() + "_action_" + 1,
                "state.apply",
                1,
                singletonMap("mods", "remotecommands"),
                singletonMap("pillar",
                        new HashMap<String, String>() {{
                                put("mgr_remote_cmd_script", "salt://scripts/script_1.sh");
                                put("mgr_remote_cmd_runas", "foobar");
                        }}
                )
        ));
        states.add(new SaltSystemReboot(
                ACTION_STATE_ID_PREFIX + actionChain.getId() + "_action_" + 2,
                2,
                1
        ));
        states.add(new SaltModuleRun(
                "schedule_next_chunk",
                "mgractionchains.next",
                0,
                emptyMap(),
                singletonMap("pillar",
                        new HashMap<String, String>() {{
                                put("actionchain_id:", "35");
                                put("chunk", "2");
                                put("next_action_id", "397");
                                put("ssh_extra_filerefs",
                                        "salt://scripts/script_1.sh,salt://scripts/script_3.sh,salt://channels," +
                                        service.getActionChainSLSFileName(actionChain.getId(), minionSummary1, 2));
                        }}
                )
        ));
        states.add(new SaltModuleRun(
                ACTION_STATE_ID_PREFIX + actionChain.getId() + "_action_" + 3,
                "state.apply",
                3,
                singletonMap("mods", "remotecommands"),
                singletonMap("pillar",
                        new HashMap<String, String>() {{
                                put("mgr_remote_cmd_script", "salt://scripts/script_3.sh");
                                put("mgr_remote_cmd_runas", "foobar");
                        }}
                )
        ));

        File channels = stateFilesRoot.resolve("channels").toFile();
        Path scriptsDir = stateFilesRoot.resolve("scripts");
        File script1 = scriptsDir.resolve("script_1.sh").toFile();
        File script2 = scriptsDir.resolve("script_3.sh").toFile();

        FileUtils.touch(script1);
        FileUtils.touch(script2);
        FileUtils.touch(channels);

        service.createActionChainSLSFiles(actionChain, minionSummary1, states, Optional.empty());

        service.removeActionChainSLSFilesForMinion(minion1.getMachineId(), Optional.empty());

        Path sls1Path = stateFilesRoot
                .resolve(ACTIONCHAIN_SLS_FOLDER)
                .resolve(service.getActionChainSLSFileName(actionChain.getId(), minionSummary1, 1));
        File sls1 = sls1Path.toFile();

        Path sls2Path = stateFilesRoot
                .resolve(ACTIONCHAIN_SLS_FOLDER)
                .resolve(service.getActionChainSLSFileName(actionChain.getId(), minionSummary1, 2));
        File sls2 = sls2Path.toFile();

        assertFalse(sls1.exists());
        assertFalse(script1.exists());
        assertFalse(script2.exists());
        assertFalse(sls2.exists());
        assertTrue(channels.exists());
    }

    @Test
    public void testPkgInstallationSLSFiles() throws Exception {
        String label = TestUtils.randomString();
        ActionChain actionChain = ActionChainFactory.createActionChain(label, user);

        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        MinionSummary minionSummary1 = new MinionSummary(minion1);
        SystemManager.giveCapability(minion1.getId(), SystemManager.CAP_SCRIPT_RUN, 1L);

        List<SaltState> states = new ArrayList<>();
        states.add(new SaltModuleRun(
                ACTION_STATE_ID_PREFIX + actionChain.getId() + "_action_" + 1,
                "state.apply",
                1,
                singletonMap("mods", Arrays.asList("packages.pkginstall")),
                singletonMap("pillar",
                        singletonMap("param_pkgs",
                            Arrays.asList(
                                    Arrays.asList("dummy-package", "0.1.2-3"),
                                    Arrays.asList("another-package", "4.3.2-1")
                            )
                        )
                )
        ));

        Path stateFilesRoot = Files.createTempDirectory("actionchaingentest");

        SaltActionChainGeneratorService service = new SaltActionChainGeneratorService();
        service.setSuseManagerStatesFilesRoot(stateFilesRoot);
        service.setSkipSetOwner(true);
        service.createActionChainSLSFiles(actionChain, minionSummary1, states, Optional.empty());

        String fileContent = FileUtils
                .readFileToString(stateFilesRoot
                        .resolve(ACTIONCHAIN_SLS_FOLDER)
                        .resolve(service
                                .getActionChainSLSFileName(actionChain.getId(), minionSummary1, 1))
                        .toFile());

        assertEquals(("mgr_actionchain_142_action_1_chunk_1:\n" +
                        "    mgrcompat.module_run:\n" +
                        "    -   name: state.apply\n" +
                        "    -   mods:\n" +
                        "        - packages.pkginstall\n" +
                        "    -   kwargs:\n" +
                        "            pillar:\n" +
                        "                param_pkgs:\n" +
                        "                -   - dummy-package\n" +
                        "                    - 0.1.2-3\n" +
                        "                -   - another-package\n" +
                        "                    - 4.3.2-1\n"
                ).replaceAll("142", actionChain.getId() + ""),
                fileContent);
    }

    @Test
    public void testParseActionChainStateId() throws Exception {
        SaltActionChainGeneratorService service = new SaltActionChainGeneratorService();
        Optional<SaltActionChainGeneratorService.ActionChainStateId> result = service.parseActionChainStateId(
                "mgrcompat_|-mgr_actionchain_144_action_854_chunk_1_|-state.apply_|-module_run");
        assertTrue(result.isPresent());
        assertEquals(result.get().getActionChainId(), 144);
        assertEquals(result.get().getActionId(), 854);
        assertEquals(result.get().getChunk(), 1);

        result = service.parseActionChainStateId(
                "mgrcompat_|-mgr_actioncin_144_action_854_chunk_1_|-state.apply_|-module_run");
        assertFalse(result.isPresent());

        result = service.parseActionChainStateId(
                "mgrcompat_|-mgr_actionchain_144_action_chunk_1_|-state.apply_|-module_run");
        assertFalse(result.isPresent());
    }

    @Test
    public void testRemoveActionChainSLSFiles() throws Exception {
        String label = TestUtils.randomString();

        ActionChain actionChain = ActionChainFactory.createActionChain(label, user);

        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        MinionSummary minionSummary1 = new MinionSummary(minion1);
        SystemManager.giveCapability(minion1.getId(), SystemManager.CAP_SCRIPT_RUN, 1L);

        Path statesFileRoot = Files.createTempDirectory("actionchaingentest");
        Path scriptsDir = statesFileRoot.resolve("scripts");
        Files.createDirectory(scriptsDir);

        SaltActionChainGeneratorService service = new SaltActionChainGeneratorService();
        service.setSuseManagerStatesFilesRoot(statesFileRoot);
        service.setSkipSetOwner(true);

        List<SaltState> states = new ArrayList<>();
        states.add(new SaltModuleRun(
                ACTION_STATE_ID_PREFIX + actionChain.getId() + "_action_" + 1,
                "state.apply",
                1,
                singletonMap("mods", "remotecommands"),
                singletonMap("pillar",
                        new HashMap<String, String>() {{
                                put("mgr_remote_cmd_script", "salt://scripts/script_1.sh");
                                put("mgr_remote_cmd_runas", "foobar");
                        }}
                )
        ));
        states.add(new SaltModuleRun(ACTION_STATE_ID_PREFIX + actionChain.getId() + "_action_" + 2,
                "state.top",
                2,
                singletonMap("topfn",
                        service.getActionChainTopPath(actionChain.getId(), 2)),
                emptyMap()));
        String sls2Name = "salt://" + ACTIONCHAIN_SLS_FOLDER + "/" +
                service.getActionChainSLSFileName(actionChain.getId(), minionSummary1, 2);
        states.add(new SaltModuleRun(
                "schedule_next_chunk",
                "mgractionchains.next",
                0,
                emptyMap(),
                singletonMap("pillar",
                        new HashMap<String, String>() {{
                                put("actionchain_id:", "35");
                                put("chunk", "2");
                                put("next_action_id", "397");
                                put("ssh_extra_filerefs", "salt://scripts/script_2.sh,salt://channels," + sls2Name);
                        }}
                )
        ));

        service.createActionChainSLSFiles(actionChain, minionSummary1, states, Optional.empty());

        File script1 = scriptsDir.resolve("script_1.sh").toFile();
        File script2 = scriptsDir.resolve("script_2.sh").toFile();
        String topFilename = service.getActionChainTopPath(actionChain.getId(), 2);
        File top = statesFileRoot.resolve(topFilename).toFile();
        File channels = scriptsDir.resolve("channels").toFile();
        File sls2 = statesFileRoot
                .resolve(ACTIONCHAIN_SLS_FOLDER)
                .resolve(service.getActionChainSLSFileName(actionChain.getId(), minionSummary1, 2))
                .toFile();
        FileUtils.touch(script1);
        FileUtils.touch(script2);
        FileUtils.touch(top);
        FileUtils.touch(channels);
        FileUtils.touch(sls2);

        Path sls1Path = statesFileRoot
                .resolve(ACTIONCHAIN_SLS_FOLDER)
                .resolve(service.getActionChainSLSFileName(actionChain.getId(), minionSummary1, 1));
        File sls1 = sls1Path.toFile();

        List<String> slsFileRefs = service.findFileRefsToDelete(sls1Path);
        assertEquals(2, slsFileRefs.size());
        assertTrue(slsFileRefs.contains("scripts/script_1.sh"));
        assertTrue(slsFileRefs.contains(topFilename));

        service.removeActionChainSLSFiles(actionChain.getId(), minion1.getMinionId(), 1, false);

        assertFalse(sls1.exists());
        assertFalse(script1.exists());
        assertFalse(top.exists());
        assertTrue(script2.exists());
        assertTrue(sls2.exists());
        assertTrue(channels.exists());
    }


}
