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
package com.suse.manager.webui.utils.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.frontend.context.Context;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import com.suse.manager.utils.SaltKeyUtils;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.manager.webui.services.test.TestSystemQuery;
import com.suse.manager.webui.utils.MinionActionUtils;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Tests for MinionActionUtils.
 */
public class MinionActionUtilsTest extends BaseTestCaseWithUser {

    private final SystemQuery systemQuery = new TestSystemQuery();
    private final SaltApi saltApi = new TestSaltApi();
    private final SaltUtils saltUtils = new SaltUtils(systemQuery, saltApi);

    @BeforeEach
    @Override
    public void setUp() throws Exception {
        super.setUp();
        saltUtils.setScriptsDir(Files.createTempDirectory("scripts"));
    }

    @AfterEach
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        Files.delete(saltUtils.getScriptsDir());
    }

    /**
     * Verify script is deleted in case all servers are finished (COMPLETED or FAILED).
     */
    @Test
    public void testCleanupScriptActions() throws Exception {

        SaltKeyUtils saltKeyUtils = new SaltKeyUtils(saltApi);
        MinionActionUtils minionActionUtils = new MinionActionUtils(saltApi, saltUtils);

        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        ServerAction sa = ActionFactoryTest.createServerAction(ServerFactoryTest.createTestServer(user), action);
        sa.setStatus(ActionFactory.STATUS_COMPLETED);
        action.addServerAction(sa);
        ServerAction sa2 = ActionFactoryTest.createServerAction(ServerFactoryTest.createTestServer(user), action);
        sa2.setStatus(ActionFactory.STATUS_FAILED);
        action.addServerAction(sa2);
        Path scriptFile = Files.createFile(saltUtils.getScriptPath(action.getId()));

        // Testing
        minionActionUtils.cleanupScriptActions();
        assertFalse(Files.exists(scriptFile));
    }

    /**
     * Verify script is deleted in case no Action is there at all.
     */
    @Test
    public void testCleanupScriptWithoutAction() throws Exception {
        SaltKeyUtils saltKeyUtils = new SaltKeyUtils(saltApi);
        MinionActionUtils minionActionUtils = new MinionActionUtils(saltApi, saltUtils);
        saltUtils.setScriptsDir(Files.createTempDirectory("scripts"));
        Path scriptFile = Files.createFile(saltUtils.getScriptPath(123456L));

        // Testing
        minionActionUtils.cleanupScriptActions();
        assertFalse(Files.exists(scriptFile));
    }

    /**
     * Verify script is not deleted as long as not all servers have finished (e.g. PICKED_UP).
     */
    @Test
    public void testCleanupScriptActionsPickedUp() throws Exception {
        SaltKeyUtils saltKeyUtils = new SaltKeyUtils(saltApi);
        MinionActionUtils minionActionUtils = new MinionActionUtils(saltApi, saltUtils);
        saltUtils.setScriptsDir(Files.createTempDirectory("scripts"));
        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        ServerAction sa = ActionFactoryTest.createServerAction(ServerFactoryTest.createTestServer(user), action);
        sa.setStatus(ActionFactory.STATUS_PICKED_UP);
        action.addServerAction(sa);
        ServerAction sa2 = ActionFactoryTest.createServerAction(ServerFactoryTest.createTestServer(user), action);
        sa2.setStatus(ActionFactory.STATUS_COMPLETED);
        action.addServerAction(sa2);
        Path scriptFile = Files.createFile(saltUtils.getScriptPath(action.getId()));

        // Testing
        minionActionUtils.cleanupScriptActions();
        assertTrue(Files.exists(scriptFile));

        // Cleanup
        FileUtils.deleteFile(scriptFile);
    }

    @Test
    public void canRetrieveEarliestDate() {
        Instant checkInstant = Instant.now().minus(1, ChronoUnit.SECONDS);
        Context.getCurrentContext().setTimezone(TimeZone.getDefault());

        Date scheduleDate = MinionActionUtils.getScheduleDate(Optional.empty());
        assertNotNull(scheduleDate);
        assertTrue(scheduleDate.toInstant().isAfter(checkInstant));

        LocalDateTime earliestDateTime = LocalDate.of(2023, Month.JANUARY, 25).atTime(11, 30);
        Date expectedDate = Date.from(
            earliestDateTime.atZone(Context.getCurrentContext().getTimezone().toZoneId()).toInstant()
        );

        scheduleDate = MinionActionUtils.getScheduleDate(Optional.of(earliestDateTime));
        assertNotNull(scheduleDate);
        assertEquals(expectedDate, scheduleDate);
    }

    @Test
    public void canRetrieveActionChain() {
        String testLabel = "testActionChain_" + RandomStringUtils.randomAlphanumeric(8);
        ActionChain existingActionChain = ActionChainFactory.createActionChain(testLabel, user);
        assertNotNull(existingActionChain);

        // Empty value
        ActionChain actionChain = MinionActionUtils.getActionChain(Optional.empty(), user);
        assertNull(actionChain);

        // Retrieve existing
        actionChain = MinionActionUtils.getActionChain(Optional.of(testLabel), user);
        assertNotNull(actionChain);
        assertEquals(existingActionChain, actionChain);

        // Create new one
        String newLabel = "testNewChain_" + RandomStringUtils.randomAlphanumeric(8);
        actionChain = MinionActionUtils.getActionChain(Optional.of(newLabel), user);
        assertNotNull(actionChain);
        assertEquals(newLabel, actionChain.getLabel());
    }

}
