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

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.StringReader;
import java.util.Locale;
import java.util.Map;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

/**
 * Converts HTML fragments to plain text using the lenient JDK HTML parser,
 * which avoids adding another HTML parsing dependency.
 */
final class HtmlToPlainText {
    private static final Map<String, String> HTML5_TAG_REPLACEMENTS = Map.ofEntries(
            Map.entry("abbr", "span"),
            Map.entry("article", "div"),
            Map.entry("del", "span"),
            Map.entry("ins", "span"),
            Map.entry("main", "div"),
            Map.entry("nav", "div"),
            Map.entry("section", "div"),
            Map.entry("tbody", "div"),
            Map.entry("tfoot", "div"),
            Map.entry("thead", "div"),
            Map.entry("time", "span")
    );
    private static final Logger LOG = LogManager.getLogger(HtmlToPlainText.class);

    private HtmlToPlainText() { }

    /**
     * Converts an HTML fragment to plain text.
     *
     * @param html the HTML fragment
     * @return the plain-text representation, or the original fragment if parsing fails
     */
    public static String convert(String html) {
        if (html == null) {
            return null;
        }

        PlainTextCallback callback = new PlainTextCallback();
        try {
            boolean preserveSourceLineBreaks = !containsMarkup(html);
            new ParserDelegator().parse(
                    new StringReader(prepareHtml(html, preserveSourceLineBreaks)), callback, true);
            return callback.getPlainText();
        }
        catch (IOException e) {
            LOG.warn("Couldn't parse the HTML fragment -> [{}]", html, e);
            return html;
        }
    }

    private static boolean containsMarkup(String html) {
        int position = html.indexOf('<');
        while (position >= 0) {
            if (getMarkupEnd(html, position) >= 0) {
                return true;
            }
            position = html.indexOf('<', position + 1);
        }
        return false;
    }

    private static String prepareHtml(String html, boolean preserveSourceLineBreaks) {
        StringBuilder result = new StringBuilder(html.length());
        int position = 0;
        while (position < html.length()) {
            int lessThan = html.indexOf('<', position);
            if (lessThan < 0) {
                appendText(result, html, position, html.length(), preserveSourceLineBreaks);
                break;
            }

            appendText(result, html, position, lessThan, preserveSourceLineBreaks);
            int markupEnd = getMarkupEnd(html, lessThan);
            if (markupEnd >= 0) {
                String tagName = getTagName(html, lessThan, markupEnd);
                if (!isEndTag(html, lessThan) && !isSelfClosingTag(html, lessThan, markupEnd) &&
                        isSuppressedTag(tagName)) {
                    int closingTag = findClosingTag(html, tagName, markupEnd + 1);
                    if (closingTag < 0) {
                        break;
                    }
                    int closingTagEnd = getMarkupEnd(html, closingTag);
                    if (closingTagEnd < 0) {
                        break;
                    }
                    position = closingTagEnd + 1;
                    continue;
                }
                appendMarkup(result, html, lessThan, markupEnd);
                position = markupEnd + 1;
            }
            else {
                result.append("&lt;");
                position = lessThan + 1;
            }
        }
        return result.toString();
    }

    private static void appendMarkup(StringBuilder result, String html, int start, int end) {
        boolean endTag = isEndTag(html, start);
        String tagName = getTagName(html, start, end);
        String replacement = HTML5_TAG_REPLACEMENTS.get(tagName);
        if (replacement == null) {
            result.append(html, start, end + 1);
        }
        else {
            result.append(endTag ? "</" : "<").append(replacement).append(">");
        }
    }

    private static String getTagName(String html, int start, int end) {
        int nameStart = start + 1;
        if (isEndTag(html, start)) {
            nameStart++;
        }
        int nameEnd = nameStart;
        while (nameEnd < end && Character.isLetterOrDigit(html.charAt(nameEnd))) {
            nameEnd++;
        }
        return html.substring(nameStart, nameEnd).toLowerCase(Locale.ROOT);
    }

    private static boolean isEndTag(String html, int start) {
        return start + 1 < html.length() && html.charAt(start + 1) == '/';
    }

    private static boolean isSelfClosingTag(String html, int start, int end) {
        int position = end - 1;
        while (position > start && Character.isWhitespace(html.charAt(position))) {
            position--;
        }
        return html.charAt(position) == '/';
    }

