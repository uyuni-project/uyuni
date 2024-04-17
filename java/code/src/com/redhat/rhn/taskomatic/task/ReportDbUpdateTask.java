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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class ReportDbUpdateTask extends RhnJavaJob {

    private static final String GENERAL_REPORT_QUERIES = "GeneralReport_queries";
    private static final String SYSTEM_REPORT_QUERIES = "SystemReport_queries";
    private static final String CHANNEL_REPORT_QUERIES = "ChannelReport_queries";
    private static final String SCAP_REPORT_QUERIES = "ScapReport_queries";
    private static final String COCO_ATTESTATION_REPORT_QUERIES = "CoCoAttestationReport_queries";
    // Common fields
    private static final String SYSTEM_ID = "system_id";
    private static final String HISTORY_ID = "history_id";
    private static final String ACTION_ID = "action_id";
    private static final String CHANNEL_ID = "channel_id";
    private static final String CONFIG_CHANNEL_ID = "config_channel_id";
    private static final String INSTANCE_ID = "instance_id";
    private static final String INTERFACE_ID = "interface_id";
    private static final String SYSTEM_GROUP_ID = "system_group_id";
    private static final String ADDRESS = "address";
    private static final String ERRATA_ID = "errata_id";
    private static final String NAME = "name";
    private static final String ORGANIZATION = "organization";
    private static final String KEY = "key";
    public static final String ACCOUNT_ID = "account_id";
    public static final String ACCOUNT_GROUP_ID = "account_group_id";
    public static final String SCAN_ID = "scan_id";
    public static final String RULE_ID = "rule_id";
    public static final String IDENT_ID = "ident_id";
    public static final String PACKAGE_ID = "package_id";
    public static final String REPOSITORY_ID = "repository_id";
    public static final String REPORT_ID = "report_id";
    public static final String RESULT_TYPE = "result_type";

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

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        ConnectionManager rcm = ConnectionManagerFactory.localReportingConnectionManager();
        ReportDbHibernateFactory rh = new ReportDbHibernateFactory(rcm);

        try {
            fillReportDbTable(rh.getSession(), GENERAL_REPORT_QUERIES, "SystemGroup",
                Map.of(SYSTEM_GROUP_ID, 0));
            fillReportDbTable(rh.getSession(), GENERAL_REPORT_QUERIES, "SystemGroupPermission",
                Map.of(SYSTEM_GROUP_ID, 0, ACCOUNT_ID, 0));
            fillReportDbTable(rh.getSession(), GENERAL_REPORT_QUERIES, "Account",
                Map.of(ACCOUNT_ID, 0));
            fillReportDbTable(rh.getSession(), GENERAL_REPORT_QUERIES, "AccountGroup",
                Map.of(ACCOUNT_ID, 0, ACCOUNT_GROUP_ID, 0));

            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "System",
                Map.of(SYSTEM_ID, 0));
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemHistory",
                Map.of(SYSTEM_ID, 0, HISTORY_ID, 0));
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemAction",
                Map.of(SYSTEM_ID, 0, ACTION_ID, 0));
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemChannel",
                Map.of(SYSTEM_ID, 0, CHANNEL_ID, 0));
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemConfigChannel",
                Map.of(SYSTEM_ID, 0, CONFIG_CHANNEL_ID, 0));
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemVirtualData",
                Map.of(INSTANCE_ID, 0));
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemNetInterface",
                Map.of(SYSTEM_ID, 0, INTERFACE_ID, 0));
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemNetAddressV4",
                Map.of(SYSTEM_ID, 0, INTERFACE_ID, 0, ADDRESS, ""));
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemNetAddressV6",
                Map.of(SYSTEM_ID, 0, INTERFACE_ID, 0, ADDRESS, ""));
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemOutdated",
                Map.of(SYSTEM_ID, 0));
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemGroupMember",
                Map.of(SYSTEM_ID, 0, SYSTEM_GROUP_ID, 0));
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemEntitlement",
                Map.of(SYSTEM_ID, 0, SYSTEM_GROUP_ID, 0));
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemErrata",
                Map.of(SYSTEM_ID, 0, ERRATA_ID, 0));
            fillReportDbTableById(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemPackageInstalled",
                Map.of(NAME, ""));
            fillReportDbTableById(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemPackageUpdate",
                Map.of(PACKAGE_ID, 0));
            fillReportDbTable(rh.getSession(), SYSTEM_REPORT_QUERIES, "SystemCustomInfo",
                Map.of(ORGANIZATION, "", SYSTEM_ID, 0, KEY, ""));

            fillReportDbTable(rh.getSession(), CHANNEL_REPORT_QUERIES, "Channel",
                Map.of(CHANNEL_ID, 0));
            fillReportDbTable(rh.getSession(), CHANNEL_REPORT_QUERIES, "ChannelErrata",
                Map.of(CHANNEL_ID, 0, ERRATA_ID, 0));
            fillReportDbTable(rh.getSession(), CHANNEL_REPORT_QUERIES, "ChannelPackage",
                Map.of(CHANNEL_ID, 0, PACKAGE_ID, 0));
            fillReportDbTable(rh.getSession(), CHANNEL_REPORT_QUERIES, "ChannelRepository",
                Map.of(CHANNEL_ID, 0, REPOSITORY_ID, 0));
            fillReportDbTable(rh.getSession(), CHANNEL_REPORT_QUERIES, "Errata",
                Map.of(ERRATA_ID, 0));
            fillReportDbTable(rh.getSession(), CHANNEL_REPORT_QUERIES, "Package",
                Map.of(PACKAGE_ID, 0));
            fillReportDbTable(rh.getSession(), CHANNEL_REPORT_QUERIES, "Repository",
                Map.of(REPOSITORY_ID, 0));

            fillReportDbTable(rh.getSession(), SCAP_REPORT_QUERIES, "XccdScan",
                Map.of(SCAN_ID, 0));
            fillReportDbTable(rh.getSession(), SCAP_REPORT_QUERIES, "XccdScanResult",
                Map.of(SCAN_ID, 0, RULE_ID, 0, IDENT_ID, 0));

            fillReportDbTable(rh.getSession(), COCO_ATTESTATION_REPORT_QUERIES, "CoCoAttestation",
                    Map.of(REPORT_ID, 0));
            fillReportDbTable(rh.getSession(), COCO_ATTESTATION_REPORT_QUERIES, "CoCoAttestationResult",
                    Map.of(REPORT_ID, 0, RESULT_TYPE, 0));

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

    private void fillReportDbTable(Session session, String xmlName, String tableName, Map<String, Object> filterMap) {
        TimeUtils.logTime(log, "Refreshing table " + tableName, () -> {
            // Remove all the existing data
            log.debug("Deleting existing data in table {}", tableName);
            WriteMode delete = dbHelper.generateDelete(session, tableName);
            delete.executeUpdate(Map.of("mgm_id", LOCAL_MGM_ID));

            // Extract the first batch using the given filters, only adding the batch size as number of rows limit
            Map<String, Object> parametersMap = new HashMap<>(filterMap);
            parametersMap.put("limit", batchSize);

            fillTableInBatches(session, xmlName, tableName, tableName, parametersMap, filterMap.keySet());
        });
    }

    private void fillReportDbTableById(Session session, String xmlName, String tableName,
                                       Map<String, Object> filterMap) {
        TimeUtils.logTime(log, "Refreshing table " + tableName, () -> {
            SelectMode queryData = ModeFactory.getMode(xmlName, tableName + "_Ids", Map.class);

            // Remove all the existing data
            log.debug("Deleting existing data in table {}", tableName);
            WriteMode delete = dbHelper.generateDelete(session, tableName);
            delete.executeUpdate(Map.of("mgm_id", LOCAL_MGM_ID));

            // Get the full data set first
            DataResult<Map<String, Long>> dataSet = queryData.execute();
            if (dataSet.isEmpty()) {
                log.debug("No data extracted for table {}", tableName);
                return;
            }

            for (Map<String, Long> data : dataSet) {
                Long id = data.get("id");

                Map<String, Object> parametersMap = new HashMap<>(filterMap);
                parametersMap.put("id", id);
                parametersMap.put("limit", batchSize);

                fillTableInBatches(session, xmlName, tableName + "_byId", tableName, parametersMap, filterMap.keySet());
            }
        });
    }

    private void fillTableInBatches(Session session, String xmlName, String queryName, String tableName,
                                    Map<String, Object> parametersMap, Set<String> mutableFieldsSet) {
        SelectMode query = ModeFactory.getMode(xmlName, queryName, Map.class);
        DataResult<Map<String, Object>> dataBatch = query.execute(parametersMap);
        if (dataBatch.isEmpty()) {
            log.debug("No data extracted for table {}", tableName);
            return;
        }

        // Generate the insert using the column name retrieved from the select
        Set<String> columnParameters = dataBatch.get(0).keySet();
        WriteMode insert = dbHelper.generateInsertWithDate(session, tableName, LOCAL_MGM_ID, columnParameters);

        insert.executeUpdates(dataBatch);
        log.debug("Extracted {} rows for table {}", dataBatch.size(), tableName);

        // Iterate further if we can have additional rows
        while (dataBatch.size() >= batchSize) {
            dbHelper.updateParameters(parametersMap, dataBatch, mutableFieldsSet);

            dataBatch = query.execute(parametersMap);
            if (!dataBatch.isEmpty()) {
                log.debug("Extracted {} rows more for table {}", dataBatch.size(), tableName);
                insert.executeUpdates(dataBatch);
            }
        }
    }

    @Override
    public String getConfigNamespace() {
        return "report_db_update";
    }
}
