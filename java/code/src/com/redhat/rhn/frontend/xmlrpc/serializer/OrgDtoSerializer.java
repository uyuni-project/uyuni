/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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

import com.redhat.rhn.frontend.dto.OrgDto;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;


/**
 * OrgDtoSerializer
 *
 * @xmlrpc.doc
 * #struct_begin("organization info")
 *   #prop("int", "id")
 *   #prop("string", "name")
 *   #prop_desc("int", "active_users", "number of active users in the organization")
 *   #prop_desc("int", "systems", "number of systems in the organization")
 *   #prop_desc("int", "trusts", "number of trusted organizations")
 *   #prop_desc("int", "system_groups",
 *                              "number of system groups in the organization (optional)")
 *   #prop_desc("int", "activation_keys",
 *                              "number of activation keys in the organization (optional)")
 *   #prop_desc("int", "kickstart_profiles",
 *                          "number of kickstart profiles in the organization (optional)")
 *   #prop_desc("int", "configuration_channels",
 *                      "number of configuration channels in the organization (optional)")
 *   #prop_desc("boolean", "staging_content_enabled",
 *                      "is staging content enabled in organization (optional)")
 * #struct_end()
 */
public class OrgDtoSerializer extends ApiResponseSerializer<OrgDto> {

    @Override
    public Class<OrgDto> getSupportedClass() {
        return OrgDto.class;
    }

    @Override
    public SerializedApiResponse serialize(OrgDto src) {
        SerializationBuilder builder = new SerializationBuilder()
                .add("id", src.getId())
                .add("name", src.getName());
        add(builder, "active_users", src.getUsers());
        add(builder, "systems", src.getSystems());
        add(builder, "trusts", src.getTrusts());
        add(builder, "activation_keys", src.getActivationKeys());
        add(builder, "system_groups", src.getServerGroups());
        add(builder, "kickstart_profiles", src.getKickstartProfiles());
        add(builder, "configuration_channels", src.getConfigChannels());
        add(builder, "staging_content_enabled", src.isStagingContentEnabled());
        return builder.build();
    }

    private void add(SerializationBuilder builder, String name, Object value) {
        if (value != null) {
            builder.add(name, value);
        }
    }
}
