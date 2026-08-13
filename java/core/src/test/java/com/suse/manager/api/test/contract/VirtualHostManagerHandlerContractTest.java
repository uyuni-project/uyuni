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

import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.virtualhostmanager.VirtualHostManagerHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class VirtualHostManagerHandlerContractTest extends BaseOpenApiTest {

    private static final String LABEL = "test-vhm";
    private static final String MODULE_NAME = "VMware";

    @Override
    protected String getApiNamespace() {
        return "virtualhostmanager";
    }

    @Override
    protected Class<VirtualHostManagerHandler> getHandlerClass() {
        return VirtualHostManagerHandler.class;
    }

    private VirtualHostManagerHandler handler() {
        return (VirtualHostManagerHandler) handlerMock;
    }

    private VirtualHostManager virtualHostManager() {
        Org org = new Org();
        org.setId(1L);

        VirtualHostManager vhm = new VirtualHostManager();
        vhm.setLabel(LABEL);
        vhm.setOrg(org);
        vhm.setGathererModule(MODULE_NAME);
        return vhm;
    }

    @Test
    public void testListVirtualHostManagers() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listVirtualHostManagers(with(mockUser));
            will(returnValue(List.of(virtualHostManager())));
        }});

        validateApiContract("/virtualhostmanager/listVirtualHostManagers", "GET")
                .onHandlerMethod("listVirtualHostManagers", User.class);
    }

    @Test
    public void testGetDetail() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getDetail(with(mockUser), with(LABEL));
            will(returnValue(virtualHostManager()));
        }});

        validateApiContract("/virtualhostmanager/getDetail", "GET")
                .withParams(Map.of("label", new String[]{LABEL}))
                .onHandlerMethod("getDetail", User.class, String.class);
    }

    @Test
    public void testListAvailableVirtualHostGathererModules() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listAvailableVirtualHostGathererModules(with(mockUser));
            will(returnValue(List.of(MODULE_NAME, "Kubernetes")));
        }});

        validateApiContract("/virtualhostmanager/listAvailableVirtualHostGathererModules", "GET")
                .onHandlerMethod("listAvailableVirtualHostGathererModules", User.class);
    }

    @Test
    public void testGetModuleParameters() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).getModuleParameters(with(mockUser), with(MODULE_NAME));
            will(returnValue(Map.of("hostname", "", "username", "", "password", "")));
        }});

        validateApiContract("/virtualhostmanager/getModuleParameters", "GET")
                .withParams(Map.of("moduleName", new String[]{MODULE_NAME}))
                .onHandlerMethod("getModuleParameters", User.class, String.class);
    }

    @Test
    public void testCreate() throws Exception {
        Map<String, String> parameters = Map.of("hostname", "vcenter.example.com", "username", "admin");

        context.checking(new Expectations() {{
            oneOf(handler()).create(with(mockUser), with(LABEL), with(MODULE_NAME), with(parameters));
            will(returnValue(1));
        }});

        validateApiContract("/virtualhostmanager/create", "POST")
                .withBody(Map.of("label", LABEL, "moduleName", MODULE_NAME, "parameters", parameters))
                .onHandlerMethod("create", User.class, String.class, String.class, Map.class);
    }

    @Test
    public void testDelete() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).delete(with(mockUser), with(LABEL));
            will(returnValue(1));
        }});

        validateApiContract("/virtualhostmanager/delete", "POST")
                .withBody(Map.of("label", LABEL))
                .onHandlerMethod("delete", User.class, String.class);
    }
}
