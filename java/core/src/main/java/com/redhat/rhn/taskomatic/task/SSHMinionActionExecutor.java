/*
 * Copyright (c) 2020 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.taskomatic.task;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;

import com.suse.cloud.CloudPaygManager;

import org.quartz.JobExecutionContext;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Execute actions via salt-ssh.
 */
public class SSHMinionActionExecutor extends RhnJavaJob {

    private final CloudPaygManager cloudPaygManager;

    /**
     * Default Constructor
     */
    public SSHMinionActionExecutor() {
        this(GlobalInstanceHolder.PAYG_MANAGER);
    }

    /**
     * Constructor
     * @param cloudPaygManagerIn the payg manager
     */
    public SSHMinionActionExecutor(CloudPaygManager cloudPaygManagerIn) {
        cloudPaygManager = cloudPaygManagerIn;
    }

    @Override
    public int getDefaultParallelThreads() {
        return 20;
    }

    @Override
    public String getConfigNamespace() {
        return "sshminion_action_executor";
    }

    /**
     * @param context the job execution context
     * @see org.quartz.Job#execute(JobExecutionContext)
     */
    @Override
    public void execute(JobExecutionContext context) {
        long actionId = context.getJobDetail()
                .getJobDataMap().getLongValueFromString("action_id");
        boolean forcePkgRefresh = context.getJobDetail().getJobDataMap().getBooleanValue("force_pkg_list_refresh");
        String sshMinionId = context.getJobDetail().getJobDataMap().getString("ssh_minion_id");
        Optional<MinionServer> sshMinionOpt = MinionServerFactory.findByMinionId(sshMinionId);
        if (sshMinionOpt.isEmpty()) {
            log.error("SSH Minion {} not found. Aborting execution of action {}", sshMinionId, actionId);
            return;
        }
        Action action = ActionFactory.lookupById(actionId);
        if (action == null) {
            log.error("Action not found: {}", actionId);
            return;
        }
        if (!cloudPaygManager.isCompliant()) {
            log.error("This action was not executed because SUSE Multi-Linux Manager Server PAYG is unable to send " +
                    "accounting data to the cloud provider.");
            ActionFactory.rejectScheduledActions(List.of(actionId),
                    LocalizationService.getInstance().getMessage("task.action.rejection.notcompliant"));
            return;
        }

        // Check if dealing with SUMA PAYG and BYOS minions without SCC credentials
        if (cloudPaygManager.isPaygInstance()) {
            cloudPaygManager.checkRefreshCache(true);
            if (!cloudPaygManager.hasSCCCredentials()) {
                if (action.rejectScheduleActionIfByos()) {
                    return;
                }
            }
        }

        action.getServerActions().stream()
                .filter(sa -> sshMinionOpt.get().getId().equals(sa.getServerId())).findFirst()
                .ifPresent(sa -> {
                    sa.setStatusPickedUp();
                    sa.setPickupTime(new Date());
                    HibernateFactory.commitTransaction();
                });

        log.info("Executing action: {} on ssh minion: {}", actionId, sshMinionId);
        GlobalInstanceHolder.SALT_SERVER_ACTION_SERVICE.executeSSHAction(action, sshMinionOpt.get(), forcePkgRefresh);
    }
}
