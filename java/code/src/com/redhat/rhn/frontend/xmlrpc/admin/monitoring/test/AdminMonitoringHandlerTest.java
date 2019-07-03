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
        BiFunction<String, Optional<String>, Optional<InputStream>> execCtl =
                (String cmd, Optional<String> pillar) -> {
                    return Optional.of(this.getClass()
                            .getResourceAsStream("/com/suse/manager/webui/services/impl/test/monitoring/status.json"));
                };

        MonitoringService.setExecCtlFunction(execCtl);

        AdminMonitoringHandler handler = new AdminMonitoringHandler();

        Map<String, Boolean> res = handler.getStatus(satAdmin);
        assertTrue(res.get("node"));
        assertTrue(res.get("postgres"));
        assertTrue(res.get("tomcat"));
        assertTrue(res.get("taskomatic"));
    }

    public void testEnable() {
        BiFunction<String, Optional<String>, Optional<InputStream>> execCtl =
                (String cmd, Optional<String> pillar) -> {
                    return Optional.of(this.getClass()
                            .getResourceAsStream("/com/suse/manager/webui/services/impl/test/monitoring/enable.json"));
                };

        MonitoringService.setExecCtlFunction(execCtl);

        AdminMonitoringHandler handler = new AdminMonitoringHandler();

        Map<String, Boolean> res = handler.enable(satAdmin);
        assertTrue(res.get("node"));
        assertTrue(res.get("postgres"));
        assertTrue(res.get("tomcat"));
        assertTrue(res.get("taskomatic"));
    }

    public void testDisable() {
        BiFunction<String, Optional<String>, Optional<InputStream>> execCtl =
                (String cmd, Optional<String> pillar) -> {
                    return Optional.of(this.getClass()
                            .getResourceAsStream("/com/suse/manager/webui/services/impl/test/monitoring/disable.json"));
                };

        MonitoringService.setExecCtlFunction(execCtl);

        AdminMonitoringHandler handler = new AdminMonitoringHandler();

        Map<String, Boolean> res = handler.enable(satAdmin);
        assertFalse(res.get("node"));
        assertFalse(res.get("postgres"));
        assertFalse(res.get("tomcat"));
        assertFalse(res.get("taskomatic"));
    }

    public void testRoleCheck() {
        AdminMonitoringHandler handler = new AdminMonitoringHandler();

        try {
            Map<String, Boolean> res = handler.enable(regular);
            fail("PermissionCheckFailureException should be thrown");
        } catch (PermissionCheckFailureException e) {
        }

        try {
            Map<String, Boolean> res = handler.enable(admin);
            fail("PermissionCheckFailureException should be thrown");
        } catch (PermissionCheckFailureException e) {
        }
    }
}
