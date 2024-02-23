/*
 * Copyright (c) 2024 SUSE LLC
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

package com.suse.utils.test;

import static java.util.Objects.nonNull;
import static org.junit.Assert.assertEquals;

import com.suse.utils.Predicates;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Stream;

public class PredicatesTest {

    private static Stream<Arguments> expressionPredicates() {
        return Stream.of(
                Arguments.of(null, true, false, true, false, true, false),
                Arguments.of(null, true, false, true, false, true, false),
                Arguments.of(new Object(), false, true, false, true, true, false),
                Arguments.of(new String(), true, false, true, false, true, false),
                Arguments.of("", true, false, true, false, true, false),
                Arguments.of(" ", true, false, true, false, true, false),
                Arguments.of("null", false, true, false, true, false, true),
                Arguments.of(new String[0], true, false, true, false, true, false),
                Arguments.of(new String[]{}, true, false, true, false, true, false),
                Arguments.of(new String[]{"1"}, false, true, false, true, false, true),
                Arguments.of(new String[]{"1", null}, false, true, false, true, false, true),
                Arguments.of(new String[]{"1", null, "2"}, false, true, false, true, false, true),
                Arguments.of(new String[]{"1", "2", "3"}, false, true, false, true, false, true),
                Arguments.of(new String[]{null, null, "", " "}, true, false, true, false, true, false),
                Arguments.of(Arrays.asList(), true, false, true, false, true, false),
                Arguments.of(Arrays.asList(null, null), true, false, true, false, true, false),
                Arguments.of(Arrays.asList(null, new String()), true, false, true, false, true, false),
                Arguments.of(Arrays.asList(null, new String(), "not null"), false, true, false, true, false, true),
                Arguments.of(new ArrayList<>(), true, false, true, false, true, false),
                Arguments.of(new HashSet<>(), true, false, true, false, true, false),
                Arguments.of(new HashSet<>(Arrays.asList(1, 2, 3)), false, true, false, true, false, true),
                Arguments.of(Optional.of(1), false, true, false, true, false, true)
        );
    }

    @ParameterizedTest
    @MethodSource("expressionPredicates")
    public void testPredicates(
            Object value,
            boolean expectedIsAbsent,
            boolean expectedIsProvided,
            boolean expectedAllAbsent,
            boolean expectedAllProvided,
            boolean expectedNoneProvided,
            boolean expectedAnyProvided
    ) {
        assertEquals(expectedIsAbsent, Predicates.isAbsent(value));
        assertEquals(expectedIsProvided, Predicates.isProvided(value));
        assertEquals(expectedAllAbsent, Predicates.allAbsent(value));
        assertEquals(expectedAllProvided, Predicates.allProvided(value));
        if (nonNull(value) && Collection.class.isAssignableFrom(value.getClass())) {
            assertEquals(expectedNoneProvided, Predicates.noneProvided((Collection<?>) value));
            assertEquals(expectedAnyProvided, Predicates.anyProvided((Collection<?>) value));
        }
    }

}
