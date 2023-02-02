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
package com.redhat.rhn.frontend.xmlrpc.serializer;

import com.redhat.rhn.domain.image.ImageSyncItem;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * ImageSyncItemSerializer
 *
 * @apidoc.doc
 *  #struct_begin("item")
 *      #prop("int", "id")
 *      #prop("string", "repository")
 *      #prop_array("tags", "string", "list of tags")
 *      #prop("string", "tags_regex")
 *  #struct_end()
 */
public class ImageSyncItemSerializer extends ApiResponseSerializer<ImageSyncItem> {

    @Override
    public Class<ImageSyncItem> getSupportedClass() {
        return ImageSyncItem.class;
    }

    @Override
    public SerializedApiResponse serialize(ImageSyncItem src) {
        return new SerializationBuilder()
                .add("id", src.getId().intValue())
                .add("repository", src.getSrcRepository())
                .add("tags", src.getSrcTags())
                .add("tags_regex", src.getSrcTagsRegexp())
                .build();
    }
}
