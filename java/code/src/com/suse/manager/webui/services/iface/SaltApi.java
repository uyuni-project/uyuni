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

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.NoWheelResultsException;
import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.messaging.JavaMailException;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.manager.audit.scap.file.ScapFileManager;

import com.suse.manager.reactor.PGEventStream;
import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.ssl.SSLCertPair;
import com.suse.manager.utils.MailHelper;
import com.suse.manager.utils.MinionServerUtils;
import com.suse.manager.webui.controllers.utils.ContactMethodUtil;
import com.suse.manager.webui.services.impl.MinionPendingRegistrationService;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.impl.runner.MgrKiwiImageRunner;
import com.suse.manager.webui.services.impl.runner.MgrRunner;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;
import com.suse.manager.webui.utils.ElementCallJson;
import com.suse.manager.webui.utils.SaltRoster;
import com.suse.manager.webui.utils.gson.BootstrapParameters;
import com.suse.manager.webui.utils.salt.custom.ScheduleMetadata;
import com.suse.salt.netapi.AuthModule;
import com.suse.salt.netapi.calls.AbstractCall;
import com.suse.salt.netapi.calls.LocalAsyncResult;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.RunnerCall;
import com.suse.salt.netapi.calls.WheelCall;
import com.suse.salt.netapi.calls.WheelResult;
import com.suse.salt.netapi.calls.modules.Cmd;
import com.suse.salt.netapi.calls.modules.SaltUtil;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.calls.modules.State.ApplyResult;
import com.suse.salt.netapi.calls.modules.Test;
import com.suse.salt.netapi.calls.wheel.Key;
import com.suse.salt.netapi.client.SaltClient;
import com.suse.salt.netapi.client.impl.HttpAsyncClientImpl;
import com.suse.salt.netapi.datatypes.AuthMethod;
import com.suse.salt.netapi.datatypes.Batch;
import com.suse.salt.netapi.datatypes.PasswordAuth;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.datatypes.target.Target;
import com.suse.salt.netapi.errors.GenericError;
import com.suse.salt.netapi.errors.SaltError;
import com.suse.salt.netapi.event.EventListener;
import com.suse.salt.netapi.event.EventStream;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.results.SSHResult;
import com.suse.salt.netapi.results.StateApplyResult;
import com.suse.utils.Opt;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;



/**
 * Interface for interacting with salt.
 */
public class SaltApi {

    private static final Logger LOG = LogManager.getLogger(SaltApi.class);

    enum Messages {

        CLEANUP_MINION_SALT_STATE("cleanup_minion"), GENERIC_RUNNER_ERROR(
                "Generic Salt error for runner call %s: %s"), SSH_RUNNER_ERROR(
                        "SaltSSH error for runner call %s: %s"), PAYLOAD_CALL_TEMPLATE(
                                "%s with payload [%s]"), MINION_UNREACHABLE_ERROR(
                                        "minion_unreachable");

        private final String text;

        Messages(final String textIn) {
            this.text = textIn;
        }

        @Override
        public String toString() {
            return text;
        }

    }


    // Reconnecting time (in seconds) to Salt event bus
    private static final int DELAY_TIME_SECONDS = 5;
    public static final int SSH_DEFAULT_PORT = 22;

    private static final Predicate<? super String> SALT_MINION_PREDICATE =
            mid -> MinionPendingRegistrationService.containsSSHMinion(mid) ||
                    MinionServerFactory.findByMinionId(mid)
                            .filter(MinionServerUtils::isSshPushMinion).isPresent();

    private static final String SALT_USER = "admin";
    private static final String SALT_PASSWORD =
            com.redhat.rhn.common.conf.Config.get().getString("server.secret_key");
    private static final AuthModule AUTH_MODULE = AuthModule.FILE;
    static final AuthMethod PW_AUTH =
            new AuthMethod(new PasswordAuth(SALT_USER, SALT_PASSWORD, AUTH_MODULE));

    private static final URI SALT_MASTER_URI = URI.create("https://" +
            com.redhat.rhn.common.conf.Config.get().getString(ConfigDefaults.SALT_API_HOST,
                    "localhost") +
            ":" + com.redhat.rhn.common.conf.Config.get()
                    .getString(ConfigDefaults.SALT_API_PORT, "9080"));

    private final Batch defaultBatch;
    protected final SaltClient saltClient;
    private final CloseableHttpAsyncClient asyncHttpClient;
    private EventStream eventStream;

    /**
     * Default constructor
     */
    public SaltApi() {
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(0)
                .setSocketTimeout(0).setConnectionRequestTimeout(0)
                .setCookieSpec(CookieSpecs.STANDARD).build();
        HttpAsyncClientBuilder httpClientBuilder = HttpAsyncClients.custom();
        httpClientBuilder.setDefaultRequestConfig(requestConfig);

        asyncHttpClient =
                httpClientBuilder.setMaxConnPerRoute(20).setMaxConnTotal(20).build();
        asyncHttpClient.start();

        defaultBatch = Batch.custom()
                .withBatchAsAmount(ConfigDefaults.get().getSaltBatchSize())
                .withDelay(ConfigDefaults.get().getSaltBatchDelay())
                .withPresencePingTimeout(ConfigDefaults.get().getSaltPresencePingTimeout())
                .withPresencePingGatherJobTimeout(
                        ConfigDefaults.get().getSaltPresencePingGatherJobTimeout())
                .build();
        saltClient =
                new SaltClient(SALT_MASTER_URI, new HttpAsyncClientImpl(asyncHttpClient));
    }


