/*
 * Copyright (c) 2018 SUSE LLC
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
package com.suse.manager.reactor;

import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.reactor.SaltEvent;
import com.redhat.rhn.domain.reactor.SaltEventFactory;
import com.redhat.rhn.frontend.events.TransactionHelper;

import com.suse.manager.metrics.PrometheusExporter;
import com.suse.salt.netapi.datatypes.Event;
import com.suse.salt.netapi.event.AbstractEventStream;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.parser.JsonParser;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;
import org.postgresql.ds.PGSimpleDataSource;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

/**
 * Processes Salt events from the PostgreSQL database (suseSaltEvent) using the LISTEN/NOTIFY mechanism.
 *
 * The following sections describe this class’s scope, relevant concepts, and the design for which it is responsible.
 *
 * Salt events flow through the system as follows:
 *   1) Salt Master generates events (minion starts, job returns, etc.)
 *   2) Python engine (mgr_events.py) filters and batches events into the suseSaltEvent table
 *   3) Python engine sends a NOTIFY with the count of events per queue
 *   4) This class then, is responsible for listening for NOTIFYs, reading events from the database,
 *      and dispatching them to listeners.
 *
 * Queue System
 * Events are distributed across {@code THREAD_POOL_SIZE + 1} queues for parallel processing:
 * - Queue 0: Global events without a specific minion (beacons, cluster events)
 * - Queues 1-N: Minion-specific events, with sticky assignment per minion for ordering guarantees
 *
 * Each queue has a dedicated single-threaded executor with an unbounded queue, ensuring:
 * - Sequential processing of events within the same queue (per-minion ordering)
 * - Parallel processing across different queues
 * - Load balancing: new minions assigned to least-loaded queue
 *
 * Job Processing
 * A "job" is a unit of work that processes a batch of events from the database:
 * - Job size: Each job processes up to {@code MAX_EVENTS_PER_COMMIT} events (default: 1)
 * - Job scheduling: Based on NOTIFY payload counts, e.g., 500 events means 500 jobs (if batch size = 1)
 * - Job execution: DELETE events from database, parse JSON, notify listeners, COMMIT transaction
 * - Job lifecycle: Jobs queue up in the executor's {@link LinkedBlockingQueue} until processed
 *
 * Notification Poller
 * A notification poller thread runs every {@code NOTIFICATION_POLL_INTERVAL_MS} milliseconds (default 100ms).
 * Its single purpose is to poll for PostgreSQL LISTEN/NOTIFY notifications and schedule jobs accordingly,
 * by dispatching to {@code handleNotification()}
 *
 * Watchdog Mechanism
 * A background watchdog thread runs every {@code CONNECTION_WATCHDOG_INTERVAL_SECONDS} seconds (default 5s)
 * to ensure reliability:
 * - Health check: Execute dummy query to guarantee db connection is alive
 * - Check for orphaned events (events without active jobs, e.g., due to lost notifications)
 * - Schedule recovery: If idle queues have events, schedule jobs to process them
 *
 * Performance & Tuning considerations
 *
 * Throughput is determined by {@code MAX_EVENTS_PER_COMMIT} and {@code THREAD_POOL_SIZE}:
 * - MAX_EVENTS_PER_COMMIT:
 *   - Low value (default 1) = high reliability, but also low throughput (1 event/transaction)
 *   - Higher values reduce transaction overhead but increase potential event loss on crash
 * - THREAD_POOL_SIZE: Determines parallel processing capacity (default 8 queues)
 *   More queues = better parallelism for multiple minions, but diminishing returns beyond CPU core count
 *   Each queue processes sequentially, so parallelism is limited to number of active queues
 *
 * Latency:
 * Notifications are polled every {@code NOTIFICATION_POLL_INTERVAL_MS} milliseconds (default 100ms).
 * Lower values provide faster event processing but increase CPU usage.
 *
 * Memory:
 * Unbounded queues can grow indefinitely if processing can't keep up with event arrival rate.
 *
 */
