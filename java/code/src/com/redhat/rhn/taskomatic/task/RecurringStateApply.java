/**
 * Copyright (c) 2009--2015 Red Hat, Inc.
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
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import org.apache.http.HttpStatus;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import spark.Spark;

/**
 * Used for syncing repos (like yum repos) to a channel.
 * This really just calls a python script.
 */
public class RecurringStateApply extends RhnJavaJob {

    /**
     * {@inheritDoc}
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {

        JobDataMap data = context.getJobDetail().getJobDataMap();

        User user = (User) data.get("user");
        List<Long> minionIds = (List<Long>) data.get("minionIds");
        List<Long> sids =
                MinionServerFactory.lookupByIds(minionIds).map(server -> {
                    if (!SystemManager.isAvailableToUser(user, server.getId())) {
                        Spark.halt(HttpStatus.SC_FORBIDDEN);
                    }
                    return server.getId();
                }).collect(Collectors.toList());
        boolean test = Boolean.parseBoolean((String) data.get("isTest"));
        Date timeNow = context.getFireTime();

        try {
            ActionChainManager.scheduleApplyStates(user, sids, Optional.of(test), timeNow, null);
        } catch(TaskomaticApiException e) {
            throw new JobExecutionException("Invalid argument");
        }
    }
}
