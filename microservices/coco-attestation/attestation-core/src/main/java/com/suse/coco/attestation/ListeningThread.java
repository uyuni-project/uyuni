/*
 * Copyright (c) 2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.coco.attestation;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgresql.PGNotification;
import org.postgresql.jdbc.PgConnection;

import java.net.SocketException;
import java.sql.Connection;
import java.sql.Statement;
import java.util.stream.IntStream;

import javax.sql.DataSource;

/**
 * Listener thread of the {@link AttestationQueueProcessor}. Waits for the PostgreSQL notifications and notifies
 * the {@link ProcessingThread} when there is data to process.
 */
class ListeningThread extends AbstractProcessorThread {

    private static final Logger LOGGER = LogManager.getLogger(ListeningThread.class);

    private final Object connectionLock = new Object();

    private final DataSource dataSource;

    private boolean forceShutdown;

    private PgConnection pgConnection;

    private ProcessingThread processingThread;

    /**
     * Builds a listener thread
     * @param dataSourceIn the datasource to be used to connect to the database
     */
    ListeningThread(DataSource dataSourceIn) {
        super("attestation-processor-listener");

        dataSource = dataSourceIn;

        forceShutdown = false;

        processingThread = null;
        pgConnection = null;
    }

    public void setProcessingThread(ProcessingThread processingThreadIn) {
        this.processingThread = processingThreadIn;
    }

    @Override
    public void start() {
        if (processingThread == null) {
            throw new IllegalStateException("Set the processing thread before starting the listener");
        }

        super.start();
    }

    @Override
    public void stop() {
        if (!isRunning()) {
            return;
        }

        // Force the closure of the database connection
        forceShutdown = true;
        abortConnection();

        super.stop();
    }

    @Override
    public void run() {
        // Obtain a connection from the datasource
        try (Connection connection = dataSource.getConnection()) {
            // Ensure the connection is of the correct type
            if (!connection.isWrapperFor(PgConnection.class)) {
                throw new IllegalStateException("Unexpected wrapped connection " +
                    connection.unwrap(Object.class).getClass().getName());
            }

            updateConnection(connection.unwrap(PgConnection.class));

            try (Statement statement = pgConnection.createStatement()) {
                statement.execute("LISTEN pendingAttestationResult");
            }

            while (!Thread.currentThread().isInterrupted() && processingThread.isRunning()) {
                PGNotification[] notifications = pgConnection.getNotifications(60_000);
                if (notifications != null && notifications.length > 0) {
                    LOGGER.info("Got {} notification(s) from pendingAttestationResult", notifications.length);

                    if (LOGGER.isDebugEnabled()) {
                        IntStream.range(0, notifications.length)
                            .forEach(idx -> LOGGER.debug(
                                "\tNotification #{} -> {} {} {}",
                                idx,
                                notifications[idx].getPID(),
                                notifications[idx].getName(),
                                notifications[idx].getParameter()
                            ));
                    }

                    // Notify the thread waiting
                    processingThread.notifyDataAvailable();
                }
            }
        }
        catch (Exception ex) {
            // A socket closed exception is expected when we are shutting down the listener
            if (!(forceShutdown && ex.getCause() instanceof SocketException)) {
                LOGGER.error("Unexpected exception while listening to notifications", ex);
            }
        }
        finally {
            // Cleanup the state
            updateConnection(null);
            setRunning(false);

            // We are shutting down the listener. Notify the main thread to unblock it, if it is waiting.
            processingThread.notifyDataAvailable();
        }

        LOGGER.debug("Notification listener thread is stopped");
    }

    private void abortConnection() {
        synchronized (connectionLock) {
            if (pgConnection != null) {
                pgConnection.getQueryExecutor().abort();
            }
        }
    }

    private void updateConnection(PgConnection pgConnectionIn) {
        synchronized (connectionLock) {
            this.pgConnection = pgConnectionIn;
        }
    }
}
