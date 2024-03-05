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
import com.redhat.satellite.search.db.models.Errata;
import com.redhat.satellite.search.index.IndexManager;
import com.redhat.satellite.search.index.IndexingException;
import com.redhat.satellite.search.index.builder.BuilderFactory;
import com.redhat.satellite.search.index.builder.DocumentBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * IndexErrataTask
 */
public class IndexErrataTask implements Job {

    private static final Logger LOG = LogManager.getLogger(IndexErrataTask.class);
    private static final String LANG = "en";

    /**
     * {@inheritDoc}
     */
    public void execute(JobExecutionContext ctx) throws JobExecutionException {
        JobDataMap jobData = ctx.getJobDetail().getJobDataMap();
        DatabaseManager databaseManager =
            (DatabaseManager)jobData.get("databaseManager");
        IndexManager indexManager =
            (IndexManager)jobData.get("indexManager");

        try {

            List<Errata> errata = getErrata(databaseManager);
            int count = 0;
            LOG.info("found [{}] errata to index", errata.size());
            for (Iterator<Errata> iter = errata.iterator(); iter.hasNext();) {
                Errata current = iter.next();
                indexErrata(indexManager, current);
                count++;
                if (count == 10 || !iter.hasNext()) {
                    if (System.getProperties().get("isTesting") == null) {
                        updateLastErrataId(databaseManager, current.getId());
                    }
                    count = 0;
                }
            }
        }
        catch (SQLException e) {
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
     * @param eid the errata id
     */
    private void updateLastErrataId(DatabaseManager databaseManager, long eid)
        throws SQLException {

        WriteQuery updateQuery = databaseManager.getWriterQuery("updateLastErrata");
        WriteQuery insertQuery = databaseManager.getWriterQuery("createLastErrata");

        try {
            Map<String, Object> params = new HashMap<>();
            params.put("id", eid);
            params.put("last_modified", Calendar.getInstance().getTime());
            if (updateQuery.update(params) == 0) {
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
     * @param errata the errata
     */
    private void indexErrata(IndexManager indexManager, Errata errata)
        throws IndexingException {

        Map<String, String> attrs = new HashMap<>();
        attrs.put("id", Long.toString(errata.getId()));
        attrs.put("advisory", errata.getAdvisory());
        attrs.put("advisoryType", errata.getAdvisoryType());
        attrs.put("advisoryName", errata.getAdvisoryName());
        attrs.put("advisoryRel", Long.toString(errata.getAdvisoryRel()));
        attrs.put("product", errata.getProduct());
        attrs.put("description", errata.getDescription());
        attrs.put("synopsis", errata.getSynopsis());
        attrs.put("topic", errata.getTopic());
        attrs.put("solution", errata.getSolution());
        attrs.put("issueDate", errata.getIssueDate());
        attrs.put("updateDate", errata.getUpdateDate());
        attrs.put("notes", errata.getNotes());
        attrs.put("orgId", errata.getOrgId());
        attrs.put("created", errata.getCreated());
        attrs.put("modified", errata.getModified());
        attrs.put("lastModified", errata.getLastModified());
        attrs.put("name", errata.getAdvisory());

        LOG.info("Indexing errata: {}: {}", errata.getId(), attrs);
        DocumentBuilder edb = BuilderFactory.getBuilder(BuilderFactory.ERRATA_TYPE);
        Document doc = edb.buildDocument(errata.getId(), attrs);
        indexManager.addToIndex("errata", doc, LANG);
    }

    /**
     * @param databaseManager the database manager
     * @return list of errata
     */
    private List<Errata> getErrata(DatabaseManager databaseManager)
        throws SQLException {

        List<Errata> retval;
        Query<Long> query = databaseManager.getQuery("getLastErrataId");
        Long eid;
        try {
            eid = query.load();
        }
        finally {
            query.close();
        }
        if (eid == null) {
            eid = 0L;
        }
        Query<Errata> errataQuery = databaseManager.getQuery("listErrataFromId");
        try {
            retval = errataQuery.loadList(eid);
        }
        finally {
            errataQuery.close();
        }
        return retval;
    }

}
