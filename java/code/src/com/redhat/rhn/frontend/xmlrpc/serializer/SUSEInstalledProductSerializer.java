/*
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

import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;
import com.redhat.rhn.frontend.xmlrpc.system.SUSEInstalledProduct;

import java.io.IOException;
import java.io.Writer;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 * Converts an InstalledProduct object for representation as an XMLRPC struct.
 *
 * @xmlrpc.doc
 * #struct_begin("installed product")
 *     #prop("string", "name")
 *     #prop("boolean", "isBaseProduct")
 *     #prop_desc("string", "version", "returned only if applies")
 *     #prop_desc("string", "arch", "returned only if applies")
 *     #prop_desc("string", "release", "returned only if applies")
 *     #prop_desc("string", "friendlyName", "returned only if available")
 * #struct_end()
 *
 */
public class SUSEInstalledProductSerializer extends RhnXmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class getSupportedClass() {
        return SUSEInstalledProduct.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doSerialize(Object value, Writer output, XmlRpcSerializer serializer)
            throws XmlRpcException, IOException {

        SUSEInstalledProduct product = (SUSEInstalledProduct) value;
        SerializerHelper helper = new SerializerHelper(serializer);

        helper.add("name", product.getName());
        helper.add("isBaseProduct", product.isBaseproduct());

        if (product.getVersion() != null) {
            helper.add("version", product.getVersion());
        }
        if (product.getArch() != null) {
            helper.add("arch", product.getArch());
        }
        if (product.getRelease() != null) {
            helper.add("release", product.getRelease());
        }
        if (product.getFriendlyName() != null) {
            helper.add("friendlyName", product.getFriendlyName());
        }

        helper.writeTo(output);
    }
}
