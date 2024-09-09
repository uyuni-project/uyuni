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

import com.redhat.rhn.domain.entitlement.AnsibleControlNodeEntitlement;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.testing.ServerTestUtils;

import org.junit.jupiter.api.Test;

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
    @Test
    public void testIsAllowedOnSaltClients() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        assertTrue(EntitlementManager.ANSIBLE_CONTROL_NODE.isAllowedOnServer(minion));
    }

    /**
     * Tests that the entitlement is Forbidden on traditional clients.
     */
    @Test
    public void testIsForbiddenOnTraditionalClients() throws Exception {
        Server server = ServerTestUtils.createTestSystem(user, ServerConstants.getServerGroupTypeEnterpriseEntitled());
        assertFalse(EntitlementManager.ANSIBLE_CONTROL_NODE.isAllowedOnServer(server));
    }
}
