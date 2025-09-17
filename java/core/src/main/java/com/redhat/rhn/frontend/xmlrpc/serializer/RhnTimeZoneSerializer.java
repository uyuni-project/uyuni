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

import com.redhat.rhn.domain.user.RhnTimeZone;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * RhnTimeZoneSerializer will serialize an RhnTimeZone object to XMLRPC
 * syntax.
 *
 * @apidoc.doc
 *
 * #struct_begin("timezone")
 *   #prop_desc("int", "time_zone_id", "unique identifier for timezone")
 *   #prop_desc("string", "olson_name", "name as identified by the Olson database")
 * #struct_end()
 */
public class RhnTimeZoneSerializer extends ApiResponseSerializer<RhnTimeZone> {

    @Override
    public Class<RhnTimeZone> getSupportedClass() {
        return RhnTimeZone.class;
    }

    @Override
    public SerializedApiResponse serialize(RhnTimeZone src) {
        return new SerializationBuilder()
                .add("time_zone_id", src.getTimeZoneId())
                .add("olson_name", src.getOlsonName())
                .build();
    }
}
