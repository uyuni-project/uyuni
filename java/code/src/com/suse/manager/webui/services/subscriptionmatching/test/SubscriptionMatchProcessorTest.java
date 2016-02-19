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

import com.redhat.rhn.domain.server.PinnedSubscription;
import com.redhat.rhn.domain.server.PinnedSubscriptionFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.suse.manager.webui.services.subscriptionmatching.MatcherUiData;
import com.suse.manager.webui.services.subscriptionmatching.PinnedMatch;
import com.suse.manager.webui.services.subscriptionmatching.Subscription;
import com.suse.manager.webui.services.subscriptionmatching.SubscriptionMatchProcessor;
import com.suse.manager.webui.services.subscriptionmatching.System;
import com.suse.matcher.json.JsonInput;
import com.suse.matcher.json.JsonMatch;
import com.suse.matcher.json.JsonMessage;
import com.suse.matcher.json.JsonOutput;
import com.suse.matcher.json.JsonProduct;
import com.suse.matcher.json.JsonSubscription;
import com.suse.matcher.json.JsonSystem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

/**
 * Test for SubscriptionMatchProcessor.
 */
public class SubscriptionMatchProcessorTest extends BaseTestCaseWithUser {

    private JsonInput input;
    private JsonOutput output;
    private SubscriptionMatchProcessor processor;

    /**
     * {@inheritDoc}
     */
    public void setUp() throws Exception {
        super.setUp();
        processor = new SubscriptionMatchProcessor();
        input = new JsonInput(new LinkedList<>(), new LinkedList<>(), new LinkedList<>(),
                new LinkedList<>());
        output = new JsonOutput(new Date(), new LinkedList<>(), new LinkedList<>(),
                new HashMap<>());
    }

    public void testMatcherDataNotAvailable() {
        MatcherUiData data =
                (MatcherUiData) processor.getData(empty(), empty());
        assertFalse(data.isMatcherDataAvailable());
    }

    public void testMatcherDataAvailable() {
        MatcherUiData data = (MatcherUiData) processor.getData(of(input), of(output));
        assertTrue(data.isMatcherDataAvailable());
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
        input.getSystems().add(new JsonSystem(1L, "Sys1", null, true,
                false, new HashSet<>(), new HashSet<>()));

        LinkedList<JsonMessage> messages = new LinkedList<>();
        Map<String, String> messageData = new HashMap<>();
        messages.add(new JsonMessage("unknown_cpu_count", messageData));
        messageData.put("id", "1");
        output.setMessages(messages);

        List<JsonMessage> jsonMessages = ((MatcherUiData)
                processor.getData(of(input), of(output))).getMessages();
        List<JsonMessage> outputList = jsonMessages.stream()
                .filter(m -> m.getType().equals("unknownCpuCount"))
                .collect(toList());
        assertEquals(1, outputList.size());
        assertEquals("1", outputList.get(0).getData().get("id"));
    }

    public void testUnsatisfiedMatchAdjustment() throws Exception {
        input.getSystems().add(new JsonSystem(1L, "Sys1", null, true,
                false, new HashSet<>(), new HashSet<>()));
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
                .collect(toList());
        assertEquals(0, outputList.size());
    }

    public void testSubscriptions() {
        input.getSubscriptions().add(new JsonSubscription(1L, "123456", "subs name", 3,
                new Date(0), new Date(1000), "user", new HashSet<>()));

        JsonMatch match = new JsonMatch(20L, 1L, 100L, 200, true);
        output.getMatches().add(match);
        // subscriptions with null policy will be filtered out
        setSubscriptionPolicy(1L, "my policy");

        MatcherUiData data = (MatcherUiData) processor.getData(of(input), of(output));

        assertEquals(1, data.getSubscriptions().size());
        Subscription actual = data.getSubscriptions().values().iterator().next();
        assertEquals("123456", actual.getPartNumber());
        assertEquals("subs name", actual.getDescription());
        assertEquals(Integer.valueOf(3), actual.getTotalQuantity());
        assertEquals(2, actual.getMatchedQuantity());
        assertEquals(new Date(0), actual.getStartDate());
        assertEquals(new Date(1000), actual.getEndDate());
    }

