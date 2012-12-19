/**
 * Copyright (c) 2009--2010 Red Hat, Inc.
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

import org.apache.log4j.Logger;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.taskomatic.task.threaded.QueueWorker;
import com.redhat.rhn.taskomatic.task.threaded.TaskQueue;

/**
 * Worker implementation for SSH Server Push.
 */
public class SSHServerPushWorker implements QueueWorker {

    // Static SSH config
    private static String KNOWN_HOSTS = "/root/.ssh/known_hosts";
    private static String PRIVATE_KEY = "/root/.ssh/id_rsa";
    private static int R_PORT = 1234;
    private static int L_PORT = 443;

    private Logger log;
    private TaskQueue parentQueue;
    private JSch ssh;
    private String localhost;
    private Long sid;

    /**
     * Constructor.
     * @param item to work with
     * @param logger
     */
    public SSHServerPushWorker(Logger logger, Long sid) {
        this.sid = sid;
        log = logger;
        log.info("SSHServerPush -> " + sid);

        // Get localhost's FQDN
        try {
            localhost = InetAddress.getLocalHost().getCanonicalHostName();
        }
        catch (UnknownHostException e) {
            log.error(e.getMessage(), e);
        }

        // Configure ssh client
        JSch.setConfig("StrictHostKeyChecking", "yes");
        ssh = new JSch();
        try {
            ssh.setKnownHosts(KNOWN_HOSTS);
            ssh.addIdentity(PRIVATE_KEY);
        }
        catch (JSchException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * Set the parent queue.
     * @param queue
     */
    @Override
    public void setParentQueue(TaskQueue queue) {
        parentQueue = queue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        parentQueue.workerStarting();
        try {
            Server server = (Server) HibernateFactory.getSession().load(Server.class, sid);
            String host = server.getIpAddress();
            log.info("Running 'rhn_check' on: " + host);
            rhnCheck(host);
        }
        catch (Exception e) {
            log.error(e.getMessage());
        }
        finally {
            parentQueue.workerDone();
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
}
