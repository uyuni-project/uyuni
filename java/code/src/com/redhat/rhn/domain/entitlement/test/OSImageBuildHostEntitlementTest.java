package com.redhat.rhn.domain.entitlement.test;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.entitlement.OSImageBuildHostEntitlement;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.testing.ServerTestUtils;

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
        traditional.setOs("SLES");
        traditional.setRelease("12.2");
        Server minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setOs("SLES");
        minion.setRelease("12.2");

        SystemEntitlementManager.INSTANCE.setBaseEntitlement(traditional, EntitlementManager.MANAGEMENT);
        SystemEntitlementManager.INSTANCE.setBaseEntitlement(minion, EntitlementManager.SALT);

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
    public void testIsAllowedOnServerWithGrains() {
        // Nothing to test
    }
}
