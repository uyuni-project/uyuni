package com.suse.manager.webui.utils.salt;

import com.google.gson.reflect.TypeToken;
import com.suse.salt.netapi.calls.LocalCall;
import java.util.LinkedHashMap;
import java.util.Optional;

public class Status {
    private Status() {
    }

    public static LocalCall<Float> uptime() {
        LinkedHashMap<String, Object> args = new LinkedHashMap<>();
        args.put("human_readable", false);
        return new LocalCall("status.uptime",
                Optional.empty(), Optional.of(args), new TypeToken<Float>() {
        });
    }
}
