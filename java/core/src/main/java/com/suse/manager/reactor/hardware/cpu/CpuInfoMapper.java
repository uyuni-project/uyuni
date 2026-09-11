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
package com.suse.manager.reactor.hardware.cpu;

import static com.suse.manager.reactor.hardware.CpuArchUtil.ARCH_I386;
import static com.suse.manager.reactor.hardware.CpuArchUtil.ARCH_X86_64;
import static com.suse.manager.reactor.hardware.HardwareConstants.CPU_MHZ_NOT_APPLICABLE;
import static com.suse.manager.reactor.hardware.HardwareConstants.GRAIN_CPU_ARCH;
import static com.suse.manager.reactor.hardware.HardwareConstants.GRAIN_TOTAL_NUM_CPUS;
import static com.suse.manager.reactor.hardware.cpu.CpuFieldTruncator.CPU_BOGOMIPS_LENGTH;
import static com.suse.manager.reactor.hardware.cpu.CpuFieldTruncator.CPU_STEPPING_LENGTH;
import static com.suse.manager.reactor.hardware.cpu.CpuFieldTruncator.CPU_VENDOR_LENGTH;

import com.redhat.rhn.domain.server.CPU;
import com.redhat.rhn.domain.server.CPUArch;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;

import com.suse.manager.reactor.hardware.CpuArchUtil;
import com.suse.manager.reactor.utils.ValueMap;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Maps CPU information from Salt grains and cpuInfo to the database.
 * Handles architecture-specific CPU data mapping for x86, PPC64, S390, and AArch64.
 */
public class CpuInfoMapper {

    private static final Logger LOG = LogManager.getLogger(CpuInfoMapper.class);

    // /proc/cpuinfo keys, as reported by the salt module status.cpuinfo
    private static final String CPUINFO_KEY_BOGOMIPS = "bogomips";
    private static final String CPUINFO_KEY_BOGOMIPS_PER_CPU = "bogomips per cpu";
    private static final String CPUINFO_KEY_CACHE_SIZE = "cache size";
    private static final String CPUINFO_KEY_CLOCK = "clock";
    private static final String CPUINFO_KEY_CPU = "cpu";
    private static final String CPUINFO_KEY_FAMILY = "cpu family";
    private static final String CPUINFO_KEY_FEATURES = "features";
    private static final String CPUINFO_KEY_FLAGS = "flags";
    private static final String CPUINFO_KEY_MACHINE = "machine";
    private static final String CPUINFO_KEY_MHZ = "cpu MHz";
    private static final String CPUINFO_KEY_MODEL = "model";
    private static final String CPUINFO_KEY_REVISION = "revision";
    private static final String CPUINFO_KEY_STEPPING = "stepping";
    private static final String CPUINFO_KEY_VENDOR_ID = "vendor_id";

    // Grain keys
    private static final String GRAIN_BOGOMIPS = "bogomips";
    private static final String GRAIN_CPU_ARCH_SPECS = "cpu_arch_specs";
    private static final String GRAIN_CPU_CORES = "cpu_cores";
    private static final String GRAIN_CPU_MODEL = "cpu_model";
    private static final String GRAIN_CPU_SOCKETS = "cpu_sockets";
    private static final String GRAIN_CPU_STEPPING = "cpu_stepping";
    private static final String GRAIN_CPU_THREADS = "cpu_threads";
    private static final String GRAIN_CPU_VENDOR = "cpu_vendor";

    // CPU values used when the reported one is missing
    private static final long CPU_COUNT_DEFAULT = 1L;
    private static final long CPU_COUNT_UNKNOWN = 0L;
    private static final String CPU_MHZ_UNKNOWN = "-1";

    // PPC64 reports the clock with a "MHz" suffix, eg. "3690.000000MHz", which has to be stripped
    private static final int PPC64_CLOCK_SUFFIX_OFFSET = -3;

    // Error messages
    private static final String CPUARCH_NOT_FOUND_FORMAT =
            "Could not find CPUArch in db for value '%s' for minion '%s'";
    private static final String CPU_MAPPING_FAILED = "CPU mapping failed: ";
    private static final String GRAIN_CPUARCH_HAS_NO_VALUE = "CPU: Grain 'cpuarch' has no value";

