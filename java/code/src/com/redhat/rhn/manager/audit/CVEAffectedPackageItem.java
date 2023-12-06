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

public class CVEAffectedPackageItem {
    private Long systemId;
    private String systemName;
    private String packageName;
    private PackageType packageType;
    private String patchedVersion;
    private PackageEvr installedPackageEvr;
    private String cve;

    /**
     * Standard constructor
     * */
    public CVEAffectedPackageItem(Long systemId, String systemName, String packageName, PackageType packageType, String patchedVersion,
                                  String installedEpoch, String installedVersion, String installedRelease, String cveIn) {
        this.systemId = systemId;
        this.systemName = systemName;
        this.packageName = packageName;
        this.packageType = packageType;
        this.patchedVersion = patchedVersion;
        this.installedPackageEvr = new PackageEvr(installedEpoch, installedVersion, installedRelease, packageType);
        cve = cveIn;
    }

    public Long getSystemId() {
        return systemId;
    }

    public void setSystemId(Long systemId) {
        this.systemId = systemId;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public PackageType getPackageType() {
        return packageType;
    }

    public void setPackageType(PackageType packageType) {
        this.packageType = packageType;
    }

    public String getPatchedVersion() {
        return patchedVersion;
    }

    public void setPatchedVersion(String patchedVersion) {
        this.patchedVersion = patchedVersion;
    }

    public PackageEvr getInstalledPackageEvr() {
        return installedPackageEvr;
    }

    public void setInstalledPackageEvr(PackageEvr installedPackageEvr) {
        this.installedPackageEvr = installedPackageEvr;
    }

    public String getCve() {
        return cve;
    }

    public void setCve(String cveIn) {
        cve = cveIn;
    }
}
