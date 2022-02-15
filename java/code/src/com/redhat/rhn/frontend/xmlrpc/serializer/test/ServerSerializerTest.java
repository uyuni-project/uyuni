/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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

import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.frontend.xmlrpc.serializer.ServerSerializer;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.testing.TestUtils;

import java.io.StringWriter;
import java.io.Writer;

import redstone.xmlrpc.XmlRpcSerializer;


public class ServerSerializerTest extends BaseHandlerTestCase {
    /**
     * Test server of type Normal without machine Id
     * @throws Exception
     */
    public void testSerializeNormalServer() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());
        Writer output = new StringWriter();

        ServerSerializer serializer = new ServerSerializer();
        serializer.serialize(server, output, new XmlRpcSerializer());
        String finalOutput = output.toString();
        commonAssertions(finalOutput, server);
        assertTrue(!finalOutput.contains("machine_id"));

    }
    /**
     * Test server of type salt minion.
     * @throws Exception
     */
    public void testSerializeMinion() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled(), ServerFactoryTest.TYPE_SERVER_MINION);
        Writer output = new StringWriter();

        ServerSerializer serializer = new ServerSerializer();
        serializer.serialize(server, output, new XmlRpcSerializer());
        String finalOutput = output.toString();
        commonAssertions(finalOutput, server);
        assertTrue(finalOutput.contains("machine_id"));
    }
    /**
     * Test server of type Normal with machine Id
     * @throws Exception
     */
    public void testSerializeNormalServerWithMachineId() throws Exception {
        Server server = ServerFactoryTest.createTestServer(admin, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());
        server.setMachineId(TestUtils.randomString());
        Writer output = new StringWriter();

        ServerSerializer serializer = new ServerSerializer();
        serializer.serialize(server, output, new XmlRpcSerializer());
        String finalOutput = output.toString();
        commonAssertions(finalOutput, server);
        assertTrue(finalOutput.contains("machine_id"));

    }
    /**
     * Check assertion which are common between all types of server.
     * @param finalOutput
     * @param server
     */
    public void commonAssertions(String finalOutput, Server server) {
        assertTrue(finalOutput.contains(server.getName()));
        assertTrue(finalOutput.contains(EntitlementManager.ENTERPRISE_ENTITLED));
        assertTrue(finalOutput.contains("addon_entitlements"));
        assertTrue(finalOutput.contains("auto_update"));
        assertTrue(finalOutput.contains("description"));
        assertTrue(finalOutput.contains("address1"));
        assertTrue(finalOutput.contains("address2"));
        assertTrue(finalOutput.contains("city"));
        assertTrue(finalOutput.contains("state"));
        assertTrue(finalOutput.contains("country"));
        assertTrue(finalOutput.contains("building"));
        assertTrue(finalOutput.contains("room"));
        assertTrue(finalOutput.contains("rack"));
        assertTrue(finalOutput.contains("lock_status"));
        assertTrue(finalOutput.contains("contact_method"));
    }

}
