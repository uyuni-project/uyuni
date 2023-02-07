/*
 * Copyright (c) 2019 SUSE LLC
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

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.taskomatic.task.checkin.CheckinCandidatesResolver;
import com.redhat.rhn.taskomatic.task.checkin.SystemSummary;

import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.salt.netapi.datatypes.target.MinionList;

import org.quartz.JobExecutionContext;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Perform a regular check-in on minions.
 */
public class MinionCheckin extends RhnJavaJob {

    private SaltApi saltApi = GlobalInstanceHolder.SALT_API;

    @Override
    public String getConfigNamespace() {
        return "minion_checkin";
    }

    /**
     * @param context the job execution context
     * @see org.quartz.Job#execute(JobExecutionContext)
     */
    @Override
    public void execute(JobExecutionContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Perform checkin on regular minions");
        }

        List<String> minionIds = this.findCheckinCandidatesIds();
        if (!minionIds.isEmpty()) {
            this.saltApi.checkIn(new MinionList(minionIds));
        }
    }

    /**
     * Retrieves the IDs of regular minions candidates to perform a check-in.
     *
     * @see CheckinRegularMinionsResolver#getCheckinCandidates()
     * @return a list of minion IDs
     */
    private List<String> findCheckinCandidatesIds() {
        CheckinCandidatesResolver candidatesResolver = new CheckinCandidatesResolver(
                TaskConstants.TASK_QUERY_MINION_CHECKIN_FIND_CHECKIN_CANDIDATES);
        List<SystemSummary> checkinCandidates = candidatesResolver.getCheckinCandidates();

        return checkinCandidates.stream().map(SystemSummary::getMinionId).collect(Collectors.toList());
    }

    /**
     * Setter for systemQuery.
     *
     * @param saltApiIn the systemQuery instance
     */
    public void setSaltApi(SaltApi saltApiIn) {
        this.saltApi = saltApiIn;
    }
}
