/*
 * Copyright (c) 2023 SUSE LLC
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
package com.redhat.rhn.manager.report;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.hibernate.ConnectionManager;
import com.redhat.rhn.common.hibernate.ConnectionManagerFactory;
import com.redhat.rhn.common.hibernate.ReportDbHibernateFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.manager.report.dto.SystemInventoryOverview;

import com.suse.manager.utils.PagedSqlQueryBuilder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 * inventory query worker
 */
public class InventoryReport {

    protected static Logger log = LogManager.getLogger(InventoryReport.class);

    private InventoryReport() { }

    /**
     * Get system inventory data
     *
     * @param user Currently logged in user.
     * @param parser the filter value parser to use
     * @param pc PageControl
     * @return list of SystemOverviews.
     */
    public static DataResult<SystemInventoryOverview> getSystemsInventory(User user,
                                               Function<Optional<PageControl>,
                                               PagedSqlQueryBuilder.FilterWithValue> parser,
                                               PageControl pc) {
        ConnectionManager rcm = ConnectionManagerFactory.localReportingConnectionManager();
        ReportDbHibernateFactory rh = new ReportDbHibernateFactory(rcm);

        try {
            return new PagedSqlQueryBuilder("profile_name")
                    .select("system.mgm_id,\n" +
                            "    system.system_id,\n" +
                            "    system.minion_id,\n" +
                            "    system.machine_id,\n" +
                            "    system.profile_name,\n" +
                            "    system.hostname,\n" +
                            "    system.last_checkin_time,\n" +
                            "    system.synced_date,\n" +
                            "    system.kernel_version,\n" +
                            "    systemoutdated.packages_out_of_date,\n" +
                            "    systemoutdated.errata_out_of_date,\n" +
                            "    system.organization,\n" +
                            "    system.architecture")
                    .from("system \n" +
                            "     LEFT JOIN systemoutdated " +
                            " ON system.mgm_id = systemoutdated.mgm_id " +
                            " AND system.system_id = systemoutdated.system_id")
                    .where("system.organization = :user_org and system.mgm_id = 1")
                    .run(Map.of("user_org", user.getOrg().getName()),
                            pc, parser, SystemInventoryOverview.class, rh.getSession());
        }
        catch (RuntimeException ex) {
            try {
                rh.rollbackTransaction();
            }
            catch (RuntimeException rollbackException) {
                log.warn("Unable to rollback transaction", rollbackException);
            }

            throw new RuntimeException("Unable to get data from reporting db", ex);
        }
        finally {
            rh.closeSession();
            rh.closeSessionFactory();
        }
    }
}
