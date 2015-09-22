/**
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.manager.reactor.test;

import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.reactor.RegisterMinionAction;
import com.suse.manager.reactor.RegisterMinionEvent;
import com.suse.manager.webui.services.SaltService;
import com.suse.saltstack.netapi.calls.wheel.Key.Names;
import com.suse.saltstack.netapi.event.EventStream;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Tests for {@link RegisterMinionAction}.
 */
public class RegisterMinionActionTest extends RhnBaseTestCase {

    /**
     * Test the minion registration.
     */
    public void testDoExecute() {
        // Register a minion
        SaltService saltService = new SaltServiceStub();
        RegisterMinionAction action = new RegisterMinionAction(saltService) {};
        String minionId = TestUtils.randomString();
        action.doExecute(new RegisterMinionEvent(minionId));

        // Verify the resulting system entry
        String machineId = saltService.getMachineId(minionId);
        Server minion = ServerFactory.findRegisteredMinion(machineId);
        assertNotNull(minion);
        assertEquals(minionId, minion.getName());
        assertEquals(machineId, minion.getDigitalServerId());
        assertEquals("3.12.47-2-default", minion.getRunningKernel());
        assertEquals("SLES", minion.getOs());
        assertEquals("12", minion.getRelease());
        assertEquals("N", minion.getAutoUpdate());
        assertEquals(477L, minion.getRam());
        assertEquals(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"),
                minion.getServerArch());
        assertEquals(ServerFactory.findContactMethodByLabel("default"),
                minion.getContactMethod());
    }

    /**
     * Stubbed out version of {@link SaltService} for testing.
     */
    private class SaltServiceStub implements SaltService {

        private Map<String, String> machineIds = new HashMap<>();

        @Override
        public Map<String, Object> getGrains(String minionId) {
            Map<String, Object> grains = new HashMap<>();
            grains.put("kernelrelease", "3.12.47-2-default");
            grains.put("machine_id", getMachineId(minionId));
            grains.put("mem_total", 477.0);
            grains.put("os", "SLES");
            grains.put("osfullname", "SLES");
            grains.put("osrelease", "12");
            return grains;
        }

        @Override
        public String getMachineId(String minionId) {
            if (!machineIds.containsKey(minionId)) {
                machineIds.put(minionId, TestUtils.randomString());
            }
            return machineIds.get(minionId);
        }

        // Unsupported operations
        @Override
        public Names getKeys() {
            throw new UnsupportedOperationException();
        }
        @Override
        public List<String> present() {
            throw new UnsupportedOperationException();
        }
        @Override
        public Map<String, List<String>> getPackages(String minionId) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void acceptKey(String minionId) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void deleteKey(String minionId) {
            throw new UnsupportedOperationException();
        }
        @Override
        public void rejectKey(String minionId) {
            throw new UnsupportedOperationException();
        }
        @Override
        public EventStream getEventStream() {
            throw new UnsupportedOperationException();
        }
    }
}
