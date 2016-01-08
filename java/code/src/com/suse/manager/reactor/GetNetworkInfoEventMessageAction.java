package com.suse.manager.reactor;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.frontend.events.AbstractDatabaseAction;
import com.suse.manager.webui.services.SaltService;

import java.util.Map;

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

        Map<String, Map<String, Object>> interfaces = SALT_SERVICE.getNetworkInterfacesInfo(minionId);

        interfaces.forEach((k, v) -> {
            NetworkInterface eth = new NetworkInterface();

//            eth.setHwaddr();

            server.addNetworkInterface(eth);

        });


    }
}
