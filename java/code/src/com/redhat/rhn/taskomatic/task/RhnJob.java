/**
 * Copyright (c) 2010--2015 Red Hat, Inc.
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
package com.redhat.rhn.taskomatic.task;

import com.redhat.rhn.taskomatic.TaskoRun;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * RhnJob
 * @version $Rev$
 */
public interface RhnJob extends Job {

    String DEFAULT_LOGGING_LAYOUT = "%d [%t] %-5p %c %x - %m%n";

    /**
     * execute method to be called
     * @param context job context
     * @param taskRun associated run
     * @throws JobExecutionException thrown in case of any runtime exception
     */
    void execute(JobExecutionContext context, TaskoRun taskRun)
        throws JobExecutionException;

    /**
     * appends an exception message to log error
     * useful in case job failed to start
     * @param e exception
     */
    void appendExceptionToLogError(Exception e);
}
