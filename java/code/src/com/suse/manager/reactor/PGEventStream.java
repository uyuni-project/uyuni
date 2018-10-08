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
import org.apache.log4j.Logger;

import java.io.IOException;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;

/**
 * Listen for notifications from the Postgres database (suseSaltEvent) and react on those.
 */
public class PGEventStream extends AbstractEventStream implements PGNotificationListener {

    private static final Logger LOG = Logger.getLogger(PGEventStream.class);
    private static final int MAX_EVENTS_PER_COMMIT = ConfigDefaults.get().getSaltEventsPerCommit();

    private PGConnection connection;

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

            LOG.debug("Listening succeeded, making sure there is no event left in queue...");
            notification(0, null, null);
        }
        catch (SQLException e) {
            throw new SaltException(e);
        }
    }

    @Override
    public void notification(int processId, String channelName, String payload) {
        List<SaltEvent> uncommittedEvents = new LinkedList<>();
        TransactionHelper.handlingTransaction(
                () -> processEvents(uncommittedEvents),
                e -> handleExceptions(uncommittedEvents, e));
    }

    /**
     * Reads one or more events from suseSaltEvent and notifies listeners (typically, {@link PGEventListener}).
     * @param uncommittedEvents used to keep track of events being processed
     */
    private void processEvents(List<SaltEvent> uncommittedEvents) {
        List<SaltEvent> events = SaltEventFactory.popSaltEvents(MAX_EVENTS_PER_COMMIT);

        while (!events.isEmpty()) {
            events.forEach(event -> {
                if (LOG.isTraceEnabled()) {
                    LOG.trace("Handling event " + event.getId());
                    LOG.trace(event.getData());
                }
                uncommittedEvents.add(event);
                notifyListeners(JsonParser.EVENTS.parse(event.getData()));
            });

            // check if any event is left
            events = SaltEventFactory.popSaltEvents(MAX_EVENTS_PER_COMMIT);
        }
    }

    /**
     * Handles any {@link Exception} raised from processEvents. Has special code to handle those that are thrown by
     * {@link PGEventListener}, as they may contain an exception handler.
     */
    private void handleExceptions(List<SaltEvent> uncommittedEvents, Exception exception) {
        SaltEventFactory.deleteSaltEvents(uncommittedEvents.stream());
        for (SaltEvent event : uncommittedEvents) {
            LOG.error("Event " + event.getId() + " was lost");
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
