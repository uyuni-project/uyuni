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

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.redhat.rhn.common.client.ClientCertificate;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.messaging.JavaMailException;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.manager.audit.scap.file.ScapFileManager;

import com.redhat.rhn.manager.system.SystemManager;
import com.suse.manager.reactor.PGEventStream;
import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.utils.MailHelper;
import com.suse.manager.utils.MinionServerUtils;
import com.suse.manager.webui.controllers.utils.ContactMethodUtil;
import com.suse.manager.webui.services.SaltActionChainGeneratorService;
import com.suse.manager.webui.services.iface.RedhatProductInfo;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.impl.runner.MgrK8sRunner;
import com.suse.manager.webui.services.impl.runner.MgrKiwiImageRunner;
import com.suse.manager.webui.services.impl.runner.MgrRunner;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;
import com.suse.manager.webui.utils.ElementCallJson;
import com.suse.manager.webui.utils.gson.BootstrapParameters;
import com.suse.manager.webui.utils.salt.MgrActionChains;
import com.suse.manager.webui.utils.salt.State;
import com.suse.manager.webui.utils.salt.custom.PkgProfileUpdateSlsResult;
import com.suse.manager.webui.utils.salt.custom.ScheduleMetadata;
import com.suse.salt.netapi.AuthModule;
import com.suse.salt.netapi.calls.LocalAsyncResult;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.RunnerCall;
import com.suse.salt.netapi.calls.WheelCall;
import com.suse.salt.netapi.calls.WheelResult;
import com.suse.salt.netapi.calls.modules.Cmd;
import com.suse.salt.netapi.calls.modules.Config;
import com.suse.salt.netapi.calls.modules.Event;
import com.suse.salt.netapi.calls.modules.Grains;
import com.suse.salt.netapi.calls.modules.Match;
import com.suse.salt.netapi.calls.modules.SaltUtil;
import com.suse.salt.netapi.calls.modules.State.ApplyResult;
import com.suse.salt.netapi.calls.modules.Status;
import com.suse.salt.netapi.calls.modules.Test;
import com.suse.salt.netapi.calls.modules.Zypper;
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
import com.suse.salt.netapi.event.EventListener;
import com.suse.salt.netapi.event.EventStream;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.results.CmdResult;
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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Singleton class acting as a service layer for accessing the salt API.
 */
public class SaltService implements SystemQuery, SaltApi {

    private final Batch defaultBatch;

    /**
     * Singleton instance of this class
     */
    private static final SaltService INSTANCE_SALT_SERVICE = new SaltService();
    public static final SystemQuery INSTANCE = INSTANCE_SALT_SERVICE;
    public static final SaltApi INSTANCE_SALT_API = INSTANCE_SALT_SERVICE;

    // Logger
    private static final Logger LOG = Logger.getLogger(SaltService.class);

    // Salt properties
    private final URI SALT_MASTER_URI = URI.create("http://" +
            com.redhat.rhn.common.conf.Config.get()
                    .getString(ConfigDefaults.SALT_API_HOST, "localhost") +
            ":" + com.redhat.rhn.common.conf.Config.get()
                    .getString(ConfigDefaults.SALT_API_PORT, "9080"));
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

    /**
     * Enum of all the available status for Salt keys.
     */
    public enum KeyStatus {
        ACCEPTED, DENIED, UNACCEPTED, REJECTED
    }

