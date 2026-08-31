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

/**
 * Utility for CPU architectures.
 */
public class CpuArchUtil {

    // CPU architectures, as reported by the 'cpuarch' grain
    public static final String ARCH_AARCH64 = "aarch64";
    public static final String ARCH_ARM64 = "arm64";
    public static final String ARCH_I386 = "i386";
    public static final String ARCH_PPC64 = "ppc64";
    public static final String ARCH_PPC64LE = "ppc64le";
    public static final String ARCH_S390 = "s390";
    public static final String ARCH_S390X = "s390x";
    public static final String ARCH_X86_32_PREFIX = "i";
    public static final String ARCH_X86_32_SUFFIX = "86";
    public static final String ARCH_X86_64 = "x86_64";

    private CpuArchUtil() { }

    /**
     * @param cpuarch the cpu arch
     * @return true if the given cpuarch is PPC64.
     */
    public static boolean isPPC64(String cpuarch) {
        return ARCH_PPC64.equals(cpuarch) || ARCH_PPC64LE.equals(cpuarch);
    }

    /**
     * @param cpuarch the cpu arch
     * @return true if the given cpuarch is S390.
     */
    public static boolean isS390(String cpuarch) {
        return ARCH_S390.equals(cpuarch) || ARCH_S390X.equals(cpuarch);
    }

    /**
     * @param cpuarch the cpu arch
     * @return true if the given cpuarch is AArch64.
     */
    public static boolean isAarch64(String cpuarch) {
        return ARCH_AARCH64.equals(cpuarch) || ARCH_ARM64.equals(cpuarch);
    }

    /**
     *
     * @param cpuarch the cpu arch
     * @return Check if the given cpuarch is X86 (32 or 64 bit)
     */
    public static boolean isX86(String cpuarch) {
        return (cpuarch.startsWith(ARCH_X86_32_PREFIX) && cpuarch.endsWith(ARCH_X86_32_SUFFIX)) ||
                ARCH_X86_64.equals(cpuarch);
    }

    /**
     * @param cpuarch the cpu arch
     * @return true if the architecture supports DMI
     */
    public static boolean isDmiCapable(String cpuarch) {
        return isX86(cpuarch);
    }
}
