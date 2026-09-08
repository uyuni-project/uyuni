/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.reactor.messaging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.MinionTransactionalActionHistory;
import com.redhat.rhn.domain.server.MinionTransactionalActionHistory.ProgressStatus;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.action.TransactionalActionManager;
import com.suse.manager.webui.services.SaltServerActionService;

import org.jmock.Expectations;
import org.jmock.api.Invocation;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.jmock.lib.action.CustomAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Tests for {@link ResumeTransactionalActionEventMessageAction}.
 */
public class ResumeTransactionalActionEventMessageActionTest extends JMockBaseTestCaseWithUser {

    private SaltServerActionService saltServerActionService;

    @BeforeEach
    public void setUp() {
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
        saltServerActionService = mock(SaltServerActionService.class);
    }

    @Test
    public void testPendingPrerequisiteIsConfirmedBeforeScheduling() {
        Scenario scenario = createScenario();

        context().checking(new Expectations() {{
            oneOf(saltServerActionService).resumeTransactionalAction(
                    with(any(ApplyStatesAction.class)), with(any(List.class)));
            will(new CustomAction("verify prerequisite before scheduling") {
                @Override
                public Object invoke(Invocation invocation) {
                    MinionTransactionalActionHistory history = findHistory(scenario);
                    assertEquals(ProgressStatus.COMPLETED, history.getPrerequisiteStatus());
                    return Map.of(true, invocation.getParameter(1), false, List.of());
                }
            });
        }});

        execute(scenario);

        MinionTransactionalActionHistory history = findHistory(scenario);
        assertEquals(ProgressStatus.COMPLETED, history.getPrerequisiteStatus());
        assertEquals(ProgressStatus.SCHEDULED, history.getAfterRebootStatus());
        assertNotNull(history.getPrerequisiteAt());
        assertNull(history.getRebootAt());
    }

    @Test
    public void testSchedulingFailureKeepsConfirmedPrerequisite() {
        Scenario scenario = createScenario();

        context().checking(new Expectations() {{
            oneOf(saltServerActionService).resumeTransactionalAction(
                    with(any(ApplyStatesAction.class)), with(any(List.class)));
            will(returnValue(Map.of(true, List.of(), false, List.of())));
        }});

        execute(scenario);

        MinionTransactionalActionHistory history = findHistory(scenario);
        assertEquals(ProgressStatus.COMPLETED, history.getPrerequisiteStatus());
        assertEquals(ProgressStatus.FAILED, history.getAfterRebootStatus());
        assertNull(history.getRebootAt());
    }

    @Test
    public void testMissingTargetKeepsConfirmedPrerequisite() {
        MinionServer targetMinion = MinionServerFactoryTest.createTestMinionServer(user);
        Scenario scenario = createScenario();
        MinionTransactionalActionHistory missingTargetHistory =
                MinionTransactionalActionHistory.create(targetMinion.getId(), scenario.actionId());
        missingTargetHistory.setPostTransactionalFormulaList(List.of("locale"));
        TestUtils.persist(missingTargetHistory);
        TestUtils.flushAndClearSession();

        context().checking(new Expectations() {{
            never(saltServerActionService).resumeTransactionalAction(
                    with(any(ApplyStatesAction.class)), with(any(List.class)));
        }});

        execute(new Scenario(scenario.actionId(), targetMinion.getId()));

        MinionTransactionalActionHistory history = findHistory(new Scenario(
                scenario.actionId(), targetMinion.getId()));
        assertEquals(ProgressStatus.COMPLETED, history.getPrerequisiteStatus());
        assertEquals(ProgressStatus.FAILED, history.getAfterRebootStatus());
        assertNull(history.getRebootAt());
    }

    @Test
    public void testFailedPrerequisiteDoesNotScheduleContinuation() {
        Scenario scenario = createScenario();
        MinionTransactionalActionHistory history = findHistory(scenario);
        history.recordTransactionalApplyFailed("transactional failure");
        TestUtils.flushSession();

        context().checking(new Expectations() {{
            never(saltServerActionService).resumeTransactionalAction(
                    with(any(ApplyStatesAction.class)), with(any(List.class)));
        }});

        execute(scenario);

        history = findHistory(scenario);
        assertEquals(ProgressStatus.FAILED, history.getPrerequisiteStatus());
        assertEquals(ProgressStatus.NOT_NEEDED, history.getAfterRebootStatus());
    }

    @Test
    public void testCompletedPrerequisiteTimestampAndResultArePreserved() {
        Scenario scenario = createScenario();
        MinionTransactionalActionHistory history = findHistory(scenario);
        history.recordTransactionalStateApplied("transactional result");
        var prerequisiteAt = history.getPrerequisiteAt();
        TestUtils.flushSession();

        context().checking(new Expectations() {{
            oneOf(saltServerActionService).resumeTransactionalAction(
                    with(any(ApplyStatesAction.class)), with(any(List.class)));
            will(returnValue(Map.of(true, List.of(), false, List.of())));
        }});

        execute(scenario);

        history = findHistory(scenario);
        assertEquals(ProgressStatus.COMPLETED, history.getPrerequisiteStatus());
        assertEquals(prerequisiteAt, history.getPrerequisiteAt());
        assertEquals("transactional result", history.getPrerequisiteResult());
    }

    @Test
    public void testPostTransactionalFormulaListRoundTripsThroughHibernate() {
        List<String> formulas = List.of(
                "locale,custom",
                "formula with spaces",
                "formula\"quoted",
                "formulá-日",
                "locale,custom");
        Scenario scenario = createScenario(formulas);

        MinionTransactionalActionHistory history = findHistory(scenario);

        assertEquals(formulas, history.getPostTransactionalFormulaList());
        assertThrows(UnsupportedOperationException.class,
                () -> history.getPostTransactionalFormulaList().add("another-formula"));
    }

    @Test
    public void testEmptyPostTransactionalFormulaListIsPersistedAsNull() {
        Scenario scenario = createScenario(List.of());

        Object storedValue = HibernateFactory.getSession()
                .createNativeQuery("""
                        SELECT post_transactional_formulas
                          FROM suseTransactionalActionHistory
                         WHERE minion_server_id = :serverId
                           AND action_id = :actionId
                        """)
                .setParameter("serverId", scenario.serverId())
                .setParameter("actionId", scenario.actionId())
                .getSingleResult();

        assertNull(storedValue);
        assertEquals(List.of(), findHistory(scenario).getPostTransactionalFormulaList());
    }

    private Scenario createScenario() {
        return createScenario(List.of("locale"));
    }

    private Scenario createScenario(List<String> formulas) {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        ApplyStatesAction action = ActionManager.scheduleApplyStates(
                user, List.of(minion.getId()), List.of("formulas"), new Date());
        MinionTransactionalActionHistory history =
                MinionTransactionalActionHistory.create(minion.getId(), action.getId());
        history.setPostTransactionalFormulaList(formulas);
        TestUtils.persist(history);
        TestUtils.flushAndClearSession();
        return new Scenario(action.getId(), minion.getId());
    }

    private void execute(Scenario scenario) {
        new ResumeTransactionalActionEventMessageAction(saltServerActionService).execute(
                new ResumeTransactionalActionEventMessage(scenario.actionId(), scenario.serverId()));
    }

    private MinionTransactionalActionHistory findHistory(Scenario scenario) {
        return TransactionalActionManager.findTransactionalActionHistory(
                scenario.serverId(), scenario.actionId()).orElseThrow();
    }

    private record Scenario(Long actionId, Long serverId) {
    }
}
