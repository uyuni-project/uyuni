/**
 * Copyright (c) 2019 SUSE LLC
 * <p>
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 * <p>
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.manager.webui.services.impl.test;

import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.suse.manager.webui.services.impl.MonitoringService;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

public class MonitoringServiceTest extends JMockBaseTestCaseWithUser {

    public void testGetStatus() {
        BiFunction<String, Optional<String>, Optional<InputStream>> execCtl =
                (String cmd, Optional<String> pillar) -> {
                    return Optional.of(this.getClass()
                            .getResourceAsStream("/com/suse/manager/webui/services/impl/test/monitoring/status.json"));
                };

        MonitoringService.setExecCtlFunction(execCtl);

        Optional<Map<String, Boolean>> res = MonitoringService.getStatus();
        assertTrue(res.isPresent());
        assertTrue(res.get().get("node"));
        assertTrue(res.get().get("postgres"));
        assertTrue(res.get().get("tomcat"));
        assertTrue(res.get().get("taskomatic"));
    }

    public void testEnable() {
        BiFunction<String, Optional<String>, Optional<InputStream>> execCtl =
                (String cmd, Optional<String> pillar) -> {
                    return Optional.of(this.getClass()
                            .getResourceAsStream("/com/suse/manager/webui/services/impl/test/monitoring/enable.json"));
                };

        MonitoringService.setExecCtlFunction(execCtl);

        Optional<Map<String, Boolean>> res = MonitoringService.enableMonitoring();
        assertTrue(res.isPresent());
        assertTrue(res.get().get("node"));
        assertTrue(res.get().get("postgres"));
        assertTrue(res.get().get("tomcat"));
        assertTrue(res.get().get("taskomatic"));
    }

    public void testDisable() {
        BiFunction<String, Optional<String>, Optional<InputStream>> execCtl =
                (String cmd, Optional<String> pillar) -> {
                    return Optional.of(this.getClass()
                            .getResourceAsStream("/com/suse/manager/webui/services/impl/test/monitoring/disable.json"));
                };

        MonitoringService.setExecCtlFunction(execCtl);

        Optional<Map<String, Boolean>> res = MonitoringService.enableMonitoring();
        assertTrue(res.isPresent());
        assertFalse(res.get().get("node"));
        assertFalse(res.get().get("postgres"));
        assertFalse(res.get().get("tomcat"));
        assertFalse(res.get().get("taskomatic"));
    }

}
