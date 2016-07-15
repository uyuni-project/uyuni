/**
 * CHECKSTYLE:OFF
 */
package com.suse.manager.webui.utils.salt;

import com.suse.salt.netapi.calls.WheelCall;

import com.google.gson.reflect.TypeToken;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class Key {

    /**
     * A key pair as returned by "key.gen" or "key.gen_accept".
     */
    public static class Pair {

        private Optional<String> pub = Optional.empty();
        private Optional<String> priv = Optional.empty();

        public Optional<String> getPub() {
            return pub;
        }

        public Optional<String> getPriv() {
            return priv;
        }
    }

    private Key() {
    }

    public static WheelCall<Pair> genAccept(String id, Optional<Boolean> force) {
        Map<String, Object> args = new LinkedHashMap<>();
        args.put("id_", id);
        force.ifPresent(value -> args.put("force", value));
        return new WheelCall<>("key.gen_accept", Optional.of(args),
                new TypeToken<Pair>(){});
    }
}
