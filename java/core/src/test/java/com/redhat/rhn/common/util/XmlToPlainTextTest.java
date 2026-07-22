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
package com.redhat.rhn.common.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

class XmlToPlainTextTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("conversionCases")
    @DisplayName("Convert snippets into plain text")
    void convertSnippetToPlainText(String caseName, String snippet, String expected) {
        XmlToPlainText helper = new XmlToPlainText();
        assertEquals(expected, helper.convert(snippet));
    }

    @Test
    @DisplayName("Return original snippet when XML is malformed")
    void returnOriginalSnippetWhenXmlIsMalformed() {
        XmlToPlainText helper = new XmlToPlainText();
        String snippet = "<p>broken";
        assertEquals(snippet, helper.convert(snippet));
    }

    @Test
    @DisplayName("Does not throw when anchor has no href")
    void doesNotThrowWhenAnchorHasNoHref() {
        XmlToPlainText helper = new XmlToPlainText();
        assertEquals("text", helper.convert("<a>text</a>"));
    }

    @Test
    @DisplayName("Does not append href when anchor href is only whitespace")
    void ignoresWhitespaceOnlyHref() {
        XmlToPlainText helper = new XmlToPlainText();
        assertEquals("text", helper.convert("<a href=\"   \">text</a>"));
    }

    @Test
    @DisplayName("Skips unsupported nodes while preserving text content")
    void skipsUnsupportedJdomNodes() {
        XmlToPlainText helper = new XmlToPlainText();
        String snippet = "before<!-- comment --><![CDATA[cdata-text]]><?target data?>after";
        assertEquals("before cdata-text after", helper.convert(snippet));
    }

    @ParameterizedTest(name = "{0} converts to empty string")
    @ValueSource(strings = {"<p></p>", "<p>   \n\t </p>", "<p/>"})
    @DisplayName("Ignores empty or whitespace-only element text")
    void ignoresEmptyOrWhitespaceOnlyElementText(String snippet) {
        XmlToPlainText helper = new XmlToPlainText();
        assertEquals("", helper.convert(snippet));
    }

    static Stream<Arguments> conversionCases() {
        return Stream.of(
            Arguments.of("return plain text unchanged",
                "hello world", "hello world"),
            Arguments.of("flatten nested tags and preserve punctuation spacing",
                "<p>Hello <strong>world</strong><em>.</em></p>", "Hello world."),
            Arguments.of("keep leading punctuation when it is first parsed token",
                "<em>?</em><span>Hello</span>", "? Hello"),
            Arguments.of("join consecutive punctuation tokens without spaces",
                "<em>.</em><em>.</em>", ".."),
            Arguments.of("do not add space before comma from separate node",
                "<strong>Hello</strong><em>,</em><span>world</span>", "Hello, world"),
            Arguments.of("do not add space before semicolon from separate node",
                "<strong>Hello</strong><em>;</em><span>world</span>", "Hello; world"),
            Arguments.of("append href after anchor text",
                "<a href=\"http://example.com\">click here</a>", "click here (http://example.com)"),
            Arguments.of("append href for each anchor",
                "<a href=\"u1\">first</a> and <a href=\"u2\">second</a>", "first (u1) and second (u2)")
        );
    }
}
