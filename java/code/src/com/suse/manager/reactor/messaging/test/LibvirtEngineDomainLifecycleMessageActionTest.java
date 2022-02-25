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
package com.suse.manager.reactor.messaging.test;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.frontend.dto.VirtualSystemOverview;
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.reactor.messaging.AbstractLibvirtEngineMessage;
import com.suse.manager.reactor.messaging.LibvirtEngineDomainLifecycleMessageAction;
import com.suse.manager.virtualization.GuestDefinition;
import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.salt.netapi.datatypes.Event;
import com.suse.salt.netapi.event.EngineEvent;
import com.suse.salt.netapi.parser.JsonParser;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.jmock.Expectations;
import org.jmock.States;
import org.jmock.imposters.ByteBuddyClassImposteriser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Test for {@link LibvirtEngineDomainLifecycleMessageAction}
 */
public class LibvirtEngineDomainLifecycleMessageActionTest extends JMockBaseTestCaseWithUser {

    private Server host;
    private VirtManager virtManager;
    private String guid = "b99a81764f40498d8e612f6ade654fe2";
    private String uuid = "b99a8176-4f40-498d-8e61-2f6ade654fe2";

    // JsonParser for parsing events from files
    public static final JsonParser<Event> EVENTS =
            new JsonParser<>(new TypeToken<>() {
            });

    @Override
    public void setUp() throws Exception {
        super.setUp();
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);

        virtManager = mock(VirtManager.class);
        context().checking(new Expectations() { {
            allowing(virtManager).getCapabilities("testminion.local");
            will(returnValue(
                SaltTestUtils.getSaltResponse(
                        "/com/suse/manager/webui/controllers/virtualization/test/virt.guest.allcaps.json", null,
                        new TypeToken<Map<String, JsonElement>>() { })
            ));
            allowing(virtManager).updateLibvirtEngine(with(any(MinionServer.class)));
        }});

        SaltApi saltApi = new TestSaltApi();
        MonitoringManager monitoringManager = new FormulaMonitoringManager(saltApi);
        ServerGroupManager serverGroupManager = new ServerGroupManager(saltApi);
        SystemEntitlementManager systemEntitlementManager = new SystemEntitlementManager(
                new SystemUnentitler(virtManager, monitoringManager, serverGroupManager),
                new SystemEntitler(new TestSaltApi(), virtManager, monitoringManager, serverGroupManager)
        );

