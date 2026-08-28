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

import static com.suse.manager.reactor.hardware.HardwareConstants.CONTACT_METHOD_DEFAULT;
import static com.suse.manager.reactor.hardware.HardwareConstants.S390_DIGITAL_SERVER_ID_FORMAT;
import static com.suse.manager.reactor.hardware.HardwareConstants.S390_HOST_DESCRIPTION_FORMAT;
import static com.suse.manager.reactor.hardware.HardwareConstants.S390_HOST_NAME_FORMAT;
import static com.suse.manager.reactor.hardware.HardwareConstants.SERVER_ARCH_LINUX_SUFFIX;
import static com.suse.manager.reactor.hardware.HardwareConstants.SERVER_SECRET_LENGTH;
import static com.suse.manager.reactor.hardware.HardwareConstants.SYSINFO_KEY_SEQUENCE_CODE;
import static com.suse.manager.reactor.hardware.HardwareConstants.SYSINFO_KEY_TYPE;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.server.CPU;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.server.VirtualInstanceState;
import com.redhat.rhn.domain.server.VirtualInstanceType;
import com.redhat.rhn.manager.entitlement.EntitlementManager;

import com.suse.manager.reactor.hardware.CpuArchUtil;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.services.SaltGrains;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.SecureRandom;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Maps the sysinfo of a S390 mainframe to the database. The minion reports information about the host it runs
 * on, which is registered as a foreign system and linked to the minion as its virtual host.
 */
public class SysinfoMapper {

    private static final Logger LOG = LogManager.getLogger(SysinfoMapper.class);

    private final MinionServer server;
    private final ValueMap grains;

    /**
     * Create a mainframe sysinfo mapper.
     *
     * @param serverIn the minion server
     * @param grainsIn the grains
     */
    public SysinfoMapper(MinionServer serverIn, ValueMap grainsIn) {
        this.server = serverIn;
        this.grains = grainsIn;
    }

    /**
     * Map mainframe sysinfo to the database as queried from Salt.
     *
     * @param readValuesOutput mainframe sysinfo as returned by mainframesysinfo.read_values
     * @return Optional error message if mapping failed
     */
    public Optional<String> mapSysinfo(String readValuesOutput) {
        try {
            // original code: hardware.py get_sysinfo()
            if (StringUtils.isBlank(readValuesOutput)) {
                return Optional.empty();
            }

            String cpuArch = grains.getValueAsString(SaltGrains.CPUARCH.getValue()).toLowerCase();
            Map<String, String> sysvalues = SysinfoParser.parseSysinfo(readValuesOutput);
            String sequenceCode = sysvalues.get(SYSINFO_KEY_SEQUENCE_CODE);
            String type = sysvalues.get(SYSINFO_KEY_TYPE);

            // we're only interested in the special case where we're on a S390 mainframe and we got info
            // about the virtual host this system is running on
            if (StringUtils.isBlank(sequenceCode) || StringUtils.isBlank(type) || !CpuArchUtil.isS390(cpuArch)) {
                return Optional.empty();
            }

            String identifier = String.format(S390_DIGITAL_SERVER_ID_FORMAT, sequenceCode);
            Server zhost = Optional
                    .ofNullable(ServerFactory.lookupForeignSystemByDigitalServerId(identifier))
                    .orElseGet(() -> createHost(identifier, sequenceCode, type, cpuArch, sysvalues));

            LOG.debug("Update server info for: {}", identifier);
            zhost.updateServerInfo();

            updateHostCpu(zhost, cpuArch, type, SysinfoParser.totalIfls(sysvalues));
            linkMinionToHost(zhost);

            return Optional.empty();
        }
        catch (Exception e) {
            LOG.error("Failed to map mainframe sysinfo for minion {} : {}", server.getMinionId(), e);
            return Optional.of("Mainframe sysinfo mapping failed: " + e.getMessage());
        }
    }

