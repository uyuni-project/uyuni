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
package com.suse.manager.reactor.hardware.sysinfo;

import static com.redhat.rhn.common.ExceptionMessage.NOT_INSTANTIABLE;
import static com.suse.manager.reactor.hardware.HardwareConstants.S390_DEFAULT_CONTROL_PROGRAM;
import static com.suse.manager.reactor.hardware.HardwareConstants.S390_UNKNOWN_OS_VERSION;
import static com.suse.manager.reactor.hardware.HardwareConstants.SYSINFO_KEY_CONTROL_PROGRAM;
import static com.suse.manager.reactor.hardware.HardwareConstants.SYSINFO_KEY_CPUS_TOTAL;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Parses the mainframe sysinfo returned by the salt module mainframesysinfo.read_values, which in turn
 * reports the output of "/usr/bin/read_values -s" or, alternatively, of "cat /proc/sysinfo".
 */
public final class SysinfoParser {

    private static final Logger LOG = LogManager.getLogger(SysinfoParser.class);

    private SysinfoParser() {
        throw new UnsupportedOperationException(NOT_INSTANTIABLE);
    }

    /**
     * Split the sysinfo output into its "key: value" pairs.
     *
     * @param readValuesOutput mainframe sysinfo as returned by mainframesysinfo.read_values, not nullable
     * @return the reported values, keyed by their label
     */
    public static Map<String, String> parseSysinfo(String readValuesOutput) {
        Map<String, String> values = new HashMap<>();
        for (String line : readValuesOutput.split("\\r?\\n")) {
            String[] split = StringUtils.split(line, ":", 2);
            if (split.length == 2) {
                values.put(StringUtils.trim(split[0]), StringUtils.trim(split[1]));
            }
        }
        return values;
    }

    /**
     * Get the control program the minion runs on, eg. "z/VM    6.3.0" or "KVM/Linux".
     */
    private static String getOsStringForS390Arch(Map<String, String> sysvalues) {
        return sysvalues.entrySet().stream()
                .filter(e -> e.getKey().toLowerCase().contains(SYSINFO_KEY_CONTROL_PROGRAM))
                .map(Map.Entry::getValue)
                .findFirst().orElse(S390_DEFAULT_CONTROL_PROGRAM);
    }

    /**
     * Get the name of the operating system of the mainframe host, taken from the control program running the
     * minion, eg. "z/VM" out of "z/VM    6.3.0".
     *
     * @param sysvalues the parsed sysinfo
     * @return the OS name
     */
    public static String osNameForS390Arch(Map<String, String> sysvalues) {
        String osString = getOsStringForS390Arch(sysvalues);
        int index = osString.indexOf(StringUtils.SPACE);
        return index > 0 ? osString.substring(0, index) : osString;
    }

    /**
     * Get the version of the operating system of the mainframe host, eg. "6.3.0" out of "z/VM    6.3.0".
     *
     * @param sysvalues the parsed sysinfo
     * @return the OS version or "N/A" if the control program does not report one
     */
    public static String osVersionForS390Arch(Map<String, String> sysvalues) {
        String osString = getOsStringForS390Arch(sysvalues);
        int index = osString.indexOf(StringUtils.SPACE);
        return index > 0 ?
                osString.substring(index).replace(StringUtils.SPACE, StringUtils.EMPTY) : S390_UNKNOWN_OS_VERSION;
    }

    /**
     * Get the commercial name of the mainframe generation for a given machine type.
     *
     * @param type the machine type as reported by the sysinfo
     * @return the server family or an empty string if the type is unknown
     */
    public static String serverFamilyForS390Arch(String type) {
        // z17, z16, z15: https://www.ibm.com/docs/en/zos/3.1.0?topic=system-identifying-server-requirements
        // other codes: https://www.ibm.com/support/pages/processor-version-codes-and-srm-constants

        return switch (type) {
            case "9175" -> "z17";
            case "3931", "3932" -> "z16";
            case "8561", "8562" -> "z15";
            case "3906", "3907" -> "z14";
            case "2964", "2965" -> "z13";
            case "2827", "2828" -> "z12";
            case "2817", "2818" -> "zEnterprise 114";
            case "2097", "2098" -> "z10";
            case "2094", "2096" -> "z9";
            default -> "";
        };
    }

    /**
     * Get the total number of IFLs (Integrated Facility for Linux processors) of the mainframe host.
     *
     * @param sysvalues the parsed sysinfo
     * @return the number of IFLs or zero if it is missing or not a number
     */
    public static long totalIfls(Map<String, String> sysvalues) {
        try {
            return Long.parseLong(sysvalues.getOrDefault(SYSINFO_KEY_CPUS_TOTAL, "0"));
        }
        catch (NumberFormatException e) {
            LOG.warn("Invalid '{}' value: {}", SYSINFO_KEY_CPUS_TOTAL, e.getMessage());
            return 0L;
        }
    }

}
