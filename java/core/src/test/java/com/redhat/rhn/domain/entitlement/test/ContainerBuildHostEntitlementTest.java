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

import com.redhat.rhn.domain.entitlement.ContainerBuildHostEntitlement;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.testing.ServerTestUtils;

import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.test.TestSaltApi;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class ContainerBuildHostEntitlementTest extends BaseEntitlementTestCase {

    private final SaltApi saltApi = new TestSaltApi();
    private final SystemEntitlementManager systemEntitlementManager = new SystemEntitlementManager(
            new SystemUnentitler(saltApi), new SystemEntitler(saltApi)
    );

    @Override
    protected void createEntitlement() {
        ent = new ContainerBuildHostEntitlement();
    }

    @Override
    protected String getLabel() {
        return EntitlementManager.CONTAINER_BUILD_HOST_ENTITLED;
    }

    @Override
    @Test
    public void testIsAllowedOnServer() throws Exception {
        Server traditional = ServerTestUtils.createTestSystem(user);
        Server minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setOs("SLES");
        minion.setRelease("12.2");

        systemEntitlementManager.setBaseEntitlement(traditional, EntitlementManager.MANAGEMENT);
        systemEntitlementManager.setBaseEntitlement(minion, EntitlementManager.SALT);

        assertTrue(ent.isAllowedOnServer(minion));
        assertFalse(ent.isAllowedOnServer(traditional));

        minion.setOs("RedHat Linux");
        minion.setRelease("6Server");
        assertTrue(ent.isAllowedOnServer(minion));
    }

    @Override
    @Test
    public void testIsAllowedOnServerWithGrains() throws Exception {
        Server minion = MinionServerFactoryTest.createTestMinionServer(user);
        Map<String, Object> grains = new HashMap<>();
        grains.put("os_family", ServerConstants.OS_FAMILY_SUSE);
        grains.put("osmajorrelease", "12");

        assertTrue(ent.isAllowedOnServer(minion, new ValueMap(grains)));

        grains.put("os_family", "RedHat");
        grains.put("osmajorrelease", "7");
        assertTrue(ent.isAllowedOnServer(minion, new ValueMap(grains)));

        systemEntitlementManager.setBaseEntitlement(minion, EntitlementManager.MANAGEMENT);
        assertFalse(ent.isAllowedOnServer(minion, new ValueMap(grains)));
    }
}

