/**
 * Copyright (c) 2020 SUSE LLC
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
package com.suse.manager.utils;

import com.google.common.util.concurrent.RateLimiter;
import org.apache.log4j.Logger;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.suse.manager.utils.ThreadUtils.threadName;

/**
 * Factory for creating {@link Executor} instances.
 */
public class ExecutorsFactory {

    private static final Logger LOG = Logger.getLogger(ExecutorsFactory.class);

    private ExecutorsFactory() { }

    /**
     * Creates a cached thread pool with a {@link java.util.concurrent.ThreadFactory}
     * that sets the thread names to the given prefix.
     *
     * @param namePrefix thread name prefix
     * @return a cached thread pool
     */
    public static ExecutorService newCachedThreadPool(String namePrefix) {
        return Executors.newCachedThreadPool(runnable -> new Thread(runnable, threadName(namePrefix)));
    }

    /**
     * Creates an {@link Executor} backed by a cached tread pool that limits the rate of execution.
     * @param ratePerSecond rate of execution
     * @param namePrefix thread name prefix
     * @return an rate limiting executor
     */
    public static Executor rateLimitingExecutor(double ratePerSecond, String namePrefix) {
        return new Executor() {

            private Executor exec = Executors.newCachedThreadPool(runnable ->
                    new Thread(runnable, threadName(namePrefix)));
            private RateLimiter rateLimiter = RateLimiter.create(ratePerSecond);

            @Override
            public void execute(Runnable runnable) {
                rateLimiter.acquire();
                LOG.debug("Submitting task to rate limiting SSH executor");
                exec.execute(runnable);
            }
        };
    }

}
