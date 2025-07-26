/*
 * Copyright (c) 2013 SUSE LLC
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
package com.redhat.rhn.frontend.action.kickstart.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerNetAddress4;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.frontend.action.kickstart.PowerManagementAction;
import com.redhat.rhn.frontend.struts.RequestContext;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;
import com.redhat.rhn.testing.RhnMockStrutsTestCase;

import org.cobbler.CobblerConnection;
import org.cobbler.SystemRecord;
import org.cobbler.test.MockConnection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Map;

import servletunit.HttpServletRequestSimulator;

/**
 * Tests the Power Management action.
 */
public class PowerManagementActionTest extends RhnMockStrutsTestCase {

    // poor-man fixture
    /** Expected power management type. */
    public static final String EXPECTED_TYPE = "ipmilan";

    /** Expected power management address. */
    public static final String EXPECTED_ADDRESS = "192.123.23.21";

    /** Expected power management username. */
    public static final String EXPECTED_USERNAME = "power management test username";

    /** Expected power management password. */
    public static final String EXPECTED_PASSWORD = "power management test password";

    /** Expected power management ID. */
    public static final String EXPECTED_ID = "123";

    /** Alternative expected power management address. */
    public static final String EXPECTED_ADDRESS_2 = "192.123.23.22";

    /** Alternative expected power management address. */
    public static final String EXPECTED_USERNAME_2 = "power management test username 2";

    /** Alternative expected power management password. */
    public static final String EXPECTED_PASSWORD_2 = "power management test password 2";

    /** Alternative expected power management ID. */
    public static final String EXPECTED_ID_2 = "122";

