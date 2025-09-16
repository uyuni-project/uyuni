/*
 * Copyright (c) 2024 SUSE LLC
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

import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerNetAddress4;
import com.redhat.rhn.domain.server.ServerNetAddress6;
import com.redhat.rhn.domain.server.ServerNetworkFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

public class ServerNetworkFactoryTest extends BaseTestCaseWithUser {

    @Test
    public void testIPv6Handling() {

        Server s = ServerFactoryTest.createTestServer(user);

        NetworkInterface netint = new NetworkInterface();
        netint.setHwaddr("52:54:00:9e:36:00");
        netint.setModule("test");
        netint.setName("test");
        s.addNetworkInterface(netint);
        netint = TestUtils.saveAndReload(netint);

        ServerNetAddress6 a6 = new ServerNetAddress6();
        a6.setNetmask("64");
        a6.setAddress("2a00:de40:b208:1:5054:ff:fe9e:36e7");
        a6.setScope("global");
        a6.setInterfaceId(netint.getInterfaceId());
        ServerNetworkFactory.saveServerNetAddress6(a6);
        a6 = TestUtils.saveAndReload(a6);

        ArrayList<ServerNetAddress6> iPv6Addresses = netint.getIPv6Addresses();
        assertNotEmpty(iPv6Addresses);
        assertEquals("2a00:de40:b208:1:5054:ff:fe9e:36e7", iPv6Addresses.get(0).getAddress());

        ServerNetworkFactory.removeServerNetAddress6(a6);

        List<ServerNetAddress6> loadedNetAddr6 = ServerNetworkFactory.findServerNetAddress6(netint.getInterfaceId());
        Assertions.assertTrue(loadedNetAddr6.isEmpty(), "IPv6 address not removed");
    }

    @Test
    public void testIPv4Handling() {

        Server s = ServerFactoryTest.createTestServer(user);

        NetworkInterface netint = new NetworkInterface();
        netint.setHwaddr("52:54:00:9e:36:01");
        netint.setModule("test");
        netint.setName("test");
        s.addNetworkInterface(netint);
        netint = TestUtils.saveAndReload(netint);

        ServerNetAddress4 a4 = new ServerNetAddress4();
        a4.setNetmask("255.255.0.0");
        a4.setAddress("1.2.3.4");
        a4.setBroadcast("1.2.255.255");
        a4.setInterfaceId(netint.getInterfaceId());
        ServerNetworkFactory.saveServerNetAddress4(a4);
        a4 = TestUtils.saveAndReload(a4);

        ArrayList<ServerNetAddress4> iPv4Addresses = netint.getIPv4Addresses();
        assertNotEmpty(iPv4Addresses);
        assertEquals("1.2.3.4", iPv4Addresses.get(0).getAddress());

        ServerNetworkFactory.removeServerNetAddress4(a4);

        List<ServerNetAddress4> loadedNetAddr4 = ServerNetworkFactory.findServerNetAddress4(netint.getInterfaceId());
        Assertions.assertTrue(loadedNetAddr4.isEmpty(), "IPv4 address not removed");
    }
}
