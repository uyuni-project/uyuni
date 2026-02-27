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

import com.redhat.rhn.frontend.xmlrpc.api.ApiHandler;
import org.jmock.Expectations;
import org.junit.Test;

import java.util.Map;

public class ApiHandlerContractTest extends BaseOpenApiTest {

    @Override protected String getApiNamespace() { return "api"; }
    @Override protected Class<ApiHandler> getHandlerClass() { return ApiHandler.class; }

    private ApiHandler handler() {
        return (ApiHandler) handlerMock;
    }

    @Test
    public void testSystemVersion() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).systemVersion();
            will(returnValue("2026.01"));
        }});
        validateApiContract("/api/systemVersion", "GET").onHandlerMethod("systemVersion");
    }

    @Test
    public void testProductName() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).productName();
            will(returnValue("Uyuni"));
        }});
        validateApiContract("/api/productName", "GET").onHandlerMethod("productName");
    }

    @Test
    public void testGetVersion() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getVersion();
            will(returnValue("4.3.0"));
        }});
        validateApiContract("/api/getVersion", "GET").onHandlerMethod("getVersion");
    }

    @Test
    public void testGetApiNamespaces() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getApiNamespaces();
            will(returnValue(Map.of("api", "ApiHandler")));
        }});
        validateApiContract("/api/getApiNamespaces", "GET").onHandlerMethod("getApiNamespaces");
    }

    @Test
    public void testGetApiCallList() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getApiCallList();
            will(returnValue(Map.of("api", Map.of())));
        }});
        validateApiContract("/api/getApiCallList", "GET").onHandlerMethod("getApiCallList");
    }

    @Test
    public void testGetApiNamespaceCallList() throws Exception {
        var namespace = "system";
        context.checking(new Expectations() {{
            oneOf(handler()).getApiNamespaceCallList(with(namespace));
            will(returnValue(Map.of()));
        }});

        validateApiContract("/api/getApiNamespaceCallList", "GET")
                .withParams(Map.of("namespace", new String[]{namespace}))
                .onHandlerMethod("getApiNamespaceCallList", String.class);
    }
}
