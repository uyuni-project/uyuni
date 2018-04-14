/**
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

import com.redhat.rhn.common.CommonConstants;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerPath;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.suse.manager.webui.controllers.utils.ContactMethodUtil;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;
import com.suse.manager.webui.utils.SaltRoster;
import com.suse.manager.webui.utils.gson.BootstrapParameters;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.SaltSSHConfig;
import com.suse.salt.netapi.calls.modules.Match;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.client.SaltClient;
import com.suse.salt.netapi.datatypes.target.Glob;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.datatypes.target.SSHTarget;
import com.suse.salt.netapi.errors.GenericError;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.results.SSHResult;
import com.suse.salt.netapi.utils.Xor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.text.StrSubstitutor;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Code for calling salt-ssh functions.
 */
public class SaltSSHService {

    private static final String SSH_KEY_DIR = "/srv/susemanager/salt/salt_ssh";
    public static final String SSH_KEY_PATH = SSH_KEY_DIR + "/mgr_ssh_id";
    private static final String PROXY_SSH_PUSH_USER = "mgrsshtunnel";
    private static final String PROXY_SSH_PUSH_KEY =
            "/var/lib/spacewalk/" + PROXY_SSH_PUSH_USER + "/.ssh/id_susemanager_ssh_push";

    private static final int SSL_PORT = 443;

    public static final int SSH_PUSH_PORT = 22;

    private static final Logger LOG = Logger.getLogger(SaltSSHService.class);

    // Shared salt client instance
    private final SaltClient saltClient;

