/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.frontend.dto.OrgIdWrapper;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * SummaryPopulation figures out what orgs might be candidates for sending
 * daily summary email
 */
public class SummaryPopulation extends RhnJavaJob {

    @Override
    public String getConfigNamespace() {
        return "summary_population";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(JobExecutionContext ctx) throws JobExecutionException {

        try {
            // don't want duplicates otherwise we risk violating the
            // RHN_DSQUEUE_OID_UQ unique constraint on org_id
            Set orgSet = new LinkedHashSet<>();

            log.debug("Finding orgs with awol servers");
            List orgs = awolServerOrgs();
            orgSet.addAll(orgs);
            if (log.isDebugEnabled()) {
                int orgCount = 0;
                if (orgs != null) {
                    orgCount = orgs.size();
                }
                else {
                    log.debug("awolServerOrgs() returned null");
                }
                log.debug("Found  {} awol servers", orgCount);
            }

            log.debug("Finding orgs w/ recent action activity");
            orgSet.addAll(orgsWithRecentActions());
            log.debug("Done finding orgs w/ recent action activity");

            log.debug("Enqueing orgs");
            for (Object oIn : orgSet) {
                OrgIdWrapper bdw = (OrgIdWrapper) oIn;
                enqueueOrg(bdw.toLong());
            }
            log.debug("Finished enqueing orgs");
            log.debug("finished queueing orgs for daily summary emails");
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }

    private List awolServerOrgs() {
        SelectMode m = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_SUMMARYPOP_AWOL_SERVER_IN_ORGS);

        Map<String, Object> params = new HashMap<>();
        return m.execute(params);
    }

    private List orgsWithRecentActions() {
        SelectMode m = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_SUMMARYPOP_ORGS_RECENT_ACTIONS);
        return m.execute();
    }

    private int enqueueOrg(Long orgId) {
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", orgId);
        SelectMode select = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_VERIFY_SUMMARY_QUEUE);
        WriteMode m = ModeFactory.getWriteMode(
                TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_INSERT_SUMMARY_QUEUE);

        try {
            DataResult result = select.execute(params);
            Map row = (Map) result.get(0);
            Long count = (Long) row.get("queued");
            if (count.intValue() == 0) {
                return m.executeUpdate(params);
            }
            log.warn("Skipping {} because it's already queued", orgId);
            return 0;
        }
        catch (RuntimeException e) {
            log.warn(e.getMessage(), e);
            return -1;
        }
    }

}
