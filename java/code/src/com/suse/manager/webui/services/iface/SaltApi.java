/*
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
package com.suse.manager.webui.services.iface;

import com.redhat.rhn.domain.server.MinionServer;

import com.suse.manager.ssl.SSLCertPair;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.services.impl.runner.MgrK8sRunner;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;
import com.suse.manager.webui.utils.ElementCallJson;
import com.suse.manager.webui.utils.gson.BootstrapParameters;
import com.suse.manager.webui.utils.salt.custom.ScheduleMetadata;
import com.suse.manager.webui.utils.salt.custom.SumaUtil;
import com.suse.manager.webui.utils.salt.custom.SystemInfo;
import com.suse.salt.netapi.calls.LocalAsyncResult;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.SaltUtil;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.calls.runner.Jobs;
import com.suse.salt.netapi.calls.wheel.Key;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.datatypes.target.Target;
import com.suse.salt.netapi.errors.GenericError;
import com.suse.salt.netapi.event.EventStream;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.results.SSHResult;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Interface for interacting with salt.
 */
public interface SaltApi {

    /**
     * Sync the channels of a list of minions
     * @param minionIds of the targets.
     * @throws SaltException if anything goes wrong.
     */
    void deployChannels(List<String> minionIds) throws SaltException;

    /**
     * Get information about all containers running in a Kubernetes cluster.
     * @param kubeconfig path to the kubeconfig file
     * @param context kubeconfig context to use
     * @return a list of containers
     */
    Optional<List<MgrK8sRunner.Container>> getAllContainers(String kubeconfig, String context);

    /**
     * Using a RunnerCall, store given contents to given path and set the mode, so that SSH likes it
     * (read-write for owner, nothing for others).
     *
     * @param path the path where key will be stored
     * @param contents the contents of the key (PEM format)
     * @throws IllegalStateException if something goes wrong during the operation, or if given path is not absolute
     */
    void storeSshKeyFile(Path path, String contents);

    /**
     * Match minions synchronously using a compound matcher.
     * @param target compound matcher
     * @return list of minion ids
     */
    List<String> matchCompoundSync(String target);

    /**
     * Remove given file using RunnerCall
     *
     * @param path the path of file to be removed
     * @throws IllegalStateException if the given path is not absolute
     * @return Optional with true if the file deletion succeeded.
     */
    Optional<Boolean> removeFile(Path path);

    /**
     * Create a directory using RunnerCall
     *
     * @param path the absolute path of the directory
     * @param modeString the desired mode
     * @return Optional with true if the directory was created
     */
    Optional<Boolean> mkDir(Path path, String modeString);

    /**
     * Copy given file using RunnerCall
     *
     * @param src the source path of file to be removed
     * @param dst the destination path of file to be removed
     * @throws IllegalStateException if the given path is not absolute
     * @return Optional with true if the file deletion succeeded.
     */
    Optional<Boolean> copyFile(Path src, Path dst);

    /**
     * Performs an test.echo on a target set of minions for checkIn purpose.
     * @param targetIn the target
     * @return the LocalAsyncResult of the test.echo call
     * @throws SaltException if we get a failure from Salt
     */
    Optional<LocalAsyncResult<String>> checkIn(MinionList targetIn) throws SaltException;

    /**
     * Apply util.systeminfo state on the specified minion list
     * @param minionTarget minion list
     */
    void updateSystemInfo(MinionList minionTarget);

    /**
     * Apply util.systeminfo_full state on the specified minion and wait for the result
     * @param minion minion id
     * @return the SystemInfo result
     */
    Optional<SystemInfo> getSystemInfoFull(String minion);

    /**
     * Call sumautil.instance_flavor.
     * @param minionId of the target minion.
     * @return PublicCloudInstanceFlavor result
     */
    SumaUtil.PublicCloudInstanceFlavor getInstanceFlavor(String minionId);

    /**
     * Store the files uploaded by a minion to the SCAP storage directory.
     * @param minion the minion
     * @param uploadDir the uploadDir
     * @param actionId the action id
     * @return a map with one element: @{code true} -> scap store path,
     * {@code false} -> err message
     *
     */
    Map<Boolean, String> storeMinionScapFiles(MinionServer minion, String uploadDir, Long actionId);

    /**
     * Return show highstate result.
     * @param minionId of the target minion.
     * @return show highstate result.
     * @throws SaltException if anything goes wrong.
     */
    Map<String, Result<Object>> showHighstate(String minionId) throws SaltException;

    /**
     * Call the custom mgrutil.ssh_keygen runner if the key files are not present.
     *
     * @param path of the key files
     * @param pubkeyCopy create a copy of the pubkey at this place. Set NULL when no copy should be created
     * @return the result of the runner call as a map
     */
    Optional<MgrUtilRunner.SshKeygenResult> generateSSHKey(String path, String pubkeyCopy);

