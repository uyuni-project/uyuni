package com.suse.manager.reactor;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerNetAddress4;
import com.redhat.rhn.domain.server.ServerNetAddress6;
import com.redhat.rhn.domain.server.ServerNetworkFactory;
import com.redhat.rhn.frontend.events.AbstractDatabaseAction;
import com.suse.manager.webui.services.SaltService;
import com.suse.manager.webui.utils.salt.Network;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by matei on 1/7/16.
 */
public class GetNetworkInfoEventMessageAction extends AbstractDatabaseAction {

    // Reference to the SaltService instance
    private final SaltService SALT_SERVICE;

    public GetNetworkInfoEventMessageAction(SaltService SALT_SERVICE) {
        this.SALT_SERVICE = SALT_SERVICE;
    }

    @Override
    protected void doExecute(EventMessage msg) {
        GetNetworkInfoEventMessage event = (GetNetworkInfoEventMessage)msg;
        String machineId = event.getMachineId();
        String minionId = event.getMinionId();

        Server server = ServerFactory.findRegisteredMinion(machineId);

        Map<String, Network.Interface> interfaces = SALT_SERVICE.getNetworkInterfacesInfo(minionId);
        List<String> primaryIps = SALT_SERVICE.getPrimaryIps(minionId);
        String primaryIPv4 = primaryIps.get(0);
        String primaryIPv6 = primaryIps.get(1);

        com.redhat.rhn.domain.server.Network network = new com.redhat.rhn.domain.server.Network();
        network.setHostname(null); // TODO
        network.setIpaddr(primaryIPv4);
        network.setIp6addr(primaryIPv6);

        server.addNetwork(network);

        interfaces.forEach((name, saltIface) -> {
            NetworkInterface iface = server.getNetworkInterface(name);
            if(iface == null) {
                // we got a new interface
                iface = new NetworkInterface();
            }
            // else update the existing interface

            iface.setHwaddr(saltIface.getHWAddr());
            iface.setModule(null); // TODO custom grains/module to find this out
            iface.setServer(server);
            iface.setName(name);

            server.addNetworkInterface(iface);

            // we have to do this because we need the id of the interface afterwards
            ServerFactory.saveNetworkInterface(iface);

            // set IPv4 network info
            ServerNetAddress4 ipv4 = ServerNetworkFactory.findServerNetAddress4(iface.getInterfaceId());
            if (ipv4 == null) {
                ipv4 = new ServerNetAddress4();
            }

            ipv4.setInterfaceId(iface.getInterfaceId());

            Optional<Network.INet> inet = saltIface.getInet().stream().findFirst();
            ipv4.setAddress(inet.map(Network.INet::getAddress)
                    .orElse(null));
            ipv4.setNetmask(inet.map(Network.INet::getNetmask)
                    .orElse(null));
            ipv4.setBroadcast(inet.map(Network.INet::getBroadcast)
                    .orElse(null));
            ServerFactory.saveServerNetAddress4(ipv4);

            if (StringUtils.equals(ipv4.getAddress(), primaryIPv4)) {
                iface.setPrimary("Y");
            }

            // set IPv6 network info
            ServerNetAddress6 ipv6 = ServerNetworkFactory.findServerNetAddress6(iface.getInterfaceId());
            if (ipv6 == null) {
                ipv6 = new ServerNetAddress6();
            }
            ipv6.setInterfaceId(iface.getInterfaceId());

            Optional<Network.INet6> inet6 = saltIface.getInet6().stream().findFirst();
            ipv6.setAddress(inet6.map(Network.INet6::getAddress)
                    .orElse(null));
            ipv6.setNetmask(inet6.map(Network.INet6::getPrefixlen).orElse(null));
            // scope is part of the entity's composite-id
            // so if it's null we'll get a list with null on namedQuery.list()
            // therefore we need a default value
            ipv6.setScope(inet6.map(Network.INet6::getScope).orElse("unknown"));
            ServerFactory.saveServerNetAddress6(ipv6);

            if (StringUtils.equals(ipv6.getAddress(), primaryIPv6)) {
                iface.setPrimary("Y");
            }

        });


    }
}
