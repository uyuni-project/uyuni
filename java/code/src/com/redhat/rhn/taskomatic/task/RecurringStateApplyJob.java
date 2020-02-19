/**
 * Copyright (c) 2020 SUSE LLC
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

import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.webui.controllers.StatesAPI;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Used to run a scheduled Recurring Highstate Apply action
 */
public class RecurringStateApplyJob extends RhnJavaJob {

    /**
     * {@inheritDoc}
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap data = context.getJobDetail().getJobDataMap();
        if (Boolean.parseBoolean(data.get("active").toString())) {
            User user = Optional.ofNullable(data.get("user_id"))
                    .map(id -> Long.parseLong(id.toString()))
                    .map(userId -> UserFactory.lookupById(userId))
                    .orElse(null);
            if (user == null) {
                throw new NullPointerException("User not found");
            }
            List<Long> minionIds = new ArrayList<>();

            /* TODO: This needs some rework */
            StatesAPI.getMinionNamesAndIds(data.get("targetType").toString(),
                    Long.parseLong(data.get("targetId").toString()), user).ifPresentOrElse(
                    m -> minionIds.addAll(Arrays.stream(m.get("minionIds")
                            .replaceAll("([\\[\\] ])", "")
                            .split(",")).map(Long::parseLong).collect(Collectors.toList())),
                    () -> { throw new NullPointerException("No minion Ids provided"); }
            );

            List<Long> sids = MinionServerFactory.lookupByIds(minionIds).map(server -> {
                if (!SystemManager.isAvailableToUser(user, server.getId())) {
                    log.error("System " + server.getName() + " not available to user with uid: " + user.getId());
                }
                return server.getId();
            }).collect(Collectors.toList());
            boolean test = Boolean.parseBoolean((String) data.get("test"));
            Date timeNow = context.getFireTime();

            try {
                ActionChainManager.scheduleApplyStates(user, sids, Optional.of(test), timeNow, null);
            }
            catch (Exception e) {
                log.error(e.getMessage(), e);
                throw new JobExecutionException(e);
            }
        }
        else {
            String scheduleName = context.getJobDetail().getKey().getName();
            log.info("Schedule" + scheduleName + "not active - skipping execution");
        }
    }
}
