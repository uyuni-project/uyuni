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

package com.suse.manager.webui.services.impl.test;

import com.redhat.rhn.testing.RhnJmockBaseTestCase;

import com.suse.manager.webui.services.impl.MonitoringService;

import java.io.InputStream;
import java.util.Optional;
import java.util.function.BiFunction;

public class MonitoringServiceTest extends RhnJmockBaseTestCase {

    public void testGetStatusWithTaskomaticEnableNeeded() {
        BiFunction<String, Optional<String>, Optional<InputStream>> execCtl =
                (String cmd, Optional<String> pillar) -> Optional.of(this.getClass()
                        .getResourceAsStream("/com/suse/manager/webui/services/impl/test/monitoring/status.json"));

        MonitoringService.setExecCtlFunction(execCtl);
        MonitoringService.setTomcatJmxStatusSupplier(() -> true);
        MonitoringService.setTaskomaticJmxStatusSupplier(() -> true);
        MonitoringService.setSelfMonitoringStatusSupplier(() -> true);

        Optional<MonitoringService.MonitoringStatus> res = MonitoringService.getStatus();
        assertTrue(res.isPresent());
        assertTrue(res.get().getExporters().get("node"));
        assertTrue(res.get().getExporters().get("postgres"));
        assertTrue(res.get().getExporters().get("tomcat"));
        assertFalse(res.get().getExporters().get("taskomatic"));
        assertTrue(res.get().getExporters().get("self_monitoring"));

        assertEquals(null, res.get().getMessages().get("tomcat"));
        assertEquals("restart", res.get().getMessages().get("taskomatic"));
        assertEquals(null, res.get().getMessages().get("self_monitoring"));
    }

    public void testGetStatusWithSelfMonitoringRestartNeeded() {
        String jsonFile = "/com/suse/manager/webui/services/impl/test/monitoring/status_self_monitoring_restart.json";
        BiFunction<String, Optional<String>, Optional<InputStream>> execCtl =
                (String cmd, Optional<String> pillar) -> Optional.of(this.getClass()
                        .getResourceAsStream(jsonFile));

        MonitoringService.setExecCtlFunction(execCtl);
        MonitoringService.setTomcatJmxStatusSupplier(() -> true);
        MonitoringService.setTaskomaticJmxStatusSupplier(() -> true);
        MonitoringService.setSelfMonitoringStatusSupplier(() -> false);

        Optional<MonitoringService.MonitoringStatus> res = MonitoringService.getStatus();
        assertTrue(res.isPresent());
        assertTrue(res.get().getExporters().get("node"));
        assertTrue(res.get().getExporters().get("postgres"));
        assertTrue(res.get().getExporters().get("tomcat"));
        assertFalse(res.get().getExporters().get("taskomatic"));
        assertTrue(res.get().getExporters().get("self_monitoring"));
        assertEquals(null, res.get().getMessages().get("tomcat"));
        assertEquals("restart", res.get().getMessages().get("taskomatic"));
        assertEquals("restart", res.get().getMessages().get("self_monitoring"));
    }

    public void testGetStatusWithTaskomaticRestartNeeded() {
        String jsonFile = "/com/suse/manager/webui/services/impl/test/monitoring/status_tasko_restart.json";
        BiFunction<String, Optional<String>, Optional<InputStream>> execCtl =
                (String cmd, Optional<String> pillar) -> Optional.of(this.getClass()
                        .getResourceAsStream(jsonFile));

        MonitoringService.setExecCtlFunction(execCtl);
        MonitoringService.setTomcatJmxStatusSupplier(() -> true);
        MonitoringService.setTaskomaticJmxStatusSupplier(() -> false);
        MonitoringService.setSelfMonitoringStatusSupplier(() -> true);

        Optional<MonitoringService.MonitoringStatus> res = MonitoringService.getStatus();
        assertTrue(res.isPresent());
        assertTrue(res.get().getExporters().get("node"));
        assertTrue(res.get().getExporters().get("postgres"));
        assertTrue(res.get().getExporters().get("tomcat"));
        assertTrue(res.get().getExporters().get("taskomatic"));
        assertTrue(res.get().getExporters().get("self_monitoring"));
        assertEquals(null, res.get().getMessages().get("tomcat"));
        assertEquals("restart", res.get().getMessages().get("taskomatic"));
    }

