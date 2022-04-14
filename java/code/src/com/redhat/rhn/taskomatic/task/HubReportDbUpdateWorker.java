/*
 * Copyright (c) 2022 SUSE LLC
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
import static com.redhat.rhn.taskomatic.task.ReportDBHelper.generateExistingTables;
import static com.redhat.rhn.taskomatic.task.ReportDBHelper.generateInsert;
import static com.redhat.rhn.taskomatic.task.ReportDBHelper.generateQuery;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.ConnectionManager;
import com.redhat.rhn.common.hibernate.ConnectionManagerFactory;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.ReportDbHibernateFactory;
import com.redhat.rhn.common.util.TimeUtils;
import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.server.MgrServerInfo;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.taskomatic.task.threaded.QueueWorker;
import com.redhat.rhn.taskomatic.task.threaded.TaskQueue;

import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


public class HubReportDbUpdateWorker implements QueueWorker {

    private static final int BATCH_SIZE = Config.get()
            .getInt(ConfigDefaults.REPORT_DB_BATCH_SIZE, 2000);
    private TaskQueue parentQueue;
    private final MgrServerInfo mgrServerInfo;
    private Logger log;

    private static final List<String> TABLES =
            List.of("SystemGroup", "SystemGroupPermission", "System", "SystemHistory", "SystemAction", "SystemChannel",
            "SystemConfigChannel", "SystemVirtualData", "SystemNetInterface", "SystemNetAddressV4",
            "SystemNetAddressV6", "SystemOutdated", "SystemGroupMember", "SystemEntitlement", "SystemErrata",
            "SystemPackageInstalled", "SystemPackageUpdate", "SystemCustomInfo", "Account", "AccountGroup",
            "Channel", "ChannelPackage", "ChannelRepository", "ChannelErrata", "Errata", "Package", "Repository",
            "XccdScan", "XccdScanResult"
            );

    /**
     * Hub Reporting DB Worker
     * @param loggerIn logger
     * @param mgrServerInfoIn mgr server to query data from
     */
    public HubReportDbUpdateWorker(Logger loggerIn, MgrServerInfo mgrServerInfoIn) {
        this.mgrServerInfo = mgrServerInfoIn;
        this.log = loggerIn;
    }

    @Override
    public void setParentQueue(TaskQueue queue) {
        this.parentQueue = queue;
    }

    private List<String> filterExistingTables(Session remoteSession, Long serverId) {
        SelectMode query = generateExistingTables(remoteSession, TABLES);
        DataResult<Map<String, Object>> result = query.execute();
        Set<Map.Entry<String, Object>> tableEntry = result.get(0).entrySet();
        tableEntry.removeIf(t -> {
            if (t.getValue() == null) {
                log.warn("Table '" + t.getKey() + "' does not exist in server " + serverId + ": " +
                        "this table will not be updated");
                return true;
            }
            return false;
        });
        List<String> existingTables = tableEntry.stream().map(t ->
                String.valueOf(t.getValue())).collect(Collectors.toList());
        return existingTables;
    }

    private void updateRemoteData(Session remoteSession, Session localSession, String tableName, long mgmId) {
        TimeUtils.logTime(log, "Refreshing table " + tableName, () -> {
            SelectMode query = generateQuery(remoteSession, tableName, log);

            // Remove all the existing data
            log.debug("Deleting existing data in table {}", tableName);
            WriteMode delete = generateDelete(localSession, tableName);
            delete.executeUpdate(Map.of("mgm_id", mgmId));

            // Extract the first batch
            @SuppressWarnings("unchecked")
            DataResult<Map<String, Object>> firstBatch = query.execute(Map.of("offset", 0, "limit", BATCH_SIZE));
            firstBatch.forEach(e -> e.remove("mgm_id"));

            if (!firstBatch.isEmpty()) {
                // Generate the insert using the column name retrieved from the select
                WriteMode insert = generateInsert(localSession, tableName, mgmId, firstBatch.get(0).keySet());
                insert.executeBatchUpdates(firstBatch);
                log.debug("Extracted {} rows for table {}", firstBatch.size(), tableName);

                // Iterate further if we can have additional rows
                if (firstBatch.size() == BATCH_SIZE) {
                    ReportDBHelper.<Map<String, Object>>batchStream(query, BATCH_SIZE, BATCH_SIZE)
                            .forEach(batch -> {
                                batch.forEach(e -> e.remove("mgm_id"));
                                insert.executeBatchUpdates(batch);
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
    public void run() {
        try {
            parentQueue.workerStarting();
            ConnectionManager localRcm = ConnectionManagerFactory.localReportingConnectionManager();
            ReportDbHibernateFactory localRh = new ReportDbHibernateFactory(localRcm);
            Credentials credentials = mgrServerInfo.getReportDbCredentials();
            ConnectionManager remoteDBCM = ConnectionManagerFactory.reportingConnectionManager(
                    credentials.getUsername(), credentials.getPassword(),
                    ConfigDefaults.get().remoteReportDBUrl(
                            mgrServerInfo.getReportDbHost(),
                            mgrServerInfo.getReportDbPort(),
                            mgrServerInfo.getReportDbName())
                    );
            ReportDbHibernateFactory remoteDB = new ReportDbHibernateFactory(remoteDBCM);
            try {
                List<String> existingTables = filterExistingTables(remoteDB.getSession(), mgrServerInfo.getId());
                existingTables.forEach(table -> {
                    updateRemoteData(remoteDB.getSession(), localRh.getSession(), table, mgrServerInfo.getId());
                });
                ReportDBHelper.analyzeReportDb(localRh.getSession());
                Server mgrServer = ServerFactory.lookupById(mgrServerInfo.getId());
                mgrServer.getMgrServerInfo().setReportDbLastSynced(new Date());
                ServerFactory.save(mgrServer);
                HibernateFactory.commitTransaction();
                localRcm.commitTransaction();
                log.info("Reporting db updated for server " + mgrServerInfo.getServer().getId() + " successfully.");
            }
            catch (RuntimeException ex) {
                log.warn("Unable to update reporting db", ex);

                try {
                    localRcm.rollbackTransaction();
                }
                catch (RuntimeException rollbackException) {
                    log.warn("Unable to rollback transaction", rollbackException);
                }
            }
            finally {
                remoteDB.closeSession();
                remoteDB.closeSessionFactory();
                localRcm.closeSession();
                localRcm.close();
            }
        }
        catch (Exception e) {
            log.error(e);
        }
        finally {
            parentQueue.workerDone();
        }
    }
}
