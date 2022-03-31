/*
 * Copyright (c) 2021 SUSE LLC
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

import com.redhat.rhn.domain.server.ansible.AnsiblePath;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * XMLRPC Serializer for AnsiblePath
 *
 * @xmlrpc.doc
 *   #struct_begin("ansible path")
 *     #prop("int", "path id")
 *     #prop("string", "type label")
 *     #prop("int", "id of the ansible control node system")
 *     #prop("string", "local path to inventory or playbook")
 *   #struct_end()
 */
public class AnsiblePathSerializer extends ApiResponseSerializer<AnsiblePath> {

    @Override
    public Class<AnsiblePath> getSupportedClass() {
        return AnsiblePath.class;
    }

    @Override
    public SerializedApiResponse serialize(AnsiblePath src) {
        return new SerializationBuilder()
                .add("id", src.getId())
                .add("type", src.getEntityType().getLabel())
                .add("server_id", src.getMinionServer().getId())
                .add("path", src.getPath().toString())
                .build();
    }
}
