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

import static com.suse.manager.reactor.PGEventStream.SUSE_SALT_EVENT_NOTIFICATION_CHANNEL;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.testing.RhnJmockBaseTestCase;

import com.suse.manager.reactor.PGEventStream;
import com.suse.salt.netapi.exception.SaltException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * Unit tests for {@link PGEventStream}.
 */
class PGEventStreamTest extends RhnJmockBaseTestCase {
    private static final int QUEUE_COUNT = ConfigDefaults.get().getSaltEventThreadPoolSize() + 1;
    private static final int DEFAULT_SALT_EVENT_NOTIFICATION_POLL_INTERVAL_MS_VALUE =
            ConfigDefaults.get().getSaltEventNotificationPollIntervalMs();
    private static final int DEFAULT_SALT_EVENT_CONNECTION_WATCHDOG_INTERVAL_SECONDS_VALUE =
            ConfigDefaults.get().getSaltEventConnectionWatchdogIntervalSeconds();
    private PGEventStream stream;


    @BeforeAll
    public static void beforeAll() {
        Config.get().setString(ConfigDefaults.SALT_EVENT_NOTIFICATION_POLL_INTERVAL_MS, "100");
        Config.get().setString(ConfigDefaults.SALT_EVENT_CONNECTION_WATCHDOG_INTERVAL_SECONDS, "1");
    }

    @AfterAll
    public static void afterAll() {
        Config.get().setString(
                ConfigDefaults.SALT_EVENT_NOTIFICATION_POLL_INTERVAL_MS,
                String.valueOf(DEFAULT_SALT_EVENT_NOTIFICATION_POLL_INTERVAL_MS_VALUE)
        );
        Config.get().setString(
                ConfigDefaults.SALT_EVENT_CONNECTION_WATCHDOG_INTERVAL_SECONDS,
                String.valueOf(DEFAULT_SALT_EVENT_CONNECTION_WATCHDOG_INTERVAL_SECONDS_VALUE)
        );
    }

    @AfterEach
    void closeStream() throws IOException {
        if (stream != null && !stream.isEventStreamClosed()) {
            stream.close();
        }
    }

