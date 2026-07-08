/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.taskomatic.core;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Entry point for starting Taskomatic scheduling.
 * <p>
 * This class obtains the {@link SchedulerKernel} singleton and starts it in
 * a dedicated worker thread.
 * <p>
 * Despite its name, this class does not perform Unix-style daemonization
 * (no fork/detach/pidfile handling). It is expected to run in the foreground,
 * while process supervision and restart behavior are provided externally, by
 * systemd through the taskomatic shell wrapper.
 * @see SchedulerKernel
 */
public class TaskomaticDaemon {

    private static final Logger LOG = LogManager.getLogger(TaskomaticDaemon.class);

    private static final int SUCCESS = 0;

    private static final int FAILURE = -1;

    private final SchedulerKernel kernel;

    /**
     * Creates a new TaskomaticDaemon instance with the provided SchedulerKernel.
     *
     * @param kernelIn the {@link SchedulerKernel} instance to use for scheduling operations
     */
    private TaskomaticDaemon(SchedulerKernel kernelIn) {
        this.kernel = kernelIn;
    }

    /**
     * Main entry point for taskomatic
     *
     * @param argv "Command-line" parameters (currently ignored)
     */
    public static void main(String[] argv) {
        TaskomaticDaemon taskomatic = new TaskomaticDaemon(SchedulerKernel.getInstance());
        int exitCode = taskomatic.run();

        System.exit(exitCode);
    }

    /**
     * Starts the scheduler kernel and waits until it exits.
     *
     * @return {@link #SUCCESS} when startup runs to completion, {@link #FAILURE} otherwise
     */
    int run() {
        try {
            CompletableFuture<Void> resultFuture = new CompletableFuture<>();

            // With current implementation, using a separate thread here is mostly equivalent to
            // running startup() in main, but we keep this split to separate lifecycle orchestration
            // from the actual execution
            Thread kernelThread = new Thread(() -> startKernel(resultFuture), "kernel-daemon");
            kernelThread.start();
            
            // SchedulerKernel.startup() blocks until shutdown, so waiting here keeps the process alive.
            resultFuture.get();
            return SUCCESS;
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            LOG.fatal("Thread interrupted while starting SchedulerKernel", ex);
            return FAILURE;
        }
        catch (ExecutionException ex) {
            LOG.fatal("Unable to start SchedulerKernel", ex.getCause());
            return FAILURE;
        }
        catch (RuntimeException ex) {
            LOG.fatal("Unexpected exception while starting TaskomaticDaemon", ex);
            return FAILURE;
        }
    }

    private void startKernel(CompletableFuture<Void> resultFuture) {
        try {
            kernel.startup();
            resultFuture.complete(null);
        }
        catch (TaskomaticException | RuntimeException ex) {
            resultFuture.completeExceptionally(ex);
        }
    }
}
