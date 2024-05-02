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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.AssertionsKt.fail;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Disabled;
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

@Disabled("These tests are flaky and fails when executed by GitHub actions")
class UnboundedGrowingThreadPoolExecutorTest {

    public static final String THREAD_PREFIX = "unit-test";

    private ExecutorService executorService;

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
    }

    @Test
    @DisplayName("Creates and start the correct number of core threads")
    void canCreateAndStartCoreThreads() {
        executorService = new UnboundedGrowingThreadPoolExecutor(2, 10, Duration.ofSeconds(1), THREAD_PREFIX);
        // Verify core threads are created
        assertEquals(2, getUnitThreads().size());
    }

    @Test
    @DisplayName("Allows zero core threads")
    void canStartWithoutCoreThreads() {
        executorService = new UnboundedGrowingThreadPoolExecutor(0, 10, Duration.ofSeconds(1), THREAD_PREFIX);
        // Verify no core threads are created
        assertEquals(0, getUnitThreads().size());
    }

    @Test
    @DisplayName("Can increase number of threads when tasks are added and close them when done")
    void canIncreaseNumberOfThreads() {
        executorService = new UnboundedGrowingThreadPoolExecutor(5, 10, Duration.ofMillis(500), THREAD_PREFIX);

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

        // Complete the tasks
        completeTaskLatch.countDown();
        // Thread should still be the same number
        assertEquals(8, getUnitThreads().size());

        sleep(Duration.ofSeconds(1));

        // Non-core threads should be expired
        assertEquals(5, getUnitThreads().size());

        sleep(Duration.ofSeconds(1));
        // Non-core threads should not expire
        assertEquals(5, getUnitThreads().size());
    }

    @Test
    @DisplayName("Does not create more threads than the specified max pool size")
    void cannotCreateMoreThreadThanGivenMaxPoolSize() {
        executorService = new UnboundedGrowingThreadPoolExecutor(1, 5, Duration.ofMillis(500), THREAD_PREFIX);

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
        // Threads should increase
        assertEquals(3, getUnitThreads().size());
        // All tasks should be started
        assertTrue(taskList.stream().allMatch(task -> task.getStatus() == TaskStatus.RUNNING));

        // start 2 more tasks
        IntStream.range(0, 2).forEach(idx -> {
            DummyTask task = new DummyTask(phaseOneLatch);
            taskList.add(task);
            executorService.submit(task);
        });
        // Threads reach max pool size
        assertEquals(5, getUnitThreads().size());
        // All tasks should be started
        assertTrue(taskList.stream().allMatch(task -> task.getStatus() == TaskStatus.RUNNING));

        // start 3 more tasks
        IntStream.range(0, 3).forEach(idx -> {
            DummyTask task = new DummyTask(phaseTwoLatch);
            taskList.add(task);
            executorService.submit(task);
        });
        // Threads should not exceed max pool size
        assertEquals(5, getUnitThreads().size());
        // 5 tasks should be started, 3 should not
        assertEquals(5, taskList.stream().filter(task -> task.getStatus() == TaskStatus.RUNNING).count());
        assertEquals(3, taskList.stream().filter(task -> task.getStatus() == TaskStatus.WAITING).count());

        // Complete phase one and additional tasks should be picked up
        phaseOneLatch.countDown();
        // We should still have a full pool of threads
        assertEquals(5, getUnitThreads().size());
        // 5 tasks should be completed, 3 should be started
        assertEquals(5, taskList.stream().filter(task -> task.getStatus() == TaskStatus.COMPLETED).count());
        assertEquals(3, taskList.stream().filter(task -> task.getStatus() == TaskStatus.RUNNING).count());

        // Wait a bit
        sleep(Duration.ofSeconds(1));
        // Two unused threads should be expired
        assertEquals(3, getUnitThreads().size());

        // Complete the remaining tasks
        phaseTwoLatch.countDown();
        // We should still have 3 threads
        assertEquals(3, getUnitThreads().size());
        // All tasks should be completed
        assertEquals(8, taskList.stream().filter(task -> task.getStatus() == TaskStatus.COMPLETED).count());

        // Wait a bit
        sleep(Duration.ofSeconds(1));
        // Only one core thread should still be available
        assertEquals(1, getUnitThreads().size());
    }

    private List<Thread> getUnitThreads() {
        return Thread.getAllStackTraces().keySet().stream()
            .filter(t -> t.getName().startsWith(THREAD_PREFIX))
            .collect(Collectors.toList());
    }

    private synchronized void sleep(Duration duration) {
        // Wait for the keep alive period to expire
        try {
            Thread.sleep(duration.toMillis());
        }
        catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            fail("Unexpected interruption", ex);
        }
    }

    private enum TaskStatus {
        WAITING, RUNNING, COMPLETED
    }

    private static class DummyTask implements Runnable {

        private TaskStatus status;

        private final CountDownLatch terminateLatch;

        DummyTask(CountDownLatch terminateLatchIn) {
            this.terminateLatch = terminateLatchIn;
            this.status = TaskStatus.WAITING;
        }

        @Override
        public void run() {
            try {
                status = TaskStatus.RUNNING;
                terminateLatch.await();
            }
            catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                fail("Unexpected interruption", ex);
            }

            status = TaskStatus.COMPLETED;
        }

        public TaskStatus getStatus() {
            return status;
        }
    }
}