    /**
     * Register the S390 host as a foreign system
     *
     * @param identifier the host's digital server id
     * @param sequenceCode the sequence code of the host
     * @param type the type of the host
     * @param cpuArch the CPU architecture of the host
     * @param sysvalues the system values parsed from the sysinfo output
     * @return the created Server instance representing the S390 host
     */
    private Server createHost(
            String identifier, String sequenceCode, String type, String cpuArch, Map<String, String> sysvalues
    ) {
        String os = SysinfoParser.osNameForS390Arch(sysvalues);
        String osVersion = SysinfoParser.osVersionForS390Arch(sysvalues);

        Server zhost = ServerFactory.createServer();
        // OLDTODO extract this cpuarch + "-redhat-linux" in some common util
        zhost.setServerArch(ServerFactory.lookupServerArchByLabel(cpuArch + SERVER_ARCH_LINUX_SUFFIX));
        zhost.setName(
                String.format(S390_HOST_NAME_FORMAT, SysinfoParser.serverFamilyForS390Arch(type), type, sequenceCode)
        );
        zhost.setOs(os);
        zhost.setRelease(type);
        zhost.setLastBoot(System.currentTimeMillis() / 1000);
        // see server_hardware.py SystemInformation.__init__()
        zhost.setDescription(String.format(S390_HOST_DESCRIPTION_FORMAT, os, osVersion, cpuArch));

        zhost.setDigitalServerId(identifier);
        zhost.setOrg(OrgFactory.getSatelliteOrg()); // OLDTODO clarify this
        zhost.setSecret(RandomStringUtils.random(SERVER_SECRET_LENGTH, 0, 0, true, true, null, new SecureRandom()));
        zhost.setAutoUpdate("N");
        zhost.setContactMethod(ServerFactory.findContactMethodByLabel(CONTACT_METHOD_DEFAULT));
        server.setLastBoot(System.currentTimeMillis() / 1000);

        ServerFactory.save(zhost);

        GlobalInstanceHolder.SYSTEM_ENTITLEMENT_MANAGER.setBaseEntitlement(zhost,
                EntitlementManager.getByName(EntitlementManager.FOREIGN_ENTITLED));
        LOG.debug("New host created: {}", identifier);

        return zhost;
    }

    /**
     * Keep the CPU of the S390 host in sync with the number of IFLs it reports.
     * @param zhost the S390 host server instance
     * @param cpuArch the CPU architecture of the host
     * @param type the type of the host
     * @param totalIfls the total number of IFLs reported by the host
     */
    private void updateHostCpu(Server zhost, String cpuArch, String type, long totalIfls) {
        CPU hostCpu = zhost.getCpu();
        if (hostCpu != null && (hostCpu.getNrsocket() == null || hostCpu.getNrsocket().longValue() == totalIfls)) {
            return;
        }

        LOG.debug("Update host cpu: {}", totalIfls);
        CPU cpu = Optional.ofNullable(hostCpu).orElseGet(CPU::new);
        cpu.setNrCPU(totalIfls);
        cpu.setVersion(null);
        cpu.setMHz("0");
        cpu.setCache(null);
        cpu.setFamily(null);
        cpu.setBogomips(null);
        cpu.setNrsocket(totalIfls);
        cpu.setNrCore(totalIfls);
        cpu.setNrThread(1L);
        cpu.setArch(ServerFactory.lookupCPUArchByName(cpuArch));
        cpu.setFlags(null);
        cpu.setStepping(null);
        cpu.setModel(cpuArch);
        cpu.setVendor(type);

        zhost.setCpu(cpu);
        cpu.setServer(zhost);
    }

    /**
     * Register the minion as a guest of the S390 host.
     * @param zhost the S390 host server instance
     */
    private void linkMinionToHost(Server zhost) {
        VirtualInstanceFactory vinstFactory = VirtualInstanceFactory.getInstance();
        VirtualInstance vinst = vinstFactory.lookupByGuestId(server.getId());

        if (vinst == null || vinst.getHostSystem() == null) {
            VirtualInstanceType fullVirtType = vinstFactory.getFullyVirtType();
            VirtualInstanceState unknownState = vinstFactory.getUnknownState();

            // create the host
            VirtualInstance vinstHost = new VirtualInstance();
            vinstHost.setHostSystem(zhost);
            vinstHost.setGuestSystem(null);
            vinstHost.setConfirmed(1L);
            vinstHost.setUuid(null);
            vinstHost.setType(fullVirtType);
            vinstHost.setState(unknownState);
            vinstFactory.saveVirtualInstance(vinstHost);

            // create the guest
            VirtualInstance vinstGuest = new VirtualInstance();
            vinstGuest.setHostSystem(zhost);
            vinstGuest.setGuestSystem(server);
            vinstGuest.setConfirmed(1L);
            vinstGuest.setUuid(UUID.randomUUID().toString().replace("-", StringUtils.EMPTY));
            vinstGuest.setType(fullVirtType);
            vinstGuest.setState(unknownState);
            vinstFactory.saveVirtualInstance(vinstGuest);
        }
        else if (!vinst.getHostSystem().getId().equals(zhost.getId())) {
            LOG.debug("Updating virtual instance {} with {}", vinst.getId(), zhost.getId());
            vinst.setHostSystem(zhost);
            vinstFactory.saveVirtualInstance(vinst);
        }
    }

}
