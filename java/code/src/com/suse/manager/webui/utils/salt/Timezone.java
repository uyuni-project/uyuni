/**
 * CHECKSTYLE:OFF
 */
package com.suse.manager.webui.utils.salt;

import com.suse.salt.netapi.calls.LocalCall;

import com.google.gson.reflect.TypeToken;

import java.util.Optional;

/**
 * salt.modules.timezone
 *
 * https://docs.saltstack.com/en/latest/ref/modules/all/salt.modules.timezone.html
 */
public class Timezone {

    private Timezone() {}

    /**
     * salt.modules.timezone.get_offset()
     */
    public static LocalCall<String> getOffset() {
        return new LocalCall<>("timezone.get_offset", Optional.empty(), Optional.empty(),
                new TypeToken<String>(){});
    }
}
