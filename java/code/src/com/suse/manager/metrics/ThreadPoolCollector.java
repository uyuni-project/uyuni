/**
 * Copyright (c) 2018 SUSE LLC
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

import io.prometheus.client.Collector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Collector for a ThreadPool.
 */
public class ThreadPoolCollector extends Collector {

    private ThreadPoolExecutor pool;
    private String poolId;

    /**
     * Standard constructor.
     * @param poolIn a thread pool
     * @param poolIdIn a unique ID for the pool
     */
    public ThreadPoolCollector(ThreadPoolExecutor poolIn, String poolIdIn) {
        this.pool = poolIn;
        this.poolId = poolIdIn;
    }

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> out = new ArrayList<>();

        out.add(CustomCollectorUtils.gaugeFor("thread_pool_threads",
                "Threads total count", this.pool.getPoolSize(), this.poolId));
        out.add(CustomCollectorUtils.gaugeFor("thread_pool_threads_active",
                "Active threads count", this.pool.getActiveCount(), this.poolId));
        out.add(CustomCollectorUtils.counterFor("thread_pool_task_count",
                "Number of tasks ever submitted", this.pool.getTaskCount(), this.poolId));
        out.add(CustomCollectorUtils.counterFor("thread_pool_completed_task_count",
                "Number of tasks ever completed", this.pool.getCompletedTaskCount(), this.poolId));

        return out;
    }
}
