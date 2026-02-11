/*
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.reactor.SaltEvent;
import com.redhat.rhn.domain.reactor.SaltEventFactory;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.Test;

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

    @Test
    public void testCountSaltEvents() {
        // verify there are no salt events
        List<Long> saltEventsCount = SaltEventFactory.countSaltEvents(4);
        assertEquals(Arrays.asList(0L, 0L, 0L, 0L), saltEventsCount);

        // create and pop 1 salt event
        SaltEvent saltEvent1 = new SaltEvent(1L, "minion_1", "data_minion_1", 1);
        insertIntoSuseSaltEvent(saltEvent1);

        saltEventsCount = SaltEventFactory.countSaltEvents(4);
        assertEquals(Arrays.asList(0L, 1L, 0L, 0L), saltEventsCount);

        // create and pop another salt event
        SaltEvent saltEvent2 = new SaltEvent(2L, "minion_2", "data_minion_2", 2);
        insertIntoSuseSaltEvent(saltEvent2);

        saltEventsCount = SaltEventFactory.countSaltEvents(4);
        assertEquals(Arrays.asList(0L, 1L, 1L, 0L), saltEventsCount);

        // create and pop another salt event, not associated to a minion
        SaltEvent saltEvent3 = new SaltEvent(3L, null, "data_event_3", 0);
        insertIntoSuseSaltEvent(saltEvent3);
        saltEventsCount = SaltEventFactory.countSaltEvents(4);
        assertEquals(Arrays.asList(1L, 1L, 1L, 0L), saltEventsCount);

        //leave the table empty
        SaltEventFactory.popSaltEvents(2, 1);
        saltEventsCount = SaltEventFactory.countSaltEvents(4);
        assertEquals(Arrays.asList(1L, 0L, 1L, 0L), saltEventsCount);

        SaltEventFactory.popSaltEvents(2, 2);
        saltEventsCount = SaltEventFactory.countSaltEvents(4);
        assertEquals(Arrays.asList(1L, 0L, 0L, 0L), saltEventsCount);

        SaltEventFactory.popSaltEvents(2, 0);
        saltEventsCount = SaltEventFactory.countSaltEvents(4);

        assertEquals(Arrays.asList(0L, 0L, 0L, 0L), saltEventsCount);
    }

    @Test
    public void testPopSaltEvents() {
        // verify there are no salt events
        List<Long> saltEventsCount = SaltEventFactory.countSaltEvents(4);
        assertEquals(Arrays.asList(0L, 0L, 0L, 0L), saltEventsCount);

        // try to pop a salt event when there are none
        List<SaltEvent> popedSaltEvents = SaltEventFactory.popSaltEvents(1, 0).toList();
        assertEquals(0, popedSaltEvents.size());

        // create and pop 1 salt event
        SaltEvent saltEvent1 = new SaltEvent(1L, "minion_1", "data_minion_1", 1);
        insertIntoSuseSaltEvent(saltEvent1);

        saltEventsCount = SaltEventFactory.countSaltEvents(4);
        assertEquals(Arrays.asList(0L, 1L, 0L, 0L), saltEventsCount);

        popedSaltEvents = SaltEventFactory.popSaltEvents(1, 1).toList();
        assertEquals(popedSaltEvents.size(), 1);
        assertTrue(popedSaltEvents.stream().allMatch(se -> se.equals(saltEvent1)));

        saltEventsCount = SaltEventFactory.countSaltEvents(4);
        assertEquals(Arrays.asList(0L, 0L, 0L, 0L), saltEventsCount);

        // create and pop more than 1 salt event
        int count = 4;

        List<SaltEvent> saltEvents = IntStream.range(0, count)
                .mapToObj(i -> new SaltEvent(i, "minion_" + i, "data_minion_" + i, i % 3 + 1))
                .toList();
        saltEvents.forEach(this::insertIntoSuseSaltEvent);


        Map<Integer, List<SaltEvent>> hashedSaltEvents =  saltEvents.stream()
                .collect(Collectors.groupingBy(SaltEvent::getQueue));

        saltEventsCount = SaltEventFactory.countSaltEvents(4);
        assertEquals(Arrays.asList(0L, 2L, 1L, 1L), saltEventsCount);

        IntStream.range(0, 4).forEach(queue -> {
            List<SaltEvent> popedEvents = SaltEventFactory.popSaltEvents(count, queue).toList();
            assertEquals(popedEvents.size(), hashedSaltEvents.getOrDefault(queue, new ArrayList<>()).size());
            assertTrue(popedEvents.stream()
                    .allMatch(pse -> hashedSaltEvents.get(queue).stream().anyMatch(pse::equals)));
        });

        saltEventsCount = SaltEventFactory.countSaltEvents(4);
        assertEquals(Arrays.asList(0L, 0L, 0L, 0L), saltEventsCount);
    }

    @Test
    public void testDeleteSaltEvents() {
        // verify there are no salt events
        List<Long> saltEventsCount = SaltEventFactory.countSaltEvents(4);
        assertEquals(Arrays.asList(0L, 0L, 0L, 0L), saltEventsCount);

        // create and delete 1 salt event
        SaltEvent saltEvent1 = new SaltEvent(1L, "minion_1", "data_minion_1", 1);
        insertIntoSuseSaltEvent(saltEvent1);

        saltEventsCount = SaltEventFactory.countSaltEvents(4);
        assertEquals(Arrays.asList(0L, 1L, 0L, 0L), saltEventsCount);

        List<Long> deletedEventIds = SaltEventFactory.deleteSaltEvents(List.of(saltEvent1.getId()));
        assertEquals(deletedEventIds.size(), 1);
        assertTrue(deletedEventIds.stream().allMatch(did -> did.equals(saltEvent1.getId())));

        saltEventsCount = SaltEventFactory.countSaltEvents(4);
        assertEquals(Arrays.asList(0L, 0L, 0L, 0L), saltEventsCount);

        // create and delete more than 1 salt event
        int count = 2;
        List<SaltEvent> saltEvents = new ArrayList<>();

        IntStream.range(0, count).forEach(
                i -> {
                    SaltEvent saltEvent = new SaltEvent(i, "minion_" + i, "data_minion_" + i, i % 3 + 1);
                    insertIntoSuseSaltEvent(saltEvent);
                    saltEvents.add(saltEvent);
                }
            );

        // create and pop another salt event, not associated to a minion
        SaltEvent saltEvent3 = new SaltEvent(count + 1L, null, "data_event_3", 0);
        insertIntoSuseSaltEvent(saltEvent3);

        saltEventsCount = SaltEventFactory.countSaltEvents(4);
        saltEvents.add(saltEvent3);
        assertEquals(Arrays.asList(1L, 1L, 1L, 0L), saltEventsCount);

        List<Long> saltEventIds = saltEvents.stream().map(SaltEvent::getId).toList();
        deletedEventIds = SaltEventFactory.deleteSaltEvents(saltEventIds);
        assertEquals(saltEvents.size(), deletedEventIds.size());
        assertTrue(deletedEventIds.stream().allMatch(did -> saltEventIds.stream().anyMatch(id -> id.equals(did))));

        saltEventsCount = SaltEventFactory.countSaltEvents(4);
        assertEquals(Arrays.asList(0L, 0L, 0L, 0L), saltEventsCount);
    }

    @Test
    public void testFixQueueNumbers() {
        // verify there are no salt events
        List<Long> saltEventsCount = SaltEventFactory.countSaltEvents(5);
        assertEquals(Arrays.asList(0L, 0L, 0L, 0L, 0L), saltEventsCount);

        // create events in queue 1, 2, 3 and 4
        SaltEvent saltEvent1 = new SaltEvent(1L, "minion_1", "data_minion_1", 1);
        insertIntoSuseSaltEvent(saltEvent1);
        SaltEvent saltEvent2 = new SaltEvent(2L, "minion_2", "data_minion_2", 2);
        insertIntoSuseSaltEvent(saltEvent2);
        SaltEvent saltEvent3 = new SaltEvent(3L, "minion_3", "data_minion_3", 3);
        insertIntoSuseSaltEvent(saltEvent3);
        SaltEvent saltEvent4 = new SaltEvent(4L, "minion_4", "data_minion_4", 4);
        insertIntoSuseSaltEvent(saltEvent4);
        saltEventsCount = SaltEventFactory.countSaltEvents(5);
        assertEquals(Arrays.asList(0L, 1L, 1L, 1L, 1L), saltEventsCount);

        int changed = SaltEventFactory.fixQueueNumbers(4);
        // Nothing should be changed as queue 1-4 are still available
        saltEventsCount = SaltEventFactory.countSaltEvents(5);
        assertEquals(Arrays.asList(0L, 1L, 1L, 1L, 1L), saltEventsCount);
        assertEquals(0, changed);

        changed = SaltEventFactory.fixQueueNumbers(2);
        // events from queue > 2 should be moved to queue 2, queues 3-4 should be emptied
        saltEventsCount = SaltEventFactory.countSaltEvents(5);
        assertEquals(Arrays.asList(0L, 1L, 3L, 0L, 0L), saltEventsCount);
        assertEquals(2, changed);

        // pop the events
        List<SaltEvent> popedSaltEvents = SaltEventFactory.popSaltEvents(3, 2).toList();
        assertEquals(popedSaltEvents.size(), 3);
        popedSaltEvents = SaltEventFactory.popSaltEvents(1, 1).toList();
        assertEquals(popedSaltEvents.size(), 1);

        saltEventsCount = SaltEventFactory.countSaltEvents(5);
        assertEquals(Arrays.asList(0L, 0L, 0L, 0L, 0L), saltEventsCount);
    }


    private void insertIntoSuseSaltEvent(SaltEvent saltEvent) {
        HibernateFactory.getSession().createNativeMutationQuery(
                "INSERT INTO suseSaltEvent (id, minion_id, data, queue) VALUES (:id, :minionId, :data, :queue)")
                .setParameter("id", saltEvent.getId())
                .setParameter("minionId", saltEvent.getMinionId())
                .setParameter("data", saltEvent.getData())
                .setParameter("queue", saltEvent.getQueue())
                .executeUpdate();
    }
}