    /**
     * Sync the channels of a list of minions
     * @param minionIds of the targets.
     * @throws SaltException if anything goes wrong.
     */
    public void deployChannels(List<String> minionIds) throws SaltException {
        callSync(com.suse.salt.netapi.calls.modules.State
                .apply(ApplyStatesEventMessage.CHANNELS), new MinionList(minionIds));
    }

    /**
     * Using a RunnerCall, store given contents to given path and set the mode,
     * so that SSH likes it (read-write for owner, nothing for others).
     *
     * @param path the path where key will be stored
     * @param contents the contents of the key (PEM format)
     * @throws IllegalStateException if something goes wrong during the
     * operation, or if given path is not absolute
     */
    public void storeSshKeyFile(Path path, String contents) {
        ensureAbsolutePath(path);

        String absolutePath = path.toAbsolutePath().toString();
        RunnerCall<String> createFile = MgrRunner.writeTextFile(absolutePath, contents);
        if (callSync(createFile).isEmpty()) {
            throw new IllegalStateException("Can't create SSH priv key file " + path);
        }

        // this might not be needed, the file is created with sane perms already
        RunnerCall<String> setMode = MgrRunner.setFileMode(absolutePath, "0600");
        if (callSync(setMode).isEmpty()) {
            throw new IllegalStateException("Can't set mode for SSH priv key file " + path);
        }
    }

    /**
     * Remove given file using RunnerCall
     *
     * @param path the path of file to be removed
     * @throws IllegalStateException if the given path is not absolute
     * @return Optional with true if the file deletion succeeded.
     */
    public Optional<Boolean> removeFile(Path path) {
        ensureAbsolutePath(path);
        String absolutePath = path.toAbsolutePath().toString();
        RunnerCall<Boolean> createFile = MgrRunner.removeFile(absolutePath);
        return callSync(createFile);
    }

    /**
     * Copy given file using RunnerCall
     *
     * @param src the source path of file to be removed
     * @param dst the destination path of file to be removed
     * @throws IllegalStateException if the given path is not absolute
     * @return Optional with true if the file deletion succeeded.
     */
    public Optional<Boolean> copyFile(Path src, Path dst) {
        ensureAbsolutePath(src);
        ensureAbsolutePath(dst);
        RunnerCall<Boolean> call = MgrRunner.copyFile(src.toAbsolutePath().toString(),
                dst.toAbsolutePath().toString(), false, false);
        return callSync(call);
    }

    private void ensureAbsolutePath(Path path) {
        if (!path.isAbsolute()) {
            throw new IllegalStateException("Given path is not absolute: " + path);
        }
    }

    /**
     * Performs an test.echo on a target set of minions for checkIn purpose.
     * @param targetIn the target
     * @return the LocalAsyncResult of the test.echo call
     * @throws SaltException if we get a failure from Salt
     */
    public Optional<LocalAsyncResult<String>> checkIn(MinionList targetIn)
        throws SaltException {
        try {
            LocalCall<String> call = Test.echo("checkIn");
            return callAsync(call, targetIn);
        }
        catch (SaltException e) {
            throw new RhnRuntimeException(e);
        }
    }

    /**
     * Apply util.systeminfo state on the specified minion list
     * @param minionTarget minion list
     */
    public void updateSystemInfo(MinionList minionTarget) {
        try {
            callAsync(
                    State.apply(
                            Collections.singletonList(ApplyStatesEventMessage.SYSTEM_INFO),
                            Optional.empty()),
                    minionTarget,
                    Optional.of(ScheduleMetadata.getDefaultMetadata().withMinionStartup()));
        }
        catch (SaltException ex) {
            LOG.debug("Error while executing util.systeminfo state: {}", ex.getMessage());
        }
    }

