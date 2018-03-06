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

import com.google.gson.reflect.TypeToken;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.state.StateFactory;

import com.redhat.rhn.manager.audit.scap.file.ScapFileManager;
import com.suse.manager.reactor.SaltReactor;
import com.suse.manager.webui.services.SaltCustomStateStorageManager;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;
import com.suse.manager.webui.utils.MinionServerUtils;
import com.suse.manager.webui.utils.gson.BootstrapParameters;
import com.suse.salt.netapi.calls.modules.Config;
import com.suse.salt.netapi.AuthModule;
import com.suse.salt.netapi.calls.LocalAsyncResult;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.RunnerCall;
import com.suse.salt.netapi.calls.WheelCall;
import com.suse.salt.netapi.calls.WheelResult;
import com.suse.salt.netapi.calls.modules.Cmd;
import com.suse.salt.netapi.calls.modules.Grains;
import com.suse.salt.netapi.calls.modules.Match;
import com.suse.salt.netapi.calls.modules.SaltUtil;
import com.suse.salt.netapi.calls.modules.Schedule;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.calls.modules.Test;
import com.suse.salt.netapi.calls.modules.Timezone;
import com.suse.salt.netapi.calls.runner.Jobs;
import com.suse.salt.netapi.calls.wheel.Key;
import com.suse.salt.netapi.client.SaltClient;
import com.suse.salt.netapi.config.ClientConfig;
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

