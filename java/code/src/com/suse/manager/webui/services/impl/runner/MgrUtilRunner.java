/**
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

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.suse.salt.netapi.calls.RunnerCall;

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

        @SerializedName("returncode")
        private int returnCode;
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
     * Remove a Salt key from the "Rejected Keys" category.
     * @param minionId the minionId to look for in "Rejected Keys"
     * @return the execution result
     */
    public static RunnerCall<ExecResult> deleteRejectedKey(String minionId) {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("minion", minionId);
        RunnerCall<ExecResult> call =
                new RunnerCall<>("mgrutil.delete_rejected_key", Optional.of(args),
                        new TypeToken<ExecResult>() { });
        return call;
    }

    /**
     * Generate a ssh key pair.
     * @param path path where to generate the keys
     * @return the execution result
     */
    public static RunnerCall<ExecResult> generateSSHKey(String path) {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("path", path);
        RunnerCall<ExecResult> call =
                new RunnerCall<>("mgrutil.ssh_keygen", Optional.of(args),
                        new TypeToken<ExecResult>() { });
        return call;
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
        RunnerCall<ExecResult> call =
                new RunnerCall<>("mgrutil.chain_ssh_cmd", Optional.of(args),
                        new TypeToken<ExecResult>() {
                        });
        return call;
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
        RunnerCall<Map<Boolean, String>> call =
                new RunnerCall<>("mgrutil.move_minion_uploaded_files", Optional.of(args),
                        new TypeToken<Map<Boolean, String>>() { });
        return call;
    }

}
