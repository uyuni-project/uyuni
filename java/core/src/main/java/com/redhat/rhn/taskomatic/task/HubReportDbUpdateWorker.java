/*
 * Copyright (c) 2022--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.taskomatic.task;

import static com.redhat.rhn.common.conf.ConfigDefaults.REPORT_DB_BATCH_SIZE;

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
import com.redhat.rhn.domain.credentials.ReportDBCredentials;
import com.redhat.rhn.domain.notification.UserNotificationFactory;
import com.redhat.rhn.domain.notification.types.NotificationData;
import com.redhat.rhn.domain.notification.types.ReportDatabaseUpdateFailed;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.MgrServerInfo;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.taskomatic.TaskoFactory;
import com.redhat.rhn.taskomatic.task.threaded.QueueWorker;
import com.redhat.rhn.taskomatic.task.threaded.TaskQueue;

import org.apache.logging.log4j.Logger;
import org.hibernate.Session;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;


public class HubReportDbUpdateWorker implements QueueWorker {

    private final int batchSize;
    private final ReportDBHelper dbHelper;
    private TaskQueue parentQueue;
    private final MgrServerInfo mgrServerInfo;
    private final Logger log;

    private static final List<String> TABLES =
            List.of("SystemGroup", "SystemGroupPermission", "System", "SystemHardware", "SystemHistory", "SystemAction",
            "SystemChannel", "SystemConfigChannel", "SystemVirtualData", "SystemNetInterface", "SystemNetAddressV4",
            "SystemNetAddressV6", "SystemOutdated", "SystemGroupMember", "SystemEntitlement", "SystemErrata",
            "SystemPackageInstalled", "SystemPackageUpdate", "SystemCustomInfo", "Account", "AccountGroup",
            "Channel", "ChannelPackage", "ChannelRepository", "ChannelErrata", "Errata", "Package", "Repository",
            "CLMEnvironmentDifference", "XccdScan", "XccdScanResult", "CoCoAttestation", "CoCoAttestationResult"
            );

    /**
     * Hub Reporting DB Worker
     * @param loggerIn logger
     * @param mgrServerInfoIn mgr server to query data from
     */
    public HubReportDbUpdateWorker(Logger loggerIn, MgrServerInfo mgrServerInfoIn) {
        this(loggerIn, mgrServerInfoIn, ReportDBHelper.INSTANCE, Config.get().getInt(REPORT_DB_BATCH_SIZE, 2000));
    }

    /**
     * Test constructor for Hub Reporting DB Worker.
     * @param loggerIn logger
     * @param mgrServerInfoIn mgr server to query data from
     * @param dbHelperIn the {@link ReportDBHelper}
     * @param batchSizeIn the batch size
     */
    public HubReportDbUpdateWorker(Logger loggerIn, MgrServerInfo mgrServerInfoIn, ReportDBHelper dbHelperIn,
                                      int batchSizeIn) {
        this.mgrServerInfo = mgrServerInfoIn;
        this.log = loggerIn;
        this.dbHelper = dbHelperIn;
        this.batchSize = batchSizeIn;
    }

    @Override
    public void setParentQueue(TaskQueue queue) {
        this.parentQueue = queue;
    }

    private Stream<String> filterExistingTables(Session remoteSession, Long serverId) {
        SelectMode query = dbHelper.generateExistingTables(remoteSession, TABLES);
        DataResult<Map<String, Object>> result = query.execute();
        Set<Map.Entry<String, Object>> tableEntry = result.get(0).entrySet();
        tableEntry.removeIf(t -> {
            if (t.getValue() == null) {
                log.warn("Table '{}' does not exist in server {}: this table will not be updated",
                        t.getKey(), serverId);
                return true;
            }
            return false;
        });

        return tableEntry.stream()
            .map(t -> String.valueOf(t.getValue()));
    }

    private void updateRemoteData(Session remoteSession, Session localSession, String tableName, long mgmId) {
        TimeUtils.logTime(log, "Refreshing table " + tableName, () -> {
            SelectMode query = dbHelper.generateQuery(remoteSession, tableName, log);

            // Remove all the existing data
            log.debug("Deleting existing data in table {}", tableName);
            WriteMode delete = dbHelper.generateDelete(localSession, tableName);
            delete.executeUpdate(Map.of("mgm_id", mgmId));

            // Extract the first batch
            DataResult<Map<String, Object>> firstBatch = query.execute(Map.of("offset", 0, "limit", batchSize));
            firstBatch.forEach(e -> e.remove("mgm_id"));

            if (!firstBatch.isEmpty()) {
                // Generate the insert using the column name retrieved from the select
                WriteMode insert = dbHelper.generateInsert(localSession, tableName, mgmId, firstBatch.get(0).keySet());
                insert.executeBatchUpdates(firstBatch);
                log.debug("Extracted {} rows for table {}", firstBatch.size(), tableName);

                // Iterate further if we can have additional rows
                if (firstBatch.size() == batchSize) {
                    dbHelper.<Map<String, Object>>batchStream(query, batchSize, batchSize)
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
            HubReportDbUpdateDriver.getCurrentMgrServerInfos().add(mgrServerInfo);
            parentQueue.workerStarting();

            ReportDbHibernateFactory localDB = getLocalReportDbHibernateFactory();
            ReportDbHibernateFactory remoteDB = getRemoteReportDbHibernateFactory();

            extractDataFromRemoteDatabase(localDB, remoteDB);
        }
        catch (Exception e) {
            TaskoFactory.markFailed(parentQueue.getQueueRun());
            parentQueue.changeRun(null);

            NotificationData messageData = new ReportDatabaseUpdateFailed(e, mgrServerInfo.getServer().getName());
            UserNotificationFactory.storeNotificationMessageFor(
                UserNotificationFactory.createNotificationMessage(messageData),
                RoleFactory.SAT_ADMIN,
                Optional.empty()
            );

            log.error("Unable to update reporting db from {}", mgrServerInfo.getServer().getName(), e);
        }
        finally {
            parentQueue.workerDone();
            HibernateFactory.closeSession();
            HubReportDbUpdateDriver.getCurrentMgrServerInfos().remove(mgrServerInfo);
        }
    }

    private void extractDataFromRemoteDatabase(ReportDbHibernateFactory localDB, ReportDbHibernateFactory remoteDB) {
        try {
            Stream<String> existingTables = filterExistingTables(remoteDB.getSession(), mgrServerInfo.getId());
            existingTables.forEach(table -> {
                updateRemoteData(remoteDB.getSession(), localDB.getSession(), table, mgrServerInfo.getId());
            });
            dbHelper.analyzeReportDb(localDB.getSession());
            Server mgrServer = ServerFactory.lookupById(mgrServerInfo.getId());
            mgrServer.getMgrServerInfo().setReportDbLastSynced(new Date());
            ServerFactory.save(mgrServer);
            HibernateFactory.commitTransaction();
            localDB.commitTransaction();
            log.info("Reporting db updated for server {} successfully.", mgrServerInfo.getServer().getId());
        }
        catch (RuntimeException ex) {
            try {
                localDB.rollbackTransaction();
            }
            catch (RuntimeException rollbackException) {
                log.warn("Unable to rollback transaction", rollbackException);
            }

            throw ex;
        }
        finally {
            remoteDB.closeSession();
            remoteDB.closeSessionFactory();
            localDB.closeSession();
            localDB.closeSessionFactory();
        }
    }

    private ReportDbHibernateFactory getRemoteReportDbHibernateFactory() {
        ReportDBCredentials credentials = mgrServerInfo.getReportDbCredentials();
        ConnectionManager remoteConnectionManager = ConnectionManagerFactory.reportingConnectionManager(
                credentials.getUsername(), credentials.getPassword(),
                ConfigDefaults.get().remoteReportDBUrl(
                        mgrServerInfo.getReportDbHost(),
                        mgrServerInfo.getReportDbPort(),
                        mgrServerInfo.getReportDbName())
                );

        return new ReportDbHibernateFactory(remoteConnectionManager);
    }

    private static ReportDbHibernateFactory getLocalReportDbHibernateFactory() {
        ConnectionManager localConnectionManager = ConnectionManagerFactory.localReportingConnectionManager();
        return new ReportDbHibernateFactory(localConnectionManager);
    }
}
