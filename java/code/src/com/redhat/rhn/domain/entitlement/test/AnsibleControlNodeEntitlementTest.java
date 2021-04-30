package com.redhat.rhn.domain.entitlement.test;

import com.redhat.rhn.domain.entitlement.AnsibleControlNodeEntitlement;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.testing.ServerTestUtils;

/**
 * Test for {@link com.redhat.rhn.domain.entitlement.AnsibleControlNodeEntitlement}
 */
public class AnsibleControlNodeEntitlementTest extends BaseEntitlementTestCase {

    @Override
    protected void createEntitlement() {
        ent = new AnsibleControlNodeEntitlement();
    }

    @Override
    protected String getLabel() {
        return EntitlementManager.ANSIBLE_CONTROL_NODE_ENTITLED;
    }

    /**
     * Tests that the entitlement is allowed on salt clients.
     */
    public void testIsAllowedOnSaltClients() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        assertTrue(EntitlementManager.ANSIBLE_CONTROL_NODE.isAllowedOnServer(minion));
    }

    /**
     * Tests that the entitlement is Forbidden on traditional clients.
     */
    public void testIsForbiddenOnTraditionalClients() throws Exception {
        Server server = ServerTestUtils.createTestSystem(user);
        assertFalse(EntitlementManager.ANSIBLE_CONTROL_NODE.isAllowedOnServer(server));
    }
}