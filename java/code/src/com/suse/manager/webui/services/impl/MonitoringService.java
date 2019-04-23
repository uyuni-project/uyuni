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
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

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
     * @return a {@link Map} with the result of enabling each exporter or an empty optional in case of error
     */
    public static Optional<Map<String, Boolean>> enableMonitoring() {
        String dbUser = Config.get().getString(ConfigDefaults.DB_USER);
        String dbPass = Config.get().getString(ConfigDefaults.DB_PASSWORD);
        Map<String, String> pillar = new HashMap<>();
        pillar.put("db_user", dbUser);
        pillar.put("db_pass", dbPass);
        String pillarJson = GSON.toJson(pillar);
        return invokeMonitoringCtl("enable", Optional.of(pillarJson),
                new Tuple2<>("node",
                        "service_|-node_exporter_service_|-prometheus-node_exporter_|-running"),
                new Tuple2<>("postgres",
                        "service_|-postgres_exporter_service_|-postgres-exporter_|-running"),
                new Tuple2<>("tomcat",
                        "service_|-jmx_exporter_tomcat_service_|-jmx-exporter@tomcat_|-running"),
                new Tuple2<>("taskomatic",
                        "service_|-jmx_exporter_taskomatic_service_|-jmx-exporter@taskomatic_|-running")
        );
    }

    /**
     * Disable monitoring exporters.
     * @return a {@link Map} with the result of disabling each exporter or an empty optional in case of error
     */
    public static Optional<Map<String, Boolean>> disableMonitoring() {
        return invokeMonitoringCtl("disable", Optional.empty(),
                new Tuple2<>("node",
                        "service_|-node_exporter_service_|-prometheus-node_exporter_|-dead"),
                new Tuple2<>("postgres",
                        "service_|-postgres_exporter_service_|-prometheus-postgres_exporter_|-dead"),
                new Tuple2<>("tomcat",
                        "service_|-jmx_exporter_tomcat_service_|-jmx-exporter@tomcat_|-dead"),
                new Tuple2<>("taskomatic",
                        "service_|-jmx_exporter_taskomatic_service_|-jmx-exporter@taskomatic_|-dead")
                );
    }

    private static Optional<Map<String, Boolean>> invokeMonitoringCtl(String cmd, Optional<String> pillar,
                                                                      Tuple2<String, String>... exporterStates) {
        try {
            Process process = new ProcessBuilder().command("/usr/bin/sudo",
                    MGR_MONITORING_CTL, cmd, pillar.orElse(""))
                    .start();
            boolean exited = process.waitFor(5, TimeUnit.MINUTES);
            if (!exited) {
                LOG.error("Timeout waiting for " + MGR_MONITORING_CTL + " to complete");
                return Optional.empty();
            }
            Map<String, Boolean> exporters = new HashMap<>();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String cmdOutput = IOUtils.toString(reader);
                LOG.debug(MGR_MONITORING_CTL + " output:" + cmdOutput);
                LocalResponse<Map<String, ModuleRun<JsonElement>>> jsonOut = GSON.fromJson(cmdOutput,
                        new TypeToken<LocalResponse<Map<String, ModuleRun<JsonElement>>>>() { }.getType());

                for (Tuple2<String, String> state: exporterStates) {
                    exporters.put(state.getA(),
                            isResultTrue(jsonOut.getLocal(), state.getB()));
                }

                return Optional.of(exporters);
            }
            catch (JsonParseException e) {
                LOG.error("Error parsing JSON: " + e.getMessage());
                return Optional.empty();
            }
        }
        catch (IOException e) {
            LOG.error("Error invoking " + MGR_MONITORING_CTL, e);
            return Optional.empty();
        }
        catch (InterruptedException e) {
            LOG.error("Waiting for " + MGR_MONITORING_CTL + " was interrupted", e);
            return Optional.empty();
        }
    }


    private static boolean isResultTrue(Map<String, ModuleRun<JsonElement>> result, String key) {
        return Optional.ofNullable(result.get(key))
                .map(r -> r.isResult()).orElse(false);
    }

}
