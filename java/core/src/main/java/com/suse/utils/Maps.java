/*
 * Copyright (c) 2021--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Maps {

    private Maps() {
    }

    /**
     * Get the value from a nested map structure by a colon separated path.
     * E.g. key1:key2:key3 for a map with a depth of 3.
     * @param data the nested map
     * @param path the path
     * @return a value if available
     */
    public static Optional<Object> getValueByPath(Map<String, Object> data, String path) {
        String[] tokens = StringUtils.split(path, ":");
        Map<String, Object> current = data;
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            Object val = current.get(token);
            if (i == tokens.length - 1) {
                return Optional.ofNullable(val);
            }
            if (val == null) {
                return Optional.empty();
            }
            if (val instanceof Map) {
                current = (Map<String, Object>)val;
            }
            else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    /**
     * Converts a Map into a List of key-value pairs.
     *
     * <p>This method transforms a map like {@code {101 -> "Item A", 102 -> "Item B"}} into a
     * list of arrays structured as {@code [[101, "Item A"], [102, "Item B"]]}. This format is
     * useful for JSON serialization, as it can be directly deserialized into a JavaScript
     * {@code Map} by passing it to the {@code new Map()} constructor, preserving non-string keys.
     *
     * @param <K> the type of keys maintained by the map
     * @param <V> the type of mapped values
     * @param map the map to convert, not null
     * @return a new list containing key-value arrays for each entry in the source map.
     */
    public static <K, V> List<Object[]> mapToEntryList(Map<K, V> map) {
        if (map == null) {
            return null;
        }

        return map.entrySet().stream()
            .map(entry -> new Object[] {entry.getKey(), entry.getValue()})
            .toList();
    }

    /**
     * Inverts a multi-map represented as a {@code Map<K, List<V>>}.
     *
     * <p>This method transforms a map where each key is associated with a list of values
     * into a new map where each unique value from the original map's lists becomes a key.
     * Each new key is then associated with a list of all the original keys that mapped to it.
     *
     * @param <K> The type of the keys in the original map.
     * @param <V> The type of the values in the lists of the original map.
     * @param originalMap The map to invert, representing a one-to-many relationship. Must not be null.
     * @return A new map where keys are the values from the original map's lists, and
     * values are lists of the corresponding original keys. Returns an empty map
     * if the input is empty.
     */
    public static <K, V> Map<V, List<K>> invertMultimap(Map<K, List<V>> originalMap) {
        if (originalMap == null) {
            return null;
        }

        // Stream the entries Stream<{ K1 -> {V1, V2} }>
        return originalMap.entrySet().stream()
            // Map the stream to Stream<{V1, K1}, {V2, K1}>
            .flatMap(entry -> entry.getValue().stream().map(value -> ImmutablePair.of(value, entry.getKey())))
            // Group the swapped pairs with the original values as keys and the original keys in a set
            .collect(Collectors.groupingBy(
                Pair::getKey,
                Collectors.mapping(Pair::getValue, Collectors.toList())
            ));
    }
}
