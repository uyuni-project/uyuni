/**
 * Copyright (c) 2013 Novell
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
package com.redhat.rhn.taskomatic.task.serverpush;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.frontend.dto.ServerPath;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.task.threaded.QueueWorker;
import com.redhat.rhn.taskomatic.task.threaded.TaskQueue;

/**
 * Worker implementation for SSH Server Push.
 */
public class SSHServerPushWorker implements QueueWorker {

    // Static configuration
    private static final String SSH_PUSH_TUNNEL = "ssh-push-tunnel";
    private static final String KNOWN_HOSTS = "/root/.ssh/known_hosts";
    private static final String PRIVATE_KEY = "/root/.ssh/id_susemanager";
    private static final String REMOTE_USER = "root";
    private static final String RHN_CHECK = "rhn_check";
    private static final int SSL_PORT = 443;

    // Message text used for error detection
    private static final String PORT_FORWARDING_FAILED = "remote port forwarding failed";

    // Client and proxy hostnames
    private String proxy;
    private String client;

    private Logger log;
    private SSHServerPushSystem system;
    private int remotePort;
    private String localhost;
    private TaskQueue parentQueue;
    private JSch ssh;

    /**
     * Constructor.
     * @param logger Logger for this instance
     * @param port High port to use for remote port forwarding
     * @param s the system to work with
     */
    public SSHServerPushWorker(Logger logger, int port, SSHServerPushSystem s) {
        remotePort = port;
        system = s;

        // Init logging
        log = logger;
        if (log.isDebugEnabled()) {
            log.debug("SSHServerPush -> " + system.getName());
        }

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
     * {@inheritDoc}
     */
    @Override
    public void setParentQueue(TaskQueue queue) {
        parentQueue = queue;
    }

    /**
     * Get the primary proxy's hostname for a given server or null.
     * @return primary proxy for server or null
     */
    private String getPrimaryProxy(Server server) {
       String proxyHost = null;
       DataResult<?> retval = null;

       // Get connection path
       if (server != null) {
           retval = SystemManager.getConnectionPath(server.getId());

           // Loop through the proxy path and return 1st in chain
           if (retval != null) {
               for (Iterator<?> itr = retval.iterator(); itr.hasNext();) {
                   ServerPath path = (ServerPath) itr.next();
                   if (path.getPosition().toString().equals("1")) {
                       proxyHost = path.getHostname();
                       break;
                   }
               }
           }
       }

       return proxyHost;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void run() {
        parentQueue.workerStarting();
        try {
            // Get the client's address
            Server server = (Server) HibernateFactory.getSession().load(
                    Server.class, system.getId());
            client = server.getIpAddress();
            if (log.isDebugEnabled()) {
                log.debug("Running 'rhn_check' for: " + client);
            }

            // Get the server's primary proxy (if any)
            proxy = getPrimaryProxy(server);

            // Connect to the client
            rhnCheck();
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
    private void rhnCheck() {
        Session session = null;
        ChannelExec channel = null;
        try {
            // Setup session
            session = ssh.getSession(REMOTE_USER, proxy != null ? proxy : client);
            session.connect();

            // Setup port forwarding if needed
            if (proxy == null && system.getContactMethodLabel().equals(SSH_PUSH_TUNNEL)) {
                session.setPortForwardingR(remotePort, localhost, SSL_PORT);
            }

            // Open channel
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(getCommand());

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
            // Check exit status for errors
            if (exitStatus != 0) {
                log.error("Exit status: " + exitStatus);
                log.error("stdout:\n" + sb.toString());
            }
            else if (log.isTraceEnabled()) {
                log.trace("stdout:\n" + sb.toString());
            }
        }
        catch (JSchException e) {
            Throwable cause = e.getCause();
            if (cause instanceof NoRouteToHostException ||
                    cause instanceof ConnectException) {
                log.warn(cause.getMessage());
            }
            // Check if a tunnel is currently open
            else if (e.getMessage().startsWith(PORT_FORWARDING_FAILED)) {
                log.info("Skipping " + system.getName() + ", tunnel seems to be busy");
                if (log.isDebugEnabled()) {
                    log.debug(e.getMessage());
                }
            }
            else {
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
     * Construct command to be run via ssh on either the proxy or the client.
     * @return command as string
     */
    private String getCommand() {
        String cmd = RHN_CHECK;
        if (proxy != null) {
            StringBuilder sb = new StringBuilder("ssh ");
            sb.append(client);
            if (system.getContactMethodLabel().equals(SSH_PUSH_TUNNEL)) {
                sb.append(" -R ");
                sb.append(remotePort);
                sb.append(":");
                sb.append(proxy);
                sb.append(":");
                sb.append(SSL_PORT);
            }
            sb.append(" ");
            sb.append(RHN_CHECK);
            cmd = sb.toString();
        }
        return cmd;
    }
}
