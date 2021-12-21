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
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

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
public class SystemGroupsDTOSerializer extends RhnXmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class getSupportedClass() {
        return SystemGroupsDTO.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doSerialize(Object value, Writer output, XmlRpcSerializer serializer)
        throws XmlRpcException, IOException {
        SystemGroupsDTO systemGroupsDTO = (SystemGroupsDTO) value;
        SerializerHelper helper = new SerializerHelper(serializer);
        helper.add("id", systemGroupsDTO.getSystemID());
        helper.add("system_groups", serializeSystemGroups(systemGroupsDTO.getSystemGroups()));

        helper.writeTo(output);
    }

    private List<Map<String, Object>> serializeSystemGroups(List<SystemGroupID> systemGroups) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (SystemGroupID systemGroupID : systemGroups) {
            Map<String, Object> groupInfo = new HashMap<String, Object>();
            groupInfo.put("id", systemGroupID.getGroupID());
            groupInfo.put("name", systemGroupID.getGroupName());
            result.add(groupInfo);
        }
        return result;
    }
}