public class PGEventStream extends AbstractEventStream {
    public static final String SUSE_SALT_EVENT_NOTIFICATION_CHANNEL = "suseSaltEvent";
    public static final String SALT_EVENT_CONNECTION_WATCHDOG = "salt-event-connection-watchdog";
    public static final String SALT_EVENT_NOTIFICATION_POLLER = "salt-event-notification-poller";

    private static final Logger LOG = LogManager.getLogger(PGEventStream.class);
    private static final int MAX_EVENTS_PER_COMMIT = ConfigDefaults.get().getSaltEventsPerCommit();
    private static final int THREAD_POOL_SIZE = ConfigDefaults.get().getSaltEventThreadPoolSize();
    private static final int NOTIFICATION_POLL_INTERVAL_MS =
            ConfigDefaults.get().getSaltEventNotificationPollIntervalMs();
    private static final int CONNECTION_WATCHDOG_INTERVAL_SECONDS =
            ConfigDefaults.get().getSaltEventConnectionWatchdogIntervalSeconds();

    private final Connection connection;
    private final List<ThreadPoolExecutor> executorServices = IntStream.range(0, THREAD_POOL_SIZE + 1).mapToObj(i ->
        new ThreadPoolExecutor(
                1,
                1,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                BasicThreadFactory.builder()
                    .namingPattern(i == 0 ? "salt-global-event-thread-%d" : String.format("salt-event-thread-%d", i))
                    .build()
        )
    ).toList();

    private final ScheduledExecutorService notificationPollerExecutor = Executors.newSingleThreadScheduledExecutor(
            r -> new Thread(r, SALT_EVENT_NOTIFICATION_POLLER)
    );

    private final ScheduledExecutorService watchdogExecutor = Executors.newSingleThreadScheduledExecutor(
            r -> new Thread(r, SALT_EVENT_CONNECTION_WATCHDOG)
    );

    private final AtomicBoolean shutdownInProgress = new AtomicBoolean(false);


    /**
     * Default constructor, connects to Postgres and waits for events.
     * @throws SaltException if connection fails
     */
    public PGEventStream() throws SaltException {
        PGSimpleDataSource dataSource = new PGSimpleDataSource();
        Config config = Config.get();
        dataSource.setServerNames(new String[]{config.getString(ConfigDefaults.DB_HOST)});
        dataSource.setPortNumbers(new int[]{config.getInt(ConfigDefaults.DB_PORT)});
        dataSource.setDatabaseName(config.getString(ConfigDefaults.DB_NAME));
        dataSource.setUser(config.getString(ConfigDefaults.DB_USER));
        dataSource.setPassword(config.getString(ConfigDefaults.DB_PASSWORD));
        dataSource.setSslMode("allow");

        // register the executor service for exporting metrics
        PrometheusExporter.INSTANCE.registerThreadPoolList(this.executorServices, "salt_queue");

        try {
            int pending = SaltEventFactory.fixQueueNumbers(THREAD_POOL_SIZE);
            if (pending > 0) {
                LOG.info("Found {} queued salt events", pending);
            }

            connection = dataSource.getConnection();

            try (Statement stmt = connection.createStatement()) {
                stmt.execute("LISTEN " + SUSE_SALT_EVENT_NOTIFICATION_CHANNEL);
            }

            notificationPollerExecutor.scheduleWithFixedDelay(
                    this::pollForNotifications,
                    0,
                    NOTIFICATION_POLL_INTERVAL_MS,
                    TimeUnit.MILLISECONDS
            );
            watchdogExecutor.scheduleWithFixedDelay(
                    this::connectionWatchdog,
                    0,
                    CONNECTION_WATCHDOG_INTERVAL_SECONDS,
                    TimeUnit.SECONDS
            );

            LOG.debug("Listening succeeded, making sure there is no event left in queue...");
            handleNotification(SaltEventFactory.countSaltEvents(THREAD_POOL_SIZE + 1));
        }
        catch (SQLException e) {
            throw new SaltException(e);
        }
    }

