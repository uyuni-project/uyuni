/**
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
package com.suse.manager.webui.utils.test;

import java.nio.file.Files;
import java.nio.file.Path;

import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.utils.MinionActionUtils;

/**
 * Tests for MinionActionUtils.
 */
public class MinionActionUtilsTest extends BaseTestCaseWithUser {

    /**
     * Verify script is deleted in case all servers are finished (COMPLETED or FAILED).
     */
    public void testCleanupScriptActions() throws Exception {
        SaltUtils.INSTANCE.setScriptsDir(Files.createTempDirectory("scripts"));
        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        ServerAction sa = ActionFactoryTest.createServerAction(ServerFactoryTest.createTestServer(user), action);
        sa.setStatus(ActionFactory.STATUS_COMPLETED);
        action.addServerAction(sa);
        ServerAction sa2 = ActionFactoryTest.createServerAction(ServerFactoryTest.createTestServer(user), action);
        sa2.setStatus(ActionFactory.STATUS_FAILED);
        action.addServerAction(sa2);
        Path scriptFile = Files.createFile(SaltUtils.INSTANCE.getScriptPath(action.getId()));

        // Testing
        MinionActionUtils.cleanupScriptActions();
        assertFalse(Files.exists(scriptFile));

        // Cleanup
        Files.delete(SaltUtils.INSTANCE.getScriptsDir());
    }

    /**
     * Verify script is deleted in case no Action is there at all.
     */
    public void testCleanupScriptWithoutAction() throws Exception {
        SaltUtils.INSTANCE.setScriptsDir(Files.createTempDirectory("scripts"));
        Path scriptFile = Files.createFile(SaltUtils.INSTANCE.getScriptPath(123456L));

        // Testing
        MinionActionUtils.cleanupScriptActions();
        assertFalse(Files.exists(scriptFile));

        // Cleanup
        Files.delete(SaltUtils.INSTANCE.getScriptsDir());
    }

    /**
     * Verify script is not deleted as long as not all servers have finished (e.g. PICKED_UP).
     */
    public void testCleanupScriptActionsPickedUp() throws Exception {
        SaltUtils.INSTANCE.setScriptsDir(Files.createTempDirectory("scripts"));
        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        ServerAction sa = ActionFactoryTest.createServerAction(ServerFactoryTest.createTestServer(user), action);
        sa.setStatus(ActionFactory.STATUS_PICKED_UP);
        action.addServerAction(sa);
        ServerAction sa2 = ActionFactoryTest.createServerAction(ServerFactoryTest.createTestServer(user), action);
        sa2.setStatus(ActionFactory.STATUS_COMPLETED);
        action.addServerAction(sa2);
        Path scriptFile = Files.createFile(SaltUtils.INSTANCE.getScriptPath(action.getId()));

        // Testing
        MinionActionUtils.cleanupScriptActions();
        assertTrue(Files.exists(scriptFile));

        // Cleanup
        FileUtils.deleteFile(scriptFile);
        Files.delete(SaltUtils.INSTANCE.getScriptsDir());
    }
}
