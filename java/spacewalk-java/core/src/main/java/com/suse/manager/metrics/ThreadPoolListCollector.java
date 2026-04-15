/*
 * Copyright (c) 2023 SUSE LLC
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
package com.suse.manager.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.IntStream;

import io.prometheus.metrics.model.registry.MultiCollector;
import io.prometheus.metrics.model.snapshots.CounterSnapshot;
import io.prometheus.metrics.model.snapshots.GaugeSnapshot;
import io.prometheus.metrics.model.snapshots.Labels;
import io.prometheus.metrics.model.snapshots.MetricSnapshot;
import io.prometheus.metrics.model.snapshots.MetricSnapshots;

/**
 * Collector for a List of ThreadPool.
 */
public class ThreadPoolListCollector implements MultiCollector {

    private final List<ThreadPoolExecutor> pool;
    private final String poolId;

    /**
     * Standard constructor.
     * @param poolIn a thread pool
     * @param poolIdIn a unique ID for the pool
     */
    public ThreadPoolListCollector(List<ThreadPoolExecutor> poolIn, String poolIdIn) {
        this.pool = poolIn;
        this.poolId = poolIdIn;
    }

    @Override
    public MetricSnapshots collect() {
        List<MetricSnapshot> out = new ArrayList<>();

        GaugeSnapshot.Builder poolThreadsBuilder = GaugeSnapshot.builder()
                .name(poolId + "_" + "thread_pool_size")
                .help("Number of threads in the pool");
        IntStream.range(0, this.pool.size()).forEach(i -> poolThreadsBuilder.dataPoint(
                GaugeSnapshot.GaugeDataPointSnapshot.builder()
                        .labels(Labels.of("queue", String.format("%d", i)))
                        .value(this.pool.get(i).getPoolSize())
                        .build()));
        out.add(poolThreadsBuilder.build());

        GaugeSnapshot.Builder activeThreadsBuilder = GaugeSnapshot.builder()
                .name(poolId + "_" + "thread_pool_active_threads")
                .help("Number of active threads");
        IntStream.range(0, this.pool.size()).forEach(i -> activeThreadsBuilder.dataPoint(
                GaugeSnapshot.GaugeDataPointSnapshot.builder()
                        .labels(Labels.of("queue", String.format("%d", i)))
                        .value(this.pool.get(i).getActiveCount())
                        .build()));
        out.add(activeThreadsBuilder.build());

        CounterSnapshot.Builder tasksBuilder = CounterSnapshot.builder()
                .name(poolId + "_" + "thread_pool_tasks")
                .help("Tasks count");
        IntStream.range(0, this.pool.size()).forEach(i -> tasksBuilder.dataPoint(
                CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(Labels.of("queue", String.format("%d", i)))
                        .value(this.pool.get(i).getTaskCount())
                        .build()));
        out.add(tasksBuilder.build());

        CounterSnapshot.Builder completedBuilder = CounterSnapshot.builder()
                .name(poolId + "_" + "thread_pool_completed_tasks")
                .help("Completed tasks count");
        IntStream.range(0, this.pool.size()).forEach(i -> completedBuilder.dataPoint(
                CounterSnapshot.CounterDataPointSnapshot.builder()
                        .labels(Labels.of("queue", String.format("%d", i)))
                        .value(this.pool.get(i).getCompletedTaskCount())
                        .build()));
        out.add(completedBuilder.build());

        return new MetricSnapshots(out);
    }
}
