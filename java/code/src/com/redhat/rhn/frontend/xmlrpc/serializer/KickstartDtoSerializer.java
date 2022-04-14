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

import com.redhat.rhn.frontend.dto.kickstart.KickstartDto;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;


/**
 * ActivationKeySerializer
 *
 * @xmlrpc.doc
 *   #struct_begin("kickstart")
 *          #prop("string", "label")
 *          #prop("string", "tree_label")
 *          #prop("string", "name")
 *          #prop("boolean", "advanced_mode")
 *          #prop("boolean", "org_default")
 *          #prop("boolean", "active")
 *          #prop("string", "update_type")
 *   #struct_end()
 */
public class KickstartDtoSerializer extends ApiResponseSerializer<KickstartDto> {

    @Override
    public Class<KickstartDto> getSupportedClass() {
        return KickstartDto.class;
    }

    @Override
    public SerializedApiResponse serialize(KickstartDto src) {
        return new SerializationBuilder()
                .add("label", src.getLabel())
                .add("active", src.isActive())
                .add("tree_label", src.getTreeLabel())
                .add("name", src.getLabel())
                .add("advanced_mode", src.isAdvancedMode())
                .add("org_default", src.isOrgDefault())
                .add("update_type", src.getUpdateType())
                .build();
    }
}