    /**
     * Store the files uploaded by a minion to the SCAP storage directory.
     * @param minion the minion
     * @param uploadDir the uploadDir
     * @param actionId the action id
     * @return a map with one element: @{code true} -> scap store path,
     * {@code false} -> err message
     *
     */
    public Map<Boolean, String> storeMinionScapFiles(MinionServer minion, String uploadDir,
            Long actionId) {
        String actionPath = ScapFileManager.getActionPath(minion.getOrg().getId(),
                minion.getId(), actionId);
        Path mountPoint = Paths.get(com.redhat.rhn.common.conf.Config.get()
                .getString(ConfigDefaults.MOUNT_POINT));
        try {
            // create dirs
            Path actionDir = Files.createDirectories(mountPoint.resolve(actionPath));

            UserPrincipalLookupService lookupService =
                    FileSystems.getDefault().getUserPrincipalLookupService();
            GroupPrincipal susemanagerGroup =
                    lookupService.lookupPrincipalByGroupName("susemanager");
            GroupPrincipal wwwGroup = lookupService.lookupPrincipalByGroupName("www");
            // systems/<orgId>/<serverId>/actions/<actionId>
            changeGroupAndPerms(actionDir, susemanagerGroup);
            // systems/<orgId>/<serverId>/actions
            actionDir = actionDir.getParent();
            while (!actionDir.equals(mountPoint)) {
                changeGroupAndPerms(actionDir, wwwGroup);
                actionDir = actionDir.getParent();
            }

        }
        catch (IOException e) {
            LOG.error("Error creating dir {}", mountPoint.resolve(actionPath), e);
        }

        RunnerCall<Map<Boolean, String>> call = MgrUtilRunner.moveMinionUploadedFiles(
                minion.getMinionId(), uploadDir, com.redhat.rhn.common.conf.Config.get()
                        .getString(ConfigDefaults.MOUNT_POINT),
                actionPath);
        Optional<Map<Boolean, String>> result = callSync(call, err -> err.fold(e -> {
            LOG.error(String.format("Function [%s] not available for runner call %s.",
                    e.getFunctionName(), callToString(call)));
            return Optional.of(Collections.singletonMap(false,
                    String.format("Function [%s] not available", e.getFunctionName())));
        }, e -> {
            LOG.error(String.format("Module [%s] not supported for runner call %s.",
                    e.getModuleName(), callToString(call)));
            return Optional.of(Collections.singletonMap(false,
                    String.format("Module [%s] not supported", e.getModuleName())));
        }, e -> {
            LOG.error(String.format("Error parsing json response from runner call %s: %s",
                    callToString(call), e.getJson()));
            return Optional.of(Collections.singletonMap(false,
                    "Error parsing json response: " + e.getJson()));
        }, e -> {
            LOG.error(String.format(Messages.GENERIC_RUNNER_ERROR.toString(),
                    callToString(call), e.getMessage()));
            return Optional.of(Collections.singletonMap(false,
                    "Generic Salt error: " + e.getMessage()));
        }, e -> {
            LOG.error(String.format(Messages.SSH_RUNNER_ERROR.toString(),
                    callToString(call), e.getMessage()));
            return Optional.of(
                    Collections.singletonMap(false, "SaltSSH error: " + e.getMessage()));
        }));
        return result.orElseGet(() -> Collections.singletonMap(false,
                "Error moving scap result files." + " Please check the logs."));
    }

    /**
     * Call the custom mgrutil.ssh_keygen runner if the key files are not
     * present.
     *
     * @param path of the key files
     * @return the result of the runner call as a map
     */
    public Optional<MgrUtilRunner.SshKeygenResult> generateSSHKey(String path) {
        RunnerCall<MgrUtilRunner.SshKeygenResult> call = MgrUtilRunner.generateSSHKey(path);
        return callSync(call);
    }

    /**
     * Removes a hostname from the Salt ~/.ssh/known_hosts file.
     * @param hostname the hostname to remote
     * @return the result of the runner call
     */
    public Optional<MgrUtilRunner.RemoveKnowHostResult> removeSaltSSHKnownHost(
            String hostname) {
        return removeSaltSSHKnownHost(hostname, SSH_DEFAULT_PORT);
    }

    /**
     * Removes a hostname from the Salt ~/.ssh/known_hosts file.
     * @param hostname the hostname to remote
     * @param port the port of the host to remove
     * @return the result of the runner call
     */
    public Optional<MgrUtilRunner.RemoveKnowHostResult> removeSaltSSHKnownHost(
            String hostname, int port) {
        RunnerCall<MgrUtilRunner.RemoveKnowHostResult> call =
                MgrUtilRunner.removeSSHKnowHost("salt", hostname, port);
        return callSync(call);
    }

    /**
     * Call 'saltutil.sync_grains' to sync the grains to the target minion(s).
     * @param minionList minion list
     */
    public void syncGrains(MinionList minionList) {
        try {
            LocalCall<List<String>> call =
                    SaltUtil.syncGrains(Optional.empty(), Optional.empty());
            callSync(call, minionList);
        }
        catch (SaltException e) {
            throw new RhnRuntimeException(e);
        }
    }

    /**
     * Call 'saltutil.sync_modules' to sync the grains to the target minion(s).
     * @param minionList minion list
     */
    public void syncModules(MinionList minionList) {
        try {
            LocalCall<List<String>> call =
                    SaltUtil.syncModules(Optional.empty(), Optional.empty());
            callSync(call, minionList);
        }
        catch (SaltException e) {
            throw new RhnRuntimeException(e);
        }
    }

    /**
     * Call 'saltutil.sync_all' to sync everything to the target minion(s).
     * @param minionList minion list
     */
    public void syncAll(MinionList minionList) {
        try {
            LocalCall<Map<String, Object>> call =
                    SaltUtil.syncAll(Optional.empty(), Optional.empty());
            callSync(call, minionList);
        }
        catch (SaltException e) {
            throw new RhnRuntimeException(e);
        }
    }

    /**
     * call salt test.ping
     * @param minionId id of the target minion
     * @return true
     */
    public Optional<Boolean> ping(String minionId) {
        return callSync(Test.ping(), minionId);
    }

    /**
     * Delete a given minion's key.
     *
     * @param minionId id of the minion
     */
    public void deleteKey(String minionId) {
        if (callSync(Key.delete(minionId)).isEmpty()) {
            throw new NoWheelResultsException();
        }
    }

