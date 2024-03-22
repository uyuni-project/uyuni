/*
 * Copyright (c) 2015--2021 SUSE LLC
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

import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.common.client.ClientCertificate;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.messaging.JavaMailException;
import com.redhat.rhn.common.util.http.HttpClientAdapter;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.manager.audit.scap.file.ScapFileManager;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.reactor.PGEventStream;
import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.ssl.SSLCertPair;
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
import com.suse.manager.webui.utils.salt.custom.MgrActionChains;
import com.suse.manager.webui.utils.salt.custom.PkgProfileUpdateSlsResult;
import com.suse.manager.webui.utils.salt.custom.ScheduleMetadata;
import com.suse.manager.webui.utils.salt.custom.SumaUtil.PublicCloudInstanceFlavor;
import com.suse.manager.webui.utils.salt.custom.SystemInfo;
import com.suse.salt.netapi.AuthModule;
import com.suse.salt.netapi.calls.AbstractCall;
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
import com.suse.salt.netapi.calls.modules.State;
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
import com.suse.salt.netapi.results.StateApplyResult;
import com.suse.utils.Json;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Singleton class acting as a service layer for accessing the salt API.
 */
public class SaltService implements SystemQuery, SaltApi {

    private final Batch defaultBatch;

    // Logger
    private static final Logger LOG = LogManager.getLogger(SaltService.class);

    // Salt properties
    private static final URI SALT_MASTER_URI = URI.create("https://" +
            com.redhat.rhn.common.conf.Config.get()
                    .getString(ConfigDefaults.SALT_API_HOST, "localhost") +
            ":" + com.redhat.rhn.common.conf.Config.get()
                    .getString(ConfigDefaults.SALT_API_PORT, "9080"));
    private static final String SALT_USER = "admin";
    private static final String SALT_PASSWORD = com.redhat.rhn.common.conf.Config.get().getString("server.secret_key");
    private static final AuthModule AUTH_MODULE = AuthModule.FILE;

    // Shared salt client instance
    private final SaltClient saltClient;
    private final CloseableHttpAsyncClient asyncHttpClient;

    // executing salt-ssh calls
    private final SaltSSHService saltSSHService;
    private static final AuthMethod PW_AUTH = new AuthMethod(new PasswordAuth(SALT_USER, SALT_PASSWORD, AUTH_MODULE));

    private static final Predicate<? super String> SALT_MINION_PREDICATE = mid ->
            MinionPendingRegistrationService.containsSSHMinion(mid) ||
                        MinionServerFactory
                                .findByMinionId(mid)
                                .filter(MinionServerUtils::isSshPushMinion)
                                .isPresent();

    private static final String CLEANUP_MINION_SALT_STATE = "cleanup_minion";
    protected static final String MINION_UNREACHABLE_ERROR = "minion_unreachable";

    private static final String GENERIC_RUNNER_ERROR = "Generic Salt error for runner call %s: %s";
    private static final String SSH_RUNNER_ERROR = "SaltSSH error for runner call %s: %s";
    private static final String PAYLOAD_CALL_TEMPLATE = "%s with payload [%s]";

    /**
     * Enum of all the available status for Salt keys.
     */
    public enum KeyStatus {
        ACCEPTED, DENIED, UNACCEPTED, REJECTED
    }

    private static final String CA_CERT_REGEX =
            "(?m)^-{3,}BEGIN CERTIFICATE-{3,}$(?s).*?^-{3,}END CERTIFICATE-{3,}$";
    private static final String RSA_KEY_REGEX =
            "(?m)^-{3,}BEGIN RSA PRIVATE KEY-{3,}$(?s).*?^-{3,}END RSA PRIVATE KEY-{3,}$";
    private static final Pattern SENSITIVE_DATA_PATTERN = Pattern.compile(CA_CERT_REGEX + "|" + RSA_KEY_REGEX);

