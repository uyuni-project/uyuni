/*
 * Copyright (c) 2009--2013 Red Hat, Inc.
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
package com.redhat.rhn.common.translation.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.db.ConstraintViolationException;
import com.redhat.rhn.common.db.WrappedSQLException;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.HibernateHelper;
import com.redhat.rhn.common.translation.ExceptionConstants;
import com.redhat.rhn.common.translation.SqlExceptionTranslator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class ExceptionsWrapperTest  {

    private static final Logger LOG = LogManager.getLogger(ExceptionsWrapperTest.class);
    private static final String EXCEPTION_TRANSLATOR =
        "com.redhat.rhn.common.translation.ExceptionTranslator";

    @Test
    public void testConstraintViolation() {
        HibernateFactory.getSession().doWork(connection -> {
            Statement statement = null;
            try {
                statement = connection.createStatement();

                statement.execute("insert into exceptions_test (small_column, id) " +
                    "values ('tooBigAString', 1)");
            }
            catch (SQLException e) {
                connection.rollback();
                try {
                    throw SqlExceptionTranslator.sqlException(e);
                }
                catch (ConstraintViolationException c) {
                    assertNull(c.getConstraint());
                    assertEquals(c.getConstraintType(), ExceptionConstants.VALUE_TOO_LARGE);
                }
                // PostgreSQL
                catch (WrappedSQLException c) {
                    assertTrue(c.getMessage().toLowerCase().contains("value too long"));
                }
            }
            finally {
                HibernateHelper.cleanupDB(statement);
            }
        });
    }

    @Test
    public void testNamedConstraint() {
        HibernateFactory.getSession().doWork(connection -> {
            Statement statement = null;
            try {
                statement = connection.createStatement();

                statement.execute("insert into exceptions_test (small_column, id) " +
                    "values ('in', 1)");
                statement.execute("insert into exceptions_test (small_column, id) " +
                    "values ('ano', 1)");
            }
            catch (SQLException e) {
                connection.rollback();
                try {
                    throw SqlExceptionTranslator.sqlException(e);
                }
                catch (ConstraintViolationException c) {
                    assertTrue(c.getConstraint().contains("EXCEPTIONS_TEST_PK"));
                    assertEquals(c.getConstraintType(), ExceptionConstants.VALUE_TOO_LARGE);
                }
                // PostgreSQL
                catch (WrappedSQLException w) {
                    assertTrue(w.getMessage().toLowerCase().contains("duplicate key"));
                }
            }
            finally {
                HibernateHelper.cleanupDB(statement);
            }
        });
    }

    @Test
    public void testNotReplaced() {
        HibernateFactory.getSession().doWork(connection -> {
            Statement statement = null;
            try {
                statement = connection.createStatement();

                statement.execute("insert into exceptions_test (foobar, id) " +
                    "values ('in', 1)");
                statement.execute("insert into exceptions_test (small_column, id) " +
                    "values ('ano', 1)");
            }
            catch (SQLException e) {
                connection.rollback();
                try {
                    throw SqlExceptionTranslator.sqlException(e);
                }
                catch (WrappedSQLException c) {
                    // Expected WrappedSQLException
                }
            }
            finally {
                HibernateHelper.cleanupDB(statement);
            }
        });
    }

    // Make sure that there are no StackTraceElements from
    // com.redhat.rhn.common.translation
    @Test
    public void testStackElements() {
        HibernateFactory.getSession().doWork(connection -> {
            Statement statement = null;
            try {
                statement = connection.createStatement();

                statement.execute("insert into exceptions_test (foobar, id) " +
                    "values ('in', 1)");
                statement.execute("insert into exceptions_test (small_column, id) " +
                    "values ('ano', 1)");
            }
            catch (SQLException e) {
                connection.rollback();
                try {
                    throw SqlExceptionTranslator.sqlException(e);
                }
                catch (WrappedSQLException w) {
                    StackTraceElement[] elements = w.getStackTrace();
                    for (StackTraceElement elementIn : elements) {
                        String method = elementIn.getMethodName();
                        String className = elementIn.getClassName();
                        assertNotEquals(EXCEPTION_TRANSLATOR, className);
                        assertNotEquals("convert", method);
                    }
                }
            }
            finally {
                HibernateHelper.cleanupDB(statement);
            }
        });
    }

    @BeforeAll
    public static void oneTimeSetup() {
        HibernateFactory.getSession().doWork(connection -> {
            Statement statement = null;
            try {
                statement = connection.createStatement();
                statement.executeQuery("select 1 from exceptions_test");
            }
            catch (SQLException e) {
                // Couldn't select 1, so the table didn't exist, create it
                connection.rollback();
                statement.execute("create table exceptions_test ( " +
                        "small_column VarChar(5), " +
                        "id numeric " +
                        "constraint exceptions_test_pk primary key" +
                        ")");

                connection.commit();
            }
            finally {
                HibernateHelper.cleanupDB(statement);
            }
        });
    }

    @AfterAll
    public static void oneTimeTeardown() {
        HibernateFactory.getSession().doWork(connection -> {
            Statement statement = null;
            try {
                statement = connection.createStatement();
                forceQuery(connection, "drop table exceptions_test");
                connection.commit();
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
