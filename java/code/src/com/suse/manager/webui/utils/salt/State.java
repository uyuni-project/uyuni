/**
 * CHECKSTYLE:OFF
 */
package com.suse.manager.webui.utils.salt;

import com.suse.salt.netapi.calls.LocalCall;

import com.google.gson.reflect.TypeToken;

import java.util.Collections;
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

    public static LocalCall<Map<String, Object>> apply(String mod, Map<String, Object> pillar) {
        LinkedHashMap args = new LinkedHashMap();
        args.put("mods", Collections.singletonList(mod));
        args.put("pillar", pillar);
        return new LocalCall("state.apply", Optional.empty(), Optional.of(args),
                new TypeToken<Map<String, Object>>() { });
    }

}
