/**
 * Copyright (c) 2014 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate SUSE trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.frontend.xmlrpc.serializer;

import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;

import com.suse.mgrsync.XMLChannel;

import java.io.IOException;
import java.io.Writer;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 * Serializer for {@link XMLChannel} objects.
 *
 * @xmlrpc.doc
 *   #struct("channel")
 *     #prop_desc("string", "arch", "Architecture of the channel")
 *     #prop_desc("string", "description", "Description of the channel")
 *     #prop_desc("string", "family", "Channel family label")
 *     #prop_desc("boolean", "is_signed", "Channel has signed metadata")
 *     #prop_desc("string", "label", "Label of the channel")
 *     #prop_desc("string", "name", "Name of the channel")
 *     #prop_desc("boolean", "optional", "Channel is optional")
 *     #prop_desc("string", "parent", "The label of the parent channel")
 *     #prop_desc("string", "product_name", "Product name")
 *     #prop_desc("string", "product_version", "Product version")
 *     #prop_desc("string", "source_url", "Repository source URL")
 *     #prop_desc("string", "status", "Status: available, unavailable or installed")
 *     #prop_desc("string", "summary", "Channel summary")
 *     #prop_desc("string", "update_tag", "Update tag")
 *   #struct_end()
 */
public class XMLChannelSerializer extends RhnXmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<XMLChannel> getSupportedClass() {
        return XMLChannel.class;
    }

    @Override
    protected void doSerialize(Object obj, Writer writer, XmlRpcSerializer serializer)
            throws XmlRpcException, IOException {
        XMLChannel channel = (XMLChannel) obj;
        SerializerHelper helper = new SerializerHelper(serializer);
        helper.add("arch", channel.getArch());
        helper.add("description", channel.getDescription());
        helper.add("family", channel.getFamily());
        helper.add("is_signed", channel.isSigned());
        helper.add("label", channel.getLabel());
        helper.add("name", channel.getName());
        helper.add("optional", channel.isOptional());
        helper.add("parent", channel.getParent());
        helper.add("product_name", channel.getProductName());
        helper.add("product_version", channel.getProductVersion());
        helper.add("source_url", channel.getSourceUrl());
        helper.add("status", channel.getStatus().name());
        helper.add("summary", channel.getSummary());
        helper.add("update_tag", channel.getUpdateTag());
        helper.writeTo(writer);
    }
}
