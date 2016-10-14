/**
 * Copyright (c) 2016 SUSE LLC
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

import com.redhat.rhn.frontend.xmlrpc.activationkey.ChannelInfo;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

import java.io.IOException;
import java.io.Writer;

/**
 *
 * ChannelInfoSerializer
 * @version $Rev$
 *
 * @xmlrpc.doc
 *  #struct("channelinfo")
 *      #prop("string", "label")
 *      #prop("string", "name")
 *      #prop("string", "url")
 *      #prop("string", "token")
 *  #struct_end()
 *
 */
public class ChannelInfoSerializer extends RhnXmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    public Class getSupportedClass() {
        return ChannelInfo.class;
    }

    /**
     * {@inheritDoc}
     */
    protected void doSerialize(Object value, Writer output, XmlRpcSerializer serializer)
        throws XmlRpcException, IOException {
        SerializerHelper helper = new SerializerHelper(serializer);
        ChannelInfo c = (ChannelInfo) value;

        helper.add("label", c.getLabel());
        helper.add("name", c.getName());
        helper.add("url", c.getUrl());
        helper.add("token", c.getToken());

        helper.writeTo(output);
    }
}
