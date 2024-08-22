/*
 * Copyright (c) 2016 SUSE LLC
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
package com.suse.manager.webui.services.impl;

import static com.suse.manager.webui.services.SaltActionChainGeneratorService.ACTIONCHAIN_SLS_FOLDER;
import static com.suse.manager.webui.services.SaltConstants.SALT_FS_PREFIX;
import static java.util.Collections.singletonList;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.CommonConstants;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerPath;
import com.redhat.rhn.domain.token.ActivationKeyFactory;

import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.controllers.StatesAPI;
import com.suse.manager.webui.controllers.utils.ContactMethodUtil;
import com.suse.manager.webui.services.FutureUtils;
import com.suse.manager.webui.services.SaltActionChainGeneratorService;
import com.suse.manager.webui.services.SaltConstants;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;
import com.suse.manager.webui.utils.ActionSaltState;
import com.suse.manager.webui.utils.SaltModuleRun;
import com.suse.manager.webui.utils.SaltRoster;
import com.suse.manager.webui.utils.SaltState;
import com.suse.manager.webui.utils.SaltTop;
import com.suse.manager.webui.utils.gson.BootstrapParameters;
import com.suse.manager.webui.utils.salt.custom.MgrActionChains;
import com.suse.salt.netapi.AuthModule;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.SaltSSHConfig;
import com.suse.salt.netapi.calls.modules.Match;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.calls.modules.State.ApplyResult;
import com.suse.salt.netapi.client.SaltClient;
import com.suse.salt.netapi.datatypes.AuthMethod;
import com.suse.salt.netapi.datatypes.PasswordAuth;
import com.suse.salt.netapi.datatypes.target.Glob;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.datatypes.target.SSHTarget;
import com.suse.salt.netapi.errors.GenericError;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.results.SSHResult;
import com.suse.salt.netapi.results.StateApplyResult;
import com.suse.salt.netapi.utils.Xor;
import com.suse.utils.Opt;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Code for calling salt-ssh functions.
 */
public class SaltSSHService {

    private static final String SSH_KEY_DIR = "/var/lib/salt/.ssh";
    private static final String PUB_SSH_KEY_DIR = "/srv/susemanager/salt/salt_ssh";
    public static final String SSH_KEY_PATH = SSH_KEY_DIR + "/mgr_ssh_id";
    public static final String SSH_PUBKEY_PATH = SSH_KEY_DIR + "/mgr_ssh_id.pub";
    public static final String SUMA_SSH_PUB_KEY = PUB_SSH_KEY_DIR + "/mgr_ssh_id.pub";
    private static final Path SSH_TEMP_BOOTSTRAP_KEY_DIR = Path.of(SSH_KEY_DIR,  "temp_bootstrap_keys");
    private static final String PROXY_SSH_PUSH_USER = "mgrsshtunnel";
    private static final String PROXY_SSH_PUSH_KEY =
            "/var/lib/spacewalk/" + PROXY_SSH_PUSH_USER + "/.ssh/id_susemanager_ssh_push";

    private static final int SSL_PORT = 443;

    public static final int SSH_DEFAULT_PORT = 22;
    public static final int SSH_PUSH_PORT = SSH_DEFAULT_PORT;

    private static final Logger LOG = LogManager.getLogger(SaltSSHService.class);
    private static final String CLEANUP_SSH_MINION_SALT_STATE = "cleanup_ssh_minion";

    public static final List<String> ACTION_STATES_LIST = Arrays.asList(
            "certs", "channels", "cleanup_ssh_minion", "configuration",
            "distupgrade", "hardware", "images", "packages/init.sls",
            "packages/patchdownload.sls", "packages/patchinstall.sls", "packages/pkgdownload.sls",
            "packages/pkginstall.sls", "packages/pkgupdate.sls", "packages/pkgremove.sls", "packages/profileupdate.sls",
            "packages/redhatproductinfo.sls", "remotecommands", "scap", "services", "gpg",
            "custom", "custom_groups", "custom_org", "util", "bootstrap", "formulas.sls");

    public static final String ACTION_STATES = ACTION_STATES_LIST
            .stream()
            .map(state -> SALT_FS_PREFIX + state)
            .collect(Collectors.joining(","));

    public static final List<String> DEFAULT_TOPS = Arrays.asList(
            "channels",
            "certs",
            "packages",
            "custom",
            "custom_groups",
            "custom_org",
            "formulas",
            "services.salt-minion",
            "services.docker");
    private final String SALT_USER = "admin";
    private final String SALT_PASSWORD = com.redhat.rhn.common.conf.Config.get().getString("server.secret_key");
    private final AuthModule AUTH_MODULE = AuthModule.FILE;

    private final AuthMethod PW_AUTH = new AuthMethod(new PasswordAuth(SALT_USER, SALT_PASSWORD, AuthModule.FILE));

    // Shared salt client instance
    private final SaltClient saltClient;

    private Executor asyncSaltSSHExecutor;

    private SaltActionChainGeneratorService saltActionChainGeneratorService;

