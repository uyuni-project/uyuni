/*
 * Copyright (c) 2019--2021 SUSE LLC
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

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.reactor.messaging.BatchStartedEventMessage;
import com.suse.manager.reactor.messaging.BatchStartedEventMessageAction;
import com.suse.salt.netapi.datatypes.Event;
import com.suse.salt.netapi.event.BatchStartedEvent;
import com.suse.salt.netapi.parser.JsonParser;

import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test for {@link BatchStartedEventMessageAction}
 */
public class BatchStartedEventMessageActionTest extends BaseTestCaseWithUser {

    private JsonParser<Event> eventParser;
    private BatchStartedEventMessageAction messageAction;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        this.messageAction = new BatchStartedEventMessageAction();
        this.eventParser = new JsonParser<>(new TypeToken<>() {
        });
    }

    @Test
    public void testExecute() throws Exception {
        // Create 2 minions
        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        MinionServer minion2 = MinionServerFactoryTest.createTestMinionServer(user);

        // Create an action for the 2 minions. Process a 'batch-start' event where both minions are available.
        // No server action should be marked as 'FAILED'.
        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_ERRATA);
        Action foundAction = ActionFactory.lookupById(action.getId());
        assertEquals(foundAction.getId(), action.getId());
        assertNull(foundAction.getServerActions());

        ServerAction serverAction1 = ActionFactoryTest.createServerAction(minion1, action);
        ServerAction serverAction2 = ActionFactoryTest.createServerAction(minion2, action);

        ActionFactory.save(serverAction1);
        ActionFactory.save(serverAction2);
        TestUtils.flushAndEvict(action);

        foundAction = ActionFactory.lookupById(action.getId());
        assertEquals(foundAction.getId(), action.getId());
        assertEquals(foundAction.getServerActions().size(), 2);
        assertTrue(foundAction.getServerActions().stream().allMatch(sa -> sa.getServerId().equals(minion1.getId()) ||
                        sa.getServerId().equals(minion2.getId())));
        assertTrue(foundAction.getServerActions().stream().noneMatch(ServerAction::isFailed));

        BatchStartedEvent batchStartedEvent = getBatchStartedEvent(action.getId(),
                asList(minion1, minion2), new ArrayList<>());

        BatchStartedEventMessage msg = new BatchStartedEventMessage(batchStartedEvent);
        messageAction.execute(msg);

        foundAction = ActionFactory.lookupById(action.getId());
        assertEquals(foundAction.getId(), action.getId());
        assertEquals(foundAction.getServerActions().size(), 2);
        assertTrue(foundAction.getServerActions().stream().allMatch(sa -> sa.getServerId().equals(minion1.getId()) ||
                        sa.getServerId().equals(minion2.getId())));
        assertTrue(foundAction.getServerActions().stream().noneMatch(ServerAction::isFailed));

        // Create an action for the 2 minions. Process a 'batch-start' event where both minions are down.
        // Both server action should be marked as 'FAILED'.
        action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_ERRATA);
        foundAction = ActionFactory.lookupById(action.getId());
        assertEquals(foundAction.getId(), action.getId());
        assertNull(foundAction.getServerActions());

        serverAction1 = ActionFactoryTest.createServerAction(minion1, action);
        serverAction2 = ActionFactoryTest.createServerAction(minion2, action);

        ActionFactory.save(serverAction1);
        ActionFactory.save(serverAction2);
        TestUtils.flushAndEvict(action);

        foundAction = ActionFactory.lookupById(action.getId());
        assertEquals(foundAction.getId(), action.getId());
        assertEquals(foundAction.getServerActions().size(), 2);
        assertTrue(foundAction.getServerActions().stream().allMatch(sa -> sa.getServerId().equals(minion1.getId()) ||
                        sa.getServerId().equals(minion2.getId())));
        assertTrue(foundAction.getServerActions().stream().noneMatch(ServerAction::isFailed));

        batchStartedEvent = getBatchStartedEvent(action.getId(),
                new ArrayList<>(), asList(minion1, minion2));

        msg = new BatchStartedEventMessage(batchStartedEvent);
        messageAction.execute(msg);

        foundAction = ActionFactory.lookupById(action.getId());
        assertEquals(foundAction.getId(), action.getId());
        assertEquals(foundAction.getServerActions().size(), 2);
        assertTrue(foundAction.getServerActions().stream().allMatch(sa -> sa.getServerId().equals(minion1.getId()) ||
                        sa.getServerId().equals(minion2.getId())));
        assertTrue(foundAction.getServerActions().stream().allMatch(ServerAction::isFailed));

        // Create an action for the 2 minions. Process a 'batch-start' event where 'minion1'
        // is available but 'minion2' is down.
        // Only the server action for 'minion1' should be marked as 'FAILED'.
        action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_ERRATA);
        foundAction = ActionFactory.lookupById(action.getId());
        assertEquals(foundAction.getId(), action.getId());
        assertNull(foundAction.getServerActions());

        serverAction1 = ActionFactoryTest.createServerAction(minion1, action);
        serverAction2 = ActionFactoryTest.createServerAction(minion2, action);

        ActionFactory.save(serverAction1);
        ActionFactory.save(serverAction2);
        TestUtils.flushAndEvict(action);

        foundAction = ActionFactory.lookupById(action.getId());
        assertEquals(foundAction.getId(), action.getId());
        assertEquals(foundAction.getServerActions().size(), 2);
        assertTrue(foundAction.getServerActions().stream().allMatch(sa -> sa.getServerId().equals(minion1.getId()) ||
                        sa.getServerId().equals(minion2.getId())));
        assertTrue(foundAction.getServerActions().stream().noneMatch(ServerAction::isFailed));

        batchStartedEvent = getBatchStartedEvent(action.getId(), asList(minion2), asList(minion1));

        msg = new BatchStartedEventMessage(batchStartedEvent);
        messageAction.execute(msg);

        foundAction = ActionFactory.lookupById(action.getId());
        assertEquals(foundAction.getId(), action.getId());
        assertEquals(foundAction.getServerActions().size(), 2);
        assertTrue(foundAction.getServerActions().stream().allMatch(sa -> sa.getServerId().equals(minion1.getId()) ||
                        sa.getServerId().equals(minion2.getId())));
        assertTrue(foundAction.getServerActions().stream().filter(sa -> sa.getServerId().equals(minion1.getId()))
                .allMatch(ServerAction::isFailed));
        assertTrue(foundAction.getServerActions().stream().filter(sa -> sa.getServerId().equals(minion2.getId()))
                .noneMatch(ServerAction::isFailed));
    }

    private BatchStartedEvent getBatchStartedEvent(Long actionId, List<MinionServer> availableMinions,
            List<MinionServer> downMinions) throws Exception {
        Map<String, String> placeholders = new HashMap<>();

        placeholders.put("$SUMA_ACTION_ID$", actionId.toString());
        placeholders.put("$AVAILABLE_MINIONS$", availableMinions.stream()
                .map(m -> "\"" + m.getMinionId() + "\"").collect(joining(",")));
        placeholders.put("$DOWN_MINIONS$", downMinions.stream()
                .map(m -> "\"" + m.getMinionId() + "\"").collect(joining(",")));

        return BatchStartedEvent.parse(getEvent("batch.start.json", actionId, placeholders)).get();
    }

    private Event getEvent(String filename, long actionId, Map<String, String> placeholders) throws Exception {
        Path path = new File(TestUtils.findTestData(
                "/com/suse/manager/reactor/messaging/test/" + filename).getPath()).toPath();
        String eventString = Files.lines(path)
                .collect(joining("\n"))
                .replaceAll("\"suma-action-id\": \\d+", "\"suma-action-id\": " + actionId);

        if (placeholders != null) {
            for (Map.Entry<String, String> entries : placeholders.entrySet()) {
                String placeholder = entries.getKey();
                String value = entries.getValue();
                eventString = StringUtils.replace(eventString, placeholder, value);
            }
        }
        return eventParser.parse(eventString);
    }

}
