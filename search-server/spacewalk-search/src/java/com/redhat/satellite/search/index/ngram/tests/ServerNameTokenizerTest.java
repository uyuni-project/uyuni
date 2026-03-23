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
package com.redhat.satellite.search.index.ngram.tests;

import com.redhat.satellite.search.index.ngram.ServerNameTokenizer;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class ServerNameTokenizerTest extends NGramTestSetup {

    private static Logger log = LogManager.getLogger(ServerNameTokenizerTest.class);

    public ServerNameTokenizerTest() {
        super();
    }

    public void testSplitOnDots() throws Exception {
        ServerNameTokenizer tokenizer = new ServerNameTokenizer(new StringReader("appserver03.demo.center"));

        List<String> tokens = collectTokens(tokenizer);

        assertEquals("Should split into 3 tokens", 3, tokens.size());
        assertEquals("appserver03", tokens.get(0));
        assertEquals("demo", tokens.get(1));
        assertEquals("center", tokens.get(2));
    }

    public void testSplitOnHyphens() throws Exception {
        ServerNameTokenizer tokenizer = new ServerNameTokenizer(new StringReader("test-demo-fw-01"));

        List<String> tokens = collectTokens(tokenizer);

        assertEquals("Should split into 4 tokens", 4, tokens.size());
        assertEquals("test", tokens.get(0));
        assertEquals("demo", tokens.get(1));
        assertEquals("fw", tokens.get(2));
        assertEquals("01", tokens.get(3));
    }

    public void testSplitOnMixedDelimiters() throws Exception {
        ServerNameTokenizer tokenizer = new ServerNameTokenizer(new StringReader("test-demo-fw-01.demo.center"));

        List<String> tokens = collectTokens(tokenizer);

        assertEquals("Should split into 6 tokens", 6, tokens.size());
        assertEquals("test", tokens.get(0));
        assertEquals("demo", tokens.get(1));
        assertEquals("fw", tokens.get(2));
        assertEquals("01", tokens.get(3));
        assertEquals("demo", tokens.get(4));
        assertEquals("center", tokens.get(5));
    }

    public void testMultipleConsecutiveDelimiters() throws Exception {
        ServerNameTokenizer tokenizer = new ServerNameTokenizer(new StringReader("a--b...c"));

        List<String> tokens = collectTokens(tokenizer);

        assertEquals("Should handle consecutive delimiters", 3, tokens.size());
        assertEquals("a", tokens.get(0));
        assertEquals("b", tokens.get(1));
        assertEquals("c", tokens.get(2));
    }

    public void testPreserveAlphanumeric() throws Exception {
        ServerNameTokenizer tokenizer = new ServerNameTokenizer(new StringReader("appserver03"));

        List<String> tokens = collectTokens(tokenizer);

        assertEquals("Should preserve alphanumeric token", 1, tokens.size());
        assertEquals("appserver03", tokens.get(0));
    }

    public void testNoDelimiters() throws Exception {
        ServerNameTokenizer tokenizer = new ServerNameTokenizer(new StringReader("hostname"));

        List<String> tokens = collectTokens(tokenizer);

        assertEquals("Should return original token", 1, tokens.size());
        assertEquals("hostname", tokens.get(0));
    }

    public void testEmptyParts() throws Exception {
        ServerNameTokenizer tokenizer = new ServerNameTokenizer(new StringReader(".leading"));

        List<String> tokens = collectTokens(tokenizer);

        assertEquals("Should skip empty parts", 1, tokens.size());
        assertEquals("leading", tokens.get(0));
    }

    public void testReset() throws Exception {
        ServerNameTokenizer tokenizer = new ServerNameTokenizer(new StringReader("a.b.c"));

        List<String> firstRun = collectTokens(tokenizer);
        assertEquals("First run should have 3 tokens", 3, firstRun.size());
        assertEquals("a", firstRun.get(0));
        assertEquals("b", firstRun.get(1));
        assertEquals("c", firstRun.get(2));

        tokenizer.reset(new StringReader("x.y.z"));

        List<String> secondRun = collectTokens(tokenizer);
        assertEquals("Second run should have 3 tokens", 3, secondRun.size());
        assertEquals("x", secondRun.get(0));
        assertEquals("y", secondRun.get(1));
        assertEquals("z", secondRun.get(2));
    }

    public void testOffsets() throws Exception {
        ServerNameTokenizer tokenizer = new ServerNameTokenizer(new StringReader("web.demo.center"));

        Token token1 = tokenizer.next();
        assertEquals("web", new String(token1.termBuffer(), 0, token1.termLength()));
        assertEquals(0, token1.startOffset());
        assertEquals(3, token1.endOffset());

        Token token2 = tokenizer.next();
        assertEquals("demo", new String(token2.termBuffer(), 0, token2.termLength()));
        assertEquals(4, token2.startOffset());
        assertEquals(7, token2.endOffset());

        Token token3 = tokenizer.next();
        assertEquals("center", new String(token3.termBuffer(), 0, token3.termLength()));
        assertEquals(8, token3.startOffset());
        assertEquals(14, token3.endOffset());

        Token token4 = tokenizer.next();
        assertNull("Should be no more tokens", token4);
    }

    public void testOffsetWithConsecutiveDelimiters() throws Exception {
        ServerNameTokenizer tokenizer = new ServerNameTokenizer(new StringReader("a--b"));

        Token token1 = tokenizer.next();
        assertEquals("a", new String(token1.termBuffer(), 0, token1.termLength()));
        assertEquals(0, token1.startOffset());
        assertEquals(1, token1.endOffset());

        Token token2 = tokenizer.next();
        assertEquals("b", new String(token2.termBuffer(), 0, token2.termLength()));
        assertEquals(3, token2.startOffset());
        assertEquals(4, token2.endOffset());
    }

    private List<String> collectTokens(TokenStream ts) throws Exception {
        List<String> tokens = new ArrayList<String>();
        Token token;
        while ((token = ts.next()) != null) {
            String text = new String(token.termBuffer(), 0, token.termLength());
            tokens.add(text);
            if (log.isDebugEnabled()) {
                log.debug("Token: [" + text + "] offset: " +
                         token.startOffset() + "-" + token.endOffset());
            }
        }
        return tokens;
    }
}
