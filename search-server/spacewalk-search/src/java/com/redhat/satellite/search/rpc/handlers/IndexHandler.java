/*
 * Copyright (c) 2008--2014 Red Hat, Inc.
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

package com.redhat.satellite.search.rpc.handlers;

import com.redhat.satellite.search.db.DatabaseManager;
import com.redhat.satellite.search.db.Query;
import com.redhat.satellite.search.index.IndexManager;
import com.redhat.satellite.search.index.IndexingException;
import com.redhat.satellite.search.index.QueryParseException;
import com.redhat.satellite.search.index.Result;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.search.BooleanQuery;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import redstone.xmlrpc.XmlRpcFault;

/**
 * XML-RPC handler which handles calls for indexing
 */
public class IndexHandler {

    private static final Logger LOG = LogManager.getLogger(IndexHandler.class);
    private final IndexManager indexManager;
    private final DatabaseManager databaseManager;
    public static final int QUERY_ERROR = 100;
    public static final int INDEX_ERROR = 200;
    public static final int DB_ERROR = 300;
    public static final String DEFAULT_LANG = new Locale("EN", "US").toString();

    /**
     * Constructor
     *
     * @param idxManager Search engine interface
     * @param dbMgr the database manager
     */
    public IndexHandler(IndexManager idxManager, DatabaseManager dbMgr) {
        indexManager = idxManager;
        databaseManager = dbMgr;
    }

    /**
     * Search index - using session id as String to avoid Integer overflow
     *
     * @param sessionId user's application session id
     * @param indexName index to use
     * @param query search query
     * @param isFineGrained is fine grained search
     * @return list of document ids as results
     * @throws XmlRpcFault something bad happened
     */
    public List<Result> search(String sessionId, String indexName, String query, boolean isFineGrained)
            throws XmlRpcFault {
        return search(Long.parseLong(sessionId), indexName, query, DEFAULT_LANG, isFineGrained);
    }

    /**
     * Search index -
     * assumes English language as default language
     *
     * @param sessionId
     *            user's application session id
     * @param indexName
     *            index to use
     * @param query
     *            search query
     * @return list of document ids as results
     * @throws XmlRpcFault something bad happened
     */
    public List<Result> search(long sessionId, String indexName, String query)
            throws XmlRpcFault {
        return search(sessionId, indexName, query, DEFAULT_LANG);
    }

    /**
     * Search index -
     * assumes English language as default language
     *
     * @param sessionId
     *            user's application session id
     * @param indexName
     *            index to use
     * @param query
     *            search query
     *  @param isFineGrained
     *            if set will restrict matches to be stricter and less forgiving
     * @return list of document ids as results
     * @throws XmlRpcFault something bad happened
     */
    public List<Result> search(long sessionId, String indexName, String query,
            boolean isFineGrained)
            throws XmlRpcFault {
        return search(sessionId, indexName, query, DEFAULT_LANG, isFineGrained);
    }

    /**
     * Search index
     *
     * @param sessionId
     *            user's application session id
     * @param indexName
     *            index to use
     * @param query
     *            search query
     *  @param lang
     *            language
     * @return list of document ids as results
     * @throws XmlRpcFault something bad happened
     */
    public List<Result> search(long sessionId, String indexName, String query,
            String lang)  throws XmlRpcFault {
        return search(sessionId, indexName, query, lang, false);
    }

    /**
     * Search index
     *
     * @param sessionId
     *            user's application session id
     * @param indexName
     *            index to use
     * @param query
     *            search query
     *  @param lang
     *            language
     *  @param isFineGrained
     *            if set will restrict matches to be stricter and less forgiving
     * @return list of document ids as results
     * @throws XmlRpcFault something bad happened
     */
    public List<Result> search(long sessionId, String indexName, String query, String lang, boolean isFineGrained)
            throws XmlRpcFault {
        LOG.debug("IndexHandler:: searching for: {}, indexName = {}, lang = {}", query, indexName, lang);
        while (true) {
            try {
                List<Result> hits = indexManager.search(indexName, query, lang, isFineGrained);
                if (List.of("package", "errata", "server").contains(indexName)) {
                    return screenHits(sessionId, indexName, hits);
                }
                return hits;
            }
            catch (IndexingException e) {
                LOG.error("Caught index exception: ", e);
                throw new XmlRpcFault(INDEX_ERROR, e.getMessage());
            }
            catch (QueryParseException e) {
                LOG.error("Caught query exception: ", e);
                throw new XmlRpcFault(QUERY_ERROR, e.getMessage());
            }
            catch (SQLException e) {
                LOG.error("Caught exception: ", e);
                throw new XmlRpcFault(DB_ERROR, e.getMessage());
            }
            catch (BooleanQuery.TooManyClauses e) {
                int oldQueries = BooleanQuery.getMaxClauseCount();
                if (Integer.MAX_VALUE / 2 > oldQueries) {
                    // increase number of max clause count
                    // if there's no overflow danger
                    int newQueries = oldQueries * 2;
                    LOG.error("Too many hits for query: {}. Increasing max clause count to {}\n" +
                            "exception message: {}", oldQueries, newQueries, e.getMessage());
                    BooleanQuery.setMaxClauseCount(newQueries);
                }
                else {
                    // there's no more help
                    throw e;
                }
            }
        }
    }

    private List<Result> screenHits(long sessionId, String indexName, List<Result> hits) throws SQLException {

        if (hits == null || hits.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("NO HITS FOUND");
            }
            return Collections.emptyList();
        }
        List<Long> ids = new ArrayList<>();
        for (Result pr : hits) {
            ids.add(Long.valueOf(pr.getId()));
        }

        Query<String> query;
        switch (indexName) {
            case "package":
                query = databaseManager.getQuery("verifyPackageVisibility");
                break;
            case "errata":
                query = databaseManager.getQuery("verifyErrataVisibility");
                break;
            case "server":
                query = databaseManager.getQuery("verifyServerVisibility");
                break;
            default:
                if (LOG.isDebugEnabled()) {
                    LOG.debug("screenHits({}) no 'screening of results' performed", indexName);
                    LOG.debug("results: {}", hits);
                }
                return hits;
        }

        try {
           Set<String> visible = new HashSet<>();

           // we have to batch the visibility query in clause to no more
           // than 1000 for oracle
           final int batchSize = 1000;
           for (int i = 0; i < ids.size(); i += batchSize) {
               Map<String, Object> params = new HashMap<>();
               params.put("session_id", sessionId);
               // sub list includes the first index and excludes the last.
               // so the proper last index of ids to pass to subList is ids.size()
               params.put("id_list", ids.subList(i, Math.min((i + batchSize), ids.size())));
               visible.addAll(query.loadList(params));
           }

           if (LOG.isDebugEnabled()) {
               LOG.debug("results: {}", visible);
           }

           // add the PackageResults that match the visible list
           List<Result> realResults = new ArrayList<>();
           for (Result pr : hits) {
               if (visible.contains(pr.getId())) {
                   realResults.add(pr);
               }
           }
           return realResults;
        }
        finally {
            query.close();
        }
    }
}
