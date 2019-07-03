/**
 * Copyright (c) 2019 SUSE LLC
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

import com.redhat.rhn.domain.contentmgmt.ContentProjectFilter;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

import java.io.IOException;
import java.io.Writer;

/**
 * Serializer for {@link ContentProjectFilter}
 * Also serializes the information about the associated {@link ContentFilter}
 *
 * @xmlrpc.doc
 * #struct("Assigned Content Filter information")
 *   #prop("string", "state")
 *   $ContentFilterSerializer
 * #struct_end()
 */
public class ContentProjectFilterSerializer extends RhnXmlRpcCustomSerializer {

    @Override
    public Class getSupportedClass() {
        return ContentProjectFilter.class;
    }

    @Override
    protected void doSerialize(Object obj, Writer writer, XmlRpcSerializer serializer)
            throws XmlRpcException, IOException {
        ContentProjectFilter filter = (ContentProjectFilter) obj;
        SerializerHelper helper = new SerializerHelper(serializer);
        helper.add("state", filter.getState());
        helper.add("filter", filter.getFilter());
        helper.writeTo(writer);
    }
}
