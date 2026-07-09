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

import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class TransactionalUpdateCallsTest {

    @Test
    public void testApplyUsesCorrectFunctionName() {
        LocalCall<Map<String, State.ApplyResult>> call =
                TransactionalUpdateCalls.apply(List.of("hardware.prereq"));
        assertEquals("transactional_update.apply", call.getPayload().get("fun"));
    }

    @Test
    public void testApplyIncludesModsInKwargs() {
        LocalCall<Map<String, State.ApplyResult>> call =
                TransactionalUpdateCalls.apply(List.of("hardware.prereq", "certs"));
        Map<String, Object> kwargs = (Map<String, Object>) call.getPayload().get("kwarg");
        assertNotNull(kwargs);
        assertEquals(List.of("hardware.prereq", "certs"), kwargs.get("mods"));
    }

    @Test
    public void testApplyWithPillarIncludesPillarInKwargs() {
        Map<String, Object> pillar = Map.of("key", "value");
        LocalCall<Map<String, State.ApplyResult>> call =
                TransactionalUpdateCalls.apply(List.of("hardware.prereq"), Optional.of(pillar));
        Map<String, Object> kwargs = (Map<String, Object>) call.getPayload().get("kwarg");
        assertNotNull(kwargs);
        assertEquals(pillar, kwargs.get("pillar"));
    }

    @Test
    public void testApplyWithoutPillarOmitsPillarFromKwargs() {
        LocalCall<Map<String, State.ApplyResult>> call =
                TransactionalUpdateCalls.apply(List.of("hardware.prereq"), Optional.empty());
        Map<String, Object> kwargs = (Map<String, Object>) call.getPayload().get("kwarg");
        assertNotNull(kwargs);
        assertFalse(kwargs.containsKey("pillar"), "pillar should be absent when Optional.empty()");
    }

    @Test
    public void testApplyRejectsEmptyMods() {
        assertThrows(IllegalArgumentException.class, () -> TransactionalUpdateCalls.apply(List.of()));
    }
}
