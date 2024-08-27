/*
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

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.salt.PlaybookAction;
import com.redhat.rhn.domain.action.salt.PlaybookActionDetails;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.errata.cache.test.ErrataCacheManagerTest;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests for {@link ActionChainManager}.
 */
public class ActionChainManagerTest extends JMockBaseTestCaseWithUser {

    private static final TaskomaticApi TASKO_TEST_API = new TaskomaticApi() {
        @Override
        public void scheduleActionChainExecution(ActionChain actionchain) {
        }
        @Override
        public void scheduleActionExecution(Action action) {
        }
    };

    /**
     * {@inheritDoc}
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        ActionChainManager.setTaskomaticApi(TASKO_TEST_API);
    }

    /**
     * Tests schedulePackageUpgrades().
     * @throws Exception if something goes wrong
     */
    @Test
    public void testSchedulePackageUpgrades() throws Exception {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);

        Server s = ServerFactoryTest.createTestServer(user, true);

        Package p = PackageTest.createTestPackage(user.getOrg());

        Map<String, Long> packageMap = new HashMap<>();
        packageMap.put("name_id", p.getPackageName().getId());
        packageMap.put("evr_id", p.getPackageEvr().getId());
        packageMap.put("arch_id", p.getPackageArch().getId());
        Map<Long, List<Map<String, Long>>> packageMaps = new HashMap<>();
        packageMaps.put(s.getId(), singletonList(packageMap));

        List<Action> actions = ActionChainManager.schedulePackageUpgrades(user, packageMaps,
                new Date(), null);

        for (Action action : actions) {
            assertNotNull(action.getId());
            Action retrievedAction = ActionManager.lookupAction(user, action.getId());
            assertNotNull(retrievedAction);
            assertEquals(action, retrievedAction);
        }
    }

    /**
     * Tests scheduleErrataUpdates().
     * @throws Exception if something goes wrong
     */
    @Test
    public void testScheduleErrataUpdates() throws Exception {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);

        Map<String, Object> info =
                ErrataCacheManagerTest.createServerNeededCache(user,
                        ErrataFactory.ERRATA_TYPE_BUG);
        List<Integer> upgradePackages = new ArrayList<>();
        Server s = (Server) info.get("server");
        Errata e = (Errata) info.get("errata");

        List<Integer> errataIds = singletonList(e.getId().intValue());
        String label = TestUtils.randomString();
        ActionChain actionChain = ActionChainFactory.createActionChain(label, user);
        List<Long> actions = ActionChainManager.scheduleErrataUpdate(user, List.of(s),
                errataIds, new Date(), actionChain);

        assertEquals(1, actionChain.getEntries().size());

        assertSame(actionChain.getEntries().iterator().next().getAction().getId(),
                actions.iterator().next());
    }

    /**
     * Schedule Ansible playbook application for a control node.
     *
     * @throws Exception in case of an error
     */
    @Test
    public void testScheduleAnsiblePlaybook() throws Exception {
        Server server = ServerFactoryTest.createTestServer(user);
        Date earliestAction = new Date();
        PlaybookAction action = ActionChainManager.scheduleExecutePlaybook(user, server.getId(),
                "/path/to/myplaybook.yml", "/path/to/hosts", null, earliestAction, false, false);

        // Look it up and verify
        PlaybookAction savedAction = (PlaybookAction) ActionFactory.lookupByUserAndId(user, action.getId());
        assertNotNull(savedAction);
        assertEquals("Execute playbook 'myplaybook.yml'", savedAction.getName());
        assertEquals(ActionFactory.TYPE_PLAYBOOK, savedAction.getActionType());
        assertEquals(earliestAction, savedAction.getEarliestAction());

        // Verify the details
        PlaybookActionDetails details = savedAction.getDetails();
        assertNotNull(details);
        assertEquals("/path/to/myplaybook.yml", details.getPlaybookPath());
        assertEquals("/path/to/hosts", details.getInventoryPath());
        assertFalse(details.isTestMode());
        assertFalse(details.isFlushCache());
    }

    /**
     * Schedule Ansible playbook application in TEST mode for a control node.
     *
     * @throws Exception in case of an error
     */
    @Test
    public void testScheduleAnsiblePlaybookTestMode() throws Exception {
        Server server = ServerFactoryTest.createTestServer(user);
        Date earliestAction = new Date();
        PlaybookAction action = ActionChainManager.scheduleExecutePlaybook(user, server.getId(),
                "/path/to/myplaybook.yml", null, null, earliestAction, true, true);

        // Look it up and verify
        PlaybookAction savedAction = (PlaybookAction) ActionFactory.lookupByUserAndId(user, action.getId());
        assertNotNull(savedAction);
        assertEquals("Execute playbook 'myplaybook.yml'", savedAction.getName());
        assertEquals(ActionFactory.TYPE_PLAYBOOK, savedAction.getActionType());
        assertEquals(earliestAction, savedAction.getEarliestAction());

        // Verify the details
        PlaybookActionDetails details = savedAction.getDetails();
        assertNotNull(details);
        assertEquals("/path/to/myplaybook.yml", details.getPlaybookPath());
        assertNull(details.getInventoryPath());
        assertTrue(details.isTestMode());
        assertTrue(details.isFlushCache());
    }
}