    private final MinionServer server;
    private final ValueMap grains;

    /**
     * Create a CPU information mapper.
     *
     * @param serverIn the minion server
     * @param grainsIn the grains
     */
    public CpuInfoMapper(MinionServer serverIn, ValueMap grainsIn) {
        this.server = serverIn;
        this.grains = grainsIn;
    }

    /**
     * Store CPU information given as a {@link ValueMap}.
     *
     * @param cpuInfo Salt returns /proc/cpuInfo data
     * @return Optional error message if mapping failed
     */
    public Optional<String> mapCpuInfo(ValueMap cpuInfo) {
        try {
            final CPU cpu = Optional.ofNullable(server.getCpu()).orElseGet(CPU::new);

            // os.uname[4]
            String cpuArch = getCpuArch();
            if (StringUtils.isBlank(cpuArch)) {
                LOG.error("Grain 'cpuarch' has no value for minion: {}", server.getMinionId());
                return Optional.of(GRAIN_CPUARCH_HAS_NO_VALUE);
            }

            CPUArch arch = ServerFactory.lookupCPUArchByName(cpuArch);
            if (arch == null) {
                // should not happen but cpu.arch is not nullable so if we don't have
                // the arch we cannot persist the cpu
                String error = String.format(CPUARCH_NOT_FOUND_FORMAT, cpuArch, server.getMinionId());
                LOG.error(error);
                return Optional.of(error);
            }

            cpu.setArch(arch);

            // See hardware.py read_cpuInfo()
            if (CpuArchUtil.isX86(cpuArch)) {
                mapX86CpuInfo(cpu, cpuInfo);
            }
            else if (CpuArchUtil.isPPC64(cpuArch)) {
                mapPpc64CpuInfo(cpu, cpuInfo);
            }
            else if (CpuArchUtil.isS390(cpuArch)) {
                mapS390CpuInfo(cpu, cpuInfo, cpuArch);
            }
            else if (CpuArchUtil.isAarch64(cpuArch)) {
                mapAarch64CpuInfo(cpu);
            }
            else {
                cpu.setVendor(cpuArch);
                cpu.setModel(cpuArch);
            }

            // Map common CPU information from grains
            cpu.setNrsocket(grains.getValueAsLong(GRAIN_CPU_SOCKETS).orElse(CPU_COUNT_DEFAULT));
            cpu.setNrCore(grains.getValueAsLong(GRAIN_CPU_CORES).orElse(CPU_COUNT_DEFAULT));
            cpu.setNrThread(grains.getValueAsLong(GRAIN_CPU_THREADS).orElse(CPU_COUNT_DEFAULT));
            // Use our custom grain. Salt has a 'num_cpus' grain but it gives
            // the number of active CPUs not the total num of CPUs in the system.
            // On s390x this number of active and actual CPUs can be different.
            cpu.setNrCPU(grains.getValueAsLong(GRAIN_TOTAL_NUM_CPUS).orElse(CPU_COUNT_UNKNOWN));

            var archSpecs = grains.get(GRAIN_CPU_ARCH_SPECS)
                    .filter(v -> v instanceof Map)
                    .map(v -> (Map<String, Object>) v)
                    .filter(map -> !map.isEmpty())
                    .orElse(null);
            cpu.setArchSpecs(archSpecs);

            cpu.setServer(server);
            server.setCpu(cpu);

            return Optional.empty();
        }
        catch (Exception e) {
            LOG.error("Failed to map CPU info for minion {} : {}", server.getMinionId(), e);
            return Optional.of(CPU_MAPPING_FAILED + e.getMessage());
        }
    }

    /**
     * Extracts the value of the `cpuarch` grain and normalizes it
     *
     * @return the normalized CPU architecture string in lowercase
     *
     */
    protected String getCpuArch() {
        String cpuArch = grains.getValueAsString(GRAIN_CPU_ARCH).toLowerCase();
        return CpuArchUtil.isX86(cpuArch) && !cpuArch.equals(ARCH_X86_64) ? ARCH_I386 : cpuArch;
    }


