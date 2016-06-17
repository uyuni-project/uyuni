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
import java.util.function.Supplier;


/**
 * Helper functions for working with Optional
 */
public class Opt {

    private Opt() {
    }

    /**
     * Given a function from T to Optional of R returns a function that
     * operates on Optional of T instead.
     * @param fn the function to transform
     * @param <T> argument type of the function
     * @param <R> the type inside the return Optional
     * @return a function operating on Optional of T
     */
    public static <T, R> Function<Optional<T>, Optional<R>> flatMap(
            Function<T, Optional<R>> fn) {
        return o -> o.flatMap(fn);
    }

    /**
     * Takes an Optional and transforms it to a value depending
     * on the state of the Optional even if it is empty.
     *
     * @param opt the Optional
     * @param empty supplier in case Optional is empty
     * @param present function transforming the content of the Optional if present
     * @param <T> content type of the supplied Optional
     * @param <C> type of the resulting value
     * @return transformed value
     */
    public static <T, C> C fold(Optional<T> opt, Supplier<? extends C> empty,
            Function<? super T, ? extends C> present) {
        if (!opt.isPresent()) {
            return empty.get();
        }
        else {
           return present.apply(opt.get());
        }
    }
}
