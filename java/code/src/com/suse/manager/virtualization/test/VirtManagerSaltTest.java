/*
 * Copyright (c) 2020 SUSE LLC
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
package com.suse.manager.virtualization.test;

import com.redhat.rhn.manager.system.VirtualInstanceManager;

import com.suse.manager.reactor.messaging.test.SaltTestUtils;
import com.suse.manager.virtualization.VirtManagerSalt;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.manager.webui.utils.salt.custom.VmInfo;
import com.suse.salt.netapi.calls.LocalCall;

import com.google.gson.reflect.TypeToken;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import junit.framework.TestCase;

/**
 * Test the Salt implementation of the VirtManager interface
 */
public class VirtManagerSaltTest extends TestCase {

    /**
     * Test the getGuestsUpdatePlan method
     */
    public void testGetGuestsUpdatePlan() {
        SaltApi testSaltApi = new TestSaltApi() {
            @Override
            public <R> Optional<R> callSync(LocalCall<R> call, String minionId) {
                return SaltTestUtils.<R>getSaltResponse(
                        "/com/suse/manager/virtualization/test/virt.vm_info.all.json",
                        Collections.emptyMap(),
                        new TypeToken<>() {
                        });
            }
        };
        VirtManager virtManager = new VirtManagerSalt(testSaltApi);
        Optional<List<VmInfo>> plan = virtManager.getGuestsUpdatePlan("theminion");

        assertTrue(plan.isPresent());
        assertEquals(3, plan.get().size());
        assertTrue(plan.get().stream().
                anyMatch(info -> info.getEventType().equals(VirtualInstanceManager.EVENT_TYPE_FULLREPORT)));

        VmInfo vm1infos = plan.get().stream()
                .filter(info -> info.getGuestProperties() != null && info.getGuestProperties().getName().equals("vm01"))
                .findFirst().get();
        assertEquals(2, vm1infos.getGuestProperties().getVcpus());
        assertEquals(1024, vm1infos.getGuestProperties().getMemorySize());
        assertEquals("stopped", vm1infos.getGuestProperties().getState());
        assertEquals("b99a8176-4f40-498d-8e61-2f6ade654fe2", vm1infos.getGuestProperties().getUuid());
        assertEquals(VirtualInstanceManager.EVENT_TYPE_EXISTS, vm1infos.getEventType());

        VmInfo vm2infos = plan.get().stream()
                .filter(info -> info.getGuestProperties() != null && info.getGuestProperties().getName().equals("vm02"))
                .findFirst().get();
        assertEquals(12, vm2infos.getGuestProperties().getVcpus());
        assertEquals(2048L, vm2infos.getGuestProperties().getMemorySize());
        assertEquals("running", vm2infos.getGuestProperties().getState());
        assertEquals("98eef4f7-eb7f-4be8-859d-11658506c496", vm2infos.getGuestProperties().getUuid());
        assertEquals(VirtualInstanceManager.EVENT_TYPE_EXISTS, vm2infos.getEventType());
    }

    public void testGetFeatures() {
        SaltApi testSaltApi = new TestSaltApi() {
            @Override
            public <R> Optional<R> callSync(LocalCall<R> call, String minionId) {
                return SaltTestUtils.<R>getSaltResponse(
                        "/com/suse/manager/virtualization/test/virt.features.json",
                        Collections.emptyMap(),
                        new TypeToken<>() {
                        });
            }
        };
        VirtManager virtManager = new VirtManagerSalt(testSaltApi);
        Optional<Map<String, Boolean>> actual = virtManager.getFeatures("minion0");
        assertTrue(actual.get().get("enhanced_network"));
    }
}
