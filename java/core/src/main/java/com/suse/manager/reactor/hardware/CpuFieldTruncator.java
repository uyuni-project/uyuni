/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.reactor.hardware;

import static com.redhat.rhn.common.ExceptionMessage.NOT_INSTANTIABLE;

import com.suse.manager.reactor.utils.ValueMap;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for truncating CPU field values - helps to match database constraints and standardize values.
 * Consolidates 9 similar/identical truncation methods from HardwareMapper into a single utility.
 */
public final class CpuFieldTruncator {

    /**
     * Get CPU vendor and truncate to schema length.
     *
     * @param cpuInfo the CPU info map
     * @param key the key to look up
     * @return truncated vendor string or empty string if not found
     */
    public static String vendor(ValueMap cpuInfo, String key) {
        return cpuInfo.getValueAsString(key, HardwareConstants.CPU_VENDOR_LENGTH);
    }

    /**
     * Get CPU model and truncate to schema length.
     *
     * @param value the model value
     * @return truncated model string
     */
    public static String model(String value) {
        return StringUtils.substring(value, 0, HardwareConstants.CPU_MODEL_LENGTH);
    }

    /**
     * Get CPU version and truncate to schema length.
     *
     * @param cpuInfo the CPU info map
     * @param key the key to look up
     * @return truncated version string or empty string if not found
     */
    public static String version(ValueMap cpuInfo, String key) {
        return cpuInfo.getValueAsString(key, HardwareConstants.CPU_VERSION_LENGTH);
    }

    /**
     * Get CPU family and truncate to schema length.
     *
     * @param cpuInfo the CPU info map
     * @param key the key to look up
     * @return truncated family string or empty string if not found
     */
    public static String family(ValueMap cpuInfo, String key) {
        return cpuInfo.getValueAsString(key, HardwareConstants.CPU_FAMILY_LENGTH);
    }

    /**
     * Get CPU stepping and truncate to schema length.
     *
     * @param cpuInfo the CPU info map
     * @param key the key to look up
     * @return truncated stepping string or empty string if not found
     */
    public static String stepping(ValueMap cpuInfo, String key) {
        return cpuInfo.getValueAsString(key, HardwareConstants.CPU_STEPPING_LENGTH);
    }

    /**
     * Get CPU flags and truncate to schema length.
     *
     * @param value the flags value
     * @return truncated flags string
     */
    public static String flags(String value) {
        return StringUtils.substring(value, 0, HardwareConstants.CPU_FLAGS_LENGTH);
    }

    /**
     * Get CPU bogomips and truncate to schema length.
     *
     * @param cpuInfo the CPU info map
     * @param key the key to look up
     * @return truncated bogomips string or empty string if not found
     */
    public static String bogomips(ValueMap cpuInfo, String key) {
        return cpuInfo.getValueAsString(key, HardwareConstants.CPU_BOGOMIPS_LENGTH);
    }

    /**
     * Get CPU cache and truncate to schema length.
     *
     * @param cpuInfo the CPU info map
     * @param key the key to look up
     * @return truncated cache string or empty string if not found
     */
    public static String cache(ValueMap cpuInfo, String key) {
        return cpuInfo.getValueAsString(key, HardwareConstants.CPU_CACHE_LENGTH);
    }

    /**
     * Get CPU MHz and truncate to schema length.
     *
     * @param value the MHz value
     * @return truncated MHz string
     */
    public static String mhz(String value) {
        return StringUtils.substring(value, 0, HardwareConstants.CPU_MHZ_LENGTH);
    }

    private CpuFieldTruncator() {
        throw new UnsupportedOperationException(NOT_INSTANTIABLE);
    }
}
