/**
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

import com.redhat.rhn.domain.server.CPU;
import com.redhat.rhn.domain.server.CPUArch;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.services.SaltGrains;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Get the CPU information from a minion an store it in the SUMA db.
 */
public class CpuMapper extends AbstractHardwareMapper<CPU> {

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(CpuMapper.class);

    /**
     * The constructor
     * @param saltServiceInvoker a {@link SaltServiceInvoker} instance
     */
    public CpuMapper(SaltServiceInvoker saltServiceInvoker) {
        super(saltServiceInvoker);
    }

    @Override
    public void doMap(MinionServer server, ValueMap grains) {

        final CPU cpu = Optional.ofNullable(server.getCpu()).orElseGet(CPU::new);

        // os.uname[4]
        String cpuarch = grains.getValueAsString(SaltGrains.CPUARCH.getValue())
                .toLowerCase();

        if (StringUtils.isBlank(cpuarch)) {
            setError("Grain 'cpuarch' has no value");
            LOG.warn("Grain 'cpuarch' has no value for minion: " + server.getMinionId());
            return;
        }

        ValueMap cpuinfo = saltInvoker.getCpuInfo(server.getMinionId())
                .map(ValueMap::new).orElseGet(ValueMap::new);
        // salt returns /proc/cpuinfo data

        // See hardware.py read_cpuinfo()
        if (CpuArchUtil.isX86(cpuarch)) {
            if (cpuarch.equals("x86_64")) {
                cpuarch = "x86_64";
            }
            else {
                cpuarch = "i386";
            }

            // /proc/cpuinfo -> model name
            cpu.setModel(truncateModel(grains.getValueAsString("cpu_model")));
            // some machines don't report cpu MHz
            cpu.setMHz(truncateMhz(cpuinfo.get("cpu MHz").flatMap(ValueMap::toString)
                    .orElse("-1")));
            cpu.setVendor(truncateVendor(cpuinfo, "vendor_id"));
            cpu.setStepping(truncateStepping(cpuinfo, "stepping"));
            cpu.setFamily(truncateFamily(cpuinfo, "cpu family"));
            cpu.setCache(truncateCache(cpuinfo, "cache size"));
            cpu.setBogomips(truncateBogomips(cpuinfo, "bogomips"));
            cpu.setFlags(truncateFlags(cpuinfo.getValueAsCollection("flags")
                    .map(c -> c.stream()
                        .map(e -> Objects.toString(e, ""))
                        .collect(Collectors.joining(" ")))
                    .orElse(null)));
            cpu.setVersion(truncateVersion(cpuinfo, "model"));

        }
        else if (CpuArchUtil.isPPC64(cpuarch)) {
            cpu.setModel(truncateModel(cpuinfo.getValueAsString("cpu")));
            cpu.setVersion(truncateVersion(cpuinfo, "revision"));
            cpu.setBogomips(truncateBogomips(cpuinfo, "bogompis"));
            cpu.setVendor(truncateVendor(cpuinfo, "machine"));
            cpu.setMHz(truncateMhz(StringUtils.substring(cpuinfo.get("clock")
                    .flatMap(ValueMap::toString)
                    .orElse("-1MHz"), 0, -3))); // remove MHz sufix
        }
        else if (CpuArchUtil.isS390(cpuarch)) {
            cpu.setVendor(truncateVendor(cpuinfo, "vendor_id"));
            cpu.setModel(truncateModel(cpuarch));
            cpu.setBogomips(truncateBogomips(cpuinfo, "bogomips per cpu"));
            cpu.setFlags(truncateFlags(cpuinfo.get("features")
                    .flatMap(ValueMap::toString).orElse(null)));
            cpu.setMHz("0");
        }
        else {
            cpu.setVendor(cpuarch);
            cpu.setModel(cpuarch);
        }

        CPUArch arch = ServerFactory.lookupCPUArchByName(cpuarch);
        cpu.setArch(arch);

        cpu.setNrsocket(grains.getValueAsLong("cpusockets").orElse(null));
        // Use our custom grain. Salt has a 'num_cpus' grain but it gives
        // the number of active CPUs not the total num of CPUs in the system.
        // On s390x this number of active and actual CPUs can be different.
        cpu.setNrCPU(grains.getValueAsLong("total_num_cpus").orElse(0L));

        if (arch != null) {
            // should not happen but arch is not nullable so if we don't have
            // the arch we cannot insert the cpu data
            cpu.setServer(server);
            server.setCpu(cpu);
        }
    }

    private String truncateVendor(ValueMap cpuinfo, String key) {
        return cpuinfo.getValueAsString(key, 32);
    }

    private String truncateVersion(ValueMap cpuinfo, String key) {
        return cpuinfo.getValueAsString(key, 32);
    }

    private String truncateBogomips(ValueMap cpuinfo, String key) {
        return cpuinfo.getValueAsString(key, 16);
    }

    private String truncateCache(ValueMap cpuinfo, String key) {
        return cpuinfo.getValueAsString(key, 16);
    }

    private String truncateFamily(ValueMap cpuinfo, String key) {
        return cpuinfo.getValueAsString(key, 32);
    }

    private String truncateMhz(String value) {
        return StringUtils.substring(value, 0, 16);
    }

    private String truncateStepping(ValueMap cpuinfo, String key) {
        return cpuinfo.getValueAsString(key, 16);
    }

    private String truncateModel(String value) {
        return StringUtils.substring(value, 0, 32);
    }

    private String truncateFlags(String value) {
        return StringUtils.substring(value, 0, 2048);
    }
}
