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
import com.redhat.satellite.search.index.KeywordAnalyzer;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ngram.NGramTokenFilter;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.RAMDirectory;

public class ServerNameAnalyzerTest extends NGramTestSetup {

    private static class TestServerNameAnalyzer extends Analyzer {
        private final int minNgram = 1;
        private final int maxNgram = 5;

        public TokenStream tokenStream(String fieldName, java.io.Reader reader) {
            TokenStream result = new ServerNameTokenizer(reader);
            result = new LowerCaseFilter(result);
            result = new NGramTokenFilter(result, minNgram, maxNgram);
            return result;
        }
    }

    public void testFineGrainedSearchFindsHyphenatedHostname() throws Exception {
        RAMDirectory dir = new RAMDirectory();

        Analyzer defaultAnalyzer = new TestServerNameAnalyzer();
        PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(defaultAnalyzer);
        analyzer.addAnalyzer("name", new TestServerNameAnalyzer());
        analyzer.addAnalyzer("hostname", new TestServerNameAnalyzer());

        IndexWriter writer = new IndexWriter(dir, analyzer, true);

        Document doc = new Document();
        doc.add(new Field("name", "test-server-fw-01.example.com", Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field("hostname", "test-server-fw-01.example.com", Field.Store.YES, Field.Index.TOKENIZED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(dir);
        QueryParser parser = new QueryParser("name", analyzer);
        Query query = parser.parse("example.com");

        Hits hits = searcher.search(query);

        assertEquals("Should find hyphenated hostname with fine-grained search", 1, hits.length());
        assertEquals("test-server-fw-01.example.com", hits.doc(0).get("name"));

        searcher.close();
    }

    public void testHostnameFieldQueryWithHyphens() throws Exception {
        RAMDirectory dir = new RAMDirectory();

        Analyzer defaultAnalyzer = new TestServerNameAnalyzer();
        PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(defaultAnalyzer);
        analyzer.addAnalyzer("hostname", new TestServerNameAnalyzer());

        IndexWriter writer = new IndexWriter(dir, analyzer, true);

        Document doc = new Document();
        doc.add(new Field("name", "server01", Field.Store.YES, Field.Index.TOKENIZED));
        doc.add(new Field("hostname", "web-app-srv-01.domain.local", Field.Store.YES, Field.Index.TOKENIZED));
        writer.addDocument(doc);
        writer.close();

        IndexSearcher searcher = new IndexSearcher(dir);
        QueryParser parser = new QueryParser("hostname", analyzer);
        Query query = parser.parse("domain.local");

        Hits hits = searcher.search(query);

        assertEquals("Should find server by hostname field query", 1, hits.length());
        assertEquals("web-app-srv-01.domain.local", hits.doc(0).get("hostname"));

        searcher.close();
    }
}
