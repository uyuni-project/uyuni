/*
 * Copyright (c) 2010--2012 Red Hat, Inc.
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

import com.redhat.rhn.frontend.dto.NetworkDto;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Writer;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;


/**
 * NetworkDtoSerializer
 *
 * @xmlrpc.doc
 *
 * #struct_begin("system")
 *      #prop("int", "systemId")
 *      #prop("string", "systemName")
 *      #prop_desc("dateTime.iso8601",  "last_checkin", "Last time server
 *              successfully checked in")
 * #struct_end()
 */
public class NetworkDtoSerializer extends RhnXmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    public Class getSupportedClass() {
        return NetworkDto.class;
    }

    /** {@inheritDoc} */
    protected void doSerialize(Object value, Writer output, XmlRpcSerializer serializer)
        throws XmlRpcException, IOException {
        NetworkDto net = (NetworkDto)value;
        SerializerHelper helper = new SerializerHelper(serializer);
        helper.add("systemId", net.getId());
        String name = net.getName();
        if (StringUtils.isBlank(name)) {
            name = "unknown";
        }
        helper.add("systemName", name);
        helper.add("last_checkin", net.getLastCheckin());
        helper.writeTo(output);
    }

}
