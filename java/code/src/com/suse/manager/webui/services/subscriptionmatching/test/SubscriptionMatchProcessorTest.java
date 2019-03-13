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
import com.suse.manager.webui.services.subscriptionmatching.Product;
import com.suse.matcher.json.InputJson;
import com.suse.matcher.json.MatchJson;
import com.suse.matcher.json.MessageJson;
import com.suse.matcher.json.OutputJson;
import com.suse.matcher.json.ProductJson;
import com.suse.matcher.json.SubscriptionJson;
import com.suse.matcher.json.SystemJson;

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

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.stream.Collectors.toList;

/**
 * Test for SubscriptionMatchProcessor.
 */
public class SubscriptionMatchProcessorTest extends BaseTestCaseWithUser {

    private InputJson input;
    private OutputJson output;
    private SubscriptionMatchProcessor processor;

    /**
     * {@inheritDoc}
     */
    public void setUp() throws Exception {
        super.setUp();
        processor = new SubscriptionMatchProcessor();
        input = new InputJson(new Date(), new LinkedList<>(), new LinkedList<>(),
                new LinkedList<>(), new LinkedList<>(), new LinkedList<>());
        output = new OutputJson(new Date(), new LinkedList<>(), new LinkedList<>(),
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
        LinkedList<MessageJson> messages = new LinkedList<>();
        Map<String, String> messageData = new HashMap<>();
        messages.add(new MessageJson("some_arbitrary_type", messageData));
        messageData.put("any", "arbitrary_data");
        output.setMessages(messages);

        List<MessageJson> jsonMessages = ((MatcherUiData)
                processor.getData(of(input), of(output))).getMessages();
        List<MessageJson> outputList = jsonMessages.stream()
                .filter(m -> m.getType().equals("some_arbitrary_type"))
                .collect(toList());
        assertEquals(1, outputList.size());
        assertEquals(messageData, outputList.get(0).getData());
    }

    public void testSystemIdAdjustment() throws Exception {
        input.getSystems().add(new SystemJson(1L, "Sys1", null, true,
                false, new HashSet<>(), new HashSet<>()));

        LinkedList<MessageJson> messages = new LinkedList<>();
        Map<String, String> messageData = new HashMap<>();
        messages.add(new MessageJson("unknown_cpu_count", messageData));
        messageData.put("id", "1");
        output.setMessages(messages);

        List<MessageJson> jsonMessages = ((MatcherUiData)
                processor.getData(of(input), of(output))).getMessages();
        List<MessageJson> outputList = jsonMessages.stream()
                .filter(m -> m.getType().equals("unknownCpuCount"))
                .collect(toList());
        assertEquals(1, outputList.size());
        assertEquals("1", outputList.get(0).getData().get("id"));
    }

    public void testUnsatisfiedMatchAdjustment() throws Exception {
        input.getSystems().add(new SystemJson(1L, "Sys1", null, true,
                false, new HashSet<>(), new HashSet<>()));
        input.getSubscriptions().add(new SubscriptionJson(100L, "123456", "subs name", 1,
                new Date(), new Date(), "user", new HashSet<>()));

        LinkedList<MessageJson> messages = new LinkedList<>();
        Map<String, String> messageData = new HashMap<>();
        messages.add(new MessageJson("unsatisfied_pinned_match", messageData));
        messageData.put("system_id", "1");
        messageData.put("subscription_id", "100");
        output.setMessages(messages);

        List<MessageJson> jsonMessages = ((MatcherUiData)
                processor.getData(of(input), of(output))).getMessages();
        List<MessageJson> outputList = jsonMessages.stream()
                .collect(toList());
        assertEquals(0, outputList.size());
    }

    public void testSubscriptions() {
        input.getSubscriptions().add(new SubscriptionJson(1L, "123456", "subs name", 3,
                new Date(0), new Date(1000), "user", new HashSet<>()));

        MatchJson match = new MatchJson(20L, 1L, 100L, 200, true);
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
        input.getSubscriptions().add(new SubscriptionJson(1L, "123456", "subs name", 3,
                new Date(0), new Date(1000), "user", new HashSet<>()));
        setSubscriptionPolicy(1L, "my policy");

        Subscription subscription = ((MatcherUiData) processor
                .getData(of(input), of(output))).getSubscriptions()
                .values().iterator().next();

        assertEquals("my policy", subscription.getPolicy());
    }

    public void testSubscriptionNullPolicy() {
        input.getSubscriptions().add(new SubscriptionJson(1L, "123456", "subs name", 3,
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

    public void testUnmatchedProductsSimpleScenario() {
        input.getProducts().add(new ProductJson(100L, "product 100", "", false, false));
        // one system with one product, which is unmatched
        input.setSystems(Collections.singletonList(
                new SystemJson(1L, "system 1", 1, true, false, Collections.emptySet(),
                        Collections.singleton(100L))));

        Map<String, Product> products = ((MatcherUiData) processor
                .getData(of(input), of(output))).getProducts();

        assertEquals(1, products.size());
        assertEquals(1, products.get("100").getUnmatchedSystemCount().intValue());
        assertEquals(
                Long.valueOf(1L),
                products.get("100").getUnmatchedSystemIds().iterator().next());
    }

    public void testUnmatchedProducts() {
        input.getProducts().add(new ProductJson(100L, "product 100", "", false, false));
        input.getProducts().add(new ProductJson(101L, "product 101", "", false, false));
        input.getProducts().add(new ProductJson(102L, "product 102", "", false, false));

        Set<Long> products = new HashSet<>();
        products.add(100L);
        products.add(101L);
        products.add(102L);
        input.getSystems().add(
                new SystemJson(1L, "system 1", 1, true, false, Collections.emptySet(),
                        products));

        Set<Long> products2 = new HashSet<>();
        products2.add(100L);
        products2.add(101L);
        products2.add(102L);
        input.getSystems().add(
                new SystemJson(2L, "system 2", 1, true, false, Collections.emptySet(),
                        products2));

        Set<Long> products3 = new HashSet<>();
        products3.add(100L);
        products3.add(101L);
        input.getSystems().add(
                new SystemJson(3L, "system 3", 1, true, false, Collections.emptySet(),
                        products3));

        output.getMatches().add(new MatchJson(1L, 10L, 100L, 100, true));
        output.getMatches().add(new MatchJson(2L, 20L, 100L, 100, true));
        output.getMatches().add(new MatchJson(3L, 20L, 100L, 100, true));

        MatcherUiData data = (MatcherUiData) processor.getData(of(input), of(output));
        Map<String, Product> unmatchedProducts = data.getProducts();

        // product 100 is matched on every system, that's why it's not in the output at all
        assertTrue(unmatchedProducts.get("100").getUnmatchedSystemIds().isEmpty());

        // product 101 is matched nowhere
        assertEquals(3, unmatchedProducts.get("101").getUnmatchedSystemCount().intValue());

        // product 102 is matched on 2 systems (id 1 and 2)
        Set<Long> systems = new HashSet<>();
        systems.add(1L);
        systems.add(2L);
        assertEquals(systems, unmatchedProducts.get("102").getUnmatchedSystemIds());

        assertEquals(2, data.getUnmatchedProductIds().size());
        assertTrue(data.getUnmatchedProductIds().contains(101L));
        assertTrue(data.getUnmatchedProductIds().contains(102L));
    }

    public void testPartiallyMatchedSystems() {
        input.getProducts().add(new ProductJson(100L, "prod 1", "", false, false));
        input.getProducts().add(new ProductJson(101L, "prod 2", "", false, false));
        Set<Long> productsIn = new HashSet<>();
        productsIn.add(100L);
        productsIn.add(101L);
        input.setSystems(Collections.singletonList(
                new SystemJson(1L, "system name", 1, true, false, Collections.emptySet(),
                        productsIn)));
        output.getMatches().add(new MatchJson(1L, 10L, 100L, 100, true));

        MatcherUiData data = (MatcherUiData) processor
                .getData(of(input), of(output));

        Map<String, Product> products = data.getProducts();
        assertEquals(2, products.size());
        assertEquals(1, products.get("101").getUnmatchedSystemCount().intValue());
        assertEquals(
                Long.valueOf(1L),
                products.get("101").getUnmatchedSystemIds().iterator().next());

        assertEquals(1, data.getUnmatchedProductIds().size());
        assertTrue(data.getUnmatchedProductIds().contains(101L));
    }

    public void testNewPin() throws Exception {
        input.setSystems(Arrays.asList(new SystemJson(100L, "my system", 1, true, false,
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
        input.setSystems(Arrays.asList(new SystemJson(100L, "my system", 1, true, false,
                new HashSet<>(), new HashSet<>())));
        SubscriptionJson subscription = new SubscriptionJson(10L, "10",
                "subscritption id 10, pn 10",
                1, date("2009-03-01T00:00:00.000Z"), date("2018-02-28T00:00:00.000Z"), "",
                Collections.singleton(1004L));
        input.setSubscriptions(Arrays.asList(subscription));
        List<MatchJson> matches = Arrays.asList(new MatchJson(100L, 10L, 1000L, 100, true));
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
        input.setSystems(Arrays.asList(new SystemJson(100L, "my system", 1, true, false,
                new HashSet<>(), new HashSet<>())));
        SubscriptionJson subscription = new SubscriptionJson(10L, "10",
                "subscritption id 10, pn 10",
                1, date("2009-03-01T00:00:00.000Z"), date("2018-02-28T00:00:00.000Z"), "",
                Collections.singleton(1004L));
        input.setSubscriptions(Arrays.asList(subscription));
        input.setPinnedMatches(Arrays.asList(new MatchJson(100L, 10L, 1L, 100, null)));

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
        List<ProductJson> productsIn = new LinkedList<>();
        productsIn.add(new ProductJson(1000L, "product id 1000", "", false, false));
        productsIn.add(new ProductJson(1001L, "product id 1001", "", false, false));
        productsIn.add(new ProductJson(1003L, "product id 1003", "", false, false));
        productsIn.add(new ProductJson(1004L, "product id 1004, with expired subscription",
                "", false, false));
        input.setProducts(productsIn);

        List<SubscriptionJson> subscriptions = new LinkedList<>();

        // subscription for product 1000
        subscriptions.add(new SubscriptionJson(100L, "100", "subscritption id 100, pn 100",
                6, date("2014-03-01T00:00:00.000Z"), date("2018-02-28T00:00:00.000Z"), "",
                Collections.singleton(1000L)));

        // not used subscription
        subscriptions.add(new SubscriptionJson(101L, "101", "subscritption id 101, pn 101",
                6, date("2014-03-01T00:00:00.000Z"), date("2018-02-28T00:00:00.000Z"), "",
                Collections.singleton(999L)));

        // subscription with zero quantity
        subscriptions.add(new SubscriptionJson(102L, "102",
                "subscritption id 102, pn 102, quantity=0",
                0, date("2014-03-01T00:00:00.000Z"), date("2018-02-28T00:00:00.000Z"), "",
                Collections.singleton(1001L)));

        // subscription that will have virtualization policy
        subscriptions.add(new SubscriptionJson(103L, "103",
                "subscritption id 103, pn 103, unlimited virt. policy",
                1, date("2014-03-01T00:00:00.000Z"), date("2018-02-28T00:00:00.000Z"), "",
                Collections.singleton(1003L)));

        // "expired subscription"
        subscriptions.add(new SubscriptionJson(104L, "104",
                "subscritption id 104, pn 104, 'expired' in our tests",
                0, date("2009-03-01T00:00:00.000Z"), date("2010-02-28T00:00:00.000Z"), "",
                Collections.singleton(1004L)));
        input.setSubscriptions(subscriptions);


        // SYSTEMS
        List<SystemJson> systems = new LinkedList<>();
        systems.add(new SystemJson(10L, "system 10", 1, true, false, new HashSet<>(),
                Collections.singleton(1000L)));

        Set prods = new HashSet<>();
        prods.add(1000L);
        prods.add(1004L);
        systems.add(new SystemJson(20L,
                "partially compliant system 20, has product with expired subs",
                1, true, false, new HashSet<>(), prods));
        systems.add(new SystemJson(21L,
                "partially compliant system 21, has product with 0-quantity subs",
                1, true, false, new HashSet<>(), Collections.singleton(1001L)));

        Set<Long> virtGuests = new HashSet<>();
        virtGuests.add(31L);
        virtGuests.add(32L);
        virtGuests.add(33L);
        virtGuests.add(33L);
        systems.add(new SystemJson(30L, "virtual host 30", 1, true, true, virtGuests,
                new HashSet<>()));
        systems.add(new SystemJson(31L, "virtual guest 31", 1, false, false,
                new HashSet<>(), Collections.singleton(1003L)));
        systems.add(new SystemJson(32L, "virtual guest 32", 1, false, false,
                new HashSet<>(), Collections.singleton(1003L)));
        prods = new HashSet<>();
        prods.add(1000L);
        prods.add(1003L);
        systems.add(new SystemJson(33L, "virtual guest 33, has also prod 1000 installed", 1,
                false, false, new HashSet<>(), prods));
        systems.add(new SystemJson(34L, "virtual guest 34, has also prod 1000 installed, " +
                "reported falsely as physical", 1, false, false, new HashSet<>(), prods));
        input.setSystems(systems);

        // OUTPUT
        Map<Long, String> policies = output.getSubscriptionPolicies();
        policies.put(100L, "one_two");
        policies.put(101L, "one_two");
        policies.put(102L, "one_two");
        policies.put(103L, "unlimited_virtualization");
        policies.put(104L, "one_two");

        List<MatchJson> confirmedMatches = output.getMatches();
        confirmedMatches.add(new MatchJson(10L, 100L, 1000L, 100, true));
        confirmedMatches.add(new MatchJson(20L, 100L, 1000L, 100, true));
        confirmedMatches.add(new MatchJson(30L, 103L, 1003L, 100, true));
        confirmedMatches.add(new MatchJson(31L, 103L, 1003L, 0, true));
        confirmedMatches.add(new MatchJson(32L, 103L, 1003L, 0, true));
        confirmedMatches.add(new MatchJson(33L, 100L, 1000L, 50, true));
        confirmedMatches.add(new MatchJson(33L, 103L, 1003L, 0, true));
        confirmedMatches.add(new MatchJson(34L, 100L, 1000L, 100, true));

        MatcherUiData data = (MatcherUiData) processor.getData(of(input), of(output));
        Map<String, Product> products = data.getProducts();

        // product 1000 matched on every system -> mustn't be in the report!
        assertTrue(products.get("1000").getUnmatchedSystemIds().isEmpty());

        // product with 0-quantity subscription -> it must be reported
        assertEquals(
                Collections.singleton(21L),
                products.get("1001").getUnmatchedSystemIds());

        // no confirmed match for system 34 and product 1003 -> this must be reported
        assertEquals(
                Collections.singleton(34L),
                products.get("1003").getUnmatchedSystemIds());

        // product 1004 has expired subscription -> it must be reported
        assertEquals(
                Collections.singleton(20L),
                products.get("1004").getUnmatchedSystemIds());

        // unmatched product ids
        assertEquals(3, data.getUnmatchedProductIds().size());
        assertTrue(data.getUnmatchedProductIds().contains(1001L));
        assertTrue(data.getUnmatchedProductIds().contains(1003L));
        assertTrue(data.getUnmatchedProductIds().contains(1004L));

        assertTrue(data.getMessages().isEmpty());
    }

    private Date date(String source) throws ParseException {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS").parse(source);
    }
}