import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
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

    // Salt presence properties
    private final Integer SALT_PRESENCE_TIMEOUT =
            ConfigDefaults.get().getSaltPresencePingTimeout();
    private final Integer SALT_PRESENCE_GATHER_JOB_TIMEOUT =
            ConfigDefaults.get().getSaltPresencePingGatherJobTimeout();

    // Shared salt client instance
    private final SaltClient SALT_CLIENT = new SaltClient(SALT_MASTER_URI);

    // executing salt-ssh calls
    private final SaltSSHService saltSSHService;

    private SaltCustomStateStorageManager customSaltStorageManager =
            SaltCustomStateStorageManager.INSTANCE;

    private static final Predicate<? super String> SALT_MINION_PREDICATE = (mid) ->
            SSHMinionsPendingRegistrationService.containsMinion(mid) ||
                        MinionServerFactory
                                .findByMinionId(mid)
                                .filter(m -> MinionServerUtils.isSshPushMinion(m))
                                .isPresent();

    private SaltReactor reactor = null;

    private final ScheduledExecutorService scheduledExecutorService =
            Executors.newScheduledThreadPool(5);

    // Prevent instantiation
    SaltService() {
        // Set unlimited timeout
        SALT_CLIENT.getConfig().put(ClientConfig.SOCKET_TIMEOUT, 0);
        saltSSHService = new SaltSSHService(SALT_CLIENT);
    }

    /**
     * @return the Salt reactor
     */
    public SaltReactor getReactor() {
        return reactor;
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
                return Optional.empty();
            }, r ->
                r.fold(error -> {
                    LOG.warn(error.toString());
                    return Optional.empty();
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
            Result<R> result = call.callSync(SALT_CLIENT,
                    SALT_USER, SALT_PASSWORD, AUTH_MODULE);
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
            WheelResult<Result<R>> result = call
                    .callSync(SALT_CLIENT, SALT_USER, SALT_PASSWORD, AUTH_MODULE);
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
     * For a given id check if there is a minion key in any status.
     *
     * @param id the id to check for
     * @return true if there is a key with the given id, false otherwise
     */
    public boolean keyExists(String id) {
        Key.Names keys = getKeys();
        return keys.getMinions().contains(id) ||
                keys.getUnacceptedMinions().contains(id) ||
                keys.getRejectedMinions().contains(id) ||
                keys.getDeniedMinions().contains(id);
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
     * Get the timezone offsets for a target, e.g. a list of minions.
     *
     * @param target the targeted minions
     * @return the timezone offsets of the targeted minions
     */
    public Map<String, Result<String>> getTimezoneOffsets(MinionList target) {
        try {
            return callSync(Timezone.getOffset(), target);
        }
        catch (SaltException e) {
            throw new RuntimeException(e);
        }
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
    // Do not use the shared client object here, so we can disable the timeout (set to 0).
    public EventStream getEventStream() throws SaltException {
        SaltClient client = new SaltClient(SALT_MASTER_URI);
        client.login(SALT_USER, SALT_PASSWORD, AUTH_MODULE);
        client.getConfig().put(ClientConfig.SOCKET_TIMEOUT, 0);
        return new EventStream(client.getConfig());
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

    private <R> Map<String, CompletionStage<Result<R>>> completableAsyncCall(
            LocalCall<R> call, Target<?> target, EventStream events,
            CompletableFuture<GenericError> cancel) throws SaltException {
        return call.callAsync(SALT_CLIENT, target, SALT_USER, SALT_PASSWORD,
                AuthModule.AUTO, events, cancel);
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
                        reactor.getEventStream(), cancel));
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
                    reactor.getEventStream(), cancel);
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
     * Schedule a function call for a given target.
     *
     * @param name the name to use for the scheduled job
     * @param call the module call to schedule
     * @param target the target
     * @param scheduleDate schedule date
     * @param metadata metadata to pass to the salt job
     * @return the result of the schedule call
     * @throws SaltException in case there is an error scheduling the job
     */
    public Map<String, Result<Schedule.Result>> schedule(String name,
            LocalCall<?> call, MinionList target, ZonedDateTime scheduleDate,
            Map<String, ?> metadata) throws SaltException {
        // We do one Salt call per timezone: group minions by their timezone offsets
        Map<String, Result<String>> minionOffsets = getTimezoneOffsets(target);
        Map<String, List<String>> offsetMap = minionOffsets.keySet().stream()
                .collect(Collectors.groupingBy(k -> minionOffsets.get(k).result().get()));
        if (LOG.isDebugEnabled()) {
            LOG.debug("Minions grouped by timezone offsets: " + offsetMap);
        }

        // The return type is a map of minion ids to their schedule results
        return offsetMap.entrySet().stream().flatMap(entry -> {
            LocalDateTime targetScheduleDate = scheduleDate.toOffsetDateTime()
                    .withOffsetSameInstant(ZoneOffset.of(entry.getKey())).toLocalDateTime();
            try {
                MinionList timezoneTarget = new MinionList(entry.getValue());
                Map<String, Result<Schedule.Result>> result = callSync(
                        Schedule.add(name, call, targetScheduleDate, metadata),
                        timezoneTarget);
                return result.entrySet().stream();
            }
            catch (SaltException e) {
                LOG.error(String.format("Error scheduling actions: %s", e.getMessage()));
                return Stream.empty();
            }
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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

        // Filter out minion ids of minions that do not appear active.
        // Only checking minion presence when LocalCall has no timeouts attribute
        if (!call.getPayload().keySet().containsAll(
                Arrays.asList("timeout", "gather_job_timeout"))) {
            // To avoid blocking if any targeted minion is down, we first check which
            // minions are actually up and running, and then exclude unreachable minions
            // from the current synchronous call.
            Set<String> regularActiveMinions = regularMinionIds.isEmpty() ?
                    Collections.emptySet() :
                    presencePing(new MinionList(regularMinionIds)).keySet();

            Set<String> sshActiveMinions = sshMinionIds.isEmpty() ?
                    Collections.emptySet() :
                    presencePingSSH(new MinionList(sshMinionIds)).entrySet()
                        .stream()
                        .filter(
                            s -> s.getValue().toXor().fold(error -> false, result -> true))
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toSet());

            Set<String> unreachableMinions = uniqueMinionIds.stream()
                .filter(id -> !regularActiveMinions.contains(id))
                .filter(id -> !sshActiveMinions.contains(id))
                .sorted()
                .collect(Collectors.toSet());

            if (!unreachableMinions.isEmpty()) {
                LOG.warn("Some of the targeted minions cannot be reached: " +
                        unreachableMinions.toString() +
                        ". Excluding them from the synchronous call.");
                sshMinionIds.retainAll(sshActiveMinions);
                regularMinionIds.retainAll(regularActiveMinions);
            }
        }

        Map<String, Result<T>> results = new HashMap<>();

        if (!sshMinionIds.isEmpty()) {
            results.putAll(saltSSHService.callSyncSSH(
                    call,
                    new MinionList(sshMinionIds)));
        }

        if (!regularMinionIds.isEmpty()) {
            results.putAll(call.callSync(SALT_CLIENT,
                    new MinionList(regularMinionIds),
                    SALT_USER,
                    SALT_PASSWORD,
                    AuthModule.AUTO));
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
     * Execute a LocalCall asynchronously on the default Salt client.
     *
     * @param <T> the return type of the call
     * @param call the call to execute
     * @param target minions targeted by the call
     * @return the LocalAsyncResult of the call
     * @throws SaltException in case of an error executing the job with Salt
     */
    public <T> LocalAsyncResult<T> callAsync(LocalCall<T> call, Target<?> target)
            throws SaltException {
        return call.callAsync(
                SALT_CLIENT, target, SALT_USER, SALT_PASSWORD, AuthModule.AUTO);
    }

    /**
     * Remove a scheduled job from the minion
     *
     * @param name the name of the job to delete from the schedule
     * @param target the target
     * @return the result
     */
    public Map<String, Result<Schedule.Result>> deleteSchedule(
            String name, MinionList target) {
        try {
            return callSync(Schedule.delete(name), target);
        }
        catch (SaltException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Save a Salt .sls file.
     * @param orgId the organization id
     * @param name the name of the file
     * @param content the content of the file
     * @param oldName the previous name of the file,
     *                when the file already exists
     * @param oldChecksum the checksum of the file at
     *                    the time of showing it to the user
     */
    public void saveCustomState(long orgId, String name, String content,
                                String oldName, String oldChecksum) {
        try {
            customSaltStorageManager.storeState(orgId, name, content, oldName, oldChecksum);
            if (customSaltStorageManager.isRename(oldName, name)) {
                // for some reason the following native query does not trigger a flush
                // and the new name is not yet in the db
                StateFactory.getSession().flush();

                SaltStateGeneratorService.INSTANCE.regenerateCustomStates(orgId, name);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Delete a Salt .sls file.
     * @param orgId the organization id
     * @param name the name of the file
     */
    public void deleteCustomState(long orgId, String name) {
        try {
            StateFactory.CustomStateRevisionsUsage usage = StateFactory
                    .latestStateRevisionsByCustomState(orgId, name);
            customSaltStorageManager.deleteState(orgId, name);
            SaltStateGeneratorService.INSTANCE.regenerateCustomStates(usage);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a list of all Salt .sls files
     * for a given organization.
     *
     * @param orgId the organization id
     * @return a list of names without the .sls extension
     */
    public List<String> getCatalogStates(long orgId) {
        return customSaltStorageManager.listByOrg(orgId);
    }

    /**
     * Get the content of the give Salt .sls file.
     * @param orgId the organization id
     * @param name the name of the file
     * @return the content of the file if the file exists
     */
    public Optional<String> getOrgStateContent(long orgId, String name) {
        try {
            return customSaltStorageManager.getContent(orgId, name);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns true if an org state exists.
     * @param orgId the organization id
     * @param name the name of the file
     * @return true if the file exists
     */
    public boolean orgStateExists(long orgId, String name) {
        return customSaltStorageManager.exists(orgId, name);
    }

    /**
     * Add the organization namespace to the given states.
     * @param orgId the organization id
     * @param states the states names
     * @return a set of names that included the organization namespace
     */
    public Set<String> resolveOrgStates(long orgId, Set<String> states) {
        return states.stream().map(state -> customSaltStorageManager
                .getOrgNamespace(orgId) + "." + state)
                .collect(Collectors.toSet());
    }

    /**
     * Pings a target set of minions.
     * @param targetIn the target
     * @return a Map from minion ids which responded to the ping to Boolean.TRUE
     * @throws SaltException if we get a failure from Salt
     */
    public Map<String, Result<Boolean>> ping(MinionList targetIn) throws SaltException {
        return callSync(
            Test.ping(),
            targetIn
        );
    }

    /**
     * Pings a target set of minions using a short timeout to check presence
     * @param targetIn the target
     * @return a Map from minion ids which responded to the ping to Boolean.TRUE
     * @throws SaltException if we get a failure from Salt
     */
    public Map<String, Result<Boolean>> presencePing(MinionList targetIn)
            throws SaltException {
        return new LocalCall<>("test.ping",
                Optional.empty(), Optional.empty(), new TypeToken<Boolean>() { },
                Optional.of(SALT_PRESENCE_TIMEOUT),
                Optional.of(SALT_PRESENCE_GATHER_JOB_TIMEOUT))
            .callSync(SALT_CLIENT, targetIn, SALT_USER, SALT_PASSWORD, AuthModule.AUTO)
            .entrySet().stream().filter(kv -> {
                return kv.getValue().result().orElse(true);
            })
            .collect(Collectors.toMap(k -> k.getKey(), v -> v.getValue()));
    }

    /**
     * Pings a target set of SSH minions using a short timeout to check presence
     * @param targetInSSH the SSH target
     * @return a Map from minion ids which responded to the ping to Boolean.TRUE
     * @throws SaltException if we get a failure from Salt
     */
    public Map<String, Result<Boolean>> presencePingSSH(MinionList targetInSSH)
            throws SaltException {
        return saltSSHService.callSyncSSH(
            new LocalCall<>("test.ping",
                Optional.empty(), Optional.empty(), new TypeToken<Boolean>() { },
                Optional.of(SALT_PRESENCE_TIMEOUT),
                Optional.of(SALT_PRESENCE_GATHER_JOB_TIMEOUT)), targetInSSH)
                .entrySet().stream().filter(kv -> {
                    return kv.getValue().result().orElse(true);
                })
                .collect(Collectors.toMap(k -> k.getKey(), v -> v.getValue()));
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
                    new LocalCall<>("status.uptime",
                            Optional.empty(),
                            Optional.empty(),
                            new TypeToken<Object>() { }),
                    minion.getMinionId())
                    .flatMap(SaltService::convertUptimeCompat);

            if (!uptime.isPresent()) {
                uptime = callSync(
                    new LocalCall<>("status.uptime",
                            Optional.empty(),
                            Optional.of(Collections.singletonMap("human_readable", false)),
                            new TypeToken<Float>() { }),
                    minion.getMinionId())
                    .flatMap(SaltService::convertUptimeCompat);
            }

            if (!uptime.isPresent()) {
                LOG.error("Can't get uptime for " + minion.getMinionId());
            }
        }
        catch (RuntimeException e) {
            LOG.error(e);
        }
        return uptime;
    }

    // compat method for old salt
    private static Optional<Long> convertUptimeCompat(Object o) {
        if (o instanceof Float) {
            LOG.info("Extracting uptime from deprecated salt result.");
            return Optional.of(((Float) o).longValue());
        }
        else if (o instanceof Map) {
            Object seconds = ((Map) o).get("seconds");
            if (seconds instanceof Number) {
                return Optional.of(((Number) seconds).longValue());
            }
        }
        LOG.error("Cannot extract uptime from  '" + o + "'.");
        return Optional.empty();
    }

    /**
     * Get the directory where custom state files are stored on disk.
     * @param orgId the organization id
     * @return the path where .sls files are stored
     */
    public String getCustomStateBaseDir(long orgId) {
        return customSaltStorageManager.getBaseDirPath();
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
    public Optional<Map<String, State.ApplyResult>> applyState(
            String minionId, String state) {
        return callSync(State.apply(Arrays.asList(state), Optional.empty(),
                Optional.of(true)), minionId);
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
    public Result<SSHResult<Map<String, State.ApplyResult>>> bootstrapMinion(
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
     * Call the custom mgrutil.ssh_keygen runner.
     *
     * @param path of the key files
     * @return the result of the runner call as a map
     */
    public Optional<MgrUtilRunner.ExecResult> generateSSHKey(String path) {
        RunnerCall<MgrUtilRunner.ExecResult> call =
                MgrUtilRunner.generateSSHKey(path);

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

}
