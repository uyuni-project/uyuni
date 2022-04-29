/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
import com.redhat.rhn.frontend.dto.ConfigFileDto;
import com.redhat.rhn.frontend.dto.ConfigFileNameDto;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;


/**
 * ConfigFileDtoSerializer
 * @xmlrpc.doc
 * #struct_begin("configuration file information")
 *   #prop("string", "type")
 *              #options()
 *                  #item("file")
 *                  #item("directory")
 *                  #item("symlink")
 *              #options_end()
 *   #prop_desc("string", "path", "File Path")
 *   #prop_desc("string", "channel_label",
 *      "the label of the  central configuration channel
 *      that has this file. Note this entry only shows up
 *      if the file has not been overridden by a central channel.")
 *   #prop("struct", "channel_type")
 *   $ConfigChannelTypeSerializer
 *   #prop_desc($date, "last_modified","Last Modified Date")
 * #struct_end()
 */
public class ConfigFileNameDtoSerializer extends ApiResponseSerializer<ConfigFileNameDto> {

    @Override
    public Class<ConfigFileNameDto> getSupportedClass() {
        return ConfigFileNameDto.class;
    }

    @Override
    public SerializedApiResponse serialize(ConfigFileNameDto src) {
        SerializationBuilder builder = new SerializationBuilder();
        builder.add("type", src.getConfigFileType());
        builder.add("path", src.getPath());
        ConfigChannelType type = ConfigChannelType.lookup(src.getConfigChannelType());
        builder.add("channel_type", type);
        if (type.equals(ConfigChannelType.normal())) {
            builder.add("channel_label", src.getConfigChannelLabel());
        }
        builder.add("last_modified", src.getLastModifiedDate());
        return builder.build();
    }

    /**
     * Basically creates ConfigFileNameDto and populates the
     *  appropriate fields from the ConfigFileDto.. This
     *  is here and NOT in ConfigFileDto because
     *  the fields we will be populating here
     *  must match with what we want when we serialize.
     * in ConfigFileName
     * @param dto configle file dto
     * @param configChannelType the config channel type
     * @param configChannelLabel the config chanel label
     * @return ConfigFileNameDto
     */
    public static ConfigFileNameDto toNameDto(ConfigFileDto dto,
                                        String configChannelType,
                                        String configChannelLabel) {
        ConfigFileNameDto nameDto = new ConfigFileNameDto();
        nameDto.setConfigFileType(dto.getType());
        nameDto.setConfigChannelType(configChannelType);
        nameDto.setConfigChannelLabel(configChannelLabel);
        nameDto.setPath(dto.getPath());
        nameDto.setLastModifiedDate(dto.getModified());
        return nameDto;
    }
}
