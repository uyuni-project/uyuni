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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.redhat.rhn.domain.server.CPU;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerInfo;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.services.SaltService;

/**
 * Check virtual host information in case of a S390 minion.
 */
public class SysinfoMapper extends AbstractHardwareMapper<VirtualInstance> {

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(SysinfoMapper.class);

    /**
     * The constructor.
     * @param saltService a {@link SaltService} instance
     */
    public SysinfoMapper(SaltService saltService) {
        super(saltService);
    }

    @Override
    public VirtualInstance map(MinionServer server, ValueMap grains) {
        String cpuarch = grains.getValueAsString("cpuarch").toLowerCase();
        String minionId = server.getMinionId();
        try {
            // call custom minion to get read_values info (if available)
            // original code: hardware.py get_sysinfo()
            String readValuesOutput = SALT_SERVICE.getMainframeSysinfoReadValues(minionId);
            Map<String, String> sysvalues = new HashMap<>();
            for (String line : readValuesOutput.split("\\r?\\n")) {
                if (!line.contains(":")) {
                    continue;
                }
                String[] split = StringUtils.split(line, ":", 1);
                sysvalues.put(StringUtils.trim(split[0]), StringUtils.trim(split[1]));
            }

            // original code: hardware.py get_sysinfo()
            if (StringUtils.isNotBlank(sysvalues.get("Sequence Code")) &&
                    StringUtils.isNotBlank(sysvalues.get("Type")) &&
                    CpuArchUtil.isS390(cpuarch)) {
                // we're on a S390 mainframe and we have a
                // special case: we got info about a virtual host
                // where this system is running on

                String identifier = String.format("Z-%s", sysvalues.get("Sequence Code"));
                String os = "z/OS";
                String name = String.format("IBM Mainframe %s %s", sysvalues.get("Type"),
                        sysvalues.get("Sequence Code"));
                String arch = cpuarch;
                Long totalIfls = null;
                try {
                    totalIfls = Long.parseLong(sysvalues.getOrDefault("CPUs IFL", "0"));
                }
                catch (NumberFormatException e) {
                    LOG.warn("Invalid 'CPUs IFL' value: " + e.getMessage());
                }
                String type = sysvalues.get("Type");

                // register the info about the S390 host in the db

                // original code: server_hardware.py class SystemInformation
                Optional<MinionServer> optionalMinionServer = MinionServerFactory
                        .findByMachineId(identifier);
                MinionServer zhost = optionalMinionServer.orElseGet(() -> {
                    // create a new minion server entry
                    MinionServer minionServer = new MinionServer();
                    String cpurch = grains.getValueAsString("cpuarch");
                    minionServer.setServerArch(ServerFactory
                            .lookupServerArchByLabel(cpurch + "-redhat-linux"));
                    minionServer.setName(name);
                    minionServer.setOs(os);
                    minionServer.setRelease(type);
                    minionServer.setLastBoot(System.currentTimeMillis() / 1000);
                    // see server_hardware.py SystemInformation.__init__()
                    minionServer.setDescription(
                        String.format("Initial Registration Parameters:\n" +
                        "OS: %s\n" +
                        "Release: %s\n" +
                        "CPU Arch: %s", os, sysvalues.get("type"), cpuarch));

                    minionServer.setDigitalServerId(identifier);

                    ServerFactory.save(minionServer);

                    server.setBaseEntitlement(EntitlementManager
                            .getByName(EntitlementManager.FOREIGN_ENTITLED));
                    LOG.debug("New host created: " + identifier);
                    return minionServer;
                });

                // update checkin for new as well as already existing servers
                LOG.debug("Update server info for: " + identifier);
                updateServerInfo(zhost);

                CPU hostcpu = zhost.getCpu();
                if (hostcpu == null || (hostcpu.getNrsocket() != null &&
                        hostcpu.getNrsocket().longValue() != totalIfls)) {
                    LOG.debug("Update host cpu: " + totalIfls);
                    hostcpu = new CPU();
                    hostcpu.setNrCPU(totalIfls);
                    hostcpu.setVersion(null);
                    hostcpu.setMHz("0");
                    hostcpu.setCache(null);
                    hostcpu.setFamily(null);
                    hostcpu.setBogomips(null);
                    hostcpu.setNrsocket(totalIfls);
                    hostcpu.setArch(ServerFactory.lookupCPUArchByName(cpuarch));
                    hostcpu.setFlags(null);
                    hostcpu.setStepping(null);
                    hostcpu.setModel(arch);
                    hostcpu.setVendor(type);
                    zhost.setCpu(hostcpu);
                    hostcpu.setServer(zhost);
                }

                VirtualInstanceFactory vinstFactory = VirtualInstanceFactory.getInstance();
                // TODO lookup only by server.getId()
                VirtualInstance vinst = vinstFactory
                        .lookupByGuestId(server.getOrg(), server.getId());
                if (vinst == null || vinst.getHostSystem() == null) {

                    // first create the host
                    vinst = new VirtualInstance();
                    vinst.setHostSystem(zhost);
                    vinst.setGuestSystem(null);
                    vinst.setUuid(null);
                    vinst.setType(vinstFactory.getFullyVirtType());
                    vinst.setState(vinstFactory.getUnknownState());
                    vinstFactory.saveVirtualInstance(vinst);

                    // create the guest
                    vinst = new VirtualInstance();
                    vinst.setHostSystem(zhost);
                    vinst.setGuestSystem(server);
                    vinst.setUuid(UUID.randomUUID().toString().replace("-", ""));
                    vinst.setType(vinstFactory.getFullyVirtType());
                    vinst.setState(vinstFactory.getUnknownState());
                    vinstFactory.saveVirtualInstance(vinst);
                }
                else if (vinst.getHostSystem().getId().equals(zhost.getId())) {
                    LOG.debug("Updating virtual instance " + vinst.getId() +
                            " with " + zhost.getId());
                    vinst.setHostSystem(zhost);
                    vinstFactory.saveVirtualInstance(vinst);
                }
            }

        }
        catch (com.google.gson.JsonSyntaxException e) {
            LOG.warn("Could not retrieve SYS info from minion '" + minionId +
                    "'. JSON syntax error.");
        }

        return null;
    }

    // TODO extract this to a common utility. Is also needed in VirtualHostManagerProcessor
    private void updateServerInfo(Server server) {
        ServerInfo serverInfo = server.getServerInfo();
        if (serverInfo == null) {
            serverInfo = new ServerInfo();
            serverInfo.setServer(server);
            server.setServerInfo(serverInfo);
            serverInfo.setCheckinCounter(0L);
        }

        serverInfo.setCheckin(new Date());
        serverInfo.setCheckinCounter(serverInfo.getCheckinCounter() + 1);
    }
}
