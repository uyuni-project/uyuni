/**
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
package com.redhat.rhn.manager.audit;

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.product.SUSEProductSet;

import java.util.Optional;
import java.util.Set;

/**
 * ImageAuditTarget
 */
public class ImageAuditTarget implements AuditTarget {

    private final ImageInfo imageInfo;

    /**
     * Constructor
     * @param imageInfoIn an image Info object
     */
    public ImageAuditTarget(ImageInfo imageInfoIn) {
        this.imageInfo = imageInfoIn;
    }


    @Override
    public Set<Channel> getAssignedChannels() {
        return imageInfo.getChannels();
    }

    @Override
    public Optional<SUSEProductSet> getInstalledProductSet() {
        if (imageInfo.getInstalledProducts().isEmpty()) {
            return Optional.empty();
        }
        else {
            return Optional.of(new SUSEProductSet(imageInfo.getInstalledProducts()));
        }
    }

    @Override
    public ChannelArch getCompatibleChannelArch() {
        return imageInfo.getImageArch().getCompatibleChannelArch();
    }
}
