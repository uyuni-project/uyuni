package com.suse.manager.metrics;

import com.redhat.rhn.common.conf.ConfigDefaults;
import io.prometheus.client.exporter.HTTPServer;
import org.apache.log4j.Logger;
import org.quartz.Scheduler;

import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Copyright (c) 2016 SUSE LLC
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
public enum PrometheusExporter{
    INSTANCE;

    private static final boolean ENABLED = ConfigDefaults.get().isPrometheusMonitoringEnabled();

    // Logger
    private static final Logger LOG = Logger.getLogger(PrometheusExporter.class);

    // Listening port for non-Servlet based applications
    private static final int PORT = 2830;

    public void startHttpServer() {
        if (ENABLED){
            try {
                new HTTPServer(PORT);
            }
            catch (IOException e) {
                LOG.warn("Unable to register Prometheus HttpServer on port " + PORT, e);
            }
        }
    }

    public void registerThreadPool(ThreadPoolExecutor pool, String poolId) {
        if (ENABLED){
            new ThreadPoolCollector(pool, poolId).register();
        }
    }

    public void registerScheduler(Scheduler scheduler, String schedulerId) {
        if (ENABLED){
            new SchedulerCollector(scheduler, schedulerId).register();
        }
    }
}
