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
package com.redhat.rhn.manager.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.TransactionalMode;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.webui.services.SaltParameters;
import com.suse.manager.webui.services.TransactionalUpdateCalls;
import com.suse.manager.webui.utils.salt.LocalCallWithExecutors;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ActionManagerSaltCallsTest {

    @Test
    public void testPrepareSaltCallsSeparatesRegularAndTransactionalMinions() {
        MinionSummary regularMinion = new MinionSummary(1L, "regular", null, null, null, "SLES", false);
        MinionSummary transactionalMinion = new MinionSummary(2L, "transactional", null, null, null, "SLES", true);
        LocalCall<?> call = State.apply(
                List.of(ApplyStatesEventMessage.SYSTEM_INFO), Optional.empty());

        Map<LocalCall<?>, List<MinionSummary>> calls =
                ActionManager.prepareSaltCalls(Map.of(call, List.of(regularMinion, transactionalMinion)));

        assertEquals(2, calls.size());
        assertTrue(calls.entrySet().stream()
                .anyMatch(entry -> !entry.getKey().getPayload().containsKey("module_executors") &&
                        entry.getValue().equals(List.of(regularMinion))));
        assertTrue(calls.entrySet().stream()
                .anyMatch(entry -> List.of("direct_call").equals(entry.getKey().getPayload().get("module_executors")) &&
                        entry.getValue().equals(List.of(transactionalMinion))));
    }

    @Test
    public void testPrepareSaltCallsPreservesRecipientsForSharedExplicitExecutor() {
        MinionSummary regularMinion = new MinionSummary(1L, "regular", null, null, null, "SLES", false);
        MinionSummary transactionalMinion = new MinionSummary(2L, "transactional", null, null, null, "SLES", true);
        List<MinionSummary> minions = new ArrayList<>(List.of(regularMinion, transactionalMinion));
        LocalCall<?> call = new LocalCallWithExecutors<>(
                State.apply(List.of(ApplyStatesEventMessage.SYSTEM_INFO), Optional.empty()),
                List.of("direct_call"),
                Map.of());

        Map<LocalCall<?>, List<MinionSummary>> input = new HashMap<>();
        input.put(call, minions);
        Map<LocalCall<?>, List<MinionSummary>> inputCopy = new HashMap<>(input);
        List<MinionSummary> expectedMinions = List.copyOf(minions);
        Map<LocalCall<?>, List<MinionSummary>> result = ActionManager.prepareSaltCalls(input);

        assertEquals(expectedMinions, result.get(call));
        assertEquals(expectedMinions, minions);
        assertEquals(inputCopy, input);
        assertSame(minions, input.get(call));
    }

    @Test
    public void testPrepareSaltCallsPreservesRecipientsForOtherSharedExplicitExecutor() {
        MinionSummary regularMinion = new MinionSummary(1L, "regular", null, null, null, "SLES", false);
        MinionSummary transactionalMinion = new MinionSummary(2L, "transactional", null, null, null, "SLES", true);
        List<MinionSummary> minions = List.of(regularMinion, transactionalMinion);
        LocalCall<?> call = new LocalCallWithExecutors<>(
                State.apply(List.of(ApplyStatesEventMessage.SYSTEM_INFO), Optional.empty()),
                List.of("sudo"),
                Map.of("timeout", 30));

        Map<LocalCall<?>, List<MinionSummary>> result = ActionManager.prepareSaltCalls(Map.of(call, minions));

        assertEquals(minions, result.get(call));
        assertEquals(List.of("sudo"), result.keySet().iterator().next().getPayload().get("module_executors"));
    }

    @Test
    public void testPrepareSaltCallsPreservesRecipientsForPreparedTransactionalUpdate() {
        MinionSummary regularMinion = new MinionSummary(1L, "regular", null, null, null, "SLES", false);
        MinionSummary transactionalMinion = new MinionSummary(2L, "transactional", null, null, null, "SLES", true);
        List<MinionSummary> minions = List.of(regularMinion, transactionalMinion);
        LocalCall<?> call = TransactionalUpdateCalls.apply(
                List.of(SaltParameters.PACKAGES_PKGINSTALL),
                Optional.of(Map.of("key", "value")),
                Optional.of(true),
                Optional.empty());

        Map<LocalCall<?>, List<MinionSummary>> result = ActionManager.prepareSaltCalls(Map.of(call, minions));

        assertEquals(minions, result.get(call));
        assertEquals("transactional_update.apply", result.keySet().iterator().next().getPayload().get("fun"));
    }

    @Test
    public void testPrepareSaltCallsPreservesEmptyTargetLists() {
        LocalCall<?> call = State.apply(List.of(SaltParameters.PACKAGES_PKGINSTALL), Optional.empty());

        Map<LocalCall<?>, List<MinionSummary>> calls =
                ActionManager.prepareSaltCalls(Map.of(call, List.of()));

        assertEquals(1, calls.size());
        assertEquals(List.of(), calls.get(call));
    }

    /**
     * The persisted observation of the transactional grain, and not the operating system name,
     * must drive the routing. Both minions below are classified against their OS based fallback.
     */
    @Test
    public void testPrepareSaltCallsRoutesAccordingToTheObservedStatus() {
        MinionSummary observedTransactional = new MinionSummary(1L, "sles-transactional", null, null, null,
                ServerConstants.SLES, TransactionalMode.TRANSACTIONAL);
        MinionSummary observedConventional = new MinionSummary(2L, "micro-conventional", null, null, null,
                ServerConstants.SLEMICRO, TransactionalMode.NON_TRANSACTIONAL);
        List<String> states = List.of(SaltParameters.PACKAGES_PKGINSTALL);
        LocalCall<?> call = State.apply(states, Optional.empty());

        Map<LocalCall<?>, List<MinionSummary>> calls = ActionManager.prepareSaltCalls(
                Map.of(call, List.of(observedTransactional, observedConventional)));

        assertEquals(2, calls.size());

        LocalCall<?> transactionalCall = findCallByFun(calls, "transactional_update.apply");
        assertEquals(List.of(observedTransactional), calls.get(transactionalCall),
                "only the minion observed as transactional must be routed to transactional_update.apply");
        assertEquals(states, ((Map<?, ?>) transactionalCall.getPayload().get("kwarg")).get("mods"));

        LocalCall<?> regularCall = findCallByFun(calls, "state.apply");
        assertEquals(List.of(observedConventional), calls.get(regularCall),
                "the minion observed as conventional must keep the regular state.apply");
        assertEquals(states, ((Map<?, ?>) regularCall.getPayload().get("kwarg")).get("mods"));
        assertNull(regularCall.getPayload().get("module_executors"));

        assertEquals(2, calls.values().stream().mapToLong(List::size).sum(), "no recipient may be lost");
    }

    /**
     * A state that has no transactional counterpart stays a live operation, executed with the
     * direct_call executor, also when the classification comes from the observed grain.
     */
    @Test
    public void testPrepareSaltCallsKeepsDirectCallForUnmappedStateOnObservedTransactional() {
        MinionSummary observedTransactional = new MinionSummary(1L, "sles-transactional", null, null, null,
                ServerConstants.SLES, TransactionalMode.TRANSACTIONAL);
        MinionSummary observedConventional = new MinionSummary(2L, "micro-conventional", null, null, null,
                ServerConstants.SLEMICRO, TransactionalMode.NON_TRANSACTIONAL);
        List<String> states = List.of(ApplyStatesEventMessage.SYSTEM_INFO);
        LocalCall<?> call = State.apply(states, Optional.empty());

        Map<LocalCall<?>, List<MinionSummary>> calls = ActionManager.prepareSaltCalls(
                Map.of(call, List.of(observedTransactional, observedConventional)));

        assertEquals(2, calls.size());

        LocalCall<?> liveCall = calls.keySet().stream()
                .filter(c -> List.of("direct_call").equals(c.getPayload().get("module_executors")))
                .findFirst()
                .orElseThrow();
        assertEquals("state.apply", liveCall.getPayload().get("fun"));
        assertEquals(List.of(observedTransactional), calls.get(liveCall));
        assertEquals(states, ((Map<?, ?>) liveCall.getPayload().get("kwarg")).get("mods"));

        LocalCall<?> regularCall = calls.keySet().stream()
                .filter(c -> c.getPayload().get("module_executors") == null)
                .findFirst()
                .orElseThrow();
        assertEquals(List.of(observedConventional), calls.get(regularCall));

        assertEquals(2, calls.values().stream().mapToLong(List::size).sum(), "no recipient may be lost");
    }

    private static LocalCall<?> findCallByFun(Map<LocalCall<?>, List<MinionSummary>> calls, String fun) {
        return calls.keySet().stream()
                .filter(call -> fun.equals(call.getPayload().get("fun")))
                .findFirst()
                .orElseThrow();
    }
}
