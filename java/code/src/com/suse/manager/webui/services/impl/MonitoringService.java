/*
 * Copyright (c) 2019 SUSE LLC
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

package com.suse.manager.webui.services.impl;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.product.Tuple2;
import com.redhat.rhn.taskomatic.TaskoXmlRpcHandler;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.salt.netapi.results.ModuleRun;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Service to manage server monitoring.
 */
public class MonitoringService {

    private static final Logger LOG = LogManager.getLogger(MonitoringService.class);

    private static final Gson GSON = new GsonBuilder()
            .serializeNulls()
            .create();
    public static final String MGR_MONITORING_CTL = "/usr/sbin/mgr-monitoring-ctl";

    private MonitoringService() { }

    private static class LocalResponse<T> {

        private T local;

        /**
         * @return local to get
         */
        public T getLocal() {
            return local;
        }
    }

    /**
     * Status of monitoring services.
     */
    public static class MonitoringStatus {

        private Map<String, Boolean> exporters = new HashMap<>();
        private Map<String, String> messages = new HashMap<>();

        /**
         * @return exporters to get
         */
        public Map<String, Boolean> getExporters() {
            return exporters;
        }

        /**
         * @param exportersIn to set
         */
        public void setExporters(Map<String, Boolean> exportersIn) {
            this.exporters = exportersIn;
        }

        /**
         * @return config to get
         */
        public Map<String, String> getMessages() {
            return messages;
        }

        /**
         * @param messagesIn to set
         */
        public void setMessages(Map<String, String> messagesIn) {
            this.messages = messagesIn;
        }
    }

    private static BiFunction<String, Optional<String>, Optional<InputStream>> execCtl =
            (String cmd, Optional<String> pillar) -> {
        try {
            Process process = new ProcessBuilder().command("/usr/bin/sudo",
                    MGR_MONITORING_CTL, cmd, pillar.orElse(""))
                    .start();
            boolean exited = process.waitFor(5, TimeUnit.MINUTES);
            if (!exited) {
                LOG.error("Timeout waiting for " + MGR_MONITORING_CTL + " to complete");
                return Optional.empty();
            }
            if (process.exitValue() != 0) {
                LOG.error("{} returned non zero exit code: {}", process.info().commandLine(), process.exitValue());
                try (InputStream stderrIn = process.getErrorStream(); InputStream stdoutIn = process.getInputStream()) {
                    LOG.error("stderr:\n {}", IOUtils.toString(stderrIn));
                    LOG.error("stdout:\n{}", IOUtils.toString(stdoutIn));
                }
                return Optional.empty();
            }
            return Optional.of(process.getInputStream());
        }
        catch (IOException | InterruptedException e) {
            LOG.error("Error executing " + MGR_MONITORING_CTL, e);
            return Optional.empty();
        }
    };

    private static Supplier<Boolean> tomcatJmxStatusSupplier = TaskoXmlRpcHandler::isJmxEnabled;

    private static Supplier<Boolean> taskomaticJmxStatusSupplier = () -> {
        TaskomaticApi taskomatic = new TaskomaticApi();
        try {
            return taskomatic.isJmxEnabled();
        }
        catch (TaskomaticApiException e) {
            LOG.error("Error getting Taskomatic JMX status", e);
            throw new RuntimeException(e.getMessage(), e);
        }
    };

    private static Supplier<Boolean> selfMonitoringStatusSupplier =
            () -> ConfigDefaults.get().isPrometheusMonitoringEnabled();

    /**
     * Used only for unit tests
     * @param execCtlIn to set
     */
    public static void setExecCtlFunction(BiFunction<String, Optional<String>, Optional<InputStream>> execCtlIn) {
        MonitoringService.execCtl = execCtlIn;
    }

    /**
     * Used only for unit tests
     * @param tomcatJmxEnabledIn to set
     */
    public static void setTomcatJmxStatusSupplier(Supplier<Boolean> tomcatJmxEnabledIn) {
        MonitoringService.tomcatJmxStatusSupplier = tomcatJmxEnabledIn;
    }

    /**
     * Used only for unit tests
     * @param taskomaticJmxEnabledIn to set
     */
    public static void setTaskomaticJmxStatusSupplier(Supplier<Boolean> taskomaticJmxEnabledIn) {
        MonitoringService.taskomaticJmxStatusSupplier = taskomaticJmxEnabledIn;
    }

    /**
     * Used only for unit tests
     * @param selfMonitoringStatusSupplierIn to set
     */
    public static void setSelfMonitoringStatusSupplier(Supplier<Boolean> selfMonitoringStatusSupplierIn) {
        MonitoringService.selfMonitoringStatusSupplier = selfMonitoringStatusSupplierIn;
    }

    /**
     * Check if at least one exporter is running
     *
     * @return true if at least one exporter is running, false otherwise
     */
    public static boolean isMonitoringEnabled() {
        return getStatus()
                .map(status -> status.getExporters().values().contains(true))
                .orElse(false);
    }

