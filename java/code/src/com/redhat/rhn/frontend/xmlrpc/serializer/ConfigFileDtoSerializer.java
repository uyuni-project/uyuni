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

import com.redhat.rhn.frontend.dto.ConfigFileDto;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;


/**
 * ConfigFileDtoSerializer
 *
 * @xmlrpc.doc
 * #struct_begin("Configuration File information")
 *   #prop("string", "type")
 *              #options()
 *                  #item("file")
 *                  #item("directory")
 *                  #item("symlink")
 *              #options_end()
 *   #prop_desc("string", "path","File Path")
 *   #prop_desc($date, "last_modified","Last Modified Date")
 * #struct_end()
 */
public class ConfigFileDtoSerializer extends ApiResponseSerializer<ConfigFileDto> {

    @Override
    public Class<ConfigFileDto> getSupportedClass() {
        return ConfigFileDto.class;
    }

    @Override
    public SerializedApiResponse serialize(ConfigFileDto src) {
        return new SerializationBuilder()
                .add("type", src.getType())
                .add("path", src.getPath())
                .add("last_modified", src.getModified())
                .build();
    }
}
