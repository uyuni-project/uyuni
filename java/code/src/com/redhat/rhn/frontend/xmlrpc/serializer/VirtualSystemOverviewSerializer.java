/*
 * Copyright (c) 2009--2016 Red Hat, Inc.
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


import com.redhat.rhn.frontend.dto.VirtualSystemOverview;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 *
 * VirtualSystemOverviewSerializer
 *
 * @xmlrpc.doc
 *  #struct_begin("virtual system")
 *      #prop("int", "id")
 *      #prop("string", "name")
 *      #prop_desc("string", "guest_name", "The virtual guest name as provided
 *                  by the virtual host")
 *      #prop_desc("dateTime.iso8601", "last_checkin", "Last time server successfully
 *                   checked in.")
 *      #prop("string", "uuid")
 *   #struct_end()
 *
 */
public class VirtualSystemOverviewSerializer extends ApiResponseSerializer<VirtualSystemOverview> {

    @Override
    public Class<VirtualSystemOverview> getSupportedClass() {
        return VirtualSystemOverview.class;
    }

    @Override
    public SerializedApiResponse serialize(VirtualSystemOverview src) {
        return new SerializationBuilder()
                .add("uuid", src.getUuid())
                .add("id", src.getVirtualSystemId())
                .add("guest_name", src.getName())
                .add("name", src.getServerName())
                .add("last_checkin", src.getLastCheckinDate())
                .build();
    }
}
