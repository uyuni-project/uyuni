package com.suse.manager.metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import io.prometheus.client.Collector;

public class ThreadPoolCollector extends Collector{

    private ThreadPoolExecutor pool;
    private String poolId;

    public ThreadPoolCollector(ThreadPoolExecutor pool, String poolId) {
        this.pool = pool;
        this.poolId = poolId;
    }

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> out = new ArrayList<>();

        out.add(CustomCollectorUtils.generateGaugeFor("thread_pool_threads",
                "Threads total count", this.pool.getPoolSize(), this.poolId));
        out.add(CustomCollectorUtils.generateGaugeFor("thread_pool_threads_active",
                "Active threads count", this.pool.getActiveCount(), this.poolId));

        return out;
    }

}