    /**
     * Map x86/x86_64 CPU information.
     */
    protected void mapX86CpuInfo(CPU cpu, ValueMap cpuInfo) {
        // /proc/cpuInfo -> model name
        cpu.setModel(CpuFieldTruncator.model(grains.getValueAsString(GRAIN_CPU_MODEL)));
        // some machines don't report cpu MHz
        cpu.setMHz(CpuFieldTruncator.mhz(
            cpuInfo.get(CPUINFO_KEY_MHZ).flatMap(ValueMap::toString).orElse(CPU_MHZ_UNKNOWN)));
        cpu.setVendor(CpuFieldTruncator.vendor(cpuInfo, CPUINFO_KEY_VENDOR_ID));
        cpu.setStepping(CpuFieldTruncator.stepping(cpuInfo, CPUINFO_KEY_STEPPING));
        cpu.setFamily(CpuFieldTruncator.family(cpuInfo, CPUINFO_KEY_FAMILY));
        cpu.setCache(CpuFieldTruncator.cache(cpuInfo, CPUINFO_KEY_CACHE_SIZE));
        cpu.setBogomips(CpuFieldTruncator.bogomips(cpuInfo, CPUINFO_KEY_BOGOMIPS));
        cpu.setFlags(CpuFieldTruncator.flags(
            cpuInfo.getValueAsCollection(CPUINFO_KEY_FLAGS)
                .map(c -> c.stream()
                    .map(e -> Objects.toString(e, StringUtils.EMPTY))
                    .collect(Collectors.joining(StringUtils.SPACE)))
                .orElse(null)));
        cpu.setVersion(CpuFieldTruncator.version(cpuInfo, CPUINFO_KEY_MODEL));
    }

    /**
     * Map PPC64 CPU information.
     */
    protected void mapPpc64CpuInfo(CPU cpu, ValueMap cpuInfo) {
        cpu.setModel(CpuFieldTruncator.model(cpuInfo.getValueAsString(CPUINFO_KEY_CPU)));
        cpu.setVersion(CpuFieldTruncator.version(cpuInfo, CPUINFO_KEY_REVISION));
        cpu.setBogomips(CpuFieldTruncator.bogomips(cpuInfo, CPUINFO_KEY_BOGOMIPS));
        cpu.setVendor(CpuFieldTruncator.vendor(cpuInfo, CPUINFO_KEY_MACHINE));
        cpu.setMHz(CpuFieldTruncator.mhz(cpuInfo.get(CPUINFO_KEY_CLOCK)
                .flatMap(ValueMap::toString)
                .map(s -> StringUtils.substring(s, 0, PPC64_CLOCK_SUFFIX_OFFSET)) // remove MHz suffix
                .orElse(CPU_MHZ_UNKNOWN)));
    }

    /**
     * Map S390 mainframe CPU information.
     */
    protected void mapS390CpuInfo(CPU cpu, ValueMap cpuInfo, String cpuArch) {
        cpu.setVendor(CpuFieldTruncator.vendor(cpuInfo, CPUINFO_KEY_VENDOR_ID));
        cpu.setModel(CpuFieldTruncator.model(cpuArch));
        cpu.setBogomips(CpuFieldTruncator.bogomips(cpuInfo, CPUINFO_KEY_BOGOMIPS_PER_CPU));
        cpu.setFlags(CpuFieldTruncator.flags(cpuInfo.get(CPUINFO_KEY_FEATURES)
                .flatMap(ValueMap::toString).orElse(null)));
        cpu.setMHz(CPU_MHZ_NOT_APPLICABLE);
    }

    /**
     * Map AArch64 CPU information.
     */
    protected void mapAarch64CpuInfo(CPU cpu) {
        cpu.setBogomips(grains.getValueAsString(GRAIN_BOGOMIPS, CPU_BOGOMIPS_LENGTH));
        cpu.setVendor(grains.getValueAsString(GRAIN_CPU_VENDOR, CPU_VENDOR_LENGTH));
        cpu.setStepping(grains.getValueAsString(GRAIN_CPU_STEPPING, CPU_STEPPING_LENGTH));
        cpu.setModel(CpuFieldTruncator.model(grains.getValueAsString(GRAIN_CPU_MODEL)));
    }

}
