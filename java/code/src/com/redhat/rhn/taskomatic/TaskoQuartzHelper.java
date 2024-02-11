/*
 * Copyright (c) 2010--2011 Red Hat, Inc.
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

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.frontend.events.TransactionHelper;
import com.redhat.rhn.taskomatic.core.SchedulerKernel;
import com.redhat.rhn.taskomatic.domain.TaskoSchedule;

import org.apache.commons.collections.MapUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DateBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.jdbcjobstore.StdJDBCConstants;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;


/**
 * TaskoQuartzHelper
 */
public class TaskoQuartzHelper {

    private static final String QRTZ_PREFIX = Config.get()
            .getString("org.quartz.jobStore.tablePrefix", StdJDBCConstants.DEFAULT_TABLE_PREFIX);
    private static final String QRTZ_TRIGGERS = QRTZ_PREFIX.concat("TRIGGERS");
    private static final String QRTZ_SIMPLE_TRIGGERS = QRTZ_PREFIX.concat("SIMPLE_TRIGGERS");
    private static final String QRTZ_CRON_TRIGGERS = QRTZ_PREFIX.concat("CRON_TRIGGERS");
    private static final String QRTZ_SIMPROP_TRIGGERS = QRTZ_PREFIX.concat("SIMPROP_TRIGGERS");
    private static final String QRTZ_BLOB_TRIGGERS = QRTZ_PREFIX.concat("BLOB_TRIGGERS");

    private static Logger log = LogManager.getLogger(TaskoQuartzHelper.class);

    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            .withZone(ZoneId.systemDefault());

    /**
     * cann't construct
     */
    private TaskoQuartzHelper() {
    }
    /**
     * unschedule quartz trigger
     * just for sanity purposes
     * @param trigger trigger to unschedule
     */
    public static void unscheduleTrigger(Trigger trigger) {
        try {
            log.warn("Removing trigger {}.{}", trigger.getKey().getGroup(), trigger.getKey().getName());
            SchedulerKernel.getScheduler().unscheduleJob(
                    triggerKey(trigger.getKey().getName(), trigger.getKey().getGroup()));
        }
        catch (SchedulerException e) {
            log.error("Unable to remove scheduled trigger {}", trigger.getJobKey(), e);
        }
    }

    /**
     * creates a quartz job according to the schedule
     * @param schedule schedule as a job template
     * @return date of first schedule
     * @throws InvalidParamException thrown in case of invalid cron expression
     * @throws SchedulerException
     */
    public static Date createJob(TaskoSchedule schedule) throws InvalidParamException, SchedulerException {
        // create trigger
        Trigger trigger = null;
        if (isCronExpressionEmpty(schedule.getCronExpr())) {
            trigger = newTrigger()
                    .withIdentity(schedule.getJobLabel(), getGroupName(schedule.getOrgId()))
                    .startAt(schedule.getActiveFrom())
                    .endAt(schedule.getActiveTill())
                    .forJob(schedule.getJobLabel(), getGroupName(schedule.getOrgId()))
                    .build();
        }
        else {
            try {
                trigger = newTrigger()
                        .withIdentity(schedule.getJobLabel(),
                                getGroupName(schedule.getOrgId()))
                        .withSchedule(cronSchedule(schedule.getCronExpr()))
                        .startAt(schedule.getActiveFrom())
                        .endAt(schedule.getActiveTill())
                        .forJob(schedule.getJobLabel(), getGroupName(schedule.getOrgId()))
                        .build();
            }
            catch (Exception e) {
                throw new InvalidParamException("Invalid cron expression " +
                        schedule.getCronExpr());
            }

        }
        // create job
        JobBuilder jobDetail = newJob(TaskoJob.class)
                .withIdentity(schedule.getJobLabel(),
                getGroupName(schedule.getOrgId()));
        // set job params
        if (schedule.getDataMap() != null) {
            jobDetail.usingJobData(new JobDataMap(schedule.getDataMap()));
        }
        jobDetail.usingJobData("schedule_id", schedule.getId());

        // schedule job
        Date date = SchedulerKernel.getScheduler().scheduleJob(jobDetail.build(), trigger);
        log.info("Job {} scheduled successfully.", schedule.getJobLabel());
        return date;
    }

