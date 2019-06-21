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

package com.redhat.rhn.frontend.xmlrpc.admin.monitoring;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.satellite.MonitoringException;
import com.suse.manager.webui.services.impl.MonitoringService;

import java.util.Map;
import java.util.Optional;

/**
 * AdminMonitoringHandler
 * @xmlrpc.namespace admin.monitoring
 * @xmlrpc.doc Provides methods to manage the monitoring of the Uyuni server.
 */
public class AdminMonitoringHandler extends BaseHandler {

    /**
     * Enable monitoring.
     * @param loggedInUser the current user
     * @return a map with the status of each exporter
     *
     * @xmlrpc.doc Enable monitoring.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.returntype
     *  #array()
     *      #struct("Exporters")
     *          #prop("boolean", "node")
     *          #prop("boolean", "tomcat")
     *          #prop("boolean", "taskomatic")
     *          #prop("boolean", "postgres")
     *      #struct_end()
     *  #array_end()
     */
    public Map<String, Boolean> enable(User loggedInUser) {
        ensureSatAdmin(loggedInUser);
        Optional<Map<String, Boolean>> exporters = MonitoringService.enableMonitoring();
        return exporters.orElseThrow(() -> new MonitoringException("Error enabling server monitoring"));
    }

    /**
     * Disable monitoring.
     * @param loggedInUser the current user
     * @return a map with the status of each exporter
     *
     * @xmlrpc.doc Disable monitoring.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.returntype
     *  #array()
     *      #struct("Exporters")
     *          #prop("boolean", "node")
     *          #prop("boolean", "tomcat")
     *          #prop("boolean", "taskomatic")
     *          #prop("boolean", "postgres")
     *      #struct_end()
     *  #array_end()
     */
    public Map<String, Boolean> disable(User loggedInUser) {
        ensureSatAdmin(loggedInUser);
        Optional<Map<String, Boolean>> exporters = MonitoringService.disableMonitoring();
        return exporters.orElseThrow(() -> new MonitoringException("Error disabling server monitoring"));
    }

    /**
     * Get the status of each Prometheus exporter.
     * @param loggedInUser the current user
     * @return a map with the status of each exporter
     *
     * @xmlrpc.doc Get the status of each Prometheus exporter.
     * @xmlrpc.param #param("string", "sessionKey")
     * @xmlrpc.returntype
     *  #array()
     *      #struct("Exporters")
     *          #prop("boolean", "node")
     *          #prop("boolean", "tomcat")
     *          #prop("boolean", "taskomatic")
     *          #prop("boolean", "postgres")
     *      #struct_end()
     *  #array_end()
     */
    public Map<String, Boolean> getStatus(User loggedInUser) {
        ensureSatAdmin(loggedInUser);
        Optional<Map<String, Boolean>> exporters = MonitoringService.getStatus();
        return exporters.orElseThrow(() -> new MonitoringException("Error getting server monitoring status"));
    }

}
