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

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

/**
 * Converts HTML fragments to plain text using the lenient JDK HTML parser.
 */
class HtmlToPlainText {
    private static final Logger LOG = LogManager.getLogger(HtmlToPlainText.class);

    /**
     * Converts an HTML fragment to plain text.
     *
     * @param html the HTML fragment
     * @return the plain-text representation, or the original fragment if parsing fails
     */
    public String convert(String html) {
        if (html == null) {
            return null;
        }

        PlainTextCallback callback = new PlainTextCallback();
        try {
            new ParserDelegator().parse(new StringReader(html), callback, true);
            return callback.getPlainText();
        }
        catch (IOException e) {
            LOG.warn("Couldn't parse the HTML fragment -> [{}]", html, e);
            return html;
        }
    }

    private static class PlainTextCallback extends HTMLEditorKit.ParserCallback {
        private static final String IGNORABLES = ".,;'\"?";

        private final StringBuilder plainText = new StringBuilder();
        private String href;
        private boolean anchorHasText;

        @Override
        public void handleStartTag(HTML.Tag tag, MutableAttributeSet attributes, int position) {
            if (HTML.Tag.A.equals(tag)) {
                Object hrefAttribute = attributes.getAttribute(HTML.Attribute.HREF);
                href = hrefAttribute == null ? null : StringUtils.trimToNull(hrefAttribute.toString());
                anchorHasText = false;
            }
        }

        @Override
        public void handleEndTag(HTML.Tag tag, int position) {
            if (HTML.Tag.A.equals(tag)) {
                if (anchorHasText && href != null) {
                    plainText.append(" (").append(href).append(")");
                }
                href = null;
                anchorHasText = false;
            }
        }

        @Override
        public void handleText(char[] data, int position) {
            String text = StringUtils.normalizeSpace(new String(data));
            if (StringUtils.isBlank(text)) {
                return;
            }

            if (!plainText.isEmpty() && !IGNORABLES.contains(text)) {
                plainText.append(" ");
            }
            plainText.append(text);
            if (href != null) {
                anchorHasText = true;
            }
        }

        public String getPlainText() {
            return plainText.toString();
        }
    }
}
