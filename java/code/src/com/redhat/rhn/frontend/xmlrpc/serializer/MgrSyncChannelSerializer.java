/**
 * Copyright (c) 2014 SUSE
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SUSE trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate SUSE trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.frontend.xmlrpc.serializer;

import com.redhat.rhn.frontend.xmlrpc.serializer.util.NoNullHashMap;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;

import com.suse.mgrsync.MgrSyncChannel;
import com.suse.mgrsync.MgrSyncProduct;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 * MgrSyncChannelSerializer
 * @version $Rev$
 * @xmlrpc.doc List all channels that are accessible to the organization.
 * @xmlrpc.param #param_desc("string", "sessionKey", "Session token, issued at login")
 * @xmlrpc.returntype
 * #struct("entry")
 *   #prop_desc("string", "name", "The name of the channel")
 *   #prop_desc("string", "target", "Distribution target")
 *   #prop_desc("string", "description", "Description of the channel")
 *   #prop_desc("string", "url", "URL of the repository")
 *   #prop_desc("string", "status", "Status: available, unavailable or installed")
 *   #prop_desc("string", "arch", "Architecture of the product")
 *   #prop_desc("string", "family", "Product family")
 *   #prop_desc("string", "label", "Label of the product")
 *   #prop_desc("string", "parent", "Parent product")
 *   #prop_desc("string", "product_name", "Product name")
 *   #prop_desc("string", "product_version", "Product version")
 *   #prop_desc("string", "summary", "Summary")
 *   #prop_desc("string", "update_tag", "Update tag")
 *   #struct("distribution")
 *     #prop_desc("string", "os", "OS name")
 *     #prop_desc("string", "release", "OS release")
 *   #struct_end()
 * #struct_end()
 */
public class MgrSyncChannelSerializer extends RhnXmlRpcCustomSerializer {

    @Override
    public Class getSupportedClass() {
        return MgrSyncChannel.class;
    }

    /**
     * Turn to the {@link Map} an instance of {@link MgrSyncChannel} object.
     *
     * @param channel
     * @return {@link Map}
     */
    private Map<String, Object> serializeChannel(MgrSyncChannel channel) {
        Map<String, Object> data = new NoNullHashMap<String, Object>();
        data.put("name", channel.getName());
        data.put("target", channel.getFamily());
        data.put("description", channel.getDescription());
        data.put("url", channel.getSourceUrl());
        data.put("status", channel.getStatus());
        data.put("arch", channel.getArch());
        data.put("family", channel.getFamily());
        data.put("label", channel.getLabel());
        data.put("parent", channel.getParent());
        data.put("product_name", channel.getProductName());
        data.put("product_version", channel.getProductVersion());
        data.put("summary", channel.getSummary());
        data.put("update_tag", channel.getUpdateTag());

        // Add distro
        Map<String, Object> dist = new NoNullHashMap<String, Object>();
        if (channel.getDistribution() != null) {
            dist.put("os", channel.getDistribution().getOs());
            dist.put("release", channel.getDistribution().getRelease());
        } else {
            dist.put("os", null);
            dist.put("release", null);
        }
        data.put("distribution", dist);

        return data;
    }

    /**
     * Turn to the {@link Map} an instance of {@link MgrSyncProduct} object.
     *
     * @param product
     * @return {@link Map}
     */
    private Map<String, Object> serializeProduct(MgrSyncProduct product) {
        Map<String, Object> data = new NoNullHashMap<String, Object>();
        data.put("id", product.getId());
        data.put("name", product.getName());
        data.put("version", product.getVersion());

        return data;
    }


    @Override
    protected void doSerialize(Object obj, Writer writer, XmlRpcSerializer serializer)
            throws XmlRpcException, IOException {
        MgrSyncChannel channel = (MgrSyncChannel) obj;
        SerializerHelper helper = this.serializeMap(this.serializeChannel(channel),
                                                    serializer);
        // Add child products
        List<Map<String, Object>> products = new ArrayList<Map<String, Object>>();
        for (MgrSyncProduct product : channel.getProducts()) {
            products.add(this.serializeProduct(product));
        }
        helper.add("products", products);

        helper.writeTo(writer);
    }
}
