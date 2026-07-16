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
    private static final String APPLY_FUNCTION = "transactional_update.apply";

    private TransactionalUpdateCalls() {
    }

    /**
     * Apply states using {@code transactional_update.apply}.
     *
     * <p>Equivalent to {@code state.apply} but stages OS-level changes in a new
     * Btrfs snapshot that becomes active only after the next reboot.</p>
     *
     * @param mods list of SLS state names to apply (must not be null or empty)
     * @return a {@link LocalCall} ready to be dispatched via the Salt API
     */
    public static LocalCall<Map<String, State.ApplyResult>> apply(List<String> mods) {
        return apply(mods, Optional.empty());
    }

    /**
     * Apply states using {@code transactional_update.apply}, optionally passing pillar data.
     *
     * @param mods   list of SLS state names to apply (must not be null or empty)
     * @param pillar optional pillar override map; use {@link Optional#empty()} to omit
     * @return a {@link LocalCall} ready to be dispatched via the Salt API
     */
    public static LocalCall<Map<String, State.ApplyResult>> apply(
            List<String> mods, Optional<Map<String, Object>> pillar) {
        if (mods == null || mods.isEmpty()) {
            throw new IllegalArgumentException("At least one state must be specified");
        }
        Map<String, Object> kwargs = new LinkedHashMap<>();
        kwargs.put("mods", mods);
        pillar.ifPresent(p -> kwargs.put("pillar", p));
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
                .map(x -> x.fold(Arrays::asList, List::of).contains(APPLY_FUNCTION))
                .orElse(false);
    }
}
