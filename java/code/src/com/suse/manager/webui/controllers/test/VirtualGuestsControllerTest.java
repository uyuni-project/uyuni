/**
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

package com.suse.manager.webui.controllers.test;

import static junit.framework.Assert.assertEquals;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.virtualization.VirtualizationSetMemoryAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationSetVcpusAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationShutdownAction;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.frontend.dto.ScheduledAction;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.system.VirtualizationActionCommand;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.ServerTestUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.suse.manager.reactor.messaging.test.SaltTestUtils;
import com.suse.manager.virtualization.DomainCapabilitiesJson;
import com.suse.manager.virtualization.GuestDefinition;
import com.suse.manager.webui.controllers.VirtualGuestsController;
import com.suse.manager.virtualization.test.TestVirtManager;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.impl.SaltService;

import org.jmock.Expectations;

import java.util.*;

import spark.HaltException;

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
        VirtualizationActionCommand.setTaskomaticApi(taskomaticMock);
        context().checking(new Expectations() {{
            ignoring(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
        }});

        virtManager = new TestVirtManager() {

            @Override
            public Optional<Map<String, JsonElement>> getCapabilities(String minionId) {
                return SaltTestUtils.getSaltResponse(
                        "/com/suse/manager/webui/controllers/test/virt.guest.allcaps.json", null,
                        new TypeToken<Map<String, JsonElement>>() { });
            }

            @Override
            public void updateLibvirtEngine(MinionServer minion) {
                assertTrue(minion.getMinionId().startsWith("serverfactorytest"));
            }

            @Override
            public Optional<GuestDefinition> getGuestDefinition(String minionId, String domainName) {
                return SaltTestUtils.<String>getSaltResponse(
                        "/com/suse/manager/reactor/messaging/test/virt.guest.definition.xml", Collections.emptyMap(), null)
                        .map(GuestDefinition::parse);
            }
        };

        SystemEntitlementManager systemEntitlementManager = new SystemEntitlementManager(
                new SystemUnentitler(),
                new SystemEntitler(new SaltService(), virtManager)
        );

        host = ServerTestUtils.createVirtHostWithGuests(user, 2, true, systemEntitlementManager);
        host.asMinionServer().get().setMinionId("testminion.local");
        host.getGuests().iterator().next().setUuid(guid);

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
                response, user);
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
    @SuppressWarnings("unchecked")
    public void testStateChangeAction() throws Exception {
        VirtualInstance guest = host.getGuests().iterator().next();
        Long sid = host.getId();

        String json = virtualGuestsController.action(
                getPostRequestWithCsrfAndBody("/manager/api/systems/details/virtualization/guests/:sid/:action",
                                              "{uuids: [\"" + guest.getUuid() + "\"]}",
                                              sid, "shutdown"),
                response, user);

        // Make sure the shutdown action was queued
        DataResult<ScheduledAction> actions = ActionManager.pendingActions(user, null);
        assertEquals(ActionFactory.TYPE_VIRTUALIZATION_SHUTDOWN.getName(),
                     actions.get(0).getTypeName());

        Action action = ActionManager.lookupAction(user, actions.get(0).getId());
        VirtualizationShutdownAction virtAction = (VirtualizationShutdownAction)action;
        assertEquals(guest.getUuid(), virtAction.getUuid());

        // Check the response
        Map<String, Long> model = GSON.fromJson(json, new TypeToken<Map<String, Long>>() {}.getType());
        assertEquals(action.getId(), model.get(guest.getUuid()));
    }


    /**
     * Test a VM vcpu change action
     *
     * @throws Exception if anything unexpected happens during the test
     */
    @SuppressWarnings("unchecked")
    public void testSetVcpuAction() throws Exception {
        VirtualInstance guest = host.getGuests().iterator().next();
        Long sid = host.getId();

        Integer vcpus = 3;
        String json = virtualGuestsController.action(
                getPostRequestWithCsrfAndBody("/manager/api/systems/details/virtualization/guests/:sid/:action",
                                              "{uuids: [\"" + guest.getUuid() + "\"], value: " + vcpus + "}",
                                              sid, "setVcpu"),
                response, user);

        // Make sure the setVpu action was queued
        DataResult<ScheduledAction> actions = ActionManager.pendingActions(user, null);
        assertEquals(ActionFactory.TYPE_VIRTUALIZATION_SET_VCPUS.getName(),
                     actions.get(0).getTypeName());

        Action action = ActionManager.lookupAction(user, actions.get(0).getId());
        VirtualizationSetVcpusAction virtAction = (VirtualizationSetVcpusAction)action;
        assertEquals(vcpus, virtAction.getVcpu());

        // Check the response
        Map<String, Long> model = GSON.fromJson(json, new TypeToken<Map<String, Long>>() {}.getType());
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
            virtualGuestsController.action(
                    getPostRequestWithCsrfAndBody("/manager/api/systems/details/virtualization/guests/:sid/:action",
                                                  "{uuids: [\"" + guest.getUuid() + "\"]}",
                                                  sid, "setVcpu"),
                    response, user);
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
    @SuppressWarnings("unchecked")
    public void testSetMemMultiAction() throws Exception {

        VirtualInstance[] guests = host.getGuests().toArray(new VirtualInstance[host.getGuests().size()]);
        Arrays.sort(guests, (VirtualInstance o1, VirtualInstance o2) -> o1.getUuid().compareTo(o2.getUuid()));
        Long sid = host.getId();

        Integer mem = 2048;
        String json = virtualGuestsController.action(
                getPostRequestWithCsrfAndBody("/manager/api/systems/details/virtualization/guests/:sid/:action",
                                              "{uuids: [\"" + guests[0].getUuid() + "\", " +
                                                       "\"" + guests[1].getUuid() + "\"], " +
                                                      "value: " + mem + "}",
                                              sid, "setMemory"),
                response, user);

        // Make sure the setVpu action was queued
        DataResult<ScheduledAction> scheduledActions = ActionManager.pendingActions(user, null);
        ArrayList<VirtualizationSetMemoryAction> virtActions = new ArrayList<VirtualizationSetMemoryAction>();
        scheduledActions.stream().forEach(action -> virtActions.add(
                (VirtualizationSetMemoryAction)ActionManager.lookupAction(user, action.getId())));
        virtActions.sort((VirtualizationSetMemoryAction a1, VirtualizationSetMemoryAction a2) ->
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
        Map<String, Long> model = GSON.fromJson(json, new TypeToken<Map<String, Long>>() {}.getType());
        assertEquals(virtActions.get(0).getId(), model.get(guests[0].getUuid()));
        assertEquals(virtActions.get(1).getId(), model.get(guests[1].getUuid()));
    }

    /**
     * Test the API querying the XML definition of a VM using salt.
     *
     * @throws Exception if anything unexpected happens during the test
     */
    @SuppressWarnings("unchecked")
    public void testGetGuest() throws Exception {
        String json = virtualGuestsController.getGuest(
                getRequestWithCsrf("/manager/api/systems/details/virtualization/guests/:sid/guest/:uuid",
                        host.getId(), guid),
                response, user);
        GuestDefinition def = GSON.fromJson(json, new TypeToken<GuestDefinition>() {}.getType());
        assertEquals(uuid, def.getUuid());
        assertEquals("sles12sp2", def.getName());
        assertEquals(1024*1024, def.getMaxMemory());
        assertEquals("spice", def.getGraphics().getType());
        assertEquals(5903, def.getGraphics().getPort());

        assertEquals(1, def.getInterfaces().size());
        assertEquals("network", def.getInterfaces().get(0).getType());
        assertEquals("default", def.getInterfaces().get(0).getSource());

        assertEquals(2, def.getDisks().size());
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
                        host.getId()), response, user);

        DomainsCapsJson caps = GSON.fromJson(json, new TypeToken<DomainsCapsJson>() {}.getType());
        assertTrue(caps.osTypes.contains("hvm"));
        assertEquals("i686", caps.domainsCaps.get(0).getArch());

        assertEquals("kvm", caps.domainsCaps.get(0).getDomain());
        assertTrue(caps.domainsCaps.get(0).getDevices().get("disk").get("bus").contains("virtio"));
        assertFalse(caps.domainsCaps.get(1).getDevices().get("disk").get("bus").contains("virtio"));
    }

    /**
     * Represents the output of VirtualGuestsController.getDomainsCapabilities.
     * There is no need to share this structure since these data will only be used from Javascript.
     */
    private class DomainsCapsJson {
        public List<String> osTypes;
        public List<DomainCapabilitiesJson> domainsCaps;
    }
}
