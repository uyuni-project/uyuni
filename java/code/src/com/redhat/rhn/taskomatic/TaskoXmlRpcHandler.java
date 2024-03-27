/*
 * Copyright (c) 2010--2012 Red Hat, Inc.
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

import static org.quartz.TriggerKey.triggerKey;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.notification.NotificationMessage;
import com.redhat.rhn.domain.notification.UserNotificationFactory;
import com.redhat.rhn.domain.notification.types.CreateBootstrapRepoFailed;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.taskomatic.core.SchedulerKernel;
import com.redhat.rhn.taskomatic.domain.TaskoBunch;
import com.redhat.rhn.taskomatic.domain.TaskoRun;
import com.redhat.rhn.taskomatic.domain.TaskoSchedule;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.matchers.GroupMatcher;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * TaskoXmlRpcHandler
 */
public class TaskoXmlRpcHandler {

    private static Logger log = LogManager.getLogger(TaskoXmlRpcHandler.class);

    /**
     * dummy call
     * @param orgId organization id
     * @return 1
     */
    public int one(Integer orgId) {
        return 1;
    }

    /**
     * lists all available organizational bunches
     * @param orgId organization id
     * @return list of bunches
     */
    public List<TaskoBunch> listBunches(Integer orgId) {
        return TaskoFactory.listOrgBunches();
    }

    /**
     * lists all available satellite bunches
     * @return list of bunches
     */
    public List<TaskoBunch> listSatBunches() {
        return TaskoFactory.listSatBunches();
    }

    /**
     * lookup schedule by id
     * @param scheduleId schedule id
     * @return schedule
     */
    public TaskoSchedule lookupScheduleById(Integer scheduleId) {
        return TaskoFactory.lookupScheduleById(scheduleId.longValue());
    }

    /**
     * lookup schedule by label
     *
     * @param jobLabel schedule label
     * @return schedule
     */
    public List<TaskoSchedule> listScheduleByLabel(String jobLabel) {
        return TaskoFactory.listScheduleByLabel(jobLabel);
    }

    /**
     * lookup bunch by label
     * @param bunchName bunch label
     * @return bunch
     */
    public TaskoBunch lookupBunchByName(String bunchName) {
        return TaskoFactory.lookupBunchByName(bunchName);
    }

    /**
     * start scheduling a organizational bunch
     * @param orgId organization id
     * @param bunchName bunch name
     * @param jobLabel job name
     * @param startTime schedule from
     * @param endTime schedule till
     * @param cronExpression cron expression
     * @param params job parameters
     * @return date of the first schedule
     * @throws NoSuchBunchTaskException thrown if bunch name not known
     * @throws InvalidParamException thrown if job name already in use,
     * invalid cron expression, ...
     */
    public Date scheduleBunch(Integer orgId, String bunchName, String jobLabel,
            Date startTime, Date endTime, String cronExpression, Map params)
            throws NoSuchBunchTaskException, InvalidParamException, SchedulerException {

        TaskoBunch bunch = doBasicCheck(orgId, bunchName, jobLabel);
        if (!TaskoQuartzHelper.isValidCronExpression(cronExpression)) {
            throw new InvalidParamException("Cron trigger: " + cronExpression);
        }
        // create schedule
        TaskoSchedule schedule = new TaskoSchedule(orgId, bunch, jobLabel, params, startTime, endTime, cronExpression);
        TaskoFactory.save(schedule);
        HibernateFactory.commitTransaction();
        // create job
        try {
            return TaskoQuartzHelper.createJob(schedule);
        }
        catch (SchedulerException | InvalidParamException e) {
            log.error("Unable to create job {}", schedule.getJobLabel(), e);
            TaskoFactory.delete(schedule);
            HibernateFactory.commitTransaction();
            throw e;
        }
    }

    /**
     * start scheduling a satellite bunch
     * @param bunchName bunch name
     * @param jobLabel job name
     * @param startTime schedule from
     * @param endTime scchedule till
     * @param cronExpression crom expression
     * @param params job parameters
     * @return date of the first schedule
     * @throws NoSuchBunchTaskException thrown if bunch name not known
     * @throws InvalidParamException thrown if job name already in use,
     * invalid cron expression, ...
     */
    public Date scheduleSatBunch(String bunchName, String jobLabel,
            Date startTime, Date endTime, String cronExpression, Map params)
            throws NoSuchBunchTaskException, InvalidParamException, SchedulerException {

        return scheduleBunch(null, bunchName, jobLabel, startTime, endTime, cronExpression, params);
    }