    public void testSubscriptionPolicy() {
        input.getSubscriptions().add(new JsonSubscription(1L, "123456", "subs name", 3,
                new Date(0), new Date(1000), "user", new HashSet<>()));
        setSubscriptionPolicy(1L, "my policy");

        Subscription subscription = ((MatcherUiData) processor
                .getData(of(input), of(output))).getSubscriptions()
                .values().iterator().next();

        assertEquals("my policy", subscription.getPolicy());
    }

    public void testSubscriptionNullPolicy() {
        input.getSubscriptions().add(new JsonSubscription(1L, "123456", "subs name", 3,
                new Date(0), new Date(1000), "user", new HashSet<>()));
        setSubscriptionPolicy(1L, null);

        Map<String, Subscription> subscriptions = ((MatcherUiData) processor
                .getData(of(input), of(output))).getSubscriptions();

        assertTrue(subscriptions.isEmpty());
    }

    private void setSubscriptionPolicy(Long subId, String policy) {
        Map<Long,String> mapping = new HashMap<>();
        mapping.put(subId, policy);
        output.setSubscriptionPolicies(mapping);
    }

    public void testUnmatchedSystems() {
        input.setSystems(Collections.singletonList(
                new JsonSystem(1L, "system name", 1, true, false, Collections.emptySet(),
                        Collections.singleton(100L))));

        List<System> unmatchedSystems = ((MatcherUiData) processor
                .getData(of(input), of(output))).getUnmatchedSystems();

        assertEquals(1, unmatchedSystems.get(0).getProducts().size());
    }

    public void testPartiallyMatchedSystems() {
        Set<Long> products = new HashSet<>();
        products.add(100L);
        products.add(101L);
        input.setSystems(Collections.singletonList(
                new JsonSystem(1L, "system name", 1, true, false, Collections.emptySet(),
                        products)));
        output.getMatches().add(new JsonMatch(1L, 10L, 100L, 100, true));

        List<System> unmatchedSystems = ((MatcherUiData) processor
                .getData(of(input), of(output))).getUnmatchedSystems();

        assertEquals(1, unmatchedSystems.size());
        assertEquals(1, unmatchedSystems.get(0).getProducts().size());
        assertEquals("Unknown product (101)", unmatchedSystems.get(0).getProducts().get(0));
    }

    public void testNewPin() throws Exception {
        input.setSystems(Arrays.asList(new JsonSystem(100L, "my system", 1, true, false,
                new HashSet<>(), new HashSet<>())));

        PinnedSubscription newPinDb = new PinnedSubscription();
        newPinDb.setSubscriptionId(10L);
        newPinDb.setSystemId(100L);
        PinnedSubscriptionFactory.getInstance().save(newPinDb);

        List<PinnedMatch> pinnedMatches = ((MatcherUiData) processor
                .getData(of(input), of(output))).getPinnedMatches();

        assertEquals(1, pinnedMatches.size());
        PinnedMatch pinnedMatch = pinnedMatches.get(0);
        assertEquals(100L, pinnedMatch.getSystemId().longValue());
        assertEquals("pending", pinnedMatch.getStatus());
    }

    public void testConfirmedPin() throws Exception {
        // setup a confirmed match of one system and one subscription
        input.setSystems(Arrays.asList(new JsonSystem(100L, "my system", 1, true, false,
                new HashSet<>(), new HashSet<>())));
        JsonSubscription subscription = new JsonSubscription(10L, "10",
                "subscritption id 10, pn 10",
                1, date("2009-03-01T00:00:00.000Z"), date("2018-02-28T00:00:00.000Z"), "",
                Collections.singleton(1004L));
        input.setSubscriptions(Arrays.asList(subscription));
        List<JsonMatch> matches = Arrays.asList(new JsonMatch(100L, 10L, 1000L, 100, true));
        input.setPinnedMatches(matches);
        output.setMatches(matches);

        // create a corresponding pin
        PinnedSubscription newPinDb = new PinnedSubscription();
        newPinDb.setSubscriptionId(10L);
        newPinDb.setSystemId(100L);
        PinnedSubscriptionFactory.getInstance().save(newPinDb);

        List<PinnedMatch> pinnedMatches = ((MatcherUiData) processor
                .getData(of(input), of(output))).getPinnedMatches();

        assertEquals(1, pinnedMatches.size());
        PinnedMatch pinnedMatch = pinnedMatches.get(0);
        assertEquals(100L, pinnedMatch.getSystemId().longValue());
        assertEquals("satisfied", pinnedMatch.getStatus());
    }


