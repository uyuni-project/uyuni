package com.suse.manager.reactor.hardware;

import com.suse.manager.webui.services.SaltService;
import com.suse.manager.webui.utils.salt.Smbios;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Wrapper around {@link SaltService} that caches some of the calls.
 */
public class SaltServiceInvoker {

    private SaltService saltService;

    private Map<String, Object> cache = new HashMap<>();

    public SaltServiceInvoker(SaltService saltServiceIn) {
        this.saltService = saltServiceIn;
    }

    private <T> T getOrInvoke(String key, Supplier<T> supplier) {
        return (T)cache.computeIfAbsent(key, k -> {
            Object saltValue = supplier.get();
            cache.put(k, saltValue);
            return saltValue;
        });
    }

    public Map<String, Object> getDmiRecords(String minionId,
                                             Smbios.RecordType recordType) {
        return getOrInvoke("dmi_" + recordType.getType(),
                () -> saltService.getDmiRecords(minionId, recordType));
    }

    public List<Map<String, Object>> getUdevdb(String minionId) {
        return getOrInvoke("udevdb", () -> saltService.getUdevdb(minionId));
    }

    public String getFileContent(String minionId, String path) {
        // Don't cache this because the result may be large
        return saltService.getFileContent(minionId, path);
    }

    public String getMainframeSysinfoReadValues(String minionId) {
        return getOrInvoke("sysinfo",
                () -> saltService.getMainframeSysinfoReadValues(minionId));
    }

    public Map<String, Object> getCpuInfo(String minionId) {
        return getOrInvoke("cpu", () -> saltService.getCpuInfo(minionId));
    }

    public Map<String, Object> getGrains(String minionId) {
        return getOrInvoke("cpu", () -> saltService.getGrains(minionId));
    }

    public SaltService getSaltService() {
        return saltService;
    }
}
