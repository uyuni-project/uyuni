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

import com.redhat.rhn.frontend.dto.TrustedOrgDto;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * TrustedOrgDtoSerializer is a custom serializer for the XMLRPC library.
 * It converts an TrustedOrgDto to an XMLRPC &lt;struct&gt;.
 * @xmlrpc.doc
 *     #struct_begin("trusted organizations")
 *       #prop("int", "org_id")
 *       #prop("string", "org_name")
 *       #prop("int", "shared_channels")
 *     #struct_end()
 */
public class TrustedOrgDtoSerializer extends ApiResponseSerializer<TrustedOrgDto> {

    @Override
    public Class<TrustedOrgDto> getSupportedClass() {
        return TrustedOrgDto.class;
    }

    @Override
    public SerializedApiResponse serialize(TrustedOrgDto src) {
        return new SerializationBuilder()
                .add("org_id", src.getId())
                .add("org_name", src.getName())
                .add("shared_channels", src.getSharedChannels())
                .build();
    }
}
