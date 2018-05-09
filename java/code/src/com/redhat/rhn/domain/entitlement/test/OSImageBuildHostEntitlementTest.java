package com.redhat.rhn.domain.entitlement.test;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.entitlement.OSImageBuildHostEntitlement;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.testing.ServerTestUtils;

import com.suse.manager.reactor.utils.ValueMap;

import java.util.HashMap;
import java.util.Map;

public class OSImageBuildHostEntitlementTest extends BaseEntitlementTestCase {

    @Override public void setUp() throws Exception {
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
