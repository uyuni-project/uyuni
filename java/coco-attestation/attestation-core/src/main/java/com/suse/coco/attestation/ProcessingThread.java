/*
 * Copyright (c) 2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.coco.attestation;

import com.suse.coco.module.AttestationModuleLoader;
import com.suse.coco.module.AttestationWorker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Main processing thread of the {@link AttestationQueueProcessor}. Extracts the attestation result of the supported
 * types and uses the correct {@link com.suse.coco.module.AttestationModule} to process them.
 */
class ProcessingThread extends AbstractProcessorThread {

    private static final Logger LOGGER = LogManager.getLogger(ProcessingThread.class);

    private final Object dataAvailableLock = new Object();

    private final AttestationModuleLoader moduleLoader;

    private final AttestationResultService service;

    private final ExecutorService executorService;

    private final int batchSize;

    private ListeningThread listeningThread;

    ProcessingThread(AttestationResultService serviceIn, ExecutorService executorServiceIn,
                     AttestationModuleLoader moduleLoaderIn, int batchSizeIn) {
        super("attestation-processor-main");

        moduleLoader = moduleLoaderIn;
        service = serviceIn;
        executorService = executorServiceIn;
        batchSize = batchSizeIn;
    }

    public void setListeningThread(ListeningThread listeningThreadIn) {
        this.listeningThread = listeningThreadIn;
    }

    /**
     * Notifies the processor that some data is ready to be processed. Used by the {@link ListeningThread} to trigger
     * the processing after receiving a notification.
     */
    public void notifyDataAvailable() {
        if (isRunning()) {
            synchronized (dataAvailableLock) {
                dataAvailableLock.notifyAll();
            }
        }
    }

    @Override
    public void start() {
        if (listeningThread == null) {
            throw new IllegalArgumentException("Set the listening thread before starting the processor");
        }

        super.start();
    }

    @Override
    public void run() {
        try {
            while (!Thread.currentThread().isInterrupted() && listeningThread.isRunning()) {
                // Load the pending attestation results of the supported types
                List<Long> results = service.getPendingResultByType(moduleLoader.getSupportedResultTypes(), batchSize);
                if (results.isEmpty()) {
                    LOGGER.info("No attestation result to process - Waiting");
                    synchronized (dataAvailableLock) {
                        dataAvailableLock.wait();
                    }
                    continue;
                }

                LOGGER.info("Processing attestation results {}", results);
                CountDownLatch workersDone = new CountDownLatch(results.size());

                // Process each one of them in a separate worker thread
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

                // Wait for all workers to complete
                workersDone.await();
            }
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        finally {
            setRunning(false);

            // Shut down the executor service
            if (!executorService.isShutdown()) {
                LOGGER.debug("Shutting down the worker executor service");
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
        }

        LOGGER.debug("Processor thread is stopped");
    }
}