    /**
     * Sets up action path and mocked Cobbler connection.
     * @throws Exception if something goes wrong
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        MockConnection.clear();
        setRequestPathInfo("/systems/details/kickstart/PowerManagement");
    }

    /**
     * Tests that action returns correct default parameters for a system without
     * a profile.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testExecuteNewSystemsDefault() {

        Server server = ServerFactoryTest.createTestServer(user, true);
        NetworkInterface networkInterface = server.getNetworkInterfaces().iterator().next();
        ServerNetAddress4 ip4 = new ServerNetAddress4() {
            {
                setAddress(EXPECTED_ADDRESS);
            }
        };
        ArrayList<ServerNetAddress4> iplist = new ArrayList<>();
        iplist.add(ip4);
        networkInterface.setSa4(iplist);
        addRequestParameter(RequestContext.SID, server.getId().toString());
        actionPerform();

        Map<String, String> types = (Map<String, String>) request
            .getAttribute(PowerManagementAction.TYPES);
        assertTrue(types.containsValue(EXPECTED_TYPE));

        assertEquals(EXPECTED_TYPE, request.getAttribute(PowerManagementAction.POWER_TYPE));
        assertNull(request.getAttribute(PowerManagementAction.POWER_ADDRESS));
        assertNull(request.getAttribute(PowerManagementAction.POWER_USERNAME));
        assertNull(request.getAttribute(PowerManagementAction.POWER_PASSWORD));
        assertNull(request.getAttribute(PowerManagementAction.POWER_ID));
        verifyNoActionErrors();
        verifyNoActionMessages();
    }

    /**
     * Tests saving the configuration of a new system.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testExecuteSaveNewSystem() {
        Server server = ServerFactoryTest.createTestServer(user, true);

        addRequestParameter(RequestContext.SID, server.getId().toString());
        request.setMethod(HttpServletRequestSimulator.POST);
        addSubmitted();
        addDispatchCall("kickstart.powermanagement.jsp.save_only");
        request.addParameter(PowerManagementAction.POWER_TYPE, EXPECTED_TYPE);
        request.addParameter(PowerManagementAction.POWER_ADDRESS, EXPECTED_ADDRESS);
        request.addParameter(PowerManagementAction.POWER_USERNAME, EXPECTED_USERNAME);
        request.addParameter(PowerManagementAction.POWER_PASSWORD, EXPECTED_PASSWORD);
        request.addParameter(PowerManagementAction.POWER_ID, EXPECTED_ID);
        actionPerform();

        Map<String, String> types = (Map<String, String>) request
            .getAttribute(PowerManagementAction.TYPES);
        assertTrue(types.containsValue(EXPECTED_TYPE));

        assertEquals(EXPECTED_TYPE, request.getAttribute(PowerManagementAction.POWER_TYPE));
        assertEquals(EXPECTED_ADDRESS,
            request.getAttribute(PowerManagementAction.POWER_ADDRESS));
        assertEquals(EXPECTED_USERNAME,
            request.getAttribute(PowerManagementAction.POWER_USERNAME));
        assertEquals(EXPECTED_PASSWORD,
            request.getAttribute(PowerManagementAction.POWER_PASSWORD));
        assertEquals(EXPECTED_ID, request.getAttribute(PowerManagementAction.POWER_ID));
        verifyNoActionErrors();
        verifyActionMessage("kickstart.powermanagement.saved");

        CobblerConnection connection = CobblerXMLRPCHelper.getConnection(user);
        SystemRecord systemRecord = SystemRecord.lookupById(connection, server.getCobblerId());

        assertEquals(EXPECTED_TYPE, systemRecord.getPowerType());
        assertEquals(EXPECTED_ADDRESS, systemRecord.getPowerAddress());
        assertEquals(EXPECTED_USERNAME, systemRecord.getPowerUsername());
        assertEquals(EXPECTED_PASSWORD, systemRecord.getPowerPassword());
        assertEquals(EXPECTED_ID, systemRecord.getPowerId());
    }

    /**
     * Tests reading the configuration of a system that has already been
     * configured.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testExecuteReadSavedSystem() {
        Server server = ServerFactoryTest.createTestServer(user, true);

        addRequestParameter(RequestContext.SID, server.getId().toString());
        request.setMethod(HttpServletRequestSimulator.POST);
        addSubmitted();
        addDispatchCall("kickstart.powermanagement.jsp.save_only");
        request.addParameter(PowerManagementAction.POWER_TYPE, EXPECTED_TYPE);
        request.addParameter(PowerManagementAction.POWER_ADDRESS, EXPECTED_ADDRESS);
        request.addParameter(PowerManagementAction.POWER_USERNAME, EXPECTED_USERNAME);
        request.addParameter(PowerManagementAction.POWER_PASSWORD, EXPECTED_PASSWORD);
        request.addParameter(PowerManagementAction.POWER_ID, EXPECTED_ID);
        actionPerform();

        clearRequestParameters();
        addRequestParameter(RequestContext.SID, server.getId().toString());
        request.setMethod(HttpServletRequestSimulator.GET);
        actionPerform();

        Map<String, String> types = (Map<String, String>) request
            .getAttribute(PowerManagementAction.TYPES);
        assertTrue(types.containsValue(EXPECTED_TYPE));

        assertEquals(EXPECTED_TYPE, request.getAttribute(PowerManagementAction.POWER_TYPE));
        assertEquals(EXPECTED_ADDRESS,
            request.getAttribute(PowerManagementAction.POWER_ADDRESS));
        assertEquals(EXPECTED_USERNAME,
            request.getAttribute(PowerManagementAction.POWER_USERNAME));
        assertEquals(EXPECTED_PASSWORD,
            request.getAttribute(PowerManagementAction.POWER_PASSWORD));
        assertEquals(EXPECTED_ID, request.getAttribute(PowerManagementAction.POWER_ID));
        verifyNoActionErrors();
    }

    /**
     * Tests overwriting the configuration of an existing system.
     */
    @Test
    public void testExecuteOverwriteExistingSystem() {
        Server server = ServerFactoryTest.createTestServer(user, true);

        addRequestParameter(RequestContext.SID, server.getId().toString());
        request.setMethod(HttpServletRequestSimulator.POST);
        addSubmitted();
        addDispatchCall("kickstart.powermanagement.jsp.save_only");
        request.addParameter(PowerManagementAction.POWER_TYPE, EXPECTED_TYPE);
        request.addParameter(PowerManagementAction.POWER_ADDRESS, EXPECTED_ADDRESS);
        request.addParameter(PowerManagementAction.POWER_USERNAME, EXPECTED_USERNAME);
        request.addParameter(PowerManagementAction.POWER_PASSWORD, EXPECTED_PASSWORD);
        request.addParameter(PowerManagementAction.POWER_ID, EXPECTED_ID);
        actionPerform();

        request.addParameter(PowerManagementAction.POWER_TYPE, EXPECTED_TYPE);
        request.addParameter(PowerManagementAction.POWER_ADDRESS, EXPECTED_ADDRESS_2);
        request.addParameter(PowerManagementAction.POWER_USERNAME, EXPECTED_USERNAME_2);
        request.addParameter(PowerManagementAction.POWER_PASSWORD, EXPECTED_PASSWORD_2);
        request.addParameter(PowerManagementAction.POWER_ID, EXPECTED_ID_2);
        actionPerform();

        assertEquals(EXPECTED_ADDRESS_2,
            request.getAttribute(PowerManagementAction.POWER_ADDRESS));
        assertEquals(EXPECTED_USERNAME_2,
            request.getAttribute(PowerManagementAction.POWER_USERNAME));
        assertEquals(EXPECTED_PASSWORD_2,
            request.getAttribute(PowerManagementAction.POWER_PASSWORD));
        assertEquals(EXPECTED_ID_2, request.getAttribute(PowerManagementAction.POWER_ID));

        CobblerConnection connection = CobblerXMLRPCHelper.getConnection(user);
        SystemRecord systemRecord = SystemRecord.lookupById(connection, server.getCobblerId());

        assertEquals(EXPECTED_ADDRESS_2, systemRecord.getPowerAddress());
        assertEquals(EXPECTED_USERNAME_2, systemRecord.getPowerUsername());
        assertEquals(EXPECTED_PASSWORD_2, systemRecord.getPowerPassword());
        assertEquals(EXPECTED_ID_2, systemRecord.getPowerId());
        verifyNoActionErrors();
    }

