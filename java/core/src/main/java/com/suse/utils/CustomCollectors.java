/*
 * Copyright (c) 2025 SUSE LLC
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

package com.suse.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public final class CustomCollectors {

    private CustomCollectors() {
        // Prevent instantiation
    }

    /**
     * Returns a Collector implementing a "group by" operation on input elements of type T, grouping elements according
     * to a classification function, and returning the results in a Map.
     *
     * <p>The classification function maps elements to some key type K. The collector produces a
     * Map&lt;K, List&lt;V&gt;&gt; whose keys are the values resulting from applying the classification function to
     * the input elements, and whose corresponding values are Lists containing the input elements which map to the
     * associated key under the classification function.
     *
     * <p>In contrast with the default {@link Collectors#groupingBy(Function)}, this implementation is designed to
     * handle null as key values.
     *
     * @param <T>         The type of input elements.
     * @param <K>         The type of the keys in the resulting map.
     * @param classifier  The classifier function mapping input elements to keys.
     * @return A {@link Collector} that groups elements into a {@link Map}.
     */
    public static <T, K> Collector<T, ?, Map<K, List<T>>> nullSafeGroupingBy(
        Function<? super T, ? extends K> classifier) {
        return nullSafeGroupingBy(classifier, Function.identity());
    }


    /**
     * Returns a Collector implementing a "group by" operation on input elements of type T, grouping elements according
     * to a classification function, and returning the results in a Map.
     *
     * <p>The classification function maps elements to some key type K. The collector produces a
     * Map&lt;K, List&lt;V&gt;&gt; whose keys are the values resulting from applying the classification function to
     * the input elements, and whose corresponding values are Lists containing the result of the application of the
     * value mapping function.
     *
     * <p>In contrast with the default {@link Collectors#groupingBy(Function)}, this implementation is designed to
     * handle null as key values.
     *
     * @param <T>         The type of input elements.
     * @param <K>         The type of the keys in the resulting map.
     * @param <V>         The type of the values in the resulting lists.
     * @param classifier  The classifier function mapping input elements to keys.
     * @param mapper      The mapper function, mapping input elements to the result list values
     * @return A {@link Collector} that groups elements into a {@link Map}.
     */
    public static <T, K, V> Collector<T, ?, Map<K, List<V>>> nullSafeGroupingBy(
        Function<? super T, ? extends K> classifier, Function<T, V> mapper) {
        return Collectors.toMap(
            classifier,
            item -> List.of(mapper.apply(item)),
            (list1, list2) -> Lists.union(list1, list2),
            () -> new HashMap<>()
        );
    }
}
