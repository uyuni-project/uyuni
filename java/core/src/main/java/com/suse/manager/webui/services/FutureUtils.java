/*
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
package com.suse.manager.webui.services;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Class providing utility methods for futures.
 */
public class FutureUtils {

    private static final ScheduledExecutorService SCHEDULED_EXECUTOR_SERVICE =
            Executors.newScheduledThreadPool(1);

    private FutureUtils() {
    }

    /**
     * Creates a future that fails with a timeout exception after a given amount of time.
     * @param seconds until the timeout.
     * @param <T> result type of the future which is never used since this future always fails.
     * @return the future.
     */
    public static <T> CompletableFuture<T> failAfter(int seconds) {
        final CompletableFuture<T> promise = new CompletableFuture<>();
        SCHEDULED_EXECUTOR_SERVICE.schedule(() -> {
            final TimeoutException ex = new TimeoutException("Timeout after " + seconds);
            return promise.completeExceptionally(ex);
        }, seconds, TimeUnit.SECONDS);

        return promise;
    }
}
