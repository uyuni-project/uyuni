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
package com.suse.utils;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Helper functions for working with Functions
 */
public class Fn {

    private Fn() {
    }

    /**
     * Given a T, a predicate for T and a function "fn" from T to R this function
     * will return fn
     * @param value value to test and extract from
     * @param predicate predicate to test the value
     * @param fn function to extract R from T
     * @param <T> type of the value to extract from
     * @param <R> type of the extracted value
     * @return Optional containing the extracted value
     */
    public static <T, R> Optional<R> applyIf(T value, Predicate<T> predicate,
            Function<T, R> fn) {
        return predicate.test(value) ? Optional.of(fn.apply(value)) : Optional.empty();
    }

}