    /**
     * Default constructor
     */
    public SaltService() {
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(HttpClientAdapter.getHTTPConnectionTimeout(5))
                .setSocketTimeout(HttpClientAdapter.getSaltApiHTTPSocketTimeout(12 * 60 * 60))
                .setConnectionRequestTimeout(5 * 60 * 1000)
                .setCookieSpec(CookieSpecs.STANDARD)
                .build();
        HttpAsyncClientBuilder httpClientBuilder = HttpAsyncClients.custom();
        httpClientBuilder.setDefaultRequestConfig(requestConfig);

        asyncHttpClient = httpClientBuilder
                .setMaxConnPerRoute(20)
                .setMaxConnTotal(20)
                .build();
        asyncHttpClient.start();

        saltClient = new SaltClient(SALT_MASTER_URI, new HttpAsyncClientImpl(asyncHttpClient));
        saltSSHService = new SaltSSHService(saltClient, SaltActionChainGeneratorService.INSTANCE);
        defaultBatch = Batch.custom().withBatchAsAmount(ConfigDefaults.get().getSaltBatchSize())
                        .withDelay(ConfigDefaults.get().getSaltBatchDelay())
                        .withPresencePingTimeout(ConfigDefaults.get().getSaltPresencePingTimeout())
                        .withPresencePingGatherJobTimeout(ConfigDefaults.get().getSaltPresencePingGatherJobTimeout())
                        .build();
    }

    /**
     * Constructor to use for unit testing
     *
     * @param client Salt client
     */
    public SaltService(SaltClient client) {
        asyncHttpClient = null;
        saltClient = client;
        saltSSHService = new SaltSSHService(saltClient, SaltActionChainGeneratorService.INSTANCE);
        defaultBatch = Batch.custom().withBatchAsAmount(ConfigDefaults.get().getSaltBatchSize())
                .withDelay(ConfigDefaults.get().getSaltBatchDelay())
                .withPresencePingTimeout(ConfigDefaults.get().getSaltPresencePingTimeout())
                .withPresencePingGatherJobTimeout(ConfigDefaults.get().getSaltPresencePingGatherJobTimeout())
                .build();
    }

    /**
     * Close the opened resources when the service is no longer needed
     */
    public void close() {
        if (asyncHttpClient == null) {
            return;
        }

        try {
            asyncHttpClient.close();
        }
        catch (IOException eIn) {
            LOG.warn("Failed to close HTTP client", eIn);
        }
    }

