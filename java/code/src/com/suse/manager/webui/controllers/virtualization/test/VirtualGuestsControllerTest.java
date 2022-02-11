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

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.virtualization.VirtualizationSetMemoryGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationSetVcpusGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationShutdownGuestAction;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.VirtualInstance;
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
import com.suse.manager.virtualization.DomainCapabilitiesJson;
import com.suse.manager.virtualization.GuestDefinition;
import com.suse.manager.virtualization.HostInfo;
import com.suse.manager.virtualization.VirtualizationActionHelper;
import com.suse.manager.virtualization.VmInfoJson;
import com.suse.manager.virtualization.test.TestVirtManager;
import com.suse.manager.webui.controllers.test.BaseControllerTestCase;
import com.suse.manager.webui.controllers.virtualization.VirtualGuestsController;
import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.test.TestSaltApi;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import org.jmock.Expectations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TimeZone;

import spark.HaltException;
import spark.ModelAndView;

/**
 * Tests for VirtualGuestsController
 */
public class VirtualGuestsControllerTest extends BaseControllerTestCase {

    private TaskomaticApi taskomaticMock;
    private static final Gson GSON = new GsonBuilder().create();
    private Server host;
    private VirtManager virtManager;
    private VirtualGuestsController virtualGuestsController;
    private String guid = "b99a81764f40498d8e612f6ade654fe2";
    private String uuid = "b99a8176-4f40-498d-8e61-2f6ade654fe2";

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void setUp() throws Exception {
        super.setUp();

        taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);
        VirtualizationActionHelper.setTaskomaticApi(taskomaticMock);
        context().checking(new Expectations() {{
            ignoring(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
        }});
        Context.getCurrentContext().setTimezone(TimeZone.getTimeZone("UTC"));

        virtManager = new TestVirtManager() {

            @Override
            public Optional<Map<String, JsonElement>> getCapabilities(String minionId) {
                return SaltTestUtils.getSaltResponse(
                        "/com/suse/manager/webui/controllers/virtualization/test/virt.guest.allcaps.json", null,
                        new TypeToken<Map<String, JsonElement>>() { });
            }

            @Override
            public void updateLibvirtEngine(MinionServer minion) {
                assertTrue(minion.getMinionId().startsWith("serverfactorytest"));
            }

            @Override
            public Optional<GuestDefinition> getGuestDefinition(String minionId, String uuidIn) {
                Optional<Map<String, JsonElement>> vm = SaltTestUtils.getSaltResponse(
                        "/com/suse/manager/webui/controllers/virtualization/test/virt_utils.vm_definition.json",
                        Collections.emptyMap(),
                        new TypeToken<Map<String, JsonElement>>() { });
                return vm.map(data -> {
                    Optional<VmInfoJson> info = Optional.empty();
                    if (data.containsKey("info")) {
                        info = Optional.ofNullable(new GsonBuilder().create()
                                .fromJson(data.get("info"), new TypeToken<VmInfoJson>() { }.getType()));
                    }
                    if (data.containsKey("definition")) {
                        String xml = data.get("definition").getAsString();
                        return GuestDefinition.parse(xml, info);
                    }
                    return null;
                });
            }

            @Override
            public Optional<Map<String, Map<String, JsonElement>>> getVmInfos(String minionId) {
                return SaltTestUtils.<Map<String, Map<String, JsonElement>>>getSaltResponse(
                        "/com/suse/manager/webui/controllers/virtualization/test/virt_utils.vm.info.json",
                        Collections.emptyMap(),
                        new TypeToken<Map<String, Map<String, JsonElement>>>() { });
            }

            @Override
            public Optional<HostInfo> getHostInfo(String minionId) {
                HostInfo info = new HostInfo();
                info.setHypervisor("kvm");
                return Optional.of(info);
            }
        };

        SaltApi saltApi = new TestSaltApi();
        MonitoringManager monitoringManager = new FormulaMonitoringManager(saltApi);
        ServerGroupManager serverGroupManager = new ServerGroupManager(saltApi);
        SystemEntitlementManager systemEntitlementManager = new SystemEntitlementManager(
                new SystemUnentitler(virtManager, monitoringManager, serverGroupManager),
                new SystemEntitler(saltApi, virtManager, monitoringManager, serverGroupManager)
        );

        host = ServerTestUtils.createVirtHostWithGuests(user, 2, true, systemEntitlementManager);
        host.asMinionServer().get().setMinionId("testminion.local");
        VirtualInstance guest = host.getGuests().iterator().next();
        guest.setUuid(guid);
        guest.setName("sles12sp2");

        virtualGuestsController = new VirtualGuestsController(virtManager);

        // Clean pending actions for easier checks in the tests
        DataResult<ScheduledAction> actions = ActionManager.allActions(user, null);
        for (ScheduledAction scheduledAction : actions) {
            ActionManager.failSystemAction(user, host.getId(), scheduledAction.getId(), "test clean up");
        }
    }

