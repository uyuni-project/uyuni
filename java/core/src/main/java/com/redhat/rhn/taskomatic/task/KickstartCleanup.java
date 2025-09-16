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
/*
 * Copyright (c) 2010 SUSE LLC
 */
package com.redhat.rhn.taskomatic.task;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.manager.system.SystemManager;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Cleans up stale Kickstarts
 */

public class  KickstartCleanup extends RhnJavaJob {

    @Override
    public String getConfigNamespace() {
        return "kickstart_cleanup";
    }

    /**
     * Primarily a convenience method to make testing easier
     * @param ctx Quartz job runtime environment
     *
     * @throws JobExecutionException Indicates somes sort of fatal error
     */
    @Override
    public void execute(JobExecutionContext ctx) throws JobExecutionException {
        try {
            SelectMode select = ModeFactory.getMode(TaskConstants.MODE_NAME,
                    TaskConstants.TASK_QUERY_KSCLEANUP_FIND_CANDIDATES);
            DataResult<Row> dr = select.execute(Collections.emptyMap());
            if (log.isDebugEnabled()) {
                log.debug("Found {} entries to process", dr.size());
            }
            // Bail early if no candidates
            if (dr.isEmpty()) {
                return;
            }

            Long failedStateId = findFailedStateId();
            if (failedStateId == null) {
                log.warn("Failed kickstart state id not found");
                return;
            }
            for (Row row : dr) {
                processRow(failedStateId, row);
            }
        }
        catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }

    private Long findFailedStateId() {
        Long retval = null;
        SelectMode select = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_KSCLEANUP_FIND_FAILED_STATE_ID);
        DataResult<Row> dr = select.execute(Collections.emptyMap());
        if (!dr.isEmpty()) {
            retval = (Long) dr.get(0).get("id");
        }
        return retval;
    }

    private void processRow(Long failedStateId, Map<String, Object> row) {
        Long sessionId = (Long) row.get("id");
        if (log.isInfoEnabled()) {
            log.info("Processing stalled kickstart session {}", sessionId);
        }
        Long actionId = (Long) row.get("action_id");
        Long oldServerId = (Long) row.get("old_server_id");
        Long newServerId = (Long) row.get("new_server_id");
        if (actionId != null) {
            actionId = findTopmostParentAction(actionId);
            if (oldServerId != null) {
                ActionFactory.removeActionForSystem(actionId, oldServerId);
                SystemManager.updateSystemOverview(oldServerId);
            }
            if (newServerId != null) {
                ActionFactory.removeActionForSystem(actionId, newServerId);
                SystemManager.updateSystemOverview(newServerId);
            }
        }
        markFailed(sessionId, failedStateId);
    }

    private void markFailed(Long sessionId, Long failedStateId) {
        WriteMode update = ModeFactory.getWriteMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_KSCLEANUP_MARK_SESSION_FAILED);
        Map<String, Object> params = new HashMap<>();
        params.put("session_id", sessionId);
        params.put("failed_state_id", failedStateId);
        update.executeUpdate(params);
    }

    private Long findTopmostParentAction(Long startingAction) {
        SelectMode select = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_KSCLEANUP_FIND_PREREQ_ACTION);
        Map<String, Object> params = new HashMap<>();
        params.put("action_id", startingAction);
        if (log.isDebugEnabled()) {
            log.debug("StartingAction: {}", startingAction);
        }

        Long retval = startingAction;
        Long preqid = startingAction;
        DataResult<Row> dr = select.execute(params);
        if (log.isDebugEnabled()) {
            log.debug("dr: {}", dr);
        }

        while (!dr.isEmpty() && preqid != null) {
            preqid = (Long) dr.get(0).get("prerequisite");
            if (preqid != null) {
                retval = preqid;
                params.put("action_id", retval);
                dr = select.execute(params);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("preqid: {}", preqid);
            log.debug("Returning: {}", retval);
        }

        return retval;
    }
}
