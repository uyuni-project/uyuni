/*
 * Copyright (c) 2021 SUSE LLC
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

package com.redhat.rhn.taskomatic.task.payg;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.domain.cloudpayg.PaygSshData;
import com.redhat.rhn.taskomatic.task.payg.beans.PaygInstanceInfo;

import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Arrays;

public class PaygAuthDataExtractor {

    private static final Path PAYG_INSTANCE_INFO_JSON = Path.of("/var/cache/rhn/payg.json");
    private static final int VALIDITY_MINUTES = 11;

    private static final String CONNECTION_TIMEOUT_PROPEERRTY = "java.payg.connection_timeout";
    private static final String WAIT_RESPONSE_TIMEOUT_PROPEERRTY = "java.payg.repsonse_timeout";

    // time in milliseconds
    private static final int CONNECTION_TIMEOUT = Config.get().getInt(CONNECTION_TIMEOUT_PROPEERRTY, 5000);
    private static final int RESPONSE_TIMEOUT = Config.get().getInt(WAIT_RESPONSE_TIMEOUT_PROPEERRTY, 20000);

    private static final Logger LOG = LogManager.getLogger(PaygAuthDataExtractor.class);

    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
        .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
        .serializeNulls()
        .create();

    public enum OsSpecificExtractor {
        SLES_EXTRACTOR("SLE", "sudo python3", "script/payg_extract_repo_data.py"),
        RHEL_EXTRACTOR("RHEL", "sudo /usr/libexec/platform-python", "script/rhui_extract_repo_data.py"),
        RHEL7_EXTRACTOR("RHEL7", "sudo python", "script/rhui7_extract_repo_data.py");

        private final String osLabel;
        private final String scriptExecutor;
        private final String extractorScript;

        /**
         * Constructor
         * @param osLabelIn the lable
         * @param scriptExecutorIn the script executor
         * @param extractorScriptIn the script path
         */
        OsSpecificExtractor(String osLabelIn, String scriptExecutorIn, String extractorScriptIn) {
            this.osLabel = osLabelIn;
            this.scriptExecutor = scriptExecutorIn;
            this.extractorScript = extractorScriptIn;
        }

        /**
         * @param osLabel the os label
         * @return the extractor
         */
        public static OsSpecificExtractor forOS(String osLabel) {
            return Arrays.stream(PaygAuthDataExtractor.OsSpecificExtractor.values())
                .filter(extractor -> osLabel.equalsIgnoreCase(extractor.getOsLabel()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unable to create an extractor for OS " + osLabel));
        }

        // Getters
        /**
         * @return return the os label
         */
        public String getOsLabel() {
            return osLabel;
        }

        /**
         * @return return the extractor script
         */
        public String getExtractorScript() {
            return extractorScript;
        }

        /**
         * @return return the script executor
         */
        public String getScriptExecutor() {
            return scriptExecutor;
        }
    }

    private PaygInstanceInfo processOutput(int exitStatus, String error, String output) {
        if (exitStatus != 0 || error.length() > 0) {
            LOG.error("Exit status: {}", exitStatus);
            LOG.error("stderr:\n{}", error);
            throw new PaygDataExtractException(error);
        }
        if (output.length() == 0) {
            LOG.error("Exit status was success but no data retrieved");
            throw new PaygDataExtractException("No data retrieved from the instance.");
        }
        return GSON.fromJson(output, PaygInstanceInfo.class);
    }


    /**
     * This method will use the ssh connection data to open an ssh connection to the instance
     * and extract all authentication data and cryptographic material needed to connect to the cloud rmt servers.
     * To o that, a python script will be executed on the target instance.
     * @param instance payg ssh data connection object
     * @return Authentication data and cryptographic material to connect to cloud rmt host
     * @throws Exception
     */
    private PaygInstanceInfo extractAuthDataSSH(PaygSshData instance) throws Exception {
        Session sessionsTarget = null, sessionBastion = null;
        try {
            JSch.setConfig("StrictHostKeyChecking", "no");
            JSch sshTarget = new JSch();
            if (!StringUtils.isEmpty(instance.getKey())) {
                String authKeypassIn = instance.getKeyPassword() != null ? instance.getKeyPassword() : "";
                sshTarget.addIdentity("targetkey", instance.getKey().getBytes(), null, authKeypassIn.getBytes());
            }
            int sshPortIn = instance.getPort() != null ? instance.getPort() : 22;

            if (!StringUtils.isEmpty(instance.getBastionHost())) {
                JSch sshBastion = new JSch();
                if (!StringUtils.isEmpty(instance.getBastionKey())) {
                    String bastionAuthKeyPassIn = instance.getBastionKeyPassword() != null ?
                            instance.getBastionKeyPassword() : "";
                    sshBastion.addIdentity("bastionkey", instance.getBastionKey().getBytes(), null,
                            bastionAuthKeyPassIn.getBytes());
                }
                int bastionSshPortIn = instance.getBastionPort() != null ? instance.getBastionPort() : 22;
                sessionBastion = sshBastion.getSession(instance.getBastionUsername(), instance.getBastionHost(),
                        bastionSshPortIn);
                if (!StringUtils.isEmpty(instance.getBastionPassword())) {
                    sessionBastion.setPassword(instance.getBastionPassword());
                }
                sessionBastion.setTimeout(CONNECTION_TIMEOUT);
                sessionBastion.connect();

                int assignedPort = sessionBastion.setPortForwardingL(0, instance.getHost(), sshPortIn);
                sessionsTarget = sshTarget.getSession(instance.getUsername(), "127.0.0.1", assignedPort);
            }
            else {
                sessionsTarget = sshTarget.getSession(instance.getUsername(), instance.getHost(), sshPortIn);
            }
            if (!StringUtils.isEmpty(instance.getPassword())) {
                sessionsTarget.setPassword(instance.getPassword());
            }
            sessionsTarget.setTimeout(CONNECTION_TIMEOUT);
            sessionsTarget.connect();

            OsSpecificExtractor extractor = getOsSpecificExtractor(sessionsTarget);
            ChannelExec channel = (ChannelExec) sessionsTarget.openChannel("exec");

            try {
                channel.setCommand(extractor.getScriptExecutor());
                channel.setInputStream(PaygAuthDataExtractor.class.getResourceAsStream(extractor.getExtractorScript()));

                InputStream stdout = channel.getInputStream();
                InputStream stderr = channel.getErrStream();
                channel.connect();

                // read all command output, otherwise channel will never be closed
                StringBuilder output = getCommandOutput(stdout);
                StringBuilder error = getCommandOutput(stderr);

                waitForChannelClosed(channel);
                int exitStatus = channel.getExitStatus();

                return processOutput(exitStatus, error.toString(), output.toString());
            }
            finally {
                if (channel != null) {
                    try {
                        channel.disconnect();
                    }
                    catch (Exception e) {
                        LOG.error("Error disconnection jsch session", e);
                    }
                }
            }

        }
        finally {
            if (sessionBastion != null) {
                try {
                    sessionBastion.disconnect();
                }
                catch (Exception e) {
                    LOG.error("Error disconnection jsch session", e);
                }
            }
            if (sessionsTarget != null) {
                try {
                    sessionsTarget.disconnect();
                }
                catch (Exception e) {
                    LOG.error("Error disconnection jsch session", e);
                }
            }
        }
    }

    /**
     * This method will use the local instance
     * and extract all authentication data and cryptographic material needed to connect to the cloud rmt servers.
     * To do that, a python script will be executed on this instance.
     * (Just public to be able to mock this method in tests)
     * @return Authentication data and cryptographic material to connect to cloud rmt host
     */
    protected PaygInstanceInfo extractAuthDataLocal() {
        try (BufferedReader json = Files.newBufferedReader(PAYG_INSTANCE_INFO_JSON)) {
            PaygInstanceInfo instanceInfo = GSON.fromJson(json, PaygInstanceInfo.class);
            if (Duration.between(instanceInfo.getTimestamp(), Instant.now()).toMinutes() > VALIDITY_MINUTES) {
                throw new PaygDataExtractException("Local PAYG Instance info is outdated.");
            }

            return instanceInfo;
        }
        catch (Exception ex) {
            throw new PaygDataExtractException("Error retrieving data from local instance", ex);
        }
    }

    /**
     * This method will use the ssh connection data to open an ssh connection to the instance
     * and extract all authentication data and cryptographic material needed to connect to the cloud rmt servers.
     * To o that, a python script will be executed on the target instance.
     * @param instance payg ssh data connection object
     * @return Authentication data and cryptographic material to connect to cloud rmt host
     * @throws Exception
     */
    public PaygInstanceInfo extractAuthData(PaygSshData instance) throws Exception {
        if (instance.isSUSEManagerPayg()) {
            return extractAuthDataLocal();
        }

        return extractAuthDataSSH(instance);
    }

    private OsSpecificExtractor getOsSpecificExtractor(Session sessionsTarget) throws JSchException, IOException {
        ChannelExec channel = (ChannelExec) sessionsTarget.openChannel("exec");
        try {
            channel.setCommand("/bin/sh ");
            channel.setInputStream(PaygAuthDataExtractor.class.getResourceAsStream("script/detect_os.sh"));

            //channel.setInputStream(null);
            InputStream stdout = channel.getInputStream();
            InputStream stderr = channel.getErrStream();
            channel.connect();

            // read all command output, otherwise channel will never be closed
            StringBuilder output = getCommandOutput(stdout);
            StringBuilder error = getCommandOutput(stderr);

            waitForChannelClosed(channel);
            int exitStatus = channel.getExitStatus();

            if (exitStatus != 0 || error.length() > 0) {
                LOG.error("Exit status: {}", exitStatus);
                LOG.error("stderr:\n{}", error);
                throw new PaygDataExtractException(error.toString());
            }
            if (output.length() == 0) {
                LOG.error("Exit status was success but no data retrieved");
                throw new PaygDataExtractException("No data retrieved from the instance.");
            }
            return OsSpecificExtractor.forOS(output.toString().trim());
        }
        finally {
            if (channel != null) {
                try {
                    channel.disconnect();
                }
                catch (Exception e) {
                    LOG.error("Error disconnection jsch session", e);
                }
            }
        }
    }
    private StringBuilder getCommandOutput(InputStream channelStdout) throws IOException {
        StringBuilder output = new StringBuilder();
        InputStreamReader stream = new InputStreamReader(channelStdout);
        char[] buffer = new char[128];
        int read;
        while ((read = stream.read(buffer, 0, buffer.length)) >= 0) {
            output.append(buffer, 0, read);
        }
        return output;
    }

    private void waitForChannelClosed(ChannelExec channel) {
        long startTime = System.currentTimeMillis();
        while (!channel.isClosed()) {
            // Check to see if we have been waiting too long, converts ms to minutes
            long elapsedTimeInMillis = System.currentTimeMillis() - startTime;
            if (elapsedTimeInMillis > RESPONSE_TIMEOUT) {
                LOG.error("Task took too long to complete");
            }
            try {
                Thread.sleep(RESPONSE_TIMEOUT / 10);
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                // Should not happen
                LOG.error("error when waiting for channel to be closed", e);
            }
        }
    }
}
