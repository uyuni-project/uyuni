/**
 * Copyright (c) 2016 SUSE LLC
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
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerNetAddress4;
import com.redhat.rhn.domain.server.ServerNetAddress6;
import com.redhat.rhn.domain.server.ServerNetworkFactory;

import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.services.SaltGrains;
import com.suse.manager.webui.utils.salt.custom.SumaUtil;
import com.suse.salt.netapi.calls.modules.Network;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Store minion hardware details in the SUSE Manager database.
 */
public class HardwareMapper {

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(HardwareMapper.class);

    private final MinionServer server;
    private final ValueMap grains;
    private boolean hasPrimaryInterfaceSet = false;
    private List<String> errors = new LinkedList<>();

    /**
     * @param serverIn the minion server
     * @param grainsIn the grains
     */
    public HardwareMapper(MinionServer serverIn, ValueMap grainsIn) {
        this.server = serverIn;
        this.grains = grainsIn;
    }

    /**
     * Store CPU information given as a {@link ValueMap}.
     *
     * @param cpuinfo Salt returns /proc/cpuinfo data
     */
    public void mapCpuInfo(ValueMap cpuinfo) {
        final CPU cpu = Optional.ofNullable(server.getCpu()).orElseGet(CPU::new);

        // os.uname[4]
        String cpuarch = grains.getValueAsString(SaltGrains.CPUARCH.getValue())
                .toLowerCase();

        if (StringUtils.isBlank(cpuarch)) {
            errors.add("CPU: Grain 'cpuarch' has no value");
            LOG.error("Grain 'cpuarch' has no value for minion: " + server.getMinionId());
            return;
        }

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

    /**
     * Store network information as returned by Salt.
     *
     * @param interfaces network interfaces
     * @param primaryIps primary IP addresses
     * @param netModules network modules
     */
    public void mapNetworkInfo(Map<String, Network.Interface> interfaces,
            Optional<Map<SumaUtil.IPVersion, SumaUtil.IPRoute>> primaryIps,
            Map<String, Optional<String>> netModules) {
        if (interfaces.isEmpty()) {
            errors.add("Network: Salt module 'network.interfaces' returned en empty value");
            LOG.error("Salt module 'network.interfaces' returned en empty value " +
                    "for minion: " + server.getMinionId());
            return;
        }

        Optional<String> primaryIPv4 = primaryIps
                    .flatMap(x -> Optional.ofNullable(x.get(SumaUtil.IPVersion.IPV4)))
                    .map(SumaUtil.IPRoute::getSource);
        Optional<String> primaryIPv6 = primaryIps
                    .flatMap(x -> Optional.ofNullable(x.get(SumaUtil.IPVersion.IPV6)))
                    .map(SumaUtil.IPRoute::getSource);

        com.redhat.rhn.domain.server.Network network =
                new com.redhat.rhn.domain.server.Network();
        network.setHostname(grains.getOptionalAsString("fqdn").orElse(null));
        primaryIPv4.ifPresent(network::setIpaddr);
        primaryIPv6.ifPresent(network::setIp6addr);

        server.getNetworks().clear();
        server.addNetwork(network);

        interfaces.forEach((name, saltIface) -> {
            NetworkInterface ifaceEntity = server.getNetworkInterface(name);
            if (ifaceEntity == null) {
                // we got a new interface
                ifaceEntity = new NetworkInterface();
            }
            // else update the existing interface
            final NetworkInterface iface = ifaceEntity;

            iface.setHwaddr(saltIface.getHWAddr());
            iface.setModule(netModules.get(name).orElse(null));
            iface.setServer(server);
            iface.setName(name);

            server.addNetworkInterface(iface);

            // we have to do this because we need the id of the interface afterwards
            ServerFactory.saveNetworkInterface(iface);
            // flush & refresh iface because generated="insert"
            // on interfaceId does not seem to work
            ServerFactory.getSession().flush();
            ServerFactory.getSession().refresh(iface);

            Optional<Network.INet> inet = Optional.ofNullable(saltIface.getInet())
                    .flatMap(addr -> addr.stream().findFirst());

            inet.ifPresent(addr4 -> {
                // set IPv4 network info
                ServerNetAddress4 ipv4 = ServerNetworkFactory
                        .findServerNetAddress4(iface.getInterfaceId());
                if (ipv4 == null) {
                    ipv4 = new ServerNetAddress4();
                }
                ipv4.setInterfaceId(iface.getInterfaceId());
                ipv4.setAddress(addr4.getAddress().orElse(null));
                ipv4.setNetmask(addr4.getNetmask().orElse(null));
                ipv4.setBroadcast(addr4.getBroadcast().orElse(null));

                ServerNetworkFactory.saveServerNetAddress4(ipv4);

                if (StringUtils.equals(ipv4.getAddress(), primaryIPv4.orElse(null))) {
                    hasPrimaryInterfaceSet = true;
                    iface.setPrimary("Y");
                }
            });

            Optional<Network.INet6> inet6 = Optional.ofNullable(saltIface.getInet6())
                    .flatMap(addr -> addr.stream().findFirst());
            inet6.ifPresent(addr6 -> {
                // set IPv6 network info
                ServerNetAddress6 ipv6 = ServerNetworkFactory
                        .findServerNetAddress6(iface.getInterfaceId());
                if (ipv6 == null) {
                    ipv6 = new ServerNetAddress6();
                }
                ipv6.setInterfaceId(iface.getInterfaceId());
                ipv6.setAddress(addr6.getAddress());
                ipv6.setNetmask(addr6.getPrefixlen());
                // scope is part of the entity's composite-id
                // so if it's null we'll get a list with null on namedQuery.list()
                // therefore we need a default value
                ipv6.setScope(Optional.ofNullable(addr6.getScope()).orElse("unknown"));

                ServerNetworkFactory.saveServerNetAddress6(ipv6);
            });
        });

        if (!hasPrimaryInterfaceSet) {
            primaryIPv6.ifPresent(ipv6Primary -> {
                server.getNetworkInterfaces().stream()
                    .filter(netIf -> netIf.getIPv6Addresses().stream()
                        .anyMatch(address -> ipv6Primary.equals(address.getAddress()))
                    )
                    .findFirst()
                    .ifPresent(primaryNetIf -> primaryNetIf.setPrimary("Y"));
            });
        }
    }

    /**
     * Return a (possibly empty) list of error messages.
     *
     * @return error messages
     */
    public List<String> getErrors() {
        return errors;
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
