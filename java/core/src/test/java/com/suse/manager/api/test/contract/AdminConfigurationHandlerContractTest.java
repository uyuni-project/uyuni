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
import com.redhat.rhn.frontend.xmlrpc.admin.configuration.AdminConfigurationHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class AdminConfigurationHandlerContractTest extends BaseOpenApiTest {

    @Override
    protected String getApiNamespace() {
        return "admin.configuration";
    }

    @Override
    protected Class<AdminConfigurationHandler> getHandlerClass() {
        return AdminConfigurationHandler.class;
    }

    private AdminConfigurationHandler handler() {
        return (AdminConfigurationHandler) handlerMock;
    }

    @Test
    public void testConfigure() throws Exception {
        var content = Map.<String, Object>of("mgr_server_hostname", "manager.example.com");

        context.checking(new Expectations() {{
            oneOf(handler()).configure(with(mockUser), with(content));
            will(returnValue(1));
        }});

        validateApiContract("/admin.configuration/configure", "POST")
                .withBody(Map.of("content", content))
                .onHandlerMethod("configure", User.class, Map.class);
    }
}
