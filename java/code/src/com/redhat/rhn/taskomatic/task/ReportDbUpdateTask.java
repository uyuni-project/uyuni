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
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.ParsedMode;
import com.redhat.rhn.common.db.datasource.ParsedQuery;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.ConnectionManager;
import com.redhat.rhn.common.hibernate.ConnectionManagerFactory;
import com.redhat.rhn.common.hibernate.ReportDbHibernateFactory;

import org.hibernate.Session;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class ReportDbUpdateTask extends RhnJavaJob {

    private static final int BATCH_SIZE = 100;

    private <T> Stream<DataResult<T>> batchStream(SelectMode m, int batchSize, int initialOffset) {
        return Stream.iterate(initialOffset, i -> i + batchSize)
                .map(offset -> (DataResult<T>)m.execute(Map.of(
                        "offset", offset,
                        "limit", batchSize
                )))
                .takeWhile(batch -> !batch.isEmpty());
    }

    private WriteMode generateDelete(String table, Session session) {
        return new WriteMode(session, new ParsedMode() {
            @Override
            public String getName() {
                return "generated.delete." + table;
            }

            @Override
            public ModeType getType() {
                return ModeType.WRITE;
            }

            @Override
            public ParsedQuery getParsedQuery() {
                return new ParsedQuery() {
                    @Override
                    public String getName() {
                        return "";
                    }

                    @Override
                    public String getAlias() {
                        return "";
                    }

                    @Override
                    public String getSqlStatement() {
                        return "DELETE FROM " + table + " WHERE mgm_id = :mgm_id";
                    }

                    @Override
                    public String getElaboratorJoinColumn() {
                        return "";
                    }

                    @Override
                    public List<String> getParameterList() {
                        return List.of("mgm_id");
                    }

                    @Override
                    public boolean isMultiple() {
                        return false;
                    }
                };
            }

            @Override
            public String getClassname() {
                return null;
            }

            @Override
            public List<ParsedQuery> getElaborators() {
                return null;
            }
        });
    }

    private WriteMode generateInsert(long mgmId, String table, Set<String> params, Session session) {
        return new WriteMode(session, new ParsedMode() {
            @Override
            public String getName() {
                return "generated.insert." + table;
            }

            @Override
            public ModeType getType() {
                return ModeType.WRITE;
            }

            @Override
            public ParsedQuery getParsedQuery() {
                return new ParsedQuery() {
                    @Override
                    public String getName() {
                        return "";
                    }

                    @Override
                    public String getAlias() {
                        return "";
                    }

                    @Override
                    public String getSqlStatement() {
                        return String.format(
                                "INSERT INTO %s (mgm_id, synced_date, %s) VALUES (%s, current_timestamp, %s)",
                                table,
                                params.stream().collect(Collectors.joining(",")),
                                mgmId,
                                params.stream().map(p -> ":" + p).collect(Collectors.joining(",")));
                    }

                    @Override
                    public String getElaboratorJoinColumn() {
                        return "";
                    }

                    @Override
                    public List<String> getParameterList() {
                        return new ArrayList<>(params);
                    }

                    @Override
                    public boolean isMultiple() {
                        return false;
                    }
                };
            }

            @Override
            public String getClassname() {
                return null;
            }

            @Override
            public List<ParsedQuery> getElaborators() {
                return null;
            }
        });
    }

    private void fillReportDbTable(Session session, String xmlQueryName, String queryMode, String tableName,
                                   Set<String> columns, long mgmId) {
        SelectMode query = ModeFactory.getMode(xmlQueryName, queryMode, Map.class);
        WriteMode delete = generateDelete(tableName, session);
        delete.executeUpdate(Map.of("mgm_id", 1));
        WriteMode insert = generateInsert(mgmId, tableName, columns, session);
        this.<Map<String, Object>>batchStream(query, BATCH_SIZE, 0)
                .forEach(insert::executeUpdates);
    }

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        ConnectionManager rcm = ConnectionManagerFactory.localReportingConnectionManager();
        ReportDbHibernateFactory rh = new ReportDbHibernateFactory(rcm);
        long mgmId = 1;
        //fillSystems(rh.getSession());
        fillReportDbTable(rh.getSession(), "SystemReport_queries", "System", "System",
            Set.of("system_id", "profile_name", "hostname", "minion_id",
                "minion_os_family", "minion_kernel_live_version", "machine_id",
                "registered_by", "registration_time", "last_checkin_time", "kernel_version",
                "architecture", "organization", "hardware"), mgmId);
        fillReportDbTable(rh.getSession(), "SystemReport_queries", "SystemHistory", "SystemHistory",
            Set.of("history_id", "system_id", "event", "event_data", "event_time", "hostname"), mgmId);

        fillReportDbTable(rh.getSession(), "ChannelReport_queries", "Channel", "Channel",
            Set.of("channel_id", "name", "label", "type", "arch", "summary", "description",
                "parent_channel_label", "organization"), mgmId);
        fillReportDbTable(rh.getSession(), "ChannelReport_queries", "Errata", "Errata",
            Set.of("errata_id", "advisory_name", "advisory_type", "advisory_status", "issue_date",
                "update_date", "severity", "reboot_required", "affects_package_manager", "cve",
                "synopsis", "organization"), mgmId);
        fillReportDbTable(rh.getSession(), "ChannelReport_queries", "Package", "Package",
            Set.of("package_id", "arch", "epoch", "installed_size", "name", "organization",
                "package_size", "payload_size", "release", "type", "vendor", "version"), mgmId);
        fillReportDbTable(rh.getSession(), "SystemReport_queries", "SystemAction", "SystemAction",
            Set.of("action_id", "system_id", "completion_time", "event", "event_data", "hostname", "pickup_time",
                "status"), mgmId);
        fillReportDbTable(rh.getSession(), "SystemReport_queries", "SystemChannel", "SystemChannel",
            Set.of("channel_id", "system_id", "architecture_name", "description", "name", "parent_channel_id",
                "parent_channel_name"), mgmId);
        fillReportDbTable(rh.getSession(), "SystemReport_queries", "SystemConfigChannel", "SystemConfigChannel",
            Set.of("config_channel_id", "system_id", "name", "position"), mgmId);

        fillReportDbTable(rh.getSession(), "SystemReport_queries", "SystemVirtualData", "SystemVirtualData",
            Set.of("instance_id", "host_system_id", "virtual_system_id", "confirmed", "instance_type_name",
                "memory_size", "name", "state_name", "uuid", "vcpus"), mgmId);
        fillReportDbTable(rh.getSession(), "SystemReport_queries", "SystemNetInterface", "SystemNetInterface",
            Set.of("system_id", "interface_id", "name", "hardware_addres", "module", "primary_interface"), mgmId);
        fillReportDbTable(rh.getSession(), "SystemReport_queries", "SystemNetAddressV4", "SystemNetAddressV4",
            Set.of("system_id", "interface_id", "address", "netmask", "broadcast"), mgmId);
        fillReportDbTable(rh.getSession(), "SystemReport_queries", "SystemNetAddressV6", "SystemNetAddressV6",
            Set.of("system_id", "interface_id", "scope", "address", "netmask"), mgmId);
        fillReportDbTable(rh.getSession(), "SystemReport_queries", "SystemOutdated", "SystemOutdated",
            Set.of("system_id", "errata_out_of_date", "packages_out_of_date"), mgmId);
        fillReportDbTable(rh.getSession(), "ChannelReport_queries", "SystemErrata", "SystemErrata",
            Set.of("system_id", "errata_id", "advisory_name", "advisory_type", "hostname"), mgmId);
        fillReportDbTable(rh.getSession(), "SystemReport_queries", "SystemGroup", "SystemGroup",
            Set.of("system_id", "system_group_id", "current_members", "description", "name", "organization"),
            mgmId);
        fillReportDbTable(rh.getSession(), "SystemReport_queries", "SystemEntitlement", "SystemEntitlement",
            Set.of("system_id", "system_group_id", "current_members", "description", "group_type",
                "group_type_name", "name", "organization"), mgmId);

        rh.commitTransaction();
        rh.closeSession();
        rh.closeSessionFactory();
    }

    @Override
    public String getConfigNamespace() {
        return "report_db_update";
    }
}
