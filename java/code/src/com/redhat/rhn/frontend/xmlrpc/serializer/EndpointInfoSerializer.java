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


import com.redhat.rhn.domain.dto.EndpointInfo;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

/**
 *
 * EndpointInfoSerializer
 *
 * @xmlrpc.doc
 *
 * #struct_begin("endpoint info")
 *   #prop("int", "system_id")
 *   #prop("string", "endpoint_name")
 *   #prop("string", "exporter_name")
 *   #prop("string", "module")
 *   #prop("string", "path")
 *   #prop("int", "port")
 *   #prop("bool", "tls_enabled")
 * #struct_end()
 */
public class EndpointInfoSerializer extends ApiResponseSerializer<EndpointInfo> {

    @Override
    public Class<EndpointInfo> getSupportedClass() {
        return EndpointInfo.class;
    }

    @Override
    public SerializedApiResponse serialize(EndpointInfo src) {
        return new SerializationBuilder()
                .add("system_id", src.getSystemID())
                .add("endpoint_name", src.getEndpointName())
                .add("exporter_name", src.getExporterName().orElse(null))
                .add("module", src.getModule())
                .add("path", src.getPath())
                .add("port", src.getPort())
                .add("tls_enabled", src.isTlsEnabled())
                .build();
    }
}
