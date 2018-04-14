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

import io.prometheus.client.CounterMetricFamily;
import io.prometheus.client.GaugeMetricFamily;

/**
 * Shared methods for Prometheus metrics.
 */
public class CustomCollectorUtils {

    private CustomCollectorUtils() { }

    /**
     * Returns a Gauge object.
     * @param metricName name of the metric
     * @param help help string
     * @param metricValue current value of the metric
     * @param metricPrefix prefix for the metric name
     * @return a Gauge object
     */
    public static GaugeMetricFamily gaugeFor(String metricName, String help, double metricValue, String metricPrefix) {
        return new GaugeMetricFamily(metricPrefix + "_" + metricName, metricPrefix + " - " + help, metricValue);
    }

    /**
     * Returns a Counter object.
     * @param metricName name of the metric
     * @param help help string
     * @param metricValue current value of the metric
     * @param metricPrefix prefix for the metric name
     * @return a Counter object
     */
    public static CounterMetricFamily counterFor(String metricName, String help, long metricValue,
                                                 String metricPrefix) {
        return new CounterMetricFamily(metricPrefix + "_" + metricName, metricPrefix + " - " + help, metricValue);
    }
}
