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
import com.redhat.rhn.domain.product.CachingSUSEProductFactory;
import com.redhat.rhn.domain.product.SUSEProduct;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toList;

/**
 * ImageAuditTarget
 */
public class ImageAuditTarget implements AuditTarget {

    private final ImageInfo imageInfo;

    private final CachingSUSEProductFactory productFactory;

    /**
     * Constructor
     * @param imageInfoIn an image Info object
     * @param productFactoryIn the factory object
     */
    public ImageAuditTarget(ImageInfo imageInfoIn, CachingSUSEProductFactory productFactoryIn) {
        this.imageInfo = imageInfoIn;
        this.productFactory = productFactoryIn;
    }


    @Override
    public Set<Channel> getAssignedChannels() {
        return imageInfo.getChannels();
    }

    @Override
    public List<SUSEProduct> getSUSEProducts() {
        return productFactory.map(imageInfo.getInstalledProducts()).collect(toList());
    }

    @Override
    public ChannelArch getCompatibleChannelArch() {
        return imageInfo.getImageArch().getCompatibleChannelArch();
    }
}
