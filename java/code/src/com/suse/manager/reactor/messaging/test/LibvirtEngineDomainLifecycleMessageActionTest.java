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

import static org.hamcrest.Matchers.containsString;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.frontend.dto.VirtualSystemOverview;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.google.gson.reflect.TypeToken;
import com.suse.manager.reactor.messaging.LibvirtEngineDomainLifecycleMessageAction;
import com.suse.manager.reactor.messaging.AbstractLibvirtEngineMessage;
import com.suse.manager.virtualization.VirtManager;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.datatypes.Event;
import com.suse.salt.netapi.event.EngineEvent;
import com.suse.salt.netapi.parser.JsonParser;

import org.apache.commons.lang3.StringUtils;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Test for {@link LibvirtEngineDomainLifecycleMessageAction}
 */
public class LibvirtEngineDomainLifecycleMessageActionTest extends JMockBaseTestCaseWithUser {

    private SaltService saltServiceMock;
    private Server host;

    // JsonParser for parsing events from files
    public static final JsonParser<Event> EVENTS =
            new JsonParser<>(new TypeToken<Event>(){});

    @Override
    @SuppressWarnings("unchecked")
    public void setUp() throws Exception {
        super.setUp();
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        setImposteriser(ClassImposteriser.INSTANCE);

        saltServiceMock = context().mock(SaltService.class);
        context().checking(new Expectations() {{
            allowing(saltServiceMock).callSync(
                    with(any(LocalCall.class)),
                    with(containsString("serverfactorytest")));
        }});
        SystemManager.mockSaltService(saltServiceMock);
        VirtManager.setSaltService(saltServiceMock);

        host = ServerTestUtils.createVirtHostWithGuests(user, 1, true);
        host.asMinionServer().get().setMinionId("testminion.local");
    }

    @SuppressWarnings("unchecked")
    public void testNewGuest() throws Exception {
        context().checking(new Expectations() {{
            oneOf(saltServiceMock).callSync(
                    with(any(LocalCall.class)),
                    with(host.asMinionServer().get().getMinionId()));
            will(returnValue(getSaltResponse("virt.guest.definition.xml", null)));
        }});

        Optional<EngineEvent> event = EngineEvent.parse(getEngineEvent("virtevents.guest.started.json", null));
        AbstractLibvirtEngineMessage message = AbstractLibvirtEngineMessage.create(event.get());

        new LibvirtEngineDomainLifecycleMessageAction().execute(message);

        DataResult<VirtualSystemOverview> guests = SystemManager.virtualGuestsForHostList(user, host.getId(), null);
        List<VirtualSystemOverview> newGuests = guests.stream().filter(vso -> vso.getName().equals("sles12sp2"))
                .collect(Collectors.toList());

        assertEquals(VirtualInstanceFactory.getInstance().getRunningState().getLabel(),
                     newGuests.get(0).getStateLabel());
    }

    @SuppressWarnings("unchecked")
    public void testShutdownPersistent() throws Exception {
        VirtualInstance guest = host.getGuests().iterator().next();
        String guid = guest.getUuid();
        String uuid = guid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("sles12sp2", guest.getName());
        placeholders.put("b99a8176-4f40-498d-8e61-2f6ade654fe2", uuid);

        context().checking(new Expectations() {{
            oneOf(saltServiceMock).callSync(
                    with(any(LocalCall.class)),
                    with(host.asMinionServer().get().getMinionId()));
            will(returnValue(getSaltResponse("virt.guest.definition.xml", placeholders)));
        }});

        Optional<EngineEvent> event = EngineEvent.parse(getEngineEvent("virtevents.guest.shutdown.json", placeholders));
        AbstractLibvirtEngineMessage message = AbstractLibvirtEngineMessage.create(event.get());

        new LibvirtEngineDomainLifecycleMessageAction().execute(message);

        DataResult<VirtualSystemOverview> guests = SystemManager.virtualGuestsForHostList(user, host.getId(), null);
        List<VirtualSystemOverview> matchingGuests = guests.stream().filter(vso -> vso.getUuid().equals(guid))
                .collect(Collectors.toList());
        assertEquals(VirtualInstanceFactory.getInstance().getStoppedState().getLabel(),
                     matchingGuests.get(0).getStateLabel());
    }


    @SuppressWarnings("unchecked")
    public void testShutdownTransient() throws Exception {
        VirtualInstance guest = host.getGuests().iterator().next();
        String guid = guest.getUuid();
        String uuid = guid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("sles12sp2", guest.getName());
        placeholders.put("b99a8176-4f40-498d-8e61-2f6ade654fe2", uuid);

        context().checking(new Expectations() {{
            oneOf(saltServiceMock).callSync(
                    with(any(LocalCall.class)),
                    with(host.asMinionServer().get().getMinionId()));
            will(returnValue(Optional.of("ERROR: The VM \"" + guest.getName() +"\" is not present")));
        }});

        Optional<EngineEvent> event = EngineEvent.parse(getEngineEvent("virtevents.guest.shutdown.json", placeholders));
        AbstractLibvirtEngineMessage message = AbstractLibvirtEngineMessage.create(event.get());

        new LibvirtEngineDomainLifecycleMessageAction().execute(message);

        DataResult<VirtualSystemOverview> guests = SystemManager.virtualGuestsForHostList(user, host.getId(), null);
        List<VirtualSystemOverview> matchingGuests = guests.stream().filter(vso -> vso.getUuid().equals(guid))
                .collect(Collectors.toList());

        assertEquals(0, matchingGuests.size());
    }

    @SuppressWarnings("unchecked")
    public void testUpdate() throws Exception {
        VirtualInstance guest = host.getGuests().iterator().next();
        String guid = guest.getUuid();
        String uuid = guid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("sles12sp2", guest.getName());
        placeholders.put("b99a8176-4f40-498d-8e61-2f6ade654fe2", uuid);

        context().checking(new Expectations() {{
            oneOf(saltServiceMock).callSync(
                    with(any(LocalCall.class)),
                    with(host.asMinionServer().get().getMinionId()));
            will(returnValue(getSaltResponse("virt.guest.definition.xml", placeholders)));
        }});

        Optional<EngineEvent> event = EngineEvent.parse(getEngineEvent("virtevents.guest.updated.json", placeholders));
        AbstractLibvirtEngineMessage message = AbstractLibvirtEngineMessage.create(event.get());

        new LibvirtEngineDomainLifecycleMessageAction().execute(message);

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
