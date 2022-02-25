/*
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
package com.redhat.rhn.taskomatic;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.taskomatic.domain.TaskoRun;
import com.redhat.rhn.taskomatic.domain.TaskoSchedule;
import com.redhat.rhn.taskomatic.domain.TaskoTask;
import com.redhat.rhn.taskomatic.domain.TaskoTemplate;
import com.redhat.rhn.taskomatic.task.RhnJob;
import com.redhat.rhn.taskomatic.task.RhnQueueJob;
import com.redhat.rhn.taskomatic.task.TaskHelper;

import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 *
 * TaskoJob
 */
public class TaskoJob implements Job {

    private static Logger log = Logger.getLogger(TaskoJob.class);
    private static Map<String, Integer> tasks = new ConcurrentHashMap<>();
    private static Map<String, Object> lastStatus = new ConcurrentHashMap<>();

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss")
            .withZone(ZoneId.systemDefault());


    private Long scheduleId;

    static {
        for (TaskoTask task : TaskoFactory.listTasks()) {
            tasks.put(task.getName(), 0);
            lastStatus.put(task.getName(), TaskoRun.STATUS_FINISHED);
        }
        TaskoFactory.closeSession();
    }

    /**
     * default constructor
     * job is always associated with a schedule
     * @param scheduleIdIn schedule id
     */
    public TaskoJob(Long scheduleIdIn) {
        setScheduleId(scheduleIdIn);
    }

    private boolean isTaskSingleThreaded(TaskoTask task) {
        try {
            return Class.forName(task.getTaskClass()).newInstance() instanceof RhnQueueJob;
        }
        catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
            // will be caught later
            log.error("Error trying to instance a new class of " +
                    task.getTaskClass() + ": " + e.getMessage());
            return false;
        }
    }

    private boolean isTaskRunning(TaskoTask task) {
        return tasks.get(task.getName()) > 0;
    }

    private boolean isTaskThreadAvailable(RhnJob job, TaskoTask task) {
        return tasks.get(task.getName()) < job.getParallelThreads();
    }

    private static synchronized void markTaskRunning(TaskoTask task) {
        int count = tasks.get(task.getName());
        count++;
        tasks.put(task.getName(), count);
    }

    private static synchronized void unmarkTaskRunning(TaskoTask task) {
        int count = tasks.get(task.getName());
        count--;
        tasks.put(task.getName(), count);
    }

    /**
     * {@inheritDoc}
     */
    public void execute(JobExecutionContext context)
        throws JobExecutionException {
        TaskoRun previousRun = null;

        TaskoSchedule schedule = TaskoFactory.lookupScheduleById(scheduleId);
        if (schedule == null) {
            // means, that schedule was deleted (in the DB), but quartz still schedules it
            log.error("No such schedule with id  " + scheduleId);
            TaskoQuartzHelper.unscheduleTrigger(context.getTrigger());
            return;
        }

        Instant start = Instant.now();
        log.info(schedule.getJobLabel() + ":" + " bunch " + schedule.getBunch().getName() +
                " STARTED");

        for (TaskoTemplate template : schedule.getBunch().getTemplates()) {
            if ((previousRun == null) ||    // first run
                    (template.getStartIf() == null) ||  // do not care
                    (previousRun.getStatus().equals(template.getStartIf()))) {
                TaskoTask task = template.getTask();

                if (isTaskSingleThreaded(task) && isTaskRunning(task)) {
                    log.debug(schedule.getJobLabel() + ":" + " task " + task.getName() +
                            " already running ... LEAVING");
                    previousRun = null;
                }
                else {

                    try {
                        Class<RhnJob> jobClass = (Class<RhnJob>) Class.forName(template.getTask().getTaskClass());
                        RhnJob job = jobClass.getDeclaredConstructor().newInstance();
                        int rescheduleSeconds = job.getRescheduleTime();
                        if (!isTaskThreadAvailable(job, task)) {
                            log.info(schedule.getJobLabel() + " RESCHEDULED in " + rescheduleSeconds + " seconds");
                            TaskoQuartzHelper.rescheduleJob(schedule,
                                    ZonedDateTime.now().plusSeconds(rescheduleSeconds).toInstant());
                        }
                        else {

                            markTaskRunning(task);

                            try {
                                log.debug(schedule.getJobLabel() + ":" + " task " + task.getName() +
                                        " started");
                                TaskoRun taskRun = new TaskoRun(schedule.getOrgId(), template, scheduleId);
                                TaskoFactory.save(taskRun);
                                HibernateFactory.commitTransaction();
                                HibernateFactory.closeSession();

                                try {
                                    job.execute(context, taskRun);
                                }
                                catch (Exception e) {
                                    if (HibernateFactory.getSession().getTransaction().isActive()) {
                                        HibernateFactory.rollbackTransaction();
                                        HibernateFactory.closeSession();
                                    }
                                    job.appendExceptionToLogError(e);
                                    taskRun.failed();
                                    HibernateFactory.commitTransaction();
                                    HibernateFactory.closeSession();
                                }

                                // rollback everything, what the application changed and didn't committed
                                if (TaskoFactory.getSession().getTransaction().isActive()) {
                                    TaskoFactory.rollbackTransaction();
                                    HibernateFactory.closeSession();
                                }

                                log.debug(task.getName() + " (" + schedule.getJobLabel() + ") ... " +
                                        taskRun.getStatus().toLowerCase());
                                if (((taskRun.getStatus() == TaskoRun.STATUS_FINISHED) ||
                                        (taskRun.getStatus() == TaskoRun.STATUS_FAILED)) &&
                                        (taskRun.getStatus() != lastStatus.get(task.getName()))) {
                                    String email = "Taskomatic bunch " + schedule.getBunch().getName() +
                                            " was scheduled to run within the " + schedule.getJobLabel() +
                                            " schedule.\n\n" + "Subtask " + task.getName();
                                    if (taskRun.getStatus() == TaskoRun.STATUS_FAILED) {
                                        email += " failed.\n\n" + "For more information check ";
                                        email += taskRun.getStdErrorPath() + ".";
                                    }
                                    else {
                                        email += " finished successfully and is back to normal.";
                                    }
                                    log.info("Sending e-mail ... " + task.getName());
                                    TaskHelper.sendTaskoEmail(taskRun.getOrgId(), email);
                                    lastStatus.put(task.getName(), taskRun.getStatus());
                                }
                                previousRun = taskRun;
                            }
                            finally {
                                unmarkTaskRunning(task);
                            }
                        }
                    }
                    catch (Exception e) {
                        log.error(e);
                    }
                }

            }
            else {
                log.info("Interrupting " + schedule.getBunch().getName() +
                        " (" + schedule.getJobLabel() + ")");
                break;
            }
        }
        HibernateFactory.closeSession();
        log.info(schedule.getJobLabel() + ":" + " bunch " + schedule.getBunch().getName() +
                " FINISHED");
        log.debug(schedule.getJobLabel() + ":" + " bunch " + schedule.getBunch().getName() +
                " START: " + TIMESTAMP_FORMAT.format(start) + " END: " + TIMESTAMP_FORMAT.format(Instant.now()));
    }

    /**
     * setter for scheduleId
     * @param scheduleIdIn The scheduleId to set.
     */
    public void setScheduleId(Long scheduleIdIn) {
        this.scheduleId = scheduleIdIn;
    }

    /**
     * getter for scheduleId
     * @return Returns the scheduleId.
     */
    public Long getScheduleId() {
        return scheduleId;
    }
}
