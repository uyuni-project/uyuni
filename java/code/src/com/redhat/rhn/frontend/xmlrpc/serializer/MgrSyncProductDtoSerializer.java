/**
 * Copyright (c) 2014--2018 SUSE LLC
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

import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;
import com.redhat.rhn.manager.content.MgrSyncProductDto;

import java.io.IOException;
import java.io.Writer;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 * Serializes {@link MgrSyncProductDto}.
 *
 * @xmlrpc.doc
 *   #struct("product")
 *     #prop_desc("string", "friendly_name", "Friendly name of the product")
 *     #prop_desc("string", "arch", "Architecture")
 *     #prop_desc("string", "status", "'available', 'unavailable' or 'installed'")
 *     #array()
 *       $MgrSyncChannelDtoSerializer
 *     #array_end()
 *     #array()
 *       #struct("extension product")
 *         #prop_desc("string", "friendly_name", "Friendly name of extension product")
 *         #prop_desc("string", "arch", "Architecture")
 *         #prop_desc("string", "status", "'available', 'unavailable' or 'installed'")
 *         #array()
 *           $MgrSyncChannelDtoSerializer
 *         #array_end()
 *       #struct_end()
 *     #array_end()
 *     #prop_desc("bool", "recommended", "Recommended")
 *   #struct_end()
 */
public class MgrSyncProductDtoSerializer extends RhnXmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<MgrSyncProductDto> getSupportedClass() {
        return MgrSyncProductDto.class;
    }

    @Override
    protected void doSerialize(Object obj, Writer output, XmlRpcSerializer serializer)
            throws XmlRpcException, IOException {
        MgrSyncProductDto product = (MgrSyncProductDto) obj;
        SerializerHelper helper = new SerializerHelper(serializer);

        helper.add("friendly_name", product.getFriendlyName());
        helper.add("arch", product.getArch().orElse("noarch"));
        helper.add("status", product.getStatus().toString().toLowerCase());
        helper.add("channels", product.getChannels());
        helper.add("extensions", product.getExtensions());
        helper.add("recommended", product.isRecommended());

        helper.writeTo(output);
    }
}
