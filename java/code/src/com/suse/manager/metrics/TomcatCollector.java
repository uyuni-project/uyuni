package com.suse.manager.metrics;

import org.apache.log4j.Logger;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.management.ReflectionException;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;

public class TomcatCollector extends Collector {

    // Logger
    private static final Logger LOG = Logger.getLogger(TomcatCollector.class);

    private static String CATALINA_JMX_DOMAIN = "Catalina";
    private static String TOMCAT_PREFIX = "tomcat";

    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> out = new ArrayList<>();

        out.addAll(this.generateThreadPoolMetrics());

        return out;
    }

    private List<MetricFamilySamples> generateThreadPoolMetrics() {
        List<MetricFamilySamples> out = new ArrayList<MetricFamilySamples>();

        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            ObjectName filterName =
                    new ObjectName(CATALINA_JMX_DOMAIN + ":type=ThreadPool,name=*");
            Set<ObjectInstance> mBeans = server.queryMBeans(filterName, null);

            if (!mBeans.isEmpty()) {
                List<String> labels = Collections.singletonList("name");

                GaugeMetricFamily currentThreadsCountGauge = new GaugeMetricFamily(
                        TOMCAT_PREFIX + "_current_threads", "Current threads count", labels);
                GaugeMetricFamily maxThreadsCountGauge = new GaugeMetricFamily(
                        TOMCAT_PREFIX + "_max_threads", "Max number of threads", labels);
                GaugeMetricFamily activeThreadsCountGauge = new GaugeMetricFamily(
                        TOMCAT_PREFIX + "_active_threads", "Active threads count", labels);

                for (final ObjectInstance mBean : mBeans) {
                    List<String> labelValues = Collections
                            .singletonList(mBean.getObjectName().getKeyProperty("name"));

                    currentThreadsCountGauge.addMetric(labelValues, ((Integer) server
                            .getAttribute(mBean.getObjectName(), "currentThreadCount")).doubleValue());
                    maxThreadsCountGauge.addMetric(labelValues, ((Integer) server
                            .getAttribute(mBean.getObjectName(), "maxThreads")).doubleValue());
                    activeThreadsCountGauge.addMetric(labelValues, ((Integer) server
                            .getAttribute(mBean.getObjectName(), "currentThreadsBusy")).doubleValue());
                }
                out.add(currentThreadsCountGauge);
                out.add(maxThreadsCountGauge);
                out.add(activeThreadsCountGauge);
            }
        }
        catch (InstanceNotFoundException | AttributeNotFoundException |
                ReflectionException | MBeanException | MalformedObjectNameException e) {
            LOG.warn("Unable to register Tomcat Metrics", e);
        }
        return out;
    }
}
