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
import com.redhat.rhn.domain.rhnpackage.PackageType;

/**
 * A package that is affected by a CVE
 */
public class CVEAffectedPackageItem {
    private Long systemId;
    private String systemName;
    private String packageName;
    private PackageType packageType;
    private String patchedVersion;
    private PackageEvr installedPackageEvr;
    private String cve;
    private String patchStatus;

    /**
     * Standard constructor
     * @param systemIdIn the system id
     * @param systemNameIn the system name
     * @param packageNameIn the package name
     * @param packageTypeIn the package type
     * @param patchedVersionIn the patched version
     * @param installedEpochIn the installed epoch
     * @param installedVersionIn the installed version
     * @param installedReleaseIn the installed release
     * @param cveIn the cve identifier
     * @param patchStatusIn the patch status
     */
    public CVEAffectedPackageItem(Long systemIdIn, String systemNameIn, String packageNameIn, PackageType packageTypeIn,
                                  String patchedVersionIn, String installedEpochIn, String installedVersionIn,
                                  String installedReleaseIn, String cveIn, String patchStatusIn) {
        this.systemId = systemIdIn;
        this.systemName = systemNameIn;
        this.packageName = packageNameIn;
        this.packageType = packageTypeIn;
        this.patchedVersion = patchedVersionIn;
        this.installedPackageEvr = new PackageEvr(installedEpochIn, installedVersionIn, installedReleaseIn,
                packageTypeIn);
        this.cve = cveIn;
        this.patchStatus = patchStatusIn;
    }


    public Long getSystemId() {
        return systemId;
    }

    /**
     * Set the system id.
     * @param systemIdIn the system id
     */
    public void setSystemId(Long systemIdIn) {
        this.systemId = systemIdIn;
    }

    public String getSystemName() {
        return systemName;
    }

    /**
     * Set the system name.
     * @param systemNameIn the system name
     */
    public void setSystemName(String systemNameIn) {
        this.systemName = systemNameIn;
    }

    public String getPackageName() {
        return packageName;
    }

    /**
     * Set the package name.
     * @param packageNameIn the package name
     */
    public void setPackageName(String packageNameIn) {
        this.packageName = packageNameIn;
    }

    public PackageType getPackageType() {
        return packageType;
    }

    /**
     * Set the package type.
     * @param packageTypeIn the package type
     */
    public void setPackageType(PackageType packageTypeIn) {
        this.packageType = packageTypeIn;
    }

    public String getPatchedVersion() {
        return patchedVersion;
    }

    /**
     * Set the patched version.
     * @param patchedVersionIn the patched version
     */
    public void setPatchedVersion(String patchedVersionIn) {
        this.patchedVersion = patchedVersionIn;
    }

    public PackageEvr getInstalledPackageEvr() {
        return installedPackageEvr;
    }

    /**
     * Set the installed package evr.
     * @param installedPackageEvrIn the installed package evr
     */
    public void setInstalledPackageEvr(PackageEvr installedPackageEvrIn) {
        this.installedPackageEvr = installedPackageEvrIn;
    }

    public String getCve() {
        return cve;
    }

    /**
     * Set the cve.
     * @param cveIn the cve identifier
     */
    public void setCve(String cveIn) {
        cve = cveIn;
    }

    public String getPatchStatus() {
        return patchStatus;
    }

    /**
     * Set the patch status.
     * @param patchStatusIn the patch status
     */
    public void setPatchStatus(String patchStatusIn) {
        this.patchStatus = patchStatusIn;
    }
}
