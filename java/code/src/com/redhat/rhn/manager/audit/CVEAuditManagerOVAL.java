/*
 * Copyright (c) 2023 SUSE LLC
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

package com.redhat.rhn.manager.audit;


import static com.redhat.rhn.manager.audit.CVEAuditManager.SUCCESSOR_PRODUCT_RANK_BOUNDARY;

import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.rhnpackage.PackageManager;

import com.suse.oval.OVALCachingFactory;
import com.suse.oval.ShallowSystemPackage;
import com.suse.oval.vulnerablepkgextractor.VulnerablePackage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class, same as {@link CVEAuditManager}, provides the functionality of CVE auditing.It bases its evaluation on
 * OVAL data, in addition to channels data. Therefore, it provides more accurate results.
 * <p>
 * We can't get rid of {@link CVEAuditManager} yet because not all supported Linux distributions provide
 * OVAL vulnerability definitions, thus, we fall back to {@link CVEAuditManager} in that case.
 *
 */
public class CVEAuditManagerOVAL {

    private static final Logger LOG = LogManager.getLogger(CVEAuditManagerOVAL.class);

    private CVEAuditManagerOVAL() {

    }

    /**
     * List visible systems with their patch status regarding a given CVE identifier.
     *
     * @param user the calling user
     * @param cveIdentifier the CVE identifier to lookup
     * @param patchStatuses the patch statuses
     * @return list of system records with patch status
     * @throws UnknownCVEIdentifierException if the CVE number is not known
     */
    public static List<CVEAuditServer> listSystemsByPatchStatus(User user, String cveIdentifier,
                                                                EnumSet<PatchStatus> patchStatuses)
            throws UnknownCVEIdentifierException {
        if (isCVEIdentifierUnknown(cveIdentifier)) {
            throw new UnknownCVEIdentifierException();
        }

        List<CVEAuditServer> result = new ArrayList<>();

        List<CVEAuditManager.CVEPatchStatus> results = CVEAuditManager.listSystemsByPatchStatus(user, cveIdentifier)
                .collect(Collectors.toList());

        // Group the results by system
        Map<Long, List<CVEAuditManager.CVEPatchStatus>> resultsBySystem =
                results.stream().collect(Collectors.groupingBy(CVEAuditManager.CVEPatchStatus::getSystemId));

        Set<Server> clients = user.getServers();
        for (Server clientServer : clients) {
            CVEAuditSystemBuilder systemAuditResult;
            // We need this initially to be able to get errata and audit channels information for the OVAL
            // implementation.
            CVEAuditSystemBuilder auditWithChannelsResult =
                    CVEAuditManager.doAuditSystem(clientServer.getId(), resultsBySystem.get(clientServer.getId()));

            systemAuditResult = doAuditSystem(cveIdentifier, resultsBySystem.get(clientServer.getId()),
                    clientServer);
            systemAuditResult.setChannels(auditWithChannelsResult.getChannels());
            systemAuditResult.setErratas(auditWithChannelsResult.getErratas());

            if (patchStatuses.contains(systemAuditResult.getPatchStatus())) {
                result.add(new CVEAuditServer(
                        systemAuditResult.getId(),
                        systemAuditResult.getSystemName(),
                        systemAuditResult.getPatchStatus(),
                        systemAuditResult.getChannels(),
                        systemAuditResult.getErratas()));
            }
        }

        return result;
    }

    private static boolean isCVEIdentifierUnknown(String cveIdentifier) {
        return !OVALCachingFactory.canAuditCVE(cveIdentifier) && CVEAuditManager.isCVEIdentifierUnknown(cveIdentifier);
    }