    /**
     * Polls for PostgreSQL LISTEN/NOTIFY notifications and process them.
     */
    public void pollForNotifications() {
        try {
            PGConnection pgConn = connection.unwrap(PGConnection.class);
            PGNotification[] notifications = pgConn.getNotifications(1);

            if (notifications == null) {
                return;
            }

            for (PGNotification notification : notifications) {
                List<Long> counts = Arrays.stream(notification.getParameter().split(","))
                        .map(Long::valueOf)
                        .toList();
                handleNotification(counts);
            }
        }
        catch (SQLException e) {
            LOG.error("Error polling for notifications", e);
            shutdown("Postgres notification connection was lost", false, true);
        }
        catch (Exception e) {
            LOG.error("Unexpected exception in notification poller:", e);
        }
    }

    /**
     * Connection watchdog task that checks the database connection health and reschedules any orphaned events that
     * lost their notifications.
     */
    public void connectionWatchdog() {
        try {
            try (Statement s = connection.createStatement()) {
                s.execute("SELECT 'salt-event-connection-watchdog';");

                // if we have any rows in suseSaltEvent that do not yet have a process task active
                // then schedule tasks for them
                // this can only happen in case we lost notifications somehow
                List<Long> allJobs = SaltEventFactory.countSaltEvents(THREAD_POOL_SIZE + 1);

                List<Long> missingJobs = IntStream.range(0, allJobs.size())
                        .mapToObj(i -> executorServices.get(i).getActiveCount() > 0 ? 0 : allJobs.get(i))
                        .toList();

                if (missingJobs.stream().mapToLong(l -> l).sum() > 0) {
                    LOG.warn("Found {} events without a job. Scheduling...", missingJobs);
                    handleNotification(missingJobs);
                }
            }
        }
        catch (SQLException e) {
            shutdown("Postgres notification connection was lost", true, false);
        }
        catch (Exception e) {
            LOG.error("Unexpected exception in watchdog:", e);
        }
        finally {
            // create new session for each run
            HibernateFactory.closeSession();
        }
    }

    /**
     * Handles a notification from Postgres by scheduling one job per queue for the number of events to be processed.
     * mgr_engine.py is responsible for raising notifications with the number of events in suseSaltEvent for each
     * queue (associated to minions or not).
     * @param counts the list of the number of INSERTed events to be handled by queue. The first queue
     *      is the one for events that aren't associated to a minion, queue 0.
     */
    public void handleNotification(List<Long> counts) {
        LOG.trace("Got notification: {}", counts);
        // compute the number of jobs we need to do - each job COMMITs individually
        // jobs = events / MAX_EVENTS_PER_COMMIT (rounded up)
        IntStream.range(0, THREAD_POOL_SIZE + 1).forEach(queue -> {
            long jobs = (counts.get(queue) + MAX_EVENTS_PER_COMMIT - 1) / MAX_EVENTS_PER_COMMIT;

            // queue one handlingTransaction(processEvents) call per job
            LongStream.range(0L, jobs).forEach(job -> {
                LOG.trace("Scheduling a job for queue {}", queue);
                ThreadPoolExecutor executor = executorServices.get(queue);
                executor.execute(() -> {
                    List<SaltEvent> uncommittedEvents = new LinkedList<>();
                    TransactionHelper.handlingTransaction(
                            () -> processEvents(uncommittedEvents, queue),
                            e -> handleExceptions(uncommittedEvents, e));
                });
            });
        });
    }

