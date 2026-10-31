/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.redhat.rhn.domain.kickstart;

import com.redhat.rhn.domain.rhnpackage.PackageName;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.io.Serializable;

public class KickstartPackageId implements Serializable {

    @Serial
    private static final long serialVersionUID = 3421403318007445343L;

    private KickstartData ksData;

    private PackageName packageName;

    /**
     * Constructor
     */
    public KickstartPackageId() {
    }

    /**
     * Constructor
     *
     * @param ksDataIn      the input ksData
     * @param packageNameIn the input packageName
     */
    public KickstartPackageId(KickstartData ksDataIn, PackageName packageNameIn) {
        ksData = ksDataIn;
        packageName = packageNameIn;
    }

    public KickstartData getKsData() {
        return ksData;
    }

    public void setKsData(KickstartData ksDataIn) {
        ksData = ksDataIn;
    }

    public PackageName getPackageName() {
        return packageName;
    }

    public void setPackageName(PackageName packageNameIn) {
        packageName = packageNameIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (!(oIn instanceof KickstartPackageId that)) {
            return false;
        }

        return new EqualsBuilder()
                .append(ksData, that.ksData)
                .append(packageName, that.packageName)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(ksData)
                .append(packageName)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "KickstartPackageId{" +
                "ksData=" + ksData +
                ", packageName=" + packageName +
                '}';
    }
}
