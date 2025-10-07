/*
 * Copyright (c) 2010--2014 Red Hat, Inc.
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
import com.redhat.rhn.taskomatic.core.SchedulerKernel;
import com.redhat.rhn.taskomatic.domain.TaskoBunch;
import com.redhat.rhn.taskomatic.domain.TaskoRun;
import com.redhat.rhn.taskomatic.domain.TaskoSchedule;
import com.redhat.rhn.taskomatic.domain.TaskoTask;
import com.redhat.rhn.taskomatic.domain.TaskoTemplate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.query.Query;
import org.quartz.SchedulerException;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.NoResultException;

/**
 * TaskoFactory
 */
public class TaskoFactory extends HibernateFactory {
    private static TaskoFactory singleton = new TaskoFactory();
    private static Logger log = LogManager.getLogger(TaskoFactory.class);

    /**
     * default constructor
     */
    TaskoFactory() {
        super();
    }

    @Override
    protected Logger getLogger() {
        return log;
    }

    /**
     * lookup a organization bunch by name
     * @param bunchName bunch name
     * @return bunch
     */
    public static TaskoBunch lookupOrgBunchByName(String bunchName) {
        return singleton.lookupObjectByNamedQuery("TaskoBunch.lookupOrgBunchByName", Map.of("name", bunchName));
    }

    /**
     * lookup a satellite bunch by name
     * @param bunchName bunch name
     * @return bunch
     */
    public static TaskoBunch lookupSatBunchByName(String bunchName) {
        return singleton.lookupObjectByNamedQuery("TaskoBunch.lookupSatBunchByName", Map.of("name", bunchName));
    }

    /**
     * list all available organizational bunches
     * @return list of bunches
     */
    public static List<TaskoBunch> listOrgBunches() {
        return singleton.listObjectsByNamedQuery("TaskoBunch.listOrgBunches", Map.of());
    }

    /**
     * list all available satellite bunches
     * @return list of bunches
     */
    public static List<TaskoBunch> listSatBunches() {
        return singleton.listObjectsByNamedQuery("TaskoBunch.listSatBunches", Map.of());
    }

    /**
     * hibernate save run
     * @param taskoRun run to save
     */
    public static void save(TaskoRun taskoRun) {
        singleton.saveObject(taskoRun);
    }

    /**
     * hibernate delete run
     * @param taskoRun run to delete
     */
    public static void delete(TaskoRun taskoRun) {
        singleton.removeObject(taskoRun);
    }

    /**
     * hibernate delete schedule
     * @param taskoSchedule schedule to delete
     */
    public static void delete(TaskoSchedule taskoSchedule) {
        singleton.removeObject(taskoSchedule);
    }

    /**
     * hibernate save schedule
     * @param taskoSchedule schedule to save
     */
    public static void save(TaskoSchedule taskoSchedule) {
        singleton.saveObject(taskoSchedule);
    }

    /**
     * hibernate save template
     * @param taskoTemplate run to save
     */
    public static void save(TaskoTemplate taskoTemplate) {
        singleton.saveObject(taskoTemplate);
    }

    /**
     * hibernate delete template
     * @param taskoTemplate run to delete
     */
    public static void delete(TaskoTemplate taskoTemplate) {
        singleton.removeObject(taskoTemplate);
    }

    /**
     * hibernate save bunch
     * @param taskoBunch run to save
     */
    public static void save(TaskoBunch taskoBunch) {
        singleton.saveObject(taskoBunch);
    }

    /**
     * hibernate delete bunch
     * @param taskoBunch run to delete
     */
    public static void delete(TaskoBunch taskoBunch) {
        singleton.removeObject(taskoBunch);
    }

    /**
     * hibernate save task
     * @param taskoTask run to save
     */
    public static void save(TaskoTask taskoTask) {
        singleton.saveObject(taskoTask);
    }

    /**
     * hibernate delete task
     * @param taskoTask run to delete
     */
    public static void delete(TaskoTask taskoTask) {
        singleton.removeObject(taskoTask);
    }

    /**
     * lists all available tasks
     * @return list of tasks
     */
    public static List<TaskoTask> listTasks() {
        return getSession()
                .createQuery("from com.redhat.rhn.taskomatic.domain.TaskoTask as t", TaskoTask.class)
                .list();
    }

