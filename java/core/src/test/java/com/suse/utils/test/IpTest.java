/*
 * Copyright (c) 2021 SUSE LLC
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
package com.suse.utils.test;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.suse.utils.Ip;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IpTest {

    @Test
    public void testPrefixConversion() throws UnknownHostException {
        assertEquals(24, Ip.netmaskToPrefix("255.255.255.0"));
        assertEquals(17, Ip.netmaskToPrefix("255.255.128.0"));
        assertEquals(64, Ip.netmaskToPrefix("ffff:ffff:ffff:ffff::"));
    }

    @Test
    public void testPrefixToNetmask() throws UnknownHostException {
        assertEquals("255.255.255.0",
                InetAddress.getByAddress(Ip.prefixToNetmask(24, 4)).getHostAddress());
        assertEquals("255.255.128.0",
                InetAddress.getByAddress(Ip.prefixToNetmask(17, 4)).getHostAddress());
    }

    @Test
    public void testGetNetworkAddress() throws UnknownHostException {
        assertEquals("192.168.129.0", Ip.getNetworkAddress("192.168.129.12", 24));
        assertEquals("192.168.128.0", Ip.getNetworkAddress("192.168.129.12", 17));
        assertEquals("2a01:cb10:87cb:4e00:0:0:0:0",
                Ip.getNetworkAddress("2a01:cb10:87cb:4e00:23a0:6566:5ebc:2cff", 64));
    }
}
