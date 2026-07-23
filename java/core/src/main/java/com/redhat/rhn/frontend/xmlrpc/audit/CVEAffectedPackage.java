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

package com.redhat.rhn.frontend.xmlrpc.audit;

import com.redhat.rhn.domain.rhnpackage.PackageType;

import java.util.Objects;

/**
 * A package that is affected by a vulnerability .i.e. CVE, but not necessarily vulnerable if the installed version is
 * patched.
 */
public class CVEAffectedPackage {
    private String packageName;
    private String installedVersion;
    private String patchedVersion;
    private PackageType packageType;
    private Status status;

    public enum Status {
        PATCHED, VULNERABLE
    }

    /**
     * Standard constructor
     *
     * @param packageNameIn the package name
     * @param installedVersionIn the installed version
     * @param patchedVersionIn the patched version
     * @param packageTypeIn the package type
     * @param statusIn the pre-calculated status
     */
    public CVEAffectedPackage(String packageNameIn, String installedVersionIn, String patchedVersionIn,
                              PackageType packageTypeIn, Status statusIn) {
        Objects.requireNonNull(packageNameIn);
        Objects.requireNonNull(packageTypeIn);
        Objects.requireNonNull(installedVersionIn);
        Objects.requireNonNull(statusIn);

        this.packageName = packageNameIn;
        this.installedVersion = installedVersionIn;
        this.patchedVersion = patchedVersionIn;
        this.packageType = packageTypeIn;
        this.status = statusIn;
    }

    public String getPackageName() {
        return packageName;
    }


    public String getInstalledVersion() {
        return installedVersion;
    }

    public String getPatchedVersion() {
        return patchedVersion;
    }

    public Status getStatus() {
        return status;
    }

    public PackageType getPackageType() {
        return packageType;
    }
}
