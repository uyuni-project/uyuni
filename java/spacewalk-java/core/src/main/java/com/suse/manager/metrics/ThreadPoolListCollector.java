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

import io.prometheus.client.Collector;
import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.GaugeMetricFamily;

/**
 * Collector for a List of ThreadPool.
 */
public class ThreadPoolListCollector extends Collector {

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
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> out = new ArrayList<>();

        GaugeMetricFamily poolThreads = new GaugeMetricFamily(poolId + "_" + "thread_pool_size",
                "Number of threads in the pool", List.of("queue"));
        IntStream.range(0, this.pool.size()).forEach(i -> poolThreads.addMetric(List.of(String.format("%d", i)),
                this.pool.get(i).getPoolSize()));
        out.add(poolThreads);

        GaugeMetricFamily activeThreads = new GaugeMetricFamily(poolId + "_" + "thread_pool_active_threads",
                "Number of active threads", List.of("queue"));
        IntStream.range(0, this.pool.size()).forEach(i -> activeThreads.addMetric(List.of(String.format("%d", i)),
                this.pool.get(i).getActiveCount()));
        out.add(activeThreads);

        CounterMetricFamily tasks = new CounterMetricFamily(poolId + "_" + "thread_pool_tasks_total",
                "Tasks count", List.of("queue"));
        IntStream.range(0, this.pool.size()).forEach(i -> tasks.addMetric(List.of(String.format("%d", i)),
                this.pool.get(i).getTaskCount()));
        out.add(tasks);

        CounterMetricFamily completed = new CounterMetricFamily(poolId + "_" + "thread_pool_completed_tasks_total",
                "Completed tasks count", List.of("queue"));
        IntStream.range(0, this.pool.size()).forEach(i -> completed.addMetric(List.of(String.format("%d", i)),
                this.pool.get(i).getCompletedTaskCount()));
        out.add(completed);

        return out;
    }
}
