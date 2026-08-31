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

/**
 * Constants shared by more than one class taking part in the hardware mapping
 */
public final class HardwareConstants {

    // Grain keys
    public static final String GRAIN_CPU_ARCH = "cpuarch";
    public static final String GRAIN_MEM_TOTAL = "mem_total";
    public static final String GRAIN_SWAP_TOTAL = "swap_total";
    public static final String GRAIN_TOTAL_NUM_CPUS = "total_num_cpus";

    // Smbios record keys
    public static final String DMI_KEY_MANUFACTURER = "manufacturer";
    public static final String DMI_KEY_PRODUCT_NAME = "product_name";

    // CPU reports no frequency
    public static final String CPU_MHZ_NOT_APPLICABLE = "0";

    // Errors
    public static final String HARDWARE_REFRESH_ERROR = "Hardware list could not be refreshed";
    public static final String HARDWARE_REFRESH_INCOMPLETE = "Hardware list could not be refreshed completely:\n";
    public static final Long ERROR_RESULT_CODE = -1L;

    private HardwareConstants() {
        throw new UnsupportedOperationException(NOT_INSTANTIABLE);
    }
}
