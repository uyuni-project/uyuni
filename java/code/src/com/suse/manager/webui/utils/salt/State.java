/**
 * Copyright (c) 2017 SUSE LLC
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

import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State.ApplyResult;

import com.google.gson.reflect.TypeToken;

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
     *  Apply function with additional argument of test in order to run in dry run mode.
     * @param mods mods
     * @param pillar pillar data
     * @param queue queue
     * @param test test-mode true/false
     * @return LocalCall<Map<String, ApplyResult>> result
     */
    public static LocalCall<Map<String, ApplyResult>> apply(List<String> mods,
            Optional<Map<String, Object>> pillar, Optional<Boolean> queue,
                                                  Optional<Boolean> test) {
        Map<String, Object> kwargs = new LinkedHashMap<>();
        kwargs.put("mods", mods);
        pillar.ifPresent(p -> kwargs.put("pillar", p));
        queue.ifPresent(q -> kwargs.put("queue", q));
        test.ifPresent(q -> kwargs.put("test", q));
        return new LocalCall<>("state.apply", Optional.empty(), Optional.of(kwargs),
                new TypeToken<Map<String, ApplyResult>>() { });
    }


}
