/*
 * Copyright (c) 2021 SUSE LLC
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

import com.redhat.rhn.domain.entitlement.PeripheralServerEntitlement;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.virtualization.VirtManagerSalt;
import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.manager.webui.services.test.TestSystemQuery;

import org.junit.jupiter.api.Test;

/**
 * Test for {@link com.redhat.rhn.domain.entitlement.PeripheralServerEntitlement}
 */
public class PeripheralServerEntitlementTest extends BaseEntitlementTestCase {
    private final SystemQuery systemQuery = new TestSystemQuery();
    private final SaltApi saltApi = new TestSaltApi();
    private final ServerGroupManager serverGroupManager = new ServerGroupManager(saltApi);
    private final VirtManager virtManager = new VirtManagerSalt(saltApi);
    private final MonitoringManager monitoringManager = new FormulaMonitoringManager(saltApi);
    private final SystemEntitlementManager systemEntitlementManager = new SystemEntitlementManager(
            new SystemUnentitler(virtManager, monitoringManager, serverGroupManager),
            new SystemEntitler(saltApi, virtManager, monitoringManager, serverGroupManager)
    );

    @Override
    protected void createEntitlement() {
        ent = new PeripheralServerEntitlement();
    }

    @Override
    protected String getLabel() {
        return EntitlementManager.PERIPHERAL_SERVER_ENTITLED;
    }

    /**
     * Tests that the entitlement is allowed on salt clients.
     */
    @Test
    public void testIsAllowed() throws Exception {
        Server foreign = ServerTestUtils.createForeignSystem(user, "9999");
        Server minion = MinionServerFactoryTest.createTestMinionServer(user);
        foreign.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        minion.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        foreign.addFqdn("test.com");
        minion.addFqdn("test.com");

        assertTrue(EntitlementManager.PERIPHERAL_SERVER.isAllowedOnServer(foreign));
        assertTrue(EntitlementManager.PERIPHERAL_SERVER.isAllowedOnServer(minion));

        systemEntitlementManager.addEntitlementToServer(foreign, EntitlementManager.PERIPHERAL_SERVER);
        TestUtils.saveAndFlush(foreign);

        System.out.println("foreign: " + foreign.hasEntitlement(EntitlementManager.PERIPHERAL_SERVER));
        System.out.println("minion: " + minion.hasEntitlement(EntitlementManager.PERIPHERAL_SERVER));
        // the entitlement can't be enabled on 2 servers with the same fqdn
        assertFalse(EntitlementManager.PERIPHERAL_SERVER.isAllowedOnServer(minion));
    }
}
