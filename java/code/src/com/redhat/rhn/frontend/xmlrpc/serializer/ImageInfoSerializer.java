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
package com.redhat.rhn.frontend.xmlrpc.serializer;

import com.redhat.rhn.domain.common.Checksum;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageStore;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * ImageInfoSerializer
 * @xmlrpc.doc
 * #struct_begin("image information")
 *   #prop("int", "id")
 *   #prop_desc("string", "name", "image name")
 *   #prop_desc("string", "version", "image tag/version")
 *   #prop_desc("int", "revision", "image build revision number")
 *   #prop_desc("string", "arch", "image architecture")
 *   #prop_desc("boolean", "external", "true if the image is built externally,
 *          false otherwise")
 *   #prop("string", "storeLabel")
 *   #prop("string", "checksum")
 *   #prop("string", "obsolete")
 * #struct_end()
 */
public class ImageInfoSerializer extends ApiResponseSerializer<ImageInfo> {

    @Override
    public Class<ImageInfo> getSupportedClass() {
        return ImageInfo.class;
    }

    @Override
    public SerializedApiResponse serialize(ImageInfo src) {
        Checksum chk = src.getChecksum();
        ImageStore store = src.getStore();
        return new SerializationBuilder()
                .add("id", src.getId())
                .add("name", src.getName())
                .add("type", src.getImageType())
                .add("version", src.getVersion())
                .add("revision", src.getRevisionNumber())
                .add("arch", src.getImageArch().getLabel())
                .add("external", src.isExternalImage())
                .add("storeLabel", store != null ? store.getLabel() : "")
                .add("checksum", chk != null ? chk.getChecksum() : "")
                .add("obsolete", src.isObsolete())
                .build();
    }
}