    /**
     * lists runs older than given date
     * @param limitTime date of interest
     * @return list of runs
     */
    public static List<TaskoRun> listRunsOlderThan(Date limitTime) {
        return getSession()
                .createQuery("FROM com.redhat.rhn.taskomatic.domain.TaskoRun WHERE endTime < :limit_time",
                        TaskoRun.class)
                .setParameter("limit_time", limitTime)
                .list();
    }

    /**
     * lists runs newer than given date
     * @param limitTime date of interest
     * @return list of runs
     */
    public static List<TaskoRun> listRunsNewerThan(Date limitTime) {
        return getSession()
                .createQuery("FROM com.redhat.rhn.taskomatic.domain.TaskoRun WHERE endTime >= :limit_time",
                        TaskoRun.class)
                .setParameter("limit_time", limitTime)
                .list();
    }

    /**
     * deletes specified tasko run
     * @param run run to delete
     */
    public static void deleteRun(TaskoRun run) {
        TaskoFactory.delete(run);
    }

    /**
     * lists active schedules for a given org
     * @param orgId organization id
     * @return list of active schedules
     */
    public static List<TaskoSchedule> listActiveSchedulesByOrg(Integer orgId) {
        List<TaskoSchedule> schedules;
        List<String> filter = List.of("recurring-action-executor-bunch");    // List of bunch names to be excluded

        if (orgId == null) {
            schedules = getSession()
                    .createQuery("""
                            FROM com.redhat.rhn.taskomatic.domain.TaskoSchedule
                            WHERE orgId IS NULL
                            AND activeFrom < :timestamp
                            AND (activeTill IS NULL OR :timestamp < activeTill)""", TaskoSchedule.class)
                    .setParameter("timestamp", new Date()) // use server time, not DB time
                    .list();
        }
        else {
            schedules = getSession()
                    .createQuery("""
                            FROM com.redhat.rhn.taskomatic.domain.TaskoSchedule
                            WHERE orgId = :org_id
                            AND (activeFrom < :timestamp
                                 AND (activeTill IS NULL OR :timestamp < activeTill))""", TaskoSchedule.class)
                    .setParameter("timestamp", new Date()) // use server time, not DB time
                    .setParameter("org_id", orgId)
                    .list();
        }

        // Remove schedules with bunch names in 'filter'
        schedules.removeIf(schedule -> filter.contains(schedule.getBunch().getName()));
        return  schedules;
    }

    /**
     * lists active schedules of given name for a given org
     * @param orgId organization id
     * @param jobLabel unique job name
     * @return list of active schedules
     */
    public static List<TaskoSchedule> listActiveSchedulesByOrgAndLabel(Integer orgId, String jobLabel) {
        if (orgId == null) {
            return getSession()
                    .createQuery("""
                        FROM com.redhat.rhn.taskomatic.domain.TaskoSchedule
                        WHERE orgId IS NULL
                        AND jobLabel = :job_label
                        AND activeFrom < :timestamp
                        AND (activeTill IS NULL OR :timestamp < activeTill)""", TaskoSchedule.class)
                    .setParameter("job_label", jobLabel)
                    .setParameter("timestamp", new Date())    // use server time, not DB time
                    .list();
        }
        return getSession()
                .createQuery("""
                        FROM com.redhat.rhn.taskomatic.domain.TaskoSchedule
                        WHERE orgId = :org_id
                        AND jobLabel = :job_label
                        AND (activeFrom < :timestamp
                             AND (activeTill IS NULL OR :timestamp < activeTill))""", TaskoSchedule.class)
                .setParameter("job_label", jobLabel)
                .setParameter("timestamp", new Date())    // use server time, not DB time
                .setParameter("org_id", orgId)
                .list();
    }