    public void testUnsatisfiedPin() throws Exception {
        // setup a  of one system and one subscription
        input.setSystems(Arrays.asList(new JsonSystem(100L, "my system", 1, true, false,
                new HashSet<>(), new HashSet<>())));
        JsonSubscription subscription = new JsonSubscription(10L, "10",
                "subscritption id 10, pn 10",
                1, date("2009-03-01T00:00:00.000Z"), date("2018-02-28T00:00:00.000Z"), "",
                Collections.singleton(1004L));
        input.setSubscriptions(Arrays.asList(subscription));
        input.setPinnedMatches(Arrays.asList(new JsonMatch(100L, 10L, 1L, 100, null)));

        // create a corresponding pin
        PinnedSubscription newPinDb = new PinnedSubscription();
        newPinDb.setSubscriptionId(10L);
        newPinDb.setSystemId(100L);
        PinnedSubscriptionFactory.getInstance().save(newPinDb);

        List<PinnedMatch> pinnedMatches = ((MatcherUiData) processor
                .getData(of(input), of(output))).getPinnedMatches();

        assertEquals(1, pinnedMatches.size());
        PinnedMatch pinnedMatch = pinnedMatches.get(0);
        assertEquals(100L, pinnedMatch.getSystemId().longValue());
        assertEquals("unsatisfied", pinnedMatch.getStatus());
    }

