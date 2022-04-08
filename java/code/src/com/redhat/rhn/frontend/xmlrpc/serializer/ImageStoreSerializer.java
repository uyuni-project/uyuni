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

import com.redhat.rhn.domain.image.ImageStore;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;


/**
 * ImageStoreSerializer
 *
 * @xmlrpc.doc
 * #struct_begin("Image Store information")
 *   #prop("string", "label")
 *   #prop("string", "uri")
 *   #prop("string", "storetype")
 *   #prop("boolean", "hasCredentials")
 *   #prop("string", "username")
 * #struct_end()
 */
public class ImageStoreSerializer extends ApiResponseSerializer<ImageStore> {

    @Override
    public Class<ImageStore> getSupportedClass() {
        return ImageStore.class;
    }

    @Override
    public SerializedApiResponse serialize(ImageStore src) {
        return new SerializationBuilder()
                .add("label", src.getLabel())
                .add("uri", src.getUri())
                .add("storetype", src.getStoreType().getLabel())
                .add("hasCredentials", src.getCreds() != null)
                .add("username", src.getCreds() != null ? src.getCreds().getUsername() : "")
                .build();
    }
}