    /**
     * Sends a PostgreSQL NOTIFY command on the specified channel with the given payload.
     *
     * @param channel the channel to send the notification to
     * @param payload the payload of the notification
     */
    private static void notify(String channel, String payload) {
        HibernateFactory.getSession().doWork(connection -> {
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("NOTIFY " + channel + ", '" + payload + "'");
            }
        });
        HibernateFactory.commitTransaction();
    }

    /**
     * Verifies if creating a PGEventStream:
     * - Registers the notify listener;
     * - Sets up watchdog
     */
    @Test
    void testPGEventStreamStartup() throws Exception {
        stream = new PGEventStreamSpy.PGEventStreamSpyBuilder()
                .notificationPollerLatchCount(10)
                .watchdogLatchCount(2)
                .build();
        PGEventStreamSpy pgEventStreamSpy = ((PGEventStreamSpy) stream);

        // assert listener is registered
        // pgsql notification channels are connection-specific, this means so we need reuse the same connection we
        // used for creating the LISTEN subscription
        Connection connection = getStreamField("connection");
        List<String> channels = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM pg_listening_channels()")) {
            while (rs.next()) {
                channels.add(rs.getString(1));
            }
        }
        assertEquals(1, channels.size());
        assertEquals(SUSE_SALT_EVENT_NOTIFICATION_CHANNEL.toLowerCase(), channels.get(0));

        // assert all executorServices are set
        List<ThreadPoolExecutor> executorServices = getStreamField("executorServices");
        assertEquals(ConfigDefaults.get().getSaltEventThreadPoolSize() + 1, executorServices.size());

        assertEquals(1, pgEventStreamSpy.getNotificationHandlerInvocations());
        assertTrue(pgEventStreamSpy.getNotificationPollerLatch().await(3, TimeUnit.SECONDS),
                "Notification poller didn't complete 10 runs in 3s");
        assertTrue(pgEventStreamSpy.getWatchdogLatch().await(3, TimeUnit.SECONDS),
                "Watchdog didn't complete 2 runs in 3s");
    }

    /**
     * Test PGEventStream stream is open after constructor and can be closed properly.
     * Verifies that all resources (main/child executors, connection, etc.) are properly shut down.
     */
    @Test
    void testCloseEventStream() throws Exception {
        stream = new PGEventStream();

        ScheduledExecutorService watchdog = getStreamField("watchdogExecutor");
        ScheduledExecutorService notificationPoller = getStreamField("notificationPollerExecutor");
        List<ThreadPoolExecutor> executors = getStreamField("executorServices");
        Connection conn = getStreamField("connection");

        assertFalse(stream.isEventStreamClosed());

        // action
        stream.close();

        // assert watchdog and notification poller executors are terminated
        assertTrue(watchdog.isTerminated());
        assertTrue(notificationPoller.isTerminated());

        // assert all executor services are also terminated
        for (ThreadPoolExecutor executorIn : executors) {
            assertTrue(executorIn.isTerminated());
        }

        // assert connection is closed
        assertTrue(conn.isClosed());

        assertTrue(stream.isEventStreamClosed());
    }

    /**
     * Tests notification listener is detecting the PG Notify and triggering {@link PGEventStream#handleNotification}
     */
    @Test
    void testHandleNotificationIsCalledOnPgNotify() throws Exception {
        stream = new PGEventStreamSpy.PGEventStreamSpyBuilder().notificationHandlerLatchCount(3).build();
        PGEventStreamSpy pgEventStreamSpy = ((PGEventStreamSpy) stream);

        String payload = IntStream.range(0, QUEUE_COUNT)
                .mapToObj(i -> "0")
                .collect(Collectors.joining(","));

        // Unlike testListenerIsRegistered, we don't need to reuse the same connection for firing the NOTIFY
        // So we can fire a real NOTIFY from an independent connection — this is what mgr_engine.py does
        notify("notSuseSaltEvent", payload);
        notify(SUSE_SALT_EVENT_NOTIFICATION_CHANNEL, payload);
        notify("notSuseSaltEvent", payload);
        notify(SUSE_SALT_EVENT_NOTIFICATION_CHANNEL, payload);
        notify("notSuseSaltEvent", payload);

        // Expect 1 notification handle invoked by the constructor plus 2 from the notification channel
        assertTrue(pgEventStreamSpy.getNotificationHandlerLatch().await(3, TimeUnit.SECONDS),
                "Notification handler did not run in 3s");
        assertEquals(3,  pgEventStreamSpy.getNotificationHandlerInvocations());
    }

    /**
     * Test handleNotification throws no errors when all queues have salt events
     */
    @Test
    void testHandleNotificationThrowsNoErrors() throws SaltException {
        stream = new PGEventStream();
        List<Long> regularCounts = IntStream.range(0, QUEUE_COUNT)
                .mapToLong(i -> i + 1)
                .boxed()
                .toList();
        List<Long> zeroCounts = LongStream.generate(() -> 0L)
                .limit(QUEUE_COUNT)
                .boxed()
                .toList();

        assertDoesNotThrow(() -> stream.handleNotification(regularCounts));
        assertDoesNotThrow(() -> stream.handleNotification(zeroCounts));
    }

    @Test
    void testConnectionWatchdogSQLExceptionTriggersShutdown() throws Exception {
        stream =  new PGEventStream() {
            @Override
            public void connectionWatchdog() {
                closeDatabaseConnection();
                super.connectionWatchdog();
            }
        };

        AtomicBoolean shutdownInProgress = getStreamField("shutdownInProgress");
        awaitTrue(shutdownInProgress,
                ConfigDefaults.get().getSaltEventConnectionWatchdogIntervalSeconds() * 1000L * 3L);
    }

    @Test
    void testNotificationPollerSQLExceptionTriggersShutdown() throws Exception {
        stream =  new PGEventStream() {
            @Override
            public void pollForNotifications() {
                closeDatabaseConnection();
                super.pollForNotifications();
            }
        };

        AtomicBoolean shutdownInProgress = getStreamField("shutdownInProgress");
        awaitTrue(shutdownInProgress, ConfigDefaults.get().getSaltEventNotificationPollIntervalMs() * 3L);
    }

    /**
     * Wait until the given flag becomes true or fail after the timeout expires.
     *
     * @param condition the flag to observe
     * @param timeoutMs the maximum time to wait in milliseconds
     * @throws InterruptedException if the wait is interrupted
     */
    private void awaitTrue(AtomicBoolean condition, long timeoutMs) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMs);
        while (!condition.get() && System.nanoTime() < deadline) {
            Thread.sleep(10L);
        }
        assertTrue(condition.get());
    }

    /**
     * Utility method to access private fields of PGEventStream using reflection.
     *
     * @param fieldName the name of the field to access
     * @return the value of the field
     * @throws NoSuchFieldException if the field does not exist
     * @throws IllegalAccessException if the field cannot be accessed
     */
    @SuppressWarnings("unchecked")
    private <T> T getStreamField(String fieldName) throws NoSuchFieldException, IllegalAccessException {
        Field field = PGEventStream.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(stream);
    }
}