    private Executor asyncSaltSSHExecutor;
    /**
     * Standard constructor.
     * @param saltClientIn salt client to use for the underlying salt calls
     */
    public SaltSSHService(SaltClient saltClientIn) {
        this.saltClient = saltClientIn;
        // use a small fixed pool so we don't overwhelm the salt-api
        // with salt-ssh executions
        this.asyncSaltSSHExecutor = Executors.newFixedThreadPool(3);
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
    public <R> Map<String, Result<R>> callSyncSSH(LocalCall<R> call, MinionList target)
            throws SaltException {
        // Using custom LocalCall timeout if included in the payload
        Optional<Integer> sshTimeout = call.getPayload().containsKey("timeout") ?
                    Optional.ofNullable((Integer) call.getPayload().get("timeout")) :
                    getSshPushTimeout();
        SaltRoster roster = prepareSaltRoster(target, sshTimeout);
        return unwrapSSHReturn(
                callSyncSSHInternal(call, target, roster, false, isSudoUser(getSSHUser())));
    }

    private SaltRoster prepareSaltRoster(MinionList target, Optional<Integer> sshTimeout) {
        SaltRoster roster = new SaltRoster();

        // these values are mostly fixed, which should change when we allow configuring
        // per-minionserver
        target.getTarget().stream()
            .forEach(mid -> {
                if (SSHMinionsPendingRegistrationService.containsMinion(mid)) {
                    SSHMinionsPendingRegistrationService.get(mid).ifPresent(minion -> {
                        roster.addHost(mid, getSSHUser(), Optional.empty(),
                                Optional.of(SSH_PUSH_PORT),
                                remotePortForwarding(minion.getProxyPath().orElse(null),
                                        minion.getContactMethod()),
                                sshProxyCommandOption(
                                        minion.getProxyPath().orElse(null),
                                        minion.getContactMethod(),
                                        mid),
                                sshTimeout,
                                minionOpts(mid, minion.getContactMethod())
                        );
                    });
                }
                else {
                    Optional<MinionServer> minionOpt = MinionServerFactory.
                            findByMinionId(mid);
                    minionOpt.ifPresent(minion -> {
                        List<String> proxyPath = proxyPathToHostnames(
                                minion.getServerPaths(), Optional.empty());
                        roster.addHost(mid, getSSHUser(), Optional.empty(),
                                Optional.of(SSH_PUSH_PORT),
                                remotePortForwarding(
                                        proxyPath, minion.getContactMethod().getLabel()
                                ),
                                sshProxyCommandOption(proxyPath,
                                        minion.getContactMethod().getLabel(),
                                        minion.getMinionId()
                                ),
                                sshTimeout,
                                minionOpts(mid, minion.getContactMethod().getLabel())
                        );
                    });
                    if (!minionOpt.isPresent()) {
                        LOG.error("Minion id='" + mid + "' not found in the database");
                    }
                }
            }
        );
        return roster;
    }

    /**
     * @param serverPaths a set ot {@link ServerPath}
     * @param lastProxy the last proxy in the chain
     * @return a list of proxy hostnames, the last one beeing connected
     * dirrectly to the minion
     */
    public static List<String> proxyPathToHostnames(Set<ServerPath> serverPaths,
                                                    Server lastProxy) {
        String lastProxyHostname = lastProxy.getHostname();
        return proxyPathToHostnames(serverPaths, Optional.of(lastProxyHostname));
    }

    /**
     * @param serverPaths a set ot {@link ServerPath}
     * @param lastProxy the last proxy in the chain
     * @return a list of proxy hostnames, the last one being connected
     * directly to the minion
     */
    public static List<String> proxyPathToHostnames(Set<ServerPath> serverPaths,
                                                    Optional<String> lastProxy) {
        if (CollectionUtils.isEmpty(serverPaths) && !lastProxy.isPresent()) {
            return Collections.emptyList();
        }
        List<ServerPath> proxyPath = serverPaths == null ?
                new ArrayList<>() : new ArrayList<>(serverPaths);
        Collections.sort(proxyPath, (p1, p2) ->
            -ObjectUtils.compare(p1.getPosition(), p2.getPosition()));
        List<String> hostnamePath = new ArrayList<>();
        hostnamePath.addAll(proxyPath.stream().map(p -> p.getHostname())
                .collect(Collectors.toList()));
        lastProxy.ifPresent(p -> hostnamePath.add(p));
        return hostnamePath;
    }

    /**
     * Generate the <Code>ProxyCommand</Code> string for connecting via proxies.
     * @param proxyPath a list of proxy hostnames
     * @param contactMethod the contect method
     * @param minionHostname the hostname of the minion
     * @return the <Code>ProxyCommand</Code> string used by salt-ssh to connect
     * to the minion.
     */
    public static Optional<String> sshProxyCommandOption(List<String> proxyPath,
                                                         String contactMethod,
                                                         String minionHostname) {
        if (CollectionUtils.isEmpty(proxyPath)) {
            return Optional.empty();
        }
        boolean tunnel = ContactMethodUtil.SSH_PUSH_TUNNEL.equals(contactMethod);
        StringBuilder proxyCommand = new StringBuilder();
        proxyCommand.append("ProxyCommand='");
        for (int i = 0; i < proxyPath.size(); i++) {
            String proxyHostname = proxyPath.get(i);
            String key;
            String stdioFwd = "";
            if (i == 0) {
                key = SSH_KEY_PATH;
            }
            else {
                key = PROXY_SSH_PUSH_KEY;
            }
            if (!tunnel && i == proxyPath.size() - 1) {
                stdioFwd = String.format("-W %s:%s", minionHostname, SSH_PUSH_PORT);
            }

            proxyCommand.append(String.format(
                    "/usr/bin/ssh -i %s -o StrictHostKeyChecking=no -o User=%s %s %s ",
                    key, PROXY_SSH_PUSH_USER, stdioFwd, proxyHostname));
        }
        if (tunnel) {
            Map<String, String> values = new HashMap<>();
            values.put("pushKey", PROXY_SSH_PUSH_KEY);
            values.put("user", getSSHUser());
            values.put("pushPort", getSshPushRemotePort() + "");
            values.put("proxy", proxyPath.get(proxyPath.size() - 1));
            values.put("sslPort", SSL_PORT + "");
            values.put("minion", minionHostname);
            values.put("ownKey",
                    ("root".equals(getSSHUser()) ? "/root" : "/home/" + getSSHUser()) +
                            "/.ssh/mgr_own_id");
            values.put("sshPort", SSH_PUSH_PORT + "");

            StrSubstitutor sub = new StrSubstitutor(values);
            proxyCommand.append(
                sub.replace("/usr/bin/ssh -i ${pushKey} -o StrictHostKeyChecking=no " +
                        "-o User=${user} -R ${pushPort}:${proxy}:${sslPort} ${minion} " +
                            "ssh -i ${ownKey} -W ${minion}:${sshPort} " +
                            "-o StrictHostKeyChecking=no -o User=${user} ${minion}"));
        }
        proxyCommand.append("'");
        return Optional.of(proxyCommand.toString());
    }

    /**
     * Executes salt-ssh calls in another thread and returns {@link CompletionStage}s.
     * @param call the salt call
     * @param target the minion list target
     * @param <R> result type of the salt function
     * @param cancel a future used to cancel waiting
     * @return the result of the call
     */
    public <R> Map<String, CompletionStage<Result<R>>> callAsyncSSH(
            LocalCall<R> call, MinionList target, CompletableFuture<GenericError> cancel) {
        SaltRoster roster = prepareSaltRoster(target, getSshPushTimeout());
        Map<String, CompletableFuture<Result<R>>> futures = new HashedMap();
        target.getTarget().forEach(minionId ->
            futures.put(minionId, new CompletableFuture<>())
        );
        CompletableFuture<Map<String, Result<R>>> asyncCallFuture =
                CompletableFuture.supplyAsync(() -> {
            try {
                return unwrapSSHReturn(
                        callSyncSSHInternal(call, target, roster,
                                false, isSudoUser(getSSHUser())));
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
     * Synchronously executes a salt function on given glob using salt-ssh.
     *
     * Before the execution, this method creates an one-time roster corresponding all
     * minions with the ssh contact method and minions being currently bootstrapped.
     *
     * @param call the salt call
     * @param target the minion list target
     * @param <R> result type of the salt function
     * @return the result of the call
     * @throws SaltException if something goes wrong during command execution or
     * during manipulation the salt-ssh roster
     */
    public <R> Map<String, Result<R>> callSyncSSH(LocalCall<R> call, Glob target)
            throws SaltException {
        SaltRoster roster = createAllServersRoster();
        return unwrapSSHReturn(
                callSyncSSHInternal(call, target, roster, false, isSudoUser(getSSHUser())));
    }

    /**
     * Helper method for creating a salt roster containing all minions with ssh contact
     * method and all minions being currently bootstrapped.
     * @return roster
     */
    private SaltRoster createAllServersRoster() {
        SaltRoster roster = new SaltRoster();

        // Add temporary systems
        SSHMinionsPendingRegistrationService.getMinions().forEach((mid, minion) ->
                        roster.addHost(mid,
                                getSSHUser(),
                                Optional.empty(),
                                Optional.of(SSH_PUSH_PORT),
                                remotePortForwarding(minion.getProxyPath().orElse(null),
                                        minion.getContactMethod()),
                                sshProxyCommandOption(
                                        minion.getProxyPath().orElse(null),
                                        minion.getContactMethod(),
                                        mid),
                                getSshPushTimeout(),
                                minionOpts(mid, minion.getContactMethod()))
                );

        // Add systems from the database, possible duplicates in roster will be overwritten
        addSaltSSHMinionsFromDb(roster);

        return roster;
    }

    private boolean addSaltSSHMinionsFromDb(SaltRoster roster) {
        List<MinionServer> minions = MinionServerFactory
                .listSSHMinions();
        minions.forEach(minion -> {
            List<String> proxyPath = proxyPathToHostnames(minion.getServerPaths(),
                    Optional.empty());
            roster.addHost(minion.getMinionId(),
                    getSSHUser(),
                    Optional.empty(),
                    Optional.of(SSH_PUSH_PORT),
                    remotePortForwarding(proxyPath,
                            minion.getContactMethod().getLabel()),
                    sshProxyCommandOption(proxyPath,
                            minion.getContactMethod().getLabel(),
                            minion.getMinionId()),
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
    public Result<SSHResult<Map<String, State.ApplyResult>>> bootstrapMinion(
            BootstrapParameters parameters, List<String> bootstrapMods,
            Map<String, Object> pillarData) throws SaltException {
        LOG.info("Bootstrapping host: " + parameters.getHost());
        LocalCall<Map<String, State.ApplyResult>> call = State.apply(
                bootstrapMods,
                Optional.of(pillarData),
                Optional.of(true));

        List<String> bootstrapProxyPath = parameters.getProxyId()
                .map(proxyId -> ServerFactory.lookupById(proxyId))
                .map(proxy ->
                        proxyPathToHostnames(proxy.getServerPaths(), proxy)
                )
                .orElse(Collections.emptyList());

        Optional<String> portForwarding = parameters.getFirstActivationKey()
                .map(ActivationKeyFactory::lookupByKey)
                .map(key -> key.getContactMethod().getLabel())
                .flatMap(contactMethod ->
                        remotePortForwarding(bootstrapProxyPath, contactMethod));

        SaltRoster roster = new SaltRoster();
        roster.addHost(parameters.getHost(), parameters.getUser(), parameters.getPassword(),
                parameters.getPort(),
                portForwarding,
                sshProxyCommandOption(bootstrapProxyPath,
                        ContactMethodUtil.SSH_PUSH,
                        parameters.getHost()),
                getSshPushTimeout(),
                minionOpts(parameters.getHost(), ContactMethodUtil.SSH_PUSH));

        Map<String, Result<SSHResult<Map<String, State.ApplyResult>>>> result =
                callSyncSSHInternal(call,
                        new MinionList(parameters.getHost()),
                        roster,
                        parameters.isIgnoreHostKeys(),
                        isSudoUser(parameters.getUser()));
        return result.get(parameters.getHost());
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
        if (ContactMethodUtil.SSH_PUSH_TUNNEL.equals(sshContactMethod) &&
                CollectionUtils.isEmpty(proxyPath)) {
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
    private <T> Map<String, Result<T>> unwrapSSHReturn(Map<String,
            Result<SSHResult<T>>> sshResults) {
         return sshResults.entrySet().stream()
                .collect(Collectors.toMap(
                        kv -> kv.getKey(),
                        kv -> kv.getValue().fold(
                                err -> new Result<T>(Xor.left(err)),
                                succ -> new Result<T>(Xor.right(succ.getReturn().get())))));
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
            SSHTarget target, SaltRoster roster, boolean ignoreHostKeys, boolean sudo)
            throws SaltException {
        if (!(target instanceof MinionList || target instanceof Glob)) {
            throw new UnsupportedOperationException("Only MinionList and Glob supported.");
        }

        Path rosterPath = null;
        try {
            rosterPath = roster.persistInTempFile();
            SaltSSHConfig sshConfig = new SaltSSHConfig.Builder()
                    .ignoreHostKeys(ignoreHostKeys)
                    .rosterFile(rosterPath.getFileName().toString())
                    .priv(SSH_KEY_PATH)
                    .sudo(sudo)
                    .refreshCache(true)
                    .build();

            return call.callSyncSSH(saltClient, target, sshConfig);
        }
        catch (IOException e) {
            LOG.error("Error operating on roster file: " + e.getMessage());
            throw new SaltException(e);
        }
        finally {
            if (rosterPath != null) {
                try {
                    Files.deleteIfExists(rosterPath);
                }
                catch (IOException e) {
                    LOG.error("Can't delete roster file: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Retrieve the public key used for ssh-push from the given proxy.
     * @param proxyId id of the proxy
     * @return the content of the public key
     */
    public static Optional<String> retrieveSSHPushProxyPubKey(long proxyId) {
        Server proxy = ServerFactory.lookupById(proxyId);
        String keyFile = proxy.getHostname() + ".pub";
        List<String> proxyPath = proxyPathToHostnames(proxy.getServerPaths(),
                Optional.of(proxy.getHostname()));

        Map<String, String> options = new HashMap<>();
        options.put("StrictHostKeyChecking", "no");
        options.put("ConnectTimeout", ConfigDefaults.get().getSaltSSHConnectTimeout() + "");
        Optional<MgrUtilRunner.ExecResult> ret = SaltService.INSTANCE
                .chainSSHCommand(proxyPath,
                        SSH_KEY_PATH,
                        PROXY_SSH_PUSH_KEY,
                        PROXY_SSH_PUSH_USER,
                        options,
                        "cat " + PROXY_SSH_PUSH_KEY + ".pub",
                        SSH_KEY_DIR + "/" + keyFile);
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
                                roster,
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
}
