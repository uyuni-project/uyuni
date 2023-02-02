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

import com.redhat.rhn.frontend.dto.ShortImageSyncProject;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * ShortImageSyncProjectSerializer
 *
 * @apidoc.doc
 *  #struct_begin("project")
 *      #prop("int", "id")
 *      #prop("string", "name")
 *      #prop("string", "src_store_label")
 *      #prop("string", "dest_store_label")
 *      #prop("boolean", "scoped")
 *  #struct_end()
 */
public class ShortImageSyncProjectSerializer extends ApiResponseSerializer<ShortImageSyncProject> {

    @Override
    public Class<ShortImageSyncProject> getSupportedClass() {
        return ShortImageSyncProject.class;
    }

    @Override
    public SerializedApiResponse serialize(ShortImageSyncProject prj) {
        return new SerializationBuilder()
                .add("id", prj.getId().intValue())
                .add("name", prj.getName())
                .add("src_store_label", prj.getSrcStore().getLabel())
                .add("dest_store_label", prj.getDestinationImageStore().getLabel())
                .add("scoped", prj.isScoped())
                .build();
    }
}
