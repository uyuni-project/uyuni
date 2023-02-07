/*
 * Copyright (c) 2016--2021 SUSE LLC
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
package com.redhat.rhn.domain.server.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by franky on 2/9/16.
 */
public class PinnedSubscriptionFactoryTest extends BaseTestCaseWithUser {

    @Test
    public void testSave() {
        PinnedSubscription subscription = new PinnedSubscription();
        subscription.setSubscriptionId(10L);
        subscription.setSystemId(100L);
        PinnedSubscriptionFactory.getInstance().save(subscription);

        List<PinnedSubscription> subs = PinnedSubscriptionFactory.getInstance()
                .listPinnedSubscriptions();
        assertEquals(1, subs.size());
        assertEquals(subscription, subs.get(0));
    }

    @Test
    public void testRemove() {
        PinnedSubscription subscription = new PinnedSubscription();
        subscription.setSubscriptionId(10L);
        subscription.setSystemId(100L);

        PinnedSubscriptionFactory.getInstance().save(subscription);
        PinnedSubscriptionFactory.getInstance().remove(subscription);

        assertTrue(PinnedSubscriptionFactory.getInstance().listPinnedSubscriptions()
                .isEmpty());
    }

    @Test
    public void testCleanStalePins() {
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

    @Test
    public void testDontCleanGoodPins() throws Exception {
        Map<Long, SCCSubscription> subscriptionsBySccId = SCCCachingFactory.lookupSubscriptions()
                .stream().collect(Collectors.toMap(SCCSubscription::getSccId, s -> s));
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
