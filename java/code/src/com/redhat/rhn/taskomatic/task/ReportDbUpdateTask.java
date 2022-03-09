/*
 * Copyright (c) 2021 SUSE LLC
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
package com.redhat.rhn.taskomatic.task;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.GeneratedWriteMode;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.ConnectionManager;
import com.redhat.rhn.common.hibernate.ConnectionManagerFactory;
import com.redhat.rhn.common.hibernate.ReportDbHibernateFactory;
import com.redhat.rhn.common.util.TimeUtils;

import org.apache.log4j.LogMF;
import org.hibernate.Session;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ReportDbUpdateTask extends RhnJavaJob {

    private static final int BATCH_SIZE = 100;

    @SuppressWarnings("unchecked")
    private <T> Stream<DataResult<T>> batchStream(SelectMode m, int batchSize, int initialOffset) {
        return Stream.iterate(initialOffset, i -> i + batchSize)
                .map(offset -> (DataResult<T>) m.execute(Map.of("offset", offset, "limit", batchSize)))
                .takeWhile(batch -> !batch.isEmpty());
    }

    private WriteMode generateDelete(Session session, String table) {
        final String sqlStatement = "DELETE FROM " + table + " WHERE mgm_id = :mgm_id";
        final List<String> params = List.of("mgm_id");

        return new GeneratedWriteMode("delete." + table, session, sqlStatement, params);
    }

    private WriteMode generateInsert(Session session, String table, long mgmId, Set<String> params) {
        final String sqlStatement = String.format(
            "INSERT INTO %s (mgm_id, synced_date, %s) VALUES (%s, current_timestamp, %s)",
            table,
            String.join(",", params),
            mgmId,
            params.stream().map(p -> ":" + p).collect(Collectors.joining(","))
        );

        return new GeneratedWriteMode("insert." + table, session, sqlStatement, params);
    }

    private void fillReportDbTable(Session session, String xmlName, String tableName, long mgmId) {
        TimeUtils.logTime(log, "Refreshing table " + tableName, () -> {
            SelectMode query = ModeFactory.getMode(xmlName, tableName, Map.class);

            // Remove all the existing data
            LogMF.debug(log, "Deleting existing data in table {}", tableName);
            WriteMode delete = generateDelete(session, tableName);
            delete.executeUpdate(Map.of("mgm_id", 1));

            // Extract the first batch
            @SuppressWarnings("unchecked")
            DataResult<Map<String, Object>> firstBatch = query.execute(Map.of("offset", 0, "limit", BATCH_SIZE));
            if (!firstBatch.isEmpty()) {
                // Generate the insert using the column name retrieved from the select
                WriteMode insert = generateInsert(session, tableName, mgmId, firstBatch.get(0).keySet());
                insert.executeUpdates(firstBatch);
                LogMF.debug(log, "Extracted {} rows for table {}", firstBatch.size(), tableName);

                // Iterate further if we can have additional rows
                if (firstBatch.size() == BATCH_SIZE) {
                    this.<Map<String, Object>>batchStream(query, BATCH_SIZE, BATCH_SIZE)
                        .forEach(batch -> {
                            insert.executeUpdates(batch);
                            LogMF.debug(log, "Extracted {} rows more for table {}", firstBatch.size(), tableName);
                        });
                }
            }
            else {
                LogMF.debug(log, "No data extracted for table {}", tableName);
            }
        });
    }

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        ConnectionManager rcm = ConnectionManagerFactory.localReportingConnectionManager();
        ReportDbHibernateFactory rh = new ReportDbHibernateFactory(rcm);
        long mgmId = 1;

        try {
            fillReportDbTable(rh.getSession(), "SystemReport_queries", "System", mgmId);
            fillReportDbTable(rh.getSession(), "SystemReport_queries", "SystemHistory", mgmId);
            fillReportDbTable(rh.getSession(), "SystemReport_queries", "SystemAction", mgmId);
            fillReportDbTable(rh.getSession(), "SystemReport_queries", "SystemChannel", mgmId);
            fillReportDbTable(rh.getSession(), "SystemReport_queries", "SystemConfigChannel", mgmId);
            fillReportDbTable(rh.getSession(), "SystemReport_queries", "SystemVirtualData", mgmId);
            fillReportDbTable(rh.getSession(), "SystemReport_queries", "SystemNetInterface", mgmId);
            fillReportDbTable(rh.getSession(), "SystemReport_queries", "SystemNetAddressV4", mgmId);
            fillReportDbTable(rh.getSession(), "SystemReport_queries", "SystemNetAddressV6", mgmId);
            fillReportDbTable(rh.getSession(), "SystemReport_queries", "SystemOutdated", mgmId);
            fillReportDbTable(rh.getSession(), "SystemReport_queries", "SystemGroup", mgmId);
            fillReportDbTable(rh.getSession(), "SystemReport_queries", "SystemEntitlement", mgmId);
            fillReportDbTable(rh.getSession(), "SystemReport_queries", "SystemErrata", mgmId);

            fillReportDbTable(rh.getSession(), "ChannelReport_queries", "Channel", mgmId);
            fillReportDbTable(rh.getSession(), "ChannelReport_queries", "Errata", mgmId);
            fillReportDbTable(rh.getSession(), "ChannelReport_queries", "Package", mgmId);

            rh.commitTransaction();
            log.info("Reporting db updated successfully.");
        }
        catch (RuntimeException ex) {
            log.warn("Unable to update reporting db", ex);

            try {
                rh.rollbackTransaction();
            }
            catch (RuntimeException rollbackException) {
                log.warn("Unable to rollback transaction", rollbackException);
            }
        }
        finally {
            rh.closeSession();
            rh.closeSessionFactory();
        }
    }

    @Override
    public String getConfigNamespace() {
        return "report_db_update";
    }
}