    /**
     * Default constructor
     */
    public SaltService() {
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
        defaultBatch = Batch.custom().withBatchAsAmount(ConfigDefaults.get().getSaltBatchSize())
                        .withDelay(ConfigDefaults.get().getSaltBatchDelay())
                        .withPresencePingTimeout(ConfigDefaults.get().getSaltPresencePingTimeout())
                        .withPresencePingGatherJobTimeout(ConfigDefaults.get().getSaltPresencePingGatherJobTimeout())
                        .build();
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
     * {@inheritDoc}
     */
    @Override
    public Optional<Boolean> ping(String minionId) {
        return callSync(Test.ping(), minionId);
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
            LOG.debug("Runner callSync: " + runnerCallToString(call));
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
     * {@inheritDoc}
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
            LOG.debug("Wheel callSync: " + wheelCallToString(call));
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
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    public Key.Fingerprints getFingerprints() {
        return callSync(Key.finger("*"))
                .orElseThrow(() -> new RuntimeException("no wheel results"));
    }

    /**
     * {@inheritDoc}
     */
    public Key.Pair generateKeysAndAccept(String id,
            boolean force) {
        return callSync(Key.genAccept(id, Optional.of(force)))
                .orElseThrow(() -> new RuntimeException("no wheel results"));
    }

    /**
     * {@inheritDoc}
     */
    public <T> Optional<T> getGrains(String minionId, TypeToken<T> type, String... grainNames) {
       return callSync(com.suse.manager.webui.utils.salt.Grains.item(false, type, grainNames), minionId);
    }

    /**
     * {@inheritDoc}
     */
    public Optional<Map<String, Object>> getGrains(String minionId) {
        return callSync(Grains.items(false), minionId);
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    public void acceptKey(String match) {
        callSync(Key.accept(match))
                .orElseThrow(() -> new RuntimeException("no wheel results"));
    }

    /**
     * {@inheritDoc}
     */
    public void deleteKey(String minionId) {
        callSync(Key.delete(minionId))
                .orElseThrow(() -> new RuntimeException("no wheel results"));
    }

    /**
     * {@inheritDoc}
     */
    public void rejectKey(String minionId) {
        callSync(Key.reject(minionId))
                .orElseThrow(() -> new RuntimeException("no wheel results"));
    }

    // Reconnecting time (in seconds) to Salt event bus
    private static final int DELAY_TIME_SECONDS = 5;

    private EventStream eventStream;

    private synchronized void eventStreamClosed() {
        eventStream = null;
    }

    private synchronized EventStream createOrGetEventStream() {

        int retries = 0;

        while (eventStream == null || eventStream.isEventStreamClosed()) {
            retries++;
            try {
                eventStream = createEventStream();
                eventStream.addEventListener(new EventListener() {
                    @Override
                    public void notify(com.suse.salt.netapi.datatypes.Event event) {
                    }

                    @Override
                    public void eventStreamClosed(int code, String phrase) {
                        SaltService.this.eventStreamClosed();
                    }
                });
                if (eventStream.isEventStreamClosed()) {
                    eventStream = null;
                }

                if (retries > 1) {
                    LOG.warn("Successfully connected to the Salt event bus after " +
                            (retries - 1) + " retries.");
                }
                else {
                    LOG.info("Successfully connected to the Salt event bus");
                }
            }
            catch (SaltException e) {
                try {
                    LOG.error("Unable to connect: " + e + ", retrying in " +
                            DELAY_TIME_SECONDS + " seconds.");
                    Thread.sleep(1000 * DELAY_TIME_SECONDS);
                    if (retries == 1) {
                        MailHelper.withSmtp().sendAdminEmail("Cannot connect to salt event bus",
                                "salt-api daemon is not responding. Check the status of " +
                                        "salt-api daemon and (re)-start it if needed\n\n" +
                                        "This is the only notification you will receive.");
                    }
                }
                catch (JavaMailException javaMailException) {
                    LOG.error("Error sending email: " + javaMailException.getMessage());
                }
                catch (InterruptedException e1) {
                    LOG.error("Interrupted during sleep: " + e1);
                }
            }
        }
        return eventStream;
    }

    private EventStream createEventStream() throws SaltException {
        if (ConfigDefaults.get().isPostgresql()) {
            return new PGEventStream();
        }
        Token token = adaptException(SALT_CLIENT.login(SALT_USER, SALT_PASSWORD, AUTH_MODULE));
        return SALT_CLIENT.events(token, 0, 0, 0);
    }

    /**
     * {@inheritDoc}
     */
    public EventStream getEventStream() {
        return createOrGetEventStream();
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
     * {@inheritDoc}
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
        LocalCall<R> call = callIn.withMetadata(ScheduleMetadata.getDefaultMetadata().withBatchMode());
        return adaptException(call.callAsync(SALT_CLIENT, target, PW_AUTH, events, cancel, defaultBatch));
    }

    /**
     * {@inheritDoc}
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
                        getEventStream(), cancel).orElseGet(Collections::emptyMap));
            }
            catch (SaltException e) {
                throw new RuntimeException(e);
            }
        }

        return results;
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @Override
    public Optional<JsonElement> rawJsonCall(LocalCall<?> call, String minionId) {
        return callSync(new ElementCallJson(call), minionId);
    }

    /**
     * {@inheritDoc}
     */
    public Optional<Map<String, Jobs.ListJobsEntry>> jobsByMetadata(Object metadata) {
        return callSync(Jobs.listJobs(metadata));
    }

    /**
     * {@inheritDoc}
     */
    public Optional<Map<String, Jobs.ListJobsEntry>> jobsByMetadata(Object metadata,
                                                                    LocalDateTime startTime, LocalDateTime endTime) {
        return callSync(Jobs.listJobs(metadata, startTime, endTime));
    }

    /**
     * {@inheritDoc}
     */
    public Optional<Jobs.Info> listJob(String jid) {
        return callSync(Jobs.listJob(jid));
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, CompletionStage<Result<Boolean>>> matchAsync(
            String target, CompletableFuture<GenericError> cancel) {
        try {
            return completableAsyncCall(Match.glob(target), new Glob(target),
                    getEventStream(), cancel).orElseGet(Collections::emptyMap);
        }
        catch (SaltException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @Override
    public Optional<List<Zypper.ProductInfo>> getProducts(String minionId) {
        return callSync(Zypper.listProducts(false), minionId);
    }

    /**
     * {@inheritDoc}
     */
    public void syncAll(MinionList minionList) {
        try {
             LocalCall<Map<String, Object>> call = SaltUtil.syncAll(Optional.empty(),
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
     * @param callIn the call to execute
     * @param target minions targeted by the call
     * @return the result of the call
     * @throws SaltException in case of an error executing the job with Salt
     */
    private <T> Map<String, Result<T>> callSync(LocalCall<T> callIn, MinionList target)
            throws SaltException {
        HashSet<String> uniqueMinionIds = new HashSet<>(target.getTarget());
        Map<Boolean, List<String>> minionPartitions =
                partitionMinionsByContactMethod(uniqueMinionIds);

        List<String> sshMinionIds = minionPartitions.get(true);
        List<String> regularMinionIds = minionPartitions.get(false);

        Map<String, Result<T>> results = new HashMap<>();

        if (!sshMinionIds.isEmpty()) {
            results.putAll(saltSSHService.callSyncSSH(
                    callIn,
                    new MinionList(sshMinionIds)));
        }

        if (!regularMinionIds.isEmpty()) {
            ScheduleMetadata metadata = ScheduleMetadata.getDefaultMetadata().withBatchMode();
            LOG.debug("Local callSync: " + SaltService.localCallToString(callIn));
            List<Map<String, Result<T>>> callResult =
                    adaptException(callIn.withMetadata(metadata).callSync(SALT_CLIENT,
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
     * {@inheritDoc}
     */
    @Override
    public Map<String, Result<Object>> showHighstate(String minionId) throws SaltException {
        return callSync(com.suse.salt.netapi.calls.modules.State.showHighstate(), new MinionList(minionId));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Result<Map<String, String>>> getPendingResume(List<String> minionIds) throws SaltException {
        return callSync(
                MgrActionChains.getPendingResume(),
                new MinionList(minionIds));
    }

    /**
     * Return local call options as a string (for debugging)
     *
     * @param call the local call
     * @return string representation
     */
    public static String localCallToString(LocalCall<?> call) {
        return "[" + call.getModuleName() + "." +
                call.getFunctionName() + "] with payload [" +
                call.getPayload() + "]";
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
    private <T> Optional<LocalAsyncResult<T>> callAsync(LocalCall<T> call, Target<?> target)
            throws SaltException {
        return callAsync(call, target, Optional.empty());
    }

    /**
     * {@inheritDoc}
     */
    public <T> Optional<LocalAsyncResult<T>> callAsync(LocalCall<T> callIn, Target<?> target,
            Optional<ScheduleMetadata> metadataIn) throws SaltException {
        ScheduleMetadata metadata =
                Opt.fold(metadataIn, () -> ScheduleMetadata.getDefaultMetadata(), Function.identity()).withBatchMode();
        LOG.debug("Local callAsync: " + SaltService.localCallToString(callIn));
        return adaptException(callIn.withMetadata(metadata).callAsync(SALT_CLIENT, target, PW_AUTH, defaultBatch));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deployChannels(List<String> minionIds) throws SaltException {
        callSync(
                com.suse.salt.netapi.calls.modules.State.apply(ApplyStatesEventMessage.CHANNELS),
                new MinionList(minionIds));
    }

    /**
     * {@inheritDoc}
     */
    public Optional<LocalAsyncResult<String>> checkIn(MinionList targetIn) throws SaltException {
        try {
            LocalCall<String> call = Test.echo("checkIn");
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
     * {@inheritDoc}
     */
    public void updateSystemInfo(MinionList minionTarget) {
        try {
            callAsync(State.apply(Arrays.asList(ApplyStatesEventMessage.SYSTEM_INFO), Optional.empty()), minionTarget,
                    Optional.of(ScheduleMetadata.getDefaultMetadata().withMinionStartup()));
        }
        catch (SaltException ex) {
            LOG.debug("Error while executing util.systeminfo state: " + ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    public Optional<String> getMasterHostname(String minionId) {
        return callSync(Config.get(Config.MASTER), minionId);
    }

    /**
     * {@inheritDoc}
     */
    private Optional<Map<String, ApplyResult>> applyState(
            String minionId, String state) {
        return callSync(State.apply(Arrays.asList(state), Optional.empty()), minionId);
    }

    /**
     * {@inheritDoc}
     */
    public Optional<RedhatProductInfo> redhatProductInfo(String minionId) {
        return callSync(State.apply(Arrays.asList("packages.redhatproductinfo"), Optional.empty()), minionId).map(result -> {
            Optional<String> centosReleaseContent = Optional.ofNullable(result.get(PkgProfileUpdateSlsResult.PKG_PROFILE_CENTOS_RELEASE)
                    .getChanges(CmdResult.class).getStdout());
            Optional<String> rhelReleaseContent = Optional.ofNullable(result.get(PkgProfileUpdateSlsResult.PKG_PROFILE_REDHAT_RELEASE)
                    .getChanges(CmdResult.class).getStdout());
            Optional<String> whatProvidesRes = Optional.ofNullable(result.get(PkgProfileUpdateSlsResult.PKG_PROFILE_WHATPROVIDES_SLES_RELEASE)
                    .getChanges(CmdResult.class).getStdout());

            return new RedhatProductInfo(centosReleaseContent, rhelReleaseContent, whatProvidesRes);
        });
    }

    /**
     * {@inheritDoc}
     */
    public Result<SSHResult<Map<String, ApplyResult>>> bootstrapMinion(
            BootstrapParameters parameters, List<String> bootstrapMods,
            Map<String, Object> pillarData) throws SaltException {
        return saltSSHService.bootstrapMinion(parameters, bootstrapMods, pillarData);
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    public Optional<MgrUtilRunner.ExecResult> deleteRejectedKey(String minionId) {
        RunnerCall<MgrUtilRunner.ExecResult> call = MgrUtilRunner.deleteRejectedKey(minionId);
        return callSync(call);
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    public Optional<List<MgrK8sRunner.Container>> getAllContainers(String kubeconfig,
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
        ).map(s -> s.getContainers());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifySystemIdGenerated(MinionServer minion) throws InstantiationException, SaltException {
        ClientCertificate cert = SystemManager.createClientCertificate(minion);
        Map<String, Object> data = new HashMap<>();
        data.put("data", cert.toString());
        callAsync(
                Event.fire(data, "suse/systemid/generated"),
                new MinionList(minion.getMinionId())
        );
    }

    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @Override
    public void storeSshKeyFile(Path path, String contents) {
        ensureAbsolutePath(path);

        String absolutePath = path.toAbsolutePath().toString();
        RunnerCall<String> createFile = MgrRunner.writeTextFile(absolutePath, contents);
        callSync(createFile).orElseThrow(() -> new IllegalStateException("Can't create SSH priv key file " + path));

        // this might not be needed, the file is created with sane perms already
        String desiredMode = "0600";
        RunnerCall<String> setMode = MgrRunner.setFileMode(absolutePath, desiredMode);
        String mode = callSync(setMode)
                .orElseThrow(() -> new IllegalStateException("Can't set mode for SSH priv key file " + path));

        if (!mode.equals(desiredMode)) {
            throw new IllegalStateException(
                    String.format("Invalid mode '%s' for SSH Key private file '%s'", path, mode));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Boolean> removeFile(Path path) {
        ensureAbsolutePath(path);
        String absolutePath = path.toAbsolutePath().toString();
        RunnerCall<Boolean> createFile = MgrRunner.removeFile(absolutePath);
        return callSync(createFile);
    }

    private void ensureAbsolutePath(Path path) {
        if (!path.isAbsolute()) {
            throw new IllegalStateException("Given path is not absolute: " + path);
        }
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
     * {@inheritDoc}
     */
    public SaltSSHService getSaltSSHService() {
        return saltSSHService;
    }

    /**
     * {@inheritDoc}
     */
    public Optional<MgrUtilRunner.ExecResult> collectKiwiImage(MinionServer minion, String filepath,
            String imageStore) {
        RunnerCall<MgrUtilRunner.ExecResult> call =
                MgrKiwiImageRunner.collectImage(minion.getMinionId(), filepath, imageStore);
        return callSync(call);
    }
}
