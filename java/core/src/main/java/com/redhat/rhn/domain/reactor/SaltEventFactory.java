/*
 * Copyright (c) 2018 SUSE LLC
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

package com.redhat.rhn.domain.reactor;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.type.StandardBasicTypes;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import jakarta.persistence.Tuple;

/**
 * Creates {@link SaltEvent} objects.
 */
public class SaltEventFactory extends HibernateFactory {

    private static final Logger LOG = LogManager.getLogger(SaltEventFactory.class);

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    private SaltEventFactory() {
    }

    /**
     * Returns the approximate number of Salt events currently queued.
     * @param queuesCount the number of queues handling events
     * @return the list of events count per queue starting with a queue corresponding to events
     *  without any minion ID. This queue is referred to as queue 0.
     */
    public static List<Long> countSaltEvents(int queuesCount) {
        Session session = HibernateFactory.getSession();
        List<Tuple> countObjects = session.createNativeQuery(
                "SELECT queue, COUNT(*) AS count FROM suseSaltEvent GROUP BY queue", Tuple.class)
                .addScalar("queue", StandardBasicTypes.INTEGER)
                .addScalar("count", StandardBasicTypes.LONG)
                .list();

        return IntStream.range(0, queuesCount)
                .mapToLong(i -> countObjects.stream()
                        .filter(c -> c.get(0, Integer.class).equals(i))
                        .map(c -> c.get(1, Long.class))
                        .findFirst()
                        .orElse(0L)
                ).boxed().collect(Collectors.toList());
    }

    /**
     * Returns Salt events, if any, up to limit.
     * @param limit the maximum count of events to return
     * @param queue the thread to pop events for, 0 for those associated with no particular queue.
     * @return events
     */
    public static Stream<SaltEvent> popSaltEvents(int limit, int queue) {
        Session session = HibernateFactory.getSession();
        return session.createNativeQuery("""
                        DELETE FROM suseSaltEvent
                        WHERE id IN (
                                     SELECT id FROM suseSaltEvent
                                     WHERE queue = :queue
                                     ORDER BY id
                                     FOR UPDATE SKIP LOCKED
                                     LIMIT :limit)
                        RETURNING id, minion_id, data, queue
                        """, Tuple.class)
                .setParameter("limit", limit)
                .setParameter("queue", queue)
                .addScalar("id", StandardBasicTypes.LONG)
                .addScalar("minion_id", StandardBasicTypes.STRING)
                .addScalar("data", StandardBasicTypes.STRING)
                .addScalar("queue", StandardBasicTypes.INTEGER)
                .stream()
                .map(o -> new SaltEvent(o.get(0, Long.class), o.get(1, String.class),
                        o.get(2, String.class), o.get(3, Integer.class)));
    }

    /**
     * Deletes SaltEvents
     * @param ids event ids
     * @return event ids actually deleted
     */
    public static List<Long> deleteSaltEvents(Collection<Long> ids) {
        Session session = HibernateFactory.getSession();
        return session.createNativeQuery("DELETE FROM suseSaltEvent WHERE id IN :ids RETURNING id", Long.class)
                .setParameter("ids", ids)
                .addScalar("id", StandardBasicTypes.LONG)
                .list();
    }

    /**
     * Update event queue numbers after config change.
     * Has an effect only when reducing the number of available queues.
     * All events which are in queues higher than max queues will be moved to the last queue
     * @param maxQueueNum maximal available queue number
     * @return the number of updated events
     */
    public static int fixQueueNumbers(int maxQueueNum) {
        return getSession().createNativeMutationQuery("UPDATE suseSaltEvent SET queue = :q WHERE queue > :q")
                .setParameter("q", maxQueueNum)
                .executeUpdate();
    }
}
