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

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Optional;

/**
 * Execute actions via salt-ssh.
 */
public class SSHMinionActionExecutor extends RhnJavaJob {

    /**
     * @param context the job execution context
     * @see org.quartz.Job#execute(JobExecutionContext)
     */
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        long actionId = context.getJobDetail()
                .getJobDataMap().getLongValueFromString("action_id");
        String sshMinionId = context.getJobDetail().getJobDataMap().getString("ssh_minion_id");
        Optional<MinionServer> sshMinionOpt = MinionServerFactory.findByMinionId(sshMinionId);
        if (sshMinionId.isEmpty()) {
            log.error("SSH Minion " + sshMinionId + " not found. Aborting execution of action " + actionId);
            return;
        }
        Action action = ActionFactory.lookupById(actionId);
        if (action == null) {
            log.error("Action not found: " + actionId);
            return;
        }
        log.info("Executing action: " + actionId + " on ssh minion: " + sshMinionId);
        GlobalInstanceHolder.SALT_SERVER_ACTION_SERVICE.executeSSHAction(action, sshMinionOpt.get());
    }
}
