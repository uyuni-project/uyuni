/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */


package com.redhat.rhn.domain.rhnpackage.profile;


import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageName;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.io.Serializable;


public class ProfileEntryId implements Serializable {

    @Serial
    private static final long serialVersionUID = -6612481216610063117L;

    private Profile profile;

    private PackageEvr evr;

    private PackageName name;

    private PackageArch arch;

    /**
     * Constructor
     */
    public ProfileEntryId() {
    }

    /**
     * Constructor
     *
     * @param profileIn the input profile
     * @param evrIn     the input evr
     * @param nameIn    the input name
     * @param archIn    the input arch
     */
    public ProfileEntryId(Profile profileIn, PackageEvr evrIn, PackageName nameIn, PackageArch archIn) {
        profile = profileIn;
        evr = evrIn;
        name = nameIn;
        arch = archIn;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profileIn) {
        profile = profileIn;
    }

    public PackageEvr getEvr() {
        return evr;
    }

    public void setEvr(PackageEvr evrIn) {
        evr = evrIn;
    }

    public PackageName getName() {
        return name;
    }

    public void setName(PackageName nameIn) {
        name = nameIn;
    }

    public PackageArch getArch() {
        return arch;
    }

    public void setArch(PackageArch archIn) {
        arch = archIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (!(oIn instanceof ProfileEntryId that)) {
            return false;
        }

        return new EqualsBuilder()
                .append(profile, that.profile)
                .append(evr, that.evr)
                .append(name, that.name)
                .append(arch, that.arch)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(profile)
                .append(evr)
                .append(name)
                .append(arch)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "ProfileEntryId{" +
                "profile=" + profile +
                ", evr=" + evr +
                ", name=" + name +
                ", arch=" + arch +
                '}';
    }
}
