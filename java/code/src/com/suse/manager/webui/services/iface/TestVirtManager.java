package com.suse.manager.webui.services.iface;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.redhat.rhn.domain.server.MinionServer;
import com.suse.manager.virtualization.GuestDefinition;
import com.suse.manager.virtualization.PoolCapabilitiesJson;
import com.suse.manager.virtualization.PoolDefinition;

import java.util.Map;
import java.util.Optional;

public class TestVirtManager implements VirtManager {

    @Override
    public Optional<GuestDefinition> getGuestDefinition(String minionId, String domainName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<Map<String, JsonElement>> getCapabilities(String minionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<PoolCapabilitiesJson> getPoolCapabilities(String minionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<PoolDefinition> getPoolDefinition(String minionId, String poolName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, JsonObject> getNetworks(String minionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, JsonObject> getPools(String minionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Map<String, JsonObject>> getVolumes(String minionId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void updateLibvirtEngine(MinionServer minion) {
        throw new UnsupportedOperationException();
    }
}
