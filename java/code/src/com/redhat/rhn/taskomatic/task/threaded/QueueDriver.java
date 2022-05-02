/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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

import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * "Driver" for a work queue of worker threads
 * @param <T> type of the queue driver candidates
 */
public interface QueueDriver<T> {

    /**
     * Set the logger to use for all logging operations
     * @param loggerIn logger to be set
     */
    void setLogger(Logger loggerIn);

    /**
     * The logger to use for all logging operations
     * @return log4j Logger
     */
    Logger getLogger();

    /**
     * List of work items to "prime" the queue
     * @return list of work items
     */
    List<T> getCandidates();

    /**
     * Maximum number of worker threads to run
     * @return number of worker threads
     */
    int getMaxWorkers();

    /**
     * Create a worker instance to work on a particular work item
     * @param workItem object contained in the list returned from getCandidates()
     * @return worker instance
     */
    QueueWorker makeWorker(T workItem);

    /**
     * Logic to tell the queue when to stop running
     * Queues will always stop when there is no more work to do.
     * This method can return false to cause the queue to stop early.
     * @return true if processing can continue, otherwise false
     */
    boolean canContinue();

    /**
     * Actions that has to be executed, when queue is created
     */
    void initialize();

    /**
     * Specify if this is a blocking worker thread
     * @return true if the task queue should wait end of the workers before continue
     */
    default boolean isBlockingTaskQueue() {
        return false;
    }
}
