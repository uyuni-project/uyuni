/**
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
import static java.util.stream.Collectors.toList;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.reactor.SaltEvent;
import com.redhat.rhn.domain.reactor.SaltEventFactory;
import com.redhat.rhn.frontend.events.TransactionHelper;

import com.impossibl.postgres.api.jdbc.PGConnection;
import com.impossibl.postgres.api.jdbc.PGNotificationListener;
import com.impossibl.postgres.jdbc.PGDataSource;
import com.suse.salt.netapi.event.AbstractEventStream;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.parser.JsonParser;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

/**
 * Listen for notifications from the Postgres database (suseSaltEvent) and react on those.
 */
public class PGEventStream extends AbstractEventStream implements PGNotificationListener {

    private static final Logger LOG = Logger.getLogger(PGEventStream.class);
    private static final int MAX_EVENTS_PER_COMMIT = ConfigDefaults.get().getSaltEventsPerCommit();
    private static final int THREAD_POOL_SIZE = ConfigDefaults.get().getSaltEventThreadPoolSize();

    private PGConnection connection;
    private final ThreadPoolExecutor executorService = new ThreadPoolExecutor(
            THREAD_POOL_SIZE,
            THREAD_POOL_SIZE,
            0L,
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(),
            new BasicThreadFactory.Builder().namingPattern("salt-event-thread-%d").build()
    );

    /**
     * Default constructor, connects to Postgres and waits for events.
     * @throws SaltException if connection fails
     */
    public PGEventStream() throws SaltException {
        PGDataSource dataSource = new PGDataSource();
        Config config = Config.get();
        dataSource.setHost(config.getString(ConfigDefaults.DB_HOST));
        dataSource.setPort(config.getInt(ConfigDefaults.DB_PORT));
        dataSource.setDatabase(config.getString(ConfigDefaults.DB_NAME));
        dataSource.setUser(config.getString(ConfigDefaults.DB_USER));
        dataSource.setPassword(config.getString(ConfigDefaults.DB_PASSWORD));

        try {
            connection = (PGConnection) dataSource.getConnection();
            connection.addNotificationListener(this);

            Statement stmt = connection.createStatement();
            stmt.execute("LISTEN suseSaltEvent");
            stmt.close();

            startConnectionWatchdog();

            LOG.debug("Listening succeeded, making sure there is no event left in queue...");
            notification(SaltEventFactory.countSaltEvents());
        }
        catch (SQLException e) {
            throw new SaltException(e);
        }
    }

    /**
     * Checks every 5s that the connection is alive. If not, notifies all listeners that we are shutting down.
     *
     * It is up to SaltReactor to create a new instance of this class and restart from scratch.
     */
    private void startConnectionWatchdog() {
        new Timer("salt-event-connection-watchdog").schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    try (Statement s = connection.createStatement()) {
                        s.execute("SELECT 'salt-event-connection-watchdog';");
                    }
                }
                catch (SQLException e) {
                    cancel();
                    clearListeners(0, "Postgres notification connection was lost");
                }
            }
        }, 0, 5_000);
    }

    @Override
    public void notification(int processId, String channelName, String payload) {
        notification(Long.parseLong(payload));
    }

    /**
     * Called every time a notification from Postgres (ultimately from mgr_engine.py) is fired.
     * @param count the number of INSERTed events to be handled
     */
    public void notification(long count) {
        // compute the number of jobs we need to do - each job COMMITs individually
        // jobs = events / MAX_EVENTS_PER_COMMIT (rounded up)
        long jobs = (count + MAX_EVENTS_PER_COMMIT - 1) / MAX_EVENTS_PER_COMMIT;

        // queue one handlingTransaction(processEvents) call per job
        for (int j = 0; j < jobs; j++) {
            executorService.execute(() -> {
                List<SaltEvent> uncommittedEvents = new LinkedList<>();
                TransactionHelper.handlingTransaction(
                        () -> processEvents(uncommittedEvents),
                        e -> handleExceptions(uncommittedEvents, e));
            });
        }
    }

    /**
     * Reads one or more events from suseSaltEvent and notifies listeners (typically, {@link PGEventListener}).
     * @param uncommittedEvents used to keep track of events being processed
     */
    private void processEvents(List<SaltEvent> uncommittedEvents) {
        Stream<SaltEvent> events = SaltEventFactory.popSaltEvents(MAX_EVENTS_PER_COMMIT)
                .sorted(comparing(SaltEvent::getMinionId, nullsLast(naturalOrder())).thenComparing(SaltEvent::getId));

        events.forEach(event -> {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Handling event " + event.getId());
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
            List<Long> ids = uncommittedEvents.stream().map(SaltEvent::getId).collect(toList());
            List<Long> deletedIds = SaltEventFactory.deleteSaltEvents(ids);
            LOG.error("Events " + deletedIds.toString() + " were lost");
        }

        if (exception instanceof PGEventListenerException) {
            PGEventListenerException listenerException = (PGEventListenerException) exception;
            listenerException.getExceptionHandler().run();
        }
    }

    @Override
    public boolean isEventStreamClosed() {
        try {
            return connection == null || connection.isClosed();
        }
        catch (SQLException e) {
            return true;
        }
    }

    @Override
    public void close() throws IOException {
        try {
            connection.close();
            LOG.debug("connection closed gracefully");
        }
        catch (SQLException e) {
            throw new IOException(e);
        }
    }
}
