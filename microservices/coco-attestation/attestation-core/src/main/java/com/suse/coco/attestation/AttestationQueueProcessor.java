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

import com.suse.coco.configuration.Configuration;
import com.suse.coco.module.AttestationModuleLoader;
import com.suse.common.concurrent.UnboundedGrowingThreadPoolExecutor;

import org.apache.ibatis.session.SqlSessionFactory;

import java.time.Duration;
import java.util.concurrent.ExecutorService;

import javax.sql.DataSource;

/**
 * Process entry in the table suseAttestationResult and executes the proper based on the result type.
 */
public class AttestationQueueProcessor {

    private final ListeningThread listeningThread;

    private final ProcessingThread processingThread;

    /**
     * Create an attestation queue processor.
     * @param sessionFactory the session factory to access the database
     * @param configuration the current application configuration
     * @param moduleLoader the attestation module loader
     */
    public AttestationQueueProcessor(
        SqlSessionFactory sessionFactory,
        Configuration configuration,
        AttestationModuleLoader moduleLoader
    ) {
        // Call the other constructor with the correct values
        this(
            sessionFactory.getConfiguration().getEnvironment().getDataSource(),
            new AttestationResultService(sessionFactory),
            new UnboundedGrowingThreadPoolExecutor(
                configuration.getCorePoolSize(),
                configuration.getMaximumPoolSize(),
                Duration.ofSeconds(configuration.getThreadKeepAliveInSeconds()),
                "attestation-processor-worker"
            ),
            moduleLoader,
            configuration.getBatchSize()
        );
    }

    /**
     * Create an attestation queue processor.
     * @param dataSource the datasource to be used to listen to the database notification
     * @param service the attestation result service
     * @param executorService the executor to perform the process
     * @param moduleLoader the attestation module loader
     * @param batchSize the batch size
     */
    protected AttestationQueueProcessor(
        DataSource dataSource,
        AttestationResultService service,
        ExecutorService executorService,
        AttestationModuleLoader moduleLoader,
        int batchSize
    ) {
        listeningThread = new ListeningThread(dataSource);
        processingThread = new ProcessingThread(service, executorService, moduleLoader, batchSize);

        // Link the two threads
        listeningThread.setProcessingThread(processingThread);
        processingThread.setListeningThread(listeningThread);
    }

    /**
     * Start the execution of the queue processor. This method will start the processing asynchronously and exit
     * immediately. Use {@link #awaitTermination()} to wait for the processor to complete.
     */
    public void start() {
        listeningThread.start();
        processingThread.start();
    }

    /**
     * Wait indefinitely for the processor to complete. This is the same as awaitTermination(0L)
     * @throws InterruptedException if the wait for all the threads to stop is interrupted
     */
    public void awaitTermination() throws InterruptedException {
        listeningThread.await();
        processingThread.await();
    }

    /**
     * Interrupts the execution of this processor.
     */
    public void stop() {
        listeningThread.stop();
        processingThread.stop();
    }

    /**
     * Check if this attestation processor is running
     * @return true if at least one of the processor threads is still running
     */
    public boolean isRunning() {
        return listeningThread.isRunning() || processingThread.isRunning();
    }
}
