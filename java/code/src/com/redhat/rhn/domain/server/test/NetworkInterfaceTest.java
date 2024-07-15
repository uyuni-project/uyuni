/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.domain.server.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerNetAddress4;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.hibernate.Session;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Date;

/**
 * NetworkInterfaceTest
 */
public class NetworkInterfaceTest extends RhnBaseTestCase {

    public static final String TEST_MAC = "AA:AA:BB:BB:CC:CC";
    /**
     * Test the equals method for NetworkInterface.
     * @throws Exception something bad happened
     */
    @Test
    public void testEquals() throws Exception {
        NetworkInterface netint1 = createTestNetworkInterface();
        NetworkInterface netint2 = new NetworkInterface();

        assertNotEquals(netint1, netint2);
        assertNotEquals(netint1, new Date());

        Session session = HibernateFactory.getSession();
        netint2 = (NetworkInterface) session.getNamedQuery("NetworkInterface.lookup")
                                            .setParameter("server", netint1.getServer())
                                            .setParameter("name", netint1.getName())
                                            .uniqueResult();

        assertEquals(netint1, netint2);
    }

    /**
     * Test NetworkInterface.isContainerNetwork
     * @throws Exception something bad happened
     */
    @Test
    public void testIsContainerNetwork() throws Exception {
        NetworkInterface netint1 = createTestNetworkInterface();
        assertFalse(netint1.isContainerNetwork());
        netint1.setModule("bridge");
        netint1.setName("docker0");
        assertTrue(netint1.isContainerNetwork());
        netint1.setModule("bridge");
        netint1.setName("cni-podman0");
        assertTrue(netint1.isContainerNetwork());
    }

    /**
     * Creates a test NetworkInterface object
     * @return Returns a new NetworkInterface object all filled out for testing purposes.
     * @throws Exception something bad happened
     */
    public static NetworkInterface createTestNetworkInterface() throws Exception {
        User user = UserTestUtils.findNewUser("testuser", "testorg");
        Server s = ServerFactoryTest.createTestServer(user);
        return createTestNetworkInterface(s);
    }

    /**
     * Creates a test NetworkInterface object
     * @param server The server to associate with this network interface
     * @return Returns a new NetworkInterface object all filled out for testing purposes.
     * @throws Exception something bad happened
     */
    public static NetworkInterface createTestNetworkInterface(Server server)
    throws Exception {
        return createTestNetworkInterface(server, TestUtils.randomString(),
                "127.0.0.1", TEST_MAC);
    }

    /**
     * Creates a test NetworkInterface object
     * @param server The server to associate with this network interface
     * @param networkName the network name
     * @param ipAddress the ip address
     * @param macAddress the MAC address
     * @return Returns a new NetworkInterface object all filled out for testing purposes.
     */
    public static NetworkInterface createTestNetworkInterface(Server server,
            String networkName, String ipAddress, String macAddress) {

        NetworkInterface netint = new NetworkInterface();
        netint.setHwaddr(macAddress);
        netint.setModule("test");
        netint.setName(networkName);
        ServerNetAddress4 netAddr = new ServerNetAddress4();
        netAddr.setAddress(ipAddress);
        ArrayList<ServerNetAddress4> salist = new ArrayList<>();
        salist.add(netAddr);
        netint.setSa4(salist);
        server.addNetworkInterface(netint);
        netint = (NetworkInterface) TestUtils.saveAndReload(netint);
        netAddr.setInterfaceId(netint.getInterfaceId());
        TestUtils.saveAndFlush(netAddr);
        return netint;
    }
}
