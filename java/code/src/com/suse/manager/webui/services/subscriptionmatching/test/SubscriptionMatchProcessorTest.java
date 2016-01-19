/**
 * Copyright (c) 2016 SUSE LLC
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
package com.suse.manager.webui.services.subscriptionmatching.test;

import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.suse.manager.webui.services.subscriptionmatching.MatcherUiData;
import com.suse.manager.webui.services.subscriptionmatching.Subscription;
import com.suse.manager.webui.services.subscriptionmatching.SubscriptionMatchProcessor;
import com.suse.matcher.json.JsonInput;
import com.suse.matcher.json.JsonMatch;
import com.suse.matcher.json.JsonMessage;
import com.suse.matcher.json.JsonOutput;
import com.suse.matcher.json.JsonSubscription;
import com.suse.matcher.json.JsonSystem;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

public class SubscriptionMatchProcessorTest extends BaseTestCaseWithUser {

    private JsonInput input;
    private JsonOutput output;
    private SubscriptionMatchProcessor processor;

    public void setUp() throws Exception {
        super.setUp();
        processor = new SubscriptionMatchProcessor();
        input = new JsonInput(new LinkedList<>(), new LinkedList<>(), new LinkedList<>(),
                new LinkedList<>());
        output = new JsonOutput(new Date(), new LinkedList<>(), new LinkedList<>());
    }

    public void testArbitraryMessagePassthrough() {
        LinkedList<JsonMessage> messages = new LinkedList<>();
        Map<String, String> messageData = new HashMap<>();
        messages.add(new JsonMessage("some_arbitrary_type", messageData));
        messageData.put("any", "arbitrary_data");
        output.setMessages(messages);

        List<JsonMessage> jsonMessages = ((MatcherUiData)
                processor.getData(of(input), of(output))).getMessages();
        List<JsonMessage> outputList = jsonMessages.stream()
                .filter(m -> m.getType().equals("some_arbitrary_type"))
                .collect(toList());
        assertEquals(1, outputList.size());
        assertEquals(messageData, outputList.get(0).getData());
    }

    public void testSystemIdAdjustment() throws Exception {
        input.getSystems().add(new JsonSystem(1L, "Sys1", null, true, new HashSet<>(),
                new HashSet<>()));

        LinkedList<JsonMessage> messages = new LinkedList<>();
        Map<String, String> messageData = new HashMap<>();
        messages.add(new JsonMessage("unknown_cpu_count", messageData));
        messageData.put("id", "1");
        output.setMessages(messages);

        List<JsonMessage> jsonMessages = ((MatcherUiData)
                processor.getData(of(input), of(output))).getMessages();
        List<JsonMessage> outputList = jsonMessages.stream()
                .filter(m -> m.getType().equals("unknown_cpu_count"))
                .collect(toList());
        assertEquals(1, outputList.size());
        assertEquals("Sys1", outputList.get(0).getData().get("name"));
    }

    public void testSystemIdAdjustmentNameMissing() throws Exception {
        // system with a null name
        input.getSystems().add(new JsonSystem(1L, null, null, true, new HashSet<>(),
                new HashSet<>()));

        LinkedList<JsonMessage> messages = new LinkedList<>();
        Map<String, String> messageData = new HashMap<>();
        messages.add(new JsonMessage("unknown_cpu_count", messageData));
        messageData.put("id", "1");
        output.setMessages(messages);

        List<JsonMessage> jsonMessages = ((MatcherUiData)
                processor.getData(of(input), of(output))).getMessages();
        List<JsonMessage> outputList = jsonMessages.stream()
                .filter(m -> m.getType().equals("unknown_cpu_count"))
                .collect(toList());
        assertEquals(1, outputList.size());
        // we expect a "fallback" name
        assertEquals("System id: 1", outputList.get(0).getData().get("name"));
    }

    public void testUnsatisfiedMatchAdjustment() throws Exception {
        input.getSystems().add(new JsonSystem(1L, "Sys1", null, true, new HashSet<>(),
                new HashSet<>()));
        input.getSubscriptions().add(new JsonSubscription(100L, "123456", "subs name", 1,
                new Date(), new Date(), "user", new HashSet<>()));

        LinkedList<JsonMessage> messages = new LinkedList<>();
        Map<String, String> messageData = new HashMap<>();
        messages.add(new JsonMessage("unsatisfied_pinned_match", messageData));
        messageData.put("system_id", "1");
        messageData.put("subscription_id", "100");
        output.setMessages(messages);

        List<JsonMessage> jsonMessages = ((MatcherUiData)
                processor.getData(of(input), of(output))).getMessages();
        List<JsonMessage> outputList = jsonMessages.stream()
                .filter(m -> m.getType().equals("unsatisfied_pinned_match"))
                .collect(toList());
        assertEquals(1, outputList.size());
        assertEquals("Sys1", outputList.get(0).getData().get("system_name"));
        assertEquals("subs name", outputList.get(0).getData().get("subscription_name"));
    }

    public void testUnsatisfiedMatchAdjustmentNamesMissing() throws Exception {
        // system with a null name
        input.getSystems().add(new JsonSystem(1L, null, null, true, new HashSet<>(),
                new HashSet<>()));
        // subscriptions with a null name
        input.getSubscriptions().add(new JsonSubscription(100L, "123456", null, 1,
                new Date(), new Date(), "user", new HashSet<>()));

        LinkedList<JsonMessage> messages = new LinkedList<>();
        Map<String, String> messageData = new HashMap<>();
        messages.add(new JsonMessage("unsatisfied_pinned_match", messageData));
        messageData.put("system_id", "1");
        messageData.put("subscription_id", "100");
        output.setMessages(messages);

        List<JsonMessage> jsonMessages = ((MatcherUiData)
                processor.getData(of(input), of(output))).getMessages();
        List<JsonMessage> outputList = jsonMessages.stream()
                .filter(m -> m.getType().equals("unsatisfied_pinned_match"))
                .collect(toList());
        assertEquals(1, outputList.size());
        assertEquals("System id: 1", outputList.get(0).getData().get("system_name"));
        assertEquals("Subscription id: 100",
                outputList.get(0).getData().get("subscription_name"));
    }

    public void testSubscriptions() {
        input.getSubscriptions().add(new JsonSubscription(1L, "123456", "subs name", 3,
                new Date(0), new Date(1000), "user", new HashSet<>()));

        JsonMatch match = new JsonMatch(20L, 1L, 100L, 200);
        output.getConfirmedMatches().add(match);

        MatcherUiData data = (MatcherUiData) processor.getData(of(input), of(output));

        assertEquals(1, data.getSubscriptions().size());
        Subscription actual = data.getSubscriptions().get(0);
        assertEquals("123456", actual.getPartNumber());
        assertEquals("subs name", actual.getDescription());
        assertEquals(3, actual.getTotalQuantity());
        assertEquals(2, actual.getMatchedQuantity());
        assertEquals(new Date(0), actual.getStartDate());
        assertEquals(new Date(1000), actual.getEndDate());
    }
}