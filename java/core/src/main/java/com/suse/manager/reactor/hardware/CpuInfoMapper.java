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

import com.redhat.rhn.domain.server.CPU;
import com.redhat.rhn.domain.server.CPUArch;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;

import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.services.SaltGrains;

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
                return Optional.of("CPU: Grain 'cpuarch' has no value");
            }

            CPUArch arch = ServerFactory.lookupCPUArchByName(cpuArch);
            if (arch == null) {
                // should not happen but cpu.arch is not nullable so if we don't have
                // the arch we cannot persist the cpu
                String error = String.format("Could not find CPUArch in db for value '%s' for minion '%s'",
                        cpuArch, server.getMinionId());
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
            cpu.setNrsocket(grains.getValueAsLong("cpu_sockets").orElse(1L));
            cpu.setNrCore(grains.getValueAsLong("cpu_cores").orElse(1L));
            cpu.setNrThread(grains.getValueAsLong("cpu_threads").orElse(1L));
            // Use our custom grain. Salt has a 'num_cpus' grain but it gives
            // the number of active CPUs not the total num of CPUs in the system.
            // On s390x this number of active and actual CPUs can be different.
            cpu.setNrCPU(grains.getValueAsLong("total_num_cpus").orElse(0L));

            var archSpecs = grains.get("cpu_arch_specs")
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
            return Optional.of("CPU mapping failed: " + e.getMessage());
        }
    }

    /**
     * Extracts the value of the `cpuarch` grain and normalizes it
     *
     * @return the normalized CPU architecture string in lowercase
     *
     */
    protected String getCpuArch() {
        String cpuArch = grains.getValueAsString(SaltGrains.CPUARCH.getValue()).toLowerCase();
        return CpuArchUtil.isX86(cpuArch) && !cpuArch.equals("x86_64") ? "i386" : cpuArch;
    }


    /**
     * Map x86/x86_64 CPU information.
     */
    protected void mapX86CpuInfo(CPU cpu, ValueMap cpuInfo) {
        // /proc/cpuInfo -> model name
        cpu.setModel(CpuFieldTruncator.model(grains.getValueAsString("cpu_model")));
        // some machines don't report cpu MHz
        cpu.setMHz(CpuFieldTruncator.mhz(
            cpuInfo.get("cpu MHz").flatMap(ValueMap::toString).orElse("-1")));
        cpu.setVendor(CpuFieldTruncator.vendor(cpuInfo, "vendor_id"));
        cpu.setStepping(CpuFieldTruncator.stepping(cpuInfo, "stepping"));
        cpu.setFamily(CpuFieldTruncator.family(cpuInfo, "cpu family"));
        cpu.setCache(CpuFieldTruncator.cache(cpuInfo, "cache size"));
        cpu.setBogomips(CpuFieldTruncator.bogomips(cpuInfo, "bogomips"));
        cpu.setFlags(CpuFieldTruncator.flags(
            cpuInfo.getValueAsCollection("flags")
                .map(c -> c.stream()
                    .map(e -> Objects.toString(e, ""))
                    .collect(Collectors.joining(" ")))
                .orElse(null)));
        cpu.setVersion(CpuFieldTruncator.version(cpuInfo, "model"));
    }

    /**
     * Map PPC64 CPU information.
     */
    protected void mapPpc64CpuInfo(CPU cpu, ValueMap cpuInfo) {
        cpu.setModel(CpuFieldTruncator.model(cpuInfo.getValueAsString("cpu")));
        cpu.setVersion(CpuFieldTruncator.version(cpuInfo, "revision"));
        cpu.setBogomips(CpuFieldTruncator.bogomips(cpuInfo, "bogomips"));
        cpu.setVendor(CpuFieldTruncator.vendor(cpuInfo, "machine"));
        cpu.setMHz(CpuFieldTruncator.mhz(cpuInfo.get("clock")
                .flatMap(ValueMap::toString)
                .map(s -> StringUtils.substring(s, 0, -3)) // remove MHz suffix
                .orElse("-1")));
    }

    /**
     * Map S390 mainframe CPU information.
     */
    protected void mapS390CpuInfo(CPU cpu, ValueMap cpuInfo, String cpuArch) {
        cpu.setVendor(CpuFieldTruncator.vendor(cpuInfo, "vendor_id"));
        cpu.setModel(CpuFieldTruncator.model(cpuArch));
        cpu.setBogomips(CpuFieldTruncator.bogomips(cpuInfo, "bogomips per cpu"));
        cpu.setFlags(CpuFieldTruncator.flags(cpuInfo.get("features")
                .flatMap(ValueMap::toString).orElse(null)));
        cpu.setMHz("0");
    }

    /**
     * Map AArch64 CPU information.
     */
    protected void mapAarch64CpuInfo(CPU cpu) {
        cpu.setBogomips(grains.getValueAsString("bogomips", HardwareConstants.CPU_BOGOMIPS_LENGTH));
        cpu.setVendor(grains.getValueAsString("cpu_vendor", HardwareConstants.CPU_VENDOR_LENGTH));
        cpu.setStepping(grains.getValueAsString("cpu_stepping", HardwareConstants.CPU_STEPPING_LENGTH));
        cpu.setModel(CpuFieldTruncator.model(grains.getValueAsString("cpu_model")));
    }

}
