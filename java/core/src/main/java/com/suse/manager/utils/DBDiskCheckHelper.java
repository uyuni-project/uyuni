/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.utils;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.sql.Statement;

/**
 * Class to execute a check of the available disk space using a sql function.
 * Uses a JDBC savepoint so that a failure does not poison the Hibernate transaction.
 */
public class DBDiskCheckHelper extends DiskCheckHelper {

    private static final Logger LOG = LogManager.getLogger(DBDiskCheckHelper.class);

    // PostgreSQL SQLSTATE for undefined_function
    private static final String SQLSTATE_UNDEFINED_FUNCTION = "42883";

    /**
     * {@inheritDoc}
     */
    @Override
    protected int performCheck() {
        return HibernateFactory.getSession().doReturningWork(connection -> {
            Savepoint sp = connection.setSavepoint();
            try (Statement stmt = connection.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT get_pgsql_disk_severity()")) {
                rs.next();
                connection.releaseSavepoint(sp);
                return rs.getInt(1);
            }
            catch (SQLException e) {
                connection.rollback(sp);
                if (SQLSTATE_UNDEFINED_FUNCTION.equals(e.getSQLState())) {
                    LOG.warn("get_pgsql_disk_severity() is not installed; reporting UNDEFINED");
                    return -1;
                }
                throw e;
            }
        });
    }
}
