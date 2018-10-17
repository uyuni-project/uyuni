/**
 * Copyright (c) 2018 SUSE LLC
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

import com.redhat.rhn.frontend.dto.EmptySystemProfileOverview;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SystemSerializerUtils;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

import java.io.IOException;
import java.io.Writer;


/**
 *
 * EmptySystemProfileSerializer
 *
 * @xmlrpc.doc
 *
 * #struct("system")
 *     #prop("int", "id")
 *     #prop("string", "name")
 *     #prop_desc("dateTime.iso8601",  "last_checkin", "Last time server
 *             successfully checked in")
 *     #prop_desc("dateTime.iso8601",  "created", "Server registration time")
 *     #prop_desc("dateTime.iso8601",  "last_boot", "Last server boot time")
 *     #prop_desc("int",  "extra_pkg_count", "Number of packages not belonging
 *             to any assigned channel")
 *     #prop_desc("int",  "outdated_pkg_count", "Number of out-of-date packages")
 *     #array_single("string", "hw_addresses")
 * #struct_end()
 */
public class EmptySystemProfileSerializer extends RhnXmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    public Class getSupportedClass() {
        return EmptySystemProfileOverview.class;
    }

    /**
     * {@inheritDoc}
     */
    protected void doSerialize(Object value, Writer output, XmlRpcSerializer serializer)
            throws XmlRpcException, IOException {
        EmptySystemProfileOverview system = (EmptySystemProfileOverview) value;
        SerializerHelper helper = new SerializerHelper(serializer);
        SystemSerializerUtils.serializeSystemOverview(system, helper);
        helper.add("hw_addresses", system.getMacs());
        helper.writeTo(output);
    }
}
