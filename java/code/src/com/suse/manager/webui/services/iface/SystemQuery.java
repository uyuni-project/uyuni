/**
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

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.JsonElement;
import com.redhat.rhn.domain.server.MinionServer;
import com.suse.manager.reactor.SaltReactor;
import com.suse.manager.virtualization.GuestDefinition;
import com.suse.manager.virtualization.PoolCapabilitiesJson;
import com.suse.manager.virtualization.PoolDefinition;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.services.impl.runner.MgrK8sRunner;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;
import com.suse.manager.webui.utils.gson.BootstrapParameters;
import com.suse.manager.webui.utils.salt.custom.ScheduleMetadata;
import com.suse.salt.netapi.calls.LocalAsyncResult;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.SaltUtil;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.calls.modules.Zypper;
import com.suse.salt.netapi.calls.runner.Jobs;
import com.suse.salt.netapi.calls.wheel.Key;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.datatypes.target.Target;
import com.suse.salt.netapi.errors.GenericError;
import com.suse.salt.netapi.event.EventStream;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.results.SSHResult;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Interface containing methods for directly interacting and getting information from a system.
 * Note: This interface should be split up further at some point.
 */
public interface SystemQuery {

    /**
     * Query virtual host and domains capabilities.
     *
     * @param minionId the salt minion virtual host to ask about
     * @return the output of the salt virt.all_capabilities call in JSON
     */
    Optional<Map<String, JsonElement>> getCapabilities(String minionId);

    /**
     * Query the list of virtual networks defined on a salt minion.
     *
     * @param minionId the minion to ask about
     * @return a list of the network names
     */
    Map<String, JsonObject> getNetworks(String minionId);

    /**
     * Query virtual storage pool definition
     *
     * @param minionId the host minion ID
     * @param poolName the domain name to look for
     * @return the XML definition or an empty Optional
     */
    Optional<PoolDefinition> getPoolDefinition(String minionId, String poolName);

    /**
     * Query virtual machine definition
     *
     * @param minionId the host minion ID
     * @param domainName the domain name to look for
     * @return the XML definition or an empty Optional
     */
    Optional<GuestDefinition> getGuestDefinition(String minionId, String domainName);

    /**
     * Query virtual storage pool capabilities
     *
     * @param minionId the salt minion virtual host to ask about
     * @return the output of the salt virt.pool_capabilities call
     */
    Optional<PoolCapabilitiesJson> getPoolCapabilities(String minionId);

    /**
     * call salt test.ping
     * @param minionId id of the target minion
     * @return true
     */
    Optional<Boolean> ping(String minionId);

    /**
     * Get the minion keys from salt with their respective status.
     *
     * @return the keys with their respective status as returned from salt
     */
    Key.Names getKeys();

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
     * Generate a key pair for the given id and accept the public key.
     *
     * @param id the id to use
     * @param force set true to overwrite an already existing key
     * @return the generated key pair
     */
    Key.Pair generateKeysAndAccept(String id, boolean force);

    /**
     * Get the machine id for a given minion.
     *
     * @param minionId id of the target minion
     * @return the machine id as a string
     */
    Optional<String> getMachineId(String minionId);

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
     * Delete a Salt key from the "Rejected Keys" category using the mgrutil runner.
     *
     * @param minionId the minionId to look for in "Rejected Keys"
     * @return the result of the runner call as a map
     */
    Optional<MgrUtilRunner.ExecResult> deleteRejectedKey(String minionId);

    /**
     * Return the stream of events happening in salt.
     *
     * @return the event stream
     * @throws SaltException exception occured during connection (if any)
     */
    EventStream getEventStream();

    /**
     * Run a remote command on a given minion.
     *
     * @param target the target
     * @param cmd the command
     * @return the output of the command
     */
    Map<String, Result<String>> runRemoteCommand(MinionList target, String cmd);

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
     * Query the list of virtual storage pools defined on a salt minion.
     *
     * @param minionId the minion to ask about
     * @return a map associating pool names with their informations as Json elements
     */
    Map<String, JsonObject> getPools(String minionId);

