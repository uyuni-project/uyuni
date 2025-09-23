/*
 * Copyright (c) 2024 SUSE LLC
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

package com.redhat.rhn.taskomatic.task.threaded;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public abstract class AbstractQueueDriver<T> implements QueueDriver {

    private final Queue<T> workItemsQueue = new LinkedList<>();

    @Override
    public int fetchCandidates() {
        workItemsQueue.addAll(getCandidates());
        return workItemsQueue.size();
    }

    @Override
    public boolean hasCandidates() {
        return !workItemsQueue.isEmpty();
    }

    @Override
    public QueueWorker nextWorker() {
        T workItem = workItemsQueue.remove();
        return makeWorker(workItem);
    }

    /**
     * Retrieves the list of work items to "prime" the queue
     * @return list of work items
     */
    protected abstract List<T> getCandidates();

    /**
     * Create a worker instance to work on a particular work item
     * @param workItem object contained in the list returned from getCandidates()
     * @return worker instance
     */
    protected abstract QueueWorker makeWorker(T workItem);
}