    /**
     * lists active schedule of the given bunch
     * @param orgId organization id
     * @param bunchName bunch name
     * @return list of schedules
     * @throws NoSuchBunchTaskException in case of unknown bunch name
     */
    public static List<TaskoSchedule> listActiveSchedulesByOrgAndBunch(Integer orgId, String bunchName)
            throws NoSuchBunchTaskException {
        TaskoBunch bunch = lookupBunchByOrgAndName(orgId, bunchName);
        if (orgId == null) {
            return getSession()
                    .createQuery("""
                            FROM com.redhat.rhn.taskomatic.domain.TaskoSchedule
                            WHERE orgId IS NULL
                            AND bunch_id = :bunch_id
                            AND activeFrom < :timestamp
                            AND (activeTill IS NULL OR :timestamp < activeTill)""", TaskoSchedule.class)
                    .setParameter("timestamp", new Date())   // use server time, not DB time
                    .setParameter("bunch_id", bunch.getId())
                    .list();
        }
        return getSession()
                .createQuery("""
                        FROM com.redhat.rhn.taskomatic.domain.TaskoSchedule
                        WHERE orgId = :org_id
                        AND bunch_id = :bunch_id
                        AND (activeFrom < :timestamp
                             AND (activeTill IS NULL
                              OR :timestamp < activeTill))""", TaskoSchedule.class)
                .setParameter("timestamp", new Date())   // use server time, not DB time
                .setParameter("bunch_id", bunch.getId())
                .setParameter("org_id", orgId)
                .list();
    }


    /**
     * list schedules, that shall be run sometime in the future
     * @return list of schedules to be run at least once
     */
    public static List<TaskoSchedule> listFuture() {
        return getSession()
                .createQuery("""
                        FROM com.redhat.rhn.taskomatic.domain.TaskoSchedule
                        WHERE activeTill IS NULL
                        OR :timestamp < activeTill""", TaskoSchedule.class)
                .setParameter("timestamp", new Date())
                .list();
    }

    /**
     * list all schedule runs with (future) timestamps newer than limitTime
     * @param scheduleId schedule id
     * @param limitTime limit time
     * @return list of runs
     */
    public static List<TaskoRun> listNewerRunsBySchedule(Long scheduleId, Date limitTime) {
        return getSession()
                .createQuery("""
                        FROM com.redhat.rhn.taskomatic.domain.TaskoRun
                        WHERE scheduleId = :schedule_id
                        AND endTime > :limit_time""", TaskoRun.class)
                .setParameter("schedule_id", scheduleId)
                .setParameter("limit_time", limitTime)
                .list();
    }

    private static TaskoBunch lookupBunchByOrgAndName(Integer orgId, String bunchName)
        throws NoSuchBunchTaskException {
        TaskoBunch bunch;
        if (orgId == null) {
            bunch = lookupSatBunchByName(bunchName);
        }
        else {
            bunch = lookupOrgBunchByName(bunchName);
        }
        if (bunch == null) {
            throw new NoSuchBunchTaskException(bunchName);
        }
        return bunch;
    }

    /**
     * lookup schedule by id
     * @param scheduleId schedule id
     * @return schedule
     */
    public static TaskoSchedule lookupScheduleById(Long scheduleId) {
        return getSession()
                .createQuery("FROM com.redhat.rhn.taskomatic.domain.TaskoSchedule WHERE id = :schedule_id",
                        TaskoSchedule.class)
                .setParameter("schedule_id", scheduleId)
                .getSingleResult();
    }

    /**
     * list schedule by label
     *
     * @param jobLabel schedule label
     * @return list of schedule
     */
    public static List<TaskoSchedule> listScheduleByLabel(String jobLabel) {
        return getSession()
                .createQuery("FROM com.redhat.rhn.taskomatic.domain.TaskoSchedule WHERE jobLabel = :job_label",
                        TaskoSchedule.class)
                .setParameter("job_label", jobLabel)
                .list();
    }

    /**
     * lookup bunch by label
     * @param bunchName bunch label
     * @return bunch
     */
    public static TaskoBunch lookupBunchByName(String bunchName) {
        return singleton.lookupObjectByNamedQuery("TaskoBunch.lookupByName", Map.of("name", bunchName));
    }

    /**
     * lists all schedules for an org
     * @param orgId organizational id
     * @return list of all schedules
     */
    public static List<TaskoSchedule> listSchedulesByOrg(Integer orgId) {
        if (orgId == null) {
            return getSession()
                    .createQuery("FROM com.redhat.rhn.taskomatic.domain.TaskoSchedule WHERE orgId IS NULL",
                            TaskoSchedule.class)
                    .list();
        }
        return getSession()
                .createQuery("FROM com.redhat.rhn.taskomatic.domain.TaskoSchedule WHERE orgId = :org_id",
                        TaskoSchedule.class)
                .setParameter("org_id", orgId)
                .list();
    }

    /**
     * list all runs associated with a schedule
     * @param scheduleId schedule id
     * @return list of runs
     */
    public static List<TaskoRun> listRunsBySchedule(Long scheduleId) {
        return getSession()
                .createQuery("FROM com.redhat.rhn.taskomatic.domain.TaskoRun WHERE scheduleId = :schedule_id",
                        TaskoRun.class)
                .setParameter("schedule_id", scheduleId)
                .list();
    }

