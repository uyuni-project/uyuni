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

import static com.redhat.rhn.taskomatic.task.ReportDBHelper.generateDelete;
import static com.redhat.rhn.taskomatic.task.ReportDBHelper.generateInsertWithDate;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.ConnectionManager;
import com.redhat.rhn.common.hibernate.ConnectionManagerFactory;
import com.redhat.rhn.common.hibernate.ReportDbHibernateFactory;
import com.redhat.rhn.common.util.TimeUtils;

import org.hibernate.Session;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Map;



public class ReportDbUpdateTask extends RhnJavaJob {

    private static final String GENERAL_REPORT_QUERIES = "GeneralReport_queries";
    private static final String SYSTEM_REPORT_QUERIES = "SystemReport_queries";
    private static final String CHANNEL_REPORT_QUERIES = "ChannelReport_queries";
    private static final String SCAP_REPORT_QUERIES = "ScapReport_queries";

    private static final int BATCH_SIZE = Config.get()
            .getInt(ConfigDefaults.REPORT_DB_BATCH_SIZE, 2000);


    private void fillReportDbTable(Session session, String xmlName, String tableName, long mgmId) {
        TimeUtils.logTime(log, "Refreshing table " + tableName, () -> {
            SelectMode query = ModeFactory.getMode(xmlName, tableName, Map.class);

            // Remove all the existing data
            log.debug("Deleting existing data in table {}", tableName);
            WriteMode delete = generateDelete(session, tableName);
            delete.executeUpdate(Map.of("mgm_id", 1));

            // Extract the first batch
            @SuppressWarnings("unchecked")
            DataResult<Map<String, Object>> firstBatch = query.execute(Map.of("offset", 0, "limit", BATCH_SIZE));
            if (!firstBatch.isEmpty()) {
                // Generate the insert using the column name retrieved from the select
                WriteMode insert = generateInsertWithDate(session, tableName, mgmId, firstBatch.get(0).keySet());
                insert.executeUpdates(firstBatch);
                log.debug("Extracted {} rows for table {}", firstBatch.size(), tableName);

                // Iterate further if we can have additional rows
                if (firstBatch.size() == BATCH_SIZE) {
                    ReportDBHelper.<Map<String, Object>>batchStream(query, BATCH_SIZE, BATCH_SIZE)
                        .forEach(batch -> {
                            insert.executeUpdates(batch);
                            log.debug("Extracted {} rows more for table {}", firstBatch.size(), tableName);
                        });
                }
            }
            else {
                log.debug("No data extracted for table {}", tableName);
            }
        });
    }

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        ConnectionManager rcm = ConnectionManagerFactory.localReportingConnectionManager();
        ReportDbHibernateFactory rh = new ReportDbHibernateFactory(rcm);
        long mgmId = 1;

        try {
            fillReportDbTable(rh.getSession(), GENERAL_REPORT_QUERIES, "SystemGroup", mgmId);
            fillReportDbTable(rh.getSession(), GENERAL_REPORT_QUERIES, "SystemGroupPermission", mgmId);
            fillReportDbTable(rh.getSession(), GENERAL_REPORT_QUERIES, "Account", mgmId);
            fillReportDbTable(rh.getSession(), GENERAL_REPORT_QUERIES, "AccountGroup", mgmId);

            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "System", mgmId);
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemHistory", mgmId);
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemAction", mgmId);
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemChannel", mgmId);
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemConfigChannel", mgmId);
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemVirtualData", mgmId);
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemNetInterface", mgmId);
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemNetAddressV4", mgmId);
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemNetAddressV6", mgmId);
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemOutdated", mgmId);
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemGroupMember", mgmId);
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemEntitlement", mgmId);
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemErrata", mgmId);
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemPackageInstalled", mgmId);
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemPackageUpdate", mgmId);
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemCustomInfo", mgmId);

            fillReportDbTable(rh.getSession(), CHANNEL_REPORT_QUERIES, "Channel", mgmId);
            fillReportDbTable(rh.getSession(), CHANNEL_REPORT_QUERIES, "ChannelErrata", mgmId);
            fillReportDbTable(rh.getSession(), CHANNEL_REPORT_QUERIES, "ChannelPackage", mgmId);
            fillReportDbTable(rh.getSession(), CHANNEL_REPORT_QUERIES, "ChannelRepository", mgmId);
            fillReportDbTable(rh.getSession(), CHANNEL_REPORT_QUERIES, "Errata", mgmId);
            fillReportDbTable(rh.getSession(), CHANNEL_REPORT_QUERIES, "Package", mgmId);
            fillReportDbTable(rh.getSession(), CHANNEL_REPORT_QUERIES, "Repository", mgmId);

            fillReportDbTable(rh.getSession(), SCAP_REPORT_QUERIES, "XccdScan", mgmId);
            fillReportDbTable(rh.getSession(), SCAP_REPORT_QUERIES, "XccdScanResult", mgmId);

            ReportDBHelper.analyzeReportDb(rh.getSession());

            rh.commitTransaction();
            log.info("Reporting db updated successfully.");
        }
        catch (RuntimeException ex) {
            try {
                rh.rollbackTransaction();
            }
            catch (RuntimeException rollbackException) {
                log.warn("Unable to rollback transaction", rollbackException);
            }

            throw new JobExecutionException("Unable to update reporting db", ex);
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
