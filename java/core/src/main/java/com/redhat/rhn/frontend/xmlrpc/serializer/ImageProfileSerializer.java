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

import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.token.ActivationKeyFactory;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * ImageProfileSerializer
 *
 * @apidoc.doc
 * #struct_begin("image profile information")
 *   #prop("string", "label")
 *   #prop("string", "imageType")
 *   #prop("string", "imageStore")
 *   #prop("string", "activationKey")
 *   #prop_desc("string", "path", "in case type support path")
 * #struct_end()
 */
public class ImageProfileSerializer extends ApiResponseSerializer<ImageProfile> {

    @Override
    public Class<ImageProfile> getSupportedClass() {
        return ImageProfile.class;
    }

    @Override
    public SerializedApiResponse serialize(ImageProfile src) {
        SerializationBuilder builder = new SerializationBuilder()
                .add("label", src.getLabel())
                .add("imageType", src.getImageType())
                .add("imageStore", src.getTargetStore().getLabel());

        String activationKey = src.getToken() != null ?
                ActivationKeyFactory.lookupByToken(src.getToken()).getKey() : "";
        builder.add("activationKey", activationKey);

        if (src.asDockerfileProfile().isPresent()) {
            builder.add("path", src.asDockerfileProfile().get().getPath());
        }

        return builder.build();
    }
}