    /**
     * start scheduling a organizational bunch
     * @param orgId organization id
     * @param bunchName bunch name
     * @param jobLabel job name
     * @param cronExpression crom expression
     * @param params job parameters
     * @return date of the first schedule
     * @throws NoSuchBunchTaskException thrown if bunch name not known
     * @throws InvalidParamException thrown if job name already in use,
     * invalid cron expression, ...
     */
    public Date scheduleBunch(Integer orgId, String bunchName, String jobLabel,
            String cronExpression, Map params)
            throws NoSuchBunchTaskException, InvalidParamException, SchedulerException {

        return scheduleBunch(orgId, bunchName, jobLabel, new Date(), null, cronExpression, params);
    }

    /**
     * start scheduling a satellite bunch
     * @param bunchName bunch name
     * @param jobLabel job name
     * @param cronExpression crom expression
     * @param params job paramters
     * @return date of the first schedule
     * @throws NoSuchBunchTaskException thrown if bunch name not known
     * @throws InvalidParamException thrown if job name already in use,
     * invalid cron expression, ...
     */
    public Date scheduleSatBunch(String bunchName, String jobLabel,
            String cronExpression, Map params)
            throws NoSuchBunchTaskException, InvalidParamException, SchedulerException {

        return scheduleBunch(null, bunchName, jobLabel, cronExpression, params);
    }

    private TaskoBunch doBasicCheck(Integer orgId, String bunchName, String jobLabel)
            throws NoSuchBunchTaskException, InvalidParamException, SchedulerException {

        TaskoBunch bunch = checkBunchName(orgId, bunchName);
        isAlreadyScheduled(orgId, jobLabel);
        return bunch;
    }

    /**
     * stop scheduling an organizational bunch
     * @param orgId organization id
     * @param jobLabel job name
     * @return 1 on success
     */
    public Integer unscheduleBunch(Integer orgId, String jobLabel) {
        // one or none shall be returned
        List<TaskoSchedule> scheduleList = TaskoFactory.listActiveSchedulesByOrgAndLabel(orgId, jobLabel);
        TriggerKey triggerKey;
        Trigger trigger;
        try {
            triggerKey = triggerKey(jobLabel, TaskoQuartzHelper.getGroupName(orgId));
            trigger = SchedulerKernel.getScheduler().getTrigger(triggerKey);

            // Try to find retry triggers as fallback
            if (trigger == null) {
                triggerKey = SchedulerKernel.getScheduler()
                    .getTriggerKeys(GroupMatcher.anyGroup()).stream()
                    .filter(it -> it.getName().startsWith(jobLabel + "-retry"))
                    .findFirst().orElse(null);
                trigger = SchedulerKernel.getScheduler().getTrigger(triggerKey);
            }
        }
        catch (SchedulerException e) {
            trigger = null;
            triggerKey = null;
        }
        // check for inconsistencies
        // quartz unschedules job after trigger end time
        // so better handle quartz and schedules separately
        if ((scheduleList.isEmpty()) && (trigger == null)) {
            log.error("Unscheduling of bunch {} failed: no such job label", jobLabel);
            return 0;
        }
        for (TaskoSchedule schedule : scheduleList) {
            schedule.unschedule();
        }
        if (trigger != null) {
            TaskoQuartzHelper.destroyJob(triggerKey);
        }
        return 1;
    }

    /**
     * stop scheduling a satellite bunch
     * @param jobLabels job names
     * @return 1 if successful
     */
    public Integer unscheduleSatBunches(List<String> jobLabels) {
        for (String jobLabel: jobLabels) {
            unscheduleBunch(null, jobLabel);
        }
        return 1;
    }

    /**
     * schedule a one time satellite bunch
     * @param bunchName bunch name
     * @param jobLabel job label
     * @param params job parameters
     * @param start schedule time
     * @return date of the schedule
     * @throws NoSuchBunchTaskException thrown if bunch name not known
     * @throws InvalidParamException shall not be thrown
     */
    public Date scheduleSingleSatBunchRun(String bunchName, String jobLabel, Map<?, ?> params, Date start)
            throws NoSuchBunchTaskException, InvalidParamException, SchedulerException {

        return scheduleSingleBunchRun(null, bunchName, jobLabel, params, start);
    }

