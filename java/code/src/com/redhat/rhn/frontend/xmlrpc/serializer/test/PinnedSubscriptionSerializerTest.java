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

package com.redhat.rhn.frontend.xmlrpc.serializer.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.server.PinnedSubscription;
import com.redhat.rhn.frontend.xmlrpc.serializer.PinnedSubscriptionSerializer;

import org.junit.jupiter.api.Test;

import java.io.StringWriter;
import java.io.Writer;

import redstone.xmlrpc.XmlRpcSerializer;

/**
 * PinnedSubscriptionSerializer test
 */
public class PinnedSubscriptionSerializerTest  {

    /**
     * Simple PinnedSubscription serialization test
     */
    @Test
    public void testSerialize() {
        PinnedSubscriptionSerializer serializer = new PinnedSubscriptionSerializer();
        Writer output = new StringWriter();
        PinnedSubscription pinnedSubscription = new PinnedSubscription();
        pinnedSubscription.setId(1L);
        pinnedSubscription.setSubscriptionId(10L);
        pinnedSubscription.setSystemId(100L);
        serializer.serialize(pinnedSubscription, output, new XmlRpcSerializer());

        String actual = output.toString();
        assertTrue(actual.contains("<name>id</name>"));
        assertTrue(actual.contains("<i4>1</i4>"));
        assertTrue(actual.contains("<name>subscription_id</name>"));
        assertTrue(actual.contains("<i4>10</i4>"));
        assertTrue(actual.contains("<name>system_id</name>"));
        assertTrue(actual.contains("<i4>100</i4>"));
    }
}
