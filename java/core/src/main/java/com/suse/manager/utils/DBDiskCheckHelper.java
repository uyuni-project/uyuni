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

package com.suse.manager.utils;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

/**
 * Class to execute a check of the available disk space using a sql function
 */
public class DBDiskCheckHelper extends DiskCheckHelper {

    private static final Logger LOG = LogManager.getLogger(DBDiskCheckHelper.class);

    /**
     * Executes the SQL function and returns the result as an exit code.
     * @return the value returned from the database query.
     * @throws IOException when https://bugzilla.suse.com/show_bug.cgi?id=1253153 a database error occurs.
     */
    @Override
    protected int performCheck() throws IOException, InterruptedException {
        try {
            Object result = HibernateFactory.getSession()
                    .createNativeQuery("SELECT get_pgsql_disk_usage_percent()")
                    .getSingleResult();

            if (result instanceof Number) {
                return ((Number) result).intValue();
            }
            throw new IOException("Database did not return a numeric value.");
        }
        catch (Exception e) {
            // Wrapping Hibernate exceptions into IOException to match the parent signature
            throw new IOException("Failed to execute database disk check", e);
        }
    }
}