    /**
     * schedule a list of jobs with the same bunch name
     * @param bunchName bunch name
     * @param jobLabel job label
     * @param params List of job parameters
     * @return List of scheduled dates
     * @throws NoSuchBunchTaskException thrown if bunch name not known
     * @throws InvalidParamException shall not be thrown
     */
    public List<Date> scheduleRuns(String bunchName, String jobLabel,  List<Map<?, ?>> params)
            throws NoSuchBunchTaskException, InvalidParamException {

        return scheduleRuns(null, bunchName, jobLabel, params);
    }

    /**
     * schedule a one time organizational bunch
     * @param orgId organization id
     * @param bunchName bunch name
     * @param params job parameters
     * @param start schedule time
     * @return date of the schedule
     * @throws NoSuchBunchTaskException thrown if bunch name not known
     * @throws InvalidParamException shall not be thrown
     */
    public Date scheduleSingleBunchRun(Integer orgId, String bunchName, Map params, Date start)
            throws NoSuchBunchTaskException, InvalidParamException, SchedulerException {

        String jobLabel = getUniqueSingleJobLabel(orgId, bunchName);
        return scheduleSingleBunchRun(orgId, bunchName, jobLabel, params, start);
    }

    /**
     * schedule a one time organizational bunch
     * @param orgId organization id
     * @param bunchName bunch name
     * @param jobLabel job label
     * @param params job parameters
     * @param start schedule time
     * @return date of the schedule
     * @throws NoSuchBunchTaskException thrown if bunch name not known
     * @throws InvalidParamException shall not be thrown
     */
    public Date scheduleSingleBunchRun(Integer orgId, String bunchName, String jobLabel, Map params, Date start)
            throws NoSuchBunchTaskException, InvalidParamException, SchedulerException {

        TaskoBunch bunch = doBasicCheck(orgId, bunchName, jobLabel);
        List<TaskoSchedule> taskoSchedules = TaskoFactory.listScheduleByLabel(jobLabel);

        TaskoSchedule schedule;
        if (taskoSchedules.isEmpty()) {
            // create schedule
            schedule = new TaskoSchedule(orgId, bunch, jobLabel, params, start, null, null);
        }
        else {
            // update existing schedule
            schedule = taskoSchedules.get(0);
            schedule.setBunch(bunch);
            schedule.setDataMap(params);
            schedule.setActiveFrom(start);
            schedule.setActiveTill(start);
        }
        TaskoFactory.save(schedule);
        HibernateFactory.commitTransaction();
        log.info("Schedule created for {}. Creating quartz Job...", jobLabel);

        // create job
        try {
            return TaskoQuartzHelper.createJob(schedule);
        }
        catch (SchedulerException | InvalidParamException e) {
            log.error("Unable to create job {}", schedule.getJobLabel(), e);
            TaskoFactory.delete(schedule);
            HibernateFactory.commitTransaction();
            log.debug("Schedule removed.");
            throw e;
        }
    }

    /**
     * schedule a list of jobs with the same bunch name
     * @param orgId organization id
     * @param bunchName bunch name
     * @param jobLabel job label
     * @param paramsList job parameters

     * @return List of scheduled dates
     * @throws NoSuchBunchTaskException thrown if bunch name not known
     * @throws InvalidParamException shall not be thrown
     */
     public List<Date> scheduleRuns(Integer orgId, String bunchName, String jobLabel, List<Map<?, ?>> paramsList)
             throws NoSuchBunchTaskException, InvalidParamException {

        List<Date> scheduleDates = new ArrayList<>();
        TaskoBunch bunch = checkBunchName(orgId, bunchName);
        for (Map params : paramsList) {
           String label = getJobLabel(params, jobLabel);

            try {
                isAlreadyScheduled(orgId, label);
            }
            catch (SchedulerException | InvalidParamException e) {
                log.warn("Already scheduled {}: {}", label, e.getMessage(), e);
                continue;
            }
            // create schedule
            String earliestAction = String.valueOf(params.get("earliest_action"));
            params.remove("earliest_action"); // don't need it anymore.
            Date start = Date.from(ZonedDateTime.parse(earliestAction)
                    .withZoneSameInstant(ZoneId.systemDefault()).toInstant());
            TaskoSchedule schedule = new TaskoSchedule(orgId, bunch, label, params, start, null, null);
            TaskoFactory.save(schedule);
            HibernateFactory.commitTransaction();

            // create job
            Date scheduleDate = null;
            try {
                scheduleDate = TaskoQuartzHelper.createJob(schedule);
            }
            catch (InvalidParamException | SchedulerException e) {
                log.error("Unable to create job {}", schedule.getJobLabel(), e);
                TaskoFactory.delete(schedule);
            }
            if (scheduleDate != null) {
                scheduleDates.add(scheduleDate);
            }
        }
        HibernateFactory.commitTransaction();
        return scheduleDates;
    }

