/*
 * Copyright (c) 2017 SUSE LLC
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
package com.suse.manager.webui.services.impl.runner;

import com.suse.manager.ssl.SSLCertPair;
import com.suse.salt.netapi.calls.RunnerCall;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Runner calls specific to SUSE Manager.
 */
public class MgrUtilRunner {

    private MgrUtilRunner() { }

    /**
     * Command execution result.
     */
    public static class ExecResult {

        @SerializedName("retcode")
        protected int returnCode;
        @SerializedName("stdout")
        private String stdout;
        @SerializedName("stderr")
        private String stderr;

        /**
         * @return command return code
         */
        public int getReturnCode() {
            return returnCode;
        }

        /**
         * @return command stdout
         */
        public String getStdout() {
            return stdout;
        }

        /**
         * @return command stderr
         */
        public String getStderr() {
            return stderr;
        }

        /**
         * Create an empty result with return code 0.
         * @return a result with return code 0.
         */
        public static ExecResult success() {
            ExecResult res = new ExecResult();
            res.returnCode = 0;
            return res;
        }
    }

    /**
     * ssh-keygen result
     */
    public static class SshKeygenResult extends ExecResult {
        private String key;

        @SerializedName("public_key")
        private String publicKey;

        /**
         * Create a result with key pair, mostly for testing purpose
         *
         * @param keyIn the ssh key
         * @param pubKeyIn the ssh public key
         */
        public SshKeygenResult(String keyIn, String pubKeyIn) {
            key = keyIn;
            publicKey = pubKeyIn;
            returnCode = 0;
        }

        /**
         * Create an empty result with return code 0
         * @return a result with return code 0.
         */
        public static SshKeygenResult success() {
            return new SshKeygenResult(null, null);
        }
        /**
         * @return value of key
         */
        public String getKey() {
            return key;
        }

        /**
         * @return value of publicKey
         */
        public String getPublicKey() {
            return publicKey;
        }
    }

    /**
     * Result of removing a hostname from the ~/.ssh/known_hosts file
     */
    public static class RemoveKnowHostResult {

        @SerializedName("status")
        private String status;

        @SerializedName("comment")
        private String comment;

        /**
         * Only needed for unit tests.
         * @param statusIn status
         * @param commentIn comment
         */
        public RemoveKnowHostResult(String statusIn, String commentIn) {
            this.status = statusIn;
            this.comment = commentIn;
        }

        /**
         * @return status to get
         */
        public String getStatus() {
            return status;
        }

        /**
         * @return comment to get
         */
        public String getComment() {
            return comment;
        }
    }

    /**
     * Remove a Salt key from the "Rejected Keys" category.
     * @param minionId the minionId to look for in "Rejected Keys"
     * @return the execution result
     */
    public static RunnerCall<ExecResult> deleteRejectedKey(String minionId) {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("minion", minionId);
        return new RunnerCall<>("mgrutil.delete_rejected_key", Optional.of(args), new TypeToken<>() { });
    }

    /**
     * Generate a ssh key pair.
     * @param path path where to generate the keys or null to return them
     * @param pubkeyCopy create a copy of the pubkey at this place. Set NULL when no copy should be created
     * @return the execution result
     */
    public static RunnerCall<SshKeygenResult> generateSSHKey(String path, String pubkeyCopy) {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("path", path);
        args.put("pubkeycopy", pubkeyCopy);
        return new RunnerCall<>("mgrutil.ssh_keygen", Optional.of(args), new TypeToken<>() { });
    }

    /**
     * Removes a hostname from a user's ~/.ssh/known_hosts file.
     * @param user the user for which to remove the hostname
     * @param hostname hotname to remove
     * @param port port to remove
     * @return the execution result
     */
    public static RunnerCall<RemoveKnowHostResult> removeSSHKnowHost(String user, String hostname, int port) {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("user", user);
        args.put("hostname", hostname);
        args.put("port", port);
        return new RunnerCall<>("mgrutil.remove_ssh_known_host", Optional.of(args), new TypeToken<>() { });
    }

    /**
     * Connect through the given hosts to execute the command
     * on the last host in the chain.
     * @param hosts the hosts through which to connect
     * @param clientKey the key to auth on the first host
     * @param proxyKey the key to auth on subsequent hosts
     * @param user the user used to connect
     * @param options SSH options
     * @param command the command to execute
     * @param outputfile the file to which to dump the command stdout
     * @return the execution result
     */
    public static RunnerCall<ExecResult> chainSSHCommand(List<String> hosts,
                                                         String clientKey,
                                                         String proxyKey,
                                                         String user,
                                                         Map<String, String> options,
                                                         String command,
                                                         String outputfile) {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("hosts", hosts);
        args.put("clientkey", clientKey);
        args.put("proxykey", proxyKey);
        args.put("user", user);
        args.put("options", options);
        args.put("command", command);
        args.put("outputfile", outputfile);
        return new RunnerCall<>("mgrutil.chain_ssh_cmd", Optional.of(args), new TypeToken<>() { });
    }

    /**
     * Moves a directory from
     * {@code /var/cache/salt/master/minions/[minion]/files/[dirToMove]}
     * to {@code scapStorePath}
     *
     * @param minion minion id
     * @param dirToMove path of directory to move. Relative to Salt's cache dir.
     * @param basePath absolute base path where to action dir is located
     * @param actionPath relative path of the action dir
     * @return a {@link RunnerCall} to pass to the SaltClient
     */
    public static RunnerCall<Map<Boolean, String>> moveMinionUploadedFiles(
            String minion, String dirToMove, String basePath, String actionPath) {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("minion", minion);
        args.put("dirtomove", dirToMove);
        args.put("basepath", basePath);
        args.put("actionpath", actionPath);
        return new RunnerCall<>("mgrutil.move_minion_uploaded_files", Optional.of(args), new TypeToken<>() { });
    }

    /**
     * Check SSL certificates before deploying them.
     *
     * @param rootCA root CA used to sign the SSL certificate in PEM format
     * @param intermediateCAs intermediate CAs used to sign the SSL certificate in PEM format
     * @param serverCrtKey server CRT and Key pair
     * @return the certificate and key to deploy
     */
    public static RunnerCall<Map<String, String>> checkSSLCert(String rootCA, SSLCertPair serverCrtKey,
                                                            List<String> intermediateCAs) {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("root_ca", rootCA);
        args.put("server_crt", serverCrtKey.getCertificate());
        args.put("server_key", serverCrtKey.getKey());
        args.put("intermediate_cas", intermediateCAs);
        return new RunnerCall<>("mgrutil.check_ssl_cert", Optional.of(args), new TypeToken<>() { });
    }

    /**
     * Call 'mgrutil.select_minions'
     * @param target return the minions matching the target
     * @param targetType type of target
     * @return list of matching minions
     */
    public static RunnerCall<List<String>> selectMinions(String target, String targetType) {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("target", target);
        args.put("target_type", targetType);
        return new RunnerCall<>("mgrutil.select_minions", Optional.of(args), new TypeToken<List<String>>() { });
    }
}
