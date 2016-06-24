/**
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.manager.reactor.messaging;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerNetAddress4;
import com.redhat.rhn.domain.server.ServerNetAddress6;
import com.redhat.rhn.domain.server.ServerNetworkFactory;
import com.suse.manager.reactor.hardware.AbstractHardwareMapper;
import com.suse.manager.reactor.hardware.SaltServiceInvoker;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.utils.salt.custom.SumaUtil;
import com.suse.salt.netapi.calls.modules.Network;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Get and process network information from a minion.
 */
public class NetworkInfoMapper extends AbstractHardwareMapper<MinionServer> {

    /* Logger for this class */
    private static final Logger LOG = Logger.getLogger(NetworkInfoMapper.class);
    private boolean hasPrimaryInterfaceSet = false;

    /**
     * The constructor.
     * @param saltServiceInvoker a {@link SaltServiceInvoker} instance
     */
    public NetworkInfoMapper(SaltServiceInvoker saltServiceInvoker) {
        super(saltServiceInvoker);
    }

    protected MinionServer doMap(MinionServer server, ValueMap grains) {

        String minionId = server.getMinionId();

        if (grains == null) {
            grains = saltInvoker.getGrains(minionId)
                .map(ValueMap::new).orElseGet(ValueMap::new);
        }

        Map<String, Network.Interface> interfaces = saltInvoker
                .getNetworkInterfacesInfo(minionId)
                .orElse(Collections.emptyMap());
        Optional<Map<SumaUtil.IPVersion, SumaUtil.IPRoute>> primaryIps =
                saltInvoker.getPrimaryIps(minionId);
        Map<String, Optional<String>> netModules = saltInvoker.getNetModules(minionId)
                .orElse(Collections.emptyMap());

        if (interfaces.isEmpty()) {
            setError("Salt module 'network.interfaces' returned en empty value");
            LOG.error("Salt module 'network.interfaces' returned en empty value " +
                    "for minion: " + minionId);
            return null;
        }

        Optional<String> primaryIPv4 = primaryIps
                    .flatMap(x -> Optional.ofNullable(x.get(SumaUtil.IPVersion.IPV4)))
                    .map(SumaUtil.IPRoute::getSource);
        Optional<String> primaryIPv6 = primaryIps
                    .flatMap(x -> Optional.ofNullable(x.get(SumaUtil.IPVersion.IPV6)))
                    .map(SumaUtil.IPRoute::getSource);

        com.redhat.rhn.domain.server.Network network =
                new com.redhat.rhn.domain.server.Network();
        network.setHostname(grains.getOptionalAsString("fqdn").orElse(null));
        primaryIPv4.ifPresent(network::setIpaddr);
        primaryIPv6.ifPresent(network::setIp6addr);

        server.getNetworks().clear();
        server.addNetwork(network);

        interfaces.forEach((name, saltIface) -> {
            NetworkInterface ifaceEntity = server.getNetworkInterface(name);
            if (ifaceEntity == null) {
                // we got a new interface
                ifaceEntity = new NetworkInterface();
            }
            // else update the existing interface
            final NetworkInterface iface = ifaceEntity;

            iface.setHwaddr(saltIface.getHWAddr());
            iface.setModule(netModules.get(name).orElse(null));
            iface.setServer(server);
            iface.setName(name);

            server.addNetworkInterface(iface);

            // we have to do this because we need the id of the interface afterwards
            ServerFactory.saveNetworkInterface(iface);
            // flush & refresh iface because generated="insert"
            // on interfaceId does not seem to work
            ServerFactory.getSession().flush();
            ServerFactory.getSession().refresh(iface);

            Optional<Network.INet> inet = Optional.ofNullable(saltIface.getInet())
                    .flatMap(addr -> addr.stream().findFirst());

            inet.ifPresent(addr4 -> {
                // set IPv4 network info
                ServerNetAddress4 ipv4 = ServerNetworkFactory
                        .findServerNetAddress4(iface.getInterfaceId());
                if (ipv4 == null) {
                    ipv4 = new ServerNetAddress4();
                }
                ipv4.setInterfaceId(iface.getInterfaceId());
                ipv4.setAddress(addr4.getAddress().orElse(null));
                ipv4.setNetmask(addr4.getNetmask().orElse(null));
                ipv4.setBroadcast(addr4.getBroadcast().orElse(null));

                ServerNetworkFactory.saveServerNetAddress4(ipv4);

                if (StringUtils.equals(ipv4.getAddress(), primaryIPv4.orElse(null))) {
                    hasPrimaryInterfaceSet = true;
                    iface.setPrimary("Y");
                }
            });

            Optional<Network.INet6> inet6 = Optional.ofNullable(saltIface.getInet6())
                    .flatMap(addr -> addr.stream().findFirst());
            inet6.ifPresent(addr6 -> {
                // set IPv6 network info
                ServerNetAddress6 ipv6 = ServerNetworkFactory
                        .findServerNetAddress6(iface.getInterfaceId());
                if (ipv6 == null) {
                    ipv6 = new ServerNetAddress6();
                }
                ipv6.setInterfaceId(iface.getInterfaceId());
                ipv6.setAddress(addr6.getAddress());
                ipv6.setNetmask(addr6.getPrefixlen());
                // scope is part of the entity's composite-id
                // so if it's null we'll get a list with null on namedQuery.list()
                // therefore we need a default value
                ipv6.setScope(Optional.ofNullable(addr6.getScope()).orElse("unknown"));

                ServerNetworkFactory.saveServerNetAddress6(ipv6);
            });
        });

        if (!hasPrimaryInterfaceSet) {
            primaryIPv6.ifPresent(ipv6Primary -> {
                server.getNetworkInterfaces().stream()
                    .filter(netIf -> netIf.getIPv6Addresses().stream()
                        .anyMatch(address -> ipv6Primary.equals(address.getAddress()))
                    )
                    .findFirst()
                    .ifPresent(primaryNetIf -> primaryNetIf.setPrimary("Y"));
            });
        }

        return server;
    }
}
