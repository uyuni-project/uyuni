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
package com.suse.manager.reactor.messaging.tasks;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.common.LoggingFactory;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactory;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.manager.errata.ErrataManager;

import com.suse.manager.webui.utils.salt.Zypper.ProductInfo;
import com.suse.manager.webui.utils.salt.custom.PkgProfileUpdateSlsResult;
import com.suse.manager.webui.utils.salt.events.JobReturnEvent.Data;
import com.suse.salt.netapi.calls.modules.Pkg;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Parallelizable task performing a package list refresh on the database.
 */
public class PackageListRefreshTask implements Runnable {

    private static final Logger LOG = Logger.getLogger(PackageListRefreshTask.class);
    private static final String ROLLBACK_MSG = "Error during transaction. Rolling back.";

    private final Optional<Long> userId;
    private final long serverId;
    private final Data eventData;

    /**
     * Constructor for creating package list refresh tasks.
     *
     * @param userIdIn the user id
     * @param serverIdIn the server id
     * @param resultIn the parsed result
     */
    public PackageListRefreshTask(Optional<Long> userIdIn, long serverIdIn, Data eventDataIn) {
        userId = userIdIn;
        serverId = serverIdIn;
        eventData = eventDataIn;
    }

    @Override
    public void run() {
        boolean commit = true;
        try {
            // initialize logging
            LoggingFactory.clearLogId();
            userId.ifPresent(id -> {
                LoggingFactory.setLogAuth(id);
            });

            MinionServerFactory.lookupById(serverId).ifPresent(minionServer -> {
                handlePackageProfileUpdate(minionServer,
                        eventData.getResult(PkgProfileUpdateSlsResult.class));
            });
        }
        catch (Exception e) {
            commit = false;
            LOG.error("Error executing action " + getClass().getName(), e);
        }
        finally {
            handleTransactions(commit);
        }
    }

    /**
     * Perform the actual update of the database based on given event data.
     *
     * @param server the minion server
     * @param result the result of the call as parsed from event data
     */
    private void handlePackageProfileUpdate(MinionServer server,
            PkgProfileUpdateSlsResult result) {
        Instant start = Instant.now();

        Optional.of(result.getInfoInstalled().getChanges().getRet())
            .map(saltPkgs -> saltPkgs.entrySet().stream().map(
                entry -> createPackageFromSalt(entry.getKey(), entry.getValue(), server)
            ).collect(Collectors.toSet())
        ).ifPresent(newPackages -> {
            Set<InstalledPackage> oldPackages = server.getPackages();
            oldPackages.addAll(newPackages);
            oldPackages.retainAll(newPackages);
        });

        Optional<List<ProductInfo>> productInfo = Optional.of(
                result.getListProducts().getChanges().getRet());
        productInfo
                .map(this::getInstalledProducts)
                .ifPresent(server::setInstalledProducts);

        ServerFactory.save(server);
        if (LOG.isDebugEnabled()) {
            long duration = Duration.between(start, Instant.now()).getSeconds();
            LOG.debug("Package profile updated for minion: " + server.getMinionId() +
                    " (" + duration + " seconds)");
        }

        // Trigger update of errata cache for this server
        ErrataManager.insertErrataCacheTask(server);
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

    /**
     * Convert a list of {@link ProductInfo} objects into a set of {@link InstalledProduct}
     * objects.
     *
     * @param productsIn list of products as received from Salt
     * @return set of installed products
     */
    private Set<InstalledProduct> getInstalledProducts(
            List<ProductInfo> productsIn) {
        return productsIn.stream().flatMap(saltProduct -> {
            String name = saltProduct.getName();
            String version = saltProduct.getVersion();
            String release = saltProduct.getRelease();
            String arch = saltProduct.getArch();
            boolean isbase = saltProduct.getIsbase();

            // Find the corresponding SUSEProduct in the database
            Optional<SUSEProduct> suseProduct = Optional.ofNullable(SUSEProductFactory
                    .findSUSEProduct(name, version, release, arch, true));
            if (!suseProduct.isPresent()) {
                LOG.warn(String.format("No product match found for: %s %s %s %s",
                        name, version, release, arch));
            }

            return suseProduct.map(product -> {
                InstalledProduct installedProduct = new InstalledProduct();
                installedProduct.setName(product.getName());
                installedProduct.setVersion(product.getVersion());
                installedProduct.setRelease(product.getRelease());
                installedProduct.setArch(product.getArch());
                installedProduct.setBaseproduct(isbase);
                return Stream.of(installedProduct);
            }).orElseGet(Stream::empty);
        }).collect(Collectors.toSet());
    }

    /**
     * Commits the current thread transaction, as well as close the Hibernate session.
     * <p/>
     * Note that this call <em>MUST</em> take place for any database operations done in
     * a message queue action for the transaction to be committed.
     */
    protected void handleTransactions(boolean commit) {
        boolean committed = false;

        try {
            if (commit) {
                HibernateFactory.commitTransaction();
                committed = true;

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Transaction committed");
                }
            }
        }
        catch (HibernateException e) {
            LOG.error(ROLLBACK_MSG, e);
        }
        finally {
            try {
                if (!committed) {
                    try {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Rolling back transaction");
                        }
                        HibernateFactory.rollbackTransaction();
                    }
                    catch (HibernateException e) {
                        final String msg = "Additional error during rollback";
                        LOG.warn(msg, e);
                    }
                }
            }
            finally {
                // cleanup the session
                HibernateFactory.closeSession();
            }
        }
    }
}