    /**
     * Chain ssh calls over one or more hops to run a command on the last host in the chain.
     * This calls the mgrutil.chain_ssh_command runner.
     *
     * @param hosts a list of hosts, where the last one is where
     *              the command will be executed
     * @param clientKey the ssh key to use to connect to the first host
     * @param proxyKey the ssh key path to use for the rest of the hosts
     * @param user the user
     * @param options ssh options
     * @param command the command to execute
     * @param outputfile the file to which to dump the command stdout
     * @return the execution result
     */
    Optional<MgrUtilRunner.ExecResult> chainSSHCommand(List<String> hosts, String clientKey, String proxyKey,
                                                       String user, Map<String, String> options, String command,
                                                       String outputfile);

    /**
     * Removes a hostname from the Salt ~/.ssh/known_hosts file.
     * @param hostname the hostname to remote
     * @return the result of the runner call
     */
    Optional<MgrUtilRunner.RemoveKnowHostResult> removeSaltSSHKnownHost(String hostname);

    /**
     * Removes a hostname from the Salt ~/.ssh/known_hosts file.
     * @param hostname the hostname to remote
     * @param port the port of the host to remove
     * @return the result of the runner call
     */
    Optional<MgrUtilRunner.RemoveKnowHostResult> removeSaltSSHKnownHost(String hostname, int port);

    /**
     * Call 'saltutil.sync_grains' to sync the grains to the target minion(s).
     * @param minionList minion list
     */
    void syncGrains(MinionList minionList);

    /**
     * Call 'saltutil.sync_modules' to sync the grains to the target minion(s).
     * @param minionList minion list
     */
    void syncModules(MinionList minionList);

    /**
     * Call 'saltutil.sync_all' to sync everything to the target minion(s).
     * @param minionList minion list
     */
    void syncAll(MinionList minionList);

    /**
     * call salt test.ping
     * @param minionId id of the target minion
     * @return true
     */
    Optional<Boolean> ping(String minionId);

    /**
     * Return the stream of events happening in salt.
     *
     * @return the event stream
     */
    EventStream getEventStream();

    /**
     * Delete a given minion's key.
     *
     * @param minionId id of the minion
     */
    void deleteKey(String minionId);

    /**
     * Accept all keys matching the given pattern
     *
     * @param match a pattern for minion ids
     */
    void acceptKey(String match);

    /**
     * Reject a given minion's key.
     *
     * @param minionId id of the minion
     */
    void rejectKey(String minionId);

    /**
     * Get the specified grains for a given minion.
     * @deprecated this function is too general and should be replaced by more specific functionality.
     * @param minionId id of the target minion
     * @param type  class type, result should be parsed into
     * @param grainNames list of grains names
     * @param <T> Type result should be parsed into
     * @return Optional containing the grains parsed into specified type
     */
    @Deprecated
    <T> Optional<T> getGrains(String minionId, TypeToken<T> type, String... grainNames);

    /**
     * Get the grains for a given minion.
     *
     * @deprecated this function is too general and should be replaced by more specific functionality.
     * @param minionId id of the target minion
     * @return map containing the grains
     */
    @Deprecated
    Optional<Map<String, Object>> getGrains(String minionId);

    /**
     * Gets a minion's master hostname.
     *
     * @param minionId the minion id
     * @return the master hostname
     */
    Optional<String> getMasterHostname(String minionId);

    /**
     * Run a remote command on a given minion.
     *
     * @param target the target
     * @param cmd the command
     * @return the output of the command
     */
    Map<String, Result<String>> runRemoteCommand(MinionList target, String cmd);

    /**
     * Delete a Salt key from the "Rejected Keys" category using the mgrutil runner.
     *
     * @param minionId the minionId to look for in "Rejected Keys"
     * @return the result of the runner call as a map
     */
    Optional<MgrUtilRunner.ExecResult> deleteRejectedKey(String minionId);

    /**
     * Get the minion keys from salt with their respective status.
     *
     * @return the keys with their respective status as returned from salt
     */
    Key.Names getKeys();

    /**
     * Generate a key pair for the given id and accept the public key.
     *
     * @param id the id to use
     * @param force set true to overwrite an already existing key
     * @return the generated key pair
     */
    Key.Pair generateKeysAndAccept(String id, boolean force);

    /**
     * For a given minion id check if there is a key in any of the given status. If no status is given as parameter,
     * all the available status are considered.
     *
     * @param id the id to check for
     * @param statusIn array of key status to consider
     * @return true if there is a key with the given id, false otherwise
     */
    boolean keyExists(String id, SaltService.KeyStatus... statusIn);

    /**
     * Get the minion keys from salt with their respective status and fingerprint.
     *
     * @return the keys with their respective status and fingerprint as returned from salt
     */
    Key.Fingerprints getFingerprints();

    /**
     * Remove SUSE Manager specific configuration from a Salt minion.
     *
     * @param minion the minion.
     * @param timeout operation timeout
     * @return list of error messages or empty if no error
     */
    Optional<List<String>> cleanupMinion(MinionServer minion, int timeout);

    /**
     * Bootstrap a system using salt-ssh.
     *
     * @param parameters - bootstrap parameters
     * @param bootstrapMods - state modules to be applied during the bootstrap
     * @param pillarData - pillar data used salt-ssh call
     * @throws SaltException if something goes wrong during command execution or
     * during manipulation the salt-ssh roster
     * @return the result of the underlying ssh call for given host
     */
    Result<SSHResult<Map<String, State.ApplyResult>>> bootstrapMinion(
            BootstrapParameters parameters, List<String> bootstrapMods,
            Map<String, Object> pillarData) throws SaltException;


