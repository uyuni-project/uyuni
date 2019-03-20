/**
 * Copyright (c) 2013 SUSE LLC
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
package com.redhat.rhn.taskomatic.task.sshpush;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import com.redhat.rhn.common.CommonConstants;
import com.redhat.rhn.common.conf.ConfigDefaults;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.frontend.dto.ServerPath;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.task.checkin.SystemSummary;
import com.redhat.rhn.taskomatic.task.threaded.QueueWorker;
import com.redhat.rhn.taskomatic.task.threaded.TaskQueue;

/**
 * Worker implementation for SSH Server Push.
 */
public class SSHPushWorker implements QueueWorker {

    // Static configuration
    private static final String SSH_PUSH_TUNNEL = "ssh-push-tunnel";
    private static final String KNOWN_HOSTS = "/root/.ssh/known_hosts";
    private static final String PRIVATE_KEY = "/root/.ssh/id_susemanager";
    private static final String RHN_CHECK = "/usr/sbin/rhn_check";
    private static final int SSL_PORT = 443;

    // Message text used for error detection
    private static final String PORT_FORWARDING_FAILED = "remote port forwarding failed";

    // Config keys
    private static final String CONFIG_KEY_USE_HOSTNAME = "ssh_push_use_hostname";
    private static final String CONFIG_KEY_TASK_TIMEOUT = "ssh_push_task_timeout";

    // Client and proxy hostnames
    private String proxy;
    private String client;

    private String sudoUser;
    private Logger log;
    private SystemSummary system;
    private int remotePort;
    private String localhost;
    private TaskQueue parentQueue;
    private JSch ssh;

    // Maximum wait time for a task in minutes
    private int maxWait;

