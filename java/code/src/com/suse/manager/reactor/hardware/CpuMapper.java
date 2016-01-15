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
import com.suse.manager.webui.services.SaltService;
import org.apache.commons.lang.StringUtils;

/**
 * Get the CPU information from a minion an store it in the SUMA db.
 */
public class CpuMapper extends AbstractHardwareMapper<CPU> {

    /**
     * The constructor
     * @param saltService a {@link SaltService} instance
     */
    public CpuMapper(SaltService saltService) {
        super(saltService);
    }

    @Override
    public CPU map(MinionServer server, ValueMap grains) {

        CPU cpu = new CPU();

        // os.uname[4]
        String cpuarch = grains.getValueAsString("cpuarch").toLowerCase();

        ValueMap cpuinfo = new ValueMap(SALT_SERVICE.getCpuInfo(server.getMinionId()));
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
            cpu.setModel(grains.getValueAsString("cpu_model"));
            // some machines don't report cpu MHz
            cpu.setMHz(cpuinfo.get("cpu MHz").flatMap(cpuinfo::toString).orElse("-1"));
            cpu.setVendor(cpuinfo.getValueAsString("vendor_id"));
            cpu.setStepping(cpuinfo.getValueAsString("stepping"));
            cpu.setFamily(cpuinfo.getValueAsString("cpu family"));
            cpu.setCache(cpuinfo.getValueAsString("cache size"));
            cpu.setBogomips(cpuinfo.getValueAsString("bogomips"));
            cpu.setFlags(cpuinfo.get("flags'").flatMap(cpuinfo::toString).orElse(null));
            cpu.setVersion(cpuinfo.getValueAsString("model"));

        }
        else if (CpuArchUtil.isPPC64(cpuarch)) {
            cpu.setModel(cpuinfo.getValueAsString("cpu"));
            cpu.setVersion(cpuinfo.getValueAsString("revision"));
            cpu.setBogomips(cpuinfo.getValueAsString("bogompis"));
            cpu.setVendor(cpuinfo.getValueAsString("machine"));
            cpu.setMHz(StringUtils.substring(cpuinfo.get("clock")
                    .flatMap(cpuinfo::toString)
                    .orElse("-1MHz"), 0, -3)); // remove MHz sufix
        }
        else if (CpuArchUtil.isS390(cpuarch)) {
            cpu.setVendor(cpuinfo.getValueAsString("vendor_id"));
            cpu.setModel(cpuarch);
            cpu.setBogomips(cpuinfo.getValueAsString("bogomips per cpu"));
            cpu.setFlags(cpuinfo.get("features").flatMap(cpuinfo::toString).orElse(null));
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

        return cpu;
    }
}
