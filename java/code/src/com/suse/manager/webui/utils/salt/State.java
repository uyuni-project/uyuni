/**
 * Copyright (c) 2015 SUSE LLC
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
import com.suse.saltstack.netapi.calls.LocalCall;

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

    /**
     * this function will move to the saltstack-netapi library
     * @param mods modules
     * @return salt call
     */
    public static LocalCall<Map<String, Object>> apply(List<String> mods) {
        LinkedHashMap<String, Object> args = new LinkedHashMap<>();
        args.put("mods", mods);
        return new LocalCall<>("state.apply", Optional.empty(), Optional.of(args),
                new TypeToken<Map<String, Object>>() { });
    }

    /**
     * this function will move to the saltstack-netapi library
     * @param mods modules
     * @return salt call
     */
    public static LocalCall<Map<String, Object>> apply(String... mods) {
        return apply(Arrays.asList(mods));
    }

}