    /**
     * Tests powering on a system.
     */
    @Test
    public void testPowerOn() {
        Server server = ServerFactoryTest.createTestServer(user, true);

        addRequestParameter(RequestContext.SID, server.getId().toString());
        request.setMethod(HttpServletRequestSimulator.POST);
        addSubmitted();
        addDispatchCall("kickstart.powermanagement.jsp.power_on");
        request.addParameter(PowerManagementAction.POWER_TYPE, EXPECTED_TYPE);
        request.addParameter(PowerManagementAction.POWER_ADDRESS, EXPECTED_ADDRESS);
        request.addParameter(PowerManagementAction.POWER_USERNAME, EXPECTED_USERNAME);
        request.addParameter(PowerManagementAction.POWER_PASSWORD, EXPECTED_PASSWORD);
        request.addParameter(PowerManagementAction.POWER_ID, EXPECTED_ID);
        actionPerform();

        verifyNoActionErrors();
        assertEquals("power_system on " + server.getCobblerId(),
            MockConnection.getLatestPowerCommand());
    }

    /**
     * Tests powering off a system.
     */
    @Test
    public void testPowerOff() {
        Server server = ServerFactoryTest.createTestServer(user, true);

        addRequestParameter(RequestContext.SID, server.getId().toString());
        request.setMethod(HttpServletRequestSimulator.POST);
        addSubmitted();
        addDispatchCall("kickstart.powermanagement.jsp.power_off");
        request.addParameter(PowerManagementAction.POWER_TYPE, EXPECTED_TYPE);
        request.addParameter(PowerManagementAction.POWER_ADDRESS, EXPECTED_ADDRESS);
        request.addParameter(PowerManagementAction.POWER_USERNAME, EXPECTED_USERNAME);
        request.addParameter(PowerManagementAction.POWER_PASSWORD, EXPECTED_PASSWORD);
        request.addParameter(PowerManagementAction.POWER_ID, EXPECTED_ID);
        actionPerform();

        verifyNoActionErrors();
        assertEquals("power_system off " + server.getCobblerId(),
            MockConnection.getLatestPowerCommand());
    }

    /**
     * Tests powering off and on a system.
     */
    @Test
    public void testReboot() {
        Server server = ServerFactoryTest.createTestServer(user, true);

        addRequestParameter(RequestContext.SID, server.getId().toString());
        request.setMethod(HttpServletRequestSimulator.POST);
        addSubmitted();
        addDispatchCall("kickstart.powermanagement.jsp.reboot");
        request.addParameter(PowerManagementAction.POWER_TYPE, EXPECTED_TYPE);
        request.addParameter(PowerManagementAction.POWER_ADDRESS, EXPECTED_ADDRESS);
        request.addParameter(PowerManagementAction.POWER_USERNAME, EXPECTED_USERNAME);
        request.addParameter(PowerManagementAction.POWER_PASSWORD, EXPECTED_PASSWORD);
        request.addParameter(PowerManagementAction.POWER_ID, EXPECTED_ID);
        actionPerform();

        verifyNoActionErrors();
        assertEquals("power_system reboot " + server.getCobblerId(),
            MockConnection.getLatestPowerCommand());
    }

    /**
     * Tests retrieving the status of a system.
     */
    @Test
    public void testGetStatus() {
        Server server = ServerFactoryTest.createTestServer(user, true);

        addRequestParameter(RequestContext.SID, server.getId().toString());
        request.setMethod(HttpServletRequestSimulator.POST);
        addSubmitted();
        addDispatchCall("kickstart.powermanagement.jsp.get_status");
        request.addParameter(PowerManagementAction.POWER_TYPE, EXPECTED_TYPE);
        request.addParameter(PowerManagementAction.POWER_ADDRESS, EXPECTED_ADDRESS);
        request.addParameter(PowerManagementAction.POWER_USERNAME, EXPECTED_USERNAME);
        request.addParameter(PowerManagementAction.POWER_PASSWORD, EXPECTED_PASSWORD);
        request.addParameter(PowerManagementAction.POWER_ID, EXPECTED_ID);
        actionPerform();

        verifyNoActionErrors();
        assertEquals(true, request.getAttribute(PowerManagementAction.POWER_STATUS_ON));
        assertEquals("power_system status " + server.getCobblerId(),
            MockConnection.getLatestPowerCommand());
    }
}
