/*
 * Copyright (c) 2022 SUSE LLC
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

import org.apache.commons.lang3.tuple.Pair;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * The service to apply rate-limiting to any arbitrary resource
 */
public class ThrottlingService {
    public static final long DEF_THROTTLE_PERIOD_SECS = 60;
    public static final long DEF_MAX_CALLS_PER_PERIOD = 100;

    // Call history map of: Route X User X FirstCallTime & CallCount
    private final Map<String, Map<Long, Pair<LocalDateTime, AtomicLong>>> routeCallMap;

    /**
     * Construct a {@link ThrottlingService} instance
     */
    public ThrottlingService() {
        routeCallMap = new ConcurrentHashMap<>();
    }

    /**
     * Log a single call to the resource
     *
     * Calls are logged per resource, per user. If the rate-limit is exceeded, the call throws a
     * {@link TooManyCallsException}. Otherwise, the caller should continue to execute the call.
     * @param uid the user ID
     * @param path the resource path
     * @throws TooManyCallsException if the rate-limit is exceeded
     */
    public void call(long uid, String path) throws TooManyCallsException {
        call(uid, path, DEF_MAX_CALLS_PER_PERIOD, DEF_THROTTLE_PERIOD_SECS);
    }

    /**
     * Log a single call to the resource
     *
     * Calls are logged per resource, per user. If the rate-limit is exceeded, the call throws a
     * {@link TooManyCallsException}. Otherwise, the caller should continue to execute the call.
     * @param uid the user ID
     * @param path the resource path
     * @param maxCalls maximum number of allowed calls per throttling period
     * @param period the throttling period in seconds
     * @throws TooManyCallsException if the rate-limit is exceeded
     */
    public void call(long uid, String path, long maxCalls, long period) throws TooManyCallsException {
        routeCallMap.putIfAbsent(path, new ConcurrentHashMap<>());
        Map<Long, Pair<LocalDateTime, AtomicLong>> userCallCount = routeCallMap.get(path);

        if (userCallCount.containsKey(uid)) {
            Pair<LocalDateTime, AtomicLong> callInfo = userCallCount.get(uid);
            if (ChronoUnit.SECONDS.between(callInfo.getLeft(), LocalDateTime.now()) < period) {
                long callCount = userCallCount.get(uid).getRight().incrementAndGet();
                if (callCount > maxCalls) {
                    throw new TooManyCallsException();
                }
                return;
            }
        }

        // Reset count
        userCallCount.put(uid, Pair.of(LocalDateTime.now(), new AtomicLong(1)));
    }
}
