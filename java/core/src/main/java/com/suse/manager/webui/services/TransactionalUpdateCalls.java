/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.webui.services;

import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.utils.Xor;

import com.google.gson.reflect.TypeToken;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Provides {@code transactional_update.apply} as a typed {@link LocalCall}.
 */
public class TransactionalUpdateCalls {
    public static final String APPLY_FUNCTION = "transactional_update.apply";

    private TransactionalUpdateCalls() {
    }

    /**
     * Apply states using {@code transactional_update.apply}.
     *
     * <p>Equivalent to {@code state.apply} but stages OS-level changes in a new
     * Btrfs snapshot that becomes active only after the next reboot.</p>
     *
     * @param mods list of SLS state names to apply. Salt applies the highstate when an empty list is given
     * @return a {@link LocalCall} ready to be dispatched via the Salt API
     */
    public static LocalCall<Map<String, State.ApplyResult>> apply(List<String> mods) {
        return apply(mods, Optional.empty());
    }

    /**
     * Apply states using {@code transactional_update.apply}, optionally passing pillar data.
     *
     * @param mods   list of SLS state names to apply. Salt applies the highstate when an empty list is given
     * @param pillar optional pillar override map; use {@link Optional#empty()} to omit
     * @return a {@link LocalCall} ready to be dispatched via the Salt API
     */
    public static LocalCall<Map<String, State.ApplyResult>> apply(
            List<String> mods, Optional<Map<String, Object>> pillar) {
        return apply(mods, pillar, Optional.empty(), Optional.empty());
    }

    /**
     * Apply states using {@code transactional_update.apply}, optionally passing pillar data and execution flags.
     *
     * @param mods   list of SLS state names to apply. Salt applies the highstate when an empty list is given
     * @param pillar optional pillar override map; use {@link Optional#empty()} to omit
     * @param queue  optional queue flag
     * @param test   optional test mode flag
     * @return a {@link LocalCall} ready to be dispatched via the Salt API
     */
    public static LocalCall<Map<String, State.ApplyResult>> apply(
            List<String> mods, Optional<Map<String, Object>> pillar, Optional<Boolean> queue, Optional<Boolean> test) {
        if (mods == null) {
            throw new IllegalArgumentException("State list must not be null");
        }
        Map<String, Object> kwargs = new LinkedHashMap<>();
        if (!mods.isEmpty()) {
            kwargs.put("mods", mods);
        }
        pillar.ifPresent(p -> kwargs.put("pillar", p));
        queue.ifPresent(q -> kwargs.put("queue", q));
        test.ifPresent(t -> kwargs.put("test", t));
        return new LocalCall<>(APPLY_FUNCTION, Optional.empty(), Optional.of(kwargs),
                new TypeToken<Map<String, State.ApplyResult>>() { });
    }

    /**
     * Check whether the given Salt function is {@code transactional_update.apply}.
     *
     * @param function Salt function from a job return
     * @return true when the job return was produced by transactional-update apply
     */
    public static boolean isApplyFunction(Optional<Xor<String[], String>> function) {
        return function
                .map(x -> x.fold(Arrays::asList, List::of).stream().anyMatch(TransactionalUpdateCalls::isApplyFunction))
                .orElse(false);
    }

    /**
     * Check whether the given Salt function is {@code transactional_update.apply}.
     *
     * @param function Salt function name
     * @return true when the function is transactional-update apply
     */
    public static boolean isApplyFunction(String function) {
        return APPLY_FUNCTION.equals(function);
    }
}