    /**
     * Accept all keys matching the given pattern
     *
     * @param match a pattern for minion ids
     */
    public void acceptKey(String match) {
        if (callSync(Key.accept(match)).isEmpty()) {
            throw new NoWheelResultsException();
        }
    }

    /**
     * Reject a given minion's key.
     *
     * @param minionId id of the minion
     */
    public void rejectKey(String minionId) {
        if (callSync(Key.reject(minionId)).isEmpty()) {
            throw new NoWheelResultsException();
        }
    }

    /**
     * Run a remote command on a given minion.
     *
     * @param target the target
     * @param cmd the command
     * @return the output of the command
     */
    public Map<String, Result<String>> runRemoteCommand(MinionList target, String cmd) {
        try {
            return callSync(Cmd.run(cmd), target);
        }
        catch (SaltException e) {
            throw new RhnRuntimeException(e);
        }
    }

    /**
     * Delete a Salt key from the "Rejected Keys" category using the mgrutil
     * runner.
     *
     * @param minionId the minionId to look for in "Rejected Keys"
     * @return the result of the runner call as a map
     */
    public Optional<MgrUtilRunner.ExecResult> deleteRejectedKey(String minionId) {
        RunnerCall<MgrUtilRunner.ExecResult> call =
                MgrUtilRunner.deleteRejectedKey(minionId);
        return callSync(call);
    }

    /**
     * Generate a key pair for the given id and accept the public key.
     *
     * @param id the id to use
     * @param force set true to overwrite an already existing key
     * @return the generated key pair
     */
    public Key.Pair generateKeysAndAccept(String id, boolean force) {
        return callSync(Key.genAccept(id, Optional.of(force))).
                orElseThrow( () -> new NoWheelResultsException("no wheel results"));
        }

    /**
     * Bootstrap a system using salt-ssh.
     *
     * The call internally uses ssh identity key/cert on a hardcoded path.
     * If the key/cert doesn't exist, it's created by salt and copied to the target host.
     * Copying is implemented via a salt state (mgr_ssh_identity), as ssh_key_deploy
     * is ignored by the api.)
     *
     * @param parameters - bootstrap parameters
     * @param bootstrapMods - state modules to be applied during the bootstrap
     * @param pillarData - pillar data used in the salt-ssh call
     * @throws SaltException if something goes wrong during command execution or
     * during manipulation the salt-ssh roster
     * @return the result of the underlying ssh call for given host
     */
    public Result<SSHResult<Map<String, ApplyResult>>> bootstrapMinion(
            BootstrapParameters parameters, List<String> bootstrapMods,
            Map<String, Object> pillarData) throws SaltException {
        LOG.info("Bootstrapping host: {}", parameters.getHost());
        LocalCall<Map<String, ApplyResult>> call = State.apply(bootstrapMods, Optional.of(pillarData));

        List<String> bootstrapProxyPath;
        if (parameters.getProxyId().isPresent()) {
            bootstrapProxyPath = parameters.getProxyId()
                                           .map(ServerFactory::lookupById)
                                           .map(SaltSSHService::proxyPathToHostnames)
                                           .orElseThrow(() -> new SaltException(
                                                   "Proxy not found for id: " + parameters.getProxyId().get()));
        }
        else {
            bootstrapProxyPath = Collections.emptyList();
        }

        String contactMethod = parameters.getFirstActivationKey()
                .map(ActivationKeyFactory::lookupByKey)
                .map(key -> key.getContactMethod().getLabel()).orElse("");

        Optional<String> portForwarding = SaltSSHService.remotePortForwarding(bootstrapProxyPath, contactMethod);

        // private key handling just for bootstrap
        Optional<Path> tmpKeyFileAbsolutePath = parameters.getPrivateKey().map(key ->
        SaltSSHService.createTempKeyFilePath());

        try {
            tmpKeyFileAbsolutePath.ifPresent(p -> parameters.getPrivateKey().ifPresent(k ->
                    this.storeSshKeyFile(p, k)));
            SaltRoster roster = new SaltRoster();
            roster.addHost(parameters.getHost(),
                    parameters.getUser(),
                    parameters.getPassword(),
                    tmpKeyFileAbsolutePath.map(Path::toString),
                    parameters.getPrivateKeyPassphrase(),
                    parameters.getPort(),
                    portForwarding,
                    SaltSSHService.getSSHProxyCommandOption(bootstrapProxyPath,
                        contactMethod,
                        parameters.getHost(),
                        parameters.getPort().orElse(SaltSSHService.SSH_PUSH_PORT)),
                    SaltSSHService.getSshPushTimeout(),
                    SaltSSHService.getMinionOpts(parameters.getHost(), contactMethod),
                    Optional.ofNullable(SaltSSHService.getSaltSSHPreflightScriptPath()),
                    Optional.of(Arrays.asList(
                            bootstrapProxyPath.isEmpty() ?
                                    ConfigDefaults.get().getCobblerHost() :
                                    bootstrapProxyPath.get(bootstrapProxyPath.size() - 1).split(":")[0],
                            ContactMethodUtil.SSH_PUSH_TUNNEL.equals(contactMethod) ?
                                    SaltSSHService.getSshPushRemotePort() : SaltSSHService.SSL_PORT,
                                    SaltSSHService.getSSHUseSaltThin() ? 1 : 0,
                            1
                            ))
                    );

            Map<String, Result<SSHResult<Map<String, ApplyResult>>>> result =
                    callSyncSSHInternal(call,
                            new MinionList(parameters.getHost()),
                            Optional.of(roster),
                            parameters.isIgnoreHostKeys(),
                            SaltSSHService.isSudoUser(parameters.getUser()));
            return result.get(parameters.getHost());
        }
        finally {
            tmpKeyFileAbsolutePath.ifPresent(this::cleanUpTempKeyFile);
        }
    }

