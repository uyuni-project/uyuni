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
package com.suse.manager.reactor.messaging.test;

import com.google.gson.JsonElement;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.frontend.dto.VirtualSystemOverview;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.google.gson.reflect.TypeToken;
import com.suse.manager.reactor.messaging.LibvirtEngineDomainLifecycleMessageAction;
import com.suse.manager.reactor.messaging.AbstractLibvirtEngineMessage;
import com.suse.manager.virtualization.GuestDefinition;
import com.suse.manager.virtualization.test.TestVirtManager;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.salt.netapi.datatypes.Event;
import com.suse.salt.netapi.event.EngineEvent;
import com.suse.salt.netapi.parser.JsonParser;

import org.apache.commons.lang3.StringUtils;
import org.jmock.lib.legacy.ClassImposteriser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
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
            new JsonParser<>(new TypeToken<Event>(){});

    @Override
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        super.setUp();
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        setImposteriser(ClassImposteriser.INSTANCE);

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
                new SystemEntitler(new SaltService(), new TestVirtManager())
        );

        host = ServerTestUtils.createVirtHostWithGuests(user, 1, true, systemEntitlementManager);
        host.getGuests().iterator().next().setUuid(guid);
        host.getGuests().iterator().next().setName("sles12sp2");
        host.asMinionServer().get().setMinionId("testminion.local");
    }

    @SuppressWarnings("unchecked")
    public void testNewGuest() throws Exception {
        Optional<EngineEvent> event = EngineEvent.parse(getEngineEvent("virtevents.guest.started.json", null));
        AbstractLibvirtEngineMessage message = AbstractLibvirtEngineMessage.create(event.get());

        new LibvirtEngineDomainLifecycleMessageAction(virtManager).execute(message);

        DataResult<VirtualSystemOverview> guests = SystemManager.virtualGuestsForHostList(user, host.getId(), null);
        List<VirtualSystemOverview> newGuests = guests.stream().filter(vso -> vso.getName().equals("sles12sp2"))
                .collect(Collectors.toList());

        assertEquals(VirtualInstanceFactory.getInstance().getRunningState().getLabel(),
                     newGuests.get(0).getStateLabel());
    }

    @SuppressWarnings("unchecked")
    public void testShutdownPersistent() throws Exception {
        Optional<EngineEvent> event = EngineEvent.parse(getEngineEvent("virtevents.guest.shutdown.json", Collections.emptyMap()));
        AbstractLibvirtEngineMessage message = AbstractLibvirtEngineMessage.create(event.get());

        new LibvirtEngineDomainLifecycleMessageAction(virtManager).execute(message);

        DataResult<VirtualSystemOverview> guests = SystemManager.virtualGuestsForHostList(user, host.getId(), null);
        List<VirtualSystemOverview> matchingGuests = guests.stream().filter(vso -> vso.getUuid().equals(guid))
                .collect(Collectors.toList());
        assertEquals(VirtualInstanceFactory.getInstance().getStoppedState().getLabel(),
                     matchingGuests.get(0).getStateLabel());
    }


    @SuppressWarnings("unchecked")
    public void testShutdownTransient() throws Exception {
        VirtManager virtManager = new TestVirtManager() {
            @Override
            public Optional<GuestDefinition> getGuestDefinition(String minionId, String domainName) {
                return Optional.empty();
            }
        };
        Optional<EngineEvent> event = EngineEvent.parse(getEngineEvent("virtevents.guest.shutdown.json", Collections.emptyMap()));
        AbstractLibvirtEngineMessage message = AbstractLibvirtEngineMessage.create(event.get());

        new LibvirtEngineDomainLifecycleMessageAction(virtManager).execute(message);

        DataResult<VirtualSystemOverview> guests = SystemManager.virtualGuestsForHostList(user, host.getId(), null);
        List<VirtualSystemOverview> matchingGuests = guests.stream().filter(vso -> vso.getUuid().equals(guid))
                .collect(Collectors.toList());

        assertEquals(0, matchingGuests.size());
    }

    @SuppressWarnings("unchecked")
    public void testUpdate() throws Exception {
        Optional<EngineEvent> event = EngineEvent.parse(getEngineEvent("virtevents.guest.updated.json", Collections.emptyMap()));
        AbstractLibvirtEngineMessage message = AbstractLibvirtEngineMessage.create(event.get());

        new LibvirtEngineDomainLifecycleMessageAction(virtManager).execute(message);

        DataResult<VirtualSystemOverview> guests = SystemManager.virtualGuestsForHostList(user, host.getId(), null);
        List<VirtualSystemOverview> matchingGuests = guests.stream().filter(vso -> vso.getUuid().equals(guid))
                .collect(Collectors.toList());


        assertEquals(Long.valueOf(2), matchingGuests.get(0).getVcpus());
        assertEquals(Long.valueOf(1048576), matchingGuests.get(0).getMemory());
    }

    protected Optional<String> getSaltResponse(String filename, Map<String, String> placeholders) throws Exception {
        return SaltTestUtils.getSaltResponse("/com/suse/manager/reactor/messaging/test/" + filename, placeholders, null);
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
}
