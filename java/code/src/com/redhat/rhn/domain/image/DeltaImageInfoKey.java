/*
 * Copyright (c) 2021 SUSE LLC
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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Primary key class for DeltaImageInfo.
 */
public class DeltaImageInfoKey implements Serializable {

    private ImageInfo sourceImageInfo;
    private ImageInfo targetImageInfo;

    /**
     * @return the source image info
     */
    public ImageInfo getSourceImageInfo() {
        return sourceImageInfo;
    }

    /**
     * @param imageInfoIn the source image info
     */
    public void setSourceImageInfo(ImageInfo imageInfoIn) {
        this.sourceImageInfo = imageInfoIn;
    }

    /**
     * @return the target image info
     */
    public ImageInfo getTargetImageInfo() {
        return targetImageInfo;
    }

    /**
     * @param imageInfoIn the target image info
     */
    public void setTargetImageInfo(ImageInfo imageInfoIn) {
        this.targetImageInfo = imageInfoIn;
    }

    /**
     *
     * {@inheritDoc}
     */
    public int hashCode() {
        HashCodeBuilder builder =  new HashCodeBuilder()
                .append(sourceImageInfo.getId())
                .append(targetImageInfo.getId());
        return builder.toHashCode();

    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object other) {

        if (other instanceof DeltaImageInfoKey) {
            DeltaImageInfoKey otherInfo = (DeltaImageInfoKey) other;
            return new EqualsBuilder()
                    .append(this.getSourceImageInfo(), otherInfo.getSourceImageInfo())
                    .append(this.getTargetImageInfo(), otherInfo.getTargetImageInfo())
                    .isEquals();
        }
        else {
            return false;
        }
    }

}