    /**
     * list schedules older than given date
     * @param limitTime time of interest
     * @return list of schedules
     */
    public static List<TaskoSchedule> listSchedulesOlderThan(Date limitTime) {
        return getSession()
                .createQuery("FROM com.redhat.rhn.taskomatic.domain.TaskoSchedule WHERE activeTill < :limit_time",
                        TaskoSchedule.class)
                .setParameter("limit_time", limitTime)
                .list();
    }

    /**
     * lists organizational schedules by name
     * @param orgId organization id
     * @param jobLabel unique job name
     * @return list of schedules
     */
    public static List<TaskoSchedule> listSchedulesByOrgAndLabel(Integer orgId, String jobLabel) {
        if (orgId == null) {
            return getSession()
                    .createQuery("""
                        FROM  com.redhat.rhn.taskomatic.domain.TaskoSchedule
                        WHERE orgId IS NULL
                        AND   jobLabel = :job_label""", TaskoSchedule.class)
                    .setParameter("job_label", jobLabel)
                    .list();
        }
        return getSession()
                .createQuery("""
                        FROM com.redhat.rhn.taskomatic.domain.TaskoSchedule
                        WHERE orgId = :org_id
                        AND jobLabel = :job_label""", TaskoSchedule.class)
                .setParameter("job_label", jobLabel)
                .setParameter("org_id", orgId)
                .list();
    }

    /**
     * lookup run by id
     * @param runId run id
     * @return run
     */
    public static TaskoRun lookupRunById(Long runId) {
        return getSession()
                .createQuery("FROM com.redhat.rhn.taskomatic.domain.TaskoRun WHERE id = :run_id", TaskoRun.class)
                .setParameter("run_id", runId)
                .uniqueResult();
    }

    /**
     * lookup organizational run by id
     * @param orgId organizational id
     * @param runId run id
     * @return run
     * @throws InvalidParamException thrown in case of wrong runId
     */
    public static TaskoRun lookupRunByOrgAndId(Integer orgId, Integer runId)
        throws InvalidParamException {
        TaskoRun run = lookupRunById(runId.longValue());
        if ((run == null) || (!runBelongToOrg(orgId, run))) {
            throw new InvalidParamException("No such run id");
        }
        return run;
    }

    /**
     * lists organizational runs by schedule
     * @param orgId organization id
     * @param scheduleId schedule id
     * @return list of runs
     */
    public static List<TaskoRun> listRunsByOrgAndSchedule(Integer orgId,
            Integer scheduleId) {
        List<TaskoRun> runs = listRunsBySchedule(scheduleId.longValue());
        // verify it belongs to the right org
        runs.removeIf(taskoRunIn -> !runBelongToOrg(orgId, taskoRunIn));
        return runs;
    }

    /**
     * lists runs by bunch
     * @param bunchName bunch name
     * @return list of runs
     */
    public static List<TaskoRun> listRunsByBunch(String bunchName) {
        return getSession()
                .createQuery("""
                        FROM com.redhat.rhn.taskomatic.domain.TaskoRun
                        WHERE template.bunch.name = :bunch_name
                        ORDER BY startTime DESC, id DESC""", TaskoRun.class)
                .setParameter("bunch_name", bunchName)
                .list();
    }

    /**
     * Returns the latest run from the specified bunch
     * @param bunchName the bunch name
     * @return the latest run or null if none exists
     */
    public static TaskoRun getLatestRun(String bunchName) {
        String sql = """
            SELECT tr.* FROM rhnTaskoRun tr WHERE tr.template_id IN
            (SELECT tt.id FROM rhnTaskoTemplate tt WHERE tt.bunch_id =
            (SELECT tb.id FROM rhnTaskoBunch tb WHERE tb.name = :bunchName))
            AND tr.status IN (:status1, :status2, :status3) ORDER BY tr.start_time DESC, tr.id DESC LIMIT 1
            """;

        // Create the native query
        Query<TaskoRun> query = getSession().createNativeQuery(sql, TaskoRun.class);

        // Set the parameters for bunchName and status
        query.setParameter("bunchName", bunchName);
        query.setParameter("status1", TaskoRun.STATUS_RUNNING);
        query.setParameter("status2", TaskoRun.STATUS_FINISHED);
        query.setParameter("status3", TaskoRun.STATUS_INTERRUPTED);

        // Execute the query and return the result (or null if no result is found)
        try {
            return query.getSingleResult();
        }
        catch (NoResultException e) {
            // Handle the case where no result is found
            return null;
        }
    }

