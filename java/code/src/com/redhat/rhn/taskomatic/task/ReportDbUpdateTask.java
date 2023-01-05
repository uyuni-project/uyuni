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

import static com.redhat.rhn.common.conf.ConfigDefaults.REPORT_DB_BATCH_SIZE;
import static com.redhat.rhn.taskomatic.task.ReportDBHelper.LOCAL_MGM_ID;

import com.redhat.rhn.common.conf.Config;
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
import java.util.Set;


public class ReportDbUpdateTask extends RhnJavaJob {

    private static final String GENERAL_REPORT_QUERIES = "GeneralReport_queries";
    private static final String SYSTEM_REPORT_QUERIES = "SystemReport_queries";
    private static final String CHANNEL_REPORT_QUERIES = "ChannelReport_queries";
    private static final String SCAP_REPORT_QUERIES = "ScapReport_queries";

    private final int batchSize;

    private final ReportDBHelper dbHelper;

    /**
     * Default constructor
     */
    public ReportDbUpdateTask() {
        this(ReportDBHelper.INSTANCE, Config.get().getInt(REPORT_DB_BATCH_SIZE, 2000));
    }

    /**
     * Constructor used for unit test to specify the {@link ReportDBHelper}
     * @param dbHelperIn the {@link ReportDBHelper}
     * @param batchSizeIn the batch size
     */
    public ReportDbUpdateTask(ReportDBHelper dbHelperIn, int batchSizeIn) {
        this.dbHelper = dbHelperIn;
        this.batchSize = batchSizeIn;
    }

    private void fillReportDbTable(Session session, String xmlName, String tableName) {
        TimeUtils.logTime(log, "Refreshing table " + tableName, () -> {
            SelectMode query = ModeFactory.getMode(xmlName, tableName, Map.class);

            // Remove all the existing data
            log.debug("Deleting existing data in table {}", tableName);
            WriteMode delete = dbHelper.generateDelete(session, tableName);
            delete.executeUpdate(Map.of("mgm_id", LOCAL_MGM_ID));

            // Extract the first batch
            DataResult<Map<String, Object>> firstBatch = query.execute(Map.of("offset", 0, "limit", batchSize));
            if (!firstBatch.isEmpty()) {
                // Generate the insert using the column name retrieved from the select
                Set<String> columnParameters = firstBatch.get(0).keySet();
                WriteMode insert = dbHelper.generateInsertWithDate(session, tableName, LOCAL_MGM_ID, columnParameters);

                insert.executeUpdates(firstBatch);
                log.debug("Extracted {} rows for table {}", firstBatch.size(), tableName);

                // Iterate further if we can have additional rows
                if (firstBatch.size() == batchSize) {
                    dbHelper.<Map<String, Object>>batchStream(query, batchSize, batchSize)
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

        try {
            fillReportDbTable(rh.getSession(), GENERAL_REPORT_QUERIES, "SystemGroup");
            fillReportDbTable(rh.getSession(), GENERAL_REPORT_QUERIES, "SystemGroupPermission");
            fillReportDbTable(rh.getSession(), GENERAL_REPORT_QUERIES, "Account");
            fillReportDbTable(rh.getSession(), GENERAL_REPORT_QUERIES, "AccountGroup");

            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "System");
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemHistory");
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemAction");
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemChannel");
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemConfigChannel");
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemVirtualData");
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemNetInterface");
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemNetAddressV4");
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemNetAddressV6");
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemOutdated");
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemGroupMember");
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemEntitlement");
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemErrata");
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemPackageInstalled");
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemPackageUpdate");
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemCustomInfo");

            fillReportDbTable(rh.getSession(), CHANNEL_REPORT_QUERIES, "Channel");
            fillReportDbTable(rh.getSession(), CHANNEL_REPORT_QUERIES, "ChannelErrata");
            fillReportDbTable(rh.getSession(), CHANNEL_REPORT_QUERIES, "ChannelPackage");
            fillReportDbTable(rh.getSession(), CHANNEL_REPORT_QUERIES, "ChannelRepository");
            fillReportDbTable(rh.getSession(), CHANNEL_REPORT_QUERIES, "Errata");
            fillReportDbTable(rh.getSession(), CHANNEL_REPORT_QUERIES, "Package");
            fillReportDbTable(rh.getSession(), CHANNEL_REPORT_QUERIES, "Repository");

            fillReportDbTable(rh.getSession(), SCAP_REPORT_QUERIES, "XccdScan");
            fillReportDbTable(rh.getSession(), SCAP_REPORT_QUERIES, "XccdScanResult");

            dbHelper.analyzeReportDb(rh.getSession());

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