    /**
     * Reschedule the job with the given schedule by creating a new trigger with the given
     * start date.
     * @param schedule for the job to be rescheduled
     * @param startAtDate trigger time
     * @param bunchStartIndex the index of the starting task in the bunch. Different from zero if it was not possible
     *                        to execute one of the task of the bunch due to missing capacity
     * @return the date of the trigger or null if scheduling was not successful
     * @throws SchedulerException
     */
    public static Date rescheduleJob(TaskoSchedule schedule, Instant startAtDate, long bunchStartIndex)
        throws SchedulerException {
        // create trigger
        String timestamp = TIMESTAMP_FORMAT.format(startAtDate);
        String quartzGroupName = getGroupName(schedule.getOrgId());
        TriggerKey retryTriggerKey = new TriggerKey(schedule.getJobLabel() + "-retry" + timestamp, quartzGroupName);

        Trigger retryTrigger = SchedulerKernel.getScheduler().getTrigger(retryTriggerKey);
        if (retryTrigger != null) {
            log.warn("Retry trigger {} already exists", retryTriggerKey);
            return retryTrigger.getStartTime();
        }

        Trigger trigger = newTrigger().withIdentity(retryTriggerKey)
            .startAt(Date.from(startAtDate))
            // execute job immediately after discovering a misfire situation
            .withSchedule(simpleSchedule().withMisfireHandlingInstructionFireNow())
            .forJob(retryTriggerKey.getName(), retryTriggerKey.getGroup())
            .build();

        // create job
        JobBuilder jobDetail = newJob().ofType(TaskoJob.class)
                                       .withIdentity(retryTriggerKey.getName(), retryTriggerKey.getGroup())
                                       .usingJobData("schedule_id", schedule.getId())
                                       .usingJobData("bunch_start_index", bunchStartIndex);

        // set job params
        if (MapUtils.isNotEmpty(schedule.getDataMap())) {
            jobDetail.usingJobData(new JobDataMap(schedule.getDataMap()));
        }

        // schedule job
        Date date = SchedulerKernel.getScheduler().scheduleJob(jobDetail.build(), trigger);
        log.info("Job {} rescheduled with trigger {}", schedule.getJobLabel(), trigger.getKey());
        return date;
    }

    /**
     * Reschedules a Job that is currently being executed based on the provided execution context.
     * This method is used to retry a Job after a specified interval.
     * @param context    the context of the current execution of the Job
     * @param retryCount the current number of retries for this Job
     * @param interval   the interval in seconds for triggering the Job again
     * @throws SchedulerException if there is an issue with the scheduler while rescheduling the Job
     */
    public static void rescheduleJob(JobExecutionContext context, int retryCount, int interval)
            throws SchedulerException {
        Trigger previousJobTrigger = context.getTrigger();

        JobDataMap newJobData = previousJobTrigger.getJobDataMap();
        newJobData.put("retryCount", retryCount + 1);
        String newName = retryTriggerName(previousJobTrigger.getKey().getName(), retryCount);

        Trigger newTrigger = TriggerBuilder
                .newTrigger()
                .withIdentity(newName, previousJobTrigger.getKey().getGroup())
                .forJob(previousJobTrigger.getJobKey())
                .usingJobData(newJobData)
                .startAt(DateBuilder.futureDate(interval, DateBuilder.IntervalUnit.SECOND))
                .build();
        context.getScheduler().scheduleJob(newTrigger);
    }

