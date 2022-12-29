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
package com.redhat.rhn.taskomatic.task.threaded;

import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides an easy-to-use way to locate and use threaded
 * work queues. Each queue is only created once.
 */
public class TaskQueueFactory {

    private static final TaskQueueFactory INSTANCE = new TaskQueueFactory();

    private final Map<String, TaskQueue> queues = new HashMap<>();

    /**
     * Get the singleton instance
     * @return the single instance of TaskQueueFactory
     */
    public static TaskQueueFactory get() {
        return INSTANCE;
    }

    private TaskQueueFactory() {
        Runtime.getRuntime().addShutdownHook(new ShutdownHook(this));
    }

    /**
     * Retrieves a queue by name
     * @param name queue name
     * @return queue if found, otherwise null
     */
    public TaskQueue getQueue(String name) {
        synchronized (queues) {
            return queues.get(name);
        }
    }

    /**
     * Create the queue, if it doesn't exist already. If the
     * queue has been created on a previous call to createQueue(),
     * then that instance is returned instead.
     * @param name queue name
     * @param driverClass class to use as the queue driver
     * @param loggerIn queue logger
     * @return queue instance
     * @throws Exception error occurred during queue creation
     */
    public TaskQueue createQueue(String name, Class<? extends QueueDriver<?>> driverClass, Logger loggerIn)
        throws Exception {
        TaskQueue retval;
        synchronized (queues) {
            retval = queues.get(name);
            if (retval == null) {
                retval = new TaskQueue();
                QueueDriver<?> driver = driverClass.getDeclaredConstructor().newInstance();
                driver.setLogger(loggerIn);
                driver.initialize();
                retval.setQueueDriver(driver);
                queues.put(name, retval);
            }
        }
        return retval;
    }

    /**
     * Removes the queue from the map of available queues.
     * This DOES NOT shutdown the queue or perform any cleanup.
     * @param name queue name
     * @return queue instance if found, otherwise null
     */
    public TaskQueue removeQueue(String name) {
        synchronized (queues) {
            return queues.remove(name);
        }
    }

    void closeAllQueues() {
        synchronized (queues) {
            queues.values().forEach(TaskQueue::shutdown);
        }
        queues.clear();
    }

    /**
     * JVM shutdown hook used to clean up any remaining queues
     */
    static class ShutdownHook extends Thread {

        private final TaskQueueFactory factory;

        ShutdownHook(TaskQueueFactory factoryIn) {
            factory = factoryIn;
        }

        @Override
        public void run() {
            factory.closeAllQueues();
        }
    }
}