        host = ServerTestUtils.createVirtHostWithGuests(user, 1, true, systemEntitlementManager);
        host.getGuests().iterator().next().setUuid(guid);
        host.getGuests().iterator().next().setName("sles12sp2");
        host.asMinionServer().get().setMinionId("testminion.local");
    }

    public void testNewGuestNoRestart() throws Exception {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("sles12sp2", "sles12sp2-new");
        placeholders.put(uuid, "98eef4f7-eb7f-4be8-859d-11658506c496");
        expectGuestDefinition("98eef4f7-eb7f-4be8-859d-11658506c496",
                "/com/suse/manager/reactor/messaging/test/virt.guest.definition.xml", placeholders);
        Optional<EngineEvent> startEvent = EngineEvent.parse(getEngineEvent("virtevents.guest.started.json", null));
        AbstractLibvirtEngineMessage startMessage = AbstractLibvirtEngineMessage.create(startEvent.get());

        // Start event
        LibvirtEngineDomainLifecycleMessageAction action = new LibvirtEngineDomainLifecycleMessageAction(virtManager);
        action.execute(startMessage);

        DataResult<VirtualSystemOverview> guests = SystemManager.virtualGuestsForHostList(user, host.getId(), null);
        List<VirtualSystemOverview> newGuests = guests.stream().filter(vso -> vso.getName().equals("sles12sp2-new"))
                .collect(Collectors.toList());

        assertEquals(VirtualInstanceFactory.getInstance().getRunningState().getLabel(),
                     newGuests.get(0).getStateLabel());

        // Shutdown event
        context().checking(new Expectations() {{
            never(virtManager).startGuest(host.getMinionId(), "sles12sp2-new");
        }});
        Optional<EngineEvent> stopEvent = EngineEvent.parse(
                getEngineEvent("virtevents.guest.shutdown.json", placeholders));
        AbstractLibvirtEngineMessage stopMessage = AbstractLibvirtEngineMessage.create(stopEvent.get());

        action.execute(stopMessage);
    }

    public void testNewGuestFirstReboot() throws Exception {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("sles12sp2", "sles12sp2-new");
        placeholders.put(uuid, "98eef4f7-eb7f-4be8-859d-11658506c496");
        placeholders.put("<on_reboot>restart</on_reboot>", "<on_reboot>destroy</on_reboot>");
        expectGuestDefinition("98eef4f7-eb7f-4be8-859d-11658506c496",
                "/com/suse/manager/reactor/messaging/test/virt.guest.definition.xml", placeholders);

        // Start event
        Optional<EngineEvent> startEvent = EngineEvent.parse(getEngineEvent("virtevents.guest.started.json", null));
        AbstractLibvirtEngineMessage startMessage = AbstractLibvirtEngineMessage.create(startEvent.get());

        LibvirtEngineDomainLifecycleMessageAction action = new LibvirtEngineDomainLifecycleMessageAction(virtManager);
        action.execute(startMessage);

        // Shutdown event
        context().checking(new Expectations() {{
            oneOf(virtManager).startGuest(host.getMinionId(), "sles12sp2-new");
        }});
        Optional<EngineEvent> stopEvent = EngineEvent.parse(
                getEngineEvent("virtevents.guest.shutdown.json", placeholders));
        AbstractLibvirtEngineMessage stopMessage = AbstractLibvirtEngineMessage.create(stopEvent.get());

        action.execute(stopMessage);
    }

    public void testNewGuestFirstRebootAborted() throws Exception {
        States vmState = context().states("vm").startsAs("started");

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("sles12sp2", "sles12sp2-new");
        placeholders.put(uuid, "98eef4f7-eb7f-4be8-859d-11658506c496");
        placeholders.put("<on_reboot>restart</on_reboot>", "<on_reboot>destroy</on_reboot>");
        expectGuestDefinition("98eef4f7-eb7f-4be8-859d-11658506c496",
                "/com/suse/manager/reactor/messaging/test/virt.guest.definition.xml", placeholders,
                vmState, "started");
        expectGuestDefinition("98eef4f7-eb7f-4be8-859d-11658506c496", "virt.noguest.txt", null,
                vmState, "deleted");

        // Start event
        Optional<EngineEvent> startEvent = EngineEvent.parse(getEngineEvent("virtevents.guest.started.json", null));
        AbstractLibvirtEngineMessage startMessage = AbstractLibvirtEngineMessage.create(startEvent.get());

        LibvirtEngineDomainLifecycleMessageAction action = new LibvirtEngineDomainLifecycleMessageAction(virtManager);
        action.execute(startMessage);

        // Shutdown event
        vmState.become("deleted");

        context().checking(new Expectations() { {
            never(virtManager).startGuest(host.getMinionId(), "sles12sp2-new");
        } });
        Optional<EngineEvent> stopEvent = EngineEvent.parse(
                getEngineEvent("virtevents.guest.shutdown.json", placeholders));
        AbstractLibvirtEngineMessage stopMessage = AbstractLibvirtEngineMessage.create(stopEvent.get());

        action.execute(stopMessage);
    }

    public void testShutdownPersistent() throws Exception {
        expectGuestDefinition(uuid,
                "/com/suse/manager/reactor/messaging/test/virt.guest.definition.xml", null);
        context().checking(new Expectations() {{
            never(virtManager).startGuest(host.getMinionId(), "sles12sp2");
        }});
        Optional<EngineEvent> event = EngineEvent.parse(
                getEngineEvent("virtevents.guest.shutdown.json", Collections.emptyMap()));
        AbstractLibvirtEngineMessage message = AbstractLibvirtEngineMessage.create(event.get());

        new LibvirtEngineDomainLifecycleMessageAction(virtManager).execute(message);

        DataResult<VirtualSystemOverview> guests = SystemManager.virtualGuestsForHostList(user, host.getId(), null);
        List<VirtualSystemOverview> matchingGuests = guests.stream().filter(vso -> vso.getUuid().equals(guid))
                .collect(Collectors.toList());
        assertEquals(VirtualInstanceFactory.getInstance().getStoppedState().getLabel(),
                     matchingGuests.get(0).getStateLabel());
    }

    public void testShutdownTransient() throws Exception {
        expectGuestDefinition(uuid, null, null);
        context().checking(new Expectations() {{
            never(virtManager).startGuest(host.getMinionId(), "sles12sp2");
        }});
        Optional<EngineEvent> event = EngineEvent.parse(
                getEngineEvent("virtevents.guest.shutdown.json", Collections.emptyMap()));
        AbstractLibvirtEngineMessage message = AbstractLibvirtEngineMessage.create(event.get());

        new LibvirtEngineDomainLifecycleMessageAction(virtManager).execute(message);

        DataResult<VirtualSystemOverview> guests = SystemManager.virtualGuestsForHostList(user, host.getId(), null);
        List<VirtualSystemOverview> matchingGuests = guests.stream().filter(vso -> vso.getUuid().equals(guid))
                .collect(Collectors.toList());

        assertEquals(0, matchingGuests.size());
    }

    public void testUpdate() throws Exception {
        expectGuestDefinition(uuid,
                "/com/suse/manager/reactor/messaging/test/virt.guest.definition.xml", null);
        Optional<EngineEvent> event = EngineEvent.parse(
                getEngineEvent("virtevents.guest.updated.json", Collections.emptyMap()));
        AbstractLibvirtEngineMessage message = AbstractLibvirtEngineMessage.create(event.get());

        new LibvirtEngineDomainLifecycleMessageAction(virtManager).execute(message);

        DataResult<VirtualSystemOverview> guests = SystemManager.virtualGuestsForHostList(user, host.getId(), null);
        List<VirtualSystemOverview> matchingGuests = guests.stream().filter(vso -> vso.getUuid().equals(guid))
                .collect(Collectors.toList());


        assertEquals(Long.valueOf(2), matchingGuests.get(0).getVcpus());
        assertEquals(Long.valueOf(1024), matchingGuests.get(0).getMemory());
    }

    protected Optional<String> getSaltResponse(String filename, Map<String, String> placeholders) throws Exception {
        return SaltTestUtils.getSaltResponse("/com/suse/manager/reactor/messaging/test/" + filename,
                placeholders, null);
    }

    private Event getEngineEvent(String filename, Map<String, String> placeholders) throws Exception {
        Path path = new File(TestUtils.findTestData(
                "/com/suse/manager/reactor/messaging/test/" + filename).getPath()).toPath();
        String eventString = Files.lines(path).collect(Collectors.joining("\n"));

        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                eventString = StringUtils.replace(eventString, entry.getKey(), entry.getValue());
            }
        }
        return EVENTS.parse(eventString);
    }

    private void expectGuestDefinition(String uuidIn, String file,
                                       Map<String, String> placeholders) {
        expectGuestDefinition(uuidIn, file, placeholders, null, null);
    }

    private void expectGuestDefinition(String uuidIn, String file,
                                       Map<String, String> placeholders,
                                       States state, String expectedState) {
        context().checking(new Expectations() {{
            allowing(virtManager).getGuestDefinition("testminion.local", uuidIn);
            will(returnValue(
                    file == null ?
                            Optional.empty() :
                            SaltTestUtils
                                .<String>getSaltResponse(file, placeholders, null)
                                .map(xml -> GuestDefinition.parse(xml, Optional.empty()))
            ));
            if (state != null && expectedState != null) {
                when(state.is(expectedState));
            }
        }});
    }
}
