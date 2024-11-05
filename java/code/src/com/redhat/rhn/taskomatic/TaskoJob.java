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
import org.quartz.SchedulerException;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;


/**
 *
 * TaskoJob
 */
public class TaskoJob implements Job {

    private static final Logger LOG = LogManager.getLogger(TaskoJob.class);
    private static final Map<String, Integer> TASKS = new ConcurrentHashMap<>();
    private static final Map<String, Object> LAST_STATUS = new ConcurrentHashMap<>();

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss")
            .withZone(ZoneId.systemDefault());


    private final long scheduleId;

    private final long bunchStartIndex;

    static {
        for (TaskoTask task : TaskoFactory.listTasks()) {
            TASKS.put(task.getName(), 0);
            LAST_STATUS.put(task.getName(), TaskoRun.STATUS_FINISHED);
        }
        HibernateFactory.closeSession();
    }

    /**
     * default constructor
     * job is always associated with a schedule
     * @param scheduleIdIn schedule id
     * @param bunchStartIndexIn the index of the task within the bunch from where the execution should start
     */
    public TaskoJob(long scheduleIdIn, long bunchStartIndexIn) {
        this.scheduleId = scheduleIdIn;
        this.bunchStartIndex = bunchStartIndexIn;
    }

    private boolean isTaskSingleThreaded(TaskoTask task) {
        try {
            return Class.forName(task.getTaskClass()).getDeclaredConstructor().newInstance() instanceof RhnQueueJob;
        }
        catch (ReflectiveOperationException e) {
            // will be caught later
            LOG.error("Error trying to instance a new class of {}: {}", task.getTaskClass(), e.getMessage(), e);
            return false;
        }
    }

    private boolean isTaskRunning(TaskoTask task) {
        return TASKS.get(task.getName()) > 0;
    }

    private boolean isTaskThreadAvailable(RhnJob job, TaskoTask task) {
        return TASKS.get(task.getName()) < job.getParallelThreads();
    }

    private static synchronized void markTaskRunning(TaskoTask task) {
        int count = TASKS.get(task.getName());
        count++;
        TASKS.put(task.getName(), count);
    }

    private static synchronized void unmarkTaskRunning(TaskoTask task) {
        int count = TASKS.get(task.getName());
        count--;
        TASKS.put(task.getName(), count);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(JobExecutionContext context) {
        TaskoSchedule schedule = TaskoFactory.lookupScheduleById(scheduleId);
        if (schedule == null) {
            // means, that schedule was deleted (in the DB), but quartz still schedules it
            LOG.error("No such schedule with id  {}", scheduleId);
            TaskoQuartzHelper.unscheduleTrigger(context.getTrigger());
            return;
        }

        Instant start = Instant.now();
        LOG.info("{}: bunch {} STARTED", schedule.getJobLabel(), schedule.getBunch().getName());

        // Get from the bunch the task templated we need to process
        List<TaskoTemplate> bunchTemplates = schedule.getBunch()
                                                     .getTemplates()
                                                     .stream()
                                                     .sorted(Comparator.comparing(TaskoTemplate::getOrdering))
                                                     .filter(template -> template.getOrdering() >= bunchStartIndex)
                                                     .toList();

        String previousRunStatus = null;
        for (TaskoTemplate template : bunchTemplates) {
            // If it's not the first run and the template requires a previous state, check for it
            if (previousRunStatus != null &&
                    template.getStartIf() != null && !template.getStartIf().equals(previousRunStatus)) {
                LOG.info("Interrupting {} ({})", schedule.getBunch().getName(), schedule.getJobLabel());
                break;
            }

            TaskoRun runResult = runTemplate(schedule, template, context);
            if (runResult == null) {
                // Null result means it was not possible to execute the template. Interrupt the bunch execution
                LOG.info("Interrupting {} ({})", schedule.getBunch().getName(), schedule.getJobLabel());
                break;
            }

            previousRunStatus = runResult.getStatus();
        }
        HibernateFactory.closeSession();
        LOG.info("{}: bunch {} FINISHED", schedule.getJobLabel(), schedule.getBunch().getName());
        if (LOG.isDebugEnabled()) {
            LOG.debug("{}: bunch {} START: {} END: {}", schedule.getJobLabel(), schedule.getBunch().getName(),
                    TIMESTAMP_FORMAT.format(start), TIMESTAMP_FORMAT.format(Instant.now()));
        }
    }

    private boolean isTaskRunning(TaskoSchedule schedule, TaskoTask task) {
        if (isTaskSingleThreaded(task) && isTaskRunning(task)) {
            LOG.debug("{}: task {} already running ... LEAVING", schedule.getJobLabel(), task.getName());
            return true;
        }
        return false;
    }

    private TaskoRun runTemplate(TaskoSchedule schedule, TaskoTemplate template, JobExecutionContext context) {
        TaskoTask task = template.getTask();
        if (isTaskRunning(schedule, task)) {
            return null;
        }

        try {
            RhnJob job = createJob(task.getTaskClass());
            if (!isTaskThreadAvailable(job, task)) {
                rescheduleBunchFromTask(schedule, template, job.getRescheduleTime());
                return null;
            }

            markTaskRunning(task);

            try {
                LOG.debug("{}: task {} started", schedule.getJobLabel(), task.getName());
                TaskoRun taskRun = new TaskoRun(schedule.getOrgId(), template, scheduleId);
                TaskoFactory.save(taskRun);
                HibernateFactory.commitTransaction();
                HibernateFactory.closeSession();
                LOG.debug("Tasko run for {} created. Running job...", schedule.getJobLabel());

                doExecute(job, context, taskRun);

                // rollback everything, what the application changed and didn't committed
                if (HibernateFactory.getSession().getTransaction().isActive()) {
                    HibernateFactory.rollbackTransaction();
                    HibernateFactory.closeSession();
                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug("{} ({}) ... {}", task.getName(), schedule.getJobLabel(),
                            taskRun.getStatus().toLowerCase());
                }

                if (List.of(TaskoRun.STATUS_FINISHED, TaskoRun.STATUS_FAILED).contains(taskRun.getStatus()) &&
                        !Objects.equals(taskRun.getStatus(), LAST_STATUS.get(task.getName()))) {
                    sendStatusMail(schedule, taskRun, task);
                    LAST_STATUS.put(task.getName(), taskRun.getStatus());
                }

                return taskRun;
            }
            finally {
                unmarkTaskRunning(task);
            }
        }
        catch (Exception e) {
            LOG.error("Unable to run task", e);
            return null;
        }
    }

    private static void rescheduleBunchFromTask(TaskoSchedule schedule, TaskoTemplate template, int rescheduleSeconds)
        throws SchedulerException {
        LOG.info("Cannot find a thread for {} of {}. Rescheduling starting from {} in {} seconds",
            template.getTask().getName(), schedule.getJobLabel(), template.getOrdering(), rescheduleSeconds);

        Instant newScheduledTime = ZonedDateTime.now().plusSeconds(rescheduleSeconds).toInstant();
        TaskoQuartzHelper.rescheduleJob(schedule, newScheduledTime, template.getOrdering());
    }

    private static RhnJob createJob(String jobClassName) throws ReflectiveOperationException {
        Class<? extends RhnJob> jobClass = Class.forName(jobClassName).asSubclass(RhnJob.class);
        return jobClass.getDeclaredConstructor().newInstance();
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
        LOG.info("Sending e-mail ... {}", task.getName());
        TaskHelper.sendTaskoEmail(taskRun.getOrgId(), email);
    }
}