    private static boolean isSuppressedTag(String tagName) {
        return "script".equals(tagName) || "style".equals(tagName);
    }

    private static int findClosingTag(String html, String tagName, int start) {
        int position = html.indexOf('<', start);
        String closingTag = "/" + tagName;
        while (position >= 0) {
            int nameStart = position + 1;
            if (html.regionMatches(true, nameStart, closingTag, 0, closingTag.length())) {
                int boundary = nameStart + closingTag.length();
                if (boundary < html.length() && isTagBoundary(html.charAt(boundary))) {
                    return position;
                }
            }
            position = html.indexOf('<', position + 1);
        }
        return -1;
    }

    private static void appendText(StringBuilder result, String html, int start, int end,
            boolean preserveSourceLineBreaks) {
        if (!preserveSourceLineBreaks) {
            result.append(html, start, end);
            return;
        }

        int position = start;
        while (position < end) {
            char character = html.charAt(position);
            if (isSourceLineBreak(character)) {
                int lineBreaks = 0;
                while (position < end && isSourceLineBreak(html.charAt(position))) {
                    if (html.charAt(position) == '\r' && position + 1 < end && html.charAt(position + 1) == '\n') {
                        position++;
                    }
                    lineBreaks++;
                    position++;
                }
                result.append(lineBreaks > 1 ? "<p>" : "<br>");
            }
            else {
                result.append(character);
                position++;
            }
        }
    }

    private static boolean isSourceLineBreak(char character) {
        return character == '\r' || character == '\n';
    }

    private static int getMarkupEnd(String value, int position) {
        if (value.startsWith("<!--", position)) {
            int commentEnd = value.indexOf("-->", position + 4);
            return commentEnd < 0 ? -1 : commentEnd + 2;
        }

        int nameStart = position + 1;
        if (nameStart < value.length() && value.charAt(nameStart) == '/') {
            nameStart++;
        }
        if (nameStart >= value.length() || !Character.isLetter(value.charAt(nameStart))) {
            return -1;
        }

        int nameEnd = nameStart + 1;
        while (nameEnd < value.length() && Character.isLetterOrDigit(value.charAt(nameEnd))) {
            nameEnd++;
        }
        if (nameEnd >= value.length() || !isTagBoundary(value.charAt(nameEnd))) {
            return -1;
        }

        String tagName = value.substring(nameStart, nameEnd).toLowerCase(Locale.ROOT);
        if (HTML.getTag(tagName) == null && !HTML5_TAG_REPLACEMENTS.containsKey(tagName)) {
            return -1;
        }
        return findTagEnd(value, nameEnd);
    }

    private static boolean isTagBoundary(char character) {
        return Character.isWhitespace(character) || character == '/' || character == '>';
    }

    private static int findTagEnd(String value, int position) {
        char quote = 0;
        for (int index = position; index < value.length(); index++) {
            char character = value.charAt(index);
            if (quote != 0) {
                if (character == quote) {
                    quote = 0;
                }
            }
            else if (character == '\'' || character == '"') {
                quote = character;
            }
            else if (character == '>') {
                return index;
            }
        }
        return -1;
    }

    private static class PlainTextCallback extends HTMLEditorKit.ParserCallback {
        private static final int NO_SEPARATOR = 0;
        private static final int SPACE_SEPARATOR = 1;
        private static final int LINE_SEPARATOR = 2;
        private static final int PARAGRAPH_SEPARATOR = 3;

        private final StringBuilder plainText = new StringBuilder();
        private String href;
        private boolean anchorHasText;
        private int anchorTextStart;
        private int pendingSeparator;

        @Override
        public void handleStartTag(HTML.Tag tag, MutableAttributeSet attributes, int position) {
            requestTagSeparator(tag);
            if (HTML.Tag.A.equals(tag)) {
                Object hrefAttribute = attributes.getAttribute(HTML.Attribute.HREF);
                href = hrefAttribute == null ? null : StringUtils.trimToNull(hrefAttribute.toString());
                anchorHasText = false;
                anchorTextStart = plainText.length();
            }
        }

