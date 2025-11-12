/*
 * Copyright (c) 2009--2015 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.taskomatic.task.threaded;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.taskomatic.domain.TaskoRun;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Generic threaded queue suitable for use wherever Taskomatic
 * tasks need to process a number of work items in parallel.
 */
public class TaskQueue {

    private final byte[] emptyQueueWait = new byte[0];
    private final String queueName;
    private QueueDriver queueDriver;
    private ThreadPoolExecutor executor = null;
    private int executingWorkers = 0;
    private int queueSize = 0;
    private boolean taskQueueDone = true;
    private TaskoRun queueRun = null;

    /**
     * Default constructor
     *
     * @param queueNameIn the name of the queue
     */
    public TaskQueue(String queueNameIn) {
        this.queueName = queueNameIn;
    }

    /**
     * Store the QueueDriver instance used when run() is called
     * @param driver to be used as the current QueueDriver
     */
    public void setQueueDriver(QueueDriver driver) {
        queueDriver = driver;
    }

    /**
     * Get the current QueueDriver
     * @return current QueueDriver
     */
    public QueueDriver getQueueDriver() {
        return queueDriver;
    }

    /**
     * Callback all workers should call when starting
     * to process work
     */
    public synchronized void workerStarting() {
        executingWorkers++;
    }

    /**
     * Callback all workers should call when
     * finished with their work item
     */
    public synchronized void workerDone() {
        executingWorkers--;
        queueSize--;
        if (executingWorkers < 0) {
            executingWorkers = 0;
        }
        if (queueSize < 0) {
            queueSize = 0;
        }
        if (executingWorkers == 0) {
            synchronized (emptyQueueWait) {
                emptyQueueWait.notifyAll();
            }
            taskQueueDone = true;
        }
    }

    /**
     * Returns the number of currently executing workers
     * This should never be more than the thread pool's
     * maximum size
     * @return number of currently executing workers
     */
    public int getExecutingWorkerCount() {
        return executingWorkers;
    }

    /**
     * Returns the number of workers pending
     * @return number of workers pending
     */
    public int getQueueSize() {
        return queueSize;
    }

    /**
     * Create workers for all current candidates or set the current job run to FINISHED in
     * case there is no new candidates and workers are all done.
     */
    public void run() {
        shutdownExecutor();
        BlockingQueue<Runnable> workers = new LinkedBlockingQueue<>();
        queueSize += queueDriver.fetchCandidates();
        if (queueSize > 0) {
            queueDriver.getLogger().info("In the queue: {}", queueSize);
        }
        while (queueDriver.hasCandidates() && queueDriver.canContinue()) {
            QueueWorker worker = queueDriver.nextWorker();
            worker.setParentQueue(this);
            try {
                queueDriver.getLogger().debug("Putting worker");
                workers.put(worker);
                queueDriver.getLogger().debug("Put worker");
                unsetTaskQueueDone();
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                queueDriver.getLogger().error(e.getMessage(), e);
                HibernateFactory.commitTransaction();
                HibernateFactory.closeSession();
                HibernateFactory.getSession();
                return;
            }
        }
        executor = setupQueue(workers);

        if (queueDriver.isBlockingTaskQueue()) {
            queueDriver.getLogger().debug("Waiting for empty queue");
            try {
                waitForEmptyQueue();
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                queueDriver.getLogger().error(e.getMessage(), e);
                HibernateFactory.commitTransaction();
                HibernateFactory.closeSession();
                HibernateFactory.getSession();
                return;
            }
        }

        if (isTaskQueueDone()) {
            // everything done
            if (queueRun != null) {
                queueDriver.getLogger().debug("Finishing Task Queue run {}", queueRun.getId());
                queueRun.finished();
                queueRun.saveStatus(TaskoRun.STATUS_FINISHED);
            }
            else {
                queueDriver.getLogger().debug("Finishing Task Queue");
            }
            HibernateFactory.commitTransaction();
            HibernateFactory.closeSession();
            changeRun(null);
        }
        else {
            queueDriver.getLogger().debug("TaskQueue is not done. Leaving the run {} as RUNNING", queueRun.getId());
        }
    }

    /**
     * Waits indefinitely until the queue has emptied of all workers
     * @throws InterruptedException the wait is interrupted
     */
    public void waitForEmptyQueue() throws InterruptedException {
        synchronized (emptyQueueWait) {
            while (queueSize != 0) {
                emptyQueueWait.wait();
            }
        }
    }

    void shutdown() {
        executor.shutdownNow();
        waitExecutorTermination();
    }

    private void shutdownExecutor() {
        if (executor != null) {
            executor.shutdown();
            waitExecutorTermination();
            executor = null;
        }
    }

    private void waitExecutorTermination() {
        boolean terminated = executor.isTerminated();
        while (!terminated) {
            try {
                terminated = executor.awaitTermination(100, TimeUnit.MILLISECONDS);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                queueDriver.getLogger().error(e.getMessage(), e);
                return;
            }
        }
    }

    private ThreadPoolExecutor setupQueue(BlockingQueue<Runnable> workers) {
        int size = queueDriver.getMaxWorkers();

        TaskThreadFactory factory = new TaskThreadFactory(queueName);
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(size, size, 5, TimeUnit.SECONDS, workers, factory);

        poolExecutor.allowCoreThreadTimeOut(false);
        poolExecutor.prestartAllCoreThreads();

        return poolExecutor;
    }

    /**
     * - while there are workers in the queue,
     * they will be executed as within the same run
     * to get stored all the logs together
     * - otherwise we would lose the logs,
     * because worker execution get managed automatically by the queue
     * @param runIn associated run
     * @return whether run was changed
     */
    public boolean changeRun(TaskoRun runIn) {
        synchronized (this) {
            if (runIn == null) {
                queueRun = null;
                return true;
            }
            else if (queueRun == null) {
                queueRun = runIn;
                return true;
            }
            return false;
        }
    }

    /**
     * returns queue run
     * @return queue log run
     */
    public TaskoRun getQueueRun() {
        return queueRun;
    }

    private synchronized boolean isTaskQueueDone() {
        return taskQueueDone;
    }

    private synchronized void unsetTaskQueueDone() {
        taskQueueDone = false;
    }
}
