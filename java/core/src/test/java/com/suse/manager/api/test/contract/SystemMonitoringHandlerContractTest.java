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

import com.redhat.rhn.domain.dto.EndpointInfo;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.system.monitoring.SystemMonitoringHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class SystemMonitoringHandlerContractTest extends BaseOpenApiTest {

    private static final Integer SYSTEM_ID = 1000010000;

    @Override
    protected String getApiNamespace() {
        return "system.monitoring";
    }

    @Override
    protected Class<SystemMonitoringHandler> getHandlerClass() {
        return SystemMonitoringHandler.class;
    }

    private SystemMonitoringHandler handler() {
        return (SystemMonitoringHandler) handlerMock;
    }

    @Test
    public void testListEndpoints() throws Exception {
        EndpointInfo endpoint = new EndpointInfo(SYSTEM_ID.longValue(), "node-exporter", "node", 9100,
                "default", "/metrics", true);

        context.checking(new Expectations() {{
            oneOf(handler()).listEndpoints(with(mockUser), with(any(List.class)));
            will(returnValue(List.of(endpoint)));
        }});

        validateApiContract("/system.monitoring/listEndpoints", "GET")
                .withParams(Map.of("sids", new String[] {SYSTEM_ID.toString()}))
                .onHandlerMethod("listEndpoints", User.class, List.class);
    }
}