    /**
     * Test getting the data from a virtual host
     *
     * @throws Exception if anything unexpected happens during the test
     */
    @SuppressWarnings("unchecked")
    public void testData() throws Exception {
        int size = host.getGuests().size();
        VirtualInstance[] guests = host.getGuests().toArray(new VirtualInstance[size]);
        Long sid = host.getId();


        String json = virtualGuestsController.data(
                getRequestWithCsrf("/manager/api/systems/details/virtualization/guests/:sid/data", sid),
                response, user, host);
        List<Map<String, Object>> model = GSON.fromJson(json, List.class);

        // Sort both actual and expected arrays to ease assertions
        Arrays.sort(guests, (VirtualInstance o1, VirtualInstance o2) -> o1.getUuid().compareTo(o2.getUuid()));
        model.sort((o1, o2) -> ((String)o1.get("uuid")).compareTo((String)o2.get("uuid")));

        assertEquals(size, model.size());
        assertEquals(guests[0].getUuid(), model.get(0).get("uuid"));
        assertEquals(guests[1].getUuid(), model.get(1).get("uuid"));

        Double vCpus = (Double)model.get(0).get("vcpus");
        assertEquals(guests[0].getNumberOfCPUs().intValue(), vCpus.intValue());
        assertEquals(guests[1].getState().getLabel(), model.get(1).get("stateLabel"));
    }

    /**
     * Test a VM state change action
     *
     * @throws Exception if anything unexpected happens during the test
     */
    public void testStateChangeAction() throws Exception {
        VirtualInstance guest = host.getGuests().iterator().next();
        Long sid = host.getId();

        String json = virtualGuestsController.shutdown(
                getPostRequestWithCsrfAndBody("/manager/api/systems/details/virtualization/guests/:sid/shutdown",
                                              "{uuids: [\"" + guest.getUuid() + "\"]}",
                                              sid),
                response, user, host);

        // Make sure the shutdown action was queued
        DataResult<ScheduledAction> actions = ActionManager.pendingActions(user, null);
        assertEquals(ActionFactory.TYPE_VIRTUALIZATION_SHUTDOWN.getName(),
                     actions.get(0).getTypeName());

        Action action = ActionManager.lookupAction(user, actions.get(0).getId());
        VirtualizationShutdownGuestAction virtAction = (VirtualizationShutdownGuestAction)action;
        assertEquals(guest.getUuid(), virtAction.getUuid());

        // Check the response
        Map<String, Long> model = GSON.fromJson(json, new TypeToken<Map<String, Long>>() { }.getType());
        assertEquals(action.getId(), model.get(guest.getUuid()));
    }


    /**
     * Test a VM vcpu change action
     *
     * @throws Exception if anything unexpected happens during the test
     */
    public void testSetVcpuAction() throws Exception {
        VirtualInstance guest = host.getGuests().iterator().next();
        Long sid = host.getId();

        Integer vcpus = 3;
        String json = virtualGuestsController.setVcpu(
                getPostRequestWithCsrfAndBody("/manager/api/systems/details/virtualization/guests/:sid/:action",
                                              "{uuids: [\"" + guest.getUuid() + "\"], value: " + vcpus + "}",
                                              sid, "setVcpu"),
                response, user, host);

        // Make sure the setVpu action was queued
        DataResult<ScheduledAction> actions = ActionManager.pendingActions(user, null);
        assertEquals(ActionFactory.TYPE_VIRTUALIZATION_SET_VCPUS.getName(),
                     actions.get(0).getTypeName());

        Action action = ActionManager.lookupAction(user, actions.get(0).getId());
        VirtualizationSetVcpusGuestAction virtAction = (VirtualizationSetVcpusGuestAction)action;
        assertEquals(vcpus, virtAction.getVcpu());

        // Check the response
        Map<String, Long> model = GSON.fromJson(json, new TypeToken<Map<String, Long>>() { }.getType());
        assertEquals(action.getId(), model.get(guest.getUuid()));
    }

