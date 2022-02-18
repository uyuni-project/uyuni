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

import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageName;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Primary key class for suseImagePackage table.
 */
public class ImagePackageKey implements Serializable {

    private PackageEvr evr;
    private PackageName name;
    private ImageInfo imageInfo;

    /**
     * @return the package name
     */
    public PackageName getName() {
        return name;
    }

    /**
     * @param evrIn the package evr
     */
    public void setEvr(PackageEvr evrIn) {
        this.evr = evrIn;
    }

    /**
     * @return the package evr
     */
    public PackageEvr getEvr() {
        return evr;
    }

    /**
     * @param imageInfoIn the image info
     */
    public void setImageInfo(ImageInfo imageInfoIn) {
        this.imageInfo = imageInfoIn;
    }

    /**
     * @return the image info
     */
    public ImageInfo getImageInfo() {
        return imageInfo;
    }

    /**
     * @param nameIn the package name
     */
    public void setName(PackageName nameIn) {
        this.name = nameIn;
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
        return builder.toHashCode();

    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object other) {

        if (other instanceof ImagePackageKey) {
            ImagePackageKey otherPack = (ImagePackageKey) other;
            return new EqualsBuilder()
                    .append(this.getName(), otherPack.getName())
                    .append(this.getEvr(), otherPack.getEvr())
                    .append(this.getImageInfo(), otherPack.getImageInfo())
                    .isEquals();
        }
        else {
            return false;
        }
    }
}
