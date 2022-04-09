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

import com.redhat.rhn.frontend.dto.ServerPath;

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;


/**
 * ServerPathSerializer: Converts a ServerPathSerializer object for representation as an
 * XMLRPC struct.
 *
 *
 * @xmlrpc.doc
 *  #struct_begin("proxy connection path details")
 *         #prop_desc("int", "position", "Position of proxy in chain. The proxy that the
 *             system connects directly to is listed in position 1.")
 *         #prop_desc("int", "id", "Proxy system id")
 *         #prop_desc("string", "hostname", "Proxy host name")
 *  #struct_end()
 */
public class ServerPathSerializer extends ApiResponseSerializer<ServerPath> {

    @Override
    public Class<ServerPath> getSupportedClass() {
        return ServerPath.class;
    }

    @Override
    public SerializedApiResponse serialize(ServerPath src) {
        return new SerializationBuilder()
                .add("position", src.getPosition())
                .add("id", src.getId())
                .add("hostname", src.getHostname())
                .build();
    }
}
