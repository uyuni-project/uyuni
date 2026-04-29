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

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.ServerFQDN;
import com.redhat.rhn.domain.server.ServerFactory;

import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.utils.salt.custom.SumaUtil;
import com.suse.salt.netapi.calls.modules.Network;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Maps network interface information from Salt to the database.
 * Handles network interfaces, IP addresses (IPv4/IPv6), and FQDNs.
 */
public class NetworkMapper {

    private static final Logger LOG = LogManager.getLogger(NetworkMapper.class);

    private final MinionServer server;
    private final ValueMap grains;

    /**
     * Create a network mapper.
     *
     * @param serverIn the minion server
     * @param grainsIn the grains
     */
    public NetworkMapper(MinionServer serverIn, ValueMap grainsIn) {
        this.server = serverIn;
        this.grains = grainsIn;
    }

    /**
     * Maps network interfaces, IP addresses, and FQDNs.
     *
     * @param interfaces network interfaces from Salt
     * @param primaryIps primary IP addresses (IPv4/IPv6)
     * @param netModules network modules
     * @param fqdns fully qualified domain names
     * @return Optional error message if mapping failed
     */
    public Optional<String> mapNetworkInfo(
            Map<String, Network.Interface> interfaces,
            Optional<Map<SumaUtil.IPVersion, SumaUtil.IPRoute>> primaryIps,
            Map<String, Optional<String>> netModules,
            List<String> fqdns
    ) {

        try {
            if (interfaces.isEmpty()) {
                String error = "Network: Salt module 'network.interfaces' returned an empty value";
                LOG.error("{} for minion: {}", error, server.getMinionId());
                return Optional.of(error);
            }

            // Extract primary IPs
            Optional<String> primaryIPv4 = primaryIps
                    .flatMap(x -> Optional.ofNullable(x.get(SumaUtil.IPVersion.IPV4)))
                    .map(SumaUtil.IPRoute::getSource)
                    .filter(addr -> !"127.0.0.1".equals(addr));
            Optional<String> primaryIPv6 = primaryIps
                    .flatMap(x -> Optional.ofNullable(x.get(SumaUtil.IPVersion.IPV6)))
                    .map(SumaUtil.IPRoute::getSource)
                    .filter(addr -> !"::1".equals(addr));

            // Set hostname and FQDNs
            server.setHostname(grains.getOptionalAsString("fqdn").orElse(null));
            setFqdns(server, fqdns);

            // Remove interfaces not present in Salt result by name
            server.getNetworkInterfaces().removeIf(i -> !interfaces.containsKey(i.getName()));

            // Add/update interfaces from Salt result
            interfaces.forEach((name, saltInterface) ->
                    syncSingleInterface(name, saltInterface, netModules)
            );

            // Determine and set primary interface
            determinePrimaryInterface(primaryIPv4, primaryIPv6);

            // Set primary FQDN to hostname if no primary FQDN is specified
            if (StringUtils.isNotBlank(server.getHostname()) && server.getFqdns().stream()
                   .noneMatch(ServerFQDN::isPrimary)) {
                server.setPrimaryFQDNWithName(server.getHostname());
            }

            return Optional.empty();
        }
        catch (Exception e) {
            LOG.error("Failed to map network info for minion {} : {} ", server.getMinionId(), e);
            return Optional.of("Network mapping failed: " + e.getMessage());
        }

    }

    /**
     * Set FQDNs for the server.
     */
    private void setFqdns(MinionServer serverIn, List<String> fqdns) {
        if (fqdns.isEmpty()) {
            LOG.warn("Salt module 'network.fqdns' returned an empty value for minion: {}", server.getMinionId());
            return;
        }

        Collection<ServerFQDN> serverFQDNs = serverIn.getFqdns();
        Collection<ServerFQDN> srvFqdnsObj = fqdns.stream()
                .map(fqdn -> new ServerFQDN(serverIn, fqdn))
                .toList();
        serverFQDNs.retainAll(srvFqdnsObj);
        serverFQDNs.addAll(srvFqdnsObj);
    }

    /**
     * Synchronizes a single network interface with Salt data.
     * Updates existing interface or creates a new one, then syncs IP addresses.
     *
     * @param name interface name
     * @param saltInterface interface data from Salt
     * @param netModules network driver modules
     */
    private void syncSingleInterface(
            String name,
            Network.Interface saltInterface,
            Map<String, Optional<String>> netModules
    ) {

        NetworkInterface networkInterface = server.getNetworkInterface(name);
        if (networkInterface == null) {
            networkInterface = new NetworkInterface();

            networkInterface.setName(name);
            networkInterface.setServer(server);
            server.addNetworkInterface(networkInterface);
        }

        // Update interface properties
        networkInterface.setHwaddr(saltInterface.getHWAddr());
        networkInterface.setModule(netModules.get(name).orElse(null));

        // Persist to get ID for IP address syncing
        networkInterface = ServerFactory.saveNetworkInterface(networkInterface);

        // Sync IP addresses
        IpAddressHandler.syncIPv4Addresses(networkInterface.getInterfaceId(), saltInterface.getInet());
        IpAddressHandler.syncIPv6Addresses(networkInterface.getInterfaceId(), saltInterface.getInet6());
    }

    /**
     * Determines which network interface should be marked as primary.
     * Prefers interface with primary IPv4 address, falls back to IPv6 if not found.
     * Resets all interfaces before determining the new primary.
     *
     * @param ipv4 extracted primary IPv4s from Salt
     * @param ipv6 extracted primary IPv6s from Salt
     */
    private void determinePrimaryInterface(Optional<String> ipv4, Optional<String> ipv6) {

        // Reset primary IP flag, we will re-compute it
        server.getNetworkInterfaces().forEach(n -> n.setPrimary(null));

        // Find the interface having primary IPv4 addr
        Optional<NetworkInterface> primaryNetworkInterface = ipv4.flatMap(pipv4 ->
                server.getNetworkInterfaces().stream()
                        .filter(netIf -> netIf.getIPv4Addresses().stream()
                                .anyMatch(addr -> Objects.equals(pipv4, addr.getAddress())))
                        .findFirst());

        if (primaryNetworkInterface.isEmpty()) {
            // No primary IPv4, fallback to IPv6
            primaryNetworkInterface = ipv6.flatMap(pipv6 ->
                    server.getNetworkInterfaces().stream()
                            .filter(netIf -> netIf.getIPv6Addresses().stream()
                                    .anyMatch(addr -> Objects.equals(pipv6, addr.getAddress())))
                            .findFirst());
        }

        // We found an interface with the same addr as the primary IPv4/v6 addr, set it as primary
        primaryNetworkInterface.ifPresent(server::setPrimaryInterface);

    }
}
