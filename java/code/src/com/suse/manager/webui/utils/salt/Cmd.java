/**
 * CHECKSTYLE:OFF
 */
package com.suse.manager.webui.utils.salt;

import com.suse.salt.netapi.calls.LocalCall;

import com.google.gson.reflect.TypeToken;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Optional;

/**
 * salt.modules.cmdmod
 */
public class Cmd {

    private Cmd() {
    }

    public static LocalCall<String> execCodeAll(String lang, String code) {
        LinkedHashMap<String, Object> args = new LinkedHashMap<>();
        return new LocalCall<>("cmd.exec_code_all", Optional.of(Arrays.asList(lang, code)),
                Optional.of(args), new TypeToken<String>(){});
    }
}
