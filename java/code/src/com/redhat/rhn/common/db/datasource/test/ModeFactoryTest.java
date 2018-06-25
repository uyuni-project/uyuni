package com.redhat.rhn.common.db.datasource.test;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.testing.RhnJmockBaseTestCase;

public class ModeFactoryTest extends RhnJmockBaseTestCase {

    private static final String MODE_NAME = "insert_into_table";
    private static final String POSTGRES_QUERY_SUFFIX = "_pg";
    private static final String MODE_NAME_POSTGRESQL = MODE_NAME + POSTGRES_QUERY_SUFFIX;

    private static final String DB_BACKEND = "db_backend";
    private static final String DB_BACKEND_ORACLE = "oracle";
    private static final String DB_BACKEND_POSTGRESQL = "postgresql";

    public void testGetWriteMode() {
        String dbBackend = Config.get().getString(DB_BACKEND);

        try {
            Config.get().setString(DB_BACKEND, DB_BACKEND_ORACLE);
            WriteMode writeMode = ModeFactory.getWriteMode("test_queries", MODE_NAME, true);
            this.assertWriteMode(writeMode, MODE_NAME);

            Config.get().setString(DB_BACKEND, DB_BACKEND_ORACLE);
            writeMode = ModeFactory.getWriteMode("test_queries", MODE_NAME, false);
            this.assertWriteMode(writeMode, MODE_NAME);

            Config.get().setString(DB_BACKEND, DB_BACKEND_POSTGRESQL);
            writeMode = ModeFactory.getWriteMode("test_queries", MODE_NAME, true);
            this.assertWriteMode(writeMode, MODE_NAME_POSTGRESQL);

            Config.get().setString(DB_BACKEND, DB_BACKEND_POSTGRESQL);
            writeMode = ModeFactory.getWriteMode("test_queries", MODE_NAME, false);
            this.assertWriteMode(writeMode, MODE_NAME);
        }
        finally {
            if(dbBackend != null) {
                Config.get().setString(DB_BACKEND, dbBackend);
            } else {
                Config.get().remove(DB_BACKEND);
            }
        }
    }

    private void assertWriteMode(WriteMode writeMode, String modeName) {
        assertNotNull(writeMode);
        assertEquals(modeName, writeMode.getName());
    }

}
