package com.suse.manager.webui.utils.salt;

import com.google.gson.reflect.TypeToken;
import com.suse.salt.netapi.calls.LocalCall;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

/**
 * TODO move to netapi
 * https://github.com/SUSE/salt-netapi-client/pull/168
 */
public class SaltUtil {

    public static LocalCall<List<String>> syncBeacons(
            Optional<Boolean> refresh, Optional<String> saltenv) {
        LinkedHashMap<String, Object> args = syncArgs(refresh, saltenv);
        return new LocalCall<>("saltutil.sync_beacons", Optional.empty(),
                Optional.of(args), new TypeToken<List<String>>() {
        });
    }

    private static LinkedHashMap<String, Object> syncArgs(
            Optional<Boolean> refresh, Optional<String> saltenv) {
        LinkedHashMap<String, Object> args = new LinkedHashMap<>();
        refresh.ifPresent(value -> args.put("refresh", value));
        saltenv.ifPresent(value -> args.put("saltenv", value));
        return args;
    }
}
