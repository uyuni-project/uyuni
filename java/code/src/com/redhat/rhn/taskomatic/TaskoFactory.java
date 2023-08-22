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


import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.taskomatic.domain.TaskoBunch;
import com.redhat.rhn.taskomatic.domain.TaskoRun;
import com.redhat.rhn.taskomatic.domain.TaskoSchedule;
import com.redhat.rhn.taskomatic.domain.TaskoTask;
import com.redhat.rhn.taskomatic.domain.TaskoTemplate;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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
        return singleton.listObjectsByNamedQuery("TaskoTask.listTasks", Map.of());
    }

    /**
     * lists runs older than given date
     * @param limitTime date of interest
     * @return list of runs
     */
    public static List<TaskoRun> listRunsOlderThan(Date limitTime) {
        return singleton.listObjectsByNamedQuery("TaskoRun.listOlderThan", Map.of("limit_time", limitTime));
    }

    /**
     * lists runs newer than given date
     * @param limitTime date of interest
     * @return list of runs
     */
    public static List<TaskoRun> listRunsNewerThan(Date limitTime) {
        return singleton.listObjectsByNamedQuery("TaskoRun.listNewerThan", Map.of("limit_time", limitTime));
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
        Map<String, Object> params = new HashMap<>();

        params.put("timestamp", new Date());    // use server time, not DB time
        if (orgId == null) {
            schedules = singleton.listObjectsByNamedQuery("TaskoSchedule.listActiveInSat", params);
        }
        else {
            params.put("org_id", orgId);
            schedules = singleton.listObjectsByNamedQuery("TaskoSchedule.listActiveByOrg", params);
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
    public static List<TaskoSchedule> listActiveSchedulesByOrgAndLabel(Integer orgId,
            String jobLabel) {
        Map<String, Object> params = new HashMap<>();
        params.put("job_label", jobLabel);
        params.put("timestamp", new Date());    // use server time, not DB time
        if (orgId == null) {
            return singleton.listObjectsByNamedQuery("TaskoSchedule.listActiveInSatByLabel", params);
        }
        params.put("org_id", orgId);
        return singleton.listObjectsByNamedQuery("TaskoSchedule.listActiveByOrgAndLabel", params);
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
        Map<String, Object> params = new HashMap<>();
        params.put("timestamp", new Date());    // use server time, not DB time
        params.put("bunch_id", bunch.getId());
        if (orgId == null) {
            return singleton.listObjectsByNamedQuery("TaskoSchedule.listActiveInSatByBunch", params);
        }
        params.put("org_id", orgId);
        return singleton.listObjectsByNamedQuery("TaskoSchedule.listActiveByOrgAndBunch", params);
    }


    /**
     * list schedules, that shall be run sometime in the future
     * @return list of schedules to be run at least once
     */
    public static List<TaskoSchedule> listFuture() {
        return singleton.listObjectsByNamedQuery("TaskoSchedule.listFuture", Map.of("timestamp", new Date()));
    }

    /**
     * list all schedule runs with (future) timestamps newer than limitTime
     * @param scheduleId schedule id
     * @param limitTime limit time
     * @return list of runs
     */
    public static List<TaskoRun> listNewerRunsBySchedule(Long scheduleId, Date limitTime) {
        return singleton.listObjectsByNamedQuery("TaskoRun.listByScheduleNewerThan",
                Map.of("schedule_id", scheduleId, "limit_time", limitTime));
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
        return singleton.lookupObjectByNamedQuery("TaskoSchedule.lookupById", Map.of("schedule_id", scheduleId));
    }

    /**
     * lookup schedule by label
     * @param jobLabel schedule label
     * @return schedule
     */
    public static TaskoSchedule lookupScheduleByLabel(String jobLabel) {
        return singleton.lookupObjectByNamedQuery("TaskoSchedule.lookupByLabel", Map.of("job_label", jobLabel));
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
            return singleton.listObjectsByNamedQuery("TaskoSchedule.listInSat", Map.of());
        }
        return singleton.listObjectsByNamedQuery("TaskoSchedule.listByOrg", Map.of("org_id", orgId));
    }

    /**
     * list all runs associated with a schedule
     * @param scheduleId schedule id
     * @return list of runs
     */
    public static List<TaskoRun> listRunsBySchedule(Long scheduleId) {
        return singleton.listObjectsByNamedQuery("TaskoRun.listBySchedule", Map.of("schedule_id", scheduleId));
    }

    /**
     * list schedules older than given date
     * @param limitTime time of interest
     * @return list of schedules
     */
    public static List<TaskoSchedule> listSchedulesOlderThan(Date limitTime) {
        return singleton.listObjectsByNamedQuery("TaskoSchedule.listOlderThan", Map.of("limit_time", limitTime));
    }

    /**
     * lists organizational schedules by name
     * @param orgId organization id
     * @param jobLabel unique job name
     * @return list of schedules
     */
    public static List<TaskoSchedule> listSchedulesByOrgAndLabel(Integer orgId,
            String jobLabel) {
        Map<String, Object> params = new HashMap<>();
        params.put("job_label", jobLabel);
        if (orgId == null) {
            return singleton.listObjectsByNamedQuery("TaskoSchedule.listInSatByLabel", params);
        }
        params.put("org_id", orgId);
        return singleton.listObjectsByNamedQuery("TaskoSchedule.listByOrgAndLabel", params);
    }

    /**
     * lookup run by id
     * @param runId run id
     * @return run
     */
    public static TaskoRun lookupRunById(Long runId) {
        return singleton.lookupObjectByNamedQuery("TaskoRun.lookupById", Map.of("run_id", runId));
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
        return singleton.listObjectsByNamedQuery("TaskoRun.listByBunch", Map.of("bunch_name", bunchName));
    }

    /**
     * Returns the latest run from the specified bunch
     * @param bunchName the bunch name
     * @return the latest run or null if none exists
     */
    public static TaskoRun getLatestRun(String bunchName) {
        DetachedCriteria bunchIds = DetachedCriteria.forClass(TaskoBunch.class)
                .add(Restrictions.eq("name", bunchName))
                .setProjection(Projections.id());

        DetachedCriteria templateIds = DetachedCriteria.forClass(TaskoTemplate.class)
                .add(Subqueries.propertyIn("bunch", bunchIds))
                .setProjection(Projections.id());

        return (TaskoRun) getSession()
            .createCriteria(TaskoRun.class)
            .add(Subqueries.propertyIn("template.id", templateIds))
            .add(Restrictions.in("status",
                    new Object[] {
                            TaskoRun.STATUS_RUNNING,
                            TaskoRun.STATUS_FINISHED,
                            TaskoRun.STATUS_INTERRUPTED
            }))
            .addOrder(Order.desc("startTime"))
            .addOrder(Order.desc("id"))
            .setFirstResult(0)
            .setMaxResults(1)
            .uniqueResult();
    }

    /**
     * Reinitializes schedule
     * used, when quartz needs to be updated according to our tasko table entries
     * @param schedule schedule to reinit
     * @param now time to set
     * @return schedule
     */
    public static TaskoSchedule reinitializeScheduleFromNow(TaskoSchedule schedule,
            Date now) {
        TaskoQuartzHelper.destroyJob(schedule.getOrgId(), schedule.getJobLabel());
        schedule.setActiveFrom(now);
        if (!schedule.isCronSchedule()) {
            schedule.setActiveTill(now);
        }
        TaskoFactory.save(schedule);
        try {
            TaskoQuartzHelper.createJob(schedule);
            return schedule;
        }
        catch (InvalidParamException e) {
            // Pech gehabt()
        }
        return null;
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
        return singleton.listObjectsByNamedQuery("TaskoRun.listUnfinished", Map.of());
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
        return singleton.listObjectsByNamedQuery("TaskoSchedule.listNewerThanByBunch",
                Map.of("bunch_id", bunch.getId(), "date", date));
    }
}
