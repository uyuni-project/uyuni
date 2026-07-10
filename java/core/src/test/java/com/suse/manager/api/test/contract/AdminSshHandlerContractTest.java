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
import com.redhat.rhn.frontend.xmlrpc.admin.ssh.AdminSshHandler;

import org.jmock.Expectations;
import org.junit.Test;

import java.util.Map;

public class AdminSshHandlerContractTest extends BaseOpenApiTest {

    @Override
    protected String getApiNamespace() {
        return "admin.ssh";
    }

    @Override
    protected Class<AdminSshHandler> getHandlerClass() {
        return AdminSshHandler.class;
    }

    private AdminSshHandler handler() {
        return (AdminSshHandler) handlerMock;
    }

    @Test
    public void testRemoveKnownHost() throws Exception {
        var hostname = "minion.example.com";
        var port = 22;

        context.checking(new Expectations() {{
            oneOf(handler()).removeKnownHost(with(mockUser), with(hostname), with(port));
            will(returnValue(1));
        }});

        validateApiContract("/admin.ssh/removeKnownHost", "POST")
                .withBody(Map.of("hostname", hostname, "port", port))
                .onHandlerMethod("removeKnownHost", User.class, String.class, Integer.class);
    }
}
