/*
 * Copyright (c) 2015 Red Hat, Inc.
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

import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.errata.ErrataAction;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.manager.action.ActionManager;

import com.suse.manager.maintenance.MaintenanceManager;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * This is what automatically schedules automatic errata update actions.
 * This used to be part of what the Errata Queue job did, but that didn't work well.
 * Errata Queue is a run-once job that happens when you need to send notification
 * emails about new errata. But adding new errata is not the only time you might need
 * to schedule auto errata updates, instead you also need to do it if your server is
 * changing channel subscriptions or has installed on older version of a package. So
 * It made the most sense to separate things out into two separate jobs. This also
 * ensures that we don't miss auto errata update actions if the errata cache is not
 * ready or something, as now they'll just be scheduled the next time this job runs
 * after the errata cache is done.
 *
 */

public class AutoErrataTask extends RhnJavaJob {

    private Queue<Action> actionsToSchedule = new LinkedList<>();

    @Override
    public String getConfigNamespace() {
        return "auto_errata";
    }

    /**
     * {@inheritDoc}
     */
    public void execute(JobExecutionContext context)
        throws JobExecutionException {

        List<Long> systems = getAutoErrataSystems();
        if (systems == null || systems.size() == 0) {
            log.debug("No systems with auto errata enabled");
            return;
        }

        List<Map<String, Long>> results = getErrataToProcess(filterSystemsInMaintenanceMode(systems));
        if (results == null || results.size() == 0) {
            log.debug("No unapplied auto errata found. Skipping systems not in maintenance mode... exiting");
            return;
        }

        log.debug("=== Scheduling {} auto errata updates", results.size());

        for (Map<String, Long> result : results) {
            Long errataId = result.get("errata_id");
            Long serverId = result.get("server_id");
            Long orgId = result.get("org_id");
            try {
                Errata errata = ErrataFactory.lookupErrataById(errataId);
                Org org = OrgFactory.lookupById(orgId);
                ErrataAction errataAction = ActionManager.
                        createErrataAction(org, errata);
                ActionManager.addServerToAction(serverId, errataAction);
                ActionManager.storeAction(errataAction);
                actionsToSchedule.add(errataAction);
            }
            catch (Exception e) {
                log.error("Errata: {}, Org Id: {}, Server: {}", errataId, orgId, serverId, e);
                throw new JobExecutionException(e);
            }
            log.debug("Scheduling auto update actions for server {} and erratum {}", serverId, errataId);
        }
    }

    /**
     * Return the list of systems that have the auto errata update function enabled
     *
     * @return list of systems with auto errata update enabled
     */
    protected List<Long> getAutoErrataSystems() {
        SelectMode select = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_AUTO_ERRATA_SYSTEMS);
        @SuppressWarnings("unchecked")
        List<Map<String, Long>> results = select.execute();
        return results.stream().map(system -> system.get("id")).collect(Collectors.toList());
    }

    /**
     * Return list of systems that are in maintenance mode
     *
     * @param systems systems with auto errata enabled
     * @return list of systems in maintenance mode
     */
    protected List<Long> filterSystemsInMaintenanceMode(List<Long> systems) {
        MaintenanceManager mm = new MaintenanceManager();
        return ServerFactory.lookupByIds(systems).stream()
                .filter(mm::isSystemInMaintenanceMode)
                .map(Server::getId)
                .collect(Collectors.toList());
    }

    /**
     * The brains of the operation resides in this query. The query logic is:
     * Find all errata-server combinations where:
     * - server is auto-update capable (has feature)
     * - server has enabled auto-updates
     * - the errata cache (rhnServerNeededCache) says that the erratum is an upgrade
     *     for this server
     * - the channel that the erratum in is not currently regenerating yum metadata
     * - we have not already scheduled an action for this errata-server combination
     *   - If we have ever scheduled an action before then it'll never get rescheduled.
     *     So if the action failed or something the user will need to fix whatever was
     *     wrong and manually re-schedule.
     *
     * @return maps of errata_id, server_id, and org_id that need actions
     */
    protected List<Map<String, Long>> getErrataToProcess(List<Long> sids) {
        /*
         * Additional check might be needed. Do not schedule anything
         * if a task with bunch name "repo-sync-bunch" is ready or running
         * or a CLM build is in process.
         *
         * select 1
         *  from rhnTaskoSchedule rts
         *  join rhnTaskoBunch rtb on rts.bunch_id = rtb.id
         *  join rhnTaskoRun rtr on rtr.schedule_id = rts.id
         * where rtb.name = 'repo-sync-bunch'
         *   and rtr.status in ('RUNNING','READY')
         */
        if (sids == null || sids.size() == 0) {
            return new ArrayList<>();
        }

        SelectMode select = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_AUTO_ERRATA_CANDIDATES);
        @SuppressWarnings("unchecked")
        List<Map<String, Long>> results = select.execute(sids);
        return results;
    }

    @Override
    protected void finishJob() {
        actionsToSchedule.forEach(TaskHelper::scheduleActionExecution);
    }
}