    /**
     * Audit the given {@code clientServer} regarding the given CVE identifier based on OVAL and Channels data.
     *
     * @param clientServer the server to audit
     * @param results list produced by {@link CVEAuditManager#listSystemsByPatchStatus(User, String)},
     *                helpful for determining the availability of a patch for the vulnerability in channels.
     * @param cveIdentifier the CVE identifier
     * @return a record with data about a single system containing that system's patch status regarding a certain
     * given CVE identifier as well as sets of relevant channels and erratas.
     * */
    public static CVEAuditSystemBuilder doAuditSystem(String cveIdentifier,
                                                      List<CVEAuditManager.CVEPatchStatus> results,
                                                      Server clientServer) {
        // It's possible to find more than one patch for a particular package in the available channels. It's NOT
        // necessary to apply all of them because they will have the same outcome i.e. patch the package;
        // instead we need to choose only one. To choose the one, we rank patches based on the channel they come
        // from .e.g. assigned, successor product, etc. And for each vulnerable package we keep only the highest
        // ranking patch.
        results = keepOnlyPatchCandidates(results);

        CVEAuditSystemBuilder cveAuditServerBuilder = new CVEAuditSystemBuilder(clientServer.getId());
        cveAuditServerBuilder.setSystemName(clientServer.getName());

        List<ShallowSystemPackage> allInstalledPackages =
                PackageManager.shallowSystemPackageList(clientServer.getId());

        LOG.debug("Vulnerable packages before filtering: {}",
                OVALCachingFactory.getVulnerablePackagesByProductAndCve(clientServer.getCpe(), cveIdentifier));

        Set<VulnerablePackage> clientProductVulnerablePackages =
                OVALCachingFactory.getVulnerablePackagesByProductAndCve(clientServer.getCpe(), cveIdentifier).stream()
                        .filter(pkg -> isPackageInstalled(pkg, allInstalledPackages))
                        .collect(Collectors.toSet());

        LOG.debug("Vulnerable packages after filtering: {}", clientProductVulnerablePackages);

        if (clientProductVulnerablePackages.isEmpty()) {
            cveAuditServerBuilder.setPatchStatus(PatchStatus.NOT_AFFECTED);
            return cveAuditServerBuilder;
        }

        // The list of vulnerable packages for which a patch has been released
        Set<VulnerablePackage> patchedVulnerablePackages = clientProductVulnerablePackages.stream()
                .filter(vulnerablePackage -> vulnerablePackage.getFixVersion().isPresent()).collect(
                        Collectors.toSet());

        Set<VulnerablePackage> unpatchedVulnerablePackages = clientProductVulnerablePackages.stream()
                .filter(vulnerablePackage -> vulnerablePackage.getFixVersion().isEmpty()).collect(
                        Collectors.toSet());

        boolean allPackagesUnpatched = unpatchedVulnerablePackages.size() == clientProductVulnerablePackages.size();

        if (allPackagesUnpatched) {
            cveAuditServerBuilder.setPatchStatus(PatchStatus.AFFECTED_PATCH_UNAVAILABLE);
        }
        else {
            boolean allPackagesPatched = patchedVulnerablePackages.stream().allMatch(patchedPackage ->
                    getInstalledPackageVersions(patchedPackage, allInstalledPackages)
                            .stream().allMatch(installedPackage -> installedPackage.getPackageEVR()
                                    .compareTo(PackageEvr.parseRpm(patchedPackage.getFixVersion().get())) >= 0));

            if (allPackagesPatched) {
                cveAuditServerBuilder.setPatchStatus(PatchStatus.PATCHED);
            }
            else {
                List<CVEAuditManager.CVEPatchStatus> patchesInAssignedChannels = results.stream()
                        .filter(CVEAuditManager.CVEPatchStatus::isChannelAssigned)
                        .collect(Collectors.toList());

                List<CVEAuditManager.CVEPatchStatus> patchesInUnassignedChannels = results.stream()
                        .filter(cvePatchStatus -> !cvePatchStatus.isChannelAssigned())
                        .collect(Collectors.toList());

                long numberOfPackagesWithPatchInAssignedChannels =
                        patchedVulnerablePackages.stream().filter(patchedPackage -> patchesInAssignedChannels
                                .stream()
                                .anyMatch(patch ->
                                        patch.getPackageName().equals(Optional.of(patchedPackage.getName()))
                                )
                        ).count();

                boolean allPackagesHavePatchInAssignedChannels =
                        numberOfPackagesWithPatchInAssignedChannels == patchedVulnerablePackages.size();
                boolean somePackagesHavePatchInAssignedChannels = numberOfPackagesWithPatchInAssignedChannels > 0;

                if (allPackagesHavePatchInAssignedChannels) {
                    cveAuditServerBuilder.setPatchStatus(PatchStatus.AFFECTED_FULL_PATCH_APPLICABLE);
                }
                else if (somePackagesHavePatchInAssignedChannels) {
                    cveAuditServerBuilder.setPatchStatus(PatchStatus.AFFECTED_PARTIAL_PATCH_APPLICABLE);
                }
                else {
                    long numberOfPackagesWithPatchInUnassignedChannels =
                            patchedVulnerablePackages.stream().filter(patchedPackage -> patchesInUnassignedChannels
                                    .stream()
                                    .anyMatch(patch ->
                                            patch.getPackageName().equals(Optional.of(patchedPackage.getName()))
                                    )
                            ).count();

                    boolean somePackagesHavePatchInUnassignedChannels =
                            numberOfPackagesWithPatchInUnassignedChannels > 0 &&
                                    numberOfPackagesWithPatchInUnassignedChannels == patchedVulnerablePackages.size();

                    boolean allPackagesHavePatchInUnassignedChannels =
                            numberOfPackagesWithPatchInUnassignedChannels == patchedVulnerablePackages.size();

                    if (allPackagesHavePatchInUnassignedChannels) {
                        boolean allPackagesHavePatchInSuccessorChannel = patchesInUnassignedChannels.stream()
                                .allMatch(patch ->
                                        patch.getChannelRank().orElse(0L) >= SUCCESSOR_PRODUCT_RANK_BOUNDARY);
                        if (allPackagesHavePatchInSuccessorChannel) {
                            cveAuditServerBuilder
                                    .setPatchStatus(PatchStatus.AFFECTED_PATCH_INAPPLICABLE_SUCCESSOR_PRODUCT);
                        }
                        else {
                            cveAuditServerBuilder.setPatchStatus(PatchStatus.AFFECTED_PATCH_INAPPLICABLE);
                        }
                    }
                    else if (somePackagesHavePatchInUnassignedChannels) {
                        cveAuditServerBuilder.setPatchStatus(PatchStatus.AFFECTED_PATCH_INAPPLICABLE);
                    }
                    else {
                        cveAuditServerBuilder.setPatchStatus(PatchStatus.AFFECTED_PATCH_UNAVAILABLE_IN_UYUNI);
                    }
                }
            }
        }

        LOG.debug("'{}' patch status: {}", cveAuditServerBuilder.getSystemName(),
                cveAuditServerBuilder.getPatchStatus());

        return cveAuditServerBuilder;
    }

