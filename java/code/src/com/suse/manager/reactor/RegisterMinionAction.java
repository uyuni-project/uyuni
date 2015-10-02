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
package com.suse.manager.reactor;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactory;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerInfo;
import com.redhat.rhn.frontend.events.AbstractDatabaseAction;
import com.redhat.rhn.manager.entitlement.EntitlementManager;

import com.suse.manager.webui.services.SaltService;
import com.suse.manager.webui.services.impl.SaltAPIService;

import com.suse.saltstack.netapi.calls.modules.Pkg;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.log4j.Logger;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Event handler to create system records for salt minions.
 */
public class RegisterMinionAction extends AbstractDatabaseAction {

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(RegisterMinionAction.class);

    // Reference to the SaltService instance
    private final SaltService SALT_SERVICE;

    /**
     * Default constructor.
     */
    public RegisterMinionAction() {
        SALT_SERVICE = SaltAPIService.INSTANCE;
    }

    /**
     * Constructor taking a {@link SaltService} instance.
     *
     * @param saltService the salt service to use
     */
    protected RegisterMinionAction(SaltService saltService) {
        SALT_SERVICE = saltService;
    }

    /**
     * {@inheritDoc}
     */
    public void doExecute(EventMessage msg) {
        RegisterMinionEvent event = (RegisterMinionEvent) msg;
        String minionId = event.getMinionId();

        // Match minions via their machine id
        String machineId = SALT_SERVICE.getMachineId(minionId);
        if (machineId == null) {
            LOG.info("Cannot find machine id for minion: " + minionId);
            return;
        }
        if (ServerFactory.findRegisteredMinion(machineId) != null) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Minion already registered, skipping registration: " +
                        minionId + " [" + machineId + "]");
            }
            return;
        }
        try {
            // Create the server
            Server server = ServerFactory.createServer();
            server.setName(minionId);
            server.setDigitalServerId(machineId);

            // All registered minions initially belong to the default organization
            server.setOrg(OrgFactory.getSatelliteOrg());

            // TODO: Set complete OS, hardware and network information here
            Map<String, Object> grains = SALT_SERVICE.getGrains(minionId);
            server.setOs((String) grains.get("osfullname"));
            server.setRelease((String) grains.get("osrelease"));
            server.setRunningKernel((String) grains.get("kernelrelease"));
            server.setSecret(RandomStringUtils.randomAlphanumeric(64));
            server.setAutoUpdate("N");
            server.setLastBoot(System.currentTimeMillis() / 1000);
            server.setCreated(new Date());
            server.setModified(server.getCreated());
            server.setContactMethod(ServerFactory.findContactMethodByLabel("default"));
            server.setServerArch(
                    ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
            ServerInfo serverInfo = new ServerInfo();
            serverInfo.setServer(server);
            server.setServerInfo(serverInfo);
            server.setRam(((Double) grains.get("mem_total")).longValue());

            Map<String, Pkg.Info> saltPackages =
                    SALT_SERVICE.getInstalledPackageDetails(minionId);
            Set<InstalledPackage> packages = saltPackages.entrySet().stream().map(
                    entry -> createPackageFromSalt(entry.getKey(), entry.getValue(), server)
            ).collect(Collectors.toSet());

            server.setPackages(packages);
            ServerFactory.save(server);

            // Assign the SaltStack base entitlement by default
            server.setBaseEntitlement(
                    EntitlementManager.getByName(EntitlementManager.SALTSTACK_ENTITLED));
            LOG.info("Finished minion registration: " + minionId);
        }
        catch (Throwable t) {
            LOG.error("Error registering minion for event: " + event, t);
        }
    }

    /**
     * Creates a new InstalledPackage object from package name and info
     * @param name name of the package
     * @param info package info from salt
     * @param server server this package will be added to
     * @return The InstalledPackage object
     */
    private InstalledPackage createPackageFromSalt(String name, Pkg.Info info,
                                                   Server server) {

        String epoch = info.getEpoch().orElse(null);
        String release = info.getRelease().orElse(null);
        String version = info.getVersion();
        PackageEvr evr = PackageEvrFactory
                .lookupOrCreatePackageEvr(epoch, version, release);

        PackageName pkgName = PackageFactory.lookupOrCreatePackageByName(name);

        String arch = info.getArchitecture();
        PackageArch packageArch = PackageFactory.lookupPackageArchByLabel(arch);

        InstalledPackage pkg = new InstalledPackage();
        pkg.setEvr(evr);
        pkg.setArch(packageArch);

        pkg.setInstallTime(Date.from(info.getInstallDate().toInstant()));
        pkg.setName(pkgName);
        pkg.setServer(server);
        return pkg;
    }
}
