/**
 * Copyright (c) 2016 SUSE LLC
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
package com.redhat.rhn.common.messaging;

import com.redhat.rhn.frontend.events.TraceBackAction;
import com.redhat.rhn.frontend.events.TraceBackEvent;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.log4j.Logger;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Message queue thread pool for concurrent dispatching of messages.
 */
public class MessageQueueThreadPool extends ThreadPoolExecutor {

    /* Logger for this class */
    private final Logger log = Logger.getLogger(MessageQueueThreadPool.class);

    /* A warning is logged if the queue is growing bigger than this */
    private static final int QUEUE_SIZE_WARNING_THRESHOLD = 100;

    /**
     * Constructor for creating a thread pool for being used with the message queue.
     *
     * @param size the number of threads to create, i.e. pool size
     */
    public MessageQueueThreadPool(int size) {
        super(size, size, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        setThreadFactory(new BasicThreadFactory.Builder().namingPattern("message-queue-thread-%d").build());
        log.info("Started message queue thread pool (size: " + size + ")");
    }

    @Override
    public void execute(Runnable command) {
        int queueSize = getQueue().size();
        if (queueSize >= QUEUE_SIZE_WARNING_THRESHOLD) {
            log.warn("Thread pool queue size is: " + queueSize);
        }
        else if (log.isDebugEnabled()) {
            log.debug("Thread pool queue size is: " + queueSize);
        }
        super.execute(command);
    }

    @Override
    protected void afterExecute(Runnable task, Throwable thrown) {
        super.afterExecute(task, thrown);

        if (thrown == null && task instanceof Future<?>) {
            try {
                ((Future<?>) task).get();
            }
            catch (CancellationException ce) {
                thrown = ce;
            }
            catch (ExecutionException ee) {
                thrown = ee.getCause();
            }
            catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
        if (thrown != null) {
            log.error("Error in message queue: " + thrown.getMessage(), thrown);

            try {
                // Email the admins about what is going on
                TraceBackEvent evt = new TraceBackEvent();
                evt.setUser(null);
                evt.setRequest(null);
                evt.setException(thrown);

                TraceBackAction tba = new TraceBackAction();
                tba.execute(evt);
            }
            catch (Throwable t) {
                log.error("Error sending traceback email, logging for posterity.", t);
            }
        }
        else {
            log.info("Finished: " + task);
        }
    }
}
