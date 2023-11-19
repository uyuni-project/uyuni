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
package com.redhat.satellite.search.scheduler.tasks;

import com.redhat.satellite.search.db.DatabaseManager;
import com.redhat.satellite.search.db.Query;
import com.redhat.satellite.search.db.WriteQuery;
import com.redhat.satellite.search.db.models.GenericRecord;
import com.redhat.satellite.search.index.IndexManager;
import com.redhat.satellite.search.index.IndexingException;
import com.redhat.satellite.search.index.builder.BuilderFactory;
import com.redhat.satellite.search.index.builder.DocumentBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.StatefulJob;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * GenericIndexTask
 */
public abstract class GenericIndexTask implements StatefulJob {

    private static final Logger LOG = LogManager.getLogger(GenericIndexTask.class);
    private static final String LANG = "en";
    /**
     * {@inheritDoc}
     */
    public void execute(JobExecutionContext ctx)
        throws JobExecutionException {
        JobDataMap jobData = ctx.getJobDetail().getJobDataMap();
        DatabaseManager databaseManager = (DatabaseManager)jobData.get("databaseManager");
        IndexManager indexManager = (IndexManager)jobData.get("indexManager");

        try {
            //try to create the index first incase we never actually
            //   have any records (BZ 537502)
            indexManager.createIndex(getIndexName(), LANG);
            List<GenericRecord> data = getRecords(databaseManager);
            int count = 0;
            LOG.info("{} found [{}] items to index", super.getClass(), data.size());
            for (Iterator<GenericRecord> iter = data.iterator(); iter.hasNext();) {
                GenericRecord current = iter.next();
                indexRecord(indexManager, current);
                count++;
                if (count == 10 || !iter.hasNext()) {
                    if (System.getProperties().get("isTesting") == null) {
                        updateLastRecord(databaseManager, current.getId());
                    }
                    count = 0;
                }
            }
            //
            // Check to see if any records have been deleted from database, so
            // we should delete from our indexes.
            //
            int numDel = handleDeletedRecords(databaseManager, indexManager);
            LOG.info("Deleted {} records from index <{}>", numDel, getIndexName());
        }
        catch (SQLException e) {
            e.printStackTrace();
            throw new JobExecutionException(e);
        }
        catch (IndexingException e) {
            LOG.debug(e);
            if (e.getMessage().contains("LockObtainFailedException: Lock obtain timed out")) {
                LOG.info("Indexer already running. Skipping");
                return;
            }
            throw new JobExecutionException(e);
        }
    }
    /**
     * @param databaseManager the database manager
     * @param sid the server id
     */
    private void updateLastRecord(DatabaseManager databaseManager, long sid)
        throws SQLException {

        WriteQuery updateQuery = databaseManager.getWriterQuery(getQueryUpdateLastRecord());
        WriteQuery insertQuery = null;

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("id", sid);
            params.put("last_modified", Calendar.getInstance().getTime());

            if (updateQuery.update(params) == 0) {
                insertQuery = databaseManager.getWriterQuery(getQueryCreateLastRecord());
                insertQuery.insert(params);
            }
        }
        finally {
            try {
                if (updateQuery != null) {
                    updateQuery.close();
                }
            }
            finally {
                if (insertQuery != null) {
                    insertQuery.close();
                }
            }
        }
    }

    /**
     * @param indexManager the index manager
     * @param data the data
     */
    private void indexRecord(IndexManager indexManager,
            GenericRecord data)
        throws IndexingException {

        Map<String, String> attrs = getFieldMap(data);
        LOG.info("{} Indexing object: {}: {}", super.getClass(), data.getId(), attrs);
        DocumentBuilder pdb = BuilderFactory.getBuilder(getIndexName());
        Document doc = pdb.buildDocument(data.getId(), attrs);
        indexManager.addUniqueToIndex(getIndexName(), doc, getUniqueFieldId(), LANG);
    }


    /**
     * @param databaseManager the database manager
     * @return list of records
     */
    private List<GenericRecord> getRecords(DatabaseManager databaseManager)
        throws SQLException {
        // What was the last object id we indexed?
        List<GenericRecord> retval;
        Query<Long> query = databaseManager.getQuery(getQueryLastRecord());
        Long sid;
        try {
            sid = query.load();
        }
        finally {
            query.close();
        }
        if (sid == null) {
            sid = 0L;
        }
        // When was the last time we ran the indexing of servers?
        Query<Date> queryLast = databaseManager.getQuery(getQueryLastIndexDate());
        Date lastRun;
        try {
            lastRun = queryLast.load();
        }
        finally {
            queryLast.close();
        }
        if (lastRun == null) {
            lastRun = new Date(0);
        }
        // Lookup what objects have not been indexed, or need to be reindexed.
        Query<GenericRecord> srvrQuery = databaseManager.getQuery(
                getQueryRecordsToIndex());
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("id", sid);
            params.put("last_modified", lastRun);
            LOG.info("GenericIndexTask<{} last processed id = {}, lastRun was {}>", super.getClass(), sid, lastRun);
            retval = srvrQuery.loadList(params);
            LOG.info("GenericIndexTask<{} number of results returned = {}>", super.getClass(), retval.size());
        }
        finally {
            srvrQuery.close();
        }
        return retval;
    }

    /**
     * Will determine if any records have been deleted from the DB, then will
     * delete those records from the lucene index.
     * @return number of deleted records
     */
    protected int handleDeletedRecords(DatabaseManager databaseManager,
            IndexManager indexManager)
        throws SQLException {
        List<Object> records;
        Query<Object> query = null;
        String uniqField;
        String indexName;
        HashSet<String> idSet;
        try {
            query = databaseManager.getQuery(getQueryAllIds());
            records = query.loadList(Collections.emptyMap());
            idSet = new HashSet<>();
            for (Object dbrecord : records) {
                idSet.add(dbrecord.toString());
            }
            uniqField = getUniqueFieldId();
            indexName = getIndexName();
        }
        finally {
            if (query != null) {
                query.close();
            }
        }
        return indexManager.deleteRecordsNotInList(idSet, indexName, uniqField);
    }

    /**
     *
     * @param data fully populated DTO object
     * @return map which represents the fields to index along with their values
     * @throws ClassCastException when data isn't castable to the intended DTO
     */
    protected abstract Map<String, String> getFieldMap(GenericRecord data)
        throws ClassCastException;
    /**
     *
     * @return the index name
     */
    public abstract String getIndexName();
    /**
     * @return the Document field name which represents the unique id for this data
     */
    public abstract String getUniqueFieldId();
    /**
     *
     * @return name of query which shows the last record indexed
     */
    protected abstract String getQueryLastRecord();
    /**
     *
     * @return name of query which will update the last record indexed
     */
    protected abstract String getQueryUpdateLastRecord();
    /**
     *
     * @return name of query which will create the last record indexed
     */
    protected abstract String getQueryCreateLastRecord();
    /**
     *
     * @return name of query which will give back records to be indexed
     */
    protected abstract String getQueryRecordsToIndex();
    /**
     *
     * @return name of query which will show the date this task last ran
     */
    protected abstract String getQueryLastIndexDate();

    /**
     * @return name of the query which will return all current ids.
     */
    protected abstract String getQueryAllIds();
}
