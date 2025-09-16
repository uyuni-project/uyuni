/*
 * Copyright (c) 2018 SUSE LLC
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

import com.redhat.rhn.frontend.dto.EmptySystemProfileOverview;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

import java.util.Date;


/**
 *
 * EmptySystemProfileSerializer
 *
 * @apidoc.doc
 * #struct_begin("system")
 *     #prop("int", "id")
 *     #prop("string", "name")
 *     #prop_desc("$date",  "created", "Server creation time")
 *     #prop_array("hw_address", "string", "HW address")
 * #struct_end()
 */
public class EmptySystemProfileSerializer extends ApiResponseSerializer<EmptySystemProfileOverview> {

    @Override
    public Class<EmptySystemProfileOverview> getSupportedClass() {
        return EmptySystemProfileOverview.class;
    }

    @Override
    public SerializedApiResponse serialize(EmptySystemProfileOverview src) {
        SerializationBuilder builder = new SerializationBuilder()
                .add("id", src.getId())
                .add("name", src.getName());
        Date regDate = src.getCreated();
        if (regDate != null) {
            builder.add("created", regDate);
        }
        builder.add("hw_addresses", src.getMacs());
        return builder.build();
    }
}