    /**
     * Smoke test.
     * @throws ParseException
     */
    public void testComplete() throws ParseException {
        List<JsonProduct> products = new LinkedList<>();
        products.add(new JsonProduct(1000L, "product id 1000", false));
        products.add(new JsonProduct(1001L, "product id 1001", false));
        products.add(new JsonProduct(1003L, "product id 1003", false));
        products.add(new JsonProduct(1004L, "product id 1004, with expired subsription", false));
        input.setProducts(products);

        List<JsonSubscription> subscriptions = new LinkedList<>();

        // subscription for product 1000
        subscriptions.add(new JsonSubscription(100L, "100", "subscritption id 100, pn 100",
                6, date("2014-03-01T00:00:00.000Z"), date("2018-02-28T00:00:00.000Z"), "",
                Collections.singleton(1000L)));

        // not used subscription
        subscriptions.add(new JsonSubscription(101L, "101", "subscritption id 101, pn 101",
                6, date("2014-03-01T00:00:00.000Z"), date("2018-02-28T00:00:00.000Z"), "",
                Collections.singleton(999L)));

        // subscription with zero quantity
        subscriptions.add(new JsonSubscription(102L, "102",
                "subscritption id 102, pn 102, quantity=0",
                0, date("2014-03-01T00:00:00.000Z"), date("2018-02-28T00:00:00.000Z"), "",
                Collections.singleton(1001L)));

        // subscription that will have virtualization policy
        subscriptions.add(new JsonSubscription(103L, "103",
                "subscritption id 103, pn 103, unlimited virt. policy",
                1, date("2014-03-01T00:00:00.000Z"), date("2018-02-28T00:00:00.000Z"), "",
                Collections.singleton(1003L)));

        // "expired subscription"
        subscriptions.add(new JsonSubscription(104L, "104",
                "subscritption id 104, pn 104, 'expired' in our tests",
                0, date("2009-03-01T00:00:00.000Z"), date("2010-02-28T00:00:00.000Z"), "",
                Collections.singleton(1004L)));
        input.setSubscriptions(subscriptions);


        // SYSTEMS
        List<JsonSystem> systems = new LinkedList<>();
        systems.add(new JsonSystem(10L, "system 10", 1, true, false, new HashSet<>(),
                Collections.singleton(1000L)));

        Set prods = new HashSet<>();
        prods.add(1000L);
        prods.add(1004L);
        systems.add(new JsonSystem(20L,
                "partially compliant system 20, has product with expired subs",
                1, true, false, new HashSet<>(), prods));
        systems.add(new JsonSystem(21L,
                "partially compliant system 21, has product with 0-quantity subs",
                1, true, false, new HashSet<>(), Collections.singleton(1001L)));

        Set<Long> virtGuests = new HashSet<>();
        virtGuests.add(31L);
        virtGuests.add(32L);
        virtGuests.add(33L);
        virtGuests.add(33L);
        systems.add(new JsonSystem(30L, "virtual host 30", 1, true, true, virtGuests,
                new HashSet<>()));
        systems.add(new JsonSystem(31L, "virtual guest 31", 1, false, false,
                new HashSet<>(), Collections.singleton(1003L)));
        systems.add(new JsonSystem(32L, "virtual guest 32", 1, false, false,
                new HashSet<>(), Collections.singleton(1003L)));
        prods = new HashSet<>();
        prods.add(1000L);
        prods.add(1003L);
        systems.add(new JsonSystem(33L, "virtual guest 33, has also prod 1000 installed", 1,
                false, false, new HashSet<>(), prods));
        systems.add(new JsonSystem(34L, "virtual guest 34, has also prod 1000 installed, " +
                "reported falsely as physical", 1, false, false, new HashSet<>(), prods));
        input.setSystems(systems);

        // OUTPUT
        Map<Long, String> policies = output.getSubscriptionPolicies();
        policies.put(100L, "one_two");
        policies.put(101L, "one_two");
        policies.put(102L, "one_two");
        policies.put(103L, "unlimited_virtualization");
        policies.put(104L, "one_two");

        List<JsonMatch> confirmedMatches = output.getMatches();
        confirmedMatches.add(new JsonMatch(10L, 100L, 1000L, 100, true));
        confirmedMatches.add(new JsonMatch(20L, 100L, 1000L, 100, true));
        confirmedMatches.add(new JsonMatch(30L, 103L, 1003L, 100, true));
        confirmedMatches.add(new JsonMatch(31L, 103L, 1003L, 0, true));
        confirmedMatches.add(new JsonMatch(32L, 103L, 1003L, 0, true));
        confirmedMatches.add(new JsonMatch(33L, 100L, 1000L, 50, true));
        confirmedMatches.add(new JsonMatch(33L, 103L, 1003L, 0, true));
        confirmedMatches.add(new JsonMatch(34L, 100L, 1000L, 100, true));

        MatcherUiData data = (MatcherUiData) processor.getData(of(input), of(output));

        assertEquals(
                Arrays.asList(20L, 21L, 34L),
                data.getUnmatchedSystems().stream()
                        .map(s -> s.getId())
                        .collect(Collectors.toList()));

        // expired or 0 quantity subscription shouldn't be in the output
        assertEquals(
                Arrays.asList(100L, 101L, 103L),
                data.getSubscriptions().values().stream()
                        .map(s -> s.getId())
                        .collect(Collectors.toList()));

        // unmatched products
        List<String> unmatchedProducts = data.getUnmatchedSystems().stream()
                .filter(s -> s.getId().equals(20L))
                .findFirst()
                .get().getProducts();
        assertEquals(1, unmatchedProducts.size());
        assertEquals(
                Arrays.asList("product id 1004, with expired subsription"),
                unmatchedProducts);

        unmatchedProducts = data.getUnmatchedSystems().stream()
                .filter(s -> s.getId().equals(21L))
                .findFirst()
                .get().getProducts();
        assertEquals(1, unmatchedProducts.size());
        assertEquals(
                Arrays.asList("product id 1001"),
                unmatchedProducts);

        unmatchedProducts = data.getUnmatchedSystems().stream()
                .filter(s -> s.getId().equals(34L))
                .findFirst()
                .get().getProducts();
        assertEquals(1, unmatchedProducts.size());
        assertEquals(
                Arrays.asList("product id 1003"),
                unmatchedProducts);

        assertTrue(data.getMessages().isEmpty());
    }

    private Date date(String source) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(source);
    }
}