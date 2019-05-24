/**
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.domain.product.Tuple2;
import com.suse.salt.netapi.results.ModuleRun;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;

/**
 * Service to manage server monitoring.
 */
public class MonitoringService {

    private static final Logger LOG = Logger.getLogger(MonitoringService.class);

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
                LOG.error(process.info().commandLine() + " returned non zero exit code: " + process.exitValue());
                try (InputStream stderrIn = process.getErrorStream(); InputStream stdoutIn = process.getInputStream()) {
                    LOG.error("stderr:\n " + IOUtils.toString(stderrIn));
                    LOG.error("stdout:\n" + IOUtils.toString(stdoutIn));
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

    public static void setExecCtlFunction(BiFunction<String, Optional<String>, Optional<InputStream>> execCtlIn) {
        MonitoringService.execCtl = execCtlIn;
    }

    /**
     * Check if at least one exporter is running
     *
     * @return true if at least one exporter is running, false otherwise
     */
    public static boolean isMonitoringEnabled() {
        return getStatus()
                .map(status -> status.values().contains(true))
                .orElse(false);
    }

    /**
     * Get the status of Prometheus exporters.
     * @return a {@link Map} with the status of each Prometheus exporter
     */
    public static Optional<Map<String, Boolean>> getStatus() {
        return invokeMonitoringCtl("status", Optional.empty(),
                new Tuple2<>("node",
                        "module_|-node_exporter_service_|-service.status_|-run"),
                new Tuple2<>("postgres",
                        "module_|-postgres_exporter_service_|-service.status_|-run"),
                new Tuple2<>("tomcat",
                        "module_|-jmx_tomcat_exporter_service_|-service.status_|-run"),
                new Tuple2<>("taskomatic",
                        "module_|-jmx_taskomatic_exporter_service_|-service.status_|-run")
                );
    }

    /**
     * Enable monitoring exporters.
     * @return a {@link Map} with the status of each exporter (true - running, false - stopped)
     * or an empty optional in case of error
     */
    public static Optional<Map<String, Boolean>> enableMonitoring() {
        String dbUser = Config.get().getString(ConfigDefaults.DB_USER);
        String dbPass = Config.get().getString(ConfigDefaults.DB_PASSWORD);
        Map<String, String> pillar = new HashMap<>();
        pillar.put("db_user", dbUser);
        pillar.put("db_pass", dbPass);
        String pillarJson = GSON.toJson(pillar);
        // started successfully (true) -> service state (true - running)
        return invokeMonitoringCtl("enable", Optional.of(pillarJson),
                new Tuple2<>("node",
                        "service_|-node_exporter_service_|-prometheus-node_exporter_|-running"),
                new Tuple2<>("postgres",
                        "service_|-postgres_exporter_service_|-prometheus-postgres_exporter_|-running"),
                new Tuple2<>("tomcat",
                        "service_|-jmx_exporter_tomcat_service_|-prometheus-jmx_exporter@tomcat_|-running"),
                new Tuple2<>("taskomatic",
                        "service_|-jmx_exporter_taskomatic_service_|-prometheus-jmx_exporter@taskomatic_|-running")
        );
    }

    /**
     * Disable monitoring exporters.
     * @return a {@link Map} with the status of each exporter (true - running, false - stopped)
     * or an empty optional in case of error
     */
    public static Optional<Map<String, Boolean>> disableMonitoring() {
        return invokeMonitoringCtl("disable", Optional.empty(),
                new Tuple2<>("node",
                        "service_|-node_exporter_service_|-prometheus-node_exporter_|-dead"),
                new Tuple2<>("postgres",
                        "service_|-postgres_exporter_service_|-prometheus-postgres_exporter_|-dead"),
                new Tuple2<>("tomcat",
                        "service_|-jmx_exporter_tomcat_service_|-prometheus-jmx_exporter@tomcat_|-dead"),
                new Tuple2<>("taskomatic",
                        "service_|-jmx_exporter_taskomatic_service_|-prometheus-jmx_exporter@taskomatic_|-dead")
                ).map(map -> {
                    // disabled successfully (true) -> service state (false - not running)
                    map.forEach((k, v) -> map.put(k, !v));
                    return map;
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
                    LOG.debug(MGR_MONITORING_CTL + " output: " + cmdOutput);

                    LocalResponse<Map<String, ModuleRun<JsonElement>>> jsonOut = GSON.fromJson(cmdOutput,
                            new TypeToken<LocalResponse<Map<String, ModuleRun<JsonElement>>>>() {
                            }.getType());

                    for (Tuple2<String, String> state : exporterStates) {
                        exporters.put(state.getA(),
                                isResultTrue(jsonOut.getLocal(), state.getB()));
                    }

                    return Optional.of(exporters);
                } catch (JsonParseException e) {
                    LOG.error("Error parsing JSON: " + e.getMessage());
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
        return Optional.ofNullable(result.get(key))
                .map(r -> r.isResult()).orElse(false);
    }

}