    /**
     * schedule a one time satellite bunch
     * @param bunchName bunch name
     * @param params job parameters
     * @param start schedule time
     * @return date of the schedule
     * @throws NoSuchBunchTaskException thrown if bunch name not known
     * @throws InvalidParamException shall not be thrown
     */
    public Date scheduleSingleSatBunchRun(String bunchName, Map params, Date start)
            throws NoSuchBunchTaskException, InvalidParamException, SchedulerException {

        return scheduleSingleBunchRun(null, bunchName, params, start);
    }

    /**
     * schedule a one time organizational bunch asap
     * @param orgId organization id
     * @param bunchName bunch name
     * @param params job parameters
     * @return date of the schedule
     * @throws NoSuchBunchTaskException thrown if bunch name not known
     * @throws InvalidParamException shall not be thrown
     */
    public Date scheduleSingleBunchRun(Integer orgId, String bunchName, Map params)
            throws NoSuchBunchTaskException, InvalidParamException, SchedulerException {

        return scheduleSingleBunchRun(orgId, bunchName, params, new Date());
    }

    /**
     * schedule a one time satellite bunch asap
     * @param bunchName bunch name
     * @param params job parameters
     * @return date of the schedule
     * @throws NoSuchBunchTaskException thrown if bunch name not known
     * @throws InvalidParamException shall not be thrown
     */
    public Date scheduleSingleSatBunchRun(String bunchName, Map params)
            throws NoSuchBunchTaskException, InvalidParamException, SchedulerException {

        return scheduleSingleBunchRun(null, bunchName, params, new Date());
    }

    private String getUniqueSingleJobLabel(Integer orgId, String bunchName) throws SchedulerException {
        String jobLabel = "single-" + bunchName + "-";
        int count = 0;
        while (!TaskoFactory.listSchedulesByOrgAndLabel(orgId, jobLabel + count)
                .isEmpty() ||
                (SchedulerKernel.getScheduler()
                        .getTrigger(triggerKey(jobLabel + count,
                                TaskoQuartzHelper.getGroupName(orgId))) != null)) {
            count++;
        }
        return jobLabel + count;
    }

    private TaskoBunch checkBunchName(Integer orgId, String bunchName) throws NoSuchBunchTaskException {
        TaskoBunch bunch = null;
        if (orgId == null) {
            bunch = TaskoFactory.lookupSatBunchByName(bunchName);
        }
        else {
            bunch = TaskoFactory.lookupOrgBunchByName(bunchName);
        }
        if (bunch == null) {
            throw new NoSuchBunchTaskException(bunchName);
        }
        return bunch;
    }

    /**
     * lists all organizational schedules
     * @param orgId organization id
     * @return list of schedules
     */
    public List<TaskoSchedule> listAllSchedules(Integer orgId) {
        return TaskoFactory.listSchedulesByOrg(orgId);
    }

    /**
     * lists all satellite schedules
     * @return list of schedules
     */
    public List<TaskoSchedule> listAllSatSchedules() {
        return listAllSchedules(null);
    }

    /**
     * lists all active organizational schedules
     * @param orgId organizational id
     * @return list of schedules
     */
    public List<TaskoSchedule> listActiveSchedules(Integer orgId) {
        return TaskoFactory.listActiveSchedulesByOrg(orgId);
    }

    /**
     * lists all active satellite schedules
     * @return list of schedules
     */
    public List<TaskoSchedule> listActiveSatSchedules() {
        return listActiveSchedules(null);
    }

    /**
     * lists all active organizational schedules associated with given bunch
     * @param orgId organizational id
     * @param bunchName taskomatic bunch name
     * @return list of schedules
     * @throws NoSuchBunchTaskException in case of unknown org bunch name
     */
    public List<TaskoSchedule> listActiveSchedulesByBunch(Integer orgId, String bunchName)
            throws NoSuchBunchTaskException {
        return TaskoFactory.listActiveSchedulesByOrgAndBunch(orgId, bunchName);
    }

