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
package com.suse.manager.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.server.MinionSummary;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.webui.services.SaltParameters;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.utils.Xor;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TransactionalActionManagerTest {

    @Test
    public void testHardwareProfileUpdateUsesTransactionalPrerequisiteState() {
        LocalCall<?> call = TransactionalActionManager.getTransactionalSaltCall(
                ApplyStatesEventMessage.HARDWARE_PROFILE_UPDATE, Optional.empty());

        assertEquals("transactional_update.apply", call.getPayload().get("fun"));
        assertEquals(List.of(SaltParameters.HARDWARE_PROFILE_UPDATE_PREREQ),
                ((Map<?, ?>) call.getPayload().get("kwarg")).get("mods"));
    }

    @Test
    public void testUnknownStateExecutesWithDirectCall() {
        LocalCall<?> call = TransactionalActionManager.getTransactionalSaltCall(
                "unknown.state", Optional.empty());

        assertEquals("state.apply", call.getPayload().get("fun"));
        assertEquals(List.of("direct_call"), call.getPayload().get("module_executors"));
    }

    @Test
    public void testPatchInstallUsesTransactionalUpdate() {
        LocalCall<?> call = TransactionalActionManager.getTransactionalSaltCall(
                SaltParameters.PACKAGES_PATCHINSTALL, Optional.empty());

        assertEquals("transactional_update.apply", call.getPayload().get("fun"));
        assertEquals(List.of(SaltParameters.PACKAGES_PATCHINSTALL),
                ((Map<?, ?>) call.getPayload().get("kwarg")).get("mods"));
    }

    @Test
    public void testTransactionalUpdateApplyResultIsRecognizedAsTransactional() {
        assertTrue(TransactionalActionManager.isTransactionalResult(
                Optional.of(Xor.right("transactional_update.apply"))));
    }

    @Test
    public void testStateApplyResultIsNotRecognizedAsTransactional() {
        assertFalse(TransactionalActionManager.isTransactionalResult(
                Optional.of(Xor.right("state.apply"))));
    }

    @Test
    public void testPendingTransactionCheckStateIsRecognized() {
        assertTrue(TransactionalActionManager.isPendingTransactionCheck(
                Optional.of(List.of(ApplyStatesEventMessage.TRANSACTIONAL_PENDING_TRANSACTION))));
    }

    @Test
    public void testExtractStatesFromFunctionArgs() {
        Map<String, Object> kwargs = Map.of("mods",
                List.of(SaltParameters.HARDWARE_PROFILE_UPDATE_PREREQ));

        Optional<List<String>> states =
                TransactionalActionManager.getStatesFromFunctionArgs(List.of(kwargs));

        assertEquals(Optional.of(List.of(SaltParameters.HARDWARE_PROFILE_UPDATE_PREREQ)), states);
    }

    @Test
    public void testAddApplyCallsUsesTransactionalUpdateForTransactionalMinionsOnly() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary regularMinion = new MinionSummary(1L, "regular", null, null, null, "SLES", false);
        MinionSummary transactionalMinion = new MinionSummary(2L, "transactional", null, null, null, "SLES", true);

        TransactionalActionManager.addApplyCalls(
                calls,
                List.of(SaltParameters.PACKAGES_PKGREMOVE),
                Optional.empty(),
                List.of(regularMinion, transactionalMinion));

        assertEquals(2, calls.size());
        assertTrue(calls.entrySet().stream()
                .anyMatch(entry -> "state.apply".equals(entry.getKey().getPayload().get("fun")) &&
                        entry.getValue().equals(List.of(regularMinion))));
        assertTrue(calls.entrySet().stream()
                .anyMatch(entry -> "transactional_update.apply".equals(entry.getKey().getPayload().get("fun")) &&
                        entry.getValue().equals(List.of(transactionalMinion))));
    }

    @Test
    public void testAddApplyCallsExecutesUnmappedStateWithDirectCallForTransactionalMinions() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary regularMinion = new MinionSummary(1L, "regular", null, null, null, "SLES", false);
        MinionSummary transactionalMinion = new MinionSummary(2L, "transactional", null, null, null, "SLES", true);

        TransactionalActionManager.addApplyCalls(
                calls,
                List.of(ApplyStatesEventMessage.SYSTEM_INFO),
                Optional.empty(),
                List.of(regularMinion, transactionalMinion));

        assertEquals(2, calls.size());
        assertTrue(calls.entrySet().stream()
                .anyMatch(entry -> "state.apply".equals(entry.getKey().getPayload().get("fun")) &&
                        !entry.getKey().getPayload().containsKey("module_executors") &&
                        entry.getValue().equals(List.of(regularMinion))));
        assertTrue(calls.entrySet().stream()
                .anyMatch(entry -> "state.apply".equals(entry.getKey().getPayload().get("fun")) &&
                        List.of("direct_call").equals(entry.getKey().getPayload().get("module_executors")) &&
                        entry.getValue().equals(List.of(transactionalMinion))));
    }

    @Test
    public void testPrepareSaltCallsExecutesWithDirectCallForTransactionalMinions() {
        MinionSummary regularMinion = new MinionSummary(1L, "regular", null, null, null, "SLES", false);
        MinionSummary transactionalMinion = new MinionSummary(2L, "transactional", null, null, null, "SLES", true);
        LocalCall<?> call = com.suse.salt.netapi.calls.modules.State.apply(
                List.of(ApplyStatesEventMessage.SYSTEM_INFO), Optional.empty());

        Map<LocalCall<?>, List<MinionSummary>> calls =
                TransactionalActionManager.prepareSaltCallsForTransactionalMinions(Map.of(
                        call, List.of(regularMinion, transactionalMinion)));

        assertEquals(2, calls.size());
        assertTrue(calls.entrySet().stream()
                .anyMatch(entry -> !entry.getKey().getPayload().containsKey("module_executors") &&
                        entry.getValue().equals(List.of(regularMinion))));
        assertTrue(calls.entrySet().stream()
                .anyMatch(entry -> List.of("direct_call").equals(entry.getKey().getPayload().get("module_executors")) &&
                        entry.getValue().equals(List.of(transactionalMinion))));
    }

    @Test
    public void testPrepareSaltCallExecutesWithDirectCallForTransactionalMinion() {
        MinionSummary transactionalMinion = new MinionSummary(2L, "transactional", null, null, null, "SLES", true);
        LocalCall<?> call = com.suse.salt.netapi.calls.modules.State.apply(
                List.of(ApplyStatesEventMessage.SYSTEM_INFO), Optional.empty());

        LocalCall<?> preparedCall = TransactionalActionManager.prepareSaltCallForTransactionalMinions(
                call, List.of(transactionalMinion));

        assertEquals("state.apply", preparedCall.getPayload().get("fun"));
        assertEquals(List.of("direct_call"), preparedCall.getPayload().get("module_executors"));
    }

    @Test
    public void testPrepareSaltCallUsesTransactionalUpdateForMappedState() {
        MinionSummary transactionalMinion = new MinionSummary(2L, "transactional", null, null, null, "SLES", true);
        LocalCall<?> call = com.suse.salt.netapi.calls.modules.State.apply(
                List.of(ApplyStatesEventMessage.CHANNELS),
                Optional.of(Map.of("key", "value")),
                Optional.of(true),
                Optional.empty());

        LocalCall<?> preparedCall = TransactionalActionManager.prepareSaltCallForTransactionalMinions(
                call, List.of(transactionalMinion));

        assertEquals("transactional_update.apply", preparedCall.getPayload().get("fun"));
        Map<?, ?> kwargs = (Map<?, ?>) preparedCall.getPayload().get("kwarg");
        assertEquals(List.of(ApplyStatesEventMessage.CHANNELS), kwargs.get("mods"));
        assertEquals(Map.of("key", "value"), kwargs.get("pillar"));
        assertEquals(true, kwargs.get("queue"));
    }

    @Test
    public void testPrepareSaltCallExecutesMixedStatesWithDirectCall() {
        MinionSummary transactionalMinion = new MinionSummary(2L, "transactional", null, null, null, "SLES", true);
        LocalCall<?> call = com.suse.salt.netapi.calls.modules.State.apply(
                List.of(ApplyStatesEventMessage.CHANNELS, ApplyStatesEventMessage.SYSTEM_INFO), Optional.empty());

        LocalCall<?> preparedCall = TransactionalActionManager.prepareSaltCallForTransactionalMinions(
                call, List.of(transactionalMinion));

        assertEquals("state.apply", preparedCall.getPayload().get("fun"));
        assertEquals(List.of("direct_call"), preparedCall.getPayload().get("module_executors"));
    }

    @Test
    public void testApplyStatesActionUsesTransactionalUpdateForMappedState() {
        Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
        MinionSummary regularMinion = new MinionSummary(1L, "regular", null, null, null, "SLES", false);
        MinionSummary transactionalMinion = new MinionSummary(2L, "transactional", null, null, null, "SLES", true);

        TransactionalActionManager.addCustomStateApplyCalls(
                calls,
                List.of(SaltParameters.PACKAGES_PKGLOCK),
                Optional.empty(),
                Optional.of(true),
                Optional.empty(),
                List.of(regularMinion, transactionalMinion));

        assertEquals(2, calls.size());
        assertTrue(calls.entrySet().stream()
                .anyMatch(entry -> "state.apply".equals(entry.getKey().getPayload().get("fun")) &&
                        entry.getValue().equals(List.of(regularMinion))));
        assertTrue(calls.entrySet().stream()
                .anyMatch(entry -> "transactional_update.apply".equals(entry.getKey().getPayload().get("fun")) &&
                        entry.getValue().equals(List.of(transactionalMinion))));
    }

    @Test
    public void testCustomStatesUseTransactionalUpdateWhenEnabled() {
        withCustomStatesTransactionalUpdateConfig("true", () -> {
            Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
            MinionSummary regularMinion = new MinionSummary(1L, "regular", null, null, null, "SLES", false);
            MinionSummary transactionalMinion = new MinionSummary(2L, "transactional", null, null, null, "SLES", true);

            TransactionalActionManager.addCustomStateApplyCalls(
                    calls,
                    List.of("custom"),
                    Optional.empty(),
                    Optional.of(true),
                    Optional.of(true),
                    List.of(regularMinion, transactionalMinion));

            assertEquals(2, calls.size());
            assertTrue(calls.entrySet().stream()
                    .anyMatch(entry -> "state.apply".equals(entry.getKey().getPayload().get("fun")) &&
                            entry.getValue().equals(List.of(regularMinion))));
            Optional<LocalCall<?>> transactionalCall = calls.entrySet().stream()
                    .filter(entry -> entry.getValue().equals(List.of(transactionalMinion)))
                    .map(Map.Entry::getKey)
                    .findFirst();
            assertTrue(transactionalCall.isPresent());
            assertEquals("transactional_update.apply", transactionalCall.get().getPayload().get("fun"));
            Map<?, ?> kwargs = (Map<?, ?>) transactionalCall.get().getPayload().get("kwarg");
            assertEquals(List.of("custom"), kwargs.get("mods"));
            assertEquals(true, kwargs.get("queue"));
            assertEquals(true, kwargs.get("test"));
        });
    }

    @Test
    public void testCustomStatesUseRegularStateApplyWhenDisabled() {
        withCustomStatesTransactionalUpdateConfig("false", () -> {
            Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
            MinionSummary transactionalMinion = new MinionSummary(2L, "transactional", null, null, null, "SLES", true);

            TransactionalActionManager.addCustomStateApplyCalls(
                    calls,
                    List.of("custom"),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    List.of(transactionalMinion));

            assertEquals(1, calls.size());
            Map<String, Object> payload = calls.keySet().iterator().next().getPayload();
            assertEquals("state.apply", payload.get("fun"));
            assertEquals(List.of("direct_call"), payload.get("module_executors"));
        });
    }

    @Test
    public void testHighstateDoesNotUseCustomStateTransactionalConfig() {
        withCustomStatesTransactionalUpdateConfig("true", () -> {
            Map<LocalCall<?>, List<MinionSummary>> calls = new HashMap<>();
            MinionSummary transactionalMinion = new MinionSummary(2L, "transactional", null, null, null, "SLES", true);

            TransactionalActionManager.addCustomStateApplyCalls(
                    calls,
                    List.of(),
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    List.of(transactionalMinion));

            assertEquals(1, calls.size());
            Map<String, Object> payload = calls.keySet().iterator().next().getPayload();
            assertEquals("state.apply", payload.get("fun"));
            assertEquals(List.of("direct_call"), payload.get("module_executors"));
        });
    }

    @Test
    public void testPendingTransactionCheckUsesDirectCall() {
        Map<String, Object> payload = TransactionalActionManager.getPendingTransactionCheckSaltCall().getPayload();

        assertEquals("state.apply", payload.get("fun"));
        assertEquals(List.of(ApplyStatesEventMessage.TRANSACTIONAL_PENDING_TRANSACTION),
                ((Map<?, ?>) payload.get("kwarg")).get("mods"));
        assertEquals(List.of("direct_call"), payload.get("module_executors"));
    }

    private void withCustomStatesTransactionalUpdateConfig(String value, Runnable test) {
        String previousValue = Config.get().getString(ConfigDefaults.SALT_CUSTOM_STATES_USE_TRANSACTIONAL_UPDATE);
        Config.get().setBoolean(ConfigDefaults.SALT_CUSTOM_STATES_USE_TRANSACTIONAL_UPDATE, value);
        try {
            test.run();
        }
        finally {
            if (previousValue == null) {
                Config.get().remove(ConfigDefaults.SALT_CUSTOM_STATES_USE_TRANSACTIONAL_UPDATE);
            }
            else {
                Config.get().setString(ConfigDefaults.SALT_CUSTOM_STATES_USE_TRANSACTIONAL_UPDATE, previousValue);
            }
        }
    }
}
