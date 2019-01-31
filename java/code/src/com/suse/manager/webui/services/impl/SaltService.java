/**
 * Copyright (c) 2015 SUSE LLC
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

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.manager.audit.scap.file.ScapFileManager;

import com.suse.manager.reactor.PGEventStream;
import com.suse.manager.reactor.SaltReactor;
import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.utils.MinionServerUtils;
import com.suse.manager.webui.controllers.utils.ContactMethodUtil;
import com.suse.manager.webui.services.SaltActionChainGeneratorService;
import com.suse.manager.webui.services.impl.runner.MgrK8sRunner;
import com.suse.manager.webui.services.impl.runner.MgrKiwiImageRunner;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;
import com.suse.manager.webui.utils.gson.BootstrapParameters;
import com.suse.manager.webui.utils.salt.State;
import com.suse.manager.webui.utils.salt.custom.ScheduleMetadata;
import com.suse.salt.netapi.AuthModule;
import com.suse.salt.netapi.calls.LocalAsyncResult;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.RunnerCall;
import com.suse.salt.netapi.calls.WheelCall;
import com.suse.salt.netapi.calls.WheelResult;
import com.suse.salt.netapi.calls.modules.Cmd;
import com.suse.salt.netapi.calls.modules.Config;
import com.suse.salt.netapi.calls.modules.Grains;
import com.suse.salt.netapi.calls.modules.Match;
import com.suse.salt.netapi.calls.modules.SaltUtil;
import com.suse.salt.netapi.calls.modules.State.ApplyResult;
import com.suse.salt.netapi.calls.modules.Status;
import com.suse.salt.netapi.calls.modules.Test;
import com.suse.salt.netapi.calls.runner.Jobs;
import com.suse.salt.netapi.calls.wheel.Key;
import com.suse.salt.netapi.client.SaltClient;
import com.suse.salt.netapi.client.impl.HttpAsyncClientImpl;
import com.suse.salt.netapi.datatypes.AuthMethod;
import com.suse.salt.netapi.datatypes.Batch;
import com.suse.salt.netapi.datatypes.PasswordAuth;
import com.suse.salt.netapi.datatypes.Token;
import com.suse.salt.netapi.datatypes.target.Glob;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.datatypes.target.Target;
import com.suse.salt.netapi.errors.GenericError;
import com.suse.salt.netapi.errors.SaltError;
import com.suse.salt.netapi.event.EventStream;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.results.SSHResult;
import com.suse.utils.Opt;

import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Singleton class acting as a service layer for accessing the salt API.
 */
public class SaltService {

    private final Optional<Batch> defaultBatch;

    /**
     * Singleton instance of this class
     */
    public static final SaltService INSTANCE = new SaltService();

    // Logger
    private static final Logger LOG = Logger.getLogger(SaltService.class);

    // Salt properties
    private final URI SALT_MASTER_URI = URI.create("http://localhost:9080");
    private final String SALT_USER = "admin";
    private final String SALT_PASSWORD = "";
    private final AuthModule AUTH_MODULE = AuthModule.AUTO;

    // Shared salt client instance
    private final SaltClient SALT_CLIENT;

    // executing salt-ssh calls
    private final SaltSSHService saltSSHService;
    private final AuthMethod PW_AUTH = new AuthMethod(new PasswordAuth(SALT_USER, SALT_PASSWORD, AuthModule.AUTO));

    private static final Predicate<? super String> SALT_MINION_PREDICATE = (mid) ->
            MinionPendingRegistrationService.containsSSHMinion(mid) ||
                        MinionServerFactory
                                .findByMinionId(mid)
                                .filter(m -> MinionServerUtils.isSshPushMinion(m))
                                .isPresent();

    private static final String CLEANUP_MINION_SALT_STATE = "cleanup_minion";
    protected static final String MINION_UNREACHABLE_ERROR = "minion_unreachable";

    private SaltReactor reactor = null;

    private final ScheduledExecutorService scheduledExecutorService =
            Executors.newScheduledThreadPool(5);

    /**
     * Enum of all the available status for Salt keys.
     */
    public enum KeyStatus {
        ACCEPTED, DENIED, UNACCEPTED, REJECTED
    }

