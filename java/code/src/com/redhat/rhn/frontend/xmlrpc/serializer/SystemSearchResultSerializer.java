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

import com.redhat.rhn.frontend.dto.SystemSearchResult;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;
/**
 *
 * SystemSearchResultSerializer
 *
 * @xmlrpc.doc
 *
 * #struct_begin("system")
 *      #prop("int", "id")
 *      #prop("string", "name")
 *      #prop_desc("$date",  "last_checkin", "last time server
 *              successfully checked in")
 *      #prop("string", "hostname")
 *      #prop("string", "ip")
 *      #prop_desc("string",  "hw_description", "HW description if not null")
 *      #prop_desc("string",  "hw_device_id", "HW device id if not null")
 *      #prop_desc("string",  "hw_vendor_id", "HW vendor id if not null")
 *      #prop_desc("string",  "hw_driver", "HW driver if not null")
 * #struct_end()
 *
 */
public class SystemSearchResultSerializer extends ApiResponseSerializer<SystemSearchResult> {

    @Override
    public Class<SystemSearchResult> getSupportedClass() {
        return SystemSearchResult.class;
    }

    @Override
    public SerializedApiResponse serialize(SystemSearchResult src) {
        SerializationBuilder builder = new SerializationBuilder()
                .add("id", src.getId())
                .add("name", src.getName())
                .add("last_checkin", src.getLastCheckinDate())
                .add("hostname", src.getHostname())
                .add("ip", src.getIpaddr());

        if (src.getHw() != null) {
            builder.add("hw_description", src.getHw().getDescription())
                    .add("hw_device_id", src.getHw().getDeviceId())
                    .add("hw_vendor_id", src.getHw().getVendorId())
                    .add("hw_driver", src.getHw().getDriver());
        }
        return builder.build();
    }
}