    /**
     * lists all active satellite schedules associated with given bunch
     * @param bunchName taskomatic bunch name
     * @return list of schedules
     * @throws NoSuchBunchTaskException in case of unknown sat bunch name
     */
    public List<TaskoSchedule> listActiveSatSchedulesByBunch(String bunchName) throws NoSuchBunchTaskException {

        return TaskoFactory.listActiveSchedulesByOrgAndBunch(null, bunchName);
    }

    /**
     * lists all organizational runs of a give schedule
     * @param orgId organizational id
     * @param scheduleId schedule id
     * @return list of runs
     */
    public List<TaskoRun> listScheduleRuns(Integer orgId, Integer scheduleId) {
        return TaskoFactory.listRunsByOrgAndSchedule(orgId, scheduleId);
    }

    /**
     * lists all satellite runs of a give schedule
     * @param scheduleId schedule id
     * @return list of runs
     */
    public List<TaskoRun> listScheduleSatRuns(Integer scheduleId) {
        return listScheduleRuns(null, scheduleId);
    }

    /**
     * lists all satellite runs of a give bunch
     * @param bunchName bunch name
     * @return list of runs
     */
    public List<TaskoRun> listBunchSatRuns(String bunchName) {
        return TaskoFactory.listRunsByBunch(bunchName);
    }

    /**
     * reinitialize all schedules
     * meant to be called, when taskomatic has to be reinitialized
     * (f.e. because of time shift)
     * @return list of successfully reinitialized schedules
     */
    public List<TaskoSchedule> reinitializeAllSchedulesFromNow() {
        List<TaskoSchedule> schedules = new ArrayList<>();
        Date now = new Date();
        for (TaskoSchedule schedule : TaskoFactory.listFuture()) {
            try {
                TaskoSchedule reinited = TaskoFactory.reinitializeScheduleFromNow(schedule, now);
                schedules.add(reinited);
            }
            catch (InvalidParamException | SchedulerException e) {
                log.error("Unable to reinitialize schedule for job {}", schedule.getJobLabel(), e);
            }
        }
        return schedules;
    }

    /**
     * Get the job label by constructing using partial job label and some other parameters
     * @param paramsMap maps containing data about actionn
     * @param partialJobLabel partial job label
     */
    private String getJobLabel(Map<String, String> paramsMap, String partialJobLabel) {
        StringBuilder label = new StringBuilder(partialJobLabel).append(paramsMap.get("action_id"));
        if (paramsMap.containsKey("staging_job")) {
            label = label.append("-").append(paramsMap.get("staging_job_minion_server_id"));
        }
        return label.toString();
    }

    /**
     * Check if job with the given label is already scheduled
     * @param orgId organization Id
     * @param jobLabel job label
     * @throws SchedulerException
     * @throws InvalidParamException
     */
    private void isAlreadyScheduled(Integer orgId, String jobLabel) throws SchedulerException, InvalidParamException {

        if (!TaskoFactory.listActiveSchedulesByOrgAndLabel(orgId, jobLabel).isEmpty() ||
                (SchedulerKernel.getScheduler().getTrigger(triggerKey(jobLabel,
                        TaskoQuartzHelper.getGroupName(orgId))) != null)) {
            throw new InvalidParamException("jobLabel already in use");
        }
    }

    /**
     * Check if JMX is enabled.
     * @return true if JavaAgent class can be found
     */
    public static boolean isJmxEnabled() {
        try {
            Class.forName("io.prometheus.jmx.shaded.io.prometheus.jmx.JavaAgent");
        }
        catch (ClassNotFoundException ex) {
            return false;
        }
        return true;
    }

    /**
     * Create a BootstrapRepoFailed Notification and assign it to SUSE Manager administrators
     * @param identifier bootstrap repo identifier label
     * @param details error details
     * @return 1 when finished
     */
    public int createBootstrapRepoFailedNotification(String identifier, String details) {
        NotificationMessage notificationMessage = UserNotificationFactory.createNotificationMessage(
                new CreateBootstrapRepoFailed(identifier, details));
        UserNotificationFactory.storeNotificationMessageFor(notificationMessage,
                Collections.singleton(RoleFactory.SAT_ADMIN), Optional.empty());
        return 1;
    }
}
