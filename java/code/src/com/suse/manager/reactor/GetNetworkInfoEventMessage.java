package com.suse.manager.reactor;

import com.redhat.rhn.common.messaging.EventMessage;

/**
 * Created by matei on 1/7/16.
 */
public class GetNetworkInfoEventMessage implements EventMessage {

    private String machineId;
    private String minionId;

    public GetNetworkInfoEventMessage(String machineId, String minionId) {
        this.machineId = machineId;
        this.minionId = minionId;
    }

    @Override
    public String toText() {
        return null;
    }

    @Override
    public Long getUserId() {
        return null;
    }

    public String getMachineId() {
        return machineId;
    }

    public String getMinionId() {
        return minionId;
    }
}
