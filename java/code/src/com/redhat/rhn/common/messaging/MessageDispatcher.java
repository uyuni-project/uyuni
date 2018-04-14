/**
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
package com.redhat.rhn.common.messaging;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.frontend.events.TraceBackAction;
import com.redhat.rhn.frontend.events.TraceBackEvent;
import com.suse.manager.metrics.PrometheusExporter;
import org.apache.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Polls the EventQueue for events and executes them
 *
 * @version $Rev $
 */
public class MessageDispatcher implements Runnable {

    private static Logger log = Logger.getLogger(MessageDispatcher.class);
    private boolean isStopped = false;

    /* Thread pool for concurrent execution of message actions */
    private ExecutorService threadPool = new MessageQueueThreadPool(
            Config.get().getInt(ConfigDefaults.MESSAGE_QUEUE_THREAD_POOL_SIZE));

    /**
     * Signals the dispatcher to stop
     */
    public synchronized void stop() {
        // Gracefully shut down the thread pool
        threadPool.shutdown();
        log.info("Awaiting termination of threads (for 1 minute)");
        try {
            final boolean done = threadPool.awaitTermination(1, TimeUnit.MINUTES);
            log.info("Thread pool shut down: " + done);
        }
        catch (InterruptedException e) {
            log.error("Interrupted while awaiting termination", e);
        }

        isStopped = true;
    }

    /**
     * Returns the current stop state
     * @return true if stopped, else false
     */
    public synchronized boolean isStopped() {
        return isStopped;
    }

    /**
     * Main run loop where events are popped off the queue
     * and executed. Events are wrapped inside of a Runnable instance
     */
    public void run() {

        // register the executor service for exporting metrics
        PrometheusExporter.INSTANCE.registerThreadPool((ThreadPoolExecutor) this.threadPool, "message_queue");


        while (!isStopped) {
            try {
                ActionExecutor actionHandler = MessageQueue.popEventMessage();
                if (actionHandler == null) {
                    continue;
                }
                else if (actionHandler.canRunConcurrently()) {
                    log.info("Executing in thread pool: " + actionHandler);
                    threadPool.execute(actionHandler);
                }
                else {
                    actionHandler.run();
                }
            }
            catch (InterruptedException e) {
                log.error("Error occurred in the MessageQueue", e);
                return;
            }
            catch (Throwable t) {
                // better log this puppy to let folks know we have a problem
                // but keep the queue running.
                log.error("Error occurred with an event in the MessageQueue", t);

                try {
                    // ok let's email the admins of what's going on.
                    // WARNING! DO NOT PUBLISH THE EVENT TO THE QUEUE!
                    TraceBackEvent evt = new TraceBackEvent();
                    evt.setUser(null);
                    evt.setRequest(null);
                    evt.setException(t);

                    TraceBackAction tba = new TraceBackAction();
                    tba.execute(evt);
                }
                catch (Throwable t1) {
                    log.error("Error sending traceback email, logging for posterity.", t1);
                }
            }

        }
    }

}
