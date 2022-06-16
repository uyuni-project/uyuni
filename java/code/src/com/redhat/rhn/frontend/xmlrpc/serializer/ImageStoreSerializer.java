/**
 * Copyright (c) 2017 SUSE LLC
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

import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;


/**
 * ImageStoreSerializer
 *
 * @xmlrpc.doc
 * #struct_begin("image store information")
 *   #prop("string", "label")
 *   #prop("string", "uri")
 *   #prop("string", "storetype")
 *   #prop("boolean", "hasCredentials")
 *   #prop("string", "username")
 * #struct_end()
 */
public class ImageStoreSerializer extends RhnXmlRpcCustomSerializer {

    /**
     *
     * {@inheritDoc}
     */
    protected void doSerialize(Object value, Writer output, XmlRpcSerializer serializer)
       throws XmlRpcException, IOException {
        SerializerHelper helper = new SerializerHelper(serializer);
        ImageStore store = (ImageStore) value;
        helper.add("label", store.getLabel());
        helper.add("uri", store.getUri());
        helper.add("storetype", store.getStoreType().getLabel());
        helper.add("hasCredentials", (store.getCreds() != null));
        helper.add("username", store.getCreds() != null ?
                store.getCreds().getUsername() : "");
        helper.writeTo(output);
    }

    /**
     * {@inheritDoc}
     */
    public Class getSupportedClass() {
        return ImageStore.class;
    }
}
