/**
 * CHECKSTYLE:OFF
 */
package com.suse.manager.webui.utils.salt;

import com.suse.salt.netapi.calls.LocalCall;

import com.google.gson.reflect.TypeToken;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * salt.modules.state
 */
public class State {

    private State() { }

    public static LocalCall<Object> showHighstate() {
        return new LocalCall<>("state.show_highstate", Optional.empty(), Optional.empty(),
                new TypeToken<Object>() { });
    }

    public static LocalCall<Map<String, Object>> apply(List<String> mods, Optional<Map<String, Object>> pillar) {
        LinkedHashMap args = new LinkedHashMap();
        args.put("mods", mods);
        if (pillar.isPresent()) {
            args.put("pillar", pillar.get());
        }
        args.put("queue", true);
        return new LocalCall("state.apply", Optional.empty(), Optional.of(args),
                new TypeToken<Map<String, Object>>() { });
    }

    public static LocalCall<Map<String, Object>> apply(List<String> mods) {
        return apply(mods, Optional.empty());
    }

    public static LocalCall<Map<String, Object>> apply(String... mods) {
        return apply(Arrays.asList(mods), Optional.empty());
    }
}
