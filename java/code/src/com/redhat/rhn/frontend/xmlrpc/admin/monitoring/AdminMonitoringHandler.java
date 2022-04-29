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

package com.redhat.rhn.frontend.xmlrpc.admin.monitoring;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.satellite.MonitoringException;

import com.suse.manager.api.ReadOnly;
import com.suse.manager.webui.services.impl.MonitoringService;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * AdminMonitoringHandler
 * @xmlrpc.namespace admin.monitoring
 * @xmlrpc.doc Provides methods to manage the monitoring of the #product() server.
 */
public class AdminMonitoringHandler extends BaseHandler {

    private static Map<String, String> messageMap;
    {
        messageMap = new HashMap<>();
        messageMap.put("enable", "enable_again_to_sync_config");
        messageMap.put("disable", "disable_again_to_sync_config");
        messageMap.put("restart", "restart_needed");
    }

    private static String toString(boolean enabled, String msg) {
        String str = enabled ? "enabled" : "disabled";
        if (StringUtils.isNotEmpty(msg)) {
            return str + ":" + Optional.ofNullable(messageMap.get(msg)).orElse(msg);
        }
        return str;
    }

    private Map<String, String> toResponse(MonitoringService.MonitoringStatus status) {
        return status.getExporters().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> toString(e.getValue(), status.getMessages().get(e.getKey()))));
    }

    /**
     * Enable monitoring.
     * @param loggedInUser the current user
     * @return a map with the status of each exporter
     *
     * @xmlrpc.doc Enable monitoring.
     * @xmlrpc.param #session_key()
     * @xmlrpc.returntype
     *  #return_array_begin()
     *      #struct_begin("Exporters")
     *          #prop("string", "node")
     *          #prop("string", "tomcat")
     *          #prop("string", "taskomatic")
     *          #prop("string", "postgres")
     *          #prop("string", "self_monitoring")
     *      #struct_end()
     *  #array_end()
     */
    public Map<String, String> enable(User loggedInUser) {
        ensureSatAdmin(loggedInUser);
        MonitoringService.MonitoringStatus status = MonitoringService.enableMonitoring()
                .orElseThrow(() -> new MonitoringException("Error enabling server monitoring"));
        return toResponse(status);
    }



    /**
     * Disable monitoring.
     * @param loggedInUser the current user
     * @return a map with the status of each exporter
     *
     * @xmlrpc.doc Disable monitoring.
     * @xmlrpc.param #session_key()
     * @xmlrpc.returntype
     *  #return_array_begin()
     *      #struct_begin("Exporters")
     *          #prop("string", "node")
     *          #prop("string", "tomcat")
     *          #prop("string", "taskomatic")
     *          #prop("string", "postgres")
     *          #prop("string", "self_monitoring")
     *      #struct_end()
     *  #array_end()
     */
    public Map<String, String> disable(User loggedInUser) {
        ensureSatAdmin(loggedInUser);
        MonitoringService.MonitoringStatus status = MonitoringService.disableMonitoring()
                .orElseThrow(() -> new MonitoringException("Error disabling server monitoring"));
        return toResponse(status);
    }

    /**
     * Get the status of each Prometheus exporter.
     * @param loggedInUser the current user
     * @return a map with the status of each exporter
     *
     * @xmlrpc.doc Get the status of each Prometheus exporter.
     * @xmlrpc.param #session_key()
     * @xmlrpc.returntype
     *  #return_array_begin()
     *      #struct_begin("Exporters")
     *          #prop("string", "node")
     *          #prop("string", "tomcat")
     *          #prop("string", "taskomatic")
     *          #prop("string", "postgres")
     *          #prop("string", "self_monitoring")
     *      #struct_end()
     *  #array_end()
     */
    @ReadOnly
    public Map<String, String> getStatus(User loggedInUser) {
        ensureSatAdmin(loggedInUser);
        MonitoringService.MonitoringStatus status = MonitoringService.getStatus()
                .orElseThrow(() -> new MonitoringException("Error getting server monitoring status"));
        return toResponse(status);
    }

}