    /**
     * Constructor.
     * @param logger Logger for this instance
     * @param port High port to use for remote port forwarding
     * @param s the system to work with
     */
    public SSHPushWorker(Logger logger, int port, SystemSummary s) {
        remotePort = port;
        system = s;

        // Remember that we are talking to this system
        SSHPushDriver.getCurrentSystems().add(system);

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
            ssh.addIdentity(PRIVATE_KEY, StringUtils.EMPTY);
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
     * Get the primary proxy's IP address or hostname (in case the ssh_push_use_hostname
     * option is set to true) for a given server or null in case there is no proxy involved.
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
                       if (Config.get().getBoolean(CONFIG_KEY_USE_HOSTNAME)) {
                           proxyHost = path.getHostname();
                       }
                       else {
                           // Load the proxy server object to determine the IP address
                           Server proxyServer = HibernateFactory.getSession()
                                   .load(Server.class, path.getId());
                           proxyHost = proxyServer.getIpAddress();
                       }

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
        try {
            parentQueue.workerStarting();

            // Get the client's hostname or address
            Server server = HibernateFactory.getSession().load(
                    Server.class, system.getId());
            if (Config.get().getBoolean(CONFIG_KEY_USE_HOSTNAME)) {
                client = server.getHostname();
            }
            else {
                client = server.getIpAddress();
            }

            if (!StringUtils.isEmpty(Config.get()
                    .getString(ConfigDefaults.CONFIG_KEY_SUDO_USER))) {
                sudoUser = Config.get().getString(ConfigDefaults.CONFIG_KEY_SUDO_USER);
            }

            // Get the server's primary proxy (if any)
            proxy = getPrimaryProxy(server);

            maxWait = Config.get().getInt(CONFIG_KEY_TASK_TIMEOUT);

            if (log.isDebugEnabled()) {
                String proxySuffix = proxy == null ? "" : " (proxy: " + proxy + ")";
                log.debug("Running 'rhn_check': " + client + proxySuffix);
            }

            // Connect to the client
            rhnCheck();
            HibernateFactory.commitTransaction();
        }
        catch (Exception e) {
            log.error(e.getMessage());
            HibernateFactory.rollbackTransaction();
        }
        finally {
            parentQueue.workerDone();
            HibernateFactory.closeSession();

            // Finished talking to this system
            SSHPushDriver.getCurrentSystems().remove(system);
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
            session = ssh.getSession(sudoUser != null ? sudoUser : CommonConstants.ROOT,
                    proxy != null ? proxy : client);
            Optional<String> hostKeyType = hostKeyType(session.getHost());
            if (hostKeyType.isPresent()) {
                session.setConfig("server_host_key", hostKeyType.get());
            }
            session.connect();

            // Setup port forwarding if needed
            if (proxy == null && system.getContactMethodLabel().equals(SSH_PUSH_TUNNEL)) {
                session.setPortForwardingR(remotePort, localhost, SSL_PORT);
            }

            // Init channel and streams
            channel = (ChannelExec) session.openChannel("exec");
            channel.setCommand(getCommand());
            channel.setInputStream(null);
            InputStream stdout = channel.getInputStream();
            InputStream stderr = channel.getErrStream();

            // Connect and wait for the exit status
            channel.connect();
            waitForChannelClosed(channel);
            int exitStatus = channel.getExitStatus();

            if (exitStatus != 0 || log.isTraceEnabled()) {
                log.error("Exit status: " + exitStatus + " [" + client + "]");
                log.error("stdout:\n" + IOUtils.toString(stdout));
                log.error("stderr:\n" + IOUtils.toString(stderr));
            }
            else {
                log.debug("Exit status: " + exitStatus + " [" + client + "]");
            }
        }
        catch (JSchException e) {
            Throwable cause = e.getCause();
            if (cause instanceof NoRouteToHostException ||
                    cause instanceof ConnectException) {
                log.warn(cause.getMessage() + " [" + client + "]");
            }
            // Check if a tunnel is currently open
            else if (e.getMessage().startsWith(PORT_FORWARDING_FAILED)) {
                log.info("Skipping " + client + ", tunnel seems to be busy");
                if (log.isDebugEnabled()) {
                    log.debug(e.getMessage());
                }
            }
            else {
                log.error(e.getMessage() + " [" + client + "]", e);
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
     * Lookup the host key type to use for a given hostname or ip address (in known_hosts).
     * @param host the hostname or ip address to lookup
     * @return the host key type or empty optional
     */
    private Optional<String> hostKeyType(String host) {
        HostKeyRepository hostKeyRepo = ssh.getHostKeyRepository();
        HostKey[] hostKeys = hostKeyRepo.getHostKey();
        if (hostKeys != null) {
            if (log.isDebugEnabled()) {
                log.debug("Looking up host key in: " +
                        hostKeyRepo.getKnownHostsRepositoryID());
            }

            for (HostKey hostKey : hostKeys) {
                for (String hostString: hostKey.getHost().split(",")) {
                    if (hostString.matches(host)) {
                        if (log.isDebugEnabled()) {
                            log.debug("Host key type for " + hostString + ": " +
                                    hostKey.getType());
                        }
                        return Optional.of(hostKey.getType());
                    }
                }
            }
        }
        log.warn("Unknown host: " + host);
        return Optional.empty();
    }

    /**
     * Construct command to be run via ssh on either the proxy or the client.
     * @return command as string
     */
    private String getCommand() {
        String cmd = RHN_CHECK;
        if (sudoUser != null) {
            cmd = "sudo " + cmd;
        }
        if (proxy != null) {
            StringBuilder sb = new StringBuilder("ssh");
            if (sudoUser != null) {
                sb.append(" -l ");
                sb.append(sudoUser);
            }
            sb.append(" -i ");
            sb.append("~/.ssh/id_susemanager");
            sb.append(" ");
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
            sb.append(cmd);
            cmd = sb.toString();
        }
        if (log.isDebugEnabled()) {
            log.debug("Command: " + cmd);
        }
        return cmd;
    }

    /**
     * Wait for a given {@link ChannelExec} to be closed.
     * Will close channel if maxWait is set and exceeded.
     * @param channel the channel
     */
    private void waitForChannelClosed(ChannelExec channel) {
        long startTime = System.currentTimeMillis();
        while (!channel.isClosed()) {
            // Check to see if we have been waiting too long, converts ms to minutes
            long elapsedTimeInMins =
                    TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - startTime);
            if (maxWait > 0 && elapsedTimeInMins > maxWait) {
                log.error("Task took too long to complete");
                channel.disconnect();
            }
            try {
                Thread.sleep(1000);
            }
            catch (InterruptedException e) {
                // Should not happen
            }
        }
    }
}