    /**
     * Test a VM vcpu without value change action
     *
     * @throws Exception if anything unexpected happens during the test
     */
    public void testSetVcpuInvalidAction() throws Exception {
        VirtualInstance guest = host.getGuests().iterator().next();
        Long sid = host.getId();

        try {
            virtualGuestsController.setVcpu(
                    getPostRequestWithCsrfAndBody("/manager/api/systems/details/virtualization/guests/:sid/:action",
                                                  "{uuids: [\"" + guest.getUuid() + "\"]}",
                                                  sid, "setVcpu"),
                    response, user, host);
            fail();
        }
        catch (HaltException e) {
            // Make sure the action was not queued
            assertTrue(ActionManager.pendingActions(user, null).isEmpty());
        }
    }

    /**
     * Test a memory change on several VMs
     *
     * @throws Exception if anything unexpected happens during the test
     */
    public void testSetMemMultiAction() throws Exception {

        VirtualInstance[] guests = host.getGuests().toArray(new VirtualInstance[host.getGuests().size()]);
        Arrays.sort(guests, (VirtualInstance o1, VirtualInstance o2) -> o1.getUuid().compareTo(o2.getUuid()));
        Long sid = host.getId();

        Integer mem = 2048;
        String json = virtualGuestsController.setMemory(
                getPostRequestWithCsrfAndBody("/manager/api/systems/details/virtualization/guests/:sid/:action",
                                              "{uuids: [\"" + guests[0].getUuid() + "\", " +
                                                       "\"" + guests[1].getUuid() + "\"], " +
                                                      "value: " + mem + "}",
                                              sid, "setMemory"),
                response, user, host);

        // Make sure the setVpu action was queued
        DataResult<ScheduledAction> scheduledActions = ActionManager.pendingActions(user, null);
        ArrayList<VirtualizationSetMemoryGuestAction> virtActions = new ArrayList<VirtualizationSetMemoryGuestAction>();
        scheduledActions.stream().forEach(action -> virtActions.add(
                (VirtualizationSetMemoryGuestAction)ActionManager.lookupAction(user, action.getId())));
        virtActions.sort((VirtualizationSetMemoryGuestAction a1, VirtualizationSetMemoryGuestAction a2) ->
                a1.getUuid().compareTo(a2.getUuid()));

        assertEquals(ActionFactory.TYPE_VIRTUALIZATION_SET_MEMORY.getName(),
                scheduledActions.get(0).getTypeName());
        assertEquals(Integer.valueOf(mem), virtActions.get(0).getMemory());
        assertEquals(guests[0].getUuid(), virtActions.get(0).getUuid());

        assertEquals(ActionFactory.TYPE_VIRTUALIZATION_SET_MEMORY.getName(),
                scheduledActions.get(1).getTypeName());
        assertEquals(Integer.valueOf(mem), virtActions.get(1).getMemory());
        assertEquals(guests[1].getUuid(), virtActions.get(1).getUuid());


        // Check the response
        Map<String, Long> model = GSON.fromJson(json, new TypeToken<Map<String, Long>>() { }.getType());
        assertEquals(virtActions.get(0).getId(), model.get(guests[0].getUuid()));
        assertEquals(virtActions.get(1).getId(), model.get(guests[1].getUuid()));
    }

