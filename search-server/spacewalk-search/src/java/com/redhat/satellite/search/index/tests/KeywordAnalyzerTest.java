/*
 * Copyright (c) 2010--2015 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.satellite.search.index.tests;

import com.redhat.satellite.search.index.KeywordAnalyzer;

import org.apache.lucene.analysis.TokenStream;

import junit.framework.TestCase;

import java.io.StringReader;

public class KeywordAnalyzerTest extends TestCase {

    public void processString(String originalValue) throws Exception {
        KeywordAnalyzer ka = new KeywordAnalyzer();
        StringReader sr = new StringReader(originalValue);
        TokenStream ts = ka.tokenStream("ignoredField", sr);
        assertTrue("Text Should be Untouched", new String(ts.next().termBuffer()).trim().
                        compareTo(originalValue) == 0);
        assertTrue("Token should be null", ts.next() == null);
    }
    public void testBasicParse() throws Exception {
        processString("i386");
        processString("bx-gh-3&^0-993$#@!%^&*()-=+_><?/.,';:[]}{)");
        processString("j839,.     43    ..,.-=-=`1~!@#");
    }
}
