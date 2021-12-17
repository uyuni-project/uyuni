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

import com.redhat.rhn.domain.server.PinnedSubscription;
import com.redhat.rhn.frontend.xmlrpc.serializer.util.SerializerHelper;

import java.io.IOException;
import java.io.Writer;

import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 * Serializer for the PinnedSubscription class
 *
 * @xmlrpc.doc
 *  #struct_begin("pinned subscription")
 *      #prop("int", "id")
 *      #prop("int", "subscription_id")
 *      #prop("int", "system_id")
 *  #struct_end()
 */
public class PinnedSubscriptionSerializer extends RhnXmlRpcCustomSerializer {

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<PinnedSubscription> getSupportedClass() {
        return PinnedSubscription.class;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doSerialize(Object obj, Writer writer, XmlRpcSerializer serializer)
            throws XmlRpcException, IOException {
        PinnedSubscription pinnedSubscription = (PinnedSubscription) obj;
        SerializerHelper helper = new SerializerHelper(serializer);
        helper.add("id", pinnedSubscription.getId());
        helper.add("subscription_id", pinnedSubscription.getSubscriptionId());
        helper.add("system_id", pinnedSubscription.getSystemId());
        helper.writeTo(writer);
    }
}
