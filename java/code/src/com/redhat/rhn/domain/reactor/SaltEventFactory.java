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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Creates {@link SaltEvent} objects.
 */
public class SaltEventFactory extends HibernateFactory {

    private static Logger log = LogManager.getLogger(SaltEventFactory.class);
    private static SaltEventFactory singleton = new SaltEventFactory();

    @Override
    protected Logger getLogger() {
        return log;
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
        List<Object[]> countObjects = singleton.listObjectsByNamedQuery("SaltEvent.countSaltEvents", Map.of());

        return IntStream.range(0, queuesCount).mapToLong(i -> countObjects.stream()
                .filter(c -> c[0].equals(i))
                .map(c -> (Long) c[1])
                .findFirst()
                .orElse(0L)).boxed().collect(Collectors.toList());
    }

    /**
     * Returns Salt events, if any, up to limit.
     * @param limit the maximum count of events to return
     * @param queue the thread to pop events for, 0 for those associated with no particular queue.
     * @return events
     */
    public static Stream<SaltEvent> popSaltEvents(int limit, int queue) {
        List<Object[]> eventObjects = singleton.listObjectsByNamedQuery("SaltEvent.popSaltEvents",
                Map.of("limit", limit, "queue", queue));

        return eventObjects.stream()
                .map(o -> new SaltEvent((long)o[0], (String)o[1], (String)o[2], (int)o[3]));
    }

    /**
     * Deletes SaltEvents
     * @param ids event ids
     * @return event ids actually deleted
     */
    public static List<Long> deleteSaltEvents(Collection<Long> ids) {
        return singleton.listObjectsByNamedQuery("SaltEvent.deleteSaltEvents", Map.of("ids", ids));
    }

    /**
     * Update event queue numbers after config change.
     * @param queues number of queues
     * @return the number of updated events
     */
    public static int fixQueueNumbers(int queues) {
        return getSession()
                .getNamedQuery("SaltEvent.fixQueueNumbers")
                .setParameter("queues", queues)
                .executeUpdate();
    }

}
