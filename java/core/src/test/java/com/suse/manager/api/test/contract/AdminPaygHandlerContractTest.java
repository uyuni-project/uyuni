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

import com.redhat.rhn.domain.cloudpayg.PaygSshData;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.xmlrpc.admin.AdminPaygHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class AdminPaygHandlerContractTest extends BaseOpenApiTest {

    @Override
    protected String getApiNamespace() {
        return "admin.payg";
    }

    @Override
    protected Class<AdminPaygHandler> getHandlerClass() {
        return AdminPaygHandler.class;
    }

    private AdminPaygHandler handler() {
        return (AdminPaygHandler) handlerMock;
    }

    /**
     * Builds a PaygSshData serialized by the registered PaygSshDataSerializer, so the
     * response is validated against the documented snake_case schema.
     */
    private PaygSshData paygSshData() {
        var data = new PaygSshData();
        data.setDescription("test instance");
        data.setHost("payg.example.com");
        data.setPort(22);
        data.setUsername("root");
        data.setBastionHost("bastion.example.com");
        data.setBastionPort(2222);
        data.setBastionUsername("bastion");
        return data;
    }

    @Test
    public void testCreate() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).create(with(mockUser), with("test instance"), with("payg.example.com"), with(22),
                    with("root"), with("secret"), with("private-key"), with("key-secret"),
                    with("bastion.example.com"), with(2222), with("bastion"), with("bastion-secret"),
                    with("bastion-key"), with("bastion-key-secret"));
            will(returnValue(1));
        }});

        validateApiContract("/admin.payg/create", "POST")
                .withBody(Map.ofEntries(
                        Map.entry("description", "test instance"),
                        Map.entry("host", "payg.example.com"),
                        Map.entry("port", 22),
                        Map.entry("username", "root"),
                        Map.entry("password", "secret"),
                        Map.entry("key", "private-key"),
                        Map.entry("keyPassword", "key-secret"),
                        Map.entry("bastionHost", "bastion.example.com"),
                        Map.entry("bastionPort", 2222),
                        Map.entry("bastionUsername", "bastion"),
                        Map.entry("bastionPassword", "bastion-secret"),
                        Map.entry("bastionKey", "bastion-key"),
                        Map.entry("bastionKeyPassword", "bastion-key-secret")))
                .onHandlerMethod("create", User.class, String.class, String.class, Integer.class, String.class,
                        String.class, String.class, String.class, String.class, Integer.class, String.class,
                        String.class, String.class, String.class);
    }

    @Test
    public void testSetDetails() throws Exception {
        var details = Map.<String, Object>of("description", "updated", "port", 22, "username", "root");

        context.checking(new Expectations() {{
            oneOf(handler()).setDetails(with(mockUser), with("payg.example.com"), with(details));
            will(returnValue(1));
        }});

        validateApiContract("/admin.payg/setDetails", "POST")
                .withBody(Map.of("host", "payg.example.com", "details", details))
                .onHandlerMethod("setDetails", User.class, String.class, Map.class);
    }

    @Test
    public void testList() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).list(with(mockUser));
            will(returnValue(List.of(paygSshData())));
        }});

        validateApiContract("/admin.payg/list", "POST")
                .onHandlerMethod("list", User.class);
    }

    @Test
    public void testGetDetails() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getDetails(with(mockUser), with("payg.example.com"));
            will(returnValue(paygSshData()));
        }});

        validateApiContract("/admin.payg/getDetails", "POST")
                .withBody(Map.of("host", "payg.example.com"))
                .onHandlerMethod("getDetails", User.class, String.class);
    }

    @Test
    public void testDelete() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).delete(with(mockUser), with("payg.example.com"));
            will(returnValue(1));
        }});

        validateApiContract("/admin.payg/delete", "POST")
                .withBody(Map.of("host", "payg.example.com"))
                .onHandlerMethod("delete", User.class, String.class);
    }
}