    /**
     * Standard constructor.
     * @param saltClientIn salt client to use for the underlying salt calls
     * @param saltActionChainGeneratorServiceIn the action chain file generator service
     */
    public SaltSSHService(SaltClient saltClientIn, SaltActionChainGeneratorService saltActionChainGeneratorServiceIn) {
        this.saltClient = saltClientIn;
        // use a small fixed pool so we don't overwhelm the salt-api
        // with salt-ssh executions
        this.asyncSaltSSHExecutor = Executors.newFixedThreadPool(3);
        this.saltActionChainGeneratorService = saltActionChainGeneratorServiceIn;
    }

    /**
     * Returns the user that should be used for the ssh calls done by salt-ssh.
     * @return the user
     */
    public static String getSSHUser() {
        String sudoUser = Config.get().getString(ConfigDefaults.CONFIG_KEY_SUDO_USER);
        return StringUtils.isBlank(sudoUser) ? CommonConstants.ROOT : sudoUser;
    }

    /**
     * Returns the path to pre flight script used by salt-ssh.
     * @return the pre flight script path
     */
    public static String getSaltSSHPreflightScriptPath() {
        String preflightScriptPath = Config.get().getString("ssh_salt_pre_flight_script");
        return StringUtils.isBlank(preflightScriptPath) ? null : preflightScriptPath;
    }

    /**
     * Returns true if salt-thin should be used to handle salt-ssh session
     * @return the result of the call
     */
    public static boolean getSSHUseSaltThin() {
        return Config.get().getBoolean("ssh_use_salt_thin");
    }

    /**
     * Synchronously executes a salt function on given minion list using salt-ssh.
     *
     * Before the execution, this method creates an one-time roster corresponding to targets
     * in given minion list.
     *
     * @param call the salt call
     * @param target the minion list target
     * @param extraFileRefs --extra-fileresfs salt-ssh param
     * @param <R> result type of the salt function
     * @return the result of the call
     * @throws SaltException if something goes wrong during command execution or
     * during manipulation the salt-ssh roster
     */
    public <R> Map<String, Result<R>> callSyncSSH(LocalCall<R> call, MinionList target, Optional<String> extraFileRefs)
            throws SaltException {
        // Using custom LocalCall timeout if included in the payload
        Optional<Integer> sshTimeout = call.getPayload().containsKey("timeout") ?
                Optional.ofNullable((Integer) call.getPayload().get("timeout")) :
                    getSshPushTimeout();
        Optional<SaltRoster> roster = prepareSaltRoster(target, sshTimeout);
        return unwrapSSHReturn(
                callSyncSSHInternal(call, target, roster, false, isSudoUser(getSSHUser()), extraFileRefs));
    }

    /**
     * Synchronously executes a salt function on given minion list using salt-ssh.
     *
     * Before the execution, this method creates an one-time roster corresponding to targets
     * in given minion list.
     *
     * @param call the salt call
     * @param target the minion list target
     * @param <R> result type of the salt function
     * @return the result of the call
     * @throws SaltException if something goes wrong during command execution or
     * during manipulation the salt-ssh roster
     */
    public <R> Map<String, Result<R>> callSyncSSH(LocalCall<R> call, MinionList target) throws SaltException {
        return callSyncSSH(call, target, Optional.empty());
    }

    private Optional<SaltRoster> prepareSaltRoster(MinionList target, Optional<Integer> sshTimeout) {
        SaltRoster roster = new SaltRoster();
        boolean pendingMinion = false;

        // these values are mostly fixed, which should change when we allow configuring
        // per-minion server
        for (String mid : target.getTarget()) {
            if (MinionPendingRegistrationService.containsSSHMinion(mid)) {
                pendingMinion = true;
                MinionPendingRegistrationService.get(mid).ifPresent(minion -> {
                    String contactMethodLabel = minion.getContactMethod();

                    List<String> proxyPath = minion.getProxyPath();

                    roster.addHost(mid, getSSHUser(), Optional.empty(),
                            Optional.empty(), Optional.empty(),
                            minion.getSSHPushPort(),
                            remotePortForwarding(proxyPath, contactMethodLabel),
                            sshProxyCommandOption(proxyPath,
                                contactMethodLabel,
                                mid,
                                minion.getSSHPushPort().orElse(SSH_PUSH_PORT)),
                            sshTimeout,
                            minionOpts(mid, contactMethodLabel),
                            Optional.ofNullable(getSaltSSHPreflightScriptPath()),
                            Optional.of(Arrays.asList(
                                    proxyPath.isEmpty() ?
                                            ConfigDefaults.get().getCobblerHost() :
                                            proxyPath.get(proxyPath.size() - 1).split(":")[0],
                                    ContactMethodUtil.SSH_PUSH_TUNNEL.equals(contactMethodLabel) ?
                                            getSshPushRemotePort() : SSL_PORT,
                                    getSSHUseSaltThin() ? 1 : 0
                            ))
                    );
                });
            }
            else {
                MinionServerFactory.findByMinionId(mid).ifPresentOrElse(minion -> {
                    List<String> proxyPath = proxyPathToHostnames(minion.getServerPaths(), Optional.empty());
                    String contactMethodLabel = minion.getContactMethod().getLabel();

                    roster.addHost(mid, getSSHUser(), Optional.empty(),
                            Opt.wrapFirstNonNull(minion.getSSHPushPort(), SSH_PUSH_PORT),
                            remotePortForwarding(proxyPath, contactMethodLabel),
                            sshProxyCommandOption(proxyPath,
                                contactMethodLabel,
                                minion.getMinionId(),
                                Optional.ofNullable(minion.getSSHPushPort()).orElse(SSH_PUSH_PORT)),
                            sshTimeout,
                            minionOpts(mid, contactMethodLabel)
                    );
                }, () -> LOG.error("Minion id='{}' not found in the database", mid));
            }
        }
        // we only need a roster when we may contact pending minions which are not yet in DB
        // otherwise the roster is generated from DB
        return pendingMinion ? Optional.of(roster) : Optional.empty();
    }