    /**
     * Reinitializes schedule
     * used, when quartz needs to be updated according to our tasko table entries
     * @param schedule schedule to reinit
     * @param now time to set
     * @return schedule
     */
    public static TaskoSchedule reinitializeScheduleFromNow(TaskoSchedule schedule,
            Date now) throws InvalidParamException, SchedulerException {
        TaskoQuartzHelper.destroyJob(schedule.getOrgId(), schedule.getJobLabel());
        schedule.setActiveFrom(now);
        if (!schedule.isCronSchedule()) {
            schedule.setActiveTill(now);
        }
        TaskoFactory.save(schedule);
        TaskoQuartzHelper.createJob(schedule);
        return schedule;
    }

    private static boolean runBelongToOrg(Integer orgId, TaskoRun run) {
        if (orgId == null) {
            return (run.getOrgId() == null);
        }
        return orgId.equals(run.getOrgId());
    }

    /**
     * Lists taskomatic runs those endTime IS NULL (most probably were interrupted by
     * taskomatic shutdown)
     * @return list of unfinished runs
     */
    public static List<TaskoRun> listUnfinishedRuns() {
        return getSession()
                .createQuery("FROM com.redhat.rhn.taskomatic.domain.TaskoRun WHERE endTime IS NULL", TaskoRun.class)
                .list();
    }

    /**
     * List repo-sync-bunch schedules newer than a given date. If the given date is null,
     * all schedules will be returned with an "activeFrom" date > January 1, 1970.
     * @param date time of interest
     * @return list of repo sync schedules
     */
    public static List<TaskoSchedule> listRepoSyncSchedulesNewerThan(Date date) {
        TaskoBunch bunch = lookupBunchByName("repo-sync-bunch");
        if (date == null) {
            date = new Date(0);
        }
        return getSession()
                .createQuery("""
                        FROM com.redhat.rhn.taskomatic.domain.TaskoSchedule
                        WHERE bunch.id = :bunch_id
                        AND activeFrom > :date""", TaskoSchedule.class)
                .setParameter("bunch_id", bunch.getId())
                .setParameter("date", date)
                .list();
    }

    protected static TaskoBunch checkBunchName(Integer orgId, String bunchName) throws NoSuchBunchTaskException {
        TaskoBunch bunch;
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
     * Get a unique label single job.
     *
     * @param orgId the organisation ID for the job
     * @param bunchName the bunch name
     * @return the unique job label
     *
     * @throws SchedulerException in case of internal scheduler error
     * * *
     * @throws SchedulerException in case of internal scheduler error
     */
    protected static String getUniqueSingleJobLabel(Integer orgId, String bunchName) throws SchedulerException {
        String jobLabel = "single-" + bunchName + "-";
        int count = 0;
        while (!TaskoFactory.listSchedulesByOrgAndLabel(orgId, jobLabel + count).isEmpty() ||
                (SchedulerKernel.getScheduler() != null && SchedulerKernel.getScheduler()
                        .getTrigger(triggerKey(jobLabel + count, TaskoQuartzHelper.getGroupName(orgId))) != null)) {
            count++;
        }
        return jobLabel + count;
    }

    /**
     * Create a new single bunch run in the database.
     *
     * @param orgId the organization ID
     * @param bunchName the bunch name
     * @param params the job parameters
     * @param start the start date of the job
     * @throws NoSuchBunchTaskException if the bunchName doesn't refer to an existing bunch
     * @throws SchedulerException for internal scheduler errors
     */
    public static void addSingleBunchRun(Integer orgId, String bunchName, Map<String, Object> params, Date start)
            throws NoSuchBunchTaskException, SchedulerException {
        TaskoBunch bunch = checkBunchName(orgId, bunchName);
        String jobLabel = getUniqueSingleJobLabel(null, bunchName);
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
        }
        // Don't set active till until the job it actually runs.
        schedule.setActiveTill(null);
        TaskoFactory.save(schedule);
        HibernateFactory.commitTransaction();
        log.info("Schedule created for {}.", jobLabel);
    }
}
