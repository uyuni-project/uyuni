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
  /*      if (CVEAuditManager.isCVEIdentifierUnknown(cveIdentifier)) {
            throw new UnknownCVEIdentifierException();
        }*/

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
            // TODO: We could make a custom query that would get us only the data we're interested in instead of relying
            //  on CVEAuditManager#doAuditManager implementation.

            CVEAuditSystemBuilder auditWithChannelsResult =
                    CVEAuditManager.doAuditSystem(clientServer.getId(), resultsBySystem.get(clientServer.getId()));

            if (doesSupportOVALAuditing(clientServer)) {
                systemAuditResult = doAuditSystem(cveIdentifier, resultsBySystem.get(clientServer.getId()),
                        clientServer);
                systemAuditResult.setChannels(auditWithChannelsResult.getChannels());
                systemAuditResult.setErratas(auditWithChannelsResult.getErratas());
            }
            else {
                systemAuditResult = auditWithChannelsResult;
            }

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

    public static boolean doesSupportOVALAuditing(Server clientServer) {
        // TODO: check if OVAL is synced and client product is support .e.g. Red Hat, Debian, Ubuntu or SUSE
        return true;
    }

    public static CVEAuditSystemBuilder doAuditSystem(String cveIdentifier,
                                                      List<CVEAuditManager.CVEPatchStatus> results,
                                                      Server clientServer) {
        // It's possible to have 2 or more patches for one package. It's necessary to apply all of them because
        // they will have the same outcome i.e. patch the package; instead we need to choose only one.
        // To choose the one, we rank patches based on the channel they come from .e.g.
        // assigned, successor product, etc. And for each vulnerable package we keep only the highest ranking patch
        results = keepOnlyPatchCandidates(results);

        CVEAuditSystemBuilder cveAuditServerBuilder = new CVEAuditSystemBuilder(clientServer.getId());
        cveAuditServerBuilder.setSystemName(clientServer.getName());

        List<ShallowSystemPackage> allInstalledPackages =
                PackageManager.shallowSystemPackageList(clientServer.getId());

        LOG.error("Vul packages before filtering: {}",
                OVALCachingFactory.getVulnerablePackagesByProductAndCve(clientServer.getCpe(), cveIdentifier));

        Set<VulnerablePackage> clientProductVulnerablePackages =
                OVALCachingFactory.getVulnerablePackagesByProductAndCve(clientServer.getCpe(), cveIdentifier).stream()
                        .filter(pkg -> isPackageInstalled(pkg, allInstalledPackages))
                        .collect(Collectors.toSet());

        LOG.error("Vul packages: {}", clientProductVulnerablePackages);

        if (clientProductVulnerablePackages.isEmpty()) {
            cveAuditServerBuilder.setPatchStatus(PatchStatus.NOT_AFFECTED);
            return cveAuditServerBuilder;
        }

        Set<VulnerablePackage> patchedVulnerablePackages = clientProductVulnerablePackages.stream()
                .filter(vulnerablePackage -> vulnerablePackage.getFixVersion().isPresent()).collect(
                        Collectors.toSet());

        Set<VulnerablePackage> unpatchedVulnerablePackages = clientProductVulnerablePackages.stream()
                .filter(vulnerablePackage -> vulnerablePackage.getFixVersion().isEmpty()).collect(
                        Collectors.toSet());

        if (patchedVulnerablePackages.isEmpty() && !unpatchedVulnerablePackages.isEmpty()) {
            cveAuditServerBuilder.setPatchStatus(PatchStatus.AFFECTED_PATCH_UNAVAILABLE);
        }
        else {
            boolean allPackagesPatched = patchedVulnerablePackages.stream().allMatch(patchedPackage ->
                    allInstalledPackages.stream()
                            .filter(installedPackage ->
                                    Objects.equals(installedPackage.getName(), patchedPackage.getName()))
                            .anyMatch(installedPackage ->
                                    installedPackage.getPackageEVR()
                                            .compareTo(PackageEvr.parseRpm(
                                                    patchedPackage.getFixVersion().get())) >= 0));

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

                    boolean allPackagesHavePatchInUnassignedChannels =
                            numberOfPackagesWithPatchInUnassignedChannels == patchedVulnerablePackages.size();
                    if (allPackagesHavePatchInUnassignedChannels) {
                        cveAuditServerBuilder.setPatchStatus(PatchStatus.AFFECTED_PATCH_INAPPLICABLE);
                    }
                    else {
                        cveAuditServerBuilder.setPatchStatus(PatchStatus.AFFECTED_PATCH_UNAVAILABLE);
                    }
                }
            }
        }

        LOG.error("Patch Status: {}", cveAuditServerBuilder.getPatchStatus());

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
        // TODO: Audit images with OVAL
        return CVEAuditManager.listImagesByPatchStatus(user, cveIdentifier, patchStatuses);
    }

    public static List<RankedChannel> populateCVEChannels(AuditTarget auditTarget) {
        return CVEAuditManager.populateCVEChannels(auditTarget);
    }

    public static void populateCVEChannels() {
        CVEAuditManager.populateCVEChannels();
    }
}
