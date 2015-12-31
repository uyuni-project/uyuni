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
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactory;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.MinionFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.frontend.events.AbstractDatabaseAction;

import com.suse.manager.webui.services.SaltService;
import com.suse.manager.webui.services.impl.SaltAPIService;
import com.suse.saltstack.netapi.calls.modules.Pkg;

import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * TODO: Write a comment.
 */
public class UpdatePackageProfileEventMessageAction extends AbstractDatabaseAction {

    /* Reference to the SaltService instance */
    private final SaltService SALT_SERVICE;

    /**
     * Default constructor.
     */
    public UpdatePackageProfileEventMessageAction() {
        this(SaltAPIService.INSTANCE);
    }

    /**
     * Constructor expecting a {@link SaltService} instance.
     * 
     * @param saltService the salt service instance to use
     */
    public UpdatePackageProfileEventMessageAction(SaltService saltService) {
        SALT_SERVICE = saltService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doExecute(EventMessage msg) {
        UpdatePackageProfileEventMessage eventMessage =
                (UpdatePackageProfileEventMessage) msg;

        // Query info about installed packages and save the server
        MinionFactory.lookupById(eventMessage.getServerId()).ifPresent(server -> {
            Map<String, Pkg.Info> saltPackages =
                    SALT_SERVICE.getInstalledPackageDetails(server.getMinionId());
            Set<InstalledPackage> packages = saltPackages.entrySet().stream().map(
                    entry -> createPackageFromSalt(entry.getKey(), entry.getValue(), server)
            ).collect(Collectors.toSet());

            server.setPackages(packages);
            ServerFactory.save(server);
        });
    }

    /**
     * Create a {@link InstalledPackage} object from package name and info and return it.
     *
     * @param name name of the package
     * @param info package info from salt
     * @param server server this package will be added to
     * @return the InstalledPackage object
     */
    private InstalledPackage createPackageFromSalt(String name, Pkg.Info info,
            Server server) {
        String epoch = info.getEpoch().orElse(null);
        String release = info.getRelease().orElse("0");
        String version = info.getVersion();
        PackageEvr evr = PackageEvrFactory
                .lookupOrCreatePackageEvr(epoch, version, release);

        InstalledPackage pkg = new InstalledPackage();
        pkg.setEvr(evr);
        pkg.setArch(PackageFactory.lookupPackageArchByLabel(info.getArchitecture()));
        pkg.setInstallTime(Date.from(info.getInstallDate().toInstant()));
        pkg.setName(PackageFactory.lookupOrCreatePackageByName(name));
        pkg.setServer(server);
        return pkg;
    }
}