    /**
     * Synchronously executes a salt function on a single minion.
     * If a SaltException is thrown, re-throw a RuntimeException.
     *
     * @param call salt function to call
     * @param minionId minion id to target
     * @param <R> result type of the salt function
     * @return Optional holding the result of the function
     * or empty if the minion did not respond.
     */
    <R> Optional<R> callSync(LocalCall<R> call, String minionId);

    /**
     * Run a remote command on a given minion asynchronously.
     * @param target the target
     * @param cmd the command to execute
     * @param cancel a future used to cancel waiting on return events
     * @return a map holding a {@link CompletionStage}s for each minion
     */
    Map<String, CompletionStage<Result<String>>> runRemoteCommandAsync(
            MinionList target, String cmd, CompletableFuture<GenericError> cancel);

    /**
     * Returns the currently running jobs on the target
     *
     * @param target the target
     * @return list of running jobs
     */
    Map<String, Result<List<SaltUtil.RunningInfo>>> running(MinionList target);

    /**
     * Return the result for a jobId
     *
     * @param jid the job id
     * @return map from minion to result
     */
    Optional<Jobs.Info> listJob(String jid);

    /**
     * Match the given target expression asynchronously.
     * @param target the target expression
     * @param cancel  a future used to cancel waiting on return events
     * @return a map holding a {@link CompletionStage}s for each minion
     */
    Map<String, CompletionStage<Result<Boolean>>> matchAsync(
            String target, CompletableFuture<GenericError> cancel);

    /**
     * Return the jobcache filtered by metadata
     *
     * @param metadata search metadata
     * @return list of running jobs
     */
    Optional<Map<String, Jobs.ListJobsEntry>> jobsByMetadata(Object metadata);

    /**
     * Return the jobcache filtered by metadata and start and end time.
     *
     * @param metadata search metadata
     * @param startTime jobs start time
     * @param endTime jobs end time
     * @return list of running jobs
     */
    Optional<Map<String, Jobs.ListJobsEntry>> jobsByMetadata(Object metadata, LocalDateTime startTime,
                                                             LocalDateTime endTime);

    /**
     * Executes match.glob in another thread and returns a {@link CompletionStage}.
     * @param target the target to pass to match.glob
     * @param cancel a future used to cancel waiting
     * @return a future or Optional.empty if there's no ssh-push minion in the db
     */
    Optional<CompletionStage<Map<String, Result<Boolean>>>> matchAsyncSSH(
            String target, CompletableFuture<GenericError> cancel);

    /**
     * Execute a LocalCall asynchronously on the default Salt client.
     *
     * @deprecated this function is too general and should be replaced by more specific functionality.
     * @param <T> the return type of the call
     * @param callIn the call to execute
     * @param target minions targeted by the call
     * @param metadataIn the metadata to be passed in the call
     * @return the LocalAsyncResult of the call
     * @throws SaltException in case of an error executing the job with Salt
     */
    @Deprecated
    <T> Optional<LocalAsyncResult<T>> callAsync(LocalCall<T> callIn, Target<?> target,
                                                Optional<ScheduleMetadata> metadataIn) throws SaltException;


    /**
     * Get pending resume information.
     * @param minionIds to target.
     * @return pending resume information.
     * @throws SaltException if anything goes wrong.
     */
    Map<String, Result<Map<String, String>>> getPendingResume(List<String> minionIds) throws SaltException;

    /**
     * Execute generic salt call.
     * @param call salt call to execute.
     * @param minionId of the target minion.
     * @return raw salt call result in json format.
     */
    Optional<Result<JsonElement>> rawJsonCall(LocalCall<?> call, String minionId);

    /**
     * Execute generic salt call.
     * @param call salt call to execute.
     * @param minionId of the target minion.
     * @return raw salt call result in json format.
     * @deprecated this method should not be used for new code
     */
    @Deprecated
    default Optional<JsonElement> rawJsonCallOld(LocalCall<?> call, String minionId) {
        return callSync(new ElementCallJson(call), minionId);
    }

    /**
     * @deprecated this function is too general and should be replaced by more specific functionality.
     * @return saltSSHService to get
     */
    @Deprecated
    SaltSSHService getSaltSSHService();

    /**
     * Call 'saltutil.refresh_pillar' to sync the grains to the target minion(s).
     * @param minionList minion list
     */
    void refreshPillar(MinionList minionList);

    /**
     * Check SSL certificates before deploying them.
     *
     * @param rootCA root CA used to sign the SSL certificate in PEM format
     * @param intermediateCAs intermediate CAs used to sign the SSL certificate in PEM format
     * @param serverCertKey server CRT an Key pair
     * @return the certificate to deploy
     *
     * @throws IllegalArgumentException if the cert check fails due to erroneous certificates
     */
     String checkSSLCert(String rootCA, SSLCertPair serverCertKey, List<String> intermediateCAs)
             throws IllegalArgumentException;
}
