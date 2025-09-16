/*
 * Copyright (c) 2015 SUSE LLC
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

import org.apache.commons.lang3.StringUtils;

/**
 * Utility for CPU architectures.
 */
public class CpuArchUtil {

    private CpuArchUtil() { }

    /**
     * @param cpuarch the cpu arch
     * @return true if the given cpuarch is PPC64.
     */
    public static boolean isPPC64(String cpuarch) {
        return "ppc64".equals(cpuarch) || "ppc64le".equals(cpuarch);
    }

    /**
     * @param cpuarch the cpu arch
     * @return true if the given cpuarch is S390.
     */
    public static boolean isS390(String cpuarch) {
        return "s390".equals(cpuarch) || "s390x".equals(cpuarch);
    }

    /**
     * @param cpuarch the cpu arch
     * @return true if the given cpuarch is AArch64.
     */
    public static boolean isAarch64(String cpuarch) {
        return "aarch64".equals(cpuarch) || "arm64".equals(cpuarch);
    }

    /**
     *
     * @param cpuarch the cpu arch
     * @return Check if the given cpuarch is X86 (32 or 64 bit)
     */
    public static boolean isX86(String cpuarch) {
        return (cpuarch.startsWith("i") &&
                StringUtils.substring(cpuarch, -2, cpuarch.length()).equals("86")) ||
                "x86_64".equals(cpuarch);
    }

    /**
     * @param cpuarch the cpu arch
     * @return true if the architecture supports DMI
     */
    public static boolean isDmiCapable(String cpuarch) {
        return isX86(cpuarch);
    }
}
