/**
 * Copyright (c) 2019 SUSE LLC
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

package com.redhat.rhn.domain.reactor.test;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.reactor.SaltEvent;
import com.redhat.rhn.domain.reactor.SaltEventFactory;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.hibernate.query.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Test for {@link SaltEventFactory} class.
 */
public class SaltEventFactoryTest extends RhnBaseTestCase {

    private static String INSERT_INTO_SUSE_SALT_EVENT_QUERY =
            "INSERT INTO suseSaltEvent (id, minion_id, data) VALUES (:id, :minionId, :data)";

    public void testCountSaltEvents() {
        // skip test on Oracle
        if (ConfigDefaults.get().isOracle()) {
            return;
        }

            // verify there are no salt events
        long saltEventsCount = SaltEventFactory.countSaltEvents();
        assertEquals(saltEventsCount, 0);

        // create and pop 1 salt event
        SaltEvent saltEvent1 = new SaltEvent(1L, "minion_1", "data_minion_1");
        insertIntoSuseSaltEvent(saltEvent1);

        saltEventsCount = SaltEventFactory.countSaltEvents();
        assertEquals(saltEventsCount, 1);

        // create and pop another salt event
        SaltEvent saltEvent2 = new SaltEvent(2L, "minion_2", "data_minion_2");
        insertIntoSuseSaltEvent(saltEvent2);

        saltEventsCount = SaltEventFactory.countSaltEvents();
        assertEquals(saltEventsCount, 2);

        //leave the table empty
        SaltEventFactory.popSaltEvents(2);
        saltEventsCount = SaltEventFactory.countSaltEvents();
        assertEquals(saltEventsCount, 0);
    }

    public void testPopSaltEvents() {
        // skip test on Oracle
        if (ConfigDefaults.get().isOracle()) {
            return;
        }

        // verify there are no salt events
        long saltEventsCount = SaltEventFactory.countSaltEvents();
        assertEquals(saltEventsCount, 0);

        // try to pop a salt event when there are none
        List<SaltEvent> popedSaltEvents = SaltEventFactory.popSaltEvents(1).collect(Collectors.toList());
        assertEquals(popedSaltEvents.size(), 0);

        // create and pop 1 salt event
        SaltEvent saltEvent1 = new SaltEvent(1L, "minion_1", "data_minion_1");
        insertIntoSuseSaltEvent(saltEvent1);

        saltEventsCount = SaltEventFactory.countSaltEvents();
        assertEquals(saltEventsCount, 1);

        popedSaltEvents = SaltEventFactory.popSaltEvents(1).collect(Collectors.toList());
        assertEquals(popedSaltEvents.size(), 1);
        assertTrue(popedSaltEvents.stream().allMatch(se -> se.equals(saltEvent1)));

        saltEventsCount = SaltEventFactory.countSaltEvents();
        assertEquals(saltEventsCount, 0);

        // create and pop more than 1 salt event
        int count = 2;

        List<SaltEvent> saltEvents = IntStream.range(0, count)
                .mapToObj(i -> new SaltEvent(i, "minion_" + i, "data_minion_" + i))
                .collect(Collectors.toList());
        saltEvents.forEach(se -> insertIntoSuseSaltEvent(se));

        saltEventsCount = SaltEventFactory.countSaltEvents();
        assertEquals(saltEventsCount, count);

        popedSaltEvents = SaltEventFactory.popSaltEvents(count).collect(Collectors.toList());
        assertEquals(popedSaltEvents.size(), count);
        assertTrue(popedSaltEvents.stream().allMatch(pse -> saltEvents.stream().anyMatch(se -> pse.equals(se))));

        saltEventsCount = SaltEventFactory.countSaltEvents();
        assertEquals(saltEventsCount, 0);
    }

    public void testDeleteSaltEvents() {
        // skip test on Oracle
        if (ConfigDefaults.get().isOracle()) {
            return;
        }

        // verify there are no salt events
        long saltEventsCount = SaltEventFactory.countSaltEvents();
        assertEquals(saltEventsCount, 0);

        // create and delete 1 salt event
        SaltEvent saltEvent1 = new SaltEvent(1L, "minion_1", "data_minion_1");
        insertIntoSuseSaltEvent(saltEvent1);

        saltEventsCount = SaltEventFactory.countSaltEvents();
        assertEquals(saltEventsCount, 1);

        List<Long> deletedEventIds = SaltEventFactory.deleteSaltEvents(Arrays.asList(saltEvent1.getId()));
        assertEquals(deletedEventIds.size(), 1);
        assertTrue(deletedEventIds.stream().allMatch(did -> did.equals(saltEvent1.getId())));

        saltEventsCount = SaltEventFactory.countSaltEvents();
        assertEquals(saltEventsCount, 0);

        // create and delete more than 1 salt event
        int count = 2;
        List<SaltEvent> saltEvents = new ArrayList<SaltEvent>();

        IntStream.range(0, count).forEach(
                i -> {
                    SaltEvent saltEvent = new SaltEvent(i, "minion_" + i, "data_minion_" + i);
                    insertIntoSuseSaltEvent(saltEvent);
                    saltEvents.add(saltEvent);
                }
            );
        saltEventsCount = SaltEventFactory.countSaltEvents();
        assertEquals(saltEventsCount, count);

        List<Long> saltEventIds = saltEvents.stream().map(SaltEvent::getId).collect(Collectors.toList());
        deletedEventIds = SaltEventFactory.deleteSaltEvents(saltEventIds);
        assertEquals(deletedEventIds.size(), count);
        assertTrue(deletedEventIds.stream().allMatch(did -> saltEventIds.stream().anyMatch(id -> id.equals(did))));

        saltEventsCount = SaltEventFactory.countSaltEvents();
        assertEquals(saltEventsCount, 0);
    }

    private void insertIntoSuseSaltEvent(SaltEvent saltEvent) {
        Query query = HibernateFactory.getSession().createNativeQuery(INSERT_INTO_SUSE_SALT_EVENT_QUERY);
        query.setParameter("id", saltEvent.getId());
        query.setParameter("minionId", saltEvent.getMinionId());
        query.setParameter("data", saltEvent.getData());
        query.executeUpdate();
    }

}