    /**
     * Test the API querying the XML definition of a VM using salt.
     *
     * @throws Exception if anything unexpected happens during the test
     */
    public void testGetGuest() throws Exception {
        String json = virtualGuestsController.getGuest(
                getRequestWithCsrf("/manager/api/systems/details/virtualization/guests/:sid/guest/:uuid",
                        host.getId(), guid),
                response, user, host);
        GuestDefinition def = GSON.fromJson(json, new TypeToken<GuestDefinition>() { }.getType());
        assertEquals(uuid, def.getUuid());
        assertEquals("sles12sp2", def.getName());
        assertEquals(1024 * 1024, def.getMaxMemory());
        assertEquals("spice", def.getGraphics().getType());
        assertEquals(5903, def.getGraphics().getPort());

        assertEquals(1, def.getInterfaces().size());
        assertEquals("network", def.getInterfaces().get(0).getType());
        assertEquals("default", def.getInterfaces().get(0).getSource());

        assertEquals(5, def.getDisks().size());
        assertEquals("file", def.getDisks().get(0).getType());
        assertEquals("disk", def.getDisks().get(0).getDevice());
        assertEquals("qcow2", def.getDisks().get(0).getFormat());
        assertEquals("vda", def.getDisks().get(0).getTarget());
        assertEquals("virtio", def.getDisks().get(0).getBus());
        assertEquals("/srv/vms/sles12sp2.qcow2", def.getDisks().get(0).getSource().get("file"));

        assertEquals("file", def.getDisks().get(1).getType());
        assertEquals("cdrom", def.getDisks().get(1).getDevice());
        assertEquals("raw", def.getDisks().get(1).getFormat());
        assertEquals("hda", def.getDisks().get(1).getTarget());
        assertEquals("ide", def.getDisks().get(1).getBus());
        assertEquals(null, def.getDisks().get(1).getSource());

        assertEquals("volume", def.getDisks().get(2).getType());
        assertEquals("disk", def.getDisks().get(2).getDevice());
        assertEquals("raw", def.getDisks().get(2).getFormat());
        assertEquals("vdb", def.getDisks().get(2).getTarget());
        assertEquals("virtio", def.getDisks().get(2).getBus());
        assertEquals("ses-pool", def.getDisks().get(2).getSource().get("pool"));
        assertEquals("test-vol", def.getDisks().get(2).getSource().get("volume"));

        assertEquals("volume", def.getDisks().get(3).getType());
        assertEquals("disk", def.getDisks().get(3).getDevice());
        assertEquals("raw", def.getDisks().get(3).getFormat());
        assertEquals("vdc", def.getDisks().get(3).getTarget());
        assertEquals("virtio", def.getDisks().get(3).getBus());
        assertEquals("iscsi-pool", def.getDisks().get(3).getSource().get("pool"));
        assertEquals("unit:0:0:1", def.getDisks().get(3).getSource().get("volume"));

        assertEquals("block", def.getDisks().get(4).getType());
        assertEquals("disk", def.getDisks().get(4).getDevice());
        assertEquals("raw", def.getDisks().get(4).getFormat());
        assertEquals("vdd", def.getDisks().get(4).getTarget());
        assertEquals("virtio", def.getDisks().get(4).getBus());
        assertEquals("/dev/disk/by-path/pci-0000:00:0b.0-scsi-0:0:0:0",
                def.getDisks().get(4).getSource().get("dev"));
    }

    /**
     * Test the API querying the domains capabilities of a virtual host using salt.
     *
     * @throws Exception if anything unexpected happens during the test
     */
    public void testGetDomainsCapabilities() throws Exception {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("[\"ide\", \"fdc\", \"scsi\", \"virtio\", \"usb\"]", "[\"ide\", \"fdc\", \"scsi\", \"usb\"]");

        String json = virtualGuestsController.getDomainsCapabilities(
                getRequestWithCsrf("/manager/api/systems/details/virtualization/guests/:sid/domains_capabilities",
                        host.getId()), response, user, host);

        DomainsCapsJson caps = GSON.fromJson(json, new TypeToken<DomainsCapsJson>() { }.getType());
        assertTrue(caps.getOsTypes().contains("hvm"));
        assertEquals("i686", caps.getDomainsCaps().get(0).getArch());

        assertEquals("kvm", caps.getDomainsCaps().get(0).getDomain());
        assertTrue(caps.getDomainsCaps().get(0).getDevices().get("disk").get("bus").contains("virtio"));
        assertFalse(caps.getDomainsCaps().get(1).getDevices().get("disk").get("bus").contains("virtio"));
    }

    public void testShow() {
        ModelAndView page = virtualGuestsController.show(
                getRequestWithCsrf("/manager/systems/details/virtualization/guests/:sid",
                        host.getId()), response, user, host);
        Map<String, Object> model = (Map<String, Object>) page.getModel();
        assertEquals("{\"hypervisor\":\"kvm\",\"cluster_other_nodes\":[]}", model.get("hostInfo"));
    }

    public void testShowVHM() throws Exception {
        Server vhmHost = ServerTestUtils.createForeignSystem(user, "server_digital_id");
        ModelAndView page = virtualGuestsController.show(
                getRequestWithCsrf("/manager/systems/details/virtualization/guests/:sid",
                        vhmHost.getId()), response, user, vhmHost);
        Map<String, Object> model = (Map<String, Object>) page.getModel();
        assertEquals("{}", model.get("hostInfo"));
    }

    /**
     * Represents the output of VirtualGuestsController.getDomainsCapabilities.
     * There is no need to share this structure since these data will only be used from Javascript.
     */
    private class DomainsCapsJson {
        private List<String> osTypes;
        private List<DomainCapabilitiesJson> domainsCaps;

        public List<String> getOsTypes() {
            return osTypes;
        }

        public void setOsTypes(List<String> osTypesIn) {
            osTypes = osTypesIn;
        }

        public List<DomainCapabilitiesJson> getDomainsCaps() {
            return domainsCaps;
        }

        public void setDomainsCaps(List<DomainCapabilitiesJson> domainsCapsIn) {
            domainsCaps = domainsCapsIn;
        }
    }
}
