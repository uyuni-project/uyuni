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

import com.redhat.rhn.domain.iss.IssSlave;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * IssSlaveSerializer
 *
 * @apidoc.doc
 * #struct_begin("IssSlave info")
 *   #prop("int", "id")
 *   #prop("string", "slave")
 *   #prop("boolean", "enabled")
 *   #prop("boolean", "allowAllOrgs")
 * #struct_end()
 */
public class IssSlaveSerializer extends ApiResponseSerializer<IssSlave> {

    @Override
    public Class<IssSlave> getSupportedClass() {
        return IssSlave.class;
    }

    @Override
    public SerializedApiResponse serialize(IssSlave src) {
        return new SerializationBuilder()
                .add("id", src.getId())
                .add("label", src.getSlave())
                .add("enabled", "Y".equals(src.getEnabled()))
                .add("allowAllOrgs", "Y".equals(src.getAllowAllOrgs()))
                .build();
    }
}
