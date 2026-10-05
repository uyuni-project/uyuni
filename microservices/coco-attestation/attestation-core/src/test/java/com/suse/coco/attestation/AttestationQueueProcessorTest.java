/*
 * Copyright (c) 2024 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.coco.attestation;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.AdditionalAnswers.answersWithDelay;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.suse.coco.module.AttestationModuleLoader;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.postgresql.PGNotification;
import org.postgresql.core.QueryExecutor;
import org.postgresql.jdbc.PgConnection;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

@ExtendWith(MockitoExtension.class)
@Timeout(30)
class AttestationQueueProcessorTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private AttestationResultService resultService;

    @Mock
    private ExecutorService executorService;

    @Mock
    private AttestationModuleLoader moduleLoader;

    @Mock
    private PgConnection pgConnection;

    @Mock
    private QueryExecutor queryExecutor;

    @Mock
    private Statement statement;

    private AttestationQueueProcessor processor;

    @BeforeEach
    public void setUp() throws SQLException, InterruptedException {
        Awaitility.setDefaultTimeout(5, TimeUnit.SECONDS);

        processor = new AttestationQueueProcessor(dataSource, resultService, executorService, moduleLoader, 10);

        // Basic mocking
        when(dataSource.getConnection())
            .thenReturn(pgConnection);

        when(pgConnection.isWrapperFor(PgConnection.class))
            .thenReturn(true);

        when(pgConnection.unwrap(argThat(clazz -> clazz.getName().equals(PgConnection.class.getName()))))
            .thenReturn(pgConnection);

        when(pgConnection.createStatement())
            .thenReturn(statement);

        when(pgConnection.getNotifications(anyInt()))
            // Return a new notification each quarter of a second
            .thenAnswer(answersWithDelay(250, ivn -> new PGNotification[0]));

        // Simulate the executor shutdown
        when(executorService.isShutdown()).thenReturn(false);
        when(executorService.isTerminated()).thenReturn(false);
        when(executorService.awaitTermination(anyLong(), any())).thenReturn(true);
    }

    @AfterEach
    public void tearDown() {
        Awaitility.reset();
    }

    @Test
    @DisplayName("An interruption in the listener process terminates the processor")
    void testInterruptingListenerShouldTerminateProcessor() {
        processor.start();

        await("The listener thread to start")
            .untilAsserted(() -> {
                // Verify the listener thread has been created
                assertNotNull(getThreadByName("attestation-processor-listener"));
                // Verify the main thread has been created
                assertNotNull(getThreadByName("attestation-processor-main"));
                // And it's actually waiting for notifications
                verify(pgConnection, atLeastOnce()).getNotifications(anyInt());
            });

        // Interrupt the listener
        Thread processorListener = getThreadByName("attestation-processor-listener");
        assertNotNull(processorListener);
        processorListener.interrupt();

        // Verify the processor and the listener are correctly stopped
        await("The processor and the listener threads to properly stopped")
            .untilAsserted(() -> {
                assertFalse(processor.isRunning());

                assertNull(getThreadByName("attestation-processor-listener"));
                assertNull(getThreadByName("attestation-processor-main"));
                assertNull(getThreadByName("test-thread"));

                verify(executorService).shutdown();
            });

        // Verify shutdown was called
        verify(executorService).shutdown();
    }

    @Test
    @DisplayName("An interruption in the main process terminates the listener")
    void testInterruptingProcessorShouldTerminateListener() {
        processor.start();

        await("The listener thread to start")
            .untilAsserted(() -> {
                assertTrue(processor.isRunning());

                // Verify the listener thread has been created
                assertNotNull(getThreadByName("attestation-processor-listener"));
                // Verify the main thread has been created
                assertNotNull(getThreadByName("attestation-processor-main"));
                // And it's actually waiting for notifications
                verify(pgConnection, atLeastOnce()).getNotifications(anyInt());
            });

        // Interrupt the listener
        Thread processorMain = getThreadByName("attestation-processor-main");
        assertNotNull(processorMain);
        processorMain.interrupt();

        // Verify the processor and the listener are correctly stopped
        await("The processor and the listener threads to properly stopped")
            .untilAsserted(() -> {
                assertFalse(processor.isRunning());

                assertNull(getThreadByName("attestation-processor-listener"));
                assertNull(getThreadByName("attestation-processor-main"));
                assertNull(getThreadByName("test-thread"));

                verify(executorService).shutdown();
            });
    }

    @Test
    @DisplayName("The processor can be manually shut down")
    void canBeShutDown() {
        when(pgConnection.getQueryExecutor())
            .thenReturn(queryExecutor);

        processor.start();

        await("The processor and the listener threads to properly stopped")
            .untilAsserted(() -> {
                assertTrue(processor.isRunning());

                // Verify the listener thread has been created
                assertNotNull(getThreadByName("attestation-processor-listener"));
                // Verify the main thread has been created
                assertNotNull(getThreadByName("attestation-processor-main"));
                // And it's actually waiting for notifications
                verify(pgConnection, atLeastOnce()).getNotifications(anyInt());
            });

        // Interrupt the listener
        processor.stop();

        // Verify the processor and the listener are correctly stopped
        await("The processor and the listener threads to properly stopped")
            .untilAsserted(() -> {
                assertFalse(processor.isRunning());

                assertNull(getThreadByName("attestation-processor-listener"));
                assertNull(getThreadByName("attestation-processor-main"));
                assertNull(getThreadByName("test-thread"));

                verify(executorService).shutdown();
                verify(queryExecutor).abort();
            });
    }

    private static Thread getThreadByName(String name) {
        return Thread.getAllStackTraces().keySet().stream()
            .filter(t -> Objects.equals(t.getName(), name))
            .findFirst()
            .orElse(null);
    }
}
