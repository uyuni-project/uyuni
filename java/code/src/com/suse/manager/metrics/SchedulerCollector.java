package com.suse.manager.metrics;

import org.apache.log4j.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import java.util.ArrayList;
import java.util.List;
import io.prometheus.client.Collector;

public class SchedulerCollector extends Collector{

 // Logger
    private static final Logger LOG = Logger.getLogger(PrometheusExporter.class);

    private Scheduler scheduler;
    private String schedulerId;

    public SchedulerCollector(Scheduler scheduler, String schedulerId) {
        this.scheduler = scheduler;
        this.schedulerId = schedulerId;
    }

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> out = new ArrayList<>();

        //TODO: add metrics here
        try {
            out.add(CustomCollectorUtils.generateGaugeFor("thread_pool_size",
                    "Thread pool size", this.scheduler.getMetaData().getThreadPoolSize(),
                    this.schedulerId));
            out.add(CustomCollectorUtils.generateGaugeFor("executed_jobs_total_count",
                    "Executed jobs total count",
                    this.scheduler.getMetaData().getNumberOfJobsExecuted(),
                    this.schedulerId));
            out.add(CustomCollectorUtils.generateGaugeFor("currently_executing_jobs_count",
                    "Currently executing jobs count",
                    this.scheduler.getCurrentlyExecutingJobs().size(), this.schedulerId));
        }
        catch (SchedulerException e) {
            LOG.warn("Unable to collect scheduler info ", e);
        }
        return out;
    }

}
