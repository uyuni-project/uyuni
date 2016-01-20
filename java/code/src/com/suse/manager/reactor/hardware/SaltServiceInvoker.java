/**
 * Copyright (c) 2016 SUSE LLC
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

    /**
     * The constructor
     * @param saltServiceIn the {@link SaltService} used to call Salt
     */
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

    /**
     * @see SaltService#getDmiRecords(String, Smbios.RecordType)
     * @param minionId the minion id
     * @param recordType the smbios record type
     * @return the DMI data as a map.
     */
    public Map<String, Object> getDmiRecords(String minionId,
                                             Smbios.RecordType recordType) {
        return getOrInvoke("dmi_" + recordType.getType(),
                () -> saltService.getDmiRecords(minionId, recordType));
    }

    /**
     * @see SaltService#getUdevdb(String)
     * @param minionId the minion id
     * @return the udev db as a map.
     */
    public List<Map<String, Object>> getUdevdb(String minionId) {
        return getOrInvoke("udevdb", () -> saltService.getUdevdb(minionId));
    }

    /**
     * This doesn't do any caching.
     * @see SaltService#getFileContent(String, String)
     * @param minionId the minion id
     * @param path the file path
     * @return the file content as a string
     */
    public String getFileContent(String minionId, String path) {
        // Don't cache this because the result may be large
        return saltService.getFileContent(minionId, path);
    }

    /**
     * @see SaltService#getMainframeSysinfoReadValues(String)
     * @param minionId the minion id
     * @return the read_values output as a string
     */
    public String getMainframeSysinfoReadValues(String minionId) {
        return getOrInvoke("sysinfo",
                () -> saltService.getMainframeSysinfoReadValues(minionId));
    }

    /**
     * @see SaltService#getCpuInfo(String)
     * @param minionId the minion id
     * @return the cpu info as a map.
     */
    public Map<String, Object> getCpuInfo(String minionId) {
        return getOrInvoke("cpu", () -> saltService.getCpuInfo(minionId));
    }

    /**
     * @see SaltService#getGrains(String)
     * @param minionId the minion id
     * @return the grains as a map.
     */
    public Map<String, Object> getGrains(String minionId) {
        return getOrInvoke("cpu", () -> saltService.getGrains(minionId));
    }

    /**
     * @return the {@link SaltService} used to call Salt
     */
    public SaltService getSaltService() {
        return saltService;
    }
}