    // Prevent instantiation
    SaltService() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(0)
                .setSocketTimeout(0)
                .setConnectionRequestTimeout(0)
                .setCookieSpec(CookieSpecs.STANDARD)
                .build();
        HttpAsyncClientBuilder httpClientBuilder = HttpAsyncClients.custom();
        httpClientBuilder.setDefaultRequestConfig(requestConfig);

        CloseableHttpAsyncClient asyncHttpClient = httpClientBuilder
                .setMaxConnPerRoute(20)
                .setMaxConnTotal(20)
                .build();
        asyncHttpClient.start();

        SALT_CLIENT = new SaltClient(SALT_MASTER_URI, new HttpAsyncClientImpl(asyncHttpClient));
        saltSSHService = new SaltSSHService(SALT_CLIENT, SaltActionChainGeneratorService.INSTANCE);
        defaultBatch = Optional.of(Batch.asAmount(ConfigDefaults.get().getSaltBatchSize())
                .delayed(ConfigDefaults.get().getSaltBatchDelay())
        );
    }

    /**
     * @param reactorIn the Salt reactor
     */
    public void setReactor(SaltReactor reactorIn) {
        this.reactor = reactorIn;
    }

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
    public <R> Optional<R> callSync(LocalCall<R> call, String minionId) {
        try {
            Map<String, Result<R>> stringRMap = callSync(call, new MinionList(minionId));

            return Opt.fold(Optional.ofNullable(stringRMap.get(minionId)), () -> {
                LOG.warn("Got no result for " + call.getPayload().get("fun") +
                        " on minion " + minionId + " (minion did not respond in time)");
                return Optional.<R>empty();
            }, r ->
                r.fold(error -> {
                    LOG.warn(error.toString());
                    return Optional.<R>empty();
                }, Optional::of)
            );
        }
        catch (SaltException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Executes a salt runner module function. On error it
     * logs the error and returns an empty result.
     *
     * @param call salt function to call
     * @param <R> result type of the salt function
     * @return the result of the call or empty on error
     */
    public <R> Optional<R> callSync(RunnerCall<R> call) {
        return callSync(call, p ->
                p.fold(
                        e -> {
                            LOG.error("Function [" + e.getFunctionName() +
                                    "] not available for runner call " +
                                    runnerCallToString(call)
                            );
                            return Optional.empty();
                        },
                        e -> {
                            LOG.error("Module [" + e.getModuleName() +
                                    "] not supported for runner call " +
                                    runnerCallToString(call)
                            );
                            return Optional.empty();
                        },
                        e -> {
                            LOG.error("Error parsing json response from runner call " +
                                    runnerCallToString(call) +
                                    ": " + e.getJson());
                            return Optional.empty();
                        },
                        e -> {
                            LOG.error("Generic Salt error for runner call " +
                                    runnerCallToString(call) +
                                    ": " + e.getMessage());
                            return Optional.empty();
                        }
                ));
    }

    private String runnerCallToString(RunnerCall<?> call) {
        return "[" + call.getModuleName() + "." +
                call.getFunctionName() + "] with payload [" +
                call.getPayload() + "]";
    }

    /**
     * Executes a salt runner module function. On error it
     * invokes the {@code errorHandler} passed as parameter.
     *
     * @param call salt function to call
     * @param errorHandler function that handles errors
     * @param <R> result type of the salt function
     * @return the result of the call or empty on error
     */
    public <R> Optional<R> callSync(RunnerCall<R> call,
                                    Function<SaltError, Optional<R>> errorHandler) {
        try {
            Result<R> result = adaptException(call.callSync(SALT_CLIENT, PW_AUTH));
            return result.fold(p -> errorHandler.apply(p),
                    r -> Optional.of(r)
            );
        }
        catch (SaltException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the minion keys from salt with their respective status.
     *
     * @return the keys with their respective status as returned from salt
     */
    public Key.Names getKeys() {
        return callSync(Key.listAll())
                .orElseThrow(() -> new RuntimeException("no wheel results"));
    }

    /**
     * Executes a salt wheel module function.
     *
     * @param call wheel call
     * @param <R> result type of the wheel call
     * @return the result of the call or empty on error
     */
    public <R> Optional<R> callSync(WheelCall<R> call) {
        return callSync(call,  p ->
                p.fold(
                    e -> {
                        LOG.error("Function [" + e.getFunctionName() +
                                "] not available for wheel call " +
                                wheelCallToString(call)
                        );
                        return Optional.empty();
                    },
                    e -> {
                        LOG.error("Module [" + e.getModuleName() +
                                "] not supported for wheel call " +
                                wheelCallToString(call)
                        );
                        return Optional.empty();
                    },
                    e -> {
                        LOG.error("Error parsing json response from wheel call " +
                                wheelCallToString(call) +
                                ": " + e.getJson());
                        return Optional.empty();
                    },
                    e -> {
                        LOG.error("Generic Salt error for wheel call " +
                                wheelCallToString(call) +
                                ": " + e.getMessage());
                        return Optional.empty();
                    }
        ));
    }

    /**
     * Executes a salt wheel module function. On error it
     * invokes the {@code errorHandler} passed as parameter.
     *
     * @param call wheel call
     * @param errorHandler function that handles errors
     * @param <R> result type of the wheel call
     * @return the result of the call or empty on error
     */
    public <R> Optional<R> callSync(WheelCall<R> call,
                                     Function<SaltError, Optional<R>> errorHandler) {
        try {
            WheelResult<Result<R>> result = adaptException(call.callSync(SALT_CLIENT, PW_AUTH));
            return result.getData().getResult().fold(
                    err -> errorHandler.apply(err),
                    r -> Optional.of(r));
        }
        catch (SaltException e) {
            throw new RuntimeException(e);
        }
    }

    private String wheelCallToString(WheelCall<?> call) {
        return "[" + call.getModuleName() + "." +
                call.getFunctionName() + "] with payload [" +
                call.getPayload() + "]";
    }

    /**
     * For a given minion id check if there is a key in any of the given status. If no status is given as parameter,
     * all the available status are considered.
     *
     * @param id the id to check for
     * @param statusIn array of key status to consider
     * @return true if there is a key with the given id, false otherwise
     */
    public boolean keyExists(String id, KeyStatus... statusIn) {
        final Set<KeyStatus> status = new HashSet<>(Arrays.asList(statusIn));
        if (status.isEmpty()) {
            status.addAll(Arrays.asList(KeyStatus.values()));
        }

        Key.Names keys = getKeys();
        return status.contains(KeyStatus.ACCEPTED) && keys.getMinions().contains(id) ||
                status.contains(KeyStatus.DENIED) && keys.getDeniedMinions().contains(id) ||
                status.contains(KeyStatus.UNACCEPTED) && keys.getUnacceptedMinions().contains(id) ||
                status.contains(KeyStatus.REJECTED) && keys.getRejectedMinions().contains(id);
    }

    /**
     * Get the minion keys from salt with their respective status and fingerprint.
     *
     * @return the keys with their respective status and fingerprint as returned from salt
     */
    public Key.Fingerprints getFingerprints() {
        return callSync(Key.finger("*"))
                .orElseThrow(() -> new RuntimeException("no wheel results"));
    }

    /**
     * Generate a key pair for the given id and accept the public key.
     *
     * @param id the id to use
     * @param force set true to overwrite an already existing key
     * @return the generated key pair
     */
    public Key.Pair generateKeysAndAccept(String id,
            boolean force) {
        return callSync(Key.genAccept(id, Optional.of(force)))
                .orElseThrow(() -> new RuntimeException("no wheel results"));
    }

    /**
     * Get the grains for a given minion.
     *
     * @param minionId id of the target minion
     * @return map containing the grains
     */
    public Optional<Map<String, Object>> getGrains(String minionId) {
        return callSync(Grains.items(false), minionId);
    }

    /**
     * Get the machine id for a given minion.
     *
     * @param minionId id of the target minion
     * @return the machine id as a string
     */
    public Optional<String> getMachineId(String minionId) {
        return getGrain(minionId, "machine_id").flatMap(grain -> {
          if (grain instanceof String) {
              return Optional.of((String) grain);
          }
          else {
              LOG.warn("Minion " + minionId + " returned non string: " +
                      grain + " as minion_id");
              return Optional.empty();
          }
        });
    }

    /**
     * Accept all keys matching the given pattern
     *
     * @param match a pattern for minion ids
     */
    public void acceptKey(String match) {
        callSync(Key.accept(match))
                .orElseThrow(() -> new RuntimeException("no wheel results"));
    }

    /**
     * Delete a given minion's key.
     *
     * @param minionId id of the minion
     */
    public void deleteKey(String minionId) {
        callSync(Key.delete(minionId))
                .orElseThrow(() -> new RuntimeException("no wheel results"));
    }

    /**
     * Reject a given minion's key.
     *
     * @param minionId id of the minion
     */
    public void rejectKey(String minionId) {
        callSync(Key.reject(minionId))
                .orElseThrow(() -> new RuntimeException("no wheel results"));
    }

    /**
     * Return the stream of events happening in salt.
     *
     * @return the event stream
     * @throws SaltException exception occured during connection (if any)
     */
    public EventStream getEventStream() throws SaltException {
        if (ConfigDefaults.get().isPostgresql()) {
            return new PGEventStream();
        }
        Token token = adaptException(SALT_CLIENT.login(SALT_USER, SALT_PASSWORD, AUTH_MODULE));
        return SALT_CLIENT.events(token, 0, 0, 0);
    }

    /**
     * Get a given grain's value from a given minion.
     *
     * @param minionId id of the target minion
     * @param grain name of the grain
     * @return the grain value
     */
    private Optional<Object> getGrain(String minionId, String grain) {
        return callSync(Grains.item(true, grain), minionId).flatMap(grains ->
           Optional.ofNullable(grains.get(grain))
        );
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
            throw new RuntimeException(e);
        }
    }

    private <R> Optional<Map<String, CompletionStage<Result<R>>>> completableAsyncCall(
            LocalCall<R> callIn, Target<?> target, EventStream events,
            CompletableFuture<GenericError> cancel) throws SaltException {

        LocalCall<R> call = Opt.fold(defaultBatch, () -> callIn,
                b -> callIn.withMetadata(ScheduleMetadata.getDefaultMetadata().withBatchMode()));

        return adaptException(call.callAsync(SALT_CLIENT, target, PW_AUTH, events, cancel, defaultBatch));
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

        HashSet<String> uniqueMinionIds = new HashSet<>(target.getTarget());
        Map<Boolean, List<String>> minionPartitions =
                partitionMinionsByContactMethod(uniqueMinionIds);

        List<String> sshMinionIds = minionPartitions.get(true);
        List<String> regularMinionIds = minionPartitions.get(false);
        Map<String, CompletionStage<Result<String>>> results =
                new HashMap<>();
        LocalCall<String> call = Cmd.run(cmd);
        if (!sshMinionIds.isEmpty()) {
            results.putAll(
                saltSSHService.callAsyncSSH(
                        call,
                        new MinionList(sshMinionIds),
                        cancel));
        }

        if (!regularMinionIds.isEmpty()) {
            try {
                results.putAll(
                        completableAsyncCall(call, target,
                        reactor.getEventStream(), cancel).orElseGet(Collections::emptyMap));
            }
            catch (SaltException e) {
                throw new RuntimeException(e);
            }
        }

        return results;
    }

    /**
     * Create a {@link CompletableFuture} that completes exceptionally after
     * the given number of seconds.
     * @param seconds the seconds after which it completes exceptionally
     * @return a {@link CompletableFuture}
     */
    public CompletableFuture failAfter(int seconds) {
        final CompletableFuture promise = new CompletableFuture();
        scheduledExecutorService.schedule(() -> {
            final TimeoutException ex = new TimeoutException("Timeout after " + seconds);
            return promise.completeExceptionally(ex);
        }, seconds, TimeUnit.SECONDS);

        return promise;
    }

    /**
     * Returns the currently running jobs on the target
     *
     * @param target the target
     * @return list of running jobs
     */
    public Map<String, Result<List<SaltUtil.RunningInfo>>> running(MinionList target) {
        try {
            return callSync(SaltUtil.running(), target);
        }
        catch (SaltException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Return the jobcache filtered by metadata
     *
     * @param metadata search metadata
     * @return list of running jobs
     */
    public Optional<Map<String, Jobs.ListJobsEntry>> jobsByMetadata(Object metadata) {
        return callSync(Jobs.listJobs(metadata));
    }

    /**
     * Return the jobcache filtered by metadata and start and end time.
     *
     * @param metadata search metadata
     * @param startTime jobs start time
     * @param endTime jobs end time
     * @return list of running jobs
     */
    public Optional<Map<String, Jobs.ListJobsEntry>> jobsByMetadata(Object metadata,
                                                                    LocalDateTime startTime, LocalDateTime endTime) {
        return callSync(Jobs.listJobs(metadata, startTime, endTime));
    }

    /**
     * Return the result for a jobId
     *
     * @param jid the job id
     * @return map from minion to result
     */
    public Optional<Jobs.Info> listJob(String jid) {
        return callSync(Jobs.listJob(jid));
    }

    /**
     * Match the given target expression asynchronously.
     * @param target the target expression
     * @param cancel  a future used to cancel waiting on return events
     * @return a map holding a {@link CompletionStage}s for each minion
     */
    public Map<String, CompletionStage<Result<Boolean>>> matchAsync(
            String target, CompletableFuture<GenericError> cancel) {
        try {
            return completableAsyncCall(Match.glob(target), new Glob(target),
                    reactor.getEventStream(), cancel).orElseGet(Collections::emptyMap);
        }
        catch (SaltException e) {
            throw new RuntimeException(e);
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
        return saltSSHService.matchAsyncSSH(target, cancel);
    }

    /**
     * Call 'saltutil.sync_beacons' to sync the beacons to the target minion(s).
     * @param minionList minionList
     */
    public void syncBeacons(MinionList minionList) {
        try {
            LocalCall<List<String>> call = SaltUtil.syncBeacons(
                            Optional.of(true),
                            Optional.empty());

            callSync(call, minionList);
        }
        catch (SaltException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Call 'saltutil.refresh_pillar' to sync the grains to the target minion(s).
     * @param minionList minion list
     */
    public void refreshPillar(MinionList minionList) {
        try {
            LocalCall<Boolean> call = SaltUtil.refreshPillar(Optional.empty(),
                    Optional.empty());
            callAsync(call, minionList);
        }
        catch (SaltException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Call 'saltutil.sync_grains' to sync the grains to the target minion(s).
     * @param minionList minion list
     */
    public void syncGrains(MinionList minionList) {
        try {
            LocalCall<List<String>> call = SaltUtil.syncGrains(Optional.empty(),
                    Optional.empty());
            callSync(call, minionList);
        }
        catch (SaltException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Call 'saltutil.sync_modules' to sync the grains to the target minion(s).
     * @param minionList minion list
     */
    public void syncModules(MinionList minionList) {
        try {
            LocalCall<List<String>> call = SaltUtil.syncModules(Optional.empty(),
                    Optional.empty());
            callSync(call, minionList);
        }
        catch (SaltException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Execute a LocalCall synchronously on the default Salt client.
     * Note that salt-ssh systems are also called by this method.
     *
     * @param <T> the return type of the call
     * @param call the call to execute
     * @param target minions targeted by the call
     * @return the result of the call
     * @throws SaltException in case of an error executing the job with Salt
     */
    public <T> Map<String, Result<T>> callSync(LocalCall<T> call, MinionList target)
            throws SaltException {
        HashSet<String> uniqueMinionIds = new HashSet<>(target.getTarget());
        Map<Boolean, List<String>> minionPartitions =
                partitionMinionsByContactMethod(uniqueMinionIds);

        List<String> sshMinionIds = minionPartitions.get(true);
        List<String> regularMinionIds = minionPartitions.get(false);

        Map<String, Result<T>> results = new HashMap<>();

        if (!sshMinionIds.isEmpty()) {
            results.putAll(saltSSHService.callSyncSSH(
                    call,
                    new MinionList(sshMinionIds)));
        }

        if (!regularMinionIds.isEmpty()) {
            List<Map<String, Result<T>>> callResult =
                    adaptException(call.callSync(SALT_CLIENT,
                            new MinionList(regularMinionIds), PW_AUTH, defaultBatch));
            results.putAll(
                    callResult.stream().flatMap(map -> map.entrySet().stream())
                            .collect(Collectors.toMap(Entry<String, Result<T>>::getKey,
                                    Entry<String, Result<T>>::getValue))
            );
        }

        return results;
    }

    /**
     * Partitions minion ids according to the contact method of corresponding minions
     * (salt-ssh minions in one partition, regular minions in the other).
     *
     * @param minionIds minion ids
     * @return map with partitioning
     */
    public static Map<Boolean, List<String>> partitionMinionsByContactMethod(
            Collection<String> minionIds) {
        return minionIds.stream()
                .collect(Collectors.partitioningBy(SALT_MINION_PREDICATE));
    }

    /**
     * Execute a LocalCall asynchronously on the default Salt client,
     * without passing any metadata on the call.
     *
     * @param <T> the return type of the call
     * @param call the call to execute
     * @param target minions targeted by the call
     * @return the LocalAsyncResult of the call
     * @throws SaltException in case of an error executing the job with Salt
     */
    public <T> Optional<LocalAsyncResult<T>> callAsync(LocalCall<T> call, Target<?> target)
            throws SaltException {
        return callAsync(call, target, Optional.empty());
    }

    /**
     * Execute a LocalCall asynchronously on the default Salt client.
     *
     * @param <T> the return type of the call
     * @param callIn the call to execute
     * @param target minions targeted by the call
     * @param metadata the metadata to be passed in the call
     * @return the LocalAsyncResult of the call
     * @throws SaltException in case of an error executing the job with Salt
     */
    public <T> Optional<LocalAsyncResult<T>> callAsync(LocalCall<T> callIn, Target<?> target,
            Optional<ScheduleMetadata> metadata) throws SaltException {
        LocalCall<T> call = Opt.fold(metadata, () -> callIn, m -> Opt.fold(defaultBatch,
                () -> callIn.withMetadata(m), b -> callIn.withMetadata(m.withBatchMode())));

        return adaptException(call.callAsync(SALT_CLIENT, target, PW_AUTH, defaultBatch));
    }

    /**
     * Pings a target set of minions.
     * @param targetIn the target
     * @return the LocalAsyncResult of the test.ping call
     * @throws SaltException if we get a failure from Salt
     */
    public Optional<LocalAsyncResult<Boolean>> ping(MinionList targetIn) throws SaltException {
        try {
            LocalCall<Boolean> call = Test.ping();
            return callAsync(call, targetIn);
        }
        catch (SaltException e) {
            throw new RuntimeException(e);
        }
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

    /**
     * Retrieves the uptime of the minion (in seconds).
     *
     * @param minion the minion
     * @return Optional with the uptime in seconds or empty on error or if salt returned
     * no value.
     */
    public Optional<Long> getUptimeForMinion(MinionServer minion) {
        Optional<Long> uptime = Optional.empty();
        try {
            uptime = callSync(
                    Status.uptime(),
                    minion.getMinionId())
                    .map(r -> ((Number) r.get("seconds")).longValue());

            if (!uptime.isPresent()) {
                LOG.error("Can't get uptime for " + minion.getMinionId());
            }
        }
        catch (RuntimeException e) {
            LOG.error(e);
        }
        return uptime;
    }

    /**
     * Apply util.systeminfo state on the specified minion list
     * @param minionTarget minion list
     */
    public void updateSystemInfo(MinionList minionTarget) {
        try {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put(ScheduleMetadata.SUMA_MINION_STARTUP, true);
            callAsync(State.apply(Arrays.asList(ApplyStatesEventMessage.SYSTEM_INFO),
                            Optional.empty()).withMetadata(metadata), minionTarget);
        }
        catch (SaltException ex) {
            LOG.debug("Error while executing util.systeminfo state: " + ex.getMessage());
        }
    }

    /**
     * Gets a minion's master hostname.
     *
     * @param minionId the minion id
     * @return the master hostname
     */
    public Optional<String> getMasterHostname(String minionId) {
        return callSync(Config.get(Config.MASTER), minionId);
    }

    /**
     * Apply a state synchronously.
     * @param minionId the minion id
     * @param state the state to apply
     * @return the result of applying the state
     */
    public Optional<Map<String, ApplyResult>> applyState(
            String minionId, String state) {
        return callSync(State.apply(Arrays.asList(state), Optional.empty()), minionId);
    }

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
    public Result<SSHResult<Map<String, ApplyResult>>> bootstrapMinion(
            BootstrapParameters parameters, List<String> bootstrapMods,
            Map<String, Object> pillarData) throws SaltException {
        return saltSSHService.bootstrapMinion(parameters, bootstrapMods, pillarData);
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
    public Map<Boolean, String> storeMinionScapFiles(
            MinionServer minion, String uploadDir, Long actionId) {
        String actionPath = ScapFileManager
                .getActionPath(minion.getOrg().getId(),
                        minion.getId(), actionId);
        Path mountPoint = Paths.get(com.redhat.rhn.common.conf.Config.get()
                .getString(ConfigDefaults.MOUNT_POINT));
        try {
            // create dirs
            Path actionDir = Files.createDirectories(mountPoint.resolve(actionPath));

            UserPrincipalLookupService lookupService = FileSystems.getDefault()
                    .getUserPrincipalLookupService();
            GroupPrincipal susemanagerGroup = lookupService
                    .lookupPrincipalByGroupName("susemanager");
            GroupPrincipal wwwGroup = lookupService
                    .lookupPrincipalByGroupName("www");
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
            LOG.error("Error creating dir " + mountPoint.resolve(actionPath), e);
        }

        RunnerCall<Map<Boolean, String>> call = MgrUtilRunner.moveMinionUploadedFiles(
                minion.getMinionId(),
                uploadDir,
                com.redhat.rhn.common.conf.Config.get()
                        .getString(ConfigDefaults.MOUNT_POINT),
                actionPath);
        Optional<Map<Boolean, String>> result = callSync(call,
                err -> err.fold(
                        e -> {
                            LOG.error("Function [" + e.getFunctionName() +
                                    " not available for runner call " +
                                    "[mgrutil.move_minion_uploaded_files].");
                            return Optional.of(Collections.singletonMap(false,
                                    "Function [" + e.getFunctionName()));
                        },
                        e -> {
                            LOG.error("Module [" + e.getModuleName() +
                                    "] not supported for runner call " +
                                    "[mgrutil.move_minion_uploaded_files].");
                            return Optional.of(Collections.singletonMap(false,
                                    "Module [" + e.getModuleName() + "] not supported"));
                        },
                        e -> {
                            LOG.error("Error parsing json response from " +
                                    "runner call [mgrutil.move_minion_uploaded_files]: " +
                                    e.getJson());
                            return Optional.of(Collections.singletonMap(false,
                                    "Error parsing json response: " + e.getJson()));
                        },
                        e -> {
                            LOG.error("Generic Salt error for runner call " +
                                    "[mgrutil.move_minion_uploaded_files]: " +
                                    e.getMessage());
                            return Optional.of(Collections.singletonMap(false,
                                    "Generic Salt error: " + e.getMessage()));
                        }
                )
        );
        return result.orElseGet(() ->
                Collections.singletonMap(false, "Error moving scap result files." +
                        " Please check the logs.")
        );
    }

    private void changeGroupAndPerms(Path dir, GroupPrincipal group) {
        PosixFileAttributeView posixAttrs = Files
                .getFileAttributeView(dir,
                        PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        try {
            posixAttrs.setPermissions(PosixFilePermissions.fromString("rwxrwxr-x"));
        }
        catch (IOException e) {
            LOG.warn(String.format("Could not set 'rwxrwxr-x' permissions on %s: %s",
                    dir, e.getMessage()));
        }
        try {
            posixAttrs.setGroup(group);
        }
        catch (IOException e) {
            LOG.warn(String.format("Could not set group on %s to %s: %s",
                    dir, group, e.getMessage()));
        }
    }

    /**
     * Call the custom mgrutil.ssh_keygen runner if the key files are not present.
     *
     * @param path of the key files
     * @return the result of the runner call as a map
     */
    public Optional<MgrUtilRunner.ExecResult> generateSSHKey(String path) {
        File pubKey = new File(path + ".pub");
        if (!pubKey.isFile()) {
            RunnerCall<MgrUtilRunner.ExecResult> call = MgrUtilRunner.generateSSHKey(path);
            return callSync(call);
        }
        return Optional.of(MgrUtilRunner.ExecResult.success());
    }

    /**
     * Delete a Salt key from the "Rejected Keys" category using the mgrutil runner.
     *
     * @param minionId the minionId to look for in "Rejected Keys"
     * @return the result of the runner call as a map
     */
    public Optional<MgrUtilRunner.ExecResult> deleteRejectedKey(String minionId) {
        RunnerCall<MgrUtilRunner.ExecResult> call = MgrUtilRunner.deleteRejectedKey(minionId);
        return callSync(call);
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
    public Optional<MgrUtilRunner.ExecResult> chainSSHCommand(List<String> hosts,
                                                    String clientKey,
                                                    String proxyKey,
                                                    String user,
                                                    Map<String, String> options,
                                                    String command,
                                                    String outputfile) {
        RunnerCall<MgrUtilRunner.ExecResult> call =
                MgrUtilRunner.chainSSHCommand(
                        hosts, clientKey, proxyKey, user, options, command, outputfile);
        return callSync(call);
    }

    /**
     * Get information about all containers running in a Kubernetes cluster.
     * @param kubeconfig path to the kubeconfig file
     * @param context kubeconfig context to use
     * @return a list of containers
     */
    public Optional<MgrK8sRunner.ContainersList> getAllContainers(String kubeconfig,
                                                        String context) {
        RunnerCall<MgrK8sRunner.ContainersList> call =
                MgrK8sRunner.getAllContainers(kubeconfig, context);
        return callSync(call,
                err -> err.fold(
                    e -> {
                        LOG.error("Function [" + e.getFunctionName() +
                                " not available for runner call " +
                                "[mgrk8s.get_all_containers].");
                        throw new NoSuchElementException();
                    },
                    e -> {
                        LOG.error("Module [" + e.getModuleName() +
                                "] not supported for runner call " +
                                "[mgrk8s.get_all_containers].");
                        throw new NoSuchElementException();
                    },
                    e -> {
                        LOG.error("Error parsing json response from " +
                                "runner call [mgrk8s.get_all_containers]: " +
                                e.getJson());
                        throw new NoSuchElementException();
                    },
                    e -> {
                        LOG.error("Generic Salt error for runner call " +
                                "[mgrk8s.get_all_containers]: " +
                                e.getMessage());
                        throw new NoSuchElementException();
                    }
                )
        );
    }

    /**
     * Remove SUSE Manager specific configuration from a Salt minion.
     *
     * @param minion the minion.
     * @param timeout operation timeout
     * @return list of error messages or empty if no error
     */
    public Optional<List<String>> cleanupMinion(MinionServer minion,
                                                   int timeout) {
        boolean sshPush = Stream.of(
                ServerFactory.findContactMethodByLabel(ContactMethodUtil.SSH_PUSH),
                ServerFactory.findContactMethodByLabel(ContactMethodUtil.SSH_PUSH_TUNNEL)
        ).anyMatch(cm -> minion.getContactMethod().equals(cm));

        if (sshPush) {
            return saltSSHService.cleanupSSHMinion(minion, timeout);
        }
        return this.cleanupRegularMinion(minion);
    }

    /**
     * Remove SUSE Manager specific configuration from a Salt regular minion.
     *
     * @param minion the minion.
     * @return list of error messages or empty if no error
     */
    private Optional<List<String>> cleanupRegularMinion(MinionServer minion) {
        Optional<Map<String, ApplyResult>> response = applyState(minion.getMinionId(), CLEANUP_MINION_SALT_STATE);

        //response is empty in case the minion is down
        if (response.isPresent()) {
            return response.get().values().stream().filter(value -> !value.isResult())
                    .map(value -> value.getComment())
                    .collect(Collectors.collectingAndThen(Collectors.toList(),
                            (list) -> list.isEmpty() ? Optional.<List<String>>empty() :
                                    Optional.of(list)));
        }
        return Optional.of(Collections.singletonList(SaltService.MINION_UNREACHABLE_ERROR));
    }

    /**
     * @return saltSSHService to get
     */
    public SaltSSHService getSaltSSHService() {
        return saltSSHService;
    }
    /**
     * Upload built Kiwi image to SUSE Manager
     *
     * @param minion     the minion
     * @param filepath   the filepath of the image to upload, in the build host
     * @param imageStore the image store location
     * @return the execution result
     */
    public Optional<MgrUtilRunner.ExecResult> collectKiwiImage(MinionServer minion, String filepath,
            String imageStore) {
        RunnerCall<MgrUtilRunner.ExecResult> call =
                MgrKiwiImageRunner.collectImage(minion.getMinionId(), filepath, imageStore);
        return callSync(call);
    }
}
