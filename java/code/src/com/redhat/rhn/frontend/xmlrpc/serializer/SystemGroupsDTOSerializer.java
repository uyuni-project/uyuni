/*
 * Copyright (c) 2020 SUSE LLC
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


import com.redhat.rhn.domain.dto.SystemGroupID;
import com.redhat.rhn.domain.dto.SystemGroupsDTO;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
*
* SystemGroupsDTOSerializer
*
* @xmlrpc.doc
*
* #struct_begin("system")
*   #prop_desc("int", "id", "system ID")
*   #prop_array_begin("system_groups")
*     #struct_begin("system_group")
*       #prop_desc("int", "id", "system group ID")
*       #prop_desc("string", "name", "system group name")
*     #struct_end()
*   #prop_array_end()
* #struct_end()
*/
public class SystemGroupsDTOSerializer extends ApiResponseSerializer<SystemGroupsDTO> {

    @Override
    public Class<SystemGroupsDTO> getSupportedClass() {
        return SystemGroupsDTO.class;
    }

    @Override
    public SerializedApiResponse serialize(SystemGroupsDTO src) {
        return new SerializationBuilder()
                .add("id", src.getSystemID())
                .add("system_groups", serializeSystemGroups(src.getSystemGroups()))
                .build();
    }

    private List<Map<String, Object>> serializeSystemGroups(List<SystemGroupID> systemGroups) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (SystemGroupID systemGroupID : systemGroups) {
            Map<String, Object> groupInfo = new HashMap<>();
            groupInfo.put("id", systemGroupID.getGroupID());
            groupInfo.put("name", systemGroupID.getGroupName());
            result.add(groupInfo);
        }
        return result;
    }
}
