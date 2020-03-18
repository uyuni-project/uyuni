package com.suse.manager.webui.services.impl;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.google.gson.JsonElement;
import com.redhat.rhn.domain.server.MinionServer;
import com.suse.manager.reactor.SaltReactor;
import com.suse.manager.virtualization.GuestDefinition;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public interface SystemQuery {

    Optional<Map<String, JsonElement>> getCapabilities(String minionId);

    Map<String, JsonObject> getNetworks(String minionId);

    Optional<GuestDefinition> getGuestDefinition(String minionId, String domainName);

    Optional<Boolean> ping(String minionId);

    Key.Names getKeys();
    boolean keyExists(String id, SaltService.KeyStatus... statusIn);
    Key.Fingerprints getFingerprints();
    Key.Pair generateKeysAndAccept(String id, boolean force);
    Optional<String> getMachineId(String minionId);
    void deleteKey(String minionId);
    void acceptKey(String match);
    void rejectKey(String minionId);
    Optional<MgrUtilRunner.ExecResult> deleteRejectedKey(String minionId);
    EventStream getEventStream() throws SaltException;
    Map<String, Result<String>> runRemoteCommand(MinionList target, String cmd);
    Map<String, CompletionStage<Result<String>>> runRemoteCommandAsync(
            MinionList target, String cmd, CompletableFuture<GenericError> cancel);

    Map<String, JsonObject> getPools(String minionId);

    Map<String, Map<String, JsonObject>> getVolumes(String minionId);

    Map<String, Result<List<SaltUtil.RunningInfo>>> running(MinionList target);
    Optional<Jobs.Info> listJob(String jid);

    Map<String, CompletionStage<Result<Boolean>>> matchAsync(
            String target, CompletableFuture<GenericError> cancel);

    Optional<CompletionStage<Map<String, Result<Boolean>>>> matchAsyncSSH(
            String target, CompletableFuture<GenericError> cancel);

    void refreshPillar(MinionList minionList);
    void syncGrains(MinionList minionList);
    void syncModules(MinionList minionList);
    void syncAll(MinionList minionList);

    Optional<LocalAsyncResult<String>> checkIn(MinionList targetIn) throws SaltException;
    void updateSystemInfo(MinionList minionTarget);
    Optional<String> getMasterHostname(String minionId);

    Result<SSHResult<Map<String, State.ApplyResult>>> bootstrapMinion(
            BootstrapParameters parameters, List<String> bootstrapMods,
            Map<String, Object> pillarData) throws SaltException;

        Map<Boolean, String> storeMinionScapFiles(
            MinionServer minion, String uploadDir, Long actionId);
    Optional<MgrUtilRunner.ExecResult> generateSSHKey(String path);
    Optional<MgrUtilRunner.ExecResult> chainSSHCommand(List<String> hosts,
        String clientKey,
        String proxyKey,
        String user,
        Map<String, String> options,
        String command,
        String outputfile);

    Optional<List<MgrK8sRunner.Container>> getAllContainers(String kubeconfig, String context);

    Optional<List<String>> cleanupMinion(MinionServer minion, int timeout);

    void notifySystemIdGenerated(MinionServer minion) throws InstantiationException, SaltException;

    Map<String, Result<Map<String, String>>> getPendingResume(List<String> minionIds) throws SaltException;
    Map<String, Result<Object>> showHighstate(String minionId) throws SaltException;

    Optional<List<Zypper.ProductInfo>> getProducts(String minionId);

    @Deprecated
    <T> Optional<LocalAsyncResult<T>> callAsync(LocalCall<T> callIn, Target<?> target,
                                                       Optional<ScheduleMetadata> metadataIn) throws SaltException;
    void deployChannels(List<String> minionIds) throws SaltException;

    Optional<MgrUtilRunner.ExecResult> collectKiwiImage(MinionServer minion, String filepath,
                                                               String imageStore);

    void updateLibvirtEngine(MinionServer minion);

    Optional<JsonElement> rawJsonCall(LocalCall<?> call, String minionId);

    Optional<Map<String, Jobs.ListJobsEntry>> jobsByMetadata(Object metadata);
    Optional<Map<String, Jobs.ListJobsEntry>> jobsByMetadata(
            Object metadata,
            LocalDateTime startTime,
            LocalDateTime endTim);
    @Deprecated
    <T> Optional<T> getGrains(String minionId, TypeToken<T> type, String... grainNames);
    @Deprecated
    Optional<Map<String, Object>> getGrains(String minionId);
    @Deprecated
    void setReactor(SaltReactor reactorIn);
    @Deprecated
    SaltSSHService getSaltSSHService();

    @Deprecated
    Optional<Map<String, State.ApplyResult>> applyState(String minionId, String state);

}
