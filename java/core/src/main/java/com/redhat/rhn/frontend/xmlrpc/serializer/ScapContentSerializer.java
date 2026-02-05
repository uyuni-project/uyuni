/*
 * Copyright (c) 2026 SUSE LLC
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

import com.redhat.rhn.domain.audit.ScapContent;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 * ScapContentSerializer
 *
 * @apidoc.doc
 * #struct_begin("SCAP content information")
 *   #prop_desc("int", "id", "SCAP content ID")
 *   #prop_desc("string", "name", "SCAP content name")
 *   #prop_desc("string", "description", "SCAP content description")
 *   #prop_desc("string", "dataStreamFileName", "DataStream file name")
 *   #prop_desc("string", "xccdfFileName", "XCCDF file name")
 * #struct_end()
 */
public class ScapContentSerializer extends ApiResponseSerializer<ScapContent> {

    @Override
    public Class<ScapContent> getSupportedClass() {
        return ScapContent.class;
    }

    @Override
    public SerializedApiResponse serialize(ScapContent src) {
        return new SerializationBuilder()
                .add("id", src.getId())
                .add("name", src.getName())
                .add("description", src.getDescription())
                .add("dataStreamFileName", src.getDataStreamFileName())
                .add("xccdfFileName", src.getXccdfFileName())
                .build();
    }
}
