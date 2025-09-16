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

import com.redhat.rhn.frontend.dto.FilePreservationDto;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * FilePreservationDtoSerializer
 *
 * @apidoc.doc
 *   #struct_begin("file preservation")
 *      #prop("int", "id")
 *      #prop("string", "name")
 *      #prop("$date", "created")
 *      #prop("$date", "last_modified")
 *   #struct_end()
 */
public class FilePreservationDtoSerializer extends ApiResponseSerializer<FilePreservationDto> {

    @Override
    public Class<FilePreservationDto> getSupportedClass() {
        return FilePreservationDto.class;
    }

    @Override
    public SerializedApiResponse serialize(FilePreservationDto src) {
        return new SerializationBuilder()
                .add("name", src.getLabel())
                .add("id", src.getId())
                .add("created", src.getCreated())
                .add("last_modified", src.getModified())
                .build();
    }
}
