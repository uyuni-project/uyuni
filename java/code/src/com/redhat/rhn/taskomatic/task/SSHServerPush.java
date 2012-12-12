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

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.Server;

/**
 * Call rhn_check on relevant systems via SSH using remote port forwarding.
 */
public class SSHServerPush extends RhnJavaJob {

    private String localhost;
    private int rport;
    private int lport;
    private JSch ssh;

    /**
     * {@inheritDoc}
     */
    public void execute(JobExecutionContext context) throws JobExecutionException {
        List<?> systems = getCandidates();
        log.info("--> SSHServerPush: " + systems.size());
        if (systems.size() > 0) {
            init();
        }
        for (Object s : systems) {
            Long sid = (Long) (((HashMap<?, ?>) s).get("id"));
            Server server = (Server) HibernateFactory.getSession().load(Server.class, sid);
            String host = server.getIpAddress();
            log.info("Running 'rhn_check' for: " + host);
            rhnCheck(host);
        }
    }

    /**
     * Init job instance, do this only once per job execution.
     */
    private void init() {
        // Get local address
        try {
            localhost = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            log.error(e.getMessage(), e);
        }

        // Setup port forwarding
        rport = 1234;
        lport = 443;

        // Configure ssh client
        JSch.setConfig("StrictHostKeyChecking", "yes");
        ssh = new JSch();
        try {
            ssh.setKnownHosts("/root/.ssh/known_hosts");
            ssh.addIdentity("/root/.ssh/id_rsa");
        } catch (JSchException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Call rhn_check via JSch.
     */
    private void rhnCheck(String host) {
        Session session = null;
        ChannelExec channel = null;
        try {
            // Setup session
            session = ssh.getSession("root", host);
            session.connect();
            session.setPortForwardingR(rport, localhost, lport);

            // Open channel
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand("rhn_check");

            // Init streams
            channel.setInputStream(null);
            InputStream in = channel.getInputStream();

            // Connect and read STDOUT
            channel.connect();
            StringBuilder sb = new StringBuilder();
            byte[] tmp = new byte[0x800];
            int exitStatus;
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, tmp.length);
                    if (i < 0) {
                        break;
                    }
                    sb.append(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    exitStatus = channel.getExitStatus();
                    break;
                }
            }
            log.info("exit code: " + exitStatus);
            log.debug("stdout:\n" + sb.toString());
        }
        catch (JSchException e) {
            log.error(e.getMessage(), e);
        }
        catch (IOException ioe) {
            log.error(ioe.getMessage(), ioe);
        }
        finally {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
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
}
