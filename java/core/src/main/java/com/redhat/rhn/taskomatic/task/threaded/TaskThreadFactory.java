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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.taskomatic.task.threaded;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadFactory impl for Taskomatic
 */
public class TaskThreadFactory implements ThreadFactory {

    private final AtomicInteger threadNumberSequence = new AtomicInteger(0);

    private final String queueName;

    /**
     * Create a thread factory
     * @param queueNameIn the name of the queue
     */
    public TaskThreadFactory(String queueNameIn) {
        this.queueName = queueNameIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Thread newThread(Runnable task) {
        int taskNumber = threadNumberSequence.incrementAndGet();

        Thread thread = new Thread(task);
        thread.setName("TaskQueue-" + queueName + "-" + taskNumber);
        thread.setDaemon(true);
        return thread;
    }

}
