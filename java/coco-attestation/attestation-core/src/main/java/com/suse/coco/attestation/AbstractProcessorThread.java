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

/**
 * Base class for the threads used by the {@link AttestationQueueProcessor}.
 */
abstract class AbstractProcessorThread implements Runnable {

    private final Thread thread;

    private boolean running;

    protected AbstractProcessorThread(String name) {
        thread = new Thread(this, name);
        running = false;
    }

    /**
     * Starts the processing of the thread.
     */
    public void start() {
        setRunning(true);
        thread.start();
    }

    /**
     * Stops the processing of the thread, if it is running.
     */
    public void stop() {
        if (!isRunning()) {
            return;
        }

        thread.interrupt();
    }

    /**
     * Wait for this thread to complete its processing.
     *
     * @throws InterruptedException when the wait is interrupted
     */
    public void await() throws InterruptedException {
        if (!isRunning()) {
            return;
        }

        thread.join();
    }

    /**
     * Check if this thread is currently running.
     * @return true if the thread is running.
     */
    public synchronized boolean isRunning() {
        return running;
    }

    /**
     * Specify that this thread has already stopped it's processing and should not be considered as running.
     * @param runningIn the new status
     */
    protected synchronized void setRunning(boolean runningIn) {
        this.running = runningIn;
    }
}
