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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.coco.attestation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.suse.coco.module.AttestationModuleLoader;

import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opentest4j.AssertionFailedError;
import org.postgresql.PGConnection;
import org.postgresql.PGNotification;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ExecutorService;

@ExtendWith(MockitoExtension.class)
@Timeout(30)
class AttestationQueueProcessorTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SqlSessionFactory sessionFactory;

    @Mock
    private AttestationResultService resultService;

    @Mock
    private ExecutorService executorService;

    @Mock
    private AttestationModuleLoader moduleLoader;

    @Mock
    private Connection connection;

    @Mock
    private PGConnection pgConnection;

    @Mock
    private Statement statement;

    private AttestationQueueProcessor processor;

    @BeforeEach
    public void setUp() throws SQLException {
        processor = new AttestationQueueProcessor(sessionFactory, resultService, executorService, moduleLoader, 10);

        // Basic mocking
        when(sessionFactory.getConfiguration().getEnvironment().getDataSource().getConnection())
            .thenReturn(connection);

        when(connection.isWrapperFor(PGConnection.class))
            .thenReturn(true);

        when(connection.unwrap(argThat(clazz -> clazz.getName().equals(PGConnection.class.getName()))))
            .thenReturn(pgConnection);

        when(connection.createStatement())
            .thenReturn(statement);

        when(pgConnection.getNotifications(anyInt()))
            .thenReturn(new PGNotification[0]);
    }

    @Test
    @DisplayName("An interruption in the listener process terminate the processor")
    void testInterruptingListenerShouldTerminateProcessor() throws Exception {
        // Simulate the executor shutdown
        when(executorService.isShutdown()).thenReturn(false);
        when(executorService.isTerminated()).thenReturn(false);
        when(executorService.awaitTermination(anyLong(), any())).thenReturn(true);

        runInTestThread(() -> processor.run());

        awaitAsserted(() -> {
            // Verify the listener thread has been created
            assertNotNull(getThreadByName("attestation-processor-listener"));
            // And it's actually waiting for notifications
            verify(pgConnection).getNotifications(anyInt());
        });

        // Interrupt the listener
        Thread processorListener = getThreadByName("attestation-processor-listener");
        processorListener.interrupt();

        // Verify the processor and the listener are correctly stopped
        awaitAsserted(() -> {
            assertNull(getThreadByName("attestation-processor-listener"));
            assertNull(getThreadByName("test-thread"));

            verify(executorService).shutdown();
        });
    }

    private static void runInTestThread(Runnable runnable) {
        new Thread(runnable, "test-thread").start();
    }

    private static Thread getThreadByName(String name) {
        return Thread.getAllStackTraces().keySet().stream()
            .filter(t -> Objects.equals(t.getName(), name))
            .findFirst()
            .orElse(null);
    }

    private static void awaitAsserted(Executable assertions) {
        awaitAsserted(assertions, Duration.ofSeconds(5), Duration.ofMillis(100));
    }

    private static void awaitAsserted(Executable assertions, Duration timeout, Duration pollInterval) {
        Instant startInstant = Instant.now();
        Instant expirationInstant = startInstant.plusSeconds(timeout.toSeconds());

        while (Instant.now().isBefore(expirationInstant)) {
            try {
                assertions.execute();
                return;
            }
            catch (AssertionError error) {
                try {
                    // Assertions are not satisfied. Wait a bit and retry
                    Thread.sleep(pollInterval.toMillis());
                }
                catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
            catch (Throwable th) {
                throw new AssertionFailedError("Unhandled error during assertions execution", th);
            }
        }
    }
}