    /**
     * Query the list of virtual storage volumes defined on a salt minion.
     *
     * @param minionId the minion to ask about
     * @return a map associating pool names with the list of volumes it contains mapped by their names
     */
    Map<String, Map<String, JsonObject>> getVolumes(String minionId);

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
     * Executes match.glob in another thread and returns a {@link CompletionStage}.
     * @param target the target to pass to match.glob
     * @param cancel a future used to cancel waiting
     * @return a future or Optional.empty if there's no ssh-push minion in the db
     */
    Optional<CompletionStage<Map<String, Result<Boolean>>>> matchAsyncSSH(
            String target, CompletableFuture<GenericError> cancel);

    /**
     * Call 'saltutil.refresh_pillar' to sync the grains to the target minion(s).
     * @param minionList minion list
     */
    void refreshPillar(MinionList minionList);

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
     * Gets a minion's master hostname.
     *
     * @param minionId the minion id
     * @return the master hostname
     */
    Optional<String> getMasterHostname(String minionId);

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
     * Call the custom mgrutil.ssh_keygen runner if the key files are not present.
     *
     * @param path of the key files
     * @return the result of the runner call as a map
     */
    Optional<MgrUtilRunner.ExecResult> generateSSHKey(String path);

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
            String user, Map<String, String> options, String command, String outputfile);

    /**
     * Get information about all containers running in a Kubernetes cluster.
     * @param kubeconfig path to the kubeconfig file
     * @param context kubeconfig context to use
     * @return a list of containers
     */
    Optional<List<MgrK8sRunner.Container>> getAllContainers(String kubeconfig, String context);

    /**
     * Remove SUSE Manager specific configuration from a Salt minion.
     *
     * @param minion the minion.
     * @param timeout operation timeout
     * @return list of error messages or empty if no error
     */
    Optional<List<String>> cleanupMinion(MinionServer minion, int timeout);

    /**
     * Send notification about a system id to be generated.
     * @param minion target minion.
     * @throws InstantiationException if signature generation fails
     * @throws SaltException if anything goes wrong.
     */
    void notifySystemIdGenerated(MinionServer minion) throws InstantiationException, SaltException;

    /**
     * Get pending resume information.
     * @param minionIds to target.
     * @return pending resume information.
     * @throws SaltException if anything goes wrong.
     */
    Map<String, Result<Map<String, String>>> getPendingResume(List<String> minionIds) throws SaltException;

    /**
     * Return show highstate result.
     * @param minionId of the target minion.
     * @return show highstate result.
     * @throws SaltException if anything goes wrong.
     */
    Map<String, Result<Object>> showHighstate(String minionId) throws SaltException;

    /**
     * Query product information.
     * @param minionId of the target minion.
     * @return product information
     */
    Optional<List<Zypper.ProductInfo>> getProducts(String minionId);

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
     * Sync the channels of a list of minions
     * @param minionIds of the targets.
     * @throws SaltException if anything goes wrong.
     */
    void deployChannels(List<String> minionIds) throws SaltException;

    /**
     * Upload built Kiwi image to SUSE Manager
     *
     * @param minion     the minion
     * @param filepath   the filepath of the image to upload, in the build host
     * @param imageStore the image store location
     * @return the execution result
     */
    Optional<MgrUtilRunner.ExecResult> collectKiwiImage(MinionServer minion, String filepath,
            String imageStore);

    /**
     * Update libvirt engine on a given minion.
     * @param minion to update.
     */
    void updateLibvirtEngine(MinionServer minion);

    /**
     * Execute generic salt call.
     * @param call salt call to execute.
     * @param minionId of the target minion.
     * @return raw salt call result in json format.
     */
    Optional<JsonElement> rawJsonCall(LocalCall<?> call, String minionId);

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
     * @deprecated this function is too general and should be replaced by more specific functionality.
     * @return saltSSHService to get
     */
    @Deprecated
    SaltSSHService getSaltSSHService();

    /**
     * Get redhat product information
     * @param minionId id of the target minion
     * @return redhat product information
     */
    Optional<RedhatProductInfo> redhatProductInfo(String minionId);

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
     * Remove given file using RunnerCall
     *
     * @param path the path of file to be removed
     * @throws IllegalStateException if the given path is not absolute
     * @return todo
     */
    Optional<Boolean> removeFile(Path path);
}
