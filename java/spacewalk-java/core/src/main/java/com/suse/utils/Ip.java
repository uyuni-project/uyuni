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
package com.suse.utils;

import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * Ip addresses computation utilities
 */
public class Ip {

    private Ip() {
    }

    /**
     * Convert a netmask into the corresponding prefix number
     *
     * @param mask the netmask as a string, e.g. "255.255.255.0"
     * @return the prefix, e.g. 24
     *
     * @throws UnknownHostException if the netmask can't be parsed to an IP address
     */
    public static int netmaskToPrefix(String mask) throws UnknownHostException {
        int prefix = 0;
        InetAddress addr = InetAddress.getByName(mask);
        byte[] data = addr.getAddress();
        for (byte b : data) {
            prefix += countSetBits(b);
        }
        return prefix;
    }

    /**
     * Compute the network address from a IP address and its prefix
     *
     * @param address the IP address
     * @param prefix the network prefix
     * @return the network IP address as a string
     *
     * @throws UnknownHostException if the IP address can't be parsed
     */
    public static String getNetworkAddress(String address, int prefix) throws UnknownHostException {
        InetAddress hostAddr = InetAddress.getByName(address);
        byte[] mask = prefixToNetmask(prefix, hostAddr.getAddress().length);
        byte[] netAddr = hostAddr.getAddress();
        for (int i = 0; i < netAddr.length; i++) {
            netAddr[i] = (byte)(Byte.toUnsignedInt(netAddr[i]) & Byte.toUnsignedInt(mask[i]));
        }
        return InetAddress.getByAddress(netAddr).getHostAddress();
    }

    /**
     * Convert a network prefix into the corresponding netmask address data.
     *
     * @param mask the prefix, e.g. 24
     * @param size the size of the expected address (depending whether IPv4 or IPv6 is expected)
     *
     * @return the computed data
     */
    public static byte[] prefixToNetmask(int mask, int size) {
        byte[] addr = new byte[size];
        int b = 0;
        for (int i = 0; i < mask; i++) {
            b += 1 << (7 - i % Byte.SIZE);
            if (i % Byte.SIZE == 7 || i == mask - 1) {
                addr[(i / Byte.SIZE)] = (byte)b;
                b = 0;
            }
        }
        return addr;
    }

    /**
     * Count the number of 1 bits in a byte
     *
     * @param value the byte to count the bits in
     * @return the number of set bits.
     */
    private static int countSetBits(byte value) {
        int bits = Byte.toUnsignedInt(value);
        int count = 0;
        while (bits > 0) {
            count += bits & 1;
            bits >>= 1;
        }
        return count;
    }
}
