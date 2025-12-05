/*
 * Copyright (c) 2023 SUSE LLC
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

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.manager.entitlement.EntitlementManager;

import org.quartz.JobExecutionContext;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Schedule a System Profile refresh
 *  - hardware refresh of all systems org by org
 */
public class SystemProfileRefreshTask extends RhnJavaJob {

    private Queue<Action> actionsToSchedule = new LinkedList<>();

    @Override
    public String getConfigNamespace() {
        return "system_profile_refresh";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(JobExecutionContext context) {

        log.info("running hardware refresh");

        actionsToSchedule.clear();

        for (Org org : OrgFactory.lookupAllOrgs()) {

            Set<Long> sids = ServerFactory.listOrgSystems(org.getId()).stream()
                    .filter(s -> !s.isInactive())
                    .filter(s -> (s.hasEntitlement(EntitlementManager.SALT) ||
                                s.hasEntitlement(EntitlementManager.MANAGEMENT)))
                    .map(Server::getId)
                    .collect(Collectors.toSet());
            if (sids.isEmpty()) {
                continue;
            }

            Action act = ActionFactory.createAction(ActionFactory.TYPE_HARDWARE_REFRESH_LIST);
            // set up needed fields for the action
            act.setName(act.getActionTypeName());
            act.setOrg(org);
            ActionFactory.save(act);

            ActionFactory.scheduleForExecution(act, sids);

            log.info("  schedule HW refresh for {} systems in org {}", sids.size(), org.getName());
            actionsToSchedule.add(act);
        }
    }

    @Override
    protected void finishJob() {
        int cnt = 0;
        for (Action a : actionsToSchedule) {
            TaskHelper.scheduleActionExecution(a);
            cnt++;
            try {
                if (cnt < actionsToSchedule.size()) {
                    Thread.sleep(2 * 60 * 1000L); // sleep 2 minutes before schedule next org
                }
            }
            catch (InterruptedException e) {
                log.info("Sleep interrupted. Schedule next action");
            }
        }
    }
}
