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

package com.redhat.rhn.domain.server.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.NetworkInterfaceFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.ServerTestUtils;

import java.util.stream.Stream;

public class NetworkInterfaceFactoryTest extends RhnBaseTestCase {

    /**
     * Tests retrieval of network interfaces by their hardware address
     * @throws Exception if anything goes wrong
     */
    public void testLookupNetInterfaceByHwAddress() throws Exception {
        String hwAddress = "aa:bb:cc:dd:ee:00";
        String hwAddressUppercase = hwAddress.toUpperCase();

        Server testSystem = ServerTestUtils.createTestSystem();
        NetworkInterface netInterface = new NetworkInterface();
        netInterface.setName("eth0");
        netInterface.setHwaddr(hwAddress);
        netInterface.setServer(testSystem);
        HibernateFactory.getSession().save(netInterface);

        Stream<NetworkInterface> interfaces = NetworkInterfaceFactory
                .lookupNetworkInterfacesByHwAddress(hwAddressUppercase);

        assertEquals(1, interfaces.count());
    }

}
