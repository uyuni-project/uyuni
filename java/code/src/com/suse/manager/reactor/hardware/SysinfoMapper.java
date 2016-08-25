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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.VirtualInstanceState;
import com.redhat.rhn.domain.server.VirtualInstanceType;
import com.suse.manager.webui.services.SaltGrains;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.redhat.rhn.domain.server.CPU;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.suse.manager.reactor.utils.ValueMap;

/**
 * Check virtual host information in case of a S390 minion.
 */
public class SysinfoMapper extends AbstractHardwareMapper<VirtualInstance> {

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(SysinfoMapper.class);

    /**
     * The constructor.
     * @param saltServiceInvoker a {@link SaltServiceInvoker} instance
     */
    public SysinfoMapper(SaltServiceInvoker saltServiceInvoker) {
        super(saltServiceInvoker);
    }

    @Override
    public void doMap(MinionServer server, ValueMap grains) {
        String cpuarch = grains.getValueAsString(SaltGrains.CPUARCH.getValue())
                .toLowerCase();
        String minionId = server.getMinionId();
        try {
            // call custom minion to get read_values info (if available)
            // original code: hardware.py get_sysinfo()
            String readValuesOutput = saltInvoker.getMainframeSysinfoReadValues(minionId)
                    .orElse("");
            Map<String, String> sysvalues = new HashMap<>();
            for (String line : readValuesOutput.split("\\r?\\n")) {
                if (!line.contains(":")) {
                    continue;
                }
                String[] split = StringUtils.split(line, ":", 2);
                if (split.length == 2) {
                    sysvalues.put(StringUtils.trim(split[0]), StringUtils.trim(split[1]));
                }
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
                Server zhost = ServerFactory
                        .lookupForeignSystemByDigitalServerId(identifier);

                if (zhost == null) {
                    // create a new z/OS host server entry
                    zhost = ServerFactory.createServer();
                    String cpurch = grains.getValueAsString(SaltGrains.CPUARCH.getValue());
                    // TODO extract this cpuarch + "-redhat-linux" in some common util
                    zhost.setServerArch(ServerFactory
                            .lookupServerArchByLabel(cpurch + "-redhat-linux"));
                    zhost.setName(name);
                    zhost.setOs(os);
                    zhost.setRelease(type);
                    zhost.setLastBoot(System.currentTimeMillis() / 1000);
                    // see server_hardware.py SystemInformation.__init__()
                    zhost.setDescription(
                            String.format("Initial Registration Parameters:\n" +
                                    "OS: %s\n" +
                                    "Release: %s\n" +
                                    "CPU Arch: %s", os, sysvalues.get("Type"), cpuarch));

                    zhost.setDigitalServerId(identifier);
                    zhost.setOrg(OrgFactory.getSatelliteOrg()); // TODO clarify this
                    zhost.setSecret(RandomStringUtils.randomAlphanumeric(64));
                    zhost.setAutoUpdate("N");
                    zhost.setContactMethod(ServerFactory
                            .findContactMethodByLabel("default"));
                    server.setLastBoot(System.currentTimeMillis() / 1000);

                    ServerFactory.save(zhost);

                    zhost.setBaseEntitlement(EntitlementManager
                            .getByName(EntitlementManager.FOREIGN_ENTITLED));
                    LOG.debug("New host created: " + identifier);
                }

                // update checkin for new as well as already existing servers
                LOG.debug("Update server info for: " + identifier);
                zhost.updateServerInfo();

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
                    zhost.setCpu(hostcpu); // TODO test if this deletes any existing CPU
                    hostcpu.setServer(zhost);
                }

                VirtualInstanceFactory vinstFactory = VirtualInstanceFactory.getInstance();
                VirtualInstance vinst = vinstFactory
                        .lookupByGuestId(server.getId());
                if (vinst == null || vinst.getHostSystem() == null) {

                    VirtualInstanceType fullVirtType = vinstFactory.getFullyVirtType();
                    VirtualInstanceState unknownState = vinstFactory.getUnknownState();

                    // first create the host
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
                    vinstGuest.setUuid(UUID.randomUUID().toString().replace("-", ""));
                    vinstGuest.setType(fullVirtType);
                    vinstGuest.setState(unknownState);
                    vinstFactory.saveVirtualInstance(vinstGuest);
                }
                else if (!vinst.getHostSystem().getId().equals(zhost.getId())) {
                    LOG.debug("Updating virtual instance " + vinst.getId() +
                            " with " + zhost.getId());
                    vinst.setHostSystem(zhost);
                    vinstFactory.saveVirtualInstance(vinst);
                }
            }

        }
        catch (com.google.gson.JsonSyntaxException e) {
            setError("Could not retrieve SYS info");
            LOG.warn("Could not retrieve SYS info from minion '" + minionId +
                    "': " + e.getMessage());
        }
    }

}
