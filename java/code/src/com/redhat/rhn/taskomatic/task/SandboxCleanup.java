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

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.db.datasource.CallableMode;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.common.db.datasource.SelectMode;

import org.quartz.JobExecutionContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SandboxCleanup
 */
public class SandboxCleanup extends RhnJavaJob {

    @Override
    public String getConfigNamespace() {
        return "sandbox_cleanup";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(JobExecutionContext arg0In) {

        int sandboxLifetime = Config.get().getInt("sandbox_lifetime"); //in days

        Map<String, Object> params = new HashMap<>();
        params.put("window", sandboxLifetime);
        remove("find_sandbox_file_candidates", params, "remove_sandbox_file");
        remove("find_sandbox_channel_candidates", params, "remove_sandbox_channel");
    }

    private void remove(String candidateQuery, Map<String, Object> candidateParams, String removeQuery) {
        SelectMode candidateMode =
            ModeFactory.getMode("Task_queries", candidateQuery);
        CallableMode removeMode =
            ModeFactory.getCallableMode("Task_queries", removeQuery);
        List<Row> candidates = candidateMode.execute(candidateParams);
        if (candidates != null && !candidates.isEmpty()) {
            if (removeQuery.contains("file")) {
                log.info("Removing sandbox files: {}", candidates.size());
            }
            else if (removeQuery.contains("channel")) {
                log.info("Removing sandbox channels: {}", candidates.size());
            }
            Map<String, Object> params = new HashMap<>();
            Map<String, Integer> out = new HashMap<>();
            for (Row row: candidates) {
                params.put("id", row.get("id"));
                removeMode.execute(params, out);
            }
        }
    }

}
