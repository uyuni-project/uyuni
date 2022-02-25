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

package com.redhat.rhn.frontend.xmlrpc.admin.monitoring.test;

import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.frontend.xmlrpc.admin.monitoring.AdminMonitoringHandler;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;

import com.suse.manager.webui.services.impl.MonitoringService;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class AdminMonitoringHandlerTest extends BaseHandlerTestCase {

    public void testGetStatus() {
        String monitoringRestartFile =
                "/com/suse/manager/webui/services/impl/test/monitoring/status_self_monitoring_restart.json";
        BiFunction<String, Optional<String>, Optional<InputStream>> execCtl =
                (String cmd, Optional<String> pillar) -> Optional.of(
                        this.getClass().getResourceAsStream(monitoringRestartFile));

        MonitoringService.setExecCtlFunction(execCtl);
        MonitoringService.setTomcatJmxStatusSupplier(() -> true);
        MonitoringService.setTaskomaticJmxStatusSupplier(() -> true);
        MonitoringService.setSelfMonitoringStatusSupplier(() -> true);

        AdminMonitoringHandler handler = new AdminMonitoringHandler();

        Map<String, String> res = handler.getStatus(satAdmin);
        assertEquals("enabled", res.get("node"));
        assertEquals("enabled", res.get("postgres"));
        assertEquals("enabled", res.get("tomcat"));
        assertEquals("disabled:restart_needed", res.get("taskomatic"));
        assertEquals("enabled", res.get("self_monitoring"));
    }

    public void testEnable() {
        BiFunction<String, Optional<String>, Optional<InputStream>> execCtl =
                (String cmd, Optional<String> pillar) -> Optional.of(this.getClass()
                        .getResourceAsStream("/com/suse/manager/webui/services/impl/test/monitoring/enable.json"));

        MonitoringService.setExecCtlFunction(execCtl);
        MonitoringService.setTomcatJmxStatusSupplier(() -> true);
        MonitoringService.setTaskomaticJmxStatusSupplier(() -> true);
        MonitoringService.setSelfMonitoringStatusSupplier(() -> false);

        AdminMonitoringHandler handler = new AdminMonitoringHandler();

        Map<String, String> res = handler.enable(satAdmin);
        assertEquals("enabled", res.get("node"));
        assertEquals("enabled", res.get("postgres"));
        assertEquals("enabled", res.get("tomcat"));
        assertEquals("enabled", res.get("taskomatic"));
        assertEquals("enabled:restart_needed", res.get("self_monitoring"));
    }

    public void testDisable() {
        BiFunction<String, Optional<String>, Optional<InputStream>> execCtl =
                (String cmd, Optional<String> pillar) -> Optional.of(this.getClass()
                        .getResourceAsStream("/com/suse/manager/webui/services/impl/test/monitoring/disable.json"));

        MonitoringService.setExecCtlFunction(execCtl);
        MonitoringService.setTomcatJmxStatusSupplier(() -> true);
        MonitoringService.setTaskomaticJmxStatusSupplier(() -> true);
        MonitoringService.setSelfMonitoringStatusSupplier(() -> true);

        AdminMonitoringHandler handler = new AdminMonitoringHandler();

        Map<String, String> res = handler.disable(satAdmin);
        assertEquals("disabled", res.get("node"));
        assertEquals("disabled", res.get("postgres"));
        assertEquals("disabled:restart_needed", res.get("tomcat"));
        assertEquals("disabled:restart_needed", res.get("taskomatic"));
        assertEquals("disabled:restart_needed", res.get("self_monitoring"));
    }

    public void testRoleCheck() {
        AdminMonitoringHandler handler = new AdminMonitoringHandler();

        try {
            Map<String, String> res = handler.enable(regular);
            fail("PermissionCheckFailureException should be thrown");
        }
        catch (PermissionCheckFailureException e) {
        }

        try {
            Map<String, String> res = handler.enable(admin);
            fail("PermissionCheckFailureException should be thrown");
        }
        catch (PermissionCheckFailureException e) {
        }
    }
}
