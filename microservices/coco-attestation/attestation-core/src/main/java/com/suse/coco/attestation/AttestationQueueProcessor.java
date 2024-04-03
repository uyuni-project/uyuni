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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.coco.attestation;

import com.suse.coco.configuration.Configuration;
import com.suse.coco.module.AttestationModuleLoader;
import com.suse.coco.modules.AttestationWorker;
import com.suse.common.concurrent.UnboundedGrowingThreadPoolExecutor;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;

import java.sql.Connection;
import java.sql.Statement;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import javax.sql.DataSource;

/**
 * Process entry in the table suseAttestationResult and executes the proper based on the result type.
 */
public class AttestationQueueProcessor implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger(AttestationQueueProcessor.class);

    private final Object dataAvailable = new Object();

    private final AtomicBoolean listenerRunning = new AtomicBoolean(false);

    private final SqlSessionFactory sessionFactory;

    private final AttestationModuleLoader moduleLoader;

    private final AttestationResultService service;

    private final ExecutorService executorService;

    private final int batchSize;

    /**
     * Create an attestation queue processor.
     * @param sessionFactoryIn the session factory to access the database
     * @param configurationIn the current application configuration
     * @param moduleLoaderIn the attestation module loader
     */
    public AttestationQueueProcessor(
        SqlSessionFactory sessionFactoryIn,
        Configuration configurationIn,
        AttestationModuleLoader moduleLoaderIn
    ) {
        sessionFactory = sessionFactoryIn;
        service = new AttestationResultService(sessionFactoryIn);

        executorService = new UnboundedGrowingThreadPoolExecutor(
            configurationIn.getCorePoolSize(),
            configurationIn.getMaximumPoolSize(),
            Duration.ofSeconds(configurationIn.getThreadKeepAliveInSeconds()),
            "attestation-processor-worker"
        );

        moduleLoader = moduleLoaderIn;
        batchSize = configurationIn.getBatchSize();
    }

    /**
     * Create an attestation queue processor.
     * @param sessionFactoryIn the session factory
     * @param serviceIn the attestation result service
     * @param executorServiceIn the executor to perform the process
     * @param moduleLoaderIn the attestation module loader
     * @param batchSizeIn the batch size
     */
    protected AttestationQueueProcessor(
            SqlSessionFactory sessionFactoryIn,
            AttestationResultService serviceIn,
            ExecutorService executorServiceIn,
            AttestationModuleLoader moduleLoaderIn,
            int batchSizeIn
    ) {
        sessionFactory = sessionFactoryIn;
        service = serviceIn;
        executorService = executorServiceIn;
        moduleLoader = moduleLoaderIn;
        batchSize = batchSizeIn;
    }

    @Override
    public void run() {
        Thread listenerThread = createListenerThread();

        try {
            while (!Thread.currentThread().isInterrupted() && listenerRunning.get()) {
                List<Long> results = service.getPendingResultByType(moduleLoader.getSupportedResultTypes(), batchSize);
                if (results.isEmpty()) {
                    LOGGER.info("No attestation result to process - Waiting");
                    synchronized (dataAvailable) {
                        dataAvailable.wait();
                    }
                    continue;
                }

                LOGGER.info("Processing attestation results {}", results);
                CountDownLatch workersDone = new CountDownLatch(results.size());

                results.forEach(resultId -> executorService.execute(() -> {
                    try {
                        service.processAttestationResult(resultId, (session, result) -> {
                            AttestationWorker worker = moduleLoader.createWorker(result.getResultType());
                            return worker.process(session, result);
                        });
                    }
                    catch (Exception ex) {
                        LOGGER.error("Unable to correctly process attestation result with id {}", resultId, ex);
                    }
                    finally {
                        workersDone.countDown();
                    }
                }));

                workersDone.await();
            }
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        catch (Exception ex) {
            LOGGER.error("Unable to process", ex);
        }

        // Shut down the executor
        shutdownExecutor();

        // Interrupt the listener thread if still alive
        stopListenerThread(listenerThread);
    }

    private Thread createListenerThread() {
        Thread listenerThread = new Thread(this::listenForAttestationResults, "attestation-processor-listener");

        listenerRunning.set(true);
        listenerThread.start();

        return listenerThread;
    }

    private static void stopListenerThread(Thread listenerThread) {
        if (!listenerThread.isAlive() || listenerThread.isInterrupted()) {
            return;
        }

        listenerThread.interrupt();
    }

    private void shutdownExecutor() {
        if (executorService.isShutdown()) {
            return;
        }

        executorService.shutdown();

        boolean isTerminated = executorService.isTerminated();
        while (!isTerminated) {
            try {
                isTerminated = executorService.awaitTermination(1, TimeUnit.SECONDS);
            }
            catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void listenForAttestationResults() {
        try {
            // Retrieve the datasource from mybatis configuration and obtain a connection
            DataSource dataSource = sessionFactory.getConfiguration().getEnvironment().getDataSource();
            try (Connection connection = dataSource.getConnection()) {
                // Ensure the connection is of the correct type
                if (!connection.isWrapperFor(PGConnection.class)) {
                    throw new IllegalStateException("Unexpected wrapped connection " +
                        connection.unwrap(Object.class).getClass().getName());
                }

                try (Statement statement = connection.createStatement()) {
                    statement.execute("LISTEN pendingAttestationResult");
                }

                while (!Thread.currentThread().isInterrupted()) {
                    PGNotification[] notifications = connection.unwrap(PGConnection.class).getNotifications(60_000);
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
                        synchronized (dataAvailable) {
                            dataAvailable.notifyAll();
                        }
                    }
                }
            }
        }
        catch (Exception ex) {
            LOGGER.error("Unexpected exception while listening to notifications", ex);
        }

        LOGGER.info("Stopping notification listener");
        listenerRunning.set(false);

        // Listener is stopping, better unblock the other thread.
        synchronized (dataAvailable) {
            dataAvailable.notifyAll();
        }
    }

}
