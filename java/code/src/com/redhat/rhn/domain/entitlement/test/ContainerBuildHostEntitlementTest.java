/**
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

import com.redhat.rhn.domain.entitlement.ContainerBuildHostEntitlement;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.testing.ServerTestUtils;

import com.suse.manager.reactor.utils.ValueMap;

import java.util.HashMap;
import java.util.Map;

public class ContainerBuildHostEntitlementTest extends BaseEntitlementTestCase {

    @Override
    protected void createEntitlement() {
        ent = new ContainerBuildHostEntitlement();
    }

    @Override
    protected String getLabel() {
        return EntitlementManager.CONTAINER_BUILD_HOST_ENTITLED;
    }

    @Override
    public void testIsAllowedOnServer() throws Exception {
        Server traditional = ServerTestUtils.createTestSystem(user);
        Server minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setOs("SLES");
        minion.setRelease("12.2");

        traditional.setBaseEntitlement(EntitlementManager.MANAGEMENT);
        minion.setBaseEntitlement(EntitlementManager.SALT);

        assertTrue(ent.isAllowedOnServer(minion));
        assertFalse(ent.isAllowedOnServer(traditional));

        minion.setOs("RedHat Linux");
        minion.setRelease("6Server");
        assertTrue(ent.isAllowedOnServer(minion));
    }

    @Override
    public void testIsAllowedOnServerWithGrains() throws Exception {
        Server minion = MinionServerFactoryTest.createTestMinionServer(user);
        Map<String, Object> grains = new HashMap<>();
        grains.put("os_family", "Suse");
        grains.put("osmajorrelease", "12");

        assertTrue(ent.isAllowedOnServer(minion, new ValueMap(grains)));

        grains.put("os_family", "RedHat");
        grains.put("osmajorrelease", "7");
        assertTrue(ent.isAllowedOnServer(minion, new ValueMap(grains)));

        minion.setBaseEntitlement(EntitlementManager.MANAGEMENT);
        assertFalse(ent.isAllowedOnServer(minion, new ValueMap(grains)));
    }
}

