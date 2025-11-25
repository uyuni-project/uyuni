/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2009--2010 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.action.rhnpackage;


import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.product.Tuple2;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.ServerAppStream;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.reactor.utils.RhelUtils;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.utils.salt.custom.KernelLiveVersionInfo;
import com.suse.manager.webui.utils.salt.custom.PkgProfileUpdateSlsResult;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.Pkg;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.results.CmdResult;
import com.suse.salt.netapi.results.StateApplyResult;
import com.suse.utils.Json;

import com.google.gson.JsonElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * PackageRefreshListAction
 */
@Entity
@DiscriminatorValue("1")
public class PackageRefreshListAction extends PackageAction {
    private static final Logger LOG = LogManager.getLogger(PackageRefreshListAction.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<LocalCall<?>, List<MinionSummary>> getSaltCalls(List<MinionSummary> minionSummaries) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        ret.put(State.apply(List.of(ApplyStatesEventMessage.PACKAGES_PROFILE_UPDATE),
                Optional.empty()), minionSummaries);
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleUpdateServerAction(ServerAction serverAction, JsonElement jsonResult, UpdateAuxArgs auxArgs) {
        if (serverAction.isStatusFailed()) {
            serverAction.setResultMsg("Failure");
        }
        else {
            serverAction.setResultMsg("Success");
        }
        serverAction.getServer().asMinionServer()
                .ifPresent(minionServer -> handlePackageProfileUpdate(minionServer, Json.GSON.fromJson(jsonResult,
                        PkgProfileUpdateSlsResult.class)));
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

        HibernateFactory.doWithoutAutoFlushing(() -> updatePackages(server, result));

        Optional.ofNullable(result.getListProducts())
                .map(products -> products.getChanges().getRet())
                .map(SaltUtils::getInstalledProducts)
                .ifPresent(server::setInstalledProducts);

        Optional<String> rhelReleaseFile =
                Optional.ofNullable(result.getRhelReleaseFile())
                        .map(StateApplyResult::getChanges)
                        .filter(ret -> ret.getStdout() != null)
                        .map(CmdResult::getStdout);
        Optional<String> centosReleaseFile =
                Optional.ofNullable(result.getCentosReleaseFile())
                        .map(StateApplyResult::getChanges)
                        .filter(ret -> ret.getStdout() != null)
                        .map(CmdResult::getStdout);
        Optional<String> oracleReleaseFile =
                Optional.ofNullable(result.getOracleReleaseFile())
                        .map(StateApplyResult::getChanges)
                        .filter(ret -> ret.getStdout() != null)
                        .map(CmdResult::getStdout);
        Optional<String> alibabaReleaseFile =
                Optional.ofNullable(result.getAlibabaReleaseFile())
                        .map(StateApplyResult::getChanges)
                        .filter(ret -> ret.getStdout() != null)
                        .map(CmdResult::getStdout);
        Optional<String> almaReleaseFile =
                Optional.ofNullable(result.getAlmaReleaseFile())
                        .map(StateApplyResult::getChanges)
                        .filter(ret -> ret.getStdout() != null)
                        .map(CmdResult::getStdout);
        Optional<String> amazonReleaseFile =
                Optional.ofNullable(result.getAmazonReleaseFile())
                        .map(StateApplyResult::getChanges)
                        .filter(ret -> ret.getStdout() != null)
                        .map(CmdResult::getStdout);
        Optional<String> rockyReleaseFile =
                Optional.ofNullable(result.getRockyReleaseFile())
                        .map(StateApplyResult::getChanges)
                        .filter(ret -> ret.getStdout() != null)
                        .map(CmdResult::getStdout);
        Optional<String> resReleasePkg =
                Optional.ofNullable(result.getWhatProvidesResReleasePkg())
                        .map(StateApplyResult::getChanges)
                        .filter(ret -> ret.getStdout() != null)
                        .map(CmdResult::getStdout);
        Optional<String> sllReleasePkg =
                Optional.ofNullable(result.getWhatProvidesSLLReleasePkg())
                        .map(StateApplyResult::getChanges)
                        .filter(ret -> ret.getStdout() != null)
                        .map(CmdResult::getStdout);

        ValueMap grains = new ValueMap(result.getGrains());

        if (rhelReleaseFile.isPresent() || centosReleaseFile.isPresent() ||
                oracleReleaseFile.isPresent() || alibabaReleaseFile.isPresent() ||
                almaReleaseFile.isPresent() || amazonReleaseFile.isPresent() ||
                rockyReleaseFile.isPresent() || resReleasePkg.isPresent()) {
            Set<InstalledProduct> products = getInstalledProductsForRhel(
                    server, resReleasePkg, sllReleasePkg,
                    rhelReleaseFile, centosReleaseFile, oracleReleaseFile, alibabaReleaseFile,
                    almaReleaseFile, amazonReleaseFile, rockyReleaseFile);
            server.setInstalledProducts(products);
        }
        else if ("ubuntu".equalsIgnoreCase(grains.getValueAsString("os"))) {
            String osArch = grains.getValueAsString("osarch") + "-deb";
            String osVersion = grains.getValueAsString("osrelease");
            // Check if we have a product for the specific arch and version
            SUSEProduct ubuntuProduct = SUSEProductFactory.findSUSEProduct("ubuntu-client", osVersion, null, osArch,
                    false);
            if (ubuntuProduct != null) {
                InstalledProduct installedProduct = SUSEProductFactory.findInstalledProduct(ubuntuProduct)
                        .orElse(new InstalledProduct(ubuntuProduct));
                server.setInstalledProducts(Collections.singleton(installedProduct));
            }
        }
        else if ("debian".equalsIgnoreCase(grains.getValueAsString("os"))) {
            String osArch = grains.getValueAsString("osarch") + "-deb";
            String osVersion = grains.getValueAsString("osmajorrelease");
            // Check if we have a product for the specific arch and version
            SUSEProduct debianProduct = SUSEProductFactory.findSUSEProduct("debian-client", osVersion, null, osArch,
                    false);
            if (debianProduct != null) {
                InstalledProduct installedProduct = SUSEProductFactory.findInstalledProduct(debianProduct)
                        .orElse(new InstalledProduct(debianProduct));
                server.setInstalledProducts(Collections.singleton(installedProduct));
            }
        }

        // Update last boot time
        SaltUtils.handleUptimeUpdate(server, result.getUpTime()
                .map(ut -> (Number) ut.getChanges().getRet().get("seconds"))
                .map(n -> n.longValue())
                .orElse(null));

        result.getRebootRequired()
                .map(rr -> rr.getChanges().getRet())
                .filter(Objects::nonNull)
                .map(ret -> (Boolean) ret.get("reboot_required"))
                .ifPresent(flag -> server.setRebootRequiredAfter(flag ? new Date() : null));

        // Update live patching version
        server.setKernelLiveVersion(result.getKernelLiveVersionInfo()
                .map(klv -> klv.getChanges().getRet()).filter(Objects::nonNull)
                .map(KernelLiveVersionInfo::getKernelLiveVersion).orElse(null));

        // Update AppStream modules
        Set<ServerAppStream> enabledAppStreams = result.getEnabledAppstreamModules()
                .map(m -> m.getChanges().getRet())
                .orElse(Collections.emptySet())
                .stream()
                .map(nsvca -> new ServerAppStream(server, nsvca))
                .collect(Collectors.toSet());

        server.getAppStreams().clear();
        server.getAppStreams().addAll(enabledAppStreams);

        // Update grains
        if (!result.getGrains().isEmpty()) {
            server.setOsFamily(grains.getValueAsString("os_family"));
            server.setRunningKernel(grains.getValueAsString("kernelrelease"));
            server.setOs(grains.getValueAsString("osfullname"));
            server.setCpe(grains.getValueAsString("cpe"));

            /** Release is set directly from grain information for SUSE systems only.
             RH systems require some parsing on the grains to get the correct release
             See RegisterMinionEventMessageAction#getOsRelease

             However, release can change only after product migration and SUMA supports this only on SUSE systems.
             Also, the getOsRelease method requires remote command execution and was therefore avoided for now.
             If we decide to support RedHat distro/SP upgrades in the future, this code has to be reviewed.
             */
            if (server.getOsFamily().equals(ServerConstants.OS_FAMILY_SUSE)) {
                server.setRelease(grains.getValueAsString("osrelease"));
            }
        }

        ServerFactory.save(server);
        if (LOG.isDebugEnabled()) {
            long duration = Duration.between(start, Instant.now()).getSeconds();
            LOG.debug("Package profile updated for minion: {} ({} seconds)", server.getMinionId(), duration);
        }

        // Trigger update of errata cache for this server
        ErrataManager.insertErrataCacheTask(server);
    }

    /**
     * Updates a minion's packages with the result coming from Salt
     *
     * @param server a Server object corresponding to a minion
     * @param result the result from the package profile update state
     */
    private void updatePackages(MinionServer server,
                                PkgProfileUpdateSlsResult result) {
        Set<InstalledPackage> packages = server.getPackages();

        Map<String, InstalledPackage> oldPackageMap = packages.stream()
                .collect(Collectors.toMap(
                        SaltUtils::packageToKey,
                        Function.identity()
                ));

        Map<String, Map.Entry<String, Pkg.Info>> newPackageMap =
                result.getInfoInstalled().getChanges().getRet()
                        .entrySet().stream()
                        .flatMap(entry ->
                                entry.getValue().fold(Stream::of, Collection::stream)
                                        .flatMap(x -> {
                                            Map<String, Pkg.Info> infoTuple = new HashMap<>();
                                            infoTuple.put(entry.getKey(), x);
                                            return infoTuple.entrySet().stream();
                                        })
                        )
                        .collect(Collectors.toMap(
                                SaltUtils::packageToKey,
                                Function.identity(),
                                PackageRefreshListAction::resolveDuplicatePackage
                        ));

        Collection<InstalledPackage> unchanged = oldPackageMap.entrySet().stream().filter(
                e -> newPackageMap.containsKey(e.getKey())
        ).map(Map.Entry::getValue).collect(Collectors.toList());
        packages.retainAll(unchanged);

        Map<String, Tuple2<String, Pkg.Info>> packagesToAdd = newPackageMap.entrySet().stream().filter(
                e -> !oldPackageMap.containsKey(e.getKey())
        ).collect(Collectors.toMap(Map.Entry::getKey,
                e -> new Tuple2<>(e.getValue().getKey(), e.getValue().getValue())));

        packages.addAll(SaltUtils.createPackagesFromSalt(packagesToAdd, server));
        SystemManager.updateSystemOverview(server.getId());
    }

    private Set<InstalledProduct> getInstalledProductsForRhel(
            MinionServer server,
            Optional<String> resPackage,
            Optional<String> sllPackage,
            Optional<String> rhelReleaseFile,
            Optional<String> centosRelaseFile,
            Optional<String> oracleReleaseFile,
            Optional<String> alibabaReleaseFile,
            Optional<String> almaReleaseFile,
            Optional<String> amazonReleaseFile,
            Optional<String> rockyReleaseFile) {

        Optional<RhelUtils.RhelProduct> rhelProductInfo =
                RhelUtils.detectRhelProduct(server, resPackage, sllPackage,
                        rhelReleaseFile, centosRelaseFile, oracleReleaseFile,
                        alibabaReleaseFile, almaReleaseFile, amazonReleaseFile,
                        rockyReleaseFile);

        if (!rhelProductInfo.isPresent()) {
            LOG.warn("Could not determine RHEL product type for minion: {}", server.getMinionId());
            return Collections.emptySet();
        }

        LOG.debug("Detected minion {} as a RedHat compatible system: {} {} {} {}",
                server.getMinionId(),
                rhelProductInfo.get().getName(), rhelProductInfo.get().getVersion(),
                rhelProductInfo.get().getRelease(), server.getServerArch().getName());

        return rhelProductInfo.get().getAllSuseProducts().stream().map(product -> {
            String arch = server.getServerArch().getLabel().replace("-redhat-linux", "");

            InstalledProduct installedProduct = new InstalledProduct();
            installedProduct.setName(product.getName());
            installedProduct.setVersion(product.getVersion());
            installedProduct.setRelease(product.getRelease());
            installedProduct.setArch(PackageFactory.lookupPackageArchByLabel(arch));
            installedProduct.setBaseproduct(product.isBase());

            return installedProduct;
        }).collect(Collectors.toSet());
    }

    private static Map.Entry<String, Pkg.Info> resolveDuplicatePackage(Map.Entry<String, Pkg.Info> firstEntry,
                                                                Map.Entry<String, Pkg.Info> secondEntry) {
        Pkg.Info first = firstEntry.getValue();
        Pkg.Info second = secondEntry.getValue();

        if (first.getInstallDateUnixTime().isEmpty() && second.getInstallDateUnixTime().isEmpty()) {
            LOG.warn("Got duplicate packages NEVRA and the install timestamp is missing." +
                    " Taking the first one. First:  {}, second: {}", first, second);
            return firstEntry;
        }

        if (first.getInstallDateUnixTime().orElse(0L) > second.getInstallDateUnixTime().orElse(0L)) {
            return firstEntry;
        }
        else {
            return secondEntry;
        }
    }
}
