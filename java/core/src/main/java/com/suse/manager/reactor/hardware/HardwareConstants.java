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
 * Constants used in hardware mapping operations.
 */
public final class HardwareConstants {

    // CPU field length limits (from rhnCpu table schema)
    public static final int CPU_VENDOR_LENGTH = 32;
    public static final int CPU_MODEL_LENGTH = 32;
    public static final int CPU_VERSION_LENGTH = 32;
    public static final int CPU_FAMILY_LENGTH = 32;
    public static final int CPU_STEPPING_LENGTH = 16;
    public static final int CPU_FLAGS_LENGTH = 2048;
    public static final int CPU_BOGOMIPS_LENGTH = 16;
    public static final int CPU_CACHE_LENGTH = 16;
    public static final int CPU_MHZ_LENGTH = 16;

    // Error messages
    public static final String HARDWARE_REFRESH_INCOMPLETE = "Hardware list could not be refreshed completely:\n";
    public static final String HARDWARE_REFRESH_ERROR = "Hardware list could not be refreshed";

    // Error codes
    public static final Long ERROR_RESULT_CODE = -1L;

    private HardwareConstants() {
        throw new UnsupportedOperationException(NOT_INSTANTIABLE);
    }
}
