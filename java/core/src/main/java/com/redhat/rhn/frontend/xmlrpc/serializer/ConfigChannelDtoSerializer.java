/*
 * Copyright (c) 2009--2013 Red Hat, Inc.
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

import com.redhat.rhn.domain.config.ConfigChannelType;
import com.redhat.rhn.frontend.dto.ConfigChannelDto;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;


/**
 * ConfigChannelDtoSerializer
 *
 * @apidoc.doc
 * #struct_begin("configuration channel information")
 *   #prop("int", "id")
 *   #prop("int", "orgId")
 *   #prop("string", "label")
 *   #prop("string", "name")
 *   #prop("string", "description")
 *   #prop("string", "type")
 *   #prop("struct", "configChannelType")
 *   $ConfigChannelTypeSerializer
 * #struct_end()
 */
public class ConfigChannelDtoSerializer extends ApiResponseSerializer<ConfigChannelDto> {

    @Override
    public Class<ConfigChannelDto> getSupportedClass() {
        return ConfigChannelDto.class;
    }

    @Override
    public SerializedApiResponse serialize(ConfigChannelDto src) {
        return new SerializationBuilder()
                .add("id", src.getId())
                .add("label", src.getLabel())
                .add("name", src.getName())
                .add("description", src.getDescription())
                .add("orgId", src.getOrgId())
                .add("configChannelType", ConfigChannelType.lookup(src.getType()))
                .build();
    }
}
