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
package com.suse.manager.reactor.messaging;

import org.apache.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Thread pool for parallel
 */
public enum MessageHandlerThreadPool {

    /* Singleton instance of this class */
    INSTANCE;

    /* Logger for this class */
    private final Logger log = Logger.getLogger(MessageHandlerThreadPool.class);

    /* The executor service to be used */
    private final ExecutorService executorService;

    /* The number of threads can be configurable */
    private static final int NO_OF_THREADS = 5;

    MessageHandlerThreadPool() {
        executorService = Executors.newFixedThreadPool(NO_OF_THREADS);
        log.debug("Started thread pool for concurrent mesage handling.");
    }

    /**
     * Submit a {@link Runnable} for execution.
     *
     * @param runnable the runnable to submit
     */
    public Future<?> submit(Runnable runnable) {
        log.debug("Submitting runnable");
        return executorService.submit(runnable);
    }

    /**
     * Call shutdown() on the executor service.
     */
    public void shutdown() {
        log.debug("Shutting down message queue thread pool.");
        executorService.shutdown();
    }
}
