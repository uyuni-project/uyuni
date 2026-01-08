package com.redhat.rhn.common.hibernate.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.HibernateHelper;
import com.redhat.rhn.domain.test.TestFactory;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

abstract class HibernateBaseTest extends RhnBaseTestCase {
    private static final Logger LOG = LogManager.getLogger(HibernateBaseTest.class);

    @Override
    @BeforeEach
    public void setUp() {
        TestFactory.getSession();
    }

    @AfterEach
    public void tearDown() {
        try {
            if (HibernateFactory.inTransaction()) {
                HibernateFactory.commitTransaction();
            }
        }
        catch (Exception e) {
            LOG.warn("Error committing transaction in tearDown", e);
        }
        finally {
            HibernateFactory.closeSession();
        }
    }

    @BeforeAll
    public static void oneTimeSetup() {
        TestFactory.getSession().doWork(connection -> {
            Statement statement = null;
            try {
                statement = connection.createStatement();

                // Always clean up and recreate to ensure schema is current
                forceQuery(connection, "drop table if exists persist_test cascade");
                forceQuery(connection, "drop sequence if exists persist_sequence");

                statement.execute("create sequence persist_sequence");
                statement.execute("""
                        create table persist_test(
                                foobar VarChar(32),
                                test_column VarChar(5),
                                pin    numeric,
                                hidden VarChar(32),
                                id     numeric constraint persist_test_pk primary key,
                                parent_id numeric,
                                created timestamp with time zone,
                                modified timestamp with time zone
                                )""");
                statement.execute("insert into persist_test (foobar, id) " +
                        "values ('Blarg', nextval('persist_sequence'))");
                statement.execute("insert into persist_test (foobar, id) " +
                        "values ('duplicate', nextval('persist_sequence'))");
                statement.execute("insert into persist_test (foobar, id) " +
                        "values ('duplicate', nextval('persist_sequence'))");
                statement.execute("insert into persist_test (foobar, hidden, id) " +
                        "values ('duplicate', 'xxxxx', nextval('persist_sequence'))");
                statement.execute("insert into persist_test (foobar, id) " +
                        "values ('vito', nextval('persist_sequence'))");
                statement.execute("insert into persist_test (foobar, id, parent_id) " +
                        "values ('sonny', nextval('persist_sequence'), " +
                        "(select id from persist_test where foobar='vito'))");
                statement.execute("insert into persist_test (foobar, id, parent_id) " +
                        "values ('fredo', nextval('persist_sequence'), " +
                        "(select id from persist_test where foobar='vito'))");
                statement.execute("insert into persist_test (foobar, id, parent_id) " +
                        "values ('michael', nextval('persist_sequence'), " +
                        "(select id from persist_test where foobar='vito'))");

                connection.commit();
            }
            finally {
                HibernateHelper.cleanupDB(statement);
            }
        });
    }

    @AfterAll
    public static void oneTimeTeardown() {
        TestFactory.getSession().doWork(connection -> {
            Statement statement = null;
            try {
                statement = connection.createStatement();
                // Couldn't select 1, so the table didn't exist, create it
                forceQuery(connection, "drop sequence persist_sequence");
                forceQuery(connection, "drop table persist_test");
            }
            finally {
                HibernateHelper.cleanupDB(statement);
            }
        });
    }

    private static void forceQuery(Connection c, String query) {
        try {
            Statement stmt = c.createStatement();
            stmt.execute(query);
        }
        catch (SQLException se) {
            LOG.warn("Failed to execute query {}: {}", query, se.toString());
        }
    }

}
