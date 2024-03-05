/*
 * Copyright (c) 2008--2015 Red Hat, Inc.
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

package com.redhat.satellite.search.index;

import com.redhat.satellite.search.config.Configuration;
import com.redhat.satellite.search.index.builder.BuilderFactory;
import com.redhat.satellite.search.index.ngram.NGramAnalyzer;
import com.redhat.satellite.search.index.ngram.NGramQueryParser;
import com.redhat.satellite.search.rpc.handlers.IndexHandler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Indexing workhorse class
 */
public class IndexManager {

    private static final Logger LOG = LogManager.getLogger(IndexManager.class);
    private String indexWorkDir;
    private final int maxHits;
    private final double scoreThreshold;
    private final double systemScoreThreshold;
    private final double errataScoreThreshold;
    private final double errataAdvisoryScoreThreshold;
    private final int minNgram;
    private final int maxNgram;
    private boolean filterDocResults = false;
    private boolean explainResults = false;
    // Name conflict with our Configuration class and Hadoop's
    private final Map<String, String> docLocaleLookUp = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    /**
     * Constructor
     *
     * @param config application config
     */
    public IndexManager(Configuration config) {
        maxHits = config.getInt("search.max_hits_returned", 0);
        indexWorkDir = config.getString("search.index_work_dir", null);
        if (indexWorkDir == null) {
            throw new IllegalArgumentException(
                    "search.index_work_dir config entry " + "is missing");
        }
        if (!indexWorkDir.endsWith("/")) {
            indexWorkDir += "/";
        }
        scoreThreshold = config.getDouble("search.score_threshold", .30);
        systemScoreThreshold = config.getDouble("search.system_score_threshold", .30);
        errataScoreThreshold = config.getDouble("search.errata_score_threshold", .30);
        errataAdvisoryScoreThreshold =
            config.getDouble("search.errata.advisory_score_threshold", .30);
        minNgram = config.getInt("search.min_ngram", 1);
        maxNgram = config.getInt("search.max_ngram", 5);
        filterDocResults = config.getBoolean("search.doc.limit_results");
        explainResults = config.getBoolean("search.log.explain.results");
    }


    /**
     * @return String of the index working directory
     */
    public String getIndexWorkDir() {
        return indexWorkDir;
    }

    /**
     * Query a index
     *
     * @param indexName name of the index
     * @param query search query
     * @param lang language
     * @return list of hits
     * @throws IndexingException if there is a problem indexing the content.
     * @throws QueryParseException when something goes wrong
     */
    public List<Result> search(String indexName, String query, String lang)
            throws IndexingException, QueryParseException {
        return search(indexName, query, lang, false);
    }