    private static List<CVEAuditManager.CVEPatchStatus> keepOnlyPatchCandidates(
            List<CVEAuditManager.CVEPatchStatus> results) {
        List<CVEAuditManager.CVEPatchStatus> patchCandidates = new ArrayList<>();

        Map<String, List<CVEAuditManager.CVEPatchStatus>> resultsByPackage = results.stream()
                .filter(result -> result.getPackageName().isPresent())
                .collect(Collectors.groupingBy(r -> r.getPackageName().get()));

        for (String packageName : resultsByPackage.keySet()) {
            List<CVEAuditManager.CVEPatchStatus> packageResults = resultsByPackage.get(packageName);
            CVEAuditManager.getPatchCandidateResult(packageResults).ifPresent(patchCandidates::add);
        }

        return patchCandidates;
    }

    private static boolean isPackageInstalled(VulnerablePackage pkg, List<ShallowSystemPackage> allInstalledPackages) {
        return allInstalledPackages.stream()
                .anyMatch(installed -> Objects.equals(installed.getName(), pkg.getName()));
    }

    /**
     * Returns the list of installed versions of {@code pkg}
     * */
    private static List<ShallowSystemPackage> getInstalledPackageVersions(
            VulnerablePackage pkg,
            List<ShallowSystemPackage> allInstalledPackages) {

        return allInstalledPackages.stream().filter(installed -> Objects.equals(installed.getName(), pkg.getName()))
                .collect(Collectors.toList());
    }

    /**
     * List visible images with their patch status regarding a given CVE identifier.
     *
     * @param user the calling user
     * @param cveIdentifier the CVE identifier to lookup
     * @param patchStatuses the patch statuses
     * @return list of images records with patch status
     * @throws UnknownCVEIdentifierException if the CVE number is not known
     */
    public static List<CVEAuditImage> listImagesByPatchStatus(User user,
                                                              String cveIdentifier, EnumSet<PatchStatus> patchStatuses)
            throws UnknownCVEIdentifierException {
        return CVEAuditManager.listImagesByPatchStatus(user, cveIdentifier, patchStatuses);
    }

    /**
     * Populate channels for CVE Audit
     * */
    public static void populateCVEChannels() {
        CVEAuditManager.populateCVEChannels();
    }
}
