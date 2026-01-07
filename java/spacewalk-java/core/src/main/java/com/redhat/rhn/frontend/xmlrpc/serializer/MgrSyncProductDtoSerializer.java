/*
 * Copyright (c) 2014--2018 SUSE LLC
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

import com.redhat.rhn.manager.content.MgrSyncProductDto;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * Serializes {@link MgrSyncProductDto}.
 *
 * @apidoc.doc
 *   #struct_begin("product")
 *     #prop_desc("string", "friendly_name", "friendly name of the product")
 *     #prop_desc("string", "arch", "architecture")
 *     #prop_desc("string", "status", "'available', 'unavailable' or 'installed'")
 *     #prop_array_begin("channels")
 *       $MgrSyncChannelDtoSerializer
 *     #array_end()
 *     #prop_array_begin("extensions")
 *       #struct_begin("extension product")
 *         #prop_desc("string", "friendly_name", "friendly name of extension product")
 *         #prop_desc("string", "arch", "architecture")
 *         #prop_desc("string", "status", "'available', 'unavailable' or 'installed'")
 *         #prop_array_begin("channels")
 *           $MgrSyncChannelDtoSerializer
 *         #array_end()
 *       #struct_end()
 *     #array_end()
 *     #prop_desc("boolean", "recommended", "recommended")
 *   #struct_end()
 */
public class MgrSyncProductDtoSerializer extends ApiResponseSerializer<MgrSyncProductDto> {

    @Override
    public Class<MgrSyncProductDto> getSupportedClass() {
        return MgrSyncProductDto.class;
    }

    @Override
    public SerializedApiResponse serialize(MgrSyncProductDto src) {
        return new SerializationBuilder()
                .add("friendly_name", src.getFriendlyName())
                .add("arch", src.getArch().orElse("noarch"))
                .add("status", src.getStatus().toString().toLowerCase())
                .add("channels", src.getChannels())
                .add("extensions", src.getExtensions())
                .add("recommended", src.isRecommended())
                .build();
    }
}
