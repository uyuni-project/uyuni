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

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Test for {@link SaltEventFactory} class.
 */
public class SaltEventFactoryTest extends RhnBaseTestCase {

    private static String INSERT_INTO_SUSE_SALT_EVENT_QUERY =
            "INSERT INTO suseSaltEvent (id, minion_id, data, queue) VALUES (:id, :minionId, :data, :queue)";

    public void testCountSaltEvents() {
        // skip test on Oracle
        if (ConfigDefaults.get().isOracle()) {
            return;
        }

        // verify there are no salt events
        List<Long> saltEventsCount = SaltEventFactory.countSaltEvents(3);
        assertEquals(Arrays.asList(0L, 0L, 0L), saltEventsCount);

        // create and pop 1 salt event
        SaltEvent saltEvent1 = new SaltEvent(1L, "minion_1", "data_minion_1", 0);
        insertIntoSuseSaltEvent(saltEvent1);

        saltEventsCount = SaltEventFactory.countSaltEvents(3);
        assertEquals(Arrays.asList(1L, 0L, 0L), saltEventsCount);

        // create and pop another salt event
        SaltEvent saltEvent2 = new SaltEvent(2L, "minion_2", "data_minion_2", 1);
        insertIntoSuseSaltEvent(saltEvent2);

        saltEventsCount = SaltEventFactory.countSaltEvents(3);
        assertEquals(Arrays.asList(1L, 1L, 0L), saltEventsCount);


        //leave the table empty
        SaltEventFactory.popSaltEvents(2, 0);
        saltEventsCount = SaltEventFactory.countSaltEvents(3);
        assertEquals(Arrays.asList(0L, 1L, 0L), saltEventsCount);

        SaltEventFactory.popSaltEvents(2, 1);
        saltEventsCount = SaltEventFactory.countSaltEvents(3);
        assertEquals(Arrays.asList(0L, 0L, 0L), saltEventsCount);
    }

    public void testPopSaltEvents() throws NoSuchAlgorithmException {
        // skip test on Oracle
        if (ConfigDefaults.get().isOracle()) {
            return;
        }

        // verify there are no salt events
        List<Long> saltEventsCount = SaltEventFactory.countSaltEvents(3);
        assertEquals(Arrays.asList(0L, 0L, 0L), saltEventsCount);

        // try to pop a salt event when there are none
        List<SaltEvent> popedSaltEvents = SaltEventFactory.popSaltEvents(1, 0).collect(Collectors.toList());
        assertEquals(0, popedSaltEvents.size());

        // create and pop 1 salt event
        SaltEvent saltEvent1 = new SaltEvent(1L, "minion_1", "data_minion_1", 0);
        insertIntoSuseSaltEvent(saltEvent1);

        saltEventsCount = SaltEventFactory.countSaltEvents(3);
        assertEquals(Arrays.asList(1L, 0L, 0L), saltEventsCount);

        popedSaltEvents = SaltEventFactory.popSaltEvents(1, 0).collect(Collectors.toList());
        assertEquals(popedSaltEvents.size(), 1);
        assertTrue(popedSaltEvents.stream().allMatch(se -> se.equals(saltEvent1)));

        saltEventsCount = SaltEventFactory.countSaltEvents(3);
        assertEquals(Arrays.asList(0L, 0L, 0L), saltEventsCount);

        // create and pop more than 1 salt event
        int count = 4;

        List<SaltEvent> saltEvents = IntStream.range(0, count)
                .mapToObj(i -> new SaltEvent(i, "minion_" + i, "data_minion_" + i, i % 3))
                .collect(Collectors.toList());
        saltEvents.forEach(se -> insertIntoSuseSaltEvent(se));


        Map<Integer, List<SaltEvent>> hashedSaltEvents =  saltEvents.stream()
                .collect(Collectors.groupingBy(se -> se.getQueue()));

        saltEventsCount = SaltEventFactory.countSaltEvents(3);
        assertEquals(Arrays.asList(2L, 1L, 1L), saltEventsCount);

        IntStream.range(0, 3).forEach(queue -> {
            List<SaltEvent> popedEvents = SaltEventFactory.popSaltEvents(count, queue).collect(Collectors.toList());
            assertEquals(popedEvents.size(), hashedSaltEvents.get(queue).size());
            assertTrue(popedEvents.stream().allMatch(pse -> hashedSaltEvents.get(queue).stream().anyMatch(se -> pse.equals(se))));
        });

        saltEventsCount = SaltEventFactory.countSaltEvents(3);
        assertEquals(Arrays.asList(0L, 0L, 0L), saltEventsCount);
    }

    public void testDeleteSaltEvents() {
        // skip test on Oracle
        if (ConfigDefaults.get().isOracle()) {
            return;
        }

        // verify there are no salt events
        List<Long> saltEventsCount = SaltEventFactory.countSaltEvents(3);
        assertEquals(Arrays.asList(0L, 0L, 0L), saltEventsCount);

        // create and delete 1 salt event
        SaltEvent saltEvent1 = new SaltEvent(1L, "minion_1", "data_minion_1", 0);
        insertIntoSuseSaltEvent(saltEvent1);

        saltEventsCount = SaltEventFactory.countSaltEvents(3);
        assertEquals(Arrays.asList(1L, 0L, 0L), saltEventsCount);

        List<Long> deletedEventIds = SaltEventFactory.deleteSaltEvents(Arrays.asList(saltEvent1.getId()));
        assertEquals(deletedEventIds.size(), 1);
        assertTrue(deletedEventIds.stream().allMatch(did -> did.equals(saltEvent1.getId())));

        saltEventsCount = SaltEventFactory.countSaltEvents(3);
        assertEquals(Arrays.asList(0L, 0L, 0L), saltEventsCount);

        // create and delete more than 1 salt event
        int count = 2;
        List<SaltEvent> saltEvents = new ArrayList<SaltEvent>();

        IntStream.range(0, count).forEach(
                i -> {
                    SaltEvent saltEvent = new SaltEvent(i, "minion_" + i, "data_minion_" + i, i % 3);
                    insertIntoSuseSaltEvent(saltEvent);
                    saltEvents.add(saltEvent);
                }
            );
        saltEventsCount = SaltEventFactory.countSaltEvents(3);
        assertEquals(Arrays.asList(1L, 1L, 0L), saltEventsCount);

        List<Long> saltEventIds = saltEvents.stream().map(SaltEvent::getId).collect(Collectors.toList());
        deletedEventIds = SaltEventFactory.deleteSaltEvents(saltEventIds);
        assertEquals(count, deletedEventIds.size());
        assertTrue(deletedEventIds.stream().allMatch(did -> saltEventIds.stream().anyMatch(id -> id.equals(did))));

        saltEventsCount = SaltEventFactory.countSaltEvents(3);
        assertEquals(Arrays.asList(0L, 0L, 0L), saltEventsCount);
    }

    private void insertIntoSuseSaltEvent(SaltEvent saltEvent) {
        Query query = HibernateFactory.getSession().createNativeQuery(INSERT_INTO_SUSE_SALT_EVENT_QUERY);
        query.setParameter("id", saltEvent.getId());
        query.setParameter("minionId", saltEvent.getMinionId());
        query.setParameter("data", saltEvent.getData());
        query.setParameter("queue", saltEvent.getQueue());
        query.executeUpdate();
    }

}
