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
package com.redhat.rhn.taskomatic.task.repomd;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import javax.xml.transform.Result;

/**
 * An {@link ContentHandler} that does no escaping. Be very careful!
 */
public class UnescapingContentHandler implements ContentHandler {

    private final ContentHandler delegate;

    /**
     * Default constructor
     * @param delegateIn the instance to wrap and delegate
     */
    public UnescapingContentHandler(ContentHandler delegateIn) {
        this.delegate = delegateIn;
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        // Send a hint to the Transformer to disable escaping for this chunk
        delegate.processingInstruction(Result.PI_DISABLE_OUTPUT_ESCAPING, "");
        delegate.characters(ch, start, length);
        delegate.processingInstruction(Result.PI_ENABLE_OUTPUT_ESCAPING, "");
    }

    // Just delegate the other methods

    @Override
    public void setDocumentLocator(Locator locator) {
        delegate.setDocumentLocator(locator);
    }

    @Override
    public void startDocument() throws SAXException {
        delegate.startDocument();
    }

    @Override
    public void endDocument() throws SAXException {
        delegate.endDocument();
    }

    @Override
    public void startPrefixMapping(String s, String s1) throws SAXException {
        delegate.startPrefixMapping(s, s1);
    }

    @Override
    public void endPrefixMapping(String s) throws SAXException {
        delegate.endPrefixMapping(s);
    }

    @Override
    public void startElement(String s, String s1, String s2, Attributes attributes) throws SAXException {
        delegate.startElement(s, s1, s2, attributes);
    }

    @Override
    public void endElement(String s, String s1, String s2) throws SAXException {
        delegate.endElement(s, s1, s2);
    }

    @Override
    public void ignorableWhitespace(char[] chars, int i, int i1) throws SAXException {
        delegate.ignorableWhitespace(chars, i, i1);
    }

    @Override
    public void processingInstruction(String s, String s1) throws SAXException {
        delegate.processingInstruction(s, s1);
    }

    @Override
    public void skippedEntity(String s) throws SAXException {
        delegate.skippedEntity(s);
    }

    @Override
    public void declaration(String version, String encoding, String standalone) throws SAXException {
        delegate.declaration(version, encoding, standalone);
    }
}