    public void testGetStatusNoMessage() {
        String jsonFile = "/com/suse/manager/webui/services/impl/test/monitoring/status_tasko_restart.json";
        BiFunction<String, Optional<String>, Optional<InputStream>> execCtl =
                (String cmd, Optional<String> pillar) -> Optional.of(this.getClass()
                        .getResourceAsStream(jsonFile));

        MonitoringService.setExecCtlFunction(execCtl);
        MonitoringService.setTomcatJmxStatusSupplier(() -> true);
        MonitoringService.setTaskomaticJmxStatusSupplier(() -> true);
        MonitoringService.setSelfMonitoringStatusSupplier(() -> true);

        Optional<MonitoringService.MonitoringStatus> res = MonitoringService.getStatus();
        assertTrue(res.isPresent());
        assertTrue(res.get().getExporters().get("node"));
        assertTrue(res.get().getExporters().get("postgres"));
        assertTrue(res.get().getExporters().get("tomcat"));
        assertTrue(res.get().getExporters().get("taskomatic"));
        assertTrue(res.get().getExporters().get("self_monitoring"));
        assertEquals(null, res.get().getMessages().get("tomcat"));
        assertEquals(null, res.get().getMessages().get("taskomatic"));
        assertEquals(null, res.get().getMessages().get("self_monitoring"));
    }

    public void testEnableWithTomcatRestartNeeded() {
        BiFunction<String, Optional<String>, Optional<InputStream>> execCtl =
                (String cmd, Optional<String> pillar) -> Optional.of(this.getClass()
                        .getResourceAsStream("/com/suse/manager/webui/services/impl/test/monitoring/enable.json"));

        MonitoringService.setExecCtlFunction(execCtl);
        MonitoringService.setTomcatJmxStatusSupplier(() -> false);
        MonitoringService.setTaskomaticJmxStatusSupplier(() -> true);
        MonitoringService.setSelfMonitoringStatusSupplier(() -> true);

        Optional<MonitoringService.MonitoringStatus> res = MonitoringService.enableMonitoring();
        assertTrue(res.isPresent());
        assertTrue(res.get().getExporters().get("node"));
        assertTrue(res.get().getExporters().get("postgres"));
        assertTrue(res.get().getExporters().get("tomcat"));
        assertTrue(res.get().getExporters().get("taskomatic"));
        assertTrue(res.get().getExporters().get("self_monitoring"));
        assertEquals("restart", res.get().getMessages().get("tomcat"));
        assertEquals(null, res.get().getMessages().get("taskomatic"));
    }

    public void testDisableWithTaskomaticRestartNeeded() {
        BiFunction<String, Optional<String>, Optional<InputStream>> execCtl =
                (String cmd, Optional<String> pillar) -> Optional.of(this.getClass()
                        .getResourceAsStream("/com/suse/manager/webui/services/impl/test/monitoring/disable.json"));

        MonitoringService.setExecCtlFunction(execCtl);
        MonitoringService.setTomcatJmxStatusSupplier(() -> false);
        MonitoringService.setTaskomaticJmxStatusSupplier(() -> true);
        MonitoringService.setSelfMonitoringStatusSupplier(() -> true);

        Optional<MonitoringService.MonitoringStatus> res = MonitoringService.disableMonitoring();
        assertTrue(res.isPresent());
        assertFalse(res.get().getExporters().get("node"));
        assertFalse(res.get().getExporters().get("postgres"));
        assertFalse(res.get().getExporters().get("tomcat"));
        assertFalse(res.get().getExporters().get("taskomatic"));
        assertFalse(res.get().getExporters().get("self_monitoring"));
        assertEquals(null, res.get().getMessages().get("tomcat"));
        assertEquals("restart", res.get().getMessages().get("taskomatic"));
        assertEquals("restart", res.get().getMessages().get("self_monitoring"));
    }

}
