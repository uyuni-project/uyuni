package com.suse.manager.webui.utils.salt;

import com.google.gson.reflect.TypeToken;
import com.suse.saltstack.netapi.calls.LocalCall;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by matei on 1/7/16.
 */
public class Network {

    /**
     * Get the network interfaces.
     * @return a LocalCall to pass to the SaltStackClient
     */
    public static LocalCall<Map<String, Map<String, Object>>> interfaces() {
        Map<String, Object> args = new LinkedHashMap<>();
        return new LocalCall<>("network.interfaces", Optional.empty(), Optional.of(args),
                new TypeToken<Map<String, Map<String, Object>>>() { });
    }

}
