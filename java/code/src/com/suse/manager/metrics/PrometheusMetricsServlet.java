package com.suse.manager.metrics;

import javax.servlet.ServletException;

import io.prometheus.client.exporter.MetricsServlet;

public class PrometheusMetricsServlet extends MetricsServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void init() throws ServletException {
        super.init();
        PrometheusExporter.INSTANCE.registerTomcatCollector();
    }

}
