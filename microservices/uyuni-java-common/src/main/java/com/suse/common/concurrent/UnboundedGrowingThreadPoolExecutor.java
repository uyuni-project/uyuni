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

package com.suse.common.concurrent;

import java.time.Duration;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link ThreadPoolExecutor} that increases the threads in the pool even if the queue is not full.
 */
public class UnboundedGrowingThreadPoolExecutor extends ThreadPoolExecutor {

    private static final AtomicInteger CURRENT_ID = new AtomicInteger(1);

    private final PutBackExecutionHandler handler;

    /**
     * Default constructor
     * @param corePoolSize the number of core threads
     * @param maximumPoolSize the maximum number of threads to allow in the pool
     * @param keepAlive when the number of threads is greater than the core, this is the maximum time that excess
     *     idle
     *     threads will wait for new tasks before terminating.
     * @param threadNamePrefix the prefix to use for the name of the threads part of the pool
     */
    public UnboundedGrowingThreadPoolExecutor(int corePoolSize, int maximumPoolSize, Duration keepAlive,
                                              String threadNamePrefix) {
        super(corePoolSize, maximumPoolSize, keepAlive.toMillis(), TimeUnit.MILLISECONDS,
            new TransferOnOfferBlockingQueue());

        handler = new PutBackExecutionHandler();

        super.setRejectedExecutionHandler(handler);
        super.setThreadFactory(runnable -> new Thread(runnable, threadNamePrefix + "-" + CURRENT_ID.incrementAndGet()));
        super.prestartAllCoreThreads();
    }

    @Override
    public void setRejectedExecutionHandler(RejectedExecutionHandler handlerIn) {
        handler.setExternalHandler(handlerIn);
    }

    /**
     * A blocking queue implementation that always tries to transfer the items offered
     */
    private static class TransferOnOfferBlockingQueue extends LinkedTransferQueue<Runnable> {

        @Override
        public boolean offer(Runnable runnable) {
            // Try to transfer immediately the element to a consumer. If no consumers are available, reject the offer.
            // This will trigger the increase of the consumers pool
            return super.tryTransfer(runnable);
        }
    }

    /**
     * Custom handler of the rejection event to make sure the rejected item are put back to the queue
     */
    private static class PutBackExecutionHandler implements RejectedExecutionHandler {

        private RejectedExecutionHandler externalHandler;

        @Override
        public void rejectedExecution(Runnable runnable, ThreadPoolExecutor executor) {
            try {
                // This does the actual put into the queue.
                // Once the max threads have been reached, the tasks will then queue up.
                executor.getQueue().put(runnable);
                // we do this after the put() to stop race conditions
                if (executor.isShutdown()) {
                    if (externalHandler == null) {
                        throw new RejectedExecutionException("Task " + runnable + " rejected from " + executor);
                    }
                    else {
                        externalHandler.rejectedExecution(runnable, executor);
                    }
                }
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        public void setExternalHandler(RejectedExecutionHandler externalHandlerIn) {
            this.externalHandler = externalHandlerIn;
        }
    }
}
