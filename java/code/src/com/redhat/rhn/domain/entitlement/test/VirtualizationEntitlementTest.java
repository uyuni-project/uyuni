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

import com.redhat.rhn.domain.entitlement.ManagementEntitlement;
import com.redhat.rhn.domain.entitlement.VirtualizationEntitlement;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.testing.ServerTestUtils;

public class VirtualizationEntitlementTest extends BaseEntitlementTestCase {

    @Override
    protected void createEntitlement() {
        ent = new VirtualizationEntitlement();
    }

    @Override
    protected String getLabel() {
        return EntitlementManager.VIRTUALIZATION_ENTITLED;
    }

    @Override
    public void testIsAllowedOnServer() throws Exception {
        Server host = ServerTestUtils.createVirtHostWithGuests(1);
        Server guest = host.getGuests().iterator().next().getGuestSystem();
        guest.setBaseEntitlement(EntitlementManager.MANAGEMENT);

        assertTrue(ent.isAllowedOnServer(host));
        assertFalse(ent.isAllowedOnServer(guest));
    }

}
