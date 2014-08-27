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
package com.redhat.rhn.frontend.xmlrpc.serializer.test;

import com.redhat.rhn.frontend.xmlrpc.serializer.MgrSyncChannelSerializer;

import com.suse.mgrsync.MgrSyncChannel;
import com.suse.mgrsync.MgrSyncStatus;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import junit.framework.TestCase;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcSerializer;

/**
 * Test class to verify {@link MgrSyncChannelSerializer} functionality.
 */
public class MgrSyncChannelSerializerTest extends TestCase {

    public void testSerialize() throws XmlRpcException, IOException {
        // Setup a channel
        MgrSyncChannel channel = new MgrSyncChannel();
        channel.setArch("arch");
        channel.setDescription("description");
        channel.setFamily("family");
        channel.setLabel("label");
        channel.setName("name");
        channel.setOptional(false);
        channel.setParent("parent");
        channel.setProductName("productName");
        channel.setProductVersion("productVersion");
        channel.setSigned(true);
        channel.setSourceUrl("sourceUrl");
        channel.setStatus(MgrSyncStatus.AVAILABLE);
        channel.setSummary("summary");
        channel.setUpdateTag("updateTag");

        // Serialize it
        MgrSyncChannelSerializer serializer = new MgrSyncChannelSerializer();
        Writer output = new StringWriter();
        serializer.serialize(channel, output, new XmlRpcSerializer());

        // Verify
        String actual = output.toString();
        assertTrue(actual.contains("<name>arch</name>"));
        assertTrue(actual.contains("<string>arch</string>"));
        assertTrue(actual.contains("<name>description</name>"));
        assertTrue(actual.contains("<string>description</string>"));
        assertTrue(actual.contains("<name>family</name>"));
        assertTrue(actual.contains("<string>family</string>"));
        assertTrue(actual.contains("<name>is_signed</name>"));
        assertTrue(actual.contains("<boolean>1</boolean>"));
        assertTrue(actual.contains("<name>label</name>"));
        assertTrue(actual.contains("<string>label</string>"));
        assertTrue(actual.contains("<name>name</name>"));
        assertTrue(actual.contains("<string>name</string>"));
        assertTrue(actual.contains("<name>optional</name>"));
        assertTrue(actual.contains("<boolean>0</boolean>"));
        assertTrue(actual.contains("<name>parent</name>"));
        assertTrue(actual.contains("<string>parent</string>"));
        assertTrue(actual.contains("<name>product_name</name>"));  
        assertTrue(actual.contains("<string>productName</string>"));
        assertTrue(actual.contains("<name>product_version</name>"));        
        assertTrue(actual.contains("<string>productVersion</string>"));
        assertTrue(actual.contains("<name>source_url</name>"));
        assertTrue(actual.contains("<string>sourceUrl</string>"));
        assertTrue(actual.contains("<name>status</name>"));
        assertTrue(actual.contains("<string>AVAILABLE</string>"));
        assertTrue(actual.contains("<name>summary</name>"));
        assertTrue(actual.contains("<string>summary</string>"));
        assertTrue(actual.contains("<name>update_tag</name>"));
        assertTrue(actual.contains("<string>updateTag</string>"));
    }
}