    /**
     * Get the status of Prometheus exporters.
     * @return a {@link Map} with the status of each Prometheus exporter
     */
    public static Optional<MonitoringStatus> getStatus() {
        Optional<Map<String, Boolean>> res = invokeMonitoringCtl("status", Optional.empty(),
                new Tuple2<>("node",
                        "mgrcompat_|-node_exporter_service_|-service.status_|-module_run"),
                new Tuple2<>("postgres",
                        "mgrcompat_|-postgres_exporter_service_|-service.status_|-module_run"),
                new Tuple2<>("tomcat",
                        "mgrcompat_|-jmx_tomcat_java_config_|-file.search_|-module_run"),
                new Tuple2<>("taskomatic",
                        "mgrcompat_|-jmx_taskomatic_java_config_|-file.search_|-module_run"),
                new Tuple2<>("self_monitoring",
                        "cmd_|-mgr_is_prometheus_self_monitoring_enabled_|-grep*")
                );
        return res.map(map -> {
            MonitoringStatus status = new MonitoringStatus();
            status.getExporters().put("node", map.get("node"));
            status.getExporters().put("postgres", map.get("postgres"));
            status.getExporters().put("tomcat", map.get("tomcat"));
            status.getExporters().put("taskomatic", map.get("taskomatic"));
            status.getExporters().put("self_monitoring", map.get("self_monitoring"));

            getTomcatMessage(map.get("tomcat"))
                    .ifPresent(msg -> status.getMessages().put("tomcat", msg));
            getTaskomaticMessage(map.get("taskomatic"))
                    .ifPresent(msg -> status.getMessages().put("taskomatic", msg));
            getSelfMonitoringMessage(map.get("self_monitoring"))
                    .ifPresent(msg -> status.getMessages().put("self_monitoring", msg));

            return status;
        });
    }

    private static Optional<String> getSelfMonitoringMessage(boolean selfMonitoringConfigEnabled) {
        if (selfMonitoringConfigEnabled != getSelfMonitoringStatus())  {
            return Optional.of("restart");
        }
        return Optional.empty();
    }

    private static Optional<String> getTomcatMessage(boolean configPresent) {
        return computeMessage(configPresent, getTomcatRuntimeJmxStatus());
    }

    private static Optional<String> getTaskomaticMessage(boolean configPresent) {
        return computeMessage(configPresent, getTaskomaticRuntimeJmxStatus());
    }

    private static Optional<String> computeMessage(boolean configPresent,
                                                   boolean runtimeJmxEnabled) {
        if (configPresent != runtimeJmxEnabled) {
            return Optional.of("restart");
        }
        return Optional.empty();
    }

    private static boolean getTaskomaticRuntimeJmxStatus() {
        return taskomaticJmxStatusSupplier.get();
    }

    private static boolean getTomcatRuntimeJmxStatus() {
        return tomcatJmxStatusSupplier.get();
    }

    private static boolean getSelfMonitoringStatus() {
        return selfMonitoringStatusSupplier.get();
    }

    /**
     * Enable monitoring exporters.
     * @return a {@link Map} with the status of each exporter (true - running, false - stopped)
     * or an empty optional in case of error
     */
    public static Optional<MonitoringStatus> enableMonitoring() {
        String dbUser = Config.get().getString(ConfigDefaults.DB_USER);
        String dbPass = Config.get().getString(ConfigDefaults.DB_PASSWORD);
        String dbHost = Config.get().getString(ConfigDefaults.DB_HOST);
        String dbPort = Config.get().getString(ConfigDefaults.DB_PORT);
        String dbName = Config.get().getString(ConfigDefaults.DB_NAME);
        Map<String, String> pillar = new HashMap<>();
        pillar.put("db_user", dbUser);
        pillar.put("db_pass", dbPass);
        pillar.put("db_host", dbHost);
        pillar.put("db_port", dbPort);
        pillar.put("db_name", dbName);
        String pillarJson = GSON.toJson(pillar);
        // started successfully (true) -> service state (true - running)
        Optional<Map<String, Boolean>> res =  invokeMonitoringCtl("enable", Optional.of(pillarJson),
                new Tuple2<>("node",
                        "service_|-node_exporter_service_|-prometheus-node_exporter_|-running"),
                new Tuple2<>("postgres",
                        "service_|-postgres_exporter_service_|-prometheus-postgres_exporter_|-running"),
                new Tuple2<>("tomcat",
                        "file_|-jmx_tomcat_config_|-/usr/lib/systemd/system/tomcat.service.d/jmx.conf_|-managed"),
                new Tuple2<>("taskomatic",
                        "file_|-jmx_taskomatic_config_|-/usr/lib/systemd/system/taskomatic.service.d/jmx.conf_|" +
                        "-managed"),
                new Tuple2<>("self_monitoring",
                        "cmd_|-mgr_is_prometheus_self_monitoring_enabled_|-grep*")
        );

        return res.map(map -> {
            MonitoringStatus status = new MonitoringStatus();
            status.getExporters().put("node", map.get("node"));
            status.getExporters().put("postgres", map.get("postgres"));
            status.getExporters().put("tomcat", map.get("tomcat"));
            status.getExporters().put("taskomatic", map.get("taskomatic"));
            status.getExporters().put("self_monitoring", map.get("self_monitoring"));

            getTomcatMessage(map.get("tomcat"))
                    .ifPresent(msg -> status.getMessages().put("tomcat", msg));
            getTaskomaticMessage(map.get("taskomatic"))
                    .ifPresent(msg -> status.getMessages().put("taskomatic", msg));
            getSelfMonitoringMessage(map.get("self_monitoring"))
                    .ifPresent(msg -> status.getMessages().put("self_monitoring", msg));

            return status;
        });
    }

