/**
 * Copyright (c) 2016 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.webui.utils.salt;

import com.google.gson.reflect.TypeToken;
import com.suse.salt.netapi.calls.LocalCall;

import java.util.Arrays;
import java.util.Optional;

/**
 * Returns configuration information from minions.
 */
public class Config {

    /** The configuration key for the master's hostname. */
    public static final String MASTER = "master";

    private Config() { }

    /**
     * Returns a configuration parameter.
     * @param key the parameter name
     * @return the {@link LocalCall} object to make the call
     */
    public static LocalCall<String> get(String key) {
        return new LocalCall<>(
            "config.get",
            Optional.of(Arrays.asList(key)),
            Optional.empty(),
            new TypeToken<String>() { });
    }
}
