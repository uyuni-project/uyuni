/*
 * Copyright (c) 2018--2021 SUSE LLC
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
package com.redhat.rhn.common.db.datasource.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.testing.RhnJmockBaseTestCase;

import org.junit.jupiter.api.Test;

public class ModeFactoryTest extends RhnJmockBaseTestCase {

    private static final String MODE_NAME = "insert_into_table";
    private static final String POSTGRES_QUERY_SUFFIX = "_pg";
    private static final String MODE_NAME_POSTGRESQL = MODE_NAME + POSTGRES_QUERY_SUFFIX;

    private static final String DB_BACKEND = "db_backend";
    private static final String DB_BACKEND_POSTGRESQL = "postgresql";

    @Test
    public void testGetWriteMode() {
        String dbBackend = Config.get().getString(DB_BACKEND);

        try {
            Config.get().setString(DB_BACKEND, DB_BACKEND_POSTGRESQL);
            WriteMode writeMode = ModeFactory.getWriteMode("test_queries", MODE_NAME, true);
            this.assertWriteMode(writeMode, MODE_NAME_POSTGRESQL);

            Config.get().setString(DB_BACKEND, DB_BACKEND_POSTGRESQL);
            writeMode = ModeFactory.getWriteMode("test_queries", MODE_NAME, false);
            this.assertWriteMode(writeMode, MODE_NAME);
        }
        finally {
            if (dbBackend != null) {
                Config.get().setString(DB_BACKEND, dbBackend);
            }
            else {
                Config.get().remove(DB_BACKEND);
            }
        }
    }

    private void assertWriteMode(WriteMode writeMode, String modeName) {
        assertNotNull(writeMode);
        assertEquals(modeName, writeMode.getName());
    }

}
