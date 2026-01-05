/*
 * Copyright (c) 2016--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.action.salt.inspect;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionFormatter;
import com.redhat.rhn.domain.action.ActionStatus;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageInfoFactory;
import com.redhat.rhn.domain.image.ImagePackage;
import com.redhat.rhn.domain.image.ImageRepoDigest;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.PackageType;
import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.manager.errata.ErrataManager;

import com.suse.manager.reactor.utils.RhelUtils;
import com.suse.manager.saltboot.SaltbootUtils;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.manager.webui.utils.salt.custom.ImageInspectSlsResult;
import com.suse.manager.webui.utils.salt.custom.ImagesProfileUpdateSlsResult;
import com.suse.manager.webui.utils.salt.custom.OSImageInspectSlsResult;
import com.suse.manager.webui.utils.salt.custom.PkgProfileUpdateSlsResult;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.Pkg;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.results.CmdResult;
import com.suse.salt.netapi.results.StateApplyResult;
import com.suse.utils.Json;
import com.suse.utils.Opt;

import com.google.gson.JsonElement;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;

/**
 * ImageInspectAction
 */
@Entity
@DiscriminatorValue("505")
public class ImageInspectAction extends Action {
    private static final Logger LOG = LogManager.getLogger(ImageInspectAction.class);

