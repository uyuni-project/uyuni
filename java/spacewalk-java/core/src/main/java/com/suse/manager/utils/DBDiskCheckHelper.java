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

import java.sql.ResultSet;
import java.sql.Savepoint;
import java.sql.Statement;

/**
 * Class to execute a check of the available disk space using a sql function.
 * Uses a JDBC savepoint so that a failure does not poison the Hibernate transaction.
 */
public class DBDiskCheckHelper extends DiskCheckHelper {

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
            catch (Exception e) {
                connection.rollback(sp);
                throw e;
            }
        });
    }
}
