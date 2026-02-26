/*
 * Copyright (c) 2026 SUSE LCC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.manager.content.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.beans.PropertyVetoException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * C3P0 connection pool test
 */
public class C3P0ConnectionPoolTest {

    private ComboPooledDataSource dataSource;
    private static final String JDBC_DRIVER = "org.postgresql.Driver";
    private static final String DB_HOST = "localhost";
    private static final int DB_PORT = 5432;
    private static final String DB_NAME = "susemanager";
    private static final String DB_USER = "spacewalk";
    private static final String DB_PASSWORD = "spacewalk";

    @BeforeEach
    public void setUp() throws PropertyVetoException {
        dataSource = new ComboPooledDataSource();
        dataSource.setDriverClass(JDBC_DRIVER);
        dataSource.setJdbcUrl("jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME);
        dataSource.setUser(DB_USER);
        dataSource.setPassword(DB_PASSWORD);
    }

    @AfterEach
    public void tearDown() {
        if (dataSource != null) {
            try {
                dataSource.close();
            } catch (Exception e) {
                fail("Error closing datasource: " + e.getMessage());
            }
        }
    }

    /**
     * Test maxPoolSize by trying to add more connections than max.
     * Acquires max connections, then expects the following one to timeout.
     */
    @Test
    @Timeout(value = 15, unit = TimeUnit.SECONDS)
    public void testMaxPoolSizeViolation() throws SQLException, InterruptedException {
        int maxPoolSize = 2;
        List<Connection> connections = new ArrayList<>();

        //
        dataSource.setMaxPoolSize(maxPoolSize);
        dataSource.setMinPoolSize(1);
        dataSource.setCheckoutTimeout(2000);

        try {
            for(int i = 0 ; i < maxPoolSize; i++) {
                connections.add(dataSource.getConnection());
            }

            assertEquals(maxPoolSize, dataSource.getNumBusyConnections());
            assertEquals(0, dataSource.getNumIdleConnections());

            long startTime = System.currentTimeMillis();
            SQLException thrownException = null;
            try {
                dataSource.getConnection();
                fail("Should not be able to acquire 3rd connection when maxPoolSize=" + maxPoolSize);
            } catch (SQLException e) {
                thrownException = e;
                assertTrue(System.currentTimeMillis() - startTime >= 2000);
            }

            assertNotNull(thrownException, "Should have thrown SQLException for pool exhaustion");
        } finally {
            for (Connection conn : connections) {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            }
        }

        Thread.sleep(500);
        assertEquals(0, dataSource.getNumBusyConnections());
        assertEquals(maxPoolSize, dataSource.getNumIdleConnections());
    }

    /**
     * Same test as before, just with concurrency.
     */
    @Test
    @Timeout(value = 20, unit = TimeUnit.SECONDS)
    public void testMaxPoolSizeViolationWithConcurrency() throws InterruptedException, SQLException {
        int maxPoolSize = 2;
        int numberOfThreads = 10;
        AtomicInteger successfulAcquired = new AtomicInteger(0);
        AtomicInteger timeoutThreads = new AtomicInteger(0);

        //
        dataSource.setMaxPoolSize(maxPoolSize);
        dataSource.setMinPoolSize(1);
        dataSource.setCheckoutTimeout(2000);

        // Create threads trying to acquire connections simultaneously
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < numberOfThreads; i++) {
            Thread t = new Thread(() -> {
                try {
                    Connection conn = dataSource.getConnection();
                    successfulAcquired.incrementAndGet();

                    // Hold the connection for a longer time to force contention
                    Thread.sleep(3000);
                    conn.close();
                } catch (SQLException e) {
                    timeoutThreads.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            threads.add(t);
        }

        for (Thread t : threads) {
            t.start();
        }
        for (Thread t : threads) {
            t.join();
        }

        Thread.sleep(500);
        assertEquals(maxPoolSize, successfulAcquired.get());
        assertEquals(numberOfThreads - maxPoolSize, timeoutThreads.get());
        assertEquals(0, dataSource.getNumBusyConnections());
        assertEquals(maxPoolSize, dataSource.getNumIdleConnections());
    }

}

