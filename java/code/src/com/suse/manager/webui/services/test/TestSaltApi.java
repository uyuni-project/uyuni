/*
 * Copyright (c) 2020--2021 SUSE LLC
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
package com.suse.manager.webui.services.test;

import com.redhat.rhn.domain.server.MinionServer;

import com.suse.manager.ssl.SSLCertPair;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.services.impl.runner.MgrK8sRunner;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;
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

public class TestSaltApi implements SaltApi {

    @Override
    public void deployChannels(List<String> minionIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<List<MgrK8sRunner.Container>> getAllContainers(String kubeconfig, String context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void storeSshKeyFile(Path path, String contents) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> matchCompoundSync(String target) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Boolean> removeFile(Path path) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Boolean> mkDir(Path path, String modeString) {
        return Optional.empty();
    }

    @Override
    public Optional<Boolean> copyFile(Path src, Path dst) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<LocalAsyncResult<String>> checkIn(MinionList targetIn) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateSystemInfo(MinionList minionTarget) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<SystemInfo> getSystemInfoFull(String minionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SumaUtil.PublicCloudInstanceFlavor getInstanceFlavor(String minionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<Boolean, String> storeMinionScapFiles(MinionServer minion, String uploadDir, Long actionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Result<Object>> showHighstate(String minionId) {
        throw new UnsupportedOperationException();
    }

    @Override
        public Optional<MgrUtilRunner.SshKeygenResult> generateSSHKey(String path, String pubkeyCopy) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<MgrUtilRunner.ExecResult> chainSSHCommand(List<String> hosts, String clientKey, String proxyKey,
                                                              String user, Map<String, String> options, String command,
                                                              String outputfile) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<MgrUtilRunner.RemoveKnowHostResult> removeSaltSSHKnownHost(String hostname) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<MgrUtilRunner.RemoveKnowHostResult> removeSaltSSHKnownHost(String hostname, int port) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void syncGrains(MinionList minionList) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void syncModules(MinionList minionList) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void syncAll(MinionList minionList) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Boolean> ping(String minionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EventStream getEventStream() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteKey(String minionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void acceptKey(String match) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void rejectKey(String minionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Optional<T> getGrains(String minionId, TypeToken<T> type, String... grainNames) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Map<String, Object>> getGrains(String minionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<String> getMasterHostname(String minionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Result<String>> runRemoteCommand(MinionList target, String cmd) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<MgrUtilRunner.ExecResult> deleteRejectedKey(String minionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Key.Names getKeys() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Key.Pair generateKeysAndAccept(String id, boolean force) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean keyExists(String id, SaltService.KeyStatus... statusIn) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Key.Fingerprints getFingerprints() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<List<String>> cleanupMinion(MinionServer minion, int timeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Result<SSHResult<Map<String, State.ApplyResult>>> bootstrapMinion(BootstrapParameters parameters,
                                                                             List<String> bootstrapMods,
                                                                             Map<String, Object> pillarData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <R> Optional<R> callSync(LocalCall<R> call, String minionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, CompletionStage<Result<String>>> runRemoteCommandAsync(MinionList target, String cmd,
                                                                              CompletableFuture<GenericError> cancel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Result<List<SaltUtil.RunningInfo>>> running(MinionList target) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Jobs.Info> listJob(String jid) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, CompletionStage<Result<Boolean>>> matchAsync(String target,
                                                                    CompletableFuture<GenericError> cancel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Map<String, Jobs.ListJobsEntry>> jobsByMetadata(Object metadata) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Map<String, Jobs.ListJobsEntry>> jobsByMetadata(Object metadata, LocalDateTime startTime,
                                                                    LocalDateTime endTime) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<CompletionStage<Map<String, Result<Boolean>>>> matchAsyncSSH(
            String target, CompletableFuture<GenericError> cancel) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Optional<LocalAsyncResult<T>> callAsync(LocalCall<T> callIn, Target<?> target,
                                                       Optional<ScheduleMetadata> metadataIn) {
        return Optional.empty();
    }

    @Override
    public Map<String, Result<Map<String, String>>> getPendingResume(List<String> minionIds) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Result<JsonElement>> rawJsonCall(LocalCall<?> call, String minionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SaltSSHService getSaltSSHService() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void refreshPillar(MinionList minionList) {
    }

    @Override
    public String checkSSLCert(String rootCA, SSLCertPair serverCertKey,
                                         List<String> intermediateCAs) throws IllegalArgumentException {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> selectMinions(String target, String targetType) {
        throw new UnsupportedOperationException();
    }

}