    /**
     * Query a index
     *
     * @param indexName name of the index
     * @param query search query
     * @param lang language
     * @param isFineGrained
     *      true:   will limit results, less are returned but they are closer
     *              to the search query, useful for advanced/free form queries
     *
     *      false:  will allow queries to be more flexible returning words
     *              which are spelled similarly
     *
     * @return list of hits
     * @throws IndexingException if there is a problem indexing the content.
     * @throws QueryParseException when something goes wrong
     */
    public List<Result> search(String indexName, String query, String lang,
            boolean isFineGrained)
            throws IndexingException, QueryParseException {
        IndexSearcher searcher = null;
        IndexReader reader = null;
        List<Result> retval = null;
        try {
            reader = getIndexReader(indexName, lang);
            searcher = getIndexSearcher(indexName, lang);
            QueryParser qp = getQueryParser(indexName, lang, isFineGrained);
            Query q = qp.parse(query);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Original query was: {}", query);
                LOG.debug("Parsed Query is: {}", q);
            }
            Hits hits = searcher.search(q);
            if (LOG.isDebugEnabled()) {
                LOG.debug("{} results were found.", hits.length());
            }
            Set<Term> queryTerms;
            try {
                queryTerms = new HashSet<>();
                Query newQ = q.rewrite(reader);
                newQ.extractTerms(queryTerms);
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new QueryParseException(e);
            }
            retval = processHits(indexName, hits, queryTerms, query, lang);
            if (explainResults) {
                debugExplainResults(indexName, hits, searcher, q, queryTerms);
            }
        }
        catch (IOException e) {
            // this exception is thrown, when there're no packages or errata on the system
            // and the user performs a search
            // if this is the case, just return 0 results, otherwise rethrow the exception
            if (!e.getMessage().contains(
                    "no segments* file found in org.apache.lucene.store.FSDirectory@/var/lib/rhn/search/indexes")) {
                throw new IndexingException(e);
            }
            LOG.error(e.getMessage());
            retval = new ArrayList<>();
        }
        catch (ParseException e) {
            throw new QueryParseException("Could not parse query: '" + query + "'");
        }
        finally {
            try {
                if (searcher != null) {
                    searcher.close();
                }
                if (reader != null) {
                    reader.close();
                }
            }
            catch (IOException ex) {
                LOG.error("Index Manager Error: ", ex);
            }
        }
        return retval;
    }


    /**
     * Create an empty index if it exists
     *
     * @param indexName index to use
     * @param lang language.
     * @throws IndexingException something went wrong adding the document
     */
    public void createIndex(String indexName, String lang)
        throws IndexingException {

        try {
            IndexWriter writer = getIndexWriter(indexName, lang);
            try {
                writer.flush();
            }
            finally {
                try {
                    writer.close();
                }
                finally {
                    // unlock it if it is locked.
                    unlockIndex(indexName);
                }
            }
        }
        catch (IOException e) {
            throw new IndexingException(e);
        }
    }


    /**
     * Adds a document to an index
     *
     * @param indexName index to use
     * @param doc Document to be indexed.
     * @param lang language.
     * @throws IndexingException something went wrong adding the document
     */
    public void addToIndex(String indexName, Document doc, String lang)
        throws IndexingException {

        try {
            IndexWriter writer = getIndexWriter(indexName, lang);
            try {
                writer.addDocument(doc);
                writer.flush();
            }
            finally {
                try {
                    writer.close();
                }
                finally {
                    // unlock it if it is locked.
                    unlockIndex(indexName);
                }
            }
        }
        catch (IOException e) {
            throw new IndexingException(e);
        }
    }
    /**
     * @param indexName the index name
     * @param doc document with data to index
     * @param uniqueField field in doc which identifies this uniquely
     * @param lang language
     * @throws IndexingException something went wrong adding the document
     */
    public void addUniqueToIndex(String indexName, Document doc,
            String uniqueField, String lang)
        throws IndexingException {
        IndexReader reader = null;
        int numFound = 0;
        try {
            reader = getIndexReader(indexName, lang);
            Term term = new Term(uniqueField, doc.get(uniqueField));
            numFound = reader.docFreq(term);
        }
        catch (FileNotFoundException e) {
            // Index doesn't exist, so this add will be unique
            // we don't need to do anything/
        }
        catch (IOException e) {
            throw new IndexingException(e);
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException e) {
                    //
                }
            }
        }
        if (numFound > 0) {
            if (LOG.isInfoEnabled()) {
                LOG.info("Found {} <{}> docs for {}:{} will remove them now.", numFound, indexName,
                        uniqueField, doc.get(uniqueField));
            }
            removeFromIndex(indexName, uniqueField, doc.get(uniqueField));
        }
        addToIndex(indexName, doc, lang);
    }

    /**
     * Remove a document from an index
     *
     * @param indexName index to use
     * @param uniqueField field name which represents this data's unique id
     * @param objectId unique document id
     * @throws IndexingException something went wrong removing the document
     */
    public void removeFromIndex(String indexName, String uniqueField, String objectId)
            throws IndexingException {
        LOG.info("Removing <{}> {}:{}", indexName, uniqueField, objectId);
        Term t = new Term(uniqueField, objectId);
        IndexReader reader;
        try {
            reader = getIndexReader(indexName, IndexHandler.DEFAULT_LANG);
            try {
                reader.deleteDocuments(t);
                reader.flush();
            }
            finally {
                reader.close();
            }
        }
        catch (IOException e) {
            throw new IndexingException(e);
        }
    }

    /**
     * Unlocks the index at the given directory if it is currently locked.
     * Otherwise, does nothing.
     * @param indexName index name
     * @throws IOException thrown if there is a problem unlocking the index.
     */
    private void unlockIndex(String indexName) throws IOException {
        String path = indexWorkDir + indexName;
        File f = new File(path);
        Directory dir = FSDirectory.getDirectory(f);
        if (IndexReader.isLocked(dir)) {
            IndexReader.unlock(dir);
        }
    }

    private IndexWriter getIndexWriter(String name, String lang) throws IOException {
        String path = indexWorkDir + name;
        File f = new File(path);
        f.mkdirs();
        Analyzer analyzer = getAnalyzer(name, lang);
        IndexWriter writer = new IndexWriter(path, analyzer);
        writer.setUseCompoundFile(true);
        return writer;
    }

    private IndexReader getIndexReader(String indexName, String locale) throws IOException {
        String path = indexWorkDir + indexName;
        LOG.info("IndexManager::getIndexReader({}, {}) path = {}", indexName, locale, path);
        File f = new File(path);
        return IndexReader.open(FSDirectory.getDirectory(f));
    }

    private IndexSearcher getIndexSearcher(String indexName, String locale) throws IOException {
        String path = indexWorkDir + indexName;
        LOG.info("IndexManager::getIndexSearcher({}, {}) path = {}", indexName, locale, path);
        return new IndexSearcher(path);
    }

    private QueryParser getQueryParser(String indexName, String lang, boolean isFineGrained) {
        LOG.debug("getQueryParser({}, {}, {})", indexName, lang, isFineGrained);
        Analyzer analyzer = getAnalyzer(indexName, lang);
        QueryParser qp = new NGramQueryParser("name", analyzer, isFineGrained);
        qp.setDateResolution(DateTools.Resolution.MINUTE);
        return qp;
    }


    private Analyzer getAnalyzer(String indexName, String lang) {
        LOG.debug("getAnalyzer({}, {})", indexName, lang);
        switch (indexName) {
            case BuilderFactory.SERVER_TYPE:
                return getServerAnalyzer();
            case BuilderFactory.ERRATA_TYPE:
                return getErrataAnalyzer();
            case BuilderFactory.SNAPSHOT_TAG_TYPE:
                return getSnapshotTagAnalyzer();
            case BuilderFactory.HARDWARE_DEVICE_TYPE:
                return getHardwareDeviceAnalyzer();
            case BuilderFactory.SERVER_CUSTOM_INFO_TYPE:
                return getServerCustomInfoAnalyzer();
            default:
                LOG.debug("{} using getDefaultAnalyzer()", indexName);
                return getDefaultAnalyzer();
        }
    }

    private List<Result> processHits(String indexName, Hits hits, Set<Term> queryTerms,
            String query, String lang)
        throws IOException {
        List<Result> retval = new ArrayList<Result>();
        for (int x = 0; x < hits.length(); x++) {
            Document doc = hits.doc(x);
            Result pr = null;
            if (!isScoreAcceptable(indexName, hits, x, query)) {
                break;
            }
            switch (indexName) {
                case BuilderFactory.SERVER_TYPE:
                    pr = new Result(x,
                            doc.getField("id").stringValue(),
                            doc.getField("name").stringValue(),
                            hits.score(x),
                            doc.getField("uuid").stringValue());
                    break;
                case BuilderFactory.SNAPSHOT_TAG_TYPE:
                    pr = new SnapshotTagResult(x, hits.score(x), doc);
                    break;
                case BuilderFactory.HARDWARE_DEVICE_TYPE:
                    pr = new HardwareDeviceResult(x, hits.score(x), doc);
                    break;
                case BuilderFactory.SERVER_CUSTOM_INFO_TYPE:
                    pr = new ServerCustomInfoResult(x, hits.score(x), doc);
                    break;
                case BuilderFactory.XCCDF_IDENT_TYPE:
                    pr = new Result(x,
                            doc.getField("id").stringValue(),
                            doc.getField("identifier").stringValue(),
                            hits.score(x));
                    break;
                default:
                    //Type Errata and Package
                    pr = new Result(x,
                            doc.getField("id").stringValue(),
                            doc.getField("name").stringValue(),
                            hits.score(x));
                    break;
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Hit[{}] Score = {}, Result = {}", x, hits.score(x), pr);
            }
            /*
             * matchingField will help the webUI to understand what field was responsible
             * for this match.  Later implementation should use "Explanation" to determine
             * field, for now we will simply grab one term and return it's field.
             */
            try {
                MatchingField match = new MatchingField(query, doc, queryTerms);
                pr.setMatchingField(match.getFieldName());
                pr.setMatchingFieldValue(match.getFieldValue());
                LOG.info("hit[{}] matchingField is being set to: <{}> based on passed in query field. " +
                        "matchingFieldValue = {}", x, pr.getMatchingField(), pr.getMatchingFieldValue());
            }
            catch (Exception e) {
                LOG.error("Caught exception: ", e);
            }
            retval.add(pr);
            if (maxHits > 0 && x == maxHits) {
                break;
            }
        }
        return retval;
    }
    /**
     *
     * @param indexName index name
     * @param hits hits
     * @param x x
     * @param queryIn query
     * @return  true - score is acceptable
     *          false - score is NOT acceptable
     * @throws IOException when something goes wrong
     */
    private boolean isScoreAcceptable(String indexName, Hits hits, int x, String queryIn) throws IOException {
        String guessMainQueryTerm = MatchingField.getFirstFieldName(queryIn);

        /*
         * Dropping matches which are a poor fit.
         * system searches are filtered based on "system_score_threshold"
         * other searches will return 10 best matches, then filter anything below
         * "score_threshold"
         */
        if ((indexName.equals(BuilderFactory.SERVER_TYPE) ||
                indexName.equals(BuilderFactory.SERVER_CUSTOM_INFO_TYPE) ||
                indexName.equals(BuilderFactory.SNAPSHOT_TAG_TYPE) ||
                indexName.equals(BuilderFactory.HARDWARE_DEVICE_TYPE)) &&
                (hits.score(x) < systemScoreThreshold)) {
            LOG.debug("hits.score({}) is {}", x, hits.score(x));
            LOG.debug("Filtering out search results from {} to {}, due to their score being below " +
                    "system_score_threshold = {}", x, hits.length(), systemScoreThreshold);
            return false;
        }
        else if (indexName.compareTo(BuilderFactory.ERRATA_TYPE) == 0) {
            if (guessMainQueryTerm.equals("name") && (hits.score(x) < errataAdvisoryScoreThreshold)) {
                LOG.debug("hits.score({}) is {}", x, hits.score(x));
                LOG.debug("Filtering out search results from {} to {}, due to their score being below " +
                        "errata_advisory_score_threshold = {}", x, hits.length(), errataAdvisoryScoreThreshold);
                return false;
            }
            else if (hits.score(x) < errataScoreThreshold) {
                LOG.debug("hits.score({}) is {}", x, hits.score(x));
                LOG.debug("Filtering out search results from {} to {}, due to their score being below " +
                        "errata_score_threshold = {}", x, hits.length(), errataScoreThreshold);
                return false;
            }
        }
        else if (((hits.score(x) < scoreThreshold) && (x > 10)) || (hits.score(x) < 0.001)) {
            /*
             * Dropping matches which are a poor fit.
             * First term is configurable, it allows matches like spelling errors or
             * suggestions to be possible.
             * Second term is intended to get rid of pure and utter crap hits
             */
            LOG.debug("hits.score({}) is {}", x, hits.score(x));
            LOG.debug("Filtering out search results from {} to {}, due to their score being below " +
                    "score_threshold = {}", x, hits.length(), scoreThreshold);
            return false;
        }
        return true;
    }

    /**
     * Removes any documents which are not related to the passed in Set of good value
     * @param ids Set of ids of all known/good values
     * @param indexName index name to operate on
     * @param uniqField the name of the field in the Document to uniquely identify
     * this record
     * @return the number of documents deleted
     */
    public int deleteRecordsNotInList(Set<String> ids, String indexName,
            String uniqField) {
        int count = 0;
        IndexReader reader = null;
        try {
            reader = getIndexReader(indexName, IndexHandler.DEFAULT_LANG);

            // Use maxDoc() to iterate over all docs, numDocs() returns the
            // number of currently alive docs leaving out the deleted ones.
            int maxDoc = reader.maxDoc();
            for (int i = 0; i < maxDoc; i++) {
                if (!reader.isDeleted(i)) {
                    Document doc = reader.document(i);
                    String uniqId = doc.getField(uniqField).stringValue();
                    if (!ids.contains(uniqId)) {
                        LOG.info("{}:{}: <{}> not found in list of current/good values assuming this has been " +
                                "deleted from Database and we should remove it.", indexName, uniqField, uniqId);
                        removeFromIndex(indexName, uniqField, uniqId);
                        count++;
                    }
                }
            }
        }
        catch (IOException | IndexingException e) {
            e.printStackTrace();
            LOG.info("deleteRecordsNotInList() caught exception : ", e);
        }
        finally {
            if (reader != null) {
                try {
                    reader.close();
                }
                catch (IOException e) {
                    //
                }
            }
        }
        return count;
    }

    private void debugExplainResults(String indexName, Hits hits, IndexSearcher searcher,
            Query q, Set<Term> queryTerms) throws IOException {
        LOG.debug("Parsed Query is {}", q);
        LOG.debug("Looking at index: {}", indexName);
        for (int i = 0; i < hits.length(); i++) {
            if ((i < 10)) {
                Document doc = hits.doc(i);
                Float score = hits.score(i);
                Explanation ex = searcher.explain(q, hits.id(i));
                LOG.debug("Looking at hit<{}, {}, {}>: {}", i, hits.id(i), score, doc);
                LOG.debug("Explanation: {}", ex);
                MatchingField match = new MatchingField(q.toString(), doc, queryTerms);
                String fieldName = match.getFieldName();
                String fieldValue = match.getFieldValue();
                LOG.debug("Guessing that matched fieldName is {} = {}", fieldName, fieldValue);
            }
        }
    }

    private Analyzer getServerAnalyzer() {
        PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new
                NGramAnalyzer(minNgram, maxNgram));
        analyzer.addAnalyzer("checkin", new KeywordAnalyzer());
        analyzer.addAnalyzer("registered", new KeywordAnalyzer());
        analyzer.addAnalyzer("ram", new KeywordAnalyzer());
        analyzer.addAnalyzer("swap", new KeywordAnalyzer());
        analyzer.addAnalyzer("cpuMHz", new KeywordAnalyzer());
        analyzer.addAnalyzer("cpuNumberOfCpus", new KeywordAnalyzer());


        return analyzer;
    }

    private Analyzer getErrataAnalyzer() {
        PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new
                NGramAnalyzer(minNgram, maxNgram));
        analyzer.addAnalyzer("advisoryName", new KeywordAnalyzer());
        analyzer.addAnalyzer("synopsis", new StandardAnalyzer());
        analyzer.addAnalyzer("description", new StandardAnalyzer());
        analyzer.addAnalyzer("topic", new StandardAnalyzer());
        analyzer.addAnalyzer("solution", new StandardAnalyzer());

        return analyzer;
    }

    private Analyzer getSnapshotTagAnalyzer() {
        PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new
                NGramAnalyzer(minNgram, maxNgram));
        analyzer.addAnalyzer("id", new KeywordAnalyzer());
        analyzer.addAnalyzer("snapshotId", new KeywordAnalyzer());
        analyzer.addAnalyzer("orgId", new KeywordAnalyzer());
        analyzer.addAnalyzer("serverId", new KeywordAnalyzer());
        analyzer.addAnalyzer("tagNameId", new KeywordAnalyzer());
        analyzer.addAnalyzer("created", new KeywordAnalyzer());
        analyzer.addAnalyzer("modified", new KeywordAnalyzer());
        return analyzer;
    }

    private Analyzer getHardwareDeviceAnalyzer() {
        PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new
                NGramAnalyzer(minNgram, maxNgram));
        analyzer.addAnalyzer("id", new KeywordAnalyzer());
        analyzer.addAnalyzer("serverId", new KeywordAnalyzer());
        analyzer.addAnalyzer("pciType", new KeywordAnalyzer());
        return analyzer;
    }

    private Analyzer getServerCustomInfoAnalyzer() {
        PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new
                NGramAnalyzer(minNgram, maxNgram));
        analyzer.addAnalyzer("id", new KeywordAnalyzer());
        analyzer.addAnalyzer("serverId", new KeywordAnalyzer());
        analyzer.addAnalyzer("created", new KeywordAnalyzer());
        analyzer.addAnalyzer("modified", new KeywordAnalyzer());
        analyzer.addAnalyzer("createdBy", new KeywordAnalyzer());
        analyzer.addAnalyzer("lastModifiedBy", new KeywordAnalyzer());
        return analyzer;
    }

    private Analyzer getDefaultAnalyzer() {
        PerFieldAnalyzerWrapper analyzer = new PerFieldAnalyzerWrapper(new
                NGramAnalyzer(minNgram, maxNgram));
        analyzer.addAnalyzer("id", new KeywordAnalyzer());
        analyzer.addAnalyzer("arch", new KeywordAnalyzer());
        analyzer.addAnalyzer("epoch", new KeywordAnalyzer());
        analyzer.addAnalyzer("version", new KeywordAnalyzer());
        analyzer.addAnalyzer("release", new KeywordAnalyzer());
        analyzer.addAnalyzer("filename", new KeywordAnalyzer());
        return analyzer;
    }

}
