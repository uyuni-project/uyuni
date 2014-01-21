/**
 * Copyright (c) 2013 SUSE
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
package com.redhat.rhn.frontend.action.ssm.test;

import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgEntitlementType;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.action.kickstart.PowerManagementAction;
import com.redhat.rhn.frontend.action.kickstart.test.PowerManagementActionTest;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;
import com.redhat.rhn.testing.RhnMockStrutsTestCase;
import com.redhat.rhn.testing.ServerTestUtils;

import org.cobbler.CobblerConnection;
import org.cobbler.SystemRecord;

import servletunit.HttpServletRequestSimulator;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Tests PowerManagementConfigurationAction
 * @author Silvio Moioli <smoioli@suse.de>
 */
public class PowerManagementConfigurationActionTest extends RhnMockStrutsTestCase {
    private CobblerConnection connection;
    private List<Server> servers;

    /**
     * Sets up a request.
     * @throws Exception if things go wrong
     * @see com.redhat.rhn.testing.RhnMockStrutsTestCase#setUp()
     */
    public void setUp() throws Exception {
        super.setUp();
        connection = CobblerXMLRPCHelper.getConnection(user.getLogin());
        servers = setUpTestProvisionableSsmServers(user);
    }

    /**
     * Sets up a list of servers with provisioning entitlement and adds them to
     * the SSM for test purposes.
     * @param user the current user
     * @return the list of server
     * @throws Exception if something goes wrong
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static List<Server> setUpTestProvisionableSsmServers(User user)
            throws Exception {
        Org org = user.getOrg();
        Set entitlements = org.getEntitlements();
        OrgEntitlementType type = OrgFactory.lookupEntitlementByLabel("rhn_provisioning");
        entitlements.add(type);
        org.setEntitlements(entitlements);
        OrgFactory.save(org);

        List<Server> result = new LinkedList<Server>();
        for (int i = 0; i < 2; i++) {
            Server server = ServerTestUtils.createTestSystem(user,
                ServerConstants.getServerGroupTypeProvisioningEntitled());
            result.add(server);
            ServerTestUtils.addServersToSsm(user, server.getId());
        }
        return result;
    }

    /**
     * Tests creating Cobbler system records with a chosen profile from SSM.
     * @throws Exception if something goes wrong
     */
    public void testExecute() throws Exception {
        setRequestPathInfo("/systems/ssm/provisioning/PowerManagementConfiguration");
        request.setMethod(HttpServletRequestSimulator.POST);

        addSubmitted();
        addDispatchCall("ssm.provisioning.powermanagement.configuration.update");
        request.addParameter(PowerManagementAction.POWER_TYPE,
            PowerManagementActionTest.EXPECTED_TYPE);
        request.addParameter(PowerManagementAction.POWER_ADDRESS,
            PowerManagementActionTest.EXPECTED_ADDRESS);
        request.addParameter(PowerManagementAction.POWER_USERNAME,
            PowerManagementActionTest.EXPECTED_USERNAME);
        request.addParameter(PowerManagementAction.POWER_PASSWORD,
            PowerManagementActionTest.EXPECTED_PASSWORD);
        request.addParameter(PowerManagementAction.POWER_ID,
            PowerManagementActionTest.EXPECTED_ID);
        actionPerform();

        for (Server server : servers) {
            SystemRecord record = SystemRecord
                .lookupById(connection, server.getCobblerId());
            assertEquals(PowerManagementActionTest.EXPECTED_TYPE, record.getPowerType());
            assertEquals(PowerManagementActionTest.EXPECTED_ADDRESS,
                record.getPowerAddress());
            assertEquals(PowerManagementActionTest.EXPECTED_USERNAME,
                record.getPowerUsername());
            assertEquals(PowerManagementActionTest.EXPECTED_PASSWORD,
                record.getPowerPassword());
            assertEquals(PowerManagementActionTest.EXPECTED_ID, record.getPowerId());
        }
    }
}
