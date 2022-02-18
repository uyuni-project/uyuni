/*
 * Copyright (c) 2017 SUSE LLC
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
package com.redhat.rhn.domain.image;


import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageName;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * suseImageInfoPackage table class.
 */
@Entity
@IdClass(ImagePackageKey.class)
@Table(name = "suseImageInfoPackage")
public class ImagePackage implements Comparable<ImagePackage> {

    private PackageEvr evr;
    private PackageName name;
    private PackageArch arch;
    private ImageInfo imageInfo;
    private Date installTime;

    /**
     * @return imageInfo
     */
    @Id
    @ManyToOne
    @JoinColumn(name = "image_info_id")
    public ImageInfo getImageInfo() {
        return imageInfo;
    }

    /**
     * @param imageInfoIn imageInfo
     */
    public void setImageInfo(ImageInfo imageInfoIn) {
        this.imageInfo = imageInfoIn;
    }

    /**
     * @return Returns the arch.
     */
    @ManyToOne
    @JoinColumn(name = "package_arch_id")
    public PackageArch getArch() {
        return arch;
    }

    /**
     * @param archIn The arch to set.
     */
    public void setArch(PackageArch archIn) {
        this.arch = archIn;
    }

    /**
     * @return Returns the evr.
     */
    @Id
    @ManyToOne
    @JoinColumn(name = "evr_id")
    public PackageEvr getEvr() {
        return evr;
    }

    /**
     * @param evrIn The evr to set.
     */
    public void setEvr(PackageEvr evrIn) {
        this.evr = evrIn;
    }

    /**
     * @return Returns the name.
     */
    @Id
    @ManyToOne
    @JoinColumn(name = "name_id")
    public PackageName getName() {
        return name;
    }

    /**
     * @param nameIn The name to set.
     */
    public void setName(PackageName nameIn) {
        this.name = nameIn;
    }

    /**
     * Getter for installTime
     * @return Date when package was installed (as reported by rpm database).
     */
    @Column(name = "installtime")
    public Date getInstallTime() {
        return this.installTime;
    }

    /**
     * Setter for installTime
     * @param installTimeIn to set
     */
    public void setInstallTime(Date installTimeIn) {
        this.installTime = installTimeIn;
    }

    /**
     *
     * {@inheritDoc}
     */
    public int hashCode() {
        HashCodeBuilder builder =  new HashCodeBuilder().append(name.getName())
                .append(evr.getEpoch())
                .append(evr.getRelease())
                .append(evr.getVersion())
                .append(imageInfo.getId());
        if (this.arch != null) {
            builder.append(arch.getName());
        }
        return builder.toHashCode();

    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object other) {

        if (other instanceof ImagePackage) {
            ImagePackage otherPack = (ImagePackage) other;
            return new EqualsBuilder().append(this.getName(), otherPack.getName())
                    .append(this.getEvr(), otherPack.getEvr())
                    .append(this.getImageInfo(), otherPack.getImageInfo())
                    .append(this.getArch(), otherPack.getArch()).isEquals();


        }
        else if (other instanceof Package) {
            Package otherPack = (Package) other;

            EqualsBuilder builder =  new EqualsBuilder()
                    .append(this.getName(), otherPack.getPackageName())
                    .append(this.getEvr(), otherPack.getPackageEvr());

            if (this.getArch() != null) {
                builder.append(this.getArch(), otherPack.getPackageArch());
            }
            return builder.isEquals();
        }
        else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    public int compareTo(ImagePackage ip) {
        if (equals(ip)) {
            return 0;
        }
        if (!getName().equals(ip.getName())) {
            return getName().compareTo(ip.getName());
        }
        if (!getEvr().equals(ip.getEvr())) {
            return getEvr().compareTo(ip.getEvr());
        }
        if (getArch() != null) {
            return getArch().compareTo(ip.getArch());
        }

        if (ip.getArch() != null) {
            return -1;
        }
        return 0;
    }
}
