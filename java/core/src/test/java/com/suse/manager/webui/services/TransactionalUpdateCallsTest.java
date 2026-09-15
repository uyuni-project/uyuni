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
package com.suse.manager.webui.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.utils.Xor;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TransactionalUpdateCallsTest {
    private static final String TRANSACTIONAL_PREREQ =
            SaltParameters.HARDWARE_PROFILE_UPDATE_PREREQ;

    @Test
    public void testApplyUsesCorrectFunctionName() {
        LocalCall<Map<String, State.ApplyResult>> call =
                TransactionalUpdateCalls.apply(List.of(TRANSACTIONAL_PREREQ), Optional.empty());
        assertEquals("transactional_update.apply", call.getPayload().get("fun"));
    }

    @Test
    public void testApplyIncludesModsInKwargs() {
        LocalCall<Map<String, State.ApplyResult>> call =
                TransactionalUpdateCalls.apply(List.of(TRANSACTIONAL_PREREQ, "certs"), Optional.empty());
        Map<String, Object> kwargs = (Map<String, Object>) call.getPayload().get("kwarg");
        assertNotNull(kwargs);
        assertEquals(List.of(TRANSACTIONAL_PREREQ, "certs"), kwargs.get("mods"));
    }

    @Test
    public void testApplyWithPillarIncludesPillarInKwargs() {
        Map<String, Object> pillar = Map.of("key", "value");
        LocalCall<Map<String, State.ApplyResult>> call =
                TransactionalUpdateCalls.apply(List.of(TRANSACTIONAL_PREREQ), Optional.of(pillar));
        Map<String, Object> kwargs = (Map<String, Object>) call.getPayload().get("kwarg");
        assertNotNull(kwargs);
        assertEquals(pillar, kwargs.get("pillar"));
    }

    @Test
    public void testApplyWithoutPillarOmitsPillarFromKwargs() {
        LocalCall<Map<String, State.ApplyResult>> call =
                TransactionalUpdateCalls.apply(List.of(TRANSACTIONAL_PREREQ), Optional.empty());
        Map<String, Object> kwargs = (Map<String, Object>) call.getPayload().get("kwarg");
        assertNotNull(kwargs);
        assertFalse(kwargs.containsKey("pillar"), "pillar should be absent when Optional.empty()");
        assertFalse(kwargs.containsKey("exclude"), "exclude should be absent when no IDs are provided");
    }

    @Test
    public void testApplyWithEmptyModsOmitsModsForHighstate() {
        LocalCall<Map<String, State.ApplyResult>> call = TransactionalUpdateCalls.apply(List.of(), Optional.empty());
        Map<String, Object> kwargs = (Map<String, Object>) call.getPayload().get("kwarg");
        assertNotNull(kwargs);
        assertFalse(kwargs.containsKey("mods"), "mods should be absent when applying highstate");
        assertFalse(kwargs.containsKey("exclude"), "exclude should be absent when no IDs are provided");
    }

    @Test
    public void testApplyIncludesSingleExcludedId() {
        LocalCall<Map<String, State.ApplyResult>> call = TransactionalUpdateCalls.apply(
                List.of(TRANSACTIONAL_PREREQ),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of("mgr_timezone_setting"));
        Map<String, Object> kwargs = (Map<String, Object>) call.getPayload().get("kwarg");

        assertEquals(List.of(Map.of("id", "mgr_timezone_setting")), kwargs.get("exclude"));
    }

    @Test
    public void testApplyIncludesMultipleExcludedIdsInInputOrder() {
        List<String> excludeIds = List.of(
                "mgr_timezone_setting",
                "mgr_kb_settings",
                "mgr_language_settings");
        LocalCall<Map<String, State.ApplyResult>> call = TransactionalUpdateCalls.apply(
                List.of(TRANSACTIONAL_PREREQ),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                excludeIds);
        Map<String, Object> kwargs = (Map<String, Object>) call.getPayload().get("kwarg");

        assertEquals(List.of(
                Map.of("id", "mgr_timezone_setting"),
                Map.of("id", "mgr_kb_settings"),
                Map.of("id", "mgr_language_settings")), kwargs.get("exclude"));
    }

    @Test
    public void testHighstateIncludesExcludedIds() {
        LocalCall<Map<String, State.ApplyResult>> call = TransactionalUpdateCalls.apply(
                List.of(),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of("mgr_timezone_setting"));
        Map<String, Object> kwargs = (Map<String, Object>) call.getPayload().get("kwarg");

        assertFalse(kwargs.containsKey("mods"));
        assertEquals(List.of(Map.of("id", "mgr_timezone_setting")), kwargs.get("exclude"));
    }

    @Test
    public void testApplyWithEmptyExcludeOmitsExcludeFromKwargs() {
        LocalCall<Map<String, State.ApplyResult>> call = TransactionalUpdateCalls.apply(
                List.of(TRANSACTIONAL_PREREQ),
                Optional.empty(),
                Optional.empty(),
                Optional.empty(),
                List.of());
        Map<String, Object> kwargs = (Map<String, Object>) call.getPayload().get("kwarg");

        assertFalse(kwargs.containsKey("exclude"), "exclude should be absent for an empty ID list");
    }

    @Test
    public void testApplyRejectsNullMods() {
        assertThrows(IllegalArgumentException.class, () -> TransactionalUpdateCalls.apply(null, Optional.empty()));
    }

    @Test
    public void testTransactionalUpdateApplyFunctionIsDetected() {
        assertTrue(TransactionalUpdateCalls.isApplyFunction(
                Optional.of(Xor.right("transactional_update.apply"))));
    }

    @Test
    public void testStateApplyFunctionIsNotDetectedAsTransactionalUpdateApply() {
        assertFalse(TransactionalUpdateCalls.isApplyFunction(Optional.of(Xor.right("state.apply"))));
    }
}
