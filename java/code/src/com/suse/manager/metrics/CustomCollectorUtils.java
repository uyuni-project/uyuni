package com.suse.manager.metrics;

import io.prometheus.client.GaugeMetricFamily;

public class CustomCollectorUtils{

    protected static GaugeMetricFamily generateGaugeFor(String metricName, String helpStr, double metricValue, String metricPrefix) {
        return new GaugeMetricFamily(metricPrefix + "_" + metricName, metricPrefix + " - " + helpStr,
                metricValue);
    }

}
