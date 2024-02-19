/*
 * Copyright (c) 2024 SUSE LLC
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

package com.suse.manager.webui.controllers.test;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.testing.ServerTestUtils;

import com.suse.manager.reactor.messaging.test.SaltTestUtils;
import com.suse.manager.webui.controllers.SystemsController;
import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.manager.webui.utils.SparkTestUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import spark.ModelAndView;
import spark.Request;

public class SystemsControllerTest extends BaseControllerTestCase {

    private static final Gson GSON = new GsonBuilder().create();
    private static final Map<String, List<String>> SERVER_NAMES_BY_HOST_SERVER_NAME_NAME_MAP = Map.of(
            "hostA", Arrays.asList("hostA_VmA", "hostA_VmB", "hostA_VmC"),
            "hostB", Arrays.asList("hostB_VmA", "hostB_VmB")
    );
    private static final String BASE_URI = "http://localhost:8080/rhn/manager/systems/list/virtual";
    private static final String KEY_HOST_SERVER_NAME = "host_server_name";
    private static final String KEY_SERVER_NAME = "server_name";
    private static final String PROPERTY_HOST_SERVER_NAME = "hostServerName";
    private static final String PROPERTY_SERVER_NAME = "name";

    //
    private VirtManager virtManager;
    private Map<String, Server> serversByHostServerName;
    private SystemsController systemsController;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);

        virtManager = mock(VirtManager.class);
        context().checking(new Expectations() {
            {
                allowing(virtManager).getCapabilities("testminion.local");
                will(returnValue(
                        SaltTestUtils.getSaltResponse(
                                "/com/suse/manager/webui/controllers/virtualization/test/virt.guest.allcaps.json", null,
                                new TypeToken<Map<String, JsonElement>>() {
                                })
                ));
                allowing(virtManager).updateLibvirtEngine(with(any(MinionServer.class)));
            }
        });

        SaltApi saltApi = new TestSaltApi();
        systemsController = new SystemsController(saltApi);
        MonitoringManager monitoringManager = new FormulaMonitoringManager(saltApi);
        ServerGroupManager serverGroupManager = new ServerGroupManager(saltApi);
        SystemEntitlementManager systemEntitlementManager = new SystemEntitlementManager(
                new SystemUnentitler(virtManager, monitoringManager, serverGroupManager),
                new SystemEntitler(new TestSaltApi(), virtManager, monitoringManager, serverGroupManager)
        );

        serversByHostServerName = new HashMap<>();
        for (Map.Entry<String, List<String>> e : SERVER_NAMES_BY_HOST_SERVER_NAME_NAME_MAP.entrySet()) {

            // Set up the host
            String hostName = e.getKey();
            Server host = ServerTestUtils.createVirtHostWithGuests(user, e.getValue().size(), true,
                    systemEntitlementManager);
            host.setName(hostName);
            serversByHostServerName.put(hostName, host);

            // Set up the host's vms
            Iterator<String> vmNamesIterator = e.getValue().iterator();
            Iterator<VirtualInstance> guestIterator = host.getGuests().iterator();
            while (guestIterator.hasNext() && vmNamesIterator.hasNext()) {
                VirtualInstance guest = guestIterator.next();
                String vmName = vmNamesIterator.next();
                guest.setName(vmName);
            }

        }

        // Assert all vms are created
        for (Server host : serversByHostServerName.values()) {
            int expectedVmsSize = SERVER_NAMES_BY_HOST_SERVER_NAME_NAME_MAP.get(host.getName()).size();
            int actualVmsSize = SystemManager.virtualGuestsForHostList(user, host.getId(), null).size();
            assertEquals(expectedVmsSize, actualVmsSize,
                    "Expected to find " + expectedVmsSize + " guests for host " + host.getName() +
                            " but got " + actualVmsSize);
        }
    }

    /**
     * Test that all virtual systems are returned when no filter is provided
     */
    @Test
    public void testVirtualSystemsReturnAllSystemsWhenNoFilter() {
        Request request = SparkTestUtils.createMockRequest(BASE_URI);
        Object virtualSystemsObject = systemsController.virtualSystems(request, response, user);
        assertNotNull(virtualSystemsObject);
        Map<String, Object> virtualSystemsAsMap = new Gson().fromJson(virtualSystemsObject.toString(), Map.class);
        assertEquals(5, ((Collection) virtualSystemsAsMap.get("items")).size());
    }

    /**
     * Test that all virtual systems are returned when blank params are provided
     */
    @Test
    public void testVirtualSystemsReturnAllSystemsWhenBlankParams() {
        List<Map<String, Object>> items = getVirtualSystemsItems(StringUtils.EMPTY, StringUtils.EMPTY);
        assertEquals(5, items.size());
    }

    /**
     * Test that virtual systems are returned when filtered exact host or vm names
     */
    @Test
    public void testVirtualSystemsReturnAllHostVMsWhenFilterByExactName() {
        SERVER_NAMES_BY_HOST_SERVER_NAME_NAME_MAP.entrySet().forEach(e -> {
            String hostName = e.getKey();
            List<String> hostVmNames = e.getValue();

            List<Map<String, Object>> hostFilteredItems = getVirtualSystemsItems(KEY_HOST_SERVER_NAME, hostName);
            assertEquals(hostVmNames.size(), hostFilteredItems.size(),
                    "Expected host  " + hostName + " to return " + hostVmNames.size() +
                            " but got " + hostFilteredItems.size());
            assertTrue(hostFilteredItems.stream().allMatch(
                            obj -> obj.get(PROPERTY_HOST_SERVER_NAME).equals(hostName)),
                    "Querying for " + hostName + " should return only \"+hostName+\"'s vms"
            );

            hostVmNames.forEach(vmName -> {
                List<Map<String, Object>> vmFilteredItems = getVirtualSystemsItems(KEY_SERVER_NAME, vmName);
                assertEquals(1, vmFilteredItems.size());
                assertEquals(vmName, vmFilteredItems.get(0).get(PROPERTY_SERVER_NAME));
            });
        });
    }

    /**
     * Test virtual systems are returned when filtered by common/partial name
     */
    @Test
    public void testVirtualSystemsReturnAllWhenFilterByPartialName() {
        List<Map<String, Object>> items = getVirtualSystemsItems(KEY_HOST_SERVER_NAME, "host");
        assertEquals(5, items.size());
        assertTrue(items.stream().allMatch(obj -> obj.get(PROPERTY_HOST_SERVER_NAME).toString().contains("host")));

        assertEquals(2, getVirtualSystemsItems(KEY_SERVER_NAME, "VmA").size());
        assertEquals(2, getVirtualSystemsItems(KEY_SERVER_NAME, "VmB").size());
        assertEquals(1, getVirtualSystemsItems(KEY_SERVER_NAME, "VmC").size());
        assertEquals(5, getVirtualSystemsItems(KEY_SERVER_NAME, "Vm").size());
    }

    /**
     * Test no virtual systems are returned when filtered by a non-existent host name
     */
    @Test
    public void testVirtualSystemsReturnsNoneWhenNameMatches() {
        assertEquals(0, getVirtualSystemsItems(KEY_HOST_SERVER_NAME, "hostY").size());
        assertEquals(0, getVirtualSystemsItems(KEY_SERVER_NAME, "VmY").size());
    }


    /**
     * Sets up the query parameters and executes @link{SystemsController#virtualSystems}
     *
     * @param queryColumn the name of the column to filter by
     * @param query       the value to filter by
     * @return filtered virtual systems
     */
    private List<Map<String, Object>> getVirtualSystemsItems(String queryColumn, String query) {
        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("qc", queryColumn);
        queryParams.put("q", query);
        Request request = SparkTestUtils.createMockRequestWithParams(BASE_URI, queryParams);

        //
        Object virtualSystemsObject = systemsController.virtualSystems(request, response, user);

        //
        assertNotNull(virtualSystemsObject);
        Map<String, Object> virtualSystemsAsMap = GSON.fromJson(virtualSystemsObject.toString(), Map.class);

        return (List<Map<String, Object>>) virtualSystemsAsMap.get("items");
    }

    @Test
    public void testVirtualListPageQueryParamsConversion() {
        final String paramQC = "dummy_param_qc";
        final String paramQ = "dummy_param_q";

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("qc", paramQC);
        queryParams.put("q", paramQ);
        Request request = SparkTestUtils.createMockRequestWithParams(BASE_URI, queryParams);
        ModelAndView virtualSystemsModelAndView = systemsController.virtualListPage(request, response, user);

        assertNotNull(virtualSystemsModelAndView);
        assertNotNull(virtualSystemsModelAndView.getModel());

        HashMap model = (HashMap) virtualSystemsModelAndView.getModel();
        assertAll(
                () -> assertTrue(model.containsKey("queryColumn")),
                () -> assertEquals(String.format("'%s'", paramQC), model.get("queryColumn"))
        );
        assertAll(
                () -> assertTrue(model.containsKey("query")),
                () -> assertEquals(String.format("'%s'", paramQ), model.get("query"))
        );
    }


}
