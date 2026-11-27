/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.manager.utils;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Converts enum constant names coming from API or UI requests.
 */
public final class EnumNameParser {

    private EnumNameParser() {
        // Utility class
    }

    /**
     * Parse enum constant names.
     *
     * @param names the names to parse, may be null
     * @param type the enum class
     * @param onInvalid called for each name that is not a constant of the enum, it may throw to reject the request
     * @return the parsed constants, ignoring the invalid names
     * @param <E> the enum type
     */
    public static <E extends Enum<E>> Set<E> parse(Collection<String> names, Class<E> type,
                                                   Consumer<IllegalArgumentException> onInvalid) {
        Set<E> result = EnumSet.noneOf(type);
        if (names == null) {
            return result;
        }
        for (String name : names) {
            try {
                result.add(Enum.valueOf(type, name));
            }
            catch (IllegalArgumentException e) {
                onInvalid.accept(e);
            }
        }
        return result;
    }
}
