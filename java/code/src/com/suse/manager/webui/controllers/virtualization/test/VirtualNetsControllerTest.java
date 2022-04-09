/*
 * Copyright (c) 2018 SUSE LLC
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
package com.suse.manager.webui.controllers.virtualization.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.virtualization.VirtualizationNetworkCreateAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationNetworkStateChangeAction;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.frontend.context.Context;
import com.redhat.rhn.frontend.dto.ScheduledAction;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.ServerTestUtils;

import com.suse.manager.reactor.messaging.test.SaltTestUtils;
import com.suse.manager.virtualization.NetworkDefinition;
import com.suse.manager.virtualization.VirtualizationActionHelper;
import com.suse.manager.virtualization.test.TestVirtManager;
import com.suse.manager.webui.controllers.test.BaseControllerTestCase;
import com.suse.manager.webui.controllers.virtualization.VirtualNetsController;
import com.suse.manager.webui.controllers.virtualization.gson.VirtualNetworkInfoJson;
import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.test.TestSaltApi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.hamcrest.collection.IsMapContaining;
import org.jmock.Expectations;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

public class VirtualNetsControllerTest extends BaseControllerTestCase {

    private TaskomaticApi taskomaticMock;
    private VirtManager virtManager;
    private Server host;
    private static final Gson GSON = new GsonBuilder().create();

    /**
     * {@inheritDoc}
     */
    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);
        VirtualizationActionHelper.setTaskomaticApi(taskomaticMock);
        context().checking(new Expectations() {{
            ignoring(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
        }});

        virtManager = new TestVirtManager() {
            @Override
            public void updateLibvirtEngine(MinionServer minion) {
            }

            @Override
            public Map<String, JsonObject> getNetworks(String minionId) {
                return SaltTestUtils.getSaltResponse(
                        "/com/suse/manager/webui/controllers/virtualization/test/virt.net.info.json",
                        null,
                        new TypeToken<Map<String, JsonObject>>() { })
                        .orElse(Collections.emptyMap());
            }

            @Override
            public List<JsonObject> getHostDevices(String minionId) {
                return SaltTestUtils.getSaltResponse(
                        "/com/suse/manager/webui/controllers/virtualization/test/virt.node_devices.json",
                        null,
                        new TypeToken<List<JsonObject>>() { })
                        .orElse(Collections.emptyList());
            }
        };

        SaltApi saltApi = new TestSaltApi();
        MonitoringManager monitoringManager = new FormulaMonitoringManager(saltApi);
        ServerGroupManager serverGroupManager = new ServerGroupManager(saltApi);
        SystemEntitlementManager systemEntitlementManager = new SystemEntitlementManager(
                new SystemUnentitler(virtManager, monitoringManager, serverGroupManager),
                new SystemEntitler(new TestSaltApi(), virtManager, monitoringManager, serverGroupManager)
        );

        host = ServerTestUtils.createVirtHostWithGuests(user, 1, true, systemEntitlementManager);
        host.asMinionServer().get().setMinionId("testminion.local");

        Context.getCurrentContext().setTimezone(TimeZone.getTimeZone("Europe/Paris"));
    }

    @Test
    public void testData() throws Exception {
        VirtualNetsController virtualNetsController = new VirtualNetsController(virtManager);
        String json = virtualNetsController.data(getRequestWithCsrf(
                "/manager/api/systems/details/virtualization/nets/:sid/data", host.getId()), response, user, host);

        List<VirtualNetworkInfoJson> nets = GSON.fromJson(
                json, new TypeToken<List<VirtualNetworkInfoJson>>() { }.getType());
        assertTrue(nets.stream().filter(net -> net.getName().equals("net0")).findFirst().isPresent());
        VirtualNetworkInfoJson net1 = nets.stream().filter(net -> net.getName().equals("net1")).findFirst().get();
        assertEquals("virbr0", net1.getBridge());
        assertFalse(net1.isActive());
        assertFalse(net1.isAutostart());
        assertTrue(net1.isPersistent());
        assertEquals("860e49a3-d227-4105-95ca-d19dc8f0c8b6", net1.getUuid());

        VirtualNetworkInfoJson net0 = nets.stream().filter(net -> net.getName().equals("net0")).findFirst().get();
        assertNull(net0.getBridge());
    }

    @Test
    public void testDevices() throws Exception {
        VirtualNetsController virtualNetsController = new VirtualNetsController(virtManager);
        String json = virtualNetsController.devices(getRequestWithCsrf(
                "/manager/api/systems/details/virtualization/nets/:sid/devices", host.getId()), response, user, host);

        List<JsonObject> devs = GSON.fromJson(json, new TypeToken<List<JsonObject>>() { }.getType());

        // Physical function device
        JsonObject eth0 = devs.stream().filter(dev -> dev.get("name").getAsString().equals("eth0")).findFirst().get();
        assertTrue(eth0.get("PF").getAsBoolean());
        assertFalse(eth0.get("VF").getAsBoolean());

        // Non SR-IOV device (can be a device where SR-IOV is not enabled from the host side)
        JsonObject eth1 = devs.stream().filter(dev -> dev.get("name").getAsString().equals("eth1")).findFirst().get();
        assertFalse(eth1.get("PF").getAsBoolean());
        assertFalse(eth1.get("VF").getAsBoolean());

        // Virtual function device
        JsonObject eth4 = devs.stream().filter(dev -> dev.get("name").getAsString().equals("eth4")).findFirst().get();
        assertFalse(eth4.get("PF").getAsBoolean());
        assertTrue(eth4.get("VF").getAsBoolean());
        assertEquals("42:8a:c6:98:8d:00", eth4.get("address").getAsString());
        assertEquals("0000:3d:02.6", eth4.get("PCI address").getAsString());
        assertEquals("down", eth4.get("state").getAsString());
    }

    @Test
    public void testStart() throws Exception {
        VirtualNetsController virtualNetsController = new VirtualNetsController(virtManager);
        String json = virtualNetsController.start(
                getPostRequestWithCsrfAndBody("/manager/api/systems/details/virtualization/nets/:sid/start",
                                              "{names: [\"net0\"]}",
                                              host.getId()),
                response, user, host);

        // Ensure the start action is queued
        DataResult<ScheduledAction> actions = ActionManager.pendingActions(user, null);
        assertEquals(1, actions.size());
        assertEquals(ActionFactory.TYPE_VIRTUALIZATION_NETWORK_STATE_CHANGE.getName(), actions.get(0).getTypeName());

        Action action = ActionManager.lookupAction(user, actions.get(0).getId());
        VirtualizationNetworkStateChangeAction virtAction = (VirtualizationNetworkStateChangeAction) action;
        assertEquals("net0", virtAction.getNetworkName());
        assertEquals("start", virtAction.getState());

        // Check the returned message
        Map<String, Long> model = GSON.fromJson(json, new TypeToken<Map<String, Long>>() { }.getType());
        assertTrue(IsMapContaining.hasEntry("net0", action.getId()).matches(model));
    }

    @Test
    public void testStop() throws Exception {
        VirtualNetsController virtualNetsController = new VirtualNetsController(virtManager);
        String json = virtualNetsController.stop(
                getPostRequestWithCsrfAndBody("/manager/api/systems/details/virtualization/nets/:sid/stop",
                        "{names: [\"net0\"]}",
                        host.getId()),
                response, user, host);

        // Ensure the stop action is queued
        DataResult<ScheduledAction> actions = ActionManager.pendingActions(user, null);
        assertEquals(1, actions.size());
        assertEquals(ActionFactory.TYPE_VIRTUALIZATION_NETWORK_STATE_CHANGE.getName(), actions.get(0).getTypeName());

        Action action = ActionManager.lookupAction(user, actions.get(0).getId());
        VirtualizationNetworkStateChangeAction virtAction = (VirtualizationNetworkStateChangeAction) action;
        assertEquals("net0", virtAction.getNetworkName());
        assertEquals("stop", virtAction.getState());

        // Check the returned message
        Map<String, Long> model = GSON.fromJson(json, new TypeToken<Map<String, Long>>() { }.getType());
        assertTrue(IsMapContaining.hasEntry("net0", action.getId()).matches(model));
    }

    @Test
    public void testDelete() throws Exception {
        VirtualNetsController virtualNetsController = new VirtualNetsController(virtManager);
        String json = virtualNetsController.delete(
                getPostRequestWithCsrfAndBody("/manager/api/systems/details/virtualization/nets/:sid/delete",
                        "{names: [\"net0\"]}",
                        host.getId()),
                response, user, host);

        // Ensure the stop action is queued
        DataResult<ScheduledAction> actions = ActionManager.pendingActions(user, null);
        assertEquals(1, actions.size());
        assertEquals(ActionFactory.TYPE_VIRTUALIZATION_NETWORK_STATE_CHANGE.getName(), actions.get(0).getTypeName());

        Action action = ActionManager.lookupAction(user, actions.get(0).getId());
        VirtualizationNetworkStateChangeAction virtAction = (VirtualizationNetworkStateChangeAction) action;
        assertEquals("net0", virtAction.getNetworkName());
        assertEquals("delete", virtAction.getState());

        // Check the returned message
        Map<String, Long> model = GSON.fromJson(json, new TypeToken<Map<String, Long>>() { }.getType());
        assertTrue(IsMapContaining.hasEntry("net0", action.getId()).matches(model));
    }

    @Test
    public void testCreate() throws Exception {
        VirtualNetsController virtualNetsController = new VirtualNetsController(virtManager);
        String json = virtualNetsController.create(
                getPostRequestWithCsrfAndBody("/manager/api/systems/details/virtualization/nets/:sid/create",
                        "{names: ['net0'], definition: {type: 'bridge', autostart: true, bridge: 'ovs0', " +
                                "virtualport: {type: 'openvswitch', interfaceid: 'thevportuuid'}, " +
                                "vlans:[{tag: 41}]}, earliest: '2021-02-17T10:09:00.000Z'}",
                        host.getId()),
                response, user, host);

        // Ensure the stop action is queued
        DataResult<ScheduledAction> actions = ActionManager.pendingActions(user, null);
        assertEquals(1, actions.size());
        assertEquals(ActionFactory.TYPE_VIRTUALIZATION_NETWORK_CREATE.getName(), actions.get(0).getTypeName());

        Action action = ActionManager.lookupAction(user, actions.get(0).getId());
        VirtualizationNetworkCreateAction virtAction = (VirtualizationNetworkCreateAction) action;
        assertEquals("net0", virtAction.getNetworkName());
        NetworkDefinition def = virtAction.getDefinition();
        assertEquals("bridge", def.getForwardMode());
        assertEquals(Optional.of("ovs0"), def.getBridge());
        assertTrue(def.isAutostart());
        assertEquals("openvswitch", def.getVirtualPort().get().getType());
        assertEquals(Optional.of("thevportuuid"), def.getVirtualPort().get().getInterfaceId());
        assertEquals(41, def.getVlans().get(0).getTag());

        // Check the returned message
        Map<String, Long> model = GSON.fromJson(json, new TypeToken<Map<String, Long>>() { }.getType());
        assertTrue(IsMapContaining.hasEntry("net0", action.getId()).matches(model));
    }

    @Test
    public void testCreateNat() throws Exception {
        VirtualNetsController virtualNetsController = new VirtualNetsController(virtManager);
        String json = virtualNetsController.create(
                getPostRequestWithCsrfAndBody("/manager/api/systems/details/virtualization/nets/:sid/create",
                        "{names: ['net0'], definition: {type: 'nat', autostart: false, " +
                                "nat: {address: {start: '192.168.10.3', end: '192.168.10.4'}, " +
                                "port: {start: '1234', end: '1235'}}, ipv4: {address: '192.168.10.0', prefix: 24}}, " +
                                "earliest: '2021-02-17T10:09:00.000Z'}",
                        host.getId()),
                response, user, host);

        // Ensure the stop action is queued
        DataResult<ScheduledAction> actions = ActionManager.pendingActions(user, null);
        assertEquals(1, actions.size());
        assertEquals(ActionFactory.TYPE_VIRTUALIZATION_NETWORK_CREATE.getName(), actions.get(0).getTypeName());

        Action action = ActionManager.lookupAction(user, actions.get(0).getId());
        VirtualizationNetworkCreateAction virtAction = (VirtualizationNetworkCreateAction) action;
        assertEquals("net0", virtAction.getNetworkName());
        NetworkDefinition def = virtAction.getDefinition();
        assertEquals("nat", def.getForwardMode());
        assertEquals(Optional.empty(), def.getBridge());
        assertFalse(def.isAutostart());
        assertEquals("192.168.10.3", def.getNat().orElseThrow().getAddress().orElseThrow().getStart());
        assertEquals("192.168.10.4", def.getNat().orElseThrow().getAddress().orElseThrow().getEnd());
        assertEquals(Integer.valueOf(1234), def.getNat().get().getPort().orElseThrow().getStart());
        assertEquals(Integer.valueOf(1235), def.getNat().get().getPort().orElseThrow().getEnd());
        assertEquals("192.168.10.0", def.getIpv4().orElseThrow().getAddress());
        assertEquals(Integer.valueOf(24), def.getIpv4().orElseThrow().getPrefix());

        // Check the returned message
        Map<String, Long> model = GSON.fromJson(json, new TypeToken<Map<String, Long>>() { }.getType());
        assertTrue(IsMapContaining.hasEntry("net0", action.getId()).matches(model));
    }
}