    /**
     * Synchronously executes a salt function on a single minion and returns the result.
     *
     * @param call the salt function
     * @param minionId the minion server id
     * @param <R> type of result
     * @return an optional containing the result or empty if no result was retrieved from the minion
     * @throws RuntimeException when a {@link SaltException} is thrown
     */
    public <R> Optional<Result<R>> callSyncResult(LocalCall<R> call, String minionId) {
        try {
            Map<String, Result<R>> stringRMap = callSync(call, new MinionList(minionId));

            return Opt.fold(Optional.ofNullable(stringRMap.get(minionId)), () -> {
                LOG.warn("Got no result for {} on minion {} (minion did not respond in time)",
                        call.getPayload().get("fun"), minionId);
                        return Optional.empty();
                    }, Optional::of);
        }
        catch (SaltException e) {
            throw new RhnRuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <R> Optional<R> callSync(LocalCall<R> call, String minionId) {
        return callSyncResult(call, minionId).flatMap(r ->
            r.fold(error -> {
                LOG.warn(error.toString());
                return Optional.empty();
            }, Optional::of)
        );
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
                            LOG.error(String.format("Function [%s] not available for runner call %s",
                                    e.getFunctionName(), runnerCallToString(call)));
                            return Optional.empty();
                        },
                        e -> {
                            LOG.error(String.format("Module [%s] not supported for runner call %s",
                                    e.getModuleName(), runnerCallToString(call)));
                            return Optional.empty();
                        },
                        e -> {
                            LOG.error("Error parsing json response from runner call {}: {}",
                                    runnerCallToString(call), e.getJson());
                            return Optional.empty();
                        },
                        e -> {
                            LOG.error(String.format(GENERIC_RUNNER_ERROR, runnerCallToString(call), e.getMessage()));
                            return Optional.empty();
                        },
                        e -> {
                            LOG.error(String.format(SSH_RUNNER_ERROR, runnerCallToString(call), e.getMessage()));
                            return Optional.empty();
                        }
                ));
    }

    private String callToString(AbstractCall<?> call) {
        return String.format("[%s.%s]", call.getModuleName(), call.getFunctionName());
    }

    private static Map<String, Object> filterPayload(Map<String, Object> payload) {
        var kwarg = payload.get("kwarg");
        if (kwarg == null) {
            return payload;
        }
        Function<Entry<?, ?>, Object> filterValue = e -> SENSITIVE_DATA_PATTERN
                .matcher(e.getValue().toString()).find() ? "HIDDEN" : e.getValue();
        var kwargFiltered = kwarg instanceof Map ?
                ((Map<?, ?>) kwarg).entrySet().stream()
                        .filter(e -> e.getValue() != null)
                        .collect(Collectors.toMap(Entry::getKey, filterValue)) :
                kwarg;
        payload.put("kwarg", kwargFiltered);
        return payload;
    }

    private String runnerCallToString(RunnerCall<?> call) {
        return String.format(PAYLOAD_CALL_TEMPLATE, call.getModuleName(), filterPayload(call.getPayload()));
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
            LOG.debug("Runner callSync: {}", runnerCallToString(call));
            Result<R> result = adaptException(call.callSync(saltClient, PW_AUTH));
            return result.fold(errorHandler, Optional::of);
        }
        catch (SaltException e) {
            throw new RhnRuntimeException(e);
        }
    }

    private RuntimeException noWheelResult() {
        return new RhnRuntimeException("no wheel results");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Key.Names getKeys() {
        return callSync(Key.listAll()).orElseThrow(this::noWheelResult);
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
                        LOG.error(String.format("Function [%s] not available for wheel call %s",
                                e.getFunctionName(), wheelCallToString(call)));
                        return Optional.empty();
                    },
                    e -> {
                        LOG.error(String.format("Module [%s] not supported for wheel call %s",
                                e.getModuleName(), wheelCallToString(call)));
                        return Optional.empty();
                    },
                    e -> {
                        LOG.error("Error parsing json response from wheel call {}: {}",
                                wheelCallToString(call), e.getJson());
                        return Optional.empty();
                    },
                    e -> {
                        LOG.error("Generic Salt error for wheel call {}: {}", wheelCallToString(call), e.getMessage());
                        return Optional.empty();
                    },
                    e -> {
                        LOG.error("SaltSSH error for wheel call {}: {}", wheelCallToString(call), e.getMessage());
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
            LOG.debug("Wheel callSync: {}", wheelCallToString(call));
            WheelResult<Result<R>> result = adaptException(call.callSync(saltClient, PW_AUTH));
            return result.getData().getResult().fold(errorHandler, Optional::of);
        }
        catch (SaltException e) {
            throw new RhnRuntimeException(e);
        }
    }

    private String wheelCallToString(WheelCall<?> call) {
        return String.format(PAYLOAD_CALL_TEMPLATE, call, call.getPayload());
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
    public Key.Fingerprints getFingerprints() {
        return callSync(Key.finger("*")).orElseThrow(this::noWheelResult);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Key.Pair generateKeysAndAccept(String id,
            boolean force) {
        return callSync(Key.genAccept(id, Optional.of(force))).orElseThrow(this::noWheelResult);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Optional<T> getGrains(String minionId, TypeToken<T> type, String... grainNames) {
       return callSync(Grains.item(false, type, grainNames), minionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Map<String, Object>> getGrains(String minionId) {
        return callSync(Grains.items(false), minionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> getMachineId(String minionId) {
        return getGrain(minionId, "machine_id").flatMap(grain -> {
          if (grain instanceof String) {
              return Optional.of((String) grain);
          }
          else {
              LOG.warn("Minion {} returned non string: {} as minion_id", minionId, grain);
              return Optional.empty();
          }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void acceptKey(String match) {
        if (callSync(Key.accept(match)).isEmpty()) {
            throw noWheelResult();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteKey(String minionId) {
        if (callSync(Key.delete(minionId)).isEmpty()) {
            throw noWheelResult();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void rejectKey(String minionId) {
        if (callSync(Key.reject(minionId)).isEmpty()) {
            throw noWheelResult();
        }
    }

    // Reconnecting time (in seconds) to Salt event bus
    private static final int DELAY_TIME_SECONDS = 5;

    private EventStream eventStream;

    private synchronized void eventStreamClosed() {
        eventStream = null;
    }

    @SuppressWarnings("java:S2276") // sleep fits in this solution as no locks are held
    private synchronized EventStream createOrGetEventStream() {

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
                        SaltService.this.eventStreamClosed();
                    }
                });
                if (eventStream.isEventStreamClosed()) {
                    eventStream = null;
                }

                if (retries > 1) {
                    LOG.warn("Successfully connected to the Salt event bus after {} retries.", retries - 1);
                }
                else {
                    LOG.info("Successfully connected to the Salt event bus");
                }
            }
            catch (SaltException e) {
                try {
                    LOG.error("Unable to connect: {}, retrying in " + DELAY_TIME_SECONDS + " seconds.", e);
                    Thread.sleep(1000 * DELAY_TIME_SECONDS);
                    if (retries == 1) {
                        MailHelper.withSmtp().sendAdminEmail("Cannot connect to salt event bus",
                                "salt-api daemon is not responding. Check the status of " +
                                        "salt-api daemon and (re)-start it if needed\n\n" +
                                        "This is the only notification you will receive.");
                    }
                }
                catch (JavaMailException javaMailException) {
                    LOG.error("Error sending email: {}", javaMailException.getMessage(), javaMailException);
                }
                catch (InterruptedException e1) {
                    LOG.error("Interrupted during sleep: {}", e1);
                }
            }
        }
        return eventStream;
    }

    private EventStream createEventStream() throws SaltException {
        return new PGEventStream();
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
    public Map<String, Result<String>> runRemoteCommand(MinionList target, String cmd) {
        try {
            return callSync(Cmd.run(cmd), target);
        }
        catch (SaltException e) {
            throw new RhnRuntimeException(e);
        }
    }

    private <R> Optional<Map<String, CompletionStage<Result<R>>>> completableAsyncCall(
            LocalCall<R> callIn, Target<?> target, EventStream events,
            CompletableFuture<GenericError> cancel) throws SaltException {
        LocalCall<R> call = callIn.withMetadata(ScheduleMetadata.getDefaultMetadata().withBatchMode());
        return adaptException(call.callAsync(saltClient, target, PW_AUTH, events, cancel, defaultBatch));
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
                throw new RhnRuntimeException(e);
            }
        }

        return results;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Result<List<SaltUtil.RunningInfo>>> running(MinionList target) {
        try {
            return callSync(SaltUtil.running(), target);
        }
        catch (SaltException e) {
            throw new RhnRuntimeException(e);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Result<JsonElement>> rawJsonCall(LocalCall<?> call, String minionId) {
        return callSyncResult(new ElementCallJson(call), minionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Map<String, Jobs.ListJobsEntry>> jobsByMetadata(Object metadata) {
        return callSync(Jobs.listJobs(metadata));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Map<String, Jobs.ListJobsEntry>> jobsByMetadata(Object metadata,
                                                                    LocalDateTime startTime, LocalDateTime endTime) {
        return callSync(Jobs.listJobs(metadata, startTime, endTime));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Jobs.Info> listJob(String jid) {
        return callSync(Jobs.listJob(jid));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, CompletionStage<Result<Boolean>>> matchAsync(
            String target, CompletableFuture<GenericError> cancel) {
        try {
            return completableAsyncCall(Match.glob(target), new Glob(target),
                    getEventStream(), cancel).orElseGet(Collections::emptyMap);
        }
        catch (SaltException e) {
            throw new RhnRuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> matchCompoundSync(String target) {
        try {
            Map<String, Result<Boolean>> result =
                    callSync(Match.compound(target, Optional.empty()), new Glob("*"));
            return result.entrySet().stream()
                    .filter(e -> e.getValue().result().isPresent() && Boolean.TRUE.equals(e.getValue().result().get()))
                    .map(Entry::getKey)
                    .collect(Collectors.toList());
        }
        catch (SaltException e) {
            throw new RhnRuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
            throw new RhnRuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void refreshPillar(MinionList minionList) {
        try {
            LocalCall<Boolean> call = SaltUtil.refreshPillar(Optional.empty(),
                    Optional.empty());
            callAsync(call, minionList);

            // Salt pillar refresh doesn't reload the modules with the new pillar
            LocalCall<Boolean> modulesRefreshCall = new LocalCall<>("saltutil.refresh_modules",
                    Optional.empty(), Optional.empty(), new TypeToken<>() {
            });
            callAsync(modulesRefreshCall, minionList);
        }
        catch (SaltException e) {
            throw new RhnRuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void syncGrains(MinionList minionList) {
        try {
            LocalCall<List<String>> call = SaltUtil.syncGrains(Optional.empty(),
                    Optional.empty());
            callSync(call, minionList);
        }
        catch (SaltException e) {
            throw new RhnRuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void syncModules(MinionList minionList) {
        try {
            LocalCall<List<String>> call = SaltUtil.syncModules(Optional.empty(),
                    Optional.empty());
            callSync(call, minionList);
        }
        catch (SaltException e) {
            throw new RhnRuntimeException(e);
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
    @Override
    public void syncAll(MinionList minionList) {
        try {
             LocalCall<Map<String, Object>> call = SaltUtil.syncAll(Optional.empty(),
                    Optional.empty());
            callSync(call, minionList);
        }
        catch (SaltException e) {
            throw new RhnRuntimeException(e);
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
            LOG.debug("Local callSync: {}", SaltService.localCallToString(callIn));
            List<Map<String, Result<T>>> callResult =
                    adaptException(callIn.withMetadata(metadata).callSync(saltClient,
                            new MinionList(regularMinionIds), PW_AUTH, defaultBatch));
            results.putAll(
                    callResult.stream().flatMap(map -> map.entrySet().stream())
                            .collect(Collectors.toMap(Entry<String, Result<T>>::getKey,
                                    Entry<String, Result<T>>::getValue))
            );
        }

        return results;
    }

    private <T> Map<String, Result<T>> callSync(LocalCall<T> callIn, Target<?> target)
            throws SaltException {

        ScheduleMetadata metadata = ScheduleMetadata.getDefaultMetadata().withBatchMode();
        LOG.debug("Local callSync: {}", SaltService.localCallToString(callIn));
        List<Map<String, Result<T>>> callResult =
                adaptException(callIn.withMetadata(metadata).callSync(saltClient,
                        target, PW_AUTH, defaultBatch));
        return callResult.stream().flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
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
        return String.format(PAYLOAD_CALL_TEMPLATE, call, call.getPayload());
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
    @Override
    public <T> Optional<LocalAsyncResult<T>> callAsync(LocalCall<T> callIn, Target<?> target,
            Optional<ScheduleMetadata> metadataIn) throws SaltException {
        ScheduleMetadata metadata =
                Opt.fold(metadataIn, ScheduleMetadata::getDefaultMetadata, Function.identity()).withBatchMode();
        LOG.debug("Local callAsync: {}", SaltService.localCallToString(callIn));
        return adaptException(callIn.withMetadata(metadata).callAsync(saltClient, target, PW_AUTH, defaultBatch));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deployChannels(List<String> minionIds) throws SaltException {
        callAsync(
                com.suse.salt.netapi.calls.modules.State.apply(ApplyStatesEventMessage.CHANNELS),
                new MinionList(minionIds));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<LocalAsyncResult<String>> checkIn(MinionList targetIn) throws SaltException {
        try {
            LocalCall<String> call = Test.echo("checkIn");
            return callAsync(call, targetIn);
        }
        catch (SaltException e) {
            throw new RhnRuntimeException(e);
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

            if (uptime.isEmpty()) {
                LOG.error("Can't get uptime for {}", minion.getMinionId());
            }
        }
        catch (RuntimeException e) {
            LOG.error(e.getMessage(), e);
        }
        return uptime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateSystemInfo(MinionList minionTarget) {
        try {
            callAsync(State.apply(Collections.singletonList(ApplyStatesEventMessage.SYSTEM_INFO),
                    Optional.empty()), minionTarget,
                    Optional.of(ScheduleMetadata.getDefaultMetadata().withMinionStartup()));
        }
        catch (SaltException ex) {
            LOG.debug("Error while executing util.systeminfo state: {}", ex.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<SystemInfo> getSystemInfoFull(String minionId) {
        return rawJsonCall(State.apply(Collections.singletonList(ApplyStatesEventMessage.SYSTEM_INFO_FULL),
               Optional.empty()), minionId)
               .flatMap(result -> result.result())
               .map(result -> Json.GSON.fromJson(result, SystemInfo.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PublicCloudInstanceFlavor getInstanceFlavor(String minionId) {
        LocalCall<String> call = new LocalCall<>("sumautil.instance_flavor",
                                                 Optional.empty(),
                                                 Optional.empty(),
                                                 new TypeToken<>() {  });
        return callSync(call, minionId)
            .map(res -> {
                    try {
                        return PublicCloudInstanceFlavor.valueOf(res.toUpperCase());
                    }
                    catch (IllegalArgumentException e) {
                        return PublicCloudInstanceFlavor.UNKNOWN;
                    }
                })
            .orElse(PublicCloudInstanceFlavor.UNKNOWN);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<String> getMasterHostname(String minionId) {
        return callSync(Config.get(Config.MASTER), minionId);
    }

    /**
     * {@inheritDoc}
     */
    private Optional<Map<String, ApplyResult>> applyState(String minionId, String state) {
        return callSync(State.apply(Collections.singletonList(state), Optional.empty()), minionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<RedhatProductInfo> redhatProductInfo(String minionId) {
        return callSync(State.apply(Collections.singletonList("packages.redhatproductinfo"),
                Optional.empty()), minionId)
                .map(result -> {
                    if (result.isEmpty()) {
                        return new RedhatProductInfo();
                    }
                    Optional<String> oracleReleaseContent = Optional
                            .ofNullable(result.get(PkgProfileUpdateSlsResult.PKG_PROFILE_ORACLE_RELEASE)
                            .getChanges(CmdResult.class).getStdout());
                    Optional<String> centosReleaseContent = Optional
                            .ofNullable(result.get(PkgProfileUpdateSlsResult.PKG_PROFILE_CENTOS_RELEASE)
                            .getChanges(CmdResult.class).getStdout());
                    Optional<String> rhelReleaseContent = Optional
                            .ofNullable(result.get(PkgProfileUpdateSlsResult.PKG_PROFILE_REDHAT_RELEASE)
                            .getChanges(CmdResult.class).getStdout());
                    Optional<String> alibabaReleaseContent = Optional
                            .ofNullable(result.get(PkgProfileUpdateSlsResult.PKG_PROFILE_ALIBABA_RELEASE)
                            .getChanges(CmdResult.class).getStdout());
                    Optional<String> almaReleaseContent = Optional
                            .ofNullable(result.get(PkgProfileUpdateSlsResult.PKG_PROFILE_ALMA_RELEASE)
                            .getChanges(CmdResult.class).getStdout());
                    Optional<String> amazonReleaseContent = Optional
                            .ofNullable(result.get(PkgProfileUpdateSlsResult.PKG_PROFILE_AMAZON_RELEASE)
                            .getChanges(CmdResult.class).getStdout());
                    Optional<String> rockyReleaseContent = Optional
                            .ofNullable(result.get(PkgProfileUpdateSlsResult.PKG_PROFILE_ROCKY_RELEASE)
                            .getChanges(CmdResult.class).getStdout());
                    Optional<String> whatProvidesRes = Optional
                            .ofNullable(result.get(PkgProfileUpdateSlsResult.PKG_PROFILE_WHATPROVIDES_SLES_RELEASE)
                            .getChanges(CmdResult.class).getStdout());
                    Optional<String> whatProvidesSLL = Optional
                            .ofNullable(result.get(PkgProfileUpdateSlsResult.PKG_PROFILE_WHATPROVIDES_SLL_RELEASE)
                            .getChanges(CmdResult.class).getStdout());

                    return new RedhatProductInfo(centosReleaseContent, rhelReleaseContent,
                            oracleReleaseContent, alibabaReleaseContent, almaReleaseContent,
                            amazonReleaseContent, rockyReleaseContent, whatProvidesRes,
                            whatProvidesSLL);
                });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Result<SSHResult<Map<String, ApplyResult>>> bootstrapMinion(
            BootstrapParameters parameters, List<String> bootstrapMods,
            Map<String, Object> pillarData) throws SaltException {
        return saltSSHService.bootstrapMinion(parameters, bootstrapMods, pillarData);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
            LOG.error("Error creating dir {}", mountPoint.resolve(actionPath), e);
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
                            LOG.error(String.format("Function [%s] not available for runner call %s.",
                                    e.getFunctionName(), callToString(call)));
                            return Optional.of(Collections.singletonMap(false,
                                    String.format("Function [%s] not available", e.getFunctionName())));
                        },
                        e -> {
                            LOG.error(String.format("Module [%s] not supported for runner call %s.",
                                    e.getModuleName(), callToString(call)));
                            return Optional.of(Collections.singletonMap(false,
                                    String.format("Module [%s] not supported", e.getModuleName())));
                        },
                        e -> {
                            LOG.error(String.format("Error parsing json response from runner call %s: %s",
                                    callToString(call), e.getJson()));
                            return Optional.of(Collections.singletonMap(false,
                                    "Error parsing json response: " + e.getJson()));
                        },
                        e -> {
                            LOG.error(String.format(GENERIC_RUNNER_ERROR, callToString(call), e.getMessage()));
                            return Optional.of(Collections.singletonMap(false,
                                    "Generic Salt error: " + e.getMessage()));
                        },
                        e -> {
                            LOG.error(String.format(SSH_RUNNER_ERROR, callToString(call), e.getMessage()));
                            return Optional.of(Collections.singletonMap(false,
                                    "SaltSSH error: " + e.getMessage()));
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
            Set<PosixFilePermission> wantedPers = PosixFilePermissions.fromString("rwxrwxr-x");
            if (!posixAttrs.readAttributes().permissions().equals(wantedPers)) {
                posixAttrs.setPermissions(wantedPers);
            }
        }
        catch (IOException e) {
            LOG.warn(String.format("Could not set 'rwxrwxr-x' permissions on %s: %s",
                    dir, e.getMessage()));
        }
        try {
            if (!posixAttrs.readAttributes().group().equals(group)) {
                posixAttrs.setGroup(group);
            }
        }
        catch (IOException e) {
            LOG.warn(String.format("Could not set group on %s to %s: %s",
                    dir, group, e.getMessage()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<MgrUtilRunner.SshKeygenResult> generateSSHKey(String path, String pubkeyCopy) {
        RunnerCall<MgrUtilRunner.SshKeygenResult> call = MgrUtilRunner.generateSSHKey(path, pubkeyCopy);
        return callSync(call);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<MgrUtilRunner.RemoveKnowHostResult> removeSaltSSHKnownHost(String hostname) {
        return removeSaltSSHKnownHost(hostname, SaltSSHService.SSH_DEFAULT_PORT);
    }

    @Override
    public Optional<MgrUtilRunner.RemoveKnowHostResult> removeSaltSSHKnownHost(String hostname, int port) {
        RunnerCall<MgrUtilRunner.RemoveKnowHostResult> call = MgrUtilRunner.removeSSHKnowHost("salt", hostname, port);
        return callSync(call);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<MgrUtilRunner.ExecResult> deleteRejectedKey(String minionId) {
        RunnerCall<MgrUtilRunner.ExecResult> call = MgrUtilRunner.deleteRejectedKey(minionId);
        return callSync(call);
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
    public Optional<List<MgrK8sRunner.Container>> getAllContainers(String kubeconfig,
                                                        String context) {
        RunnerCall<MgrK8sRunner.ContainersList> call =
                MgrK8sRunner.getAllContainers(kubeconfig, context);
        return callSync(call,
                err -> err.fold(
                    e -> {
                        LOG.error(String.format("Function [%s] not available for runner call %s.",
                                e.getFunctionName(), callToString(call)));
                        throw new NoSuchElementException();
                    },
                    e -> {
                        LOG.error(String.format("Module [%s] not supported for runner call %s",
                                e.getModuleName(), callToString(call)));
                        throw new NoSuchElementException();
                    },
                    e -> {
                        LOG.error(String.format("Error parsing json response from runner call %s: %s",
                                callToString(call), e.getJson()));
                        throw new NoSuchElementException();
                    },
                    e -> {
                        LOG.error(String.format(GENERIC_RUNNER_ERROR, callToString(call), e.getMessage()));
                        throw new NoSuchElementException();
                    },
                    e -> {
                        LOG.error(String.format(SSH_RUNNER_ERROR, callToString(call), e.getMessage()));
                        throw new NoSuchElementException();
                    }
                )
        ).map(MgrK8sRunner.ContainersList::getContainers);
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
    @Override
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
     * {@inheritDoc}
     */
    @Override
    public Optional<Boolean> removeFile(Path path) {
        ensureAbsolutePath(path);
        String absolutePath = path.toAbsolutePath().toString();
        RunnerCall<Boolean> createFile = MgrRunner.removeFile(absolutePath);
        return callSync(createFile);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<Boolean> mkDir(Path path, String modeString) {
        ensureAbsolutePath(path);
        String absolutePath = path.toAbsolutePath().toString();
        RunnerCall<Boolean> mkdir = MgrRunner.mkDir(absolutePath, modeString);
        return callSync(mkdir);
    }
    /**
     * {@inheritDoc}
     */
    @Override
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
                    .map(StateApplyResult::getComment)
                    .collect(Collectors.collectingAndThen(Collectors.toList(),
                            list -> list.isEmpty() ? Optional.empty() : Optional.of(list)));
        }
        return Optional.of(Collections.singletonList(SaltService.MINION_UNREACHABLE_ERROR));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SaltSSHService getSaltSSHService() {
        return saltSSHService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<MgrUtilRunner.ExecResult> collectKiwiImage(MinionServer minion, String filepath,
            String imageStore) {
        RunnerCall<MgrUtilRunner.ExecResult> call =
                MgrKiwiImageRunner.collectImage(minion.getMinionId(), minion.getIpAddress(), filepath, imageStore);
        return callSync(call);
    }

    @Override
    public String checkSSLCert(String rootCA, SSLCertPair serverCertKey, List<String> intermediateCAs)
            throws IllegalArgumentException {
        RunnerCall<Map<String, String>> call = MgrUtilRunner.checkSSLCert(rootCA, serverCertKey, intermediateCAs);
        Map<String, String> result = callSync(call)
                .orElseThrow(() -> new IllegalArgumentException("Unknown error while checking certificates"));
        String error = result.getOrDefault("error", null);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
        return result.get("cert");
    }
}
