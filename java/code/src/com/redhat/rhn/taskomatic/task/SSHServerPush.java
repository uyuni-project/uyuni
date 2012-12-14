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
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
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

    // Paths
    private static String KNOWN_HOSTS = "/root/.ssh/known_hosts";
    private static String PRIVATE_KEY = "/root/.ssh/id_rsa";

    // Ports
    private static int R_PORT = 1234;
    private static int L_PORT = 443;

    // Setup these in init()
    private String localhost;
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
     * Init this instance, do this only once per job execution.
     */
    private void init() {
        // Get local address
        try {
            localhost = InetAddress.getLocalHost().getCanonicalHostName();
        } catch (UnknownHostException e) {
            log.error(e.getMessage(), e);
        }

        // Configure ssh client
        JSch.setConfig("StrictHostKeyChecking", "yes");
        ssh = new JSch();
        try {
            ssh.setKnownHosts(KNOWN_HOSTS);
            ssh.addIdentity(PRIVATE_KEY);
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
            session.setPortForwardingR(R_PORT, localhost, L_PORT);

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
            Throwable cause = e.getCause();
            if (cause instanceof NoRouteToHostException ||
                    cause instanceof ConnectException) {
                log.warn(cause.getMessage());
            } else {
                log.error(e.getMessage(), e);
            }
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
