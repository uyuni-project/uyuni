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
import org.apache.log4j.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import java.util.ArrayList;
import java.util.List;

import static com.suse.manager.metrics.CustomCollectorUtils.counterFor;
import static com.suse.manager.metrics.CustomCollectorUtils.gaugeFor;

/**
 * Collector for a Taskomatic Scheduler.
 */
public class SchedulerCollector extends Collector {

    private static final Logger LOG = Logger.getLogger(PrometheusExporter.class);

    private Scheduler scheduler;
    private String schedulerId;

    /**
     * Standard constructor.
     * @param schedulerIn a scheduler
     * @param schedulerIdIn a unique ID for the scheduler
     */
    public SchedulerCollector(Scheduler schedulerIn, String schedulerIdIn) {
        this.scheduler = schedulerIn;
        this.schedulerId = schedulerIdIn;
    }

    @Override
    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> out = new ArrayList<>();

        try {
            out.add(counterFor("scheduler_threads",
                    "Threads total count",
                    this.scheduler.getMetaData().getThreadPoolSize(),
                    this.schedulerId));
            out.add(gaugeFor("scheduler_threads_active",
                    "Active threads count",
                    this.scheduler.getCurrentlyExecutingJobs().size(),
                    this.schedulerId));
            out.add(counterFor("scheduler_completed_task_count",
                    "Number of tasks ever completed",
                    this.scheduler.getMetaData().getNumberOfJobsExecuted(),
                    this.schedulerId));
        }
        catch (SchedulerException e) {
            LOG.warn("Unable to collect scheduler info ", e);
        }
        return out;
    }
}
