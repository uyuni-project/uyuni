/**
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
package com.redhat.rhn.manager.kickstart.test;

import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartIpRange;
import com.redhat.rhn.domain.kickstart.test.KickstartDataTest;
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerNetAddress4;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.manager.kickstart.KickstartManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;


public class KickstartManagerTest extends BaseTestCaseWithUser {



    public void testFindProfileForIpAddress() throws Exception {

        KickstartData ks = KickstartDataTest.createTestKickstartData(user.getOrg());

        KickstartIpRange range = new KickstartIpRange();
        range.setMaxString("192.168.0.255");
        range.setMinString("192.168.0.1");
        range.setKsdata(ks);
        range.setOrg(user.getOrg());
        ks.getIps().add(range);

        Server serv = ServerFactoryTest.createTestServer(user);
        NetworkInterface nic = new NetworkInterface();
        nic.setName("eth0");
        nic.setHwaddr("aa:aa:aa");
        nic.setServer(serv);
        ServerNetAddress4 netAddr = new ServerNetAddress4();
        netAddr.setAddress("192.168.0.122");
        nic.setSa4(netAddr);
        serv.getNetworkInterfaces().add(nic);


        KickstartData result = KickstartManager.getInstance().
                findProfileForServersNetwork(serv);
        assertEquals(ks, result);

    }

    /**
     * Test with  'eth1' instead of eth0
     * @throws Exception something bad happened
     */
    public void testFindProfileForIpAddressEth1() throws Exception {

        KickstartData ks = KickstartDataTest.createTestKickstartData(user.getOrg());

        KickstartIpRange range = new KickstartIpRange();
        range.setMaxString("192.168.0.255");
        range.setMinString("192.168.0.1");
        range.setKsdata(ks);
        range.setOrg(user.getOrg());
        ks.getIps().add(range);

        Server serv = ServerFactoryTest.createTestServer(user);
        NetworkInterface nic = new NetworkInterface();
        nic.setName("eth0");
        nic.setHwaddr("aa:aa:aa");
        nic.setServer(serv);
        serv.getNetworkInterfaces().add(nic);

        nic = new NetworkInterface();
        nic.setName("eth1");
        nic.setHwaddr("aa:aa:aa");
        ServerNetAddress4 netAddr = new ServerNetAddress4();
        netAddr.setAddress("192.168.0.123");
        nic.setSa4(netAddr);
        nic.setServer(serv);
        serv.getNetworkInterfaces().add(nic);



        KickstartData result = KickstartManager.getInstance().
                findProfileForServersNetwork(serv);
        assertEquals(ks, result);

    }

    /*
     * Test where 'eth0' doesn't match anything, but there should be a default
     *  Kickstart set;
     */
    public void testFindProfileForIpAddressDefault() throws Exception {

        KickstartData ks = KickstartDataTest.createTestKickstartData(user.getOrg());

        KickstartData ksDefault = KickstartDataTest.createTestKickstartData(user.getOrg());
        ksDefault.setOrgDefault(true);

        KickstartIpRange range = new KickstartIpRange();
        range.setMaxString("192.168.0.255");
        range.setMinString("192.168.0.1");
        range.setKsdata(ks);
        range.setOrg(user.getOrg());
        ks.getIps().add(range);

        Server serv = ServerFactoryTest.createTestServer(user);
        NetworkInterface nic = new NetworkInterface();
        nic.setName("eth0");
        nic.setHwaddr("aa:aa:aa");
        nic.setServer(serv);
        ServerNetAddress4 netAddr = new ServerNetAddress4();
        netAddr.setAddress("1.2.3.4");
        nic.setSa4(netAddr);
        serv.getNetworkInterfaces().add(nic);


        KickstartData result = KickstartManager.getInstance().
                findProfileForServersNetwork(serv);
        assertEquals(ksDefault, result);

    }


    /*
     * Test where 'eth0' doesn't match anything, resulting in NULL
     */
    public void testFindProfileForIpAddressNull() throws Exception {

        KickstartData ks = KickstartDataTest.createTestKickstartData(user.getOrg());

        KickstartIpRange range = new KickstartIpRange();
        range.setMaxString("192.168.0.255");
        range.setMinString("192.168.0.1");
        range.setKsdata(ks);
        range.setOrg(user.getOrg());
        ks.getIps().add(range);

        Server serv = ServerFactoryTest.createTestServer(user);
        NetworkInterface nic = new NetworkInterface();
        nic.setName("eth0");
        nic.setHwaddr("aa:aa:aa");
        nic.setServer(serv);
        ServerNetAddress4 netAddr = new ServerNetAddress4();
        netAddr.setAddress("1.2.3.4");
        nic.setSa4(netAddr);
        serv.getNetworkInterfaces().add(nic);


        KickstartData result = KickstartManager.getInstance().
                findProfileForServersNetwork(serv);
        assertNull(result);

    }

}
