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
package com.suse.manager.reactor.messaging;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactory;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.frontend.events.AbstractDatabaseAction;
import com.redhat.rhn.manager.errata.ErrataManager;

import com.suse.manager.webui.services.SaltService;
import com.suse.manager.webui.services.impl.SaltAPIService;

import com.suse.salt.netapi.calls.modules.Pkg;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Perform a package profile update when an {@link UpdatePackageProfileEventMessage} is
 * received via the MessageQueue.
 */
public class UpdatePackageProfileEventMessageAction extends AbstractDatabaseAction {

    /* Logger for this class */
    private static final Logger LOG = Logger
            .getLogger(UpdatePackageProfileEventMessageAction.class);

    /* Reference to the SaltService instance */
    private final SaltService SALT_SERVICE;

    /**
     * Default constructor.
     */
    public UpdatePackageProfileEventMessageAction() {
        this(SaltAPIService.INSTANCE);
    }

    /**
     * The package "name" comes as a key of the set of these attributes, hence is implicit.
     */
    public static final List<String> PKGATTR = Collections.unmodifiableList(Arrays.asList(
            "arch", "version", "release", "install_date", "epoch"
    ));

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
    public void doExecute(EventMessage msg) {
        UpdatePackageProfileEventMessage eventMessage =
                (UpdatePackageProfileEventMessage) msg;

        // Query info about installed packages and save the server
        MinionServerFactory.lookupById(eventMessage.getServerId()).ifPresent(server -> {

            SALT_SERVICE.getInstalledPackageDetails(server.getMinionId(), PKGATTR)
                    .map(saltPkgs ->
                saltPkgs.entrySet().stream().map(
                   entry -> createPackageFromSalt(entry.getKey(), entry.getValue(), server)
                ).collect(Collectors.toSet())
            ).ifPresent(newPackages -> {
                Set<InstalledPackage> oldPackages = server.getPackages();
                oldPackages.addAll(newPackages);
                oldPackages.retainAll(newPackages);
            });


            getInstalledProducts(server.getMinionId())
                    .ifPresent(server::setInstalledProducts);

            ServerFactory.save(server);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Package profile updated for minion: " + server.getMinionId());
            }

            // Trigger update of errata cache for this server
            ErrataManager.insertErrataCacheTask(server);
        });
    }

    /**
     * Query the installed products on a minion
     *
     * @param minionId the id of the minion
     * @return a list of installed products
     */
    private Optional<Set<InstalledProduct>> getInstalledProducts(String minionId) {
        return SALT_SERVICE.getInstalledProducts(minionId).map(result ->
            result.stream().flatMap(saltProduct -> {
                String name = saltProduct.getName();
                String version = saltProduct.getVersion();
                String release = saltProduct.getRelease();
                String arch = saltProduct.getArch();
                boolean isbase = saltProduct.getIsbase();

                Optional<SUSEProduct> product = Optional.ofNullable(
                    SUSEProductFactory.findSUSEProduct(
                            name, version, release, arch, true
                    )
                );
                if (!product.isPresent()) {
                    LOG.info(String.format("No product match found for: %s %s %s %s",
                            name, version, release, arch));
                }
                return product.map(prod -> {
                    InstalledProduct prd = new InstalledProduct();
                    prd.setName(prod.getName());
                    prd.setVersion(prod.getVersion());
                    prd.setRelease(prod.getRelease());
                    prd.setArch(prod.getArch());
                    prd.setBaseproduct(isbase);
                    return Stream.of(prd);
                }).orElseGet(Stream::empty);
            }).collect(Collectors.toSet())
        );
    }

    /**
     * Create a {@link InstalledPackage} object from package name and info and return it.
     *
     * @param name name of the package
     * @param info package info from salt
     * @param server server this package will be added to
     * @return the InstalledPackage object
     */
    private InstalledPackage createPackageFromSalt(
            String name, Pkg.Info info, Server server) {
        String epoch = info.getEpoch().orElse(null);
        String release = info.getRelease().orElse("0");
        String version = info.getVersion().get();
        PackageEvr evr = PackageEvrFactory
                .lookupOrCreatePackageEvr(epoch, version, release);

        InstalledPackage pkg = new InstalledPackage();
        pkg.setEvr(evr);
        pkg.setArch(PackageFactory.lookupPackageArchByLabel(info.getArchitecture().get()));
        pkg.setInstallTime(Date.from(info.getInstallDate().get().toInstant()));
        pkg.setName(PackageFactory.lookupOrCreatePackageByName(name));
        pkg.setServer(server);
        return pkg;
    }
}
