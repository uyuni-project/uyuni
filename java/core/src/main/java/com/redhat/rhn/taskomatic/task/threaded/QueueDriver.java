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

/**
 * "Driver" for a work queue of worker threads
 */
public interface QueueDriver {

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
     * Retrieves the work items and "prime" the queue
     * @return the number of work items fetched
     */
    int fetchCandidates();

    /**
     * Check if the queue has additional work item candidates
     * @return true if work items are available in the queue
     */
    boolean hasCandidates();

    /**
     * Maximum number of worker threads to run
     * @return number of worker threads
     */
    int getMaxWorkers();

    /**
     * Create a worker instance to work on the next work item of the queue
     * @return worker instance
     */
    QueueWorker nextWorker();

    /**
     * Logic to tell the queue when to stop running
     * Queues will always stop when there is no more work to do.
     * This method can return false to cause the queue to stop early.
     * @return true if processing can continue, otherwise false
     */
    default boolean canContinue() {
        return true;
    }

    /**
     * Actions that has to be executed, when queue is created
     */
    default void initialize() {
        // Do nothing by default
    }

    /**
     * Specify if this is a blocking worker thread
     * @return true if the task queue should wait end of the workers before continue
     */
    default boolean isBlockingTaskQueue() {
        return false;
    }
}