    /**
     * Generates a new trigger name based on the current trigger name and retry count.
     *
     * @param currentName the current trigger name
     * @param retryCount  the number of retries for this trigger
     * @return a new trigger name modified based on the retry count
     */
    public static String retryTriggerName(String currentName, int retryCount) {
        String baseTriggerName = currentName.split("-retry")[0];
        return baseTriggerName + "-retry-" + retryCount;
    }

    /**
     * unschedules job
     *
     * @param orgId    organization id
     * @param jobLabel job name
     */
    public static void destroyJob(Integer orgId, String jobLabel) {
        destroyJob(triggerKey(jobLabel, getGroupName(orgId)));
    }

    /**
     * unschedules job
     *
     * @param key trigger key
     */
    public static void destroyJob(TriggerKey key) {
        try {
            SchedulerKernel.getScheduler().unscheduleJob(key);
            log.info("Job {} unscheduled successfully.", key.getName());
        }
        catch (SchedulerException e) {
            log.error("Unable to unschedule job {} of organization # {}", key.getName(), key.getGroup(), e);
        }
    }

    /**
     * return quartz group name
     * @param orgId organizational id
     * @return group name
     */
    public static String getGroupName(Integer orgId) {
        if (orgId == null) {
            return null;
        }
        return orgId.toString();
    }

    private static boolean isCronExpressionEmpty(String cronExpr) {
        return (cronExpr == null || cronExpr.isEmpty());
    }

    /**
     * returns, whether cron expression is valid
     * @param cronExpression cron expression
     * @return true, if expression is valid
     */
    public static boolean isValidCronExpression(String cronExpression) {
        if (isCronExpressionEmpty(cronExpression)) {
            return true;
        }
        try {
            newTrigger().withSchedule(cronSchedule(cronExpression)).build();
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * This method is responsible for detecting and removing invalid triggers from the Quartz database.
     * In some cases, the Quartz database can become inconsistent, with records in the main QRTZ_TRIGGERS table lacking
     * corresponding entries in any of the possible detail property tables (QRTZ_CRON_TRIGGERS, QRTZ_SIMPLE_TRIGGERS,
     * QRTZ_BLOB_TRIGGERS or QRTZ_SIMPROP_TRIGGERS). See: bsc#1202519, bsc#1208635 and
     * https://github.com/uyuni-project/uyuni/issues/5556
     *
     * While the exact scenarios leading to this inconsistency may not be clear, this method provides a solution to
     * prevent Taskomatic from failing to start, performing a cleanup of invalid triggers before starting the scheduler
     */
    public static void cleanInvalidTriggers() {
        log.info("Checking quartz database consistency...");
        TransactionHelper.handlingTransaction(
            () -> {
                int del = HibernateFactory.getSession().createNativeQuery(cleanInvalidTriggersQuery()).executeUpdate();
                log.info("Removed {} invalid triggers", del);
            },
            e -> log.warn("Error removing invalid triggers.", e)
        );
    }

    private static String cleanInvalidTriggersQuery() {
        return "DELETE FROM " + QRTZ_TRIGGERS + " T " +
            "WHERE T.SCHED_NAME || T.TRIGGER_NAME || T.TRIGGER_GROUP NOT IN (" +
                "SELECT c.SCHED_NAME || c.TRIGGER_NAME || c.TRIGGER_GROUP FROM " + QRTZ_CRON_TRIGGERS + " c UNION " +
                "SELECT s.SCHED_NAME || s.TRIGGER_NAME || s.TRIGGER_GROUP FROM " + QRTZ_SIMPLE_TRIGGERS + " s UNION " +
                "SELECT b.SCHED_NAME || b.TRIGGER_NAME || b.TRIGGER_GROUP FROM " + QRTZ_BLOB_TRIGGERS + " b UNION " +
                "SELECT sp.SCHED_NAME || sp.TRIGGER_NAME || sp.TRIGGER_GROUP FROM " + QRTZ_SIMPROP_TRIGGERS + " sp" +
            ")";
   }
}
