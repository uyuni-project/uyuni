/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.frontend.xmlrpc.serializer;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;
import com.suse.manager.model.hub.ServerInfoJson;

/**
 * Converts a ServerInfoJson to an XMLRPC &lt;struct&gt;.
 *
 * @apidoc.doc
 *  #struct_begin("server info")
 *      #prop_desc("boolean", "isHub", "true if server is a hub")
 *      #prop_desc("boolean", "isPeripheral", "true if server is a peripheral")
 *  #struct_end()
 */
public class ServerInfoJsonSerializer extends ApiResponseSerializer<ServerInfoJson> {

    @Override
    public Class<ServerInfoJson> getSupportedClass() {
        return ServerInfoJson.class;
    }

    @Override
    public SerializedApiResponse serialize(ServerInfoJson src) {
        return new SerializationBuilder()
                .add("isHub", src.isHub())
                .add("isPeripheral", src.isPeripheral())
                .build();
    }
}