    /**
     * Reads one or more events from suseSaltEvent and notifies listeners
     * (typically, {@link PGEventListener#notify(Event)}).
     *
     * @param uncommittedEvents used to keep track of events being processed
     * @param queue the index of the thread processing the events
     */
    private void processEvents(List<SaltEvent> uncommittedEvents, int queue) {
        Stream<SaltEvent> events = SaltEventFactory.popSaltEvents(MAX_EVENTS_PER_COMMIT, queue)
                .sorted(comparing(SaltEvent::getMinionId, nullsLast(naturalOrder())).thenComparing(SaltEvent::getId));

        events.forEach(event -> {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Handling event {} in worker #{}", event.getId(), queue);
                LOG.trace(event.getData());
            }
            uncommittedEvents.add(event);
            notifyListeners(JsonParser.EVENTS.parse(event.getData()));
        });
    }

    /**
     * Handles any {@link Exception} raised from processEvents. Has special code to handle those that are thrown by
     * {@link PGEventListener}, as they may contain an exception handler.
     */
    private void handleExceptions(List<SaltEvent> uncommittedEvents, Exception exception) {
        if (!uncommittedEvents.isEmpty()) {
            List<Long> ids = uncommittedEvents.stream().map(SaltEvent::getId).toList();
            List<Long> deletedIds = SaltEventFactory.deleteSaltEvents(ids);
            LOG.error("Events {} were lost", deletedIds);
            // In case of error processing the event the transation is rollback in the PGeventListener
            // Then this method is called. We need a commit in here to make sure we clean the DB events
            // before we call the extra exception listener. This is needed to deal with cases where
            // the extra listener fails with exception, which will cause the transaction helper to rollback
            HibernateFactory.commitTransaction();
        }

        if (exception instanceof PGEventListenerException listenerException) {
            listenerException.getExceptionHandler().run();
        }
    }

    /**
     * Closes db connection
     */
    protected void closeDatabaseConnection() {
        try {
            if (!isEventStreamClosed()) {
                connection.close();
                LOG.debug("Database connection closed");
            }
        }
        catch (SQLException e) {
            LOG.warn("Error closing database connection during shutdown", e);
        }
    }

    /**
     * Checks if the database connection is closed.
     * @return true if the connection is closed or null, false otherwise.
     */
    public boolean isEventStreamClosed() {
        try {
            return connection == null || connection.isClosed();
        }
        catch (SQLException e) {
            return true;
        }
    }

    /**
     * Closes the event stream and all associated resources.
     * After this method is called, the event stream will no longer
     * process any events, and all executors will be shut down.
     * @throws IOException
     */
    public void close() throws IOException {
        LOG.debug("Closing PGEventStream");
        shutdown("Stream closed", true, true);
    }

    /**
     * Centralized shutdown method that coordinates orderly shutdown of all resources.
     * Note: invoking shutdown from a thread and waiting for it to shutdown will
     * result in timeout.
     *
     * @param reason the reason for shutdown (for logging and listener notification)
     * @param awaitNotificationPoller waits for notification poller executor to showdown
     * @param awaitConnectionWatchdog waits for connection watchdog executor to showdown
     */
    private void shutdown(String reason, boolean awaitNotificationPoller, boolean awaitConnectionWatchdog) {
        if (!shutdownInProgress.compareAndSet(false, true)) {
            LOG.debug("Shutdown already in progress, ignoring duplicate call");
            return;
        }

        LOG.warn("Shutting down PGEventStream: {}", reason);

        // Instructs all executors to shut down so they stop accepting new tasks
        notificationPollerExecutor.shutdown();
        watchdogExecutor.shutdown();
        executorServices.forEach(ThreadPoolExecutor::shutdown);

        // wait for executors to complete, force termination if needed
        for (int i = 0; i < executorServices.size(); i++) {
            waitForShutdown(executorServices.get(i), "event-executor-" + i);
        }
        if (awaitConnectionWatchdog) {
            waitForShutdown(watchdogExecutor, SALT_EVENT_CONNECTION_WATCHDOG);
        }
        if (awaitNotificationPoller) {
            waitForShutdown(notificationPollerExecutor, SALT_EVENT_NOTIFICATION_POLLER);
        }

        // clean up resources
        HibernateFactory.closeSession();
        closeDatabaseConnection();
        clearListeners(0, reason);

        LOG.info("PGEventStream shutdown complete");
    }

    /**
     * Waits for executor shutdown to complete. After a grace period, forces termination.
     *
     * @param executor the executor service to wait for
     * @param name the executor name for logging
     */
    private void waitForShutdown(ExecutorService executor, String name) {
        try {
            // Wait for in-flight tasks to complete (up to 10 seconds)
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                LOG.warn("{} did not terminate within 10s, forcing shutdown", name);
                executor.shutdownNow();

                // Wait for forced termination (up to 5 more seconds)
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    LOG.error("{} did not terminate even after forced shutdown", name);
                }
            }
        }
        catch (InterruptedException e) {
            LOG.warn("Interrupted while waiting for {} shutdown, forcing termination", name);
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
