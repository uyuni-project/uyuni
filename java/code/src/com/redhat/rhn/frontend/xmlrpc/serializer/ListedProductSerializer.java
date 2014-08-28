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

import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;
import com.redhat.rhn.manager.content.ListedProduct;

import java.io.IOException;
import java.io.Writer;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 * Serializes ListedProducts.
 *
 * @xmlrpc.doc
 *   #struct("product")
 *     #prop_desc("string", "friendly_name", "Friendly name of the product")
 *     #prop_desc("string", "arch", "Architecture")
 *     #prop_desc("string", "status", "'available' or 'installed'")
 *     #array("channels")
 *       $MgrSyncChannelSerializer
 *     #array_end()
 *     #array("extensions")
 *       $ListedProductSerializer
 *     #array_end()
 *   #struct_end()
 */
public class ListedProductSerializer extends RhnXmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<ListedProduct> getSupportedClass() {
        return ListedProduct.class;
    }

    @Override
    protected void doSerialize(Object obj, Writer output, XmlRpcSerializer serializer)
            throws XmlRpcException, IOException {
        ListedProduct product = (ListedProduct) obj;
        SerializerHelper helper = new SerializerHelper(serializer);

        helper.add("friendly_name", product.getFriendlyName());
        helper.add("arch", product.getArch());
        helper.add("status", product.getStatus().toString().toLowerCase());
        helper.add("channels", product.getChannels());
        helper.add("extensions", product.getExtensions());

        helper.writeTo(output);
    }
}