        @Override
        public void handleEndTag(HTML.Tag tag, int position) {
            if (HTML.Tag.A.equals(tag)) {
                String anchorText = plainText.substring(anchorTextStart).trim();
                if (anchorHasText && href != null && !href.equals(anchorText)) {
                    plainText.append(" (").append(href).append(")");
                }
                href = null;
                anchorHasText = false;
                anchorTextStart = 0;
            }
            requestTagSeparator(tag);
        }

        @Override
        public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet attributes, int position) {
            requestTagSeparator(tag);
        }

        @Override
        public void handleText(char[] data, int position) {
            int index = 0;
            while (index < data.length) {
                if (isLineBreak(data[index])) {
                    int lineBreaks = 0;
                    while (index < data.length && isWhitespace(data[index])) {
                        if (data[index] == '\r') {
                            lineBreaks++;
                            if (index + 1 < data.length && data[index + 1] == '\n') {
                                index++;
                            }
                        }
                        else if (data[index] == '\n') {
                            lineBreaks++;
                        }
                        index++;
                    }
                    requestSeparator(lineBreaks > 1 ? PARAGRAPH_SEPARATOR : LINE_SEPARATOR);
                }
                else if (isWhitespace(data[index])) {
                    requestSpace();
                    while (index < data.length && isWhitespace(data[index]) && !isLineBreak(data[index])) {
                        index++;
                    }
                }
                else {
                    int textStart = index;
                    while (index < data.length && !isWhitespace(data[index])) {
                        index++;
                    }
                    appendPendingSeparator();
                    plainText.append(data, textStart, index - textStart);
                    if (href != null) {
                        anchorHasText = true;
                    }
                }
            }
        }

        private void requestSpace() {
            requestSeparator(SPACE_SEPARATOR);
        }

        private void requestTagSeparator(HTML.Tag tag) {
            if (isParagraphTag(tag)) {
                requestSeparator(PARAGRAPH_SEPARATOR);
            }
            else if (isLineTag(tag)) {
                requestSeparator(LINE_SEPARATOR);
            }
            else if (isSpaceTag(tag)) {
                requestSeparator(SPACE_SEPARATOR);
            }
        }

        private void requestSeparator(int separator) {
            if (!plainText.isEmpty()) {
                pendingSeparator = Math.max(pendingSeparator, separator);
            }
        }

        private void appendPendingSeparator() {
            if (plainText.isEmpty()) {
                pendingSeparator = NO_SEPARATOR;
                return;
            }

            if (pendingSeparator == PARAGRAPH_SEPARATOR) {
                plainText.append("\n\n");
            }
            else if (pendingSeparator == LINE_SEPARATOR) {
                plainText.append("\n");
            }
            else if (pendingSeparator == SPACE_SEPARATOR) {
                plainText.append(" ");
            }
            pendingSeparator = NO_SEPARATOR;
        }

        private static boolean isWhitespace(char character) {
            return Character.isWhitespace(character) || Character.isSpaceChar(character);
        }

        private static boolean isLineBreak(char character) {
            return character == '\r' || character == '\n';
        }

        private static boolean isParagraphTag(HTML.Tag tag) {
            return HTML.Tag.P.equals(tag) || HTML.Tag.DIV.equals(tag) ||
                    HTML.Tag.H1.equals(tag) || HTML.Tag.H2.equals(tag) || HTML.Tag.H3.equals(tag) ||
                    HTML.Tag.H4.equals(tag) || HTML.Tag.H5.equals(tag) || HTML.Tag.H6.equals(tag) ||
                    HTML.Tag.BLOCKQUOTE.equals(tag) || HTML.Tag.PRE.equals(tag) ||
                    HTML.Tag.UL.equals(tag) || HTML.Tag.OL.equals(tag) || HTML.Tag.DL.equals(tag) ||
                    HTML.Tag.TABLE.equals(tag);
        }

        private static boolean isLineTag(HTML.Tag tag) {
            return HTML.Tag.BR.equals(tag) || HTML.Tag.LI.equals(tag) || HTML.Tag.DT.equals(tag) ||
                    HTML.Tag.DD.equals(tag) || HTML.Tag.HR.equals(tag) || HTML.Tag.TR.equals(tag);
        }

        private static boolean isSpaceTag(HTML.Tag tag) {
            return HTML.Tag.TD.equals(tag) || HTML.Tag.TH.equals(tag);
        }

        public String getPlainText() {
            return plainText.toString();
        }
    }
}