    @OneToOne(mappedBy = "parentAction", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private ImageInspectActionDetails details;

    /**
     * Return the details.
     * @return details
     */
    public ImageInspectActionDetails getDetails() {
        return details;
    }

    /**
     * Set the details.
     * @param detailsIn details
     */
    public void setDetails(ImageInspectActionDetails detailsIn) {
        if (detailsIn != null) {
            detailsIn.setParentAction(this);
        }
        this.details = detailsIn;
    }

    @Override
    public ActionFormatter getFormatter() {
        if (formatter == null) {
            formatter = new ImageInspectActionFormatter(this);
        }
        return formatter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<LocalCall<?>, List<MinionSummary>> getSaltCalls(List<MinionSummary> minionSummaries) {
        if (details == null) {
            return Collections.emptyMap();
        }
        return ImageStoreFactory.lookupById(details.getImageStoreId())
                .map(store -> imageInspectAction(minionSummaries, store))
                .orElseGet(Collections::emptyMap);
    }

    private Map<LocalCall<?>, List<MinionSummary>> imageInspectAction(List<MinionSummary> minions,
                                                                      ImageStore store) {
        Map<String, Object> pillar = new HashMap<>();
        Map<LocalCall<?>, List<MinionSummary>> result = new HashMap<>();
        if (ImageStoreFactory.TYPE_OS_IMAGE.equals(store.getStoreType())) {
            pillar.put("build_id", "build" + details.getBuildActionId());
            LocalCall<Map<String, State.ApplyResult>> apply = State.apply(
                    Collections.singletonList("images.kiwi-image-inspect"),
                    Optional.of(pillar));
            result.put(apply, minions);
            return result;
        }
        else {
            List<ImageStore> imageStores = new LinkedList<>();
            imageStores.add(store);
            Map<String, Object> dockerRegistries = ImageStore.dockerRegPillar(imageStores);
            pillar.put("docker-registries", dockerRegistries);
            pillar.put("imagename", store.getUri() + "/" + details.getName() + ":" + details.getVersion());
            pillar.put("build_id", "build" + details.getBuildActionId());
            LocalCall<Map<String, State.ApplyResult>> apply = State.apply(
                    Collections.singletonList("images.profileupdate"),
                    Optional.of(pillar));
            result.put(apply, minions);
            return result;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleUpdateServerAction(ServerAction serverAction, JsonElement jsonResult, UpdateAuxArgs auxArgs) {
        if (details == null) {
            LOG.warn("Details not found while performing: {} in handleImageInspectData", getName());
            return;
        }
        Long imageStoreId = details.getImageStoreId();
        if (imageStoreId == null) { // It happens when the store is deleted during an inspect action
            LOG.warn("Image Store ID not found while performing: {} in handleImageInspectData", getName());
            return;
        }
        ImageInfoFactory
                .lookupByInspectAction(this)
                .ifPresent(imageInfo -> serverAction.getServer().asMinionServer()
                        .ifPresent(minionServer ->
                                handleImagePackageProfileUpdate(imageInfo, Json.GSON.fromJson(jsonResult,
                                                ImagesProfileUpdateSlsResult.class),
                                        serverAction)));
    }

    private void handleImagePackageProfileUpdate(ImageInfo imageInfo,
                                                 ImagesProfileUpdateSlsResult result, ServerAction serverAction) {
        ActionStatus as = ActionFactory.STATUS_COMPLETED;
        serverAction.setResultMsg("Success");
        if (Optional.ofNullable(imageInfo.getProfile()).isEmpty() ||
                imageInfo.getProfile().asDockerfileProfile().isPresent()) {
            if (result.getDockerInspect().isResult()) {
                ImageInspectSlsResult iret = result.getDockerInspect().getChanges().getRet();
                imageInfo.setChecksum(ImageInfoFactory.convertChecksum(iret.getId()));
                imageInfo.getRepoDigests().addAll(
                        iret.getRepoDigests().stream().map(digest -> {
                            ImageRepoDigest repoDigest = new ImageRepoDigest();
                            repoDigest.setRepoDigest(digest);
                            repoDigest.setImageInfo(imageInfo);
                            return repoDigest;
                        }).collect(Collectors.toSet()));
            }
            else {
                serverAction.setResultMsg(result.getDockerInspect().getComment());
                as = ActionFactory.STATUS_FAILED;
            }

            if (result.getDockerSlsBuild().isResult()) {
                PkgProfileUpdateSlsResult ret =
                        result.getDockerSlsBuild().getChanges().getRet();

                //getRet returns Map<String, Xor<Pkg.Info, List<Pkg.Info>>>
                Optional.of(ret.getInfoInstalled().getChanges().getRet())
                        .stream()
                        .flatMap(saltPkgs -> saltPkgs.entrySet().stream())
                        .forEach(entry -> entry.getValue().left()
                                .ifPresent(info -> createImagePackageFromSalt(entry.getKey(), info, imageInfo)));

                Optional.of(ret.getInfoInstalled().getChanges().getRet())
                        .stream()
                        .flatMap(saltPkgs -> saltPkgs.entrySet().stream())
                        .forEach(entry -> Opt.stream(entry.getValue().right())
                                .flatMap(Collection::stream)
                                .forEach(info -> createImagePackageFromSalt(entry.getKey(), info, imageInfo)));


                Optional.ofNullable(ret.getListProducts())
                        .map(products -> products.getChanges().getRet())
                        .map(SaltUtils::getInstalledProducts)
                        .ifPresent(imageInfo::setInstalledProducts);

                Optional<String> rhelReleaseFile =
                        Optional.ofNullable(ret.getRhelReleaseFile())
                                .map(StateApplyResult::getChanges)
                                .filter(res -> res.getStdout() != null)
                                .map(CmdResult::getStdout);
                Optional<String> centosReleaseFile =
                        Optional.ofNullable(ret.getCentosReleaseFile())
                                .map(StateApplyResult::getChanges)
                                .filter(res -> res.getStdout() != null)
                                .map(CmdResult::getStdout);
                Optional<String> alibabaReleaseFile =
                        Optional.ofNullable(ret.getAlibabaReleaseFile())
                                .map(StateApplyResult::getChanges)
                                .filter(res -> res.getStdout() != null)
                                .map(CmdResult::getStdout);
                Optional<String> oracleReleaseFile =
                        Optional.ofNullable(ret.getOracleReleaseFile())
                                .map(StateApplyResult::getChanges)
                                .filter(res -> res.getStdout() != null)
                                .map(CmdResult::getStdout);
                Optional<String> almaReleaseFile =
                        Optional.ofNullable(ret.getAlmaReleaseFile())
                                .map(StateApplyResult::getChanges)
                                .filter(res -> res.getStdout() != null)
                                .map(CmdResult::getStdout);
                Optional<String> amazonReleaseFile =
                        Optional.ofNullable(ret.getAmazonReleaseFile())
                                .map(StateApplyResult::getChanges)
                                .filter(res -> res.getStdout() != null)
                                .map(CmdResult::getStdout);
                Optional<String> rockyReleaseFile =
                        Optional.ofNullable(ret.getRockyReleaseFile())
                                .map(StateApplyResult::getChanges)
                                .filter(res -> res.getStdout() != null)
                                .map(CmdResult::getStdout);
                Optional<String> resReleasePkg =
                        Optional.ofNullable(ret.getWhatProvidesResReleasePkg())
                                .map(StateApplyResult::getChanges)
                                .filter(res -> res.getStdout() != null)
                                .map(CmdResult::getStdout);
                Optional<String> sllReleasePkg =
                        Optional.ofNullable(ret.getWhatProvidesSLLReleasePkg())
                                .map(StateApplyResult::getChanges)
                                .filter(res -> res.getStdout() != null)
                                .map(CmdResult::getStdout);
                if (rhelReleaseFile.isPresent() || centosReleaseFile.isPresent() ||
                        oracleReleaseFile.isPresent() || alibabaReleaseFile.isPresent() ||
                        almaReleaseFile.isPresent() || amazonReleaseFile.isPresent() ||
                        rockyReleaseFile.isPresent() || resReleasePkg.isPresent()) {
                    Set<InstalledProduct> products = getInstalledProductsForRhel(
                            imageInfo, resReleasePkg, sllReleasePkg,
                            rhelReleaseFile, centosReleaseFile, oracleReleaseFile, alibabaReleaseFile,
                            almaReleaseFile, amazonReleaseFile, rockyReleaseFile);
                    imageInfo.setInstalledProducts(products);
                }
            }
            else {
                // do not fail the action when no packages are returned
                serverAction.setResultMsg(result.getDockerSlsBuild().getComment());
            }
        }
        else {
            if (result.getKiwiInspect().isResult()) {
                Long instantNow = new Date().getTime() / 1000L;
                OSImageInspectSlsResult ret = result.getKiwiInspect().getChanges().getRet();
                List<OSImageInspectSlsResult.Package> packages = ret.getPackages();
                packages.forEach(pkg -> createImagePackageFromSalt(pkg.getName(), Optional.of(pkg.getEpoch()),
                        Optional.of(pkg.getRelease()), pkg.getVersion(), Optional.of(instantNow),
                        Optional.of(pkg.getArch()), imageInfo));
                if ("pxe".equals(ret.getImage().getType())) {
                    SaltStateGeneratorService.INSTANCE.generateOSImagePillar(ret.getImage(),
                            ret.getBootImage(), imageInfo);
                    if (ret.getBootImage().isPresent() && ret.getBundles().isEmpty()) {
                        SaltbootUtils.createSaltbootDistro(imageInfo);
                    }
                }
            }
            else {
                serverAction.setResultMsg(result.getKiwiInspect().getComment());
                as = ActionFactory.STATUS_FAILED;
            }
        }

        serverAction.setStatus(as);
        if (as.equals(ActionFactory.STATUS_COMPLETED)) {
            imageInfo.setBuilt(true);
        }
        ImageInfoFactory.save(imageInfo);
        ErrataManager.insertErrataCacheTask(imageInfo);
    }

    private ImagePackage createImagePackageFromSalt(String name, Pkg.Info info, ImageInfo imageInfo) {
        return createImagePackageFromSalt(name, info.getEpoch(), info.getRelease(), info.getVersion().orElse("0"),
                info.getInstallDateUnixTime(), info.getArchitecture(), imageInfo);
    }

    private ImagePackage createImagePackageFromSalt(String name, Optional<String> epoch,
                                                           Optional<String> release, String version,
                                                           Optional<Long> installDateUnixTime,
                                                           Optional<String> architecture,
                                                           ImageInfo imageInfo) {

        PackageType packageType = imageInfo.getPackageType();
        Optional<String> pkgArch = architecture.map(arch -> packageType == PackageType.DEB ? arch + "-deb" : arch);
        PackageEvr evr = SaltUtils.parsePackageEvr(epoch, version, release, packageType);
        ImagePackage pkg = new ImagePackage();
        pkg.setEvr(evr);
        pkgArch.ifPresent(arch -> pkg.setArch(PackageFactory.lookupPackageArchByLabel(arch)));
        installDateUnixTime.ifPresent(udut -> pkg.setInstallTime(new Date(udut * 1000)));
        pkg.setName(PackageFactory.lookupOrCreatePackageByName(name));
        pkg.setImageInfo(imageInfo);
        ImageInfoFactory.save(pkg);
        return pkg;
    }

    private Set<InstalledProduct> getInstalledProductsForRhel(
            ImageInfo image,
            Optional<String> resPackage,
            Optional<String> sllPackage,
            Optional<String> rhelReleaseFile,
            Optional<String> centosReleaseFile,
            Optional<String> oracleReleaseFile,
            Optional<String> alibabaReleaseFile,
            Optional<String> almaReleaseFile,
            Optional<String> amazonReleaseFile,
            Optional<String> rockyReleaseFile) {

        Optional<RhelUtils.RhelProduct> rhelProductInfo =
                RhelUtils.detectRhelProduct(image, resPackage, sllPackage,
                        rhelReleaseFile, centosReleaseFile, oracleReleaseFile,
                        alibabaReleaseFile, almaReleaseFile, amazonReleaseFile,
                        rockyReleaseFile);

        if (!rhelProductInfo.isPresent()) {
            LOG.warn("Could not determine RHEL product type for image: {} {}", image.getName(), image.getVersion());
            return Collections.emptySet();
        }

        LOG.debug("Detected image {}:{} as a RedHat compatible system: {} {} {} {}",
                image.getName(), image.getVersion(),
                rhelProductInfo.get().getName(), rhelProductInfo.get().getVersion(),
                rhelProductInfo.get().getRelease(), image.getImageArch().getName());

        return rhelProductInfo.get().getAllSuseProducts().stream().map(product -> {
            String arch = image.getImageArch().getLabel().replace("-redhat-linux", "");

            InstalledProduct installedProduct = new InstalledProduct();
            installedProduct.setName(product.getName());
            installedProduct.setVersion(product.getVersion());
            installedProduct.setRelease(product.getRelease());
            installedProduct.setArch(PackageFactory.lookupPackageArchByLabel(arch));
            installedProduct.setBaseproduct(true);

            return installedProduct;
        }).collect(Collectors.toSet());
    }

}
