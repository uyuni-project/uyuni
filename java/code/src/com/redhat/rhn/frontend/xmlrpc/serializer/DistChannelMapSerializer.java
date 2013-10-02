/**
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

import java.io.IOException;
import java.io.Writer;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

import com.redhat.rhn.domain.channel.DistChannelMap;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;

/**
 *
 * DistChannelMapSerializer
 * @version $Rev$
 *
 * @xmlrpc.doc
 *      #struct("distChannelMap")
 *          #prop_desc("string", "os", "Operationg System")
 *          #prop_desc("string", "release", "OS Relase")
 *          #prop_desc("string", "arch_name", "Channel architecture")
 *          #prop_desc("string", "channel_label", "Channel label")
 *          #prop_desc("string", "org_specific", "'Y' organization specific, 'N' default")
 *     #struct_end()
 */
public class DistChannelMapSerializer extends RhnXmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    public Class getSupportedClass() {
        return DistChannelMap.class;
    }

    /**
     * {@inheritDoc}
     */
    protected void doSerialize(Object value, Writer output, XmlRpcSerializer serializer)
        throws XmlRpcException, IOException {

        DistChannelMap dstChannelMap = (DistChannelMap) value;
        SerializerHelper helper = new SerializerHelper(serializer);

        helper.add("os", dstChannelMap.getOs());
        helper.add("release", dstChannelMap.getRelease());
        helper.add("arch_name", dstChannelMap.getChannelArch().getName());
        helper.add("channel_label", dstChannelMap.getChannel().getLabel());
        helper.add("org_specific", dstChannelMap.getOrg() == null ? "N" : "Y");

        helper.writeTo(output);
    }
}
