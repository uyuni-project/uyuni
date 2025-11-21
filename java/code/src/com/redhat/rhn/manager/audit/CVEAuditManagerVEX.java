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

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.rhnpackage.PackageManager;

import com.suse.oval.ShallowSystemPackage;
import com.suse.oval.vulnerablepkgextractor.VulnerablePackage;
import com.suse.vex.VEXCachingFactory;

/**
 * CVE auditing using VEX data.
 */
public class CVEAuditManagerVEX {

    private static final Logger LOG = LogManager.getLogger(CVEAuditManagerVEX.class);

    private CVEAuditManagerVEX() {
        // Private constructor for utility class
    }

    /**
    * Lists the patch status of systems for a given CVE using VEX data.
    *
    * @param user          the user performing the audit
    * @param cveIdentifier the CVE to audit
    * @param patchStatuses statuses to include
    * @return list of matching systems
    * @throws UnknownCVEIdentifierException if CVE is unknown
    */
    public static List<CVEAuditServer> listSystemsByPatchStatus(User user, String cveIdentifier,
                                                                EnumSet<PatchStatus> patchStatuses)

            throws UnknownCVEIdentifierException {
        if (!VEXCachingFactory.canAuditCVE(cveIdentifier)) {
            throw new UnknownCVEIdentifierException();
        }

        // If patchStatuses is null, it doesnt apply any filter
        if (patchStatuses == null) {
            patchStatuses = EnumSet.allOf(PatchStatus.class);
        }

        List<CVEAuditServer> result = new ArrayList<>();
        Set<Server> clients = user.getServers();

        for (Server clientServer : clients) {
            if (!VEXCachingFactory.checkVEXAvailability(clientServer.getCpe())) {
                continue;
            }

            CVEAuditSystemBuilder auditResult =
                    doAuditSystem(cveIdentifier, clientServer);

            if (patchStatuses.contains(auditResult.getPatchStatus())) {
                result.add(new CVEAuditServer(
                        auditResult.getId(),
                        auditResult.getSystemName(),
                        auditResult.getPatchStatus(),
                        auditResult.getChannels(),
                        auditResult.getErratas(),
                        auditResult.getScanDataSources()));
            }
        }

        return result;
    }

    /**
     * Audits a system for a given CVE using VEX data.
     *
     * @param cveIdentifier the CVE identifier
     * @param clientServer  the system to audit
     * @return audit result builder
     */
    public static CVEAuditSystemBuilder doAuditSystem(String cveIdentifier, Server clientServer) {
        CVEAuditSystemBuilder builder = new CVEAuditSystemBuilder(clientServer.getId());
        builder.setSystemName(clientServer.getName());

        List<ShallowSystemPackage> installedPackages =
                PackageManager.shallowSystemPackageList(clientServer.getId());

        Set<VulnerablePackage> vulnerablePackages =
                new HashSet<>(VEXCachingFactory.getVulnerablePackagesByProductAndCve(clientServer.getCpe(), cveIdentifier));

        LOG.debug("Vulnerable packages before filtering: {}", vulnerablePackages);

        Set<VulnerablePackage> installedVulnerablePackages = vulnerablePackages.stream()
                .filter(pkg -> isPackageInstalled(pkg, installedPackages))
                .collect(Collectors.toSet());

        LOG.debug("Vulnerable packages after filtering: {}", installedVulnerablePackages);

        if (installedVulnerablePackages.isEmpty()) {
            builder.setPatchStatus(PatchStatus.NOT_AFFECTED);
            return builder;
        }

        Set<VulnerablePackage> patched = installedVulnerablePackages.stream()
                .filter(pkg -> pkg.getFixVersion().isPresent()).collect(Collectors.toSet());

        Set<VulnerablePackage> unpatched = installedVulnerablePackages.stream()
                .filter(pkg -> pkg.getFixVersion().isEmpty()).collect(Collectors.toSet());

        if (unpatched.size() == installedVulnerablePackages.size()) {
            builder.setPatchStatus(PatchStatus.AFFECTED_PATCH_UNAVAILABLE);
        }
        else {
            boolean allPatched = patched.stream().allMatch(pkg ->
                    getInstalledVersions(pkg, installedPackages).stream()
                            .allMatch(installed -> installed.getPackageEVR()
                                    .compareTo(PackageEvr.parseRpm(pkg.getFixVersion().get())) >= 0));

            if (allPatched) {
                builder.setPatchStatus(PatchStatus.PATCHED);
            }
            else {
                builder.setPatchStatus(PatchStatus.AFFECTED_PATCH_UNAVAILABLE_IN_UYUNI);
            }
        }

        builder.setScanDataSources(ScanDataSource.VEX);
        return builder;
    }

    private static boolean isPackageInstalled(VulnerablePackage pkg, List<ShallowSystemPackage> installed) {
        return installed.stream().anyMatch(p -> Objects.equals(p.getName(), pkg.getName()));
    }

    private static List<ShallowSystemPackage> getInstalledVersions(VulnerablePackage pkg,
                                                                   List<ShallowSystemPackage> installed) {
        return installed.stream()
                .filter(p -> Objects.equals(p.getName(), pkg.getName()))
                .collect(Collectors.toList());
    }



}
