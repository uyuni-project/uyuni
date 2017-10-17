/**
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

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.ConstraintViolationException;
import com.redhat.rhn.common.db.WrappedSQLException;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.HibernateHelper;
import com.redhat.rhn.common.translation.ExceptionConstants;
import com.redhat.rhn.common.translation.SqlExceptionTranslator;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/*
 * $Rev$
 */
public class ExceptionsWrapperTest extends TestCase {

    private static final Logger LOG = Logger.getLogger(ExceptionsWrapperTest.class);
    private static final String EXCEPTION_TRANSLATOR =
        "com.redhat.rhn.common.translation.ExceptionTranslator";

    public ExceptionsWrapperTest(String name) {
        super(name);
    }

    public void testConstraintViolation() throws Exception {
        Session session = null;
        Statement stmt = null;
        try {
            session = HibernateFactory.getSession();
            stmt = session.connection().createStatement();

            stmt.execute("insert into exceptions_test (small_column, id) " +
                    "values ('tooBigAString', 1)");
        }
        catch (SQLException e) {
            if (!ConfigDefaults.get().isOracle()) {
                session.connection().rollback();
            }
            try {
                throw SqlExceptionTranslator.sqlException(e);
            }
            catch (ConstraintViolationException c) {
                assertNull(c.getConstraint());
                assertEquals(c.getConstraintType(),
                        ExceptionConstants.VALUE_TOO_LARGE);
            }
            // PostgreSQL
            catch (WrappedSQLException c) {
                assertTrue(c.getMessage().toLowerCase().contains("value too long"));
            }
        }
        finally {
            HibernateHelper.cleanupDB(stmt);
        }
    }

    public void testNamedConstraint() throws Exception {
        Session session = null;
        Statement stmt = null;
        try {
            session = HibernateFactory.getSession();
            stmt = session.connection().createStatement();

            stmt.execute("insert into exceptions_test (small_column, id) " +
                    "values ('in', 1)");
            stmt.execute("insert into exceptions_test (small_column, id) " +
                    "values ('ano', 1)");
        }
        catch (SQLException e) {
            if (!ConfigDefaults.get().isOracle()) {
                session.connection().rollback();
            }
            try {
                throw SqlExceptionTranslator.sqlException(e);
            }
            catch (ConstraintViolationException c) {
                assertTrue(c.getConstraint().indexOf("EXCEPTIONS_TEST_PK") >= 0);
                assertEquals(c.getConstraintType(),
                        ExceptionConstants.VALUE_TOO_LARGE);
            }
            // PostgreSQL
            catch (WrappedSQLException c) {
                assertTrue(c.getMessage().toLowerCase().contains("duplicate key"));
            }
        }
        finally {
            HibernateHelper.cleanupDB(stmt);
        }
    }

    public void testNotReplaced() throws Exception {
        Session session = null;
        Statement stmt = null;
        try {
            session = HibernateFactory.getSession();
            stmt = session.connection().createStatement();

            stmt.execute("insert into exceptions_test (foobar, id) " +
                    "values ('in', 1)");
            stmt.execute("insert into exceptions_test (small_column, id) " +
                    "values ('ano', 1)");
        }
        catch (SQLException e) {
            if (!ConfigDefaults.get().isOracle()) {
                session.connection().rollback();
            }
            try {
                throw SqlExceptionTranslator.sqlException(e);
            }
            catch (WrappedSQLException c) {
                // Expected WrappedSQLException
            }
        }
        finally {
            HibernateHelper.cleanupDB(stmt);
        }
    }

    // Make sure that there are no StackTraceElements from
    // com.redhat.rhn.common.translation
    public void testStackElements() throws Exception {
        Session session = null;
        Statement stmt = null;
        try {
            session = HibernateFactory.getSession();
            stmt = session.connection().createStatement();

            stmt.execute("insert into exceptions_test (foobar, id) " +
                    "values ('in', 1)");
            stmt.execute("insert into exceptions_test (small_column, id) " +
                    "values ('ano', 1)");
        }
        catch (SQLException e) {
            if (!ConfigDefaults.get().isOracle()) {
                session.connection().rollback();
            }
            try {
                throw SqlExceptionTranslator.sqlException(e);
            }
            catch (WrappedSQLException c) {
                StackTraceElement[] elements = c.getStackTrace();
                for (int i = 0; i < elements.length; i++) {
                    String method = elements[i].getMethodName();
                    String className = elements[i].getClassName();
                    assertFalse(className.equals(EXCEPTION_TRANSLATOR));
                    assertFalse(method.equals("convert"));
                }
            }
        }
        finally {
            HibernateHelper.cleanupDB(stmt);
        }

    }

    public static Test suite() throws Exception {
        TestSuite suite = new TestSuite(ExceptionsWrapperTest.class);

        return new TestSetup(suite) {
            protected void setUp() throws Exception {
                oneTimeSetup();
            }

            protected void tearDown() throws Exception {
                oneTimeTeardown();
            }
        };
    }

    protected static void oneTimeSetup() throws Exception {
        Session session = null;
        Statement stmt = null;
        try {
            session = HibernateFactory.getSession();
            stmt = session.connection().createStatement();
            stmt.executeQuery("select 1 from exceptions_test");
        }
        catch (SQLException e) {
            // Couldn't select 1, so the table didn't exist, create it
            if (ConfigDefaults.get().isOracle()) {
                stmt.execute("create table exceptions_test " +
                        "( " +
                        "  small_column VarChar2(5)," +
                        "  id     number" +
                        "         constraint exceptions_test_pk primary key" +
                        ")");
            }
            else {
                session.connection().rollback();
                stmt.execute("create table exceptions_test " +
                        "( " +
                        "  small_column VarChar(5)," +
                        "  id     numeric" +
                        "         constraint exceptions_test_pk primary key" +
                        ")");
            }

            session.connection().commit();
        }
        finally {
            HibernateHelper.cleanupDB(stmt);
        }
    }

    protected static void oneTimeTeardown() throws Exception {
        Session session = null;
        Statement stmt = null;
        try {
            session = HibernateFactory.getSession();
            stmt = session.connection().createStatement();
            Connection c = session.connection();
            forceQuery(c, "drop table exceptions_test");
            c.commit();
        }
        finally {
            HibernateHelper.cleanupDB(stmt);
        }
    }

    private static void forceQuery(Connection c, String query) {
        try {
            Statement stmt = c.createStatement();
            stmt.execute(query);
        }
        catch (SQLException se) {
            LOG.warn("Failed to execute query " + query + ": " +
                           se.toString());
        }
    }
}
