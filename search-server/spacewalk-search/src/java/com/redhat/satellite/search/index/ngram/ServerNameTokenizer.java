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
package com.redhat.satellite.search.index.ngram;

import org.apache.lucene.analysis.CharTokenizer;

import java.io.Reader;

/**
 * Tokenizer that breaks text on dots, hyphens, and whitespace.
 * Preserves alphanumeric sequences intact for server name indexing.
 */
public class ServerNameTokenizer extends CharTokenizer {

    public ServerNameTokenizer(Reader input) {
        super(input);
    }

    @Override
    protected boolean isTokenChar(char c) {
        return c != '.' && c != '-' && !Character.isWhitespace(c);
    }
}