    private void cleanUpTempKeyFile(Path path) {
        this.removeFile(path)
                .orElseThrow(() -> new IllegalStateException("Can't remove file " + path));
    }

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
    public Optional<MgrUtilRunner.ExecResult> chainSSHCommand(List<String> hosts, String clientKey,
            String proxyKey, String user, Map<String, String> options, String command, String outputfile) {

        RunnerCall<MgrUtilRunner.ExecResult> call = MgrUtilRunner.chainSSHCommand(hosts, clientKey,
                proxyKey, user, options, command, outputfile);
        return callSync(call);
    }

    /**
     * This is a helper for keeping the old exception behaviour until
     * all code makes proper use of the async api.
     * @param fn function to execute and adapt.
     * @param <T> result of fn
     * @return the result of fn
     * @throws SaltException if an exception gets thrown
     */
    public static <T> T adaptException(CompletionStage<T> fn) throws SaltException {
        try {
            return fn.toCompletableFuture().join();
        }
        catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof SaltException) {
                throw (SaltException) cause;
            }
            else {
                throw new SaltException(cause);
            }
        }
    }


    private synchronized void eventStreamClosed() {
        eventStream = null;
    }

    private EventStream createEventStream() throws SaltException {
        return new PGEventStream();
    }

    /**
     * Remove SUSE Manager specific configuration from a Salt minion.
     *
     * @param minion the minion.
     * @return list of error messages or empty if no error
     */
    public Optional<List<String>> cleanupMinion(MinionServer minion) {
        Optional<Map<String, ApplyResult>> response = applyState(minion.getMinionId(),
                Messages.CLEANUP_MINION_SALT_STATE.toString());

        // response is empty in case the minion is down
        if (response.isPresent()) {
            return response.get().values().stream().filter(value -> !value.isResult())
                    .map(StateApplyResult::getComment)
                    .collect(Collectors.collectingAndThen(Collectors.toList(),
                            list -> list.isEmpty() ? Optional.empty() : Optional.of(list)));
        }
        return Optional.of(
                Collections.singletonList(Messages.MINION_UNREACHABLE_ERROR.toString()));
    }

    /**
     * Partitions minion ids according to the contact method of corresponding
     * minions (salt-ssh minions in one partition, regular minions in the
     * other).
     *
     * @param minionIds minion ids
     * @return map with partitioning
     */
    public static Map<Boolean, List<String>> partitionMinionsByContactMethod(
            Collection<String> minionIds) {
        return minionIds.stream().collect(Collectors.partitioningBy(SALT_MINION_PREDICATE));
    }

    /**
     * Synchronously executes a salt function on a single minion. If a
     * SaltException is thrown, re-throw a RuntimeException.
     *
     * @param call salt function to call
     * @param minionId minion id to target
     * @param <R> result type of the salt function
     * @return Optional holding the result of the function or empty if the
     * minion did not respond.
     */
    <T> Map<String, Result<T>> callSync(LocalCall<T> callIn, MinionList target)
        throws SaltException {

        List<String> minionIds = getMinions(target);

        Map<String, Result<T>> results = new HashMap<>();

        ScheduleMetadata metadata = ScheduleMetadata.getDefaultMetadata().withBatchMode();
        LOG.debug("Local callSync: {}", SaltApi.localCallToString(callIn));
        List<Map<String, Result<T>>> callResult =
                adaptException(callIn.withMetadata(metadata).callSync(saltClient,
                        new MinionList(minionIds), PW_AUTH, defaultBatch));
        results.putAll(callResult.stream().flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Entry<String, Result<T>>::getKey,
                        Entry<String, Result<T>>::getValue)));

        return results;
    }

    /**
     * Synchronously executes a salt function on a single minion. If a
     * SaltException is thrown, re-throw a RuntimeException.
     *
     * @param call salt function to call
     * @param target
     * @param <R> result type of the salt function
     * @return Optional holding the result of the function or empty if the
     * minion did not respond.
     */

    <T> Map<String, Result<T>> callSync(LocalCall<T> callIn, Target<?> target)
        throws SaltException {

        ScheduleMetadata metadata = ScheduleMetadata.getDefaultMetadata().withBatchMode();
        LOG.debug("Local callSync: {}", SaltApi.localCallToString(callIn));
        List<Map<String, Result<T>>> callResult =
                adaptException(callIn.withMetadata(metadata).callSync(saltClient, target,
                        PW_AUTH, defaultBatch));
        return callResult.stream().flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
    }

    /**
     * Synchronously executes a salt function on a single minion. If a
     * SaltException is thrown, re-throw a RuntimeException.
     *
     * @param call salt function to call
     * @param minionId minion id to target
     * @param <R> result type of the salt function
     * @return Optional holding the result of the function or empty if the
     * minion did not respond.
     */
    public <R> Optional<R> callSync(LocalCall<R> call, String minionId) {
        return callSyncResult(call, minionId).flatMap(r -> r.fold(error -> {
            LOG.warn(error.toString());
            return Optional.empty();
        }, Optional::of));
    }

    /**
     * Return a minion list
     * @param target
     * @return Minion list
     */
    public List<String> getMinions(MinionList target) {

        HashSet<String> uniqueMinionIds = new HashSet<>(target.getTarget());
        Map<Boolean, List<String>> minionPartitions =
                partitionMinionsByContactMethod(uniqueMinionIds);

        return minionPartitions.get(false);
    }

    /**
     * Run a remote command on a given minion asynchronously.
     * @param target the target
     * @param cmd the command to execute
     * @param cancel a future used to cancel waiting on return events
     * @return a map holding a {@link CompletionStage}s for each minion
     */
    public Map<String, CompletionStage<Result<String>>> runRemoteCommandAsync(
            MinionList target, String cmd, CompletableFuture<GenericError> cancel) {

        List<String> minionIds = getMinions(target);

        Map<String, CompletionStage<Result<String>>> results = new HashMap<>();
        LocalCall<String> call = Cmd.run(cmd);

        if (!minionIds.isEmpty()) {
            try {
                results.putAll(completableAsyncCall(call, target, getEventStream(), cancel)
                        .orElseGet(Collections::emptyMap));
            }
            catch (SaltException e) {
                throw new RhnRuntimeException(e);
            }
        }

        return results;
    }

    /**
     * Execute a LocalCall asynchronously on the default Salt client.
     *
     * @deprecated this function is too general and should be replaced by more
     * specific functionality.
     * @param <T> the return type of the call
     * @param callIn the call to execute
     * @param target minions targeted by the call
     * @param metadataIn the metadata to be passed in the call
     * @return the LocalAsyncResult of the call
     * @throws SaltException in case of an error executing the job with Salt
     */
    @Deprecated
    public <T> Optional<LocalAsyncResult<T>> callAsync(LocalCall<T> callIn,
            Target<?> target, Optional<ScheduleMetadata> metadataIn)
        throws SaltException {
        ScheduleMetadata metadata = Opt
                .fold(metadataIn, ScheduleMetadata::getDefaultMetadata, Function.identity())
                .withBatchMode();
        LOG.debug("Local callAsync: {}", SaltApi.localCallToString(callIn));
        return adaptException(callIn.withMetadata(metadata).callAsync(saltClient, target,
                PW_AUTH, defaultBatch));
    }

    /**
     * Execute generic salt call.
     * @param call salt call to execute.
     * @param minionId of the target minion.
     * @return raw salt call result in json format.
     */
    public Optional<Result<JsonElement>> rawJsonCall(LocalCall<?> call, String minionId) {
        return callSyncResult(new ElementCallJson(call), minionId);
    }

    /**
     * Execute generic salt call.
     * @param call salt call to execute.
     * @param minionId of the target minion.
     * @return raw salt call result in json format.
     * @deprecated this method should not be used for new code
     */
    @Deprecated
    Optional<JsonElement> rawJsonCallOld(LocalCall<?> call, String minionId) {
        return callSync(new ElementCallJson(call), minionId);
    }

    /**
     * Call 'saltutil.refresh_pillar' to sync the grains to the target
     * minion(s).
     * @param minionList minion list
     */
    public void refreshPillar(MinionList minionList) {
        try {
            LocalCall<Boolean> call =
                    SaltUtil.refreshPillar(Optional.empty(), Optional.empty());
            callAsync(call, minionList);

            // Salt pillar refresh doesn't reload the modules with the new
            // pillar
            LocalCall<Boolean> modulesRefreshCall =
                    new LocalCall<>("saltutil.refresh_modules", Optional.empty(),
                            Optional.empty(), new TypeToken<>() {
                            });
            callAsync(modulesRefreshCall, minionList);
        }
        catch (SaltException e) {
            throw new RhnRuntimeException(e);
        }
    }

    /**
     * Check SSL certificates before deploying them.
     *
     * @param rootCA root CA used to sign the SSL certificate in PEM format
     * @param intermediateCAs intermediate CAs used to sign the SSL certificate
     * in PEM format
     * @param serverCertKey server CRT an Key pair
     * @return the certificate to deploy
     *
     * @throws IllegalArgumentException if the cert check fails due to erroneous
     * certificates
     */
    public String checkSSLCert(String rootCA, SSLCertPair serverCertKey,
            List<String> intermediateCAs)
        throws IllegalArgumentException {
        RunnerCall<Map<String, String>> call =
                MgrUtilRunner.checkSSLCert(rootCA, serverCertKey, intermediateCAs);
        Map<String, String> result =
                callSync(call).orElseThrow(() -> new IllegalArgumentException(
                        "Unknown error while checking certificates"));
        String error = result.getOrDefault("error", null);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
        return result.get("cert");
    }

    /**
     * Upload built Kiwi image to SUSE Manager
     *
     * @param minion the minion
     * @param filepath the filepath of the image to upload, in the build host
     * @param imageStore the image store location
     * @return the execution result
     */
    public Optional<MgrUtilRunner.ExecResult> collectKiwiImage(MinionServer minion,
            String filepath, String imageStore) {
        RunnerCall<MgrUtilRunner.ExecResult> call = MgrKiwiImageRunner.collectImage(
                minion.getMinionId(), minion.getIpAddress(), filepath, imageStore);
        return callSync(call);
    }

    private String runnerCallToString(RunnerCall<?> call) {
        return String.format(Messages.PAYLOAD_CALL_TEMPLATE.toString(), call,
                call.getPayload());
    }

    /**
     * Executes a salt wheel module function.
     *
     * @param call wheel call
     * @param <R> result type of the wheel call
     * @return the result of the call or empty on error
     */
    public <R> Optional<R> callSync(WheelCall<R> call) {
        return callSync(call, p -> p.fold(e -> {
            LOG.error(String.format("Function [%s] not available for wheel call %s",
                    e.getFunctionName(), wheelCallToString(call)));
            return Optional.empty();
        }, e -> {
            LOG.error(String.format("Module [%s] not supported for wheel call %s",
                    e.getModuleName(), wheelCallToString(call)));
            return Optional.empty();
        }, e -> {
            LOG.error("Error parsing json response from wheel call {}: {}",
                    wheelCallToString(call), e.getJson());
            return Optional.empty();
        }, e -> {
            LOG.error("Generic Salt error for wheel call {}: {}", wheelCallToString(call),
                    e.getMessage());
            return Optional.empty();
        }, e -> {
            LOG.error("SaltSSH error for wheel call {}: {}", wheelCallToString(call),
                    e.getMessage());
            return Optional.empty();
        }));
    }

    /**
     * Executes a salt wheel module function. On error it invokes the
     * {@code errorHandler} passed as parameter.
     *
     * @param call wheel call
     * @param errorHandler function that handles errors
     * @param <R> result type of the wheel call
     * @return the result of the call or empty on error
     */
    public <R> Optional<R> callSync(WheelCall<R> call,
            Function<SaltError, Optional<R>> errorHandler) {
        try {
            LOG.debug("Wheel callSync: {}", wheelCallToString(call));
            WheelResult<Result<R>> result =
                    adaptException(call.callSync(saltClient, PW_AUTH));
            return result.getData().getResult().fold(errorHandler, Optional::of);
        }
        catch (SaltException e) {
            throw new RhnRuntimeException(e);
        }
    }

    /**
     * Executes a salt runner module function. On error it logs the error and
     * returns an empty result.
     *
     * @param call salt function to call
     * @param <R> result type of the salt function
     * @return the result of the call or empty on error
     */
    public <R> Optional<R> callSync(RunnerCall<R> call) {
        return callSync(call, p -> p.fold(e -> {
            LOG.error(String.format("Function [%s] not available for runner call %s",
                    e.getFunctionName(), runnerCallToString(call)));
            return Optional.empty();
        }, e -> {
            LOG.error(String.format("Module [%s] not supported for runner call %s",
                    e.getModuleName(), runnerCallToString(call)));
            return Optional.empty();
        }, e -> {
            LOG.error("Error parsing json response from runner call {}: {}",
                    runnerCallToString(call), e.getJson());
            return Optional.empty();
        }, e -> {
            LOG.error(String.format(Messages.GENERIC_RUNNER_ERROR.toString(),
                    runnerCallToString(call), e.getMessage()));
            return Optional.empty();
        }, e -> {
            LOG.error(String.format(Messages.SSH_RUNNER_ERROR.toString(),
                    runnerCallToString(call), e.getMessage()));
            return Optional.empty();
        }));
    }

    /**
     * Executes a salt runner module function. On error it invokes the
     * {@code errorHandler} passed as parameter.
     *
     * @param call salt function to call
     * @param errorHandler function that handles errors
     * @param <R> result type of the salt function
     * @return the result of the call or empty on error
     */
    public <R> Optional<R> callSync(RunnerCall<R> call,
            Function<SaltError, Optional<R>> errorHandler) {
        try {
            LOG.debug("Runner callSync: {}", runnerCallToString(call));
            Result<R> result = adaptException(call.callSync(saltClient, PW_AUTH));
            return result.fold(errorHandler, Optional::of);
        }
        catch (SaltException e) {
            throw new RhnRuntimeException(e);
        }
    }


    /**
     * Execute a LocalCall asynchronously on the default Salt client, without
     * passing any metadata on the call.
     *
     * @param <T> the return type of the call
     * @param call the call to execute
     * @param target minions targeted by the call
     * @return the LocalAsyncResult of the call
     * @throws SaltException in case of an error executing the job with Salt
     */
    <T> Optional<LocalAsyncResult<T>> callAsync(LocalCall<T> call, Target<?> target)
        throws SaltException {
        return callAsync(call, target, Optional.empty());
    }

    private void changeGroupAndPerms(Path dir, GroupPrincipal group) {
        PosixFileAttributeView posixAttrs = Files.getFileAttributeView(dir,
                PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        try {
            Set<PosixFilePermission> wantedPers =
                    PosixFilePermissions.fromString("rwxrwxr-x");
            if (!posixAttrs.readAttributes().permissions().equals(wantedPers)) {
                posixAttrs.setPermissions(wantedPers);
            }
        }
        catch (IOException e) {
            LOG.warn(String.format("Could not set 'rwxrwxr-x' permissions on %s: %s", dir,
                    e.getMessage()));
        }
        try {
            if (!posixAttrs.readAttributes().group().equals(group)) {
                posixAttrs.setGroup(group);
            }
        }
        catch (IOException e) {
            LOG.warn(String.format("Could not set group on %s to %s: %s", dir, group,
                    e.getMessage()));
        }
    }

    String callToString(AbstractCall<?> call) {
        return String.format("[%s.%s]", call.getModuleName(), call.getFunctionName());
    }

    /**
     * Call 'saltutil.sync_beacons' to sync the beacons to the target minion(s).
     * @param minionList minionList
     */
    public void syncBeacons(MinionList minionList) {
        try {
            LocalCall<List<String>> call =
                    SaltUtil.syncBeacons(Optional.of(true), Optional.empty());

            callSync(call, minionList);
        }
        catch (SaltException e) {
            throw new RhnRuntimeException(e);
        }
    }

    /**
     * Return local call options as a string (for debugging)
     *
     * @param call the local call
     * @return string representation
     */
    public static String localCallToString(LocalCall<?> call) {
        return String.format(Messages.PAYLOAD_CALL_TEMPLATE.toString(), call,
                call.getPayload());
    }

    private String wheelCallToString(WheelCall<?> call) {
        return String.format(Messages.PAYLOAD_CALL_TEMPLATE.toString(), call,
                call.getPayload());
    }

    private Optional<Map<String, ApplyResult>> applyState(String minionId, String state) {
        return callSync(State.apply(Collections.singletonList(state), Optional.empty()),
                minionId);
    }

    /**
     * Synchronously executes a salt function on a single minion and returns the
     * result.
     *
     * @param call the salt function
     * @param minionId the minion server id
     * @param <R> type of result
     * @return an optional containing the result or empty if no result was
     * retrieved from the minion
     * @throws RuntimeException when a {@link SaltException} is thrown
     */
    public <R> Optional<Result<R>> callSyncResult(LocalCall<R> call, String minionId) {
        try {
            Map<String, Result<R>> stringRMap = callSync(call, new MinionList(minionId));

            return Opt.fold(Optional.ofNullable(stringRMap.get(minionId)), () -> {
                LOG.warn(
                        "Got no result for {} on minion {} (minion did not respond in time)",
                        call.getPayload().get("fun"), minionId);
                return Optional.empty();
            }, Optional::of);
        }
        catch (SaltException e) {
            throw new RhnRuntimeException(e);
        }
    }

    /**
     * Return the stream of events happening in salt.
     *
     * @return the event stream
     */
    public EventStream getEventStream() {

        int retries = 0;

        while (eventStream == null || eventStream.isEventStreamClosed()) {
            retries++;
            try {
                eventStream = createEventStream();
                eventStream.addEventListener(new EventListener() {

                    @Override
                    public void notify(com.suse.salt.netapi.datatypes.Event event) {
                        // Only listening for close
                    }

                    @Override
                    public void eventStreamClosed(int code, String phrase) {
                        SaltApi.this.eventStreamClosed();
                    }
                });
                if (eventStream.isEventStreamClosed()) {
                    eventStream = null;
                }

                if (retries > 1) {
                    LOG.warn(
                            "Successfully connected to the Salt event bus after {} retries.",
                            retries - 1);
                }
                else {
                    LOG.info("Successfully connected to the Salt event bus");
                }
            }
            catch (SaltException e) {
                try {
                    LOG.error("Unable to connect: {}, retrying in " + DELAY_TIME_SECONDS +
                            " seconds.", e);
                    Thread.sleep(1000 * DELAY_TIME_SECONDS);
                    if (retries == 1) {
                        MailHelper.withSmtp().sendAdminEmail(
                                "Cannot connect to salt event bus",
                                "salt-api daemon is not responding. Check the status of " +
                                        "salt-api daemon and (re)-start it if needed\n\n" +
                                        "This is the only notification you will receive.");
                    }
                }
                catch (JavaMailException javaMailException) {
                    LOG.error("Error sending email: {}", javaMailException.getMessage());
                }
                catch (InterruptedException e1) {
                    LOG.error("Interrupted during sleep: {}", e1);
                }
            }
        }
        return eventStream;
    }

    <R> Optional<Map<String, CompletionStage<Result<R>>>> completableAsyncCall(
            LocalCall<R> callIn, Target<?> target, EventStream events,
            CompletableFuture<GenericError> cancel)
        throws SaltException {
        LocalCall<R> call =
                callIn.withMetadata(ScheduleMetadata.getDefaultMetadata().withBatchMode());
        return SaltApi.adaptException(call.callAsync(saltClient, target,
                SaltApi.PW_AUTH, events, cancel, defaultBatch));
    }
}
