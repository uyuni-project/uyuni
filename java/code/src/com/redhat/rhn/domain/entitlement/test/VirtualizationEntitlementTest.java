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

package com.redhat.rhn.domain.entitlement.test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.entitlement.VirtualizationEntitlement;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.testing.ServerTestUtils;

import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.virtualization.VirtManagerSalt;
import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.manager.webui.services.test.TestSystemQuery;
import com.suse.salt.netapi.calls.LocalCall;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class VirtualizationEntitlementTest extends BaseEntitlementTestCase {

    private final SystemQuery systemQuery = new TestSystemQuery();
    private final SaltApi saltApi = new TestSaltApi() {
        public <R> Optional<R> callSync(LocalCall<R> call, String minionId) {
            return Optional.empty();
        }
    };
    private final ServerGroupManager serverGroupManager = new ServerGroupManager(saltApi);
    private final VirtManager virtManager = new VirtManagerSalt(saltApi);
    private final MonitoringManager monitoringManager = new FormulaMonitoringManager(saltApi);
    private final SystemEntitlementManager systemEntitlementManager = new SystemEntitlementManager(
            new SystemUnentitler(virtManager, monitoringManager, serverGroupManager),
            new SystemEntitler(saltApi, virtManager, monitoringManager, serverGroupManager)
    );

    @Override
    protected void createEntitlement() {
        ent = new VirtualizationEntitlement();
    }

    @Override
    protected String getLabel() {
        return EntitlementManager.VIRTUALIZATION_ENTITLED;
    }

    @Override
    @Test
    public void testIsAllowedOnServer() throws Exception {
        Server host = ServerTestUtils.createVirtHostWithGuests(1, systemEntitlementManager);
        Server guest = host.getGuests().iterator().next().getGuestSystem();
        systemEntitlementManager.setBaseEntitlement(guest, EntitlementManager.MANAGEMENT);

        assertTrue(ent.isAllowedOnServer(host));
        assertFalse(ent.isAllowedOnServer(guest));
    }

    @Override
    @Test
    public void testIsAllowedOnServerWithGrains() throws Exception {
        Server minion = MinionServerFactoryTest.createTestMinionServer(user);
        systemEntitlementManager.setBaseEntitlement(minion, EntitlementManager.SALT);

        Map<String, Object> grains = new HashMap<>();
        grains.put("virtual", "physical");

        assertTrue(ent.isAllowedOnServer(minion, new ValueMap(grains)));

        grains.put("virtual", "kvm");
        assertFalse(ent.isAllowedOnServer(minion, new ValueMap(grains)));
    }
}
