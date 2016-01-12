package com.suse.manager.reactor;

import com.redhat.rhn.common.messaging.EventMessage;
import com.suse.manager.reactor.utils.ValueMap;

/**
 * Created by matei on 1/7/16.
 */
public class GetNetworkInfoEventMessage implements EventMessage {

    private String machineId;
    private String minionId;
    private ValueMap grains;

    public GetNetworkInfoEventMessage(String machineIdIn, String minionIdIn, ValueMap grainsIn) {
        machineId = machineIdIn;
        minionId = minionIdIn;
        grains = grainsIn;
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

    public ValueMap getGrains() {
        return grains;
    }
}
