/**
 * Copyright (c) 2018 SUSE LLC
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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.gson.reflect.TypeToken;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State.ApplyResult;

/**
 * This class contains convenience methods to be moved to the salt-netapi-client library.
 */
public class State {

    private State() { }

    /**
     * Convenience method to create a LocalCall object to be used for most of the SUSE Manager "state.apply" purposes:
     * Set the "queue" parameter to true and do not pass a "test" parameter.
     *
     * @param mods state modules
     * @param pillar pillar data
     * @return LocalCall<Map<String, ApplyResult>> result
     */
    public static LocalCall<Map<String, ApplyResult>> apply(List<String> mods, Optional<Map<String, Object>> pillar) {
        return com.suse.salt.netapi.calls.modules.State.apply(mods, pillar, Optional.of(true), Optional.empty());
    }

    /**
     * Method which accepts Class type and return result after parsing actual json to that particular class
     * instead of ApplyResult
     * @param mods state modules
     * @param pillar pillar data
     * @param queue add to the queue
     * @param test  if run in test mode
     * @param returnType return type
     * @param <R> R
     * @return LocalCall<R> result
     */
    public static <R> LocalCall<R> apply(List<String> mods, Optional<Map<String, Object>> pillar,
                                         Optional<Boolean> queue, Optional<Boolean> test, Class<R> returnType) {
        return apply(mods, pillar, queue, test, TypeToken.get(returnType));
    }
    /**
     * Method which accepts TypeToken and return result after parsing actual json to that particular class
     * instead of ApplyResult
     * @param mods state modules
     * @param pillar pillar data
     * @param queue add to the queue
     * @param test  if run in test mode
     * @param returnType return type
     * @param <R> R
     * @return LocalCall<R> result
     */
    public static <R> LocalCall<R> apply(List<String> mods, Optional<Map<String, Object>> pillar,
                                         Optional<Boolean> queue, Optional<Boolean> test,
                                         TypeToken<R> returnType) {
        Map<String, Object> kwargs = new LinkedHashMap<>();
        kwargs.put("mods", mods);
        pillar.ifPresent(p -> kwargs.put("pillar", p));
        queue.ifPresent(q -> kwargs.put("queue", q));
        test.ifPresent(t -> kwargs.put("test", t));
        return new LocalCall<>("state.apply", Optional.empty(), Optional.of(kwargs), returnType);
    }
}
