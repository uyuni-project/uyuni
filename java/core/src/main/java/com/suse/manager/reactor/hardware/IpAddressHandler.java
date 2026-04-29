/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.reactor.hardware;

import static com.redhat.rhn.common.ExceptionMessage.NOT_INSTANTIABLE;

import com.redhat.rhn.domain.server.ServerNetAddress4;
import com.redhat.rhn.domain.server.ServerNetAddress6;
import com.redhat.rhn.domain.server.ServerNetworkFactory;

import com.suse.salt.netapi.calls.modules.Network;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Utility class for synchronizing IP addresses between Salt and the database.
 * Reduces duplication in IPv4 and IPv6 address handling.
 * They have the same behaviour but it is centralized.
 */
public final class IpAddressHandler {

    public static final String UNKNOWN = "unknown";

    /**
     * Synchronize IPv4 addresses for a network interface.
     * Updates existing addresses, inserts new ones, and removes stale ones.
     *
     * @param interfaceId the network interface ID
     * @param saltIpv4Addresses IPv4 addresses from Salt
     */
    public static void syncIPv4Addresses(Long interfaceId, List<Network.INet> saltIpv4Addresses) {
        List<ServerNetAddress4> dbAddresses = ServerNetworkFactory.findServerNetAddress4(interfaceId);
        List<Network.INet> saltAddresses = Optional.ofNullable(saltIpv4Addresses).orElse(List.of());

        Set<ServerNetAddress4> foundAddresses = new HashSet<>();

        for (Network.INet saltAddr : saltAddresses) {
            boolean found = false;

            for (ServerNetAddress4 dbAddr : dbAddresses) {
                if (saltAddr.getAddress().orElse("").equals(dbAddr.getAddress())) {
                    // Update existing address
                    dbAddr.setNetmask(saltAddr.getNetmask().orElse(null));
                    dbAddr.setBroadcast(saltAddr.getBroadcast().orElse(null));
                    found = true;
                    foundAddresses.add(dbAddr);
                    break;
                }
            }

            if (!found) {
                // Insert new address
                var ipv4 = new ServerNetAddress4(interfaceId, saltAddr.getAddress().orElse(null));
                ipv4.setNetmask(saltAddr.getNetmask().orElse(null));
                ipv4.setBroadcast(saltAddr.getBroadcast().orElse(null));
                ServerNetworkFactory.saveServerNetAddress4(ipv4);
            }
        }

        // Remove addresses that no longer exist in Salt
        dbAddresses.stream()
                .filter(addr -> !foundAddresses.contains(addr))
                .forEach(ServerNetworkFactory::removeServerNetAddress4);
    }

    /**
     * Synchronize IPv6 addresses for a network interface.
     * Updates existing addresses, inserts new ones, and removes stale ones.
     *
     * @param interfaceId the network interface ID
     * @param saltIpv6Addresses IPv6 addresses from Salt
     */
    public static void syncIPv6Addresses(Long interfaceId, List<Network.INet6> saltIpv6Addresses) {
        List<ServerNetAddress6> dbAddresses = ServerNetworkFactory.findServerNetAddress6(interfaceId);
        List<Network.INet6> saltAddresses = Optional.ofNullable(saltIpv6Addresses).orElse(List.of());

        Set<ServerNetAddress6> foundAddresses = new HashSet<>();

        for (Network.INet6 saltAddr : saltAddresses) {
            boolean found = false;
            String saltScope = Optional.ofNullable(saltAddr.getScope()).orElse(UNKNOWN);

            for (ServerNetAddress6 dbAddr : dbAddresses) {
                // Match by address AND scope (both are part of the composite ID and immutable)
                if (saltAddr.getAddress().equals(dbAddr.getAddress()) && saltScope.equals(dbAddr.getScope())) {
                    // Update existing address
                    dbAddr.setNetmask(saltAddr.getPrefixlen());
                    found = true;
                    foundAddresses.add(dbAddr);
                    break;
                }
            }

            if (!found) {
                // Insert new address
                ServerNetAddress6 ipv6 = new ServerNetAddress6();
                ipv6.setInterfaceId(interfaceId);
                ipv6.setAddress(saltAddr.getAddress());
                ipv6.setNetmask(saltAddr.getPrefixlen());
                ipv6.setScope(saltScope);
                ServerNetworkFactory.saveServerNetAddress6(ipv6);
            }
        }

        // Remove addresses that no longer exist in Salt (including same address with old scope)
        dbAddresses.stream()
                .filter(addr -> !foundAddresses.contains(addr))
                .forEach(ServerNetworkFactory::removeServerNetAddress6);
    }

    private IpAddressHandler() {
        throw new UnsupportedOperationException(NOT_INSTANTIABLE);
    }
}
