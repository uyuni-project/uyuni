/**
 * Copyright (c) 2020 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.taskomatic.task;

import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.system.SystemManager;

import org.apache.http.HttpStatus;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import spark.Spark;

/**
 * Used to run a scheduled Recurring Highstate Apply action
 */
public class RecurringStateApply extends RhnJavaJob {

    /**
     * {@inheritDoc}
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        /* TODO: Adapt to execute general states not just highstates*/

        JobDataMap data = context.getJobDetail().getJobDataMap();
        if(Boolean.parseBoolean(data.get("isActive").toString())) {
            try {
                User user = Optional.ofNullable(data.get("user_id"))
                        .map(id -> Long.parseLong(id.toString()))
                        .map(userId -> UserFactory.lookupById(userId))
                        .orElse(null);
                List<Long> minionIds = Arrays.stream(data.get("minionIds").toString()
                        .replaceAll("([\\[\\] ])", "")
                        .split(",")).map(Long::parseLong).collect(Collectors.toList());
                List<Long> sids = MinionServerFactory.lookupByIds(minionIds).map(server -> {
                    if (!SystemManager.isAvailableToUser(user, server.getId())) {
                        Spark.halt(HttpStatus.SC_FORBIDDEN);
                    }
                    return server.getId();
                }).collect(Collectors.toList());
                boolean test = Boolean.parseBoolean((String) data.get("isTest"));
                Date timeNow = context.getFireTime();

                ActionChainManager.scheduleApplyStates(user, sids, Optional.of(test), timeNow, null);

            } catch (Exception e) {
                throw new JobExecutionException(e.getMessage());
            }
        }
    }
}
