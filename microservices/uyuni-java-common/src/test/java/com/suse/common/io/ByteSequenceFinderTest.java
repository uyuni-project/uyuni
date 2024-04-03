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

package com.suse.common.io;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

class ByteSequenceFinderTest {

    private static final Random randomSource = new Random();

    @ParameterizedTest(name = "Search sequence \"{1}\" within \"{1}\"")
    @DisplayName("Can find sequences of bytes within text")
    @MethodSource("textAndPatternProvider")
    void testSearch(String text, String pattern) throws IOException {
        byte[] data = text.getBytes(StandardCharsets.UTF_8);
        try (InputStream stream = new ByteArrayInputStream(data)) {
            ByteSequenceFinder finder = new ByteSequenceFinder(pattern.getBytes(StandardCharsets.UTF_8));

            // Ensure both search methods gives the same result of index of
            int foundPosition = text.indexOf(pattern);
            assertEquals(foundPosition, finder.search(stream));
            assertEquals(foundPosition, finder.search(data));
        }
    }

    @RepeatedTest(50)
    @DisplayName("Does not fail on random byte sequences")
    void testRandomly() throws IOException {
        String dataString = randomAlphanumeric(1024);
        String sequenceString = randomAlphanumeric(64);

        byte[] data = dataString.getBytes(StandardCharsets.UTF_8);
        byte[] sequence = sequenceString.getBytes(StandardCharsets.UTF_8);

        try (InputStream stream = new ByteArrayInputStream(data)) {
            ByteSequenceFinder finder = new ByteSequenceFinder();

            // Ensure it find itself
            stream.reset();
            finder.setSequence(data);

            assertEquals(0, finder.search(stream));
            assertEquals(0, finder.search(data));

            // Ensure we get the same result of indexOf
            stream.reset();
            finder.setSequence(sequence);

            assertEquals(dataString.indexOf(sequenceString), finder.search(stream));
            assertEquals(dataString.indexOf(sequenceString), finder.search(data));

            // Ensure we don't find something is not part of the data
            stream.reset();
            finder.setSequence("@#$%^&&*".getBytes(StandardCharsets.UTF_8));

            assertEquals(-1, finder.search(stream));
            assertEquals(-1, finder.search(data));
        }
    }

    @Test
    @DisplayName("Does not fail when the sequence contains -1")
    void testSequenceContainingMinusOne() throws IOException {
        byte[] sequence = {110, 29, -1, 107, 72};
        byte[] data = {32, 26, -1, -20, 69, 110, 29, -1, 107, 72, 120, 14, -26, -1, -68};
        try (InputStream stream = new ByteArrayInputStream(data)) {
            ByteSequenceFinder finder = new ByteSequenceFinder(sequence);
            assertEquals(5, finder.search(stream));
            assertEquals(5, finder.search(data));
        }
    }

    static Stream<Arguments> textAndPatternProvider() {
        return Stream.of(
            arguments("", ""),
            arguments("", "ab"),

            arguments("a", "a"),
            arguments("a", "b"),

            arguments("aaa", "aaaaa"),
            arguments("aaa", "abaaba"),
            arguments("abab", "abacababc"),
            arguments("abab", "babacaba"),

            arguments("aaacaaaaac", "aaacacaacaaacaaaacaaaaac"),
            arguments("ababcababdabababcababdaba", "ababcababdabababcababdaba")
        );
    }

    public static String randomAlphanumeric(int length) {
        String allowedChars = "aAbBcCdDeEfFgGhHiIjJkKlLmMnNoOpPqQrRsStTuUvVwWxXyYzZ0123456789";
        return IntStream.range(0, length)
            .mapToObj(idx -> Character.toString(allowedChars.charAt(randomSource.nextInt(allowedChars.length()))))
            .collect(Collectors.joining());
    }
}
