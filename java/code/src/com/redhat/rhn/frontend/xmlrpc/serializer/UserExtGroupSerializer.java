/*
 * Copyright (c) 2014 Red Hat, Inc.
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

import com.redhat.rhn.domain.org.usergroup.UserExtGroup;
import com.redhat.rhn.domain.role.Role;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * UserExtGroupSerializer
 *
 * @xmlrpc.doc
 *  #struct_begin("externalGroup")
 *      #prop("string", "name")
 *      #prop_array("roles", "string", "role")
 *  #struct_end()
 *
 */
public class UserExtGroupSerializer extends ApiResponseSerializer<UserExtGroup> {

    @Override
    public Class<UserExtGroup> getSupportedClass() {
        return UserExtGroup.class;
    }

    @Override
    public SerializedApiResponse serialize(UserExtGroup src) {
        List<String> roleList = new ArrayList<>();
        for (Role role : src.getRoles()) {
            roleList.add(role.getLabel());
        }

        return new SerializationBuilder()
                .add("name", src.getLabel())
                .add("roles", roleList)
                .build();
    }
}
