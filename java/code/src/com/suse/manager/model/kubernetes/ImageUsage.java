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

package com.suse.manager.model.kubernetes;

import com.redhat.rhn.domain.image.ImageInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Information about the usage of a Docker image in
 * a Kubernetes cluster.
 */
public class ImageUsage {

    private ImageInfo imageInfo;
    private List<ContainerInfo> containerInfos = new ArrayList<>();

    // Image runtime status values, ordered by severity
    public static final int RUNTIME_NOINSTANCE = 0;
    public static final int RUNTIME_UPTODATE = 1;
    public static final int RUNTIME_UNKNOWN = 2;
    public static final int RUNTIME_OUTOFDATE = 3;

    /**
     * @param imageInfoIn image info entity bean.
     */
    public ImageUsage(ImageInfo imageInfoIn) {
        this.imageInfo = imageInfoIn;
    }

    /**
     * @return image info entity bean.
     */
    public ImageInfo getImageInfo() {
        return imageInfo;
    }

    /**
     * @param imageInfoIn to set
     */
    public void setImageInfo(ImageInfo imageInfoIn) {
        this.imageInfo = imageInfoIn;
    }

    /**
     * @return information about the containers created from this image.
     */
    public List<ContainerInfo> getContainerInfos() {
        return containerInfos;
    }

    /**
     * @param containersIn to set
     */
    public void setContainerInfos(List<ContainerInfo> containersIn) {
        this.containerInfos = containersIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ImageUsage)) {
            return false;
        }

        ImageUsage that = (ImageUsage) o;

        return imageInfo.equals(that.imageInfo);
    }

    @Override
    public int hashCode() {
        return imageInfo.hashCode();
    }
}
