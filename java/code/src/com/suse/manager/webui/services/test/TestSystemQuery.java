package com.suse.manager.webui.services.test;

import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import com.redhat.rhn.domain.server.MinionServer;
import com.suse.manager.clusters.ClusterProviderParameters;
import com.suse.manager.webui.services.iface.RedhatProductInfo;
import com.suse.manager.webui.services.iface.SystemQuery;
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

public class TestSystemQuery implements SystemQuery {

    @Override
    public Optional<String> getMachineId(String minionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Map<String, Map<String, Object>>> listClusterNodes(
            MinionServer managementNode, ClusterProviderParameters clusterProviderParameters) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void notifySystemIdGenerated(MinionServer minion) throws InstantiationException, SaltException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<List<Zypper.ProductInfo>> getProducts(String minionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <R> Optional<R> callSync(LocalCall<R> call, String minionId) {
        return Optional.empty();
    }

    @Override
    public Optional<MgrUtilRunner.ExecResult> collectKiwiImage(
            MinionServer minion, String filepath, String imageStore) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<RedhatProductInfo> redhatProductInfo(String minionId) {
        throw new UnsupportedOperationException();
    }
}
