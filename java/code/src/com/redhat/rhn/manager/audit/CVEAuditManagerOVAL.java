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
 * The OVAL version of the {@link CVEAuditManager}. This what should be used, the other implementation is legacy.
 */
public class CVEAuditManagerOVAL {
    private static Logger log = LogManager.getLogger(CVEAuditManagerOVAL.class);

    /**
     *
     * */
    public static List<CVEAuditServer> listSystemsByPatchStatus(User user, String cveIdentifier,
                                                                EnumSet<PatchStatus> patchStatuses) {

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
            CVEAuditSystemBuilder auditWithChannelsResult = CVEAuditManager.doAuditSystem(clientServer.getId(), results);

            if (doesSupportOVALAuditing(clientServer)) {
                systemAuditResult = doAuditSystem(cveIdentifier, resultsBySystem.get(clientServer.getId()), clientServer);
/*
                // Temporary workaround for error in frontend when having status AFFECTED_PATCH_INAPPLICABLE
                systemAuditResult.setChannels(Set.of(new AuditChannelInfo(12, "Houssem's Channel", "Channel Label", 6)));
                systemAuditResult.setErratas(Set.of(new ErrataIdAdvisoryPair(25, "Houssem Security")));*/

                log.error(auditWithChannelsResult.getChannels());
                log.error(auditWithChannelsResult.getErratas());
            } else {
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

        CVEAuditSystemBuilder cveAuditServerBuilder = new CVEAuditSystemBuilder(clientServer.getId());
        cveAuditServerBuilder.setSystemName(clientServer.getName());

        List<ShallowSystemPackage> allInstalledPackages =
                PackageManager.shallowSystemPackageList(clientServer.getId());

        log.error("Vul packages before filtering: {}",
                OVALCachingFactory.getVulnerablePackagesByProductAndCve(clientServer.getCpe(), cveIdentifier));

        Set<VulnerablePackage> clientProductVulnerablePackages =
                OVALCachingFactory.getVulnerablePackagesByProductAndCve(clientServer.getCpe(), cveIdentifier).stream()
                        .filter(pkg -> isPackageInstalled(pkg, allInstalledPackages))
                        .collect(Collectors.toSet());

        log.error("Vul packages: {}", clientProductVulnerablePackages);

        if (clientProductVulnerablePackages.isEmpty()) {
            cveAuditServerBuilder.setPatchStatus(PatchStatus.NOT_AFFECTED);
            return cveAuditServerBuilder;
        }

        Set<VulnerablePackage> patched = clientProductVulnerablePackages.stream()
                .filter(vulnerablePackage -> vulnerablePackage.getFixVersion().isPresent()).collect(
                        Collectors.toSet());

        Set<VulnerablePackage> unpatched = clientProductVulnerablePackages.stream()
                .filter(vulnerablePackage -> vulnerablePackage.getFixVersion().isEmpty()).collect(
                        Collectors.toSet());

        List<CVEAuditManager.CVEPatchStatus> patchesInAssignedChannels = results.stream()
                .filter(CVEAuditManager.CVEPatchStatus::isChannelAssigned)
                .collect(Collectors.toList());

        List<CVEAuditManager.CVEPatchStatus> patchesInUnassignedChannels = results.stream()
                .filter(cvePatchStatus -> !cvePatchStatus.isChannelAssigned())
                .collect(Collectors.toList());

        if (patched.isEmpty() && !unpatched.isEmpty()) {
            cveAuditServerBuilder.setPatchStatus(PatchStatus.AFFECTED_PATCH_UNAVAILABLE);
        } else {
            log.error(allInstalledPackages.stream().map(ShallowSystemPackage::getPackageEVR).collect(Collectors.toList()));

            boolean allPatchesInstalled = patched.stream().allMatch(patchedPackage ->
                    allInstalledPackages.stream()
                            .anyMatch(systemPackage ->
                            Objects.equals(systemPackage.getName(), patchedPackage.getName()) && systemPackage.getPackageEVR()
                                    .compareTo(PackageEvr.parseRpm(patchedPackage.getFixVersion().get())) >= 0));

            if (allPatchesInstalled) {
                cveAuditServerBuilder.setPatchStatus(PatchStatus.PATCHED);
            } else {
                long numberOfPackagesWithPatchInAssignedChannels =
                        patched.stream().filter(patchedPackage -> patchesInAssignedChannels
                                        .stream()
                                        .anyMatch(patch ->
                                                patch.getPackageName().equals(Optional.of(patchedPackage.getName()))))
                                .count();

                boolean allPackagesHavePatchInAssignedChannels =
                        numberOfPackagesWithPatchInAssignedChannels == patched.size();
                boolean somePackagesHavePatchInAssignedChannels = numberOfPackagesWithPatchInAssignedChannels > 0;

                if (allPackagesHavePatchInAssignedChannels) {
                    cveAuditServerBuilder.setPatchStatus(PatchStatus.AFFECTED_FULL_PATCH_APPLICABLE);
                } else if (somePackagesHavePatchInAssignedChannels) {
                    cveAuditServerBuilder.setPatchStatus(PatchStatus.AFFECTED_PARTIAL_PATCH_APPLICABLE);
                } else {
                    long numberOfPackagesWithPatchInUnassignedChannels =
                            patched.stream().filter(patchedPackage -> patchesInUnassignedChannels
                                            .stream()
                                            .anyMatch(patch ->
                                                    patch.getPackageName().equals(Optional.of(patchedPackage.getName()))))
                                    .count();

                    boolean allPackagesHavePatchInUnassignedChannels =
                            numberOfPackagesWithPatchInUnassignedChannels == patched.size();
                    if (allPackagesHavePatchInUnassignedChannels) {
                        cveAuditServerBuilder.setPatchStatus(PatchStatus.AFFECTED_PATCH_INAPPLICABLE);
                    } else {
                        cveAuditServerBuilder.setPatchStatus(PatchStatus.AFFECTED_PATCH_UNAVAILABLE);
                    }
                }
            }
        }

        log.error("Patch Status: {}", cveAuditServerBuilder.getPatchStatus());

        return cveAuditServerBuilder;
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
        return CVEAuditManager.listImagesByPatchStatus(user, cveIdentifier, patchStatuses);
    }

    public static List<RankedChannel> populateCVEChannels(AuditTarget auditTarget) {
        return CVEAuditManager.populateCVEChannels(auditTarget);
    }

    public static void populateCVEChannels() {
        CVEAuditManager.populateCVEChannels();
    }
}
