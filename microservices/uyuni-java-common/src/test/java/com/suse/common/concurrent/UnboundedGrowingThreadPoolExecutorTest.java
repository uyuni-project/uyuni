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

package com.suse.common.concurrent;

import static org.awaitility.Awaitility.await;
import static org.awaitility.Awaitility.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.AssertionsKt.fail;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

class UnboundedGrowingThreadPoolExecutorTest {

    private static final String THREAD_PREFIX = "unit-test";

    private static final Duration KEEP_THREAD_ALIVE = Duration.ofMillis(250);

    private ExecutorService executorService;

    @BeforeEach
    public void setup() {
        Awaitility.setDefaultPollDelay(0, TimeUnit.SECONDS);
        Awaitility.setDefaultTimeout(5, TimeUnit.SECONDS);
    }

    @AfterEach
    public void tearDown() {
        if (executorService == null) {
            return;
        }

        try {
            executorService.shutdownNow();
            assertTrue(executorService.awaitTermination(5, TimeUnit.SECONDS), "Unable to terminate executor");
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            fail("Unexpected interruption while terminating executor", ex);
        }

        Awaitility.reset();
    }

    @Test
    @DisplayName("Creates and start the correct number of core threads")
    void canCreateAndStartCoreThreads() {
        executorService = new UnboundedGrowingThreadPoolExecutor(2, 10, KEEP_THREAD_ALIVE, THREAD_PREFIX);
        // Verify core threads are created
        assertEquals(2, getUnitThreads().size());
    }

    @Test
    @DisplayName("Allows zero core threads")
    void canStartWithoutCoreThreads() {
        executorService = new UnboundedGrowingThreadPoolExecutor(0, 10, KEEP_THREAD_ALIVE, THREAD_PREFIX);
        // Verify no core threads are created
        assertEquals(0, getUnitThreads().size());
    }

    @Test
    @DisplayName("Can increase number of threads when tasks are added and close them when done")
    void canIncreaseNumberOfThreads() {
        executorService = new UnboundedGrowingThreadPoolExecutor(5, 10, KEEP_THREAD_ALIVE, THREAD_PREFIX);

        final CountDownLatch completeTaskLatch = new CountDownLatch(1);

        // Verify core threads are created
        assertEquals(5, getUnitThreads().size());
        // start 3 tasks
        IntStream.range(0, 3).forEach(idx -> executorService.submit(new DummyTask(completeTaskLatch)));
        // Threads should remain the same
        assertEquals(5, getUnitThreads().size());

        // start 5 more tasks
        IntStream.range(0, 5).forEach(idx -> executorService.submit(new DummyTask(completeTaskLatch)));
        // Threads should have increased
        assertEquals(8, getUnitThreads().size());

        // Complete the tasks and wait their full completion
        completeTaskLatch.countDown();

        given().pollDelay(KEEP_THREAD_ALIVE)
            .await("Non-core threads to expire")
            .untilAsserted(() -> assertEquals(5, getUnitThreads().size()));

        await("Core threads do not expire")
            .during(KEEP_THREAD_ALIVE.multipliedBy(2))
            .untilAsserted(() -> assertEquals(5, getUnitThreads().size()));
    }

    @Test
    @DisplayName("Does not create more threads than the specified max pool size")
    void cannotCreateMoreThreadThanGivenMaxPoolSize() {
        executorService = new UnboundedGrowingThreadPoolExecutor(1, 5, KEEP_THREAD_ALIVE, THREAD_PREFIX);

        final CountDownLatch phaseOneLatch = new CountDownLatch(1);
        final CountDownLatch phaseTwoLatch = new CountDownLatch(1);

        List<DummyTask> taskList = new LinkedList<>();

        // Verify core threads are created
        assertEquals(1, getUnitThreads().size());
        // start 3 tasks
        IntStream.range(0, 3).forEach(idx -> {
            DummyTask task = new DummyTask(phaseOneLatch);
            taskList.add(task);
            executorService.submit(task);
        });

        assertEquals(3, getUnitThreads().size(), "Threads should increase");
        await("All threads are running")
            .untilAsserted(() -> {
                assertTrue(taskList.stream().allMatch(task -> task.getStatus() == TaskStatus.RUNNING));
            });

        // start 2 more tasks
        IntStream.range(0, 2).forEach(idx -> {
            DummyTask task = new DummyTask(phaseOneLatch);
            taskList.add(task);
            executorService.submit(task);
        });

        assertEquals(5, getUnitThreads().size(), "Threads should reach max pool size");
        await("Threads reach max pool size")
            .untilAsserted(() -> {
                assertTrue(taskList.stream().allMatch(task -> task.getStatus() == TaskStatus.RUNNING));
            });

        // start 3 more tasks
        IntStream.range(0, 3).forEach(idx -> {
            DummyTask task = new DummyTask(phaseTwoLatch);
            taskList.add(task);
            executorService.submit(task);
        });

        assertEquals(5, getUnitThreads().size(), "Threads should not exceed max pool size");
        await("5 tasks should be started, 3 should be waiting")
            .untilAsserted(() -> {
                assertEquals(5, taskList.stream().filter(task -> task.getStatus() == TaskStatus.RUNNING).count());
                assertEquals(3, taskList.stream().filter(task -> task.getStatus() == TaskStatus.WAITING).count());
            });

        // Complete phase one and additional tasks should be picked up
        phaseOneLatch.countDown();

        // We should still have a full pool of threads
        assertEquals(5, getUnitThreads().size(), "Pool of thread is still full");

        await("5 tasks should be completed, 3 should be started")
            .untilAsserted(() -> {
                    assertEquals(5, taskList.stream().filter(task -> task.getStatus() == TaskStatus.COMPLETED).count());
                    assertEquals(3, taskList.stream().filter(task -> task.getStatus() == TaskStatus.RUNNING).count());
                });

        given().pollDelay(KEEP_THREAD_ALIVE)
            .await("2 unused threads should be expired")
            .untilAsserted(() -> assertEquals(3, getUnitThreads().size()));

        // Complete the remaining tasks
        phaseTwoLatch.countDown();

        // We should still have 3 threads
        assertEquals(3, getUnitThreads().size());
        await("All tasks should be completed")
            .untilAsserted(() -> {
                assertEquals(8, taskList.stream().filter(task -> task.getStatus() == TaskStatus.COMPLETED).count());
            });

        given().pollDelay(KEEP_THREAD_ALIVE)
            .await("Only one core thread should still be available")
            .untilAsserted(() -> assertEquals(1, getUnitThreads().size()));
    }

    private List<Thread> getUnitThreads() {
        return Thread.getAllStackTraces().keySet().stream()
            .filter(t -> t.getName().startsWith(THREAD_PREFIX))
            .collect(Collectors.toList());
    }

    private enum TaskStatus {
        WAITING, RUNNING, COMPLETED
    }

    private static class DummyTask implements Runnable {

        private TaskStatus status;

        private final CountDownLatch terminateLatch;

        DummyTask(CountDownLatch terminateLatchIn) {
            this.terminateLatch = terminateLatchIn;
            this.setStatus(TaskStatus.WAITING);
        }

        @Override
        public void run() {
            try {
                setStatus(TaskStatus.RUNNING);
                terminateLatch.await();
            }
            catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                fail("Unexpected interruption", ex);
            }

            setStatus(TaskStatus.COMPLETED);
        }

        private synchronized void setStatus(TaskStatus taskStatus) {
            status = taskStatus;
        }

        public synchronized TaskStatus getStatus() {
            return status;
        }
    }
}
