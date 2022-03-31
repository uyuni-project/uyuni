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

import com.redhat.rhn.domain.server.ManagedServerGroup;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;


/**
 * ManagedServerGroupSerializer is a custom serializer for the XMLRPC library.
 * It converts an ServerGroup to an XMLRPC &lt;struct&gt;.
 * @xmlrpc.doc
 *      #struct_begin("Server Group")
 *          #prop("int", "id")
 *          #prop("string", "name")
 *          #prop("string", "description")
 *          #prop("int", "org_id")
 *          #prop("int", "system_count")
 *      #struct_end()
 *
 */
public class ManagedServerGroupSerializer extends ApiResponseSerializer<ManagedServerGroup> {

    public static final String CURRENT_MEMBERS = "system_count";

    @Override
    public Class<ManagedServerGroup> getSupportedClass() {
        return ManagedServerGroup.class;
    }

    /**
     * Converts a ServerGroup to a {@link SerializedApiResponse} containing the top-level fields of the ServerGroup.
     * It serializes the Org as just an ID instead of traversing the entire object graph.
     * @param src the ServerGroup object
     */
    @Override
    public SerializedApiResponse serialize(ManagedServerGroup src) {
        return new SerializationBuilder()
                .add("id", src.getId())
                .add("name", src.getName())
                .add("description", src.getDescription())
                .add(CURRENT_MEMBERS, src.getCurrentMembers())
                .add("org_id", src.getOrg().getId())
                .build();
    }
}
