package com.suse.manager.webui.utils.salt;

import com.google.gson.reflect.TypeToken;
import com.suse.salt.netapi.calls.LocalCall;

import java.util.LinkedHashMap;
import java.util.Optional;

/**
 * TODO This should be removed once it has been integrated into salt-netapi-client
 */
public class SaltUtil {

    public static LocalCall<Boolean> refreshPillar(
            Optional<Boolean> refresh, Optional<String> saltenv) {
        LinkedHashMap<String, Object> args = syncArgs(refresh, saltenv);
        return new LocalCall<>("saltutil.refresh_pillar", Optional.empty(),
                Optional.of(args), new TypeToken<Boolean>() {
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
