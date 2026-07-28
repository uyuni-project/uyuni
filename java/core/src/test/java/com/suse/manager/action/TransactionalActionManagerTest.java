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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.action.errata.ErrataAction;
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
    public void testUnknownStateUsesRegularStateApply() {
        LocalCall<?> call = TransactionalActionManager.getTransactionalSaltCall(
                "unknown.state", Optional.empty());

        assertEquals("state.apply", call.getPayload().get("fun"));
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
    public void testErrataActionIsRecognizedAsTransactionalApply() {
        assertTrue(TransactionalActionManager.isTransactionalResult(
                new ErrataAction(),
                Optional.of(Xor.right("transactional_update.apply")),
                Optional.empty()));
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
}
