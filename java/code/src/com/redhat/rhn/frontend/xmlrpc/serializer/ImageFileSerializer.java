/*
 * Copyright (c) 2022 SUSE LLC
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

import com.redhat.rhn.domain.image.ImageFile;
import com.redhat.rhn.domain.image.OSImageStoreUtils;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * ImageFileSerializer
 * @xmlrpc.doc
 * #struct_begin("Image information")
 *   #prop_desc("string", "file", "file name without path")
 *   #prop_desc("string", "type", "file type")
 *   #prop_desc("boolean", "external", "true if the file is external,
 *          false otherwise")
 *   #prop_desc("string", "url", "file url")
 * #struct_end()
 */
public class ImageFileSerializer extends ApiResponseSerializer<ImageFile> {

    @Override
    public Class<ImageFile> getSupportedClass() {
        return ImageFile.class;
    }

    @Override
    public SerializedApiResponse serialize(ImageFile src) {
        return new SerializationBuilder()
                .add("file", src.getFile())
                .add("type", src.getType())
                .add("external", src.isExternal())
                .add("url", OSImageStoreUtils.getOSImageFileURI(src))
                .build();
    }
}
