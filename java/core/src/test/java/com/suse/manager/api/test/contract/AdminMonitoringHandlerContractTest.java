/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.api.test.contract;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.admin.monitoring.AdminMonitoringHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class AdminMonitoringHandlerContractTest extends BaseOpenApiTest {

    @Override
    protected String getApiNamespace() {
        return "admin.monitoring";
    }

    @Override
    protected Class<AdminMonitoringHandler> getHandlerClass() {
        return AdminMonitoringHandler.class;
    }

    private AdminMonitoringHandler handler() {
        return (AdminMonitoringHandler) handlerMock;
    }

    private Map<String, String> exporters(String status) {
        return Map.of(
                "node", status,
                "tomcat", status,
                "taskomatic", status,
                "postgres", status,
                "self_monitoring", status
        );
    }

    @Test
    public void testEnable() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).enable(with(mockUser));
            will(returnValue(exporters("enabled")));
        }});

        validateApiContract("/admin.monitoring/enable", "POST")
                .onHandlerMethod("enable", User.class);
    }

    @Test
    public void testDisable() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).disable(with(mockUser));
            will(returnValue(exporters("disabled")));
        }});

        validateApiContract("/admin.monitoring/disable", "POST")
                .onHandlerMethod("disable", User.class);
    }

    @Test
    public void testGetStatus() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getStatus(with(mockUser));
            will(returnValue(exporters("enabled:restart_needed")));
        }});

        validateApiContract("/admin.monitoring/getStatus", "GET")
                .onHandlerMethod("getStatus", User.class);
    }
}
