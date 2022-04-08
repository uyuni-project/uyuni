/*
 * Copyright (c) 2018--2021 SUSE LLC
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

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.entitlement.OSImageBuildHostEntitlement;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.testing.ServerTestUtils;

import com.suse.manager.virtualization.VirtManagerSalt;
import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.manager.webui.services.test.TestSystemQuery;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OSImageBuildHostEntitlementTest extends BaseEntitlementTestCase {

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
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        Config.get().setBoolean(ConfigDefaults.KIWI_OS_IMAGE_BUILDING_ENABLED, "true");
    }

    @Override
    protected void createEntitlement() {
        ent = new OSImageBuildHostEntitlement();
    }

    @Override
    protected String getLabel() {
        return EntitlementManager.OSIMAGE_BUILD_HOST_ENTITLED;
    }

    @Override
    @Test
    public void testIsAllowedOnServer() throws Exception {
        Server traditional = ServerTestUtils.createTestSystem(user);
        traditional.setOs("SLES");
        traditional.setRelease("12.2");
        Server minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setOs("SLES");
        minion.setRelease("12.2");

        systemEntitlementManager.setBaseEntitlement(traditional, EntitlementManager.MANAGEMENT);
        systemEntitlementManager.setBaseEntitlement(minion, EntitlementManager.SALT);

        assertTrue(ent.isAllowedOnServer(minion));
        assertFalse(ent.isAllowedOnServer(traditional));

        minion.setOs("SLES");
        minion.setRelease("15.1");
        assertTrue(ent.isAllowedOnServer(minion));

        minion.setOs("RedHat Linux");
        minion.setRelease("6Server");
        assertFalse(ent.isAllowedOnServer(minion));
    }

    @Override
    @Test
    public void testIsAllowedOnServerWithGrains() {
        // Nothing to test
    }
}
