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
package com.redhat.rhn.common.hibernate;

import com.redhat.rhn.domain.TestFactory;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.stream.Collectors;

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
            executeSqlScript(connection, "create_test_db.sql");
            connection.commit();
        });
    }

    @AfterAll
    public static void oneTimeTeardown() {
        TestFactory.getSession().doWork(connection -> {
            executeSqlScript(connection, "drop_test_db.sql");
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

    private static void executeSqlScript(Connection c, String scriptPath) {
        try (var is = HibernateBaseTest.class.getResourceAsStream(scriptPath);
             var reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(is)))) {

            // Read the file and split by semicolon
            String[] queries = reader.lines()
                    .filter(line -> StringUtils.isNotEmpty(line) && !Strings.CI.startsWithAny(line, "--", "//"))
                    .collect(Collectors.joining("\n"))
                    .split(";");

            try (Statement statement = c.createStatement()) {
                for (String query : queries) {
                    if (!query.trim().isEmpty()) {
                        statement.addBatch(query);
                    }
                }
                statement.executeBatch();
            }
        }
        catch (IOException ex) {
            LOG.error("Unable to read SQL script {}", scriptPath, ex);
            throw new RuntimeException("Unable to read SQL script " + scriptPath, ex);
        }
        catch (SQLException ex) {
            LOG.error("Unable to execute SQL script {}", scriptPath, ex);
            throw new RuntimeException("Unable to execute SQL script " + scriptPath, ex);
        }
    }

}
