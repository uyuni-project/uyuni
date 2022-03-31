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

import com.suse.manager.api.ApiResponseSerializer;
import com.suse.manager.api.SerializationBuilder;
import com.suse.manager.api.SerializedApiResponse;

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
public class PinnedSubscriptionSerializer extends ApiResponseSerializer<PinnedSubscription> {

    @Override
    public Class<PinnedSubscription> getSupportedClass() {
        return PinnedSubscription.class;
    }

    @Override
    public SerializedApiResponse serialize(PinnedSubscription src) {
        return new SerializationBuilder()
                .add("id", src.getId())
                .add("subscription_id", src.getSubscriptionId())
                .add("system_id", src.getSystemId())
                .build();
    }
}