    /**
     * Executes salt-ssh calls in another thread and returns {@link CompletionStage}s.
     * @param call the salt call
     * @param target the minion list target
     * @param cancel a future used to cancel waiting
     * @param <R> result type of the salt function
     * @return the result of the call
     */
    public <R> Map<String, CompletionStage<Result<R>>> callAsyncSSH(
            LocalCall<R> call, MinionList target, CompletableFuture<GenericError> cancel) {
        return callAsyncSSH(call, target, cancel, Optional.empty());
    }

    /**
     * Executes salt-ssh calls in another thread and returns {@link CompletionStage}s.
     * @param call the salt call
     * @param target the minion list target
     * @param <R> result type of the salt function
     * @param cancel a future used to cancel waiting
     * @param extraFilerefs value of salt-ssh param --extra-filerefs
     * @return the result of the call
     */
    public <R> Map<String, CompletionStage<Result<R>>> callAsyncSSH(
            LocalCall<R> call, MinionList target, CompletableFuture<GenericError> cancel,
            Optional<String> extraFilerefs) {
        Optional<SaltRoster> roster = prepareSaltRoster(target, getSshPushTimeout());
        Map<String, CompletableFuture<Result<R>>> futures = new HashedMap();
        target.getTarget().forEach(minionId ->
                futures.put(minionId, new CompletableFuture<>())
        );
        CompletableFuture<Map<String, Result<R>>> asyncCallFuture =
                CompletableFuture.supplyAsync(() -> {
                    try {
                        return unwrapSSHReturn(
                                callSyncSSHInternal(call, target, roster,
                                        false, isSudoUser(getSSHUser()),
                                        extraFilerefs));
                    }
                    catch (SaltException e) {
                        LOG.error("Error calling async salt-ssh minions", e);
                        throw new RuntimeException(e);
                    }
                }, asyncSaltSSHExecutor);

        asyncCallFuture.whenComplete((executionResult, err) ->
                futures.forEach((minionId, future) -> {
                    if (err == null) {
                        future.complete(executionResult.get(minionId));
                    }
                    else {
                        future.completeExceptionally(err);
                    }
                })
        );
        cancel.whenComplete((v, e) -> {
            if (v != null) {
                Map<String, Result<R>> error = target.getTarget().stream()
                        .collect(Collectors.toMap(
                                Function.identity(),
                                minionId -> Result.error(v)));
                asyncCallFuture.complete(error);
            }
            else if (e != null) {
                asyncCallFuture.completeExceptionally(e);
            }
        });
        return futures.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> (CompletionStage<Result<R>>) e.getValue()
        ));
    }

    /**
     * @param proxy the proxy server
     * @return a list of proxy hostnames, the last one being connected
     * directly to the minion
     */
    public static List<String> proxyPathToHostnames(Server proxy) {
        String port = Optional.ofNullable(proxy.getProxyInfo().getSshPort()).map(p -> ":" + p).orElse("");
        return proxyPathToHostnames(proxy.getServerPaths(), Optional.of(proxy.getHostname() + port));
    }

    /**
     * @param serverPaths a set ot {@link ServerPath}
     * @param lastProxy the last proxy in the chain
     * @return a list of proxy hostnames, the last one being connected
     * directly to the minion
     */
    public static List<String> proxyPathToHostnames(Set<ServerPath> serverPaths, Optional<String> lastProxy) {
        if (CollectionUtils.isEmpty(serverPaths) && lastProxy.isEmpty()) {
            return Collections.emptyList();
        }

        List<ServerPath> proxyPath = sortServerPaths(serverPaths);
        List<String> hostnamePath = proxyPath.stream().map(path -> {
            String port = Optional.ofNullable(path.getId().getProxyServer().getProxyInfo().getSshPort())
                    .map(p -> ":" + p.toString()).orElse("");
            return path.getHostname() + port;
        }).collect(Collectors.toList());

        lastProxy.ifPresent(hostnamePath::add);

        return hostnamePath;
    }

    private static List<ServerPath> sortServerPaths(Set<ServerPath> serverPaths) {
        if (CollectionUtils.isEmpty(serverPaths)) {
            return new ArrayList<>();
        }

        return serverPaths.stream()
                          .sorted(Comparator.comparing(ServerPath::getPosition).reversed())
                          .collect(Collectors.toList());
    }

    /**
     * Generate the <Code>ProxyCommand</Code> string for connecting via proxies.
     * @param proxyPath a list of proxy hostnames
     * @param contactMethod the contact method
     * @param minionHostname the hostname of the minion
     * @param sshPushPort the ssh port to use to send the command
     * @return the <Code>ProxyCommand</Code> string used by salt-ssh to connect
     * to the minion.
     */
    public static Optional<List<String>> sshProxyCommandOption(List<String> proxyPath,
                                                               String contactMethod,
                                                               String minionHostname,
                                                               int sshPushPort) {
        if (CollectionUtils.isEmpty(proxyPath)) {
            return Optional.empty();
        }
        boolean tunnel = ContactMethodUtil.SSH_PUSH_TUNNEL.equals(contactMethod);
        StringBuilder proxyCommand = new StringBuilder();
        proxyCommand.append("ProxyCommand='");
        for (int i = 0; i < proxyPath.size(); i++) {
            String[] proxyHostPort = proxyPath.get(i).split(":");
            String proxyHostname = proxyHostPort[0];
            String proxyPort = proxyHostPort.length > 1 ? proxyHostPort[1] : Integer.toString(SSH_DEFAULT_PORT);

            String key;
            String stdioFwd = "";
            if (i == 0) {
                key = SSH_KEY_PATH;
            }
            else {
                key = PROXY_SSH_PUSH_KEY;
            }
            if (i == proxyPath.size() - 1) {
                stdioFwd = String.format(" -W %s:%s", minionHostname, sshPushPort);
            }

            proxyCommand.append(String.format(
                    "/usr/bin/ssh -p %s -i %s -o StrictHostKeyChecking=no -o User=%s%s %s ",
                    proxyPort, key, PROXY_SSH_PUSH_USER, stdioFwd, proxyHostname));
        }
        proxyCommand.append("'");
        return Optional.of(Arrays.asList("StrictHostKeyChecking=no", proxyCommand.toString()));
    }

    private boolean addSaltSSHMinionsFromDb(SaltRoster roster) {
        List<MinionServer> minions = MinionServerFactory.listSSHMinions();
        minions.forEach(minion -> {
            List<String> proxyPath = proxyPathToHostnames(minion.getServerPaths(), Optional.empty());
            roster.addHost(minion.getMinionId(),
                    getSSHUser(),
                    Optional.empty(),
                    Opt.wrapFirstNonNull(minion.getSSHPushPort(), SSH_PUSH_PORT),
                    remotePortForwarding(proxyPath, minion.getContactMethod().getLabel()),
                    sshProxyCommandOption(proxyPath,
                        minion.getContactMethod().getLabel(),
                        minion.getMinionId(),
                        Optional.ofNullable(minion.getSSHPushPort()).orElse(SSH_PUSH_PORT)),
                    getSshPushTimeout(),
                    minionOpts(minion.getMinionId(), minion.getContactMethod().getLabel()));
        });
        return !minions.isEmpty();
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

        Optional<String> portForwarding = remotePortForwarding(bootstrapProxyPath, contactMethod);

        // private key handling just for bootstrap
        Optional<Path> tmpKeyFileAbsolutePath = parameters.getPrivateKey().map(key -> createTempKeyFilePath());

        try {
            tmpKeyFileAbsolutePath.ifPresent(p -> parameters.getPrivateKey().ifPresent(k ->
                    GlobalInstanceHolder.SALT_API.storeSshKeyFile(p, k)));
            SaltRoster roster = new SaltRoster();
            roster.addHost(parameters.getHost(),
                    parameters.getUser(),
                    parameters.getPassword(),
                    tmpKeyFileAbsolutePath.map(Path::toString),
                    parameters.getPrivateKeyPassphrase(),
                    parameters.getPort(),
                    portForwarding,
                    sshProxyCommandOption(bootstrapProxyPath,
                        contactMethod,
                        parameters.getHost(),
                        parameters.getPort().orElse(SSH_PUSH_PORT)),
                    getSshPushTimeout(),
                    minionOpts(parameters.getHost(), contactMethod),
                    Optional.ofNullable(getSaltSSHPreflightScriptPath()),
                    Optional.of(Arrays.asList(
                            bootstrapProxyPath.isEmpty() ?
                                    ConfigDefaults.get().getCobblerHost() :
                                    bootstrapProxyPath.get(bootstrapProxyPath.size() - 1).split(":")[0],
                            ContactMethodUtil.SSH_PUSH_TUNNEL.equals(contactMethod) ?
                                    getSshPushRemotePort() : SSL_PORT,
                            getSSHUseSaltThin() ? 1 : 0,
                            1
                            ))
                    );

            Map<String, Result<SSHResult<Map<String, ApplyResult>>>> result =
                    callSyncSSHInternal(call,
                            new MinionList(parameters.getHost()),
                            Optional.of(roster),
                            parameters.isIgnoreHostKeys(),
                            isSudoUser(parameters.getUser()));
            return result.get(parameters.getHost());
        }
        finally {
            tmpKeyFileAbsolutePath.ifPresent(this::cleanUpTempKeyFile);
        }
    }

    // create temp key absolute path
    private Path createTempKeyFilePath() {
        if (!GlobalInstanceHolder.SALT_API.mkDir(SSH_TEMP_BOOTSTRAP_KEY_DIR, "0700").orElse(false)) {
            LOG.error("Unable to create temp ssh bootstrap directory");
            return null;
        }
        String fileName = "boostrapKeyTmp-" + UUID.randomUUID();
        return SSH_TEMP_BOOTSTRAP_KEY_DIR.resolve(fileName).toAbsolutePath();
    }

    private void cleanUpTempKeyFile(Path path) {
        GlobalInstanceHolder.SALT_API
                .removeFile(path)
                .orElseThrow(() -> new IllegalStateException("Can't remove file " + path));
    }

    private Optional<Map<String, Object>> minionOpts(String minionId,
                                                    String sshContactMethod) {
        if (ContactMethodUtil.SSH_PUSH_TUNNEL.equals(sshContactMethod)) {
            Map<String, Object> options = new LinkedHashMap<>();
            options.put("master", minionId);
            return Optional.of(options);
        }
        return Optional.empty();
    }

    private Optional<String> remotePortForwarding(List<String> proxyPath,
                                                  String sshContactMethod) {
        if (ContactMethodUtil.SSH_PUSH_TUNNEL.equals(sshContactMethod)) {
            return Optional.of(getSshPushRemotePort() + ":" +
                    ConfigDefaults.get().getCobblerHost() + ":" + SSL_PORT);
        }
        return Optional.empty();
    }

    private static Integer getSshPushRemotePort() {
        return Config.get().getInt("ssh_push_port_https");
    }

    private boolean isSudoUser(String user) {
        return !CommonConstants.ROOT.equals(user);
    }

    private static Optional<Integer> getSshPushTimeout() {
        return Optional.ofNullable(ConfigDefaults.get().getSaltSSHConnectTimeout());
    }

    /**
     * Return the Map of Result objects that contain either the error from SSHResult or the
     * unwrapped return value from SSHResult.
     */
    @SuppressWarnings("java:S2293")
    private <T> Map<String, Result<T>> unwrapSSHReturn(Map<String,
            Result<SSHResult<T>>> sshResults) {
         return sshResults.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        kv -> kv.getValue().fold(
                                err -> new Result<T>(Xor.left(err)),
                                succ -> {
                                    if (succ.getReturn().isPresent()) {
                                        return new Result<T>(Xor.right(
                                                succ.getReturn().get()));
                                    }
                                    else {
                                        return new Result<T>(Xor.left(
                                                succ.getStderr().map(stderr ->
                                                        new GenericError("Error unwrapping ssh return: " + stderr))
                                                        .orElse(new GenericError(
                                                                "Error unwrapping ssh return: no return value"))));
                                    }
                                })));
    }

    /**
     * Boilerplate for executing a synchronous salt-ssh code. This involves:
     * - generating the salt config,
     * - generating the roster, storing it on the disk,
     * - calling the salt-ssh via salt-api,
     * - cleaning up the roster file after the job is done.
     *
     * Note on the SSH identity (key/cert pair):
     * This call uses the SSH key stored on SSH_KEY_PATH. If such file doesn't
     * exist, salt automatically generates a key/cert pair on this path.
     *
     * @param <T> the return type of the call
     * @param call the call to execute
     * @param target minions targeted by the call, only Glob and MinionList is supported
     * @param roster salt-ssh roster
     * @param ignoreHostKeys use this option to disable 'StrictHostKeyChecking'
     * @param sudo run command via sudo (default: false)
     *
     * @throws SaltException if something goes wrong during command execution or
     * during manipulation the salt-ssh roster
     *
     * @return result of the call
     */
    private <T> Map<String, Result<SSHResult<T>>> callSyncSSHInternal(LocalCall<T> call,
            SSHTarget target, Optional<SaltRoster> roster, boolean ignoreHostKeys, boolean sudo)
            throws SaltException {
        return callSyncSSHInternal(call, target, roster, ignoreHostKeys, sudo, Optional.empty());
    }

    private <T> Map<String, Result<SSHResult<T>>> callSyncSSHInternal(LocalCall<T> call,
            SSHTarget target, Optional<SaltRoster> roster, boolean ignoreHostKeys, boolean sudo,
            Optional<String> extraFilerefs) throws SaltException {
        if (!(target instanceof MinionList || target instanceof Glob)) {
            throw new UnsupportedOperationException("Only MinionList and Glob supported.");
        }

        try {
            final Path rosterPath;
            if (roster.isPresent()) {
                rosterPath = roster.get().persistInTempFile();
            }
            else {
                rosterPath = null;
            }

            SaltSSHConfig.Builder sshConfigBuilder = new SaltSSHConfig.Builder()
                    .ignoreHostKeys(ignoreHostKeys)
                    .priv(SSH_KEY_PATH)
                    .sudo(sudo)
                    .refreshCache(true);
            roster.ifPresentOrElse(
                    r -> sshConfigBuilder.rosterFile(rosterPath.getFileName().toString()),
                    () -> {
                        sshConfigBuilder.roster("uyuni");
                        LOG.info("No roster file used, using Uyuni roster module!");
                    });
            extraFilerefs.ifPresent(sshConfigBuilder::extraFilerefs);
            SaltSSHConfig sshConfig = sshConfigBuilder.build();

            LOG.debug("Local callSyncSSH: {}", SaltService.localCallToString(call));
            return SaltService.adaptException(call.callSyncSSH(saltClient, target, sshConfig, PW_AUTH)
                    .whenComplete((r, e) -> {
                        if (roster.isPresent()) {
                            try {
                                Files.deleteIfExists(rosterPath);
                            }
                            catch (IOException ex) {
                                LOG.error("Can't delete roster file: {}", ex.getMessage());
                            }
                        }
                    }));
        }
        catch (IOException e) {
            LOG.error("Error operating on roster file: {}", e.getMessage());
            throw new SaltException(e);
        }
    }

    /**
     * Executes match.glob in another thread and returns a {@link CompletionStage}.
     * @param target the target to pass to match.glob
     * @param cancel a future used to cancel waiting
     * @return a future or Optional.empty if there's no ssh-push minion in the db
     */
    public Optional<CompletionStage<Map<String, Result<Boolean>>>> matchAsyncSSH(
        String target, CompletableFuture<GenericError> cancel) {
        SaltRoster roster = new SaltRoster();
        boolean added = addSaltSSHMinionsFromDb(roster);
        if (!added) {
            return Optional.empty();
        }
        CompletableFuture<Map<String, Result<Boolean>>> f =
                CompletableFuture.supplyAsync(() -> {
            try {
                return unwrapSSHReturn(
                        callSyncSSHInternal(Match.glob(target),
                                new Glob(target),
                                Optional.empty(),
                                false,
                                isSudoUser(getSSHUser())));
            }
            catch (SaltException e) {
                LOG.error("Error matching salt-ssh minions", e);
                throw new RuntimeException(e);
            }
        }, asyncSaltSSHExecutor);
        cancel.whenComplete((v, e) -> {
            if (v != null) {
                Result<Boolean> error = Result.error(v);
                f.complete(Collections.singletonMap("", error));
            }
            else if (e != null) {
                f.completeExceptionally(e);
            }
        });
        return Optional.of(f);
    }

    /**
     * Get the cached ssh public key used for ssh-push from the given proxy
     * or retrieve it from the proxy if not cached.
     * @param proxyId id of the proxy
     * @return the content of the public key
     */
    public static Optional<String> getOrRetrieveSSHPushProxyPubKey(long proxyId) {
        Server proxy = ServerFactory.lookupById(proxyId);
        String keyFile = proxy.getHostname() + ".pub";
        if (Files.exists(Paths.get(PUB_SSH_KEY_DIR, keyFile))) {
            return Optional.of(keyFile);
        }
        return retrieveSSHPushProxyPubKey(proxyId);
    }

    /**
     * Retrieve the public key used for ssh-push from the given proxy.
     * @param proxyId id of the proxy
     * @return the content of the public key
     */
    public static Optional<String> retrieveSSHPushProxyPubKey(long proxyId) {
        Server proxy = ServerFactory.lookupById(proxyId);
        String keyFile = proxy.getHostname() + ".pub";
        List<String> proxyPath = proxyPathToHostnames(proxy);

        Map<String, String> options = new HashMap<>();
        options.put("StrictHostKeyChecking", "no");
        options.put("ConnectTimeout", ConfigDefaults.get().getSaltSSHConnectTimeout() + "");
        Optional<MgrUtilRunner.ExecResult> ret = GlobalInstanceHolder.SALT_API
                .chainSSHCommand(proxyPath,
                        SSH_KEY_PATH,
                        PROXY_SSH_PUSH_KEY,
                        PROXY_SSH_PUSH_USER,
                        options,
                        "cat " + PROXY_SSH_PUSH_KEY + ".pub",
                        PUB_SSH_KEY_DIR + "/" + keyFile);
        if (ret.map(MgrUtilRunner.ExecResult::getReturnCode).orElse(-1) == 0) {
            return Optional.of(keyFile);
        }
        else {
            String msg = ret.map(r ->
                    "Could not retrieve ssh pub key from proxy [" + proxy.getHostname() +
                    "]. ssh return code [" + r.getReturnCode() +
                    "[, stderr [" + r.getStderr() + "]")
                    .orElse("Could not retrieve ssh pub key from proxy " +
                            proxy.getHostname() + ". Please check the logs.");
            LOG.error(msg);
            throw new RuntimeException(msg);
        }
    }

    /**
     * Remove SUSE Manager specific configuration from a Salt ssh minion.
     *
     * @param minion the minion.
     * @param timeout operation timeout
     * @return list of error messages or empty if no error
     */
    public Optional<List<String>> cleanupSSHMinion(MinionServer minion, int timeout) {
        CompletableFuture<GenericError> timeoutAfter = FutureUtils.failAfter(timeout);
        try {
            Map<String, Object> pillarData = new HashMap<>();
            if (!minion.getServerPaths().isEmpty()) {
                List<ServerPath> paths = sortServerPaths(minion.getServerPaths());
                ServerPath last = paths.get(paths.size() - 1);

                SaltSSHService.getOrRetrieveSSHPushProxyPubKey(
                        last.getId().getProxyServer().getId())
                        .ifPresent(key ->
                                pillarData.put("proxy_pub_key", key));
            }
            pillarData.put("mgr_sudo_user", getSSHUser());
            Map<String, CompletionStage<Result<Map<String, ApplyResult>>>> res =
                    callAsyncSSH(
                            State.apply(
                                    Collections.singletonList(CLEANUP_SSH_MINION_SALT_STATE),
                                    Optional.of(pillarData)),
                            new MinionList(minion.getMinionId()), timeoutAfter);
            CompletionStage<Result<Map<String, ApplyResult>>> future =
                    res.get(minion.getMinionId());
            if (future == null) {
                return Optional.of(
                        singletonList("apply_result_missing"));
            }

            return future.handle((applyResult, err) -> {
                if (applyResult != null) {
                    List<String> result = applyResult.fold(
                        (saltErr) -> singletonList(SaltUtils.decodeSaltErr(saltErr).getMessage()),
                        (saltRes) -> saltRes.values().stream()
                                            .filter(value -> !value.isResult())
                                            .map(StateApplyResult::getComment)
                                            .collect(Collectors.toList())
                    );

                    return result.isEmpty() ? Optional.<List<String>>empty() : Optional.of(result);
                }
                else if (err instanceof TimeoutException) {
                    return Optional.of(singletonList(SaltService.MINION_UNREACHABLE_ERROR));
                }
                else {
                    return Optional.of(singletonList(err.getMessage()));
                }
            }).toCompletableFuture().get();

        }
        catch (InterruptedException | ExecutionException e) {
            LOG.error("Error applying state ssh_cleanup", e);
            return Optional.of(singletonList(e.getMessage()));
        }

    }

    /**
     * Collect all salt:// file refs from the salt states.
     * @param actionChainId id of the action chain
     * @param chunksPerMinion chunks for each minion
     * @param statesPerMinion states for each minion
     * @return a comma separated list of all the salt:// file refs
     */
    public String findStatesExtraFilerefs(long actionChainId, Map<MinionSummary, Integer> chunksPerMinion,
                                          Map<MinionSummary, List<SaltState>> statesPerMinion) {
        Set<String> fileRefs = statesPerMinion.entrySet().stream()
                .flatMap(entry ->
                        entry.getValue().stream()
                                .flatMap(state -> gatherSaltFileRefs(state.getData()).stream())
                                .collect(Collectors.toSet()).stream())
                .collect(Collectors.toSet());

        // add packages/package_<minion_machine_id>
        Set<String> pkgRefs = statesPerMinion.entrySet().stream()
                .map(entry -> SALT_FS_PREFIX + SaltConstants.SALT_PACKAGES_STATES_DIR + "/" +
                        StatesAPI.getPackagesSlsName(entry.getKey().getMachineId()))
                .collect(Collectors.toSet());


        Set<String> actionChainSls = chunksPerMinion.entrySet().stream()
                .flatMap(entry ->
                        IntStream.range(0, entry.getValue())
                                .mapToObj(chunk -> SALT_FS_PREFIX + ACTIONCHAIN_SLS_FOLDER + "/" +
                                        SaltActionChainGeneratorService
                                                .getActionChainSLSFileName(actionChainId, entry.getKey(), chunk + 1))
                ).collect(Collectors.toSet());

        String extraFileRefs = ACTION_STATES + "," + Stream.concat(pkgRefs.stream(),
                Stream.concat(actionChainSls.stream(), fileRefs.stream()))
                .collect(Collectors.joining(","));

        return extraFileRefs;
    }

    private Collection<String> gatherSaltFileRefs(Map<String, Object> data) {
        Collection<String> fileRefs = new LinkedList<>();
        gatherSaltFileRefs(data, fileRefs, 0);
        return fileRefs;
    }

    private void gatherSaltFileRefs(Map<String, Object> data, Collection<String> fileRefs, int depth) {
        if (depth > 50) {
            return; // guard against infinite recursion
        }
        data.forEach((key, val) -> {
            if (val instanceof String) {
                gatherSaltFileRefs((String)val, fileRefs);
            }
            else if (val instanceof List) {
                gatherSaltFileRefs((List)val, fileRefs, depth + 1);
            }
            else if (val instanceof Map) {
                gatherSaltFileRefs((Map)val, fileRefs, depth + 1);
            }
        });
    }

    private void gatherSaltFileRefs(List<Object> data, Collection<String> fileRefs, int depth) {
        if (depth > 50) {
            return; // guard against infinite recursion
        }
        for (Object val : data) {
            if (val instanceof String) {
                gatherSaltFileRefs((String) val, fileRefs);
            }
            else if (val instanceof List) {
                gatherSaltFileRefs((List)val, fileRefs, depth + 1);
            }
            else if (val instanceof Map) {
                gatherSaltFileRefs((Map)val, fileRefs, depth + 1);
            }
        }
    }

    private void gatherSaltFileRefs(String val, Collection<String> fileRefs) {
        if (val.startsWith(SALT_FS_PREFIX)) {
            fileRefs.add(val);
        }
    }

    /**
     * Collect apply highstate actions for each minion
     * @param statesPerMinion states for each minion
     * @return the apply highstate action ids for each minion
     */
    public Map<MinionSummary, List<Long>> findApplyHighstateActionsPerMinion(Map<MinionSummary,
            List<SaltState>> statesPerMinion) {
        return statesPerMinion.entrySet().stream()
                .filter(entry -> entry.getValue().stream().anyMatch(SaltSSHService::isApplyHighstate))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue()
                                .stream()
                                .filter(SaltSSHService::isApplyHighstate)
                                .filter(state -> state instanceof ActionSaltState)
                                .map(state -> ((ActionSaltState)state).getActionId())
                        .collect(Collectors.toList())
                ));
    }

    private static boolean isApplyHighstate(SaltState state) {
        if (state instanceof SaltModuleRun) {
            SaltModuleRun moduleRun = (SaltModuleRun)state;
            return "state.top".equals(moduleRun.getName());
        }
        return false;
    }

    /**
     * Generate the top file to use for applying highstate actions in action chains executed via salt-ssh.
     * @param actionChainId the action chain id
     * @param actionId the action id
     * @return a tuple containing the salt:// reference to the top file and the content of the top
     */
    public Pair<String, List<String>> generateTopFile(long actionChainId, long actionId) {
        String saltTopPath = saltActionChainGeneratorService
                .generateTop(actionChainId, actionId, new SaltTop(DEFAULT_TOPS));
        return new ImmutablePair<>(saltTopPath, DEFAULT_TOPS);
    }

    /**
     * Remove any pending action chain execution from the given minion by calling mgractionchains.clean asynchronously.
     * @param minion the minion
     */
    public void cleanPendingActionChainAsync(MinionServer minion) {
        LOG.warn("Cleaning up pending action chain execution on ssh minion {}", minion.getMinionId());
        CompletableFuture<GenericError> cancel =
                FutureUtils.failAfter(ConfigDefaults.get().getSaltSSHConnectTimeout());
        Map<String, CompletionStage<Result<Map<String, Boolean>>>> completionStages =
                callAsyncSSH(MgrActionChains.clean(), new MinionList(minion.getMinionId()), cancel);
        completionStages.forEach((minionId, future) ->
                future.whenComplete((res, err) -> {
                    if (res != null) {
                        res.fold(e -> {
                                    LOG.warn("Pending action chain execution cleanup failed for minion {}", minionId);
                            return null;
                            },
                                r -> {
                                    LOG.debug("Pending action chain execution cleaned up for minion {}", minionId);
                            return null;
                        });
                    }
                    else if (err != null) {
                        LOG.error("Error cleaning up pending action chain execution on minion {}. Remove directory " +
                                "/var/tmp/.root_XXXX_salt/minion.d manually. ", minion.getMinionId(), err);
                    }
                }));
    }

    /**
     * Remove server hostname from ssh known_hosts of the proxy where this server is connected to.
     *
     * @param server the server to remove from proxy ssh known_hosts
     * @return return true on success and false otherwise
     */
    public static boolean cleanupKnownHostsFromProxy(Server server) {
        if (server == null || server.getServerPaths().isEmpty()) {
            return true;
        }
        String hostname = server.getHostname();
        List<ServerPath> paths = sortServerPaths(server.getServerPaths());
        ServerPath last = paths.get(paths.size() - 1);
        Server proxy = last.getId().getProxyServer();
        List<String> proxyPath = proxyPathToHostnames(proxy);

        Map<String, String> options = new HashMap<>();
        options.put("StrictHostKeyChecking", "no");
        options.put("ConnectTimeout", ConfigDefaults.get().getSaltSSHConnectTimeout() + "");
        Optional<MgrUtilRunner.ExecResult> ret = GlobalInstanceHolder.SALT_API
                .chainSSHCommand(proxyPath,
                        SSH_KEY_PATH,
                        PROXY_SSH_PUSH_KEY,
                        PROXY_SSH_PUSH_USER,
                        options,
                        "/usr/bin/ssh-keygen -R " + hostname,
                        null);
        if (ret.map(MgrUtilRunner.ExecResult::getReturnCode).orElse(-1) != 0) {
            String msg = ret.map(r -> "Failed to remove [" + hostname +
                            "]. from ssh known_hosts on proxy [" + proxy.getHostname() +
                            "] return code [" + r.getReturnCode() +
                            "[, stderr [" + r.getStderr() + "]")
                    .orElse("Could not remove " + hostname + " from ssh known_hosts on proxy " +
                            proxy.getHostname() + ". Please check the logs.");
            LOG.error(msg);
            return false;
        }
        return true;
    }
}
