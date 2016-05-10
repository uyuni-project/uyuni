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

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;

import org.apache.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Message queue thread pool for concurrent dispatching of messages.
 */
public enum MessageQueueThreadPool {

    /* Singleton instance of this class */
    INSTANCE;

    /* Logger for this class */
    private final Logger log = Logger.getLogger(MessageQueueThreadPool.class);

    /* The executor service to be used */
    private final ExecutorService executorService;

    MessageQueueThreadPool() {
        int size = Config.get().getInt(ConfigDefaults.MESSAGE_QUEUE_THREAD_POOL_SIZE);
        executorService = Executors.newFixedThreadPool(size);
        log.debug("Started message queue thread pool (size: " + size + ")");
    }

    /**
     * Submit a {@link Runnable} for execution.
     *
     * @param runnable the runnable to submit
     */
    public void submit(Runnable runnable) {
        log.debug("Submitting new runnable for message queue thread pool");
        executorService.submit(runnable);
    }

    /**
     * Call shutdown() on the executor service.
     */
    public void shutdown() {
        log.debug("Shutting down message queue thread pool");
        executorService.shutdown();
    }
}