    /**
     * Disable monitoring exporters.
     * @return a {@link Map} with the status of each exporter (true - running, false - stopped)
     * or an empty optional in case of error
     */
    public static Optional<MonitoringStatus> disableMonitoring() {
        return invokeMonitoringCtl("disable", Optional.empty(),
                new Tuple2<>("node",
                        "service_|-node_exporter_service_|-prometheus-node_exporter_|-dead"),
                new Tuple2<>("postgres",
                        "service_|-postgres_exporter_service_|-prometheus-postgres_exporter_|-dead"),
                new Tuple2<>("tomcat",
                        "file_|-jmx_tomcat_config_|-/usr/lib/systemd/system/tomcat.service.d/jmx.conf_|-absent"),
                new Tuple2<>("taskomatic",
                        "file_|-jmx_taskomatic_config_|-/usr/lib/systemd/system/taskomatic.service.d/jmx.conf_|" +
                        "-absent"),
                new Tuple2<>("self_monitoring",
                        "cmd_|-mgr_is_prometheus_self_monitoring_disabled_|-grep*")
                ).map(map -> {
                    // disabled successfully (true) -> service state (false - not running)
                    MonitoringStatus status = new MonitoringStatus();
                    status.getExporters().put("node", !map.get("node"));
                    status.getExporters().put("postgres", !map.get("postgres"));
                    status.getExporters().put("tomcat", !map.get("tomcat"));
                    status.getExporters().put("taskomatic", !map.get("taskomatic"));
                    status.getExporters().put("self_monitoring", !map.get("self_monitoring"));

                    getTomcatMessage(!map.get("tomcat"))
                            .ifPresent(msg -> status.getMessages().put("tomcat", msg));
                    getTaskomaticMessage(!map.get("taskomatic"))
                            .ifPresent(msg -> status.getMessages().put("taskomatic", msg));
                    getSelfMonitoringMessage(!map.get("self_monitoring"))
                            .ifPresent(msg -> status.getMessages().put("self_monitoring", msg));

                    return status;
                });
    }

    private static Optional<Map<String, Boolean>> invokeMonitoringCtl(String cmd, Optional<String> pillar,
                                                                      Tuple2<String, String>... exporterStates) {
        try {
            Map<String, Boolean> exporters = new HashMap<>();
            Optional<InputStream> ctlOutput = execCtl.apply(cmd, pillar);
            if (ctlOutput.isPresent()) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(ctlOutput.get()))) {
                    String cmdOutput = IOUtils.toString(reader);
                    LOG.debug(MGR_MONITORING_CTL + " output: {}", cmdOutput);

                    LocalResponse<Map<String, ModuleRun<JsonElement>>> jsonOut = GSON.fromJson(cmdOutput,
                            new TypeToken<LocalResponse<Map<String, ModuleRun<JsonElement>>>>() {
                            }.getType());

                    for (Tuple2<String, String> state : exporterStates) {
                        exporters.put(state.getA(),
                                isResultTrue(jsonOut.getLocal(), state.getB()));
                    }

                    return Optional.of(exporters);
                }
                catch (JsonParseException e) {
                    LOG.error("Error parsing JSON: {}", e.getMessage());
                    return Optional.empty();
                }
            }
            else {
                LOG.error("Got empty output from " + MGR_MONITORING_CTL);
                return Optional.empty();
            }
        }
        catch (IOException e) {
            LOG.error("Error invoking " + MGR_MONITORING_CTL, e);
            return Optional.empty();
        }
    }

    private static boolean isResultTrue(Map<String, ModuleRun<JsonElement>> result, String key) {
        if (key.endsWith("*")) {
            return result.entrySet().stream()
                    .filter(e -> e.getKey().startsWith(key.substring(0, key.length() - 1)))
                    .map(e -> e.getValue().isResult())
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException(key + " not found in salt result"));
        }
        return Optional.ofNullable(result.get(key))
                .map(ModuleRun::isResult)
                .orElseThrow(() -> new RuntimeException(key + " not found in salt result"));
    }

}
