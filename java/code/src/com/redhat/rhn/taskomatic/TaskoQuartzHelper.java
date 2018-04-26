/**
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
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;

import com.redhat.rhn.taskomatic.core.SchedulerKernel;
import com.redhat.rhn.taskomatic.domain.TaskoSchedule;

import org.apache.log4j.Logger;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.quartz.Trigger;

import java.util.Date;


/**
 * TaskoQuartzHelper
 * @version $Rev$
 */
public class TaskoQuartzHelper {

    private static Logger log = Logger.getLogger(TaskoQuartzHelper.class);

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
            log.warn("Removing trigger " + trigger.getKey().getGroup() + "." +
                    trigger.getKey().getName());
            SchedulerKernel.getScheduler().unscheduleJob(
                    triggerKey(trigger.getKey().getName(), trigger.getKey().getGroup()));
        }
        catch (SchedulerException e) {
            // be silent
        }
    }

    /**
     * creates a quartz job according to the schedule
     * @param schedule schedule as a job template
     * @return date of first schedule
     * @throws InvalidParamException thrown in case of invalid cron expression
     */
    public static Date createJob(TaskoSchedule schedule) throws InvalidParamException {
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
                        .forJob(schedule.getJobLabel())
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
        try {
            Date date =
                    SchedulerKernel.getScheduler().scheduleJob(jobDetail.build(), trigger);
            log.info("Job " + schedule.getJobLabel() + " scheduled succesfully.");
            return date;
        }
        catch (SchedulerException e) {
            log.warn("Job " + schedule.getJobLabel() + " failed to schedule.");
            return null;
        }
    }

    public static Date rescheduleJob(String jobLabel, Integer orgId, Date activeFrom, Date activeTill) {
        Trigger newTrigger = newTrigger()
                .withIdentity(jobLabel, getGroupName(orgId))
                .startAt(activeFrom)
                .endAt(activeTill)
                .forJob(jobLabel, getGroupName(orgId))
                .build();
        try {
            Date date =
                    SchedulerKernel.getScheduler().rescheduleJob(
                            triggerKey(jobLabel, getGroupName(orgId)),
                            newTrigger);
            return date;
        } catch (SchedulerException e) {
            log.warn("Failed to reschedule job " + jobLabel, e);
            return null;
        }
    }

    /**
     * unschedules job
     * @param orgId organization id
     * @param jobLabel job name
     * @return 1 if successful
     */
    public static Integer destroyJob(Integer orgId, String jobLabel) {
        try {
            SchedulerKernel.getScheduler()
                    .unscheduleJob(triggerKey(jobLabel, getGroupName(orgId)));
            log.info("Job " + jobLabel + " unscheduled succesfully.");
            return 1;
        }
        catch (SchedulerException e) {
            return null;
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
}
