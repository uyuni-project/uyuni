/*
 * Copyright (c) 2026 SUSE LCC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.reactor.listener;

import com.suse.manager.reactor.PGEventStream;
import com.suse.salt.netapi.exception.SaltException;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A PGEventStream wrapper class to help Spying its behaviour
 */
public class PGEventStreamSpy extends PGEventStream {

    // Track early counters/countdown attempts that happen before counters and latches are set
    private static final AtomicInteger EARLY_NOTIFICATION_HANDLER_INVOCATIONS = new AtomicInteger(0);
    private static final AtomicInteger EARLY_NOTIFICATION_POLLER_INVOCATIONS = new AtomicInteger(0);
    private static final AtomicInteger EARLY_WATCHDOG_INVOCATIONS = new AtomicInteger(0);

    // counters and latch
    private final AtomicInteger notificationHandlerInvocations;
    private final AtomicInteger notificationPollerInvocations;
    private final AtomicInteger watchdogInvocations;

    private final CountDownLatch notificationHandlerLatch;
    private final CountDownLatch notificationPollerLatch;
    private final CountDownLatch watchdogLatch;

    /**
     * Constructor for PGEventStreamSpy
     * @param notificationHandlerLatchCount a latch to count down on each handleNotification call
     * @param notificationPollerLatchCount a latch to count down on each pollForNotifications call
     * @param watchdogLatchCount a latch to count down on each connectionWatchdog call
     * @throws SaltException if the PGEventStream constructor throws it
     */
    PGEventStreamSpy(
            int notificationHandlerLatchCount,
            int notificationPollerLatchCount,
            int watchdogLatchCount
    ) throws SaltException {
        super();

        int earlyNotificationHandlerInvocations = EARLY_NOTIFICATION_HANDLER_INVOCATIONS.getAndSet(0);
        int earlyNotificationPollerInvocations = EARLY_NOTIFICATION_POLLER_INVOCATIONS.getAndSet(0);
        int earlyWatchdogInvocations = EARLY_WATCHDOG_INVOCATIONS.getAndSet(0);


        this.notificationHandlerInvocations = new AtomicInteger(earlyNotificationPollerInvocations);
        this.notificationPollerInvocations = new AtomicInteger(EARLY_NOTIFICATION_POLLER_INVOCATIONS.getAndSet(0));
        this.watchdogInvocations = new AtomicInteger(EARLY_WATCHDOG_INVOCATIONS.getAndSet(0));

        this.notificationHandlerLatch =
                new CountDownLatch(Math.max(0, notificationHandlerLatchCount - earlyNotificationHandlerInvocations));
        this.notificationPollerLatch =
                new CountDownLatch(Math.max(0, notificationPollerLatchCount - earlyNotificationPollerInvocations));
        this.watchdogLatch = new CountDownLatch(Math.max(0, watchdogLatchCount - earlyWatchdogInvocations));

    }

    @Override
    public void handleNotification(List<Long> counts) {
        if (notificationHandlerInvocations == null) {
            EARLY_NOTIFICATION_HANDLER_INVOCATIONS.incrementAndGet();
        }
        else {
            notificationHandlerInvocations.incrementAndGet();
            notificationHandlerLatch.countDown();
        }
        super.handleNotification(counts);
    }

    @Override
    public void pollForNotifications() {
        if (notificationPollerInvocations == null) {
            EARLY_NOTIFICATION_POLLER_INVOCATIONS.incrementAndGet();
        }
        else {
            notificationPollerInvocations.incrementAndGet();
            notificationPollerLatch.countDown();
        }
        super.pollForNotifications();
    }

    @Override
    public void connectionWatchdog() {
        if (watchdogInvocations == null) {
            EARLY_WATCHDOG_INVOCATIONS.incrementAndGet();
        }
        else {
            watchdogInvocations.incrementAndGet();
            watchdogLatch.countDown();
        }
        super.connectionWatchdog();
    }

    public int getNotificationHandlerInvocations() {
        return notificationHandlerInvocations.get();
    }

    public int getNotificationPollerInvocations() {
        return notificationPollerInvocations.get();
    }

    public int getWatchdogInvocations() {
        return watchdogInvocations.get();
    }

    public CountDownLatch getNotificationHandlerLatch() {
        return notificationHandlerLatch;
    }

    public CountDownLatch getNotificationPollerLatch() {
        return notificationPollerLatch;
    }

    public CountDownLatch getWatchdogLatch() {
        return watchdogLatch;
    }

    public static class PGEventStreamSpyBuilder {
        private int notificationHandlerLatchCount;
        private int notificationPollerLatchCount;
        private int watchdogLatchCount;

        public PGEventStreamSpyBuilder notificationHandlerLatchCount(int count) {
            this.notificationHandlerLatchCount = count;
            return this;
        }

        public PGEventStreamSpyBuilder notificationPollerLatchCount(int count) {
            this.notificationPollerLatchCount = count;
            return this;
        }

        public PGEventStreamSpyBuilder watchdogLatchCount(int count) {
            this.watchdogLatchCount = count;
            return this;
        }

        public PGEventStreamSpy build() throws SaltException {
            return new PGEventStreamSpy(
                    notificationHandlerLatchCount,
                    notificationPollerLatchCount,
                    watchdogLatchCount
            );
        }
    }
}


