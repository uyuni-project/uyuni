/*
 * Copyright (c) 2010--2012 Red Hat, Inc.
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

import com.redhat.rhn.frontend.dto.NetworkDto;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

import org.apache.commons.lang3.StringUtils;


/**
 * NetworkDtoSerializer
 *
 * @xmlrpc.doc
 *
 * #struct_begin("system")
 *      #prop("int", "systemId")
 *      #prop("string", "systemName")
 *      #prop_desc("$date", "last_checkin", "last time the server successfully checked in")
 * #struct_end()
 */
public class NetworkDtoSerializer extends ApiResponseSerializer<NetworkDto> {

    @Override
    public Class<NetworkDto> getSupportedClass() {
        return NetworkDto.class;
    }

    @Override
    public SerializedApiResponse serialize(NetworkDto src) {
        return new SerializationBuilder()
                .add("systemId", src.getId())
                .add("systemName", StringUtils.defaultString(src.getName(), "unknown"))
                .add("last_checkin", src.getLastCheckin())
                .build();
    }
}
