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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


/**
 *
 * TaskoJob
 */
public class TaskoJob implements Job {

    private static Logger log = LogManager.getLogger(TaskoJob.class);
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
        HibernateFactory.closeSession();
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
            return Class.forName(task.getTaskClass()).getDeclaredConstructor().newInstance() instanceof RhnQueueJob;
        }
        catch (InstantiationException | ClassNotFoundException | IllegalAccessException | NoSuchMethodException |
               InvocationTargetException e) {
            // will be caught later
            log.error("Error trying to instance a new class of {}: {}", task.getTaskClass(), e.getMessage());
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
    @Override
    public void execute(JobExecutionContext context) {
        TaskoRun previousRun = null;

        TaskoSchedule schedule = TaskoFactory.lookupScheduleById(scheduleId);
        if (schedule == null) {
            // means, that schedule was deleted (in the DB), but quartz still schedules it
            log.error("No such schedule with id  {}", scheduleId);
            TaskoQuartzHelper.unscheduleTrigger(context.getTrigger());
            return;
        }

        Instant start = Instant.now();
        log.info("{}: bunch {} STARTED", schedule.getJobLabel(), schedule.getBunch().getName());

        for (TaskoTemplate template : schedule.getBunch().getTemplates()) {
            if ((previousRun != null) &&    // first run
                    (template.getStartIf() != null) &&  // do not care
                    !previousRun.getStatus().equals(template.getStartIf())) {
                log.info("Interrupting {} ({})", schedule.getBunch().getName(), schedule.getJobLabel());
                break;
            }
            TaskoTask task = template.getTask();
            previousRun = runTask(schedule, task, template, context);
        }
        HibernateFactory.closeSession();
        log.info("{}: bunch {} FINISHED", schedule.getJobLabel(), schedule.getBunch().getName());
        if (log.isDebugEnabled()) {
            log.debug("{}: bunch {} START: {} END: {}", schedule.getJobLabel(), schedule.getBunch().getName(),
                    TIMESTAMP_FORMAT.format(start), TIMESTAMP_FORMAT.format(Instant.now()));
        }
    }

    private boolean isTaskRunning(TaskoSchedule schedule, TaskoTask task) {
        if (isTaskSingleThreaded(task) && isTaskRunning(task)) {
            log.debug("{}: task {} already running ... LEAVING", schedule.getJobLabel(), task.getName());
            return true;
        }
        return false;
    }

    private boolean checkThreadAvailable(TaskoSchedule schedule, RhnJob job, TaskoTask task) {
        if (!isTaskThreadAvailable(job, task)) {
            int rescheduleSeconds = job.getRescheduleTime();
            log.info("{} RESCHEDULED in {} seconds", schedule.getJobLabel(), rescheduleSeconds);
            TaskoQuartzHelper.rescheduleJob(schedule,
                    ZonedDateTime.now().plusSeconds(rescheduleSeconds).toInstant());
            return false;
        }
        return true;
    }

    private TaskoRun runTask(TaskoSchedule schedule, TaskoTask task, TaskoTemplate template,
                             JobExecutionContext context) {
        TaskoRun result = null;
        if (!isTaskRunning(schedule, task)) {
            try {
                Class<RhnJob> jobClass = (Class<RhnJob>) Class.forName(template.getTask().getTaskClass());
                RhnJob job = jobClass.getDeclaredConstructor().newInstance();

                if (checkThreadAvailable(schedule, job, task)) {
                    markTaskRunning(task);

                    try {
                        log.debug("{}: task {} started", schedule.getJobLabel(), task.getName());
                        TaskoRun taskRun = new TaskoRun(schedule.getOrgId(), template, scheduleId);
                        TaskoFactory.save(taskRun);
                        HibernateFactory.commitTransaction();
                        HibernateFactory.closeSession();

                        doExecute(job, context, taskRun);

                        // rollback everything, what the application changed and didn't committed
                        if (HibernateFactory.getSession().getTransaction().isActive()) {
                            HibernateFactory.rollbackTransaction();
                            HibernateFactory.closeSession();
                        }

                        if (log.isDebugEnabled()) {
                            log.debug("{} ({}) ... {}", task.getName(), schedule.getJobLabel(),
                                    taskRun.getStatus().toLowerCase());
                        }
                        if (List.of(TaskoRun.STATUS_FINISHED, TaskoRun.STATUS_FAILED)
                                .contains(taskRun.getStatus()) &&
                                !Objects.equals(taskRun.getStatus(), lastStatus.get(task.getName()))) {
                            sendStatusMail(schedule, taskRun, task);
                            lastStatus.put(task.getName(), taskRun.getStatus());
                        }
                        result = taskRun;
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
        return result;
    }

    private void doExecute(RhnJob job, JobExecutionContext context, TaskoRun taskRun) {
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
    }

    private void sendStatusMail(TaskoSchedule schedule, TaskoRun taskRun, TaskoTask task) {
        String email = "Taskomatic bunch " + schedule.getBunch().getName() +
                " was scheduled to run within the " + schedule.getJobLabel() +
                " schedule.\n\n" + "Subtask " + task.getName();
        if (Objects.equals(taskRun.getStatus(), TaskoRun.STATUS_FAILED)) {
            email += " failed.\n\n" + "For more information check taskomatic daemon logs.";
        }
        else {
            email += " finished successfully and is back to normal.";
        }
        log.info("Sending e-mail ... {}", task.getName());
        TaskHelper.sendTaskoEmail(taskRun.getOrgId(), email);
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
