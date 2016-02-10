package com.redhat.rhn.domain.server.test;

import com.redhat.rhn.domain.server.PinnedSubscription;
import com.redhat.rhn.domain.server.PinnedSubscriptionFactory;
import com.redhat.rhn.testing.RhnBaseTestCase;

import java.util.List;

/**
 * Created by franky on 2/9/16.
 */
public class PinnedSubscriptionFactoryTest extends RhnBaseTestCase {

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
}