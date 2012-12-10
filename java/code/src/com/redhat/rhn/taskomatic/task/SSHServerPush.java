/**
 * Copyright (c) 2012 Novell
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.Server;

/**
 * Call rhn_check on relevant systems via SSH using remote port forwarding.
 */
public class SSHServerPush extends RhnJavaJob {

    /**
     * {@inheritDoc}
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        List<?> systems = getCandidates();
        log.info("--> SSHServerPush: " + systems.size());
        for (Object s : systems) {
            Long sid = (Long) (((HashMap<?, ?>) s).get("id"));
            Server server = (Server) HibernateFactory.getSession().load(Server.class, sid);
            String address = server.getIpAddress();
            log.info("Running 'rhn_check' on: " + address);
            String[] cmd = getCommand(address).toArray(new String[0]);
            executeExtCmd(cmd);
        }
    }

    /**
     * Call query to find relevant systems.
     * @return list of system IDs
     */
    private List<?> getCandidates() {
        SelectMode select = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_SSH_SERVER_PUSH_FIND_CANDIDATES);
        return select.execute();
    }

    /**
     * Return 'ssh' command for a given hostname or IP address.
     * @param hostname or IP address
     * @return command as list of strings
     */
    private static List<String> getCommand(String hostname) {
        List<String> cmd = new ArrayList<String>();
        cmd.add("ssh");
        cmd.add("-R");
        cmd.add("1234:hoag.suse.de:443");
        cmd.add("root@" + hostname);
        cmd.add("rhn_check");
        return cmd;
    }
}
