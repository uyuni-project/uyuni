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

package com.suse.utils.test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.suse.utils.Maps;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MapsTest {

    @Test
    public void testGetValueByPath() {
        Map<String, Object> map = new Yaml().load(getClass().getResourceAsStream("provider-metadata.yml"));

        Optional<Object> val = Maps.getValueByPath(map, "cluster:management_node:match");
        assertTrue(val.isPresent());
        assertEquals("I@caasp:management_node:true", val.get());
    }

    @Test
    public void testGetValueByPathWrongPath() {
        Map<String, Object> map = new Yaml().load(getClass().getResourceAsStream("provider-metadata.yml"));

        Optional<Object> val = Maps.getValueByPath(map, "cluster:foo:bar");
        assertTrue(val.isEmpty());

        val = Maps.getValueByPath(map, "foo:bar");
        assertTrue(val.isEmpty());

        val = Maps.getValueByPath(map, ":xxx");
        assertTrue(val.isEmpty());
    }

    @Nested
    class MapToEntryListTests {

        @Test
        void shouldReturnNullWhenMapIsNull() {
            assertNull(Maps.mapToEntryList(null));
        }

        @Test
        void shouldReturnEmptyListWhenMapIsEmpty() {
            List<Object[]> result = Maps.mapToEntryList(Map.of());

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        void canConvertMapToEntryList() {
            // Arrange
            Map<String, Integer> productMap = new LinkedHashMap<>();
            productMap.put("Product A", 101);
            productMap.put("Product B", 255);

            // Act
            List<Object[]> result = Maps.mapToEntryList(productMap);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size(), "The list should contain two entries.");
            assertArrayEquals(new Object[]{"Product A", 101}, result.get(0), "First entry should match.");
            assertArrayEquals(new Object[]{"Product B", 255}, result.get(1), "Second entry should match.");
        }

        @Test
        void canConvertMapToEntryListWithMixedTypes() {
            // Arrange
            Map<Long, Object> mixedMap = new LinkedHashMap<>();
            mixedMap.put(1L, "A String Value");
            mixedMap.put(2L, 12345);
            mixedMap.put(3L, true);

            // Act
            List<Object[]> result = Maps.mapToEntryList(mixedMap);

            // Assert
            assertNotNull(result);
            assertEquals(3, result.size(), "The list should contain three entries.");
            assertArrayEquals(new Object[]{1L, "A String Value"}, result.get(0));
            assertArrayEquals(new Object[]{2L, 12345}, result.get(1));
            assertArrayEquals(new Object[]{3L, true}, result.get(2));
        }
    }

    @Nested
    class InvertMultiMapTests {

        @Test
        public void canInvertMultiMap() {
            // Arrange
            Map<String, List<String>> authorToGenres = Map.of(
                "Author A", List.of("Fantasy", "Sci-Fi"),
                "Author B", List.of("Sci-Fi", "Dystopian"),
                "Author C", List.of("Fantasy")
            );

            // Act
            Map<String, List<String>> genreToAuthors = Maps.invertMultimap(authorToGenres);

            // Assert
            assertEquals(3, genreToAuthors.size());

            // Use Sets for comparison to ignore list ordering
            assertEquals(new HashSet<>(List.of("Author A", "Author C")), new HashSet<>(genreToAuthors.get("Fantasy")));
            assertEquals(new HashSet<>(List.of("Author A", "Author B")), new HashSet<>(genreToAuthors.get("Sci-Fi")));
            assertEquals(new HashSet<>(List.of("Author B")), new HashSet<>(genreToAuthors.get("Dystopian")));
        }

        @Test
        void invertMultimapWithEmptyLists() {
            // Arrange
            Map<String, List<String>> mapWithEmptyLists = new HashMap<>();
            mapWithEmptyLists.put("Author A", List.of("Horror"));
            mapWithEmptyLists.put("Author B",
                Collections.emptyList()); // This author should not appear in the output values
            mapWithEmptyLists.put("Author C", List.of("Horror", "Thriller"));

            // Act
            Map<String, List<String>> result = Maps.invertMultimap(mapWithEmptyLists);

            // Assert
            assertEquals(2, result.size());
            assertTrue(result.containsKey("Horror"));
            assertTrue(result.containsKey("Thriller"));
            assertFalse(result.containsKey("Author B")); // Ensure no key was created from the empty list

            assertEquals(new HashSet<>(List.of("Author A", "Author C")), new HashSet<>(result.get("Horror")));
            assertEquals(new HashSet<>(List.of("Author C")), new HashSet<>(result.get("Thriller")));
        }

        @Test
        void invertMultimapReturnsNullWhenInputIsNull() {
            assertNull(Maps.invertMultimap(null));
        }
    }
}
