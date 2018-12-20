package com.redhat.rhn.domain.server.test;

import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.scc.SCCOrderItem;
import com.redhat.rhn.domain.scc.SCCSubscription;
import com.redhat.rhn.domain.server.PinnedSubscription;
import com.redhat.rhn.domain.server.PinnedSubscriptionFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import com.suse.manager.matcher.MatcherJsonIO;
import com.suse.scc.model.SCCSubscriptionJson;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by franky on 2/9/16.
 */
public class PinnedSubscriptionFactoryTest extends BaseTestCaseWithUser {

    public void testSave() throws Exception {
        PinnedSubscription subscription = new PinnedSubscription();
        subscription.setSubscriptionId(10L);
        subscription.setSystemId(100L);
        PinnedSubscriptionFactory.getInstance().save(subscription);

        List<PinnedSubscription> subs = PinnedSubscriptionFactory.getInstance()
                .listPinnedSubscriptions();
        assertEquals(1, subs.size());
        assertEquals(subscription, subs.get(0));
    }

    public void testRemove() throws Exception {
        PinnedSubscription subscription = new PinnedSubscription();
        subscription.setSubscriptionId(10L);
        subscription.setSystemId(100L);

        PinnedSubscriptionFactory.getInstance().save(subscription);
        PinnedSubscriptionFactory.getInstance().remove(subscription);

        assertTrue(PinnedSubscriptionFactory.getInstance().listPinnedSubscriptions()
                .isEmpty());
    }

    public void testCleanStalePins() throws Exception {
        PinnedSubscription subscription = new PinnedSubscription();
        subscription.setSubscriptionId(10L);
        subscription.setSystemId(100L);
        PinnedSubscriptionFactory.getInstance().save(subscription);

        assertEquals(1, PinnedSubscriptionFactory.getInstance().listPinnedSubscriptions()
                .size());

        PinnedSubscriptionFactory.getInstance().cleanStalePins();

        assertEquals(0, PinnedSubscriptionFactory.getInstance().listPinnedSubscriptions()
                .size());
    }

    public void testDontCleanGoodPins() throws Exception {
        Map<Long, SCCSubscription> subscriptionsBySccId = SCCCachingFactory.lookupSubscriptions()
                .stream().collect(Collectors.toMap(s -> s.getSccId(), s -> s));
        Map<Long, SUSEProduct> productsBySccId = SUSEProductFactory.productsByProductIds();

        Server server = ServerFactoryTest.createTestServer(user);
        SCCSubscriptionJson subscription = new SCCSubscriptionJson();
        subscription.setId(123L);
        subscription.setSystemLimit(0);
        subscription.setRegcode("good good good");
        subscription.setType("good vibrations");
        subscription.setProductIds(Collections.emptyList());
        SCCCachingFactory.saveJsonSubscription(subscription, null, productsBySccId, subscriptionsBySccId);

        SCCOrderItem item = new SCCOrderItem();
        item.setSccId(123L);
        item.setSubscriptionId((long)subscription.getId());
        SCCCachingFactory.saveOrderItem(item);

        PinnedSubscription pin = new PinnedSubscription();
        pin.setSubscriptionId(item.getSccId());
        pin.setSystemId(server.getId());
        PinnedSubscriptionFactory.getInstance().save(pin);

        PinnedSubscription selfPin = new PinnedSubscription();
        selfPin.setSubscriptionId(item.getSccId());
        selfPin.setSystemId(MatcherJsonIO.SELF_SYSTEM_ID);
        PinnedSubscriptionFactory.getInstance().save(selfPin);

        assertEquals(2, PinnedSubscriptionFactory.getInstance().listPinnedSubscriptions()
                .size());

        PinnedSubscriptionFactory.getInstance().cleanStalePins();

        assertEquals(2, PinnedSubscriptionFactory.getInstance().listPinnedSubscriptions()
                .size());
    }
}