/**
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
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;

import java.io.IOException;
import java.io.Writer;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

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
public class EndpointInfoSerializer extends RhnXmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class getSupportedClass() {
        return EndpointInfo.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doSerialize(Object obj, Writer writer, XmlRpcSerializer serializer)
            throws XmlRpcException, IOException {
        EndpointInfo endpointInfo = (EndpointInfo) obj;
        SerializerHelper helper = new SerializerHelper(serializer);
        helper.add("system_id", endpointInfo.getSystemID());
        helper.add("endpoint_name", endpointInfo.getEndpointName());
        helper.add("exporter_name", endpointInfo.getExporterName().orElse(null));
        helper.add("module", endpointInfo.getModule());
        helper.add("path", endpointInfo.getPath());
        helper.add("port", endpointInfo.getPort());
        helper.add("tls_enabled", endpointInfo.isTlsEnabled());

        helper.writeTo(writer);
    }
}
