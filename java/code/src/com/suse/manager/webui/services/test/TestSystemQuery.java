package com.suse.manager.webui.services.test;

import com.redhat.rhn.domain.server.MinionServer;

import com.suse.manager.clusters.ClusterProviderParameters;
import com.suse.manager.webui.services.iface.RedhatProductInfo;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.impl.runner.MgrUtilRunner;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.Zypper;
import com.suse.salt.netapi.exception.SaltException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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
